import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/SmartRouteReminderCard.kt", "r") as f:
    content = f.read()

# Replace the block for nextClassInfo
old_nextClassInfo = """    val nextClassInfo = remember(subjects, currentDayCode, nowTotalMins) {
        var foundSubject: Subject? = null
        var foundTime = ""
        var isTomorrow = false

        // 1. Filter and sort today's classes
        val todayClasses = subjects.filter { sub -> sub.sessions.any { it.day.equals(currentDayCode, ignoreCase = true) } }
            .mapNotNull { sub ->
                val session = sub.sessions.firstOrNull { it.day.equals(currentDayCode, ignoreCase = true) }
                if (session != null) {
                    val parsed = parseStartTime(session.time)
                    if (parsed != null) {
                        val (h, m) = parsed
                        if ((h * 60 + m) + 120 > nowTotalMins) { // allow if within 2 hours after start
                            return@mapNotNull Triple(sub, session.time, h * 60 + m)
                        }
                    }
                }
                null
            }.sortedBy { it.third }

        if (todayClasses.isNotEmpty()) {
            foundSubject = todayClasses.first().first
            foundTime = todayClasses.first().second
        } else {
            // Check tomorrow's classes
            val tomorrowIndex = (currentDayIndex + 1) % 7
            val tomorrowCode = daysOfWeekCodes[tomorrowIndex]
            val tomorrowClasses = subjects.filter { sub -> sub.sessions.any { it.day.equals(tomorrowCode, ignoreCase = true) } }
                .mapNotNull { sub ->
                    val session = sub.sessions.firstOrNull { it.day.equals(tomorrowCode, ignoreCase = true) }
                    if (session != null) {
                        val parsed = parseStartTime(session.time)
                        if (parsed != null) {
                            val (h, m) = parsed
                            return@mapNotNull Triple(sub, session.time, h * 60 + m)
                        }
                    }
                    null
                }.sortedBy { it.third }

            if (tomorrowClasses.isNotEmpty()) {
                foundSubject = tomorrowClasses.first().first
                foundTime = tomorrowClasses.first().second
                isTomorrow = true
            }
        }

        if (foundSubject != null) Triple(foundSubject, foundTime, isTomorrow) else null
    }

    val firstSubject = nextClassInfo?.first
    val firstClassTime = nextClassInfo?.second ?: ""
    val isTomorrow = nextClassInfo?.third ?: false"""

new_nextClassInfo = """    val nextClassInfo = remember(subjects, currentDayCode, nowTotalMins) {
        var foundSubject: Subject? = null
        var foundTime = ""
        var daysAhead = 0

        for (i in 0..7) {
            val checkIndex = (currentDayIndex + i) % 7
            val checkCode = daysOfWeekCodes[checkIndex]
            
            val dayClasses = subjects.filter { sub -> sub.sessions.any { it.day.equals(checkCode, ignoreCase = true) } }
                .mapNotNull { sub ->
                    val session = sub.sessions.firstOrNull { it.day.equals(checkCode, ignoreCase = true) }
                    if (session != null) {
                        val parsed = parseStartTime(session.time)
                        if (parsed != null) {
                            val (h, m) = parsed
                            if (i == 0) {
                                if ((h * 60 + m) + 120 > nowTotalMins) {
                                    return@mapNotNull Triple(sub, session.time, h * 60 + m)
                                }
                            } else {
                                return@mapNotNull Triple(sub, session.time, h * 60 + m)
                            }
                        }
                    }
                    null
                }.sortedBy { it.third }
                
            if (dayClasses.isNotEmpty()) {
                foundSubject = dayClasses.first().first
                foundTime = dayClasses.first().second
                daysAhead = i
                break
            }
        }
        
        if (foundSubject != null) Triple(foundSubject, foundTime, daysAhead) else null
    }

    val firstSubject = nextClassInfo?.first
    val firstClassTime = nextClassInfo?.second ?: ""
    val daysAhead = nextClassInfo?.third ?: 0
    val isTomorrow = daysAhead == 1"""

content = content.replace(old_nextClassInfo, new_nextClassInfo)

# Replace departure time calculation
old_dep = """    // Check if user is currently past departure time
    val isPastDeparture = remember(parsedTime, baseTravelTime, isTomorrow) {
        if (isTomorrow) false else {
            parsedTime?.let { (hour, min) ->
                val (depHour, depMin) = calculateDepartureTime(hour, min, baseTravelTime)
                val now = Calendar.getInstance()
                val nowHour = now.get(Calendar.HOUR_OF_DAY)
                val nowMin = now.get(Calendar.MINUTE)
                var depTotal = depHour * 60 + depMin
                var classTotal = hour * 60 + min
                var nowTotal = nowHour * 60 + nowMin
                
                // Handle midnight wrap around for departure time (e.g. class at 01:00 AM, departure at 23:30)
                if (depTotal > classTotal) {
                    if (nowTotal < 120) nowTotal += 1440
                    classTotal += 1440
                }
                
                nowTotal >= depTotal
            } ?: false
        }
    }"""
    
new_dep = """    // Check if user is currently past departure time
    val isPastDeparture = remember(parsedTime, baseTravelTime, daysAhead) {
        parsedTime?.let { (hour, min) ->
            val totalClassMinsFromNow = (daysAhead * 24 * 60) + (hour * 60 + min) - nowTotalMins
            val depMinsFromNow = totalClassMinsFromNow - baseTravelTime - 10 // 10 mins buffer
            depMinsFromNow <= 0
        } ?: false
    }
    
    val departureDayName = remember(daysAhead) {
        if (daysAhead == 0) "hoy"
        else if (daysAhead == 1) "mañana"
        else {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, daysAhead)
            val format = java.text.SimpleDateFormat("EEEE", java.util.Locale("es", "ES"))
            "el " + format.format(cal.time).replaceFirstChar { it.titlecase() }
        }
    }"""
content = content.replace(old_dep, new_dep)

# Replace UI text
old_ui = """                // We have a class and departure time!
                val statusText = if (isTomorrow) {
                    val clothing = if (isRaining) "paraguas y abrigo" else "ropa cómoda"
                    "Mañana debes salir a las ${departureTimeFormatted}. El clima estará ${weatherDescription.lowercase()}, así que lleva $clothing."
                } else if (isPastDeparture) {
                    "¡Deberías ir saliendo! Estás lejos y tu clase es pronto."
                } else {
                    val clothing = if (isRaining) "tu paraguas" else "todo lo necesario"
                    "Tienes tiempo. Prepárate con calma para salir a las $departureTimeFormatted. No olvides $clothing."
                }
                val statusColor = if (isTomorrow) NavyBlue else if (isPastDeparture) Color.White else DarkGreen
                val statusBg = if (isTomorrow) Color(0xFFF0F4FA) else if (isPastDeparture) Terracotta else Color(0xFFF3FAF7)

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isTomorrow) "Primera clase de mañana:" else "Siguiente clase de hoy:","""
                                
new_ui = """                // We have a class and departure time!
                val statusText = if (isOutOfRange && isPastDeparture) {
                    "Uff no llegas si sales ahora."
                } else if (isOutOfRange && !isPastDeparture) {
                    "Debes salir $departureDayName a las ${departureTimeFormatted} para llegar a tiempo (trayecto largo)."
                } else if (daysAhead > 0) {
                    val clothing = if (isRaining) "paraguas y abrigo" else "ropa cómoda"
                    "${departureDayName.replaceFirstChar { it.titlecase() }} debes salir a las ${departureTimeFormatted}. El clima estará ${weatherDescription.lowercase()}, lleva $clothing."
                } else if (isPastDeparture) {
                    "¡Deberías ir saliendo! Estás lejos y tu clase es pronto."
                } else {
                    val clothing = if (isRaining) "tu paraguas" else "todo lo necesario"
                    "Tienes tiempo. Prepárate con calma para salir a las $departureTimeFormatted. No olvides $clothing."
                }
                val statusColor = if (daysAhead > 0 && !isPastDeparture) NavyBlue else if (isPastDeparture) Color.White else DarkGreen
                val statusBg = if (daysAhead > 0 && !isPastDeparture) Color(0xFFF0F4FA) else if (isPastDeparture) Terracotta else Color(0xFFF3FAF7)

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (daysAhead > 0) "Próxima clase ($departureDayName):" else "Siguiente clase de hoy:","""
content = content.replace(old_ui, new_ui)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/SmartRouteReminderCard.kt", "w") as f:
    f.write(content)

