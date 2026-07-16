with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/HeroNextClassCard.kt", "r") as f:
    content = f.read()

old_logic = """    if (parsedStartTime != null) {
        val (startHour, startMin) = parsedStartTime
        
        if (isOutOfRange && (totalStartMins - nowMins) < estimatedTravelMinutes) {
            statusText = "Uff no llegas si sales ahora"
            statusColor = Terracotta
            statusBgColor = Terracotta.copy(alpha = 0.1f)
            departureTimeStr = "--:--"
        } else {
            
        // Departure time = start time - travel time - 10 mins buffer
        val totalStartMins = startHour * 60 + startMin"""

new_logic = """    if (parsedStartTime != null) {
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
            
        // Departure time = start time - travel time - 10 mins buffer"""

content = content.replace(old_logic, new_logic)

# Remove the duplicates later down
dup_remove = """        // Compare with current local time
        val now = Calendar.getInstance()
        val nowMins = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)"""

content = content.replace(dup_remove, """        // Compare with current local time (nowMins already calculated)""")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/HeroNextClassCard.kt", "w") as f:
    f.write(content)

