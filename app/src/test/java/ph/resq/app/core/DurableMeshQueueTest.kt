package ph.resq.app.core

import android.content.Context
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import javax.crypto.spec.SecretKeySpec

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DurableMeshQueueTest {
    private lateinit var context: Context
    private lateinit var db: ResQDatabase
    private lateinit var queue: DurableMeshQueue
    private val key = ByteArray(32) { (it + 1).toByte() }
    private fun testCipher() = EncryptedFieldCipher(testKeyProvider = { SecretKeySpec(ByteArray(32) { 0x5a }, "AES") })

    @Before fun setUp() {
        context = RuntimeEnvironment.getApplication()
        context.deleteDatabase("ph_resq_local.db")
        db = ResQDatabase(context, testCipher())
        queue = DurableMeshQueue(db, testCipher())
    }
    @After fun tearDown() { db.close(); context.deleteDatabase("ph_resq_local.db") }

    @Test fun queueEncryptsPacketsDeduplicatesAndAcceptsOnlyAuthenticatedAck() {
        val now = System.currentTimeMillis()
        val envelope = MeshEnvelope("msg-1", "rotating-token", now, 4, 0, "SOS test payload".toByteArray())
        assertTrue(queue.enqueue(envelope, key, now = now))
        assertFalse(queue.enqueue(envelope, key, now = now))
        val pending = queue.pending(now = now).single()
        assertEquals("msg-1", pending.messageId)
        assertEquals("SOS test payload", MeshEnvelopeCodec.decode(pending.packet, key, now).payload.toString(Charsets.UTF_8))
        assertTrue(queue.markSent(pending.rowId))
        val goodAck = DurableMeshQueue.createAckMac("msg-1", "peer-7", key)
        assertFalse(queue.acknowledge("msg-1", "peer-7", ByteArray(32), key, now))
        assertTrue(queue.acknowledge("msg-1", "peer-7", goodAck, key, now))
        assertTrue(queue.pending(now = now).isEmpty())
    }

    @Test fun retriesAreBoundedAndReceivedMessageIdsAreDurablyDeduplicated() {
        val now = System.currentTimeMillis()
        assertTrue(queue.enqueue(MeshEnvelope("msg-2", "token", now, 2, 0, byteArrayOf(1)), key, maxAttempts = 2, now = now))
        val row = queue.pending(now = now).single()
        assertEquals("RETRY", queue.markAttempt(row.rowId, "peer unavailable"))
        assertEquals(1, queue.pending(now = now).single().attempts)
        assertEquals("FAILED", queue.markAttempt(row.rowId, "retry failed"))
        assertTrue(queue.pending(now = now).isEmpty())
        assertTrue(queue.rememberReceived("incoming-1", now, now))
        assertFalse(queue.rememberReceived("incoming-1", now, now + 1))
        assertTrue(queue.rememberReceived("incoming-1", now, now + 86_400_001L))
    }
}
