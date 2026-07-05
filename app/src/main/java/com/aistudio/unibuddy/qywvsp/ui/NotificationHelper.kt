package com.aistudio.unibuddy.qywvsp.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    private const val CHANNEL_ID = "unibuddy_notifications_channel"
    private const val CHANNEL_NAME = "UniBuddy Canales Alertas"
    private const val CHANNEL_DESC = "Alertas de asistencia de UniBuddy"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendNotification(context: Context, title: String, message: String) {
        // Enforce POST_NOTIFICATIONS check for Android 13 (Tiramisu) or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != 
                PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setColor(android.graphics.Color.parseColor("#3B82F6")) // ProBlue
            .setAutoCancel(true)
            .setLights(android.graphics.Color.parseColor("#3B82F6"), 1000, 1000)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify((System.currentTimeMillis() % 100000).toInt(), builder.build())
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    fun sendNextClassNotification(context: Context, subjectId: Int, destinationName: String, title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        }
        
        val notifId = (System.currentTimeMillis() % 100000).toInt()

        // Action 1: Marcar Asistencia
        val attendIntent = android.content.Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_MARK_ATTENDANCE
            putExtra(NotificationActionReceiver.EXTRA_SUBJECT_ID, subjectId)
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notifId)
        }
        val attendPendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            notifId * 2,
            attendIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        // Action 2: Avisar Retraso
        val lateIntent = android.content.Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_LATE_WARNING
            putExtra(NotificationActionReceiver.EXTRA_DESTINATION_NAME, destinationName)
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notifId)
        }
        val latePendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            notifId * 2 + 1,
            lateIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setColor(android.graphics.Color.parseColor("#F97316")) // Amber
            .setAutoCancel(true)
            .setLights(android.graphics.Color.parseColor("#F97316"), 1000, 1000)
            .addAction(android.R.drawable.ic_menu_today, "Marcar Asistencia", attendPendingIntent)
            .addAction(android.R.drawable.ic_menu_send, "Avisar Retraso", latePendingIntent)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(notifId, builder.build())
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }
}
