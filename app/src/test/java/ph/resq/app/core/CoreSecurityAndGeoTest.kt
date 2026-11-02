package ph.resq.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreSecurityAndGeoTest {
    private val key = ByteArray(32) { (it + 1).toByte() }

    @Test fun geohashIsStableAndValidatesCoordinates() {
        assertEquals("gcpvj", Geohash.encode(51.5074, -0.1278, 5))
        assertEquals(Geohash.encode(14.5995, 120.9842, 7), Geohash.encode(14.5995, 120.9842, 7))
        assertTrue(runCatching { Geohash.encode(91.0, 0.0) }.isFailure)
    }

    @Test fun sha256VerifierDetectsTampering() {
        val data = "verified emergency dataset".toByteArray()
        val digest = IntegrityVerifier.sha256Hex(data)
        assertTrue(IntegrityVerifier.verify(data, digest))
        assertFalse(IntegrityVerifier.verify(data + byteArrayOf(0), digest))
        assertFalse(IntegrityVerifier.verify(data, "not-a-hash"))
    }

    @Test fun meshCodecAuthenticatesPayloadAndEnforcesLimits() {
        val now = System.currentTimeMillis()
        val original = MeshEnvelope("msg-001", "token-abc", now, 4, 0, "SOS".toByteArray())
        val decoded = MeshEnvelopeCodec.decode(MeshEnvelopeCodec.encode(original, key), key, now)
        assertEquals(original.messageId, decoded.messageId)
        assertTrue(original.payload.contentEquals(decoded.payload))
        val tampered = MeshEnvelopeCodec.encode(original, key).also { it[it.size - 33] = (it[it.size - 33].toInt() xor 1).toByte() }
        assertTrue(runCatching { MeshEnvelopeCodec.decode(tampered, key, now) }.isFailure)
    }

    @Test fun meshCodecRejectsExpiredMessagesAndStopsAtHopLimit() {
        val now = System.currentTimeMillis()
        val expired = MeshEnvelope("old", "token", now - 90_000_000L, 1, 0, byteArrayOf(1))
        assertTrue(runCatching { MeshEnvelopeCodec.decode(MeshEnvelopeCodec.encode(expired, key), key, now) }.isFailure)
        assertTrue(runCatching { MeshEnvelopeCodec.forward(expired) }.isFailure)
        val envelope = MeshEnvelope("one", "token", now, 1, 0, byteArrayOf(1))
        assertEquals(1, MeshEnvelopeCodec.forward(envelope).hops)
        assertTrue(runCatching { MeshEnvelopeCodec.forward(envelope.copy(hops = 1)) }.isFailure)
    }

    @Test fun dedupeCacheRejectsRepeatedIdsAndBoundsMemory() {
        val cache = SeenMessageCache(2)
        assertTrue(cache.markIfNew("a", 1000))
        assertFalse(cache.markIfNew("a", 1001))
        assertTrue(cache.markIfNew("b", 1001))
        assertTrue(cache.markIfNew("c", 1002))
        assertTrue(cache.markIfNew("a", 1003))
    }

    @Test fun rollingTokensAreStableWithinWindowAndRotate() {
        val secret = ByteArray(32) { 7 }
        assertEquals(RollingPrivacyToken.create(secret, 1000), RollingPrivacyToken.create(secret, 2000))
        assertFalse(RollingPrivacyToken.create(secret, 1000) == RollingPrivacyToken.create(secret, 1_000_000))
    }

    @Test fun adaptiveLocationPolicyRespectsPermissionAndBattery() {
        assertEquals(AdaptiveLocationPolicy.NO_POLL, AdaptiveLocationPolicy.intervalMillis(90, true, false))
        assertEquals(300_000L, AdaptiveLocationPolicy.intervalMillis(5, true, true))
        assertEquals(15_000L, AdaptiveLocationPolicy.intervalMillis(80, true, true))
        assertEquals(60_000L, AdaptiveLocationPolicy.intervalMillis(80, false, true))
    }
}
