import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

old_task = """    fun toggleTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
            notifyWidgets()                notifyWidgets()}
    }"""

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
else:
    print("WARNING: old_task not found")
    
with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
