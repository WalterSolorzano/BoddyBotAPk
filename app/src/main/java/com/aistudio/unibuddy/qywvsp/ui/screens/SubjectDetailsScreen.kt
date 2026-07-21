package com.aistudio.unibuddy.qywvsp.ui.screens

import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import com.aistudio.unibuddy.qywvsp.utils.CalendarExportHelper
import androidx.compose.ui.platform.testTag
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aistudio.unibuddy.qywvsp.data.*
import com.aistudio.unibuddy.qywvsp.ui.theme.*
import com.aistudio.unibuddy.qywvsp.ui.components.*
import com.aistudio.unibuddy.qywvsp.ui.screens.*
import com.aistudio.unibuddy.qywvsp.ui.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlinx.coroutines.launch

// 4. DETALLE MATERIA SCREEN
@Composable
fun SubjectDetailsScreen(viewModel: UniBuddyViewModel, subjectId: Int, onBack: () -> Unit, onNavigateToTutor: (Int) -> Unit) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val subject = subjects.find { it.id == subjectId }
    val absences by viewModel.getAbsencesForSubject(subjectId).collectAsStateWithLifecycle(emptyList())
    val attendanceLogs by viewModel.getLogsForSubject(subjectId).collectAsStateWithLifecycle(emptyList())
    val subjectImportanceMap by viewModel.subjectImportanceMap.collectAsStateWithLifecycle()

    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(350)
        isLoading = false
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundBone),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = ProBlue)
        }
        return
    }

    if (subject == null) {
        onBack()
        return
    }

    val maxAbs = subject.totalClasses - ceil(subject.totalClasses * (subject.requiredAttendancePercent / 100.0)).toInt()
    
    val totalAbsCount = attendanceLogs.count { !it.isPresent && !it.isCancelled }.coerceAtLeast(absences.size)
    val totalPresCount = attendanceLogs.count { it.isPresent && !it.isCancelled }
    val totalSessionsHeld = totalPresCount + totalAbsCount
    val currentRate = if (totalSessionsHeld > 0) (totalPresCount.toDouble() / totalSessionsHeld * 100.0) else 100.0
    val remaining = maxAbs - totalAbsCount

    // Add session manual state
    var manualDateInput by remember { mutableStateOf("") }
    var selectedStatusType by remember { mutableStateOf("Asistió") } // "Asistió", "Faltó", "Canceló"
    var filterTab by remember { mutableStateOf("Todo") } // "Todo", "Asistencias", "Faltas", "Canceladas"
    var activeDetailTab by remember { mutableStateOf("Asistencias") } // "Asistencias", "Tareas"
    var currentMonthCal by remember { mutableStateOf(Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }) }

    val mergedLogs = remember(attendanceLogs, absences) {
        val list = attendanceLogs.map { IntegratedLogItem(id = it.id, date = it.date, isPresent = it.isPresent, isCancelled = it.isCancelled, isLegacy = false) }.toMutableList()
        absences.forEach { legacyAbs ->
            if (attendanceLogs.none { !it.isPresent && !it.isCancelled && it.date == legacyAbs.date }) {
                list.add(IntegratedLogItem(id = legacyAbs.id, date = legacyAbs.date, isPresent = false, isCancelled = false, isLegacy = true))
            }
        }
        list.sortByDescending { it.date }
        list
    }

    val filteredLogs = remember(mergedLogs, filterTab) {
        when (filterTab) {
            "Asistencias" -> mergedLogs.filter { it.isPresent && !it.isCancelled }
            "Faltas" -> mergedLogs.filter { !it.isPresent && !it.isCancelled }
            "Canceladas" -> mergedLogs.filter { it.isCancelled }
            else -> mergedLogs
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = NavyBlue)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = subject.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = NavyBlue,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Row {
                val context = LocalContext.current
                IconButton(
                    onClick = {
                        if (subject.sessions.isEmpty()) {
                            android.widget.Toast.makeText(context, "No hay clases programadas para agendar", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            var exportedCount = 0
                            subject.sessions.forEach { session ->
                                val timeStr = when (session.time) {
                                    "M1" -> "08:00 - 10:00"
                                    "M2" -> "10:00 - 12:00"
                                    "M3" -> "12:00 - 14:00"
                                    "T1" -> "14:00 - 16:00"
                                    "T2" -> "16:00 - 18:00"
                                    "T3" -> "18:00 - 20:00"
                                    else -> session.time
                                }
                                CalendarExportHelper.exportClassToCalendar(
                                    context = context,
                                    subjectName = subject.name,
                                    dayCode = session.day,
                                    sessionTimeStr = timeStr
                                )
                                exportedCount++
                            }
                            if (exportedCount > 0) {
                                android.widget.Toast.makeText(context, "Abriendo agenda para $exportedCount clases", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Agendar clases", tint = NavyBlue)
                }

                var showEditScheduleDialog by remember { mutableStateOf(false) }
                IconButton(onClick = { showEditScheduleDialog = true }) {
                    Icon(imageVector = Icons.Default.DateRange, contentDescription = "Editar Horario", tint = NavyBlue)
                }
                
                if (showEditScheduleDialog) {
                    var editingSessions by remember { mutableStateOf(subject.sessions.toMutableStateList()) }
                    if (editingSessions.isEmpty()) {
                        editingSessions.add(com.aistudio.unibuddy.qywvsp.data.ClassSessionDetails("Lu", "M1", ""))
                    }
                    AlertDialog(
                        onDismissRequest = { showEditScheduleDialog = false },
                        title = { Text("Editar Horario", color = NavyBlue, fontWeight = FontWeight.Bold) },
                        text = {
                            LazyColumn {
                                item {
                                    SessionEditor(
                                        sessions = editingSessions,
                                        allSubjects = subjects,
                                        currentSubjectId = subject.id
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val computedSchedule = editingSessions.distinctBy { it.day }.joinToString(", ") { it.day }
                                    val computedClasses = editingSessions.size * 14
                                    val jsonString = editingSessions.toList().toJsonString()
                                    viewModel.updateSubject(
                                        subject.copy(
                                            schedule = if (computedSchedule.isEmpty()) "Sin horario" else computedSchedule,
                                            sessions = editingSessions.toList(),
                                            totalClasses = computedClasses
                                        )
                                    )
                                    showEditScheduleDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                            ) {
                                Text("Guardar", color = Color.White)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showEditScheduleDialog = false }) {
                                Text("Cancelar", color = SlateGray)
                            }
                        }
                    )
                }

                var showDeleteConfirmation by remember { mutableStateOf(false) }
                
                IconButton(onClick = { showDeleteConfirmation = true }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar materia", tint = Terracotta)
                }
                
                if (showDeleteConfirmation) {
                    AlertDialog(
                        onDismissRequest = { showDeleteConfirmation = false },
                        title = { Text("¿Estás seguro?", color = NavyBlue, fontWeight = FontWeight.Bold) },
                        text = { Text("Se borrarán todas las notas y faltas de esta materia de forma irreversible.", color = SlateGray) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.deleteSubject(subject)
                                    showDeleteConfirmation = false
                                    onBack()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
                            ) {
                                Text("Confirmar", color = Color.White)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteConfirmation = false }) {
                                Text("Cancelar", color = SlateGray)
                            }
                        },
                        containerColor = Color.White
                    )
                }
            }
        }
        
        val rawImportance = subjectImportanceMap[subject.id]
        val importance = rawImportance ?: when (calculateSubjectCriticality(subject.name)) {
            "Alta (Pre-requisito crítico)" -> "Alta"
            "Media" -> "Media"
            else -> "Baja"
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Prioridad académica:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NavyBlue
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Baja", "Media", "Alta").forEach { level ->
                    val isSelected = importance == level
                    val tintColor = when (level) {
                        "Alta" -> Terracotta
                        "Media" -> Color(0xFFF57C00)
                        else -> DarkGreen
                    }
                    val bgCol = if (isSelected) tintColor.copy(alpha = 0.15f) else Color(0xFFF1F5F9)
                    val borderCol = if (isSelected) tintColor else Color.LightGray
                    
                    Surface(
                        modifier = Modifier
                            .clickable {
                                viewModel.saveSubjectImportance(subject.id, level)
                            },
                        color = bgCol,
                        border = BorderStroke(1.dp, borderCol),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = level,
                            color = if (isSelected) tintColor else SlateGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val assessmentsState by viewModel.getAssessmentsForSubject(subject.id).collectAsStateWithLifecycle(emptyList())
        val currentAvg = assessmentsState.filter { it.grade != null }.sumOf { it.grade!! }

        val pointsLost = assessmentsState.filter { it.grade != null }.sumOf { it.percentage - it.grade!! }
        val buddyPose = when {
            pointsLost > 30.0 || remaining <= 1 -> "idle"
            pointsLost < 10.0 && remaining >= 3 -> "greeting"
            else -> "idle"
        }
        val isBuddyHappy = pointsLost < 15.0 && remaining > 1
        val isBuddyWorried = pointsLost > 30.0 || remaining <= 1

        val bubbleText = when {
            pointsLost < 10.0 && remaining >= 3 -> "¡Vas volando! Has perdido muy pocos puntos (${String.format(Locale.US, "%.1f", pointsLost)}) y tienes $remaining vidas de sobra. ¡Excelente trabajo!"
            pointsLost > 30.0 || remaining <= 1 -> "¡Cuidado! Has perdido ${String.format(Locale.US, "%.1f", pointsLost)} pts o estás casi sin vidas ($remaining rest.). ¡No te confíes!"
            else -> "¡Todo en orden! Buen ritmo académico. Acumulando puntos para cerrar el semestre sin problemas."
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Bone),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 1. Buddy Mascot Emotional State
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BuddyMascot(
                        modifier = Modifier.size(64.dp),
                        isHappy = isBuddyHappy,
                        isWorried = isBuddyWorried,
                        pose = buddyPose,
                        mainColor = try { Color(android.graphics.Color.parseColor(subject.colorHex)) } catch(e: Exception) { DarkGreen }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = bubbleText,
                            fontSize = 12.sp,
                            color = NavyBlue,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    onClick = { onNavigateToTutor(subjectId) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = ProBlue.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, ProBlue.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Tutor IA", tint = ProBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Abrir Tutor IA para ${subject.name}",
                            color = ProBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    onClick = { 
                        val newUrl = "https://picsum.photos/seed/${subject.name.replace(" ", "")}/400/200"
                        viewModel.saveSetting("cover_url_${subject.id}", newUrl)
                        //
                        
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Generar Portada", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generar Portada con IA",
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Game-style Health Bar (Vidas de Faltas Restantes)
                Text(
                    text = "CORAZONES DE SUPERVIVENCIA (Margen de faltas)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NavyBlue,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = Terracotta, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Vidas de faltas:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (remaining > 0) "Te quedan $remaining de $maxAbs faltas permitidas" else "¡SITUACIÓN CRÍTICA! Sin faltas",
                            fontSize = 11.sp,
                            color = SlateGray
                        )
                    }

                    // Hearts Row
                    SubjectLivesIndicator(
                        subAbsCount = totalAbsCount,
                        maxAbs = maxAbs,
                        heartSize = 22.dp
                    )
                }


                Spacer(modifier = Modifier.height(16.dp))

                // 2.5. Battery of Grades
                Text(
                    text = "BATERÍA ACADÉMICA (Energía de Calificaciones)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NavyBlue,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.BatteryChargingFull, contentDescription = null, tint = if (pointsLost < 20.0) Color(0xFF10B981) else if (pointsLost < 30.0) Color(0xFFF59E0B) else Terracotta, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Energía Restante:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Has perdido ${String.format(java.util.Locale.US, "%.1f", pointsLost)} pts en el semestre",
                            fontSize = 11.sp,
                            color = SlateGray
                        )
                    }

                    // Battery Indicator
                    val maxPointsLostAllowed = 100.0 - 60.0 // Hardcoded to 60.0 for now
                    val batteryRatio = (1f - (pointsLost / maxPointsLostAllowed).toFloat()).coerceIn(0f, 1f)
                    
                    Box(modifier = Modifier.width(60.dp).height(24.dp).background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp)).border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(4.dp)), contentAlignment = Alignment.CenterStart) {
                        Box(modifier = Modifier.fillMaxWidth(batteryRatio).fillMaxHeight().background(if (batteryRatio > 0.5f) Color(0xFF10B981) else if (batteryRatio > 0.2f) Color(0xFFF59E0B) else Color(0xFFEF4444), RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = if (batteryRatio >= 0.99f) 4.dp else 0.dp, bottomEnd = if (batteryRatio >= 0.99f) 4.dp else 0.dp)))
                    }
                    Box(modifier = Modifier.width(4.dp).height(10.dp).background(Color(0xFF94A3B8), RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp)))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Faltómetro Inteligente ("¿Puedo faltar un día?")
                Text(
                    text = "FALTÓMETRO INTELIGENTE (¿Puedo faltar hoy?)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NavyBlue,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                val scheduledDays = remember(subject) {
                    subject.sessions.map { it.day }.distinct()
                }

                if (scheduledDays.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay días programados en el horario para esta materia.", fontSize = 12.sp, color = SlateGray)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        scheduledDays.forEach { d ->
                            val dayName = when (d) {
                                "Lu" -> "Lunes"
                                "Ma" -> "Martes"
                                "Mi" -> "Miércoles"
                                "Ju" -> "Jueves"
                                "Vi" -> "Viernes"
                                "Sá" -> "Sábado"
                                else -> d
                            }
                            val otherClasses = remember(subjects) {
                                subjects.filter { it.id != subject.id && it.schedule.contains(d, ignoreCase = true) }
                            }
                            val hasHighPriorityOtherClass = otherClasses.any { (subjectImportanceMap[it.id] ?: "Media") == "Alta" }
                            
                            val allAssessmentsList by viewModel.assessments.collectAsStateWithLifecycle(emptyList())
                            val hasExamOnDay = allAssessmentsList.any { it.grade == null && it.examDate.trim().equals(d, ignoreCase = true) }

                            val statusColor: Color
                            val statusTitle: String
                            val statusIcon: androidx.compose.ui.graphics.vector.ImageVector
                            val statusDesc: String

                            when {
                                remaining <= 0 -> {
                                    statusColor = Terracotta
                                    statusTitle = "FALTAS AGOTADAS"
                                    statusIcon = Icons.Default.Clear
                                    statusDesc = "Estás en el límite. Se recomienda no faltar a ninguna clase más."
                                }
                                hasExamOnDay -> {
                                    statusColor = Terracotta
                                    statusTitle = "EVALUACIÓN PROGRAMADA"
                                    statusIcon = Icons.Default.Warning
                                    statusDesc = "Hay un parcial de otra materia este día. ¡Imposible faltar!"
                                }
                                hasHighPriorityOtherClass -> {
                                    statusColor = Color(0xFFF57C00)
                                    statusTitle = "CLASE ALTA PRIORIDAD HOY"
                                    statusIcon = Icons.Default.Info
                                    statusDesc = "Cursás materias pre-requisito críticas este día."
                                }
                                else -> {
                                    statusColor = DarkGreen
                                    statusTitle = "DÍA SEGURO"
                                    statusIcon = Icons.Default.CheckCircle
                                    statusDesc = "Buen margen de faltas, sin parciales ni clases de alta prioridad."
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(statusColor.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(dayName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                                        Text(statusTitle, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = statusColor)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(statusDesc, fontSize = 11.sp, color = SlateGray, lineHeight = 14.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                // 4. Quick Standard Summary Metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Asistencias", fontSize = 11.sp, color = SlateGray)
                        Text("$totalPresCount clases", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                    }
                    Column {
                        Text("Faltas Registradas", fontSize = 11.sp, color = SlateGray)
                        Text("$totalAbsCount de $maxAbs", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Terracotta)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Tasa Asistencia", fontSize = 11.sp, color = SlateGray)
                        Text(
                            text = if (totalSessionsHeld > 0) "${String.format(Locale.US, "%.1f", currentRate)}%" else "Sin datos",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (totalSessionsHeld == 0) SlateGray else if (currentRate >= subject.requiredAttendancePercent) DarkGreen else Terracotta
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Tab Switcher
        TabRow(
            selectedTabIndex = when (activeDetailTab) {
                "Asistencias" -> 0
                "Tareas" -> 1
                else -> 2
            },
            containerColor = Color(0xFFF1F5F9),
            contentColor = NavyBlue,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[
                        when (activeDetailTab) {
                            "Asistencias" -> 0
                            "Tareas" -> 1
                            else -> 2
                        }
                    ]),
                    color = try { Color(android.graphics.Color.parseColor(subject.colorHex)) } catch(e: Exception) { DarkGreen }
                )
            },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = activeDetailTab == "Asistencias",
                onClick = { activeDetailTab = "Asistencias" },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp), tint = NavyBlue)
                        Text("Asistencias", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyBlue)
                    }
                }
            )
            Tab(
                selected = activeDetailTab == "Tareas",
                onClick = { activeDetailTab = "Tareas" },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp), tint = NavyBlue)
                        Text("Tareas", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyBlue)
                    }
                }
            )
            Tab(
                selected = activeDetailTab == "QueLlevar",
                onClick = { activeDetailTab = "QueLlevar" },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = NavyBlue)
                        Text("Qué llevar", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyBlue)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (activeDetailTab == "Asistencias") {
            // MONTHLY CALENDAR
            Text(
                text = "CALENDARIO MENSUAL DE ASISTENCIAS",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NavyBlue,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Month Selector Header
            val monthName = remember(currentMonthCal) {
                currentMonthCal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale("es", "NI")) ?: ""
            }
            val yearValue = remember(currentMonthCal) { currentMonthCal.get(Calendar.YEAR) }

            val firstDayOfWeek = remember(currentMonthCal) {
                val day = currentMonthCal.get(Calendar.DAY_OF_WEEK) // Sunday=1, Monday=2...
                when (day) {
                    Calendar.MONDAY -> 0
                    Calendar.TUESDAY -> 1
                    Calendar.WEDNESDAY -> 2
                    Calendar.THURSDAY -> 3
                    Calendar.FRIDAY -> 4
                    Calendar.SATURDAY -> 5
                    Calendar.SUNDAY -> 6
                    else -> 0
                }
            }
            val maxDaysInMonth = remember(currentMonthCal) {
                currentMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Bone)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            val prev = (currentMonthCal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                            currentMonthCal = prev
                        }) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Mes anterior", tint = NavyBlue)
                        }
                        Text(
                            text = "${monthName.uppercase(Locale.getDefault())} $yearValue",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NavyBlue
                        )
                        IconButton(onClick = {
                            val next = (currentMonthCal.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                            currentMonthCal = next
                        }) {
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Siguiente mes", tint = NavyBlue)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        listOf("L", "M", "M", "J", "V", "S", "D").forEach { d ->
                            Text(
                                text = d,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(36.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val totalCells = firstDayOfWeek + maxDaysInMonth
                    val rowsCount = ceil(totalCells.toFloat() / 7f).toInt()
                    
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (r in 0 until rowsCount) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                for (c in 0 until 7) {
                                    val cellIndex = r * 7 + c
                                    val dayNum = cellIndex - firstDayOfWeek + 1
                                    
                                    if (cellIndex < firstDayOfWeek || dayNum > maxDaysInMonth) {
                                        Box(modifier = Modifier.size(36.dp))
                                    } else {
                                        val tempCal = Calendar.getInstance().apply {
                                            set(Calendar.YEAR, yearValue)
                                            set(Calendar.MONTH, currentMonthCal.get(Calendar.MONTH))
                                            set(Calendar.DAY_OF_MONTH, dayNum)
                                        }
                                        
                                        val dayOfWeekCode = when (tempCal.get(Calendar.DAY_OF_WEEK)) {
                                            Calendar.MONDAY -> "Lu"
                                            Calendar.TUESDAY -> "Ma"
                                            Calendar.WEDNESDAY -> "Mi"
                                            Calendar.THURSDAY -> "Ju"
                                            Calendar.FRIDAY -> "Vi"
                                            Calendar.SATURDAY -> "Sá"
                                            else -> "Do"
                                        }
                                        
                                        val isClassScheduled = subject.schedule.contains(dayOfWeekCode, ignoreCase = true)
                                        
                                        val logForDay = mergedLogs.find { log ->
                                            val logDateClean = log.date.lowercase().replace(" (justificada)", "").trim()
                                            val dayStrNoZero = dayNum.toString() + " " + SimpleDateFormat("MMM", Locale.getDefault()).format(tempCal.time).lowercase().replace(".", "")
                                            val dayStrWithZero = String.format("%02d", dayNum) + " " + SimpleDateFormat("MMM", Locale.getDefault()).format(tempCal.time).lowercase().replace(".", "")
                                            logDateClean.contains(dayStrNoZero) || logDateClean.contains(dayStrWithZero)
                                        }
                                        
                                        val isJustified = logForDay != null && !logForDay.isPresent && logForDay.date.lowercase().contains("justificada")
                                        
                                        val circleColor = when {
                                            logForDay != null && logForDay.isPresent -> DarkGreen
                                            logForDay != null && !logForDay.isPresent && isJustified -> Color(0xFFFBC02D)
                                            logForDay != null && !logForDay.isPresent -> Terracotta
                                            isClassScheduled -> SlateGray.copy(alpha = 0.15f)
                                            else -> Color.Transparent
                                        }
                                        
                                        val textColor = when {
                                            logForDay != null -> Color.White
                                            isClassScheduled -> NavyBlue
                                            else -> SlateGray.copy(alpha = 0.5f)
                                        }
                                        
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(circleColor)
                                                .clickable {
                                                    val formattedDate = SimpleDateFormat("dd MMM", Locale.getDefault()).format(tempCal.time)
                                                    if (logForDay != null) {
                                                        if (logForDay.isLegacy) {
                                                            viewModel.deleteAbsence(logForDay.id)
                                                        } else {
                                                            viewModel.deleteAttendanceLog(logForDay.id)
                                                        }
                                                    } else {
                                                        viewModel.registerAttendanceLog(subject.id, isPresent = true, dateStr = formattedDate)
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$dayNum",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem(color = DarkGreen, label = "Asistido")
                        LegendItem(color = Color(0xFFFBC02D), label = "Justificada")
                        LegendItem(color = Terracotta, label = "Falta")
                        LegendItem(color = SlateGray.copy(alpha = 0.15f), label = "Clase")
                    }
                }
            }

            // Bitácora Log Filter tab
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Todo", "Asistencias", "Faltas").forEach { label ->
                    val isSelected = filterTab == label
                    Button(
                        onClick = { filterTab = label },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) DarkGreen else SlateGray.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Bone else NavyBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Ningún registro coincide con el filtro.", color = SlateGray, fontSize = 13.sp)
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filteredLogs.forEach { item ->
                        val hapticFeedback = LocalHapticFeedback.current
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                when (dismissValue) {
                                    SwipeToDismissBoxValue.EndToStart -> {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (item.isLegacy) {
                                            viewModel.deleteAbsence(item.id)
                                        } else {
                                            viewModel.deleteAttendanceLog(item.id)
                                        }
                                        true
                                    }
                                    else -> false
                                }
                            }
                        )
                        
                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                val color by animateColorAsState(
                                    when (dismissState.targetValue) {
                                        SwipeToDismissBoxValue.EndToStart -> Terracotta
                                        else -> Color.Transparent
                                    }
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color, RoundedCornerShape(12.dp))
                                        .padding(end = 16.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.White)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Bone)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val isJustified = !item.isPresent && item.date.lowercase().contains("justificada")
                                        val iconCol = when {
                                            item.isCancelled -> Color.Gray
                                            item.isPresent -> DarkGreen
                                            isJustified -> Color(0xFFFBC02D)
                                            else -> Terracotta
                                        }
                                        Icon(
                                            imageVector = if (item.isCancelled) Icons.Default.Cancel else if (item.isPresent) Icons.Default.CheckCircle else Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = iconCol
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = if (item.isCancelled) "Clase cancelada: ${item.date}" else if (item.isPresent) "Asistió: ${item.date}" else "Falta registrada: ${item.date}",
                                            fontWeight = FontWeight.Bold,
                                            color = NavyBlue,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save session form
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, Bone)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Registrar clase manual", style = MaterialTheme.typography.titleSmall, color = NavyBlue, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = manualDateInput,
                            onValueChange = { manualDateInput = it },
                            placeholder = { Text("Ej: 22 Jun") },
                            label = { Text("Fecha") },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DarkGreen, focusedLabelColor = DarkGreen)
                        )

                        // Toggle status buttons
                        Row(
                            modifier = Modifier.weight(2f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Button(
                                onClick = { selectedStatusType = "Asistió" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedStatusType == "Asistió") DarkGreen else SlateGray.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(48.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text("Asistió", color = if (selectedStatusType == "Asistió") Bone else NavyBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { selectedStatusType = "Faltó" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedStatusType == "Faltó") Terracotta else SlateGray.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(48.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text("Faltó", color = if (selectedStatusType == "Faltó") Bone else NavyBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { selectedStatusType = "Canceló" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedStatusType == "Canceló") Color.Gray else SlateGray.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1.5f).height(48.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text("Canceló", color = if (selectedStatusType == "Canceló") Bone else NavyBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val inputDate = manualDateInput.ifBlank {
                                java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault()).format(java.util.Date())
                            }
                            val isPresentVal = selectedStatusType == "Asistió"
                            val isCancelledVal = selectedStatusType == "Canceló"
                            viewModel.registerAttendanceLog(subject.id, isPresent = isPresentVal, isCancelled = isCancelledVal, dateStr = inputDate)
                            manualDateInput = ""
                        },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cargar clase registrada", color = Bone, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        } else if (activeDetailTab == "Tareas") {
            // PLANIFICADOR DE TAREAS / TO-DO LIST
            val allTasks by viewModel.tasks.collectAsStateWithLifecycle(emptyList())
            val subjectTasks = remember(allTasks, subject.id) {
                allTasks.filter { it.subjectId == subject.id }
            }
            val pendingTasks = subjectTasks.filter { !it.isCompleted }
            val completedTasks = subjectTasks.filter { it.isCompleted }

            var taskTitleInput by remember { mutableStateOf("") }
            var taskTypeSelected by remember { mutableStateOf("Tarea") } // "Tarea", "Laboratorio", "Proyecto", "Examen"
            var taskDueDateInput by remember { mutableStateOf("") }

            Text(
                text = "ENTREGAS PENDIENTES (${pendingTasks.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NavyBlue,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (pendingTasks.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Bone)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        BuddyMascot(
                            modifier = Modifier.size(56.dp),
                            isHappy = true,
                            mainColor = try { Color(android.graphics.Color.parseColor(subject.colorHex)) } catch(e: Exception) { DarkGreen }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "¡Al día por aquí!",
                            fontWeight = FontWeight.Bold,
                            color = NavyBlue,
                            fontSize = 14.sp
                        )
                        Text(
                            "No tienes tareas pendientes para esta materia.",
                            color = SlateGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pendingTasks.forEach { task ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Bone)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Checkbox(
                                        checked = task.isCompleted,
                                        onCheckedChange = { viewModel.toggleTask(task) },
                                        colors = CheckboxDefaults.colors(checkedColor = DarkGreen)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column {
                                        Text(
                                            text = task.title,
                                            fontWeight = FontWeight.Bold,
                                            color = NavyBlue,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            // Badge for Type
                                            val badgeBg = when (task.type) {
                                                "Laboratorio" -> Color(0xFFE0F7FA)
                                                "Proyecto" -> Color(0xFFEDE7F6)
                                                "Examen" -> Color(0xFFFFEBEE)
                                                else -> Color(0xFFF1F5F9)
                                            }
                                            val badgeTxt = when (task.type) {
                                                "Laboratorio" -> Color(0xFF006064)
                                                "Proyecto" -> Color(0xFF311B92)
                                                "Examen" -> Color(0xFFB71C1C)
                                                else -> SlateGray
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(badgeBg)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(task.type, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = badgeTxt)
                                            }

                                            // Due Date
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(10.dp), tint = SlateGray)
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("Entrega: ${task.dueDate}", fontSize = 10.sp, color = SlateGray, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                                if (task.type == "Examen") {
                                    val context = LocalContext.current
                                    IconButton(
                                        onClick = {
                                            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                                            val fullDateStr = if (task.dueDate.split(" ").size < 3) "${task.dueDate} $currentYear" else task.dueDate
                                            CalendarExportHelper.exportExamToCalendar(
                                                context = context,
                                                subjectName = subject.name,
                                                examTitle = task.title,
                                                dateStr = fullDateStr
                                            )
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Agendar examen", tint = DarkGreen, modifier = Modifier.size(18.dp))
                                    }
                                }
                                IconButton(onClick = { viewModel.deleteTask(task.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar tarea", tint = Terracotta, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            // ADD NEW TASK FORM
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, Bone)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Nueva Entrega o Tarea", style = MaterialTheme.typography.titleSmall, color = NavyBlue, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = taskTitleInput,
                        onValueChange = { taskTitleInput = it },
                        placeholder = { Text("Ej: Proyecto Final / Lab 3") },
                        label = { Text("Título de la tarea") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DarkGreen, focusedLabelColor = DarkGreen)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Task type chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Tarea", "Laboratorio", "Proyecto", "Examen").forEach { type ->
                            val isSelected = taskTypeSelected == type
                            val chipBg = if (isSelected) DarkGreen else SlateGray.copy(alpha = 0.1f)
                            val chipTxt = if (isSelected) Bone else NavyBlue
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(chipBg)
                                    .clickable { taskTypeSelected = type }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(type, color = chipTxt, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = taskDueDateInput,
                            onValueChange = { taskDueDateInput = it },
                            placeholder = { Text("Ej: 28 Jun") },
                            label = { Text("Fecha de Entrega") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DarkGreen, focusedLabelColor = DarkGreen)
                        )
                        
                        Button(
                            onClick = {
                                if (taskTitleInput.isNotBlank()) {
                                    val finalDueDate = taskDueDateInput.ifBlank {
                                        java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault()).format(java.util.Date())
                                    }
                                    viewModel.addTask(subject.id, taskTitleInput, taskTypeSelected, finalDueDate)
                                    taskTitleInput = ""
                                    taskDueDateInput = ""
                                }
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Añadir", color = Bone, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // COMPLETED TASKS SECTION
            if (completedTasks.isNotEmpty()) {
                Text(
                    text = "ENTREGAS COMPLETADAS (${completedTasks.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SlateGray,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    completedTasks.forEach { task ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Bone.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Checkbox(
                                        checked = task.isCompleted,
                                        onCheckedChange = { viewModel.toggleTask(task) },
                                        colors = CheckboxDefaults.colors(checkedColor = SlateGray)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = task.title,
                                        fontWeight = FontWeight.Medium,
                                        color = SlateGray,
                                        fontSize = 13.sp,
                                        style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteTask(task.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar tarea", tint = Terracotta.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // QUÉ LLEVAR CHECKLIST TAB
            val allChecklistItems by viewModel.checklistItems.collectAsStateWithLifecycle(emptyList())
            val subjectChecklistItems = remember(allChecklistItems, subject.id) {
                allChecklistItems.filter { it.subjectId == subject.id }
            }

            var checklistItemTitleInput by remember { mutableStateOf("") }
            var isExceptionItem by remember { mutableStateOf(false) }
            var exceptionDateInput by remember { mutableStateOf("") }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Checklist de Materiales (Qué llevar)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyBlue)
                    Text("Registra los implementos, libros o materiales que debes llevar siempre o en una fecha específica.", fontSize = 12.sp, color = SlateGray)

                    // Input form
                    OutlinedTextField(
                        value = checklistItemTitleInput,
                        onValueChange = { checklistItemTitleInput = it },
                        placeholder = { Text("Ej. Calculadora, Bata de Lab, Proyecto Impreso") },
                        modifier = Modifier.fillMaxWidth().testTag("checklist_item_input")
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = isExceptionItem,
                            onCheckedChange = { isExceptionItem = it },
                            colors = CheckboxDefaults.colors(checkedColor = ProBlue)
                        )
                        Text("¿Llevar solo en una fecha específica?", fontSize = 13.sp, color = NavyBlue)
                    }

                    if (isExceptionItem) {
                        OutlinedTextField(
                            value = exceptionDateInput,
                            onValueChange = { exceptionDateInput = it },
                            placeholder = { Text("Fecha de excepción (Ej. 24 de Oct)") },
                            modifier = Modifier.fillMaxWidth().testTag("checklist_date_input")
                        )
                    }

                    Button(
                        onClick = {
                            if (checklistItemTitleInput.isNotBlank()) {
                                val date = if (isExceptionItem && exceptionDateInput.isNotBlank()) exceptionDateInput else null
                                viewModel.addChecklistItem(subject.id, checklistItemTitleInput, date)
                                checklistItemTitleInput = ""
                                exceptionDateInput = ""
                                isExceptionItem = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ProBlue),
                        modifier = Modifier.fillMaxWidth().testTag("add_checklist_button")
                    ) {
                        Text("Agregar Implemento")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 1: Recurring (Always carry)
            val recurringItems = subjectChecklistItems.filter { it.date == null }
            val specificDateItems = subjectChecklistItems.filter { it.date != null }

            Text("IMPLEMENTOS PARA TODAS LAS CLASES", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = NavyBlue, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(6.dp))

            if (recurringItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay implementos fijos registrados para esta materia.", fontSize = 12.sp, color = SlateGray)
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        recurringItems.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Checkbox(
                                        checked = item.isCompleted,
                                        onCheckedChange = { viewModel.toggleChecklistItem(item) },
                                        colors = CheckboxDefaults.colors(checkedColor = ProBlue),
                                        modifier = Modifier.testTag("checklist_check_${item.id}")
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = item.itemName,
                                        fontWeight = FontWeight.Medium,
                                        color = if (item.isCompleted) SlateGray else NavyBlue,
                                        fontSize = 13.sp,
                                        style = if (item.isCompleted) androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else androidx.compose.ui.text.TextStyle.Default
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.deleteChecklistItem(item.id) },
                                    modifier = Modifier.testTag("delete_checklist_${item.id}")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Terracotta.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 2: Specific Dates (Exceptions)
            Text("LLEVAR EN FECHAS ESPECÍFICAS (EXCEPCIONES)", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = NavyBlue, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(6.dp))

            if (specificDateItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay implementos por fecha única registrados.", fontSize = 12.sp, color = SlateGray)
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        specificDateItems.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Checkbox(
                                        checked = item.isCompleted,
                                        onCheckedChange = { viewModel.toggleChecklistItem(item) },
                                        colors = CheckboxDefaults.colors(checkedColor = ProBlue),
                                        modifier = Modifier.testTag("checklist_check_${item.id}")
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column {
                                        Text(
                                            text = item.itemName,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.isCompleted) SlateGray else NavyBlue,
                                            fontSize = 13.sp,
                                            style = if (item.isCompleted) androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else androidx.compose.ui.text.TextStyle.Default
                                        )
                                        Text(
                                            text = "Requerido para: ${item.date}",
                                            fontSize = 11.sp,
                                            color = ProBlue,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { viewModel.deleteChecklistItem(item.id) },
                                    modifier = Modifier.testTag("delete_checklist_${item.id}")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Terracotta.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, fontSize = 10.sp, color = SlateGray, fontWeight = FontWeight.Bold)
    }
}
