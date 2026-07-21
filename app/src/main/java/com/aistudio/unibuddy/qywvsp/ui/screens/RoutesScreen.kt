package com.aistudio.unibuddy.qywvsp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aistudio.unibuddy.qywvsp.ui.UniBuddyViewModel
import com.aistudio.unibuddy.qywvsp.ui.theme.BackgroundBone
import com.aistudio.unibuddy.qywvsp.ui.theme.NavyBlue
import com.aistudio.unibuddy.qywvsp.ui.theme.SlateGray
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutesScreen(viewModel: UniBuddyViewModel, onBack: () -> Unit) {
    val homeAddress by viewModel.homeAddress.collectAsStateWithLifecycle()
    val workAddress by viewModel.workAddress.collectAsStateWithLifecycle()
    val origin by viewModel.origin.collectAsStateWithLifecycle()
    val destination by viewModel.destination.collectAsStateWithLifecycle()
    val baseTravelTime by viewModel.baseTravelTime.collectAsStateWithLifecycle()

    var editHome by remember(homeAddress) { mutableStateOf(homeAddress) }
    var editWork by remember(workAddress) { mutableStateOf(workAddress) }
    var editOrigin by remember(origin) { mutableStateOf(origin) }
    var editDest by remember(destination) { mutableStateOf(destination) }
    var editTime by remember(baseTravelTime) { mutableStateOf(baseTravelTime.toString()) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Rutas", fontWeight = FontWeight.Bold, color = NavyBlue) },
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Direcciones Frecuentes", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyBlue)
                    
                    OutlinedTextField(
                        value = editHome,
                        onValueChange = { editHome = it },
                        label = { Text("Dirección de Casa") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = SlateGray) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = editWork,
                        onValueChange = { editWork = it },
                        label = { Text("Dirección de Trabajo") },
                        leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = SlateGray) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { 
                            viewModel.saveHomeWorkAddresses(editHome, editWork)
                            coroutineScope.launch { snackbarHostState.showSnackbar("Direcciones guardadas") }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Guardar Direcciones")
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Ruta Diaria Principal", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyBlue)
                    Text("Esta ruta se usa para calcular tu tiempo de viaje al campus todos los días.", fontSize = 12.sp, color = SlateGray)
                    
                    OutlinedTextField(
                        value = editOrigin,
                        onValueChange = { editOrigin = it },
                        label = { Text("Punto de Origen (Ej: Casa, Trabajo)") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = SlateGray) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = editDest,
                        onValueChange = { editDest = it },
                        label = { Text("Destino (Campus Universitario)") },
                        leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null, tint = SlateGray) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editTime,
                        onValueChange = { if (it.all { char -> char.isDigit() }) editTime = it },
                        label = { Text("Tiempo estimado promedio (minutos)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { 
                            viewModel.saveRoute(editOrigin, editDest)
                            val t = editTime.toIntOrNull() ?: 25
                            viewModel.saveBaseTravelTime(t)
                            coroutineScope.launch { snackbarHostState.showSnackbar("Ruta guardada") }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Actualizar Ruta")
                    }
                }
            }
        }
    }
}
