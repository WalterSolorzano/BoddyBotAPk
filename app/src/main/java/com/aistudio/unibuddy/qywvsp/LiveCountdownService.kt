package com.aistudio.unibuddy.qywvsp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class LiveCountdownService : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val targetTime = intent?.getLongExtra("TARGET_TIME", 0L) ?: 0L
        val title = intent?.getStringExtra("TITLE") ?: "Próxima Clase"
        val subtitle = intent?.getStringExtra("SUBTITLE") ?: "Prepárate para salir"

        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, "live_activity_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Change to app icon in real use
            .setContentTitle(title)
            .setContentText(subtitle)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)

        if (targetTime > 0) {
            builder.setUsesChronometer(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setChronometerCountDown(true)
            }
            builder.setWhen(targetTime)
        }

        startForeground(777, builder.build())
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "live_activity_channel",
                "Live Activity Countdown",
                NotificationManager.IMPORTANCE_LOW // Low so it doesn't pop up every second
            )
            channel.description = "Muestra la cuenta regresiva para tu próxima clase"
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    companion object {
        fun start(context: Context, targetTimeMillis: Long, title: String, subtitle: String) {
            val intent = Intent(context, LiveCountdownService::class.java).apply {
                putExtra("TARGET_TIME", targetTimeMillis)
                putExtra("TITLE", title)
                putExtra("SUBTITLE", subtitle)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stop(context: Context) {
            context.stopService(Intent(context, LiveCountdownService::class.java))
        }
    }
}
