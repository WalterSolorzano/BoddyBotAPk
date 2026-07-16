with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

old_logic = """                // Estimate travel time: 3.5 mins per km + 12 mins buffer.
                val estimatedMins = (distance * 3.5).toInt() + 12
                _locationBasedTravelMinutes.value = estimatedMins"""

new_logic = """                // Estimate travel time: 3.5 mins per km + 12 mins buffer.
                if (outOfRange) {
                    _locationBasedTravelMinutes.value = 0
                } else {
                    val estimatedMins = (distance * 3.5).toInt() + 12
                    _locationBasedTravelMinutes.value = estimatedMins
                }"""

content = content.replace(old_logic, new_logic)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
