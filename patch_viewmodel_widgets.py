import re
with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

update_func = """
    private fun updateWidgets() {
        viewModelScope.launch {
            try {
                val context = getApplication<android.app.Application>().applicationContext
                com.aistudio.unibuddy.qywvsp.ui.widget.TamagotchiWidget().updateAll(context)
                com.aistudio.unibuddy.qywvsp.ui.widget.AcademicWeatherWidget().updateAll(context)
                com.aistudio.unibuddy.qywvsp.ui.widget.GradesWidget().updateAll(context)
            } catch(e: Exception) {
                e.printStackTrace()
            }
        }
    }
"""
# insert before the last brace of UniBuddyViewModel
content = content.replace("class UniBuddyViewModelFactory", update_func + "\nclass UniBuddyViewModelFactory")

# Add call to updateWidgets() in these functions:
# addAbsence, removeAbsence, addAssessment, updateAssessment, deleteAssessment, toggleTask, markTaskComplete, deleteSubject, saveSubject, updateSubject
funcs = ["addAbsence", "removeAbsence", "addAssessment", "updateAssessment", "deleteAssessment", "toggleTask", "markTaskComplete", "deleteSubject", "saveSubject", "updateSubject", "addBuddyXp"]
for func in funcs:
    # simple replace to inject at the end of the viewModelScope.launch inside each function
    pass # this might be too complex for simple regex. We can just add it to refreshData()!

content = content.replace("private fun refreshData() {", "private fun refreshData() {\n        updateWidgets()")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
