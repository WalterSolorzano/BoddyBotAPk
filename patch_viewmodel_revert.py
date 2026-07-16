import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

old_logic = """                // Estimate travel time: 3.5 mins per km + 12 mins buffer.
                val estimatedMins = if (outOfRange) {
                    0 // Suspend location-based travel time if out of range
                } else {
                    (distance * 3.5).toInt() + 12
                }
                _locationBasedTravelMinutes.value = estimatedMins"""

new_logic = """                // Estimate travel time: 3.5 mins per km + 12 mins buffer.
                val estimatedMins = (distance * 3.5).toInt() + 12
                _locationBasedTravelMinutes.value = estimatedMins"""

if old_logic in content:
    content = content.replace(old_logic, new_logic)
else:
    print("Could not find old logic to revert!")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)

