package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

// Mascot Buddy Custom Vector Illustration
@Composable
fun BuddyMascot(
    modifier: Modifier = Modifier,
    isWorried: Boolean = false,
    isHappy: Boolean = true
) {
    Box(
        modifier = modifier
            .size(100.dp)
            .background(Color.White, shape = CircleShape)
            .border(2.dp, DarkGreen, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val w = size.width
            val h = size.height

            // Ears
            drawCircle(color = Color.White, radius = w * 0.15f, center = Offset(w * 0.25f, h * 0.25f))
            drawCircle(color = DarkGreen, radius = w * 0.15f, center = Offset(w * 0.25f, h * 0.25f), style = Stroke(width = 4f))
            drawCircle(color = Color(0xFFFFB7B2), radius = w * 0.08f, center = Offset(w * 0.25f, h * 0.25f))

            drawCircle(color = Color.White, radius = w * 0.15f, center = Offset(w * 0.75f, h * 0.25f))
            drawCircle(color = DarkGreen, radius = w * 0.15f, center = Offset(w * 0.75f, h * 0.25f), style = Stroke(width = 4f))
            drawCircle(color = Color(0xFFFFB7B2), radius = w * 0.08f, center = Offset(w * 0.75f, h * 0.25f))

            // Head Base
            drawCircle(color = Color.White, radius = w * 0.35f, center = Offset(w * 0.5f, h * 0.52f))
            drawCircle(color = DarkGreen, radius = w * 0.35f, center = Offset(w * 0.5f, h * 0.52f), style = Stroke(width = 4.0f))

            // Yellow Construction Helmet
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
            drawPath(path = helmetPath, color = DarkGreen, style = Stroke(width = 4.0f))

            // Helmet Top ridge
            drawRect(
                color = Color(0xFFF9C74F),
                topLeft = Offset(w * 0.43f, h * 0.12f),
                size = Size(w * 0.14f, h * 0.12f)
            )
            drawRect(
                color = DarkGreen,
                topLeft = Offset(w * 0.43f, h * 0.12f),
                size = Size(w * 0.14f, h * 0.12f),
                style = Stroke(width = 4.0f)
            )

            // Eyes
            val eyeRadius = w * 0.045f
            if (isWorried) {
                drawLine(color = DarkGreen, start = Offset(w * 0.35f, h * 0.48f), end = Offset(w * 0.43f, h * 0.52f), strokeWidth = 5f)
                drawLine(color = DarkGreen, start = Offset(w * 0.65f, h * 0.48f), end = Offset(w * 0.57f, h * 0.52f), strokeWidth = 5f)
            } else {
                drawCircle(color = DarkGreen, radius = eyeRadius, center = Offset(w * 0.38f, h * 0.48f))
                drawCircle(color = DarkGreen, radius = eyeRadius, center = Offset(w * 0.62f, h * 0.48f))
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
                color = DarkGreen,
                topLeft = Offset(w * 0.41f, h * 0.53f),
                size = Size(w * 0.18f, h * 0.13f),
                style = Stroke(width = 3.0f)
            )

            // Nose
            drawCircle(color = DarkGreen, radius = w * 0.04f, center = Offset(w * 0.5f, h * 0.56f))

            // Mouth
            if (isHappy) {
                val mouthPath = Path().apply {
                    moveTo(w * 0.45f, h * 0.59f)
                    quadraticTo(w * 0.5f, h * 0.64f, w * 0.55f, h * 0.59f)
                }
                drawPath(path = mouthPath, color = DarkGreen, style = Stroke(width = 3.0f))
            } else {
                drawLine(color = DarkGreen, start = Offset(w * 0.46f, h * 0.60f), end = Offset(w * 0.54f, h * 0.60f), strokeWidth = 3f)
            }
        }
    }
}

// Map Vector Drawing for Next Class Card Background
@Composable
fun StylizedMapBackground(modifier: Modifier = Modifier) {
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
        drawCircle(color = NavyBlue.copy(alpha = 0.15f), radius = 5.dp.toPx(), center = Offset(w * 0.85f, h * 0.28f))
    }
}

// Navigation screens
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Onboarding : Screen("onboarding", "Onboarding", Icons.Default.PlayArrow)
    object Inicio : Screen("inicio", "Inicio", Icons.Default.Home)
    object Asistencia : Screen("asistencia", "Asistencia", Icons.Default.CheckCircle)
    object Notas : Screen("notas", "Notas", Icons.Default.Star)
    object Config_Tab : Screen("configuracion_tab", "Configuración", Icons.Default.Settings)
}

@Composable
fun UniBuddyApp(viewModel: UniBuddyViewModel) {
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Inicio) }
    
    // Auxiliary state to view subject details (Asistencia)
    var selectedSubjectIdForDetails by remember { mutableStateOf<Int?>(null) }
    // Auxiliary state to view assessment list for a subject (Notas)
    var selectedSubjectIdForGrades by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(isOnboardingCompleted) {
        if (!isOnboardingCompleted) {
            currentScreen = Screen.Onboarding
        } else if (currentScreen == Screen.Onboarding) {
            currentScreen = Screen.Inicio
        }
    }

    Scaffold(
        bottomBar = {
            if (currentScreen != Screen.Onboarding) {
                NavigationBar(
                    containerColor = Color(0xFFF0F4F8),
                    modifier = Modifier.shadow(8.dp)
                ) {
                    val screens = listOf(Screen.Inicio, Screen.Asistencia, Screen.Notas, Screen.Config_Tab)
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
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                Screen.Onboarding -> {
                    OnboardingScreen(
                        viewModel = viewModel,
                        onFinished = {
                            viewModel.updateOnboardingStatus(true)
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
                        }
                    )
                }
                Screen.Asistencia -> {
                    if (selectedSubjectIdForDetails == null) {
                        AsistenciaOverviewScreen(
                            viewModel = viewModel,
                            onSubjectClick = { subjectId ->
                                selectedSubjectIdForDetails = subjectId
                            }
                        )
                    } else {
                        SubjectDetailsScreen(
                            viewModel = viewModel,
                            subjectId = selectedSubjectIdForDetails!!,
                            onBack = { selectedSubjectIdForDetails = null }
                        )
                    }
                }
                Screen.Notas -> {
                    if (selectedSubjectIdForGrades == null) {
                        GradesOverviewScreen(
                            viewModel = viewModel,
                            onSubjectClick = { subjectId ->
                                selectedSubjectIdForGrades = subjectId
                            }
                        )
                    } else {
                        SubjectGradesScreen(
                            viewModel = viewModel,
                            subjectId = selectedSubjectIdForGrades!!,
                            onBack = { selectedSubjectIdForGrades = null }
                        )
                    }
                }
                Screen.Config_Tab -> {
                    ConfigTabScreen(viewModel = viewModel)
                }
            }
        }
    }
}

// Helper Location Search field
@Composable
fun LocationSearchField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    viewModel: UniBuddyViewModel,
    modifier: Modifier = Modifier
) {
    var query by remember(value) { mutableStateOf(value) }
    var isFocused by remember { mutableStateOf(false) }
    val results by viewModel.locationSearchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearchingLocations.collectAsStateWithLifecycle()

    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                onValueChange(it)
                viewModel.searchLocations(it)
            },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused },
            singleLine = true,
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = DarkGreen)
                } else if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        query = ""
                        onValueChange("")
                        viewModel.clearLocationSearchResults()
                    }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DarkGreen, focusedLabelColor = DarkGreen)
        )

        if (isFocused && results.isNotEmpty() && query.length >= 2) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .heightIn(max = 200.dp)
                    .shadow(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                LazyColumn {
                    items(results) { res ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    query = res.displayName
                                    onValueChange(res.displayName)
                                    viewModel.clearLocationSearchResults()
                                }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = DarkGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = res.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = NavyBlue
                            )
                        }
                    }
                }
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
    var onboardingDays by remember { mutableStateOf(setOf("Lu", "Mi", "Vi")) }
    var onboardingBlock by remember { mutableStateOf("B2") }
    var onboardingCustomTime by remember { mutableStateOf("10:00 AM") }
    var subjectClassesTotal by remember { mutableStateOf("32") }
    var subjectMinPercent by remember { mutableStateOf("75") }
    var subjectClassroom by remember { mutableStateOf("Aula 304, Edificio B") }

    val subjects by viewModel.subjects.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBone)
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
                for (i in 1..3) {
                    val isActive = step == i
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(if (isActive) 32.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (isActive) DarkGreen else SlateGray.copy(alpha = 0.3f))
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
                        color = SlateGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Spacer(modifier = Modifier.height(32.dp))

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
                            focusedBorderColor = DarkGreen,
                            focusedLabelColor = DarkGreen
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
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Empezar", color = Bone, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = Bone)
                    }
                }
                2 -> {
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
                        text = "Configura tus rutas habituales para llegar a tiempo a la facultad.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    LocationSearchField(
                        label = "¿Desde dónde sales?",
                        value = originInput,
                        onValueChange = { originInput = it },
                        placeholder = "Ej: Casa, Trabajo",
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LocationSearchField(
                        label = "¿A qué facultad vas?",
                        value = destinationInput,
                        onValueChange = { destinationInput = it },
                        placeholder = "Ej: Ciencias Económicas",
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = baseTravelMinutes,
                        onValueChange = { baseTravelMinutes = it.filter { char -> char.isDigit() } },
                        label = { Text("Minutos promedio de viaje") },
                        leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = SlateGray) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkGreen,
                            focusedLabelColor = DarkGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (originInput.isNotBlank() && destinationInput.isNotBlank()) {
                                viewModel.saveRoute(originInput, destinationInput)
                                baseTravelMinutes.toIntOrNull()?.let { viewModel.saveBaseTravelTime(it) }
                            }
                            step = 3
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Siguiente", color = Bone, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            step = 3
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkGreen),
                        border = BorderStroke(1.5.dp, DarkGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Configurar más tarde", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                3 -> {
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
                        text = "Configura tus asignaturas para predecir tus asistencias y notas. ¡Ya te agregamos Álgebra y Física por defecto para empezar!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Bone),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Agregar otra materia (+)", style = MaterialTheme.typography.titleMedium, color = NavyBlue, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = subjectName,
                                onValueChange = { subjectName = it },
                                label = { Text("Nombre de la Materia") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DarkGreen, focusedLabelColor = DarkGreen)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            VisualSchedulePicker(
                                selectedDays = onboardingDays,
                                onDaysChanged = { onboardingDays = it },
                                selectedBlock = onboardingBlock,
                                onBlockChanged = { onboardingBlock = it },
                                customTime = onboardingCustomTime,
                                onCustomTimeChanged = { onboardingCustomTime = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = subjectClassesTotal,
                                    onValueChange = { subjectClassesTotal = it.filter { char -> char.isDigit() } },
                                    label = { Text("Clases totales") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DarkGreen, focusedLabelColor = DarkGreen)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedTextField(
                                    value = subjectMinPercent,
                                    onValueChange = { subjectMinPercent = it.filter { char -> char.isDigit() } },
                                    label = { Text("Asistencia mínima (%)") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DarkGreen, focusedLabelColor = DarkGreen)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (subjectName.isNotBlank() && onboardingDays.isNotEmpty()) {
                                        val computedSchedule = onboardingDays.joinToString(", ")
                                        val computedTime = when (onboardingBlock) {
                                            "B1" -> "07:00 AM"
                                            "B2" -> "08:40 AM"
                                            "B3" -> "10:20 AM"
                                            "B4" -> "12:45 PM"
                                            "B5" -> "02:25 PM"
                                            "B6" -> "04:05 PM"
                                            else -> onboardingCustomTime
                                        }
                                        viewModel.addSubject(
                                            name = subjectName,
                                            schedule = computedSchedule,
                                            time = computedTime,
                                            requiredAttendancePercent = subjectMinPercent.toIntOrNull() ?: 75,
                                            totalClasses = subjectClassesTotal.toIntOrNull() ?: 32,
                                            classroom = subjectClassroom
                                        )
                                        // Reset fields
                                        subjectName = ""
                                        onboardingDays = setOf("Lu", "Mi", "Vi")
                                        onboardingBlock = "B2"
                                        onboardingCustomTime = "10:00 AM"
                                        subjectClassesTotal = "32"
                                        subjectMinPercent = "75"
                                    }
                                },
                                modifier = Modifier.align(Alignment.End),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                            ) {
                                Text("Añadir", color = Bone)
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
                        Text("Finalizar", color = Bone, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 2. DASHBOARD SCREEN (Vista de Inicio)
@Composable
fun DashboardScreen(
    viewModel: UniBuddyViewModel,
    onNavigateToDetails: (Int) -> Unit,
    onNavigateToGrades: (Int) -> Unit,
    onConfigureRoute: () -> Unit
) {
    val username by viewModel.username.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val absences by viewModel.absences.collectAsStateWithLifecycle()
    val isRaining by viewModel.isRaining.collectAsStateWithLifecycle()
    val baseTravelTime by viewModel.baseTravelTime.collectAsStateWithLifecycle()
    val tripRecords by viewModel.tripRecords.collectAsStateWithLifecycle()
    val arrivalMarginPref by viewModel.arrivalMarginPreference.collectAsStateWithLifecycle()
    val attendanceLogs by viewModel.attendanceLogs.collectAsStateWithLifecycle()
    val assessments by viewModel.assessments.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // GPS real and simulated tracking state
    var locationPermissionGranted by remember { mutableStateOf(false) }
    var realGPSDistanceKm by remember { mutableStateOf<Double?>(null) }
    var selectedSimulatedDistanceMode by remember { mutableStateOf("real") } // "real", "lejos", "moderado", "cerca", "campus"

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
                    val destLat = -34.6037
                    val destLon = -58.3816
                    val targetLoc = android.location.Location("").apply {
                        latitude = destLat
                        longitude = destLon
                    }
                    val distanceMeters = loc.distanceTo(targetLoc)
                    realGPSDistanceKm = (distanceMeters / 1000.0)
                } else {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        locationManager.getCurrentLocation(
                            provider,
                            null,
                            context.mainExecutor
                        ) { location ->
                            if (location != null) {
                                val destLat = -34.6037
                                val destLon = -58.3816
                                val targetLoc = android.location.Location("").apply {
                                    latitude = destLat
                                    longitude = destLon
                                }
                                val distanceMeters = location.distanceTo(targetLoc)
                                realGPSDistanceKm = (distanceMeters / 1000.0)
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
    val nextClassTime = nextClass?.time ?: "10:00 AM"
    val nextClassRoom = nextClass?.classroom ?: "Aula sin definir"

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
    val currentDistanceToCollege = when (selectedSimulatedDistanceMode) {
        "lejos" -> 25.4
        "moderado" -> 12.1
        "cerca" -> 3.2
        "campus" -> 0.15
        else -> realGPSDistanceKm ?: 15.0
    }

    val liveDistanceTravelMinutes = ((currentDistanceToCollege * 1.8) + 4.0).toInt().coerceAtLeast(1)

    val locationBasedTravelMinutes = if (selectedSimulatedDistanceMode != "real" || realGPSDistanceKm != null) {
        liveDistanceTravelMinutes + (if (isRaining) 8 else 0) + mondayMargin + earlyMargin
    } else {
        baseTravelTimeSource + (if (isRaining) 10 else 0) + mondayMargin + earlyMargin
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

    val worstState = remember(subjects, absences) {
        var alertCount = 0
        var criticalCount = 0
        subjects.forEach { sub ->
            val subAbs = absences.filter { it.subjectId == sub.id }
            val maxAbs = sub.totalClasses - ceil(sub.totalClasses * (sub.requiredAttendancePercent / 100.0)).toInt()
            val remaining = maxAbs - subAbs.size
            if (remaining <= 0) criticalCount++
            else if (remaining <= 2) alertCount++
        }
        if (criticalCount > 0) "critico" else if (alertCount > 0) "atencion" else "normal"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_column")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Toolbar / Header
        item {
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
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD1E4FF)), // Elegant Light Blue
                        contentAlignment = Alignment.Center
                    ) {
                        BuddyMascot(
                            modifier = Modifier.fillMaxSize().padding(2.dp),
                            isHappy = true
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
                        Text(
                            text = "Hoy es un gran día para aprender",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateGray
                        )
                    }
                }
                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(1.dp, CircleShape)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Alerts",
                        tint = NavyBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Buddy's Tip Card (Consejo de Buddy)
        item {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Bone),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BuddyMascot(
                        modifier = Modifier.size(72.dp),
                        isWorried = (worstState == "critico" || worstState == "atencion"),
                        isHappy = (worstState == "normal")
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val adviceIcon = when (worstState) {
                                "critico" -> Icons.Default.Warning
                                "atencion" -> Icons.Default.Info
                                else -> Icons.Default.CheckCircle
                            }
                            val adviceColor = when (worstState) {
                                "critico" -> Terracotta
                                "atencion" -> Amber
                                else -> DarkGreen
                            }
                            Icon(imageVector = adviceIcon, contentDescription = null, tint = adviceColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Consejo de Buddy",
                                style = MaterialTheme.typography.labelLarge,
                                color = NavyBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val adviceText = when (worstState) {
                            "critico" -> "¡Atención! Tienes materias en riesgo por faltas. Revisa tu panel para registrar asistencias."
                            "atencion" -> "El parcial se está acercando. Repasa tus apuntes hoy para asegurar el promedio."
                            else -> "¡Excelente ritmo! Vas al día con tu asistencia. Sigue así y el semestre será pan comido."
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

        // Intelligent Attendance Decision / Indicator Card (¿Vale la pena ir hoy?)
        item {
            val examToday = remember(assessments) {
                assessments.find { ass ->
                    ass.grade == null && ass.examDate.trim().equals(currentDayCode, ignoreCase = true)
                }
            }

            val adviceData = remember(subjects, absences, attendanceLogs, assessments, isRaining, examToday) {
                val todayClasses = if (currentDayCode.isEmpty()) {
                    emptyList()
                } else {
                    subjects.filter { it.schedule.contains(currentDayCode, ignoreCase = true) }
                }

                if (todayClasses.isEmpty()) {
                    Triple(
                        "¡Día sin clases!",
                        "No tenés cursadas registradas para hoy. Buen día para descansar u organizar tus pendientes.",
                        "discrecional"
                    )
                } else {
                    var criticalSubjectName: String? = null
                    var lowestMargin = 999
                    for (sub in todayClasses) {
                        val subAbsTotal = absences.count { it.subjectId == sub.id } + attendanceLogs.count { it.subjectId == sub.id && !it.isPresent }
                        val maxAbs = sub.totalClasses - Math.ceil(sub.totalClasses * (sub.requiredAttendancePercent / 100.0)).toInt()
                        val remainingAbs = maxAbs - subAbsTotal
                        if (remainingAbs < lowestMargin) lowestMargin = remainingAbs
                        if (remainingAbs <= 1) {
                            criticalSubjectName = sub.name
                        }
                    }

                    if (examToday != null) {
                        val sub = subjects.find { it.id == examToday.subjectId }
                        Triple(
                            "🚨 ¡HOY RINDES EVALUACIÓN!",
                            "Hoy tienes programado el examen '${examToday.name}' para ${sub?.name ?: "tu materia"}. ¡Bajo ninguna circunstancia debes faltar hoy!",
                            "critico_examen"
                        )
                    } else if (criticalSubjectName != null) {
                        Triple(
                            "¡ASISTENCIA CRÍTICA!",
                            "Hoy tenés clases de $criticalSubjectName donde estás al límite de faltas permitidas. ¡Tenés que ir sí o sí!",
                            "critico"
                        )
                    } else if (isRaining) {
                        Triple(
                            "¿Faltamos? (Estadísticas seguras)",
                            "Hoy cursás ${todayClasses.joinToString { it.name }} pero tenés buen margen ($lowestMargin faltas disponibles) y el clima tiene pronóstico de lluvia. Si lo preferís, podés faltar sin riesgo.",
                            "discrecional_lluvia"
                        )
                    } else {
                        Triple(
                            "ASISTENCIA RECOMENDADA",
                            "Tenés clases de ${todayClasses.joinToString { it.name }}. Tu margen de faltas es cómodo ($lowestMargin restantes), pero ir te evitará perder el hilo.",
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
                                Text(
                                    text = "☔ Tránsito Lluvioso",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyBlue,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // HUGE TRAVEL ETA HUD BLOCK
                    val hudBgColor = when {
                        minutesDiff < 0 -> Color(0xFFFFDCDA) // Warn Crimson Pink
                        minutesDiff <= 10 -> Color(0xFFFFF3CD) // Warm Yellowish Amber
                        else -> Color(0xFFD4EDDA) // Easy Green
                    }
                    val hudBorderColor = when {
                        minutesDiff < 0 -> Terracotta
                        minutesDiff <= 10 -> Color(0xFF856404)
                        else -> DarkGreen
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = hudBgColor),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, hudBorderColor.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "REPORTE DE RUTA Y HORARIO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = hudBorderColor
                                )
                                Text(
                                    text = if (minutesDiff < 0) "⌛ DEMORADO" else "⏱️ A TIEMPO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = hudBorderColor
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val dynamicStatusLabel = when {
                                minutesDiff < 0 -> "⚠️ LLEGAS TARDE POR ${-minutesDiff} MIN"
                                minutesDiff <= 10 -> "🚨 MARGEN ESTRECHO DE $minutesDiff MIN"
                                else -> "✅ BIEN A TIEMPO (+ $minutesDiff MIN)"
                            }

                            Text(
                                text = dynamicStatusLabel,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = hudBorderColor
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Divider(color = hudBorderColor.copy(alpha = 0.15f), thickness = 1.dp)
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Recorrido de Viaje", fontSize = 11.sp, color = SlateGray)
                                    Text("$locationBasedTravelMinutes min", fontSize = 13.sp, color = NavyBlue, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Llegada Estimada", fontSize = 11.sp, color = SlateGray)
                                    Text(arrivalTimeStr, fontSize = 13.sp, color = NavyBlue, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Inicio Clase ($nextClassMateria)", fontSize = 11.sp, color = SlateGray)
                                    Text(nextClassTime, fontSize = 13.sp, color = NavyBlue, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Display summary of today's classes if any exist
                    val todayClasses = if (currentDayCode.isEmpty()) emptyList() else subjects.filter { it.schedule.contains(currentDayCode, ignoreCase = true) }
                    if (todayClasses.isNotEmpty()) {
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
                        todayClasses.forEach { sub ->
                            val subAbsTotal = absences.count { it.subjectId == sub.id } + attendanceLogs.count { it.subjectId == sub.id && !it.isPresent }
                            val maxAbs = sub.totalClasses - Math.ceil(sub.totalClasses * (sub.requiredAttendancePercent / 100.0)).toInt()
                            val remainingAbs = maxAbs - subAbsTotal
                            
                            val feasibilityLabel = when {
                                examToday?.subjectId == sub.id -> "EXAMEN HOY: NO FALTAR 🚫"
                                remainingAbs <= 0 -> "FALTAS AGOTADAS: IMPOSIBLE ❌"
                                remainingAbs == 1 -> "LÍMITE ALCANZADO: CRÍTICO ⚠️"
                                remainingAbs == 2 -> "RIESGOSO: PRECAUCIÓN 🟡"
                                else -> "SEGURO: MARGEN EXCELENTE ($remainingAbs restantes) 🟢"
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
                                        Text(
                                            text = "Horario: ${sub.time} | Aula: ${sub.classroom}",
                                            fontSize = 11.sp,
                                            color = SlateGray
                                        )
                                    }
                                    Text(
                                        text = feasibilityLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (remainingAbs <= 1 || examToday?.subjectId == sub.id) Terracotta else DarkGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Next class card (Próxima Clase)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Bone),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    StylizedMapBackground(modifier = Modifier.fillMaxSize())

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
                                Text("PROXIMA CLASE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateGray, letterSpacing = 1.sp)
                                Text(
                                    text = nextClassMateria,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = NavyBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Surface(
                                color = when (travelStatus) {
                                    "normal" -> MintGreen.copy(alpha = 0.2f)
                                    "advertencia" -> Amber.copy(alpha = 0.2f)
                                    else -> Terracotta.copy(alpha = 0.2f)
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
                                            .background(
                                                when (travelStatus) {
                                                    "normal" -> MintGreen
                                                    "advertencia" -> Amber
                                                    else -> Terracotta
                                                }
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = travelStatus.replaceFirstChar { it.uppercase() },
                                        color = when (travelStatus) {
                                            "normal" -> DarkGreen
                                            "advertencia" -> Color(0xFFB07219)
                                            else -> Terracotta
                                        },
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
                                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = SlateGray, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("$nextClassTime — Viaje: $locationBasedTravelMinutes min", fontSize = 13.sp, color = NavyBlue, fontWeight = FontWeight.Bold)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = SlateGray, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("$nextClassRoom • Clase en $remainingToClass min", fontSize = 12.sp, color = SlateGray)
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
            item {
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
        item {
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
        item {
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isNormal = arrivalMarginPref == "normal"
                        Button(
                            onClick = { viewModel.setArrivalMarginPreference("normal") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isNormal) DarkGreen else Color(0xFFF0F4F8),
                                contentColor = if (isNormal) Bone else SlateGray
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(44.dp).border(
                                1.dp, if (isNormal) Color.Transparent else Color(0xFFDCE3E9), RoundedCornerShape(12.dp)
                            )
                        ) {
                            Text("Normal (Por defecto)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        val isTemprano = arrivalMarginPref == "temprano"
                        Button(
                            onClick = { viewModel.setArrivalMarginPreference("temprano") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTemprano) DarkGreen else Color(0xFFF0F4F8),
                                contentColor = if (isTemprano) Bone else SlateGray
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(44.dp).border(
                                1.dp, if (isTemprano) Color.Transparent else Color(0xFFDCE3E9), RoundedCornerShape(12.dp)
                            )
                        ) {
                            Text("Temprano (+10m)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Weekly Activity Chart (Actividad Semanal)
        item {
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

                    val weekdays = listOf("L" to 0.45f, "M" to 0.65f, "X" to 0.90f, "J" to 0.25f, "V" to 0.60f)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weekdays.forEach { (day, fraction) ->
                            val isTodayX = day == "X" 
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight(fraction)
                                        .width(36.dp)
                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        .background(if (isTodayX) DarkGreen else SlateGray.copy(alpha = 0.5f))
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = day,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTodayX) DarkGreen else SlateGray
                                )
                            }
                        }
                    }
                }
            }
        }

        // 1. GPS Location and Distance Card
        item {
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Geographic Selection Mode Controls (Real vs Simulated)
                    Text(
                        text = "Probar distancias (Simulador de Ubicación):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            "real" to "GPS Real",
                            "lejos" to "Lejos",
                            "moderado" to "Medio",
                            "cerca" to "Cerca",
                            "campus" to "Campus"
                        ).forEach { (mode, label) ->
                            val isSelected = selectedSimulatedDistanceMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) DarkGreen else Bone)
                                    .clickable {
                                        selectedSimulatedDistanceMode = mode
                                        if (mode == "real") {
                                            requestRealGPSTracking()
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Bone else SlateGray
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Journey Stopwatch & Trip Records Analytics
        item {
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
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Bone, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Comenzar Viaje Casa ➔ Universidad", color = Bone, fontWeight = FontWeight.Bold)
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

        // Bento Grid Stats (Asistencia & Notas side-by-side)
        item {
            val totalInRisk = subjects.count { sub ->
                val subAbs = absences.filter { it.subjectId == sub.id }
                val maxAbs = sub.totalClasses - ceil(sub.totalClasses * (sub.requiredAttendancePercent / 100.0)).toInt()
                (maxAbs - subAbs.size) <= 1
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Attendance Card (Left Bento Box: Purple background #E8DEF8)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(145.dp)
                        .clickable { onNavigateToDetails(subjects.firstOrNull()?.id ?: 0) }
                        .border(1.dp, Color(0xFFDCE3E9), RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8DEF8)), 
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = NavyBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Column {
                            Text(
                                text = "Asistencia",
                                fontSize = 12.sp,
                                color = NavyBlue.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (totalInRisk == 0) "Al día" else "$totalInRisk en riesgo",
                                fontSize = 16.sp,
                                color = NavyBlue,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                // Grades Card (Right Bento Box: Blue-sky background #C2E7FF)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(145.dp)
                        .clickable { onNavigateToGrades(subjects.firstOrNull()?.id ?: 0) }
                        .border(1.dp, Color(0xFFDCE3E9), RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFC2E7FF)), 
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = NavyBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Column {
                            Text(
                                text = "Notas",
                                fontSize = 12.sp,
                                color = NavyBlue.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Exámenes",
                                fontSize = 16.sp,
                                color = NavyBlue,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
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
}

// Helper Subject Color Palette for Grid Cards
fun getSubjectColorPalette(subjectName: String): Pair<Color, Color> {
    val name = subjectName.lowercase()
    return when {
        name.contains("alge") || name.contains("álge") -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32)) // Soft Green
        name.contains("físi") || name.contains("fisi") -> Pair(Color(0xFFE3F2FD), Color(0xFF1565C0)) // Soft Blue
        name.contains("mate") -> Pair(Color(0xFFFFF3E0), Color(0xFFE65100)) // Soft Orange
        name.contains("quím") || name.contains("quim") -> Pair(Color(0xFFF3E5F5), Color(0xFF7B1FA2)) // Soft Purple
        name.contains("prog") || name.contains("sist") -> Pair(Color(0xFFE0F7FA), Color(0xFF00838F)) // Soft Cyan
        else -> Pair(Color(0xFFFFEBEE), Color(0xFFC62828)) // Soft Red / Coral
    }
}

@Composable
fun SubjectGridItem(
    sub: Subject,
    absences: List<Absence>,
    attendanceLogs: List<AttendanceLog>,
    viewModel: UniBuddyViewModel,
    onSubjectClick: (Int) -> Unit
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

    val colors = getSubjectColorPalette(sub.name)
    val bgColor = colors.first
    val accentColor = colors.second

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSubjectClick(sub.id) }
            .shadow(4.dp, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                        text = sub.classroom,
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
                text = "${sub.schedule} • ${sub.time}",
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Asistido: $subPresCount",
                    fontSize = 10.sp,
                    color = SlateGray
                )
                Text(
                    text = if (remaining > 0) "$remaining de $maxAbs" else "Crítico",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCritical) Terracotta else NavyBlue
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick register assist Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = { viewModel.registerAttendanceLog(sub.id, isPresent = true) },
                    modifier = Modifier
                        .weight(1.1f)
                        .height(34.dp)
                        .background(accentColor, RoundedCornerShape(8.dp))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Asistí", tint = Bone, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Asistí", fontSize = 10.sp, color = Bone, fontWeight = FontWeight.Bold)
                    }
                }

                IconButton(
                    onClick = { viewModel.registerAttendanceLog(sub.id, isPresent = false) },
                    modifier = Modifier
                        .weight(0.9f)
                        .height(34.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, Terracotta, RoundedCornerShape(8.dp))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Falté", tint = Terracotta, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Falté", fontSize = 10.sp, color = Terracotta, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 3. ASISTENCIA OVERVIEW SCREEN
@Composable
fun AsistenciaOverviewScreen(viewModel: UniBuddyViewModel, onSubjectClick: (Int) -> Unit) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val absences by viewModel.absences.collectAsStateWithLifecycle()
    val attendanceLogs by viewModel.attendanceLogs.collectAsStateWithLifecycle()
    val weeklyStreak by viewModel.weeklyStreak.collectAsStateWithLifecycle()

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
                colors = CardDefaults.cardColors(containerColor = DarkGreen),
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
                            drawArc(
                                color = MintGreen,
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
                    var newSubClassroom by remember { mutableStateOf("Aula 102, Edificio B") }
                    var newSubClassesTotal by remember { mutableStateOf("32") }
                    var newSubMinPercent by remember { mutableStateOf("75") }
                    
                    var newSubDays by remember { mutableStateOf(setOf("Lu", "Mi")) }
                    var newSubBlock by remember { mutableStateOf("B2") }
                    var newSubCustomTime by remember { mutableStateOf("10:00 AM") }

                    AlertDialog(
                        onDismissRequest = { showAddSubjectDialog = false },
                        title = { Text("Nueva Materia", color = NavyBlue, fontWeight = FontWeight.Bold) },
                        text = {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item {
                                    OutlinedTextField(
                                        value = newSubName,
                                        onValueChange = { newSubName = it },
                                        label = { Text("Nombre de la Materia") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DarkGreen, focusedLabelColor = DarkGreen)
                                    )
                                }
                                
                                item {
                                    VisualSchedulePicker(
                                        selectedDays = newSubDays,
                                        onDaysChanged = { newSubDays = it },
                                        selectedBlock = newSubBlock,
                                        onBlockChanged = { newSubBlock = it },
                                        customTime = newSubCustomTime,
                                        onCustomTimeChanged = { newSubCustomTime = it },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                item {
                                    OutlinedTextField(
                                        value = newSubClassroom,
                                        onValueChange = { newSubClassroom = it },
                                        label = { Text("Aula / Edificio") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DarkGreen, focusedLabelColor = DarkGreen)
                                    )
                                }

                                item {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = newSubClassesTotal,
                                            onValueChange = { newSubClassesTotal = it.filter { char -> char.isDigit() } },
                                            label = { Text("Clases totales") },
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DarkGreen, focusedLabelColor = DarkGreen)
                                        )
                                        OutlinedTextField(
                                            value = newSubMinPercent,
                                            onValueChange = { newSubMinPercent = it.filter { char -> char.isDigit() } },
                                            label = { Text("Mín. Asistencia %") },
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DarkGreen, focusedLabelColor = DarkGreen)
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (newSubName.isNotBlank() && newSubDays.isNotEmpty()) {
                                        val computedSchedule = newSubDays.joinToString(", ")
                                        val computedTime = when (newSubBlock) {
                                            "B1" -> "07:00 AM"
                                            "B2" -> "08:40 AM"
                                            "B3" -> "10:20 AM"
                                            "B4" -> "12:45 PM"
                                            "B5" -> "02:25 PM"
                                            "B6" -> "04:05 PM"
                                            else -> newSubCustomTime
                                        }
                                        viewModel.addSubject(
                                            name = newSubName,
                                            schedule = computedSchedule,
                                            time = computedTime,
                                            requiredAttendancePercent = newSubMinPercent.toIntOrNull() ?: 75,
                                            totalClasses = newSubClassesTotal.toIntOrNull() ?: 32,
                                            classroom = newSubClassroom
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
            chunkedSubjects.forEach { pair ->
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        pair.forEach { sub ->
                            Box(modifier = Modifier.weight(1f)) {
                                SubjectGridItem(
                                    sub = sub,
                                    absences = absences,
                                    attendanceLogs = attendanceLogs,
                                    viewModel = viewModel,
                                    onSubjectClick = onSubjectClick
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
}

data class IntegratedLogItem(val id: Int, val date: String, val isPresent: Boolean, val isLegacy: Boolean)

// 4. DETALLE MATERIA SCREEN
@Composable
fun SubjectDetailsScreen(viewModel: UniBuddyViewModel, subjectId: Int, onBack: () -> Unit) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val subject = subjects.find { it.id == subjectId }
    val absences by viewModel.getAbsencesForSubject(subjectId).collectAsStateWithLifecycle(emptyList())
    val attendanceLogs by viewModel.getLogsForSubject(subjectId).collectAsStateWithLifecycle(emptyList())

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = NavyBlue)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = subject.name, style = MaterialTheme.typography.headlineMedium, color = NavyBlue, fontWeight = FontWeight.Bold)
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
                            Text("Confirmar", color = Bone)
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

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Bone),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "De ${subject.totalClasses} clases en total en el cuatrimestre, podés faltar hasta $maxAbs veces para conservar el ${subject.requiredAttendancePercent}% de asistencia mínima.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = NavyBlue,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Clases Asistidas", fontSize = 12.sp, color = SlateGray)
                        Text("$totalPresCount clases", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DarkGreen)
                    }
                    Column {
                        Text("Tus Faltas", fontSize = 12.sp, color = SlateGray)
                        Text("$totalAbsCount de $maxAbs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Terracotta)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Permitidas", fontSize = 12.sp, color = SlateGray)
                        Text(
                            text = if (remaining >= 0) "$remaining restantes" else "Excedido por ${-remaining}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (remaining <= 0) Terracotta else DarkGreen
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Asistencia actual: ${String.format(Locale.US, "%.1f", currentRate)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (currentRate >= subject.requiredAttendancePercent) DarkGreen else Terracotta
                )
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
                items(filteredLogs) { item ->
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
                            IconButton(
                                onClick = {
                                    if (item.isLegacy) {
                                        viewModel.deleteAbsence(item.id)
                                    } else {
                                        viewModel.deleteAttendanceLog(item.id)
                                    }
                                }
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = SlateGray)
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

    val targetApprove = 6.0
    val missingAmount = targetApprove - currentWeighted
    val gradeNeeded = if (remainingPercentage > 0) {
        (missingAmount / (remainingPercentage / 100.0)).coerceIn(0.0, 10.0)
    } else 0.0

    val roundedGrade = String.format(Locale.US, "%.1f", gradeNeeded)
    val isImpossible = gradeNeeded > 10.0

    val colors = getSubjectColorPalette(sub.name)
    val bgColor = colors.first
    val accentColor = colors.second

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(sub.id) }
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
                    Text(
                        text = sub.classroom,
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
                progress = (currentWeighted / 10.0).toFloat().coerceIn(0f, 1f),
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
                text = "Nota acumulada: ${String.format(Locale.US, "%.1f", currentWeighted)} / 10",
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
fun GradesOverviewScreen(viewModel: UniBuddyViewModel, onSubjectClick: (Int) -> Unit) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val allAssessments by viewModel.assessments.collectAsStateWithLifecycle(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Mis Notas", style = MaterialTheme.typography.headlineLarge, color = NavyBlue, fontWeight = FontWeight.Bold)
        Text(text = "Un resumen detallado del rendimiento de tus exámenes y notas académicas.", style = MaterialTheme.typography.bodyMedium, color = SlateGray)

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
            val chunkedSubjects = remember(subjects) { subjects.chunked(2) }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(chunkedSubjects) { rowSubjects ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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

    var simulatedExamGrade by remember { mutableStateOf(5.0f) }

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
        completedExams.map { it.grade!! }.average()
    } else {
        null
    }

    val historicEfficiencyPercent = if (historicAverage != null) {
        (historicAverage / 10.0) * 100.0
    } else {
        0.0
    }

    // Min grade needed on remaining components to reach exactly 6.0 (passing mark)
    val neededGradeOnRemaining = if (remainingPercentageToAcknowledge > 0.0) {
        ((6.0 - currentWeighted) / (remainingPercentageToAcknowledge / 100.0))
    } else {
        0.0
    }

    val finalGradeSimulationForecast = if (historicAverage != null) {
        currentWeighted + (historicAverage * (remainingPercentageToAcknowledge / 100.0))
    } else {
        6.0
    }

    val probabilityPercentageAndDescription = when {
        currentWeighted >= 6.0 -> Pair(100, "Asegurada (Materia Aprobada)")
        neededGradeOnRemaining > 10.0 -> Pair(0, "Crítica / Inalcanzable (Firma regular requerida)")
        neededGradeOnRemaining <= 0.0 -> Pair(100, "100% (Aprobado)")
        else -> {
            if (historicAverage != null) {
                when {
                    neededGradeOnRemaining <= 1.0 -> Pair(99, "Muy Alta")
                    neededGradeOnRemaining <= historicAverage - 1.5 -> Pair(90, "Alta")
                    neededGradeOnRemaining <= historicAverage + 0.8 -> Pair(70, "Media (Alineada a tus notas)")
                    neededGradeOnRemaining <= historicAverage + 2.0 -> Pair(35, "Baja (Requiere sobreesfuerzo)")
                    else -> Pair(12, "Muy Baja / Difícil")
                }
            } else {
                when {
                    neededGradeOnRemaining <= 4.0 -> Pair(95, "Muy Alta")
                    neededGradeOnRemaining <= 6.0 -> Pair(80, "Alta")
                    neededGradeOnRemaining <= 8.0 -> Pair(45, "Media")
                    neededGradeOnRemaining <= 9.5 -> Pair(20, "Baja")
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
            items(assessments) { ass ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                                color = DarkGreen,
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, Bone)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Registrar Examen Realizado/Pendiente", style = MaterialTheme.typography.titleMedium, color = NavyBlue, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = examName,
                        onValueChange = { examName = it },
                        label = { Text("Nombre del examen") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DarkGreen, focusedLabelColor = DarkGreen)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Día programado para el examen", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Lu" to "L", "Ma" to "M", "Mi" to "M", "Ju" to "J", "Vi" to "V", "Sá" to "S").forEach { (code, letter) ->
                            val isSelected = examDay == code
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) DarkGreen else Color(0xFFF1F3F4))
                                    .clickable { examDay = code },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(letter, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Bone else SlateGray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = examGrade,
                            onValueChange = { examGrade = it },
                            label = { Text("Nota (ej: 7.5)") },
                            placeholder = { Text("Vacío si pendiente") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DarkGreen, focusedLabelColor = DarkGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = examPercent,
                            onValueChange = { examPercent = it.filter { char -> char.isDigit() } },
                            label = { Text("Porcentaje (%)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DarkGreen, focusedLabelColor = DarkGreen)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (examName.isNotBlank() && examPercent.isNotBlank()) {
                                val percentVal = examPercent.toDoubleOrNull() ?: 25.0
                                val gradeVal = examGrade.toDoubleOrNull()
                                viewModel.addAssessment(subject.id, examName, gradeVal, percentVal, examDay)
                                examName = ""
                                examGrade = ""
                                examPercent = "25"
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                    ) {
                        Text("Registrar", color = Bone)
                    }
                }
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
                                        text = if (currentWeighted >= 6.0) {
                                            "¡Ya Aprobado!"
                                        } else if (neededGradeOnRemaining > 10.0) {
                                            "Inalcanzable (Requiere >10)"
                                        } else {
                                            String.format(Locale.US, "%.1f sobre 10", neededGradeOnRemaining.coerceAtLeast(0.0))
                                        },
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (neededGradeOnRemaining > 7.5) Terracotta else DarkGreen
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (neededGradeOnRemaining > 7.5) Terracotta.copy(alpha = 0.15f) else MintGreen.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (neededGradeOnRemaining > 7.5) Icons.Default.Warning else Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (neededGradeOnRemaining > 7.5) Terracotta else DarkGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (currentWeighted >= 6.0) {
                                    "Felicitaciones: ya acumulaste los 6.0 puntos ponderados mínimos."
                                } else if (neededGradeOnRemaining > 10.0) {
                                    "Incluso obteniendo 10 en lo restante, tu promedio máximo será de ${String.format(Locale.US, "%.2f", currentWeighted + remainingPercentage/10.0)}."
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
                                    color = if (finalGradeSimulationForecast >= 6.0) DarkGreen else Terracotta
                                )
                            }
                            Text(
                                text = if (finalGradeSimulationForecast >= 6.0) "Aprobaría" else "No alcanzaría",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (finalGradeSimulationForecast >= 6.0) DarkGreen else Terracotta,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (finalGradeSimulationForecast >= 6.0) MintGreen.copy(alpha = 0.1f) else Terracotta.copy(alpha = 0.1f))
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
                            Text("Probabilidad de Aprobación", fontSize = 12.sp, color = NavyBlue, fontWeight = FontWeight.Bold)
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
                            valueRange = 1.0f..10.0f,
                            steps = 18,
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
                                color = if (simulatedAverage >= 6.0) DarkGreen else Terracotta,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// 7. CONFIGURACION TAB SCREEN
@Composable
fun ConfigTabScreen(viewModel: UniBuddyViewModel) {
    val username by viewModel.username.collectAsStateWithLifecycle()
    val origin by viewModel.origin.collectAsStateWithLifecycle()
    val destination by viewModel.destination.collectAsStateWithLifecycle()
    val baseTravelTime by viewModel.baseTravelTime.collectAsStateWithLifecycle()
    val googleMapsApiKey by viewModel.googleMapsApiKey.collectAsStateWithLifecycle()

    var editingUsername by remember { mutableStateOf(username) }
    var editingOrigin by remember { mutableStateOf(origin) }
    var editingDestination by remember { mutableStateOf(destination) }
    var editingTravelMinutes by remember { mutableStateOf(baseTravelTime.toString()) }
    var editingGoogleMapsApiKey by remember { mutableStateOf(googleMapsApiKey) }

    var feedbackMsg by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("config_column")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = "Configuración", style = MaterialTheme.typography.headlineLarge, color = NavyBlue, fontWeight = FontWeight.Bold)
            Text(text = "Ajusta tus parámetros habituales de la aplicación.", style = MaterialTheme.typography.bodyMedium, color = SlateGray)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Bone),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Datos del Alumno", style = MaterialTheme.typography.titleMedium, color = NavyBlue, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editingUsername,
                        onValueChange = { editingUsername = it },
                        label = { Text("Nombre del alumno") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DarkGreen, focusedLabelColor = DarkGreen)
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Bone),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Configuración de Rutas", style = MaterialTheme.typography.titleMedium, color = NavyBlue, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    LocationSearchField(
                        label = "Origen habitual",
                        value = editingOrigin,
                        onValueChange = { editingOrigin = it },
                        placeholder = "Ej: Casa, Trabajo",
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LocationSearchField(
                        label = "Facultad de Destino",
                        value = editingDestination,
                        onValueChange = { editingDestination = it },
                        placeholder = "Ej: Ciencias Económicas",
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editingTravelMinutes,
                        onValueChange = { editingTravelMinutes = it.filter { char -> char.isDigit() } },
                        label = { Text("Minutos de viaje estimados") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DarkGreen, focusedLabelColor = DarkGreen)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editingGoogleMapsApiKey,
                        onValueChange = { editingGoogleMapsApiKey = it },
                        label = { Text("Google Maps API Key (Opcional)") },
                        placeholder = { Text("Para buscar ubicaciones vía Google API") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DarkGreen, focusedLabelColor = DarkGreen)
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    viewModel.saveUsername(editingUsername)
                    viewModel.saveRoute(editingOrigin, editingDestination)
                    viewModel.saveGoogleMapsApiKey(editingGoogleMapsApiKey)
                    editingTravelMinutes.toIntOrNull()?.let { viewModel.saveBaseTravelTime(it) }
                    feedbackMsg = "¡Datos de configuración guardados correctamente!"
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar Cambios", color = Bone, fontWeight = FontWeight.Bold)
            }
        }

        if (feedbackMsg.isNotEmpty()) {
            item {
                Surface(
                    color = MintGreen.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = feedbackMsg,
                        modifier = Modifier.padding(12.dp),
                        color = DarkGreen,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, Bone)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Gestión de Datos", style = MaterialTheme.typography.titleMedium, color = NavyBlue, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            feedbackMsg = "¡Backup local generado! 'unibuddy_backup.json' guardado localmente."
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, SlateGray.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Exportar / Respaldar Datos", color = NavyBlue)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Restablecer todos los datos", color = Bone)
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        editingUsername = "Estudiante"
                        editingOrigin = "Casa"
                        editingDestination = "Facultad"
                        editingTravelMinutes = "25"
                        showDialog = false
                        feedbackMsg = "Se han restablecido los datos de fábrica de la aplicación."
                    }
                ) {
                    Text("SÍ, BORRAR TODO", color = Terracotta, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("CANCELAR", color = NavyBlue)
                }
            },
            title = {
                Text("¿Confirmar reinstalación?", color = NavyBlue, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Esta acción eliminará todas las materias con su respectivo historial de asistencia y exámenes. No se puede deshacer.", color = SlateGray)
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
