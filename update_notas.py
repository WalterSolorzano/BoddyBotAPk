import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyApp.kt", "r") as f:
    content = f.read()

# Replace chunked usage in GradesOverviewScreen
old_chunk_notas = """            val chunkedSubjects = remember(filteredSubjects) { filteredSubjects.chunked(2) }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    AdvancedGradesAnalytics(gradedExams = gradedExams, allAssessments = allAssessments)
                }
                
                item {
                    PredictiveGradeCalculatorWidget(subjects = subjects, viewModel = viewModel)
                }
                
                items(chunkedSubjects, key = { it.first().id }) { rowSubjects ->
                    Row(
                        modifier = Modifier.fillMaxWidth().animateItem(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowSubjects.forEach { sub ->
                            Box(
                                modifier = Modifier.weight(1f)
                            ) {
                                SubjectGradeGridCard(sub, viewModel, onSubjectClick)
                            }
                        }
                        if (rowSubjects.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }"""

new_chunk_notas = """            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    AdvancedGradesAnalytics(gradedExams = gradedExams, allAssessments = allAssessments)
                }
                
                item {
                    PredictiveGradeCalculatorWidget(subjects = subjects, viewModel = viewModel)
                }
                
                items(filteredSubjects, key = { it.id }) { sub ->
                    SubjectGradeGridCard(sub, viewModel, onSubjectClick)
                }
            }"""
content = content.replace(old_chunk_notas, new_chunk_notas)

# Replace SubjectGradeGridCard
old_subject_grade = """fun SubjectGradeGridCard("""
end_of_subject_grade = """            // Exams list inside Card (Ponle mas elementos)
            Text(
                text = if (assessments.isEmpty()) "Sin evaluaciones" else "${assessments.size} Evaluaciones",
                fontSize = 10.sp,
                color = SlateGray
            )
        }
    }
}"""

match_start = content.find(old_subject_grade)
match_end = content.find(end_of_subject_grade, match_start) + len(end_of_subject_grade)

new_subject_grade = """fun SubjectGradeGridCard(
    sub: Subject,
    viewModel: UniBuddyViewModel,
    onClick: (Int) -> Unit
) {
    val assessments by viewModel.getAssessmentsForSubject(sub.id).collectAsStateWithLifecycle(emptyList())

    val currentWeighted = assessments.sumOf { (it.grade ?: 0.0) * (it.percentage / 100.0) }
    val currentPercentage = assessments.sumOf { it.percentage }
    val remainingPercentage = (100.0 - currentPercentage).coerceAtLeast(0.0)

    val targetApprove = 51.0
    val missingAmount = targetApprove - currentWeighted
    val gradeNeeded = if (remainingPercentage > 0) {
        (missingAmount / (remainingPercentage / 100.0)).coerceIn(0.0, 100.0)
    } else 0.0

    val isImpossible = gradeNeeded > 100.0
    val isApproved = currentWeighted >= targetApprove
    val isWarning = missingAmount > 0 && remainingPercentage < 30.0 && gradeNeeded > 70.0

    val statusColor = if (isImpossible || (isWarning && currentPercentage > 50.0)) com.aistudio.unibuddy.qywvsp.ui.theme.StatusRed else if (isWarning) com.aistudio.unibuddy.qywvsp.ui.theme.StatusAmber else if (isApproved) com.aistudio.unibuddy.qywvsp.ui.theme.StatusGreen else com.aistudio.unibuddy.qywvsp.ui.theme.StatusGray

    val hapticFeedback = androidx.compose.ui.platform.LocalHapticFeedback.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                onClick(sub.id)
            }
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = com.aistudio.unibuddy.qywvsp.ui.theme.BackgroundBone),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = sub.name,
                fontSize = 16.sp,
                color = com.aistudio.unibuddy.qywvsp.ui.theme.NavyBlue,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { (currentWeighted / 100.0).toFloat().coerceIn(0f, 1f) },
                color = statusColor,
                trackColor = Color.LightGray.copy(alpha = 0.3f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Nota: ${String.format(java.util.Locale.US, "%.1f", currentWeighted)} / 100",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = com.aistudio.unibuddy.qywvsp.ui.theme.NavyBlue
                )
                Text(
                    text = "${currentPercentage.toInt()}% evaluado",
                    fontSize = 12.sp,
                    color = com.aistudio.unibuddy.qywvsp.ui.theme.SlateGray
                )
            }
        }
    }
}"""
content = content[:match_start] + new_subject_grade + content[match_end:]

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyApp.kt", "w") as f:
    f.write(content)
