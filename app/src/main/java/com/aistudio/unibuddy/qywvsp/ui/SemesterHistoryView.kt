package com.aistudio.unibuddy.qywvsp.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.unibuddy.qywvsp.ui.components.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aistudio.unibuddy.qywvsp.data.Assessment
import com.aistudio.unibuddy.qywvsp.data.AttendanceLog
import com.aistudio.unibuddy.qywvsp.data.Subject
import com.aistudio.unibuddy.qywvsp.data.TripRecord
import com.aistudio.unibuddy.qywvsp.ui.theme.*

@Composable
fun SemesterHistoryView(viewModel: UniBuddyViewModel) {
    val context = LocalContext.current
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val attendanceLogs by viewModel.attendanceLogs.collectAsStateWithLifecycle()
    val assessments by viewModel.assessments.collectAsStateWithLifecycle()
    val tripRecords by viewModel.tripRecords.collectAsStateWithLifecycle()

    var logTab by remember { mutableStateOf("asistencias") } // "asistencias", "examenes", "gps"

    // Calculate core statistics
    val totalClasses = attendanceLogs.size
    val presentCount = attendanceLogs.count { it.isPresent }
    val attendanceRate = if (totalClasses > 0) (presentCount.toFloat() / totalClasses.toFloat() * 100f).toInt() else 100

    val gradedAssessments = assessments.filter { it.grade != null }
    val averageGrade = if (gradedAssessments.isNotEmpty()) {
        gradedAssessments.map { it.grade ?: 0.0 }.average()
    } else {
        0.0
    }

    val totalTrips = tripRecords.size
    val avgTripDuration = if (totalTrips > 0) {
        tripRecords.map { it.durationMinutes }.average().toInt()
    } else {
        0
    }

    // Subject Map for Name Resolving
    val subjectMap = remember(subjects) { subjects.associateBy { it.id } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Cards Bento Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .shadow(1.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Asistencia", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    Text("$attendanceRate%", fontSize = 20.sp, color = NavyBlue, fontWeight = FontWeight.Black)
                    Text("$presentCount de $totalClasses", fontSize = 10.sp, color = SlateGray, fontWeight = FontWeight.Medium)
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .shadow(1.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Promedio Gral", fontSize = 11.sp, color = Color(0xFF1565C0), fontWeight = FontWeight.Bold)
                    Text(String.format("%.1f", averageGrade), fontSize = 20.sp, color = NavyBlue, fontWeight = FontWeight.Black)
                    Text("${gradedAssessments.size} notas reg.", fontSize = 10.sp, color = SlateGray, fontWeight = FontWeight.Medium)
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .shadow(1.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Commute Prom.", fontSize = 11.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                    Text("$avgTripDuration min", fontSize = 20.sp, color = NavyBlue, fontWeight = FontWeight.Black)
                    Text("$totalTrips traslados", fontSize = 10.sp, color = SlateGray, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Segmented control to filter logs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFECEFF1), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("oficial" to "Oficial", "asistencias" to "Asist.", "examenes" to "Notas", "gps" to "Viajes").forEach { (tabKey, tabLabel) ->
                val isSelected = logTab == tabKey
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) NavyBlue else Color.Transparent)
                        .clickable { logTab = tabKey }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isSelected) Color.White else NavyBlue
                    )
                }
            }
        }

        // Conditional display based on tab selection
        when (logTab) {
            "oficial" -> {
                AdvancedAcademicStatistics(viewModel)
            }
            "asistencias" -> {
                if (attendanceLogs.isEmpty()) {
                    EmptyHistoryPlaceholder("No hay asistencias registradas aún.")
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        attendanceLogs.reversed().forEach { log ->
                            val sub = subjectMap[log.subjectId]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(sub?.name ?: "Materia Eliminada", fontWeight = FontWeight.Bold, color = NavyBlue, fontSize = 13.sp)
                                    Text("Registrado el: ${log.date}", fontSize = 11.sp, color = SlateGray)
                                }
                                Surface(
                                    color = if (log.isPresent) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (log.isPresent) "PRESENTE" else "AUSENTE",
                                        color = if (log.isPresent) Color(0xFF2E7D32) else Color(0xFFC62828),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            "examenes" -> {
                if (assessments.isEmpty()) {
                    EmptyHistoryPlaceholder("No hay exámenes programados o calificados.")
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        assessments.forEach { exam ->
                            val sub = subjectMap[exam.subjectId]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("${exam.name} - ${sub?.name ?: "Materia"}", fontWeight = FontWeight.Bold, color = NavyBlue, fontSize = 13.sp)
                                    Text("Porcentaje: ${exam.percentage}%", fontSize = 11.sp, color = SlateGray)
                                }
                                Surface(
                                    color = if (exam.grade != null) Color(0xFFE3F2FD) else Color(0xFFFFF3E0),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = exam.grade?.let { "Nota: $it" } ?: "PENDIENTE",
                                        color = if (exam.grade != null) Color(0xFF1565C0) else Color(0xFFE65100),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            "gps" -> {
                if (tripRecords.isEmpty()) {
                    EmptyHistoryPlaceholder("No hay traslados o registros de viaje almacenados.")
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tripRecords.reversed().forEach { trip ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(0xFFFFF3E0), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Viaje registrado", fontWeight = FontWeight.Bold, color = NavyBlue, fontSize = 13.sp)
                                        Text("Fecha: ${trip.date}", fontSize = 11.sp, color = SlateGray)
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${trip.durationMinutes} min", fontWeight = FontWeight.Bold, color = NavyBlue, fontSize = 13.sp)
                                    Text(if (trip.wasRaining) "Lloviendo" else "Despejado", fontSize = 10.sp, color = SlateGray, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Grade Goals Calculator / GPA Predictor Card
        val pendingExamsCount = remember(assessments) { assessments.count { it.grade == null } }
        if (pendingExamsCount > 0) {
            var targetGpa by remember { mutableFloatStateOf(75.0f) }
            val requiredGrade = remember(targetGpa, gradedAssessments, pendingExamsCount) {
                val totalCount = gradedAssessments.size + pendingExamsCount
                val currentSum = gradedAssessments.sumOf { it.grade ?: 0.0 }
                val neededSum = (targetGpa * totalCount) - currentSum
                val reqAvg = neededSum / pendingExamsCount
                reqAvg.coerceAtLeast(0.0)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(2.dp, ProBlue.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFE3F2FD), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Predictor de Metas Académicas", fontWeight = FontWeight.Bold, color = NavyBlue, fontSize = 14.sp)
                            Text("Estima la nota requerida para tus próximos exámenes.", fontSize = 11.sp, color = SlateGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Tu Meta de Promedio: ${String.format("%.1f", targetGpa)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = NavyBlue
                    )

                    Slider(
                        value = targetGpa,
                        onValueChange = { targetGpa = it },
                        valueRange = 51.0f..100.0f,
                        steps = 49,
                        colors = SliderDefaults.colors(
                            thumbColor = ProBlue,
                            activeTrackColor = ProBlue,
                            inactiveTrackColor = Color(0xFFE2E8F0)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = when {
                            requiredGrade <= 51.0 -> Color(0xFFE8F5E9)
                            requiredGrade <= 75.0 -> Color(0xFFE3F2FD)
                            requiredGrade <= 100.0 -> Color(0xFFFFF3E0)
                            else -> Color(0xFFFFEBEE)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (requiredGrade > 100.0) {
                                    "Meta inalcanzable matemáticamente"
                                } else {
                                    "Nota promedio requerida: ${String.format("%.2f", requiredGrade)} / 100.0"
                                },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = NavyBlue
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = when {
                                    requiredGrade <= 51.0 -> "¡Muy fácil! Ya casi tienes asegurada la meta."
                                    requiredGrade <= 75.0 -> "¡Totalmente alcanzable! Un repaso regular bastará."
                                    requiredGrade <= 100.0 -> "¡Requerirá esfuerzo! Necesitas excelentes notas en tus exámenes."
                                    else -> "Prácticamente imposible con tu promedio actual. Baja un poco tu meta."
                                },
                                fontSize = 11.sp,
                                color = SlateGray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Export data box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(1.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = NavyBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exportar para Estadísticas", fontWeight = FontWeight.Bold, color = NavyBlue, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Genera y copia registros tabulados listos para Excel, SPSS o Python.",
                    fontSize = 11.sp,
                    color = SlateGray
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            val csv = generateAttendanceCsv(attendanceLogs, subjectMap)
                            copyToClipboard(context, "Asistencias_UniBuddy.csv", csv)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Asistencias", fontSize = 11.sp, color = Color.White)
                    }

                    Button(
                        onClick = {
                            val csv = generateAssessmentsCsv(assessments, subjectMap)
                            copyToClipboard(context, "Calificaciones_UniBuddy.csv", csv)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Notas", fontSize = 11.sp, color = Color.White)
                    }

                    Button(
                        onClick = {
                            val csv = generateTripsCsv(tripRecords)
                            copyToClipboard(context, "Viajes_UniBuddy.csv", csv)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Traslados", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyHistoryPlaceholder(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BuddyMascot(modifier = Modifier.size(60.dp), isHappy = false, pose = "sleeping")
        Spacer(modifier = Modifier.height(8.dp))
        Text(message, color = SlateGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "¡Copiado como CSV! Listo para pegar en Excel.", Toast.LENGTH_LONG).show()
}

private fun generateAttendanceCsv(logs: List<AttendanceLog>, subjectMap: Map<Int, Subject>): String {
    val sb = java.lang.StringBuilder()
    sb.append("ID,Materia,Fecha,Estado\n")
    logs.forEach { log ->
        val subName = subjectMap[log.subjectId]?.name ?: "MateriaEliminada"
        sb.append("${log.id},\"${subName}\",\"${log.date}\",${if (log.isPresent) "Presente" else "Ausente"}\n")
    }
    return sb.toString()
}

private fun generateAssessmentsCsv(exams: List<Assessment>, subjectMap: Map<Int, Subject>): String {
    val sb = java.lang.StringBuilder()
    sb.append("ID,Materia,Evaluacion,Calificacion,Porcentaje\n")
    exams.forEach { exam ->
        val subName = subjectMap[exam.subjectId]?.name ?: "MateriaEliminada"
        val score = exam.grade?.toString() ?: "Pendiente"
        sb.append("${exam.id},\"${subName}\",\"${exam.name}\",${score},${exam.percentage}\n")
    }
    return sb.toString()
}

private fun generateTripsCsv(trips: List<TripRecord>): String {
    val sb = java.lang.StringBuilder()
    sb.append("ID,Fecha,Duracion_Minutos,Lluvia\n")
    trips.forEach { trip ->
        sb.append("${trip.id},\"${trip.date}\",${trip.durationMinutes},${if (trip.wasRaining) "Si" else "No"}\n")
    }
    return sb.toString()
}

@Composable
fun AdvancedAcademicStatistics(viewModel: UniBuddyViewModel) {
    val history by viewModel.academicHistory.collectAsStateWithLifecycle()

    if (history.isEmpty()) {
        EmptyHistoryPlaceholder("No hay historial académico oficial. Ve a Configuración y presiona 'Importar PDF'.")
        return
    }

    val totalCredits = history.sumOf { it.credits }
    val validGrades = history.filter { it.record.grade > 0.0 }
    val averageGrade = if (validGrades.isNotEmpty()) validGrades.map { it.record.grade }.average() else 0.0
    val totalSubjects = history.size
    val passedSubjects = history.count { it.record.status.name == "NF_R" }
    val survivalRate = if (totalSubjects > 0) (passedSubjects.toFloat() / totalSubjects.toFloat() * 100f).toInt() else 0

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f).shadow(1.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Promedio Global", fontSize = 11.sp, color = Color(0xFF283593), fontWeight = FontWeight.Bold)
                    Text(String.format("%.2f", averageGrade), fontSize = 20.sp, color = NavyBlue, fontWeight = FontWeight.Black)
                }
            }

            Card(
                modifier = Modifier.weight(1f).shadow(1.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Supervivencia", fontSize = 11.sp, color = Color(0xFF33691E), fontWeight = FontWeight.Bold)
                    Text("$survivalRate%", fontSize = 20.sp, color = NavyBlue, fontWeight = FontWeight.Black)
                }
            }
        }

        // Gráfico simple de promedios por semestre
        val semesterAverages = validGrades.groupBy { it.semester }.mapValues { (_, records) ->
            records.map { it.record.grade }.average()
        }.toList().sortedBy { it.first }

        if (semesterAverages.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Evolución por Semestre", fontWeight = FontWeight.Bold, color = NavyBlue, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val maxGrade = semesterAverages.maxOf { it.second }.toFloat().coerceAtLeast(10f)
                        semesterAverages.forEach { (semester, avg) ->
                            val barHeightWeight = (avg.toFloat() / maxGrade).coerceIn(0.1f, 1f)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(String.format("%.1f", avg), fontSize = 9.sp, color = SlateGray, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .fillMaxHeight(barHeightWeight)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(if (avg >= 60) ProBlue else Color(0xFFEF5350))
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(semester.take(3), fontSize = 9.sp, color = NavyBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // List of records
        Text("Desglose Histórico", fontWeight = FontWeight.Bold, color = NavyBlue, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            history.forEach { record ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(record.subjectName, fontWeight = FontWeight.Bold, color = NavyBlue, fontSize = 12.sp)
                        Text("${record.record.academicGroup} | Semestre: ${record.semester} | Cred: ${record.credits}", fontSize = 10.sp, color = SlateGray)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(String.format("%.1f", record.record.grade), fontWeight = FontWeight.Black, color = if (record.record.grade >= 60) DarkGreen else Color.Red, fontSize = 14.sp)
                        Text(record.record.status.name, fontSize = 9.sp, color = SlateGray, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
