with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()
    
# Remove the bad appended text
content = content.replace("    fun addDummyProfessor() {\n        viewModelScope.launch {\n            repository.insertProfessor(com.aistudio.unibuddy.qywvsp.data.Professor(name = \"Dr. Nuevo Profesor\"))\n        }\n    }\n}", "")

# Add it inside the viewmodel
content = content.replace("    // Helper to calculate next class", "    fun addDummyProfessor() {\n        viewModelScope.launch {\n            repository.insertProfessor(com.aistudio.unibuddy.qywvsp.data.Professor(name = \"Dr. Nuevo Profesor\"))\n        }\n    }\n\n    // Helper to calculate next class")
with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
