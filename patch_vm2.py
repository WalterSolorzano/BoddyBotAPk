import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

old_logic = """                    if (matchedPensum == null) {
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
                    }"""

new_logic = """                    val pensumSubjectId = matchedPensum?.id ?: repository.insertPensumSubject(
                        com.aistudio.unibuddy.qywvsp.data.PensumSubject(
                            careerId = careerId,
                            code = sub.name.take(5).uppercase(),
                            name = sub.name,
                            semester = "Extracurricular",
                            credits = 3.0,
                            isNumbers = false
                        )
                    ).toInt()

                    val status = if (subAssessments.isEmpty()) com.aistudio.unibuddy.qywvsp.data.AssessmentStatus.UNKNOWN
                                 else if (finalGrade >= 60.0) com.aistudio.unibuddy.qywvsp.data.AssessmentStatus.NF_R
                                 else com.aistudio.unibuddy.qywvsp.data.AssessmentStatus.NF_R // NF_R means Regular, grade determines pass/fail
                                 
                    repository.insertAcademicRecord(
                        com.aistudio.unibuddy.qywvsp.data.AcademicRecord(
                            pensumSubjectId = pensumSubjectId,
                            grade = finalGrade,
                            status = status,
                            year = currentYear,
                            academicGroup = "A",
                            professorId = null,
                            rating = null
                        )
                    )"""

content = content.replace(old_logic, new_logic)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
