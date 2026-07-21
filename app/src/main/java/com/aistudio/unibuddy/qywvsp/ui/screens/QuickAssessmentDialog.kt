package com.aistudio.unibuddy.qywvsp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aistudio.unibuddy.qywvsp.data.Assessment
import com.aistudio.unibuddy.qywvsp.data.Subject
import com.aistudio.unibuddy.qywvsp.ui.UniBuddyViewModel
import com.aistudio.unibuddy.qywvsp.ui.theme.NavyBlue
import com.aistudio.unibuddy.qywvsp.ui.theme.ProBlue
import com.aistudio.unibuddy.qywvsp.ui.theme.SlateGray
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAssessmentDialog(
    viewModel: UniBuddyViewModel,
    onDismiss: () -> Unit,
    activeSubjects: List<Subject>
) {
    var selectedMode by remember { mutableStateOf(0) } // 0: Ya la hice, 1: Ya sé cuándo será
    var selectedSubject by remember { mutableStateOf<Subject?>(null) }
    var expandedSubject by remember { mutableStateOf(false) }

    val defaultTypes = listOf("Examen (15%)" to 15.0, "Prueba (7%)" to 7.0)
    var customTypes by remember { mutableStateOf(viewModel.getCustomAssessmentTypes()) }
    val allTypes = defaultTypes + customTypes

    var selectedTypeIndex by remember { mutableStateOf<Int?>(null) }
    var expandedType by remember { mutableStateOf(false) }
    
    var showAddCustomType by remember { mutableStateOf(false) }
    var customTypeName by remember { mutableStateOf("") }
    var customTypePercent by remember { mutableStateOf("") }

    var grade by remember { mutableStateOf("") }
    
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    var dateString by remember { mutableStateOf(sdf.format(Date())) }

    // Pending assessments for suggestions
    val allAssessments by viewModel.assessments.collectAsState(initial = emptyList())
    val pendingAssessments = remember(allAssessments, selectedSubject) {
        if (selectedSubject == null) emptyList()
        else allAssessments.filter { it.subjectId == selectedSubject!!.id && it.grade == null }
    }
    var selectedPendingAssessment by remember { mutableStateOf<Assessment?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Registrar Evaluación", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = NavyBlue)

                // Modes
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedMode == 0,
                        onClick = { 
                            selectedMode = 0 
                            selectedPendingAssessment = null
                        },
                        label = { Text("Ya la hice / Tengo nota") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NavyBlue, selectedLabelColor = Color.White)
                    )
                    FilterChip(
                        selected = selectedMode == 1,
                        onClick = { 
                            selectedMode = 1 
                            selectedPendingAssessment = null
                        },
                        label = { Text("Programar a futuro") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ProBlue, selectedLabelColor = Color.White)
                    )
                }

                // Subject Selector
                ExposedDropdownMenuBox(
                    expanded = expandedSubject,
                    onExpandedChange = { expandedSubject = it }
                ) {
                    OutlinedTextField(
                        value = selectedSubject?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Materia") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSubject) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSubject,
                        onDismissRequest = { expandedSubject = false }
                    ) {
                        activeSubjects.forEach { subject ->
                            DropdownMenuItem(
                                text = { Text(subject.name) },
                                onClick = {
                                    selectedSubject = subject
                                    selectedPendingAssessment = null
                                    selectedTypeIndex = null
                                    expandedSubject = false
                                }
                            )
                        }
                    }
                }

                // Suggestions for Mode 0 (Ya la hice)
                if (selectedMode == 0 && selectedSubject != null && pendingAssessments.isNotEmpty()) {
                    var expandedPending by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F8)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ProBlue)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Evaluaciones programadas encontradas",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = ProBlue
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            ExposedDropdownMenuBox(
                                expanded = expandedPending,
                                onExpandedChange = { expandedPending = it }
                            ) {
                                val valueText = if (selectedPendingAssessment != null) {
                                    "${selectedPendingAssessment!!.name} (${selectedPendingAssessment!!.percentage.toInt()}%) - ${selectedPendingAssessment!!.examDate}"
                                } else {
                                    "Seleccionar evaluación programada..."
                                }
                                OutlinedTextField(
                                    value = valueText,
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedPending,
                                    onDismissRequest = { expandedPending = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Nueva evaluación (crear desde cero)") },
                                        onClick = {
                                            selectedPendingAssessment = null
                                            selectedTypeIndex = null
                                            expandedPending = false
                                        }
                                    )
                                    pendingAssessments.forEach { ass ->
                                        DropdownMenuItem(
                                            text = { Text("${ass.name} (${ass.percentage.toInt()}%) - ${ass.examDate}") },
                                            onClick = {
                                                selectedPendingAssessment = ass
                                                dateString = ass.examDate
                                                expandedPending = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Type Selector (Only editable/visible if not completing a pending assessment)
                if (selectedPendingAssessment == null) {
                    ExposedDropdownMenuBox(
                        expanded = expandedType,
                        onExpandedChange = { expandedType = it }
                    ) {
                        val typeText = if (selectedTypeIndex != null && selectedTypeIndex!! < allTypes.size) {
                            allTypes[selectedTypeIndex!!].first
                        } else ""
                        
                        OutlinedTextField(
                            value = typeText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de Evaluación") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedType,
                            onDismissRequest = { expandedType = false }
                        ) {
                            allTypes.forEachIndexed { index, pair ->
                                DropdownMenuItem(
                                    text = { Text(pair.first) },
                                    onClick = {
                                        selectedTypeIndex = index
                                        expandedType = false
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Agregar tipo personalizado...", color = ProBlue) },
                                onClick = {
                                    expandedType = false
                                    showAddCustomType = true
                                },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = ProBlue) }
                            )
                        }
                    }
                    
                    if (showAddCustomType) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = customTypeName,
                                onValueChange = { customTypeName = it },
                                label = { Text("Nombre (ej. Proyecto)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = customTypePercent,
                                onValueChange = { customTypePercent = it },
                                label = { Text("%") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(80.dp)
                            )
                            IconButton(
                                onClick = {
                                    val p = customTypePercent.toDoubleOrNull()
                                    if (customTypeName.isNotBlank() && p != null) {
                                        val newType = "$customTypeName (${p.toInt()}%)" to p
                                        viewModel.saveCustomAssessmentType(newType.first, newType.second)
                                        customTypes = viewModel.getCustomAssessmentTypes()
                                        selectedTypeIndex = defaultTypes.size + customTypes.size - 1
                                        showAddCustomType = false
                                        customTypeName = ""
                                        customTypePercent = ""
                                    }
                                },
                                modifier = Modifier.align(Alignment.CenterVertically)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Guardar", tint = ProBlue)
                            }
                        }
                    }
                } else {
                    // Display details of the selected pending assessment
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Evaluación seleccionada:",
                                    fontSize = 11.sp,
                                    color = SlateGray
                                )
                                Text(
                                    text = "${selectedPendingAssessment!!.name} (${selectedPendingAssessment!!.percentage.toInt()}%)",
                                    fontWeight = FontWeight.Bold,
                                    color = NavyBlue
                                )
                            }
                            TextButton(
                                onClick = { selectedPendingAssessment = null }
                            ) {
                                Text("Cambiar", color = SlateGray)
                            }
                        }
                    }
                }

                // Grade Input (Only if Mode 0)
                if (selectedMode == 0) {
                    OutlinedTextField(
                        value = grade,
                        onValueChange = { grade = it },
                        label = { Text("Nota Obtenida (0-100)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Date Input
                OutlinedTextField(
                    value = dateString,
                    onValueChange = { dateString = it },
                    label = { Text(if (selectedMode == 0) "Fecha de realización" else "Fecha programada") },
                    leadingIcon = { Icon(if (selectedMode == 0) Icons.Default.Event else Icons.Default.DateRange, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = SlateGray)
                    }
                    Button(
                        onClick = {
                            if (selectedSubject != null) {
                                if (selectedPendingAssessment != null) {
                                    // Complete existing assessment
                                    val g = if (selectedMode == 0) grade.toDoubleOrNull() else null
                                    viewModel.updateAssessment(
                                        selectedPendingAssessment!!.copy(
                                            grade = g,
                                            examDate = dateString
                                        )
                                    )
                                } else if (selectedTypeIndex != null) {
                                    // Create new assessment
                                    val type = allTypes[selectedTypeIndex!!]
                                    val g = if (selectedMode == 0) grade.toDoubleOrNull() else null
                                    val name = type.first.substringBefore(" (")
                                    
                                    viewModel.addAssessmentDirectly(
                                        subjectId = selectedSubject!!.id,
                                        name = name,
                                        grade = g,
                                        percentage = type.second,
                                        date = dateString
                                    )
                                }
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                        enabled = selectedSubject != null && (selectedPendingAssessment != null || selectedTypeIndex != null) && (selectedMode == 1 || grade.isNotBlank())
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun QuickAbsenceDialog(
    viewModel: com.aistudio.unibuddy.qywvsp.ui.UniBuddyViewModel,
    onDismiss: () -> Unit,
    activeSubjects: List<com.aistudio.unibuddy.qywvsp.data.Subject>
) {
    var expanded by remember { mutableStateOf(false) }
    val suggestedSubject = remember(activeSubjects) {
        if (activeSubjects.isEmpty()) null
        else {
            val calendar = java.util.Calendar.getInstance()
            val todayCode = when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
                java.util.Calendar.MONDAY -> "Lu"
                java.util.Calendar.TUESDAY -> "Ma"
                java.util.Calendar.WEDNESDAY -> "Mi"
                java.util.Calendar.THURSDAY -> "Ju"
                java.util.Calendar.FRIDAY -> "Vi"
                java.util.Calendar.SATURDAY -> "Sá"
                else -> "Do"
            }
            val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val currentMin = calendar.get(java.util.Calendar.MINUTE)
            val currentTotalMinutes = currentHour * 60 + currentMin

            val todaySubjects = activeSubjects.filter { sub ->
                sub.sessions.any { it.day.trim().equals(todayCode, ignoreCase = true) }
            }

            if (todaySubjects.isNotEmpty()) {
                todaySubjects.minByOrNull { sub ->
                    val matchingSession = sub.sessions.find { it.day.trim().equals(todayCode, ignoreCase = true) }
                    val sessionTimeStr = matchingSession?.time ?: ""
                    val startTimeStr = sessionTimeStr.split("-").firstOrNull()?.trim() ?: "08:00"
                    val timeParts = startTimeStr.split(":")
                    val startHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 8
                    val startMin = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
                    val startTotalMinutes = startHour * 60 + startMin
                    Math.abs(startTotalMinutes - currentTotalMinutes)
                }
            } else {
                activeSubjects.firstOrNull()
            }
        }
    }
    var selectedSubject by remember { mutableStateOf<com.aistudio.unibuddy.qywvsp.data.Subject?>(null) }
    LaunchedEffect(suggestedSubject) {
        if (selectedSubject == null && suggestedSubject != null) {
            selectedSubject = suggestedSubject
        }
    }
    var reason by remember { mutableStateOf("") }
    
    val dateStr = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Marcar Falta Rápida") },
        text = {
            Column {
                if (activeSubjects.isEmpty()) {
                    Text("No hay materias activas. Ve a 'Pensum' para agregarlas.", color = Color.Gray)
                } else {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedSubject?.name ?: "Selecciona una materia",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Materia") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            activeSubjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = { Text(subject.name) },
                                    onClick = {
                                        selectedSubject = subject
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Motivo (Opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Fecha: $dateStr", fontSize = 12.sp, color = Color.Gray)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedSubject?.let { sub ->
                        viewModel.insertAbsence(
                                com.aistudio.unibuddy.qywvsp.data.Absence(
                                    subjectId = sub.id,
                                    date = dateStr
                                )
                            )
                            onDismiss()
                    }
                },
                enabled = selectedSubject != null
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun QuickCancelDialog(
    viewModel: com.aistudio.unibuddy.qywvsp.ui.UniBuddyViewModel,
    onDismiss: () -> Unit,
    activeSubjects: List<com.aistudio.unibuddy.qywvsp.data.Subject>
) {
    var expanded by remember { mutableStateOf(false) }
    val suggestedSubject = remember(activeSubjects) {
        if (activeSubjects.isEmpty()) null
        else {
            val calendar = java.util.Calendar.getInstance()
            val todayCode = when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
                java.util.Calendar.MONDAY -> "Lu"
                java.util.Calendar.TUESDAY -> "Ma"
                java.util.Calendar.WEDNESDAY -> "Mi"
                java.util.Calendar.THURSDAY -> "Ju"
                java.util.Calendar.FRIDAY -> "Vi"
                java.util.Calendar.SATURDAY -> "Sá"
                else -> "Do"
            }
            val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val currentMin = calendar.get(java.util.Calendar.MINUTE)
            val currentTotalMinutes = currentHour * 60 + currentMin

            val todaySubjects = activeSubjects.filter { sub ->
                sub.sessions.any { it.day.trim().equals(todayCode, ignoreCase = true) }
            }

            if (todaySubjects.isNotEmpty()) {
                todaySubjects.minByOrNull { sub ->
                    val matchingSession = sub.sessions.find { it.day.trim().equals(todayCode, ignoreCase = true) }
                    val sessionTimeStr = matchingSession?.time ?: ""
                    val startTimeStr = sessionTimeStr.split("-").firstOrNull()?.trim() ?: "08:00"
                    val timeParts = startTimeStr.split(":")
                    val startHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 8
                    val startMin = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
                    val startTotalMinutes = startHour * 60 + startMin
                    Math.abs(startTotalMinutes - currentTotalMinutes)
                }
            } else {
                activeSubjects.firstOrNull()
            }
        }
    }
    var selectedSubject by remember { mutableStateOf<com.aistudio.unibuddy.qywvsp.data.Subject?>(null) }
    LaunchedEffect(suggestedSubject) {
        if (selectedSubject == null && suggestedSubject != null) {
            selectedSubject = suggestedSubject
        }
    }
    var reason by remember { mutableStateOf("") }
    
    val dateStr = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Clase Cancelada") },
        text = {
            Column {
                if (activeSubjects.isEmpty()) {
                    Text("No hay materias activas. Ve a 'Pensum' para agregarlas.", color = Color.Gray)
                } else {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedSubject?.name ?: "Selecciona una materia",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Materia") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            activeSubjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = { Text(subject.name) },
                                    onClick = {
                                        selectedSubject = subject
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Motivo/Detalle (Opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Fecha: $dateStr", fontSize = 12.sp, color = Color.Gray)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedSubject?.let { sub ->
                        val suffix = if (reason.isBlank()) " (Cancelada)" else " (Cancelada: $reason)"
                        viewModel.registerAttendanceLog(sub.id, isPresent = false, isCancelled = true, dateStr = "$dateStr$suffix")
                        onDismiss()
                    }
                },
                enabled = selectedSubject != null
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
