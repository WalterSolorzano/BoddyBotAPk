import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

old_func = """    private fun setWeeklyStreak(newValue: Int) {
        viewModelScope.launch {
            _weeklyStreak.value = newValue.coerceAtLeast(0)
            repository.saveSetting("weekly_streak_count", _weeklyStreak.value.toString())
        }
    }"""

new_func = """    private fun setWeeklyStreak(newValue: Int) {
        viewModelScope.launch {
            _weeklyStreak.value = newValue.coerceAtLeast(0)
            repository.saveSetting("weekly_streak_count", _weeklyStreak.value.toString())
            
            if (_weeklyStreak.value >= 5) {
                unlockBadge("Estudiante Responsable")
            }
        }
    }"""

content = content.replace(old_func, new_func)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
