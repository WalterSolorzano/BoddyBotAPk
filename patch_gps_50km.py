import re

# Update ViewModel 100.0 to 50.0
with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

content = content.replace("val outOfRange = distance > 100.0", "val outOfRange = distance > 50.0")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)

# Update Widget text
with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/GPSAndStopwatchWidget.kt", "r") as f:
    content = f.read()

content = content.replace('if (isOutOfRange) "Lejos" else "$locationBasedTravelMinutes min"', 'if (isOutOfRange) "Modo Lejos/Vacaciones" else "$locationBasedTravelMinutes min"')

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/GPSAndStopwatchWidget.kt", "w") as f:
    f.write(content)

