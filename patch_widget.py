import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/widget/TamagotchiWidget.kt", "r") as f:
    content = f.read()

old_statusMsg = """        val statusMsg = when {
            attendanceRate >= 90.0 -> "Buddy: ¡Súper Feliz!"
            attendanceRate >= 80.0 -> "Buddy: Todo en orden"
            attendanceRate >= 70.0 -> "Buddy: Algo cansado"
            attendanceRate >= 50.0 -> "Buddy: ¡Enfermo!"
            else -> "Buddy: ¡Grave! Ve a clase"
        }"""

new_statusMsg = """        val statusMsg = when {
            attendanceRate >= 90.0 -> "Buddy: ¡Súper Feliz!"
            attendanceRate >= 80.0 -> "Buddy: Todo en orden"
            attendanceRate >= 70.0 -> "Buddy: Algo cansado"
            attendanceRate >= 50.0 -> "Buddy: ¡Enfermo!"
            else -> "Buddy: ¡Grave! Ve a clase"
        }

        val calendar = Calendar.getInstance()
        val currentDayCode = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Lu"
            Calendar.TUESDAY -> "Ma"
            Calendar.WEDNESDAY -> "Mi"
            Calendar.THURSDAY -> "Ju"
            Calendar.FRIDAY -> "Vi"
            Calendar.SATURDAY -> "Sá"
            else -> "Do"
        }
        val examTomorrow = assessments.firstOrNull { it.grade == null && it.examDate.trim().equals(currentDayCode, ignoreCase = true) }
        
        val hasExam = examTomorrow != null
        val hasRisk = absences.size >= 3
        
        val criticalEvent = if (hasExam && hasRisk) {
            if (calendar.get(Calendar.MINUTE) % 2 == 0) "¡EXAMEN MAÑANA!" else "¡RIESGO DE FALTA!"
        } else if (hasExam) {
            "¡EXAMEN MAÑANA!"
        } else if (hasRisk) {
            "¡RIESGO DE FALTA!"
        } else {
            "Todo bajo control"
        }"""

content = content.replace(old_statusMsg, new_statusMsg)

old_ui = """                    Text(
                        text = statusMsg,
                        style = TextStyle(
                            color = ColorProvider(textWhite),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )"""

new_ui = """                    Text(
                        text = statusMsg,
                        style = TextStyle(
                            color = ColorProvider(textWhite),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = criticalEvent,
                        style = TextStyle(
                            color = ColorProvider(if (criticalEvent == "Todo bajo control") textColorSecondary else Color(0xFFFF5252)),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )"""

content = content.replace(old_ui, new_ui)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/widget/TamagotchiWidget.kt", "w") as f:
    f.write(content)
