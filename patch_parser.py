import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/data/HistorialParser.kt", "r") as f:
    content = f.read()

old_parsed = """    data class ParsedSubject(
        val code: String,
        val name: String,
        val credits: Double,
        val grade: Double,
        val status: AssessmentStatus,
        val group: String,
        val semester: String,
        val year: String
    )"""

new_parsed = """    data class ParsedSubject(
        val code: String,
        val name: String,
        val credits: Double,
        val grade: Double,
        val status: AssessmentStatus,
        val group: String,
        val semester: String,
        val year: String,
        var matchedCode: String? = null,
        var matchedName: String? = null,
        var professorName: String = ""
    )"""

content = content.replace(old_parsed, new_parsed)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/data/HistorialParser.kt", "w") as f:
    f.write(content)
