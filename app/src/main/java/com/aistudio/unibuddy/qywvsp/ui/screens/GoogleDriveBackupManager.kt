package com.aistudio.unibuddy.qywvsp.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder

object GoogleDriveBackupManager {

    // Default Client ID (can be customized by user or injected via BuildConfig if available)
    var customClientId: String = "851509374020-f4vmoegbeu1p8k76c7s4jkae0v5j5dpe.apps.googleusercontent.com" // UniBuddy standard web client id

    suspend fun fetchUserInfo(accessToken: String): JSONObject? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://www.googleapis.com/oauth2/v3/userinfo")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $accessToken")
            
            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()
                JSONObject(sb.toString())
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun searchBackupFile(accessToken: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://www.googleapis.com/drive/v3/files?q=name='unibuddy_backup.json'+and+'appDataFolder'+in+parents&spaces=appDataFolder&fields=files(id,name)")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $accessToken")

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()
                val response = JSONObject(sb.toString())
                val files = response.optJSONArray("files")
                if (files != null && files.length() > 0) {
                    files.getJSONObject(0).getString("id")
                } else null
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun createBackupFile(accessToken: String, content: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Bearer $accessToken")
            val boundary = "unibuddy_boundary"
            conn.setRequestProperty("Content-Type", "multipart/related; boundary=$boundary")
            conn.doOutput = true

            val out = conn.outputStream
            val writer = OutputStreamWriter(out, "UTF-8")

            writer.write("--$boundary\r\n")
            writer.write("Content-Type: application/json; charset=UTF-8\r\n\r\n")
            writer.write("{\r\n  \"name\": \"unibuddy_backup.json\",\r\n  \"parents\": [\"appDataFolder\"]\r\n}\r\n")
            writer.write("\r\n--$boundary\r\n")
            writer.write("Content-Type: application/json; charset=UTF-8\r\n\r\n")
            writer.write(content)
            writer.write("\r\n--$boundary--\r\n")
            writer.flush()
            writer.close()

            conn.responseCode in 200..204
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateBackupFile(accessToken: String, fileId: String, content: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://www.googleapis.com/upload/drive/v3/files/$fileId?uploadType=media")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PATCH" // PATCH is preferred for partial update
            conn.setRequestProperty("Authorization", "Bearer $accessToken")
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.doOutput = true

            val writer = OutputStreamWriter(conn.outputStream, "UTF-8")
            writer.write(content)
            writer.flush()
            writer.close()

            conn.responseCode in 200..204
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun downloadBackupFile(accessToken: String, fileId: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://www.googleapis.com/drive/v3/files/$fileId?alt=media")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $accessToken")

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()
                sb.toString()
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleDriveLoginDialog(
    clientId: String,
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Iniciar sesión con Google", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = onDismiss) {
                        Text("Cerrar")
                    }
                }
                
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    val url = request?.url?.toString() ?: ""
                                    if (url.startsWith("http://localhost")) {
                                        val fragment = request?.url?.fragment ?: ""
                                        if (fragment.contains("access_token=")) {
                                            val params = fragment.split("&")
                                            val tokenParam = params.find { it.startsWith("access_token=") }
                                            val token = tokenParam?.substringAfter("access_token=") ?: ""
                                            if (token.isNotEmpty()) {
                                                onSuccess(URLDecoder.decode(token, "UTF-8"))
                                            }
                                        }
                                        return true
                                    }
                                    return false
                                }
                            }
                        }
                    },
                    update = { webView ->
                        val authUrl = "https://accounts.google.com/o/oauth2/v2/auth" +
                                "?client_id=$clientId" +
                                "&redirect_uri=http://localhost" +
                                "&response_type=token" +
                                "&scope=https://www.googleapis.com/auth/drive.appdata%20https://www.googleapis.com/auth/userinfo.email%20https://www.googleapis.com/auth/userinfo.profile"
                        webView.loadUrl(authUrl)
                    },
                    modifier = Modifier.fillMaxSize().weight(1f)
                )
            }
        }
    }
}
