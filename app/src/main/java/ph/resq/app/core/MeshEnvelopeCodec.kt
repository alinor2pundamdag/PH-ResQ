package ph.resq.app.core

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

data class MeshEnvelope(
    val messageId: String,
    val senderToken: String,
    val issuedAtMillis: Long,
    val maxHops: Int,
    val hops: Int,
    val payload: ByteArray,
    val version: Int = 1
)

object MeshEnvelopeCodec {
    private val MAGIC = byteArrayOf(0x50, 0x48, 0x52, 0x51) // PHRQ
    private const val MAC_SIZE = 32
    private const val MAX_PACKET_SIZE = 65_536
    private const val MAX_PAYLOAD_SIZE = 60_000
    private const val MAX_HOPS = 8
    private const val MAX_AGE_MILLIS = 86_400_000L
    private const val MAX_FUTURE_SKEW_MILLIS = 300_000L

    fun encode(envelope: MeshEnvelope, key: ByteArray): ByteArray {
        require(key.size >= 32) { "HMAC key must be at least 256 bits" }
        validate(envelope, System.currentTimeMillis(), checkTime = false)
        val bodyBytes = ByteArrayOutputStream()
        DataOutputStream(bodyBytes).use { out ->
            out.write(MAGIC)
            out.writeInt(envelope.version)
            out.writeUTF(envelope.messageId)
            out.writeUTF(envelope.senderToken)
            out.writeLong(envelope.issuedAtMillis)
            out.writeInt(envelope.maxHops)
            out.writeInt(envelope.hops)
            out.writeInt(envelope.payload.size)
            out.write(envelope.payload)
        }
        val body = bodyBytes.toByteArray()
        val mac = hmac(key, body)
        val packet = body + mac
        require(packet.size <= MAX_PACKET_SIZE) { "Packet too large" }
        return packet
    }

    fun decode(packet: ByteArray, key: ByteArray, nowMillis: Long = System.currentTimeMillis()): MeshEnvelope {
        require(key.size >= 32) { "HMAC key must be at least 256 bits" }
        require(packet.size in (MAC_SIZE + 32)..MAX_PACKET_SIZE) { "Invalid packet size" }
        val body = packet.copyOfRange(0, packet.size - MAC_SIZE)
        val receivedMac = packet.copyOfRange(packet.size - MAC_SIZE, packet.size)
        require(MessageDigest.isEqual(hmac(key, body), receivedMac)) { "Packet authentication failed" }
        val envelope = DataInputStream(ByteArrayInputStream(body)).use { input ->
            val magic = ByteArray(MAGIC.size)
            input.readFully(magic)
            require(magic.contentEquals(MAGIC)) { "Unknown packet magic" }
            val version = input.readInt()
            require(version == 1) { "Unsupported packet version" }
            val messageId = input.readUTF()
            val senderToken = input.readUTF()
            val issuedAt = input.readLong()
            val maxHops = input.readInt()
            val hops = input.readInt()
            val payloadSize = input.readInt()
            require(payloadSize in 0..MAX_PAYLOAD_SIZE) { "Invalid payload size" }
            val payload = ByteArray(payloadSize)
            input.readFully(payload)
            require(input.available() == 0) { "Trailing packet bytes" }
            MeshEnvelope(messageId, senderToken, issuedAt, maxHops, hops, payload, version)
        }
        validate(envelope, nowMillis, checkTime = true)
        return envelope
    }

    fun forward(envelope: MeshEnvelope): MeshEnvelope {
        validate(envelope, System.currentTimeMillis(), checkTime = true)
        require(envelope.hops < envelope.maxHops) { "Hop limit reached" }
        return envelope.copy(hops = envelope.hops + 1)
    }

    private fun validate(envelope: MeshEnvelope, nowMillis: Long, checkTime: Boolean) {
        require(envelope.version == 1) { "Unsupported packet version" }
        require(envelope.messageId.isNotBlank() && envelope.messageId.length <= 64) { "Invalid message ID" }
        require(envelope.senderToken.isNotBlank() && envelope.senderToken.length <= 64) { "Invalid sender token" }
        require(envelope.maxHops in 1..MAX_HOPS) { "Invalid hop limit" }
        require(envelope.hops in 0..envelope.maxHops) { "Invalid hop count" }
        require(envelope.payload.size <= MAX_PAYLOAD_SIZE) { "Payload too large" }
        if (checkTime) {
            require(envelope.issuedAtMillis <= nowMillis + MAX_FUTURE_SKEW_MILLIS) { "Message timestamp is in the future" }
            require(nowMillis - envelope.issuedAtMillis <= MAX_AGE_MILLIS) { "Message expired" }
        }
    }

    private fun hmac(key: ByteArray, body: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(body)
    }
}

class SeenMessageCache(private val capacity: Int = 2048) {
    private val seen = object : LinkedHashMap<String, Long>(capacity, 0.75f, true) {}
    init { require(capacity > 0) }

    @Synchronized
    fun markIfNew(messageId: String, nowMillis: Long = System.currentTimeMillis()): Boolean {
        val cutoff = nowMillis - 86_400_000L
        val iterator = seen.entries.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().value < cutoff) iterator.remove()
        }
        if (seen.containsKey(messageId)) return false
        seen[messageId] = nowMillis
        while (seen.size > capacity) {
            val first = seen.entries.iterator()
            if (first.hasNext()) { first.next(); first.remove() } else break
        }
        return true
    }
}
