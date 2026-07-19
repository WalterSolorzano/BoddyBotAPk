import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

old_code = """        } else if (activeTab == "estudios") {
            // Nightly Summary"""

new_code = """        } else if (activeTab == "estudios") {
            if (subjects.isNotEmpty()) {
                PredictiveGradeCalculatorWidget(subjects, viewModel)
            }
            // Nightly Summary"""

content = content.replace(old_code, new_code)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
