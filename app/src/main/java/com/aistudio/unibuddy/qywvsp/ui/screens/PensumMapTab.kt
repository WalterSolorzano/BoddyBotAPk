package com.aistudio.unibuddy.qywvsp.ui.screens

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.unibuddy.qywvsp.data.AcademicRecordWithSubject
import com.aistudio.unibuddy.qywvsp.data.AssessmentStatus
import com.aistudio.unibuddy.qywvsp.data.Professor
import com.aistudio.unibuddy.qywvsp.data.StaticPensumSubject
import kotlin.math.roundToInt

enum class NodeState {
    PASSED_FIRST, PASSED_RECOVERY, ONGOING, LOCKED, NO_DATA
}

fun determineNodeState(subjectCode: String, subjectName: String, history: List<AcademicRecordWithSubject>, staticPensum: List<StaticPensumSubject>, ongoingSubjects: List<String>): NodeState {

    val records = history.filter { it.subjectName == subjectCode || it.subjectName.equals(subjectName, ignoreCase = true) }
    
    val passedRecords = records.filter { it.record.grade >= 60.0 }
    if (passedRecords.isNotEmpty()) {
        val anyFirst = passedRecords.any { it.record.status == AssessmentStatus.NF_R }
        return if (anyFirst) NodeState.PASSED_FIRST else NodeState.PASSED_RECOVERY
    }
    
    if (ongoingSubjects.any { it.equals(subjectName, ignoreCase = true) }) {
        return NodeState.ONGOING
    }
    
    val subject = staticPensum.find { it.code == subjectCode } ?: return NodeState.NO_DATA
    val prereqs = subject.prereqs
    if (prereqs.isEmpty()) return NodeState.NO_DATA
    
    val prereqsPassed = prereqs.all { prereqCode ->
        val prereqSubject = staticPensum.find { it.code == prereqCode }
        val prereqName = prereqSubject?.name ?: ""
        val pRecords = history.filter { it.subjectName == prereqCode || it.subjectName.equals(prereqName, ignoreCase = true) }
        pRecords.any { it.record.grade >= 60.0 }
    }
    
    return if (prereqsPassed) NodeState.NO_DATA else NodeState.LOCKED

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PensumMapTab(staticPensum: List<StaticPensumSubject>, history: List<AcademicRecordWithSubject>, professors: List<Professor>, ongoingSubjects: List<String>) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var selectedSubject by remember { mutableStateOf<StaticPensumSubject?>(null) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Layout configuration in DP
    val nodeWidth = 140.dp
    val nodeHeight = 60.dp
    val gapX = 40.dp
    val gapY = 40.dp
    
    val positions = remember(staticPensum) {
        val posMap = mutableMapOf<String, Pair<Int, Int>>() // X, Y indices
        val cols = staticPensum.groupBy { it.semester }
        cols.forEach { (sem, subjects) ->
            subjects.forEachIndexed { index, sub ->
                posMap[sub.code] = Pair(sem - 1, index)
            }
        }
        posMap
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.4f, 2.0f)
                    val newOffset = offset + pan
                    // Limit panning to prevent scrolling infinitely into blank space
                    offset = Offset(
                        x = newOffset.x.coerceIn(-1200f, 600f),
                        y = newOffset.y.coerceIn(-800f, 400f)
                    )
                }
            }
    ) {
        // Container for scalable/pannable content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        ) {
            // Lines layer
            Canvas(modifier = Modifier.fillMaxSize()) {
                val wPx = nodeWidth.toPx()
                val hPx = nodeHeight.toPx()
                val gxPx = gapX.toPx()
                val gyPx = gapY.toPx()
                
                staticPensum.forEach { subject ->
                    val toIndices = positions[subject.code] ?: return@forEach
                    val toX = toIndices.first * (wPx + gxPx)
                    val toY = toIndices.second * (hPx + gyPx)
                    
                    subject.prereqs.forEach { prereqCode ->
                        val fromIndices = positions[prereqCode] ?: return@forEach
                        val fromX = fromIndices.first * (wPx + gxPx)
                        val fromY = fromIndices.second * (hPx + gyPx)
                        
                        val prereqState = determineNodeState(prereqCode, staticPensum.find { it.code == prereqCode }?.name ?: "", history, staticPensum, ongoingSubjects)
                        val isPassed = prereqState == NodeState.PASSED_FIRST || prereqState == NodeState.PASSED_RECOVERY
                        
                        val path = Path().apply {
                            moveTo(fromX + wPx, fromY + hPx / 2)
                            cubicTo(
                                fromX + wPx + gxPx / 2, fromY + hPx / 2,
                                toX - gxPx / 2, toY + hPx / 2,
                                toX, toY + hPx / 2
                            )
                        }
                        drawPath(
                            path = path,
                            color = if (isPassed) Color(0xFF4CAF50) else Color.LightGray.copy(alpha = 0.5f),
                            style = Stroke(width = 4f)
                        )
                    }
                }
            }
            
            // Nodes layer
            staticPensum.forEach { subject ->
                val indices = positions[subject.code] ?: return@forEach
                val state = determineNodeState(subject.code, subject.name, history, staticPensum, ongoingSubjects)
                
                val bgColor = when (state) {
                    NodeState.PASSED_FIRST -> Color(0xFF4CAF50)
                    NodeState.PASSED_RECOVERY -> Color(0xFFFFB300)
                    NodeState.ONGOING -> MaterialTheme.colorScheme.surface
                    NodeState.LOCKED -> Color(0xFFE0E0E0)
                    NodeState.NO_DATA -> Color(0xFFF5F5F5) // Could add a texture modifier here
                }
                
                val textColor = when(state) {
                    NodeState.PASSED_FIRST, NodeState.PASSED_RECOVERY -> Color.White
                    NodeState.LOCKED -> Color.Gray
                    else -> MaterialTheme.colorScheme.onSurface
                }
                
                val borderColor = if (state == NodeState.ONGOING) Color(0xFF2196F3).copy(alpha = pulseAlpha) else Color.Transparent
                val borderWidth = if (state == NodeState.ONGOING) 3.dp else 0.dp
                
                Box(
                    modifier = Modifier
                        .offset { 
                            IntOffset(
                                (indices.first * (nodeWidth.toPx() + gapX.toPx())).roundToInt(),
                                (indices.second * (nodeHeight.toPx() + gapY.toPx())).roundToInt()
                            )
                        }
                        .size(nodeWidth, nodeHeight)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgColor)
                        .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
                        .clickable { selectedSubject = subject }
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
                        when (state) {
                            NodeState.PASSED_FIRST -> Icon(Icons.Filled.CheckCircle, "Passed", tint = Color.White, modifier = Modifier.size(16.dp))
                            NodeState.PASSED_RECOVERY -> Icon(Icons.Filled.Star, "Recovery", tint = Color.White, modifier = Modifier.size(16.dp))
                            NodeState.LOCKED -> Icon(Icons.Filled.Lock, "Locked", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            else -> {}
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = subject.name,
                            color = textColor,
                            fontSize = 11.sp,
                            maxLines = 2,
                            lineHeight = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        // Legend
        Card(
            modifier = Modifier.align(Alignment.TopCenter).padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f))
        ) {
            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LegendItem(Color(0xFF4CAF50), "1ra Conv")
                LegendItem(Color(0xFFFFB300), "Recup/Verano")
                LegendItem(Color(0xFF2196F3), "Cursando")
                LegendItem(Color(0xFFE0E0E0), "Bloqueada")
            }
        }
    }
    
    // Bottom sheet
    selectedSubject?.let { subject ->
        ModalBottomSheet(onDismissRequest = { selectedSubject = null }) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text(subject.name, style = MaterialTheme.typography.titleLarge)
                Text("Código: ${subject.code} | Semestre: ${subject.semester} | Categoría: ${subject.category}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                
                val subjectRecords = history.filter { it.subjectName == subject.code }
                if (subjectRecords.isEmpty()) {
                    Text("No hay registros históricos para esta materia.", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                } else {
                    subjectRecords.forEach { rec ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Estado: ${rec.record.status.name}", fontWeight = FontWeight.Bold)
                                Text("Nota Final: ${rec.record.grade}")
                                val prof = professors.find { it.id == rec.record.professorId }
                                if (prof != null) {
                                    Text("Profesor: ${prof.name}")
                                    if (rec.record.rating != null) {
                                        Text("Tu Valoración: ${rec.record.rating}/5")
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
