package com.aistudio.unibuddy.qywvsp.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.aistudio.unibuddy.qywvsp.data.ClassSessionDetails
import com.aistudio.unibuddy.qywvsp.data.CurriculumData
import com.aistudio.unibuddy.qywvsp.data.StaticPensumSubject
import com.aistudio.unibuddy.qywvsp.ui.UniBuddyViewModel
import com.aistudio.unibuddy.qywvsp.ui.getSubjectInitials
import com.aistudio.unibuddy.qywvsp.ui.components.BuddyMascot
import com.aistudio.unibuddy.qywvsp.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@Composable
fun OsmMapView(modifier: Modifier = Modifier, locationEnabled: Boolean) {
    val context = LocalContext.current
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }
    
    DisposableEffect(locationEnabled, mapViewRef) {
        val map = mapViewRef
        var myLocationOverlay: org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay? = null
        if (map != null && locationEnabled) {
            val existing = map.overlays.filterIsInstance<org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay>().firstOrNull()
            if (existing == null) {
                val overlay = org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay(
                    org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider(context), map
                )
                overlay.enableMyLocation()
                overlay.enableFollowLocation()
                map.overlays.add(overlay)
                map.invalidate()
                myLocationOverlay = overlay
            } else {
                existing.enableMyLocation()
                existing.enableFollowLocation()
                myLocationOverlay = existing
            }
        }
        onDispose {
            myLocationOverlay?.disableMyLocation()
            myLocationOverlay?.disableFollowLocation()
        }
    }
    
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                val mapController = controller
                mapController.setZoom(15.0)
                val startPoint = GeoPoint(12.1364, -86.2514) 
                mapController.setCenter(startPoint)
                mapViewRef = this
            }
        },
        update = { }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun OnboardingScreen(viewModel: UniBuddyViewModel, onFinished: () -> Unit) {
    var step by rememberSaveable { mutableStateOf(1) }
    val totalSteps = 5
    
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    // Form Inputs
    var nameInput by rememberSaveable { mutableStateOf("") }
    var uniInput by rememberSaveable { mutableStateOf("") }
    var careerInput by rememberSaveable { mutableStateOf("") }
    var selectedSemester by rememberSaveable { mutableStateOf(1) }
    
    // We store the enrolled subjects in a map or list
    val enrolledSubjects = remember { mutableStateListOf<StaticPensumSubject>() }
    // We store the schedule sessions of enrolled subjects: Map of Subject Name -> List of Sessions
    val subjectSessions = remember { mutableStateMapOf<String, List<ClassSessionDetails>>() }
    
    var originInput by rememberSaveable { mutableStateOf("") }
    var destInput by rememberSaveable { mutableStateOf("") }
    var baseTravelMinutes by rememberSaveable { mutableStateOf("25") }

    // Auto-update enrolled subjects when selectedSemester, uniInput, or careerInput changes
    LaunchedEffect(selectedSemester, uniInput, careerInput) {
        enrolledSubjects.clear()
        val matchingCurriculum = CurriculumData.getSubjectsFor(uniInput, careerInput).filter { it.semester == selectedSemester }
        enrolledSubjects.addAll(matchingCurriculum)
        
        subjectSessions.clear()
        // No auto-populate, let user define.
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBone)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress dots
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                for (i in 1..totalSteps) {
                    val isActive = step == i
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(6.dp)
                            .width(if (isActive) 24.dp else 6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isActive) NavyBlue else SlateGray.copy(alpha = 0.3f))
                    )
                }
            }

            // Back button if not step 1
            if (step > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(
                        onClick = { step-- },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = NavyBlue
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            when (step) {
                1 -> {
                    Step1Welcome(
                        nameInput = nameInput,
                        onNameChange = { nameInput = it },
                        onNext = {
                            viewModel.saveUsername(nameInput.trim())
                            step = 2
                        }
                    )
                }
                2 -> {
                    Step2University(
                        uniInput = uniInput,
                        onUniChange = { uniInput = it },
                        careerInput = careerInput,
                        onCareerChange = { careerInput = it },
                        onNext = {
                            viewModel.saveUserUniversity(uniInput)
                            viewModel.saveCareer(careerInput)
                            step = 3
                        }
                    )
                }
                3 -> {
                    Step3SemesterAndSubjects(
                        selectedSemester = selectedSemester,
                        onSemesterChange = { selectedSemester = it },
                        enrolledSubjects = enrolledSubjects,
                        onNext = {
                            step = 4
                        }
                    )
                }
                4 -> {
                    Step4ScheduleConfig(
                        enrolledSubjects = enrolledSubjects,
                        subjectSessions = subjectSessions,
                        onNext = {
                            step = 5
                        }
                    )
                }
                5 -> {
                    Step5Route(
                        originInput = originInput,
                        onOriginChange = { originInput = it },
                        destInput = destInput,
                        onDestChange = { destInput = it },
                        baseTravelMinutes = baseTravelMinutes,
                        onTravelMinutesChange = { baseTravelMinutes = it },
                        locationPermissionsState = locationPermissionsState,
                        onFinish = {
                            // Save user settings
                            viewModel.saveRoute(originInput, destInput)
                            baseTravelMinutes.toIntOrNull()?.let { viewModel.saveBaseTravelTime(it) }
                            viewModel.saveBuddyPose("happy")
                            
                            // Save selected subjects with their configured sessions
                            val availableColors = listOf("#E3F2FD", "#E8F5E9", "#FFF3E0", "#F3E5F5", "#E0F7FA", "#FFEBEE", "#FFF9C4")
                            enrolledSubjects.forEachIndexed { index, sub ->
                                val sessions = subjectSessions[sub.name] ?: emptyList()
                                val computedSchedule = sessions.joinToString(", ") { it.day }
                                val computedClasses = sessions.size * 14
                                val assignedColor = availableColors[index % availableColors.size]
                                viewModel.addSubject(
                                    name = sub.name,
                                    schedule = computedSchedule,
                                    sessions = sessions,
                                    requiredAttendancePercent = 70,
                                    totalClasses = computedClasses,
                                    colorHex = assignedColor
                                )
                            }
                            
                            viewModel.updateOnboardingStatus(true)
                            onFinished()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun Step1Welcome(
    nameInput: String,
    onNameChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .sizeIn(maxHeight = 140.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        BuddyMascot(pose = "happy", modifier = Modifier.size(110.dp))
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "¡Bienvenido a UniBuddy!",
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        color = NavyBlue,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = "¿Cómo te llamas?",
        fontSize = 15.sp,
        color = SlateGray,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(24.dp))
    
    OutlinedTextField(
        value = nameInput,
        onValueChange = onNameChange,
        label = { Text("Tu nombre") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NavyBlue,
            focusedLabelColor = NavyBlue
        ),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done
        )
    )
    
    Spacer(modifier = Modifier.height(28.dp))
    
    Button(
        onClick = onNext,
        enabled = nameInput.isNotBlank(),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
    ) {
        Text("Siguiente", color = Bone, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(8.dp))
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun Step2University(
    uniInput: String,
    onUniChange: (String) -> Unit,
    careerInput: String,
    onCareerChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Text(
        text = "Detalles de tu Universidad",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = NavyBlue,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))
    
    Text(
        text = "Selecciona tu Universidad",
        fontSize = 14.sp,
        color = SlateGray,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    
    val universities = listOf("UNI", "UNAN", "UCA", "UAM", "Keiser", "UCC")
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .height(110.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(universities, key = { it }) { uni ->
            val isSelected = uniInput == uni
            val uniColor = if (isSelected) {
                when (uni) {
                    "UNI" -> Color(0xFF0F1E36)
                    "UNAN" -> Color(0xFF0038A8)
                    "UCA" -> Color(0xFF0056B3)
                    "UAM" -> Color(0xFF102A43)
                    "Keiser" -> Color(0xFF0B2240)
                    "UCC" -> Color(0xFF1E5F3B)
                    else -> NavyBlue
                }
            } else {
                Color.LightGray.copy(alpha = 0.3f)
            }
            
            Surface(
                onClick = { 
                    onUniChange(uni) 
                    onCareerChange("") 
                },
                shape = RoundedCornerShape(12.dp),
                color = uniColor,
                contentColor = if (isSelected) Bone else SlateGray,
                modifier = Modifier.height(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(uni, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Carrera",
        fontSize = 14.sp,
        color = SlateGray,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    
    val careers = when (uniInput) {
        "UNI" -> listOf("Ing. de Sistemas", "Ing. Civil", "Ing. Industrial", "Ing. Química", "Arquitectura")
        "UNAN" -> listOf("Medicina", "Derecho", "Psicología", "Ing. de Sistemas", "Administración")
        "UCA" -> listOf("Ing. de Sistemas", "Derecho", "Diseño Gráfico", "Administración", "Comunicación")
        "UAM" -> listOf("Medicina", "Derecho", "Odontología", "Relaciones Internacionales", "Ing. Industrial")
        "Keiser" -> listOf("Business Administration", "Software Engineering", "Management Info Systems", "Cybersecurity")
        "UCC" -> listOf("Medicina Veterinaria", "Diseño Gráfico", "Contaduría", "Ing. de Sistemas")
        else -> emptyList()
    }
    
    if (careers.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Color.LightGray.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Selecciona tu universidad arriba para ver las carreras que ofrece.",
                color = SlateGray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .height(140.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(careers, key = { it }) { career ->
                val isSelected = careerInput == career
                val activeColor = when (uniInput) {
                    "UNI" -> Color(0xFF0F1E36)
                    "UNAN" -> Color(0xFF0038A8)
                    "UCA" -> Color(0xFF0056B3)
                    "UAM" -> Color(0xFF102A43)
                    "Keiser" -> Color(0xFF0B2240)
                    "UCC" -> Color(0xFF1E5F3B)
                    else -> DarkGreen
                }
                
                Surface(
                    onClick = { onCareerChange(career) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) activeColor else Color.LightGray.copy(alpha = 0.3f),
                    contentColor = if (isSelected) Bone else SlateGray,
                    modifier = Modifier.height(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 4.dp)) {
                        Text(
                            text = career,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
    
    Spacer(modifier = Modifier.height(20.dp))
    
    Button(
        onClick = onNext,
        enabled = uniInput.isNotBlank() && careerInput.isNotBlank(),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = NavyBlue,
            disabledContainerColor = Color.LightGray.copy(alpha = 0.5f)
        )
    ) {
        Text("Siguiente", color = Bone, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(8.dp))
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun Step3SemesterAndSubjects(
    selectedSemester: Int,
    onSemesterChange: (Int) -> Unit,
    enrolledSubjects: List<StaticPensumSubject>,
    onNext: () -> Unit
) {
    Text(
        text = "Inscripción de Materias",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = NavyBlue,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Selecciona tu semestre actual y las materias que cursarás.",
        fontSize = 13.sp,
        color = SlateGray,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))
    
    Text(
        text = "Selecciona tu Semestre",
        fontSize = 14.sp,
        color = SlateGray,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    
    // Grid of Semesters 1 to 10
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = Modifier
            .height(96.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items((1..10).toList()) { sem ->
            val isSelected = selectedSemester == sem
            Surface(
                onClick = { onSemesterChange(sem) },
                shape = CircleShape,
                color = if (isSelected) NavyBlue else Color.LightGray.copy(alpha = 0.3f),
                contentColor = if (isSelected) Bone else SlateGray,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(sem.toString(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Materias a Cursar",
        fontSize = 14.sp,
        color = SlateGray,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    
    // Display curriculum subjects
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (enrolledSubjects.isEmpty()) {
            Text("No hay materias disponibles para este semestre.", fontSize = 13.sp, color = SlateGray)
        } else {
            enrolledSubjects.forEach { subject ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Bone),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(NavyBlue, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = subject.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NavyBlue
                            )
                            Text(
                                text = "Código: ${subject.code}",
                                fontSize = 11.sp,
                                color = SlateGray
                            )
                        }
                    }
                }
            }
        }
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Button(
        onClick = onNext,
        enabled = enrolledSubjects.isNotEmpty(),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
    ) {
        Text("Siguiente", color = Bone, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(8.dp))
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
    }
}

fun getBlockIdForTime(time: String): String {
    val t = time
    if (t == "M1" || t.contains("08-10")) return "M1"
    if (t == "M2" || t.contains("10-12")) return "M2"
    if (t == "M3" || t.contains("12-14")) return "M3"
    if (t == "T1" || t.contains("14-16")) return "T1"
    if (t == "T2" || t.contains("16-18")) return "T2"
    if (t == "T3" || t.contains("18-20")) return "T3"
    
    val lowercase = t.lowercase()
    val hourRegex = Regex("""(\d+):""")
    val match = hourRegex.find(lowercase)
    if (match != null) {
        val hour = match.groupValues[1].toIntOrNull()
        if (hour != null) {
            return when (hour) {
                7, 8, 9 -> "M1"
                10, 11 -> "M2"
                12 -> "M3"
                13, 14, 15 -> "T1"
                16 -> "T2"
                17, 18, 19, 20 -> "T3"
                else -> {
                    when {
                        hour >= 17 -> "T3"
                        hour == 16 -> "T2"
                        hour >= 13 -> "T1"
                        hour == 12 -> "M3"
                        hour >= 10 -> "M2"
                        else -> "M1"
                    }
                }
            }
        }
    }
    
    return when {
        lowercase.startsWith("m1") -> "M1"
        lowercase.startsWith("m2") -> "M2"
        lowercase.startsWith("m3") -> "M3"
        lowercase.startsWith("t1") -> "T1"
        lowercase.startsWith("t2") -> "T2"
        lowercase.startsWith("t3") -> "T3"
        else -> "M1"
    }
}

@Composable
fun Step4ScheduleConfig(
    enrolledSubjects: MutableList<StaticPensumSubject>,
    subjectSessions: MutableMap<String, List<ClassSessionDetails>>,
    onNext: () -> Unit
) {
    var currentSubjectIndex by rememberSaveable { mutableStateOf(0) }
    val safeIndex = if (enrolledSubjects.isEmpty()) 0 else currentSubjectIndex.coerceIn(0, enrolledSubjects.lastIndex)
    
    Text(
        text = "Configuración de Horario",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = NavyBlue,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = "Define los días, bloques y aulas para cada materia asignada de forma secuencial.",
        fontSize = 13.sp,
        color = SlateGray,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))
    
    val days = listOf("Lu", "Ma", "Mi", "Ju", "Vi")
    var isAfternoon by rememberSaveable { mutableStateOf(false) }
    val activeBlocks = if (isAfternoon) {
        listOf("T1", "T2", "T3")
    } else {
        listOf("M1", "M2", "M3")
    }

    fun getOccupyingSubject(day: String, blockId: String): String? {
        for (i in enrolledSubjects.indices) {
            if (i == safeIndex) continue
            val sub = enrolledSubjects[i]
            val sessions = subjectSessions[sub.name] ?: emptyList()
            val hasSession = sessions.any { it.day == day && getBlockIdForTime(it.time) == blockId }
            if (hasSession) {
                return sub.name
            }
        }
        return null
    }

    fun addCustomSubject() {
        val customName = "Materia Nueva ${enrolledSubjects.size + 1}"
        val newSub = StaticPensumSubject(
            semester = enrolledSubjects.getOrNull(0)?.semester ?: 1,
            code = "C-CUSTOM-${enrolledSubjects.size + 1}",
            name = customName,
            prereqs = emptyList()
        )
        enrolledSubjects.add(newSub)
        subjectSessions[customName] = listOf(ClassSessionDetails("Lu", "08:00 - 10:00", "Aula por definir"))
        currentSubjectIndex = enrolledSubjects.lastIndex
    }

    fun removeCurrentSubject() {
        if (safeIndex in enrolledSubjects.indices) {
            val subToRemove = enrolledSubjects[safeIndex]
            enrolledSubjects.removeAt(safeIndex)
            subjectSessions.remove(subToRemove.name)
            if (currentSubjectIndex >= enrolledSubjects.size) {
                currentSubjectIndex = (enrolledSubjects.size - 1).coerceAtLeast(0)
            }
        }
    }

    if (enrolledSubjects.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Bone),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = NavyBlue.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No tienes materias en tu lista",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Puedes agregar una materia personalizada para comenzar tu horario.",
                    fontSize = 13.sp,
                    color = SlateGray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { addCustomSubject() },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar Materia", color = Bone)
                }
            }
        }
    } else {
        val currentSubject = enrolledSubjects[safeIndex]
        val currentSubjectName = currentSubject.name
        val currentSessions = subjectSessions[currentSubjectName]?.toMutableList() ?: mutableListOf()
        
        var subjectNameInput by remember(safeIndex, currentSubjectName) { mutableStateOf(currentSubjectName) }
        var roomInput by remember(safeIndex, currentSubjectName) {
            mutableStateOf(currentSessions.firstOrNull()?.room ?: "Aula por definir")
        }

        fun renameCurrentSubject(newName: String) {
            if (safeIndex in enrolledSubjects.indices) {
                val oldSubject = enrolledSubjects[safeIndex]
                val updatedSubject = oldSubject.copy(name = newName)
                enrolledSubjects[safeIndex] = updatedSubject
                val sessions = subjectSessions[oldSubject.name] ?: emptyList()
                subjectSessions.remove(oldSubject.name)
                subjectSessions[newName] = sessions
                subjectNameInput = newName
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Bone),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header with current index and delete button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Materia ${safeIndex + 1} de ${enrolledSubjects.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateGray
                    )
                    
                    IconButton(
                        onClick = { removeCurrentSubject() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar materia",
                            tint = Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Subject Name Input field
                OutlinedTextField(
                    value = subjectNameInput,
                    onValueChange = { renameCurrentSubject(it) },
                    label = { Text("Nombre de la Materia", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NavyBlue,
                        focusedLabelColor = NavyBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                // Classroom (Aula) input
                OutlinedTextField(
                    value = roomInput,
                    onValueChange = { newRoom ->
                        roomInput = newRoom
                        val sessions = subjectSessions[currentSubjectName]?.map { it.copy(room = newRoom) } ?: emptyList()
                        subjectSessions[currentSubjectName] = sessions
                    },
                    label = { Text("Aula o Salón de Clase", fontSize = 11.sp) },
                    placeholder = { Text("Ej: Aula 102") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkGreen,
                        focusedLabelColor = DarkGreen
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Switch for Mañana / Tarde
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Turno del Horario:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyBlue
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isAfternoon) "Tarde" else "Mañana",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAfternoon) Color(0xFFD97706) else Color(0xFF0284C7)
                        )
                        Switch(
                            checked = isAfternoon,
                            onCheckedChange = { isAfternoon = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NavyBlue,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.LightGray
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Table grid instruction
                Text(
                    text = "Toca los bloques que corresponden a esta clase:",
                    fontSize = 11.sp,
                    color = SlateGray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                
                // Table
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NavyBlue.copy(alpha = 0.08f))
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1.2f), contentAlignment = Alignment.Center) {
                            Text("Bloque", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                        }
                        days.forEach { d ->
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Text(d, fontSize = 11.sp, fontWeight = FontWeight.Black, color = NavyBlue)
                            }
                        }
                    }
                    
                    activeBlocks.forEach { blockId ->
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .fillMaxHeight()
                                    .background(NavyBlue.copy(alpha = 0.03f)),
                                contentAlignment = Alignment.Center
                            ) {
                                val timeLabel = when (blockId) {
                                    "M1" -> "M1 (08-10)"
                                    "M2" -> "M2 (10-12)"
                                    "M3" -> "M3 (12-14)"
                                    "T1" -> "T1 (14-16)"
                                    "T2" -> "T2 (16-18)"
                                    "T3" -> "T3 (18-20)"
                                    else -> blockId
                                }
                                Text(
                                    text = timeLabel,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyBlue,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 10.sp
                                )
                            }
                            
                            days.forEach { d ->
                                val occupiedBy = getOccupyingSubject(d, blockId)
                                val isSelectedForCurrent = currentSessions.any { it.day == d && getBlockIdForTime(it.time) == blockId }
                                
                                val cellBg = when {
                                    occupiedBy != null -> Color(0xFFCBD5E1)
                                    isSelectedForCurrent -> NavyBlue
                                    else -> Color.Transparent
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(cellBg)
                                        .clickable(enabled = occupiedBy == null) {
                                            if (isSelectedForCurrent) {
                                                currentSessions.removeAll { it.day == d && getBlockIdForTime(it.time) == blockId }
                                            } else {
                                                val defaultTime = when (blockId) {
                                                    "M1" -> "08:00 - 10:00"
                                                    "M2" -> "10:00 - 12:00"
                                                    "M3" -> "12:00 - 14:00"
                                                    "T1" -> "14:00 - 16:00"
                                                    "T2" -> "16:00 - 18:00"
                                                    "T3" -> "18:00 - 20:00"
                                                    else -> "08:00 - 10:00"
                                                }
                                                currentSessions.add(ClassSessionDetails(d, defaultTime, roomInput.ifEmpty { "Aula por definir" }))
                                            }
                                            subjectSessions[currentSubjectName] = currentSessions.toList()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (occupiedBy != null) {
                                        Text(
                                            text = getSubjectInitials(occupiedBy),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SlateGray,
                                            maxLines = 1,
                                            textAlign = TextAlign.Center
                                        )
                                    } else if (isSelectedForCurrent) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Seleccionado",
                                            tint = Bone,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Navigation Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (safeIndex > 0) {
                OutlinedButton(
                    onClick = { currentSubjectIndex-- },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, NavyBlue),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyBlue)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Anterior", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            val isLast = safeIndex == enrolledSubjects.lastIndex
            Button(
                onClick = {
                    if (isLast) {
                        onNext()
                    } else {
                        currentSubjectIndex++
                    }
                },
                modifier = Modifier.weight(1.3f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Text(
                    text = if (isLast) "Finalizar Horario" else "Siguiente Clase",
                    color = Bone,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Option to add a custom/extra subject
        OutlinedButton(
            onClick = { addCustomSubject() },
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DarkGreen),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkGreen)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Agregar otra materia (Custom)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Step5Route(
    originInput: String,
    onOriginChange: (String) -> Unit,
    destInput: String,
    onDestChange: (String) -> Unit,
    baseTravelMinutes: String,
    onTravelMinutesChange: (String) -> Unit,
    locationPermissionsState: com.google.accompanist.permissions.MultiplePermissionsState,
    onFinish: () -> Unit
) {
    Box(
        modifier = Modifier
            .sizeIn(maxHeight = 110.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        BuddyMascot(pose = "thinking", modifier = Modifier.size(90.dp))
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Tu Camino a Clase",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = NavyBlue,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = "Configura tu punto de partida habitual y tiempo de viaje.",
        fontSize = 13.sp,
        color = SlateGray,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))
    
    OutlinedTextField(
        value = originInput,
        onValueChange = onOriginChange,
        label = { Text("Nombre de tu ubicación") },
        placeholder = { Text("Ej. Casa, Trabajo") },
        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = DarkGreen) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NavyBlue,
            focusedLabelColor = NavyBlue
        )
    )
    
    Spacer(modifier = Modifier.height(12.dp))
    
    // Real Map View
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, Color.LightGray, RoundedCornerShape(16.dp))
    ) {
        OsmMapView(
            modifier = Modifier.fillMaxSize(),
            locationEnabled = locationPermissionsState.allPermissionsGranted
        )
        
        if (!locationPermissionsState.allPermissionsGranted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { locationPermissionsState.launchMultiplePermissionRequest() },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Bone)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Activar GPS", color = Bone)
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Señal GPS obtenida", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
            }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Minutos promedio de viaje",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = SlateGray,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(6.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        val currentMins = baseTravelMinutes.toIntOrNull() ?: 25
        IconButton(
            onClick = {
                val newVal = (currentMins - 5).coerceAtLeast(5)
                onTravelMinutesChange(newVal.toString())
            },
            modifier = Modifier
                .size(40.dp)
                .background(Color.White, CircleShape)
                .border(1.dp, Color.LightGray, CircleShape)
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Menos", tint = NavyBlue)
        }
        
        Text(
            text = "$baseTravelMinutes min",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = NavyBlue,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        IconButton(
            onClick = {
                val newVal = (currentMins + 5).coerceAtMost(180)
                onTravelMinutesChange(newVal.toString())
            },
            modifier = Modifier
                .size(40.dp)
                .background(Color.White, CircleShape)
                .border(1.dp, Color.LightGray, CircleShape)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Más", tint = NavyBlue)
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    // Quick presets
    val travelPresets = listOf(15, 30, 45, 60, 90)
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        items(travelPresets) { minVal ->
            val isSelected = baseTravelMinutes == minVal.toString()
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) NavyBlue else Color.White)
                    .border(1.dp, if (isSelected) NavyBlue else Color.LightGray, RoundedCornerShape(8.dp))
                    .clickable { onTravelMinutesChange(minVal.toString()) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "$minVal min",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else NavyBlue
                )
            }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Button(
        onClick = onFinish,
        enabled = originInput.isNotBlank(),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = NavyBlue,
            disabledContainerColor = Color.LightGray.copy(alpha = 0.5f)
        )
    ) {
        Text("Comenzar", color = Bone, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}
