package ph.resq.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SafeVolumePolicyTest {
    @Test fun clampsPlaybackGainToSafeConfiguredRange() {
        assertEquals(SafeVolumePolicy.MIN, SafeVolumePolicy.clamp(0f), 0.0001f)
        assertEquals(SafeVolumePolicy.DEFAULT, SafeVolumePolicy.clamp(SafeVolumePolicy.DEFAULT), 0.0001f)
        assertEquals(SafeVolumePolicy.MAX, SafeVolumePolicy.clamp(1f), 0.0001f)
        assertTrue(SafeVolumePolicy.clamp(0.6f) <= SafeVolumePolicy.MAX)
    }

    @Test fun rejectsNonFiniteVolume() {
        assertTrue(runCatching { SafeVolumePolicy.clamp(Float.NaN) }.isFailure)
        assertTrue(runCatching { SafeVolumePolicy.clamp(Float.POSITIVE_INFINITY) }.isFailure)
    }
}
