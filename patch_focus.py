import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/FocusModeScreen.kt", "r") as f:
    content = f.read()

old_model = "data class FocusSessionRecord(val date: String, val duration: Int, val label: String)"
new_model = "data class FocusSessionRecord(val date: String, val duration: Int, val label: String, val timeOfDay: String = \"\", val interrupted: Boolean = false)"
content = content.replace(old_model, new_model)

old_parse = """                FocusSessionRecord(
                    date = obj.getString("date"),
                    duration = obj.getInt("duration"),
                    label = obj.optString("label", "Estudio")
                )"""
new_parse = """                FocusSessionRecord(
                    date = obj.getString("date"),
                    duration = obj.getInt("duration"),
                    label = obj.optString("label", "Estudio"),
                    timeOfDay = obj.optString("timeOfDay", ""),
                    interrupted = obj.optBoolean("interrupted", false)
                )"""
content = content.replace(old_parse, new_parse)

old_serialize = """    for (s in this) {
        val obj = JSONObject()
        obj.put("date", s.date)
        obj.put("duration", s.duration)
        obj.put("label", s.label)
        arr.put(obj)
    }"""
new_serialize = """    for (s in this) {
        val obj = JSONObject()
        obj.put("date", s.date)
        obj.put("duration", s.duration)
        obj.put("label", s.label)
        obj.put("timeOfDay", s.timeOfDay)
        obj.put("interrupted", s.interrupted)
        arr.put(obj)
    }"""
content = content.replace(old_serialize, new_serialize)

old_finish = """                val updatedHistory = sessionsHistory + FocusSessionRecord(
                    date = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date()),
                    duration = if (isWorkMode) workMinutes else breakMinutes,
                    label = if (isWorkMode) "Estudio" else "Descanso"
                )
                viewModel.saveFocusSessionsHistory(updatedHistory.toSessionsJsonString())"""

new_finish = """                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                val updatedHistory = sessionsHistory + FocusSessionRecord(
                    date = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date()),
                    duration = if (isWorkMode) workMinutes else breakMinutes,
                    label = if (isWorkMode) "Estudio" else "Descanso",
                    timeOfDay = timeFormat,
                    interrupted = false
                )
                viewModel.saveFocusSessionsHistory(updatedHistory.toSessionsJsonString())"""
content = content.replace(old_finish, new_finish)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/FocusModeScreen.kt", "w") as f:
    f.write(content)
