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
    currentWeighted: Double
) {
    val completed = assessments.filter { it.grade != null }
    val pending = assessments.filter { it.grade == null }
    
    val remainingPercentage = pending.sumOf { it.percentage }
    val neededScoreFor51 = (51.0 - currentWeighted).coerceAtLeast(0.0)
    
    val targetGradePerPending = if (remainingPercentage > 0) {
        (neededScoreFor51 / (remainingPercentage / 100.0)).coerceAtLeast(0.0)
    } else {
        0.0
    }
    
    val isApproved = currentWeighted >= 51.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, Bone)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Resumen de Calificaciones",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = NavyBlue
            )
            Text(
                text = "Acumulado actual: ${String.format(Locale.US, "%.1f", currentWeighted)} / 100",
                fontSize = 13.sp,
                color = if (isApproved) DarkGreen else SlateGray,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

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
                            Text("Valor: ${ass.percentage}%", fontSize = 12.sp, color = SlateGray)
                        }
                        Text("${ass.grade}", fontWeight = FontWeight.Black, color = DarkGreen, fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (pending.isNotEmpty()) {
                Text("Evaluaciones Pendientes", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateGray)
                Spacer(modifier = Modifier.height(8.dp))

                val isImpossible = targetGradePerPending > 100.0
                
                if (isApproved) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MintGreen.copy(alpha = 0.2f))
                    ) {
                        Text("¡Ya aprobaste la materia! Todo lo que sumes ahora aumentará tu promedio final.", modifier = Modifier.padding(12.dp), fontSize = 12.sp, color = DarkGreen)
                    }
                } else if (isImpossible) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Terracotta.copy(alpha = 0.1f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Terracotta, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(" Matemáticamente inalcanzable (Necesitas >100).", fontSize = 12.sp, color = Terracotta, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE))
                    ) {
                        Text(
                            text = "Para aprobar con 51.0, necesitas promediar al menos ${String.format(Locale.US, "%.1f", targetGradePerPending)} en lo que falta.",
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
                                Text("Valor: ${ass.percentage}%", fontSize = 12.sp, color = SlateGray)
                                if (ass.examDate.isNotBlank()) {
                                    Text(" • ${ass.examDate}", fontSize = 12.sp, color = NavyBlue)
                                }
                            }
                        }
                        if (!isApproved && !isImpossible) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Meta:", fontSize = 10.sp, color = SlateGray)
                                Text(
                                    String.format(Locale.US, "%.1f", targetGradePerPending),
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
