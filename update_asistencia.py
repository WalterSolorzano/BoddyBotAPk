import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyApp.kt", "r") as f:
    content = f.read()

# 1. Replace chunked in AsistenciaOverviewScreen
old_chunk = """                                val chunkedSubjects = subjects.chunked(2)
                                items(chunkedSubjects, key = { it.first().id }) { pair ->"""

new_chunk = """                                items(subjects, key = { it.id }) { sub ->"""
content = content.replace(old_chunk, new_chunk)

# Now fix the Row and pair.forEach inside it
old_row = """                                    Row(
                        modifier = Modifier.fillMaxWidth().animateItem(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        pair.forEach { sub ->
                            Box(modifier = Modifier.weight(1f)) {
                                SubjectGridItem(
                                    sub = sub,
                                    absences = absences,
                                    attendanceLogs = attendanceLogs,
                                    viewModel = viewModel,
                                    onSubjectClick = onSubjectClick,
                                    onCheckInClick = { performAction(it, true, false) },
                                    onAbsentClick = { performAction(it, false, false) },
                                    onJustifyClick = { performAction(it, false, true) }
                                )
                            }
                        }
                        if (pair.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }"""

new_row = """                                    SubjectGridItem(
                                        sub = sub,
                                        absences = absences,
                                        attendanceLogs = attendanceLogs,
                                        viewModel = viewModel,
                                        onSubjectClick = onSubjectClick,
                                        onCheckInClick = { performAction(it, true, false) },
                                        onAbsentClick = { performAction(it, false, false) },
                                        onJustifyClick = { performAction(it, false, true) }
                                    )"""
content = content.replace(old_row, new_row)

# 2. Replace SubjectGridItem completely
old_subject_grid = """fun SubjectGridItem("""

# Find the end of SubjectGridItem
end_of_subject_grid = """                text = "Toca para ver detalles",
                fontSize = 9.sp,
                color = SlateGray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}"""

match_start = content.find(old_subject_grid)
match_end = content.find(end_of_subject_grid, match_start) + len(end_of_subject_grid)

new_subject_grid = """fun SubjectGridItem(
    sub: Subject,
    absences: List<Absence>,
    attendanceLogs: List<AttendanceLog>,
    viewModel: UniBuddyViewModel,
    onSubjectClick: (Int) -> Unit,
    onCheckInClick: (Subject) -> Unit,
    onAbsentClick: (Subject) -> Unit,
    onJustifyClick: (Subject) -> Unit
) {
    val legacySubAbs = absences.filter { it.subjectId == sub.id }
    val subLogs = attendanceLogs.filter { it.subjectId == sub.id }
    val subAbsCount = subLogs.count { !it.isPresent }.coerceAtLeast(legacySubAbs.size)
    val subPresCount = subLogs.count { it.isPresent }
    
    val maxAbs = sub.totalClasses - kotlin.math.ceil(sub.totalClasses * (sub.requiredAttendancePercent / 100.0)).toInt()
    val remaining = maxAbs - subAbsCount
    
    val fractionUsed = if (maxAbs > 0) subAbsCount.toFloat() / maxAbs.toFloat() else 0f
    val percentageUsed = (fractionUsed * 100).toInt()
    
    val isWarning = remaining <= 2 && remaining > 0
    val isCritical = remaining <= 0
    
    val statusColor = if (isCritical) com.aistudio.unibuddy.qywvsp.ui.theme.StatusRed else if (isWarning) com.aistudio.unibuddy.qywvsp.ui.theme.StatusAmber else com.aistudio.unibuddy.qywvsp.ui.theme.StatusGreen

    val hapticFeedback = androidx.compose.ui.platform.LocalHapticFeedback.current
    val todayCode = when (java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)) {
        java.util.Calendar.MONDAY -> "Lu"
        java.util.Calendar.TUESDAY -> "Ma"
        java.util.Calendar.WEDNESDAY -> "Mi"
        java.util.Calendar.THURSDAY -> "Ju"
        java.util.Calendar.FRIDAY -> "Vi"
        java.util.Calendar.SATURDAY -> "Sá"
        else -> "Do"
    }
    val defaultSession = sub.sessions.find { it.day.trim().equals(todayCode, ignoreCase = true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                onSubjectClick(sub.id)
            }
            .animateContentSize()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = com.aistudio.unibuddy.qywvsp.ui.theme.BackgroundBone),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                Text(
                    text = sub.name,
                    fontSize = 16.sp,
                    color = com.aistudio.unibuddy.qywvsp.ui.theme.NavyBlue,
                    fontWeight = FontWeight.Bold
                )
                if (defaultSession != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hoy • ${defaultSession.time} | ${defaultSession.room}",
                        fontSize = 12.sp,
                        color = com.aistudio.unibuddy.qywvsp.ui.theme.SlateGray,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(54.dp)) {
                androidx.compose.material3.CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = Color.LightGray.copy(alpha = 0.3f),
                    strokeWidth = 6.dp
                )
                androidx.compose.material3.CircularProgressIndicator(
                    progress = { fractionUsed.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxSize(),
                    color = statusColor,
                    strokeWidth = 6.dp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$subAbsCount",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColor
                    )
                    Text("faltas", fontSize = 8.sp, color = com.aistudio.unibuddy.qywvsp.ui.theme.SlateGray)
                }
            }
        }
    }
}"""
content = content[:match_start] + new_subject_grid + content[match_end:]

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyApp.kt", "w") as f:
    f.write(content)
