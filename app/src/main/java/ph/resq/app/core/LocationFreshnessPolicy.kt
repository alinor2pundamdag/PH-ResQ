package ph.resq.app.core

/**
 * Rejects stale and implausibly future-dated fixes. A small future tolerance covers clock/provider
 * rounding; future timestamps must never be treated as freshly captured SOS coordinates.
 */
object LocationFreshnessPolicy {
    fun isUsable(timestampMs: Long, nowMs: Long, maxAgeMs: Long = 120_000L, futureToleranceMs: Long = 5_000L): Boolean {
        require(maxAgeMs >= 0L && futureToleranceMs >= 0L)
        val age = nowMs - timestampMs
        return age >= -futureToleranceMs && age <= maxAgeMs
    }

    fun isFresh(timestampMs: Long, nowMs: Long, freshWindowMs: Long = 20_000L): Boolean {
        require(freshWindowMs >= 0L)
        val age = nowMs - timestampMs
        return age in 0..freshWindowMs
    }
}
