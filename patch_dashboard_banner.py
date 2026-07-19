import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

old_banner = """                        Column(modifier = Modifier.weight(1f)) {
                            Text("¡Nueva versión disponible!", fontWeight = FontWeight.Bold, color = ProBlue, fontSize = 16.sp)
                            Text("Versión ${info.versionName}: ${info.releaseNotes}", color = SlateGray, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }"""

new_banner = """                        Column(modifier = Modifier.weight(1f)) {
                            Text("¡Nueva versión disponible!", fontWeight = FontWeight.Bold, color = ProBlue, fontSize = 16.sp)
                            Text("Versión ${info.versionName}", color = SlateGray, fontSize = 10.sp)
                            val notesList = info.releaseNotes.split(Regex("\\n|- ")).filter { it.trim().isNotEmpty() }
                            Column(modifier = Modifier.padding(top = 4.dp)) {
                                notesList.take(3).forEach { note ->
                                    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(bottom = 2.dp)) {
                                        Text("• ", fontSize = 12.sp, color = SlateGray, fontWeight = FontWeight.Bold)
                                        Text(note.trim(), color = SlateGray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                if (notesList.size > 3) {
                                    Text("...", fontSize = 12.sp, color = SlateGray)
                                }
                            }
                        }"""

content = content.replace(old_banner, new_banner)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
