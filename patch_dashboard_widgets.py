import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

# Replace Pomodoro Call
content = content.replace("InteractivePomodoroWidget()", "InteractivePomodoroWidget(viewModel)")

# Replace Pomodoro Signature
old_pomodoro = """@Composable
fun InteractivePomodoroWidget() {
    var isTimerActive by remember { mutableStateOf(false) }
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
new_pomodoro = """@Composable
fun InteractivePomodoroWidget(viewModel: com.aistudio.unibuddy.qywvsp.ui.UniBuddyViewModel) {
    val isTimerActive by viewModel.isPomodoroActive.collectAsStateWithLifecycle()
    val secondsLeft by viewModel.pomodoroSecondsLeft.collectAsStateWithLifecycle()"""
content = content.replace(old_pomodoro, new_pomodoro)

# Replace Pomodoro Buttons
content = content.replace("onClick = { isTimerActive = !isTimerActive }", "onClick = { viewModel.togglePomodoro() }")
content = content.replace("onClick = {\n                            isTimerActive = false\n                            secondsLeft = 1500\n                        }", "onClick = { viewModel.resetPomodoro() }")
content = content.replace("onClick = { isTimerActive = false\nsecondsLeft = 1500 }", "onClick = { viewModel.resetPomodoro() }")

# Replace fake stress
old_wellness = """            WellnessWidget(
                upcomingExamsCount = assessments.count { it.grade == null },
                absencesCount = absences.size,
                calculatedStress = 50f,
                statusText = "Estable"
            )"""

new_wellness = """            val calculatedStress = (assessments.count { it.grade == null } * 15f + absences.size * 5f).coerceIn(0f, 100f)
            val stressStatusText = when {
                calculatedStress > 70f -> "Crítico"
                calculatedStress > 40f -> "Elevado"
                else -> "Estable"
            }
            WellnessWidget(
                upcomingExamsCount = assessments.count { it.grade == null },
                absencesCount = absences.size,
                calculatedStress = calculatedStress,
                statusText = stressStatusText
            )"""
content = content.replace(old_wellness, new_wellness)

# Replace fake average
old_grades = """            GradesHistoryWidget(
                assessments = assessments.filter { it.grade != null }.takeLast(5),
                currentWeighted = 0.0
            )"""

new_grades = """            val currentWeighted = assessments.filter { it.grade != null }.sumOf { (it.grade!! / 100.0) * it.percentage }
            GradesHistoryWidget(
                assessments = assessments.filter { it.grade != null }.takeLast(5),
                currentWeighted = currentWeighted
            )"""
content = content.replace(old_grades, new_grades)

# Fix Hex Colors in DashboardScreen.kt
# Color(0xFFEFF6FF) -> BackgroundGray or ProBlue.copy(alpha=0.05f)
# Color(0xFFFFF7ED) -> Amber.copy(alpha=0.05f)
# Color(0xFFF97316) -> Amber
# Color(0xFFFFF3E0) -> Amber.copy(alpha=0.1f)
# Color(0xFF1E1E2E) -> NavyBlue
# Color(0xFFFFD700) -> Amber
# Color(0xFFFF8A80) -> Terracotta
# Color(0xFFE2E8F0) -> SlateGray.copy(alpha = 0.5f) -> Actually Bone

content = content.replace("Color(0xFFEFF6FF)", "ProBlue.copy(alpha=0.05f)")
content = content.replace("Color(0xFFFFF7ED)", "Amber.copy(alpha=0.05f)")
content = content.replace("Color(0xFFF97316)", "Amber")
content = content.replace("Color(0xFFFFF3E0)", "Amber.copy(alpha=0.1f)")
content = content.replace("Color(0xFF1E1E2E)", "NavyBlue")
content = content.replace("Color(0xFFFFD700)", "Amber")
content = content.replace("Color(0xFFFF8A80)", "Terracotta")
content = content.replace("Color(0xFFE2E8F0)", "Bone")
content = content.replace("Color(0xFFC2410C)", "Amber")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
