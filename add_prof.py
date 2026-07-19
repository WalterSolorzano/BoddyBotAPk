import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

new_method = """    fun addDummyProfessor() {
        viewModelScope.launch {
            repository.insertProfessor(com.aistudio.unibuddy.qywvsp.data.Professor(name = "Nuevo Profesor", notes = "Añadido manualmente", rating = 4.0f))
        }
    }
"""

content = content.replace("fun generateDummyData() {", new_method + "\n    fun generateDummyData() {")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/PensumProfessorsTab.kt", "r") as f:
    content2 = f.read()

content2 = content2.replace("""                Text(if (sortByRating) "Por Valoración" else "Alfabético")
            }
        }""", """                Text(if (sortByRating) "Por Valoración" else "Alfabético")
            }
            IconButton(onClick = { viewModel.addDummyProfessor() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Professor")
            }
        }""")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/PensumProfessorsTab.kt", "w") as f:
    f.write(content2)

