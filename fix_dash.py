import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

bad_banner = """        // OTA Update Banner
        val context = androidx.compose.ui.platform.LocalContext.current
        updateInfo?.let { info ->"""

good_banner = """        // OTA Update Banner
        updateInfo?.let { info ->
            val localCtx = androidx.compose.ui.platform.LocalContext.current"""

content = content.replace(bad_banner, good_banner)

bad_btn = """onClick = { UpdateManager.downloadAndInstallUpdate(context, info.apkUrl, info.versionName) }"""
good_btn = """onClick = { UpdateManager.downloadAndInstallUpdate(localCtx, info.apkUrl, info.versionName) }"""

content = content.replace(bad_btn, good_btn)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
