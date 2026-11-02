package ph.resq.app.core

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

data class QueuedMeshPacket(
    val rowId: Long,
    val messageId: String,
    val packet: ByteArray,
    val createdAt: Long,
    val expiresAt: Long,
    val attempts: Int,
    val maxAttempts: Int,
    val status: String
)

/**
 * Durable encrypted mesh outbox and replay cache. This supplies queue/ACK primitives only; it does
 * not claim BLE/Wi-Fi peer authentication, radio delivery, or automatic retries without a transport.
 */
class DurableMeshQueue(private val database: ResQDatabase, private val cipher: EncryptedFieldCipher = EncryptedFieldCipher()) {
    companion object {
        private const val MAX_PACKET_BYTES = 65_536
        private const val MAX_AGE_MS = 86_400_000L
        private const val MAX_SEEN = 10_000
        private val ACK_PREFIX = "PH-RESQ-ACK-v1|"

        fun createAckMac(messageId: String, peerToken: String, sharedKey: ByteArray): ByteArray {
            require(messageId.isNotBlank() && messageId.length <= 64 && peerToken.isNotBlank() && peerToken.length <= 64)
            require(sharedKey.size >= 32) { "ACK key must be at least 256 bits." }
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(sharedKey, "HmacSHA256"))
            return mac.doFinal((ACK_PREFIX + messageId + "|" + peerToken).toByteArray(Charsets.UTF_8))
        }
    }

    fun enqueue(envelope: MeshEnvelope, sharedKey: ByteArray, maxAttempts: Int = 5, now: Long = System.currentTimeMillis()): Boolean {
        require(maxAttempts in 1..20)
        require(envelope.issuedAtMillis <= now + 300_000L && now - envelope.issuedAtMillis <= MAX_AGE_MS)
        val packet = MeshEnvelopeCodec.encode(envelope, sharedKey)
        require(packet.size <= MAX_PACKET_BYTES)
        val values = ContentValues().apply {
            put("message_id", envelope.messageId)
            put("packet_ciphertext", cipher.encrypt(Base64.encodeToString(packet, Base64.NO_WRAP)))
            put("created_at", now)
            put("expires_at", minOf(envelope.issuedAtMillis + MAX_AGE_MS, now + MAX_AGE_MS))
            put("attempts", 0)
            put("max_attempts", maxAttempts)
            put("status", "QUEUED")
        }
        return database.writableDatabase.insertWithOnConflict("mesh_queue", null, values, SQLiteDatabase.CONFLICT_IGNORE) != -1L
    }

    fun pending(limit: Int = 50, now: Long = System.currentTimeMillis()): List<QueuedMeshPacket> {
        val db = database.writableDatabase
        db.execSQL("UPDATE mesh_queue SET status = 'EXPIRED' WHERE status NOT IN ('ACKED','FAILED','EXPIRED') AND expires_at < ?", arrayOf(now))
        val safeLimit = limit.coerceIn(1, 100)
        val out = mutableListOf<QueuedMeshPacket>()
        db.rawQuery("SELECT id,message_id,packet_ciphertext,created_at,expires_at,attempts,max_attempts,status FROM mesh_queue WHERE status IN ('QUEUED','RETRY') AND attempts < max_attempts ORDER BY id ASC LIMIT $safeLimit", null).use { c ->
            while (c.moveToNext()) {
                val packed = cipher.decrypt(c.getString(2)) ?: continue
                val packet = Base64.decode(packed, Base64.NO_WRAP)
                if (packet.size !in 1..MAX_PACKET_BYTES) continue
                out.add(QueuedMeshPacket(c.getLong(0), c.getString(1), packet, c.getLong(3), c.getLong(4), c.getInt(5), c.getInt(6), c.getString(7)))
            }
        }
        return out
    }

    fun markSent(rowId: Long): Boolean {
        val values = ContentValues().apply { put("status", "SENT"); putNull("last_error") }
        return database.writableDatabase.update("mesh_queue", values, "id = ? AND status IN ('QUEUED','RETRY')", arrayOf(rowId.toString())) == 1
    }

    fun markAttempt(rowId: Long, error: String? = null): String {
        val db = database.writableDatabase
        db.beginTransaction()
        try {
            var status = "FAILED"
            db.rawQuery("SELECT attempts,max_attempts,expires_at,status FROM mesh_queue WHERE id = ?", arrayOf(rowId.toString())).use { c ->
                if (!c.moveToFirst()) return "MISSING"
                val current = c.getString(3)
                if (current in setOf("ACKED", "FAILED", "EXPIRED")) return current
                val attempts = c.getInt(0) + 1
                status = when {
                    c.getLong(2) < System.currentTimeMillis() -> "EXPIRED"
                    attempts >= c.getInt(1) -> "FAILED"
                    else -> "RETRY"
                }
                val values = ContentValues().apply { put("attempts", attempts); put("status", status); put("last_error", error?.take(200)) }
                db.update("mesh_queue", values, "id = ?", arrayOf(rowId.toString()))
            }
            db.setTransactionSuccessful()
            return status
        } finally { db.endTransaction() }
    }

    /** ACK is accepted only with an HMAC generated from the transport's established shared peer key. */
    fun acknowledge(messageId: String, peerToken: String, ackMac: ByteArray, sharedKey: ByteArray, now: Long = System.currentTimeMillis()): Boolean {
        val expected = createAckMac(messageId, peerToken, sharedKey)
        if (!MessageDigest.isEqual(expected, ackMac)) return false
        val values = ContentValues().apply { put("status", "ACKED"); put("acked_by", peerToken); put("acked_at", now); putNull("last_error") }
        return database.writableDatabase.update("mesh_queue", values, "message_id = ? AND status NOT IN ('FAILED','EXPIRED') AND expires_at >= ?", arrayOf(messageId, now.toString())) == 1
    }

    /** Durable replay suppression for a packet already authenticated/decoded by the caller. */
    fun rememberReceived(messageId: String, issuedAt: Long, now: Long = System.currentTimeMillis()): Boolean {
        require(messageId.isNotBlank() && messageId.length <= 64)
        require(issuedAt <= now + 300_000L && now - issuedAt <= MAX_AGE_MS)
        val db = database.writableDatabase
        db.beginTransaction()
        try {
            db.delete("mesh_seen", "seen_at < ?", arrayOf((now - MAX_AGE_MS).toString()))
            val values = ContentValues().apply { put("message_id", messageId); put("seen_at", now) }
            val inserted = db.insertWithOnConflict("mesh_seen", null, values, SQLiteDatabase.CONFLICT_IGNORE) != -1L
            if (inserted) db.execSQL("DELETE FROM mesh_seen WHERE message_id IN (SELECT message_id FROM mesh_seen ORDER BY seen_at DESC LIMIT -1 OFFSET $MAX_SEEN)")
            db.setTransactionSuccessful()
            return inserted
        } finally { db.endTransaction() }
    }
}
