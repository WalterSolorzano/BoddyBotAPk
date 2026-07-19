import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/MainActivity.kt", "r") as f:
    content = f.read()

old_create = """    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)"""

new_create = """    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            com.aistudio.unibuddy.qywvsp.ui.UpdateManager.verifyBootSuccess(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }"""

content = content.replace(old_create, new_create)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/MainActivity.kt", "w") as f:
    f.write(content)
