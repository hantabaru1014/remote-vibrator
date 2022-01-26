package com.gmail.hantabaru1014.remotevibrator

import android.app.*
import android.app.Notification.FOREGROUND_SERVICE_IMMEDIATE
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Vibrator
import java.net.URI

class ForegroundService : Service() {
    companion object {
        const val NOTIFY_CHANNEL_ID = "remote_vibrator_foreground_service"
    }
    private lateinit var wsClient: WebSocketClient

    override fun onBind(p0: Intent?): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Notification.Builder(this, NOTIFY_CHANNEL_ID)
                .setContentTitle("RemoteVibrator")
                .setContentText("Running...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
                .build()
        } else {
            Notification.Builder(this, NOTIFY_CHANNEL_ID)
                .setContentTitle("RemoteVibrator")
                .setContentText("Running...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
        }
        startForeground(1, notification)

        val wsUrl = intent?.getStringExtra("url") ?: ""
        val deviceName = intent?.getStringExtra("deviceName") ?: "Unknown"
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        wsClient = WebSocketClient(URI(wsUrl), deviceName, vibrator, null)
        wsClient.connect()

        return START_STICKY
    }

    override fun stopService(name: Intent?): Boolean {
        wsClient.close()
        stopForeground(true)
        return super.stopService(name)
    }

    override fun onDestroy() {
        super.onDestroy()
        wsClient.close()
        stopSelf()
    }
}