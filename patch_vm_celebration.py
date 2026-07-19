import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

old_task = """    fun toggleTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
            notifyWidgets()                notifyWidgets()}
    }"""

new_task = """    private val _buddyCelebrationEvent = MutableSharedFlow<Unit>()
    val buddyCelebrationEvent: SharedFlow<Unit> = _buddyCelebrationEvent.asSharedFlow()

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

content = content.replace(old_task, new_task)

old_attendance = """            val insertedId = repository.insertAttendanceLog(AttendanceLog(subjectId = subjectId, date = date, isPresent = isPresent))
            lastInsertedLogId = insertedId
            
            _snackbarEvent.emit("Asistencia registrada")"""

new_attendance = """            val insertedId = repository.insertAttendanceLog(AttendanceLog(subjectId = subjectId, date = date, isPresent = isPresent))
            lastInsertedLogId = insertedId
            
            _snackbarEvent.emit("Asistencia registrada")
            if (isPresent) {
                _buddyCelebrationEvent.emit(Unit)
            }"""

content = content.replace(old_attendance, new_attendance)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
