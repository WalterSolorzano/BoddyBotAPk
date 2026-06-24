package com.aistudio.unibuddy.qywvsp.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.aistudio.unibuddy.qywvsp.ui.theme.*

@Composable
fun FocusModeScreen() {
    var isWorkMode by remember { mutableStateOf(true) }
    var workMinutes by remember { mutableStateOf(25) }
    var breakMinutes by remember { mutableStateOf(5) }
    var timeLeft by remember { mutableStateOf(workMinutes * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var completedPomodoros by remember { mutableStateOf(0) }

    val bgColor = BackgroundGray
    val cardColor = Color.White
    val accentColor = if (isWorkMode) ProBlue else Amber
    val textColor = NavyBlue

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        while (true) {
            if (PomodoroService.isRunningInService) {
                isRunning = true
                timeLeft = PomodoroService.timeLeftSeconds
                isWorkMode = PomodoroService.currentModeIsWork
            } else if (PomodoroService.isPaused) {
                isRunning = false
                timeLeft = PomodoroService.timeLeftSeconds
                isWorkMode = PomodoroService.currentModeIsWork
            } else if (isRunning && timeLeft > 0) {
                // If it's running locally but the service isn't, we can still tick here 
                // but usually they will be in sync because we start the service
                delay(1000L)
                timeLeft--
            } else {
                delay(100L) // Fast poll to resume UI quickly
            }
            delay(100L)
        }
    }

    LaunchedEffect(isRunning, timeLeft) {
        if (timeLeft == 0 && isRunning) {
            isRunning = false
            if (isWorkMode) {
                completedPomodoros++
            }
            // Auto switch mode
            isWorkMode = !isWorkMode
            timeLeft = if (isWorkMode) workMinutes * 60 else breakMinutes * 60
        }
    }

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)
    val totalSeconds = if (isWorkMode) workMinutes * 60 else breakMinutes * 60
    val progress = if (totalSeconds > 0) 1f - (timeLeft.toFloat() / totalSeconds.toFloat()) else 0f

    // Animations
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRunning) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val mascotOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = if (isRunning) 10f else -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascotOffset"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Toggle Pomodoro Mode
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(32.dp))
                .background(Color.Black.copy(alpha = 0.2f))
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
                    containerColor = if (isWorkMode) accentColor else Color.Transparent,
                    contentColor = if (isWorkMode) textColor else Color.LightGray
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isWorkMode) 2.dp else 0.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Estudiar ($workMinutes m)", fontWeight = FontWeight.Bold)
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
                    containerColor = if (!isWorkMode) accentColor else Color.Transparent,
                    contentColor = if (!isWorkMode) textColor else Color.LightGray
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = if (!isWorkMode) 2.dp else 0.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Descanso ($breakMinutes m)", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Mascot Animation
        Box(
            modifier = Modifier
                .size(64.dp)
                .offset(y = mascotOffset.dp),
            contentAlignment = Alignment.Center
        ) {
            BuddyMascot(
                modifier = Modifier.fillMaxSize(),
                isHappy = isWorkMode && isRunning,
                pose = if (isWorkMode) "working" else "idle"
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Timer Circle
        Box(
            modifier = Modifier
                .size(280.dp)
                .graphicsLayer(
                    scaleX = if (isRunning) pulseScale else 1f,
                    scaleY = if (isRunning) pulseScale else 1f
                )
                .clip(CircleShape)
                .background(cardColor),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.2f),
                strokeWidth = 12.dp
            )
            Text(
                text = timeString,
                fontSize = 72.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { showEditDialog = true }) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar tiempo", tint = accentColor)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Editar Tiempo", color = accentColor, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Button(
                onClick = { 
                    isRunning = !isRunning
                    val intent = android.content.Intent(context, PomodoroService::class.java).apply {
                        if (isRunning) {
                            if (PomodoroService.isPaused) {
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
                    if (isRunning && !PomodoroService.isPaused) {
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
                    containerColor = accentColor,
                    contentColor = textColor
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.height(64.dp).width(160.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(if (isRunning) "Pausar" else "Iniciar", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            }

            IconButton(
                onClick = { 
                    isRunning = false
                    timeLeft = if (isWorkMode) workMinutes * 60 else breakMinutes * 60 
                    val intent = android.content.Intent(context, PomodoroService::class.java).apply {
                        action = PomodoroService.ACTION_STOP
                    }
                    context.startService(intent)
                },
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(imageVector = Icons.Default.Clear, contentDescription = "Reiniciar", tint = textColor, modifier = Modifier.size(32.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (completedPomodoros > 0) {
            Text(
                text = "🔥 Completaste $completedPomodoros pomodoros hoy",
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold
            )
        }

        if (showEditDialog) {
            var tempWork by remember { mutableStateOf(workMinutes.toString()) }
            var tempBreak by remember { mutableStateOf(breakMinutes.toString()) }

            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Editar Tiempos (minutos)", color = Color(0xFF1A237E), fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = tempWork,
                            onValueChange = { tempWork = it },
                            label = { Text("Tiempo de Estudio") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = tempBreak,
                            onValueChange = { tempBreak = it },
                            label = { Text("Tiempo de Descanso") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val newWork = tempWork.toIntOrNull() ?: 25
                        val newBreak = tempBreak.toIntOrNull() ?: 5
                        workMinutes = if (newWork > 0) newWork else 25
                        breakMinutes = if (newBreak > 0) newBreak else 5
                        if (isWorkMode) timeLeft = workMinutes * 60 else timeLeft = breakMinutes * 60
                        isRunning = false
                        context.startService(android.content.Intent(context, PomodoroService::class.java).apply {
                            action = PomodoroService.ACTION_STOP
                        })
                        showEditDialog = false
                    }) {
                        Text("Guardar", color = NavyBlue, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Cancelar", color = Color.Gray)
                    }
                }
            )
        }
    }
}
