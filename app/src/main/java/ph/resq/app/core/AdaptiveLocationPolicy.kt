package ph.resq.app.core

object AdaptiveLocationPolicy {
    const val NO_POLL = -1L

    /** Policy only; a lifecycle-aware Android location service must still enforce permissions and OS limits. */
    fun intervalMillis(batteryPercent: Int, moving: Boolean, hasLocationPermission: Boolean): Long {
        if (!hasLocationPermission) return NO_POLL
        val battery = batteryPercent.coerceIn(0, 100)
        return when {
            battery <= 5 -> 300_000L
            battery <= 15 -> 120_000L
            moving && battery > 30 -> 15_000L
            moving -> 30_000L
            else -> 60_000L
        }
    }
}
