package com.aistudio.unibuddy.qywvsp.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.unibuddy.qywvsp.data.AcademicRecordWithSubject
import com.aistudio.unibuddy.qywvsp.data.StaticPensumSubject
import com.aistudio.unibuddy.qywvsp.ui.components.BuddyMascot
import com.aistudio.unibuddy.qywvsp.ui.theme.NavyBlue
import com.aistudio.unibuddy.qywvsp.ui.theme.ProBlue
import com.aistudio.unibuddy.qywvsp.ui.theme.Amber
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PensumStatsTab(
    staticPensum: List<StaticPensumSubject>, 
    history: List<AcademicRecordWithSubject>,
    passingGrade: Double = 60.0
) {
    val scrollState = rememberScrollState()

    // 1. Progress Metrics
    val totalSubjects = staticPensum.size
    val passedSubjects = history.filter { it.record.grade >= passingGrade }.distinctBy { it.subjectCode.ifEmpty { it.subjectName } }
    val progressPercent = if (totalSubjects > 0) passedSubjects.size.toFloat() / totalSubjects else 0f

    // 2. Weighted Average
    val allGrades = history.filter { it.record.grade > 0.0 }
    val weightedAverage = remember(allGrades) {
        val totalCredits = allGrades.sumOf { it.credits }
        if (totalCredits > 0.0) {
            allGrades.sumOf { it.record.grade * it.credits } / totalCredits
        } else {
            0.0
        }
    }

    // 3. Category Radar values (Map categories to 5 axes for a beautiful pentagon)
    val radarCategories = listOf("General", "Ingeniería", "Matemáticas", "Computación", "Proyectos")
    val radarValues = remember(allGrades, staticPensum) {
        radarCategories.map { category ->
            val matchGrades = allGrades.filter { rec ->
                val matchedStatic = staticPensum.find { 
                    it.code.equals(rec.subjectCode, ignoreCase = true) || 
                    it.name.equals(rec.subjectName, ignoreCase = true) 
                }
                val cat = matchedStatic?.category ?: "General"
                cat.contains(category, ignoreCase = true) || 
                (category == "Otros" && !radarCategories.any { cat.contains(it, ignoreCase = true) })
            }
            if (matchGrades.isNotEmpty()) matchGrades.map { it.record.grade }.average().toFloat() else 75f // Default aesthetic balance
        }
    }

    // 4. Semester Evolution
    val semesterAverages = remember(allGrades) {
        allGrades.groupBy { it.semester }.mapValues { entry ->
            entry.value.map { it.record.grade }.average()
        }.toList().sortedBy { it.first }
    }

    // 5. Attempts
    val firstAttemptCount = passedSubjects.count { sub -> 
        history.count { it.subjectCode.equals(sub.subjectCode, ignoreCase = true) || it.subjectName.equals(sub.subjectName, ignoreCase = true) } == 1 
    }
    val recoveryCount = (passedSubjects.size - firstAttemptCount).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Circular Progress with Mascot at the Center
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Progreso de Carrera",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = NavyBlue
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(170.dp)) {
                    val animatedProgress = remember { Animatable(0f) }
                    LaunchedEffect(progressPercent) {
                        animatedProgress.animateTo(progressPercent, tween(1500, easing = FastOutSlowInEasing))
                    }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = Color(0xFFF1F5F9),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = ProBlue,
                            startAngle = -90f,
                            sweepAngle = animatedProgress.value * 360f,
                            useCenter = false,
                            style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    BuddyMascot(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEEF2F6)),
                        pose = if (progressPercent >= 0.8f) "celebrating" else "idle",
                        isHappy = (progressPercent >= 0.5f),
                        isWorried = false,
                        mainColor = NavyBlue
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "${String.format("%.1f", progressPercent * 100)}% Completado",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = NavyBlue
                )
                Text(
                    text = "$passedSubjects.size de $totalSubjects asignaturas aprobadas",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        // General Weighted Average & Remainder Projections
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Promedio Ponderado", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (weightedAverage > 0.0) String.format("%.2f", weightedAverage) else "S/D",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProBlue
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Semestres Restantes", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    val remainingSemesters = remember(staticPensum, passedSubjects) {
                        staticPensum.filter { sub -> 
                            passedSubjects.none { 
                                it.subjectCode.equals(sub.code, ignoreCase = true) || 
                                it.subjectName.equals(sub.name, ignoreCase = true) 
                            }
                        }.groupBy { it.semester }.size
                    }
                    Text(
                        text = "$remainingSemesters sem.",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Amber
                    )
                }
            }
        }

        // Radar / Spider Chart for Category Performance
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Áreas de Rendimiento (Radar)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = NavyBlue
                )
                Text(
                    text = "Análisis académico por categoría curricular",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    RadarSpiderChart(categories = radarCategories, values = radarValues)
                }

                Spacer(modifier = Modifier.height(12.dp))
                // Categories Legend
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    radarCategories.forEachIndexed { i, cat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3B82F6))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$cat (${String.format("%.1f", radarValues[i])})",
                                fontSize = 10.sp,
                                color = NavyBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Animated Semester Evolution Chart
        if (semesterAverages.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Evolución de Promedio por Semestre",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = NavyBlue
                    )
                    Text(
                        text = "Trayectoria histórica con semestres destacados",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedTrajectoryLineChart(semesterAverages)
                }
            }
        }

        // Segmented Experience Bar for Exam Attempts
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Proporción de Convocatorias",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = NavyBlue
                )
                Spacer(modifier = Modifier.height(12.dp))

                val totalPassed = firstAttemptCount + recoveryCount
                val ratioFirst = if (totalPassed > 0) firstAttemptCount.toFloat() / totalPassed else 1.0f

                // Experience-style segmented progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE2E8F0))
                ) {
                    val animatedWidth = remember { Animatable(0f) }
                    LaunchedEffect(ratioFirst) {
                        animatedWidth.animateTo(ratioFirst, tween(1200, easing = FastOutSlowInEasing))
                    }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val firstWidth = size.width * animatedWidth.value
                        val recoveryWidth = size.width * (1f - animatedWidth.value)

                        // 1st attempts (Emerald)
                        drawRect(
                            color = Color(0xFF10B981),
                            topLeft = Offset.Zero,
                            size = Size(firstWidth, size.height)
                        )
                        // Recoveries (Orange/Amber)
                        drawRect(
                            color = Color(0xFFF59E0B),
                            topLeft = Offset(firstWidth, 0f),
                            size = Size(recoveryWidth, size.height)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("1ra Convocatoria: $firstAttemptCount", fontSize = 11.sp, color = NavyBlue, fontWeight = FontWeight.Bold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Recuperación/Verano: $recoveryCount", fontSize = 11.sp, color = NavyBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RadarSpiderChart(categories: List<String>, values: List<Float>) {
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(values) {
        animatedProgress.animateTo(1f, tween(1200, easing = FastOutSlowInEasing))
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width.coerceAtMost(size.height) / 2 * 0.75f
        val numAxes = categories.size
        val angleStep = (2 * Math.PI / numAxes).toFloat()

        // 1. Draw web rings (3 concentric rings: 33%, 66%, 100%)
        val rings = listOf(0.33f, 0.66f, 1f)
        rings.forEach { ring ->
            val ringPath = Path()
            for (i in 0 until numAxes) {
                val angle = i * angleStep - Math.PI.toFloat() / 2
                val r = radius * ring
                val x = center.x + r * cos(angle)
                val y = center.y + r * sin(angle)
                if (i == 0) ringPath.moveTo(x, y) else ringPath.lineTo(x, y)
            }
            ringPath.close()
            drawPath(
                path = ringPath,
                color = Color(0xFFCBD5E1),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // 2. Draw axis lines from center to outer ring
        for (i in 0 until numAxes) {
            val angle = i * angleStep - Math.PI.toFloat() / 2
            val axisX = center.x + radius * cos(angle)
            val axisY = center.y + radius * sin(angle)
            drawLine(
                color = Color(0xFFE2E8F0),
                start = center,
                end = Offset(axisX, axisY),
                strokeWidth = 1.dp.toPx()
            )
        }

        // 3. Draw values polygon
        val valPath = Path()
        for (i in 0 until numAxes) {
            val angle = i * angleStep - Math.PI.toFloat() / 2
            val normalizedVal = (values[i] / 100f).coerceIn(0f, 1f)
            val r = radius * normalizedVal * animatedProgress.value
            val x = center.x + r * cos(angle)
            val y = center.y + r * sin(angle)
            if (i == 0) valPath.moveTo(x, y) else valPath.lineTo(x, y)
        }
        valPath.close()
        drawPath(valPath, Color(0x333B82F6)) // Fill with transparency
        drawPath(valPath, Color(0xFF3B82F6), style = Stroke(width = 2.dp.toPx())) // Stroke

        // 4. Draw outer nodes on the value coordinates
        for (i in 0 until numAxes) {
            val angle = i * angleStep - Math.PI.toFloat() / 2
            val normalizedVal = (values[i] / 100f).coerceIn(0f, 1f)
            val r = radius * normalizedVal * animatedProgress.value
            val x = center.x + r * cos(angle)
            val y = center.y + r * sin(angle)
            drawCircle(Color(0xFF1D4ED8), radius = 4.dp.toPx(), center = Offset(x, y))
        }
    }
}

@Composable
fun AnimatedTrajectoryLineChart(semesterAverages: List<Pair<String, Double>>) {
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(semesterAverages) {
        animatedProgress.animateTo(1f, tween(1500, easing = FastOutSlowInEasing))
    }

    val infinitePulse = rememberInfiniteTransition(label = "pulse")
    val pulseSize by infinitePulse.animateFloat(
        initialValue = 4.dp.value,
        targetValue = 9.dp.value,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (semesterAverages.isEmpty()) return@Canvas

                val maxVal = 100f
                val minVal = 50f // standard pass/fail bottom bound
                val w = size.width
                val h = size.height

                val points = semesterAverages.mapIndexed { i, pair ->
                    val x = if (semesterAverages.size > 1) {
                        (i.toFloat() / (semesterAverages.size - 1)) * w
                    } else {
                        w / 2
                    }
                    val y = h - ((pair.second.toFloat() - minVal) / (maxVal - minVal)).coerceIn(0f, 1f) * h
                    Offset(x, y)
                }

                // Draw path connecting lines
                if (points.size > 1) {
                    val path = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        val numVisible = (points.size * animatedProgress.value).toInt().coerceAtLeast(1)
                        for (i in 1 until numVisible) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = Color(0xFF2563EB),
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Highlight best score and current active semester
                val maxValSem = semesterAverages.maxOfOrNull { it.second } ?: 0.0
                val bestIdx = semesterAverages.indexOfFirst { it.second == maxValSem }
                val lastIdx = semesterAverages.lastIndex

                points.forEachIndexed { idx, point ->
                    if (idx < (points.size * animatedProgress.value).toInt()) {
                        // Pulse on the current (last) semester
                        if (idx == lastIdx) {
                            drawCircle(
                                color = Color(0x443B82F6),
                                radius = pulseSize.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = Color(0xFF3B82F6),
                                radius = 5.dp.toPx(),
                                center = point
                            )
                        } else if (idx == bestIdx) {
                            // Gold star / trophy highlight for best moment
                            drawCircle(
                                color = Color(0xFFEAB308),
                                radius = 7.dp.toPx(),
                                center = point
                            )
                        } else {
                            // Regular coordinates dots
                            drawCircle(
                                color = Color(0xFF2563EB),
                                radius = 4.dp.toPx(),
                                center = point
                            )
                        }
                    }
                }
            }
        }

        // Horizontal bottom labels with highlights
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val maxValSem = semesterAverages.maxOfOrNull { it.second } ?: 0.0
            val bestIdx = semesterAverages.indexOfFirst { it.second == maxValSem }
            semesterAverages.forEachIndexed { idx, (sem, avg) ->
                val isBest = idx == bestIdx
                val isCurrent = idx == semesterAverages.lastIndex
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = sem.replace("Semestre", "Sem").take(8),
                        fontSize = 9.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCurrent) ProBlue else Color.Gray
                    )
                    Text(
                        text = String.format("%.1f", avg),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isBest) Color(0xFFEAB308) else NavyBlue
                    )
                }
            }
        }
    }
}
