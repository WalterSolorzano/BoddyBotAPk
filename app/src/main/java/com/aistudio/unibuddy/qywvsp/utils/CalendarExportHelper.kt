package com.aistudio.unibuddy.qywvsp.utils

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import android.widget.Toast

object CalendarExportHelper {
    fun exportClassToCalendar(
        context: Context,
        subjectName: String,
        dayCode: String,
        sessionTimeStr: String
    ) {
        try {
            val calendarDayOfWeek = when (dayCode.trim().lowercase()) {
                "lu" -> java.util.Calendar.MONDAY
                "ma" -> java.util.Calendar.TUESDAY
                "mi" -> java.util.Calendar.WEDNESDAY
                "ju" -> java.util.Calendar.THURSDAY
                "vi" -> java.util.Calendar.FRIDAY
                "sá", "sa" -> java.util.Calendar.SATURDAY
                else -> java.util.Calendar.SUNDAY
            }

            val timeParts = sessionTimeStr.split("-")
            val startTimeStr = timeParts.getOrNull(0)?.trim() ?: "08:00"
            val endTimeStr = timeParts.getOrNull(1)?.trim() ?: "10:00"

            val startParts = startTimeStr.split(":")
            val startHour = startParts.getOrNull(0)?.toIntOrNull() ?: 8
            val startMin = startParts.getOrNull(1)?.toIntOrNull() ?: 0

            val endParts = endTimeStr.split(":")
            val endHour = endParts.getOrNull(0)?.toIntOrNull() ?: 10
            val endMin = endParts.getOrNull(1)?.toIntOrNull() ?: 0

            val cal = java.util.Calendar.getInstance()
            // Find next occurrence of the day
            while (cal.get(java.util.Calendar.DAY_OF_WEEK) != calendarDayOfWeek) {
                cal.add(java.util.Calendar.DATE, 1)
            }
            cal.set(java.util.Calendar.HOUR_OF_DAY, startHour)
            cal.set(java.util.Calendar.MINUTE, startMin)
            cal.set(java.util.Calendar.SECOND, 0)

            val endCal = java.util.Calendar.getInstance().apply {
                timeInMillis = cal.timeInMillis
                set(java.util.Calendar.HOUR_OF_DAY, endHour)
                set(java.util.Calendar.MINUTE, endMin)
            }

            val rruleDay = when (dayCode.trim().lowercase()) {
                "lu" -> "MO"
                "ma" -> "TU"
                "mi" -> "WE"
                "ju" -> "TH"
                "vi" -> "FR"
                "sá", "sa" -> "SA"
                else -> "SU"
            }
            val rrule = "FREQ=WEEKLY;BYDAY=$rruleDay"

            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, "Clase: $subjectName")
                putExtra(CalendarContract.Events.DESCRIPTION, "Clase semanal de $subjectName")
                putExtra(CalendarContract.Events.EVENT_LOCATION, "Universidad")
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.timeInMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endCal.timeInMillis)
                putExtra(CalendarContract.Events.RRULE, rrule)
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "No se encontró una app de calendario", Toast.LENGTH_SHORT).show()
        }
    }

    fun exportExamToCalendar(
        context: Context,
        subjectName: String,
        examTitle: String,
        dateStr: String // Format: "dd MMM yyyy"
    ) {
        try {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date = sdf.parse(dateStr) ?: Date()

            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, "Examen: $examTitle - $subjectName")
                putExtra(CalendarContract.Events.DESCRIPTION, "Examen de la materia $subjectName")
                
                val cal = java.util.Calendar.getInstance().apply {
                    time = date
                    set(java.util.Calendar.HOUR_OF_DAY, 8)
                    set(java.util.Calendar.MINUTE, 0)
                }
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.timeInMillis)
                
                cal.add(java.util.Calendar.HOUR_OF_DAY, 2)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, cal.timeInMillis)
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "No se encontró una app de calendario", Toast.LENGTH_SHORT).show()
        }
    }

    fun exportEventToCalendar(
        context: Context,
        title: String,
        description: String,
        dateStr: String // Expected format: "dd MMM yyyy" or fallback to today
    ) {
        try {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date = sdf.parse(dateStr) ?: Date()
            
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, title)
                putExtra(CalendarContract.Events.DESCRIPTION, description)
                
                // Add default time: 8:00 AM on the given day
                val cal = java.util.Calendar.getInstance().apply {
                    time = date
                    set(java.util.Calendar.HOUR_OF_DAY, 8)
                    set(java.util.Calendar.MINUTE, 0)
                }
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.timeInMillis)
                
                cal.add(java.util.Calendar.HOUR_OF_DAY, 2) // Default 2 hours
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, cal.timeInMillis)
            }
            
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "No se encontró una app de calendario", Toast.LENGTH_SHORT).show()
        }
    }
}
