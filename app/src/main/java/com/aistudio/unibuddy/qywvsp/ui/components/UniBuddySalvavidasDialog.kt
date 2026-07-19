package com.aistudio.unibuddy.qywvsp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aistudio.unibuddy.qywvsp.ui.UniBuddyViewModel
import com.aistudio.unibuddy.qywvsp.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ComparativeGauge(
    trendScore: Double,
    requiredScore: Double,
    passingGrade: Double,
    maxScore: Double = 100.0,
    mainColor: Color
) {
    val animatedTrend by animateFloatAsState(
        targetValue = trendScore.toFloat().coerceIn(0f, maxScore.toFloat()),
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "trend_anim"
    )
    val animatedRequired by animateFloatAsState(
        targetValue = requiredScore.toFloat().coerceIn(0f, maxScore.toFloat()),
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "required_anim"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .width(220.dp)
                .height(110.dp)
        ) {
            val width = size.width
            val height = size.height
            val outerStroke = 12.dp.toPx()
            val innerStroke = 8.dp.toPx()
            val gap = 6.dp.toPx()

            // Outer track bounds (Trend)
            val outerTopLeft = androidx.compose.ui.geometry.Offset(outerStroke / 2, outerStroke / 2)
            val outerSize = androidx.compose.ui.geometry.Size(width - outerStroke, (height * 2) - outerStroke)

            // Draw Background Track for Outer Arc
            drawArc(
                color = Color(0xFFE2E8F0),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = outerTopLeft,
                size = outerSize,
                style = Stroke(width = outerStroke, cap = StrokeCap.Round)
            )

            // Draw Trend Arc (Outer Track)
            val trendSweep = (animatedTrend / maxScore.toFloat()) * 180f
            drawArc(
                brush = Brush.horizontalGradient(listOf(mainColor.copy(alpha = 0.6f), mainColor)),
                startAngle = 180f,
                sweepAngle = trendSweep,
                useCenter = false,
                topLeft = outerTopLeft,
                size = outerSize,
                style = Stroke(width = outerStroke, cap = StrokeCap.Round)
            )

            // Inner track bounds (Required)
            val innerOffset = outerStroke + gap
            val innerTopLeft = androidx.compose.ui.geometry.Offset(innerOffset + innerStroke / 2, innerOffset + innerStroke / 2)
            val innerSize = androidx.compose.ui.geometry.Size(
                width - (innerOffset * 2) - innerStroke,
                (height - innerOffset) * 2 - innerStroke
            )

            if (animatedRequired > 0) {
                // Draw Background Track for Inner Arc
                drawArc(
                    color = Color(0xFFF1F5F9),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = innerTopLeft,
                    size = innerSize,
                    style = Stroke(width = innerStroke, cap = StrokeCap.Round)
                )

                // Draw Required Arc (Inner Track)
                val requiredSweep = (animatedRequired / maxScore.toFloat()) * 180f
                drawArc(
                    color = ProRed.copy(alpha = 0.85f),
                    startAngle = 180f,
                    sweepAngle = requiredSweep,
                    useCenter = false,
                    topLeft = innerTopLeft,
                    size = innerSize,
                    style = Stroke(width = innerStroke, cap = StrokeCap.Round)
                )
            }

            // Draw Passing Grade Threshold Reference Indicator
            val passingAngleRad = Math.toRadians((180.0 + (passingGrade / maxScore) * 180.0)).toFloat()
            val cosAngle = kotlin.math.cos(passingAngleRad)
            val sinAngle = kotlin.math.sin(passingAngleRad)

            val centerX = width / 2f
            val centerY = height

            val outerRadius = (width - outerStroke) / 2f
            val innerRadius = if (animatedRequired > 0) {
                (innerSize.width) / 2f
            } else {
                (width - outerStroke * 2) / 2f
            }

            val startX = centerX + innerRadius * cosAngle
            val startY = centerY + innerRadius * sinAngle

            val endX = centerX + (outerRadius + 4.dp.toPx()) * cosAngle
            val endY = centerY + (outerRadius + 4.dp.toPx()) * sinAngle

            // Draw sleek dashed threshold line
            drawLine(
                color = NavyBlue.copy(alpha = 0.45f),
                start = androidx.compose.ui.geometry.Offset(startX, startY),
                end = androidx.compose.ui.geometry.Offset(endX, endY),
                strokeWidth = 2.dp.toPx()
            )

            // Small visual bullet point at the end of reference line
            drawCircle(
                color = NavyBlue,
                radius = 3.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(endX, endY)
            )
        }

        // Concentric Info Display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = 25.dp)
        ) {
            Text(
                text = "Tendencia Proyectada",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = SlateGray
            )
            Text(
                text = String.format(Locale.US, "%.1f pts", trendScore),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NavyBlue
            )
            Text(
                text = "Aprobacion: ${String.format(Locale.US, "%.0f pts", passingGrade)}",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (trendScore >= passingGrade) StatusGreen else ProRed
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniBuddySalvavidasDialog(
    viewModel: UniBuddyViewModel,
    onDismiss: () -> Unit
) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle(emptyList())
    val assessments by viewModel.assessments.collectAsStateWithLifecycle(emptyList())
    val university by viewModel.userUniversity.collectAsStateWithLifecycle()
    val buddyColorStr by viewModel.buddyColor.collectAsStateWithLifecycle()
    val mainColor = try { Color(android.graphics.Color.parseColor(buddyColorStr)) } catch(e: Exception) { ProBlue }
    
    val scope = rememberCoroutineScope()
    val passingGrade = if (university == "UAM" || university == "UCA" || university == "Keiser") 70.0 else 60.0

    // Emergency scan/prediction simulation activation
    var isScanning by remember { mutableStateOf(false) }
    var currentAnalysisStatus by remember { mutableStateOf("") }
    var hasActivatedEmergencyScan by remember { mutableStateOf(false) }
    
    val analysisLog = remember {
        listOf(
            "Analizando ponderaciones y rendimiento por corte evaluativo.",
            "Cruzando tendencias de evaluaciones previas para proyectar escenario final.",
            "Buscando el camino de maxima eficiencia para alcanzar la nota de aprobacion.",
            "Calculando el diferencial de puntos requerido en el ultimo tramo del semestre.",
            "Optimizando tus horas de estudio para los objetivos academicos mas criticos."
        )
    }

    // Animation for pulse aura in Emergency Mode
    val infiniteTransition = rememberInfiniteTransition(label = "emergency_pulse")
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = mainColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cerrar", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MedicalServices,
                    contentDescription = null,
                    tint = ProRed,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = "UniBuddy Salvavidas",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyBlue
                    )
                    Text(
                        text = "Proyeccion Real vs Minimo de Emergencia",
                        fontSize = 10.sp,
                        color = SlateGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Interactive Mascot Card with dynamic pulse indicators
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isScanning) Color(0xFFFEF2F2) else mainColor.copy(alpha = 0.05f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isScanning) ProRed.copy(alpha = 0.4f) else mainColor.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(76.dp)
                            ) {
                                if (isScanning) {
                                    Box(
                                        modifier = Modifier
                                            .size(70.dp)
                                            .scale(auraScale)
                                            .clip(CircleShape)
                                            .background(ProRed.copy(alpha = auraAlpha))
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(
                                            1.dp,
                                            if (isScanning) ProRed else mainColor.copy(alpha = 0.15f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    BuddyMascot(
                                        pose = if (isScanning) "working" else "greeting",
                                        isHappy = !isScanning,
                                        isWorried = isScanning,
                                        mainColor = mainColor,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isScanning) "Analizando Trayectoria" else "Salvavidas Academico",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isScanning) ProRed else NavyBlue
                                )
                                Text(
                                    text = if (isScanning) currentAnalysisStatus else "Compara la proyeccion matematica real basada en tus notas historicas contra la exigencia minima requerida en los pendientes.",
                                    fontSize = 11.sp,
                                    color = SlateGray,
                                    lineHeight = 14.sp
                                )
                            }
                        }

                        // Boton de Emergencia
                        Button(
                            onClick = {
                                scope.launch {
                                    isScanning = true
                                    currentAnalysisStatus = analysisLog.random()
                                    delay(1600)
                                    isScanning = false
                                    hasActivatedEmergencyScan = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isScanning) ProRed else Color(0xFFFEF2F2),
                                contentColor = if (isScanning) Color.White else ProRed
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isScanning) ProRed else ProRed.copy(alpha = 0.25f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (isScanning) Icons.Rounded.Autorenew else Icons.Rounded.Bolt,
                                contentDescription = null,
                                tint = if (isScanning) Color.White else ProRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isScanning) "Calculando escenario optimo..." else "Boton de Emergencia",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = "ESTADO REAL DE TUS ASIGNATURAS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SlateGray,
                    letterSpacing = 1.sp
                )

                if (subjects.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Aun no registras asignaturas. Registralas desde el panel principal.",
                            fontSize = 11.sp,
                            color = SlateGray,
                            modifier = Modifier.padding(14.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                subjects.forEach { subject ->
                    val subAssessments = assessments.filter { it.subjectId == subject.id }
                    val completed = subAssessments.filter { it.grade != null }
                    val pending = subAssessments.filter { it.grade == null }
                    
                    val accumulatedScore = completed.sumOf { (it.grade ?: 0.0) * (it.percentage / 100.0) }
                    val completedPercentage = completed.sumOf { it.percentage }
                    val remainingPercentage = pending.sumOf { it.percentage }

                    // STATISTICAL TREND CALCULATION
                    // Uses running weighted average with recent momentum weighting (65% historic, 35% recent momentum)
                    val runningAvg = if (completed.isNotEmpty()) {
                        val overallAvg = completed.sumOf { (it.grade ?: 0.0) * it.percentage } / completedPercentage
                        val sorted = completed.sortedBy { it.id }
                        if (sorted.size >= 2) {
                            val lastTwoAvg = sorted.takeLast(2).map { it.grade ?: 0.0 }.average()
                            (overallAvg * 0.65) + (lastTwoAvg * 0.35)
                        } else {
                            overallAvg
                        }
                    } else {
                        70.0 // Realistic base expectation if no marks are logged yet
                    }

                    val predictedPendingScore = remainingPercentage * (runningAvg / 100.0)
                    val totalPredictedFinalScore = accumulatedScore + predictedPendingScore

                    val pointsNeeded = (passingGrade - accumulatedScore).coerceAtLeast(0.0)
                    val requiredAvgOnPending = if (remainingPercentage > 0) {
                        (pointsNeeded / (remainingPercentage / 100.0))
                    } else 0.0

                    val isAlreadyPassed = accumulatedScore >= passingGrade
                    val isImpossible = accumulatedScore + remainingPercentage < passingGrade

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Bone),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Subject identity header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = subject.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = NavyBlue
                                    )
                                    Text(
                                        text = "Acumulado real: ${String.format(Locale.US, "%.1f", accumulatedScore)} pts (${completedPercentage}% evaluado)",
                                        fontSize = 10.sp,
                                        color = SlateGray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                val isShort = totalPredictedFinalScore < passingGrade
                                val badgeColor = when {
                                    isAlreadyPassed -> StatusGreen
                                    isImpossible -> Terracotta
                                    isShort -> StatusAmber
                                    else -> ProBlue
                                }
                                val badgeLabel = when {
                                    isAlreadyPassed -> "Aprobado"
                                    isImpossible -> "Convocatoria"
                                    isShort -> "Alerta de Brecha"
                                    else -> "Tendencia Positiva"
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(badgeColor.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = badgeLabel,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = badgeColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Rich animated radial comparison visualization
                            ComparativeGauge(
                                trendScore = totalPredictedFinalScore,
                                requiredScore = if (hasActivatedEmergencyScan && !isImpossible && !isAlreadyPassed) requiredAvgOnPending else 0.0,
                                passingGrade = passingGrade,
                                mainColor = mainColor
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // REALISTIC RADAR COMPARATIVE MODULE
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                    .border(1.dp, Bone, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "INDICADORES DE PROYECCION",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SlateGray,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "Promedio Actual: ${String.format(Locale.US, "%.1f", runningAvg)}%",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = mainColor
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Proyección Column
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                            .border(1.dp, Bone, RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Tu Tendencia Real",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SlateGray
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", totalPredictedFinalScore)} pts",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (totalPredictedFinalScore >= passingGrade) StatusGreen else StatusAmber
                                        )
                                        Text(
                                            text = "Nota proyectada final",
                                            fontSize = 8.sp,
                                            color = SlateGray
                                        )
                                    }

                                    // Mínimo Emergencia Column
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (hasActivatedEmergencyScan) Color(0xFFFEF2F2) else Color.White,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                1.dp,
                                                if (hasActivatedEmergencyScan) ProRed.copy(alpha = 0.2f) else Bone,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Minimo Requerido",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (hasActivatedEmergencyScan) ProRed else SlateGray
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        if (hasActivatedEmergencyScan) {
                                            Text(
                                                text = if (isImpossible) "Fallo" else "${String.format(Locale.US, "%.1f", requiredAvgOnPending)}%",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isImpossible) Terracotta else ProRed
                                            )
                                            Text(
                                                text = if (isImpossible) "Convocatoria" else "En pendientes",
                                                fontSize = 8.sp,
                                                color = if (isImpossible) Terracotta else SlateGray
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .clickable {
                                                        scope.launch {
                                                            isScanning = true
                                                            currentAnalysisStatus = analysisLog.random()
                                                            delay(1200)
                                                            isScanning = false
                                                            hasActivatedEmergencyScan = true
                                                        }
                                                    }
                                                    .padding(vertical = 4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "Calcular Minimo",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = ProRed,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // ITEM BY ITEM REALISTIC VS REQUIRED COMPARISON
                            if (pending.isNotEmpty() && !isAlreadyPassed && !isImpossible) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "EVALUACIONES PENDIENTES (PREDICCION VS REQUERIDO)",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SlateGray,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    pending.forEach { assessment ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Bone),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = assessment.name,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = NavyBlue
                                                    )
                                                    Text(
                                                        text = "Peso evaluativo: ${assessment.percentage}%",
                                                        fontSize = 9.sp,
                                                        color = SlateGray
                                                    )
                                                }

                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Predicted Column
                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text(
                                                            text = "Proyeccion",
                                                            fontSize = 8.sp,
                                                            color = SlateGray,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = "${String.format(Locale.US, "%.1f", runningAvg)}/100",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = mainColor
                                                        )
                                                    }

                                                    // Needed Column
                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text(
                                                            text = "Necesario",
                                                            fontSize = 8.sp,
                                                            color = if (hasActivatedEmergencyScan) ProRed else SlateGray,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = if (hasActivatedEmergencyScan) "${String.format(Locale.US, "%.1f", requiredAvgOnPending)}/100" else "--",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = if (hasActivatedEmergencyScan) ProRed else SlateGray
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // FOOTER WARNING / INTELLIGENT REALISTIC FEEDBACK
                            if (!isAlreadyPassed && !isImpossible && remainingPercentage > 0) {
                                Spacer(modifier = Modifier.height(10.dp))
                                val isSafe = totalPredictedFinalScore >= passingGrade
                                val requiresPerfectScore = requiredAvgOnPending > 95.0

                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = if (requiresPerfectScore) Icons.Rounded.Campaign else if (isSafe) Icons.Rounded.CheckCircleOutline else Icons.Rounded.Warning,
                                        contentDescription = null,
                                        tint = if (requiresPerfectScore) Terracotta else if (isSafe) StatusGreen else StatusAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = when {
                                                isSafe -> {
                                                    "Tu tendencia de rendimiento es favorable. Manteniendo tu promedio de ${String.format(Locale.US, "%.0f", runningAvg)}% aprobaras con un aproximado de ${String.format(Locale.US, "%.1f", totalPredictedFinalScore)} puntos."
                                                }
                                                requiresPerfectScore -> {
                                                    "Exigencia extrema. Necesitas promediar ${String.format(Locale.US, "%.1f", requiredAvgOnPending)}% en el tramo restante. El margen de error es minimo."
                                                }
                                                else -> {
                                                    "Tu tendencia de ${String.format(Locale.US, "%.0f", runningAvg)}% es insuficiente para aprobar directamente. Necesitas elevar el promedio de tus calificaciones a un ${String.format(Locale.US, "%.1f", requiredAvgOnPending)}% en las siguientes evaluaciones."
                                                }
                                            },
                                            fontSize = 9.sp,
                                            color = if (requiresPerfectScore) Terracotta else if (isSafe) StatusGreen else StatusAmber,
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = 12.sp
                                        )
                                    }
                                }
                            } else if (isImpossible) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ErrorOutline,
                                        contentDescription = null,
                                        tint = Terracotta,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Matematicamente estas fuera del rango de aprobacion directa. Recomendamos estructurar de inmediato un calendario de repaso para la Convocatoria.",
                                        fontSize = 9.sp,
                                        color = Terracotta,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 12.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}
