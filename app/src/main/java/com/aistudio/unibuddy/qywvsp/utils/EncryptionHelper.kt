package com.aistudio.unibuddy.qywvsp.utils

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.File
import java.nio.charset.StandardCharsets

object EncryptionHelper {
    
    private fun getMasterKey(context: Context): MasterKey {
        return MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
    
    fun encryptStringToFile(context: Context, data: String, fileName: String): File {
        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            file.delete()
        }
        
        val encryptedFile = EncryptedFile.Builder(
            context,
            file,
            getMasterKey(context),
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
        
        encryptedFile.openFileOutput().use { outputStream ->
            outputStream.write(data.toByteArray(StandardCharsets.UTF_8))
            outputStream.flush()
        }
        
        return file
    }
    
    fun decryptStringFromFile(context: Context, file: File): String {
        if (!file.exists()) return ""
        
        val encryptedFile = EncryptedFile.Builder(
            context,
            file,
            getMasterKey(context),
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
        
        return encryptedFile.openFileInput().use { inputStream ->
            String(inputStream.readBytes(), StandardCharsets.UTF_8)
        }
    }
}
