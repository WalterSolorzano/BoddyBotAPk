import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

pattern = re.compile(r'    fun toggleTask\(task: Task\) \{.*?\}', re.DOTALL)

def replacer(match):
    return """    private val _buddyCelebrationEvent = kotlinx.coroutines.flow.MutableSharedFlow<Unit>()
    val buddyCelebrationEvent = kotlinx.coroutines.flow.asSharedFlow(_buddyCelebrationEvent)

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            val newIsCompleted = !task.isCompleted
            repository.updateTask(task.copy(isCompleted = newIsCompleted))
            if (newIsCompleted) {
                _buddyCelebrationEvent.emit(Unit)
            }
            notifyWidgets()
        }
    }"""

content, count = re.subn(r'    fun toggleTask\(task: Task\) \{.*?    \}', replacer, content, flags=re.DOTALL)
print(f"Replaced {count} times")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
