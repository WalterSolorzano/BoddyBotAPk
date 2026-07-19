package com.aistudio.unibuddy.qywvsp.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.unibuddy.qywvsp.data.Assessment
import com.aistudio.unibuddy.qywvsp.ui.theme.*

enum class PixelHeartType {
    FULL, HALF, EMPTY
}

@Composable
fun PixelHeart(
    heartType: PixelHeartType,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val pixelSizeX = size.toPx() / 9f
        val pixelSizeY = size.toPx() / 8f

        val fullGrid = arrayOf(
            intArrayOf(0, 0, 1, 1, 0, 1, 1, 0, 0),
            intArrayOf(0, 1, 3, 2, 1, 2, 2, 1, 0),
            intArrayOf(1, 3, 2, 2, 2, 2, 2, 2, 1),
            intArrayOf(1, 2, 2, 2, 2, 2, 2, 2, 1),
            intArrayOf(0, 1, 2, 2, 2, 2, 2, 1, 0),
            intArrayOf(0, 0, 1, 2, 2, 2, 1, 0, 0),
            intArrayOf(0, 0, 0, 1, 2, 1, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 1, 0, 0, 0, 0)
        )

        val halfGrid = arrayOf(
            intArrayOf(0, 0, 1, 1, 0, 1, 1, 0, 0),
            intArrayOf(0, 1, 3, 2, 1, 0, 0, 1, 0),
            intArrayOf(1, 3, 2, 2, 2, 0, 0, 0, 1),
            intArrayOf(1, 2, 2, 2, 2, 0, 0, 0, 1),
            intArrayOf(0, 1, 2, 2, 2, 0, 0, 1, 0),
            intArrayOf(0, 0, 1, 2, 2, 0, 1, 0, 0),
            intArrayOf(0, 0, 0, 1, 2, 1, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 1, 0, 0, 0, 0)
        )

        val emptyGrid = arrayOf(
            intArrayOf(0, 0, 1, 1, 0, 1, 1, 0, 0),
            intArrayOf(0, 1, 0, 0, 1, 0, 0, 1, 0),
            intArrayOf(1, 0, 0, 0, 0, 0, 0, 0, 1),
            intArrayOf(1, 0, 0, 0, 0, 0, 0, 0, 1),
            intArrayOf(0, 1, 0, 0, 0, 0, 0, 1, 0),
            intArrayOf(0, 0, 1, 0, 0, 0, 1, 0, 0),
            intArrayOf(0, 0, 0, 1, 0, 1, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 1, 0, 0, 0, 0)
        )

        val grid = when (heartType) {
            PixelHeartType.FULL -> fullGrid
            PixelHeartType.HALF -> halfGrid
            PixelHeartType.EMPTY -> emptyGrid
        }

        for (row in 0..7) {
            for (col in 0..8) {
                val pixelType = grid[row][col]
                if (pixelType != 0) {
                    val color = when (pixelType) {
                        1 -> Color(0xFF1E293B) // Slate outline
                        2 -> Color(0xFFFF4D4D) // Red fill
                        3 -> Color(0xFFFFFFFF) // White shine highlight
                        else -> Color.Transparent
                    }
                    drawRect(
                        color = color,
                        topLeft = Offset(col * pixelSizeX, row * pixelSizeY),
                        size = Size(pixelSizeX + 0.5f, pixelSizeY + 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun CartoonPixelLifebarWidget(
    calculatedStress: Float,
    modifier: Modifier = Modifier
) {
    val healthPercent = (100f - calculatedStress).coerceIn(0f, 100f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
            .border(2.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ACADEMIC HP",
                    fontFamily = cartoonFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color(0xFFE2E8F0)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (i in 1..5) {
                        val heartThreshold = i * 20f
                        val heartType = when {
                            healthPercent >= heartThreshold -> PixelHeartType.FULL
                            healthPercent >= heartThreshold - 10f -> PixelHeartType.HALF
                            else -> PixelHeartType.EMPTY
                        }
                        PixelHeart(heartType = heartType, size = 20.dp)
                    }
                }
            }
            Text(
                text = "${healthPercent.toInt()}/100",
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (healthPercent > 50) Color(0xFF4ADE80) else if (healthPercent > 20) Color(0xFFFBBF24) else Color(0xFFF87171)
            )
        }
    }
}

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
            
            CartoonPixelLifebarWidget(calculatedStress = calculatedStress)
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Tienes $upcomingExamsCount evaluaciones próximas y $absencesCount inasistencias acumuladas.",
                fontSize = 12.sp,
                color = DarkGray
            )
        }
    }
}
