with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

old_logic = """                // If no saved setting, try to get from selected university campus
                if (targetLat == null || targetLon == null) {
                    val uniCoords = getSelectedUniversityCoords()
                    targetLat = uniCoords?.first ?: 12.1156 // Default to Managua if everything fails
                    targetLon = uniCoords?.second ?: -86.2369
                }
                
                val distance = calculateDistance(lat, lon, targetLat, targetLon)"""

new_logic = """                // If no saved setting, try to get from selected university campus
                if (targetLat == null || targetLon == null) {
                    val uniCoords = getSelectedUniversityCoords()
                    targetLat = uniCoords?.first
                    targetLon = uniCoords?.second
                }
                
                if (targetLat != null && targetLon != null) {
                    val distance = calculateDistance(lat, lon, targetLat, targetLon)
                    _currentDistanceToCollege.value = distance
                    
                    if (distance <= 1.5) {
                        _currentLocationName.value = "En la universidad"
                        if (_autoCheckinEnabled.value && !isNicaraguaHoliday(System.currentTimeMillis())) {
                            autoCheckinToCurrentClass()
                        }
                    }
                    
                    val outOfRange = distance > 100.0
                    _isOutOfRange.value = outOfRange
                    
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

# Wait, the old code continues:
#                 val distance = calculateDistance(lat, lon, targetLat, targetLon)
#                 _currentDistanceToCollege.value = distance
# ...
# Let's replace the whole block from 804 to 825.
