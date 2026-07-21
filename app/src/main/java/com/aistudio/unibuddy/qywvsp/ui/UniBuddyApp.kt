package com.aistudio.unibuddy.qywvsp.ui

import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*

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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
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
    object Tutorial : Screen("tutorial", "Tutorial", Icons.Default.PlayArrow)
    object Inicio : Screen("inicio", "Inicio", Icons.Rounded.Fort)
    object FocusMode : Screen("focus_mode", "Focus", Icons.Rounded.SportsEsports)
    object Asistencia : Screen("asistencia", "Asistencia", Icons.Rounded.LocalFireDepartment)
    object Notas : Screen("notas", "Notas", Icons.Rounded.EmojiEvents)
    object Tutor : Screen("tutor", "Tutor IA", Icons.Rounded.School)
    object StudyPlan : Screen("study_plan", "Planificador", Icons.Default.AutoAwesome)
    object Config_Tab : Screen("configuracion_tab", "Mochila", Icons.Rounded.Backpack)
    object Pensum : Screen("pensum", "Progreso", Icons.Default.DateRange)
    object Stats : Screen("stats", "Estadísticas", Icons.Rounded.BarChart)
    object Profile : Screen("profile", "Perfil y Personalización", Icons.Rounded.Face)
    object Career : Screen("career", "Mi Carrera", Icons.Rounded.School)
    object Routes : Screen("routes", "Rutas", Icons.Rounded.Explore)
    object System : Screen("system", "Sistema", Icons.Rounded.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniBuddyApp(viewModel: UniBuddyViewModel) {
    val isInitialized by viewModel.isInitialized.collectAsStateWithLifecycle()
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()
    val isTutorialCompleted by viewModel.isTutorialCompleted.collectAsStateWithLifecycle()
    
    if (!isInitialized) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    var currentScreen by remember {
        mutableStateOf<Screen>(
            if (!isOnboardingCompleted) Screen.Onboarding
            else if (!isTutorialCompleted) Screen.Tutorial
            else Screen.Inicio
        )
    }
    
    // Auxiliary state to view subject details (Asistencia)
    var selectedSubjectIdForDetails by remember { mutableStateOf<Int?>(null) }
    // Auxiliary state to view assessment list for a subject (Notas)
    var selectedSubjectIdForGrades by remember { mutableStateOf<Int?>(null) }
    var selectedSubjectIdForTutor by remember { mutableStateOf<Int?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    var showConfetti by remember { mutableStateOf(false) }
    var activeCelebration by remember { mutableStateOf<UniBuddyViewModel.CelebrationType?>(null) }
    val pendingLateness by viewModel.pendingLateness.collectAsStateWithLifecycle()
    var correctedLatenessTime by remember { mutableStateOf("") }
    var isCorrectingLatenessTime by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.celebrationEvent.collect { type ->
            activeCelebration = type
            showConfetti = true
            com.aistudio.unibuddy.qywvsp.ui.AmbientAudioEngine.playCelebrationSound(context)
        }
    }

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

    LaunchedEffect(isOnboardingCompleted, isTutorialCompleted) {
        if (!isOnboardingCompleted) {
            currentScreen = Screen.Onboarding
        } else if (!isTutorialCompleted) {
            currentScreen = Screen.Tutorial
        } else if (currentScreen == Screen.Onboarding || currentScreen == Screen.Tutorial) {
            currentScreen = Screen.Inicio
        }
    }

    val backHandlerEnabled = selectedSubjectIdForDetails != null || 
            selectedSubjectIdForGrades != null || 
            currentScreen == Screen.Pensum || 
            (currentScreen != Screen.Inicio && currentScreen != Screen.Onboarding && currentScreen != Screen.Tutorial)

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
            currentScreen != Screen.Inicio && currentScreen != Screen.Onboarding && currentScreen != Screen.Tutorial -> {
                currentScreen = Screen.Inicio
            }
        }
    }

    var showAddAssessmentDialog by remember { mutableStateOf(false) }
    var showQuickAbsenceDialog by remember { mutableStateOf(false) }
    var showQuickCancelDialog by remember { mutableStateOf(false) }
    var showQuickActionSheet by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            val hideBottomBarScreens = listOf(
                Screen.Onboarding, Screen.Tutorial, Screen.Tutor, Screen.StudyPlan,
                Screen.Profile, Screen.Career, Screen.Routes, Screen.System,
                Screen.Stats, Screen.Pensum
            )
            if (currentScreen !in hideBottomBarScreens) {
                FloatingActionButton(
                    onClick = { showAddAssessmentDialog = true },
                    containerColor = NavyBlue,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.offset(y = 36.dp)
                ) {
                    Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = "Registrar Evaluación")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            val hideBottomBarScreens = listOf(
                Screen.Onboarding, Screen.Tutorial, Screen.Tutor, Screen.StudyPlan,
                Screen.Profile, Screen.Career, Screen.Routes, Screen.System,
                Screen.Stats, Screen.Pensum
            )
            if (currentScreen !in hideBottomBarScreens) {
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
                            }
                        )
                    }
                    Screen.Tutorial -> {
                        com.aistudio.unibuddy.qywvsp.ui.screens.TutorialScreen(
                            onFinish = {
                                viewModel.updateTutorialStatus(true)
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
                                viewModel.requestRouteSettings(true)
                                currentScreen = Screen.Config_Tab
                            },
                            onNavigateToFocus = {
                                currentScreen = Screen.FocusMode
                            },
                            onNavigateToStats = {
                                currentScreen = Screen.Stats
                            },
                            onNavigateToPensum = {
                                currentScreen = Screen.Pensum
                            },
                            onNavigateToStudyPlan = { currentScreen = Screen.StudyPlan },
                            onNavigateToTutor = {
                                currentScreen = Screen.Tutor
                            }
                        )
                    }
                    Screen.Tutor -> {
                        TutorScreen(
                            subjectId = selectedSubjectIdForTutor,
                            viewModel = viewModel,
                            onBack = {
                                currentScreen = if (selectedSubjectIdForTutor != null) Screen.Asistencia else Screen.Inicio
                            }
                        )
                    }
                    Screen.StudyPlan -> {
                        StudyPlanScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = Screen.Inicio }
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
                                        viewModel.requestRouteSettings(true)
                                        currentScreen = Screen.Config_Tab
                                    }
                                )
                            } else {
                                SubjectDetailsScreen(
                                    viewModel = viewModel,
                                    subjectId = subjectId,
                                    onBack = { selectedSubjectIdForDetails = null },
                            onNavigateToTutor = { id ->
                                        selectedSubjectIdForTutor = id
                                        currentScreen = Screen.Tutor
                                    }
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
                                        viewModel.requestRouteSettings(true)
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
                            onNavigateToProfile = { currentScreen = Screen.Profile },
                            onNavigateToCareer = { currentScreen = Screen.Career },
                            onNavigateToRoutes = { currentScreen = Screen.Routes },
                            onNavigateToSystem = { currentScreen = Screen.System },
                            onNavigateToTutorial = { currentScreen = Screen.Tutorial }
                        )
                    }
                    Screen.Profile -> {
                        ProfileScreen(viewModel = viewModel, onBack = { currentScreen = Screen.Config_Tab })
                    }
                    Screen.Career -> {
                        CareerScreen(viewModel = viewModel, onBack = { currentScreen = Screen.Config_Tab })
                    }
                    Screen.Routes -> {
                        RoutesScreen(viewModel = viewModel, onBack = { currentScreen = Screen.Config_Tab })
                    }
                    Screen.System -> {
                        SystemScreen(viewModel = viewModel, onBack = { currentScreen = Screen.Config_Tab })
                    }
                    Screen.Stats -> {
                        SemesterHistoryView(viewModel = viewModel, onBack = { currentScreen = Screen.Inicio })
                    }
                    Screen.Pensum -> {
                        PensumProgressScreen(viewModel = viewModel, onBack = { currentScreen = Screen.Inicio })
                    }
                    Screen.FocusMode -> {
                        FocusModeScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
    

    val showSeasonRecap by viewModel.showSeasonRecap.collectAsStateWithLifecycle()
    if (showSeasonRecap != null) {
        com.aistudio.unibuddy.qywvsp.ui.screens.SeasonRecapOverlay(
            recap = showSeasonRecap!!,
            onDismiss = { viewModel.dismissSeasonRecap() }
        )
    }

    if (showQuickActionSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showQuickActionSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                Text("¿Qué deseas registrar?", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NavyBlue, modifier = Modifier.padding(bottom = 16.dp))
                
                ListItem(
                    headlineContent = { Text("Registrar Evaluación", fontWeight = FontWeight.Bold) },
                    supportingContent = { Text("Guarda una nota nueva para tus materias") },
                    leadingContent = { Icon(Icons.Default.Assignment, contentDescription = null, tint = ProBlue) },
                    modifier = Modifier.clickable {
                        showQuickActionSheet = false
                        showAddAssessmentDialog = true
                    }
                )
                
                ListItem(
                    headlineContent = { Text("Marcar Falta (Ausencia)", fontWeight = FontWeight.Bold) },
                    supportingContent = { Text("Resta una vida/corazón en una de tus materias") },
                    leadingContent = { Icon(Icons.Default.PersonOff, contentDescription = null, tint = Terracotta) },
                    modifier = Modifier.clickable {
                        showQuickActionSheet = false
                        showQuickAbsenceDialog = true
                    }
                )
                
                ListItem(
                    headlineContent = { Text("Registrar Clase Cancelada", fontWeight = FontWeight.Bold) },
                    supportingContent = { Text("La clase no se impartió. No afecta tus vidas/corazones") },
                    leadingContent = { Icon(Icons.Default.Cancel, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.clickable {
                        showQuickActionSheet = false
                        showQuickCancelDialog = true
                    }
                )
            }
        }
    }


    if (showQuickAbsenceDialog) {
        val activeSubjects by viewModel.subjects.collectAsState(initial = emptyList())
        com.aistudio.unibuddy.qywvsp.ui.screens.QuickAbsenceDialog(
            viewModel = viewModel,
            onDismiss = { showQuickAbsenceDialog = false },
            activeSubjects = activeSubjects
        )
    }

    if (showQuickCancelDialog) {
        val activeSubjects by viewModel.subjects.collectAsState(initial = emptyList())
        com.aistudio.unibuddy.qywvsp.ui.screens.QuickCancelDialog(
            viewModel = viewModel,
            onDismiss = { showQuickCancelDialog = false },
            activeSubjects = activeSubjects
        )
    }

    if (showAddAssessmentDialog) {
        val activeSubjects by viewModel.subjects.collectAsState(initial = emptyList())
        com.aistudio.unibuddy.qywvsp.ui.screens.QuickAssessmentDialog(
            viewModel = viewModel,
            onDismiss = { showAddAssessmentDialog = false },
            activeSubjects = activeSubjects
        )
    }

    // Confetti overlay
    if (showConfetti) {
        ConfettiOverlay(onFinished = { showConfetti = false })
    }

    // Level-up or Badge-unlock celebration dialog
    if (activeCelebration != null) {
        AlertDialog(
            onDismissRequest = { activeCelebration = null },
            title = {
                Text(
                    text = if (activeCelebration == UniBuddyViewModel.CelebrationType.LEVEL_UP) "¡Subiste de Nivel!" else "¡Nueva Insignia Desbloqueada!",
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue
                )
            },
            text = {
                Text(
                    text = if (activeCelebration == UniBuddyViewModel.CelebrationType.LEVEL_UP) {
                        "Tu mascota de estudio ha acumulado suficiente experiencia para subir de nivel. ¡Sigue así, asistiendo y completando tus pendientes!"
                    } else {
                        "Felicidades por tu dedicación y esfuerzo constante. Se ha desbloqueado una nueva insignia para tu perfil."
                    },
                    color = NavyBlue
                )
            },
            confirmButton = {
                Button(
                    onClick = { activeCelebration = null },
                    colors = ButtonDefaults.buttonColors(containerColor = ProBlue)
                ) {
                    Text("¡Excelente!")
                }
            }
        )
    }

    // Pending lateness prompt dialog
    if (pendingLateness != null) {
        AlertDialog(
            onDismissRequest = { /* forced action, no dismiss outside buttons */ },
            title = { Text("Llegada Tarde Detectada", fontWeight = FontWeight.Bold, color = NavyBlue) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Detectamos tu llegada al campus para la materia '${pendingLateness!!.subjectName}'. El horario de inicio era a las ${pendingLateness!!.startTime}, y llegaste a las ${pendingLateness!!.arrivalTime}.",
                        color = NavyBlue
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = isCorrectingLatenessTime,
                            onCheckedChange = { 
                                isCorrectingLatenessTime = it 
                                if (it) {
                                    correctedLatenessTime = pendingLateness!!.arrivalTime
                                }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = ProBlue)
                        )
                        Text("Corregir hora manualmente", fontSize = 13.sp, color = NavyBlue)
                    }

                    if (isCorrectingLatenessTime) {
                        OutlinedTextField(
                            value = correctedLatenessTime,
                            onValueChange = { correctedLatenessTime = it },
                            placeholder = { Text("Ej. 08:15") },
                            label = { Text("Hora corregida") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val corrected = if (isCorrectingLatenessTime && correctedLatenessTime.isNotBlank()) correctedLatenessTime else null
                        viewModel.confirmLateness(pendingLateness!!, corrected)
                        isCorrectingLatenessTime = false
                        correctedLatenessTime = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ProBlue)
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        viewModel.discardLateness() 
                        isCorrectingLatenessTime = false
                        correctedLatenessTime = ""
                    }
                ) {
                    Text("Descartar", color = Color.Gray)
                }
            }
        )
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
    
    val maxAbs = sub.totalClasses - kotlin.math.ceil(sub.totalClasses * (sub.requiredAttendancePercent / 100.0)).toInt()
    val remaining = maxAbs - subAbsCount
    
    val fractionUsed = if (maxAbs > 0) subAbsCount.toFloat() / maxAbs.toFloat() else 0f
    val percentageUsed = (fractionUsed * 100).toInt()
    
    val isWarning = remaining <= 2 && remaining > 0
    val isCritical = remaining <= 0
    
    val statusColor = if (isCritical) com.aistudio.unibuddy.qywvsp.ui.theme.StatusRed else if (isWarning) com.aistudio.unibuddy.qywvsp.ui.theme.StatusAmber else com.aistudio.unibuddy.qywvsp.ui.theme.StatusGreen

    val hapticFeedback = androidx.compose.ui.platform.LocalHapticFeedback.current
    val todayCode = when (java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)) {
        java.util.Calendar.MONDAY -> "Lu"
        java.util.Calendar.TUESDAY -> "Ma"
        java.util.Calendar.WEDNESDAY -> "Mi"
        java.util.Calendar.THURSDAY -> "Ju"
        java.util.Calendar.FRIDAY -> "Vi"
        java.util.Calendar.SATURDAY -> "Sá"
        else -> "Do"
    }
    val defaultSession = sub.sessions.find { it.day.trim().equals(todayCode, ignoreCase = true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                onSubjectClick(sub.id)
            }
            .animateContentSize()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = com.aistudio.unibuddy.qywvsp.ui.theme.BackgroundBone),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                Text(
                    text = sub.name,
                    fontSize = 16.sp,
                    color = com.aistudio.unibuddy.qywvsp.ui.theme.NavyBlue,
                    fontWeight = FontWeight.Bold
                )
                if (defaultSession != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hoy • ${defaultSession.time} | ${defaultSession.room}",
                        fontSize = 12.sp,
                        color = com.aistudio.unibuddy.qywvsp.ui.theme.SlateGray,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                SubjectLivesIndicator(subAbsCount = subAbsCount, maxAbs = maxAbs)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$subAbsCount/$maxAbs",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = statusColor
                )
                Text(
                    text = "faltas",
                    fontSize = 9.sp,
                    color = com.aistudio.unibuddy.qywvsp.ui.theme.SlateGray
                )
            }
        }
    }
}

@Composable
fun WeeklyScheduleView(subjects: List<Subject>, viewModel: UniBuddyViewModel) {
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
    var showSchedulePlannerDialog by remember { mutableStateOf(false) }
    
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
                                
                                val freqLabel = when (session.safeFrequency) {
                                    "Semanas Pares" -> "Semanas Pares (2, 4, 6...)"
                                    "Semanas Impares" -> "Semanas Impares (1, 3, 5...)"
                                    else -> ""
                                }
                                if (freqLabel.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = freqLabel,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor.copy(alpha = 0.9f)
                                    )
                                }
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = com.aistudio.unibuddy.qywvsp.ui.formatTimeRange(androidx.compose.ui.platform.LocalContext.current, session.time),
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
        
        Button(
            onClick = { showSchedulePlannerDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Planificador de Horarios",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Editar Horario Completo",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
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
                text = "Fondo de Pantalla (Exportar PNG)",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }

    if (showSchedulePlannerDialog) {
        val tempSubjects = remember(subjects) {
            subjects.map { it.copy(sessions = it.sessions.toList()) }.toMutableStateList()
        }
        var selectedSubjectId by remember { mutableStateOf<Int?>(tempSubjects.firstOrNull()?.id) }
        var showMorningGrid by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showSchedulePlannerDialog = false },
            title = {
                Column {
                    Text("Planificador de Horarios", color = NavyBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Toca las materias arriba, luego asigna bloques en la cuadrícula.", color = SlateGray, fontSize = 11.sp)
                }
            },
            text = {
                Box(modifier = Modifier.heightIn(max = 450.dp)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text("Selecciona materia para asignar:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                item {
                                    val isSelected = selectedSubjectId == null
                                    Surface(
                                        modifier = Modifier.clickable { selectedSubjectId = null },
                                        color = if (isSelected) Terracotta.copy(alpha = 0.15f) else Color(0xFFF1F5F9),
                                        border = BorderStroke(1.dp, if (isSelected) Terracotta else Color.LightGray),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = "Borrar Celda",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Terracotta else SlateGray,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                                
                                items(tempSubjects) { sub ->
                                    val isSelected = selectedSubjectId == sub.id
                                    val (_, accentCol) = getSubjectColorPalette(sub.colorHex)
                                    val bgCol = if (isSelected) accentCol.copy(alpha = 0.15f) else Color(0xFFF1F5F9)
                                    val borderCol = if (isSelected) accentCol else Color.LightGray
                                    
                                    Surface(
                                        modifier = Modifier.clickable { selectedSubjectId = sub.id },
                                        color = bgCol,
                                        border = BorderStroke(1.dp, borderCol),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = sub.name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) accentCol else NavyBlue,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (showMorningGrid) "Horario Mañana" else "Horario Tarde",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyBlue
                                )
                                TextButton(
                                    onClick = { showMorningGrid = !showMorningGrid },
                                    colors = ButtonDefaults.textButtonColors(contentColor = ProBlue)
                                ) {
                                    Text(if (showMorningGrid) "Ver Tarde" else "Ver Mañana", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        item {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Box(modifier = Modifier.weight(0.8f))
                                WEEK_DAYS.forEach { day ->
                                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                        Text(day, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyBlue)
                                    }
                                }
                            }
                        }

                        val visibleBlocks = if (showMorningGrid) TIME_BLOCKS.take(3) else TIME_BLOCKS.drop(3)
                        items(visibleBlocks) { block ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(0.8f), contentAlignment = Alignment.Center) {
                                    Text(block.label, fontSize = 9.sp, textAlign = TextAlign.Center, lineHeight = 11.sp, color = SlateGray)
                                }

                                WEEK_DAYS.forEach { day ->
                                    val occupyingSubIdx = tempSubjects.indexOfFirst { sub ->
                                        sub.sessions.any { it.day == day && getBlockIdForSession(it) == block.id }
                                    }
                                    val occupyingSub = if (occupyingSubIdx != -1) tempSubjects[occupyingSubIdx] else null
                                    val isOccupiedByActive = occupyingSub != null && occupyingSub.id == selectedSubjectId

                                    val (bgColor, borderCol) = when {
                                        occupyingSub != null -> {
                                            val (bg, acc) = getSubjectColorPalette(occupyingSub.colorHex)
                                            bg to acc
                                        }
                                        else -> Color(0xFFF8FAFC) to Color(0xFFE2E8F0)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(2.dp)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(bgColor)
                                            .border(1.dp, borderCol, RoundedCornerShape(8.dp))
                                            .clickable {
                                                if (selectedSubjectId == null) {
                                                    if (occupyingSubIdx != -1) {
                                                        val sEdit = tempSubjects[occupyingSubIdx]
                                                        tempSubjects[occupyingSubIdx] = sEdit.copy(sessions = sEdit.sessions.filter { !(it.day == day && getBlockIdForSession(it) == block.id) })
                                                    }
                                                } else {
                                                    if (isOccupiedByActive) {
                                                        val updated = occupyingSub!!.sessions.filter { !(it.day == day && getBlockIdForSession(it) == block.id) }
                                                        tempSubjects[occupyingSubIdx] = occupyingSub.copy(sessions = updated)
                                                    } else {
                                                        if (occupyingSubIdx != -1) {
                                                            val oldSub = tempSubjects[occupyingSubIdx]
                                                            tempSubjects[occupyingSubIdx] = oldSub.copy(sessions = oldSub.sessions.filter { !(it.day == day && getBlockIdForSession(it) == block.id) })
                                                        }
                                                        val activeIdx = tempSubjects.indexOfFirst { it.id == selectedSubjectId }
                                                        if (activeIdx != -1) {
                                                            val activeSub = tempSubjects[activeIdx]
                                                            val updated = activeSub.sessions.toMutableList().apply {
                                                                add(ClassSessionDetails(day, block.id, "Aula por definir", "Todas las semanas", emptyList()))
                                                            }
                                                            tempSubjects[activeIdx] = activeSub.copy(sessions = updated)
                                                        }
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (occupyingSub != null) {
                                            Text(getSubjectInitials(occupyingSub.name), fontSize = 9.sp, color = borderCol, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                        }
                                    }
                                }
                            }
                        }

                        selectedSubjectId?.let { activeId ->
                            val activeIdx = tempSubjects.indexOfFirst { it.id == activeId }
                            if (activeIdx != -1) {
                                val activeSub = tempSubjects[activeIdx]
                                if (activeSub.sessions.isNotEmpty()) {
                                    item {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text("Personalizar sesiones de ${activeSub.name}:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                                    }
                                    
                                    items(activeSub.sessions.size) { sessionIdx ->
                                        val session = activeSub.sessions[sessionIdx]
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                val blockName = TIME_BLOCKS.find { it.id == session.time }?.label?.replace("\n", " ") ?: session.time
                                                Text("${session.day} - $blockName", fontWeight = FontWeight.Bold, color = NavyBlue, fontSize = 12.sp)
                                                Spacer(modifier = Modifier.height(6.dp))
                                                
                                                OutlinedTextField(
                                                    value = session.room,
                                                    onValueChange = { newRoom ->
                                                        val updatedList = activeSub.sessions.toMutableList()
                                                        updatedList[sessionIdx] = session.copy(room = newRoom)
                                                        tempSubjects[activeIdx] = activeSub.copy(sessions = updatedList)
                                                    },
                                                    label = { Text("Aula/Salón", fontSize = 10.sp) },
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DarkGreen, focusedLabelColor = DarkGreen)
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                
                                                Text("Frecuencia:", fontSize = 10.sp, color = SlateGray, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    listOf("Todas las semanas", "Semanas Pares", "Semanas Impares").forEach { freq ->
                                                        val isSelected = session.safeFrequency == freq
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .height(28.dp)
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(if (isSelected) NavyBlue else Color.White)
                                                                .border(1.dp, if (isSelected) NavyBlue else Color.LightGray, RoundedCornerShape(6.dp))
                                                                .clickable {
                                                                    val updatedList = activeSub.sessions.toMutableList()
                                                                    updatedList[sessionIdx] = session.copy(frequency = freq)
                                                                    tempSubjects[activeIdx] = activeSub.copy(sessions = updatedList)
                                                                },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = when (freq) {
                                                                    "Semanas Pares" -> "Pares"
                                                                    "Semanas Impares" -> "Impares"
                                                                    else -> "Todas"
                                                                },
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isSelected) Color.White else NavyBlue
                                                            )
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        tempSubjects.forEach { updatedSub ->
                            val computedSchedule = updatedSub.sessions.distinctBy { it.day }.joinToString(", ") { it.day }
                            val computedClasses = updatedSub.sessions.size * 14
                            viewModel.updateSubject(
                                updatedSub.copy(
                                    schedule = if (computedSchedule.isEmpty()) "Sin horario" else computedSchedule,
                                    totalClasses = computedClasses
                                )
                            )
                        }
                        showSchedulePlannerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                ) {
                    Text("Guardar Todo", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSchedulePlannerDialog = false }) {
                    Text("Cancelar", color = SlateGray)
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
fun AttendanceLockedDialog(
    subject: Subject,
    currentLocation: String,
    isLocationAvailable: Boolean,
    onDismiss: () -> Unit
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
                val isLocationValid = isLocationAvailable && currentLocation.trim().equals("En la universidad", ignoreCase = true)
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
                            text = if (isLocationValid) "Estás en el recinto universitario" else if (!isLocationAvailable) "GPS desactivado o sin permiso." else "Ubicación: $currentLocation (Debes estar a <=500m)",
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
                        val range = parseTimeRange(matchingSession.time)
                        if (range != null) {
                            val startHour = range.first.first
                            val startMin = range.first.second
                            val endHour = range.second.first
                            val endMin = range.second.second
                            val startTotalMin = startHour * 60 + startMin
                            val endTotalMin = endHour * 60 + endMin
                            currentTotalMin in (startTotalMin - 30)..(endTotalMin + 30)
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
            TextButton(onClick = onDismiss) {
                Text("Entendido", color = NavyBlue)
            }
        },
        dismissButton = null,
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

    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val absences by viewModel.absences.collectAsStateWithLifecycle()
    val attendanceLogs by viewModel.attendanceLogs.collectAsStateWithLifecycle()
    val weeklyStreak by viewModel.weeklyStreak.collectAsStateWithLifecycle()
    var viewMode by remember { mutableStateOf("materias") } // "materias" or "horario"
    var gpsConfirmSubject by remember { mutableStateOf<Subject?>(null) }
    val currentLocationName by viewModel.currentLocationName.collectAsStateWithLifecycle()
    val isLocationAvailable by viewModel.isLocationAvailable.collectAsStateWithLifecycle()
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

        // 2. Today's Classes List (Relocated TodayClassesListWidget)
        item {
            val currentDayCode = when (java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)) {
                java.util.Calendar.MONDAY -> "Lu"
                java.util.Calendar.TUESDAY -> "Ma"
                java.util.Calendar.WEDNESDAY -> "Mi"
                java.util.Calendar.THURSDAY -> "Ju"
                java.util.Calendar.FRIDAY -> "Vi"
                java.util.Calendar.SATURDAY -> "Sá"
                else -> "Do"
            }
            val isEvenWeek by viewModel.isEvenWeek.collectAsStateWithLifecycle()
            val todayClasses = remember(subjects, currentDayCode, isEvenWeek) {
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
            
            Card(
                modifier = Modifier.fillMaxWidth().testTag("today_classes_widget"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.5.dp, NavyBlue.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "CLASES PARA HOY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyBlue,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (todayClasses.isEmpty()) {
                        Text(
                            text = "No tienes clases programadas para hoy.",
                            fontSize = 11.sp,
                            color = SlateGray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            todayClasses.forEach { (sub, session, _) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(Color(android.graphics.Color.parseColor(sub.colorHex)))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = sub.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = NavyBlue,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            val classTimeFormatted = com.aistudio.unibuddy.qywvsp.ui.formatTimeRange(androidx.compose.ui.platform.LocalContext.current, session.time)
                                            Text(
                                                text = "$classTimeFormatted • Salón ${session.room}",
                                                fontSize = 11.sp,
                                                color = SlateGray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
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
                WeeklyScheduleView(subjects = subjects, viewModel = viewModel)
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
                                        val university by viewModel.userUniversity.collectAsStateWithLifecycle()
                                        val career by viewModel.career.collectAsStateWithLifecycle()
                                        val suggestions = remember(university, career) {
                                            com.aistudio.unibuddy.qywvsp.data.CurriculumData.getSubjectsFor(university.ifEmpty { "UNI" }, career.ifEmpty { "Ing. Industrial" })
                                        }
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
                                                sessions = sessionsList.toList(),
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
                                items(subjects, key = { it.id }) { sub ->
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
                                                val range = parseTimeRange(matchingSession.time)
                                                if (range != null) {
                                                    val startHour = range.first.first
                                                    val startMin = range.first.second
                                                    val endHour = range.second.first
                                                    val endMin = range.second.second
                                                    val startTotalMin = startHour * 60 + startMin
                                                    val endTotalMin = endHour * 60 + endMin
                                                    currentTotalMin in (startTotalMin - 30)..(endTotalMin + 30)
                                                } else true
                                            } catch (e: Exception) { true }
                                        } else false

                                        val isAtUni = isLocationAvailable && currentLocationName.trim().equals("En la universidad", ignoreCase = true)

                                        if (isScheduledToday && isTimeValid) {
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
            isLocationAvailable = isLocationAvailable,
            onDismiss = { blockedCheckInSubject = null }
        )
    }
}

data class IntegratedLogItem(val id: Int, val date: String, val isPresent: Boolean, val isCancelled: Boolean = false, val isLegacy: Boolean)

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

    val isImpossible = gradeNeeded > 100.0
    val isApproved = currentWeighted >= targetApprove
    val isWarning = missingAmount > 0 && remainingPercentage < 30.0 && gradeNeeded > 70.0

    val statusColor = if (isImpossible || (isWarning && currentPercentage > 50.0)) com.aistudio.unibuddy.qywvsp.ui.theme.StatusRed else if (isWarning) com.aistudio.unibuddy.qywvsp.ui.theme.StatusAmber else if (isApproved) com.aistudio.unibuddy.qywvsp.ui.theme.StatusGreen else com.aistudio.unibuddy.qywvsp.ui.theme.StatusGray

    val hapticFeedback = androidx.compose.ui.platform.LocalHapticFeedback.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                onClick(sub.id)
            }
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = com.aistudio.unibuddy.qywvsp.ui.theme.BackgroundBone),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = sub.name,
                fontSize = 16.sp,
                color = com.aistudio.unibuddy.qywvsp.ui.theme.NavyBlue,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { (currentWeighted / 100.0).toFloat().coerceIn(0f, 1f) },
                color = statusColor,
                trackColor = Color.LightGray.copy(alpha = 0.3f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Nota: ${String.format(java.util.Locale.US, "%.1f", currentWeighted)} / 100",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = com.aistudio.unibuddy.qywvsp.ui.theme.NavyBlue
                )
                Text(
                    text = "${currentPercentage.toInt()}% evaluado",
                    fontSize = 12.sp,
                    color = com.aistudio.unibuddy.qywvsp.ui.theme.SlateGray
                )
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

    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val allAssessments by viewModel.assessments.collectAsStateWithLifecycle(emptyList())
    val tasks by viewModel.tasks.collectAsStateWithLifecycle(emptyList())
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
            var showAdvancedAnalytics by remember { mutableStateOf(false) }
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
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cada materia incluye un plan estimado de 12 evaluaciones (exámenes y tareas) distribuidos en los Cortes 1 y 2 para simular tus notas del semestre.",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { showAdvancedAnalytics = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ProBlue),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ver Análisis Avanzado", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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

            if (showAdvancedAnalytics) {
                AdvancedGradesAnalytics(
                    gradedExams = gradedExams,
                    allAssessments = allAssessments,
                    onDismiss = { showAdvancedAnalytics = false }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    UpcomingAssessmentsWidget(
                        assessments = allAssessments,
                        subjects = subjects,
                        onNavigateToSubject = onSubjectClick
                    )
                }

                item {
                    UpcomingTasksWidget(
                        tasks = tasks,
                        subjects = subjects,
                        onToggleTask = { viewModel.toggleTask(it) },
                        onNavigateToSubject = onSubjectClick
                    )
                }
                
                item {
                    PredictiveGradeCalculatorWidget(subjects = subjects, viewModel = viewModel)
                }
                
                items(filteredSubjects, key = { it.id }) { sub ->
                    SubjectGradeGridCard(sub, viewModel, onSubjectClick)
                }
            }
        }
    }
}

@Composable
fun UpcomingTasksWidget(
    tasks: List<com.aistudio.unibuddy.qywvsp.data.Task>,
    subjects: List<com.aistudio.unibuddy.qywvsp.data.Subject>,
    onToggleTask: (com.aistudio.unibuddy.qywvsp.data.Task) -> Unit,
    onNavigateToSubject: (Int) -> Unit
) {
    val pendingTasks = remember(tasks) {
        tasks.filter { !it.isCompleted }
    }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("upcoming_tasks_widget"),
        colors = CardDefaults.cardColors(containerColor = com.aistudio.unibuddy.qywvsp.ui.theme.BackgroundBone),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(0.5.dp, NavyBlue.copy(alpha = 0.15f))
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
    val pendingAssessments by remember {
        derivedStateOf {
            val format = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            assessments.filter { it.grade == null && it.examDate.isNotBlank() }
                .sortedBy { 
                    try { format.parse(it.examDate)?.time ?: Long.MAX_VALUE } 
                    catch (e: Exception) { Long.MAX_VALUE }
                }
                .take(5)
        }
    }

    if (pendingAssessments.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth().testTag("upcoming_assessments_widget"),
        colors = CardDefaults.cardColors(containerColor = ProBlue.copy(alpha=0.05f)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(0.5.dp, ProBlue.copy(alpha = 0.3f))
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

// 6. SUBJECT GRADES SCREEN & PREDICTOR SIMULATOR SLIDER
@Composable
fun SubjectGradesScreen(viewModel: UniBuddyViewModel, subjectId: Int, onBack: () -> Unit) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val subject = subjects.find { it.id == subjectId }
    val assessments by viewModel.getAssessmentsForSubject(subjectId).collectAsStateWithLifecycle(emptyList())
    val university by viewModel.userUniversity.collectAsStateWithLifecycle()
    val passingGrade = if (university == "UAM" || university == "UCA" || university == "Keiser") 70.0 else 60.0

    var examName by remember { mutableStateOf("") }
    var examGrade by remember { mutableStateOf("") }
    var examPercent by remember { mutableStateOf("25") }
    var examDay by remember { mutableStateOf("Lu") }
    var examNameError by remember { mutableStateOf(false) }
    var examPercentError by remember { mutableStateOf(false) }

    var simulatedExamGrade by remember { mutableStateOf(50.0f) }

    var selectedStage by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(1) }
    var forceUnlockC2 by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    val currentWeek by viewModel.currentWeekOfSemester.collectAsStateWithLifecycle()
    var assessmentToDelete by remember { mutableStateOf<com.aistudio.unibuddy.qywvsp.data.Assessment?>(null) }

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

    assessmentToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { assessmentToDelete = null },
            title = { Text("Eliminar Evaluación", fontWeight = FontWeight.Bold, color = Terracotta) },
            text = { Text("¿Estás seguro de que deseas eliminar la evaluación '${target.name}'? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAssessment(target.id)
                        assessmentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
                ) {
                    Text("Eliminar", color = Bone)
                }
            },
            dismissButton = {
                TextButton(onClick = { assessmentToDelete = null }) {
                    Text("Cancelar", color = SlateGray)
                }
            }
        )
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

    // Min grade needed on remaining components to reach exactly passingMark
    val neededGradeOnRemaining = if (remainingPercentageToAcknowledge > 0.0) {
        ((passingGrade - currentWeighted) / (remainingPercentageToAcknowledge / 100.0))
    } else {
        0.0
    }

    val finalGradeSimulationForecast = if (historicAverage != null) {
        currentWeighted + (historicAverage * (remainingPercentageToAcknowledge / 100.0))
    } else {
        passingGrade
    }

    val probabilityPercentageAndDescription = when {
        currentWeighted >= passingGrade -> Pair(100, "Asegurada (Materia Aprobada)")
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

    val c1Assessments = assessments.filter { it.name.contains("C1", ignoreCase = true) || it.name.contains("U1", ignoreCase = true) }
    val c2Assessments = assessments.filter { it.name.contains("C2", ignoreCase = true) || it.name.contains("U2", ignoreCase = true) }
    val otherAssessments = assessments.filter { 
        !it.name.contains("C1", ignoreCase = true) && !it.name.contains("U1", ignoreCase = true) &&
        !it.name.contains("C2", ignoreCase = true) && !it.name.contains("U2", ignoreCase = true) 
    }
    
    val isC2Locked = !forceUnlockC2 && (currentWeek in 1..7) && c1Assessments.any { it.grade == null }
    val displayAssessments = if (selectedStage == 1) c1Assessments + otherAssessments else c2Assessments

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
                    Text(text = "Puntos Evaluados: ${currentPercentage.toInt()} pts / 100 pts", fontSize = 12.sp, color = SlateGray)
                }
            }
        }

        item {
            GradesHistoryWidget(assessments = assessments, currentWeighted = currentWeighted, subjectName = subject.name)
        }

        // Progress Map Node layout
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MAPA DE PROGRESO DEL SEMESTRE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyBlue,
                        letterSpacing = 1.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Node 1: Mitad 1
                        val isStage1Active = selectedStage == 1
                        val isStage1Done = c1Assessments.isNotEmpty() && c1Assessments.all { it.grade != null }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { selectedStage = 1 }
                                .weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isStage1Done) MintGreen 
                                        else if (isStage1Active) NavyBlue 
                                        else Color(0xFFE2E8F0)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isStage1Done) Icons.Default.Check else Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (isStage1Active || isStage1Done) Color.White else SlateGray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Mitad 1 (Corte 1)",
                                fontSize = 12.sp,
                                fontWeight = if (isStage1Active) FontWeight.Bold else FontWeight.Medium,
                                color = if (isStage1Active) NavyBlue else SlateGray
                            )
                            Text(
                                text = "Puntos: ${c1Assessments.sumOf { it.percentage }.toInt()}%",
                                fontSize = 10.sp,
                                color = SlateGray
                            )
                        }

                        // Connecting Line
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .weight(0.5f)
                                .background(
                                    if (isStage1Done) MintGreen else Color(0xFFE2E8F0)
                                )
                        )

                        // Node 2: Mitad 2
                        val isStage2Active = selectedStage == 2
                        val isStage2Done = c2Assessments.isNotEmpty() && c2Assessments.all { it.grade != null }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { 
                                    selectedStage = 2 
                                }
                                .weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isStage2Done) MintGreen 
                                        else if (isStage2Active) NavyBlue 
                                        else if (isC2Locked) Color(0xFFF1F5F9)
                                        else Color(0xFFE2E8F0)
                                    )
                                    .border(
                                        width = if (isC2Locked) 1.5.dp else 0.dp,
                                        color = if (isC2Locked) Color(0xFFCBD5E1) else Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isC2Locked) Icons.Default.Lock else if (isStage2Done) Icons.Default.Check else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = if (isC2Locked) SlateGray else if (isStage2Active || isStage2Done) Color.White else SlateGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Mitad 2 (Corte 2)",
                                fontSize = 12.sp,
                                fontWeight = if (isStage2Active) FontWeight.Bold else FontWeight.Medium,
                                color = if (isStage2Active) NavyBlue else SlateGray
                            )
                            Text(
                                text = if (isC2Locked) "Bloqueado" else "Puntos: ${c2Assessments.sumOf { it.percentage }.toInt()}%",
                                fontSize = 10.sp,
                                color = SlateGray
                            )
                        }
                    }
                }
            }
        }

        if (selectedStage == 2 && isC2Locked) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = SlateGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Segunda Mitad Bloqueada",
                            style = MaterialTheme.typography.titleMedium,
                            color = NavyBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Completa tus calificaciones de la primera mitad (Corte 1) o espera a que el semestre avance a la semana 8 para poder ver e ingresar notas de la segunda mitad.",
                            fontSize = 12.sp,
                            color = SlateGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { forceUnlockC2 = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Desbloquear Manualmente", color = Bone)
                        }
                    }
                }
            }
        } else {
            item {
                Text("Evaluaciones Cargadas", style = MaterialTheme.typography.titleMedium, color = NavyBlue, fontWeight = FontWeight.Bold)
            }

            if (displayAssessments.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Bone.copy(alpha = 0.5f))
                    ) {
                        Text("No hay exámenes cargados todavía para esta etapa.", modifier = Modifier.padding(16.dp), color = SlateGray, textAlign = TextAlign.Center)
                    }
                }
            } else {
                items(displayAssessments, key = { it.id }) { ass ->
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
                                    Text("Valor: ${ass.percentage.toInt()} pts", fontSize = 12.sp, color = SlateGray)
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
                                IconButton(onClick = { assessmentToDelete = ass }) {
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
                    totalCurrentPoints = assessments.sumOf { it.percentage },
                    onSave = { name, grade, percent, dateStr ->
                        val prefix = if (selectedStage == 1) "C1: " else "C2: "
                        val finalName = if (name.startsWith("Examen:") || name.startsWith("Trabajo:")) {
                            name.replaceFirst("Examen: ", "Examen: $prefix").replaceFirst("Trabajo: ", "Trabajo: $prefix")
                        } else {
                            "$prefix$name"
                        }
                        viewModel.addAssessment(subject.id, finalName, grade, percent, dateStr)
                    }
                )
            }
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
                            text = "Ajusta la nota estimada para lo restante ($remainingPercentage pts) y mira cómo cambia tu promedio final:",
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

@Composable
fun PredictiveGradeCalculatorWidget(subjects: List<Subject>, viewModel: UniBuddyViewModel) {
    var calcSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id) }
    val selectedSubject = subjects.find { it.id == calcSubjectId } ?: subjects.firstOrNull()

    val assessments by viewModel.assessments.collectAsStateWithLifecycle()
    val subjectAssessments = assessments.filter { it.subjectId == calcSubjectId }
    val accumulated = subjectAssessments.filter { it.grade != null }.sumOf { (it.grade!! / 100.0) * it.percentage }
    val pendingPercentage = 100.0 - subjectAssessments.filter { it.grade != null }.sumOf { it.percentage }
    
    val passingGrade = 60.0 // Default for Nicaragua
    val isAlreadyPassed = accumulated >= passingGrade

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Calculate,
                    contentDescription = null,
                    tint = NavyBlue,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "Calculadora Predictiva",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavyBlue
                    )
                    Text(
                        text = "Calcula cuánto necesitas en lo que falta para aprobar.",
                        fontSize = 11.sp,
                        color = SlateGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subject selector
            Text("Materia para simular:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(subjects) { sub ->
                        val isSelected = sub.id == calcSubjectId
                        val (_, accentCol) = getSubjectColorPalette(sub.colorHex)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) accentCol.copy(alpha = 0.15f) else Color.White)
                                .border(1.dp, if (isSelected) accentCol else Color.LightGray, RoundedCornerShape(8.dp))
                                .clickable { calcSubjectId = sub.id }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(sub.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) accentCol else NavyBlue)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display current stats instead of asking for them
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Acumulado Actual", fontSize = 10.sp, color = SlateGray)
                    Text("${String.format("%.1f", accumulated)} / ${(100.0 - pendingPercentage).toInt()}%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Pendiente", fontSize = 10.sp, color = SlateGray)
                    Text("${pendingPercentage.toInt()}%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ProBlue)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (pendingPercentage > 0) {
                val needed = maxOf(0.0, passingGrade - accumulated)
                val neededPercent = (needed / pendingPercentage) * 100.0

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (neededPercent <= 100) Color(0xFFF0FDF4) else Color(0xFFFEF2F2))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = if (neededPercent <= 0) "¡Ya aprobaste!" 
                                   else if (neededPercent <= 100) "Necesitas el ${String.format("%.1f", neededPercent)}% de lo pendiente"
                                   else "Imposible aprobar en ordinario",
                            fontWeight = FontWeight.Bold,
                            color = if (neededPercent <= 100) DarkGreen else Terracotta,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (neededPercent <= 0) "¡Felicidades! Sigue así para subir promedio."
                                   else if (neededPercent <= 70) "¡Vas muy bien! Solo necesitas un esfuerzo normal."
                                   else if (neededPercent <= 100) "Necesitas un esfuerzo extra. ¡Usa el Pomodoro y repasa bien!"
                                   else "Tendrás que ir a convocatoria o reparación.",
                            fontSize = 11.sp,
                            color = SlateGray
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun VacationScreen(viewModel: UniBuddyViewModel, onConfigureRoute: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BuddyMascot(
            modifier = Modifier.size(150.dp),
            pose = "sleeping",
            accessory = "sombrero_nica",
            mainColor = com.aistudio.unibuddy.qywvsp.ui.theme.ProBlue
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "¡Estás de Vacaciones!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = com.aistudio.unibuddy.qywvsp.ui.theme.NavyBlue
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Disfruta tu tiempo libre, recarga energías y prepárate para el próximo semestre.",
            fontSize = 14.sp,
            color = com.aistudio.unibuddy.qywvsp.ui.theme.SlateGray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun AdvancedGradesAnalytics(
    gradedExams: List<com.aistudio.unibuddy.qywvsp.data.Assessment>,
    allAssessments: List<com.aistudio.unibuddy.qywvsp.data.Assessment>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Análisis Avanzado", color = com.aistudio.unibuddy.qywvsp.ui.theme.NavyBlue, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
        text = { Text("No hay suficientes datos históricos para un análisis predictivo completo.") },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

@Composable
fun ConfettiOverlay(onFinished: () -> Unit) {
    val particles = remember {
        List(80) {
            ConfettiParticle(
                x = (0..1000).random().toFloat() / 1000f,
                y = -((0..500).random().toFloat() / 1000f),
                speedY = (5..15).random().toFloat() / 1000f,
                speedX = (-3..3).random().toFloat() / 2000f,
                color = listOf(
                    Color(0xFF4CAF50), // Mint Green
                    Color(0xFFFFC107), // Amber
                    Color(0xFF2196F3), // Pro Blue
                    Color(0xFFE91E63), // Pink
                    Color(0xFF9C27B0)  // Purple
                ).random(),
                size = (8..18).random().dp
            )
        }
    }

    var ticks by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        // Run confetti animation for 3 seconds (approx 150 frames at 20ms)
        for (i in 0..150) {
            kotlinx.coroutines.delay(20)
            ticks++
        }
        onFinished()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { p ->
                val currentY = (p.y + p.speedY * ticks) * size.height
                val currentX = (p.x + p.speedX * ticks) * size.width
                
                if (currentY in 0f..size.height && currentX in 0f..size.width) {
                    drawRect(
                        color = p.color,
                        topLeft = androidx.compose.ui.geometry.Offset(currentX, currentY),
                        size = androidx.compose.ui.geometry.Size(p.size.toPx(), p.size.toPx())
                    )
                }
            }
        }
    }
}

data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val speedY: Float,
    val speedX: Float,
    val color: Color,
    val size: androidx.compose.ui.unit.Dp
)
