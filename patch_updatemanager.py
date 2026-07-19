import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UpdateManager.kt", "r") as f:
    content = f.read()

old_download = """    fun downloadAndInstallUpdate(context: Context, apkUrl: String, versionName: String) {
        Toast.makeText(context, "Descargando actualización...", Toast.LENGTH_SHORT).show()
        
        val request = DownloadManager.Request(Uri.parse(apkUrl))"""

new_download = """    fun downloadAndInstallUpdate(context: Context, apkUrl: String, versionName: String) {
        Toast.makeText(context, "Descargando actualización...", Toast.LENGTH_SHORT).show()
        
        // Respaldo de la última versión funcional
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val existingUpdate = File(downloadDir, "unibuddy_update.apk")
        if (existingUpdate.exists()) {
            val backupFile = File(downloadDir, "unibuddy_backup.apk")
            if (backupFile.exists()) backupFile.delete()
            existingUpdate.renameTo(backupFile)
        }

        val request = DownloadManager.Request(Uri.parse(apkUrl))"""

content = content.replace(old_download, new_download)

old_import = "import java.io.File\n"
new_import = "import java.io.File\nimport android.content.SharedPreferences\n"
if "import android.content.SharedPreferences" not in content:
    content = content.replace(old_import, new_import)

verify_code = """
    fun verifyBootSuccess(context: Context) {
        val prefs = context.getSharedPreferences("OTA_PREFS", Context.MODE_PRIVATE)
        val currentVersion = BuildConfig.VERSION_CODE
        val lastVerified = prefs.getInt("last_verified_version", -1)
        
        if (currentVersion > lastVerified) {
            // Arranque exitoso de nueva versión
            prefs.edit().putInt("last_verified_version", currentVersion).apply()
            
            // Podemos descartar la versión anterior
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val backupFile = File(downloadDir, "unibuddy_backup.apk")
            if (backupFile.exists()) {
                backupFile.delete()
                Log.d("UpdateManager", "Arranque exitoso verificado. Respaldo anterior descartado.")
            }
        }
    }
"""
content = content + verify_code

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UpdateManager.kt", "w") as f:
    f.write(content)
