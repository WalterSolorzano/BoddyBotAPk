// UniBuddy Pomodoro Service - Version 1.5 - Fix for GitHub Build
package com.aistudio.unibuddy.qywvsp.ui

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
import com.aistudio.unibuddy.qywvsp.R
import kotlinx.coroutines.*

class PomodoroService : Service() {

    private var targetTimeMillis = 0L
    private var timerJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
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
                val timeLeftSeconds = minutes * 60
                PomodoroState.updateTime(timeLeftSeconds)
                PomodoroState.setPaused(false)
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
        timerJob?.cancel()
        PomodoroState.setRunning(true)
        PomodoroState.setWorkMode(isWorkMode)
        PomodoroState.setPaused(false)
        
        targetTimeMillis = System.currentTimeMillis() + (seconds * 1000L)
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val min = seconds / 60
        val sec = seconds % 60
        val timeStr = String.format("%02d:%02d", min, sec)
        
        // Start foreground with proper special use foreground type
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID, 
                buildNotification(timeStr, "Iniciando..."),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                } else {
                    0
                }
            )
        } else {
            startForeground(NOTIFICATION_ID, buildNotification(timeStr, "Iniciando..."))
        }

        timerJob = serviceScope.launch {
            while (isActive) {
                val currentTime = System.currentTimeMillis()
                val remainingMillis = targetTimeMillis - currentTime
                if (remainingMillis <= 0) {
                    withContext(Dispatchers.Main) {
                        onTimerFinished()
                    }
                    break
                }
                
                val remainingSeconds = (remainingMillis / 1000L).toInt()
                withContext(Dispatchers.Main) {
                    PomodoroState.updateTime(remainingSeconds)
                    val m = remainingSeconds / 60
                    val s = remainingSeconds % 60
                    val ts = String.format("%02d:%02d", m, s)
                    val title = if (isWorkMode) "Modo Focus: ¡A estudiar!" else "Modo Focus: Descanso"
                    notificationManager.notify(NOTIFICATION_ID, buildNotification(ts, title))
                }
                
                delay(500)
            }
        }
    }

    private fun onTimerFinished() {
        PomodoroState.setRunning(false)
        PomodoroState.updateTime(0)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val title = if (isWorkMode) "¡Focus Completado!" else "¡Descanso Terminado!"
        val text = if (isWorkMode) "Buen trabajo. Es hora de un descanso." else "Volvamos a estudiar."
        
        val notification = androidx.core.app.NotificationCompat.Builder(this@PomodoroService, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(com.aistudio.unibuddy.qywvsp.R.drawable.ic_launcher_foreground)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
        stopSelf()
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        PomodoroState.setPaused(true)
        PomodoroState.setRunning(false)
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val timeLeftSeconds = PomodoroState.timeLeftSeconds.value
        val min = timeLeftSeconds / 60
        val sec = timeLeftSeconds % 60
        val timeStr = String.format("%02d:%02d", min, sec)
        val title = if (isWorkMode) "Modo Focus: Pausado" else "Descanso: Pausado"
        
        notificationManager.notify(NOTIFICATION_ID, buildNotification(timeStr, title))
    }

    private fun resumeTimer() {
        val timeLeftSeconds = PomodoroState.timeLeftSeconds.value
        if (timeLeftSeconds > 0) {
            startTimerFromSeconds(timeLeftSeconds)
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        PomodoroState.setRunning(false)
        PomodoroState.setPaused(false)
    }

    private fun buildNotification(timeStr: String, title: String = "Modo Focus"): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, 
            packageManager.getLaunchIntentForPackage(packageName), 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val builder = androidx.core.app.NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("Tiempo restante: $timeStr")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)

        // Actions
        if (PomodoroState.isPaused.value) {
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
        serviceScope.cancel()
    }
}
