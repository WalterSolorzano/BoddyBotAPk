package com.aistudio.unibuddy.qywvsp.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.aistudio.unibuddy.qywvsp.data.AppDatabase
import com.aistudio.unibuddy.qywvsp.data.AttendanceLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationActionReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_MARK_ATTENDANCE = "com.aistudio.unibuddy.ACTION_MARK_ATTENDANCE"
        const val ACTION_LATE_WARNING = "com.aistudio.unibuddy.ACTION_LATE_WARNING"
        const val EXTRA_SUBJECT_ID = "extra_subject_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_DESTINATION_NAME = "extra_destination_name"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val intentAction = intent.action ?: return
        val subjectId = intent.getIntExtra(EXTRA_SUBJECT_ID, -1)
        val notifId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

        when (intentAction) {
            ACTION_MARK_ATTENDANCE -> {
                if (subjectId != -1) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val db = AppDatabase.getDatabase(context)
                        val date = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())
                        db.attendanceLogDao().insertLog(
                            AttendanceLog(
                                subjectId = subjectId,
                                date = date,
                                isPresent = true
                            )
                        )
                        Log.d("UniBuddy", "Asistencia registrada desde notificacion para subject $subjectId")
                        // Dismiss the notification
                        if (notifId != -1) {
                            NotificationManagerCompat.from(context).cancel(notifId)
                        }
                    }
                }
            }
            ACTION_LATE_WARNING -> {
                val destName = intent.getStringExtra(EXTRA_DESTINATION_NAME) ?: "clase"
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Aviso de retraso: Voy un poco tarde, pero voy en camino hacia $destName.")
                    type = "text/plain"
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val chooser = Intent.createChooser(shareIntent, "Aviso de Retraso...")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
                
                if (notifId != -1) {
                    NotificationManagerCompat.from(context).cancel(notifId)
                }
            }
        }
    }
}
