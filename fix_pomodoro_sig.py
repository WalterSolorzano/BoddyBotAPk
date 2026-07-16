with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

content = content.replace("fun InteractivePomodoroWidget(viewModel) {", "fun InteractivePomodoroWidget(viewModel: com.aistudio.unibuddy.qywvsp.ui.UniBuddyViewModel) {")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
