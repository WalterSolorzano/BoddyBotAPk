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
import androidx.compose.material3.Surface
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
    onNavigateToPensum: () -> Unit,
    onNavigateToTutor: () -> Unit,
    onNavigateToStudyPlan: () -> Unit
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
    val examSilenceTimeRemaining by viewModel.examSilenceTimeRemaining.collectAsStateWithLifecycle()
    val smartSilenceEnabled by viewModel.smartSilenceEnabled.collectAsStateWithLifecycle()
    val dailyCheckinStreak by viewModel.dailyCheckinStreak.collectAsStateWithLifecycle()
    val lastCheckinDate by viewModel.lastCheckinDate.collectAsStateWithLifecycle()
    val lastLocalTime by viewModel.lastBackupLocalTime.collectAsStateWithLifecycle()
    val lastDriveTime by viewModel.lastBackupDriveTime.collectAsStateWithLifecycle()
    val backupIntervalDays by viewModel.backupIntervalDays.collectAsStateWithLifecycle()

    val showBackupWarning = remember(lastLocalTime, lastDriveTime, backupIntervalDays) {
        if (backupIntervalDays == 0) {
            false
        } else {
            val now = System.currentTimeMillis()
            val intervalMs = backupIntervalDays.toLong() * 24L * 60L * 60L * 1000L
            val localOverdue = lastLocalTime == 0L || (now - lastLocalTime > intervalMs)
            localOverdue
        }
    }
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
        // Backup Warning Banner
        if (showBackupWarning) {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("backup_warning_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)), // Amber Light
                border = BorderStroke(1.dp, Color(0xFFFDE68A)), // Amber Border
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Atención",
                        tint = Color(0xFFD97706), // Amber Dark
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Respaldo Requerido",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val daysSinceStr = if (lastLocalTime == 0L) "Nunca has realizado un respaldo." else {
                            val days = (System.currentTimeMillis() - lastLocalTime) / (1000L * 60L * 60L * 24L)
                            "Último respaldo hace $days días."
                        }
                        Text(
                            text = "Por seguridad de tus datos, te recomendamos realizar una copia de seguridad en JSON. $daysSinceStr",
                            color = Color(0xFFB45309),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // --- 0. Exam Silence Banner / Control ---
        if (examSilenceTimeRemaining > 0L) {
            val minutes = examSilenceTimeRemaining / 60
            val seconds = examSilenceTimeRemaining % 60
            Card(
                modifier = Modifier.fillMaxWidth().testTag("exam_silence_banner"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), // Slate dark
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Modo Silencio de Exámenes Activo",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "UniBuddy silenciado. Tiempo restante: ${String.format(Locale.US, "%02d:%02d", minutes, seconds)}",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = { viewModel.stopExamSilence() },
                        colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Desactivar", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (smartSilenceEnabled) {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("exam_silence_inactive_banner"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Modo Silencio de Exámenes disponible",
                            fontWeight = FontWeight.Bold,
                            color = NavyBlue,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Silencia UniBuddy por 2 horas durante tus evaluaciones.",
                            color = SlateGray,
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = { viewModel.startExamSilence(2 * 60 * 60) },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Activar DND", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

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
        Spacer(modifier = Modifier.height(4.dp))
        
        Surface(
            onClick = onNavigateToStudyPlan,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = ProBlue.copy(alpha = 0.1f),
            border = BorderStroke(1.dp, ProBlue.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Planificador de Estudio IA", color = ProBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Generar estrategia semanal", color = ProBlue.copy(alpha = 0.8f), fontSize = 11.sp)
                }
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "IA", tint = ProBlue, modifier = Modifier.size(20.dp))
            }
        }
        }

        // --- 2b. Aviso de clima ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = if (isRaining) ProBlue.copy(alpha = 0.08f) else Amber.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(0.5.dp, (if (isRaining) ProBlue else Amber).copy(alpha = 0.2f))
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                if (isRaining) {
                    RainOverlay(modifier = Modifier.matchParentSize())
                }
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
                        text = "Pronóstico: $weatherDescription. ${if (isRaining) "Lleva paraguas para proteger tu UniBuddy." else "Excelente día para estudiar en el campus."}",
                        fontSize = 11.sp,
                        color = if (isRaining) NavyBlue else Color(0xFF78350F),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                }
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
        val currentPeriodAssessmentsCount = assessments.count {
            val isC1 = it.name.contains("C1", ignoreCase = true) || it.name.contains("U1", ignoreCase = true)
            val matchesPeriod = if (currentWeek <= 8) isC1 else !isC1
            matchesPeriod && it.grade == null
        }
        val calculatedStress = if (semesterState == "Vacaciones") 0f else (currentPeriodAssessmentsCount * 15f + absences.size * 5f).coerceIn(0f, 100f)
        val stressStatusText = if (semesterState == "Vacaciones") "Estable" else when {
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
                        .size(240.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5F5F5))
                        .border(0.5.dp, NavyBlue.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    BuddyMascot(
                        pose = if (isCelebrating) "celebrating" else if (semesterState == "Vacaciones") "sleeping" else if (calculatedStress > 50) "worried" else "greeting",
                        modifier = Modifier.size(200.dp),
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
                    text = if (semesterState == "Vacaciones") "Anímico: Durmiendo placenteramente ($stressStatusText)" else "Anímico: ${if (calculatedStress > 50) "Preocupado ($stressStatusText)" else "Feliz y Saludable ($stressStatusText)"}",
                    fontSize = 11.sp,
                    color = if (semesterState != "Vacaciones" && calculatedStress > 50) Terracotta else DarkGreen,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(14.dp))
                androidx.compose.material3.HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 1.dp, modifier = Modifier.fillMaxWidth(0.9f))
                Spacer(modifier = Modifier.height(10.dp))
                
                val todayStr = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date()) }
                val hasCheckedInToday = lastCheckinDate == todayStr

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Racha de Check-in",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateGray
                        )
                        Text(
                            text = if (dailyCheckinStreak == 1) "1 día activo" else "$dailyCheckinStreak días activos",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Amber
                        )
                    }
                    Button(
                        onClick = {
                            val success = viewModel.performDailyCheckin()
                            if (success) {
                                android.widget.Toast.makeText(context, "¡Check-in registrado! +10 XP para Buddy", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                android.widget.Toast.makeText(context, "Ya hiciste check-in hoy. ¡Vuelve mañana!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasCheckedInToday) Color.LightGray else DarkGreen
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(
                            text = if (hasCheckedInToday) "Al día" else "Hacer check-in",
                            color = if (hasCheckedInToday) SlateGray else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- 4. Bloque de trayecto / salida recomendada (Consolidado) ---
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

        val isTravelRelevant = semesterState != "Vacaciones" && nextSubject != null

        if (!isTravelRelevant) {
            // Versión mínima de una línea cuando no aplica (ej. en vacaciones o sin clases)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onConfigureRoute() },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.5.dp, NavyBlue.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DirectionsCar,
                            contentDescription = null,
                            tint = SlateGray,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (semesterState == "Vacaciones") {
                                "Trayecto al campus: No aplica en período de receso"
                            } else {
                                "Trayecto al campus: No programado para hoy"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = SlateGray
                        )
                    }
                    Text(
                        text = "Configurar",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProBlue
                    )
                }
            }
        } else {
            // Detalle expandido cuando hay clase o trayecto relevante
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(0.5.dp, NavyBlue.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "TRAYECTO AL CAMPUS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SlateGray,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Sub-card 1: Estado del viaje actual
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onConfigureRoute() },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "VIAJE ACTUAL",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SlateGray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
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
                                        text = if (isTripActive) "Activo" else "Inactivo",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyBlue
                                    )
                                }
                                if (isTripActive) {
                                    val activeMinutes = tripElapsedSeconds / 60
                                    val activeSeconds = tripElapsedSeconds % 60
                                    Text(
                                        text = String.format("%02d:%02d transcurrido", activeMinutes, activeSeconds),
                                        fontSize = 10.sp,
                                        color = SlateGray,
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    Text(
                                        text = "Iniciar ruta",
                                        fontSize = 10.sp,
                                        color = ProBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        
                        // Sub-card 2: Recomendación de salida
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onConfigureRoute() },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "SALIDA RECOMENDADA",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SlateGray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = departureTimeFormatted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ProBlue
                                )
                                Text(
                                    text = "Para llegar a tiempo",
                                    fontSize = 10.sp,
                                    color = SlateGray,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Embedded Map inside the unified trayecto block
                    InteractiveCommuteMap(
                        isTripActive = isTripActive,
                        tripElapsedSeconds = tripElapsedSeconds,
                        currentDistanceToCollege = currentDistanceToCollege,
                        locationBasedTravelTime = locationBasedTravelTime,
                        semesterState = semesterState,
                        onConfigureRoute = onConfigureRoute,
                        modifier = Modifier.height(140.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 5. Avisos contextuales (Oculto durante vacaciones para evitar duplicados) ---
        val dynamicNotices = remember(subjects, absences, tasks, calculatedStress, semesterState) {
            val list = mutableListOf<Pair<String, String>>()
            
            if (semesterState != "Vacaciones") {
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

        // --- 6. Próxima clase / Consolidated Recess Banner ---
        val isHoliday = viewModel.isNicaraguaHoliday(System.currentTimeMillis())
        if (semesterState == "Vacaciones") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), // Deep Slate Dark Blue
                border = BorderStroke(1.dp, MintGreen.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Celebration,
                        contentDescription = null,
                        tint = MintGreen,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "PERÍODO DE RECESO ACTIVO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MintGreen,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "¡Disfruta tus merecidas vacaciones!",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tu UniBuddy está durmiendo. No hay clases, evaluaciones ni trayectos programados.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp
                    )
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

@Composable
fun RainOverlay(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "RainTransition")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RainProgress"
    )

    val drops = remember {
        List(15) {
            Triple(
                Math.random().toFloat(),
                Math.random().toFloat(),
                (10 + Math.random() * 15).toFloat()
            )
        }
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        drops.forEach { (relX, relY, speedScale) ->
            val startY = ((relY + animProgress * speedScale) % 1.0f) * height
            val startX = relX * width
            
            val dx = 2f
            val dy = 15f
            
            drawLine(
                color = Color(0xFF90CAF9).copy(alpha = 0.4f),
                start = Offset(startX, startY),
                end = Offset(startX - dx, startY + dy),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
