import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

old_body = """    var isTimerActive by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableStateOf(1500) } // 25 minutes
    
    LaunchedEffect(isTimerActive) {
        if (isTimerActive) {
            while (secondsLeft > 0) {
                kotlinx.coroutines.delay(1000L)
                secondsLeft--
            }
            isTimerActive = false
        }
    }"""

new_body = """    val isTimerActive by viewModel.isPomodoroActive.collectAsStateWithLifecycle()
    val secondsLeft by viewModel.pomodoroSecondsLeft.collectAsStateWithLifecycle()"""

content = content.replace(old_body, new_body)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
