import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

# Add notifyWidgets() method at line 105 (or right before checkAndTriggerProactiveNotifications)
notify_method = """
    private fun notifyWidgets() {
        com.aistudio.unibuddy.qywvsp.ui.widget.WidgetUpdater.updateAllWidgets(getApplication())
    }

"""

content = content.replace("    private suspend fun checkAndTriggerProactiveNotifications", notify_method + "    private suspend fun checkAndTriggerProactiveNotifications")

# Now inject notifyWidgets() at the end of the viewModelScope.launch blocks of modifying functions
funcs = [
    "fun addSubject(",
    "fun updateSubject(",
    "fun deleteSubject(",
    "fun registerAbsence(",
    "fun deleteAbsence(",
    "fun addAssessment(",
    "fun deleteAssessment(",
    "fun addTask(",
    "fun toggleTask(",
    "fun deleteTask(",
    "fun registerAttendanceLog(",
    "fun undoLastAttendanceLog(",
    "fun deleteAttendanceLog("
]

for func in funcs:
    # Find the start of the function
    idx = content.find(func)
    if idx == -1:
        print(f"NOT FOUND: {func}")
        continue
    
    # Find viewModelScope.launch { inside the function
    launch_idx = content.find("viewModelScope.launch {", idx)
    if launch_idx == -1:
        launch_idx = content.find("viewModelScope.launch(Dispatchers.IO) {", idx)
    if launch_idx == -1:
        print(f"LAUNCH NOT FOUND: {func}")
        continue
        
    # Find the closing brace of the launch block
    # We need a proper brace matching algorithm
    brace_count = 0
    i = launch_idx + content[launch_idx:].find("{")
    while i < len(content):
        if content[i] == '{':
            brace_count += 1
        elif content[i] == '}':
            brace_count -= 1
            if brace_count == 0:
                # We found the end of the launch block. Inject notifyWidgets() right before it.
                # Find the last newline before the brace to indent correctly
                last_newline = content.rfind("\n", 0, i)
                indent = content[last_newline+1:i]
                
                content = content[:i] + indent + "notifyWidgets()\n" + content[i:]
                break
        i += 1

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
print("Patcher script finished")
