with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

dummy_method = """
    fun addDummyProfessor() {
        viewModelScope.launch {
            repository.insertProfessor(com.aistudio.unibuddy.qywvsp.data.Professor(name = "Dr. Nuevo Profesor"))
        }
    }
"""

# Insert before the last brace. Wait, the last brace might be of the Factory.
# Let's just find "fun addProfessor" or something similar.
