package com.aistudio.unibuddy.qywvsp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aistudio.unibuddy.qywvsp.data.Subject
import com.aistudio.unibuddy.qywvsp.ui.UniBuddyViewModel
import com.aistudio.unibuddy.qywvsp.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorScreen(
    subjectId: Int?,
    viewModel: UniBuddyViewModel,
    onBack: () -> Unit
) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val subject = subjects.find { it.id == subjectId }
    val chatMessages by viewModel.tutorChatMessages.collectAsStateWithLifecycle()
    val isTutorTyping by viewModel.isTutorTyping.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val extractedResult by viewModel.extractedResult.collectAsStateWithLifecycle()
    val isExtractingWithAI by viewModel.isExtractingWithAI.collectAsStateWithLifecycle()
    var selectedExtractedSubject by remember { mutableStateOf<com.aistudio.unibuddy.qywvsp.data.Subject?>(null) }

    LaunchedEffect(extractedResult) {
        extractedResult?.let { res ->
            selectedExtractedSubject = subjects.find { it.name.trim().equals(res.matchedSubjectName.trim(), ignoreCase = true) } 
                ?: subjects.firstOrNull()
        }
    }

    var isExtractorExpanded by remember { mutableStateOf(false) }
    var rawInputText by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(subject) {
        if (subject != null) {
            viewModel.initTutorForSubject(subject)
        }
    }

    LaunchedEffect(chatMessages.size, isTutorTyping) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Tutor IA", color = NavyBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(subject?.name ?: "Asistente General", color = SlateGray, fontSize = 12.sp)
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
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Pregunta algo...", fontSize = 14.sp) },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ProBlue,
                            unfocusedBorderColor = Color.LightGray,
                            focusedContainerColor = Bone,
                            unfocusedContainerColor = Bone
                        ),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendTutorMessage(inputText, subject)
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank()) ProBlue else Color.LightGray),
                        enabled = inputText.isNotBlank() && !isTutorTyping
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.White)
                    }
                }
            }
        },
        containerColor = Bone
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Panel de Captura Inteligente con IA
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ProBlue.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExtractorExpanded = !isExtractorExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Captura Inteligente",
                                tint = ProBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Captura Inteligente con IA",
                                fontWeight = FontWeight.Bold,
                                color = NavyBlue,
                                fontSize = 14.sp
                            )
                        }
                        Icon(
                            imageVector = if (isExtractorExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expandir/Colapsar",
                            tint = NavyBlue
                        )
                    }

                    if (isExtractorExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Pega el texto de tu sílabo o escribe libremente los detalles del examen. La IA identificará la materia, fecha, peso y sugerirá temas de repaso.",
                            color = SlateGray,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = rawInputText,
                            onValueChange = { rawInputText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Ej: El profesor anunció que el segundo examen parcial de Matemática discreta será el 30/10/2026 sobre Grafos y Árboles, vale 20 puntos.", fontSize = 12.sp) },
                            maxLines = 4,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ProBlue,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (isExtractingWithAI) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(color = ProBlue, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analizando texto con IA...", color = ProBlue, fontSize = 12.sp)
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (rawInputText.isNotBlank()) {
                                        viewModel.extractEvaluationFromText(rawInputText, subjects)
                                    }
                                },
                                enabled = rawInputText.isNotBlank(),
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = ProBlue),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Analizar y Extraer Datos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        extractedResult?.let { result ->
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Bone)
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Vista Previa de Evaluación",
                                fontWeight = FontWeight.Bold,
                                color = NavyBlue,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Matched subject dropdown selector
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = selectedExtractedSubject?.name ?: "Selecciona materia",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Materia Detectada", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth().clickable { dropdownExpanded = true },
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ProBlue),
                                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                                    enabled = false, // We handle the click on modifier
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = NavyBlue,
                                        disabledBorderColor = Color.LightGray,
                                        disabledLabelColor = SlateGray
                                    )
                                )
                                // Transparent overlay for clickable when disabled
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { dropdownExpanded = true }
                                )
                                DropdownMenu(
                                    expanded = dropdownExpanded,
                                    onDismissRequest = { dropdownExpanded = false }
                                ) {
                                    subjects.forEach { sub ->
                                        DropdownMenuItem(
                                            text = { Text(sub.name, fontSize = 12.sp) },
                                            onClick = {
                                                selectedExtractedSubject = sub
                                                dropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = result.assessmentName,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Nombre de Evaluación", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1.5f),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                                    colors = OutlinedTextFieldDefaults.colors(disabledTextColor = NavyBlue)
                                )

                                OutlinedTextField(
                                    value = "${result.percentage}%",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Peso", fontSize = 10.sp) },
                                    modifier = Modifier.weight(0.7f),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                                    colors = OutlinedTextFieldDefaults.colors(disabledTextColor = NavyBlue)
                                )

                                OutlinedTextField(
                                    value = result.examDate,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Fecha", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1.5f),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                                    colors = OutlinedTextFieldDefaults.colors(disabledTextColor = NavyBlue)
                                )
                            }

                            if (result.reviewTopics.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Temas de Repaso Sugeridos:",
                                    fontWeight = FontWeight.Bold,
                                    color = NavyBlue,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    result.reviewTopics.forEach { topic ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(start = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = DarkGreen,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = topic,
                                                color = NavyBlue,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    selectedExtractedSubject?.let { sub ->
                                        // 1. Add assessment
                                        viewModel.addAssessment(
                                            subjectId = sub.id,
                                            name = result.assessmentName,
                                            grade = null,
                                            percentage = result.percentage,
                                            examDate = result.examDate
                                        )
                                        // 2. Add each topic as a task
                                        result.reviewTopics.forEach { topic ->
                                            viewModel.addTask(
                                                subjectId = sub.id,
                                                title = "Repasar: $topic (${result.assessmentName})",
                                                type = "Examen",
                                                dueDate = result.examDate
                                            )
                                        }
                                        viewModel.clearExtractedResult()
                                        rawInputText = ""
                                        isExtractorExpanded = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Confirmar y Registrar en mi Agenda", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp)
            ) {
                items(chatMessages) { msg ->
                    ChatBubble(message = msg)
                }
                if (isTutorTyping) {
                    item {
                        TypingIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: com.aistudio.unibuddy.qywvsp.data.ChatMessage) {
    val isUser = message.isUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ProBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "IA", tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Surface(
            color = if (isUser) ProBlue else Color.White,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            shadowElevation = if (isUser) 0.dp else 2.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                color = if (isUser) Color.White else NavyBlue,
                fontSize = 14.sp,
                modifier = Modifier.padding(12.dp),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(ProBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = "IA", tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.LightGray))
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.LightGray))
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.LightGray))
            }
        }
    }
}
