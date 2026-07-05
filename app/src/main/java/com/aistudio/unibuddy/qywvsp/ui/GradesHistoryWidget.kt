package com.aistudio.unibuddy.qywvsp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.unibuddy.qywvsp.data.Assessment
import com.aistudio.unibuddy.qywvsp.ui.theme.*
import java.util.Locale

@Composable
fun GradesHistoryWidget(
    assessments: List<Assessment>,
    currentWeighted: Double,
    subjectName: String = ""
) {
    val completed = assessments.filter { it.grade != null }
    val pending = assessments.filter { it.grade == null }
    
    val currentTotalPointsEarned = completed.sumOf { it.grade ?: 0.0 }
    val remainingMaxPoints = pending.sumOf { it.percentage }
    val neededScoreFor60 = (60.0 - currentTotalPointsEarned).coerceAtLeast(0.0)
    
    val isApproved = currentTotalPointsEarned >= 60.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, Bone)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = ProBlue, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Calculadora Predictiva",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = NavyBlue
                )
            }
            if (subjectName.isNotEmpty()) {
                Text(subjectName, fontSize = 12.sp, color = SlateGray, fontWeight = FontWeight.SemiBold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Circular Progress Bar
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                    androidx.compose.material3.CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = Bone,
                        strokeWidth = 8.dp,
                    )
                    androidx.compose.material3.CircularProgressIndicator(
                        progress = { (currentTotalPointsEarned / 60.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxSize(),
                        color = if (isApproved) DarkGreen else ProBlue,
                        strokeWidth = 8.dp,
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.US, "%.1f", currentTotalPointsEarned),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = NavyBlue
                        )
                        Text("/ 60 pts", fontSize = 10.sp, color = SlateGray, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.width(24.dp))
                
                Column {
                    Text(
                        text = if (isApproved) "¡Misión Cumplida!" else "Progreso para 60pts",
                        fontSize = 13.sp,
                        color = if (isApproved) DarkGreen else SlateGray,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("Acumulado total: ${String.format(Locale.US, "%.1f", currentTotalPointsEarned)} / 100", fontSize = 11.sp, color = SlateGray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (completed.isNotEmpty()) {
                Text("Historial de Notas", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateGray)
                Spacer(modifier = Modifier.height(8.dp))
                completed.forEach { ass ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(MintGreen.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ass.name, fontWeight = FontWeight.Bold, color = NavyBlue, fontSize = 14.sp)
                            Text("Valor total: ${ass.percentage} pts", fontSize = 12.sp, color = SlateGray)
                        }
                        Text("${ass.grade} / ${ass.percentage}", fontWeight = FontWeight.Black, color = DarkGreen, fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (pending.isNotEmpty()) {
                Text("Evaluaciones Pendientes", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateGray)
                Spacer(modifier = Modifier.height(8.dp))

                val isImpossible = neededScoreFor60 > remainingMaxPoints
                
                if (isApproved) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MintGreen.copy(alpha = 0.2f))
                    ) {
                        Text("¡Ya aprobaste la clase! Todo lo que sumes ahora mejorará tu nota final.", modifier = Modifier.padding(12.dp), fontSize = 12.sp, color = DarkGreen)
                    }
                } else if (isImpossible) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Terracotta.copy(alpha = 0.1f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Terracotta, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("¡Houston, tenemos un problema! Necesitas ${String.format(Locale.US, "%.1f", neededScoreFor60)} pts, pero solo hay ${String.format(Locale.US, "%.1f", remainingMaxPoints)} pts en juego. Ya no es posible llegar a 60.", fontSize = 12.sp, color = Terracotta, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE))
                    ) {
                        Text(
                            text = "Para aprobar la clase, necesitas sumar al menos ${String.format(Locale.US, "%.1f", neededScoreFor60)} puntos en las evaluaciones restantes.",
                            modifier = Modifier.padding(12.dp),
                            fontSize = 12.sp,
                            color = Color(0xFF1967D2),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                pending.forEach { ass ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Bone.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = SlateGray, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ass.name, fontWeight = FontWeight.Bold, color = NavyBlue, fontSize = 14.sp)
                            Row {
                                Text("Vale: ${ass.percentage} pts", fontSize = 12.sp, color = SlateGray)
                                if (ass.examDate.isNotBlank()) {
                                    Text(" • ${ass.examDate}", fontSize = 12.sp, color = NavyBlue)
                                }
                            }
                        }
                        if (!isApproved && !isImpossible) {
                            // Suggest proportional points needed
                            val neededForThis = (ass.percentage / remainingMaxPoints) * neededScoreFor60
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Debes sacar al menos:", fontSize = 10.sp, color = SlateGray)
                                Text(
                                    "${String.format(Locale.US, "%.1f", neededForThis)} / ${ass.percentage}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1967D2),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            if (assessments.isEmpty()) {
                Text("No hay evaluaciones registradas.", color = SlateGray, fontSize = 14.sp, modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}
