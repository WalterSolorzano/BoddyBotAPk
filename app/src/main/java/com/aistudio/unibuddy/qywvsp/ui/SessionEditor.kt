package com.aistudio.unibuddy.qywvsp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.unibuddy.qywvsp.ui.theme.*
import com.aistudio.unibuddy.qywvsp.data.Subject
import com.aistudio.unibuddy.qywvsp.data.ClassSessionDetails
import org.json.JSONArray
import org.json.JSONObject

data class TimeBlock(val id: String, val label: String)

val WEEK_DAYS = listOf("Lu", "Ma", "Mi", "Ju", "Vi")
val TIME_BLOCKS = listOf(
    TimeBlock("M1", "M1\n08-10"),
    TimeBlock("M2", "M2\n10-12"),
    TimeBlock("M3", "M3\n12-14"),
    TimeBlock("T1", "T1\n14-16"),
    TimeBlock("T2", "T2\n16-18"),
    TimeBlock("T3", "T3\n18-20")
)

@Composable
fun SessionEditor(
    sessions: MutableList<ClassSessionDetails>,
    allSubjects: List<Subject>
) {
    var showMorning by remember { mutableStateOf(true) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Horario Interactivo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NavyBlue)
                Text("Toca los bloques para asignar.", fontSize = 12.sp, color = Color.Gray)
            }
            
            // Toggle Button
            Button(
                onClick = { showMorning = !showMorning },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3F2FD), contentColor = NavyBlue),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showMorning) {
                        Text("Mañana", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Tarde", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        // Grid
        Row(modifier = Modifier.fillMaxWidth()) {
            // Empty top-left cell
            Box(modifier = Modifier.weight(0.8f))

            WEEK_DAYS.forEach { day ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(day, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        val visibleBlocks = if (showMorning) TIME_BLOCKS.take(3) else TIME_BLOCKS.drop(3)

        visibleBlocks.forEach { block ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time Label
                Box(modifier = Modifier.weight(0.8f), contentAlignment = Alignment.Center) {
                    Text(block.label, fontSize = 10.sp, textAlign = TextAlign.Center, lineHeight = 12.sp, color = Color.DarkGray)
                }

                WEEK_DAYS.forEach { day ->
                    // Check if block is occupied by ANOTHER subject
                    var occupiedBy: Subject? = null
                    for (sub in allSubjects) {
                        val session = sub.sessions.find { it.day == day && it.time == block.id }
                        if (session != null) {
                            occupiedBy = sub
                            break
                        }
                    }

                    // Check if block is selected for CURRENT subject
                    val isSelected = sessions.any { it.day == day && it.time == block.id }

                    val bgColor = when {
                        occupiedBy != null -> Color(0xFFE0E0E0) // Gray
                        isSelected -> DarkGreen // Green
                        else -> Color(0xFFF5F5F5) // Light gray
                    }

                    val borderColor = when {
                        occupiedBy != null -> Color.Gray
                        isSelected -> DarkGreen
                        else -> Color(0xFFE0E0E0)
                    }

                    val textColor = when {
                        occupiedBy != null -> Color.DarkGray
                        isSelected -> Color.White
                        else -> Color.Transparent
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(bgColor)
                            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                            .clickable(enabled = occupiedBy == null) {
                                if (isSelected) {
                                    sessions.removeAll { it.day == day && it.time == block.id }
                                } else {
                                    sessions.add(ClassSessionDetails(day, block.id, ""))
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (occupiedBy != null) {
                            Text(occupiedBy.name.take(3).uppercase(), fontSize = 10.sp, color = textColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        } else if (isSelected) {
                            Text("✓", fontSize = 14.sp, color = textColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

