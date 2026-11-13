package ph.resq.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SettingsRepositoryTest {
    @Test fun displayAndAudioPreferencesPersistAcrossRepositoryInstances() {
        val context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("ph_resq_local", 0).edit().clear().commit()
        val first = SettingsRepository(context)
        assertFalse(first.highContrast)
        assertFalse(first.reduceMotion)
        first.setHighContrast(true)
        first.setReduceMotion(true)
        first.setVoicePlaybackVolume(0.5f)

        val reopened = SettingsRepository(context)
        assertTrue(reopened.highContrast)
        assertTrue(reopened.reduceMotion)
        assertEquals(0.5f, reopened.voicePlaybackVolume, 0.001f)
    }

    @Test fun invalidAudioPreferenceIsClampedOnReadAndWrite() {
        val context = RuntimeEnvironment.getApplication()
        val repository = SettingsRepository(context)
        repository.setVoicePlaybackVolume(Float.NaN)
        assertEquals(SafeVolumePolicy.DEFAULT, repository.voicePlaybackVolume, 0.001f)
        repository.setVoicePlaybackVolume(1.0f)
        assertEquals(SafeVolumePolicy.MAX, repository.voicePlaybackVolume, 0.001f)
    }
}
