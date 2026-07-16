with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/HeroNextClassCard.kt", "r") as f:
    content = f.read()

start_idx = content.find("    if (parsedStartTime != null) {")
end_idx = content.find("        }\n    }\n")

if start_idx != -1 and end_idx != -1:
    old_block = content[start_idx:end_idx]
    new_block = """    if (parsedStartTime != null) {
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
            var departureMins = totalStartMins - estimatedTravelMinutes - 10
            if (departureMins < 0) {
                departureMins += 24 * 60
            }
            
            val depHour = (departureMins / 60) % 24
            val depMin = departureMins % 60
            departureTimeStr = formatTime12Hour(depHour, depMin)

            when {
                nowMins > totalStartMins -> {
                    isLate = true
                    val minsPast = nowMins - totalStartMins
                    statusText = "Clase iniciada hace $minsPast min"
                    statusColor = Terracotta
                    statusBgColor = Terracotta.copy(alpha = 0.1f)
                }
                nowMins > departureMins -> {
                    isLate = true
                    val minsDelayed = nowMins - departureMins
                    statusText = "Deberías estar en camino (atraso de $minsDelayed min)"
                    statusColor = Terracotta
                    statusBgColor = Terracotta.copy(alpha = 0.1f)
                }
                nowMins == departureMins -> {
                    statusText = "¡Debes salir ahora mismo!"
                    statusColor = Color(0xFFD97706) // Darker Amber
                    statusBgColor = Color(0xFFFEF3C7)
                }
                else -> {
                    val minsLeft = departureMins - nowMins
                    statusText = "A tiempo (sugerido salir en $minsLeft min)"
                    statusColor = DarkGreen
                    statusBgColor = MintGreen.copy(alpha = 0.1f)
                }
            }
        }"""
    content = content[:start_idx] + new_block + content[end_idx:]

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/HeroNextClassCard.kt", "w") as f:
    f.write(content)
