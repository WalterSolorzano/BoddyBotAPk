import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

# Let's truncate everything from class UniBuddyViewModelFactory to EOF and append it cleanly
match = re.search(r'class UniBuddyViewModelFactory', content)
if match:
    content = content[:match.start()]
    
    # Let's ensure the UniBuddyViewModel is closed
    content = content.rstrip()
    if not content.endswith('}'):
        content += "\n}\n"
        
    factory_and_stuff = """
class UniBuddyViewModelFactory(private val application: android.app.Application) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UniBuddyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UniBuddyViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
"""
    content += factory_and_stuff
    
    with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
        f.write(content)

