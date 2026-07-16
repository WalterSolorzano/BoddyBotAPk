package com.aistudio.unibuddy.qywvsp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.unibuddy.qywvsp.data.TripRecord
import com.aistudio.unibuddy.qywvsp.ui.theme.*
import java.util.Locale

@Composable
fun GPSAndStopwatchWidget(
    currentDistanceToCollege: Double?,
    locationBasedTravelMinutes: Int,
    baseTravelTimeSource: Int,
    isOutOfRange: Boolean,
    isTripActive: Boolean,
    tripElapsedSeconds: Int,
    onRequestGPS: () -> Unit,
    onStartTrip: () -> Unit,
    onEndTrip: (Int) -> Unit
) {
    val delayMinutes = locationBasedTravelMinutes - baseTravelTimeSource

    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(20.dp)).padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Bone)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // -- GPS INFO --
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Ubicación y Trayecto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavyBlue
                    )
                    Text(
                        text = "Distancia calculada hacia la universidad",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateGray
                    )
                }
                IconButton(
                    onClick = onRequestGPS,
                    modifier = Modifier.size(32.dp).background(Bone, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualizar GPS",
                        tint = NavyBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Distancia",
                        fontSize = 11.sp,
                        color = SlateGray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (currentDistanceToCollege == null) "-- km" else String.format(Locale.US, "%.2f km", currentDistanceToCollege),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = NavyBlue
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Tiempo Estimado",
                        fontSize = 11.sp,
                        color = SlateGray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isOutOfRange) "Fuera del Rango Académico\n(Modo Vacaciones)" else "$locationBasedTravelMinutes min",
                        fontSize = if (isOutOfRange) 12.sp else 20.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Right,
                        fontWeight = FontWeight.Black,
                        color = if (isOutOfRange) Terracotta else DarkGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val isTooFar = isOutOfRange

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isTooFar || delayMinutes > 5) Terracotta.copy(alpha = 0.08f) else MintGreen.copy(alpha = 0.08f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isTooFar || delayMinutes > 5) Icons.Default.Warning else Icons.Default.Info,
                    contentDescription = null,
                    tint = if (isTooFar || delayMinutes > 5) Terracotta else DarkGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isTooFar) {
                        "Atención: Estás demasiado lejos de la universidad (a ${String.format(Locale.US, "%.1f", currentDistanceToCollege)} km). El cálculo de trayecto es inusualmente largo."
                    } else if (delayMinutes > 5) {
                        "¡Viajarás +$delayMinutes min respecto a tu promedio diario ($baseTravelTimeSource min)."
                    } else if (delayMinutes < -2) {
                        val absDiff = -delayMinutes
                        "Llegarás rápido (-$absDiff min de tu promedio)."
                    } else {
                        "Viaje dentro del promedio habitual ($baseTravelTimeSource min)."
                    },
                    fontSize = 12.sp,
                    color = if (isTooFar || delayMinutes > 5) Color(0xFF7A1C1C) else Color(0xFF1E4E2C),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val context = androidx.compose.ui.platform.LocalContext.current
            Button(
                onClick = {
                    val shareIntent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(android.content.Intent.EXTRA_TEXT, "Acabo de salir. Mi tiempo estimado de llegada es de $locationBasedTravelMinutes minutos. ¡Te aviso cuando llegue!")
                        type = "text/plain"
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Aviso de Viaje Seguro..."))
                },
                modifier = Modifier.fillMaxWidth().height(42.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Compartir Estado (Viaje Seguro)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Bone)
            Spacer(modifier = Modifier.height(16.dp))

            // -- STOPWATCH --
            if (!isTripActive) {
                Button(
                    onClick = onStartTrip,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Comenzar Viaje Casa -> Universidad", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE8DEF8), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Terracotta)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("VIAJE EN CURSO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Terracotta, letterSpacing = 1.sp)
                        }
                        val mins = tripElapsedSeconds / 60
                        val secs = tripElapsedSeconds % 60
                        Text(
                            text = String.format(Locale.US, "%02dm %02ds", mins, secs),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = NavyBlue
                        )
                    }

                    Button(
                        onClick = {
                            val finalMinutes = (tripElapsedSeconds / 60).coerceAtLeast(1)
                            onEndTrip(finalMinutes)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Bone, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Llegué", color = Bone, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
