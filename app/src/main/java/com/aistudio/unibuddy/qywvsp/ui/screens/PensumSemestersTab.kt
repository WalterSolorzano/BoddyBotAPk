package com.aistudio.unibuddy.qywvsp.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.unibuddy.qywvsp.data.AcademicRecordWithSubject
import com.aistudio.unibuddy.qywvsp.data.StaticPensumSubject
import com.aistudio.unibuddy.qywvsp.data.Professor
import com.aistudio.unibuddy.qywvsp.ui.theme.NavyBlue
import com.aistudio.unibuddy.qywvsp.ui.theme.ProBlue
import com.aistudio.unibuddy.qywvsp.ui.theme.Amber

@Composable
fun PensumSemestersTab(
    staticPensum: List<StaticPensumSubject>,
    history: List<AcademicRecordWithSubject>,
    professors: List<Professor>,
    passingGrade: Double = 60.0
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // Group static subjects by semester
    val semGroups = remember(staticPensum) {
        staticPensum.groupBy { it.semester }.toList().sortedBy { it.first }
    }
    
    var expandedSemester by remember { mutableStateOf<Int?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // "Exportar para Estadísticas" card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Share, "Export", tint = NavyBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exportar para Estadísticas", fontWeight = FontWeight.Bold, color = NavyBlue, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Copia tus registros académicos en formato CSV listo para Excel, SPSS o scripts de Python.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val csv = generateAcademicStatsCsv(history, staticPensum, professors, passingGrade)
                        copyToClipboard(context, "UniBuddy_Academic_Stats.csv", csv)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Copiar CSV para Excel/SPSS/Python", fontSize = 12.sp, color = Color.White)
                }
            }
        }
        
        Text("Progreso Semestral", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyBlue)
        
        semGroups.forEach { (sem, subjects) ->
            val isExpanded = expandedSemester == sem
            
            // Calculate metrics for this specific semester
            val semRecords = history.filter { rec ->
                subjects.any { 
                    it.code.equals(rec.subjectCode, ignoreCase = true) || 
                    it.name.equals(rec.subjectName, ignoreCase = true) 
                }
            }
            val passedCount = semRecords.count { it.record.grade >= passingGrade }
            val avg = if (semRecords.isNotEmpty()) semRecords.map { it.record.grade }.average() else 0.0
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedSemester = if (isExpanded) null else sem },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, if (isExpanded) ProBlue.copy(alpha = 0.5f) else Color(0xFFF1F5F9))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Semestre $sem", fontWeight = FontWeight.Bold, color = NavyBlue, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Aprobadas: $passedCount de ${subjects.size}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (avg > 0.0) {
                                Surface(
                                    color = Color(0xFFEFF6FF),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Prom: ${String.format("%.1f", avg)}",
                                        color = ProBlue,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand",
                                tint = Color.Gray
                            )
                        }
                    }
                    
                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Divider(color = Color(0xFFF1F5F9))
                            subjects.forEach { sub ->
                                val matchRec = history.find { rec ->
                                    rec.subjectCode.equals(sub.code, ignoreCase = true) || 
                                    rec.subjectName.equals(sub.name, ignoreCase = true) 
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(0.7f)) {
                                        Text(sub.name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = NavyBlue)
                                        val subCredits = matchRec?.credits ?: 4.0
                                        Text("Código: ${sub.code} | Créditos: $subCredits", fontSize = 10.sp, color = Color.Gray)
                                    }
                                    
                                    if (matchRec != null) {
                                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(0.3f)) {
                                            Text(
                                                text = "${matchRec.record.grade}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (matchRec.record.grade >= passingGrade) Color(0xFF10B981) else Color(0xFFEF4444)
                                            )
                                            Text(
                                                text = matchRec.record.status.name,
                                                fontSize = 9.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "Pendiente",
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            modifier = Modifier.weight(0.3f),
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "¡CSV de Estadísticas copiado al portapapeles!", Toast.LENGTH_LONG).show()
}

private fun generateAcademicStatsCsv(
    history: List<AcademicRecordWithSubject>,
    staticPensum: List<StaticPensumSubject>,
    professors: List<Professor>,
    passingGrade: Double
): String {
    val builder = StringBuilder()
    builder.append("Semestre,Codigo,Materia,Nota,Estado,Creditos,Profesor,Valoracion_Profesor\n")
    
    staticPensum.forEach { sub ->
        val matchRec = history.find { rec ->
            rec.subjectCode.equals(sub.code, ignoreCase = true) || 
            rec.subjectName.equals(sub.name, ignoreCase = true) 
        }
        val prof = matchRec?.let { r -> professors.find { it.id == r.record.professorId } }
        
        val semester = sub.semester
        val code = sub.code
        val name = sub.name.replace(",", " ")
        val grade = matchRec?.record?.grade?.toString() ?: "S/D"
        val status = matchRec?.record?.status?.name ?: "S/D"
        val credits = matchRec?.credits ?: 4.0
        val profName = prof?.name?.replace(",", " ") ?: "S/D"
        val profRating = matchRec?.record?.rating?.toString() ?: "S/D"
        
        builder.append("$semester,$code,$name,$grade,$status,$credits,$profName,$profRating\n")
    }
    return builder.toString()
}
