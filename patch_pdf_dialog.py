import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

old_parse_logic = """                        val parser = com.aistudio.unibuddy.qywvsp.data.HistorialParser()
                        val parsedSubjects = parser.parsePdfText(text)
                        
                        if (parsedSubjects.isNotEmpty()) {
                            parsedSubjectsToConfirm = parsedSubjects
                        } else {
                            resultMessage = "No se encontraron registros académicos con el formato estándar en este PDF."
                        }"""

new_parse_logic = """                        val parser = com.aistudio.unibuddy.qywvsp.data.HistorialParser()
                        val parsedSubjects = parser.parsePdfText(text)
                        
                        if (parsedSubjects.isNotEmpty()) {
                            val uni = viewModel.userUniversity.value
                            val career = viewModel.career.value
                            val staticSubjects = com.aistudio.unibuddy.qywvsp.data.CurriculumData.getSubjectsFor(
                                uni.ifEmpty { "UNI" },
                                career.ifEmpty { "Ing. Industrial" }
                            )
                            parsedSubjects.forEach { parsed ->
                                var matched = staticSubjects.find {
                                    it.code.equals(parsed.code, ignoreCase = true) ||
                                    it.name.equals(parsed.name, ignoreCase = true)
                                }
                                if (matched == null) {
                                    matched = staticSubjects.find {
                                        com.aistudio.unibuddy.qywvsp.data.FuzzyMatch.isSimilar(it.name, parsed.name)
                                    }
                                }
                                parsed.matchedCode = matched?.code
                                parsed.matchedName = matched?.name
                            }
                            parsedSubjectsToConfirm = parsedSubjects
                        } else {
                            resultMessage = "No se encontraron registros académicos con el formato estándar en este PDF."
                        }"""

content = content.replace(old_parse_logic, new_parse_logic)

old_dialog_content = """                if (parsedSubjectsToConfirm.isNotEmpty()) {
                    Text("Se capturaron ${parsedSubjectsToConfirm.size} materias. Confirma para guardar:", fontWeight = FontWeight.Bold, color = DarkGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(parsedSubjectsToConfirm) { sub ->
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Bone)) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(sub.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyBlue)
                                    Text("Grupo: ${sub.group} | Nota: ${sub.grade} | Créditos: ${sub.credits} | Estado: ${sub.status.name}", fontSize = 11.sp, color = SlateGray)
                                }
                            }
                        }
                    }
                }"""

new_dialog_content = """                if (parsedSubjectsToConfirm.isNotEmpty()) {
                    Text("Se capturaron ${parsedSubjectsToConfirm.size} materias. Confirma y ajusta:", fontWeight = FontWeight.Bold, color = DarkGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        itemsIndexed(parsedSubjectsToConfirm) { index, sub ->
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Bone)) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Extraído: ${sub.name} (${sub.code})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyBlue)
                                    Text("Nota: ${sub.grade} | ${sub.status.name}", fontSize = 11.sp, color = SlateGray)
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(if (sub.matchedName != null) "Emparejado con: ${sub.matchedName}" else "Sin coincidencia en pensum", 
                                        color = if (sub.matchedName != null) DarkGreen else Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = sub.professorName,
                                        onValueChange = { 
                                            val newList = parsedSubjectsToConfirm.toMutableList()
                                            newList[index] = sub.copy(professorName = it)
                                            parsedSubjectsToConfirm = newList
                                        },
                                        label = { Text("Profesor (Opcional)", fontSize = 10.sp) },
                                        modifier = Modifier.fillMaxWidth().height(50.dp),
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                                    )
                                }
                            }
                        }
                    }
                }"""

content = content.replace(old_dialog_content, new_dialog_content)

# We need to add itemsIndexed import if not present
if "import androidx.compose.foundation.lazy.itemsIndexed" not in content:
    content = content.replace("import androidx.compose.foundation.lazy.items", "import androidx.compose.foundation.lazy.items\nimport androidx.compose.foundation.lazy.itemsIndexed")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
