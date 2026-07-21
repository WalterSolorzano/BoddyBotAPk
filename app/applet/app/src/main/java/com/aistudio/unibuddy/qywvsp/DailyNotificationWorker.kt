package com.aistudio.unibuddy.qywvsp

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aistudio.unibuddy.qywvsp.data.AppDatabase
import com.aistudio.unibuddy.qywvsp.ui.NotificationHelper
import kotlinx.coroutines.flow.first
import java.util.Calendar

class DailyNotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val db = AppDatabase.getDatabase(applicationContext)
            val subjects = db.subjectDao().getAllSubjects().first()
            val assessments = db.assessmentDao().getAllAssessments().first()
            val absences = db.absenceDao().getAllAbsences().first()

            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val tomorrowCode = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "Lu"
                Calendar.TUESDAY -> "Ma"
                Calendar.WEDNESDAY -> "Mi"
                Calendar.THURSDAY -> "Ju"
                Calendar.FRIDAY -> "Vi"
                Calendar.SATURDAY -> "Sá"
                else -> "Do"
            }
            val tomorrowFull = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(calendar.time)

            // 1. Recordatorio de Exámenes (noche antes)
            val tomorrowAssessments = assessments.filter {
                it.grade == null && (it.examDate.equals(tomorrowCode, ignoreCase = true) || it.examDate == tomorrowFull)
            }
            tomorrowAssessments.forEach { assessment ->
                val sub = subjects.find { it.id == assessment.subjectId }
                if (sub != null) {
                    NotificationHelper.sendNotification(
                        applicationContext,
                        "¡Examen Mañana!",
                        "Recuerda que mañana tienes evaluación de ${assessment.name} en ${sub.name}. ¡Mucho éxito!"
                    )
                }
            }

            // 2 & 3. Riesgos de Ausencia
            subjects.forEach { subject ->
                val subAbsences = absences.count { it.subjectId == subject.id }
                val maxAbsences = (subject.totalClasses * (100 - subject.requiredAttendancePercent)) / 100
                if (subAbsences > 0 && subAbsences == maxAbsences) {
                    NotificationHelper.sendNotification(
                        applicationContext,
                        "¡Alerta de Inasistencia!",
                        "En ${subject.name} estás al límite de faltas ($subAbsences/$maxAbsences). Una falta más y podrías perder la clase."
                    )
                } else if (subAbsences > maxAbsences) {
                    NotificationHelper.sendNotification(
                        applicationContext,
                        "Límite Excedido :(",
                        "Has faltado a ${subject.name} más veces de lo permitido ($subAbsences). Intenta hablar con el profesor para recuperar."
                    )
                }
            }

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}
