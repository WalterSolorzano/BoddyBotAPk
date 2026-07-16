import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

import_code = "import androidx.compose.ui.text.font.FontWeight\n"
new_import = import_code + "import androidx.compose.ui.text.style.TextOverflow\n"
if "import androidx.compose.ui.text.style.TextOverflow" not in content:
    content = content.replace(import_code, new_import)

content = content.replace("Icons.Default.SystemUpdate", "Icons.Default.Info")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
