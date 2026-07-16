import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

old_code = """                    // Check if user is out of range (>100km)
                    val outOfRange = distance > 50.0
                    _isOutOfRange.value = outOfRange
                    
                    // Estimate travel time: 3.5 mins per km + 12 mins buffer.
                    if (outOfRange) {
                        _locationBasedTravelMinutes.value = 0
                    } else {
                        val estimatedMins = (distance * 3.5).toInt() + 12
                        _locationBasedTravelMinutes.value = estimatedMins
                    }"""

new_code = """                    // Check if user is out of range (>100km)
                    val outOfRange = distance > 50.0
                    _isOutOfRange.value = outOfRange
                    
                    // Estimate travel time: 3.5 mins per km + 12 mins buffer.
                    val mins = if (outOfRange) 0 else (distance * 3.5).toInt() + 12
                    _locationBasedTravelMinutes.value = mins
                    
                    // Save to Room for widgets
                    viewModelScope.launch {
                        repository.saveSetting("widget_out_of_range", outOfRange.toString())
                        repository.saveSetting("widget_travel_time", mins.toString())
                    }"""

content = content.replace(old_code, new_code)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
