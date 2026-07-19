package com.aistudio.unibuddy.qywvsp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
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
    mainColor: Color,
    hasGrades: Boolean
) {
    val animatedTrend by animateFloatAsState(
        targetValue = if (hasGrades) trendScore.toFloat().coerceIn(0f, maxScore.toFloat()) else 0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "trend_anim"
    )
    val animatedRequired by animateFloatAsState(
        targetValue = if (hasGrades) requiredScore.toFloat().coerceIn(0f, maxScore.toFloat()) else 0f,
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

            if (hasGrades) {
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
            } else {
                // Draw a beautiful dashed/dotted track in center to indicate no grades yet
                val innerOffset = outerStroke + gap
                val innerTopLeft = androidx.compose.ui.geometry.Offset(innerOffset + innerStroke / 2, innerOffset + innerStroke / 2)
                val innerSize = androidx.compose.ui.geometry.Size(
                    width - (innerOffset * 2) - innerStroke,
                    (height - innerOffset) * 2 - innerStroke
                )
                drawArc(
                    color = Color(0xFFCBD5E1),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = innerTopLeft,
                    size = innerSize,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // Concentric Info Display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = 25.dp)
        ) {
            if (hasGrades) {
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
            } else {
                Text(
                    text = "Sin Notas",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateGray
                )
                Text(
                    text = "Nuevos Datos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NavyBlue
                )
                Text(
                    text = "Registra una calificacion",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium,
                    color = SlateGray
                )
            }
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

    // Index tracking for the Subject Swipeable Carousel
    var selectedSubjectIndex by remember { mutableStateOf(0) }
    val currentIndex = if (subjects.isNotEmpty()) {
        selectedSubjectIndex.coerceIn(0, subjects.lastIndex)
    } else 0

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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        BuddyMascot(
                            modifier = Modifier.size(90.dp),
                            pose = "sleeping"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Aún no registramos nada",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyBlue
                        )
                        Text(
                            text = "Registra tus asignaturas y notas en el panel principal para proyectar tus promedios.",
                            fontSize = 11.sp,
                            color = SlateGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp)
                        )
                    }
                } else {
                    // HORIZONTAL SUBJECT SELECTOR CHIPS
                    val chipScrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(chipScrollState)
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        subjects.forEachIndexed { index, sub ->
                            val isSelected = index == currentIndex
                            val pillBg = if (isSelected) mainColor.copy(alpha = 0.12f) else Color(0xFFF1F5F9)
                            val pillBorder = if (isSelected) mainColor.copy(alpha = 0.3f) else Color.Transparent
                            val pillText = if (isSelected) mainColor else SlateGray
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(pillBg)
                                    .border(1.dp, pillBorder, RoundedCornerShape(12.dp))
                                    .clickable { selectedSubjectIndex = index }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = sub.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = pillText
                                )
                            }
                        }
                    }

                    // Slide transition between subjects
                    AnimatedContent(
                        targetState = currentIndex,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> -width } + fadeOut()
                                )
                            } else {
                                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> width } + fadeOut()
                                )
                            }.using(
                                SizeTransform(clip = false)
                            )
                        },
                        label = "subject_slide"
                    ) { targetIndex ->
                        val subject = subjects[targetIndex]
                        val subAssessments = assessments.filter { it.subjectId == subject.id }
                        val completed = subAssessments.filter { it.grade != null }
                        val pending = subAssessments.filter { it.grade == null }
                        
                        val hasGrades = completed.isNotEmpty()

                        val accumulatedScore = completed.sumOf { (it.grade ?: 0.0) * (it.percentage / 100.0) }
                        val completedPercentage = completed.sumOf { it.percentage }
                        val remainingPercentage = pending.sumOf { it.percentage }

                        // STATISTICAL TREND CALCULATION
                        // Uses running weighted average with recent momentum weighting (65% historic, 35% recent momentum)
                        val runningAvg = if (hasGrades) {
                            val overallAvg = completed.sumOf { (it.grade ?: 0.0) * it.percentage } / completedPercentage
                            val sorted = completed.sortedBy { it.id }
                            if (sorted.size >= 2) {
                                val lastTwoAvg = sorted.takeLast(2).map { it.grade ?: 0.0 }.average()
                                (overallAvg * 0.65) + (lastTwoAvg * 0.35)
                            } else {
                                overallAvg
                            }
                        } else {
                            0.0 // No grades yet - set to 0.0 to prevent showing fake 70.0
                        }

                        val predictedPendingScore = remainingPercentage * (runningAvg / 100.0)
                        val totalPredictedFinalScore = accumulatedScore + predictedPendingScore

                        val pointsNeeded = (passingGrade - accumulatedScore).coerceAtLeast(0.0)
                        val requiredAvgOnPending = if (remainingPercentage > 0) {
                            (pointsNeeded / (remainingPercentage / 100.0))
                        } else {
                            if (!hasGrades) passingGrade else 0.0
                        }

                        val isAlreadyPassed = accumulatedScore >= passingGrade
                        val isImpossible = accumulatedScore + remainingPercentage < passingGrade

                        var showBreakdown by remember(targetIndex) { mutableStateOf(false) }

                        val urgencyColor = when {
                            isAlreadyPassed -> StatusGreen
                            isImpossible -> Terracotta
                            !hasGrades -> SlateGray
                            requiredAvgOnPending > 85.0 -> Terracotta
                            totalPredictedFinalScore >= passingGrade + 5.0 -> StatusGreen
                            else -> StatusAmber
                        }
                        val urgencyLabel = when {
                            isAlreadyPassed -> "Aprobado"
                            isImpossible -> "Convocatoria"
                            !hasGrades -> "Sin Notas"
                            requiredAvgOnPending > 85.0 -> "Peligro: Exigencia Alta"
                            totalPredictedFinalScore >= passingGrade + 5.0 -> "Favorable"
                            else -> "Ajustado"
                        }

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
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = NavyBlue
                                        )
                                        Text(
                                            text = if (hasGrades) {
                                                "Acumulado real: ${String.format(Locale.US, "%.1f", accumulatedScore)} pts (${completedPercentage}% evaluado)"
                                            } else {
                                                "Sin calificaciones registradas"
                                            },
                                            fontSize = 10.sp,
                                            color = SlateGray,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(urgencyColor.copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = urgencyLabel,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = urgencyColor
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Rich animated radial comparison visualization
                                ComparativeGauge(
                                    trendScore = totalPredictedFinalScore,
                                    requiredScore = if (hasActivatedEmergencyScan && !isImpossible && !isAlreadyPassed) requiredAvgOnPending else 0.0,
                                    passingGrade = passingGrade,
                                    mainColor = urgencyColor,
                                    hasGrades = hasGrades
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
                                            text = if (hasGrades) {
                                                "Promedio Actual: ${String.format(Locale.US, "%.1f", runningAvg)}%"
                                            } else {
                                                "Promedio: Pendiente"
                                            },
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
                                                text = if (hasGrades) {
                                                    "${String.format(Locale.US, "%.1f", totalPredictedFinalScore)} pts"
                                                } else {
                                                    "S/N"
                                                },
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (!hasGrades) SlateGray else if (totalPredictedFinalScore >= passingGrade) StatusGreen else StatusAmber
                                            )
                                            Text(
                                                text = if (hasGrades) "Nota proyectada final" else "Sin notas registradas",
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
                                                    text = when {
                                                        isImpossible -> "Fallo"
                                                        isAlreadyPassed -> "Aprobado"
                                                        else -> "${String.format(Locale.US, "%.1f", requiredAvgOnPending)}%"
                                                    },
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isImpossible) Terracotta else if (isAlreadyPassed) StatusGreen else ProRed
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

                                // Helpful explainer of what required minimum is
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF1F5F9).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "El Minimo Requerido representa la calificacion promedio que debes obtener en todas tus evaluaciones pendientes para alcanzar la nota minima de aprobacion (${passingGrade.toInt()} puntos).",
                                        fontSize = 9.sp,
                                        color = SlateGray,
                                        lineHeight = 12.sp
                                    )
                                }

                                // ITEM BY ITEM REALISTIC VS REQUIRED COMPARISON
                                if (!hasGrades) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Bone),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.School,
                                                contentDescription = null,
                                                tint = SlateGray.copy(alpha = 0.6f),
                                                modifier = Modifier.size(36.dp)
                                            )
                                            Text(
                                                text = "Sin calificaciones registradas",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = NavyBlue
                                            )
                                            Text(
                                                text = "No se registran notas para este periodo todavia. Agrega tus calificaciones en el panel principal para proyectar tus promedios y simular escenarios.",
                                                fontSize = 11.sp,
                                                color = SlateGray,
                                                textAlign = TextAlign.Center,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                } else if (pending.isNotEmpty() && !isAlreadyPassed && !isImpossible) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    // Main Aggregate Indicator
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = urgencyColor.copy(alpha = 0.08f)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, urgencyColor.copy(alpha = 0.25f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(urgencyColor.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Analytics,
                                                    contentDescription = null,
                                                    tint = urgencyColor,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Meta de Aprobacion",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = NavyBlue
                                                )
                                                Text(
                                                    text = if (hasActivatedEmergencyScan) {
                                                        "Debes promediar un ${String.format(Locale.US, "%.1f", requiredAvgOnPending)}% en lo pendiente."
                                                    } else {
                                                        "Pulsa 'Calcular Minimo' para estimar la meta de tus pendientes."
                                                    },
                                                    fontSize = 10.sp,
                                                    color = SlateGray,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Expandable toggle button
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showBreakdown = !showBreakdown }
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (showBreakdown) "Ocultar desglose completo" else "Ver desglose completo",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = urgencyColor
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = if (showBreakdown) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                            contentDescription = null,
                                            tint = urgencyColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    AnimatedVisibility(
                                        visible = showBreakdown,
                                        enter = expandVertically() + fadeIn(),
                                        exit = shrinkVertically() + fadeOut()
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            pending.forEach { assessment ->
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Bone),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(12.dp),
                                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = assessment.name,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = NavyBlue
                                                            )
                                                            Text(
                                                                text = "Peso: ${assessment.percentage}%",
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Medium,
                                                                color = SlateGray
                                                            )
                                                        }

                                                        // Compact Horizontal Progress Bar
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(20.dp),
                                                            contentAlignment = Alignment.CenterStart
                                                        ) {
                                                            // Track background
                                                            Box(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .height(6.dp)
                                                                    .clip(CircleShape)
                                                                    .background(Color(0xFFE2E8F0))
                                                            )
                                                            // Projected progress fill
                                                            val projCoerced = (runningAvg / 100.0).coerceIn(0.0, 1.0).toFloat()
                                                            Box(
                                                                modifier = Modifier
                                                                    .fillMaxWidth(projCoerced)
                                                                    .height(6.dp)
                                                                    .clip(CircleShape)
                                                                    .background(urgencyColor)
                                                            )
                                                            // Required marker tick
                                                            if (hasActivatedEmergencyScan && !isImpossible && !isAlreadyPassed) {
                                                                val reqCoerced = (requiredAvgOnPending / 100.0).coerceIn(0.0, 1.0).toFloat()
                                                                Box(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth(reqCoerced)
                                                                        .height(14.dp)
                                                                ) {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .align(Alignment.CenterEnd)
                                                                            .width(3.dp)
                                                                            .height(14.dp)
                                                                            .clip(RoundedCornerShape(1.dp))
                                                                            .background(ProRed)
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        // Labels
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(
                                                                text = "Proyeccion: ${runningAvg.toInt()}%",
                                                                fontSize = 9.sp,
                                                                color = SlateGray,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                            if (hasActivatedEmergencyScan && !isImpossible && !isAlreadyPassed) {
                                                                Text(
                                                                    text = "Requerido: ${requiredAvgOnPending.toInt()}%",
                                                                    fontSize = 9.sp,
                                                                    color = ProRed,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // FOOTER WARNING / INTELLIGENT REALISTIC FEEDBACK
                                if (!hasGrades) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Info,
                                            contentDescription = null,
                                            tint = ProBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Aún no registramos calificaciones para esta asignatura. Registra tus notas en el panel principal para proyectar tu tendencia real automáticamente. Por ahora, necesitas promediar un ${passingGrade.toInt()}% en todas tus evaluaciones.",
                                            fontSize = 9.sp,
                                            color = SlateGray,
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = 12.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                } else if (!isAlreadyPassed && !isImpossible && remainingPercentage > 0) {
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
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}
