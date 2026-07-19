import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

content = content.replace("val tasks: StateFlow<List<Task>> = repository.tasks\n        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())", "val tasks: StateFlow<List<Task>> = repository.tasks\n        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())\n\n    val seasonRecaps: StateFlow<List<com.aistudio.unibuddy.qywvsp.data.SeasonRecap>> = repository.seasonRecaps\n        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())\n\n    private val _showSeasonRecap = kotlinx.coroutines.flow.MutableStateFlow<com.aistudio.unibuddy.qywvsp.data.SeasonRecap?>(null)\n    val showSeasonRecap: kotlinx.coroutines.flow.StateFlow<com.aistudio.unibuddy.qywvsp.data.SeasonRecap?> = _showSeasonRecap.asStateFlow()\n    fun dismissSeasonRecap() { _showSeasonRecap.value = null }")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
