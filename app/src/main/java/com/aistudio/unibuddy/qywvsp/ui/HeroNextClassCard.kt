package com.aistudio.unibuddy.qywvsp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.unibuddy.qywvsp.data.Subject
import com.aistudio.unibuddy.qywvsp.ui.theme.*
import java.util.Calendar
import java.util.Locale

@Composable
fun HeroNextClassCard(
    subject: Subject?,
    classTime: String,
    totalAbsencesCount: Int,
    isExamMode: Boolean,
    importanceLevel: String,
    estimatedTravelMinutes: Int,
    isOutOfRange: Boolean = false
) {
    if (subject == null) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = SlateGray, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Día libre", fontSize = 24.sp, fontWeight = FontWeight.Black, color = NavyBlue)
                Text("No tienes más clases programadas para hoy.", color = SlateGray, textAlign = TextAlign.Center)
            }
        }
        return
    }

    // Calculate maximum permitted absences and remaining ones
    val maxAbs = subject.totalClasses - kotlin.math.ceil(subject.totalClasses * (subject.requiredAttendancePercent / 100.0)).toInt()
    val remainingAbsences = (maxAbs - totalAbsencesCount).coerceAtLeast(0)
    val isAbsencesCritical = remainingAbsences <= 2

    // Parse the start time of the class to compute departure recommendation
    val parsedStartTime = parseStartTime(classTime)
    
    var departureTimeStr = "--:--"
    var statusText = "Horario no especificado"
    var statusColor = SlateGray
    var statusBgColor = BackgroundGray
    var isLate = false

    if (parsedStartTime != null) {
        val (startHour, startMin) = parsedStartTime
        val totalStartMins = startHour * 60 + startMin
        val now = Calendar.getInstance()
        val nowMins = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        
        if (isOutOfRange && (totalStartMins - nowMins) < estimatedTravelMinutes) {
            statusText = "Uff no llegas si sales ahora"
            statusColor = Terracotta
            statusBgColor = Terracotta.copy(alpha = 0.1f)
            departureTimeStr = "--:--"
        } else {
            // Departure time = start time - travel time - 10 mins buffer
            var departureMins = totalStartMins - estimatedTravelMinutes - 10
            if (departureMins < 0) {
                departureMins += 24 * 60
            }
            
            val depHour = (departureMins / 60) % 24
            val depMin = departureMins % 60
            departureTimeStr = formatTime12Hour(depHour, depMin)

            when {
                nowMins > totalStartMins -> {
                    isLate = true
                    val minsPast = nowMins - totalStartMins
                    statusText = "Clase iniciada hace $minsPast min"
                    statusColor = Terracotta
                    statusBgColor = Terracotta.copy(alpha = 0.1f)
                }
                nowMins > departureMins -> {
                    isLate = true
                    val minsDelayed = nowMins - departureMins
                    statusText = "Deberías estar en camino (atraso de $minsDelayed min)"
                    statusColor = Terracotta
                    statusBgColor = Terracotta.copy(alpha = 0.1f)
                }
                nowMins == departureMins -> {
                    statusText = "¡Debes salir ahora mismo!"
                    statusColor = Color(0xFFD97706) // Darker Amber
                    statusBgColor = Color(0xFFFEF3C7)
                }
                else -> {
                    val minsLeft = departureMins - nowMins
                    statusText = "A tiempo (sugerido salir en $minsLeft min)"
                    statusColor = DarkGreen
                    statusBgColor = MintGreen.copy(alpha = 0.1f)
                }
            }
        }
    }

    // Determine attendance urgency decision (Should I go or not?)
    val attendanceUrgency: String
    val attendanceReason: String
    val urgencyColor: Color
    val urgencyBgColor: Color

    when {
        isExamMode -> {
            attendanceUrgency = "ASISTENCIA OBLIGATORIA"
            attendanceReason = "Tienes una evaluación programada para hoy. ¡No faltes!"
            urgencyColor = Terracotta
            urgencyBgColor = Terracotta.copy(alpha = 0.12f)
        }
        isAbsencesCritical -> {
            attendanceUrgency = "ASISTENCIA CRÍTICA"
            attendanceReason = "Te quedan pocas faltas permitidas ($remainingAbsences de $maxAbs). ¡Evita reprobar!"
            urgencyColor = Terracotta
            urgencyBgColor = Terracotta.copy(alpha = 0.12f)
        }
        importanceLevel == "Alta" -> {
            attendanceUrgency = "ASISTENCIA RECOMENDADA"
            attendanceReason = "Esta materia tiene alta relevancia académica."
            urgencyColor = ProBlue
            urgencyBgColor = ProBlue.copy(alpha = 0.12f)
        }
        else -> {
            attendanceUrgency = "ASISTENCIA OPCIONAL"
            attendanceReason = "Tienes buen margen de faltas ($remainingAbsences restantes) y la materia tiene prioridad estándar."
            urgencyColor = DarkGreen
            urgencyBgColor = MintGreen.copy(alpha = 0.12f)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Bone)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Urgency Badge & Class Start Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(urgencyBgColor)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = attendanceUrgency,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = urgencyColor
                    )
                }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BackgroundGray)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = classTime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyBlue
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            // Subject Name
            Text(
                text = subject.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = NavyBlue,
                lineHeight = 28.sp
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Attendance Urgency Reason Description
            Text(
                text = attendanceReason,
                fontSize = 13.sp,
                color = SlateGray,
                lineHeight = 16.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            HorizontalDivider(color = Bone)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Departure Time Card Segment
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large departure clock representation
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(BackgroundGray)
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "HORA DE SALIDA",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = departureTimeStr,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = NavyBlue
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Route travel duration & current status summary
                Column(
                    modifier = Modifier.weight(1.9f)
                ) {
                    // Current Status indicator chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusBgColor)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = if (isOutOfRange) "Trayecto ultra largo ($estimatedTravelMinutes min). Verifica tu ubicación." else "Trayecto estimado de $estimatedTravelMinutes min de puerta a puerta.",
                        fontSize = 12.sp,
                        color = SlateGray,
                        lineHeight = 15.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Extra stats: Importance & absences margin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Importance Level
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BackgroundGray)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PriorityHigh,
                        contentDescription = null,
                        tint = if (importanceLevel == "Alta") Terracotta else SlateGray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("IMPORTANCIA", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SlateGray)
                        Text(importanceLevel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                    }
                }
                
                // Absences Margin
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BackgroundGray)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("FALTAS RESTANTES", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SlateGray)
                        Spacer(modifier = Modifier.height(4.dp))
                        SubjectLivesIndicator(
                            subAbsCount = maxAbs - remainingAbsences,
                            maxAbs = maxAbs,
                            heartSize = 14.dp
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime12Hour(hour: Int, minute: Int): String {
    val amPm = if (hour >= 12) "PM" else "AM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format(Locale.US, "%d:%02d %s", displayHour, minute, amPm)
}
