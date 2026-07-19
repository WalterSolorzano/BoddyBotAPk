import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

import_code = "import java.text.SimpleDateFormat\n"
if "import java.text.SimpleDateFormat" not in content:
    content = content.replace("import java.util.Calendar\n", "import java.util.Calendar\nimport java.text.SimpleDateFormat\nimport java.util.Date\n")

# Call the widget below BuddyMascotRoomWidget
old_call = """            BuddyMascotRoomWidget(
                absencesCount = absences.size, 
                assessments = assessments,
                buddyXp = buddyXp,
                examTomorrow = examTomorrow,
                isCelebrating = isCelebrating
            )
        } else if (activeTab == "estudios") {"""

new_call = """            BuddyMascotRoomWidget(
                absencesCount = absences.size, 
                assessments = assessments,
                buddyXp = buddyXp,
                examTomorrow = examTomorrow,
                isCelebrating = isCelebrating
            )
            
            BuddyMoodHistoryWidget(attendanceLogs = attendanceLogs)
        } else if (activeTab == "estudios") {"""

content = content.replace(old_call, new_call)

widget_def = """
@Composable
fun BuddyMoodHistoryWidget(attendanceLogs: List<com.aistudio.unibuddy.qywvsp.data.AttendanceLog>) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Estado de Ánimo Semanal",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SlateGray
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val calendar = Calendar.getInstance()
                val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                
                // Get last 7 days
                val days = (6 downTo 0).map { i ->
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, -i)
                    cal.time
                }
                
                days.forEach { date ->
                    val dateStr = sdf.format(date)
                    val logsForDay = attendanceLogs.filter { it.date == dateStr }
                    val dayName = SimpleDateFormat("E", Locale.getDefault()).format(date).take(1).uppercase()
                    
                    val moodColor = when {
                        logsForDay.isEmpty() -> Color(0xFFEEEEEE)
                        logsForDay.all { it.isPresent } -> Color(0xFF4CAF50)
                        logsForDay.any { !it.isPresent } -> Color(0xFFE57373)
                        else -> Color(0xFFEEEEEE)
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(moodColor)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(dayName, fontSize = 10.sp, color = SlateGray, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
"""

content = content + widget_def

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
