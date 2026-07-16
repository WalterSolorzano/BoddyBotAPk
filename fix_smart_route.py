import re
with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/SmartRouteReminderCard.kt", "r") as f:
    content = f.read()

# Replace isPastDeparture block
dep_block_start = content.find("val isPastDeparture = remember(parsedTime, baseTravelTime")
dep_block_end = content.find("Card(", dep_block_start)

if dep_block_start != -1 and dep_block_end != -1:
    old_dep = content[dep_block_start:dep_block_end]
    new_dep = """val isPastDeparture = remember(parsedTime, baseTravelTime, daysAhead) {
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
            "el " + format.format(cal.time).replaceFirstChar { it.uppercase() }
        }
    }

    """
    content = content[:dep_block_start] + new_dep + content[dep_block_end:]
else:
    print("Could not find isPastDeparture block")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/SmartRouteReminderCard.kt", "w") as f:
    f.write(content)
