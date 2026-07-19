package com.aistudio.unibuddy.qywvsp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.aistudio.unibuddy.qywvsp.ui.theme.*
import com.aistudio.unibuddy.qywvsp.data.Subject
import com.aistudio.unibuddy.qywvsp.data.ClassSessionDetails

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

fun getBlockIdForSession(session: ClassSessionDetails): String {
    val t = session.time
    if (t == "M1" || t.contains("08-10")) return "M1"
    if (t == "M2" || t.contains("10-12")) return "M2"
    if (t == "M3" || t.contains("12-14")) return "M3"
    if (t == "T1" || t.contains("14-16")) return "T1"
    if (t == "T2" || t.contains("16-18")) return "T2"
    if (t == "T3" || t.contains("18-20")) return "T3"
    
    val lowercase = t.lowercase()
    val hourRegex = Regex("""(\d+):""")
    val match = hourRegex.find(lowercase)
    if (match != null) {
        val hour = match.groupValues[1].toIntOrNull()
        if (hour != null) {
            return when (hour) {
                7, 8, 9 -> "M1"
                10, 11 -> "M2"
                12 -> "M3"
                13, 14, 15 -> "T1"
                16 -> "T2"
                17, 18, 19, 20 -> "T3"
                else -> {
                    when {
                        hour >= 17 -> "T3"
                        hour == 16 -> "T2"
                        hour >= 13 -> "T1"
                        hour == 12 -> "M3"
                        hour >= 10 -> "M2"
                        else -> "M1"
                    }
                }
            }
        }
    }
    
    return when {
        lowercase.startsWith("m1") -> "M1"
        lowercase.startsWith("m2") -> "M2"
        lowercase.startsWith("m3") -> "M3"
        lowercase.startsWith("t1") -> "T1"
        lowercase.startsWith("t2") -> "T2"
        lowercase.startsWith("t3") -> "T3"
        else -> "M1"
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionEditor(
    sessions: MutableList<ClassSessionDetails>,
    allSubjects: List<Subject>,
    currentSubjectId: Int? = null,
    currentSubjectName: String? = null
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
                        Text("Mañana (M1-M3)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Tarde (T1-T3)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        // Grid Header Days
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(0.8f)) // Left spacing for time label
            WEEK_DAYS.forEach { day ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(day, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyBlue)
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
                    Text(block.label, fontSize = 10.sp, textAlign = TextAlign.Center, lineHeight = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                }

                WEEK_DAYS.forEach { day ->
                    // Check if block is occupied by ANOTHER subject
                    var occupiedBy: Subject? = null
                    for (sub in allSubjects) {
                        if (currentSubjectId != null && sub.id == currentSubjectId) continue
                        if (currentSubjectName != null && sub.name.equals(currentSubjectName, ignoreCase = true)) continue
                        val session = sub.sessions.find { it.day == day && getBlockIdForSession(it) == block.id }
                        if (session != null) {
                            occupiedBy = sub
                            break
                        }
                    }

                    // Check if block is selected for CURRENT subject
                    val isSelected = sessions.any { it.day == day && getBlockIdForSession(it) == block.id }

                    val bgColor = when {
                        occupiedBy != null -> Color(0xFFE2E8F0) // Gray
                        isSelected -> DarkGreen // Green
                        else -> Color(0xFFF1F5F9) // Light gray
                    }

                    val borderColor = when {
                        occupiedBy != null -> Color(0xFFCBD5E1)
                        isSelected -> DarkGreen
                        else -> Color(0xFFE2E8F0)
                    }

                    val textColor = when {
                        occupiedBy != null -> SlateGray
                        isSelected -> Color.White
                        else -> Color.Transparent
                    }

                    val context = LocalContext.current
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgColor)
                            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                            .clickable {
                                if (occupiedBy != null) {
                                    android.widget.Toast.makeText(context, "Cuidado, ya tienes ${occupiedBy.name} a esa misma hora", android.widget.Toast.LENGTH_SHORT).show()
                                } else if (isSelected) {
                                    sessions.removeAll { it.day == day && getBlockIdForSession(it) == block.id }
                                } else {
                                    val defaultTime = when (block.id) {
                                        "M1" -> "08:00 - 10:00"
                                        "M2" -> "10:00 - 12:00"
                                        "M3" -> "12:00 - 14:00"
                                        "T1" -> "14:00 - 16:00"
                                        "T2" -> "16:00 - 18:00"
                                        "T3" -> "18:00 - 20:00"
                                        else -> "08:00 - 10:00"
                                    }
                                    sessions.add(ClassSessionDetails(day, defaultTime, "Aula por definir", "Todas las semanas", emptyList()))
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (occupiedBy != null) {
                            Text(getSubjectInitials(occupiedBy.name), fontSize = 10.sp, color = textColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        } else if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = textColor, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        // Configuration details for selected blocks
        if (sessions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Personalizar cada bloque seleccionado:",
                fontWeight = FontWeight.Bold,
                color = NavyBlue,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            sessions.forEachIndexed { index, session ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, Bone),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val blockName = TIME_BLOCKS.find { it.id == session.time }?.label?.replace("\n", " ") ?: session.time
                            Text(
                                text = "Sesión: ${session.day} - $blockName",
                                fontWeight = FontWeight.Bold,
                                color = NavyBlue,
                                fontSize = 13.sp
                            )
                            
                            IconButton(
                                onClick = { sessions.removeAt(index) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar bloque",
                                    tint = Terracotta,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Room Input Field
                        OutlinedTextField(
                            value = session.room,
                            onValueChange = { newRoom ->
                                sessions[index] = session.copy(room = newRoom)
                            },
                            placeholder = { Text("Ej: Aula 102", fontSize = 12.sp) },
                            label = { Text("Aula/Salón", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkGreen,
                                focusedLabelColor = DarkGreen
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        // Custom Time/Hour Input Field
                        OutlinedTextField(
                            value = session.time,
                            onValueChange = { newTime ->
                                sessions[index] = session.copy(time = newTime)
                            },
                            placeholder = { Text("Ej: 07:30 - 09:10", fontSize = 12.sp) },
                            label = { Text("Horario de Clase (Horas)", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkGreen,
                                focusedLabelColor = DarkGreen
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Suggested times for easy selection
                        val suggestions = listOf("07:30 - 09:10", "08:00 - 10:00", "09:20 - 11:00", "10:00 - 12:00", "11:10 - 12:50", "13:00 - 16:00", "17:30 - 19:10")
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 6.dp)
                        ) {
                            items(suggestions) { sug ->
                                val isSelected = session.time == sug
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) NavyBlue.copy(alpha = 0.15f) else Color.White)
                                        .border(1.dp, if (isSelected) NavyBlue else Color.LightGray, RoundedCornerShape(8.dp))
                                        .clickable {
                                            sessions[index] = session.copy(time = sug)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(sug, fontSize = 10.sp, color = NavyBlue, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Frequency Segmented Chips
                        Text(
                            text = "Frecuencia de la clase:",
                            fontSize = 11.sp,
                            color = SlateGray,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Todas las semanas", "Semanas Pares", "Semanas Impares").forEach { freq ->
                                val isSelected = session.safeFrequency == freq
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) NavyBlue else Color.White)
                                        .border(1.dp, if (isSelected) NavyBlue else Color.LightGray, RoundedCornerShape(8.dp))
                                        .clickable {
                                            sessions[index] = session.copy(frequency = freq)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when (freq) {
                                            "Semanas Pares" -> "Pares"
                                            "Semanas Impares" -> "Impares"
                                            else -> "Todas"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else NavyBlue
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
