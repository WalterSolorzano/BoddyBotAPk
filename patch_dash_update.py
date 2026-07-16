import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

import_code = "import androidx.compose.material.icons.filled.*\n"
new_import = import_code + "import androidx.compose.material.icons.filled.SystemUpdate\nimport android.content.Context\nimport com.aistudio.unibuddy.qywvsp.ui.UpdateManager\n"
if "import com.aistudio.unibuddy.qywvsp.ui.UpdateManager" not in content:
    content = content.replace(import_code, new_import)

val_code = "    val attendanceLogs by viewModel.attendanceLogs.collectAsStateWithLifecycle()\n"
new_val = val_code + "    val updateInfo by viewModel.updateInfo.collectAsStateWithLifecycle()\n"
if "val updateInfo by" not in content:
    content = content.replace(val_code, new_val)

banner_code = """        // Dashboard Header
        Row("""

new_banner = """        // OTA Update Banner
        val context = androidx.compose.ui.platform.LocalContext.current
        updateInfo?.let { info ->
            if (info.isUpdateAvailable) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = ProBlue.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = ProBlue, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("¡Nueva versión disponible!", fontWeight = FontWeight.Bold, color = ProBlue, fontSize = 16.sp)
                            Text("Versión ${info.versionName}: ${info.releaseNotes}", color = SlateGray, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                        Button(
                            onClick = { UpdateManager.downloadAndInstallUpdate(context, info.apkUrl, info.versionName) },
                            colors = ButtonDefaults.buttonColors(containerColor = ProBlue)
                        ) {
                            Text("Actualizar", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Dashboard Header
        Row("""

if "OTA Update Banner" not in content:
    content = content.replace(banner_code, new_banner)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
