package com.aistudio.unibuddy.qywvsp.ui

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlinx.coroutines.launch


// Map Vector Drawing for Next Class Card Background
@Composable
fun StylizedMapBackground(modifier: Modifier = Modifier, isUserOnCampus: Boolean = false) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Background tinted translucent
        drawRoundRect(
            color = Bone.copy(alpha = 0.55f),
            size = size,
            cornerRadius = CornerRadius(20.dp.toPx())
        )

        // Map outline paths
        val strokeColor = SlateGray.copy(alpha = 0.18f)
        val strokeWidth = 4.dp.toPx()

        // Street grid roads
        for (i in 1..4) {
            val posX = w * (i / 5.0f)
            drawLine(
                color = strokeColor,
                start = Offset(posX, 0f),
                end = Offset(posX, h),
                strokeWidth = strokeWidth
            )
        }
        for (i in 1..3) {
            val posY = h * (i / 4.0f)
            drawLine(
                color = strokeColor,
                start = Offset(0f, posY),
                end = Offset(w, posY),
                strokeWidth = strokeWidth
            )
        }

        // Draw a decorative subtle dashed route from origin to destination representing college route
        val routePath = Path().apply {
            moveTo(w * 0.15f, h * 0.72f)
            lineTo(w * 0.42f, h * 0.72f)
            lineTo(w * 0.42f, h * 0.28f)
            lineTo(w * 0.85f, h * 0.28f)
        }
        drawPath(
            path = routePath,
            color = NavyBlue.copy(alpha = 0.08f), // very subtle so it does not interfere or look like a glitch
            style = Stroke(
                width = 3.dp.toPx(),
                miter = 4f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
            )
        )
        // Add subtle nodes
        drawCircle(color = NavyBlue.copy(alpha = 0.12f), radius = 5.dp.toPx(), center = Offset(w * 0.15f, h * 0.72f))
        
        // University location
        val uniCenter = Offset(w * 0.85f, h * 0.28f)
        drawCircle(color = NavyBlue.copy(alpha = 0.15f), radius = 5.dp.toPx(), center = uniCenter)

        if (isUserOnCampus) {
            // Draw a small avatar / pin on the university node
            drawCircle(color = MintGreen, radius = 8.dp.toPx(), center = uniCenter)
            drawCircle(color = NavyBlue, radius = 4.dp.toPx(), center = uniCenter)
        }
    }
}

// Navigation screens
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Onboarding : Screen("onboarding", "Onboarding", Icons.Default.PlayArrow)
    object Inicio : Screen("inicio", "Inicio", Icons.Default.Home)
    object FocusMode : Screen("focus_mode", "Focus", Icons.Default.Lock)
    object Asistencia : Screen("asistencia", "Asistencia", Icons.Default.CheckCircle)
    object Notas : Screen("notas", "Notas", Icons.Default.Star)
    object Config_Tab : Screen("configuracion_tab", "Ajustes", Icons.Default.Settings)
    object Pensum : Screen("pensum", "Progreso", Icons.Default.DateRange)
}

@Composable
fun UniBuddyApp(viewModel: UniBuddyViewModel) {
    val isInitialized by viewModel.isInitialized.collectAsStateWithLifecycle()
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()
    
    if (!isInitialized) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Inicio) }
    
    // Auxiliary state to view subject details (Asistencia)
    var selectedSubjectIdForDetails by remember { mutableStateOf<Int?>(null) }
    // Auxiliary state to view assessment list for a subject (Notas)
    var selectedSubjectIdForGrades by remember { mutableStateOf<Int?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()

    LaunchedEffect(viewModel.snackbarEvent) {
        viewModel.snackbarEvent.collect { message ->
            snackbarScope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = "Deshacer",
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.undoLastAttendanceLog()
                }
            }
        }
    }

    LaunchedEffect(isOnboardingCompleted) {
        if (!isOnboardingCompleted) {
            currentScreen = Screen.Onboarding
        } else if (currentScreen == Screen.Onboarding) {
            currentScreen = Screen.Inicio
        }
    }

    val backHandlerEnabled = selectedSubjectIdForDetails != null || 
            selectedSubjectIdForGrades != null || 
            currentScreen == Screen.Pensum || 
            (currentScreen != Screen.Inicio && currentScreen != Screen.Onboarding)

    androidx.activity.compose.BackHandler(enabled = backHandlerEnabled) {
        when {
            selectedSubjectIdForDetails != null -> {
                selectedSubjectIdForDetails = null
            }
            selectedSubjectIdForGrades != null -> {
                selectedSubjectIdForGrades = null
            }
            currentScreen == Screen.Pensum -> {
                currentScreen = Screen.Config_Tab
            }
            currentScreen != Screen.Inicio && currentScreen != Screen.Onboarding -> {
                currentScreen = Screen.Inicio
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (currentScreen != Screen.Onboarding) {
                NavigationBar(
                    containerColor = Color(0xFFF0F4F8),
                    modifier = Modifier.shadow(8.dp)
                ) {
                    val screens = listOf(Screen.Inicio, Screen.FocusMode, Screen.Asistencia, Screen.Notas, Screen.Config_Tab)
                    screens.forEach { s ->
                        val selected = currentScreen == s
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                selectedSubjectIdForDetails = null
                                selectedSubjectIdForGrades = null
                                currentScreen = s
                            },
                            icon = {
                                Icon(
                                    imageVector = s.icon,
                                    contentDescription = s.title,
                                    tint = if (selected) NavyBlue else SlateGray
                                )
                            },
                            label = {
                                Text(
                                    text = s.title,
                                    color = if (selected) NavyBlue else SlateGray,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color(0xFFD3E3FD),
                            )
                        )
                    }
                }
            }
        },
        containerColor = BackgroundBone
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).imePadding()) {
            AnimatedContent(
                targetState = currentScreen,
                label = "screen_transition",
                transitionSpec = {
                    if (initialState == Screen.Onboarding && targetState == Screen.Inicio) {
                        (slideInVertically { height -> height } + fadeIn(animationSpec = tween(500)))
                            .togetherWith(slideOutVertically { height -> -height } + fadeOut(animationSpec = tween(500)))
                    } else {
                        (fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.95f, animationSpec = tween(300)))
                            .togetherWith(fadeOut(animationSpec = tween(200)))
                    }
                }
            ) { targetScreen ->
                when (targetScreen) {
                    Screen.Onboarding -> {
                        OnboardingScreen(
                            viewModel = viewModel,
                            onFinished = {
                                viewModel.updateOnboardingStatus(true)
                                viewModel.startNewSemester()
                                currentScreen = Screen.Inicio
                            }
                        )
                    }
                    Screen.Inicio -> {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToDetails = { subjectId ->
                                selectedSubjectIdForDetails = subjectId
                                currentScreen = Screen.Asistencia
                            },
                            onNavigateToGrades = { subjectId ->
                                selectedSubjectIdForGrades = subjectId
                                currentScreen = Screen.Notas
                            },
                            onConfigureRoute = {
                                currentScreen = Screen.Config_Tab
                            },
                            onNavigateToFocus = {
                                currentScreen = Screen.FocusMode
                            }
                        )
                    }
                    Screen.Asistencia -> {
                        AnimatedContent(
                            targetState = selectedSubjectIdForDetails,
                            label = "asistencia_details_transition",
                            transitionSpec = {
                                if (targetState != null) {
                                    (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                                } else {
                                    (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                                }
                            }
                        ) { subjectId ->
                            if (subjectId == null) {
                                AsistenciaOverviewScreen(
                                    viewModel = viewModel,
                                    onSubjectClick = { clickedId ->
                                        selectedSubjectIdForDetails = clickedId
                                    },
                                    onConfigureRoute = {
                                        currentScreen = Screen.Config_Tab
                                    }
                                )
                            } else {
                                SubjectDetailsScreen(
                                    viewModel = viewModel,
                                    subjectId = subjectId,
                                    onBack = { selectedSubjectIdForDetails = null }
                                )
                            }
                        }
                    }
                    Screen.Notas -> {
                        AnimatedContent(
                            targetState = selectedSubjectIdForGrades,
                            label = "notas_details_transition",
                            transitionSpec = {
                                if (targetState != null) {
                                    (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                                } else {
                                    (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                                }
                            }
                        ) { subjectId ->
                            if (subjectId == null) {
                                GradesOverviewScreen(
                                    viewModel = viewModel,
                                    onSubjectClick = { clickedId ->
                                        selectedSubjectIdForGrades = clickedId
                                    },
                                    onConfigureRoute = {
                                        currentScreen = Screen.Config_Tab
                                    }
                                )
                            } else {
                                SubjectGradesScreen(
                                    viewModel = viewModel,
                                    subjectId = subjectId,
                                    onBack = { selectedSubjectIdForGrades = null }
                                )
                            }
                        }
                    }
                    Screen.Config_Tab -> {
                        SettingsScreen(
                            viewModel = viewModel,
                            onNavigateToPensum = { currentScreen = Screen.Pensum }
                        )
                    }
                    Screen.Pensum -> {
                        PensumProgressScreen(viewModel = viewModel)
                    }
                    Screen.FocusMode -> {
                        FocusModeScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

// Helper Location Search field
@OptIn(ExperimentalMaterial3Api::class)

fun calculateSubjectCriticality(subjectName: String): String {
    val upperName = subjectName.uppercase()
    return when {
        upperName.contains("MATEMÁTICA") || upperName.contains("MATEMATICA") || 
        upperName.contains("FÍSICA") || upperName.contains("FISICA") ||
        upperName.contains("PROGRAMACIÓN") || upperName.contains("PROGRAMACION") ||
        upperName.contains("ESTUDIO DEL TRABAJO") || upperName.contains("ESTADISTICA") ||
        upperName.contains("INVESTIGACION DE OPERACIONES") -> "Alta (Pre-requisito crítico)"
        
        upperName.contains("QUÍMICA") || upperName.contains("MECANICA") ||
        upperName.contains("TERMODINÁMICA") || upperName.contains("SISTEMAS PRODUCTIVOS") ||
        upperName.contains("MÉTODOS NUMÉRICOS") || upperName.contains("PROCESOS DE MANUFACTURA") -> "Media"
        
        else -> "Baja"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetDropdownField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    options: List<String>,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DarkGreen, focusedLabelColor = DarkGreen),
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            singleLine = true,
            readOnly = readOnly
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onValueChange(selectionOption)
                        expanded = false
                    }
                )
            }
        }
    }
}

// 2. DASHBOARD SCREEN (Vista de Inicio)
fun getSubjectColorPalette(colorHex: String): Pair<Color, Color> {
    val hex = if (colorHex.isBlank() || !colorHex.startsWith("#")) "#E8F5E9" else colorHex
    val parsedColor = try { android.graphics.Color.parseColor(hex) } catch (e: Exception) { android.graphics.Color.parseColor("#E8F5E9") }
    val bgColor = Color(parsedColor)
    
    // Calculate a darker accent color for text/borders
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(parsedColor, hsv)
    hsv[1] = (hsv[1] + 0.4f).coerceAtMost(1f) // Increase saturation
    hsv[2] = (hsv[2] - 0.4f).coerceAtLeast(0f) // Decrease value (brightness)
    val accentColor = Color(android.graphics.Color.HSVToColor(hsv))

    return Pair(bgColor, accentColor)
}

@Composable
fun SubjectGridItem(
    sub: Subject,
    absences: List<Absence>,
    attendanceLogs: List<AttendanceLog>,
    viewModel: UniBuddyViewModel,
    onSubjectClick: (Int) -> Unit,
    onCheckInClick: (Subject) -> Unit,
    onAbsentClick: (Subject) -> Unit,
    onJustifyClick: (Subject) -> Unit
) {
    val legacySubAbs = absences.filter { it.subjectId == sub.id }
    val subLogs = attendanceLogs.filter { it.subjectId == sub.id }
    
    val subAbsCount = subLogs.count { !it.isPresent }.coerceAtLeast(legacySubAbs.size)
    val subPresCount = subLogs.count { it.isPresent }
    val totalClassesHeld = subPresCount + subAbsCount
    val activeAttendanceRate = if (totalClassesHeld > 0) (subPresCount.toDouble() / totalClassesHeld * 100.0) else 100.0

    val maxAbs = sub.totalClasses - ceil(sub.totalClasses * (sub.requiredAttendancePercent / 100.0)).toInt()
    val remaining = maxAbs - subAbsCount
    
    val fractionUsed = if (maxAbs > 0) subAbsCount.toFloat() / maxAbs.toFloat() else 0f
    val percentageUsed = (fractionUsed * 100).toInt()

    val isWarning = remaining <= 2 && remaining > 0
    val isCritical = remaining <= 0

    val colors = getSubjectColorPalette(sub.colorHex)
    val bgColor = colors.first
    val accentColor = colors.second

    val hapticFeedback = LocalHapticFeedback.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onSubjectClick(sub.id)
            }
            .animateContentSize()
            .shadow(4.dp, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            val defaultSession = sub.sessions.firstOrNull()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sub.name,
                        fontSize = 15.sp,
                        color = NavyBlue,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = defaultSession?.room ?: "",
                        fontSize = 11.sp,
                        color = SlateGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCritical) Icons.Default.Warning else Icons.Default.Check,
                        contentDescription = null,
                        tint = if (isCritical) Terracotta else accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${sub.schedule} • ${defaultSession?.time ?: "10:00 AM"} | ${defaultSession?.room ?: ""}",
                fontSize = 10.sp,
                color = SlateGray,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Percentage attendance indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = fractionUsed.coerceIn(0f, 1f),
                    color = if (isCritical) Terracotta else if (isWarning) Amber else accentColor,
                    trackColor = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$percentageUsed%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isCritical) Terracotta else NavyBlue
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Has asistido a $subPresCount/${sub.totalClasses} clases",
                    fontSize = 10.sp,
                    color = SlateGray,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (remaining > 0) "Quedan $remaining de $maxAbs faltas" else "Estado Crítico",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCritical) Terracotta else NavyBlue
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Toca para ver detalles",
                fontSize = 9.sp,
                color = SlateGray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun WeeklyScheduleView(subjects: List<Subject>) {
    val days = listOf(
        "Lu" to "Lunes",
        "Ma" to "Martes",
        "Mi" to "Miércoles",
        "Ju" to "Jueves",
        "Vi" to "Viernes",
        "Sá" to "Sábado"
    )
    
    val calendar = Calendar.getInstance()
    val initialDayCode = when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Lu"
        Calendar.TUESDAY -> "Ma"
        Calendar.WEDNESDAY -> "Mi"
        Calendar.THURSDAY -> "Ju"
        Calendar.FRIDAY -> "Vi"
        Calendar.SATURDAY -> "Sá"
        else -> "Lu"
    }
    
    var selectedDayCode by remember { mutableStateOf(initialDayCode) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { (code, name) ->
                val isSelected = selectedDayCode == code
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) NavyBlue else Color.Transparent)
                        .clickable { selectedDayCode = code }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = code,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isSelected) Color.White else NavyBlue
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        val daySessions = remember(subjects, selectedDayCode) {
            val list = mutableListOf<Pair<Subject, ClassSessionDetails>>()
            subjects.forEach { sub ->
                sub.sessions.forEach { session ->
                    if (session.day.equals(selectedDayCode, ignoreCase = true)) {
                        list.add(Pair(sub, session))
                    }
                }
            }
            list.sortedBy { it.second.time }
        }
        
        if (daySessions.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                BuddyMascot(
                    modifier = Modifier.size(60.dp),
                    isHappy = true,
                    pose = "sleeping"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "¡Día libre de clases!",
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue,
                    fontSize = 14.sp
                )
                Text(
                    text = "No tienes materias programadas para hoy.",
                    color = SlateGray,
                    fontSize = 11.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                daySessions.forEach { (sub, session) ->
                    val (bgColor, accentColor) = getSubjectColorPalette(sub.colorHex)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(1.dp, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(40.dp)
                                    .background(accentColor, RoundedCornerShape(2.dp))
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sub.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = accentColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        tint = accentColor.copy(alpha = 0.7f),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = session.room,
                                        fontSize = 11.sp,
                                        color = accentColor.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = session.time,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = accentColor
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        val context = androidx.compose.ui.platform.LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        Button(
            onClick = {
                coroutineScope.launch {
                    ScheduleExporter.exportToGallery(context, subjects, "Mi Universidad")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = ProBlue),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Exportar",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Exportar Horario como PNG",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun AttendanceLockedDialog(
    subject: Subject,
    currentLocation: String,
    onDismiss: () -> Unit,
    onBypass: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Terracotta,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "¡Alto Ahí, Socio!",
                    color = NavyBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.CenterHorizontally)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF1F1)),
                    contentAlignment = Alignment.Center
                ) {
                    BuddyMascot(
                        modifier = Modifier.fillMaxSize().padding(4.dp),
                        isWorried = true,
                        pose = "idle"
                    )
                }
                Text(
                    text = "No podés registrar asistencia para '${subject.name}' todavía. El GPS de Buddy detecta restricciones activas:",
                    color = NavyBlue,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Ubicación Check
                val isLocationValid = currentLocation.trim().equals("En la universidad", ignoreCase = true)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isLocationValid) Icons.Default.CheckCircle else Icons.Default.Clear,
                        contentDescription = null,
                        tint = if (isLocationValid) DarkGreen else Terracotta,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Presencia en el Campus",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyBlue
                        )
                        Text(
                            text = if (isLocationValid) "Estás en el recinto universitario" else "Ubicación: $currentLocation (Debes estar en la U)",
                            fontSize = 11.sp,
                            color = SlateGray
                        )
                    }
                }
                
                // Horario Check
                val todayCode = when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> "Lu"
                    Calendar.TUESDAY -> "Ma"
                    Calendar.WEDNESDAY -> "Mi"
                    Calendar.THURSDAY -> "Ju"
                    Calendar.FRIDAY -> "Vi"
                    Calendar.SATURDAY -> "Sá"
                    else -> "Do"
                }
                val isScheduledToday = subject.schedule.contains(todayCode, ignoreCase = true)
                val sessions = subject.sessions
                val matchingSession = sessions.find { it.day.trim().equals(todayCode, ignoreCase = true) }
                val isTimeValid = if (matchingSession != null) {
                    try {
                        val now = Calendar.getInstance()
                        val currentHour = now.get(Calendar.HOUR_OF_DAY)
                        val currentMin = now.get(Calendar.MINUTE)
                        val currentTotalMin = currentHour * 60 + currentMin
                        val parts = matchingSession.time.split("-")
                        if (parts.size == 2) {
                            val startParts = parts[0].trim().split(":")
                            val endParts = parts[1].trim().split(":")
                            if (startParts.size >= 2 && endParts.size >= 2) {
                                val startHour = startParts[0].toInt()
                                val startMin = startParts[1].replace(Regex("[^0-9]"), "").toInt()
                                val endHour = endParts[0].toInt()
                                val endMin = endParts[1].replace(Regex("[^0-9]"), "").toInt()
                                val startTotalMin = startHour * 60 + startMin
                                val endTotalMin = endHour * 60 + endMin
                                currentTotalMin in (startTotalMin - 30)..(endTotalMin + 30)
                            } else true
                        } else true
                    } catch (e: Exception) { true }
                } else false
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isScheduledToday && isTimeValid) Icons.Default.CheckCircle else Icons.Default.Clear,
                        contentDescription = null,
                        tint = if (isScheduledToday && isTimeValid) DarkGreen else Terracotta,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Horario de Clase",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyBlue
                        )
                        Text(
                            text = when {
                                !isScheduledToday -> "Hoy no cursás esta materia (${subject.schedule})"
                                !isTimeValid -> "Horario: ${matchingSession?.time ?: "Fuera de hora"}. ¡No podés adelantarte!"
                                else -> "Estás dentro del horario de clases"
                            },
                            fontSize = 11.sp,
                            color = SlateGray
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onBypass,
                colors = ButtonDefaults.buttonColors(containerColor = SlateGray)
            ) {
                Text("Bypass de pruebas", fontSize = 11.sp, color = Bone)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Entendido", color = NavyBlue)
            }
        },
        containerColor = Color.White
    )
}

// 3. ASISTENCIA OVERVIEW SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsistenciaOverviewScreen(viewModel: UniBuddyViewModel, onSubjectClick: (Int) -> Unit, onConfigureRoute: () -> Unit) {
    val semesterState by viewModel.semesterState.collectAsStateWithLifecycle()
    if (semesterState == "Vacaciones") {
        VacationScreen(viewModel = viewModel, onConfigureRoute = onConfigureRoute)
        return
    }

    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val absences by viewModel.absences.collectAsStateWithLifecycle()
    val attendanceLogs by viewModel.attendanceLogs.collectAsStateWithLifecycle()
    val weeklyStreak by viewModel.weeklyStreak.collectAsStateWithLifecycle()
    var viewMode by remember { mutableStateOf("materias") } // "materias" or "horario"
    var gpsConfirmSubject by remember { mutableStateOf<Subject?>(null) }
    val currentLocationName by viewModel.currentLocationName.collectAsStateWithLifecycle()
    var blockedCheckInSubject by remember { mutableStateOf<Subject?>(null) }

    val globalAttendancePercent = remember(subjects, attendanceLogs) {
        val totalPres = attendanceLogs.count { it.isPresent }
        val totalAbs = attendanceLogs.count { !it.isPresent }
        val total = totalPres + totalAbs
        if (total > 0) (totalPres.toFloat() / total.toFloat() * 100f).toInt() else 100
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dynamic Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Mi Asistencia", style = MaterialTheme.typography.headlineLarge, color = NavyBlue, fontWeight = FontWeight.Bold)
                    Text(text = "Rinde al máximo sin descuidar tu cupo de faltas.", style = MaterialTheme.typography.bodyMedium, color = SlateGray)
                }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                ) {
                    BuddyMascot(modifier = Modifier.fillMaxSize(), isHappy = true)
                }
            }
        }

        // Global Radial Speedometer Gauge Card & Stats Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = NavyBlue),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Custom Draw Radial Progress Gauge
                    Box(
                        modifier = Modifier.size(110.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Track
                            drawArc(
                                color = Color.White.copy(alpha = 0.15f),
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = 10.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                            )
                            // Progress
                            val swept = (globalAttendancePercent / 100f) * 270f
                            val gaugeColor = when {
                                globalAttendancePercent >= 85 -> MintGreen
                                globalAttendancePercent >= 70 -> Amber
                                else -> Terracotta
                            }
                            drawArc(
                                color = gaugeColor,
                                startAngle = 135f,
                                sweepAngle = swept,
                                useCenter = false,
                                style = Stroke(width = 10.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$globalAttendancePercent%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Bone
                            )
                            Text(
                                text = "Global",
                                fontSize = 10.sp,
                                color = Bone.copy(alpha = 0.7f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = Bone.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Bone, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Racha Reciente", fontSize = 9.sp, color = Bone.copy(alpha = 0.7f))
                                    Text("$weeklyStreak Clases", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Bone)
                                }
                            }
                        }

                        Surface(
                            color = Bone.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Bone, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Faltas Registradas", fontSize = 9.sp, color = Bone.copy(alpha = 0.7f))
                                    Text("${absences.size + attendanceLogs.count { !it.isPresent }} Faltas", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Bone)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Info Banner: Academic Rules (14 Semanas + 1 Exámenes)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.DateRange, contentDescription = null, tint = NavyBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Semestre establecido: 14 semanas lectivas completas más 1 semana dedicada a los exámenes de fin de término.",
                        fontSize = 11.sp,
                        color = NavyBlue,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Segmented selector for view mode
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFECEFF1), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (viewMode == "materias") NavyBlue else Color.Transparent)
                        .clickable { viewMode = "materias" }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Mis Materias",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (viewMode == "materias") Color.White else NavyBlue
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (viewMode == "horario") NavyBlue else Color.Transparent)
                        .clickable { viewMode = "horario" }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Horario",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (viewMode == "horario") Color.White else NavyBlue
                    )
                }
            }
        }

        if (viewMode == "horario") {
            item {
                WeeklyScheduleView(subjects = subjects)
            }
        } else {
            // Grid Title and Add Subject Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tus Materias en Curso",
                        style = MaterialTheme.typography.titleMedium,
                        color = NavyBlue,
                        fontWeight = FontWeight.Bold
                    )
                    
                    var showAddSubjectDialog by remember { mutableStateOf(false) }
                    
                    Button(
                        onClick = { showAddSubjectDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Bone, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Agregar", color = Bone, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (showAddSubjectDialog) {
                        var newSubName by remember { mutableStateOf("") }
                        var selectedColor by remember { mutableStateOf("#E8F5E9") } // Default light green
                        
                        val sessionsList = remember { mutableStateListOf<com.aistudio.unibuddy.qywvsp.data.ClassSessionDetails>(com.aistudio.unibuddy.qywvsp.data.ClassSessionDetails("Lu", "M1", "")) }
                        
                        val availableColors = listOf("#E8F5E9", "#E3F2FD", "#FFF3E0", "#F3E5F5", "#E0F7FA", "#FFEBEE", "#FFF9C4")

                        AlertDialog(
                            onDismissRequest = { showAddSubjectDialog = false },
                            title = { Text("Nueva Materia", color = NavyBlue, fontWeight = FontWeight.Bold) },
                            text = {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    item {
                                        var expanded by remember { mutableStateOf(false) }
                                        val passedSubjects by viewModel.passedSubjects.collectAsStateWithLifecycle()
                                        val suggestions = com.aistudio.unibuddy.qywvsp.data.CurriculumData.industrialEngineering
                                            .filter { !passedSubjects.contains(it.code) && !subjects.any { s -> s.name == it.name } }
                                            .map { it.name }

                                        ExposedDropdownMenuBox(
                                            expanded = expanded,
                                            onExpandedChange = { expanded = !expanded }
                                        ) {
                                            OutlinedTextField(
                                                value = newSubName,
                                                onValueChange = { newSubName = it },
                                                label = { Text("Nombre de la Materia") },
                                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DarkGreen, focusedLabelColor = DarkGreen),
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                                            )
                                            ExposedDropdownMenu(
                                                expanded = expanded,
                                                onDismissRequest = { expanded = false }
                                            ) {
                                                suggestions.filter { it.contains(newSubName, ignoreCase = true) }.take(5).forEach { selectionOption ->
                                                    DropdownMenuItem(
                                                        text = { Text(selectionOption) },
                                                        onClick = {
                                                            newSubName = selectionOption
                                                            expanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    
                                    item {
                                        Text("Color de la materia:", fontSize = 14.sp, color = NavyBlue, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            availableColors.forEach { hex ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(android.graphics.Color.parseColor(hex)))
                                                        .border(
                                                            width = if (selectedColor == hex) 2.dp else 1.dp,
                                                            color = if (selectedColor == hex) DarkGreen else Color.LightGray,
                                                            shape = CircleShape
                                                        )
                                                        .clickable { selectedColor = hex },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (selectedColor == hex) {
                                                        Icon(Icons.Default.Check, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(20.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    item {
                                        SessionEditor(sessions = sessionsList, allSubjects = subjects)
                                        
                                        val computedClasses = sessionsList.size * 14
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Clases totales estimadas (14 semanas): $computedClasses", fontSize = 12.sp, color = SlateGray)
                                        Text("Asistencia mínima requerida: 70%", fontSize = 12.sp, color = SlateGray)
                                    }
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        if (newSubName.isNotBlank() && sessionsList.isNotEmpty()) {
                                            val computedSchedule = sessionsList.distinctBy { it.day }.joinToString(", ") { it.day }
                                            val computedClasses = sessionsList.size * 14
                                            val jsonString = sessionsList.toJsonString()
                                            viewModel.addSubject(
                                                name = newSubName,
                                                schedule = computedSchedule,
                                                sessions = emptyList() /* removed jsonString */,
                                                requiredAttendancePercent = 70,
                                                totalClasses = computedClasses,
                                                colorHex = selectedColor
                                            )
                                            showAddSubjectDialog = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                                ) {
                                    Text("Añadir", color = Bone)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showAddSubjectDialog = false }) {
                                    Text("Cancelar", color = SlateGray)
                                }
                            },
                            containerColor = Color.White
                        )
                    }
                }
            }

            // Grid Layout implementation: chunking subjects lists in rows of 2
            if (subjects.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            BuddyMascot(
                                modifier = Modifier.size(100.dp),
                                isHappy = true
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("¡Todo tranquilo por aquí!", style = MaterialTheme.typography.titleMedium, color = NavyBlue, fontWeight = FontWeight.Bold)
                            Text("Agrega tu primera materia desde Configuración para empezar.", fontSize = 12.sp, color = SlateGray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
                        }
                    }
                }
            } else {
                                val chunkedSubjects = subjects.chunked(2)
                                items(chunkedSubjects, key = { it.first().id }) { pair ->
                                    val performAction = { clickedSubject: Subject, isPresent: Boolean, isJustified: Boolean ->
                                        val todayCode = when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
                                            Calendar.MONDAY -> "Lu"
                                            Calendar.TUESDAY -> "Ma"
                                            Calendar.WEDNESDAY -> "Mi"
                                            Calendar.THURSDAY -> "Ju"
                                            Calendar.FRIDAY -> "Vi"
                                            Calendar.SATURDAY -> "Sá"
                                            else -> "Do"
                                        }
                                        val isScheduledToday = clickedSubject.schedule.contains(todayCode, ignoreCase = true)
                                        val sessions = clickedSubject.sessions
                                        val matchingSession = sessions.find { it.day.trim().equals(todayCode, ignoreCase = true) }
                                        val isTimeValid = if (matchingSession != null) {
                                            try {
                                                val now = Calendar.getInstance()
                                                val currentHour = now.get(Calendar.HOUR_OF_DAY)
                                                val currentMin = now.get(Calendar.MINUTE)
                                                val currentTotalMin = currentHour * 60 + currentMin
                                                val parts = matchingSession.time.split("-")
                                                if (parts.size == 2) {
                                                    val startParts = parts[0].trim().split(":")
                                                    val endParts = parts[1].trim().split(":")
                                                    if (startParts.size >= 2 && endParts.size >= 2) {
                                                        val startHour = startParts[0].toInt()
                                                        val startMin = startParts[1].replace(Regex("[^0-9]"), "").toInt()
                                                        val endHour = endParts[0].toInt()
                                                        val endMin = endParts[1].replace(Regex("[^0-9]"), "").toInt()
                                                        val startTotalMin = startHour * 60 + startMin
                                                        val endTotalMin = endHour * 60 + endMin
                                                        currentTotalMin in (startTotalMin - 30)..(endTotalMin + 30)
                                                    } else true
                                                } else true
                                            } catch (e: Exception) { true }
                                        } else false

                                        val isAtUni = currentLocationName.trim().equals("En la universidad", ignoreCase = true)

                                        if (isScheduledToday && isTimeValid && (isAtUni || !isPresent)) {
                                            if (isPresent) {
                                                gpsConfirmSubject = clickedSubject
                                            } else {
                                                val todayStr = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault()).format(java.util.Date())
                                                val suffix = if (isJustified) " (Justificada)" else ""
                                                viewModel.registerAttendanceLog(clickedSubject.id, isPresent = false, dateStr = "$todayStr$suffix")
                                            }
                                        } else {
                                            blockedCheckInSubject = clickedSubject
                                        }
                                    }
                                    Row(
                        modifier = Modifier.fillMaxWidth().animateItem(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        pair.forEach { sub ->
                            Box(modifier = Modifier.weight(1f)) {
                                SubjectGridItem(
                                    sub = sub,
                                    absences = absences,
                                    attendanceLogs = attendanceLogs,
                                    viewModel = viewModel,
                                    onSubjectClick = onSubjectClick,
                                    onCheckInClick = { performAction(it, true, false) },
                                    onAbsentClick = { performAction(it, false, false) },
                                    onJustifyClick = { performAction(it, false, true) }
                                )
                            }
                        }
                        if (pair.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    if (gpsConfirmSubject != null) {
        GPSConfirmationDialog(
            subject = gpsConfirmSubject!!,
            viewModel = viewModel,
            onDismiss = { gpsConfirmSubject = null },
            onConfirm = { sub, address ->
                val todayStr = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault()).format(java.util.Date())
                val sectorSuffix = if (address.contains("Sector:")) {
                    " (" + address.substringAfter("Sector:").trim() + ")"
                } else {
                    " (Confirmado)"
                }
                viewModel.registerAttendanceLog(sub.id, isPresent = true, dateStr = "$todayStr$sectorSuffix")
                gpsConfirmSubject = null
            }
        )
    }

    if (blockedCheckInSubject != null) {
        AttendanceLockedDialog(
            subject = blockedCheckInSubject!!,
            currentLocation = currentLocationName,
            onDismiss = { blockedCheckInSubject = null },
            onBypass = {
                gpsConfirmSubject = blockedCheckInSubject
                blockedCheckInSubject = null
            }
        )
    }
}

data class IntegratedLogItem(val id: Int, val date: String, val isPresent: Boolean, val isLegacy: Boolean)

// 5. NOTAS (GRADES OVERVIEW SCREEN)
@Composable
fun SubjectGradeGridCard(
    sub: Subject,
    viewModel: UniBuddyViewModel,
    onClick: (Int) -> Unit
) {
    val assessments by viewModel.getAssessmentsForSubject(sub.id).collectAsStateWithLifecycle(emptyList())

    val currentWeighted = assessments.sumOf { (it.grade ?: 0.0) * (it.percentage / 100.0) }
    val currentPercentage = assessments.sumOf { it.percentage }
    val remainingPercentage = (100.0 - currentPercentage).coerceAtLeast(0.0)

    val targetApprove = 51.0
    val missingAmount = targetApprove - currentWeighted
    val gradeNeeded = if (remainingPercentage > 0) {
        (missingAmount / (remainingPercentage / 100.0)).coerceIn(0.0, 100.0)
    } else 0.0

    val roundedGrade = String.format(Locale.US, "%.1f", gradeNeeded)
    val isImpossible = gradeNeeded > 100.0

    val colors = getSubjectColorPalette(sub.colorHex)
    val bgColor = colors.first
    val accentColor = colors.second

    val hapticFeedback = LocalHapticFeedback.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick(sub.id)
            }
            .animateContentSize()
            .shadow(3.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sub.name,
                        fontSize = 15.sp,
                        color = NavyBlue,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val defaultSession = sub.sessions.firstOrNull()
                    Text(
                        text = defaultSession?.room ?: "",
                        fontSize = 10.sp,
                        color = SlateGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = (currentWeighted / 100.0).toFloat().coerceIn(0f, 1f),
                color = accentColor,
                trackColor = Color.White.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Progreso total:",
                    fontSize = 10.sp,
                    color = SlateGray
                )
                Text(
                    text = "${currentPercentage.toInt()}%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue
                )
            }
            Text(
                text = "Nota acumulada: ${String.format(Locale.US, "%.1f", currentWeighted)} / 100",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NavyBlue,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE2E8F0).copy(alpha = 0.5f)))
            Spacer(modifier = Modifier.height(8.dp))

            // Exams list inside Card (Ponle mas elementos)
            Text(
                text = "Exámenes de la materia:",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = NavyBlue.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (assessments.isNotEmpty()) {
                assessments.take(2).forEach { exam ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = exam.name,
                            fontSize = 10.sp,
                            color = SlateGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (exam.grade != null) String.format(Locale.US, "%.1f (%.0f%%)", exam.grade, exam.percentage) else "Pend. (%.0f%%)".format(exam.percentage),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (exam.grade != null) NavyBlue else SlateGray
                        )
                    }
                }
                if (assessments.size > 2) {
                    Text(
                        text = "+ ${assessments.size - 2} exámenes más...",
                        fontSize = 9.sp,
                        color = SlateGray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            } else {
                Text(
                    text = "No hay exámenes registrados.",
                    fontSize = 10.sp,
                    color = SlateGray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE2E8F0).copy(alpha = 0.5f)))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Alert target
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isImpossible) Terracotta.copy(alpha = 0.1f) else Color.White,
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        1.dp,
                        if (isImpossible) Terracotta else Color(0xFFE2E8F0),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(6.dp)
            ) {
                Column {
                    Text(
                        text = if (isImpossible) "No alcanzable" else "Próximo examen necesario",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isImpossible) Terracotta else accentColor
                    )
                    Text(
                        text = if (isImpossible) "Revisar ponderación" else "Necesitás sacar un $roundedGrade",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isImpossible) Terracotta else NavyBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Toca para simular notas >>",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = SlateGray,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

@Composable
fun VacationScreen(viewModel: UniBuddyViewModel, onConfigureRoute: () -> Unit) {
    val passedSubjects by viewModel.passedSubjects.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bone)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Star, contentDescription = null, tint = NavyBlue, modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "¡Estás en Vacaciones!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = NavyBlue
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Relájate y disfruta. UniBuddy te espera cuando inicies tu nuevo semestre.",
            fontSize = 16.sp,
            color = SlateGray,
            textAlign = TextAlign.Center
        )
        
        if (passedSubjects.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SlateGray.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text("Historial Académico", style = MaterialTheme.typography.titleMedium, color = NavyBlue, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(passedSubjects.toList()) { subCode ->
                            val subjectName = com.aistudio.unibuddy.qywvsp.data.CurriculumData.industrialEngineering.find { it.code == subCode }?.name ?: subCode
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(subjectName, color = SlateGray, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = { viewModel.startNewSemester() },
            colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Iniciar Nuevo Semestre", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onConfigureRoute,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            border = BorderStroke(1.dp, NavyBlue)
        ) {
            Text("Ir a Configuración", color = NavyBlue)
        }
    }
}

@Composable
fun AdvancedGradesAnalytics(gradedExams: List<Assessment>, allAssessments: List<Assessment>) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, NavyBlue.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = NavyBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Analíticas de Desempeño",
                        fontWeight = FontWeight.Bold,
                        color = NavyBlue,
                        fontSize = 15.sp
                    )
                }
                Text(
                    text = if (expanded) "Ocultar ▲" else "Ver Análisis ▼",
                    fontSize = 12.sp,
                    color = NavyBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                if (gradedExams.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Registra notas en tus exámenes para ver las curvas de progreso.",
                            color = SlateGray,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    Text(
                        text = "Curva de Progreso de Calificaciones",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            
                            val paddingLeft = 30f
                            val paddingRight = 30f
                            val paddingTop = 20f
                            val paddingBottom = 20f
                            
                            val graphWidth = w - paddingLeft - paddingRight
                            val graphHeight = h - paddingTop - paddingBottom
                            
                            val passingY = paddingTop + graphHeight * (1f - 51.0f / 100.0f)
                            drawLine(
                                color = Terracotta.copy(alpha = 0.4f),
                                start = Offset(paddingLeft, passingY),
                                end = Offset(w - paddingRight, passingY),
                                strokeWidth = 3f,
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                            
                            val points = gradedExams.map { it.grade ?: 0.0 }
                            val numPoints = points.size
                            
                            if (numPoints > 1) {
                                val xStep = graphWidth / (numPoints - 1)
                                val coordinates = points.mapIndexed { idx, grade ->
                                    val x = paddingLeft + idx * xStep
                                    val y = paddingTop + graphHeight * (1f - grade.toFloat() / 100.0f)
                                    Offset(x, y)
                                }
                                
                                val fillPath = Path().apply {
                                    moveTo(coordinates.first().x, h - paddingBottom)
                                    coordinates.forEach { lineTo(it.x, it.y) }
                                    lineTo(coordinates.last().x, h - paddingBottom)
                                    close()
                                }
                                drawPath(
                                    path = fillPath,
                                    color = MintGreen.copy(alpha = 0.15f)
                                )
                                
                                val strokePath = Path().apply {
                                    moveTo(coordinates.first().x, coordinates.first().y)
                                    for (i in 1 until coordinates.size) {
                                        lineTo(coordinates[i].x, coordinates[i].y)
                                    }
                                }
                                drawPath(
                                    path = strokePath,
                                    color = DarkGreen,
                                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                                )
                                
                                coordinates.forEachIndexed { idx, offset ->
                                    drawCircle(
                                        color = DarkGreen,
                                        radius = 8f,
                                        center = offset
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 4f,
                                        center = offset
                                    )
                                }
                            } else {
                                val x = paddingLeft + graphWidth / 2f
                                val y = paddingTop + graphHeight * (1f - points[0].toFloat() / 100.0f)
                                drawCircle(
                                    color = DarkGreen,
                                    radius = 8f,
                                    center = Offset(x, y)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 4f,
                                    center = Offset(x, y)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val pendingExams = allAssessments.filter { it.grade == null }
                    val gradedExamsSum = gradedExams.sumOf { (it.grade ?: 0.0) * (it.percentage / 100.0) }
                    val completedPercentage = gradedExams.sumOf { it.percentage }
                    val remainingPercentage = (100.0 - completedPercentage).coerceAtLeast(0.0)
                    
                    if (remainingPercentage > 0.0) {
                        val neededToPass = ((51.0 - gradedExamsSum) / (remainingPercentage / 100.0)).coerceIn(0.0, 100.0)
                        val neededToExcel = ((85.0 - gradedExamsSum) / (remainingPercentage / 100.0)).coerceIn(0.0, 100.0)
                        
                        Text(
                            text = "Estimador Académico (Exámenes Pendientes)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyBlue,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Porcentaje por evaluar:", fontSize = 11.sp, color = SlateGray)
                                    Text("${remainingPercentage.toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Nota necesaria para aprobar (51.0):", fontSize = 11.sp, color = SlateGray)
                                    val neededPassFormatted = String.format(Locale.US, "%.1f", neededToPass)
                                    Text(
                                        text = if (neededToPass <= 0.0) "Ya aprobado" else neededPassFormatted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (neededToPass <= 0.0) DarkGreen else if (neededToPass > 95.0) Terracotta else NavyBlue
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Nota para promedio de Honor (85.0):", fontSize = 11.sp, color = SlateGray)
                                    val neededExcelFormatted = String.format(Locale.US, "%.1f", neededToExcel)
                                    Text(
                                        text = if (neededToExcel <= 0.0) "Ya alcanzado" else if (neededToExcel > 100.0) "Fuera de alcance" else neededExcelFormatted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (neededToExcel <= 0.0) DarkGreen else if (neededToExcel > 100.0) Terracotta else NavyBlue
                                    )
                                }
                            }
                        }
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "¡Semestre completo! Has calificado todos tus exámenes.",
                                    fontSize = 11.sp,
                                    color = DarkGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GradesOverviewScreen(viewModel: UniBuddyViewModel, onSubjectClick: (Int) -> Unit, onConfigureRoute: () -> Unit) {
    val semesterState by viewModel.semesterState.collectAsStateWithLifecycle()
    if (semesterState == "Vacaciones") {
        VacationScreen(viewModel = viewModel, onConfigureRoute = onConfigureRoute)
        return
    }

    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val allAssessments by viewModel.assessments.collectAsStateWithLifecycle(emptyList())
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Mis Notas", style = MaterialTheme.typography.headlineLarge, color = NavyBlue, fontWeight = FontWeight.Bold)
        Text(text = "Un resumen detallado del rendimiento de tus exámenes y notas académicas.", style = MaterialTheme.typography.bodyMedium, color = SlateGray)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar materia o examen...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavyBlue,
                unfocusedBorderColor = SlateGray.copy(alpha = 0.5f),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (subjects.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BuddyMascot(
                        modifier = Modifier.size(100.dp),
                        isHappy = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("¡Lienzo en blanco!", style = MaterialTheme.typography.titleMedium, color = NavyBlue, fontWeight = FontWeight.Bold)
                    Text("No hay materias registradas para calcular notas.", fontSize = 12.sp, color = SlateGray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
                }
            }
        } else {
            val subjectsCount = subjects.size
            val gradedExams = allAssessments.filter { it.grade != null }
            val overallGPA = if (gradedExams.isNotEmpty()) {
                gradedExams.sumOf { it.grade ?: 0.0 } / gradedExams.size
            } else 0.0
            val overallGPAFormatted = String.format(Locale.US, "%.1f", overallGPA)

            // Dynamic Stats Panel Card - Added detailed elements
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = NavyBlue),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Rendimiento Académico General",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Stat 1: GPA
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "Promedio General",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Amber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = overallGPAFormatted,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }

                        // Stat 2: Exams recorded
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "Exámenes Rendidós",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MintGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${gradedExams.size} de ${allAssessments.size}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Stat 3: Registered Subjects
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "Materias",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Bone,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$subjectsCount Activas",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Grid layout (Cuadrícula of 2 columns) - Chunked
            val filteredSubjects = remember(subjects, searchQuery, allAssessments) {
                if (searchQuery.isBlank()) subjects else {
                    subjects.filter { sub ->
                        sub.name.contains(searchQuery, ignoreCase = true) ||
                        allAssessments.filter { it.subjectId == sub.id }.any { it.name.contains(searchQuery, ignoreCase = true) }
                    }
                }
            }
            val chunkedSubjects = remember(filteredSubjects) { filteredSubjects.chunked(2) }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    AdvancedGradesAnalytics(gradedExams = gradedExams, allAssessments = allAssessments)
                }
                
                items(chunkedSubjects, key = { it.first().id }) { rowSubjects ->
                    Row(
                        modifier = Modifier.fillMaxWidth().animateItem(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowSubjects.forEach { sub ->
                            Box(
                                modifier = Modifier.weight(1f)
                            ) {
                                SubjectGradeGridCard(sub, viewModel, onSubjectClick)
                            }
                        }
                        if (rowSubjects.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

// 6. SUBJECT GRADES SCREEN & PREDICTOR SIMULATOR SLIDER
@Composable
fun SubjectGradesScreen(viewModel: UniBuddyViewModel, subjectId: Int, onBack: () -> Unit) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val subject = subjects.find { it.id == subjectId }
    val assessments by viewModel.getAssessmentsForSubject(subjectId).collectAsStateWithLifecycle(emptyList())

    var examName by remember { mutableStateOf("") }
    var examGrade by remember { mutableStateOf("") }
    var examPercent by remember { mutableStateOf("25") }
    var examDay by remember { mutableStateOf("Lu") }
    var examNameError by remember { mutableStateOf(false) }
    var examPercentError by remember { mutableStateOf(false) }

    var simulatedExamGrade by remember { mutableStateOf(50.0f) }

    if (subject == null) {
        onBack()
        return
    }

    val currentWeighted = assessments.sumOf { (it.grade ?: 0.0) * (it.percentage / 100.0) }
    val currentPercentage = assessments.sumOf { it.percentage }
    val remainingPercentage = (100.0 - currentPercentage).coerceAtLeast(0.0)

    val simulatedAverage = currentWeighted + (simulatedExamGrade.toDouble() * (remainingPercentage / 100.0))

    val completedExams = assessments.filter { it.grade != null }
    val pendingExams = assessments.filter { it.grade == null }

    val completedPercentageSum = completedExams.sumOf { it.percentage }
    val remainingPercentageToAcknowledge = (100.0 - completedPercentageSum).coerceAtLeast(0.0)

    val historicAverage = if (completedExams.isNotEmpty()) {
        completedExams.map { it.grade ?: 0.0 }.average()
    } else {
        null
    }

    val historicEfficiencyPercent = if (historicAverage != null) {
        (historicAverage / 100.0) * 100.0
    } else {
        0.0
    }

    // Min grade needed on remaining components to reach exactly 51.0 (passing mark)
    val neededGradeOnRemaining = if (remainingPercentageToAcknowledge > 0.0) {
        ((51.0 - currentWeighted) / (remainingPercentageToAcknowledge / 100.0))
    } else {
        0.0
    }

    val finalGradeSimulationForecast = if (historicAverage != null) {
        currentWeighted + (historicAverage * (remainingPercentageToAcknowledge / 100.0))
    } else {
        51.0
    }

    val probabilityPercentageAndDescription = when {
        currentWeighted >= 51.0 -> Pair(100, "Asegurada (Materia Aprobada)")
        neededGradeOnRemaining > 100.0 -> Pair(0, "Crítica / Inalcanzable (Firma regular requerida)")
        neededGradeOnRemaining <= 0.0 -> Pair(100, "100% (Aprobado)")
        else -> {
            if (historicAverage != null) {
                when {
                    neededGradeOnRemaining <= 10.0 -> Pair(99, "Muy Alta")
                    neededGradeOnRemaining <= historicAverage - 15.0 -> Pair(90, "Alta")
                    neededGradeOnRemaining <= historicAverage + 8.0 -> Pair(70, "Media (Alineada a tus notas)")
                    neededGradeOnRemaining <= historicAverage + 20.0 -> Pair(35, "Baja (Requiere sobreesfuerzo)")
                    else -> Pair(12, "Muy Baja / Difícil")
                }
            } else {
                when {
                    neededGradeOnRemaining <= 40.0 -> Pair(95, "Muy Alta")
                    neededGradeOnRemaining <= 60.0 -> Pair(80, "Alta")
                    neededGradeOnRemaining <= 80.0 -> Pair(45, "Media")
                    neededGradeOnRemaining <= 95.0 -> Pair(20, "Baja")
                    else -> Pair(5, "Muy Baja")
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "Exámenes: ${subject.name}", style = MaterialTheme.typography.titleLarge, color = NavyBlue, fontWeight = FontWeight.Bold)
                    Text(text = "Porcentaje cargado: ${currentPercentage.toInt()}% / 100%", fontSize = 12.sp, color = SlateGray)
                }
            }
        }

        item {
            GradesHistoryWidget(assessments = assessments, currentWeighted = currentWeighted)
        }

        item {
            Text("Evaluaciones Cargadas", style = MaterialTheme.typography.titleMedium, color = NavyBlue, fontWeight = FontWeight.Bold)
        }

        if (assessments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Bone.copy(alpha = 0.5f))
                ) {
                    Text("No hay exámenes cargados todavía.", modifier = Modifier.padding(16.dp), color = SlateGray, textAlign = TextAlign.Center)
                }
            }
        } else {
            items(assessments, key = { it.id }) { ass ->
                Card(
                    modifier = Modifier.fillMaxWidth().animateItem(),
                    colors = CardDefaults.cardColors(containerColor = Bone),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(ass.name, style = MaterialTheme.typography.titleMedium, color = NavyBlue, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Porcentaje: ${ass.percentage.toInt()}%", fontSize = 12.sp, color = SlateGray)
                                if (ass.examDate.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("•", fontSize = 12.sp, color = SlateGray)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Día: ${ass.examDate.uppercase()}", fontSize = 12.sp, color = DarkGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Nota: ${ass.grade ?: "Pendiente"}",
                                style = MaterialTheme.typography.titleMedium,
                                color = NavyBlue,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { viewModel.deleteAssessment(ass.id) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = SlateGray)
                            }
                        }
                    }
                }
            }
        }

        // Add New assessment Form
        item {
            AssessmentFormWidget(
                subject = subject,
                onSave = { name, grade, percent, dateStr ->
                    viewModel.addAssessment(subject.id, name, grade, percent, dateStr)
                }
            )
        }

        // Card de Diagnóstico Predictivo Académico
        item {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.2.dp, Color(0xFFC2E7FF).copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Análisis de Desempeño y Probabilidades",
                        style = MaterialTheme.typography.titleMedium,
                        color = NavyBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Estadísticas predictivas calibradas según tus exámenes previos",
                        fontSize = 11.sp,
                        color = SlateGray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Stats Grid Bento-style (side-by-side components)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Historic Effectiveness
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Bone.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Efectividad Histórica", fontSize = 11.sp, color = SlateGray, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (historicAverage != null) String.format(Locale.US, "%.1f%%", historicEfficiencyPercent) else "Sin datos",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NavyBlue
                                )
                                Text(
                                    text = if (historicAverage != null) String.format(Locale.US, "Nota prom: %.1f", historicAverage) else "Ningún examen cargado",
                                    fontSize = 10.sp,
                                    color = SlateGray
                                )
                            }
                        }

                        // Remaining / Pending tests counter
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Bone.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Evaluaciones Restantes", fontSize = 11.sp, color = SlateGray, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${pendingExams.size + (if (remainingPercentage > 0 && pendingExams.isEmpty()) 1 else 0)} pendientes",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NavyBlue
                                )
                                Text(
                                    text = "Equivale al ${remainingPercentage.toInt()}% de la materia",
                                    fontSize = 10.sp,
                                    color = SlateGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Minimum score required visual indicator
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F9)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Nota Mínima Necesaria", fontSize = 11.sp, color = SlateGray, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = if (currentWeighted >= 51.0) {
                                            "¡Ya Aprobado!"
                                        } else if (neededGradeOnRemaining > 100.0) {
                                            "Inalcanzable (Requiere >100)"
                                        } else {
                                            String.format(Locale.US, "%.1f sobre 100", neededGradeOnRemaining.coerceAtLeast(0.0))
                                        },
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (neededGradeOnRemaining > 75.0) Terracotta else DarkGreen
                                    )
                                    if (currentWeighted < 51.0 && neededGradeOnRemaining <= 100.0 && neededGradeOnRemaining >= 0.0) {
                                        Text(
                                            text = if (neededGradeOnRemaining <= 50.0) "¡Tranquilo! Solo necesitas un ${String.format(Locale.US, "%.1f", neededGradeOnRemaining)} promedio en lo que queda para aprobar." else "¡Tú puedes! Necesitas esforzarte por un ${String.format(Locale.US, "%.1f", neededGradeOnRemaining)} en lo que falta.",
                                            fontSize = 11.sp,
                                            color = SlateGray,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (neededGradeOnRemaining > 75.0) Terracotta.copy(alpha = 0.15f) else MintGreen.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (neededGradeOnRemaining > 75.0) Icons.Default.Warning else Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (neededGradeOnRemaining > 75.0) Terracotta else DarkGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (currentWeighted >= 51.0) {
                                    "Felicitaciones: ya acumulaste los 51.0 puntos ponderados mínimos."
                                } else if (neededGradeOnRemaining > 100.0) {
                                    "Incluso obteniendo 100 en lo restante, tu promedio máximo será de ${String.format(Locale.US, "%.2f", currentWeighted + remainingPercentage)}."
                                } else {
                                    "Debes promediar al menos ${String.format(Locale.US, "%.1f", neededGradeOnRemaining.coerceAtLeast(1.0))} en los próximos exámenes para aprobar la materia."
                                },
                                fontSize = 11.sp,
                                color = SlateGray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Extrapolated prediction based on completed ones
                    if (historicAverage != null && remainingPercentageToAcknowledge > 0.0) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Predicción Extrapolada", fontSize = 11.sp, color = SlateGray, fontWeight = FontWeight.Bold)
                                Text(
                                    text = String.format(Locale.US, "Promedio proyectado: %.2f", finalGradeSimulationForecast),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (finalGradeSimulationForecast >= 51.0) DarkGreen else Terracotta
                                )
                            }
                            Text(
                                text = if (finalGradeSimulationForecast >= 51.0) "Aprobaría" else "No alcanzaría",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (finalGradeSimulationForecast >= 51.0) DarkGreen else Terracotta,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (finalGradeSimulationForecast >= 51.0) MintGreen.copy(alpha = 0.1f) else Terracotta.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Passing probability description scale and bar indicator
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val isWorried = probabilityPercentageAndDescription.first < 40
                                val isHappy = probabilityPercentageAndDescription.first >= 75
                                val pose = if (isWorried) "exam" else if (isHappy) "celebrating" else "working"
                                BuddyMascot(
                                    modifier = Modifier.size(40.dp),
                                    isWorried = isWorried,
                                    isHappy = isHappy,
                                    pose = pose,
                                    mainColor = if (isWorried) Terracotta else if (isHappy) DarkGreen else Amber
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Probabilidad de Aprobación", fontSize = 12.sp, color = NavyBlue, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "${probabilityPercentageAndDescription.first}% (${probabilityPercentageAndDescription.second})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = if (probabilityPercentageAndDescription.first >= 75) DarkGreen else if (probabilityPercentageAndDescription.first >= 40) Color(0xFFB07219) else Terracotta
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Custom drawn probability bar (no emojis, gorgeous custom styling!)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Bone)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(probabilityPercentageAndDescription.first.toFloat() / 100f)
                                    .fillMaxHeight()
                                    .background(
                                        color = if (probabilityPercentageAndDescription.first >= 75) DarkGreen else if (probabilityPercentageAndDescription.first >= 40) Amber else Terracotta,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }

        // Live predictability simulator slider (Simulador de Promedio)
        if (remainingPercentage > 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Bone),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Simulador de Promedio",
                            style = MaterialTheme.typography.titleMedium,
                            color = NavyBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Ajusta la nota estimada para el restante ($remainingPercentage%) y mira cómo cambia tu promedio final:",
                            fontSize = 12.sp,
                            color = SlateGray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Slider(
                            value = simulatedExamGrade,
                            onValueChange = { simulatedExamGrade = it },
                            valueRange = 1.0f..100.0f,
                            steps = 98,
                            colors = SliderDefaults.colors(
                                activeTrackColor = DarkGreen,
                                thumbColor = DarkGreen
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Nota Simulada: ${String.format(Locale.US, "%.1f", simulatedExamGrade)}",
                                fontWeight = FontWeight.Bold,
                                color = NavyBlue,
                                fontSize = 14.sp
                            )
                            Text(
                                "Promedio Final Proyectado: ${String.format(Locale.US, "%.2f", simulatedAverage)}",
                                fontWeight = FontWeight.Bold,
                                color = if (simulatedAverage >= 51.0) DarkGreen else Terracotta,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// 7. CONFIGURACION TAB SCREEN - REDESIGNED AS GRID
@Composable
fun VisualSchedulePicker(
    selectedDays: Set<String>,
    onDaysChanged: (Set<String>) -> Unit,
    selectedBlock: String, // "B1", "B2", "B3", "B4", "B5", "B6", "custom"
    onBlockChanged: (String) -> Unit,
    customTime: String,
    onCustomTimeChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val daysList = listOf("Lu" to "L", "Ma" to "M", "Mi" to "M", "Ju" to "J", "Vi" to "V", "Sá" to "S")
    
    val blocks = listOf(
        Triple("B1", "Bloque 1", "07:00 AM - 08:30 AM"),
        Triple("B2", "Bloque 2", "08:40 AM - 10:10 AM"),
        Triple("B3", "Bloque 3", "10:20 AM - 12:00 PM"),
        Triple("B4", "Bloque 4", "12:45 PM - 02:15 PM"),
        Triple("B5", "Bloque 5", "02:25 PM - 03:55 PM"),
        Triple("B6", "Bloque 6", "04:05 PM - 05:50 PM")
    )

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Days Selection Label
        Text(
            text = "Días de Cursada",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = NavyBlue
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            daysList.forEach { (code, letter) ->
                val isSelected = selectedDays.contains(code)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) DarkGreen else Bone)
                        .clickable {
                            val newSet = if (isSelected) selectedDays - code else selectedDays + code
                            onDaysChanged(newSet)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Bone else SlateGray
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Block category Row (Mañana vs Tarde vs Personalizado)
        Text(
            text = "Horario de Cursada",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = NavyBlue
        )

        var timeTab by remember { mutableStateOf(if (selectedBlock.startsWith("B")) { if (selectedBlock.substring(1).toInt() <= 3) "Mañana" else "Tarde" } else "Personalizado") }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Bone)
                .padding(2.dp)
        ) {
            listOf("Mañana", "Tarde", "Personalizado").forEach { tab ->
                val isTabSelected = timeTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isTabSelected) DarkGreen else Color.Transparent)
                        .clickable { 
                            timeTab = tab 
                            if (tab == "Mañana") {
                                onBlockChanged("B1")
                            } else if (tab == "Tarde") {
                                onBlockChanged("B4")
                            } else {
                                onBlockChanged("custom")
                            }
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTabSelected) Bone else SlateGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (timeTab == "Mañana") {
            // Display blocks 1, 2, 3
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                blocks.take(3).forEach { (id, title, hours) ->
                    val isBlockSelected = selectedBlock == id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isBlockSelected) Color(0xFFE2F0D9) else Bone)
                            .border(1.dp, if (isBlockSelected) DarkGreen else Color.Transparent, RoundedCornerShape(12.dp))
                            .clickable { onBlockChanged(id) }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                            Text(hours, fontSize = 11.sp, color = SlateGray)
                        }
                        RadioButton(
                            selected = isBlockSelected,
                            onClick = { onBlockChanged(id) },
                            colors = RadioButtonDefaults.colors(selectedColor = DarkGreen)
                        )
                    }
                }
            }
        } else if (timeTab == "Tarde") {
            // Display blocks 4, 5, 6
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                blocks.drop(3).forEach { (id, title, hours) ->
                    val isBlockSelected = selectedBlock == id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isBlockSelected) Color(0xFFE2F0D9) else Bone)
                            .border(1.dp, if (isBlockSelected) DarkGreen else Color.Transparent, RoundedCornerShape(12.dp))
                            .clickable { onBlockChanged(id) }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                            Text(hours, fontSize = 11.sp, color = SlateGray)
                        }
                        RadioButton(
                            selected = isBlockSelected,
                            onClick = { onBlockChanged(id) },
                            colors = RadioButtonDefaults.colors(selectedColor = DarkGreen)
                        )
                    }
                }
            }
        } else {
            // Custom time input field
            OutlinedTextField(
                value = customTime,
                onValueChange = onCustomTimeChanged,
                label = { Text("Horario Manual") },
                placeholder = { Text("Ej: 10:00 AM") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DarkGreen, focusedLabelColor = DarkGreen)
            )
        }
    }
}

@Composable
fun GradeTimelineProgressBar(
    assessments: List<Assessment>,
    currentWeighted: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, Color(0xFFC2E7FF).copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Línea de Tiempo de Calificaciones",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = NavyBlue
            )
            Text(
                text = "Puntos acumulados: ${String.format(Locale.US, "%.2f", currentWeighted)} / 100.0 (Necesitas 51.0 para aprobar)",
                fontSize = 11.sp,
                color = SlateGray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val progressColor = if (currentWeighted >= 51.0) MintGreen else Color(0xFF3B82F6)
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val barHeight = 12.dp.toPx()
                    val barY = (h - barHeight) / 2f
                    
                    // 1. Draw base grey bar
                    drawRoundRect(
                        color = Color(0xFFF1F3F4),
                        topLeft = Offset(0f, barY),
                        size = Size(w, barHeight),
                        cornerRadius = CornerRadius(barHeight / 2f)
                    )

                    // 2. Draw progress bar up to completed assessments total weight
                    val progressFraction = (currentWeighted / 100.0).coerceIn(0.0, 1.0).toFloat()
                    if (progressFraction > 0) {
                        drawRoundRect(
                            color = progressColor,
                            topLeft = Offset(0f, barY),
                            size = Size(w * progressFraction, barHeight),
                            cornerRadius = CornerRadius(barHeight / 2f)
                        )
                    }

                    // 3. Draw passing grade threshold (51.0)
                    val passX = w * 0.51f
                    drawLine(
                        color = Terracotta,
                        start = Offset(passX, barY - 4.dp.toPx()),
                        end = Offset(passX, barY + barHeight + 4.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )

                    // 4. Draw markers for each assessment in the timeline
                    var accumulatedPercentage = 0.0
                    assessments.forEach { ass ->
                        accumulatedPercentage += ass.percentage
                        val assEndX = w * (accumulatedPercentage / 100.0).coerceAtLeast(0.0).toFloat()
                        
                        val isCompleted = ass.grade != null
                        drawLine(
                            color = if (isCompleted) NavyBlue else SlateGray.copy(alpha = 0.5f),
                            start = Offset(assEndX, barY - 2.dp.toPx()),
                            end = Offset(assEndX, barY + barHeight + 2.dp.toPx()),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Inicio (0.0)", fontSize = 10.sp, color = SlateGray)
                Text("Meta Mínima (51.0)", fontSize = 10.sp, color = Terracotta, fontWeight = FontWeight.Bold)
                Text("Máximo (100.0)", fontSize = 10.sp, color = SlateGray)
            }

            if (assessments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    assessments.forEach { ass ->
                        val isCompleted = ass.grade != null
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isCompleted) progressColor.copy(alpha = 0.12f)
                                    else Color(0xFFF1F3F4).copy(alpha = 0.7f)
                                )
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = ass.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyBlue,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${ass.percentage.toInt()}%",
                                fontSize = 9.sp,
                                color = SlateGray
                            )
                            Text(
                                text = if (isCompleted) "${ass.grade}" else "Pend.",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted) DarkGreen else SlateGray
                            )
                        }
                    }
                }
            }
        }
    }
}