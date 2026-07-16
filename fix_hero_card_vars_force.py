with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/HeroNextClassCard.kt", "r") as f:
    content = f.read()

import re

# We want to replace everything from "if (parsedStartTime != null) {" to "var departureMins = totalStartMins - estimatedTravelMinutes - 10"
start_idx = content.find("    if (parsedStartTime != null) {")
end_idx = content.find("        var departureMins = totalStartMins - estimatedTravelMinutes - 10")

if start_idx != -1 and end_idx != -1:
    new_code = """    if (parsedStartTime != null) {
        val (startHour, startMin) = parsedStartTime
        val totalStartMins = startHour * 60 + startMin
        val now = Calendar.getInstance()
        val nowMins = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        
        if (isOutOfRange && (totalStartMins - nowMins) < estimatedTravelMinutes) {
            statusText = "Uff no llegas si sales ahora"
            statusColor = Terracotta
            statusBgColor = Terracotta.copy(alpha = 0.1f)
            departureTimeStr = "--:--"
        } else {
            
        // Departure time = start time - travel time - 10 mins buffer
"""
    content = content[:start_idx] + new_code + content[end_idx:]

# And we have an extra "nowMins" issue later down where it used to calculate it
dup_remove = """        // Compare with current local time (nowMins already calculated)
            
        when {"""
new_remove = """        // Compare with current local time (nowMins already calculated)
            
        when {"""
# No wait, I already replaced the "val nowMins" with "nowMins already calculated"
# But we need to make sure nowMins is available inside the `else` block because `totalStartMins` and `nowMins` are defined OUTSIDE the `else`.
# Let's just write the whole block out.

