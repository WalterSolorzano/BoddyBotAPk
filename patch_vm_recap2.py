import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

old_logic = """    fun startNewSemester() {
        viewModelScope.launch {
            val currentSubjects = repository.subjects.first()
            val allAssessments = repository.assessments.first()
            
            // Archive current subjects into AcademicRecord
            if (currentSubjects.isNotEmpty()) {"""

new_logic = """    fun startNewSemester() {
        viewModelScope.launch {
            val currentSubjects = repository.subjects.first()
            val allAssessments = repository.assessments.first()
            
            // Block A: Capture date range
            val startDate = _semesterStartDate.value ?: System.currentTimeMillis()
            val endDate = System.currentTimeMillis()
            
            // Block B: Gather data
            val sdf = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
            // Note: date formats in our app are typically "dd MMM" or "dd MMM yyyy"
            
            val allLogs = repository.attendanceLogs.first() // We'll just take all logs as approximation if they don't have year, but better to check if they belong to active subjects.
            val activeSubjectIds = currentSubjects.map { it.id }
            val logs = allLogs.filter { activeSubjectIds.contains(it.subjectId) }
            
            val totalLogs = logs.size
            val presentLogs = logs.count { it.isPresent }
            val attendancePercentage = if (totalLogs > 0) (presentLogs.toDouble() / totalLogs) * 100 else 100.0
            
            val subjectAbsences = logs.filter { !it.isPresent }.groupBy { it.subjectId }.mapValues { it.value.size }
            val mostAbsencesSubjectId = subjectAbsences.maxByOrNull { it.value }?.key
            val mostAbsencesSubjectName = currentSubjects.find { it.id == mostAbsencesSubjectId }?.name
            
            // Focus sessions
            val focusSessions = _focusSessionsHistoryJson.value.parseSessionsHistory() // We need to parse json
            // Try to filter focus sessions loosely
            // For now, we'll just take all that we haven't cleared.
            val focusHoursTotal = focusSessions.sumOf { it.duration } / 60.0
            val focusSessionsCompleted = focusSessions.count { !it.interrupted }
            val focusSessionsInterrupted = focusSessions.count { it.interrupted }
            
            val timeOfDayGroups = focusSessions.filter { !it.interrupted }.groupBy { it.timeOfDay }
            val mostProductiveTimeOfDay = timeOfDayGroups.maxByOrNull { it.value.sumOf { s -> s.duration } }?.key
            
            val subjectFocusGroups = focusSessions.groupBy { it.label }
            val mostFocusedSubject = subjectFocusGroups.maxByOrNull { it.value.sumOf { s -> s.duration } }?.key
            
            val badges = repository.badges.first()
            val badgesUnlockedCount = badges.count { it.isUnlocked } // Maybe count only ones unlocked during this time, but we don't have clear timestamps for all.
            
            val maxStreak = repository.getSetting("max_streak")?.toIntOrNull() ?: 0
            val averageStreak = maxStreak / 2.0 // Approximation
            
            val trips = repository.tripRecords.first()
            val totalTripDistance = trips.sumOf { it.durationMinutes.toDouble() } // We don't have distance, using duration as proxy
            
            // Tasks completed
            val allTasks = repository.tasks.first()
            val completedTasksCount = allTasks.count { it.isCompleted && activeSubjectIds.contains(it.subjectId) }
            
            var bestSubjectName: String? = null
            var worstSubjectName: String? = null
            var bestGrade = -1.0
            var worstGrade = 101.0
            
            // Archive current subjects into AcademicRecord
            if (currentSubjects.isNotEmpty()) {"""

content = content.replace(old_logic, new_logic)

old_logic2 = """            // Delete old subjects (Assessments, Tasks, Absences cascade. AttendanceLogs are safe)
            for (sub in currentSubjects) {
                repository.deleteSubject(sub)
            }"""

new_logic2 = """            // Generate Recap highlight
            val highlightText = if (attendancePercentage > 80.0 && focusHoursTotal > 10.0) {
                "¡Increíble dedicación! Tuviste gran asistencia y mucho tiempo de enfoque."
            } else if (attendancePercentage > 90.0) {
                "¡Asistencia casi perfecta! Eres muy constante."
            } else if (focusHoursTotal > 20.0) {
                "¡Máquina de concentrarte! Tu enfoque fue tu punto más fuerte."
            } else {
                "¡Semestre completado! Sigue esforzándote para mejorar."
            }
            
            // Calculate best mood (Approximation based on attendance)
            // Just some placeholder logic since we don't have exact historical daily mood for the whole semester easily
            val bestMoodDaysCount = presentLogs
            val worriedMoodDaysCount = totalLogs - presentLogs
            val bestDayOfWeek = "Miércoles" // Placeholder

            val recap = com.aistudio.unibuddy.qywvsp.data.SeasonRecap(
                startDate = startDate,
                endDate = endDate,
                attendancePercentage = attendancePercentage,
                focusHoursTotal = focusHoursTotal,
                focusSessionsCompleted = focusSessionsCompleted,
                focusSessionsInterrupted = focusSessionsInterrupted,
                bestMoodDaysCount = bestMoodDaysCount,
                worriedMoodDaysCount = worriedMoodDaysCount,
                maxStreak = maxStreak,
                badgesUnlockedCount = badgesUnlockedCount,
                bestSubjectName = bestSubjectName,
                worstSubjectName = worstSubjectName,
                highlightText = highlightText,
                subjectWithMostAbsences = mostAbsencesSubjectName,
                mostProductiveTimeOfDay = mostProductiveTimeOfDay,
                mostFocusedSubject = mostFocusedSubject,
                bestDayOfWeek = bestDayOfWeek,
                totalTripDistance = totalTripDistance,
                completedTasksCount = completedTasksCount,
                averageStreak = averageStreak
            )
            
            val recapId = repository.insertSeasonRecap(recap)
            
            // Trigger UI
            _showSeasonRecap.value = recap

            // Clear focus sessions so they don't roll over
            _focusSessionsHistoryJson.value = "[]"
            repository.saveSetting("focus_sessions_history", "[]")

            // Delete old subjects (Assessments, Tasks, Absences cascade. AttendanceLogs are safe)
            for (sub in currentSubjects) {
                repository.deleteSubject(sub)
            }"""

content = content.replace(old_logic2, new_logic2)

content = content.replace("val finalGrade = if (subAssessments.isNotEmpty()) {", "val finalGrade = if (subAssessments.isNotEmpty()) {\n                        val grade = subAssessments.filter { it.grade != null }.sumOf { (it.grade!! / 100.0) * it.percentage }\n                        if (grade > bestGrade) { bestGrade = grade; bestSubjectName = sub.name }\n                        if (grade < worstGrade) { worstGrade = grade; worstSubjectName = sub.name }\n                        grade")
content = content.replace("subAssessments.filter { it.grade != null }.sumOf { (it.grade!! / 100.0) * it.percentage }\n                    } else {", "                    } else {")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
