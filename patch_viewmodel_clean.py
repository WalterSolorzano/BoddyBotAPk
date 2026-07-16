with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

content = content.replace("updateWidgets()", "notifyWidgets()")

# Let's remove the extra private fun notifyWidgets() definition we accidentally added if any. 
# Wait, I added `private fun updateWidgets() { ... }`.
# So let's just replace the whole block with empty string.
import re
content = re.sub(r"private fun notifyWidgets\(\)\s*\{\s*viewModelScope\.launch\s*\{\s*try\s*\{\s*val context.*?catch\(e: Exception\)\s*\{\s*e\.printStackTrace\(\)\s*\}\s*\}\s*\}", "", content, flags=re.DOTALL)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
