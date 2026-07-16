import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

old_code = """                        val travelMins = _locationBasedTravelMinutes.value ?: _baseTravelTime.value
                        val destName = _destination.value

                        // Reminder 1: "Tiempo Antes" (Alert 15 to 45 mins before class start)
                        if (diffMinutes in 15..45) {
                            val beforeNotifKey = "${sub.id}_before_${todayDateKey}"
                            if (!sentNotifications.contains(beforeNotifKey)) {
                                sentNotifications.add(beforeNotifKey)
                                NotificationHelper.sendNextClassNotification(
                                    getApplication(),
                                    sub.id,
                                    destName,
                                    "Prepárate: Clase de ${sub.name}",
                                    "Empieza en $diffMinutes min (a las ${session.time}). Tiempo estimado de viaje: $travelMins min."
                                )
                            }
                        }

                        // Reminder 2: "A la Hora de Entrada" (Alert from -15 mins up to class start time)
                        if (diffMinutes in -15..1) {
                            val atNotifKey = "${sub.id}_at_${todayDateKey}"
                            if (!sentNotifications.contains(atNotifKey)) {
                                // Double check if user has already checked in for today
                                val existingLogs = repository.attendanceLogs.first().filter { 
                                    it.subjectId == sub.id && it.date.startsWith(todayStrShort) 
                                }
                                if (existingLogs.isEmpty()) {
                                    sentNotifications.add(atNotifKey)
                                    NotificationHelper.sendNextClassNotification(
                                        getApplication(),
                                        sub.id,
                                        destName,
                                        "¡Hora de Entrada a ${sub.name}!",
                                        "Tu clase ya está iniciando (hora: ${session.time}). Toca aquí para registrar asistencia en UniBuddy."
                                    )
                                }
                            }
                        }"""

new_code = """                        val travelMins = _locationBasedTravelMinutes.value ?: _baseTravelTime.value
                        val destName = _destination.value
                        
                        val allAbsences = repository.attendanceLogs.first().filter { it.subjectId == sub.id && !it.isPresent }
                        val maxAbs = sub.totalClasses - kotlin.math.ceil(sub.totalClasses * (sub.requiredAttendancePercent / 100.0)).toInt()
                        val remainingAbsences = (maxAbs - allAbsences.size).coerceAtLeast(0)
                        val isAbsencesCritical = remainingAbsences <= 2

                        // Reminder 1: "Tiempo Antes" (Alert 15 to 45 mins before class start)
                        if (diffMinutes in 15..45) {
                            val beforeNotifKey = "${sub.id}_before_${todayDateKey}"
                            if (!sentNotifications.contains(beforeNotifKey)) {
                                sentNotifications.add(beforeNotifKey)
                                NotificationHelper.sendNextClassNotification(
                                    getApplication(),
                                    sub.id,
                                    destName,
                                    if (isAbsencesCritical) "¡URGENTE! Clase de ${sub.name}" else "Prepárate: Clase de ${sub.name}",
                                    if (isAbsencesCritical) "Empieza en $diffMinutes min. ¡Casi repruebas por faltas, no faltes!" else "Empieza en $diffMinutes min (a las ${session.time}). Tiempo estimado de viaje: $travelMins min.",
                                    isCritical = isAbsencesCritical
                                )
                            }
                        }

                        // Reminder 2: "A la Hora de Entrada" (Alert from -15 mins up to class start time)
                        if (diffMinutes in -15..1) {
                            val atNotifKey = "${sub.id}_at_${todayDateKey}"
                            if (!sentNotifications.contains(atNotifKey)) {
                                // Double check if user has already checked in for today
                                val existingLogs = repository.attendanceLogs.first().filter { 
                                    it.subjectId == sub.id && it.date.startsWith(todayStrShort) 
                                }
                                if (existingLogs.isEmpty()) {
                                    sentNotifications.add(atNotifKey)
                                    NotificationHelper.sendNextClassNotification(
                                        getApplication(),
                                        sub.id,
                                        destName,
                                        if (isAbsencesCritical) "¡ASISTE AHORA! ${sub.name}" else "¡Hora de Entrada a ${sub.name}!",
                                        if (isAbsencesCritical) "¡Últimas faltas! Registra asistencia." else "Tu clase ya está iniciando (hora: ${session.time}). Toca aquí para registrar asistencia.",
                                        isCritical = isAbsencesCritical
                                    )
                                }
                            }
                        }"""

content = content.replace(old_code, new_code)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
