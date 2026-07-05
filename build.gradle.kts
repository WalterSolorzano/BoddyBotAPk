tasks.register("extractSettings") {
    doLast {
        val file = file("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyApp.kt")
        val lines = file.readLines()
        val startSettings = 4027
        val endSettings = 4607
        val settingsLines = lines.subList(startSettings - 1, endSettings)
        
        val newContent = """package com.aistudio.unibuddy.qywvsp.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aistudio.unibuddy.qywvsp.ui.*
import com.aistudio.unibuddy.qywvsp.ui.components.*
import com.aistudio.unibuddy.qywvsp.ui.theme.*

@Composable
""" + settingsLines.joinToString("\n").replace("fun ConfigTabScreen", "fun SettingsScreen")

        val newFile = file("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/SettingsScreen.kt")
        newFile.writeText(newContent)
        
        val newAppLines = lines.subList(0, startSettings - 1) + lines.subList(endSettings, lines.size)
        file.writeText(newAppLines.joinToString("\n"))
    }
}
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
  alias(libs.plugins.firebase.appdistribution) apply false
  alias(libs.plugins.google.services) apply false
}
