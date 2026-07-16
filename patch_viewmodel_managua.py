import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

old_logic = """                var targetLon = savedLonStr?.toDoubleOrNull()
                
                // If no saved setting, try to get from selected university campus
                if (targetLat == null || targetLon == null) {
                    val uniCoords = getSelectedUniversityCoords()
                    targetLat = uniCoords?.first ?: 12.1156 // Default to Managua if everything fails
                    targetLon = uniCoords?.second ?: -86.2369
                }
                
                val distance = calculateDistance(lat, lon, targetLat, targetLon)
                _currentDistanceToCollege.value = distance
                
                // If within 1.5 km (accommodating GPS errors), consider it "En la universidad"
                if (distance <= 1.5) {
                    _currentLocationName.value = "En la universidad"
                    
                    if (_autoCheckinEnabled.value && !isNicaraguaHoliday(System.currentTimeMillis())) {
                        autoCheckinToCurrentClass()
                    }
                }
                
                // Check if user is out of range (>100km)
                val outOfRange = distance > 100.0
                _isOutOfRange.value = outOfRange
                
                // Estimate travel time: 3.5 mins per km + 12 mins buffer.
                if (outOfRange) {
                    _locationBasedTravelMinutes.value = 0
                } else {
                    val estimatedMins = (distance * 3.5).toInt() + 12
                    _locationBasedTravelMinutes.value = estimatedMins
                }"""

new_logic = """                var targetLon = savedLonStr?.toDoubleOrNull()
                
                // If no saved setting, try to get from selected university campus
                if (targetLat == null || targetLon == null) {
                    val uniCoords = getSelectedUniversityCoords()
                    targetLat = uniCoords?.first
                    targetLon = uniCoords?.second
                }
                
                if (targetLat != null && targetLon != null) {
                    val distance = calculateDistance(lat, lon, targetLat!!, targetLon!!)
                    _currentDistanceToCollege.value = distance
                    
                    // If within 1.5 km (accommodating GPS errors), consider it "En la universidad"
                    if (distance <= 1.5) {
                        _currentLocationName.value = "En la universidad"
                        
                        if (_autoCheckinEnabled.value && !isNicaraguaHoliday(System.currentTimeMillis())) {
                            autoCheckinToCurrentClass()
                        }
                    }
                    
                    // Check if user is out of range (>100km)
                    val outOfRange = distance > 100.0
                    _isOutOfRange.value = outOfRange
                    
                    // Estimate travel time: 3.5 mins per km + 12 mins buffer.
                    if (outOfRange) {
                        _locationBasedTravelMinutes.value = 0
                    } else {
                        val estimatedMins = (distance * 3.5).toInt() + 12
                        _locationBasedTravelMinutes.value = estimatedMins
                    }
                } else {
                    _currentDistanceToCollege.value = null
                    _locationBasedTravelMinutes.value = _baseTravelTime.value
                }"""

content = content.replace(old_logic, new_logic)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
