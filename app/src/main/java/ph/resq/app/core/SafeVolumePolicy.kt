package ph.resq.app.core

/**
 * Caps PH-ResQ voice-note playback gain without changing the device's system media volume.
 * This is a software gain limit, not a guarantee of safe sound-pressure level on every device.
 */
object SafeVolumePolicy {
    const val MIN = 0.10f
    const val MAX = 0.70f
    const val DEFAULT = 0.50f

    fun clamp(requested: Float): Float {
        require(requested.isFinite()) { "Playback volume must be finite." }
        return requested.coerceIn(MIN, MAX)
    }
}
