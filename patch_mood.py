import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

old_call = """                        hasRecentActivity = hasRecentAttendance || attendanceLogs.isEmpty()
            )
            
            BuddyMoodHistoryWidget(attendanceLogs = attendanceLogs)"""

new_call = """                        hasRecentActivity = hasRecentAttendance || attendanceLogs.isEmpty()
            )
            
            BuddyMoodHistoryWidget(attendanceLogs = attendanceLogs, assessments = assessments)"""

content = content.replace(old_call, new_call)

old_func = """fun BuddyMoodHistoryWidget(attendanceLogs: List<com.aistudio.unibuddy.qywvsp.data.AttendanceLog>) {
    Card("""

new_func = """fun BuddyMoodHistoryWidget(attendanceLogs: List<com.aistudio.unibuddy.qywvsp.data.AttendanceLog>, assessments: List<com.aistudio.unibuddy.qywvsp.data.Assessment> = emptyList()) {
    Card("""

content = content.replace(old_func, new_func)

old_colors = """                    val dayName = SimpleDateFormat("E", Locale.getDefault()).format(date).take(1).uppercase()
                    
                    val moodColor = when {
                        logsForDay.isEmpty() -> Color(0xFFEEEEEE)
                        logsForDay.all { it.isPresent } -> Color(0xFF4CAF50)
                        logsForDay.any { !it.isPresent } -> Color(0xFFE57373)
                        else -> Color(0xFFEEEEEE)
                    }"""

new_colors = """                    val dayName = SimpleDateFormat("E", Locale.getDefault()).format(date).take(1).uppercase()
                    val calDate = Calendar.getInstance()
                    calDate.time = date
                    val dayCode = when (calDate.get(Calendar.DAY_OF_WEEK)) {
                        Calendar.MONDAY -> "Lu"
                        Calendar.TUESDAY -> "Ma"
                        Calendar.WEDNESDAY -> "Mi"
                        Calendar.THURSDAY -> "Ju"
                        Calendar.FRIDAY -> "Vi"
                        Calendar.SATURDAY -> "Sá"
                        else -> "Do"
                    }
                    val fullDateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
                    val hasExam = assessments.any { it.examDate.trim().equals(dayCode, ignoreCase = true) || it.examDate == fullDateStr }
                    
                    val moodColor = when {
                        logsForDay.any { !it.isPresent } -> Amber // Worried
                        hasExam -> ProBlue // Working
                        logsForDay.isNotEmpty() && logsForDay.all { it.isPresent } -> DarkGreen // Happy
                        else -> Color(0xFFEEEEEE)
                    }"""

content = content.replace(old_colors, new_colors)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
