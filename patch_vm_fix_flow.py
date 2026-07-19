import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

content = content.replace("val buddyCelebrationEvent = kotlinx.coroutines.flow.asSharedFlow(_buddyCelebrationEvent)", "val buddyCelebrationEvent = _buddyCelebrationEvent.asSharedFlow()")
content = content.replace("val buddyCelebrationEvent: kotlinx.coroutines.flow.SharedFlow<Unit> = kotlinx.coroutines.flow.asSharedFlow(_buddyCelebrationEvent)", "val buddyCelebrationEvent = _buddyCelebrationEvent.asSharedFlow()")

if "import kotlinx.coroutines.flow.asSharedFlow" not in content:
    content = content.replace("import kotlinx.coroutines.flow.MutableSharedFlow", "import kotlinx.coroutines.flow.MutableSharedFlow\nimport kotlinx.coroutines.flow.asSharedFlow")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
