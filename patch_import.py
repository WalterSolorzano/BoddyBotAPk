import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

old_logic = """            subjects.forEach { parsed ->
                // Try matching with static curriculum
                val matchedStatic = staticSubjects.find {
                    it.code.equals(parsed.code, ignoreCase = true) ||
                    it.name.equals(parsed.name, ignoreCase = true)
                }
                val subjectCode = matchedStatic?.code ?: parsed.code
                val subjectName = matchedStatic?.name ?: parsed.name
                val subjectSemId = matchedStatic?.let { "Semestre ${it.semester}" } ?: "S/D"
"""

new_logic = """            subjects.forEach { parsed ->
                // Use matching info from dialog if available
                val subjectCode = parsed.matchedCode ?: parsed.code
                val subjectName = parsed.matchedName ?: parsed.name
                val matchedStatic = staticSubjects.find { it.code == subjectCode }
                val subjectSemId = matchedStatic?.let { "Semestre ${it.semester}" } ?: "S/D"
"""
content = content.replace(old_logic, new_logic)


old_insert = """                repository.insertAcademicRecord(
                    AcademicRecord(
                        pensumSubjectId = subjectId.toInt(),
                        grade = parsed.grade,
                        status = parsed.status,
                        year = academicTerm,
                        academicGroup = parsed.group
                    )
                )"""

new_insert = """                var profId: Int? = null
                if (parsed.professorName.isNotBlank()) {
                    profId = repository.insertProfessor(
                        com.aistudio.unibuddy.qywvsp.data.Professor(
                            name = parsed.professorName.trim(),
                            avatarSeed = parsed.professorName.trim().take(2).uppercase()
                        )
                    ).toInt()
                }

                repository.insertAcademicRecord(
                    AcademicRecord(
                        pensumSubjectId = subjectId.toInt(),
                        grade = parsed.grade,
                        status = parsed.status,
                        year = academicTerm,
                        academicGroup = parsed.group,
                        professorId = profId,
                        rating = null
                    )
                )"""
content = content.replace(old_insert, new_insert)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
