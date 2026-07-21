package com.aistudio.unibuddy.qywvsp.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.aistudio.unibuddy.qywvsp.ui.UniBuddyViewModel
import com.aistudio.unibuddy.qywvsp.ui.components.BuddyMascot
import com.aistudio.unibuddy.qywvsp.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: UniBuddyViewModel, onBack: () -> Unit) {
    val username by viewModel.username.collectAsStateWithLifecycle()
    val career by viewModel.career.collectAsStateWithLifecycle()
    val photoUri by viewModel.profilePhotoUri.collectAsStateWithLifecycle()
    val userUniversity by viewModel.userUniversity.collectAsStateWithLifecycle()
    val destination by viewModel.destination.collectAsStateWithLifecycle()
    
    val accessory by viewModel.buddyAccessory.collectAsStateWithLifecycle()
    val buddyColorStr by viewModel.buddyColor.collectAsStateWithLifecycle()
    val buddyPose by viewModel.buddyPose.collectAsStateWithLifecycle()
    val badges by viewModel.badges.collectAsStateWithLifecycle()

    var editingName by remember(username) { mutableStateOf(username) }
    var editingCareer by remember(career) { mutableStateOf(career) }
    var editingUniversity by remember(userUniversity) { mutableStateOf(userUniversity) }
    var editingCampus by remember(destination) { mutableStateOf(destination) }

    val photoLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            viewModel.saveProfilePhoto(uri.toString())
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val accessoryRequirements = mapOf(
        "hat" to "Primeros Pasos",
        "cap" to "Estudiante Responsable",
        "glasses" to "Concentración Total",
        "sunglasses" to "En el Top"
    )

    val isAccessoryUnlocked: (String) -> Boolean = { acc ->
        if (acc == "none" || acc == "scarf" || acc == "sombrero_nica") true
        else {
            val reqBadge = accessoryRequirements[acc]
            if (reqBadge != null) {
                badges.find { it.name == reqBadge }?.isUnlocked == true
            } else {
                true
            }
        }
    }

    val accessoriesLabels = mapOf(
        "none" to "Ninguno", "hat" to "Casco", "cap" to "Gorra", 
        "glasses" to "Lentes", "sunglasses" to "Gafas de Sol", 
        "scarf" to "Bufanda", "sombrero_nica" to "Sombrero Pita"
    )
    val colors = listOf("#0F172A", "#4CAF50", "#2196F3", "#F44336", "#9C27B0", "#FF9800", "#607D8B")
    val poses = listOf("idle", "greeting", "working", "sleeping", "celebrating", "exam")
    val poseLabels = mapOf(
        "idle" to "Relajado", "greeting" to "Saludando", "working" to "Estudiando", 
        "sleeping" to "Durmiendo", "celebrating" to "Celebrando", "exam" to "Concentrado"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil y Personalización") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
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
            // Sección Perfil
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Información Personal", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyBlue)
                    
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
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
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = NavyBlue, modifier = Modifier.size(32.dp))
                            }
                        }
                        OutlinedTextField(
                            value = editingName,
                            onValueChange = { editingName = it },
                            label = { Text("Nombre") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = editingCareer,
                        onValueChange = { editingCareer = it },
                        label = { Text("Carrera Universitaria") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editingUniversity,
                            onValueChange = { editingUniversity = it },
                            label = { Text("Universidad") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editingCampus,
                            onValueChange = { editingCampus = it },
                            label = { Text("Campus/Sede") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.saveUsername(editingName)
                            viewModel.saveCareer(editingCareer)
                            viewModel.saveUserUniversity(editingUniversity)
                            val currentOrigin = viewModel.origin.value
                            viewModel.saveRoute(currentOrigin, editingCampus)
                            coroutineScope.launch { snackbarHostState.showSnackbar("Perfil guardado") }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Guardar Perfil", color = Bone)
                    }
                }
            }

            // Sección Mascota
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Personalizar Buddy", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyBlue)
                    
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        BuddyMascot(
                            modifier = Modifier.size(120.dp),
                            pose = buddyPose,
                            mainColor = Color(android.graphics.Color.parseColor(buddyColorStr)),
                            accessory = accessory
                        )
                    }

                    Text("Gesto / Postura", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyBlue)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(poses) { p ->
                            FilterChip(
                                selected = buddyPose == p,
                                onClick = { 
                                    viewModel.saveBuddyPose(p) 
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Postura guardada") }
                                },
                                label = { Text(poseLabels[p] ?: p) },
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
                                        width = if (buddyColorStr == colorHex) 3.dp else 1.dp,
                                        color = if (buddyColorStr == colorHex) NavyBlue else Color.LightGray,
                                        shape = CircleShape
                                    )
                                    .clickable { 
                                        viewModel.saveBuddyCustomization(accessory, colorHex)
                                        coroutineScope.launch { snackbarHostState.showSnackbar("Color guardado") }
                                    }
                            )
                        }
                    }

                    Text("Accesorio", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyBlue)
                    
                    val accessories = accessoriesLabels.keys.toList()
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        accessories.chunked(3).forEach { rowItems ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                rowItems.forEach { acc ->
                                    val unlocked = isAccessoryUnlocked(acc)
                                    FilterChip(
                                        selected = accessory == acc && unlocked,
                                        onClick = { 
                                            if (unlocked) {
                                                viewModel.saveBuddyCustomization(acc, buddyColorStr)
                                                coroutineScope.launch { snackbarHostState.showSnackbar("Accesorio guardado") }
                                            }
                                        },
                                        label = { 
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (!unlocked) {
                                                    Icon(Icons.Default.Lock, contentDescription = "Locked", modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                                Text(accessoriesLabels[acc] ?: acc, fontSize = 12.sp)
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = NavyBlue.copy(alpha = 0.15f),
                                            selectedLabelColor = NavyBlue
                                        )
                                    )
                                }
                                // fill empty space if row has less than 3
                                repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                            }
                        }
                    }
                    
                    // Show requirement for locked ones if tapped? The prompt says "marcando bloqueados y qué logro los desbloquea"
                    // Let's add a small text area at the bottom showing what is needed.
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Requisitos para desbloquear:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyBlue)
                            accessoryRequirements.forEach { (acc, badge) ->
                                Text("• ${accessoriesLabels[acc]}: Requiere la insignia '$badge'", fontSize = 11.sp, color = SlateGray)
                            }
                        }
                    }

                }
            }
        }
    }
}
