package com.aistudio.unibuddy.qywvsp.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.unibuddy.qywvsp.ui.components.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aistudio.unibuddy.qywvsp.ui.theme.*
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin

// Objectives Data Models
data class FocusObjective(val id: String, val title: String, val isCompleted: Boolean)

fun String.parseObjectives(): List<FocusObjective> {
    val list = mutableListOf<FocusObjective>()
    if (this.isBlank() || this == "[]") return list
    try {
        val arr = JSONArray(this)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                FocusObjective(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    title = obj.getString("title"),
                    isCompleted = obj.getBoolean("isCompleted")
                )
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

fun List<FocusObjective>.toObjectivesJsonString(): String {
    val arr = JSONArray()
    this.forEach { obj ->
        val item = JSONObject()
        item.put("id", obj.id)
        item.put("title", obj.title)
        item.put("isCompleted", obj.isCompleted)
        arr.put(item)
    }
    return arr.toString()
}

// Session Record Models
data class FocusSessionRecord(val date: String, val duration: Int, val label: String, val timeOfDay: String = "", val interrupted: Boolean = false)

fun String.parseSessionsHistory(): List<FocusSessionRecord> {
    val list = mutableListOf<FocusSessionRecord>()
    if (this.isBlank() || this == "[]") return list
    try {
        val arr = JSONArray(this)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                FocusSessionRecord(
                    date = obj.getString("date"),
                    duration = obj.getInt("duration"),
                    label = obj.optString("label", "Estudio"),
                    timeOfDay = obj.optString("timeOfDay", ""),
                    interrupted = obj.optBoolean("interrupted", false)
                )
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

fun List<FocusSessionRecord>.toSessionsJsonString(): String {
    val arr = JSONArray()
    this.forEach { rec ->
        val item = JSONObject()
        item.put("date", rec.date)
        item.put("duration", rec.duration)
        item.put("label", rec.label)
        arr.put(item)
    }
    return arr.toString()
}

data class Soundscape(
    val id: String,
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val description: String,
    val baseFrequency: Float,
    val accentColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusModeScreen(viewModel: UniBuddyViewModel) {
    val hapticFeedback = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

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

    // Observe focus state from persistent settings via ViewModel
    val objectivesJson by viewModel.focusObjectivesJson.collectAsStateWithLifecycle()
    val sessionsHistoryJson by viewModel.focusSessionsHistoryJson.collectAsStateWithLifecycle()

    val absences by viewModel.absences.collectAsStateWithLifecycle()
    val assessments by viewModel.assessments.collectAsStateWithLifecycle()
    val currentWeek by viewModel.currentWeekOfSemester.collectAsStateWithLifecycle()

    // Determine the active Corte/Period (C1 if week <= 8, C2 if week > 8)
    val currentPeriodAssessments = remember(assessments, currentWeek) {
        assessments.filter {
            val isC1 = it.name.contains("C1", ignoreCase = true) || it.name.contains("U1", ignoreCase = true)
            if (currentWeek <= 8) isC1 else !isC1
        }
    }

    val stressLevel = remember(currentPeriodAssessments, absences) {
        (currentPeriodAssessments.count { it.grade == null } * 15f + absences.size * 5f).coerceIn(0f, 100f)
    }
    val stressStatus = remember(stressLevel) {
        when {
            stressLevel > 70f -> "Crítico"
            stressLevel > 40f -> "Elevado"
            else -> "Saludable"
        }
    }

    var animateChart by remember { mutableStateOf(false) }
    val chartProgress by animateFloatAsState(
        targetValue = if (animateChart) 1f else 0f,
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "chart_growth"
    )
    LaunchedEffect(Unit) {
        animateChart = true
    }

    val objectives = remember(objectivesJson) { objectivesJson.parseObjectives() }
    val sessionsHistory = remember(sessionsHistoryJson) { sessionsHistoryJson.parseSessionsHistory() }

    // Timer States
    var isWorkMode by remember { mutableStateOf(true) }
    var workMinutes by remember { mutableStateOf(25) }
    var breakMinutes by remember { mutableStateOf(5) }
    var timeLeft by remember { mutableStateOf(workMinutes * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Soundscapes state
    val soundscapes = listOf(
        Soundscape("rain", "Lluvia", Icons.Default.Notifications, "Lluvia de medianoche", 0.5f, ProBlue),
        Soundscape("cafe", "Café", Icons.Default.Person, "Ambiente universitario", 1.2f, Amber),
        Soundscape("library", "Biblioteca", Icons.Default.List, "Estudio en silencio", 0.3f, DarkGreen),
        Soundscape("space", "Cosmos", Icons.Default.Star, "Frecuencia profunda", 0.1f, NavyBlue)
    )
    var selectedSoundscape by remember { mutableStateOf<Soundscape?>(null) }
    var isPlayingAmbient by remember { mutableStateOf(false) }

    // Syncing UI timer state with Service
    val svcTimeLeft by PomodoroState.timeLeftSeconds.collectAsStateWithLifecycle()
    val svcIsRunning by PomodoroState.isRunning.collectAsStateWithLifecycle()
    val svcIsPaused by PomodoroState.isPaused.collectAsStateWithLifecycle()
    val svcIsWork by PomodoroState.isWorkMode.collectAsStateWithLifecycle()

    LaunchedEffect(svcTimeLeft, svcIsRunning, svcIsPaused, svcIsWork) {
        if (svcIsRunning) {
            isRunning = true
            timeLeft = svcTimeLeft
            isWorkMode = svcIsWork
        } else if (svcIsPaused) {
            isRunning = false
            timeLeft = svcTimeLeft
            isWorkMode = svcIsWork
        }
    }

    // Checking for timer expiration
    LaunchedEffect(isRunning, timeLeft) {
        if (timeLeft == 0 && isRunning) {
            try {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            } catch (e: Exception) {}
            isRunning = false
            
            if (isWorkMode) {
                // Record session in history
                val formatter = SimpleDateFormat("dd MMM", Locale.getDefault())
                val todayStr = formatter.format(Date())
                val currentTask = objectives.find { !it.isCompleted }?.title ?: "Estudio general"
                
                val newRecord = FocusSessionRecord(todayStr, workMinutes, currentTask)
                val updatedHistory = listOf(newRecord) + sessionsHistory
                viewModel.saveFocusSessionsHistory(updatedHistory.toSessionsJsonString())
                
                // Reward Buddy XP
                viewModel.addBuddyXp(workMinutes)
            }

            // Auto switch modes
            isWorkMode = !isWorkMode
            timeLeft = if (isWorkMode) workMinutes * 60 else breakMinutes * 60
        }
    }

    // Mascot Quote based on current timer mode & state
    val mascotQuote = remember(isRunning, isWorkMode, timeLeft, objectives) {
        val incompleteTask = objectives.firstOrNull { !it.isCompleted }?.title
        when {
            !isRunning && timeLeft == (if (isWorkMode) workMinutes * 60 else breakMinutes * 60) -> {
                "¡Hola! ¿Listo para sumergirte en el estudio hoy?"
            }
            isRunning && isWorkMode -> {
                if (incompleteTask != null) {
                    "Enfocado en: \"$incompleteTask\". ¡Tú puedes lograrlo!"
                } else {
                    "¡Increíble nivel de enfoque! Sigue adelante."
                }
            }
            !isRunning && isWorkMode -> {
                "Pausado un momento. Respira hondo y continuemos cuando estés listo."
            }
            isRunning && !isWorkMode -> {
                "¡Toma un merecido descanso! Estírate o toma agua."
            }
            else -> "¡Hagamos que cada minuto cuente!"
        }
    }

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)
    val totalSeconds = if (isWorkMode) workMinutes * 60 else breakMinutes * 60
    val progress = if (totalSeconds > 0) 1f - (timeLeft.toFloat() / totalSeconds.toFloat()) else 0f

    // Infinitely pulsing scale when timer is running
    val infiniteTransition = rememberInfiniteTransition(label = "timer_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRunning) 1.04f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Soundscape Wave Animation Phase
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * java.lang.Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    // Interactive custom ambient wave haptic vibration & Audio Engine
    LaunchedEffect(isPlayingAmbient, selectedSoundscape) {
        if (isPlayingAmbient && selectedSoundscape != null) {
            AmbientAudioEngine.playSoundscape(selectedSoundscape!!.id)
        } else {
            AmbientAudioEngine.stop()
        }
    }
    
    // Stop audio when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            AmbientAudioEngine.stop()
        }
    }



    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. MASCOT TALK BUBBLE HEADER
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, NavyBlue.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        BuddyMascot(
                            modifier = Modifier.fillMaxSize(),
                            isHappy = isWorkMode && isRunning,
                            pose = if (isWorkMode) (if (isRunning) "working" else "idle") else "sleeping",
                            accessory = "glasses"
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "UniBuddy",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGray
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            val buddyXp by viewModel.buddyXp.collectAsStateWithLifecycle()
                            Surface(
                                color = ProBlue.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = ProBlue, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("$buddyXp XP", fontSize = 10.sp, color = ProBlue, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = mascotQuote,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = NavyBlue,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // 2. MAIN TIMER CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, NavyBlue.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // MODE SWITCHER
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color(0xFFECEFF1))
                            .padding(4.dp)
                    ) {
                        Button(
                            onClick = {
                                isWorkMode = true
                                timeLeft = workMinutes * 60
                                isRunning = false
                                context.startService(android.content.Intent(context, PomodoroService::class.java).apply {
                                    action = PomodoroService.ACTION_STOP
                                })
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isWorkMode) ProBlue else Color.Transparent,
                                contentColor = if (isWorkMode) Color.White else NavyBlue
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                            shape = RoundedCornerShape(28.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Estudiar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                isWorkMode = false
                                timeLeft = breakMinutes * 60
                                isRunning = false
                                context.startService(android.content.Intent(context, PomodoroService::class.java).apply {
                                    action = PomodoroService.ACTION_STOP
                                })
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isWorkMode) Amber else Color.Transparent,
                                contentColor = if (!isWorkMode) NavyBlue else NavyBlue.copy(alpha = 0.7f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                            shape = RoundedCornerShape(28.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Descanso", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ROTATING METRONOME WAVE CIRCLE
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .graphicsLayer(
                                scaleX = pulseScale,
                                scaleY = pulseScale
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            color = if (isWorkMode) ProBlue else Amber,
                            trackColor = (if (isWorkMode) ProBlue else Amber).copy(alpha = 0.15f),
                            strokeWidth = 10.dp
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = timeString,
                                fontSize = 56.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NavyBlue,
                                letterSpacing = (-1).sp
                            )
                            Text(
                                text = if (isWorkMode) "ESTUDIO" else "RELAX",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = (if (isWorkMode) ProBlue else Amber).copy(alpha = 0.8f),
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // TIME CUSTOMIZATION
                    TextButton(
                        onClick = { showEditDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = if (isWorkMode) ProBlue else Amber)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar tiempos", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Configurar Tiempos (${workMinutes}m / ${breakMinutes}m)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // BUTTONS
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                isRunning = !isRunning
                                val intent = android.content.Intent(context, PomodoroService::class.java).apply {
                                    if (isRunning) {
                                        if (PomodoroState.isPaused.value) {
                                            action = PomodoroService.ACTION_RESUME
                                        } else {
                                            action = PomodoroService.ACTION_START
                                            putExtra(PomodoroService.EXTRA_MINUTES, timeLeft / 60)
                                            putExtra(PomodoroService.EXTRA_IS_WORK, isWorkMode)
                                        }
                                    } else {
                                        action = PomodoroService.ACTION_PAUSE
                                    }
                                }
                                if (isRunning && !PomodoroState.isPaused.value) {
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                        context.startForegroundService(intent)
                                    } else {
                                        context.startService(intent)
                                    }
                                } else {
                                    context.startService(intent)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isWorkMode) ProBlue else Amber,
                                contentColor = if (isWorkMode) Color.White else NavyBlue
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .width(140.dp)
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.PlayArrow else Icons.Default.PlayArrow, // Uses PlayArrow stylised for simplicity
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isRunning) "Pausar" else "Iniciar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                isRunning = false
                                timeLeft = if (isWorkMode) workMinutes * 60 else breakMinutes * 60
                                context.startService(android.content.Intent(context, PomodoroService::class.java).apply {
                                    action = PomodoroService.ACTION_STOP
                                })
                            },
                            modifier = Modifier
                                .size(52.dp)
                                .background(Color(0xFFECEFF1), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Reiniciar",
                                tint = NavyBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        // New: Completar Session button
                        IconButton(
                            onClick = {
                                // Simulate finishing the current session
                                isRunning = false
                                isWorkMode = !isWorkMode // Toggle mode
                                timeLeft = if (isWorkMode) workMinutes * 60 else breakMinutes * 60
                                context.startService(android.content.Intent(context, PomodoroService::class.java).apply {
                                    action = PomodoroService.ACTION_STOP
                                })
                                // Optional: add toast or effect here
                            },
                            modifier = Modifier
                                .size(52.dp)
                                .background(Color(0xFFE8F5E9), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completar",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. SOUNDSCAPES / AMBIENT SOUNDS
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, NavyBlue.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = NavyBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Generador de Paisajes Sonoros",
                                fontWeight = FontWeight.Bold,
                                color = NavyBlue,
                                fontSize = 14.sp
                            )
                        }
                        
                        if (selectedSoundscape != null) {
                            IconButton(
                                onClick = { isPlayingAmbient = !isPlayingAmbient },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlayingAmbient) Icons.Default.Close else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause Ambient",
                                    tint = selectedSoundscape!!.accentColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // HORIZONTAL SOUND CARDS
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        soundscapes.forEach { scape ->
                            val isSelected = selectedSoundscape?.id == scape.id
                            val cardBg = if (isSelected) scape.accentColor.copy(alpha = 0.15f) else Color(0xFFF8FAFC)
                            val cardBorder = if (isSelected) scape.accentColor else Color.LightGray.copy(alpha = 0.3f)

                            Box(
                                modifier = Modifier
                                    .width(105.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(cardBg)
                                    .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                                    .clickable {
                                        selectedSoundscape = scape
                                        isPlayingAmbient = true
                                        try {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        } catch (e: Exception) {}
                                    }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = scape.icon,
                                        contentDescription = scape.name,
                                        tint = if (isSelected) scape.accentColor else NavyBlue.copy(alpha = 0.6f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        scape.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) scape.accentColor else NavyBlue
                                    )
                                    Text(
                                        scape.description.split(" ").first(),
                                        fontSize = 10.sp,
                                        color = SlateGray
                                    )
                                }
                            }
                        }
                    }

                    // WAVE VISUALIZER CANVAS (ONLY SHOWN IF PLAYING)
                    if (isPlayingAmbient && selectedSoundscape != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        val activeColor = selectedSoundscape!!.accentColor
                        val freqMultiplier = selectedSoundscape!!.baseFrequency

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(activeColor.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                                .padding(8.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val midY = h / 2f
                                val points = 60
                                val path = Path()
                                
                                path.moveTo(0f, midY)
                                for (i in 0..points) {
                                    val x = (i.toFloat() / points.toFloat()) * w
                                    // Sine wave formula combining wave phase infinite animation
                                    val angle = (i.toFloat() * freqMultiplier * 0.35f) + wavePhase
                                    val y = midY + sin(angle) * (h * 0.35f)
                                    path.lineTo(x, y)
                                }
                                drawPath(
                                    path = path,
                                    color = activeColor,
                                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                                )

                                // Draw visualiser audio-bars
                                for (i in 0..12) {
                                    val barX = w * (i.toFloat() / 12f)
                                    val barAngle = (i.toFloat() * freqMultiplier * 1.5f) + wavePhase * 2f
                                    val barHeight = (sin(barAngle) * (h * 0.4f)).coerceAtLeast(4f)
                                    drawLine(
                                        color = activeColor.copy(alpha = 0.4f),
                                        start = Offset(barX, midY - barHeight),
                                        end = Offset(barX, midY + barHeight),
                                        strokeWidth = 6f,
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // SPOTIFY INTEGRATION CARD
        item {
            val isSpotifyConnected by SpotifyState.isConnected.collectAsStateWithLifecycle()
            val isSpotifyPlaying by SpotifyState.isPlaying.collectAsStateWithLifecycle()
            val spotifyTrack by SpotifyState.currentTrack.collectAsStateWithLifecycle()
            val spotifyArtist by SpotifyState.currentArtist.collectAsStateWithLifecycle()
            val isSpotifyConnecting by SpotifyState.isConnecting.collectAsStateWithLifecycle()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)), // Spotify Dark
                border = BorderStroke(1.dp, Color(0xFF282828))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color(0xFF1DB954), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Spotify",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Música de Enfoque (Spotify)",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                        
                        Text(
                            text = if (isSpotifyConnected) "CONNECTED" else "CONNECT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1DB954),
                            modifier = Modifier
                                .background(Color(0xFF1DB954).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .clickable {
                                    if (!isSpotifyConnected && !isSpotifyConnecting) {
                                        SpotifyManager.connect(context)
                                    } else if (isSpotifyConnected) {
                                        SpotifyManager.disconnect()
                                    }
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (isSpotifyConnecting) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(color = Color(0xFF1DB954), modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Conectando a Spotify...", color = Color(0xFFB3B3B3), fontSize = 12.sp)
                        }
                    } else if (isSpotifyConnected) {
                        Spacer(modifier = Modifier.height(14.dp))
                        // Mini Player
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = spotifyTrack ?: "Not Playing",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = spotifyArtist ?: "",
                                    fontSize = 12.sp,
                                    color = Color(0xFFB3B3B3)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { SpotifyManager.skipPrevious() }) {
                                    Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Prev", tint = Color.White)
                                }
                                IconButton(onClick = { 
                                    if (isSpotifyPlaying) SpotifyManager.pause() else SpotifyManager.resume() 
                                }) {
                                    Icon(
                                        imageVector = if (isSpotifyPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause", 
                                        tint = Color(0xFF1DB954),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                IconButton(onClick = { SpotifyManager.skipNext() }) {
                                    Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Next", tint = Color.White)
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Conecta tu cuenta de Spotify para reproducir playlists directamente en la app.",
                            color = Color(0xFFB3B3B3),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val playlists = listOf(
                        Triple("Lofi Study Beats", "Relajantes ritmos e instrumental lofi retro.", "spotify:playlist:37i9dQZF1DWWQRwui0ExPn"),
                        Triple("Deep Focus", "Sintetizadores ambientales y ondas de enfoque.", "spotify:playlist:37i9dQZF1DWY4S6v7IdC7V"),
                        Triple("Estudio Clásico", "Guitarra clásica y piano suave inspirador.", "spotify:playlist:37i9dQZF1DX8NTLI6993gR")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        playlists.forEach { (title, desc, playlistUri) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFF2E2E2E), RoundedCornerShape(12.dp))
                                    .clickable {
                                        if (isSpotifyConnected) {
                                            SpotifyManager.play(playlistUri)
                                        } else {
                                            // Fallback
                                            try {
                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                                    data = android.net.Uri.parse(playlistUri)
                                                    putExtra(android.content.Intent.EXTRA_REFERRER, android.net.Uri.parse("android-app://" + context.packageName))
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                val webIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://open.spotify.com/playlist/" + playlistUri.split(":").last()))
                                                context.startActivity(webIntent)
                                            }
                                        }
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = desc,
                                        fontSize = 11.sp,
                                        color = Color(0xFF9F9F9F)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Reproducir",
                                    tint = Color(0xFF1DB954),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. STUDY OBJECTIVES / MICRO-TASKS
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, NavyBlue.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.List, contentDescription = null, tint = NavyBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Objetivos de Enfoque",
                                fontWeight = FontWeight.Bold,
                                color = NavyBlue,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = "${objectives.count { it.isCompleted }}/${objectives.size} Listos",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ProBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // INPUT FIELD
                    var newObjectiveText by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = newObjectiveText,
                        onValueChange = { newObjectiveText = it },
                        placeholder = { Text("Escribe un objetivo (ej: Repasar temas)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ProBlue,
                            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.6f)
                        ),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Text
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            if (newObjectiveText.isNotBlank()) {
                                val updated = objectives + FocusObjective(
                                    id = UUID.randomUUID().toString(),
                                    title = newObjectiveText.trim(),
                                    isCompleted = false
                                )
                                viewModel.saveFocusObjectives(updated.toObjectivesJsonString())
                                newObjectiveText = ""
                                keyboardController?.hide()
                            }
                        }),
                        trailingIcon = {
                            IconButton(onClick = {
                                if (newObjectiveText.isNotBlank()) {
                                    val updated = objectives + FocusObjective(
                                        id = UUID.randomUUID().toString(),
                                        title = newObjectiveText.trim(),
                                        isCompleted = false
                                    )
                                    viewModel.saveFocusObjectives(updated.toObjectivesJsonString())
                                    newObjectiveText = ""
                                    keyboardController?.hide()
                                }
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Añadir objetivo", tint = ProBlue)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // LIST OF OBJECTIVES
                    if (objectives.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Añade un micro-objetivo para mantener el rumbo.",
                                color = SlateGray,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            objectives.forEach { obj ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                                        .border(1.dp, Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                        .padding(vertical = 6.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Checkbox(
                                            checked = obj.isCompleted,
                                            onCheckedChange = { isChecked ->
                                                val updated = objectives.map {
                                                    if (it.id == obj.id) it.copy(isCompleted = isChecked) else it
                                                }
                                                viewModel.saveFocusObjectives(updated.toObjectivesJsonString())
                                                try {
                                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                } catch (e: Exception) {}
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = ProBlue)
                                        )
                                        Text(
                                            text = obj.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (obj.isCompleted) SlateGray else NavyBlue,
                                            textDecoration = if (obj.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            val updated = objectives.filter { it.id != obj.id }
                                            viewModel.saveFocusObjectives(updated.toObjectivesJsonString())
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Terracotta.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. FOCUS PROGRESS HISTORIC CHART & STATS
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, NavyBlue.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = NavyBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Historial y Progreso Semanal",
                            fontWeight = FontWeight.Bold,
                            color = NavyBlue,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val totalMinutesFocused = sessionsHistory.sumOf { it.duration }
                    val totalSessions = sessionsHistory.size

                    // STATS HIGHLIGHTS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Minutos Totales", fontSize = 10.sp, color = SlateGray, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${totalMinutesFocused}m", fontSize = 18.sp, fontWeight = FontWeight.Black, color = ProBlue)
                            }
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Sesiones Listas", fontSize = 10.sp, color = SlateGray, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("$totalSessions", fontSize = 18.sp, fontWeight = FontWeight.Black, color = DarkGreen)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // HISTORIC CHART (BAR CHART ON CANVAS WITH AXIS & ANIMATION)
                    Text(
                        text = "Curva de Enfoque Semanal",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            val paddingLeft = 70f
                            val paddingRight = 30f
                            val paddingTop = 25f
                            val paddingBottom = 40f

                            val chartWidth = w - paddingLeft - paddingRight
                            val chartHeight = h - paddingTop - paddingBottom

                            val daysOfWeek = listOf("Lu", "Ma", "Mi", "Ju", "Vi", "Sá", "Do")
                            
                            val daysMap = mutableMapOf<String, Int>()
                            daysOfWeek.forEach { daysMap[it] = 0 }
                            
                            if (sessionsHistory.isEmpty()) {
                                daysMap["Lu"] = 25
                                daysMap["Mi"] = 50
                                daysMap["Vi"] = 25
                            } else {
                                sessionsHistory.take(7).forEachIndexed { idx, item ->
                                    val key = daysOfWeek[idx % daysOfWeek.size]
                                    daysMap[key] = (daysMap[key] ?: 0) + item.duration
                                }
                            }

                            val maxVal = (daysMap.values.maxOrNull() ?: 25).coerceAtLeast(25).toFloat()
                            val numBars = daysOfWeek.size
                            val barWidth = (chartWidth / numBars) * 0.55f
                            val stepX = chartWidth / numBars

                            // 1. Draw dashed reference lines & Y-axis labels
                            val paintY = android.graphics.Paint().apply {
                                color = android.graphics.Color.parseColor("#94A3B8") // Slate 400
                                textSize = 20f
                                textAlign = android.graphics.Paint.Align.RIGHT
                                isAntiAlias = true
                            }

                            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                            // Middle reference line
                            drawLine(
                                color = Color(0xFFCBD5E1).copy(alpha = 0.5f),
                                start = Offset(paddingLeft, paddingTop + chartHeight / 2f),
                                end = Offset(w - paddingRight, paddingTop + chartHeight / 2f),
                                strokeWidth = 2f,
                                pathEffect = dashEffect
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                "${(maxVal / 2).toInt()}m",
                                paddingLeft - 12f,
                                paddingTop + chartHeight / 2f + 7f,
                                paintY
                            )

                            // Top reference line
                            drawLine(
                                color = Color(0xFFCBD5E1).copy(alpha = 0.5f),
                                start = Offset(paddingLeft, paddingTop),
                                end = Offset(w - paddingRight, paddingTop),
                                strokeWidth = 1.5f
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                "${maxVal.toInt()}m",
                                paddingLeft - 12f,
                                paddingTop + 7f,
                                paintY
                            )

                            // Bottom reference line
                            drawLine(
                                color = Color(0xFF94A3B8).copy(alpha = 0.3f),
                                start = Offset(paddingLeft, h - paddingBottom),
                                end = Offset(w - paddingRight, h - paddingBottom),
                                strokeWidth = 2f
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                "0m",
                                paddingLeft - 12f,
                                h - paddingBottom + 7f,
                                paintY
                            )

                            // 2. Draw bars & day labels
                            val paintX = android.graphics.Paint().apply {
                                color = android.graphics.Color.parseColor("#475569") // Slate 600
                                textSize = 22f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                                typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
                            }

                            val paintVal = android.graphics.Paint().apply {
                                color = android.graphics.Color.parseColor("#1E3A8A") // Navy Blue
                                textSize = 18f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                                typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
                            }

                            daysOfWeek.forEachIndexed { idx, day ->
                                val minsValue = daysMap[day] ?: 0
                                // Animate the bar height dynamically!
                                val barHeight = (minsValue.toFloat() / maxVal) * chartHeight * chartProgress
                                val barX = paddingLeft + (idx * stepX) + (stepX - barWidth) / 2f
                                val barY = h - paddingBottom - barHeight

                                // Draw single vertical rounded study bar
                                drawRoundRect(
                                    color = if (minsValue > 0) ProBlue else Color(0xFFE2E8F0),
                                    topLeft = Offset(barX, barY),
                                    size = Size(barWidth, barHeight.coerceAtLeast(2f)),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                                )

                                // Draw day label below bar
                                drawContext.canvas.nativeCanvas.drawText(
                                    day,
                                    barX + barWidth / 2f,
                                    h - paddingBottom + 28f,
                                    paintX
                                )

                                // Draw numeric value on top of bar (if studied)
                                if (minsValue > 0 && chartProgress > 0.8f) {
                                    drawContext.canvas.nativeCanvas.drawText(
                                        "${minsValue}m",
                                        barX + barWidth / 2f,
                                        barY - 6f,
                                        paintVal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // HISTORIC LIST RECORDS (LATEST 3)
                    if (sessionsHistory.isNotEmpty()) {
                        Text(
                            text = "Últimas Sesiones",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateGray,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            sessionsHistory.take(3).forEach { record ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF1F5F9).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = record.label,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NavyBlue
                                        )
                                    }
                                    Text(
                                        text = "${record.date} • ${record.duration} min",
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
            
        item {
            Spacer(modifier = Modifier.height(16.dp))
            val upcomingExamsCount = remember(assessments) { assessments.count { it.grade == null } }
            val absencesCount = absences.size
            WellnessWidget(
                upcomingExamsCount = upcomingExamsCount,
                absencesCount = absencesCount,
                calculatedStress = stressLevel.toFloat(),
                statusText = stressStatus
            )
        }
    }

    // TIME CUSTOMIZER DIALOG
    if (showEditDialog) {
        var tempWork by remember { mutableStateOf(workMinutes.toString()) }
        var tempBreak by remember { mutableStateOf(breakMinutes.toString()) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Personalizar Pomodoro", color = NavyBlue, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = tempWork,
                        onValueChange = { tempWork = it },
                        label = { Text("Minutos de Estudio") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ProBlue),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempBreak,
                        onValueChange = { tempBreak = it },
                        label = { Text("Minutos de Descanso") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Amber),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newWork = tempWork.toIntOrNull() ?: 25
                        val newBreak = tempBreak.toIntOrNull() ?: 5
                        workMinutes = if (newWork > 0) newWork else 25
                        breakMinutes = if (newBreak > 0) newBreak else 5
                        timeLeft = if (isWorkMode) workMinutes * 60 else breakMinutes * 60
                        isRunning = false
                        context.startService(android.content.Intent(context, PomodoroService::class.java).apply {
                            action = PomodoroService.ACTION_STOP
                        })
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ProBlue)
                ) {
                    Text("Guardar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancelar", color = SlateGray)
                }
            },
            containerColor = Color.White
        )
    }
}
