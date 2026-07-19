import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

content = content.replace("val buddyCelebrationEvent = _buddyCelebrationEvent.asSharedFlow()", "val buddyCelebrationEvent: kotlinx.coroutines.flow.SharedFlow<Unit> = _buddyCelebrationEvent.asSharedFlow()")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
