import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

content = content.replace("private val _weeklyStreak = MutableStateFlow(12)", "private val _weeklyStreak = MutableStateFlow(0)")

load_streak = """                repository.getSetting("google_maps_api_key")?.let { _googleMapsApiKey.value = it }"""
new_load_streak = """                repository.getSetting("google_maps_api_key")?.let { _googleMapsApiKey.value = it }
                repository.getSetting("weekly_streak_count")?.let { _weeklyStreak.value = it.toIntOrNull() ?: 0 }"""
content = content.replace(load_streak, new_load_streak)

new_func = """    private fun setWeeklyStreak(newValue: Int) {
        viewModelScope.launch {
            _weeklyStreak.value = newValue.coerceAtLeast(0)
            repository.saveSetting("weekly_streak_count", _weeklyStreak.value.toString())
        }
    }
"""
content = content.replace("    private val _weeklyStreak = MutableStateFlow(0)", new_func + "    private val _weeklyStreak = MutableStateFlow(0)")

content = content.replace("_weeklyStreak.value = _weeklyStreak.value + 1", "setWeeklyStreak(_weeklyStreak.value + 1)")
content = content.replace("_weeklyStreak.value = _weeklyStreak.value - 1", "setWeeklyStreak(_weeklyStreak.value - 1)")
content = content.replace("_weeklyStreak.value = (_weeklyStreak.value - 1).coerceAtLeast(0)", "setWeeklyStreak(_weeklyStreak.value - 1)")
content = content.replace("_weeklyStreak.value = 0", "setWeeklyStreak(0)")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
