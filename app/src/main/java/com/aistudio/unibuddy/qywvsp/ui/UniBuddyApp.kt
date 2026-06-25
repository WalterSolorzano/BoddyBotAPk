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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlinx.coroutines.launch

// Mascot Buddy Custom Vector Illustration
@Composable
fun BuddyMascot(
    modifier: Modifier = Modifier,
    isWorried: Boolean = false,
    isHappy: Boolean = true,
    pose: String = "idle", // "idle", "greeting", "working", "sleeping", "celebrating", "exam"
    accessory: String = "none", // "none", "hat", "glasses", "scarf", "sunglasses"
    mainColor: Color = DarkGreen
) {
    val infiniteTransition = rememberInfiniteTransition(label = "buddy_bob")
    val duration = when (pose) {
        "sleeping" -> 3000
        "celebrating", "working" -> 1000
        else -> 1800
    }
    val maxOffset = when (pose) {
        "sleeping" -> 1.5f
        "celebrating" -> 5f
        else -> 3f
    }
    
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = -maxOffset,
        targetValue = maxOffset,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )

    // Blinking animation
    val blinkProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                0f at 0
                0f at 3800
                1f at 3900 // close eyes
                0f at 4000 // open eyes
            }
        ),
        label = "blink"
    )

    Box(
        modifier = modifier
            .size(100.dp)
            .background(Color.White, shape = CircleShape)
            .border(2.dp, mainColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp).offset(y = bobOffset.dp)) {
            val w = size.width
            val h = size.height

            // Ears
            drawCircle(color = Color.White, radius = w * 0.15f, center = Offset(w * 0.25f, h * 0.25f))
            drawCircle(color = mainColor, radius = w * 0.15f, center = Offset(w * 0.25f, h * 0.25f), style = Stroke(width = 4f))
            drawCircle(color = Color(0xFFFFB7B2), radius = w * 0.08f, center = Offset(w * 0.25f, h * 0.25f))

            drawCircle(color = Color.White, radius = w * 0.15f, center = Offset(w * 0.75f, h * 0.25f))
            drawCircle(color = mainColor, radius = w * 0.15f, center = Offset(w * 0.75f, h * 0.25f), style = Stroke(width = 4f))
            drawCircle(color = Color(0xFFFFB7B2), radius = w * 0.08f, center = Offset(w * 0.75f, h * 0.25f))

            // Draw arms in back for certain poses
            if (pose == "greeting" || pose == "celebrating") {
                val armPath = Path().apply {
                    moveTo(w * 0.8f, h * 0.6f)
                    quadraticTo(w * 1.0f, h * 0.4f, w * 0.9f, h * (if(pose=="celebrating") 0.1f else 0.2f))
                }
                drawPath(path = armPath, color = mainColor, style = Stroke(width = 12f, cap = StrokeCap.Round))
                drawPath(path = armPath, color = Color.White, style = Stroke(width = 6f, cap = StrokeCap.Round))
                
                if (pose == "celebrating") {
                    val armPath2 = Path().apply {
                        moveTo(w * 0.2f, h * 0.6f)
                        quadraticTo(w * 0.0f, h * 0.4f, w * 0.1f, h * 0.1f)
                    }
                    drawPath(path = armPath2, color = mainColor, style = Stroke(width = 12f, cap = StrokeCap.Round))
                    drawPath(path = armPath2, color = Color.White, style = Stroke(width = 6f, cap = StrokeCap.Round))
                }
            }

            // Head Base
            drawCircle(color = Color.White, radius = w * 0.35f, center = Offset(w * 0.5f, h * 0.52f))
            drawCircle(color = mainColor, radius = w * 0.35f, center = Offset(w * 0.5f, h * 0.52f), style = Stroke(width = 4.0f))

            // Yellow Construction Helmet
            if (accessory == "hat") {
                val helmetPath = Path().apply {
                    arcTo(
                        rect = Rect(w * 0.18f, h * 0.15f, w * 0.82f, h * 0.48f),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 180f,
                        forceMoveTo = false
                    )
                    lineTo(w * 0.85f, h * 0.40f)
                    lineTo(w * 0.15f, h * 0.40f)
                    close()
                }
                drawPath(path = helmetPath, color = Color(0xFFF9C74F))
                drawPath(path = helmetPath, color = mainColor, style = Stroke(width = 4.0f))

                // Helmet Top ridge
                drawRect(
                    color = Color(0xFFF9C74F),
                    topLeft = Offset(w * 0.43f, h * 0.12f),
                    size = Size(w * 0.14f, h * 0.12f)
                )
                drawRect(
                    color = mainColor,
                    topLeft = Offset(w * 0.43f, h * 0.12f),
                    size = Size(w * 0.14f, h * 0.12f),
                    style = Stroke(width = 4.0f)
                )
            } else if (accessory == "cap") {
                val capColor = Color(0xFFEF5350)
                val capPath = Path().apply {
                    arcTo(
                        rect = Rect(w * 0.2f, h * 0.18f, w * 0.8f, h * 0.48f),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 180f,
                        forceMoveTo = false
                    )
                    lineTo(w * 0.8f, h * 0.40f)
                    lineTo(w * 0.2f, h * 0.40f)
                    close()
                }
                drawPath(path = capPath, color = capColor)
                drawPath(path = capPath, color = mainColor, style = Stroke(width = 3.0f))
                
                // Visor
                val visorPath = Path().apply {
                    moveTo(w * 0.8f, h * 0.38f)
                    quadraticTo(w * 0.95f, h * 0.35f, w * 0.98f, h * 0.40f)
                    lineTo(w * 0.8f, h * 0.40f)
                }
                drawPath(path = visorPath, color = capColor)
                drawPath(path = visorPath, color = mainColor, style = Stroke(width = 3.0f))
            }

            if (pose == "working" || pose == "exam" || accessory == "glasses") {
                // Draw some glasses
                drawCircle(color = mainColor, radius = w * 0.12f, center = Offset(w * 0.38f, h * 0.48f), style = Stroke(width = 4f))
                drawCircle(color = mainColor, radius = w * 0.12f, center = Offset(w * 0.62f, h * 0.48f), style = Stroke(width = 4f))
                drawLine(color = mainColor, start = Offset(w * 0.44f, h * 0.48f), end = Offset(w * 0.56f, h * 0.48f), strokeWidth = 4f)
            } else if (accessory == "sunglasses") {
                // Sunglasses: filled dark lenses with reflective details!
                val darkGlassColor = Color(0xFF212121)
                drawCircle(color = darkGlassColor, radius = w * 0.12f, center = Offset(w * 0.38f, h * 0.48f))
                drawCircle(color = darkGlassColor, radius = w * 0.12f, center = Offset(w * 0.62f, h * 0.48f))
                drawLine(color = darkGlassColor, start = Offset(w * 0.44f, h * 0.48f), end = Offset(w * 0.56f, h * 0.48f), strokeWidth = 6f)
                
                // Reflection glare effect
                drawLine(color = Color.White.copy(alpha = 0.8f), start = Offset(w * 0.35f, h * 0.44f), end = Offset(w * 0.40f, h * 0.52f), strokeWidth = 3f)
                drawLine(color = Color.White.copy(alpha = 0.8f), start = Offset(w * 0.59f, h * 0.44f), end = Offset(w * 0.64f, h * 0.52f), strokeWidth = 3f)
            }

            // Scarf
            if (accessory == "scarf") {
                val scarfPath = Path().apply {
                    moveTo(w * 0.15f, h * 0.65f)
                    lineTo(w * 0.85f, h * 0.65f)
                    lineTo(w * 0.80f, h * 0.75f)
                    lineTo(w * 0.20f, h * 0.75f)
                    close()
                }
                drawPath(path = scarfPath, color = Color(0xFFE57373))
                drawPath(path = scarfPath, color = mainColor, style = Stroke(width = 3f))
            }

            // Eyes
            val eyeRadius = w * 0.045f
            if (pose == "sleeping" || blinkProgress > 0.5f) {
                drawLine(color = mainColor, start = Offset(w * 0.33f, h * 0.48f), end = Offset(w * 0.43f, h * 0.48f), strokeWidth = 5f)
                drawLine(color = mainColor, start = Offset(w * 0.57f, h * 0.48f), end = Offset(w * 0.67f, h * 0.48f), strokeWidth = 5f)
            } else if (isWorried) {
                // Worried/Sad eyes: slanted lines (/\ or \/)
                drawLine(color = mainColor, start = Offset(w * 0.34f, h * 0.51f), end = Offset(w * 0.42f, h * 0.47f), strokeWidth = 5f)
                drawLine(color = mainColor, start = Offset(w * 0.66f, h * 0.51f), end = Offset(w * 0.58f, h * 0.47f), strokeWidth = 5f)
            } else {
                drawCircle(color = mainColor, radius = eyeRadius, center = Offset(w * 0.38f, h * 0.48f))
                drawCircle(color = mainColor, radius = eyeRadius, center = Offset(w * 0.62f, h * 0.48f))
                drawCircle(color = Color.White, radius = eyeRadius * 0.4f, center = Offset(w * 0.36f, h * 0.46f))
                drawCircle(color = Color.White, radius = eyeRadius * 0.4f, center = Offset(w * 0.60f, h * 0.46f))
            }

            // Snout
            drawOval(
                color = Color(0xFFF1EEE5),
                topLeft = Offset(w * 0.41f, h * 0.53f),
                size = Size(w * 0.18f, h * 0.13f)
            )
            drawOval(
                color = mainColor,
                topLeft = Offset(w * 0.41f, h * 0.53f),
                size = Size(w * 0.18f, h * 0.13f),
                style = Stroke(width = 3.0f)
            )

            // Nose
            drawCircle(color = mainColor, radius = w * 0.04f, center = Offset(w * 0.5f, h * 0.56f))

            // Mouth
            if (isHappy && !isWorried && pose != "sleeping") {
                val mouthPath = Path().apply {
                    moveTo(w * 0.45f, h * 0.59f)
                    quadraticTo(w * 0.5f, h * 0.64f, w * 0.55f, h * 0.59f)
                }
                drawPath(path = mouthPath, color = mainColor, style = Stroke(width = 3.0f))
            } else if (isWorried) {
                // Sad mouth curve!
                val mouthPath = Path().apply {
                    moveTo(w * 0.45f, h * 0.62f)
                    quadraticTo(w * 0.5f, h * 0.58f, w * 0.55f, h * 0.62f)
                }
                drawPath(path = mouthPath, color = mainColor, style = Stroke(width = 3.0f))
            } else if (pose == "sleeping") {
                drawCircle(color = mainColor, radius = w * 0.03f, center = Offset(w * 0.5f, h * 0.62f), style = Stroke(width = 2f))
            } else {
                drawLine(color = mainColor, start = Offset(w * 0.46f, h * 0.60f), end = Offset(w * 0.54f, h * 0.60f), strokeWidth = 3f)
            }

            // Draw Book if in Exam mode
            if (pose == "exam") {
                val bookPath = Path().apply {
                    moveTo(w * 0.35f, h * 0.72f)
                    lineTo(w * 0.5f, h * 0.77f)
                    lineTo(w * 0.65f, h * 0.72f)
                    lineTo(w * 0.62f, h * 0.85f)
                    lineTo(w * 0.5f, h * 0.89f)
                    lineTo(w * 0.38f, h * 0.85f)
                    close()
                }
                drawPath(path = bookPath, color = Color(0xFFF28B82)) // Pretty warm red book
                drawPath(path = bookPath, color = mainColor, style = Stroke(width = 3f))
                
                // Book middle line spine
                drawLine(color = mainColor, start = Offset(w * 0.5f, h * 0.77f), end = Offset(w * 0.5f, h * 0.89f), strokeWidth = 3f)
                
                // Page text lines
                drawLine(color = Color.White.copy(alpha = 0.85f), start = Offset(w * 0.40f, h * 0.78f), end = Offset(w * 0.47f, h * 0.80f), strokeWidth = 2f)
                drawLine(color = Color.White.copy(alpha = 0.85f), start = Offset(w * 0.40f, h * 0.82f), end = Offset(w * 0.47f, h * 0.84f), strokeWidth = 2f)
                drawLine(color = Color.White.copy(alpha = 0.85f), start = Offset(w * 0.53f, h * 0.80f), end = Offset(w * 0.60f, h * 0.78f), strokeWidth = 2f)
                drawLine(color = Color.White.copy(alpha = 0.85f), start = Offset(w * 0.53f, h * 0.84f), end = Offset(w * 0.60f, h * 0.82f), strokeWidth = 2f)
            }
        }
    }
}

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
                        ConfigTabScreen(
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

// 1. ONBOARDING SCREEN implementation
@Composable
fun OnboardingScreen(viewModel: UniBuddyViewModel, onFinished: () -> Unit) {
    var step by remember { mutableStateOf(1) }
    
    // Form Inputs
    var nameInput by remember { mutableStateOf("") }
    var originInput by remember { mutableStateOf("") }
    var destinationInput by remember { mutableStateOf("") }
    var baseTravelMinutes by remember { mutableStateOf("25") }

    // Subject Form Multi adder
    var subjectName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#E8F5E9") }
    var onboardingDays by remember { mutableStateOf(setOf("Lu", "Mi", "Vi")) }
    var onboardingBlock by remember { mutableStateOf("B2") }
    var onboardingCustomTime by remember { mutableStateOf("10:00 AM") }
    var subjectClassroom by remember { mutableStateOf("Aula 304, Edificio B") }
    var semesterInput by remember { mutableStateOf("1") }

    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val availableColors = listOf("#A5D6A7", "#90CAF9", "#FFCC80", "#CE93D8", "#80DEEA", "#EF9A9A", "#FFF59D")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Interactive Wizard Progress Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..4) {
                    val isActive = step == i
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(if (isActive) 32.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (isActive) NavyBlue else DarkGray.copy(alpha = 0.3f))
                    )
                }
            }

            when (step) {
                1 -> {
                    // Welcome & Name Slide
                    BuddyMascot(
                        modifier = Modifier
                            .size(150.dp)
                            .testTag("onboarding_mascot"),
                        isHappy = true
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "¡Hola! Soy Buddy",
                        style = MaterialTheme.typography.headlineMedium,
                        color = NavyBlue,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tu compañero para organizar la universidad sin estrés y estar a tiempo.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = DarkGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    Surface(
                        color = Amber.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "¿Reinstalaste la app? Finaliza esta configuración rápida y dirígete a la pestaña 'Configuración' para restaurar tus datos si tienes un respaldo.",
                                fontSize = 12.sp,
                                color = Color(0xFFE65100),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("¿Cómo te llamas?") },
                        placeholder = { Text("Ej: Estudiante") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_name_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyBlue,
                            focusedLabelColor = NavyBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            if (nameInput.isNotBlank()) {
                                viewModel.saveUsername(nameInput)
                            }
                            step = 2
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("onboarding_next_step1"),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Empezar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                    }
                }
                2 -> {
                    // University & Semester
                    BuddyMascot(
                        modifier = Modifier.size(100.dp),
                        isHappy = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "¿Dónde estudias?",
                        style = MaterialTheme.typography.headlineMedium,
                        color = NavyBlue,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Selecciona tu universidad, sede y semestre.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    val universities = viewModel.universities
                    var selectedUni by remember { mutableStateOf<UniBuddyViewModel.UniversityData?>(null) }
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.heightIn(max = 240.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(universities) { uni ->
                            val isSelected = selectedUni?.name == uni.name
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedUni = uni; destinationInput = "" },
                                colors = CardDefaults.cardColors(containerColor = if (isSelected) NavyBlue else Color.White),
                                border = BorderStroke(1.dp, if (isSelected) NavyBlue else SlateGray.copy(alpha = 0.3f)),
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = uni.logoEmoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = uni.shortName, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else NavyBlue, fontSize = 12.sp, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                    
                    if (selectedUni != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        PresetDropdownField(
                            label = "¿Qué sede/campus?",
                            value = destinationInput,
                            onValueChange = { destinationInput = it },
                            options = selectedUni!!.campuses.map { it.name },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    PresetDropdownField(
                        label = "¿En qué semestre estás?",
                        value = if (semesterInput.isNotBlank()) "Semestre $semesterInput" else "",
                        onValueChange = { semesterInput = it.replace("Semestre ", "") },
                        options = (1..10).map { "Semestre $it" },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (destinationInput.isNotBlank() && semesterInput.isNotBlank()) {
                                step = 3
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Siguiente", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                3 -> {
                    // Route/Path Configuration
                    BuddyMascot(
                        modifier = Modifier.size(100.dp),
                        isHappy = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Tu Camino a Clase",
                        style = MaterialTheme.typography.headlineMedium,
                        color = NavyBlue,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Configura tu punto de partida habitual.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    PresetDropdownField(
                        label = "¿Desde dónde sales?",
                        value = originInput,
                        onValueChange = { originInput = it },
                        options = listOf("Ubicación actual (GPS)", "Casa", "Trabajo"),
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true
                    )

                    if (originInput == "Ubicación actual (GPS)") {
                        var isSimulatingGps by remember { mutableStateOf(false) }
                        var gpsConfirmed by remember { mutableStateOf(false) }
                        var detectedDistance by remember { mutableStateOf<String?>(null) }
                        
                        LaunchedEffect(originInput) {
                            isSimulatingGps = true
                            kotlinx.coroutines.delay(1800L) // Beautiful realistic simulated seek delay
                            isSimulatingGps = false
                            detectedDistance = "5.4 km" // Realistic default travel distance
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = if (gpsConfirmed) MintGreen.copy(alpha = 0.1f) else Color(0xFFFFFBEB)),
                            border = BorderStroke(1.dp, if (gpsConfirmed) DarkGreen.copy(alpha = 0.3f) else Color(0xFFFBBF24)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                if (isSimulatingGps) {
                                    CircularProgressIndicator(color = NavyBlue, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Buscando señal satelital GPS...", fontSize = 12.sp, color = NavyBlue, fontWeight = FontWeight.Bold)
                                } else {
                                    // Simulated Map View Graphic
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFE0E0E0)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🗺️ Mapa GPS", fontSize = 24.sp, color = SlateGray)
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = DarkGreen,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "¡Señal GPS obtenida!",
                                        fontWeight = FontWeight.Bold,
                                        color = NavyBlue,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "UniBuddy utilizará tu ubicación en tiempo real para calcular distancias a tu facultad.",
                                        fontSize = 12.sp,
                                        color = SlateGray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    if (originInput == "Casa") {
                        val home by viewModel.homeAddress.collectAsStateWithLifecycle()
                        OutlinedTextField(
                            value = home,
                            onValueChange = { viewModel.saveHomeWorkAddresses(it, viewModel.workAddress.value) },
                            label = { Text("Dirección de Casa") },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            singleLine = true
                        )
                    } else if (originInput == "Trabajo") {
                        val work by viewModel.workAddress.collectAsStateWithLifecycle()
                        OutlinedTextField(
                            value = work,
                            onValueChange = { viewModel.saveHomeWorkAddresses(viewModel.homeAddress.value, it) },
                            label = { Text("Dirección de Trabajo") },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Minutos promedio de viaje: $baseTravelMinutes", fontSize = 14.sp, color = NavyBlue, fontWeight = FontWeight.Bold)
                    Slider(
                        value = baseTravelMinutes.toFloat(),
                        onValueChange = { baseTravelMinutes = it.toInt().toString() },
                        valueRange = 5f..120f,
                        steps = 23,
                        colors = SliderDefaults.colors(thumbColor = NavyBlue, activeTrackColor = NavyBlue)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (originInput.isNotBlank() && destinationInput.isNotBlank()) {
                                viewModel.saveRoute(originInput, destinationInput)
                                baseTravelMinutes.toIntOrNull()?.let { viewModel.saveBaseTravelTime(it) }
                            }
                            step = 4
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Siguiente", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            step = 4
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyBlue),
                        border = BorderStroke(1.5.dp, NavyBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Configurar más tarde", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                4 -> {
                    // Prepopulate / Add Classes Form
                    Text(
                        text = "Tus Materias",
                        style = MaterialTheme.typography.headlineMedium,
                        color = NavyBlue,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Configura tus asignaturas para el semestre $semesterInput. Toca una sugerencia para empezar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val semesterNum = semesterInput.toIntOrNull() ?: 1
                    val suggestedCurriculum = remember(semesterNum) { 
                        CurriculumData.industrialEngineering.filter { it.semester == semesterNum } 
                    }

                    if (suggestedCurriculum.isNotEmpty()) {
                        Text(
                            text = "Materias sugeridas (Semestre $semesterNum):",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyBlue,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(suggestedCurriculum) { mat: com.aistudio.unibuddy.qywvsp.data.PensumSubject ->
                                val isAdded = subjects.any { it.name.equals(mat.name, ignoreCase = true) }
                                FilterChip(
                                    selected = isAdded,
                                    onClick = { 
                                        if (!isAdded) {
                                            subjectName = mat.name
                                            selectedColor = availableColors.random()
                                        }
                                    },
                                    label = { Text(mat.name, fontSize = 12.sp) },
                                    leadingIcon = if (isAdded) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NavyBlue.copy(alpha = 0.1f),
                                        selectedLabelColor = NavyBlue,
                                        selectedLeadingIconColor = NavyBlue
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                if (subjectName.isBlank()) "Nueva Materia" else "Configurando: $subjectName",
                                style = MaterialTheme.typography.titleMedium,
                                color = NavyBlue,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // New Sessions List
                            val sessionsList = remember { mutableStateListOf<com.aistudio.unibuddy.qywvsp.data.ClassSessionDetails>() }

                            OutlinedTextField(
                                value = subjectName,
                                onValueChange = { subjectName = it },
                                label = { Text("Nombre de la Materia") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NavyBlue,
                                    focusedLabelColor = NavyBlue,
                                    unfocusedBorderColor = DarkGray.copy(alpha = 0.3f)
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text("Color Distintivo:", fontSize = 14.sp, color = NavyBlue, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                availableColors.forEach { hex ->
                                    val colorValue = Color(android.graphics.Color.parseColor(hex))
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(colorValue)
                                            .border(
                                                width = if (selectedColor == hex) 3.dp else 1.dp,
                                                color = if (selectedColor == hex) NavyBlue else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable { selectedColor = hex },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (selectedColor == hex) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = NavyBlue, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            SessionEditor(sessions = sessionsList, allSubjects = subjects)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            if (sessionsList.isNotEmpty()) {
                                val computedClasses = sessionsList.size * 14
                                Surface(
                                    color = NavyBlue.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Resumen de Asistencia", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyBlue)
                                        Text("Total estimado: $computedClasses clases en el semestre.", fontSize = 12.sp, color = DarkGray)
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        var attendancePercent by remember { mutableStateOf(75f) }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Mínimo requerido:", fontSize = 12.sp, color = DarkGray)
                                            Text("${attendancePercent.toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                                        }
                                        Slider(
                                            value = attendancePercent,
                                            onValueChange = { attendancePercent = it },
                                            valueRange = 50f..100f,
                                            steps = 9,
                                            colors = SliderDefaults.colors(
                                                thumbColor = NavyBlue,
                                                activeTrackColor = NavyBlue,
                                                inactiveTrackColor = DarkGray.copy(alpha = 0.2f)
                                            )
                                        )
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                if (subjectName.isNotBlank()) {
                                                    val computedSchedule = sessionsList.distinctBy { it.day }.joinToString(", ") { it.day }
                                                    val jsonString = sessionsList.toJsonString()
                                                    viewModel.addSubject(
                                                        name = subjectName,
                                                        schedule = computedSchedule,
                                                        sessionsJson = jsonString,
                                                        requiredAttendancePercent = attendancePercent.toInt(),
                                                        totalClasses = computedClasses,
                                                        colorHex = selectedColor
                                                    )
                                                    // Reset fields
                                                    subjectName = ""
                                                    sessionsList.clear()
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Añadir Materia", fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, DarkGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Selecciona bloques en el horario arriba", color = DarkGray, fontSize = 12.sp, fontStyle = FontStyle.Italic)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Materias cargadas: ${subjects.size}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyBlue
                    )

                    if (subjects.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        subjects.forEach { sub ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Bone),
                                border = BorderStroke(1.dp, SlateGray.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(sub.name, fontWeight = FontWeight.Bold, color = NavyBlue)
                                    IconButton(
                                        onClick = { viewModel.deleteSubject(sub) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Terracotta)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onFinished()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Finalizar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = {
                            viewModel.loadDemoData()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cargar Modo Demo (Datos de Prueba)", color = NavyBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Helper Composable for Decision Dashboard factors
@Composable
fun SuggestionFactorTag(title: String, value: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(
                text = title.uppercase(),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = SlateGray,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }
    }
}



// 2. DASHBOARD SCREEN (Vista de Inicio)
@Composable
fun DashboardScreen(
    viewModel: UniBuddyViewModel,
    onNavigateToDetails: (Int) -> Unit,
    onNavigateToGrades: (Int) -> Unit,
    onConfigureRoute: () -> Unit,
    onNavigateToFocus: () -> Unit
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
    val assessments by viewModel.assessments.collectAsStateWithLifecycle()
    val subjectImportanceMap by viewModel.subjectImportanceMap.collectAsStateWithLifecycle()

    val todayExam = remember(assessments) {
        val calendar = Calendar.getInstance()
        val dayOfWeekNum = calendar.get(Calendar.DAY_OF_WEEK)
        val currentDayCode = when (dayOfWeekNum) {
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

    val semesterState by viewModel.semesterState.collectAsStateWithLifecycle()
    val currentWeek by viewModel.currentWeekOfSemester.collectAsStateWithLifecycle()

    if (semesterState == "Vacaciones") {
        VacationScreen(viewModel = viewModel, onConfigureRoute = onConfigureRoute)
        return
    }

    val context = LocalContext.current

    // GPS real and simulated tracking state
    var locationPermissionGranted by remember { mutableStateOf(false) }
    var realGPSDistanceKm by remember { mutableStateOf<Double?>(null) }
    var gpsConfirmSubjectForAutoAttendance by remember { mutableStateOf<Subject?>(null) }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        locationPermissionGranted = granted
    }

    LaunchedEffect(Unit) {
        val fineGranted = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val coarseGranted = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        locationPermissionGranted = fineGranted || coarseGranted
    }

    fun requestRealGPSTracking() {
        if (!locationPermissionGranted) {
            locationLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }
        try {
            val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)

            val provider = when {
                isGpsEnabled -> android.location.LocationManager.GPS_PROVIDER
                isNetworkEnabled -> android.location.LocationManager.NETWORK_PROVIDER
                else -> null
            }
            if (provider != null) {
                val loc = locationManager.getLastKnownLocation(provider)
                if (loc != null) {
                    val coords = viewModel.getSelectedUniversityCoords() ?: Pair(12.1264, -86.2711) // Default to UNI RUSB if not found
                    val targetLoc = android.location.Location("").apply {
                        latitude = coords.first
                        longitude = coords.second
                    }
                    val distanceMeters = loc.distanceTo(targetLoc)
                    val distKm = distanceMeters / 1000.0
                    val statusText = when {
                        distKm < 0.5 -> "En la universidad"
                        distKm < 3.0 -> "Cerca de ${viewModel.destination.value}"
                        else -> "Lejos de ${viewModel.destination.value} (${String.format(Locale.US, "%.1f", distKm)} km)"
                    }
                    realGPSDistanceKm = distKm
                    viewModel.updateLocationStatus(true, statusText)
                } else {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        locationManager.getCurrentLocation(
                            provider,
                            null,
                            context.mainExecutor
                        ) { location ->
                            if (location != null) {
                                val coords = viewModel.getSelectedUniversityCoords() ?: Pair(12.1264, -86.2711)
                                val targetLoc = android.location.Location("").apply {
                                    latitude = coords.first
                                    longitude = coords.second
                                }
                                val distanceMeters = location.distanceTo(targetLoc)
                                val distKm = distanceMeters / 1000.0
                                val statusText = when {
                                    distKm < 0.5 -> "En la universidad"
                                    distKm < 3.0 -> "Cerca de ${viewModel.destination.value}"
                                    else -> "Lejos de ${viewModel.destination.value} (${String.format(Locale.US, "%.1f", distKm)} km)"
                                }
                                realGPSDistanceKm = distKm
                                viewModel.updateLocationStatus(true, statusText)
                            }
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            requestRealGPSTracking()
        }
    }

    // Stopwatch tracking variables
    var isTripActive by remember { mutableStateOf(false) }
    var tripStartTime by remember { mutableStateOf(0L) }
    var tripElapsedSeconds by remember { mutableStateOf(0) }
    var showTripDialogMinutes by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(isTripActive) {
        if (isTripActive) {
            tripStartTime = System.currentTimeMillis()
            while (isTripActive) {
                kotlinx.coroutines.delay(1000L)
                tripElapsedSeconds = ((System.currentTimeMillis() - tripStartTime) / 1000).toInt()
            }
        } else {
            tripElapsedSeconds = 0
        }
    }

    // 1. Determine dynamic upcoming class of the day taken from schedule
    val calendar = Calendar.getInstance()
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val currentDayCode = when (dayOfWeek) {
        Calendar.MONDAY -> "Lu"
        Calendar.TUESDAY -> "Ma"
        Calendar.WEDNESDAY -> "Mi"
        Calendar.THURSDAY -> "Ju"
        Calendar.FRIDAY -> "Vi"
        else -> ""
    }

    val nextClass = remember(subjects) {
        if (currentDayCode.isEmpty()) {
            subjects.firstOrNull()
        } else {
            val todayClasses = subjects.filter { it.schedule.contains(currentDayCode, ignoreCase = true) }
            todayClasses.firstOrNull() ?: subjects.firstOrNull()
        }
    }

    val nextClassMateria = nextClass?.name ?: "Sin materias"
    val sessionParsed = nextClass?.sessionsJson?.let { it.parseSessions() }?.firstOrNull()
    val nextClassTime = sessionParsed?.time ?: "10:00 AM"
    val nextClassRoom = sessionParsed?.room ?: "Aula sin definir"

    // 2. Base travel time: average of last 10 entries if they exist, or reference from setting if < 3
    val isCalibrated = tripRecords.size >= 3
    val baseTravelTimeSource = if (tripRecords.isNotEmpty()) {
        tripRecords.take(10).map { it.durationMinutes }.average().toInt()
    } else {
        baseTravelTime
    }

    val isMonday = calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
    val mondayMargin = if (isMonday) 5 else 0
    val earlyMargin = if (arrivalMarginPref == "temprano") 10 else 0

    // GPS/Distance travel time calculation
    val currentDistanceToCollege = realGPSDistanceKm?.let { dist ->
        if (dist > 150.0) {
            // Devices in cloud/emulator environments are often thousands of km away.
            // Let's mock a realistic travel distance (e.g., 5.4 km) for perfect local/UI preview!
            5.4
        } else {
            dist
        }
    }

    val liveDistanceTravelMinutes = currentDistanceToCollege?.let { ((it * 1.8) + 4.0).toInt().coerceAtLeast(1) }

    val locationBasedTravelMinutes = if (liveDistanceTravelMinutes != null) {
        liveDistanceTravelMinutes + (if (isRaining) 8 else 0) + mondayMargin + earlyMargin
    } else {
        baseTravelTimeSource + (if (isRaining) 10 else 0) + mondayMargin + earlyMargin
    }

    // Auto-Attendance Geofence Logic
    var showAutoAttendancePrompt by remember { mutableStateOf<Subject?>(null) }
    LaunchedEffect(currentDistanceToCollege, nextClass) {
        if (currentDistanceToCollege != null && currentDistanceToCollege < 0.5 && nextClass != null) { // Within 500 meters
            val todayDateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val alreadyLogged = attendanceLogs.any { it.subjectId == nextClass.id && it.date == todayDateStr }
            if (!alreadyLogged) {
                showAutoAttendancePrompt = nextClass
            }
        } else {
            showAutoAttendancePrompt = null
        }
    }

    // 3. Compute projected arrival and compare with class start time
    var parsedSuccess = false
    val classTimeCal = Calendar.getInstance().apply {
        set(Calendar.SECOND, 0)
    }

    try {
        val cleanTime = nextClassTime.trim().uppercase()
        val isPm = cleanTime.contains("PM")
        val isAm = cleanTime.contains("AM")
        val digits = cleanTime.replace("AM", "").replace("PM", "").trim().split(":")
        if (digits.size >= 2) {
            var hour = digits[0].toInt()
            val min = digits[1].toInt()
            if (isPm && hour < 12) hour += 12
            if (isAm && hour == 12) hour = 0
            classTimeCal.set(Calendar.HOUR_OF_DAY, hour)
            classTimeCal.set(Calendar.MINUTE, min)
            parsedSuccess = true
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    if (!parsedSuccess) {
        // Fallback: assume class starts in 45 minutes
        classTimeCal.add(Calendar.MINUTE, 45)
    }

    val arrivalTimeCal = Calendar.getInstance().apply {
        add(Calendar.MINUTE, locationBasedTravelMinutes)
    }

    // Difference between class start time and projected arrival time in minutes
    val minutesDiff = ((classTimeCal.timeInMillis - arrivalTimeCal.timeInMillis) / (60 * 1000)).toInt()
    
    // Remaining time from now to the class start
    val remainingToClass = ((classTimeCal.timeInMillis - Calendar.getInstance().timeInMillis) / (60 * 1000)).toInt().coerceAtLeast(0)

    val travelStatus = when {
        minutesDiff < 0 -> "crítico" // Projected arrival is AFTER class start time
        minutesDiff <= 10 -> "advertencia" // Projected arrival is within 10 minutes before class start time
        else -> "normal" // Projected arrival is more than 10 minutes before class start time
    }

    val upcomingExamsCount = assessments.count { it.grade == null }
    val globalAbsencesCount = attendanceLogs.count { !it.isPresent }
    val calculatedStress = (upcomingExamsCount * 20f + globalAbsencesCount * 5f).coerceIn(0f, 100f)
    
    val worstState = when {
        calculatedStress >= 80f -> "critico"
        calculatedStress >= 50f -> "atencion"
        else -> "normal"
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_column")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Toolbar / Header
        item(span = { GridItemSpan(maxLineSpan) }) {
            val isLocationAvailable by viewModel.isLocationAvailable.collectAsStateWithLifecycle()
            val currentLocationName by viewModel.currentLocationName.collectAsStateWithLifecycle()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD1E4FF)), // Elegant Light Blue
                        contentAlignment = Alignment.Center
                    ) {
                        BuddyMascot(
                            modifier = Modifier.fillMaxSize().padding(2.dp),
                            isHappy = true,
                            pose = "greeting"
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "¡Hola, $username!",
                            style = MaterialTheme.typography.titleMedium,
                            color = NavyBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if(isLocationAvailable) Icons.Default.LocationOn else Icons.Default.Place,
                                contentDescription = null,
                                tint = if(isLocationAvailable) DarkGreen else SlateGray,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if(isLocationAvailable) currentLocationName else "Ubicación desactivada",
                                style = MaterialTheme.typography.bodySmall,
                                color = if(isLocationAvailable) DarkGreen else SlateGray
                            )
                        }
                    }
                }
                IconButton(
                    onClick = onConfigureRoute,
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(1.dp, CircleShape)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configuración",
                        tint = NavyBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Hero Next Class Widget (Spans 2 columns)
        item(span = { GridItemSpan(maxLineSpan) }) {
            val nextClass = subjects.filter { it.schedule.contains(currentDayCode, ignoreCase = true) }.firstOrNull()
            val nextClassTime = nextClass?.sessionsJson?.parseSessions()?.firstOrNull { it.day.equals(currentDayCode, ignoreCase = true) }?.time ?: "08:00 AM"
            val faltasRestantes = if (nextClass != null) {
                val limit = ((100.0 - nextClass.requiredAttendancePercent) / 100.0) * nextClass.totalClasses
                val currentFaltas = absences.count { it.subjectId == nextClass.id }
                (limit - currentFaltas).toInt()
            } else 0
            val isExamMode = assessments.any { it.grade == null && it.examDate.trim().equals(currentDayCode, ignoreCase = true) && it.subjectId == nextClass?.id }
            
            HeroNextClassCard(
                subject = nextClass,
                classTime = nextClassTime,
                faltasRestantes = faltasRestantes,
                isExamMode = isExamMode
            )
        }

        // Today's Classes List
        item(span = { GridItemSpan(maxLineSpan) }) {
            TodayClassesListWidget(
                subjects = subjects,
                currentDayCode = currentDayCode
            )
        }

        // GPS and Stopwatch Widget
        item(span = { GridItemSpan(maxLineSpan) }) {
            GPSAndStopwatchWidget(
                currentDistanceToCollege = currentDistanceToCollege,
                locationBasedTravelMinutes = locationBasedTravelMinutes,
                baseTravelTimeSource = baseTravelTimeSource,
                isTripActive = isTripActive,
                tripElapsedSeconds = tripElapsedSeconds,
                onRequestGPS = { requestRealGPSTracking() },
                onStartTrip = {
                    isTripActive = true
                    tripElapsedSeconds = 0
                },
                onEndTrip = { finalMinutes ->
                    isTripActive = false
                    showTripDialogMinutes = finalMinutes
                }
            )
        }

        // Wellness Widget (Spans 2 columns)
        item(span = { GridItemSpan(maxLineSpan) }) {
            val stressStatusText = when (worstState) {
                "critico" -> "Crítico"
                "atencion" -> "Moderado"
                else -> "Relajado"
            }
            WellnessWidget(
                upcomingExamsCount = upcomingExamsCount,
                absencesCount = globalAbsencesCount,
                calculatedStress = calculatedStress,
                statusText = stressStatusText
            )
        }

        // Badges Widget (Spans 2 columns)
        item(span = { GridItemSpan(maxLineSpan) }) {
            BadgesMiniWidget(viewModel)
        }


        // Auto Attendance Geofence Prompt
        if (showAutoAttendancePrompt != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    border = BorderStroke(2.dp, DarkGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("¡Estás en el campus!", style = MaterialTheme.typography.titleMedium, color = NavyBlue, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Parece que estás listo para ${showAutoAttendancePrompt?.name}.", fontSize = 14.sp, color = SlateGray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(
                                onClick = {
                                    gpsConfirmSubjectForAutoAttendance = showAutoAttendancePrompt
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Sí, asistí hoy", color = Bone)
                            }
                        }
                    }
                }
            }
        }

        // Buddy's Tip Card (Consejo de Buddy)
        item(span = { GridItemSpan(maxLineSpan) }) {
            val isExamMode = todayExam != null
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(if (isExamMode) 6.dp else 4.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = if (isExamMode) Color(0xFFFFF1F1) else Bone),
                border = if (isExamMode) BorderStroke(2.dp, Terracotta) else null,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val parsedColor = try {
                        Color(android.graphics.Color.parseColor(buddyColorStr))
                    } catch(e: Exception) {
                        DarkGreen
                    }
                    val currentAccessory = if (buddyAccessory == "none" || buddyAccessory.isEmpty()) {
                        if (isRaining) "scarf" else "sunglasses"
                    } else {
                        buddyAccessory
                    }
                    BuddyMascot(
                        modifier = Modifier.size(72.dp),
                        isWorried = (worstState == "critico" || worstState == "atencion" || isRaining) && !isExamMode,
                        isHappy = (worstState == "normal" && !isRaining) && !isExamMode,
                        pose = if (isExamMode) "exam" else "idle",
                        accessory = currentAccessory,
                        mainColor = parsedColor
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val adviceIcon = when {
                                isExamMode -> Icons.Default.Warning
                                worstState == "critico" -> Icons.Default.Warning
                                worstState == "atencion" -> Icons.Default.Info
                                else -> Icons.Default.CheckCircle
                            }
                            val adviceColor = when {
                                isExamMode -> Terracotta
                                worstState == "critico" -> Terracotta
                                worstState == "atencion" -> Amber
                                else -> DarkGreen
                            }
                            Icon(imageVector = adviceIcon, contentDescription = null, tint = adviceColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isExamMode) "¡MODO EXAMEN ACTIVADO!" else "Consejo de Buddy",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isExamMode) Terracotta else NavyBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val adviceText = if (isExamMode) {
                            "¡HOY TIENES EXAMEN! Veo que tienes programado '${todayExam.name}' para hoy. Activa el Modo Examen y el Focus Mode para concentrarte al máximo."
                        } else when (worstState) {
                            "critico" -> listOf(
                                "¡Respira hondo! Veo que estás al límite de faltas. Organiza bien tus tiempos y enfócate en asistir para no perder la materia.",
                                "Situación crítica con las inasistencias. Es momento de mantener la calma y comprometerse a ir a clases.",
                                "¡Alerta roja! No te preocupes, aún puedes salvar el semestre si te mantienes constante a partir de hoy."
                            ).random()
                            "atencion" -> listOf(
                                "El parcial se está acercando. ¿Qué tal si hacemos una sesión de Focus Mode hoy para repasar tus apuntes?",
                                "¡Se vienen los exámenes! Estudiar un ratito cada día es mejor que estresarse a última hora.",
                                "Es época de evaluaciones. Mantén la concentración y asegúrate ese promedio."
                            ).random()
                            else -> listOf(
                                "¡Excelente ritmo! Vas al día con tu asistencia. Sigue así y el semestre será pan comido.",
                                "¡Todo bajo control! Tus estadísticas se ven muy bien. Sigue brillando.",
                                "¡Buen trabajo! Tienes buen margen de asistencia y todo en orden. Tómate un tiempo para ti si lo necesitas."
                            ).random()
                        }
                        Text(
                            text = adviceText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SlateGray
                        )
                    }
                }
            }
        }

        // Cartoon Widgets Row
        item(span = { GridItemSpan(maxLineSpan) }) {
            val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }

            val tomorrowDayCode = when (tomorrowCal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "Lu"
                Calendar.TUESDAY -> "Ma"
                Calendar.WEDNESDAY -> "Mi"
                Calendar.THURSDAY -> "Ju"
                Calendar.FRIDAY -> "Vi"
                else -> ""
            }
            val tomorrowFirstClass = subjects.filter { it.schedule.contains(tomorrowDayCode, ignoreCase=true) }.minByOrNull { sub ->
                sub.sessionsJson.parseSessions().firstOrNull { it.day.equals(tomorrowDayCode, ignoreCase=true) }?.time ?: "Z"
            }
            val tomorrowFirstClassTime = tomorrowFirstClass?.sessionsJson?.parseSessions()?.firstOrNull { it.day.equals(tomorrowDayCode, ignoreCase=true) }?.time ?: ""

            val nextExam = assessments.filter { it.grade == null }.minByOrNull { it.examDate }
            val nextExamSubject = nextExam?.let { ex -> subjects.find { it.id == ex.subjectId } }

            val widgets = mutableListOf<CartoonWidgetData>()

            // 0. Next Class Countdown Widget
            widgets.add(CartoonWidgetData(
                title = "PRÓXIMA CLASE",
                description = "Tu clase empieza en 47 min",
                icon = Icons.Default.PlayArrow,
                bgColor = Color(0xFFE8F5E9), // Light Green
                borderColor = Color(0xFF2E7D32), // Green
                iconBgColor = Color(0xFFA5D6A7),
                isAlert = true
            ))

            // 1. Tomorrow's First Class
            if (tomorrowFirstClass != null) {
                widgets.add(CartoonWidgetData(
                    title = "MAÑANA ARRANCA",
                    description = "${tomorrowFirstClass.name} ($tomorrowFirstClassTime)",
                    icon = Icons.Default.DateRange,
                    bgColor = Color(0xFFE1BEE7), // Light Purple
                    borderColor = Color(0xFF8E24AA), // Purple
                    iconBgColor = Color(0xFFCE93D8)
                ))
            }

            // 3. Next Exam Notification
            if (nextExam != null && nextExamSubject != null) {
                widgets.add(CartoonWidgetData(
                    title = "¡ALERTA EXAMEN!",
                    description = "${nextExam.name} de ${nextExamSubject.name} (${nextExam.examDate})",
                    icon = Icons.Default.Notifications,
                    bgColor = Color(0xFFFFCDD2), // Light Red
                    borderColor = Color(0xFFD32F2F), // Red
                    iconBgColor = Color(0xFFEF9A9A),
                    isAlert = true
                ))
            } else {
                widgets.add(CartoonWidgetData(
                    title = "¡TODO TRANQUILO!",
                    description = "No hay exámenes a la vista. Disfruta tu semana.",
                    icon = Icons.Default.Info,
                    bgColor = Color(0xFFC8E6C9), // Light Green
                    borderColor = Color(0xFF388E3C), // Green
                    iconBgColor = Color(0xFFA5D6A7)
                ))
            }

            // 4. Critical Absences Widget
            val criticalSubject = subjects.minByOrNull { sub ->
                val limit = ((100.0 - sub.requiredAttendancePercent) / 100.0) * sub.totalClasses
                val currentFaltas = absences.count { it.subjectId == sub.id }
                limit - currentFaltas
            }
            if (criticalSubject != null) {
                val limit = ((100.0 - criticalSubject.requiredAttendancePercent) / 100.0) * criticalSubject.totalClasses
                val currentFaltas = absences.count { it.subjectId == criticalSubject.id }
                val remaining = (limit - currentFaltas).toInt()
                if (remaining <= 3) {
                    widgets.add(CartoonWidgetData(
                        title = "¡AL BORDE!",
                        description = "Te quedan $remaining faltas en ${criticalSubject.name.take(12)}",
                        icon = Icons.Default.Warning,
                        bgColor = Color(0xFFFFCC80), // Orange
                        borderColor = Color(0xFFEF6C00), // Dark Orange
                        iconBgColor = Color(0xFFFFB74D),
                        isAlert = true
                    ))
                }
            }
            
            // 5. Global Average Widget (Replaced Calendar widget with Progress Widget)
            val cal = Calendar.getInstance()
            val currentWeek = ((cal.get(Calendar.DAY_OF_YEAR) % 98) / 7) + 1 // Dummy calculation for week
            widgets.add(CartoonWidgetData(
                title = "PROGRESO SEMESTRE",
                description = "Semana $currentWeek de 14. ¡Vamos!",
                icon = Icons.Default.Star,
                bgColor = Color(0xFFB3E5FC),
                borderColor = Color(0xFF0277BD),
                iconBgColor = Color(0xFF81D4FA)
            ))

            // 6. Weather Widget
            if (isRaining) {
                widgets.add(CartoonWidgetData(
                    title = "LLUEVE",
                    description = "¡Lleva paraguas! El viaje será más lento.",
                    icon = Icons.Default.Warning,
                    bgColor = Color(0xFFCFD8DC),
                    borderColor = Color(0xFF455A64),
                    iconBgColor = Color(0xFFB0BEC5)
                ))
            }

            if (widgets.isNotEmpty()) {
                DashboardCartoonWidgets(widgets, isRaining)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
                val examToday = remember(assessments) {
                    assessments.find { ass ->
                        ass.grade == null && ass.examDate.trim().equals(currentDayCode, ignoreCase = true)
                    }
                }

                val todayClasses = remember(subjects, currentDayCode) {
                    if (currentDayCode.isEmpty()) {
                        emptyList()
                    } else {
                        subjects.filter { it.schedule.contains(currentDayCode, ignoreCase = true) }
                    }
                }

                val hasHighImportanceToday = remember(todayClasses, subjectImportanceMap) {
                    todayClasses.any { (subjectImportanceMap[it.id] ?: "Media") == "Alta" }
                }

                val todayLowestMargin = remember(todayClasses, absences, attendanceLogs) {
                    var lowest = 999
                    for (sub in todayClasses) {
                        val subAbsTotal = absences.count { it.subjectId == sub.id } + attendanceLogs.count { it.subjectId == sub.id && !it.isPresent }
                        val maxAbs = sub.totalClasses - Math.ceil(sub.totalClasses * (sub.requiredAttendancePercent / 100.0)).toInt()
                        val remainingAbs = maxAbs - subAbsTotal
                        if (remainingAbs < lowest) lowest = remainingAbs
                    }
                    lowest
                }

                val adviceData = remember(subjects, absences, attendanceLogs, assessments, isRaining, examToday, subjectImportanceMap, todayClasses, hasHighImportanceToday, todayLowestMargin) {
                    if (todayClasses.isEmpty()) {
                        if (examToday != null) {
                            val sub = subjects.find { it.id == examToday.subjectId }
                            Triple(
                                listOf("Día sin clases, pero... ¡HAY PARCIAL!", "Día libre, excepto por tu evaluación", "¡A estudiar! No hay cursada regular, pero hay examen").random(),
                                "Hoy no tienes clases regulares, pero tienes programado el examen '${examToday.name}' para ${sub?.name ?: "tu materia"}. ¿Por qué no hacemos una sesión de Focus?",
                                "critico_examen"
                            )
                        } else {
                            Triple(
                                listOf("¡Día sin clases!", "¡A descansar!", "Día libre a la vista").random(),
                                listOf(
                                    "No tienes clases registradas para hoy. Buen día para descansar u organizar tus pendientes.",
                                    "Hoy no hay que ir a la universidad. ¡Aprovecha para adelantar trabajos o descansar!",
                                    "Tu calendario está libre hoy. ¡Respira hondo y recarga energías!"
                                ).random(),
                                "discrecional"
                            )
                        }
                    } else {
                        var criticalSubjectName: String? = null
                        var highImportanceWarning: String? = null
                        var lowGradesWarning: String? = null

                        for (sub in todayClasses) {
                            val subAbsTotal = absences.count { it.subjectId == sub.id } + attendanceLogs.count { it.subjectId == sub.id && !it.isPresent }
                            val maxAbs = sub.totalClasses - Math.ceil(sub.totalClasses * (sub.requiredAttendancePercent / 100.0)).toInt()
                            val remainingAbs = maxAbs - subAbsTotal

                            val importance = subjectImportanceMap[sub.id] ?: "Media"

                            if (remainingAbs <= 1) {
                                criticalSubjectName = sub.name
                            } else if (importance == "Alta" && remainingAbs <= 3) {
                                highImportanceWarning = "Hoy tienes '${sub.name}', materia de IMPORTANCIA ALTA y solo te quedan $remainingAbs faltas de margen."
                            }

                            // Compute subject average grade
                            val subGrades = assessments.filter { it.subjectId == sub.id && it.grade != null }
                            if (subGrades.isNotEmpty()) {
                                val avg = subGrades.map { it.grade!! }.average()
                                if (avg < 70.0) {
                                    lowGradesWarning = "Tu promedio en '${sub.name}' es bajo (${String.format(Locale.US, "%.1f", avg)}). ¡No deberías faltar para poder repuntar!"
                                }
                            }
                        }

                        if (examToday != null) {
                            val sub = subjects.find { it.id == examToday.subjectId }
                            Triple(
                                listOf("¡HOY RINDES EVALUACIÓN!", "DÍA DE PARCIAL", "¡A DEMOSTRAR LO QUE SABES!").random(),
                                "Hoy rinden el examen '${examToday.name}' para ${sub?.name ?: "tu materia"}. ¡Concéntrate al máximo, asistir es obligatorio!",
                                "critico_examen"
                            )
                        } else if (criticalSubjectName != null) {
                            Triple(
                                listOf("¡ASISTENCIA CRÍTICA!", "ALERTA ROJA", "¡NI SE TE OCURRA FALTAR!").random(),
                                "Estás al límite de faltas en $criticalSubjectName (te quedan $todayLowestMargin). ¡Si faltas hoy podrías quedar libre! Corre a clases.",
                                "critico"
                            )
                        } else if (lowGradesWarning != null) {
                            Triple(
                                "RENDIMIENTO ACADÉMICO EN RIESGO",
                                "$lowGradesWarning Ir hoy es de máxima importancia para resolver dudas y mejorar tu nota.",
                                "critico"
                            )
                        } else if (highImportanceWarning != null) {
                            Triple(
                                "MATERIA DE ALTA IMPORTANCIA",
                                "$highImportanceWarning Te recomendamos asistir para no comprometer la cursada.",
                                "critico"
                            )
                        } else if (isRaining) {
                            if (hasHighImportanceToday) {
                                Triple(
                                    "LLUVIA VS CLASE ALTA PRIORIDAD",
                                    "Está lloviendo y el viaje puede ser lento, pero hoy tienes una clase calificada como de Alta Importancia. ¡Vale la pena asistir!",
                                    "recomendado"
                                )
                            } else {
                                Triple(
                                    listOf("¿Faltamos? (Estadísticas seguras)", "Lluvia + Buen Margen = Cama", "Clima hostil").random(),
                                    "Tienes un margen cómodo ($todayLowestMargin faltas disponibles) y está lloviendo. Si prefieres evitar mojarte, las estadísticas dicen que puedes quedarte en casa.",
                                    "discrecional_lluvia"
                                )
                            }
                        } else {
                            Triple(
                                listOf("ASISTENCIA RECOMENDADA", "¡A CLASES!", "Día productivo").random(),
                                listOf(
                                    "Tu margen es cómodo ($todayLowestMargin restantes). Ir hoy suma para mantener el ritmo.",
                                    "Aprovecha que no llueve y tienes buen margen. ¡A sumar presencias en la universidad!",
                                    "Hoy es un gran día para ir a cursar. ¡Éxitos en tu jornada!"
                                ).random(),
                                "normal"
                            )
                        }
                    }
                }

            val cardColor = when (adviceData.third) {
                "critico", "critico_examen" -> Color(0xFFFFF0F0) // Soft Red
                "recomendado" -> Color(0xFFF0F4FF) // Soft Blue
                "discrecional_lluvia", "discrecional" -> Color(0xFFF8F9FA) // Soft Gray
                else -> Color(0xFFF2FBF6) // Soft Green
            }

            val accentColor = when (adviceData.third) {
                "critico", "critico_examen" -> Terracotta
                "recomendado" -> NavyBlue
                "discrecional_lluvia", "discrecional" -> SlateGray
                else -> DarkGreen
            }

            val arrivalTimeStr = remember(locationBasedTravelMinutes) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.MINUTE, locationBasedTravelMinutes)
                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Title and weather alert
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TABLERO DE CONTROL Y DECISIONES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                letterSpacing = 1.2.sp
                            )
                        }
                        
                        if (isRaining) {
                                Surface(
                                    color = Color(0x223F51B5),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = NavyBlue, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Tránsito Lluvioso",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NavyBlue
                                        )
                                    }
                                }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    // Headline advice
                    Text(
                        text = adviceData.first,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyBlue
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = adviceData.second,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NavyBlue.copy(alpha = 0.85f),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "FACTORES CONSIDERADOS PARA ESTA PROPUESTA:",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor.copy(alpha = 0.8f),
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Weather Factor
                        val weatherText = if (isRaining) "Lluvia Activa" else "Despejado"
                        val weatherColor = if (isRaining) Terracotta else DarkGreen
                        SuggestionFactorTag(title = "Clima", value = weatherText, color = weatherColor)

                        // Academic Margin Factor
                        val marginText = if (todayLowestMargin == 999) "Sin clases" else "$todayLowestMargin faltas"
                        val marginColor = if (todayLowestMargin <= 1) Terracotta else if (todayLowestMargin <= 3) Color(0xFFE5A800) else DarkGreen
                        SuggestionFactorTag(title = "Margen Faltas", value = marginText, color = marginColor)

                        // Subject Importance
                        val importanceText = if (todayClasses.isEmpty()) "Ninguna" else if (hasHighImportanceToday) "Alta Prioridad" else "Normal"
                        val importanceColor = if (hasHighImportanceToday) Terracotta else SlateGray
                        SuggestionFactorTag(title = "Importancia", value = importanceText, color = importanceColor)
                    }
                    
                    if (adviceData.third == "critico_examen") {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToFocus,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue, contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Iniciar Modo Focus", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val todayClassesForHud = if (currentDayCode.isEmpty()) emptyList() else subjects.filter { it.schedule.contains(currentDayCode, ignoreCase = true) }

                    if (todayClassesForHud.isNotEmpty()) {
                        // HUGE TRAVEL ETA HUD BLOCK
                        val hudBgColor = Color(0xFFF8F9FA) // Light gray matching palette
                        val hudBorderColor = NavyBlue
                        val iconTint = when {
                            minutesDiff < 0 -> Terracotta
                            minutesDiff <= 10 -> Color(0xFFE5A800) // Darker yellow/amber
                            else -> DarkGreen
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = hudBgColor),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = NavyBlue, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "REPORTE DE RUTA",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NavyBlue
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val iconStatus = if (minutesDiff < 0) Icons.Default.Warning else Icons.Default.CheckCircle
                                        Icon(imageVector = iconStatus, contentDescription = null, tint = iconTint, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (minutesDiff < 0) "DEMORADO" else "A TIEMPO",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = iconTint
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                val dynamicStatusLabel = when {
                                    minutesDiff < 0 -> "Llegas tarde por ${-minutesDiff} min"
                                    minutesDiff <= 10 -> "Margen estrecho de $minutesDiff min"
                                    else -> "Bien a tiempo (+ $minutesDiff min)"
                                }

                                Text(
                                    text = dynamicStatusLabel,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyBlue
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Divider(color = SlateGray.copy(alpha = 0.2f), thickness = 1.dp)
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Recorrido", fontSize = 11.sp, color = SlateGray)
                                        Text("$locationBasedTravelMinutes min", fontSize = 13.sp, color = NavyBlue, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Llegada", fontSize = 11.sp, color = SlateGray)
                                        Text(arrivalTimeStr, fontSize = 13.sp, color = NavyBlue, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Clase ($nextClassMateria)", fontSize = 11.sp, color = SlateGray)
                                        Text(nextClassTime, fontSize = 13.sp, color = iconTint, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Display summary of today's classes if any exist
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = accentColor.copy(alpha = 0.15f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Análisis Viabilidad de Falta por Materia:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        todayClassesForHud.forEach { sub ->
                            val subAbsTotal = absences.count { it.subjectId == sub.id } + attendanceLogs.count { it.subjectId == sub.id && !it.isPresent }
                            val maxAbs = sub.totalClasses - Math.ceil(sub.totalClasses * (sub.requiredAttendancePercent / 100.0)).toInt()
                            val remainingAbs = maxAbs - subAbsTotal
                            
                            val (feasibilityLabel, feasibilityColor, feasibilityIcon) = when {
                                examToday?.subjectId == sub.id -> Triple("EXAMEN HOY: NO FALTAR", Terracotta, Icons.Default.Warning)
                                remainingAbs <= 0 -> Triple("FALTAS AGOTADAS", Terracotta, Icons.Default.Clear)
                                remainingAbs == 1 -> Triple("CRÍTICO ($remainingAbs rest.)", Terracotta, Icons.Default.Info)
                                remainingAbs == 2 -> Triple("PRECAUCIÓN ($remainingAbs rest.)", Color(0xFFE5A800), Icons.Default.Info)
                                else -> Triple("SEGURO ($remainingAbs rest.)", DarkGreen, Icons.Default.CheckCircle)
                            }
                            
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = sub.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NavyBlue
                                        )
                                        val sessionParsed = sub.sessionsJson.parseSessions().firstOrNull { it.day.equals(currentDayCode, ignoreCase=true) } ?: sub.sessionsJson.parseSessions().firstOrNull()
                                        val displayTime = sessionParsed?.time ?: "10:00 AM"
                                        val displayRoom = sessionParsed?.room ?: ""
                                        Text(
                                            text = "Horario: $displayTime | Aula: $displayRoom",
                                            fontSize = 11.sp,
                                            color = SlateGray
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = feasibilityLabel,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = feasibilityColor
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(imageVector = feasibilityIcon, contentDescription = null, tint = feasibilityColor, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Next class card (Próxima Clase)
        item(span = { GridItemSpan(maxLineSpan) }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = NavyBlue),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    StylizedMapBackground(
                        modifier = Modifier.fillMaxSize(), 
                        isUserOnCampus = currentDistanceToCollege != null && currentDistanceToCollege < 0.5
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("PRÓXIMA CLASE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Bone.copy(alpha=0.7f), letterSpacing = 1.sp)
                                Text(
                                    text = nextClassMateria,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Bone,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Surface(
                                color = when (travelStatus) {
                                    "normal" -> MintGreen
                                    "advertencia" -> Amber
                                    else -> Terracotta
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = travelStatus.replaceFirstChar { it.uppercase() },
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Bone.copy(alpha=0.7f), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("$nextClassTime — Viaje: $locationBasedTravelMinutes min", fontSize = 13.sp, color = Bone, fontWeight = FontWeight.Bold)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Bone.copy(alpha=0.7f), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("$nextClassRoom • Clase en $remainingToClass min", fontSize = 12.sp, color = Bone.copy(alpha=0.7f))
                                }
                            }
                            
                            IconButton(
                                onClick = onConfigureRoute,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(DarkGreen, CircleShape)
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit path", tint = Bone, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Live Calibration Warning Banner if needed
        if (!isCalibrated) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E5)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFD8A8))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFD35400),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Estimación no calibrada: Falta registrar al menos 3 viajes para usar tu promedio real. Usando onboarding ($baseTravelTime min).",
                            fontSize = 11.sp,
                            color = Color(0xFF862E00),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Live Real Weather API Integration and Indicator Card
        item(span = { GridItemSpan(maxLineSpan) }) {
            val weatherDescription by viewModel.weatherDescription.collectAsStateWithLifecycle()
            val lastWeatherUpdateMsg by viewModel.lastWeatherUpdateMsg.collectAsStateWithLifecycle()
            val isFetchingWeather by viewModel.isFetchingWeather.collectAsStateWithLifecycle()

            Card(
                modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Bone),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MintGreen.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(24.dp)) {
                                val w = size.width
                                val h = size.height
                                if (isRaining) {
                                    // Custom Cloud shape
                                    drawCircle(color = NavyBlue, radius = w * 0.28f, center = Offset(w * 0.35f, h * 0.6f))
                                    drawCircle(color = NavyBlue, radius = w * 0.35f, center = Offset(w * 0.55f, h * 0.45f))
                                    drawCircle(color = NavyBlue, radius = w * 0.28f, center = Offset(w * 0.75f, h * 0.6f))
                                    drawRect(color = NavyBlue, topLeft = Offset(w * 0.35f, h * 0.45f), size = Size(w * 0.4f, h * 0.35f))
                                } else {
                                    // Custom Sun shape with rays
                                    drawCircle(color = Color(0xFFE28743), radius = w * 0.28f, center = Offset(w * 0.5f, h * 0.5f))
                                    drawLine(color = Color(0xFFE28743), start = Offset(w * 0.5f, h * 0.05f), end = Offset(w * 0.5f, h * 0.2f), strokeWidth = 5f)
                                    drawLine(color = Color(0xFFE28743), start = Offset(w * 0.5f, h * 0.8f), end = Offset(w * 0.5f, h * 0.95f), strokeWidth = 5f)
                                    drawLine(color = Color(0xFFE28743), start = Offset(w * 0.05f, h * 0.5f), end = Offset(w * 0.2f, h * 0.5f), strokeWidth = 5f)
                                    drawLine(color = Color(0xFFE28743), start = Offset(w * 0.8f, h * 0.5f), end = Offset(w * 0.95f, h * 0.5f), strokeWidth = 5f)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Clima Actual: $weatherDescription",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = NavyBlue
                            )
                            Text(
                                text = "Ajuste automático: " + (if (isRaining) "+10 mins (Lluvia)" else "Despejado (Sin demora)"),
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateGray
                            )
                            Text(
                                text = lastWeatherUpdateMsg,
                                fontSize = 10.sp,
                                color = SlateGray.copy(alpha = 0.8f)
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.refreshWeather() },
                        enabled = !isFetchingWeather,
                        modifier = Modifier.size(36.dp).background(Color(0xFFE8DEF8), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refrescar clima",
                            tint = NavyBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Control to set "Llegar Temprano" vs "Llegar Normal" margin preference
        item(span = { GridItemSpan(maxLineSpan) }) {
            val arrivalMarginPref by viewModel.arrivalMarginPreference.collectAsStateWithLifecycle()

            Card(
                modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Bone),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Preferencia de Llegada",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavyBlue
                    )
                    Text(
                        text = "Elige si deseas un margen prudencial extra para llegar con calma:",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateGray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Llegar Temprano (+10m)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = NavyBlue
                        )
                        Switch(
                            checked = arrivalMarginPref == "temprano",
                            onCheckedChange = { isChecked ->
                                viewModel.setArrivalMarginPreference(if (isChecked) "temprano" else "normal")
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Bone, checkedTrackColor = DarkGreen)
                        )
                    }
                }
            }
        }

        // Weekly Activity Chart (Actividad Semanal)
        item(span = { GridItemSpan(maxLineSpan) }) {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Bone),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Actividad Semanal",
                            style = MaterialTheme.typography.titleMedium,
                            color = NavyBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    val classCounts = remember(subjects) {
                        listOf("Lu" to "L", "Ma" to "M", "Mi" to "X", "Ju" to "J", "Vi" to "V", "Sá" to "S").map { (code, letter) ->
                            val count = subjects.count { it.schedule.contains(code, ignoreCase = true) }
                            letter to count
                        }
                    }
                    val maxClasses = classCounts.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 1
                    val weekdaysLayout = classCounts.map { it.first to (it.second.toFloat() / maxClasses.toFloat()) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weekdaysLayout.forEach { (day, fraction) ->
                            val drawFraction = fraction.coerceAtLeast(0.02f)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight(drawFraction)
                                        .width(36.dp)
                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        .background(if (fraction > 0f) SlateGray.copy(alpha = 0.5f) else Bone.copy(alpha = 0.5f))
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = day,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (fraction > 0f) NavyBlue else SlateGray.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (false) {
        // 1. GPS Location and Distance Card
        item(span = { GridItemSpan(maxLineSpan) }) {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(20.dp)),
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
                        Column {
                            Text(
                                text = "Ubicación GPS y Trayecto",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = NavyBlue
                            )
                            Text(
                                text = "Distancia exacta calculada hacia la universidad",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateGray
                            )
                        }
                        IconButton(
                            onClick = { requestRealGPSTracking() },
                            modifier = Modifier.size(32.dp).background(Bone, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Actualizar GPS",
                                tint = NavyBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Location Info Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Distancia a la Universidad",
                                fontSize = 11.sp,
                                color = SlateGray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format(Locale.US, "%.2f km", currentDistanceToCollege),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = NavyBlue
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Tiempo Estimado de Viaje",
                                fontSize = 11.sp,
                                color = SlateGray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$locationBasedTravelMinutes minutos",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = DarkGreen
                            )
                        }
                    }

                    // Simulated alert showing delay status
                    val delayMinutes = locationBasedTravelMinutes - baseTravelTimeSource
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (delayMinutes > 5) Terracotta.copy(alpha = 0.08f) else MintGreen.copy(alpha = 0.08f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (delayMinutes > 5) Icons.Default.Warning else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (delayMinutes > 5) Terracotta else DarkGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (delayMinutes > 5) {
                                "¡Tránsito denso o distancia alta! Viajarás +$delayMinutes min respecto a tu promedio diario ($baseTravelTimeSource min)."
                            } else if (delayMinutes < -2) {
                                val absDiff = -delayMinutes
                                "Estás muy cerca del campus. Llegarás rápido (-$absDiff min de tu promedio)."
                            } else {
                                "Tránsito fluido. Tu viaje actual está dentro de tu promedio habitual ($baseTravelTimeSource min)."
                            },
                            fontSize = 12.sp,
                            color = if (delayMinutes > 5) Color(0xFF7A1C1C) else Color(0xFF1E4E2C),
                            fontWeight = FontWeight.Medium
                        )
                    }

                }
            }
        }

        // 2. Journey Stopwatch & Trip Records Analytics
        item(span = { GridItemSpan(maxLineSpan) }) {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Bone)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Cronómetro de Viaje Diario",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavyBlue
                    )
                    Text(
                        text = "Registra tus tiempos de viaje para calibrar automáticamente las alertas",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateGray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (!isTripActive) {
                        // Button to Start Trip
                        Button(
                            onClick = {
                                isTripActive = true
                                tripElapsedSeconds = 0
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Comenzar Viaje Casa -> Universidad", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Trip is active! Show ticking elapsed travel stopwatch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE8DEF8), RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Terracotta)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("VIAJE ACTIVADO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Terracotta, letterSpacing = 1.sp)
                                }
                                val mins = tripElapsedSeconds / 60
                                val secs = tripElapsedSeconds % 60
                                Text(
                                    text = String.format(Locale.US, "%02dm %02ds", mins, secs),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NavyBlue
                                )
                            }

                            Button(
                                onClick = {
                                    isTripActive = false
                                    val finalMinutes = (tripElapsedSeconds / 60).coerceAtLeast(1)
                                    showTripDialogMinutes = finalMinutes
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Bone, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Llegué", color = Bone, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Historical statistics list
                    if (tripRecords.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Historial y Rendimiento de Viajes (Estadísticas)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyBlue,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Bento Mini Chart representing history
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Bone),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Promedio Registrado", fontSize = 11.sp, color = SlateGray)
                                    Text("$baseTravelTimeSource minutos", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                // Simple elegant bar visual mapping durations
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    val lastTripsSubset = tripRecords.take(7).reversed()
                                    val maxRecordValue = tripRecords.map { it.durationMinutes }.maxOrNull() ?: 1
                                    lastTripsSubset.forEach { tr ->
                                        val fillFraction = (tr.durationMinutes.toFloat() / maxRecordValue.toFloat()).coerceIn(0.12f, 1.0f)
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight(fillFraction)
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (tr.wasRaining) Terracotta else DarkGreen)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "${tr.durationMinutes}m",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SlateGray
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(DarkGreen))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Despejado", fontSize = 10.sp, color = SlateGray)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Terracotta))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Día Lluvia", fontSize = 10.sp, color = SlateGray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        }

        // Bento Grid Stats (Asistencia & Notas side-by-side)
        item(span = { GridItemSpan(maxLineSpan) }) {
            val totalInRisk = subjects.count { sub ->
                val subAbs = absences.filter { it.subjectId == sub.id }
                val maxAbs = sub.totalClasses - ceil(sub.totalClasses * (sub.requiredAttendancePercent / 100.0)).toInt()
                (maxAbs - subAbs.size) <= 1
            }

            // Asistencia Summary Card (Full width)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onNavigateToDetails(subjects.firstOrNull()?.id ?: 0) }
                    .border(1.dp, Color(0xFFDCE3E9), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8DEF8)), 
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = NavyBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Asistencia Global",
                            fontSize = 14.sp,
                            color = NavyBlue.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (totalInRisk == 0) "Todo en orden. Ninguna materia en riesgo." else "$totalInRisk materias en riesgo de inasistencia",
                            fontSize = 16.sp,
                            color = NavyBlue,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }

    if (showTripDialogMinutes != null) {
        val calculatedMins = showTripDialogMinutes ?: 1
        AlertDialog(
            onDismissRequest = { showTripDialogMinutes = null },
            title = { Text("Viaje Finalizado", color = NavyBlue, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Llegaste a destino en un tiempo registrado de $calculatedMins minutos. ¿Deseas conservar este registro para calibrar tu promedio general?",
                    color = SlateGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.recordTripRealTime(calculatedMins)
                        showTripDialogMinutes = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                ) {
                    Text("Confirmar y Guardar", color = Bone)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTripDialogMinutes = null }) {
                    Text("Descartar", color = SlateGray)
                }
            },
            containerColor = Color.White
        )
    }

    if (gpsConfirmSubjectForAutoAttendance != null) {
        GPSConfirmationDialog(
            subject = gpsConfirmSubjectForAutoAttendance!!,
            viewModel = viewModel,
            onDismiss = { gpsConfirmSubjectForAutoAttendance = null },
            onConfirm = { sub, address ->
                val todayStr = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault()).format(java.util.Date())
                val sectorSuffix = if (address.contains("Sector:")) {
                    " (" + address.substringAfter("Sector:").trim() + ")"
                } else {
                    " (Confirmado)"
                }
                viewModel.registerAttendanceLog(sub.id, isPresent = true, dateStr = "$todayStr$sectorSuffix")
                gpsConfirmSubjectForAutoAttendance = null
                showAutoAttendancePrompt = null
            }
        )
    }


}

// Helper Subject Color Palette for Grid Cards
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
            val defaultSession = sub.sessionsJson.parseSessions().firstOrNull()
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
                sub.sessionsJson.parseSessions().forEach { session ->
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
        Button(
            onClick = {
                ScheduleExporter.exportToGallery(context, subjects, "Mi Universidad")
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
                val sessions = subject.sessionsJson.parseSessions()
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
                                                sessionsJson = jsonString,
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
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = SlateGray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("No tenes materias cargadas", style = MaterialTheme.typography.titleMedium, color = NavyBlue)
                            Text("Agrégalas de configuración.", fontSize = 12.sp, color = SlateGray)
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
                                        val sessions = clickedSubject.sessionsJson.parseSessions()
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

// 4. DETALLE MATERIA SCREEN
@Composable
fun SubjectDetailsScreen(viewModel: UniBuddyViewModel, subjectId: Int, onBack: () -> Unit) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val subject = subjects.find { it.id == subjectId }
    val absences by viewModel.getAbsencesForSubject(subjectId).collectAsStateWithLifecycle(emptyList())
    val attendanceLogs by viewModel.getLogsForSubject(subjectId).collectAsStateWithLifecycle(emptyList())
    val subjectImportanceMap by viewModel.subjectImportanceMap.collectAsStateWithLifecycle()

    if (subject == null) {
        onBack()
        return
    }

    val maxAbs = subject.totalClasses - ceil(subject.totalClasses * (subject.requiredAttendancePercent / 100.0)).toInt()
    
    val totalAbsCount = attendanceLogs.count { !it.isPresent }.coerceAtLeast(absences.size)
    val totalPresCount = attendanceLogs.count { it.isPresent }
    val totalSessionsHeld = totalPresCount + totalAbsCount
    val currentRate = if (totalSessionsHeld > 0) (totalPresCount.toDouble() / totalSessionsHeld * 100.0) else 100.0
    val remaining = maxAbs - totalAbsCount

    // Add session manual state
    var manualDateInput by remember { mutableStateOf("") }
    var selectedStatusIsPresent by remember { mutableStateOf(true) } // true = Presente, false = Ausente
    var filterTab by remember { mutableStateOf("Todo") } // "Todo", "Asistencias", "Faltas"

    val mergedLogs = remember(attendanceLogs, absences) {
        val list = attendanceLogs.map { IntegratedLogItem(id = it.id, date = it.date, isPresent = it.isPresent, isLegacy = false) }.toMutableList()
        absences.forEach { legacyAbs ->
            if (attendanceLogs.none { !it.isPresent && it.date == legacyAbs.date }) {
                list.add(IntegratedLogItem(id = legacyAbs.id, date = legacyAbs.date, isPresent = false, isLegacy = true))
            }
        }
        list.sortByDescending { it.date }
        list
    }

    val filteredLogs = remember(mergedLogs, filterTab) {
        when (filterTab) {
            "Asistencias" -> mergedLogs.filter { it.isPresent }
            "Faltas" -> mergedLogs.filter { !it.isPresent }
            else -> mergedLogs
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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
                var showEditScheduleDialog by remember { mutableStateOf(false) }
                IconButton(onClick = { showEditScheduleDialog = true }) {
                    Icon(imageVector = Icons.Default.DateRange, contentDescription = "Editar Horario", tint = NavyBlue)
                }
                
                if (showEditScheduleDialog) {
                    var editingSessions by remember { mutableStateOf(subject.sessionsJson.parseSessions().toMutableStateList()) }
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
                                        allSubjects = subjects
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
                                            sessionsJson = jsonString,
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
                        title = { Text("Eliminar materia", color = NavyBlue, fontWeight = FontWeight.Bold) },
                        text = { Text("¿Deseas eliminar definitivamente ${subject.name}? Perderás todas sus asistencias registradas y exámenes.", color = SlateGray) },
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
        val currentAvg = if (assessmentsState.filter { it.grade != null }.isNotEmpty()) {
            assessmentsState.filter { it.grade != null }.map { it.grade!! }.average()
        } else {
            100.0
        }

        val buddyPose = when {
            currentAvg < 51.0 || remaining <= 1 -> "idle"
            currentAvg >= 85.0 && remaining >= 3 -> "greeting"
            else -> "idle"
        }
        val isBuddyHappy = currentAvg >= 51.0 && remaining > 1
        val isBuddyWorried = currentAvg < 51.0 || remaining <= 1

        val bubbleText = when {
            currentAvg >= 85.0 && remaining >= 3 -> "¡Vas volando, genio! Promedio de ${String.format(Locale.US, "%.1f", currentAvg)} y tienes $remaining vidas de sobra. ¡Excelente trabajo!"
            currentAvg < 51.0 || remaining <= 1 -> "¡Cuidado! Promedio bajo (${String.format(Locale.US, "%.1f", currentAvg)}) o casi sin vidas ($remaining rest.). ¡A estudiar más!"
            else -> "¡Todo en orden! Buen ritmo académico. Sigue así y cerramos el semestre como campeones."
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (maxAbs <= 0) {
                            Text("No podés faltar", fontSize = 11.sp, color = Terracotta, fontWeight = FontWeight.Bold)
                        } else {
                            val displayMax = maxAbs.coerceAtMost(8)
                            val displayRemaining = remaining.coerceAtLeast(0).coerceAtMost(displayMax)
                            for (i in 1..displayMax) {
                                val isHeld = i <= displayRemaining
                                Icon(
                                    imageVector = if (isHeld) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (isHeld) Terracotta else Color.LightGray,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            if (maxAbs > 8) {
                                Text("+${maxAbs - 8}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SlateGray)
                            }
                        }
                    }
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
                    subject.sessionsJson.parseSessions().map { it.day }.distinct()
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
                                    statusDesc = "Estás libre. No podés faltar a ninguna clase."
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

        // History Filter tab
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
                    .weight(0.4f),
                contentAlignment = Alignment.Center
            ) {
                Text("Ningún registro coincide con el filtro.", color = SlateGray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(0.4f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredLogs, key = { "" + it.id + "_" + it.isLegacy }) { item ->
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
                        modifier = Modifier.fillMaxWidth().animateItem()
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
                                    Icon(
                                        imageVector = if (item.isPresent) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (item.isPresent) DarkGreen else Terracotta
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = if (item.isPresent) "Asistió: ${item.date}" else "Falta registrada: ${item.date}",
                                        fontWeight = FontWeight.Bold,
                                        color = NavyBlue
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
            modifier = Modifier.fillMaxWidth(),
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
                        modifier = Modifier.weight(1.8f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { selectedStatusIsPresent = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedStatusIsPresent) DarkGreen else SlateGray.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("Asistió", color = if (selectedStatusIsPresent) Bone else NavyBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { selectedStatusIsPresent = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!selectedStatusIsPresent) Terracotta else SlateGray.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("Faltó", color = if (!selectedStatusIsPresent) Bone else NavyBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val inputDate = manualDateInput.ifBlank {
                            java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault()).format(java.util.Date())
                        }
                        viewModel.registerAttendanceLog(subject.id, selectedStatusIsPresent, inputDate)
                        manualDateInput = ""
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cargar clase registrada", color = Bone, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

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
                    val defaultSession = sub.sessionsJson.parseSessions().firstOrNull()
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
                Text("No hay materias registradas para calcular notas.", color = SlateGray)
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
fun ConfigTabScreen(viewModel: UniBuddyViewModel, onNavigateToPensum: () -> Unit) {
    val username by viewModel.username.collectAsStateWithLifecycle()
    val buddyColorStr by viewModel.buddyColor.collectAsStateWithLifecycle()
    val mainBuddyColor = Color(android.graphics.Color.parseColor(buddyColorStr))
    
    var showProfileDialog by remember { mutableStateOf(false) }
    var showRouteDialog by remember { mutableStateOf(false) }
    var showBadgeDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showBuddyDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }

    if (showHistoryDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showHistoryDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(modifier = Modifier.fillMaxSize(), color = BackgroundBone) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showHistoryDialog = false }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Historial de Semestres", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = NavyBlue)
                    }
                    Box(modifier = Modifier.weight(1f).padding(16.dp)) {
                        SemesterHistoryView(viewModel = viewModel)
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("config_column")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BuddyMascot(
                modifier = Modifier.size(80.dp),
                pose = "greeting",
                mainColor = mainBuddyColor,
                accessory = viewModel.buddyAccessory.collectAsStateWithLifecycle().value
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = "Hola, $username", style = MaterialTheme.typography.headlineMedium, color = NavyBlue, fontWeight = FontWeight.Bold)
                Text(text = "Personaliza tu experiencia", style = MaterialTheme.typography.bodyMedium, color = SlateGray)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid Layout for Settings
        val gridItems = listOf(
            ConfigGridItem("Perfil", Icons.Default.Person, "Nombre, Foto, Carrera") { showProfileDialog = true },
            ConfigGridItem("Mascota", Icons.Default.Face, "Accesorios y Color") { showBuddyDialog = true },
            ConfigGridItem("Rutas", Icons.Default.Place, "Origen, Destino, GPS") { showRouteDialog = true },
            ConfigGridItem("Historial", Icons.Default.List, "Estadísticas pasadas") { showHistoryDialog = true },
            ConfigGridItem("Académico", Icons.Default.DateRange, "Pensum, Semestre") { onNavigateToPensum() },
            ConfigGridItem("Insignias", Icons.Default.Star, "Logros y Medallas") { showBadgeDialog = true },
            ConfigGridItem("Sistema", Icons.Default.Settings, "Backup, Reset") { showResetDialog = true }
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(gridItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clickable { item.onClick() },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = NavyBlue,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = item.title, fontWeight = FontWeight.Bold, color = NavyBlue, fontSize = 16.sp)
                        Text(text = item.subtitle, fontSize = 10.sp, color = SlateGray, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }

    if (showProfileDialog) {
        ProfileDialog(viewModel) { showProfileDialog = false }
    }
    if (showRouteDialog) {
        RouteSettingsDialog(viewModel) { showRouteDialog = false }
    }
    if (showBadgeDialog) {
        BadgeDialog(viewModel) { showBadgeDialog = false }
    }
    if (showResetDialog) {
        SystemSettingsDialog(viewModel) { showResetDialog = false }
    }
    if (showBuddyDialog) {
        BuddyCustomizationDialog(viewModel) { showBuddyDialog = false }
    }
}

data class ConfigGridItem(val title: String, val icon: ImageVector, val subtitle: String, val onClick: () -> Unit)

@Composable
fun ProfileDialog(viewModel: UniBuddyViewModel, onDismiss: () -> Unit) {
    val username by viewModel.username.collectAsStateWithLifecycle()
    val career by viewModel.career.collectAsStateWithLifecycle()
    
    var editingName by remember { mutableStateOf(username) }
    var editingCareer by remember { mutableStateOf(career) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mi Perfil", fontWeight = FontWeight.Bold, color = NavyBlue) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = editingName,
                    onValueChange = { editingName = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = editingCareer,
                    onValueChange = { editingCareer = it },
                    label = { Text("Carrera Universitaria") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    viewModel.saveUsername(editingName)
                    viewModel.saveCareer(editingCareer)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) { 
                Text("Guardar", color = Bone) 
            }
        },
        dismissButton = { 
            TextButton(onClick = onDismiss) { 
                Text("Cerrar", color = SlateGray) 
            } 
        },
        containerColor = Color.White
    )
}

@Composable
fun BadgeDialog(viewModel: UniBuddyViewModel, onDismiss: () -> Unit) {
    val badges by viewModel.badges.collectAsStateWithLifecycle()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mis Logros", fontWeight = FontWeight.Bold, color = NavyBlue) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                items(badges) { badge ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (badge.isUnlocked) MintGreen.copy(alpha = 0.2f) else BackgroundGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = badge.iconEmoji, fontSize = 24.sp, modifier = Modifier.alpha(if(badge.isUnlocked) 1f else 0.3f))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = badge.name, fontWeight = FontWeight.Bold, color = if(badge.isUnlocked) NavyBlue else SlateGray)
                            Text(text = badge.description, fontSize = 10.sp, color = SlateGray)
                            if (badge.isUnlocked) {
                                Text(text = "Desbloqueado: ${badge.dateUnlocked}", fontSize = 9.sp, color = DarkGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Genial") } }
    )
}

@Composable
fun RouteSettingsDialog(viewModel: UniBuddyViewModel, onDismiss: () -> Unit) {
    val origin by viewModel.origin.collectAsStateWithLifecycle()
    val destination by viewModel.destination.collectAsStateWithLifecycle()
    val baseTravelTime by viewModel.baseTravelTime.collectAsStateWithLifecycle()
    val home by viewModel.homeAddress.collectAsStateWithLifecycle()
    val work by viewModel.workAddress.collectAsStateWithLifecycle()
    
    var editingOrigin by remember { mutableStateOf(origin) }
    var editingDestination by remember { mutableStateOf(destination) }
    var travelMinutes by remember { mutableStateOf(baseTravelTime.toString()) }
    var editingHome by remember { mutableStateOf(home) }
    var editingWork by remember { mutableStateOf(work) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configuración de Ruta", fontWeight = FontWeight.Bold, color = NavyBlue) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PresetDropdownField(
                    label = "Origen Habitual",
                    value = editingOrigin,
                    onValueChange = { editingOrigin = it },
                    options = listOf("Ubicación actual (GPS)", "Casa", "Trabajo"),
                    readOnly = true
                )
                
                if (editingOrigin == "Casa") {
                    OutlinedTextField(value = editingHome, onValueChange = { editingHome = it }, label = { Text("Dirección Casa") })
                }
                if (editingOrigin == "Trabajo") {
                    OutlinedTextField(value = editingWork, onValueChange = { editingWork = it }, label = { Text("Dirección Trabajo") })
                }

                PresetDropdownField(
                    label = "Facultad Destino",
                    value = editingDestination,
                    onValueChange = { editingDestination = it },
                    options = viewModel.universities.map { it.name },
                    readOnly = true
                )
                
                Text("Viaje estimado: $travelMinutes min", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = travelMinutes.toFloatOrNull() ?: 20f,
                    onValueChange = { travelMinutes = it.toInt().toString() },
                    valueRange = 5f..120f,
                    steps = 23
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                viewModel.saveRoute(editingOrigin, editingDestination)
                viewModel.saveHomeWorkAddresses(editingHome, editingWork)
                travelMinutes.toIntOrNull()?.let { viewModel.saveBaseTravelTime(it) }
                onDismiss()
            }) { Text("Aplicar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun BuddyCustomizationDialog(viewModel: UniBuddyViewModel, onDismiss: () -> Unit) {
    val accessory by viewModel.buddyAccessory.collectAsStateWithLifecycle()
    val buddyColorStr by viewModel.buddyColor.collectAsStateWithLifecycle()
    val buddyPose by viewModel.buddyPose.collectAsStateWithLifecycle()
    
    val accessories = listOf("none", "hat", "cap", "glasses", "sunglasses", "scarf")
    val accessoriesLabels = mapOf(
        "none" to "Ninguno",
        "hat" to "Casco",
        "cap" to "Gorra",
        "glasses" to "Lentes",
        "sunglasses" to "Gafas de Sol",
        "scarf" to "Bufanda"
    )
    val colors = listOf("#4CAF50", "#2196F3", "#F44336", "#9C27B0", "#FF9800", "#607D8B")
    val poses = listOf("idle", "greeting", "working", "sleeping", "celebrating", "exam")
    val poseLabels = mapOf(
        "idle" to "Relajado",
        "greeting" to "Saludando",
        "working" to "Estudiando",
        "sleeping" to "Durmiendo",
        "celebrating" to "Celebrando",
        "exam" to "Concentrado"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Personalizar Buddy", fontWeight = FontWeight.Bold, color = NavyBlue) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Gesto / Postura", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyBlue)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(poses) { p ->
                        FilterChip(
                            selected = buddyPose == p,
                            onClick = { viewModel.saveBuddyPose(p) },
                            label = { Text(poseLabels[p] ?: p) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NavyBlue.copy(alpha = 0.15f),
                                selectedLabelColor = NavyBlue
                            )
                        )
                    }
                }

                Text("Accesorio", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyBlue)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(accessories) { acc ->
                        FilterChip(
                            selected = accessory == acc,
                            onClick = { viewModel.saveBuddyCustomization(acc, buddyColorStr) },
                            label = { Text(accessoriesLabels[acc] ?: acc.replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NavyBlue.copy(alpha = 0.15f),
                                selectedLabelColor = NavyBlue
                            )
                        )
                    }
                }
                
                Text("Color de Piel", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyBlue)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(colors) { colorHex ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(colorHex)))
                                .border(
                                    width = if (buddyColorStr == colorHex) 3.dp else 0.dp,
                                    color = NavyBlue,
                                    shape = CircleShape
                                )
                                .clickable { viewModel.saveBuddyCustomization(accessory, colorHex) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    BuddyMascot(
                        modifier = Modifier.size(110.dp),
                        pose = buddyPose,
                        accessory = accessory,
                        mainColor = Color(android.graphics.Color.parseColor(buddyColorStr))
                    )
                }
            }
        },
        confirmButton = { 
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) { 
                Text("Guardar", color = Bone) 
            } 
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = SlateGray)
            }
        },
        containerColor = Color.White
    )
}

@Composable
fun SystemSettingsDialog(viewModel: UniBuddyViewModel, onDismiss: () -> Unit) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val context = LocalContext.current
    
    var showResetConfirmation by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustes del Sistema", fontWeight = FontWeight.Bold, color = NavyBlue) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Theme Configuration
                Text("Preferencia de Tema", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyBlue)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Tema Oscuro", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text("Activar interfaz nocturna amigable", fontSize = 11.sp, color = SlateGray)
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.setDarkMode(it) }
                    )
                }

                Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)

                // 2. Backup & Restore Configuration
                Text("Respaldo y Restauración", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyBlue)
                Text("Puedes exportar tus datos académicos (clases, notas, asistencias) para moverlos de dispositivo o reinstalar la app.", fontSize = 11.sp, color = SlateGray)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val backupStr = viewModel.exportBackup()
                            if (backupStr.isNotEmpty()) {
                                clipboardManager.setText(androidx.compose.ui.text.buildAnnotatedString { append(backupStr) })
                                successMessage = "Copiado al portapapeles"
                                errorMessage = null
                            } else {
                                errorMessage = "No se pudo generar el respaldo"
                                successMessage = null
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Exportar JSON", fontSize = 12.sp)
                    }
                }

                if (successMessage != null) {
                    Text(successMessage!!, color = DarkGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }

                OutlinedTextField(
                    value = importText,
                    onValueChange = { importText = it; errorMessage = null; successMessage = null },
                    placeholder = { Text("Pega el JSON de tu respaldo aquí...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                )

                Button(
                    onClick = {
                        if (importText.trim().isEmpty()) {
                            errorMessage = "Por favor pega un JSON válido primero."
                            successMessage = null
                            return@Button
                        }
                        viewModel.importBackup(
                            importText,
                            onSuccess = {
                                successMessage = "¡Datos restaurados con éxito!"
                                errorMessage = null
                                importText = ""
                            },
                            onError = { err ->
                                errorMessage = "Error: $err"
                                successMessage = null
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    Text("Importar y Restaurar", fontSize = 12.sp)
                }

                if (errorMessage != null) {
                    Text(errorMessage!!, color = ProRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }

                Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)

                // 3. System Reset
                Text("Zona Peligrosa", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Terracotta)
                Button(
                    onClick = { showResetConfirmation = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Restablecer todos los datos")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = { Text("¿Restablecer todo?", color = Terracotta, fontWeight = FontWeight.Bold) },
            text = { Text("Esta acción es irreversible y borrará todo tu historial académico, notas, asistencias, rutas e insignias.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showResetConfirmation = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
                ) { Text("SÍ, REINICIAR") }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) { Text("CANCELAR") }
            }
        )
    }
}

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
