import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

old_task = "    fun toggleTask(task: Task) {\n        viewModelScope.launch {\n            repository.updateTask(task.copy(isCompleted = !task.isCompleted))\n            notifyWidgets()                notifyWidgets()}\n    }"

new_task = """    private val _buddyCelebrationEvent = kotlinx.coroutines.flow.MutableSharedFlow<Unit>()
    val buddyCelebrationEvent: kotlinx.coroutines.flow.SharedFlow<Unit> = kotlinx.coroutines.flow.asSharedFlow(_buddyCelebrationEvent)

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

if old_task in content:
    content = content.replace(old_task, new_task)
    print("Replaced!")
else:
    print("WARNING: old_task not found still")
    
with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
