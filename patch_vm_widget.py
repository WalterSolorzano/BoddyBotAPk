import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

old_fun = """    private fun notifyWidgets() {
        com.aistudio.unibuddy.qywvsp.ui.widget.WidgetUpdater.updateAllWidgets(getApplication())
    }"""

new_fun = """    private fun notifyWidgets() {
        viewModelScope.launch {
            try {
                com.aistudio.unibuddy.qywvsp.ui.widget.PetBitmapRenderer.generateAndSavePetBitmap(
                    context = getApplication(),
                    pose = _buddyPose.value,
                    accessory = _buddyAccessory.value,
                    isHappy = true,
                    isWorried = false,
                    mainColorHex = _buddyColor.value
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            com.aistudio.unibuddy.qywvsp.ui.widget.WidgetUpdater.updateAllWidgets(getApplication())
        }
    }"""

content = content.replace(old_fun, new_fun)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
