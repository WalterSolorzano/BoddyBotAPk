with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyApp.kt", "r") as f:
    lines = f.readlines()

new_lines = []
skip = False
for line in lines:
    if line.startswith("fun AdvancedGradesAnalytics("):
        new_lines.append("fun AdvancedGradesAnalytics(gradedExams: List<com.aistudio.unibuddy.qywvsp.data.Assessment>, allAssessments: List<com.aistudio.unibuddy.qywvsp.data.Assessment>) {\n")
        new_lines.append("    AlertDialog(\n")
        new_lines.append("        onDismissRequest = {},\n")
        new_lines.append("        title = { Text(\"Análisis Avanzado\", color = com.aistudio.unibuddy.qywvsp.ui.theme.NavyBlue, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },\n")
        new_lines.append("        text = { Text(\"No hay suficientes datos históricos para un análisis predictivo completo.\") },\n")
        new_lines.append("        confirmButton = { TextButton(onClick = {}) { Text(\"Cerrar\") } }\n")
        new_lines.append("    )\n")
        new_lines.append("}\n")
        skip = True
    elif skip:
        if line.strip() == "}":
            skip = False
    else:
        new_lines.append(line)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyApp.kt", "w") as f:
    f.writelines(new_lines)
