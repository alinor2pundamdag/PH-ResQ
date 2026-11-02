package ph.resq.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder

/**
 * Visible, user-stoppable status for the SOS messaging-app handoff. It never claims that SMS was
 * sent or delivered and does not track location or transmit data in the background.
 */
class SosStatusForegroundService : Service() {
    companion object {
        const val ACTION_START = "ph.resq.app.action.START_SOS_STATUS"
        const val ACTION_STOP = "ph.resq.app.action.STOP_SOS_STATUS"
        const val CHANNEL_ID = "ph_resq_sos_status"
        private const val NOTIFICATION_ID = 4101
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "SOS handoff status",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Ongoing status while the user reviews an SOS message in their messaging app."
                    setShowBadge(false)
                }
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf(startId)
            return START_NOT_STICKY
        }
        startForeground(NOTIFICATION_ID, buildNotification())
        return START_NOT_STICKY
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, SosStatusForegroundService::class.java).setAction(ACTION_STOP)
        val stopPendingIntent = PendingIntent.getService(
            this,
            4102,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        return builder
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("PH-ResQ • SOS handoff")
            .setContentText("Review and send the SOS in your messaging app. Delivery is not confirmed.")
            .setCategory(Notification.CATEGORY_STATUS)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop status", stopPendingIntent)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
