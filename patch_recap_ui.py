import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/SeasonRecapScreen.kt", "r") as f:
    content = f.read()

old_summary = """        if (recap.bestSubjectName != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Mejor Materia: ${recap.bestSubjectName}", color = Color.White, fontWeight = FontWeight.Bold)
                    recap.worstSubjectName?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("A Mejorar: $it", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        Button("""

new_summary = """        if (recap.bestSubjectName != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Mejor Materia: ${recap.bestSubjectName}", color = Color.White, fontWeight = FontWeight.Bold)
                    recap.worstSubjectName?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("A Mejorar: $it", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        var showDetails by remember { mutableStateOf(false) }
        
        if (showDetails) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Detalles Completos:", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tareas completadas: ${recap.completedTasksCount}", color = Color.White.copy(alpha = 0.8f))
                    recap.subjectWithMostAbsences?.let { Text("Más faltas en: $it", color = Color.White.copy(alpha = 0.8f)) }
                    recap.mostProductiveTimeOfDay?.let { Text("Hora más productiva: $it", color = Color.White.copy(alpha = 0.8f)) }
                    recap.mostFocusedSubject?.let { Text("Materia más enfocada: $it", color = Color.White.copy(alpha = 0.8f)) }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Text(
                "Ver todos los detalles",
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.clickable { showDetails = true }.padding(8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Button("""

content = content.replace(old_summary, new_summary)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/SeasonRecapScreen.kt", "w") as f:
    f.write(content)
