import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

old_call = """            BuddyMascotRoomWidget(
                absencesCount = absences.size, 
                assessments = assessments,
                buddyXp = buddyXp,
                examTomorrow = examTomorrow,
                isCelebrating = isCelebrating
            )"""

new_call = """            val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
            val todayStr = sdf.format(Date())
            val yesterdayStr = sdf.format(Date(System.currentTimeMillis() - 86400000))
            val hasRecentAttendance = attendanceLogs.any { it.date == todayStr || it.date == yesterdayStr }

            BuddyMascotRoomWidget(
                absencesCount = absences.size, 
                assessments = assessments,
                buddyXp = buddyXp,
                examTomorrow = examTomorrow,
                isCelebrating = isCelebrating,
                hasRecentActivity = hasRecentAttendance || attendanceLogs.isEmpty()
            )"""

content = content.replace(old_call, new_call)

old_def = """fun BuddyMascotRoomWidget(
    absencesCount: Int,
    assessments: List<com.aistudio.unibuddy.qywvsp.data.Assessment>,
    buddyXp: Int,
    examTomorrow: Boolean,
    isCelebrating: Boolean = false
) {"""

new_def = """fun BuddyMascotRoomWidget(
    absencesCount: Int,
    assessments: List<com.aistudio.unibuddy.qywvsp.data.Assessment>,
    buddyXp: Int,
    examTomorrow: Boolean,
    isCelebrating: Boolean = false,
    hasRecentActivity: Boolean = true
) {"""

content = content.replace(old_def, new_def)

old_pose = """                        val finalPose = if (isCelebrating) "celebrating" else if (examTomorrow) "working" else if (absencesCount >= 3) "worried" else "normal"
                        BuddyMascot(
                            modifier = Modifier.size(50.dp),
                            isHappy = (absencesCount < 3) || isCelebrating,
                            pose = finalPose
                        )
                        Text(
                            text = if (examTomorrow) "¡A estudiar para mañana!" else if (absencesCount >= 3) "¡Cuidado con las faltas!" else "Tu Buddy te apoya",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyBlue
                        )"""

new_pose = """                        val finalPose = if (isCelebrating) "celebrating" else if (examTomorrow) "working" else if (absencesCount >= 3 || !hasRecentActivity) "worried" else "normal"
                        BuddyMascot(
                            modifier = Modifier.size(50.dp),
                            isHappy = ((absencesCount < 3) && hasRecentActivity) || isCelebrating,
                            pose = finalPose
                        )
                        Text(
                            text = if (examTomorrow) "¡A estudiar para mañana!" else if (absencesCount >= 3) "¡Cuidado con las faltas!" else if (!hasRecentActivity) "¡Tu Buddy te extraña!" else "Tu Buddy te apoya",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyBlue
                        )"""

content = content.replace(old_pose, new_pose)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
