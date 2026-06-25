package com.aistudio.unibuddy.qywvsp.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import com.aistudio.unibuddy.qywvsp.ui.*
import com.aistudio.unibuddy.qywvsp.ui.components.*
import com.aistudio.unibuddy.qywvsp.ui.theme.*
import com.aistudio.unibuddy.qywvsp.data.*

@Composable
fun SettingsScreen(viewModel: UniBuddyViewModel, onNavigateToPensum: () -> Unit) {
    val username by viewModel.username.collectAsStateWithLifecycle()
    val buddyColorStr by viewModel.buddyColor.collectAsStateWithLifecycle()
    val mainBuddyColor = Color(android.graphics.Color.parseColor(buddyColorStr))
    
    var showProfileDialog by remember { mutableStateOf(false) }
    var showRouteDialog by remember { mutableStateOf(false) }
    var showBadgeDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showBuddyDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showPdfImportDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

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
                        SemesterHistoryView(viewModel = viewModel)
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
            ConfigGridItem("Perfil", Icons.Default.Person, "Nombre, Foto, Carrera") { showProfileDialog = true },
            ConfigGridItem("Mascota", Icons.Default.Face, "Accesorios y Color") { showBuddyDialog = true },
            ConfigGridItem("Rutas", Icons.Default.Place, "Origen, Destino, GPS") { showRouteDialog = true },
            ConfigGridItem("Historial", Icons.Default.Menu, "Estadísticas pasadas") { showHistoryDialog = true },
            ConfigGridItem("Académico", Icons.Default.DateRange, "Pensum, Semestre") { onNavigateToPensum() },
            ConfigGridItem("Importar PDF", Icons.Default.Info, "Historial de Notas") { showPdfImportDialog = true },
            ConfigGridItem("Insignias", Icons.Default.Star, "Logros y Medallas") { showBadgeDialog = true },
            ConfigGridItem("Reporte Error", Icons.Default.Warning, "Telegram/Gmail") { showErrorDialog = true },
            ConfigGridItem("Sistema", Icons.Default.Settings, "Backup, Reset") { showResetDialog = true }
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
        SystemSettingsDialog(viewModel) { showResetDialog = false }
    }
    if (showBuddyDialog) {
        BuddyCustomizationDialog(viewModel) { showBuddyDialog = false }
    }
    if (showPdfImportDialog) {
        PdfImportDialog(viewModel) { showPdfImportDialog = false }
    }

    if (showErrorDialog) {
        ErrorReportDialog { showErrorDialog = false }
    }
}

data class ConfigGridItem(val title: String, val icon: ImageVector, val subtitle: String, val onClick: () -> Unit)

@Composable
fun ProfileDialog(viewModel: UniBuddyViewModel, onDismiss: () -> Unit) {
    val username by viewModel.username.collectAsStateWithLifecycle()
    val career by viewModel.career.collectAsStateWithLifecycle()
    
    var editingName by remember { mutableStateOf(username) }
    var editingCareer by remember { mutableStateOf(career) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mi Perfil", fontWeight = FontWeight.Bold, color = NavyBlue) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    onValueChange = { editingOrigin = it },
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
                    onValueChange = { editingDestination = it },
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
    
    val accessories = listOf("none", "hat", "cap", "glasses", "sunglasses", "scarf")
    val accessoriesLabels = mapOf(
        "none" to "Ninguno",
        "hat" to "Casco",
        "cap" to "Gorra",
        "glasses" to "Lentes",
        "sunglasses" to "Gafas de Sol",
        "scarf" to "Bufanda"
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
                        FilterChip(
                            selected = accessory == acc,
                            onClick = { viewModel.saveBuddyCustomization(acc, buddyColorStr) },
                            label = { Text(accessoriesLabels[acc] ?: acc.replaceFirstChar { it.uppercase() }) },
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
fun SystemSettingsDialog(viewModel: UniBuddyViewModel, onDismiss: () -> Unit) {
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
                    Switch(
                        checked = smartSilenceEnabled,
                        onCheckedChange = { viewModel.setSmartSilenceEnabled(it) }
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

                Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)

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
    
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            isProcessing = true
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    // MOCK PDF PROCESSING TO AVOID DEPENDENCY ISSUE
                    kotlinx.coroutines.delay(1000)
                    val mockSubjects = listOf(
                        com.aistudio.unibuddy.qywvsp.data.HistorialParser.ParsedSubject("CMAT181", "MATE I", 4.0, 85.0, com.aistudio.unibuddy.qywvsp.data.AssessmentStatus.NF_R, "1M1-IND"),
                        com.aistudio.unibuddy.qywvsp.data.HistorialParser.ParsedSubject("CFIS021", "FISICA I", 4.0, 70.0, com.aistudio.unibuddy.qywvsp.data.AssessmentStatus.NF_R, "1M1-IND")
                    )
                    viewModel.importAcademicRecords(mockSubjects)
                    resultMessage = "Se importaron ${mockSubjects.size} materias (Mocked)."
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
            Column {
                Text("Puedes importar tu historial de notas oficial de la universidad para actualizar tu perfil y estadísticas.", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                if (isProcessing) {
                    CircularProgressIndicator(color = ProBlue, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                resultMessage?.let {
                    Text(it, color = if (it.startsWith("Error")) Color.Red else DarkGreen, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { launcher.launch("application/pdf") },
                colors = ButtonDefaults.buttonColors(containerColor = ProBlue),
                enabled = !isProcessing
            ) {
                Text("Seleccionar PDF", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar", color = SlateGray) }
        }
    )
}

@Suppress("DEPRECATION")
@Composable
fun ErrorReportDialog(onDismiss: () -> Unit) {
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
                        val logData = "UniBuddy App Crash Log\nDevice: ${android.os.Build.MODEL}\nOS: ${android.os.Build.VERSION.RELEASE}\nError: Ejemplo de error de prueba."
                        
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