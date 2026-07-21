package com.aistudio.unibuddy.qywvsp.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.aistudio.unibuddy.qywvsp.R

object NotificationHelper {
    private const val CHANNEL_ID = "unibuddy_notifications_channel"
    private const val CHANNEL_NAME = "UniBuddy Canales Alertas"
    private const val CHANNEL_DESC = "Alertas de asistencia de UniBuddy"

    var isSilenceModeActive: Boolean = false

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
        if (isSilenceModeActive) {
            return
        }
        // Enforce POST_NOTIFICATIONS check for Android 13 (Tiramisu) or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != 
                PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val remoteViews = RemoteViews(context.packageName, R.layout.custom_notification).apply {
            setTextViewText(R.id.notification_title, title)
            setTextViewText(R.id.notification_message, message)
            setImageViewResource(R.id.notification_icon, R.drawable.ic_widget_school)
            setInt(R.id.notification_status_indicator, "setBackgroundColor", android.graphics.Color.parseColor("#10B981")) // Green/Bien
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(remoteViews)
            .setCustomBigContentView(remoteViews)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setColor(android.graphics.Color.parseColor("#10B981"))
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify((System.currentTimeMillis() % 100000).toInt(), builder.build())
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    fun sendNextClassNotification(context: Context, subjectId: Int, destinationName: String, title: String, message: String, isCritical: Boolean = false) {
        if (isSilenceModeActive) {
            return
        }
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

        val colorHex = if (isCritical) "#EF4444" else "#F59E0B" // Red/Crítico or Amber/Atención
        val iconRes = if (isCritical) R.drawable.buddy_widget_lluvia else R.drawable.buddy_widget_soleado

        val remoteViews = RemoteViews(context.packageName, R.layout.custom_notification).apply {
            setTextViewText(R.id.notification_title, title)
            setTextViewText(R.id.notification_message, message)
            setImageViewResource(R.id.notification_icon, iconRes)
            setInt(R.id.notification_status_indicator, "setBackgroundColor", android.graphics.Color.parseColor(colorHex))
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(remoteViews)
            .setCustomBigContentView(remoteViews)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setColor(android.graphics.Color.parseColor(colorHex))
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_menu_today, "Marcar Asistencia", attendPendingIntent)
            .addAction(android.R.drawable.ic_menu_send, "Avisar Retraso", latePendingIntent)
            
        if (isCritical) {
            builder.setColorized(true)
        }

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(notifId, builder.build())
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }
}
