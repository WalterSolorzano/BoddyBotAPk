package com.aistudio.unibuddy.qywvsp.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.unibuddy.qywvsp.data.SeasonRecap
import com.aistudio.unibuddy.qywvsp.ui.components.BuddyMascot
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.EmojiEvents
import com.aistudio.unibuddy.qywvsp.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SeasonRecapOverlay(
    recap: SeasonRecap,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBlue)
    ) {
        val pagerState = rememberPagerState(pageCount = { 5 })
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> RecapTitleCard(recap)
                1 -> RecapAttendanceCard(recap)
                2 -> RecapFocusCard(recap)
                3 -> RecapBadgesCard(recap)
                4 -> RecapSummaryCard(recap, onDismiss)
            }
        }
        
        // Indicators
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(5) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == pagerState.currentPage) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (index == pagerState.currentPage) Color.White else Color.White.copy(alpha = 0.3f))
                )
            }
        }
        
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .padding(top = 24.dp)
        ) {
            Icon(Icons.Rounded.Close, contentDescription = "Cerrar", tint = Color.White)
        }
    }
}

@Composable
fun RecapTitleCard(recap: SeasonRecap) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(300)
        visible = true
    }
    
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(1000), label = "")
    
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tu Semestre en\nResumen",
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Pet
        BuddyMascot(
            pose = if (recap.attendancePercentage > 85) "celebrating" else "thinking",
            isHappy = (recap.attendancePercentage > 85),
            isWorried = (recap.attendancePercentage <= 85),
            modifier = Modifier.size(150.dp).padding(bottom = 24.dp)
        )
        
        Text(
            text = "¡Temporada Completada!",
            fontSize = 18.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun RecapAttendanceCard(recap: SeasonRecap) {
    var target by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        delay(300)
        target = recap.attendancePercentage.toFloat()
    }
    
    val animatedPercent by animateFloatAsState(target, tween(1500), label = "")
    
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Asistencia Total", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "${animatedPercent.toInt()}%",
            fontSize = 72.sp,
            fontWeight = FontWeight.Black,
            color = if (animatedPercent > 80) ProBlue else Amber
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        val reaction = if (animatedPercent > 90) "¡Imparable! Casi no faltaste."
                       else if (animatedPercent > 70) "Buena constancia."
                       else "Semestre difícil, ¡pero llegaste al final!"
        Text(
            text = reaction,
            fontSize = 18.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun RecapFocusCard(recap: SeasonRecap) {
    var targetHours by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        delay(300)
        targetHours = recap.focusHoursTotal.toFloat()
    }
    
    val animatedHours by animateFloatAsState(targetHours, tween(1500), label = "")
    
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Modo Enfoque", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "${String.format("%.1f", animatedHours)}h",
            fontSize = 72.sp,
            fontWeight = FontWeight.Black,
            color = DarkGreen
        )
        Text("Total Enfocado", fontSize = 16.sp, color = Color.White.copy(alpha = 0.7f))
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${recap.focusSessionsCompleted}", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Completadas", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${recap.focusSessionsInterrupted}", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Terracotta)
                Text("Interrumpidas", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun RecapBadgesCard(recap: SeasonRecap) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Logros", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Whatshot,
                contentDescription = null,
                tint = Amber,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("${recap.maxStreak}", fontSize = 64.sp, fontWeight = FontWeight.Black, color = Amber)
        }
        Text("Racha Máxima", fontSize = 16.sp, color = Color.White.copy(alpha = 0.7f))
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = ProBlue,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("${recap.badgesUnlockedCount}", fontSize = 64.sp, fontWeight = FontWeight.Black, color = ProBlue)
        }
        Text("Medallas Ganadas", fontSize = 16.sp, color = Color.White.copy(alpha = 0.7f))
    }
}

@Composable
fun RecapSummaryCard(recap: SeasonRecap, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Resumen Final", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color.White)
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = recap.highlightText,
            fontSize = 20.sp,
            color = ProBlue,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (recap.bestSubjectName != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Mejor Materia: ${recap.bestSubjectName}", color = Color.White, fontWeight = FontWeight.Bold)
                    recap.worstSubjectName?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("A Mejorar: $it", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        var showDetails by remember { mutableStateOf(false) }
        
        if (showDetails) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Detalles Completos:", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tareas completadas: ${recap.completedTasksCount}", color = Color.White.copy(alpha = 0.8f))
                    recap.subjectWithMostAbsences?.let { Text("Más faltas en: $it", color = Color.White.copy(alpha = 0.8f)) }
                    recap.mostProductiveTimeOfDay?.let { Text("Hora más productiva: $it", color = Color.White.copy(alpha = 0.8f)) }
                    recap.mostFocusedSubject?.let { Text("Materia más enfocada: $it", color = Color.White.copy(alpha = 0.8f)) }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Text(
                "Ver todos los detalles",
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.clickable { showDetails = true }.padding(8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = ProBlue),
            modifier = Modifier.fillMaxWidth(0.8f).height(56.dp)
        ) {
            Text("¡Vamos al Siguiente Semestre!", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
