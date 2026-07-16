import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

import_code = "import com.aistudio.unibuddy.qywvsp.data.*\n"
new_import = import_code + "import com.aistudio.unibuddy.qywvsp.ui.UpdateManager\n"
if "import com.aistudio.unibuddy.qywvsp.ui.UpdateManager" not in content:
    content = content.replace(import_code, new_import)

prop_code = """    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()"""

new_prop = """    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    private val _updateInfo = MutableStateFlow<UpdateManager.UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateManager.UpdateInfo?> = _updateInfo.asStateFlow()"""

if "val updateInfo:" not in content:
    content = content.replace(prop_code, new_prop)

init_code = """    init {
        val db = AppDatabase.getDatabase(application)"""

new_init = """    init {
        checkForUpdates()
        val db = AppDatabase.getDatabase(application)"""

if "checkForUpdates()" not in content:
    content = content.replace(init_code, new_init)

fun_code = """    private fun notifyWidgets() {"""

new_fun = """    fun checkForUpdates() {
        viewModelScope.launch {
            val info = UpdateManager.checkForUpdates()
            if (info != null) {
                _updateInfo.value = info
                // Aplicar configuraciones dinámicas si hay alguna (Opción 2)
                info.dynamicConfig?.forEach { (key, value) ->
                    repository.saveSetting(key, value)
                }
            }
        }
    }

    private fun notifyWidgets() {"""

if "fun checkForUpdates()" not in content:
    content = content.replace(fun_code, new_fun)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
