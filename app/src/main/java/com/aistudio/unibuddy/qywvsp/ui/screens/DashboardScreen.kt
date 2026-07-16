package com.aistudio.unibuddy.qywvsp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aistudio.unibuddy.qywvsp.ui.*
import com.aistudio.unibuddy.qywvsp.ui.components.*
import java.util.Calendar
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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.aistudio.unibuddy.qywvsp.data.Task
import com.aistudio.unibuddy.qywvsp.data.Subject
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.aistudio.unibuddy.qywvsp.ui.theme.*
import com.aistudio.unibuddy.qywvsp.R

@Composable
fun DashboardScreen(
    viewModel: UniBuddyViewModel,
    onNavigateToDetails: (Int) -> Unit,
    onNavigateToGrades: (Int) -> Unit,
    onConfigureRoute: () -> Unit,
    onNavigateToFocus: () -> Unit,
    onNavigateToStats: () -> Unit
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
    val isTripActive by viewModel.isTripActive.collectAsStateWithLifecycle()
    val tripElapsedSeconds by viewModel.tripElapsedSeconds.collectAsStateWithLifecycle()

    val todayExam = remember(assessments) {
        val calendar = Calendar.getInstance()
        val currentDayCode = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Lu"
            Calendar.TUESDAY -> "Ma"
            Calendar.WEDNESDAY -> "Mi"
            Calendar.THURSDAY -> "Ju"
            Calendar.FRIDAY -> "Vi"
            Calendar.SATURDAY -> "Sá"
            else -> "Do"
        }
        assessments.firstOrNull { it.grade == null && it.examDate.trim().equals(currentDayCode, ignoreCase = true) }
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
    var activeTab by remember { mutableStateOf("hoy") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // OTA Update Banner
        updateInfo?.let { info ->
            val localCtx = androidx.compose.ui.platform.LocalContext.current
            if (info.isUpdateAvailable) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = ProBlue.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = ProBlue, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("¡Nueva versión disponible!", fontWeight = FontWeight.Bold, color = ProBlue, fontSize = 16.sp)
                            Text("Versión ${info.versionName}: ${info.releaseNotes}", color = SlateGray, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                        Button(
                            onClick = { UpdateManager.downloadAndInstallUpdate(localCtx, info.apkUrl, info.versionName) },
                            colors = ButtonDefaults.buttonColors(containerColor = ProBlue)
                        ) {
                            Text("Actualizar", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Dashboard Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hola, $username",
                    style = MaterialTheme.typography.headlineMedium,
                    color = NavyBlue,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "¡Qué bueno verte hoy!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateGray
                )
            }
            
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(BackgroundGray),
                contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Profile Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Rounded.Person, contentDescription = null, tint = NavyBlue)
                }
            }
        }
        
        // Stylized Tab Segment Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundGray, RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                "hoy" to Pair("Hoy", Icons.Rounded.Today),
                "estudios" to Pair("Estudios", Icons.Rounded.School),
                "trayecto" to Pair("Trayecto", Icons.Rounded.Map)
            ).forEach { (tabId, tabData) ->
                val (label, icon) = tabData
                val isSelected = activeTab == tabId
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) NavyBlue else Color.Transparent)
                        .clickable { activeTab = tabId }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else SlateGray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else SlateGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        val examDateFormatter = remember { java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
        val tomorrowCal = remember { java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, 1) } }
        val tomorrowStr = remember(tomorrowCal) { examDateFormatter.format(tomorrowCal.time) }
        val examTomorrow = remember(assessments, tomorrowStr) { 
            assessments.any { it.grade == null && it.examDate == tomorrowStr }
        }
        val isHoliday = viewModel.isNicaraguaHoliday(System.currentTimeMillis())

        if (activeTab == "hoy") {
            AdviceCarouselWidget(subjects, assessments, onNavigateToDetails)
            
            if (currentWeek >= 14) {
                val examsPlanned = assessments.any { it.name.contains("Examen", ignoreCase = true) }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (examsPlanned) DarkGreen.copy(alpha=0.1f) else Terracotta.copy(alpha=0.1f)),
                    border = BorderStroke(1.dp, if (examsPlanned) DarkGreen.copy(alpha=0.5f) else Terracotta.copy(alpha=0.5f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (examsPlanned) Icons.Default.CheckCircle else Icons.Default.DateRange, contentDescription = null, tint = if (examsPlanned) DarkGreen else Terracotta, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Semana $currentWeek", fontWeight = FontWeight.Bold, color = if (examsPlanned) DarkGreen else Terracotta, fontSize = 14.sp)
                            Text(if (examsPlanned) "¡Excelente! Ya planificaste algunos exámenes." else "¿Ya planificaron los exámenes finales? Añade las fechas de tus evaluaciones.", fontSize = 12.sp, color = NavyBlue)
                        }
                    }
                }
            }
            
            if (isHoliday) {
                Card(
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyBlue)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("¡Hoy es Feriado Nacional!", color = Bone, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Descansa y recarga energías.", color = Bone.copy(alpha=0.8f), fontSize = 14.sp)
                        }
                    }
                }
            } else {
                // 1. Filter and sort today's classes
                val todayClasses = remember(subjects, currentDayCode) {
                    subjects.filter { it.schedule.contains(currentDayCode, ignoreCase = true) }
                        .mapNotNull { sub ->
                            val session = sub.sessions.firstOrNull { it.day.equals(currentDayCode, ignoreCase = true) }
                            if (session != null) {
                                val parsed = com.aistudio.unibuddy.qywvsp.ui.parseStartTime(session.time)
                                if (parsed != null) {
                                    val (h, m) = parsed
                                    Triple(sub, session, h * 60 + m)
                                } else null
                            } else null
                        }.sortedBy { it.third }
                }

                // 2. Select the next active class today
                val nextClassInfo = remember(todayClasses) {
                    val now = Calendar.getInstance()
                    val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
                    
                    // First find a class that hasn't finished yet (assuming 90 min class duration)
                    todayClasses.firstOrNull { (_, _, startMinutes) ->
                        nowMinutes < startMinutes + 90
                    } ?: todayClasses.firstOrNull() // fallback to first class if all are in past
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

            val dateFormatter = java.text.SimpleDateFormat("dd MMM", Locale.getDefault())
            val currentDateStr = dateFormatter.format(java.util.Date())
            val cancelledClasses by viewModel.cancelledClasses.collectAsStateWithLifecycle()

            TodayClassesListWidget(
                subjects = subjects,
                currentDayCode = currentDayCode,
                currentDateStr = currentDateStr,
                cancelledClasses = cancelledClasses,
                onToggleCancelled = { viewModel.toggleCancelledClass(it, currentDateStr) }
            )

            val buddyXp by viewModel.buddyXp.collectAsStateWithLifecycle()

            BuddyMascotRoomWidget(
                absencesCount = absences.size, 
                assessments = assessments,
                buddyXp = buddyXp,
                examTomorrow = examTomorrow
            )
        } else if (activeTab == "estudios") {
            // Nightly Summary
            val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
            if (currentHour >= 20) {
                val tomorrowClasses = remember(subjects) {
                    val cal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, 1) }
                    val tDay = when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
                        java.util.Calendar.MONDAY -> "Lu"
                        java.util.Calendar.TUESDAY -> "Ma"
                        java.util.Calendar.WEDNESDAY -> "Mi"
                        java.util.Calendar.THURSDAY -> "Ju"
                        java.util.Calendar.FRIDAY -> "Vi"
                        java.util.Calendar.SATURDAY -> "Sa"
                        java.util.Calendar.SUNDAY -> "Do"
                        else -> ""
                    }
                    subjects.filter { sub -> sub.sessions.any { it.day == tDay } }
                }
                if (tomorrowClasses.isNotEmpty() || examTomorrow) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyBlue),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Resumen Nocturno", color = Amber, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            if (examTomorrow) {
                                Text("¡Atención! Mañana tienes evaluaciones pendientes.", color = Terracotta, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            if (tomorrowClasses.isNotEmpty()) {
                                Text("Clases de mañana: ${tomorrowClasses.joinToString(", ") { it.name.take(15) }}", color = Bone, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
            
            // Missing Grades Alert
            val missingGrades = remember(assessments) {
                val todayDate = java.util.Date()
                val format = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                assessments.filter {
                    it.grade == null && it.examDate.isNotBlank() && try {
                        val date = format.parse(it.examDate)
                        date != null && date.before(todayDate)
                    } catch (e: Exception) { false }
                }
            }
            if (missingGrades.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Amber.copy(alpha=0.1f)),
                    border = BorderStroke(1.dp, Amber)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Terracotta, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Evaluaciones sin nota", fontWeight = FontWeight.Bold, color = Terracotta, fontSize = 14.sp)
                            Text("Ya pasó la fecha de: ${missingGrades.joinToString(", ") { it.name }}. ¿Ya tienes la nota? Regístrala.", fontSize = 12.sp, color = Terracotta)
                        }
                    }
                }
            }

            val calculatedStress = (assessments.count { it.grade == null } * 15f + absences.size * 5f).coerceIn(0f, 100f)
            val stressStatusText = when {
                calculatedStress > 70f -> "Crítico"
                calculatedStress > 40f -> "Elevado"
                else -> "Estable"
            }
            WellnessWidget(
                upcomingExamsCount = assessments.count { it.grade == null },
                absencesCount = absences.size,
                calculatedStress = calculatedStress,
                statusText = stressStatusText
            )

            UpcomingAssessmentsWidget(
                assessments = assessments,
                subjects = subjects,
                onNavigateToSubject = { onNavigateToDetails(it) }
            )

            UpcomingTasksWidget(
                tasks = tasks,
                subjects = subjects,
                onToggleTask = { viewModel.toggleTask(it) },
                onNavigateToSubject = { onNavigateToDetails(it) }
            )

            val currentWeighted = assessments.filter { it.grade != null }.sumOf { (it.grade!! / 100.0) * it.percentage }
            GradesHistoryWidget(
                assessments = assessments.filter { it.grade != null }.takeLast(5),
                currentWeighted = currentWeighted
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onNavigateToStats,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ProBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.BarChart, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver Estadísticas Completas", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            InteractivePomodoroWidget(viewModel)
        } else if (activeTab == "trayecto") {
            val isLocationAvailable by viewModel.isLocationAvailable.collectAsStateWithLifecycle()
            val currentDistance = currentDistanceToCollege
            val estimatedTravelMins = if (locationBasedTravelTime > 0) locationBasedTravelTime else baseTravelTime
            val context = androidx.compose.ui.platform.LocalContext.current

            SmartRouteReminderCard(
                subjects = subjects,
                baseTravelTime = if (locationBasedTravelTime > 0) locationBasedTravelTime else baseTravelTime,
                distanceKm = currentDistanceToCollege ?: 0.0,
                isOutOfRange = isOutOfRange,
                onConfigureRoute = onConfigureRoute,
                weatherDescription = weatherDescription,
                isRaining = isRaining
            )

            GPSAndStopwatchWidget(
                currentDistanceToCollege = currentDistance,
                isOutOfRange = isOutOfRange,
                locationBasedTravelMinutes = estimatedTravelMins,
                baseTravelTimeSource = baseTravelTime,
                isTripActive = isTripActive,
                tripElapsedSeconds = tripElapsedSeconds,
                onRequestGPS = {
                    android.widget.Toast.makeText(context, "Sincronizando coordenadas GPS...", android.widget.Toast.LENGTH_SHORT).show()
                },
                onStartTrip = {
                    viewModel.startTrip()
                },
                onEndTrip = { mins ->
                    viewModel.endTrip(mins)
                    android.widget.Toast.makeText(context, "¡Viaje finalizado! Registrado: $mins min.", android.widget.Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}

@Composable
fun AdviceCarouselWidget(subjects: List<com.aistudio.unibuddy.qywvsp.data.Subject>, assessments: List<com.aistudio.unibuddy.qywvsp.data.Assessment>, onSubjectClick: (Int) -> Unit) {
    var currentIndex by remember { mutableStateOf(0) }
    
    val tips = remember(subjects, assessments) {
        val list = mutableListOf(
            "Consejo de Estudio: Estudia en bloques de 25 min (Pomodoro). Te ayuda a asimilar mejor los conceptos.",
            "Orgullo Universitario: ¡Un buen café te da el 100% de batería para la clase!",
            "¡Ánimo Buddy!: La constancia vence al talento. Dale seguimiento a tus entregas pendientes hoy.",
            "Prepárate para el Camino: Si llueve por la tarde, revisa tu SmartRoute antes de salir."
        )
        
        // Add dynamic advice based on grades
        subjects.forEach { subject ->
            val subjAssessments = assessments.filter { it.subjectId == subject.id && it.grade != null }
            val currentGrade = subjAssessments.sumOf { it.grade ?: 0.0 }
            val completedPercentage = subjAssessments.sumOf { it.percentage }
            
            if (completedPercentage > 0) {
                val average = (currentGrade / completedPercentage) * 100
                if (average < 60) { // Doing bad
                    val remaining = 100 - completedPercentage
                    if (remaining > (60 - currentGrade)) {
                        list.add(0, "¡Ánimo! En ${subject.name} aún se remonta. Según datos estadísticos hay posibilidad, enfócate en el próximo examen.")
                    } else {
                        list.add(0, "Atención en ${subject.name}: Habla con el profesor sobre opciones de recuperación. ¡No te rindas!")
                    }
                } else if (average > 90) { // Doing great
                    list.add(0, "¡Excelente trabajo en ${subject.name}! Sigue así, tienes un promedio estelar.")
                }
            }
        }
        list
    }

    LaunchedEffect(tips) {
        while (true) {
            kotlinx.coroutines.delay(5000)
            currentIndex = (currentIndex + 1) % tips.size
        }
    }

    val currentTip = tips[currentIndex]
    val mentionedSubject = subjects.find { currentTip.contains(it.name) }
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = mentionedSubject != null) {
            mentionedSubject?.let { onSubjectClick(it.id) }
        },
        colors = CardDefaults.cardColors(containerColor = ProBlue.copy(alpha=0.05f)), // Light blue
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ProBlue.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Mascot
            com.aistudio.unibuddy.qywvsp.ui.components.BuddyMascot(
                pose = "greeting",
                mainColor = ProBlue,
                modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.White)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CONSEJOS Y NOVEDADES",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ProBlue,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                androidx.compose.animation.AnimatedContent(
                    targetState = currentIndex,
                    label = "tips"
                ) { targetIndex ->
                    Text(
                        text = tips[targetIndex],
                        fontSize = 11.sp,
                        color = NavyBlue,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 15.sp
                    )
                }
            }
            
            
        }
    }
}

@Composable
fun InteractivePomodoroWidget(viewModel: com.aistudio.unibuddy.qywvsp.ui.UniBuddyViewModel) {
    val isTimerActive by viewModel.isPomodoroActive.collectAsStateWithLifecycle()
    val secondsLeft by viewModel.pomodoroSecondsLeft.collectAsStateWithLifecycle()

    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    val tips = listOf(
        "Tip: Toma agua cada 25 mins.",
        "Tip: Aleja el teléfono de tu vista.",
        "Tip: Haz estiramientos rápidos.",
        "Tip: Estudia activamente, haz preguntas.",
        "Tip: Usa ruido blanco para concentrarte."
    )
    var currentTipIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(5000L) // Auto move every 5 seconds
            currentTipIndex = (currentTipIndex + 1) % tips.size
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Amber.copy(alpha=0.05f)), // Light orange
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Amber.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "RELOJ POMODORO DE ESTUDIO",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Amber,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Timer,
                            contentDescription = null,
                            tint = Amber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = timeFormatted,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = NavyBlue
                        )
                    }
                    Text(
                        text = if (isTimerActive) "MODO ENFOQUE ACTIVO" else "Listo para iniciar sesión",
                        fontSize = 10.sp,
                        color = SlateGray
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { viewModel.togglePomodoro() },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isTimerActive) Terracotta else DarkGreen),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(if (isTimerActive) "Pausar" else "Iniciar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    
                    Button(
                        onClick = { viewModel.resetPomodoro() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Reiniciar", fontSize = 11.sp, color = NavyBlue)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Auto-moving Carousel of Tips
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                androidx.compose.animation.AnimatedContent(
                    targetState = currentTipIndex,
                    label = "TipCarousel",
                    transitionSpec = {
                        androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(500)) togetherWith androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(500))
                    }
                ) { targetIndex ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(androidx.compose.material.icons.Icons.Default.Info, contentDescription = null, tint = Amber, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tips[targetIndex],
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = NavyBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BuddyMascotRoomWidget(
    absencesCount: Int,
    assessments: List<com.aistudio.unibuddy.qywvsp.data.Assessment>,
    buddyXp: Int,
    examTomorrow: Boolean
) {
    val buddyLevel = 1 + (buddyXp / 100) // 100 XP per level
    val buddyTitle = when {
        buddyLevel < 5 -> "Novato"
        buddyLevel < 10 -> "Estudiante Promedio"
        buddyLevel < 20 -> "Veterano"
        else -> "Tesista"
    }

    val unlockedDecorations = remember(buddyLevel) {
        val list = mutableListOf<String>()
        if (buddyLevel >= 1) list.add("Escritorio")
        if (buddyLevel >= 2) list.add("Cafetera Espresso")
        if (buddyLevel >= 5) list.add("Consola Retro")
        if (buddyLevel >= 10) list.add("Planta Zen")
        if (buddyLevel >= 20) list.add("Corona de Laureles")
        list
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BackgroundGray),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CUARTO DE TU MASCOTA (UNIBUDDY)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyBlue,
                        letterSpacing = 0.5.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "LVL $buddyLevel - $buddyTitle",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("($buddyXp XP)", fontSize = 10.sp, color = SlateGray, fontWeight = FontWeight.SemiBold)
                    }
                }
                
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Bone)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Logros: ${unlockedDecorations.size}/5", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SlateGray)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(Bone, RoundedCornerShape(12.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val finalPose = if (examTomorrow) "working" else if (absencesCount >= 3) "worried" else "normal"
                        BuddyMascot(
                            modifier = Modifier.size(50.dp),
                            isHappy = absencesCount < 3,
                            pose = finalPose
                        )
                        Text(
                            text = if (examTomorrow) "¡A estudiar para mañana!" else if (absencesCount >= 3) "¡Cuidado con las faltas!" else "Tu Buddy te apoya",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyBlue
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f).padding(start = 12.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Objetos desbloqueados en su cuarto:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SlateGray)
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            unlockedDecorations.take(3).forEach { item ->
                                Box(
                                    modifier = Modifier
                                        .background(Color.White, RoundedCornerShape(6.dp))
                                        .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Text(item, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                                }
                            }
                        }
                        if (unlockedDecorations.size > 3) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                unlockedDecorations.drop(3).forEach { item ->
                                    Box(
                                        modifier = Modifier
                                            .background(Color.White, RoundedCornerShape(6.dp))
                                            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    ) {
                                        Text(item, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingTasksWidget(
    tasks: List<Task>,
    subjects: List<Subject>,
    onToggleTask: (Task) -> Unit,
    onNavigateToSubject: (Int) -> Unit
) {
    val pendingTasks = remember(tasks) {
        tasks.filter { !it.isCompleted }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Bone),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        tint = NavyBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "PRÓXIMAS ENTREGAS",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyBlue,
                        letterSpacing = 0.5.sp
                    )
                }
                
                if (pendingTasks.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Terracotta)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${pendingTasks.size} pendientes",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (pendingTasks.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = DarkGreen,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            text = "¡Al día con tus entregas!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyBlue
                        )
                        Text(
                            text = "No tienes tareas ni laboratorios pendientes hoy.",
                            fontSize = 11.sp,
                            color = SlateGray
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    pendingTasks.take(3).forEach { task ->
                        val subject = subjects.find { it.id == task.subjectId }
                        val subjectColor = remember(subject) {
                            try { Color(android.graphics.Color.parseColor(subject?.colorHex ?: "#1B5E20")) } catch(e: Exception) { DarkGreen }
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .clickable { subject?.let { onNavigateToSubject(it.id) } }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { onToggleTask(task) },
                                    colors = CheckboxDefaults.colors(checkedColor = DarkGreen)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(
                                        text = task.title,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyBlue,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Subject bullet
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(subjectColor))
                                            Text(subject?.name ?: "Materia", fontSize = 10.sp, color = SlateGray, fontWeight = FontWeight.Bold)
                                        }
                                        
                                        // Badge for type
                                        val badgeBg = when (task.type) {
                                            "Laboratorio" -> MintGreen.copy(alpha=0.1f)
                                            "Proyecto" -> ProBlue.copy(alpha=0.1f)
                                            "Examen" -> Terracotta.copy(alpha=0.1f)
                                            else -> BackgroundGray
                                        }
                                        val badgeTxt = when (task.type) {
                                            "Laboratorio" -> MintGreen
                                            "Proyecto" -> ProBlue
                                            "Examen" -> Terracotta
                                            else -> SlateGray
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(badgeBg)
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(task.type, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = badgeTxt)
                                        }
                                    }
                                }
                            }
                            
                            // Due Date
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(10.dp), tint = Terracotta)
                                Text(task.dueDate, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Terracotta)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingAssessmentsWidget(
    assessments: List<com.aistudio.unibuddy.qywvsp.data.Assessment>,
    subjects: List<com.aistudio.unibuddy.qywvsp.data.Subject>,
    onNavigateToSubject: (Int) -> Unit
) {
    val pendingAssessments = remember(assessments) {
        val format = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        assessments.filter { it.grade == null && it.examDate.isNotBlank() }
            .sortedBy { 
                try { format.parse(it.examDate)?.time ?: Long.MAX_VALUE } 
                catch (e: Exception) { Long.MAX_VALUE }
            }
            .take(5)
    }

    if (pendingAssessments.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = ProBlue.copy(alpha=0.05f)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ProBlue.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = ProBlue,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "SEMANA DE EXÁMENES",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = ProBlue,
                    letterSpacing = 0.5.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                pendingAssessments.forEach { ass ->
                    val subject = subjects.find { it.id == ass.subjectId }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .clickable { onNavigateToSubject(ass.subjectId) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ass.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyBlue
                            )
                            if (subject != null) {
                                Text(
                                    text = subject.name,
                                    fontSize = 11.sp,
                                    color = SlateGray
                                )
                            }
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = ass.examDate,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Terracotta
                            )
                            Text(
                                text = "${ass.percentage} pts",
                                fontSize = 10.sp,
                                color = SlateGray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
