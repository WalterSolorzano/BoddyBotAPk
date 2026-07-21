package com.aistudio.unibuddy.qywvsp.ui

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.aistudio.unibuddy.qywvsp.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import android.content.SharedPreferences

object UpdateManager {

    // Cambia esto a la URL de tu repositorio en GitHub
    // Formato: https://raw.githubusercontent.com/USUARIO/REPO/main/update.json
    private const val UPDATE_JSON_URL = "https://raw.githubusercontent.com/WalterSolorzano/Android/main/update.json"

    data class UpdateInfo(
        val isUpdateAvailable: Boolean,
        val versionName: String,
        val releaseNotes: String,
        val apkUrl: String,
        val dynamicConfig: Map<String, String>? = null
    )

    suspend fun checkForUpdates(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            val request = Request.Builder().url(UPDATE_JSON_URL).build()
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val jsonStr = response.body?.string() ?: return@withContext null
                val json = JSONObject(jsonStr)
                
                val serverVersionCode = json.optInt("versionCode", 0)
                val versionName = json.optString("versionName", "")
                val apkUrl = json.optString("apkUrl", "")
                val releaseNotes = json.optString("releaseNotes", "")
                
                val dynamicConfigMap = mutableMapOf<String, String>()
                if (json.has("dynamicConfig")) {
                    val configJson = json.getJSONObject("dynamicConfig")
                    val keys = configJson.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        dynamicConfigMap[key] = configJson.getString(key)
                    }
                }
                
                // Opción 1 y 2 juntas
                return@withContext UpdateInfo(
                    isUpdateAvailable = serverVersionCode > BuildConfig.VERSION_CODE,
                    versionName = versionName,
                    releaseNotes = releaseNotes,
                    apkUrl = apkUrl,
                    dynamicConfig = dynamicConfigMap
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("UpdateManager", "Error checking for updates", e)
        }
        return@withContext null
    }

    fun downloadAndInstallUpdate(context: Context, apkUrl: String, versionName: String) {
        Toast.makeText(context, "Descargando actualización...", Toast.LENGTH_SHORT).show()
        
        // Respaldo de la última versión funcional
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val existingUpdate = File(downloadDir, "unibuddy_update.apk")
        if (existingUpdate.exists()) {
            val backupFile = File(downloadDir, "unibuddy_backup.apk")
            if (backupFile.exists()) backupFile.delete()
            existingUpdate.renameTo(backupFile)
        }

        val request = DownloadManager.Request(Uri.parse(apkUrl))
            .setTitle("UniBuddy Update v$versionName")
            .setDescription("Descargando nueva versión...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "unibuddy_update.apk")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)
        
        // Registrar receiver para cuando termine
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    installApk(context, downloadId)
                    context.unregisterReceiver(this)
                }
            }
        }
        
        androidx.core.content.ContextCompat.registerReceiver(context, receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), androidx.core.content.ContextCompat.RECEIVER_EXPORTED)
    }

    private fun installApk(context: Context, downloadId: Long) {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = downloadManager.getUriForDownloadedFile(downloadId) ?: return
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al instalar la actualización. Búscala en Descargas.", Toast.LENGTH_LONG).show()
        }
    }

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
}
