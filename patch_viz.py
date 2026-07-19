import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/Visualizers.kt", "r") as f:
    content = f.read()

imports = """import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.scale"""

if "drawscope.withTransform" not in content:
    content = content.replace("import androidx.compose.ui.graphics.drawscope.Stroke", "import androidx.compose.ui.graphics.drawscope.Stroke\n" + imports)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/Visualizers.kt", "w") as f:
    f.write(content)
