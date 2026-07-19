package com.aistudio.unibuddy.qywvsp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.animation.core.*

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aistudio.unibuddy.qywvsp.ui.*
import com.aistudio.unibuddy.qywvsp.ui.components.*
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.aistudio.unibuddy.qywvsp.data.Task
import com.aistudio.unibuddy.qywvsp.data.Subject
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.platform.testTag
import com.aistudio.unibuddy.qywvsp.ui.theme.*
import com.aistudio.unibuddy.qywvsp.R

@Composable
fun DashboardScreen(
    viewModel: UniBuddyViewModel,
    onNavigateToDetails: (Int?) -> Unit,
    onNavigateToGrades: (Int?) -> Unit,
    onConfigureRoute: () -> Unit,
    onNavigateToFocus: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToPensum: () -> Unit
) {
    val username by viewModel.username.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val absences by viewModel.absences.collectAsStateWithLifecycle()
    val isRaining by viewModel.isRaining.collectAsStateWithLifecycle()
    val buddyAccessory by viewModel.buddyAccessory.collectAsStateWithLifecycle()
    val buddyColorStr by viewModel.buddyColor.collectAsStateWithLifecycle()
    val baseTravelTime by viewModel.baseTravelTime.collectAsStateWithLifecycle()
    val tripRecords by viewModel.tripRecords.collectAsStateWithLifecycle()
    val arrivalMarginPref by viewModel.arrivalMarginPreference.collectAsStateWithLifecycle()
    val attendanceLogs by viewModel.attendanceLogs.collectAsStateWithLifecycle()
    val updateInfo by viewModel.updateInfo.collectAsStateWithLifecycle()
    val assessments by viewModel.assessments.collectAsStateWithLifecycle()
    val weatherDescription by viewModel.weatherDescription.collectAsStateWithLifecycle()
    val locationBasedTravelTime by viewModel.locationBasedTravelMinutes.collectAsStateWithLifecycle()
    val currentDistanceToCollege by viewModel.currentDistanceToCollege.collectAsStateWithLifecycle()
    val isOutOfRange by viewModel.isOutOfRange.collectAsStateWithLifecycle()
    val subjectImportanceMap by viewModel.subjectImportanceMap.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle(emptyList())
    val currentWeek by viewModel.currentWeekOfSemester.collectAsStateWithLifecycle()
    val isEvenWeek by viewModel.isEvenWeek.collectAsStateWithLifecycle()
    val isTripActive by viewModel.isTripActive.collectAsStateWithLifecycle()
    val tripElapsedSeconds by viewModel.tripElapsedSeconds.collectAsStateWithLifecycle()
    val semesterState by viewModel.semesterState.collectAsStateWithLifecycle()
    val buddyXp by viewModel.buddyXp.collectAsStateWithLifecycle()
    var isCelebrating by remember { mutableStateOf(false) }
    var showSalvavidasDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.buddyCelebrationEvent.collectLatest {
            isCelebrating = true
            delay(3000)
            isCelebrating = false
        }
    }

    val currentDayCode = when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Lu"
        Calendar.TUESDAY -> "Ma"
        Calendar.WEDNESDAY -> "Mi"
        Calendar.THURSDAY -> "Ju"
        Calendar.FRIDAY -> "Vi"
        Calendar.SATURDAY -> "Sá"
        else -> "Do"
    }

    val scrollState = rememberScrollState()
    val photoUri by viewModel.profilePhotoUri.collectAsStateWithLifecycle()

    // 1. Filter and sort today's classes with parity check
    val todayClasses by remember {
        derivedStateOf {
            subjects.filter { it.schedule.contains(currentDayCode, ignoreCase = true) }
                .mapNotNull { sub ->
                    val session = sub.sessions.firstOrNull { it.day.equals(currentDayCode, ignoreCase = true) }
                    if (session != null) {
                        val freq = session.safeFrequency
                        val matchesParity = when (freq) {
                            "Semanas Pares" -> isEvenWeek
                            "Semanas Impares" -> !isEvenWeek
                            else -> true
                        }
                        if (matchesParity) {
                            val parsed = com.aistudio.unibuddy.qywvsp.ui.parseStartTime(session.time)
                            if (parsed != null) {
                                val (h, m) = parsed
                                Triple(sub, session, h * 60 + m)
                            } else null
                        } else null
                    } else null
                }.sortedBy { it.third }
        }
    }

    // 2. Select the next active class today
    val nextClassInfo by remember {
        derivedStateOf {
            val now = Calendar.getInstance()
            val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
            
            // First find a class that hasn't finished yet (assuming 90 min class duration)
            todayClasses.firstOrNull { (_, _, startMinutes) ->
                nowMinutes < startMinutes + 90
            } ?: todayClasses.firstOrNull() // fallback to first class if all are in past
        }
    }

    val nextSubject = nextClassInfo?.first
    val nextSession = nextClassInfo?.second
    val nextClassTime = nextSession?.time ?: "Sin Horario"
    val classTimeFormatted = if (nextClassTime != "Sin Horario") com.aistudio.unibuddy.qywvsp.ui.formatTimeRange(androidx.compose.ui.platform.LocalContext.current, nextClassTime) else nextClassTime
    
    val nextSubjectAbsencesCount = remember(nextSubject, absences) {
        nextSubject?.id?.let { subId -> absences.count { it.subjectId == subId } } ?: 0
    }
    
    val nextSubjectImportance = remember(nextSubject, subjectImportanceMap) {
        nextSubject?.id?.let { subjectImportanceMap[it] } ?: "Media"
    }

    val isNextSubjectExamMode = remember(nextSubject, assessments) {
        nextSubject?.let { sub ->
            val dateFormatter = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val todayStr = dateFormatter.format(java.util.Date())
            assessments.any { it.subjectId == sub.id && it.grade == null && it.examDate == todayStr }
        } ?: false
    }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(nextSubject, nextClassTime) {
        if (nextSubject != null && nextClassTime != "Sin Horario") {
            val parsed = com.aistudio.unibuddy.qywvsp.ui.parseStartTime(nextClassTime)
            if (parsed != null) {
                val cal = java.util.Calendar.getInstance()
                cal.set(java.util.Calendar.HOUR_OF_DAY, parsed.first)
                cal.set(java.util.Calendar.MINUTE, parsed.second)
                cal.set(java.util.Calendar.SECOND, 0)
                val title = if (isNextSubjectExamMode) "Próximo Examen: ${nextSubject.name}" else "Próxima Clase: ${nextSubject.name}"
                val subtitle = if (isNextSubjectExamMode) "¡A punto de empezar!" else "Prepárate para la clase"
                com.aistudio.unibuddy.qywvsp.LiveCountdownService.start(context, cal.timeInMillis, title, subtitle)
            }
        } else {
            com.aistudio.unibuddy.qywvsp.LiveCountdownService.stop(context)
        }
    }

    val estimatedTravelMins = if (locationBasedTravelTime > 0) locationBasedTravelTime else baseTravelTime

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // --- 1. OTA Update Banner ---
        updateInfo?.let { info ->
            val localCtx = androidx.compose.ui.platform.LocalContext.current
            if (info.isUpdateAvailable) {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("ota_update_banner"),
                    colors = CardDefaults.cardColors(containerColor = ProBlue.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.5.dp, ProBlue.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = ProBlue, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("¡Nueva versión disponible!", fontWeight = FontWeight.Bold, color = ProBlue, fontSize = 14.sp)
                            Text("Versión ${info.versionName}", color = SlateGray, fontSize = 10.sp)
                        }
                        Button(
                            onClick = { UpdateManager.downloadAndInstallUpdate(localCtx, info.apkUrl, info.versionName) },
                            colors = ButtonDefaults.buttonColors(containerColor = ProBlue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Actualizar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- 2. Header simple ---
        val dateSdf = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
        val formattedDate = dateSdf.format(Date()).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!photoUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Avatar de usuario",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(1.dp, NavyBlue.copy(alpha = 0.1f), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(NavyBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (username ?: "Usuario").take(1).uppercase(Locale.getDefault()),
                            fontWeight = FontWeight.Bold,
                            color = NavyBlue,
                            fontSize = 18.sp
                        )
                    }
                }
                Column {
                    Text(
                        text = "¡Hola, ${username ?: "Usuario"}!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyBlue
                    )
                    Text(
                        text = formattedDate,
                        fontSize = 11.sp,
                        color = SlateGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Small weather status icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRaining) Icons.Rounded.Cloud else Icons.Rounded.WbSunny,
                    contentDescription = "Clima",
                    tint = if (isRaining) ProBlue else Amber,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // --- 2b. Aviso de clima ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = if (isRaining) Color(0xFFE8F0FE) else Color(0xFFFFF9C4)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(0.5.dp, (if (isRaining) ProBlue else Amber).copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isRaining) Icons.Rounded.Umbrella else Icons.Rounded.Thermostat,
                    contentDescription = null,
                    tint = if (isRaining) ProBlue else Amber,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (semesterState == "Vacaciones") {
                        "Pronóstico: $weatherDescription. ${if (isRaining) "Lleva paraguas si vas a salir a pasear." else "Excelente día para relajarse y disfrutar al aire libre."}"
                    } else {
                        "Pronóstico: $weatherDescription. ${if (isRaining) "Lleva paraguas para proteger tu UniBuddy." else "Excelente día para estudiar en el campus."}"
                    },
                    fontSize = 11.sp,
                    color = if (isRaining) NavyBlue else Color(0xFF5D4037),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // --- 2c. Buddy Interactive Calendar Prompt ---
        BuddyCalendarPromptWidget(viewModel = viewModel)

        // --- 2d. UniBuddy Salvavidas SOS Trigger ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showSalvavidasDialog = true }
                .testTag("unibuddy_salvavidas_trigger"),
            colors = CardDefaults.cardColors(containerColor = ProRed.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, ProRed.copy(alpha = 0.25f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ProRed.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MedicalServices,
                        contentDescription = "Salvavidas",
                        tint = ProRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "UniBuddy Salvavidas Académico",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyBlue
                    )
                    Text(
                        text = "Calculadora SOS: ¿Cuánto necesitas para salvar el semestre?",
                        fontSize = 10.sp,
                        color = SlateGray,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = ProRed,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        if (showSalvavidasDialog) {
            UniBuddySalvavidasDialog(
                viewModel = viewModel,
                onDismiss = { showSalvavidasDialog = false }
            )
        }

        // --- 3. Bloque del pet como elemento dominante ---
        val buddyLevel = 1 + (buddyXp / 100)
        val buddyTitle = when {
            buddyLevel < 5 -> "Novato"
            buddyLevel < 10 -> "Estudiante Promedio"
            buddyLevel < 20 -> "Veterano"
            else -> "Erudito"
        }
        val calculatedStress = if (semesterState == "Vacaciones") 0f else (assessments.count { it.grade == null } * 15f + absences.size * 5f).coerceIn(0f, 100f)
        val stressStatusText = if (semesterState == "Vacaciones") "Vacaciones" else when {
            calculatedStress > 70f -> "Crítico"
            calculatedStress > 40f -> "Elevado"
            else -> "Estable"
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(0.5.dp, NavyBlue.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ESTADO DE TU UNIBUDDY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SlateGray,
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(14.dp))
                
                // Dominant mascot rendering
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5F5F5))
                        .border(0.5.dp, NavyBlue.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    BuddyMascot(
                        pose = if (isCelebrating) "celebrating" else if (semesterState == "Vacaciones") "sleeping" else if (calculatedStress > 50) "worried" else "greeting",
                        modifier = Modifier.size(130.dp),
                        isHappy = semesterState == "Vacaciones" || calculatedStress <= 50 || isCelebrating,
                        isWorried = semesterState != "Vacaciones" && calculatedStress > 50,
                        mainColor = try { Color(android.graphics.Color.parseColor(buddyColorStr)) } catch(e: Exception) { ProBlue }
                    )
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                
                Text(
                    text = "Nivel $buddyLevel • $buddyTitle",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // XP Progress Bar
                val xpInCurrentLevel = buddyXp % 100
                Row(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { xpInCurrentLevel / 100f },
                        color = DarkGreen,
                        trackColor = Color(0xFFEEEEEE),
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(CircleShape)
                    )
                    Text(
                        text = "$xpInCurrentLevel/100 XP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateGray
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = if (semesterState == "Vacaciones") "Anímico: Descansando ($stressStatusText)" else "Anímico: ${if (calculatedStress > 50) "Preocupado ($stressStatusText)" else "Feliz y Saludable ($stressStatusText)"}",
                    fontSize = 11.sp,
                    color = if (semesterState != "Vacaciones" && calculatedStress > 50) Terracotta else DarkGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // --- 4. Bloque de trayecto / salida recomendada (2-Column Grid) ---
        val parsedTime = remember(nextClassTime) {
            if (nextClassTime != "Sin Horario") {
                com.aistudio.unibuddy.qywvsp.ui.parseStartTime(nextClassTime)
            } else {
                null
            }
        }
        val departureTimeFormatted = remember(parsedTime, baseTravelTime) {
            parsedTime?.let { (hour, min) ->
                var totalMin = hour * 60 + min - baseTravelTime
                if (totalMin < 0) totalMin += 24 * 60
                val depHour = (totalMin / 60) % 24
                val depMin = totalMin % 60
                val amPm = if (depHour >= 12) "PM" else "AM"
                val displayHour = when {
                    depHour == 0 -> 12
                    depHour > 12 -> depHour - 12
                    else -> depHour
                }
                String.format("%d:%02d %s", displayHour, depMin, amPm)
            } ?: "Sin salida"
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Columna 1: Estado del viaje actual
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onConfigureRoute() },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.5.dp, NavyBlue.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "VIAJE ACTUAL",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SlateGray,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isTripActive) MintGreen else SlateGray)
                        )
                        Text(
                            text = if (isTripActive) "Viaje Activo" else "Viaje Inactivo",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyBlue
                        )
                    }
                    if (isTripActive) {
                        val activeMinutes = tripElapsedSeconds / 60
                        val activeSeconds = tripElapsedSeconds % 60
                        Text(
                            text = String.format("%02d:%02d transcurrido", activeMinutes, activeSeconds),
                            fontSize = 11.sp,
                            color = SlateGray,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "Pulsa para iniciar ruta",
                            fontSize = 11.sp,
                            color = SlateGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Columna 2: Recomendación de salida
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onConfigureRoute() },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.5.dp, NavyBlue.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "SALIDA RECOMENDADA",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SlateGray,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (semesterState == "Vacaciones") "Vacaciones" else if (nextSubject != null) departureTimeFormatted else "Sin clases hoy",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = if (semesterState == "Vacaciones") MintGreen else if (nextSubject != null) ProBlue else SlateGray
                    )
                    Text(
                        text = if (semesterState == "Vacaciones") "Disfruta tu descanso" else if (nextSubject != null) "Para llegar a tiempo" else "Disfruta tu descanso",
                        fontSize = 11.sp,
                        color = SlateGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 4b. Mapa interactivo de trayecto ---
        InteractiveCommuteMap(
            isTripActive = isTripActive,
            tripElapsedSeconds = tripElapsedSeconds,
            currentDistanceToCollege = currentDistanceToCollege,
            locationBasedTravelTime = locationBasedTravelTime,
            semesterState = semesterState,
            onConfigureRoute = onConfigureRoute
        )

        Spacer(modifier = Modifier.height(10.dp))

        // --- 5. Avisos contextuales (Up to 2 dynamic alerts) ---
        val dynamicNotices = remember(subjects, absences, tasks, calculatedStress, semesterState) {
            val list = mutableListOf<Pair<String, String>>()
            
            if (semesterState == "Vacaciones") {
                list.add("¡Feliz Descanso!" to "Aprovecha el receso para relajarte, leer o avanzar en tus proyectos personales.")
                list.add("Siguiente Semestre" to "Tu UniBuddy te notificará cuando se acerque el inicio de clases.")
            } else {
                // Attendance critical alert
                val criticalAttendanceSubject = subjects.find { sub ->
                    val count = absences.count { it.subjectId == sub.id }
                    count >= 3
                }
                if (criticalAttendanceSubject != null) {
                    val count = absences.count { it.subjectId == criticalAttendanceSubject.id }
                    list.add("¡Alerta de Faltas!" to "Tienes $count inasistencias en ${criticalAttendanceSubject.name}. ¡No acumules más!")
                }
                
                // Pending tasks alert
                val pendingTasksCount = tasks.count { !it.isCompleted }
                if (pendingTasksCount > 0) {
                    list.add("Tareas Pendientes" to "Tienes $pendingTasksCount entregas pendientes en tu agenda académica.")
                }
                
                // High stress alert fallback
                if (list.size < 2 && calculatedStress > 60f) {
                    list.add("Nivel de Estrés Alto" to "Tu UniBuddy está preocupado. Toma un respiro en la pestaña Enfoque.")
                }
                
                // General tip fallback
                if (list.isEmpty()) {
                    list.add("Consejo del Día" to "Mantén un registro al día de tus asistencias automáticas GPS.")
                }
            }
            
            list.take(2)
        }

        if (dynamicNotices.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.5.dp, Terracotta.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "AVISOS IMPORTANTES",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Terracotta,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    dynamicNotices.forEach { (title, desc) ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Terracotta, modifier = Modifier.size(14.dp))
                            Column {
                                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                                Text(desc, fontSize = 11.sp, color = SlateGray, lineHeight = 14.sp)
                            }
                        }
                    }
                }
            }
        }

        // --- 6. Próxima clase ---
        val isHoliday = viewModel.isNicaraguaHoliday(System.currentTimeMillis())
        if (semesterState == "Vacaciones") {
            Card(
                modifier = Modifier.fillMaxWidth().height(110.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavyBlue),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("¡Disfruta tus Vacaciones!", color = Bone, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("No hay clases ni pendientes programados. ¡A descansar!", color = Bone.copy(alpha = 0.8f), fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        } else if (isHoliday) {
            Card(
                modifier = Modifier.fillMaxWidth().height(110.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavyBlue),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("¡Hoy es Feriado Nacional!", color = Bone, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Descansa y recarga energías.", color = Bone.copy(alpha=0.8f), fontSize = 12.sp)
                    }
                }
            }
        } else {
            HeroNextClassCard(
                subject = nextSubject,
                classTime = classTimeFormatted, 
                totalAbsencesCount = nextSubjectAbsencesCount,
                isExamMode = isNextSubjectExamMode,
                importanceLevel = nextSubjectImportance,
                estimatedTravelMinutes = estimatedTravelMins,
                isOutOfRange = isOutOfRange
            )
        }
    }
}

@Composable
fun InteractiveCommuteMap(
    isTripActive: Boolean,
    tripElapsedSeconds: Int,
    currentDistanceToCollege: Double?,
    locationBasedTravelTime: Int,
    semesterState: String,
    onConfigureRoute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isVacation = semesterState == "Vacaciones"
    
    // Animating dash phase for the route path to make it crawl/animate nicely
    val infiniteTransition = rememberInfiniteTransition(label = "map_animations")
    val dashPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dash_phase"
    )

    // Moving progress indicator for active trip (restarts/cycles or is based on elapsed time)
    val tripProgress by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "trip_progress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable { onConfigureRoute() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), // Deep Slate Dark Blue map background
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Draw Map Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                
                // Drawing background streets
                val streetColor = Color(0xFF334155).copy(alpha = 0.3f)
                drawLine(color = streetColor, start = Offset(w * 0.1f, 0f), end = Offset(w * 0.1f, h), strokeWidth = 2f)
                drawLine(color = streetColor, start = Offset(w * 0.5f, 0f), end = Offset(w * 0.5f, h), strokeWidth = 2f)
                drawLine(color = streetColor, start = Offset(w * 0.85f, 0f), end = Offset(w * 0.85f, h), strokeWidth = 2f)
                drawLine(color = streetColor, start = Offset(0f, h * 0.3f), end = Offset(w, h * 0.3f), strokeWidth = 2f)
                drawLine(color = streetColor, start = Offset(0f, h * 0.7f), end = Offset(w, h * 0.7f), strokeWidth = 2f)
                
                // Secondary curved faint roads
                val roadPath1 = Path().apply {
                    moveTo(0f, h * 0.2f)
                    quadraticTo(w * 0.3f, h * 0.5f, w, h * 0.4f)
                }
                drawPath(path = roadPath1, color = streetColor, style = Stroke(width = 4f))

                val roadPath2 = Path().apply {
                    moveTo(w * 0.2f, h)
                    quadraticTo(w * 0.7f, h * 0.4f, w * 0.9f, 0f)
                }
                drawPath(path = roadPath2, color = streetColor, style = Stroke(width = 4f))

                // main active route path connecting home/start to destination
                val routePath = Path().apply {
                    moveTo(w * 0.15f, h * 0.75f) // start node
                    cubicTo(
                        w * 0.4f, h * 0.2f,
                        w * 0.6f, h * 0.9f,
                        w * 0.85f, h * 0.25f // end node
                    )
                }

                // Draw thick glow path
                drawPath(
                    path = routePath,
                    color = if (isVacation) MintGreen.copy(alpha = 0.2f) else ProBlue.copy(alpha = 0.2f),
                    style = Stroke(width = 10f, cap = StrokeCap.Round)
                )

                // Draw active dashed path
                drawPath(
                    path = routePath,
                    color = if (isVacation) MintGreen else ProBlue,
                    style = Stroke(
                        width = 4f,
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(15f, 15f),
                            phase = -dashPhase
                        )
                    )
                )

                // Start Node (Mi Casa o Inicio)
                drawCircle(
                    color = if (isVacation) MintGreen.copy(alpha = 0.3f) else ProBlue.copy(alpha = 0.3f),
                    radius = 16f,
                    center = Offset(w * 0.15f, h * 0.75f)
                )
                drawCircle(
                    color = if (isVacation) MintGreen else ProBlue,
                    radius = 8f,
                    center = Offset(w * 0.15f, h * 0.75f)
                )

                // End Node (Universidad o Destino Vacacional)
                drawCircle(
                    color = if (isVacation) Terracotta.copy(alpha = 0.3f) else Amber.copy(alpha = 0.3f),
                    radius = 20f,
                    center = Offset(w * 0.85f, h * 0.25f)
                )
                drawCircle(
                    color = if (isVacation) Terracotta else Amber,
                    radius = 10f,
                    center = Offset(w * 0.85f, h * 0.25f)
                )

                // Draw moving mascot pointer if trip active, or draw stationary halfway
                val activeProgress = if (isTripActive) tripProgress else 0.5f
                
                // Get coordinates along the cubic Bezier path
                val t = activeProgress
                val mt = 1f - t
                val p0 = Offset(w * 0.15f, h * 0.75f)
                val p1 = Offset(w * 0.4f, h * 0.2f)
                val p2 = Offset(w * 0.6f, h * 0.9f)
                val p3 = Offset(w * 0.85f, h * 0.25f)

                val mascotX = mt*mt*mt * p0.x + 3*mt*mt*t * p1.x + 3*mt*t*t * p2.x + t*t*t * p3.x
                val mascotY = mt*mt*mt * p0.y + 3*mt*mt*t * p1.y + 3*mt*t*t * p2.y + t*t*t * p3.y

                // Outer glowing circle for active buddy pointer
                drawCircle(
                    color = Color.White.copy(alpha = 0.25f),
                    radius = 18f,
                    center = Offset(mascotX, mascotY)
                )
                drawCircle(
                    color = if (isVacation) MintGreen else ProBlue,
                    radius = 10f,
                    center = Offset(mascotX, mascotY)
                )
                // Small core dot
                drawCircle(
                    color = Color.White,
                    radius = 4f,
                    center = Offset(mascotX, mascotY)
                )
            }

            // High impact overlay labels: Home / Destination Name
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left side: Origin
                Column {
                    Text(
                        text = "ORIGEN",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (isVacation) "Mi Hogar" else "Mi Casa",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Right side: Destination
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "DESTINO",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (isVacation) "Laguna de Apoyo" else "Uni Campus",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Big ETA Overlay card inside the map
            Card(
                modifier = Modifier
                    .padding(12.dp)
                    .width(170.dp)
                    .align(Alignment.BottomStart),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.92f)),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = if (isVacation) "TIEMPO DE DESCANSO" else "TRAYECTO ESTIMADO",
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    val distanceText = currentDistanceToCollege?.let { String.format("%.1f km", it) } ?: "2.8 km"
                    val minutesText = if (isVacation) "Vacaciones" else "$locationBasedTravelTime min"
                    
                    Text(
                        text = minutesText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isVacation) MintGreen else Color(0xFF60A5FA)
                    )
                    
                    Text(
                        text = if (isVacation) "Disfrutando el receso" else "Distancia: $distanceText",
                        fontSize = 9.5.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Quick instructions tag overlay
            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.BottomEnd)
                    .background(
                        color = if (isTripActive) MintGreen.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .border(
                        width = 0.5.dp,
                        color = if (isTripActive) MintGreen else Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isTripActive) Icons.Rounded.Navigation else Icons.Rounded.Map,
                        contentDescription = null,
                        tint = if (isTripActive) MintGreen else Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = if (isTripActive) "EN RUTA" else "VER MAPA",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isTripActive) MintGreen else Color.White
                    )
                }
            }
        }
    }
}
