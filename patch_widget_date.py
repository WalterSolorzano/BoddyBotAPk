import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/widget/TamagotchiWidget.kt", "r") as f:
    content = f.read()

old_logic = """        val examTomorrow = assessments.firstOrNull { it.grade == null && it.examDate.trim().equals(currentDayCode, ignoreCase = true) }"""

new_logic = """        val fullDateStr = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(calendar.time)
        val examTomorrow = assessments.firstOrNull { it.grade == null && (it.examDate.trim().equals(currentDayCode, ignoreCase = true) || it.examDate == fullDateStr) }"""

content = content.replace(old_logic, new_logic)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/widget/TamagotchiWidget.kt", "w") as f:
    f.write(content)
