package com.aistudio.unibuddy.qywvsp.ui.screens

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.CircleShape
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aistudio.unibuddy.qywvsp.ui.UniBuddyViewModel
import com.aistudio.unibuddy.qywvsp.ui.theme.BackgroundBone
import com.aistudio.unibuddy.qywvsp.ui.theme.NavyBlue
import com.aistudio.unibuddy.qywvsp.ui.theme.SlateGray
import com.aistudio.unibuddy.qywvsp.ui.theme.ProBlue
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemScreen(viewModel: UniBuddyViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showResetDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    // Backup State
    val lastLocalTime by viewModel.lastBackupLocalTime.collectAsStateWithLifecycle()
    val lastDriveTime by viewModel.lastBackupDriveTime.collectAsStateWithLifecycle()
    val backupIntervalDays by viewModel.backupIntervalDays.collectAsStateWithLifecycle()

    // Bug / Feedback State
    val bugReportsList by viewModel.bugReports.collectAsStateWithLifecycle()
    var bugDescription by remember { mutableStateOf("") }
    var bugScreenSelected by remember { mutableStateOf("Dashboard") }
    val screenOptions = listOf("Dashboard", "Asistencias", "Tareas y Notas", "Rutas", "Sistema", "Focus Mode")
    var isDropdownExpanded by remember { mutableStateOf(false) }

    fun formatBackupTime(time: Long): String {
        if (time == 0L) return "Nunca"
        return SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(time))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sistema y Datos", fontWeight = FontWeight.Bold, color = NavyBlue) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = NavyBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundBone)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundBone
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Backup Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Respaldo y Restauración", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyBlue)
                    Text("Crea una copia de seguridad de todos tus datos académicos, notas, e historial.", fontSize = 12.sp, color = SlateGray)
                    
                    // Backup Info Timestamps
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BackgroundBone, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Último respaldo local: ${formatBackupTime(lastLocalTime)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                        Text("Último respaldo en Google Drive: ${formatBackupTime(lastDriveTime)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                    }

                    // Backup Warning Period Settings
                    Text("Configurar recordatorio de respaldo:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(7, 14, 30, 0).forEach { days ->
                            val label = if (days == 0) "Desactivado" else "${days} d"
                            val isSelected = backupIntervalDays == days
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) ProBlue else Color.LightGray.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.saveBackupIntervalDays(days) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.White else NavyBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val json = viewModel.exportBackup()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Respaldo UniBuddy", json)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Respaldo copiado al portapapeles", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ProBlue),
                        modifier = Modifier.fillMaxWidth().testTag("export_backup_button")
                    ) {
                        Text("Exportar (Copiar JSON)")
                    }

                    var importText by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        placeholder = { Text("Pega el JSON de respaldo aquí") },
                        modifier = Modifier.fillMaxWidth().height(80.dp).testTag("import_backup_input")
                    )

                    Button(
                        onClick = {
                            if (importText.isNotBlank()) {
                                viewModel.importBackup(
                                    importText,
                                    onSuccess = { Toast.makeText(context, "Restauración exitosa", Toast.LENGTH_SHORT).show() },
                                    onError = { Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show() }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Restaurar desde JSON")
                    }
                }
            }

            // Bugs / Feedback Reporting Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Reporte de Errores y Feedback", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyBlue)
                    Text("Cuéntanos sobre algún problema o sugerencia in-app para poder solucionarlo.", fontSize = 12.sp, color = SlateGray)

                    // Dropdown for Screen selection
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { isDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth().testTag("bug_screen_dropdown")
                        ) {
                            Text("Contexto: $bugScreenSelected")
                        }
                        DropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false }
                        ) {
                            screenOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        bugScreenSelected = option
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = bugDescription,
                        onValueChange = { bugDescription = it },
                        label = { Text("Descripción del reporte") },
                        placeholder = { Text("Explica detalladamente qué falló o qué sugieres") },
                        modifier = Modifier.fillMaxWidth().height(100.dp).testTag("bug_description_input")
                    )

                    Button(
                        onClick = {
                            if (bugDescription.isNotBlank()) {
                                viewModel.addBugReport(bugDescription, bugScreenSelected)
                                bugDescription = ""
                                Toast.makeText(context, "Reporte guardado localmente", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Por favor describe el problema", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ProBlue),
                        modifier = Modifier.fillMaxWidth().testTag("submit_bug_button")
                    ) {
                        Text("Registrar Reporte")
                    }

                    // Existing Bug Reports list
                    if (bugReportsList.isNotEmpty()) {
                        Divider()
                        Text("Reportes Pendientes de Envío (${bugReportsList.size}):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyBlue)
                        
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            bugReportsList.forEach { report ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(BackgroundBone, RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("[${report.screenName}]", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = ProBlue)
                                        Text(report.description, fontSize = 12.sp, color = NavyBlue)
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteBugReport(report.id) },
                                        modifier = Modifier.testTag("delete_bug_${report.id}")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Gray)
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val reportsText = viewModel.getBugReportsExportText(bugReportsList)
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Reportes UniBuddy", reportsText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copiado al portapapeles", Toast.LENGTH_SHORT).show()
                                
                                try {
                                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_TEXT, reportsText)
                                    }
                                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Enviar Reportes"))
                                } catch (e: Exception) {}
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                            modifier = Modifier.fillMaxWidth().testTag("share_bugs_button")
                        ) {
                            Text("Exportar y Compartir Reportes")
                        }
                    }
                }
            }

            // PDF Import Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Importar Horario", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyBlue)
                    Text("Importa tu horario o pensum desde un archivo PDF de la universidad.", fontSize = 12.sp, color = SlateGray)
                    
                    Button(
                        onClick = { showImportDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Abrir Asistente de Importación")
                    }
                }
            }

            // Danger Zone Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F0)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Zona de Peligro", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Red)
                    }
                    Text("Borrar todos los datos eliminará tu historial, pensum, notas e insignias permanentemente.", fontSize = 12.sp, color = Color.Red.copy(alpha = 0.8f))
                    
                    Button(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reiniciar todos los datos", color = Color.White)
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("¿Estás seguro?") },
            text = { Text("Esta acción no se puede deshacer. Todos tus datos serán eliminados.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllData()
                    showResetDialog = false
                    Toast.makeText(context, "Datos eliminados", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Sí, borrar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showImportDialog) {
        PdfImportDialog(onDismiss = { showImportDialog = false })
    }
}

@Composable
fun PdfImportDialog(onDismiss: () -> Unit) {
    var step by remember { mutableIntStateOf(1) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Asistente de Importación PDF", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NavyBlue)
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    (1..3).forEach { i ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(if (i <= step) ProBlue else Color.LightGray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$i", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        if (i < 3) {
                            Box(modifier = Modifier.width(32.dp).height(2.dp).align(Alignment.CenterVertically).background(if (i < step) ProBlue else Color.LightGray))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                when (step) {
                    1 -> {
                        Text("Paso 1: Seleccionar Archivo", fontWeight = FontWeight.Bold, color = NavyBlue)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Busca el documento PDF oficial de tu universidad con tu horario.", fontSize = 12.sp, color = SlateGray, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { step = 2 }, colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)) {
                            Text("Seleccionar PDF (Simulado)")
                        }
                    }
                    2 -> {
                        Text("Paso 2: Extrayendo Datos...", fontWeight = FontWeight.Bold, color = NavyBlue)
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(color = ProBlue)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { step = 3 }, colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)) {
                            Text("Continuar")
                        }
                    }
                    3 -> {
                        Text("Paso 3: Confirmación", fontWeight = FontWeight.Bold, color = NavyBlue)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Se encontraron 5 materias y sus horarios. ¿Deseas agregarlas a tu semestre?", fontSize = 12.sp, color = SlateGray, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = ProBlue)) {
                            Text("Importar Clases")
                        }
                    }
                }
            }
        }
    }
}
