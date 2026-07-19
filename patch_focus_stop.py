import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/FocusModeScreen.kt", "r") as f:
    content = f.read()

old_stop = """                        IconButton(
                            onClick = {
                                isRunning = false
                                timeLeft = if (isWorkMode) workMinutes * 60 else breakMinutes * 60
                                context.startService(android.content.Intent(context, PomodoroService::class.java).apply {
                                    action = PomodoroService.ACTION_STOP
                                })
                            },
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Detener", modifier = Modifier.size(32.dp), tint = SlateGray)
                        }"""

new_stop = """                        IconButton(
                            onClick = {
                                if (isRunning && isWorkMode) {
                                    val elapsedMinutes = workMinutes - (timeLeft / 60)
                                    if (elapsedMinutes > 0) {
                                        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                                        val updatedHistory = sessionsHistory + FocusSessionRecord(
                                            date = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date()),
                                            duration = elapsedMinutes,
                                            label = "Estudio (Interrumpido)",
                                            timeOfDay = timeFormat,
                                            interrupted = true
                                        )
                                        viewModel.saveFocusSessionsHistory(updatedHistory.toSessionsJsonString())
                                    }
                                }
                                isRunning = false
                                timeLeft = if (isWorkMode) workMinutes * 60 else breakMinutes * 60
                                context.startService(android.content.Intent(context, PomodoroService::class.java).apply {
                                    action = PomodoroService.ACTION_STOP
                                })
                            },
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Detener", modifier = Modifier.size(32.dp), tint = SlateGray)
                        }"""

content = content.replace(old_stop, new_stop)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/FocusModeScreen.kt", "w") as f:
    f.write(content)
