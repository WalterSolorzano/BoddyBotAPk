package com.aistudio.unibuddy.qywvsp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.unibuddy.qywvsp.data.CurriculumData
import com.aistudio.unibuddy.qywvsp.ui.SemesterHistoryView
import com.aistudio.unibuddy.qywvsp.ui.UniBuddyViewModel
import com.aistudio.unibuddy.qywvsp.ui.theme.BackgroundBone
import com.aistudio.unibuddy.qywvsp.ui.theme.NavyBlue
import com.aistudio.unibuddy.qywvsp.ui.theme.ProBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerScreen(viewModel: UniBuddyViewModel, onBack: () -> Unit) {
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

    val passingGrade = remember(university) {
        if (university == "UAM" || university == "UCA" || university == "Keiser") 70.0 else 60.0
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Pensum", "Semestre", "Insignias") // Historial is basically Semestre history, but let's just combine it. The prompt asked for "Pensum, Historial, Semestre, Insignias", let's use all 4.

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Carrera", fontWeight = FontWeight.Bold, color = NavyBlue) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = NavyBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BackgroundBone
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = NavyBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = NavyBlue
                    )
                }
            ) {
                listOf("Pensum", "Semestre Actual", "Clases Pasadas", "Logros").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1) },
                        selectedContentColor = NavyBlue,
                        unselectedContentColor = Color.Gray
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                when (selectedTabIndex) {
                    0 -> PensumMapTab(staticPensum, history, professors, ongoingSubjectNames, passingGrade)
                    1 -> SemesterHistoryView(viewModel = viewModel, onBack = null) // We pass onBack = null so it doesn't render its own back button
                    2 -> PensumSemestersTab(staticPensum, history, professors, passingGrade, viewModel)
                    3 -> BadgesGrid(viewModel = viewModel)
                }
            }
        }
    }
}
