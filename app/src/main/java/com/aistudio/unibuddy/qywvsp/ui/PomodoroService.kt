package com.aistudio.unibuddy.qywvsp.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.aistudio.unibuddy.qywvsp.R

class PomodoroService : Service() {

    private var countDownTimer: CountDownTimer? = null
    private var isWorkMode = true

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val EXTRA_MINUTES = "EXTRA_MINUTES"
        const val EXTRA_IS_WORK = "EXTRA_IS_WORK"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "pomodoro_channel"
        
        var isRunningInService = false
        var isPaused = false
        var timeLeftSeconds = 0
        var currentModeIsWork = true
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_START -> {
                val minutes = intent.getIntExtra(EXTRA_MINUTES, 25)
                isWorkMode = intent.getBooleanExtra(EXTRA_IS_WORK, true)
                timeLeftSeconds = minutes * 60
                isPaused = false
                startTimerFromSeconds(timeLeftSeconds)
            }
            ACTION_STOP -> {
                stopTimer()
                stopSelf()
            }
            ACTION_PAUSE -> {
                pauseTimer()
            }
            ACTION_RESUME -> {
                resumeTimer()
            }
        }
        return START_NOT_STICKY
    }

    private fun startTimerFromSeconds(seconds: Int) {
        countDownTimer?.cancel()
        isRunningInService = true
        currentModeIsWork = isWorkMode
        isPaused = false
        
        val totalMillis = seconds * 1000L
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val min = timeLeftSeconds / 60
        val sec = timeLeftSeconds % 60
        val timeStr = String.format("%02d:%02d", min, sec)
        startForeground(NOTIFICATION_ID, buildNotification(timeStr, "Iniciando..."))

        countDownTimer = object : CountDownTimer(totalMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftSeconds = (millisUntilFinished / 1000).toInt()
                val m = timeLeftSeconds / 60
                val s = timeLeftSeconds % 60
                val ts = String.format("%02d:%02d", m, s)
                
                val title = if (isWorkMode) "Modo Focus: ¡A estudiar!" else "Modo Focus: Descanso"
                
                notificationManager.notify(NOTIFICATION_ID, buildNotification(ts, title))
            }

            override fun onFinish() {
                isRunningInService = false
                timeLeftSeconds = 0
                val title = if (isWorkMode) "¡Focus Completado!" else "¡Descanso Terminado!"
                val text = if (isWorkMode) "Buen trabajo. Es hora de un descanso." else "Volvamos a estudiar."
                
                val notification = NotificationCompat.Builder(this@PomodoroService, CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build()
                
                notificationManager.notify(NOTIFICATION_ID + 1, notification)
                stopSelf()
            }
        }.start()
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isPaused = true
        isRunningInService = false
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val min = timeLeftSeconds / 60
        val sec = timeLeftSeconds % 60
        val timeStr = String.format("%02d:%02d", min, sec)
        val title = if (isWorkMode) "Modo Focus: Pausado" else "Descanso: Pausado"
        
        notificationManager.notify(NOTIFICATION_ID, buildNotification(timeStr, title))
    }

    private fun resumeTimer() {
        if (timeLeftSeconds > 0) {
            startTimerFromSeconds(timeLeftSeconds)
        }
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        isRunningInService = false
        isPaused = false
    }

    private fun buildNotification(timeStr: String, title: String = "Modo Focus"): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, 
            packageManager.getLaunchIntentForPackage(packageName), 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("Tiempo restante: $timeStr")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)

        // Actions
        if (isPaused) {
            val resumeIntent = Intent(this, PomodoroService::class.java).apply { action = ACTION_RESUME }
            val resumePending = PendingIntent.getService(this, 1, resumeIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            builder.addAction(android.R.drawable.ic_media_play, "Reanudar", resumePending)
        } else {
            val pauseIntent = Intent(this, PomodoroService::class.java).apply { action = ACTION_PAUSE }
            val pausePending = PendingIntent.getService(this, 2, pauseIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            builder.addAction(android.R.drawable.ic_media_pause, "Pausar", pausePending)
        }
        
        val stopIntent = Intent(this, PomodoroService::class.java).apply { action = ACTION_STOP }
        val stopPending = PendingIntent.getService(this, 3, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Detener", stopPending)

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Temporizador Pomodoro",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }
}
