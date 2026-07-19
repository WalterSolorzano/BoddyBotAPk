package com.aistudio.unibuddy.qywvsp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aistudio.unibuddy.qywvsp.data.AcademicRecordWithSubject
import com.aistudio.unibuddy.qywvsp.data.CurriculumData
import com.aistudio.unibuddy.qywvsp.data.Professor
import com.aistudio.unibuddy.qywvsp.ui.UniBuddyViewModel
import com.aistudio.unibuddy.qywvsp.ui.theme.BackgroundBone
import com.aistudio.unibuddy.qywvsp.ui.theme.ProBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PensumProgressScreen(viewModel: UniBuddyViewModel, onBack: () -> Unit) {
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

    val history by viewModel.academicHistory.collectAsState(initial = emptyList())
    val professors by viewModel.allProfessors.collectAsState(initial = emptyList())
    val university by viewModel.userUniversity.collectAsState()
    val career by viewModel.career.collectAsState()
    val activeSubjects by viewModel.subjects.collectAsState(initial = emptyList())

    val ongoingSubjectNames = activeSubjects.map { it.name.uppercase() }

    val staticPensum = remember(university, career) {
        CurriculumData.getSubjectsFor(university, career)
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Mapa", "Estadísticas", "Profesores")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Carrera") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            
            when (selectedTabIndex) {
                0 -> PensumMapTab(staticPensum, history, professors, ongoingSubjectNames)
                1 -> PensumStatsTab(staticPensum, history)
                2 -> PensumProfessorsTab(history, professors, onAddDummyProfessor = { viewModel.addDummyProfessor() })
            }
        }
    }
}
