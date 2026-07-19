import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/widget/TamagotchiWidget.kt", "r") as f:
    content = f.read()

import_code = "import java.util.Calendar\n"
if "import java.util.Calendar" not in content:
    content = content.replace("import java.io.File\n", "import java.io.File\nimport java.util.Calendar\n")

old_db = """        val database = AppDatabase.getDatabase(context)
        val logs = database.attendanceLogDao().getAllLogs().first()
        val absences = database.absenceDao().getAllAbsences().first()"""

new_db = """        val database = AppDatabase.getDatabase(context)
        val logs = database.attendanceLogDao().getAllLogs().first()
        val absences = database.absenceDao().getAllAbsences().first()
        val assessments = database.assessmentDao().getAllAssessments().first()"""

content = content.replace(old_db, new_db)

old_statusMsg = """        val statusMsg = when {
            attendanceRate >= 90.0 -> "Buddy: ¡Súper Feliz!"
            attendanceRate >= 80.0 -> "Buddy: Todo en orden"
            attendanceRate >= 70.0 -> "Buddy: Algo cansado"
            attendanceRate >= 50.0 -> "Buddy: ¡Enfermo!"
            else -> "Buddy: ¡Crítico!"
        }"""

new_statusMsg = """        val statusMsg = when {
            attendanceRate >= 90.0 -> "Buddy: ¡Súper Feliz!"
            attendanceRate >= 80.0 -> "Buddy: Todo en orden"
            attendanceRate >= 70.0 -> "Buddy: Algo cansado"
            attendanceRate >= 50.0 -> "Buddy: ¡Enfermo!"
            else -> "Buddy: ¡Crítico!"
        }

        // Determine critical milestone
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
        val criticalEvent = if (examTomorrow != null) {
            "¡EXAMEN MAÑANA!"
        } else if (absences.size >= 3) {
            "¡RIESGO DE FALTA!"
        } else {
            "Todo bajo control"
        }"""

content = content.replace(old_statusMsg, new_statusMsg)

old_ui = """                        Text(
                            text = statusMsg,
                            style = TextStyle(color = ColorProvider(themeTextColor), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        )
                    }
                    
                    Spacer(modifier = GlanceModifier.height(12.dp))
                    
                    // Stats Box
                    Box("""

new_ui = """                        Text(
                            text = statusMsg,
                            style = TextStyle(color = ColorProvider(themeTextColor), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        )
                        
                        Text(
                            text = criticalEvent,
                            style = TextStyle(color = ColorProvider(if (criticalEvent == "Todo bajo control") Color.Gray else Color.Red), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                    
                    Spacer(modifier = GlanceModifier.height(12.dp))
                    
                    // Stats Box
                    Box("""

content = content.replace(old_ui, new_ui)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/widget/TamagotchiWidget.kt", "w") as f:
    f.write(content)
