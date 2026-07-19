with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/PensumProgressScreen.kt", "r") as f:
    content = f.read()

content = content.replace("androidx.compose.material.icons.Icons.Filled.ArrowBack", "Icons.Filled.ArrowBack")
content = content.replace("import androidx.compose.material3.*", "import androidx.compose.material3.*\nimport androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.filled.ArrowBack")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/PensumProgressScreen.kt", "w") as f:
    f.write(content)
