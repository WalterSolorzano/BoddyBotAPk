with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyNavGraph.kt", "r") as f:
    content = f.read()

content = content.replace("composable(\"focus\") {\n            FocusModeScreen(viewModel)\n        }", "composable(\"focus\") {\n            FocusModeScreen(viewModel)\n        }\n\n        composable(\"pensum\") {\n            PensumProgressScreen(viewModel = viewModel, onBack = { navController.popBackStack() })\n        }\n\n        composable(\"stats\") {\n            SemesterHistoryView(viewModel = viewModel, onBack = { navController.popBackStack() })\n        }")
content = content.replace("import com.aistudio.unibuddy.qywvsp.ui.screens.DashboardScreen", "import com.aistudio.unibuddy.qywvsp.ui.screens.DashboardScreen\nimport com.aistudio.unibuddy.qywvsp.ui.screens.PensumProgressScreen")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyNavGraph.kt", "w") as f:
    f.write(content)
