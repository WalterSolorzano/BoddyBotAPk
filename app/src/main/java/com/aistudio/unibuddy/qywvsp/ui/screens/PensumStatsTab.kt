package com.aistudio.unibuddy.qywvsp.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.unibuddy.qywvsp.data.AcademicRecordWithSubject
import com.aistudio.unibuddy.qywvsp.data.StaticPensumSubject
import com.aistudio.unibuddy.qywvsp.ui.components.BuddyMascot
import com.aistudio.unibuddy.qywvsp.ui.components.BuddyMascot

val categoryColors = mapOf(
    "General" to Color(0xFF607D8B),
    "Ingeniería" to Color(0xFFE91E63),
    "Matemáticas" to Color(0xFF3F51B5),
    "Computación" to Color(0xFF009688),
    "Economía y Negocios" to Color(0xFFFF9800),
    "Humanidades" to Color(0xFF795548),
    "Ingeniería Industrial" to Color(0xFF9C27B0),
    "Proyectos" to Color(0xFF8BC34A)
)

fun getCategoryColor(category: String): Color {
    return categoryColors[category] ?: Color.Gray
}

@Composable
fun PensumStatsTab(staticPensum: List<StaticPensumSubject>, history: List<AcademicRecordWithSubject>) {
    val scrollState = rememberScrollState()
    
    // Derived metrics
    val totalSubjects = staticPensum.size
    val passedSubjects = history.filter { it.record.grade >= 60.0 }.distinctBy { it.subjectName }
    val progressPercent = if (totalSubjects > 0) passedSubjects.size.toFloat() / totalSubjects else 0f
    
    val allGrades = history.filter { it.record.grade > 0.0 }
    val average = if (allGrades.isNotEmpty()) allGrades.map { it.record.grade }.average() else 0.0
    
    // Category stats
    val categoryStats = mutableMapOf<String, MutableList<Double>>()
    allGrades.forEach { rec ->
        val cat = staticPensum.find { it.code == rec.subjectName }?.category ?: "General"
        categoryStats.getOrPut(cat) { mutableListOf() }.add(rec.record.grade)
    }
    val categoryAverages = categoryStats.mapValues { it.value.average() }.toList().sortedByDescending { it.second }
    
    // Semester stats
    val semesterAverages = allGrades.groupBy { it.semester }.mapValues { entry ->
        entry.value.map { it.record.grade }.average()
    }.toList().sortedBy { it.first } // Very basic chronological sort assuming semester format like "1er Semestre 2024"
    
    // Attempts
    val firstAttemptCount = passedSubjects.count { sub -> history.count { it.subjectName == sub.subjectName } == 1 }
    val recoveryCount = passedSubjects.size - firstAttemptCount
    
    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Hero Progress
        HeroProgressCard(progressPercent, passedSubjects.size, totalSubjects, categoryAverages.firstOrNull()?.first)
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            // Average
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Promedio Gral.", fontSize = 12.sp, color = Color.Gray)
                    Text(String.format("%.1f", average), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
            
            // Projections
            Card(modifier = Modifier.weight(1f)) {
                 Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Semestres Rest.", fontSize = 12.sp, color = Color.Gray)
                    val remaining = staticPensum.filter { sub -> passedSubjects.none { it.subjectName == sub.code } }.groupBy { it.semester }.size
                    Text("$remaining", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
        
        if (categoryAverages.isNotEmpty()) {
            CategoryPerformanceCard(categoryAverages)
        }
        
        if (semesterAverages.isNotEmpty()) {
            SemesterEvolutionCard(semesterAverages)
        }
        
        AttemptsCard(firstAttemptCount, recoveryCount)
        
        HorizontalTimeline(history)
    }
}

@Composable
fun HeroProgressCard(progress: Float, passed: Int, total: Int, bestCat: String?) {
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        animatedProgress.animateTo(progress, tween(1500))
    }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        startAngle = 0f, sweepAngle = 360f, useCenter = false,
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = com.aistudio.unibuddy.qywvsp.ui.theme.ProBlue,
                        startAngle = -90f, sweepAngle = animatedProgress.value * 360f, useCenter = false,
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                BuddyMascot(
                    modifier = Modifier.size(80.dp).background(com.aistudio.unibuddy.qywvsp.ui.theme.SoftPurple, androidx.compose.foundation.shape.CircleShape),
                    pose = if (progress >= 1f) "celebrating" else "idle",
                    isHappy = (progress >= 1f),
                    isWorried = false,
                    mainColor = com.aistudio.unibuddy.qywvsp.ui.theme.NavyBlue
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Avance: ${String.format("%.1f", progress * 100)}%", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("$passed de $total materias", color = Color.Gray)
            bestCat?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Especialista en $it", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun CategoryPerformanceCard(categoryAverages: List<Pair<String, Double>>) {
    val animatedWidth = remember { Animatable(0f) }
    LaunchedEffect(categoryAverages) {
        animatedWidth.animateTo(1f, tween(1000))
    }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Rendimiento por Categoría", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            categoryAverages.forEachIndexed { index, pair ->
                val (cat, avg) = pair
                val isBest = index == 0
                val isWorst = index == categoryAverages.lastIndex && categoryAverages.size > 1
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Text(cat.take(15), modifier = Modifier.weight(0.3f), fontSize = 12.sp)
                    Box(modifier = Modifier.weight(0.6f).height(12.dp)) {
                         Canvas(modifier = Modifier.fillMaxSize()) {
                             val w = size.width * (avg.toFloat() / 100f) * animatedWidth.value
                             drawRoundRect(
                                 color = getCategoryColor(cat),
                                 size = Size(w, size.height),
                                 cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                             )
                         }
                    }
                    Text(String.format("%.1f", avg), modifier = Modifier.weight(0.15f), fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.End)
                    if (isBest) {
                        BuddyMascot(modifier = Modifier.size(24.dp), pose = "celebrating", isHappy = true, isWorried = false, mainColor = com.aistudio.unibuddy.qywvsp.ui.theme.NavyBlue)
                    } else if (isWorst) {
                        BuddyMascot(modifier = Modifier.size(24.dp), pose = "idle", isHappy = false, isWorried = true, mainColor = com.aistudio.unibuddy.qywvsp.ui.theme.NavyBlue)
                    } else {
                        Spacer(modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SemesterEvolutionCard(semesterAverages: List<Pair<String, Double>>) {
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(semesterAverages) {
        animatedProgress.animateTo(1f, tween(1200))
    }
    
    Card(modifier = Modifier.fillMaxWidth()) {
         Column(modifier = Modifier.padding(16.dp)) {
            Text("Evolución del Promedio", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (semesterAverages.size < 2) return@Canvas
                    
                    val maxVal = 100f
                    val minVal = 0f
                    
                    val w = size.width
                    val h = size.height
                    
                    val points = semesterAverages.mapIndexed { i, pair ->
                        val x = (i.toFloat() / (semesterAverages.size - 1)) * w
                        val y = h - ((pair.second.toFloat() - minVal) / (maxVal - minVal)) * h
                        Offset(x, y)
                    }
                    
                    val path = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until (points.size * animatedProgress.value).toInt()) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }
                    
                    drawPath(path, color = Color(0xFF2196F3), style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
                    
                    // Highlight best and worst
                    val bestIdx = semesterAverages.indexOfMaxBy { it.second }
                    val worstIdx = semesterAverages.indexOfMinBy { it.second }
                    
                    if (bestIdx >= 0 && bestIdx < (points.size * animatedProgress.value).toInt()) {
                        drawCircle(color = Color(0xFF4CAF50), radius = 6.dp.toPx(), center = points[bestIdx])
                    }
                    if (worstIdx >= 0 && worstIdx < (points.size * animatedProgress.value).toInt() && worstIdx != bestIdx) {
                        drawCircle(color = Color(0xFFF44336), radius = 6.dp.toPx(), center = points[worstIdx])
                    }
                }
            }
            // Basic X axis labels
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(semesterAverages.first().first.take(5), fontSize = 10.sp, color = Color.Gray)
                if (semesterAverages.size > 1) {
                    Text(semesterAverages.last().first.take(5), fontSize = 10.sp, color = Color.Gray)
                }
            }
         }
    }
}

@Composable
fun AttemptsCard(firstAttemptCount: Int, recoveryCount: Int) {
    val total = firstAttemptCount + recoveryCount
    val pFirst = if (total > 0) firstAttemptCount.toFloat() / total else 0f
    
    val animatedWidth = remember { Animatable(0f) }
    LaunchedEffect(total) { animatedWidth.animateTo(1f, tween(1000)) }
    
    Card(modifier = Modifier.fillMaxWidth()) {
         Column(modifier = Modifier.padding(16.dp)) {
            Text("Convocatorias Aprobadas", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(modifier = Modifier.fillMaxWidth().height(24.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w1 = size.width * pFirst * animatedWidth.value
                    val w2 = size.width * (1f - pFirst) * animatedWidth.value
                    
                    drawRoundRect(
                        color = Color(0xFF4CAF50),
                        topLeft = Offset.Zero,
                        size = Size(w1, size.height),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color(0xFFFFB300),
                        topLeft = Offset(w1, 0f),
                        size = Size(w2, size.height),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("1ra Conv: $firstAttemptCount", color = Color(0xFF388E3C), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Recup/Verano: $recoveryCount", color = Color(0xFFF57C00), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
         }
    }
}

@Composable
fun HorizontalTimeline(history: List<AcademicRecordWithSubject>) {
    val semesters = history.groupBy { it.semester }.toList().sortedBy { it.first }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Línea de Tiempo", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            semesters.forEach { (sem, records) ->
                val passed = records.count { it.record.grade >= 60.0 }
                val avg = records.filter { it.record.grade > 0 }.map { it.record.grade }.average().let { if (it.isNaN()) 0.0 else it }
                Card(modifier = Modifier.width(120.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(sem, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                        Text("Prom: ${String.format("%.1f", avg)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        Text("Aprobadas: $passed", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

// Utils
fun <T> List<T>.indexOfMaxBy(selector: (T) -> Double): Int {
    if (isEmpty()) return -1
    var maxIdx = 0
    var maxVal = selector(this[0])
    for (i in 1..lastIndex) {
        val v = selector(this[i])
        if (v > maxVal) {
            maxVal = v
            maxIdx = i
        }
    }
    return maxIdx
}

fun <T> List<T>.indexOfMinBy(selector: (T) -> Double): Int {
    if (isEmpty()) return -1
    var minIdx = 0
    var minVal = selector(this[0])
    for (i in 1..lastIndex) {
        val v = selector(this[i])
        if (v < minVal) {
            minVal = v
            minIdx = i
        }
    }
    return minIdx
}
