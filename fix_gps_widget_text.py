import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/GPSAndStopwatchWidget.kt", "r") as f:
    content = f.read()

# Fix font size dynamically if out of range
old_text = """                    Text(
                        text = if (isOutOfRange) "Fuera del Rango Académico (Modo Vacaciones/Lejos)" else "$locationBasedTravelMinutes min",
                        fontSize = 20.sp,"""

new_text = """                    Text(
                        text = if (isOutOfRange) "Fuera del Rango Académico\\n(Modo Vacaciones)" else "$locationBasedTravelMinutes min",
                        fontSize = if (isOutOfRange) 12.sp else 20.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Right,"""

content = content.replace(old_text, new_text)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/GPSAndStopwatchWidget.kt", "w") as f:
    f.write(content)

