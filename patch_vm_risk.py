import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

old_code = """        // 1. Tienes Examen Hoy
        val allAssessments = repository.assessments.first()"""

new_code = """        // 0. Anticipated Risk Notification (A una falta del riesgo)
        for (sub in currentSubjects) {
            val allAbs = repository.attendanceLogs.first().filter { it.subjectId == sub.id && !it.isPresent }
            val maxAbs = sub.totalClasses - kotlin.math.ceil(sub.totalClasses * (sub.requiredAttendancePercent / 100.0)).toInt()
            val remaining = (maxAbs - allAbs.size).coerceAtLeast(0)
            if (remaining == 3) {
                val riskNotifKey = "risk_${sub.id}_${remaining}"
                if (!sentNotifications.contains(riskNotifKey)) {
                    sentNotifications.add(riskNotifKey)
                    NotificationHelper.sendNotification(
                        getApplication(),
                        "⚠️ Riesgo Anticipado",
                        "Estás a una falta de entrar en zona de riesgo en ${sub.name}. ¡No faltes!"
                    )
                }
            }
        }

        // 1. Tienes Examen Hoy
        val allAssessments = repository.assessments.first()"""

content = content.replace(old_code, new_code)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
