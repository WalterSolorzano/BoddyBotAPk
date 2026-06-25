package com.aistudio.unibuddy.qywvsp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Place
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
            it.sessions.firstOrNull { session -> session.day.equals(currentDayCode, ignoreCase = true) }?.time ?: "23:59 PM"
        }

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Bone),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Horario de Hoy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NavyBlue
            )
            Text(
                text = if (todayClasses.isEmpty()) "No tienes clases programadas para hoy." else "Tu itinerario para hoy.",
                style = MaterialTheme.typography.bodySmall,
                color = SlateGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (todayClasses.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                    Text("¡Día libre!", color = SlateGray, fontWeight = FontWeight.Bold)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    todayClasses.forEachIndexed { index, subject ->
                        val session = subject.sessions.firstOrNull { it.day.equals(currentDayCode, ignoreCase = true) }
                        val time = session?.time ?: ""
                        val room = session?.room ?: ""
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            // Timeline visual
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
                                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(android.graphics.Color.parseColor(subject.colorHex))))
                                if (index < todayClasses.size - 1) {
                                    Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(Bone))
                                }
                            }
                            // Content
                            Column(modifier = Modifier.weight(1f).padding(bottom = if (index < todayClasses.size - 1) 16.dp else 0.dp)) {
                                Text(text = time, color = NavyBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = (-3).dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Bone)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = subject.name, fontWeight = FontWeight.Bold, color = NavyBlue, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Place, contentDescription = null, tint = SlateGray, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = room, color = SlateGray, fontSize = 12.sp)
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
}
