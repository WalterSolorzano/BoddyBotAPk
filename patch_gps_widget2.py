with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/GPSAndStopwatchWidget.kt", "r") as f:
    content = f.read()

content = content.replace('text = "$locationBasedTravelMinutes min",', 'text = if (isOutOfRange) "Lejos" else "$locationBasedTravelMinutes min",')

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/GPSAndStopwatchWidget.kt", "w") as f:
    f.write(content)
