import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/SemesterHistoryView.kt", "r") as f:
    content = f.read()

# Add state
if "val seasonRecaps by viewModel.seasonRecaps.collectAsStateWithLifecycle()" not in content:
    content = content.replace("val tripRecords by viewModel.tripRecords.collectAsStateWithLifecycle()", "val tripRecords by viewModel.tripRecords.collectAsStateWithLifecycle()\n    val seasonRecaps by viewModel.seasonRecaps.collectAsStateWithLifecycle()")

# Add tab button
old_tabs = """            SegmentedButton(
                selected = logTab == "history",
                onClick = { logTab = "history" },
                text = "Historial Académico"
            )"""
new_tabs = """            SegmentedButton(
                selected = logTab == "history",
                onClick = { logTab = "history" },
                text = "Historial Académico"
            )
            SegmentedButton(
                selected = logTab == "seasons",
                onClick = { logTab = "seasons" },
                text = "Temporadas"
            )"""
content = content.replace(old_tabs, new_tabs)

# Add seasons tab content
old_when = """        when (logTab) {
            "insights" -> {"""
new_when = """        when (logTab) {
            "seasons" -> {
                if (seasonRecaps.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("No hay temporadas cerradas aún.", color = SlateGray)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        seasonRecaps.forEach { recap ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                                    val startStr = sdf.format(java.util.Date(recap.startDate))
                                    val endStr = sdf.format(java.util.Date(recap.endDate))
                                    Text(text = "Temporada: $startStr - $endStr", fontWeight = FontWeight.Bold, color = NavyBlue)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = recap.highlightText, fontSize = 14.sp, color = ProBlue, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text("Asistencia", fontSize = 12.sp, color = SlateGray)
                                            Text("${recap.attendancePercentage.toInt()}%", fontWeight = FontWeight.Bold)
                                        }
                                        Column {
                                            Text("Enfoque", fontSize = 12.sp, color = SlateGray)
                                            Text("${String.format("%.1f", recap.focusHoursTotal)}h", fontWeight = FontWeight.Bold)
                                        }
                                        Column {
                                            Text("Racha", fontSize = 12.sp, color = SlateGray)
                                            Text("🔥 ${recap.maxStreak}", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "insights" -> {"""
content = content.replace(old_when, new_when)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/SemesterHistoryView.kt", "w") as f:
    f.write(content)
