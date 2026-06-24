package com.aistudio.unibuddy.qywvsp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.unibuddy.qywvsp.data.Assessment
import com.aistudio.unibuddy.qywvsp.ui.theme.*

@Composable
fun WellnessWidget(
    upcomingExamsCount: Int,
    absencesCount: Int,
    calculatedStress: Float,
    statusText: String
) {
    val statusColor = when (statusText) {
        "Crítico" -> ProRed
        "Moderado" -> Amber
        else -> DarkGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = statusColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Nivel de Estrés: $statusText",
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { calculatedStress / 100f },
                modifier = Modifier.fillMaxWidth().height(10.dp),
                color = statusColor,
                trackColor = DarkGray.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tienes $upcomingExamsCount evaluaciones próximas y $absencesCount inasistencias acumuladas.",
                fontSize = 12.sp,
                color = DarkGray
            )
        }
    }
}
