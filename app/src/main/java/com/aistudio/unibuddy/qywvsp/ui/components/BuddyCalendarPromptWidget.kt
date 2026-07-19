package com.aistudio.unibuddy.qywvsp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aistudio.unibuddy.qywvsp.ui.UniBuddyViewModel
import com.aistudio.unibuddy.qywvsp.ui.theme.*

@Composable
fun BuddyCalendarPromptWidget(
    viewModel: UniBuddyViewModel,
    modifier: Modifier = Modifier
) {
    val activePrompt by viewModel.activeCalendarPrompt.collectAsStateWithLifecycle()
    val buddyColorStr by viewModel.buddyColor.collectAsStateWithLifecycle()
    val mainColor = try { Color(android.graphics.Color.parseColor(buddyColorStr)) } catch(e: Exception) { ProBlue }
    
    var showAdjusterDialog by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = activePrompt != null,
        enter = slideInVertically(animationSpec = tween(500)) + fadeIn(animationSpec = tween(500)),
        exit = slideOutVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        activePrompt?.let { prompt ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        androidx.compose.foundation.BorderStroke(1.5.dp, mainColor.copy(alpha = 0.4f)),
                        RoundedCornerShape(20.dp)
                    )
                    .testTag("buddy_calendar_prompt_widget"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header with close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarMonth,
                                contentDescription = null,
                                tint = mainColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "AJUSTAR EL RITMO SEMANAL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = mainColor,
                                letterSpacing = 1.sp
                            )
                        }
                        
                        IconButton(
                            onClick = { viewModel.dismissPrompt() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Cerrar prompt",
                                tint = SlateGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Mascot + Question Bubble
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Interactive mascot animation
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(mainColor.copy(alpha = 0.06f))
                                .border(0.5.dp, mainColor.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val poseParam = when (prompt.pose) {
                                "sleeping" -> "sleeping"
                                "celebrating" -> "celebrating"
                                "exam" -> "working"
                                else -> "greeting"
                            }
                            val isWorriedParam = prompt.pose == "exam"
                            BuddyMascot(
                                pose = poseParam,
                                isHappy = prompt.pose != "exam",
                                isWorried = isWorriedParam,
                                mainColor = mainColor,
                                modifier = Modifier.size(54.dp)
                            )
                        }

                        // Bubble background with message
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            shape = RoundedCornerShape(
                                topStart = 4.dp,
                                topEnd = 16.dp,
                                bottomStart = 16.dp,
                                bottomEnd = 16.dp
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = prompt.question,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NavyBlue,
                                modifier = Modifier.padding(12.dp),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // Answer Options (Multi-button row/column)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        prompt.options.forEach { (label, actionId) ->
                            Button(
                                onClick = {
                                    when (actionId) {
                                        "toggle_parity" -> viewModel.toggleParity()
                                        "suspend_week" -> viewModel.toggleCurrentWeekSuspended()
                                        "unsuspend" -> viewModel.toggleCurrentWeekSuspended()
                                        "start_semester" -> {
                                            viewModel.updateSemesterStartDate(System.currentTimeMillis())
                                        }
                                    }
                                    viewModel.dismissPrompt()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (actionId == "confirm") mainColor else Color(0xFFF1F5F9),
                                    contentColor = if (actionId == "confirm") Color.White else NavyBlue
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(vertical = 10.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Small Manual Adjust Button
                        TextButton(
                            onClick = { showAdjusterDialog = true },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                text = "Ajuste Avanzado de Semanas",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = mainColor
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAdjusterDialog) {
        AcademicCalendarAdjusterDialog(
            viewModel = viewModel,
            onDismiss = { showAdjusterDialog = false }
        )
    }
}
