package com.aistudio.unibuddy.qywvsp.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aistudio.unibuddy.qywvsp.R
import com.aistudio.unibuddy.qywvsp.data.Subject
import com.aistudio.unibuddy.qywvsp.ui.theme.*
import java.util.Locale
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.MapStyleOptions
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
    
    // Sectors of the university for interactive adjustment
    val campusSectors = listOf(
        "Entrada Principal",
        "Pabellón A (Aulas)",
        "Pabellón B (Sistemas)",
        "Biblioteca Central",
        "Laboratorios de Cómputo",
        "Cafetería",
        "Auditorio"
    )
    
    var selectedSector by remember { mutableStateOf(campusSectors[0]) }
    var isManualEditing by remember { mutableStateOf(false) }
    var manualAddressInput by remember { mutableStateOf("") }
    
    val baseAddress = when {
        destination.contains("RUPAP", ignoreCase = true) -> "Semáforos de la Villa Progreso, 3c al Norte. Frente a la Pista Larreynaga, Managua."
        destination.contains("RUSB", ignoreCase = true) -> "Avenida Universitaria Casimiro Sotelo, frente al Estadio Nacional Soberanía, Managua."
        destination.contains("Rubén Darío", ignoreCase = true) -> "Pista de la Resistencia, de la Rotonda Universitaria 100m al Oeste, Managua."
        destination.contains("UCA", ignoreCase = true) -> "Avenida Universitaria Casimiro Sotelo, Costado Oeste del Recinto Rubén Darío, Managua."
        else -> "Calle Central Principal, contiguo a oficinas académicas de $destination."
    }
    
    val fullAddress = if (manualAddressInput.isNotBlank()) {
        manualAddressInput
    } else {
        "$baseAddress Sector: $selectedSector"
    }

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
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = NavyBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
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
                        color = DarkGreen.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, DarkGreen.copy(alpha = 0.2f)),
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
                                    .background(DarkGreen)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "GPS Listo",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkGreen
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
                            text = "Se registrará tu asistencia con fecha y firma geolocalizada en el campus.",
                            color = SlateGray,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                    val sectorOffsetMap = mapOf(
                        "Entrada Principal" to LatLng(coords.first + 0.0005, coords.second),
                        "Pabellón A (Aulas)" to LatLng(coords.first, coords.second + 0.0005),
                        "Pabellón B (Sistemas)" to LatLng(coords.first - 0.0005, coords.second),
                        "Biblioteca Central" to LatLng(coords.first, coords.second - 0.0005),
                        "Laboratorios de Cómputo" to LatLng(coords.first + 0.0002, coords.second + 0.0002),
                        "Cafetería" to LatLng(coords.first - 0.0002, coords.second - 0.0002),
                        "Auditorio" to LatLng(coords.first + 0.0003, coords.second - 0.0003)
                    )
                    
                    val activeLocation = sectorOffsetMap[selectedSector] ?: LatLng(coords.first, coords.second)
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(activeLocation, 17f)
                    }

                    LaunchedEffect(activeLocation) {
                        cameraPositionState.animate(
                            update = com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(activeLocation, 17f)
                        )
                    }

                    val context = LocalContext.current
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(
                            isMyLocationEnabled = locationPermissionsState.allPermissionsGranted,
                            mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
                        ),
                        uiSettings = MapUiSettings(
                            compassEnabled = false,
                            myLocationButtonEnabled = true,
                            mapToolbarEnabled = false
                        )
                    ) {
                        Marker(
                            state = MarkerState(position = activeLocation),
                            title = "Campus: $destination",
                            snippet = "Sector: $selectedSector"
                        )
                    }

                    // Map Overlay Labels
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Campus: $destination",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyBlue,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )

                        Text(
                            text = if (locationPermissionsState.allPermissionsGranted) "Tu posición (GPS confirmado)" else "Posición (GPS Denegado)",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (locationPermissionsState.allPermissionsGranted) ProBlue else Terracotta,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Telemetry bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Coordenadas GPS", fontSize = 10.sp, color = SlateGray, fontWeight = FontWeight.Bold)
                        Text(
                            text = String.format(Locale.US, "%.5f, %.5f", coords.first, coords.second),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = NavyBlue
                        )
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text("Precisión de Señal", fontSize = 10.sp, color = SlateGray, fontWeight = FontWeight.Bold)
                        Text("± 6.4 m (Muy Alta)", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = DarkGreen)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sector Picker label
                Text(
                    text = "Ajustar sector de la universidad si estás en un aula específica:",
                    fontSize = 11.sp,
                    color = SlateGray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                
                Spacer(modifier = Modifier.height(6.dp))

                // Sector selection chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(campusSectors) { sector ->
                        val isSelected = sector == selectedSector
                        Surface(
                            modifier = Modifier.clickable { selectedSector = sector },
                            color = if (isSelected) NavyBlue else Color.White,
                            border = BorderStroke(1.dp, if (isSelected) NavyBlue else SlateGray.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = sector,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else NavyBlue,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp), color = SlateGray.copy(alpha = 0.1f))

                // Text Address Information display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = SlateGray,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dirección detectada:", fontSize = 11.sp, color = SlateGray, fontWeight = FontWeight.Bold)
                        if (isManualEditing) {
                            OutlinedTextField(
                                value = manualAddressInput.ifBlank { fullAddress },
                                onValueChange = { manualAddressInput = it },
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                placeholder = { Text("Escribe la dirección exacta...", fontSize = 12.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                maxLines = 2,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NavyBlue,
                                    unfocusedBorderColor = SlateGray.copy(alpha = 0.4f)
                                )
                            )
                        } else {
                            Text(
                                text = fullAddress,
                                fontSize = 12.sp,
                                color = NavyBlue,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Button to toggle manual editing
                Text(
                    text = if (isManualEditing) "Volver al GPS automático" else "Editar dirección manualmente",
                    fontSize = 11.sp,
                    color = ProBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable {
                            if (isManualEditing) {
                                manualAddressInput = ""
                            } else {
                                manualAddressInput = "$baseAddress Sector: $selectedSector"
                            }
                            isManualEditing = !isManualEditing
                        }
                        .padding(4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SlateGray),
                        border = BorderStroke(1.dp, SlateGray.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            onConfirm(subject, fullAddress)
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sí, es correcta", fontWeight = FontWeight.Bold, color = Bone)
                    }
                }
            }
        }
    }
}
