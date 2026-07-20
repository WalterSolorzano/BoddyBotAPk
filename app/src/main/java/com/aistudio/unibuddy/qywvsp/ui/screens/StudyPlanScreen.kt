package com.aistudio.unibuddy.qywvsp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
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
import com.aistudio.unibuddy.qywvsp.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyPlanScreen(viewModel: UniBuddyViewModel, onBack: () -> Unit) {
    val studyPlan by viewModel.studyPlan.collectAsStateWithLifecycle()
    val isGeneratingPlan by viewModel.isGeneratingPlan.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (studyPlan == null && !isGeneratingPlan) {
            viewModel.generateStudyPlan()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Planificador Inteligente", color = NavyBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("IA de priorización semanal", color = SlateGray, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = NavyBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Bone
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isGeneratingPlan) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = ProBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Analizando tus notas, faltas y progreso...",
                        color = NavyBlue,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = ProBlue.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, ProBlue.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "IA", tint = ProBlue, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Plan generado automáticamente considerando las materias en las que has perdido más puntos o tienes menos margen de faltas.",
                                    color = ProBlue,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = studyPlan ?: "No se pudo generar el plan.",
                                color = NavyBlue,
                                fontSize = 14.sp,
                                lineHeight = 22.sp,
                                modifier = Modifier.padding(20.dp)
                            )
                        }
                    }
                    
                    item {
                        Button(
                            onClick = { viewModel.generateStudyPlan() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Regenerar Plan", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }
    }
}
