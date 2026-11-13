package ph.resq.app.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationFreshnessPolicyTest {
    @Test fun acceptsRecentAndSmallClockSkewButRejectsStaleAndFutureFixes() {
        val now = 1_000_000L
        assertTrue(LocationFreshnessPolicy.isUsable(now, now))
        assertTrue(LocationFreshnessPolicy.isUsable(now - 120_000L, now))
        assertTrue(LocationFreshnessPolicy.isUsable(now + 5_000L, now))
        assertFalse(LocationFreshnessPolicy.isUsable(now - 120_001L, now))
        assertFalse(LocationFreshnessPolicy.isUsable(now + 5_001L, now))
    }

    @Test fun earlyCompletionRequiresNonFutureFreshTimestamp() {
        val now = 1_000_000L
        assertTrue(LocationFreshnessPolicy.isFresh(now, now))
        assertTrue(LocationFreshnessPolicy.isFresh(now - 20_000L, now))
        assertFalse(LocationFreshnessPolicy.isFresh(now - 20_001L, now))
        assertFalse(LocationFreshnessPolicy.isFresh(now + 1L, now))
    }
}
