import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/SmartRouteReminderCard.kt", "r") as f:
    content = f.read()

# I will replace it using regex to be safe
start_idx = content.find("val nextClassInfo = remember(subjects, currentDayCode, nowTotalMins) {")
end_idx = content.find("val parsedTime = remember(firstClassTime) {")

if start_idx != -1 and end_idx != -1:
    new_block = """val nextClassInfo = remember(subjects, currentDayCode, nowTotalMins) {
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
                                    return@mapNotNull Triple(sub, session.time, i)
                                }
                            } else {
                                return@mapNotNull Triple(sub, session.time, i)
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
    val isTomorrow = daysAhead == 1

    """
    content = content[:start_idx] + new_block + content[end_idx:]

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/SmartRouteReminderCard.kt", "w") as f:
    f.write(content)
