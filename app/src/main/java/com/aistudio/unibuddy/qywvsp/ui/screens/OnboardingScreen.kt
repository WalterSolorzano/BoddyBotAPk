package com.aistudio.unibuddy.qywvsp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aistudio.unibuddy.qywvsp.ui.UniBuddyViewModel
import com.aistudio.unibuddy.qywvsp.ui.theme.NavyBlue
import com.aistudio.unibuddy.qywvsp.ui.theme.Bone
import com.aistudio.unibuddy.qywvsp.ui.theme.DarkGreen
import com.aistudio.unibuddy.qywvsp.ui.theme.ProBlue
import com.aistudio.unibuddy.qywvsp.data.ClassSessionDetails
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(viewModel: UniBuddyViewModel, onFinished: () -> Unit) {
    var step by rememberSaveable { mutableStateOf(1) }
    
    // Form Inputs
    var nameInput by rememberSaveable { mutableStateOf("") }
    var originInput by rememberSaveable { mutableStateOf("") }
    var destinationInput by rememberSaveable { mutableStateOf("") }
    var baseTravelMinutes by rememberSaveable { mutableStateOf("25") }
    var careerInput by rememberSaveable { mutableStateOf("") }
    var uniInput by rememberSaveable { mutableStateOf("") }

    // Subject Form Multi adder
    var subjectName by rememberSaveable { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (step) {
                1 -> {
                    Text("¡Bienvenido a UniBuddy!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("¿Cómo te llamas?", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Tu nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (nameInput.isNotBlank()) {
                                viewModel.saveUsername(nameInput.trim())
                                step = 2
                            }
                        },
                        enabled = nameInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                    ) {
                        Text("Continuar", color = Bone)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Siguiente")
                    }
                }
                2 -> {
                    Text("Detalles Académicos y Ruta", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = uniInput,
                        onValueChange = { uniInput = it },
                        label = { Text("Universidad") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = careerInput,
                        onValueChange = { careerInput = it },
                        label = { Text("Carrera") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = originInput,
                        onValueChange = { originInput = it },
                        label = { Text("Origen (ej. Casa)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = destinationInput,
                        onValueChange = { destinationInput = it },
                        label = { Text("Destino (Sede Univ.)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Minutos promedio de viaje: $baseTravelMinutes")
                    Slider(
                        value = baseTravelMinutes.toFloat(),
                        onValueChange = { baseTravelMinutes = it.toInt().toString() },
                        valueRange = 5f..120f
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            viewModel.saveCareer(careerInput)
                            viewModel.saveUserUniversity(uniInput)
                            viewModel.saveRoute(originInput, destinationInput)
                            baseTravelMinutes.toIntOrNull()?.let { viewModel.saveBaseTravelTime(it) }
                            step = 3
                        },
                        enabled = uniInput.isNotBlank() && careerInput.isNotBlank() && originInput.isNotBlank() && destinationInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text("Continuar")
                    }
                }
                3 -> {
                    Text("Tus Materias", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("Nombre de materia") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (subjectName.isNotBlank()) {
                                viewModel.addSubject(
                                    name = subjectName,
                                    schedule = "Lu,Mi",
                                    sessions = emptyList(),
                                    requiredAttendancePercent = 80,
                                    totalClasses = 30
                                )
                                subjectName = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Añadir Materia")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.saveBuddyPose("happy")
                            onFinished()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text("Comenzar", color = Bone)
                    }
                }
            }
        }
    }
}
