import re
with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

content = content.replace("androidx.compose.material.icons.Icons.Default.Upload", "androidx.compose.material.icons.Icons.Default.Share")
content = content.replace("androidx.compose.material.icons.Icons.Default.Download", "androidx.compose.material.icons.Icons.Default.Refresh")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)

