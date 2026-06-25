package com.aistudio.unibuddy.qywvsp.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aistudio.unibuddy.qywvsp.data.CurriculumData
import com.aistudio.unibuddy.qywvsp.ui.theme.*
import kotlinx.coroutines.delay

fun Modifier.bounceClick(onClick: () -> Unit) = composed {
    var isPressed by remember { mutableStateOf(false) }
    var isDebouncing by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f)
    val haptic = LocalHapticFeedback.current

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            if (!isDebouncing) {
                isDebouncing = true
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
        }
        .pointerInput(Unit) {
            while (true) {
                awaitPointerEventScope {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    waitForUpOrCancellation()
                    isPressed = false
                }
            }
        }
        .pointerInput(isDebouncing) {
            if (isDebouncing) {
                delay(300L) // Debounce window
                isDebouncing = false
            }
        }
}

@Composable
fun PensumProgressScreen(viewModel: UniBuddyViewModel) {
    val passedSubjects by viewModel.passedSubjects.collectAsStateWithLifecycle()
    val allSubjects = CurriculumData.industrialEngineering
    val totalSubjects = allSubjects.size
    val passedCount = passedSubjects.size
    val progressPercent = if (totalSubjects > 0) (passedCount.toFloat() / totalSubjects) * 100 else 0f

    val remainingSubjects = totalSubjects - passedCount
    val avgSubjPerSem = if (allSubjects.isNotEmpty()) {
        val maxSem = allSubjects.map { it.semester }.maxOrNull() ?: 1
        allSubjects.size.toFloat() / maxSem.toFloat()
    } else 5f
    val estimatedRemainingSemesters = kotlin.math.ceil(remainingSubjects / avgSubjPerSem).toInt()
    val estimatedRemainingYears = estimatedRemainingSemesters / 2f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(16.dp)
    ) {
        Text("Mi Progreso", style = MaterialTheme.typography.headlineMedium, color = NavyBlue, fontWeight = FontWeight.Bold)
        Text("Plan de Estudio: Ingeniería Industrial UNI", fontSize = 14.sp, color = DarkGray)
        Spacer(modifier = Modifier.height(16.dp))

        // Progress Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Avance de la carrera", fontWeight = FontWeight.Bold, color = NavyBlue)
                    Text("${progressPercent.toInt()}%", fontWeight = FontWeight.Bold, color = DarkGreen)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { passedCount.toFloat() / totalSubjects },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = DarkGreen,
                    trackColor = DarkGreen.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("$passedCount de $totalSubjects clases aprobadas", fontSize = 12.sp, color = DarkGray)
                    Text("~$estimatedRemainingYears años restantes", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Amber)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val grouped = allSubjects.groupBy { it.semester }
            for (semester in grouped.keys.sorted()) {
                item {
                    Text(
                        text = "Semestre $semester",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavyBlue,
                        modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                    )
                }
                items(grouped[semester] ?: emptyList()) { subject ->
                    val isPassed = passedSubjects.contains(subject.code)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick { viewModel.togglePassedSubject(subject.code) }
                            .background(if (isPassed) Color(0xFFE8F5E9) else Color.White, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isPassed) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(24.dp))
                        } else {
                            Box(modifier = Modifier
                                .size(24.dp)
                                .background(Color.Transparent, RoundedCornerShape(12.dp))
                                .padding(2.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE0E0E0), RoundedCornerShape(10.dp)))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(subject.name, fontWeight = FontWeight.Bold, color = if (isPassed) DarkGreen else Color(0xFF37474F))
                            Text("Código: ${subject.code}", fontSize = 12.sp, color = DarkGray)
                        }
                    }
                }
            }
        }
    }
}
