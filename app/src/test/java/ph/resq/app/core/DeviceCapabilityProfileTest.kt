package ph.resq.app.core

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DeviceCapabilityProfileTest {
    @Test fun profileReportsRuntimeAndStorageWithoutRequestingPermissions() {
        val context = RuntimeEnvironment.getApplication()
        val profile = DeviceCapabilityProfile.detect(context)
        assertEquals(Build.VERSION.SDK_INT, profile.sdkInt)
        assertTrue(profile.manufacturer.isNotBlank())
        assertTrue(profile.model.isNotBlank())
        assertTrue(profile.availableStorageBytes >= 0L)
        assertNotNull(profile.capability("ble"))
        assertNotNull(profile.capability("location"))
        assertNotNull(profile.capability("notifications"))
    }

    @Test fun missingOptionalCapabilitiesAreExplicitRatherThanSuccessful() {
        val profile = DeviceCapabilityProfile.detect(RuntimeEnvironment.getApplication())
        assertTrue(profile.capabilities.keys.containsAll(
            listOf("telephony", "ble", "wifi_direct", "location", "microphone", "notifications", "keystore")
        ))
        assertEquals(CapabilityState.ERROR, profile.capability("not_registered").state)
    }

    @Test fun android13NotificationPermissionIsNotAssumedGranted() {
        val context = RuntimeEnvironment.getApplication()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val profile = DeviceCapabilityProfile.detect(context)
            if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                assertEquals(CapabilityState.DENIED, profile.capability("notifications").state)
            }
        }
    }
}
