import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/OnboardingScreen.kt", "r") as f:
    content = f.read()

# Replace the auto-populate logic with just clear
old_logic = """        // Auto-populate default schedules for conflict-free start
        subjectSessions.clear()
        val days = listOf("Lu", "Ma", "Mi", "Ju", "Vi")
        val blocks = listOf("M1", "M2", "M3", "T1", "T2", "T3")
        
        matchingCurriculum.forEachIndexed { index, subject ->
            val day = days[index % days.size]
            val block = blocks[index % blocks.size]
            subjectSessions[subject.name] = listOf(ClassSessionDetails(day, block, "Aula por definir"))
        }"""

new_logic = """        subjectSessions.clear()
        // No auto-populate, let user define."""
content = content.replace(old_logic, new_logic)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/OnboardingScreen.kt", "w") as f:
    f.write(content)
