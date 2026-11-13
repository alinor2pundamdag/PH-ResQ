package ph.resq.app.core

import android.content.Context
import android.content.SharedPreferences

/**
 * Single typed owner for user-controlled display/audio preferences.
 * Device permissions and hardware state are intentionally not stored here.
 */
class SettingsRepository(context: Context) {
    private val preferences: SharedPreferences =
        context.applicationContext.getSharedPreferences("ph_resq_local", Context.MODE_PRIVATE)

    val highContrast: Boolean
        get() = preferences.getBoolean(KEY_HIGH_CONTRAST, false)

    val reduceMotion: Boolean
        get() = preferences.getBoolean(KEY_REDUCE_MOTION, false)

    val voicePlaybackVolume: Float
        get() = SafeVolumePolicy.clamp(
            preferences.getFloat(KEY_VOICE_PLAYBACK_VOLUME, SafeVolumePolicy.DEFAULT)
        )

    fun setHighContrast(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_HIGH_CONTRAST, enabled).apply()
    }

    fun setReduceMotion(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_REDUCE_MOTION, enabled).apply()
    }

    fun setVoicePlaybackVolume(value: Float) {
        preferences.edit()
            .putFloat(KEY_VOICE_PLAYBACK_VOLUME, SafeVolumePolicy.clamp(value))
            .apply()
    }

    companion object {
        private const val KEY_HIGH_CONTRAST = "high_contrast"
        private const val KEY_REDUCE_MOTION = "reduce_motion"
        private const val KEY_VOICE_PLAYBACK_VOLUME = "voice_playback_volume"
    }
}
