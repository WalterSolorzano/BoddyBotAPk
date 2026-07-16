package com.aistudio.unibuddy.qywvsp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.unibuddy.qywvsp.data.Subject
import com.aistudio.unibuddy.qywvsp.ui.theme.*
import java.util.Calendar

@Composable
fun SmartRouteReminderCard(
    subjects: List<Subject>,
    baseTravelTime: Int,
    distanceKm: Double = 0.0,
    onConfigureRoute: () -> Unit,
    weatherDescription: String = "Despejado",
    isRaining: Boolean = false,
    isOutOfRange: Boolean = false,
    modifier: Modifier = Modifier
) {
    val currentCalendar = Calendar.getInstance()
    val nowHour = currentCalendar.get(Calendar.HOUR_OF_DAY)
    val nowMin = currentCalendar.get(Calendar.MINUTE)
    val nowTotalMins = nowHour * 60 + nowMin

    val daysOfWeekCodes = listOf("Do", "Lu", "Ma", "Mi", "Ju", "Vi", "Sá")
    val currentDayIndex = currentCalendar.get(Calendar.DAY_OF_WEEK) - 1
    val currentDayCode = daysOfWeekCodes[currentDayIndex]

    val nextClassInfo = remember(subjects, currentDayCode, nowTotalMins) {
        var foundSubject: Subject? = null
        var foundTime = ""
        var daysAhead = 0

        for (i in 0..7) {
            val checkIndex = (currentDayIndex + i) % 7
            val checkCode = daysOfWeekCodes[checkIndex]
            
            val dayClasses = subjects.filter { sub -> sub.sessions.any { it.day.equals(checkCode, ignoreCase = true) } }
                .mapNotNull { sub ->
                    val session = sub.sessions.firstOrNull { it.day.equals(checkCode, ignoreCase = true) }
                    if (session != null) {
                        val parsed = parseStartTime(session.time)
                        if (parsed != null) {
                            val (h, m) = parsed
                            if (i == 0) {
                                if ((h * 60 + m) + 120 > nowTotalMins) {
                                    return@mapNotNull Triple(sub, session.time, i)
                                }
                            } else {
                                return@mapNotNull Triple(sub, session.time, i)
                            }
                        }
                    }
                    null
                }.sortedBy { it.third }
                
            if (dayClasses.isNotEmpty()) {
                foundSubject = dayClasses.first().first
                foundTime = dayClasses.first().second
                daysAhead = i
                break
            }
        }
        
        if (foundSubject != null) Triple(foundSubject, foundTime, daysAhead) else null
    }

    val firstSubject = nextClassInfo?.first
    val firstClassTime = nextClassInfo?.second ?: ""
    val daysAhead = nextClassInfo?.third ?: 0
    val isTomorrow = daysAhead == 1

    val parsedTime = remember(firstClassTime) {
        if (firstClassTime.isNotBlank()) {
            parseStartTime(firstClassTime)
        } else {
            null
        }
    }

    val departureTimeFormatted = remember(parsedTime, baseTravelTime) {
        parsedTime?.let { (hour, min) ->
            val (depHour, depMin) = calculateDepartureTime(hour, min, baseTravelTime)
            formatTime12Hour(depHour, depMin)
        }
    }

    val classStartTimeFormatted = remember(parsedTime) {
        parsedTime?.let { (hour, min) ->
            formatTime12Hour(hour, min)
        }
    }

    // Check if user is currently past departure time
    val isPastDeparture = remember(parsedTime, baseTravelTime, daysAhead) {
        parsedTime?.let { (hour, min) ->
            val totalClassMinsFromNow = (daysAhead * 24 * 60) + (hour * 60 + min) - nowTotalMins
            val depMinsFromNow = totalClassMinsFromNow - baseTravelTime - 10 // 10 mins buffer
            depMinsFromNow <= 0
        } ?: false
    }
    
    val departureDayName = remember(daysAhead) {
        if (daysAhead == 0) "hoy"
        else if (daysAhead == 1) "mañana"
        else {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, daysAhead)
            val format = java.text.SimpleDateFormat("EEEE", java.util.Locale("es", "ES"))
            "el " + format.format(cal.time).replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Bone),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = null,
                        tint = NavyBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ruta Inteligente",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyBlue
                    )
                }
                
                IconButton(
                    onClick = onConfigureRoute,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configurar Ruta",
                        tint = SlateGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (firstSubject == null || departureTimeFormatted == null) {
                // No classes or no valid time
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Hoy es un día libre",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "No tienes clases programadas para el día de hoy. Aprovecha el tiempo para descansar, organizar tus apuntes o avanzar en tus proyectos.",
                        fontSize = 13.sp,
                        color = SlateGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            } else {
                // We have a class and departure time!
                val statusText = if (isOutOfRange && isPastDeparture) {
                    "Uff no llegas si sales ahora."
                } else if (isOutOfRange && !isPastDeparture) {
                    "Debes salir $departureDayName a las ${departureTimeFormatted} para llegar a tiempo (trayecto largo)."
                } else if (daysAhead > 0) {
                    val clothing = if (isRaining) "paraguas y abrigo" else "ropa cómoda"
                    "${departureDayName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }} debes salir a las ${departureTimeFormatted}. El clima estará ${weatherDescription.lowercase()}, lleva $clothing."
                } else if (isPastDeparture) {
                    "¡Deberías ir saliendo! Estás lejos y tu clase es pronto."
                } else {
                    val clothing = if (isRaining) "tu paraguas" else "todo lo necesario"
                    "Tienes tiempo. Prepárate con calma para salir a las $departureTimeFormatted. No olvides $clothing."
                }
                val statusColor = if (daysAhead > 0 && !isPastDeparture) NavyBlue else if (isPastDeparture) Color.White else DarkGreen
                val statusBg = if (daysAhead > 0 && !isPastDeparture) Color(0xFFF0F4FA) else if (isPastDeparture) Terracotta else Color(0xFFF3FAF7)

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (daysAhead > 0) "Próxima clase ($departureDayName):" else "Siguiente clase de hoy:",
                                fontSize = 12.sp,
                                color = SlateGray,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = firstSubject.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyBlue,
                                maxLines = 1
                            )
                            Text(
                                text = "Inicia a las ${classStartTimeFormatted ?: firstClassTime}",
                                fontSize = 13.sp,
                                color = SlateGray
                            )
                            if (isOutOfRange) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Terracotta, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "Fuera de Rango (>50 km)",
                                        fontSize = 11.sp,
                                        color = Terracotta,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else if (distanceKm > 0.0) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = SlateGray, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "A ${String.format(java.util.Locale.US, "%.1f", distanceKm)} km de distancia",
                                        fontSize = 11.sp,
                                        color = SlateGray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Large beautiful departure clock visual
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .background(NavyBlue.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = NavyBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = departureTimeFormatted,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = NavyBlue
                            )
                            Text(
                                text = "Salida Máx.",
                                fontSize = 10.sp,
                                color = SlateGray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dynamic alert box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(statusBg, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(statusColor, RoundedCornerShape(50))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = statusText,
                            fontSize = 12.sp,
                            color = statusColor,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Basado en tu tiempo de viaje promedio de $baseTravelTime minutos configurado en tu perfil.",
                        fontSize = 11.sp,
                        color = SlateGray,
                        textAlign = TextAlign.Start,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

// Helper parsers and calculations
fun parseSingleTime(timeStr: String): Pair<Int, Int>? {
    try {
        val lower = timeStr.lowercase().replace(".", "").replace(" ", "")
        val isPm = lower.contains("pm")
        val isAm = lower.contains("am")
        
        val digitsAndColon = lower.replace(Regex("[^0-9:]"), "")
        if (digitsAndColon.contains(":")) {
            val parts = digitsAndColon.split(":")
            if (parts.size >= 2) {
                var hour = parts[0].toIntOrNull() ?: return null
                val min = parts[1].toIntOrNull() ?: 0
                if (isPm && hour < 12) hour += 12
                if (isAm && hour == 12) hour = 0
                return Pair(hour, min)
            }
        } else {
            val digitsOnly = lower.replace(Regex("[^0-9]"), "")
            var hour = digitsOnly.toIntOrNull() ?: return null
            val min = 0
            if (isPm && hour < 12) hour += 12
            if (isAm && hour == 12) hour = 0
            return Pair(hour, min)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun parseStartTime(timeStr: String): Pair<Int, Int>? {
    val firstPart = timeStr.substringBefore("-").trim()
    return parseSingleTime(firstPart)
}

fun parseTimeRange(timeRangeStr: String): Pair<Pair<Int, Int>, Pair<Int, Int>>? {
    try {
        val parts = timeRangeStr.split("-")
        if (parts.size == 2) {
            val start = parseSingleTime(parts[0])
            val end = parseSingleTime(parts[1])
            if (start != null && end != null) {
                return Pair(start, end)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

private fun calculateDepartureTime(startHour: Int, startMin: Int, travelTimeMinutes: Int): Pair<Int, Int> {
    var totalMin = startHour * 60 + startMin - travelTimeMinutes
    if (totalMin < 0) {
        totalMin += 24 * 60
    }
    val depHour = (totalMin / 60) % 24
    val depMin = totalMin % 60
    return Pair(depHour, depMin)
}

private fun formatTime12Hour(hour: Int, minute: Int): String {
    val amPm = if (hour >= 12) "PM" else "AM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format("%d:%02d %s", displayHour, minute, amPm)
}
