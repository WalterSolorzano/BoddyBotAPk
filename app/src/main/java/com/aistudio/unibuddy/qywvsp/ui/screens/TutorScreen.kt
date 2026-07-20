package com.aistudio.unibuddy.qywvsp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
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
