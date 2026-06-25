package com.aistudio.unibuddy.qywvsp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.unibuddy.qywvsp.data.Subject
import com.aistudio.unibuddy.qywvsp.data.parseSessions
import com.aistudio.unibuddy.qywvsp.ui.theme.*

@Composable
fun TodayClassesListWidget(
    subjects: List<Subject>,
    currentDayCode: String
) {
    val todayClasses = subjects.filter { it.schedule.contains(currentDayCode, ignoreCase = true) }
        .sortedBy { 
            it.sessionsJson.parseSessions().firstOrNull { session -> session.day.equals(currentDayCode, ignoreCase = true) }?.time ?: "23:59 PM"
        }

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Bone),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Clases de Hoy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NavyBlue
            )
            Text(
                text = if (todayClasses.isEmpty()) "No tienes clases programadas para hoy." else "Tu itinerario para hoy.",
                style = MaterialTheme.typography.bodySmall,
                color = SlateGray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (todayClasses.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                    Text("¡Día libre!", color = SlateGray, fontWeight = FontWeight.Bold)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    todayClasses.forEach { subject ->
                        val time = subject.sessionsJson.parseSessions().firstOrNull { it.day.equals(currentDayCode, ignoreCase = true) }?.time ?: ""
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8FAFC))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(android.graphics.Color.parseColor(subject.colorHex))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(subject.name.take(1).uppercase(), color = NavyBlue, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = subject.name, fontWeight = FontWeight.Bold, color = NavyBlue, fontSize = 14.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = SlateGray, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = time, color = SlateGray, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
