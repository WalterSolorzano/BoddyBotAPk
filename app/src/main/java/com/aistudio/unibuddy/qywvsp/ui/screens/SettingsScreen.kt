package com.aistudio.unibuddy.qywvsp.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.aistudio.unibuddy.qywvsp.ui.UniBuddyViewModel
import com.aistudio.unibuddy.qywvsp.ui.SemesterHistoryView
import com.aistudio.unibuddy.qywvsp.ui.PresetDropdownField
import com.aistudio.unibuddy.qywvsp.ui.components.*
import com.aistudio.unibuddy.qywvsp.ui.theme.*
import com.aistudio.unibuddy.qywvsp.data.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar

@Composable
fun SettingsScreen(viewModel: UniBuddyViewModel, onNavigateToPensum: () -> Unit) {
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(350)
        isLoading = false
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundBone),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = ProBlue)
        }
        return
    }

    val username by viewModel.username.collectAsStateWithLifecycle()
    val buddyColorStr by viewModel.buddyColor.collectAsStateWithLifecycle()
    val mainBuddyColor = Color(android.graphics.Color.parseColor(buddyColorStr))
    
    var showProfileDialog by remember { mutableStateOf(false) }
    var showRouteDialog by remember { mutableStateOf(false) }
    
    val routeSettingsRequested by viewModel.showRouteSettingsRequested.collectAsStateWithLifecycle()
    LaunchedEffect(routeSettingsRequested) {
        if (routeSettingsRequested) {
            showRouteDialog = true
            viewModel.requestRouteSettings(false) // consume the request event
        }
    }
    
    var showBadgeDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showBuddyDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showPdfImportDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showSemesterDialog by remember { mutableStateOf(false) }

    if (showHistoryDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showHistoryDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(modifier = Modifier.fillMaxSize(), color = BackgroundBone) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showHistoryDialog = false }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Historial de Semestres", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = NavyBlue)
                    }
                    Box(modifier = Modifier.weight(1f).padding(16.dp)) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            SemesterHistoryView(viewModel = viewModel)
                            Spacer(modifier = Modifier.height(24.dp))
                            FocusHistoryChart(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("config_column")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BuddyMascot(
                modifier = Modifier.size(80.dp),
                pose = "greeting",
                mainColor = mainBuddyColor,
                accessory = viewModel.buddyAccessory.collectAsStateWithLifecycle().value
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = "Hola, $username", style = MaterialTheme.typography.headlineMedium, color = NavyBlue, fontWeight = FontWeight.Bold)
                Text(text = "Personaliza tu experiencia", style = MaterialTheme.typography.bodyMedium, color = SlateGray)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid Layout for Settings
        val gridItems = listOf(
            ConfigGridItem("Perfil", Icons.Rounded.Face, "Nombre, Foto, Carrera") { showProfileDialog = true },
            ConfigGridItem("Mascota", Icons.Rounded.SmartToy, "Accesorios y Color") { showBuddyDialog = true },
            ConfigGridItem("Rutas", Icons.Rounded.Explore, "Origen, Destino, GPS") { showRouteDialog = true },
            ConfigGridItem("Historial", Icons.Rounded.QueryStats, "Estadísticas pasadas") { showHistoryDialog = true },
            ConfigGridItem("Pensum", Icons.Rounded.MenuBook, "Progreso de Carrera") { onNavigateToPensum() },
            ConfigGridItem("Semestre", Icons.Rounded.CalendarMonth, "Inicio, Notas, Feriados") { showSemesterDialog = true },
            ConfigGridItem("Importar PDF", Icons.Rounded.DriveFolderUpload, "Historial de Notas") { showPdfImportDialog = true },
            ConfigGridItem("Insignias", Icons.Rounded.EmojiEvents, "Logros y Medallas") { showBadgeDialog = true },
            ConfigGridItem("Sistema", Icons.Rounded.Build, "Backup, Reset, Reportes") { showResetDialog = true }
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(gridItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clickable { item.onClick() },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = NavyBlue,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = item.title, fontWeight = FontWeight.Bold, color = NavyBlue, fontSize = 16.sp)
                        Text(text = item.subtitle, fontSize = 10.sp, color = SlateGray, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }

    if (showProfileDialog) {
        ProfileDialog(viewModel) { showProfileDialog = false }
    }
    if (showRouteDialog) {
        RouteSettingsDialog(viewModel) { showRouteDialog = false }
    }
    if (showBadgeDialog) {
        BadgeDialog(viewModel) { showBadgeDialog = false }
    }
    if (showResetDialog) {
        SystemSettingsDialog(viewModel, onReportError = { showErrorDialog = true }) { showResetDialog = false }
    }
    if (showSemesterDialog) {
        SemesterSettingsDialog(viewModel) { showSemesterDialog = false }
    }
    if (showBuddyDialog) {
        BuddyCustomizationDialog(viewModel) { showBuddyDialog = false }
    }
    if (showPdfImportDialog) {
        PdfImportDialog(viewModel) { showPdfImportDialog = false }
    }

    if (showErrorDialog) {
        ErrorReportDialog(viewModel = viewModel) { showErrorDialog = false }
    }
}

data class ConfigGridItem(val title: String, val icon: ImageVector, val subtitle: String, val onClick: () -> Unit)

@Composable
fun ProfileDialog(viewModel: UniBuddyViewModel, onDismiss: () -> Unit) {
    val username by viewModel.username.collectAsStateWithLifecycle()
    val career by viewModel.career.collectAsStateWithLifecycle()
    
    val photoUri by viewModel.profilePhotoUri.collectAsStateWithLifecycle()
    
    var editingName by remember { mutableStateOf(username) }
    var editingCareer by remember { mutableStateOf(career) }
    
    val photoLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            viewModel.saveProfilePhoto(uri.toString())
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mi Perfil", fontWeight = FontWeight.Bold, color = NavyBlue) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(BackgroundGray)
                        .clickable { photoLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUri != null) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.CameraAlt,
                            contentDescription = null,
                            tint = NavyBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Text("Toca para cambiar foto", fontSize = 10.sp, color = SlateGray)
                
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = editingName,
                    onValueChange = { editingName = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = editingCareer,
                    onValueChange = { editingCareer = it },
                    label = { Text("Carrera Universitaria") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    viewModel.saveUsername(editingName)
                    viewModel.saveCareer(editingCareer)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) { 
                Text("Guardar", color = Bone) 
            }
        },
        dismissButton = { 
            TextButton(onClick = onDismiss) { 
                Text("Cerrar", color = SlateGray) 
            } 
        },
        containerColor = Color.White
    )
}

@Composable
fun BadgeDialog(viewModel: UniBuddyViewModel, onDismiss: () -> Unit) {
    val badges by viewModel.badges.collectAsStateWithLifecycle()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mis Logros", fontWeight = FontWeight.Bold, color = NavyBlue) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                items(badges) { badge ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (badge.isUnlocked) MintGreen.copy(alpha = 0.2f) else BackgroundGray),
                            contentAlignment = Alignment.Center
                        ) {
                            val iconVector = when(badge.category) {
                                "Attendance" -> Icons.Default.CheckCircle
                                "Grades" -> Icons.Default.Star
                                "Focus" -> Icons.Default.Timer
                                else -> Icons.Default.Flag
                            }
                            Icon(imageVector = iconVector, contentDescription = null, tint = if(badge.isUnlocked) NavyBlue else SlateGray, modifier = Modifier.size(24.dp).alpha(if(badge.isUnlocked) 1f else 0.3f))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = badge.name, fontWeight = FontWeight.Bold, color = if(badge.isUnlocked) NavyBlue else SlateGray)
                            Text(text = badge.description, fontSize = 10.sp, color = SlateGray)
                            if (badge.isUnlocked) {
                                Text(text = "Desbloqueado: ${badge.dateUnlocked}", fontSize = 9.sp, color = DarkGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Genial") } }
    )
}

@Composable
fun RouteSettingsDialog(viewModel: UniBuddyViewModel, onDismiss: () -> Unit) {
    val origin by viewModel.origin.collectAsStateWithLifecycle()
    val destination by viewModel.destination.collectAsStateWithLifecycle()
    val baseTravelTime by viewModel.baseTravelTime.collectAsStateWithLifecycle()
    val home by viewModel.homeAddress.collectAsStateWithLifecycle()
    val work by viewModel.workAddress.collectAsStateWithLifecycle()
    
    var editingOrigin by remember { mutableStateOf(origin) }
    var editingDestination by remember { mutableStateOf(destination) }
    var travelMinutes by remember { mutableStateOf(baseTravelTime.toString()) }
    var editingHome by remember { mutableStateOf(home) }
    var editingWork by remember { mutableStateOf(work) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configuración de Ruta", fontWeight = FontWeight.Bold, color = NavyBlue) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PresetDropdownField(
                    label = "Origen Habitual",
                    value = editingOrigin,
                    onValueChange = { newVal -> editingOrigin = newVal },
                    options = listOf("Ubicación actual (GPS)", "Casa", "Trabajo"),
                    readOnly = true
                )
                
                if (editingOrigin == "Casa") {
                    OutlinedTextField(value = editingHome, onValueChange = { editingHome = it }, label = { Text("Dirección Casa") })
                }
                if (editingOrigin == "Trabajo") {
                    OutlinedTextField(value = editingWork, onValueChange = { editingWork = it }, label = { Text("Dirección Trabajo") })
                }

                PresetDropdownField(
                    label = "Facultad Destino",
                    value = editingDestination,
                    onValueChange = { newVal -> editingDestination = newVal },
                    options = viewModel.universities.map { it.name },
                    readOnly = true
                )
                
                Text("Viaje estimado: $travelMinutes min", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = travelMinutes.toFloatOrNull() ?: 20f,
                    onValueChange = { travelMinutes = it.toInt().toString() },
                    valueRange = 5f..120f,
                    steps = 23
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                viewModel.saveRoute(editingOrigin, editingDestination)
                viewModel.saveHomeWorkAddresses(editingHome, editingWork)
                travelMinutes.toIntOrNull()?.let { viewModel.saveBaseTravelTime(it) }
                onDismiss()
            }) { Text("Aplicar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun BuddyCustomizationDialog(viewModel: UniBuddyViewModel, onDismiss: () -> Unit) {
    val accessory by viewModel.buddyAccessory.collectAsStateWithLifecycle()
    val buddyColorStr by viewModel.buddyColor.collectAsStateWithLifecycle()
    val buddyPose by viewModel.buddyPose.collectAsStateWithLifecycle()
    val badges by viewModel.badges.collectAsStateWithLifecycle()
    
    val accessoryRequirements = mapOf(
        "hat" to "Primeros Pasos",
        "cap" to "Estudiante Responsable",
        "glasses" to "Concentración Total",
        "sunglasses" to "En el Top"
    )
    
    val isAccessoryUnlocked: (String) -> Boolean = { acc ->
        if (acc == "none") true
        else {
            val reqBadge = accessoryRequirements[acc]
            if (reqBadge != null) {
                badges.find { it.name == reqBadge }?.isUnlocked == true
            } else {
                true
            }
        }
    }
    
    val accessories = listOf("none", "hat", "cap", "glasses", "sunglasses", "scarf", "sombrero_nica")
    val accessoriesLabels = mapOf(
        "none" to "Ninguno",
        "hat" to "Casco",
        "cap" to "Gorra",
        "glasses" to "Lentes",
        "sunglasses" to "Gafas de Sol",
        "scarf" to "Bufanda",
        "sombrero_nica" to "Sombrero Pita (Vacacionero)"
    )
    val colors = listOf("#4CAF50", "#2196F3", "#F44336", "#9C27B0", "#FF9800", "#607D8B")
    val poses = listOf("idle", "greeting", "working", "sleeping", "celebrating", "exam")
    val poseLabels = mapOf(
        "idle" to "Relajado",
        "greeting" to "Saludando",
        "working" to "Estudiando",
        "sleeping" to "Durmiendo",
        "celebrating" to "Celebrando",
        "exam" to "Concentrado"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Personalizar Buddy", fontWeight = FontWeight.Bold, color = NavyBlue) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Gesto / Postura", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyBlue)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(poses) { p ->
                        FilterChip(
                            selected = buddyPose == p,
                            onClick = { viewModel.saveBuddyPose(p) },
                            label = { Text(poseLabels[p] ?: p) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NavyBlue.copy(alpha = 0.15f),
                                selectedLabelColor = NavyBlue
                            )
                        )
                    }
                }

                Text("Accesorio", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyBlue)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(accessories) { acc ->
                        val unlocked = isAccessoryUnlocked(acc)
                        FilterChip(
                            selected = accessory == acc && unlocked,
                            onClick = { 
                                if (unlocked) {
                                    viewModel.saveBuddyCustomization(acc, buddyColorStr)
                                }
                            },
                            label = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!unlocked) {
                                        Icon(Icons.Default.Lock, contentDescription = "Locked", modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(accessoriesLabels[acc] ?: acc.replaceFirstChar { it.uppercase() })
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NavyBlue.copy(alpha = 0.15f),
                                selectedLabelColor = NavyBlue
                            )
                        )
                    }
                }
                
                Text("Color de Piel", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyBlue)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(colors) { colorHex ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(colorHex)))
                                .border(
                                    width = if (buddyColorStr == colorHex) 3.dp else 0.dp,
                                    color = NavyBlue,
                                    shape = CircleShape
                                )
                                .clickable { viewModel.saveBuddyCustomization(accessory, colorHex) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    BuddyMascot(
                        modifier = Modifier.size(110.dp),
                        pose = buddyPose,
                        accessory = accessory,
                        mainColor = Color(android.graphics.Color.parseColor(buddyColorStr))
                    )
                }
            }
        },
        confirmButton = { 
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) { 
                Text("Guardar", color = Bone) 
            } 
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = SlateGray)
            }
        },
        containerColor = Color.White
    )
}

@Composable
fun SystemSettingsDialog(viewModel: UniBuddyViewModel, onReportError: () -> Unit, onDismiss: () -> Unit) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val autoCheckinEnabled by viewModel.autoCheckinEnabled.collectAsStateWithLifecycle()
    val smartSilenceEnabled by viewModel.smartSilenceEnabled.collectAsStateWithLifecycle()
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val context = LocalContext.current
    
    var showResetConfirmation by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustes del Sistema", fontWeight = FontWeight.Bold, color = NavyBlue) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Theme Configuration
                Text("Preferencia de Tema", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyBlue)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Tema Oscuro", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text("Activar interfaz nocturna amigable", fontSize = 11.sp, color = SlateGray)
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.setDarkMode(it) }
                    )
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)

                Text("Automatización & Sensores", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyBlue)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Asistencia Automática (Geofencing)", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text("Registra la asistencia automáticamente usando FusedLocation cuando entras al campus", fontSize = 11.sp, color = SlateGray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = autoCheckinEnabled,
                        onCheckedChange = { viewModel.setAutoCheckinEnabled(it) }
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Modo Silencio Inteligente", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text("Silencia el teléfono automáticamente cuando estás en horario de clase dentro del campus", fontSize = 11.sp, color = SlateGray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    val notificationManager = remember { context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager }
                    val isPolicyGranted = remember { notificationManager.isNotificationPolicyAccessGranted }
                    Switch(
                        checked = smartSilenceEnabled,
                        onCheckedChange = { isChecked ->
                            if (isChecked && !isPolicyGranted) {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                                context.startActivity(intent)
                                android.widget.Toast.makeText(context, "Concede el permiso de 'No molestar' para usar esta función.", android.widget.Toast.LENGTH_LONG).show()
                            } else {
                                viewModel.setSmartSilenceEnabled(isChecked)
                            }
                        }
                    )
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)

                // 2. Backup & Restore Configuration
                Text("Respaldo y Restauración", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyBlue)
                Text("Puedes exportar tus datos académicos (clases, notas, asistencias) para moverlos de dispositivo o reinstalar la app.", fontSize = 11.sp, color = SlateGray)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val backupStr = viewModel.exportBackup()
                            if (backupStr.isNotEmpty()) {
                                clipboardManager.setText(androidx.compose.ui.text.buildAnnotatedString { append(backupStr) })
                                successMessage = "Copiado al portapapeles"
                                errorMessage = null
                            } else {
                                errorMessage = "No se pudo generar el respaldo"
                                successMessage = null
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Exportar JSON", fontSize = 12.sp)
                    }
                }

                if (successMessage != null) {
                    Text(successMessage!!, color = DarkGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }

                OutlinedTextField(
                    value = importText,
                    onValueChange = { importText = it; errorMessage = null; successMessage = null },
                    placeholder = { Text("Pega el JSON de tu respaldo aquí...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                )

                Button(
                    onClick = {
                        if (importText.trim().isEmpty()) {
                            errorMessage = "Por favor pega un JSON válido primero."
                            successMessage = null
                            return@Button
                        }
                        viewModel.importBackup(
                            importText,
                            onSuccess = {
                                successMessage = "¡Datos restaurados con éxito!"
                                errorMessage = null
                                importText = ""
                            },
                            onError = { err ->
                                errorMessage = "Error: $err"
                                successMessage = null
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    Text("Importar y Restaurar", fontSize = 12.sp)
                }

                if (errorMessage != null) {
                    Text(errorMessage!!, color = ProRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)

                // Report Error section
                Text("Soporte y Ayuda", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyBlue)
                Text("¿Encontraste algún problema o bug? Envía un reporte detallado a nuestro equipo de soporte.", fontSize = 11.sp, color = SlateGray)
                Button(
                    onClick = {
                        onDismiss()
                        onReportError()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    Text("Reportar un Error", fontSize = 12.sp)
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)

                // 3. System Reset
                Text("Zona Peligrosa", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Terracotta)
                Button(
                    onClick = { showResetConfirmation = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Restablecer todos los datos")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = { Text("¿Restablecer todo?", color = Terracotta, fontWeight = FontWeight.Bold) },
            text = { Text("Esta acción es irreversible y borrará todo tu historial académico, notas, asistencias, rutas e insignias.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showResetConfirmation = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
                ) { Text("SÍ, REINICIAR") }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) { Text("CANCELAR") }
            }
        )
    }
}

@Composable
fun PdfImportDialog(viewModel: UniBuddyViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var parsedSubjectsToConfirm by remember { mutableStateOf<List<com.aistudio.unibuddy.qywvsp.data.HistorialParser.ParsedSubject>>(emptyList()) }
    
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            isProcessing = true
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        val reader = com.itextpdf.text.pdf.PdfReader(inputStream)
                        val numberOfPages = reader.numberOfPages
                        val textBuilder = StringBuilder()
                        for (i in 1..numberOfPages) {
                            val pageText = com.itextpdf.text.pdf.parser.PdfTextExtractor.getTextFromPage(reader, i)
                            textBuilder.append(pageText).append("\n")
                        }
                        val text = textBuilder.toString()
                        reader.close()
                        inputStream.close()
                        
                        val parser = com.aistudio.unibuddy.qywvsp.data.HistorialParser()
                        val parsedSubjects = parser.parsePdfText(text)
                        
                        if (parsedSubjects.isNotEmpty()) {
                            val uni = viewModel.userUniversity.value
                            val career = viewModel.career.value
                            val staticSubjects = com.aistudio.unibuddy.qywvsp.data.CurriculumData.getSubjectsFor(
                                uni.ifEmpty { "UNI" },
                                career.ifEmpty { "Ing. Industrial" }
                            )
                            parsedSubjects.forEach { parsed ->
                                var matched = staticSubjects.find {
                                    it.code.equals(parsed.code, ignoreCase = true) ||
                                    it.name.equals(parsed.name, ignoreCase = true)
                                }
                                if (matched == null) {
                                    matched = staticSubjects.find {
                                        com.aistudio.unibuddy.qywvsp.data.FuzzyMatch.isSimilar(it.name, parsed.name)
                                    }
                                }
                                parsed.matchedCode = matched?.code
                                parsed.matchedName = matched?.name
                            }
                            parsedSubjectsToConfirm = parsedSubjects
                        } else {
                            resultMessage = "No se encontraron registros académicos con el formato estándar en este PDF."
                        }
                    } else {
                        resultMessage = "Error al abrir el archivo PDF."
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    resultMessage = "Error al procesar el PDF: ${e.message}"
                } finally {
                    isProcessing = false
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Importar Historial (PDF)", fontWeight = FontWeight.Bold, color = NavyBlue) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (parsedSubjectsToConfirm.isNotEmpty()) {
                    Text("Se capturaron ${parsedSubjectsToConfirm.size} materias. Confirma y ajusta:", fontWeight = FontWeight.Bold, color = DarkGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        itemsIndexed(parsedSubjectsToConfirm) { index, sub ->
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Bone)) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Extraído: ${sub.name} (${sub.code})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyBlue)
                                    Text("Nota: ${sub.grade} | ${sub.status.name}", fontSize = 11.sp, color = SlateGray)
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(if (sub.matchedName != null) "Emparejado con: ${sub.matchedName}" else "Sin coincidencia en pensum", 
                                        color = if (sub.matchedName != null) DarkGreen else Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = sub.professorName,
                                        onValueChange = { 
                                            val newList = parsedSubjectsToConfirm.toMutableList()
                                            newList[index] = sub.copy(professorName = it)
                                            parsedSubjectsToConfirm = newList
                                        },
                                        label = { Text("Profesor (Opcional)", fontSize = 10.sp) },
                                        modifier = Modifier.fillMaxWidth().height(50.dp),
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text("Puedes importar tu historial de notas oficial de la universidad para actualizar tu perfil y estadísticas.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    if (isProcessing) {
                        CircularProgressIndicator(color = ProBlue, modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    resultMessage?.let {
                        Text(it, color = if (it.startsWith("Error") || it.startsWith("No")) Color.Red else DarkGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            if (parsedSubjectsToConfirm.isNotEmpty()) {
                Button(
                    onClick = {
                        viewModel.importAcademicRecords(parsedSubjectsToConfirm)
                        resultMessage = "¡Guardado exitosamente!"
                        parsedSubjectsToConfirm = emptyList()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ProBlue)
                ) {
                    Text("Confirmar y Guardar", color = Color.White)
                }
            } else {
                Button(
                    onClick = { launcher.launch("application/pdf") },
                    colors = ButtonDefaults.buttonColors(containerColor = ProBlue),
                    enabled = !isProcessing
                ) {
                    Text("Seleccionar PDF", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (parsedSubjectsToConfirm.isNotEmpty()) {
                    parsedSubjectsToConfirm = emptyList()
                    resultMessage = null
                } else {
                    onDismiss()
                }
            }) {
                Text(if (parsedSubjectsToConfirm.isNotEmpty()) "Cancelar" else "Cerrar", color = SlateGray)
            }
        }
    )
}

@Suppress("DEPRECATION")
@Composable
fun ErrorReportDialog(viewModel: UniBuddyViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSending by remember { mutableStateOf(false) }
    var resultMsg by remember { mutableStateOf<String?>(null) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reporte de Errores Automático", fontWeight = FontWeight.Bold, color = NavyBlue) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (resultMsg != null) {
                    Text(resultMsg!!, fontSize = 14.sp, color = DarkGreen, fontWeight = FontWeight.Bold)
                } else {
                    Text("Se enviará el registro de errores directamente al bot de Telegram configurado, sin que tengas que abrir ninguna app.", fontSize = 14.sp)
                    if (isSending) {
                        CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp).align(Alignment.CenterHorizontally))
                    }
                }
            }
        },
        confirmButton = {
            if (resultMsg == null) {
                Button(
                    onClick = {
                        isSending = true
                        val logData = viewModel.getSystemLog()
                        
                        // Enviar vía OkHttp a Telegram
                        scope.launch(Dispatchers.IO) {
                            try {
                                val botToken = "8901231010:AAFViGBLQ4H7hEEm0NTmIry-OrPDIOVDh2Y"
                                val chatId = "7504800067"
                                val url = "https://api.telegram.org/bot${botToken}/sendMessage"
                                
                                val client = okhttp3.OkHttpClient()
                                val jsonBody = "{\"chat_id\": \"$chatId\", \"text\": \"$logData\"}"
                                
                                val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())
                                
                                val request = okhttp3.Request.Builder().url(url).post(requestBody).build()
                                val response = client.newCall(request).execute()
                                
                                kotlinx.coroutines.withContext(Dispatchers.Main) {
                                    isSending = false
                                    if (response.isSuccessful) {
                                        resultMsg = "¡Reporte enviado con éxito al bot!"
                                    } else {
                                        resultMsg = "Fallo al enviar. Verifica tu Chat ID. Code: ${response.code}"
                                    }
                                }
                            } catch (e: Exception) {
                                kotlinx.coroutines.withContext(Dispatchers.Main) {
                                    isSending = false
                                    resultMsg = "Error de red: ${e.message}"
                                }
                            }
                        }
                    },
                    enabled = !isSending,
                    colors = ButtonDefaults.buttonColors(containerColor = ProBlue)
                ) {
                    Text(if (isSending) "Enviando..." else "Enviar al Bot", color = Color.White)
                }
            } else {
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)) {
                    Text("Ok", color = Color.White)
                }
            }
        },
        dismissButton = {
            if (!isSending && resultMsg == null) {
                TextButton(onClick = onDismiss) { Text("Cancelar", color = SlateGray) }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemesterSettingsDialog(viewModel: UniBuddyViewModel, onDismiss: () -> Unit) {
    val semesterStartDate by viewModel.semesterStartDate.collectAsStateWithLifecycle()
    val currentWeek by viewModel.currentWeekOfSemester.collectAsStateWithLifecycle()
    val defaultExamPercent by viewModel.defaultExamPercentage.collectAsStateWithLifecycle()
    val defaultTestPercent by viewModel.defaultTestPercentage.collectAsStateWithLifecycle()

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val initialDateStr = semesterStartDate?.let { dateFormatter.format(java.util.Date(it)) } ?: "No establecida"

    var selectedDateStr by remember { mutableStateOf(initialDateStr) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    var examPercentInput by remember { mutableStateOf(defaultExamPercent.toInt().toString()) }
    var testPercentInput by remember { mutableStateOf(defaultTestPercent.toInt().toString()) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = semesterStartDate ?: Calendar.getInstance().timeInMillis
    )

    var showSuspiciousBuddyDialog by remember { mutableStateOf(false) }
    var failedSubjectsList by remember { mutableStateOf(emptyList<String>()) }
    val coroutineScope = rememberCoroutineScope()

    if (showDatePicker) {
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance().apply { timeInMillis = millis }
                        selectedDateStr = dateFormatter.format(cal.time)
                        viewModel.updateSemesterStartDate(millis)
                    }
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            androidx.compose.material3.DatePicker(state = datePickerState)
        }
    }

    if (showSuspiciousBuddyDialog) {
        AlertDialog(
            onDismissRequest = { showSuspiciousBuddyDialog = false },
            title = { Text("¿Pasaste con esas faltas?", fontWeight = FontWeight.Bold, color = Terracotta) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "UniBuddy te mira sospechoso...",
                        fontWeight = FontWeight.Bold,
                        color = SlateGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Excediste tu margen de faltas en: ${failedSubjectsList.joinToString(", ")}. Si la dejas así, reprobarás automáticamente. ¿De verdad el profe te pasó?",
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.endSemester(forcePassAbsences = true)
                        showSuspiciousBuddyDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    Text("Sí, ¡me salvaron!", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        viewModel.endSemester(forcePassAbsences = false)
                        showSuspiciousBuddyDialog = false
                        onDismiss()
                    }
                ) {
                    Text("No, la reprobé :(", color = Terracotta)
                }
            }
        )
    }

    var showHolidayPicker by remember { mutableStateOf(false) }
    var showAdvancedAdjuster by remember { mutableStateOf(false) }
    val holidayPickerState = rememberDatePickerState(
        initialSelectedDateMillis = Calendar.getInstance().timeInMillis
    )

    if (showHolidayPicker) {
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showHolidayPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showHolidayPicker = false
                    holidayPickerState.selectedDateMillis?.let { millis ->
                        viewModel.addCustomHoliday(millis)
                    }
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHolidayPicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            androidx.compose.material3.DatePicker(state = holidayPickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustes del Semestre", fontWeight = FontWeight.Bold, color = NavyBlue) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Calendario Académico (Nicaragua)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyBlue)
                
                // Semester Start Date
                OutlinedTextField(
                    value = selectedDateStr,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha de inicio del semestre") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Seleccionar Fecha", tint = NavyBlue)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyBlue, focusedLabelColor = NavyBlue)
                )

                Text(
                    text = "Semana actual calculada: ${if (currentWeek > 0) "Semana $currentWeek" else "Vacaciones"}\n" +
                           "*(Feriados de Nicaragua como Semana Santa y Fiestas Patrias son descontados automáticamente del conteo lectivo).*",
                    fontSize = 11.sp,
                    color = SlateGray,
                    fontWeight = FontWeight.Medium
                )

                Button(
                    onClick = { showAdvancedAdjuster = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ProBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditCalendar,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ajustar Semana y Paridad (Avanzado)", color = Color.White, fontSize = 12.sp)
                }

                if (showAdvancedAdjuster) {
                    AcademicCalendarAdjusterDialog(
                        viewModel = viewModel,
                        onDismiss = { showAdvancedAdjuster = false }
                    )
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)

                Text("Feriados y Días Libres", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyBlue)
                Text("Agrega manualmente días libres que no estén en el calendario nacional (ej. aniversario de la universidad o asueto local).", fontSize = 11.sp, color = SlateGray)
                
                Button(
                    onClick = { showHolidayPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue.copy(alpha = 0.8f))
                ) {
                    Text("Agregar Día Libre", color = Color.White, fontSize = 12.sp)
                }
                
                val customHols by viewModel.customHolidays.collectAsStateWithLifecycle()
                if (customHols.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        customHols.forEach { ms ->
                            val cal = Calendar.getInstance().apply { timeInMillis = ms }
                            Row(
                                modifier = Modifier.fillMaxWidth().background(Bone, RoundedCornerShape(8.dp)).padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(dateFormatter.format(cal.time), fontSize = 13.sp, color = NavyBlue)
                                IconButton(onClick = { viewModel.removeCustomHoliday(ms) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Clear, contentDescription = "Eliminar", tint = Terracotta)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)

                Text("Valores por Defecto para Evaluaciones", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyBlue)
                Text("Configura los valores de puntaje estándar para exámenes y trabajos:", fontSize = 11.sp, color = SlateGray)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = examPercentInput,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() }
                            examPercentInput = filtered
                            filtered.toDoubleOrNull()?.let { viewModel.saveDefaultExamPercentage(it) }
                        },
                        label = { Text("Exámenes (pts)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyBlue, focusedLabelColor = NavyBlue)
                    )

                    OutlinedTextField(
                        value = testPercentInput,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() }
                            testPercentInput = filtered
                            filtered.toDoubleOrNull()?.let { viewModel.saveDefaultTestPercentage(it) }
                        },
                        label = { Text("Tareas (pts)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyBlue, focusedLabelColor = NavyBlue)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val failed = viewModel.checkFailedByAbsences().first()
                            if (failed.isNotEmpty()) {
                                failedSubjectsList = failed
                                showSuspiciousBuddyDialog = true
                            } else {
                                viewModel.endSemester(forcePassAbsences = false)
                                onDismiss()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Terminar Semestre Actual", color = Color.White)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Text("Guardar", color = Bone)
            }
        },
        containerColor = Color.White
    )
}

@Composable
fun FocusHistoryChart(viewModel: UniBuddyViewModel) {
    val sessionsHistoryJson by viewModel.focusSessionsHistoryJson.collectAsStateWithLifecycle()
    val sessions = remember(sessionsHistoryJson) {
        val list = mutableListOf<Pair<String, Int>>()
        try {
            val arr = org.json.JSONArray(sessionsHistoryJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(Pair(obj.getString("date"), obj.getInt("duration")))
            }
        } catch (e: Exception) {}
        list
    }

    if (sessions.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Text("Aún no hay historial de concentración.", modifier = Modifier.padding(16.dp), fontSize = 12.sp, color = SlateGray)
        }
        return
    }

    var selectedDay by remember { mutableStateOf<Pair<String, Int>?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Historial Visual de Concentración", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyBlue)
            Text("Presiona una barra para ver detalles", fontSize = 11.sp, color = SlateGray)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Group by day (taking only last 7 days)
            val aggregated = sessions.groupBy { it.first }.mapValues { it.value.sumOf { s -> s.second } }.toList().takeLast(7)
            val maxMins = aggregated.maxOfOrNull { it.second } ?: 1
            
            Row(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                aggregated.forEach { (date, mins) ->
                    val isSelected = selectedDay?.first == date
                    val heightRatio = (mins.toFloat() / maxMins).coerceIn(0.1f, 1f)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { selectedDay = Pair(date, mins) }
                            .padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "$mins",
                            fontSize = 10.sp,
                            color = if (isSelected) Amber else ProBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .fillMaxHeight(heightRatio)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(if (isSelected) Amber else ProBlue)
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) NavyBlue else Color.Transparent,
                                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(date.take(5), fontSize = 10.sp, color = if (isSelected) NavyBlue else SlateGray, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) // "dd/MM"
                    }
                }
            }

            selectedDay?.let { (date, mins) ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Día: $date", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyBlue)
                            Text("Estudio acumulado: $mins min.", fontSize = 11.sp, color = SlateGray)
                        }
                        IconButton(onClick = { selectedDay = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = SlateGray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}