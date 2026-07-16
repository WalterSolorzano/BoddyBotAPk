with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

pomodoro_state = """    // Pomodoro State
    private val _pomodoroSecondsLeft = MutableStateFlow(1500)
    val pomodoroSecondsLeft: StateFlow<Int> = _pomodoroSecondsLeft.asStateFlow()

    private val _isPomodoroActive = MutableStateFlow(false)
    val isPomodoroActive: StateFlow<Boolean> = _isPomodoroActive.asStateFlow()

    private var pomodoroJob: kotlinx.coroutines.Job? = null

    fun togglePomodoro() {
        if (_isPomodoroActive.value) {
            _isPomodoroActive.value = false
            pomodoroJob?.cancel()
        } else {
            if (_pomodoroSecondsLeft.value <= 0) _pomodoroSecondsLeft.value = 1500
            _isPomodoroActive.value = true
            pomodoroJob = viewModelScope.launch {
                while (_pomodoroSecondsLeft.value > 0 && _isPomodoroActive.value) {
                    kotlinx.coroutines.delay(1000L)
                    _pomodoroSecondsLeft.value -= 1
                }
                _isPomodoroActive.value = false
            }
        }
    }

    fun resetPomodoro() {
        _isPomodoroActive.value = false
        pomodoroJob?.cancel()
        _pomodoroSecondsLeft.value = 1500
    }
"""

content = content.replace("private val _isTripActive = MutableStateFlow(false)", pomodoro_state + "\n    private val _isTripActive = MutableStateFlow(false)")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
