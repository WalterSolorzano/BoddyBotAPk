package com.aistudio.unibuddy.qywvsp.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aistudio.unibuddy.qywvsp.data.Subject
import com.aistudio.unibuddy.qywvsp.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import android.Manifest

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun GPSConfirmationDialog(
    subject: Subject,
    viewModel: UniBuddyViewModel,
    onDismiss: () -> Unit,
    onConfirm: (Subject, String) -> Unit
) {
    val destination by viewModel.destination.collectAsState()
    val coords = viewModel.getSelectedUniversityCoords() ?: Pair(12.1264, -86.2711)
    
    val currentLat by viewModel.currentLat.collectAsState()
    val currentLon by viewModel.currentLon.collectAsState()
    
    // Validate distance
    val distanceKm = remember(currentLat, currentLon, coords) {
        if (currentLat != null && currentLon != null) {
            val dLat = Math.toRadians(coords.first - currentLat!!)
            val dLon = Math.toRadians(coords.second - currentLon!!)
            val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(currentLat!!)) * Math.cos(Math.toRadians(coords.first)) *
                    Math.sin(dLon / 2) * Math.sin(dLon / 2)
            val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
            6371 * c // distance in km
        } else {
            null
        }
    }
    
    val isWithinCampus = distanceKm != null && distanceKm <= 0.5 // 500 meters

    // Permissions
    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        if (!locationPermissionsState.allPermissionsGranted) {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }
    
    val permissionGranted = locationPermissionsState.allPermissionsGranted
    
    // Animating the radar pulse for current location on the canvas
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadiusScale by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .shadow(12.dp, RoundedCornerShape(24.dp))
                .border(1.dp, NavyBlue.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with custom icon and indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(NavyBlue.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = NavyBlue, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Confirmar Ubicación GPS",
                                fontWeight = FontWeight.Bold,
                                color = NavyBlue,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Validación de asistencia",
                                color = SlateGray,
                                fontSize = 11.sp
                            )
                        }
                    }
                    
                    Surface(
                        color = if (permissionGranted) DarkGreen.copy(alpha = 0.1f) else Terracotta.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, if (permissionGranted) DarkGreen.copy(alpha = 0.2f) else Terracotta.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (permissionGranted) DarkGreen else Terracotta)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (permissionGranted) "GPS Listo" else "Denegado",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (permissionGranted) DarkGreen else Terracotta
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Info Box describing course
                Surface(
                    color = NavyBlue.copy(alpha = 0.03f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Clase: ${subject.name}",
                            fontWeight = FontWeight.Bold,
                            color = NavyBlue,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Se registrará tu asistencia mediante tu ubicación GPS real. Necesitas estar a menos de 500m del campus.",
                            color = SlateGray,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Beautiful Stylized Map Canvas Component
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE8F0FE)) // Light blue background for map
                        .border(1.dp, ProBlue.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Drawing grid lines for map texture
                        val gridStep = 40f
                        for (i in 0..size.width.toInt() step gridStep.toInt()) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.5f),
                                start = Offset(i.toFloat(), 0f),
                                end = Offset(i.toFloat(), size.height),
                                strokeWidth = 2f
                            )
                        }
                        for (i in 0..size.height.toInt() step gridStep.toInt()) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.5f),
                                start = Offset(0f, i.toFloat()),
                                end = Offset(size.width, i.toFloat()),
                                strokeWidth = 2f
                            )
                        }

                        // Draw animated radar ping on the center
                        val activeX = size.width / 2
                        val activeY = size.height / 2

                        drawCircle(
                            color = ProBlue.copy(alpha = pulseAlpha),
                            radius = pulseRadiusScale * 2,
                            center = Offset(activeX, activeY)
                        )

                        // Draw solid dot
                        drawCircle(
                            color = NavyBlue,
                            radius = 12f,
                            center = Offset(activeX, activeY)
                        )
                        
                        // Draw white inner dot
                        drawCircle(
                            color = Color.White,
                            radius = 4f,
                            center = Offset(activeX, activeY)
                        )
                    }
                    
                    if (distanceKm != null) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp),
                            color = Color.White.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "A ${String.format(java.util.Locale.US, "%.0f", distanceKm * 1000)}m",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = if (isWithinCampus) DarkGreen else Terracotta
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Diagnostics Panel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Coordenadas Campus", fontSize = 10.sp, color = SlateGray, fontWeight = FontWeight.Bold)
                        Text(String.format(java.util.Locale.US, "%.4f, %.4f", coords.first, coords.second), fontSize = 12.sp, color = NavyBlue)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Coordenadas Actuales", fontSize = 10.sp, color = SlateGray, fontWeight = FontWeight.Bold)
                        if (currentLat != null && currentLon != null) {
                            Text(String.format(java.util.Locale.US, "%.4f, %.4f", currentLat, currentLon), fontSize = 12.sp, color = NavyBlue)
                        } else {
                            Text("Buscando...", fontSize = 12.sp, color = SlateGray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                if (permissionGranted) {
                    if (currentLat == null) {
                         Text(
                            text = "Buscando GPS... Puedes registrar de todas formas si tienes prisa.",
                            color = SlateGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (!isWithinCampus) {
                         Text(
                            text = "Estás fuera del campus. Puedes registrar tu asistencia manualmente abajo.",
                            color = Terracotta,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Text(
                        text = "Permiso de GPS denegado. Puedes registrar tu asistencia de forma manual.",
                        color = Terracotta,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Bone)
                    ) {
                        Text("Cancelar", color = SlateGray, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    val canUseGps = permissionGranted && isWithinCampus
                    Button(
                        onClick = {
                            val locationNote = if (canUseGps) {
                                "Campus $destination"
                            } else if (currentLat == null) {
                                "Manual (Sin GPS)"
                            } else {
                                "Manual (Fuera de rango)"
                            }
                            onConfirm(subject, locationNote)
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canUseGps) NavyBlue else Amber,
                            contentColor = Color.White
                        ),
                        enabled = true
                    ) {
                        Text(
                            text = if (canUseGps) "Confirmar" else "Registrar de todas formas", 
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
