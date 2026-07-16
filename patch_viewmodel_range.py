with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

# Add _isOutOfRange property
prop_str = """    private val _locationBasedTravelMinutes = MutableStateFlow(30)
    val locationBasedTravelMinutes: StateFlow<Int> = _locationBasedTravelMinutes.asStateFlow()
    
    private val _isOutOfRange = MutableStateFlow(false)
    val isOutOfRange: StateFlow<Boolean> = _isOutOfRange.asStateFlow()
"""
content = content.replace("    private val _locationBasedTravelMinutes = MutableStateFlow(30)\n    val locationBasedTravelMinutes: StateFlow<Int> = _locationBasedTravelMinutes.asStateFlow()\n", prop_str)


# Update the logic inside updateLocationStatus
old_logic = """                // Estimate travel time: 3.5 mins per km + 12 mins buffer.
                val estimatedMins = (distance * 3.5).toInt() + 12
                _locationBasedTravelMinutes.value = estimatedMins"""
new_logic = """                // Check if user is out of range (>100km)
                val outOfRange = distance > 100.0
                _isOutOfRange.value = outOfRange
                
                // Estimate travel time: 3.5 mins per km + 12 mins buffer.
                val estimatedMins = if (outOfRange) {
                    0 // Suspend location-based travel time if out of range
                } else {
                    (distance * 3.5).toInt() + 12
                }
                _locationBasedTravelMinutes.value = estimatedMins"""

if old_logic in content:
    content = content.replace(old_logic, new_logic)
else:
    print("Could not find old logic in updateLocationStatus")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
