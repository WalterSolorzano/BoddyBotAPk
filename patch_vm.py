import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

old_func = """    fun startNewSemester() {
        viewModelScope.launch {
            // Delete old subjects, assessments, logs
            val currentSubjects = repository.subjects.first()
            for (sub in currentSubjects) {
                repository.deleteSubject(sub)
            }
            // Start a new semester by resetting start date and asking for subjects (onboarding)
            val now = System.currentTimeMillis()
            _semesterStartDate.value = now
            repository.saveSetting("semester_start_date", now.toString())
            
            _isOnboardingCompleted.value = false
            repository.saveSetting("onboarding_completed", "false")
        }
    }"""

new_func = """    fun startNewSemester() {
        viewModelScope.launch {
            val currentSubjects = repository.subjects.first()
            val allAssessments = repository.assessments.first()
            
            // Archive current subjects into AcademicRecord
            if (currentSubjects.isNotEmpty()) {
                val careerId = repository.getOrCreateCareer(
                    universityName = _userUniversity.value.takeIf { it.isNotBlank() } ?: "UNI",
                    campusName = "Campus Central",
                    careerName = _career.value.takeIf { it.isNotBlank() } ?: "Carrera"
                )
                
                val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR).toString()
                
                for (sub in currentSubjects) {
                    val subAssessments = allAssessments.filter { it.subjectId == sub.id }
                    val finalGrade = if (subAssessments.isNotEmpty()) {
                        subAssessments.filter { it.grade != null }.sumOf { (it.grade!! / 100.0) * it.percentage }
                    } else {
                        0.0
                    }
                    
                    // Match with PensumSubject or create one
                    val pensumSubjects = repository.getPensumForCareer(careerId).first()
                    var matchedPensum = pensumSubjects.find { it.name.equals(sub.name, ignoreCase = true) || it.code.equals(sub.name, ignoreCase = true) }
                    
                    if (matchedPensum == null) {
                        // Create a dummy pensum subject for the archive
                        val newId = repository.insertPensumSubject(
                            com.aistudio.unibuddy.qywvsp.data.PensumSubject(
                                careerId = careerId,
                                code = sub.name.take(5).uppercase(),
                                name = sub.name,
                                semester = "Extracurricular",
                                credits = 3.0,
                                isNumbers = false
                            )
                        )
                        matchedPensum = repository.getPensumForCareer(careerId).first().find { it.id == newId.toInt() }
                    }
                    
                    // Insert into AcademicRecord
                    if (matchedPensum != null) {
                        val status = if (subAssessments.isEmpty()) com.aistudio.unibuddy.qywvsp.data.AssessmentStatus.UNKNOWN
                                     else if (finalGrade >= 60.0) com.aistudio.unibuddy.qywvsp.data.AssessmentStatus.NF_R
                                     else com.aistudio.unibuddy.qywvsp.data.AssessmentStatus.NF_R
                                     
                        repository.insertAcademicRecord(
                            com.aistudio.unibuddy.qywvsp.data.AcademicRecord(
                                pensumSubjectId = matchedPensum.id,
                                grade = finalGrade,
                                status = status,
                                year = currentYear,
                                academicGroup = "A",
                                professorId = null,
                                rating = null
                            )
                        )
                    }
                }
            }

            // Delete old subjects (Assessments, Tasks, Absences cascade. AttendanceLogs are safe)
            for (sub in currentSubjects) {
                repository.deleteSubject(sub)
            }
            
            // Start a new semester by resetting start date and asking for subjects (onboarding)
            val now = System.currentTimeMillis()
            _semesterStartDate.value = now
            repository.saveSetting("semester_start_date", now.toString())
            
            _isOnboardingCompleted.value = false
            repository.saveSetting("onboarding_completed", "false")
        }
    }"""

content = content.replace(old_func, new_func)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
