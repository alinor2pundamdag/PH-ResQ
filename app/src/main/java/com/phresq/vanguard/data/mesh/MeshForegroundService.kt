package com.phresq.vanguard.data.mesh

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat

class MeshForegroundService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var bleMeshManager: BleMeshManager

    companion object {
        private const val CHANNEL_ID = "PHResQ_Mesh_Channel"
        private const val NOTIFICATION_ID = 2026
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Acquire partial CPU wake lock to guarantee background mesh survival during power blackouts
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PHResQ::MeshWakeLock").apply {
            acquire(12 * 60 * 60 * 1000L) // 12 hours max emergency safeguard
        }

        bleMeshManager = BleMeshManager(applicationContext)
        bleMeshManager.startMeshNode("VanguardNode_${System.currentTimeMillis()}", 0x00, 100)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("PH-ResQ Mesh Active")
            .setContentText("Emergency P2P relay and BLE scanner running in background.")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "PH-ResQ Mesh Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        try {
            wakeLock?.release()
        } catch (_: Exception) {}
        bleMeshManager.stopMesh()
    }
}
