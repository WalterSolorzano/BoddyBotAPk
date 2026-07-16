import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

old_logic = """            if (missingGrades.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Terracotta.copy(alpha=0.1f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Amber)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Terracotta, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Evaluaciones sin nota", fontWeight = FontWeight.Bold, color = Terracotta, fontSize = 14.sp)
                            Text("Ya pasó la fecha de: ${missingGrades.joinToString(", ") { it.name }}. ¿Ya tienes la nota? Regístrala.", fontSize = 12.sp, color = Terracotta)
                        }
                    }
                }
            }"""

new_logic = """            if (missingGrades.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable { onSubjectClick(missingGrades.first().subjectId) },
                    colors = CardDefaults.cardColors(containerColor = Terracotta.copy(alpha=0.1f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Amber)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Terracotta, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Evaluaciones sin nota", fontWeight = FontWeight.Bold, color = Terracotta, fontSize = 14.sp)
                            val missingTexts = missingGrades.take(3).joinToString(", ") { ass ->
                                val subName = subjects.find { it.id == ass.subjectId }?.name ?: "Materia"
                                "${ass.name} ($subName)"
                            }
                            val moreText = if (missingGrades.size > 3) " y otros más" else ""
                            Text("Ya pasó la fecha de: $missingTexts$moreText. Toca aquí para registrarla.", fontSize = 12.sp, color = Terracotta)
                        }
                    }
                }
            }"""

content = content.replace(old_logic, new_logic)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
