import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

import_code = "import com.aistudio.unibuddy.qywvsp.data.IntegratedLogItem\n"
new_import = import_code + "import com.aistudio.unibuddy.qywvsp.ui.UpdateManager\n"
content = content.replace(import_code, new_import)

prop_code = """    private val _themeColor = MutableStateFlow(ProBlue)
    val themeColor: StateFlow<Color> = _themeColor.asStateFlow()"""

new_prop = """    private val _themeColor = MutableStateFlow(ProBlue)
    val themeColor: StateFlow<Color> = _themeColor.asStateFlow()
    
    private val _updateInfo = MutableStateFlow<UpdateManager.UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateManager.UpdateInfo?> = _updateInfo.asStateFlow()"""

content = content.replace(prop_code, new_prop)

init_code = """    init {
        viewModelScope.launch {
            repository.settings.collect { settingsList ->"""

new_init = """    init {
        checkForUpdates()
        viewModelScope.launch {
            repository.settings.collect { settingsList ->"""

content = content.replace(init_code, new_init)

fun_code = """    private fun checkAttendanceAndNotify() {"""

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

    private fun checkAttendanceAndNotify() {"""

content = content.replace(fun_code, new_fun)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
