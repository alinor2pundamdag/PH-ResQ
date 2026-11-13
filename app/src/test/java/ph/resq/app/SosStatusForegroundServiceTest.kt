package ph.resq.app

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
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
        controller.startCommand(0, 1)
        val manager = RuntimeEnvironment.getApplication().getSystemService(NotificationManager::class.java)
        assertNotNull(manager.getNotificationChannel(SosStatusForegroundService.CHANNEL_ID))
        controller.destroy()
    
    @Test fun statusServiceRestartsAfterProcessReclaimUntilUserStopsIt() {
        val controller = Robolectric.buildService(SosStatusForegroundService::class.java).create()
        val service = controller.get()
        assertEquals(Service.START_STICKY, service.onStartCommand(null, 0, 1))
        val stopIntent = Intent(RuntimeEnvironment.getApplication(), SosStatusForegroundService::class.java)
            .setAction(SosStatusForegroundService.ACTION_STOP)
        assertEquals(Service.START_NOT_STICKY, service.onStartCommand(stopIntent, 0, 2))
        controller.destroy()
    }
}
}
