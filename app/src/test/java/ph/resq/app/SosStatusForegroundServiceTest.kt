package ph.resq.app

import android.app.NotificationManager
import android.content.Intent
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SosStatusForegroundServiceTest {
    @Test fun createsVisibleNotificationChannelForSosHandoff() {
        val controller = Robolectric.buildService(SosStatusForegroundService::class.java).create()
        controller.startCommand(Intent(RuntimeEnvironment.getApplication(), SosStatusForegroundService::class.java).setAction(SosStatusForegroundService.ACTION_START), 0, 1)
        val manager = RuntimeEnvironment.getApplication().getSystemService(NotificationManager::class.java)
        assertNotNull(manager.getNotificationChannel(SosStatusForegroundService.CHANNEL_ID))
        controller.destroy()
    }
}
