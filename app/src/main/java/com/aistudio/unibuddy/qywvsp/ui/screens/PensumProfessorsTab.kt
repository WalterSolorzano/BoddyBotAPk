package com.aistudio.unibuddy.qywvsp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.unibuddy.qywvsp.data.AcademicRecordWithSubject
import com.aistudio.unibuddy.qywvsp.data.Professor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PensumProfessorsTab(history: List<AcademicRecordWithSubject>, professors: List<Professor>, onAddProfessor: (String) -> Unit = {}) {
    var sortByRating by remember { mutableStateOf(false) }
    var selectedProfessor by remember { mutableStateOf<Professor?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    
    // Calculate average ratings for professors based on history (or use rating field if available)
    val profRatings = remember(history, professors) {
        val ratingsMap = mutableMapOf<Int, MutableList<Int>>()
        history.forEach { rec ->
            if (rec.record.professorId != null && rec.record.rating != null) {
                ratingsMap.getOrPut(rec.record.professorId) { mutableListOf() }.add(rec.record.rating)
            }
        }
        ratingsMap.mapValues { it.value.average() }
    }
    
    val sortedProfessors = remember(professors, sortByRating, profRatings) {
        if (sortByRating) {
            professors.sortedByDescending { profRatings[it.id] ?: 0.0 }
        } else {
            professors.sortedBy { it.name }
        }
    }

    var searchQuery by remember { mutableStateOf("") }

    val filteredProfessors = remember(sortedProfessors, searchQuery, history) {
        if (searchQuery.isBlank()) {
            sortedProfessors
        } else {
            sortedProfessors.filter { prof ->
                prof.name.contains(searchQuery, ignoreCase = true) ||
                history.any { rec ->
                    rec.record.professorId == prof.id && 
                    (rec.subjectName.contains(searchQuery, ignoreCase = true) || 
                     rec.subjectCode.contains(searchQuery, ignoreCase = true))
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Tus Profesores", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = com.aistudio.unibuddy.qywvsp.ui.theme.NavyBlue)
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { sortByRating = !sortByRating }) {
                    Icon(Icons.Filled.Sort, contentDescription = "Sort")
                    Spacer(Modifier.width(4.dp))
                    Text(if (sortByRating) "Por Valoración" else "Alfabético")
                }
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Professor")
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar por profesor o materia...", fontSize = 12.sp) },
            leadingIcon = { Icon(imageVector = androidx.compose.material.icons.Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = com.aistudio.unibuddy.qywvsp.ui.theme.ProBlue,
                unfocusedBorderColor = Color.LightGray
            ),
            singleLine = true
        )
        
        if (filteredProfessors.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (searchQuery.isBlank()) "No hay profesores registrados." else "No se encontraron coincidencias.", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(100.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredProfessors) { prof ->
                    ProfessorCard(
                        professor = prof,
                        avgRating = profRatings[prof.id],
                        onClick = { selectedProfessor = prof }
                    )
                }
            }
        }
    }
    
    selectedProfessor?.let { prof ->
        ModalBottomSheet(onDismissRequest = { selectedProfessor = null }) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProfessorAvatar(prof.name, prof.avatarSeed, 64)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(prof.name, style = MaterialTheme.typography.titleLarge)
                        val avg = profRatings[prof.id]
                        if (avg != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Star, "Rating", tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                                Text(String.format(" %.1f / 5.0", avg), fontSize = 14.sp, color = Color.Gray)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Materias Impartidas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                val taught = history.filter { it.record.professorId == prof.id }
                if (taught.isEmpty()) {
                    Text("No hay registros de materias.", color = Color.Gray)
                } else {
                    taught.forEach { rec ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(rec.subjectName, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text(rec.semester, fontSize = 12.sp, color = Color.Gray)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Nota: ${rec.record.grade}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    if (rec.record.rating != null) {
                                        Row {
                                            repeat(rec.record.rating) { Icon(Icons.Filled.Star, "", tint = Color(0xFFFFC107), modifier = Modifier.size(12.dp)) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showAddDialog) {
        AddProfessorDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { onAddProfessor(it) }
        )
    }
}

@Composable
fun AddProfessorDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir Profesor", fontWeight = FontWeight.Bold, color = com.aistudio.unibuddy.qywvsp.ui.theme.NavyBlue) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ingresa el nombre del profesor para registrarlo en tu biblioteca:", fontSize = 12.sp, color = Color.Gray)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Ej. Ing. Juan Pérez") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = com.aistudio.unibuddy.qywvsp.ui.theme.ProBlue,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = com.aistudio.unibuddy.qywvsp.ui.theme.NavyBlue)
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        }
    )
}

@Composable
fun ProfessorCard(professor: Professor, avgRating: Double?, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(8.dp)
    ) {
        ProfessorAvatar(professor.name, professor.avatarSeed, 80)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = professor.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 14.sp
        )
        if (avgRating != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Star, "Rating", tint = Color(0xFFFFC107), modifier = Modifier.size(12.dp))
                Text(String.format(" %.1f", avgRating), fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun ProfessorAvatar(name: String, seed: String, sizeDp: Int) {
    val colorHash = name.hashCode()
    val r = (colorHash and 0xFF0000 shr 16) % 128 + 127
    val g = (colorHash and 0x00FF00 shr 8) % 128 + 127
    val b = (colorHash and 0x0000FF) % 128 + 127
    val bgColor = Color(r, g, b)
    
    val initials = seed.take(2).uppercase()
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(bgColor)
    ) {
        Text(
            text = initials,
            color = Color.DarkGray,
            fontWeight = FontWeight.Bold,
            fontSize = (sizeDp / 2.5).sp
        )
    }
}
