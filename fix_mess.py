with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyApp.kt", "r") as f:
    content = f.read()

# Replace all occurrences of "}\nimport androidx.compose.foundation.layout.*" with "}"
content = content.replace("}\nimport androidx.compose.foundation.layout.*", "}")
# Replace all occurrences of "import androidx.compose.foundation.layout.*," with "" (if any)
content = content.replace("import androidx.compose.foundation.layout.*,", "")
content = content.replace("import androidx.compose.foundation.layout.*", "")

# Add it back to the imports at the top
import_str = "import androidx.compose.foundation.layout.*"
content = content.replace("import androidx.compose.ui.Modifier", "import androidx.compose.ui.Modifier\n" + import_str)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyApp.kt", "w") as f:
    f.write(content)
