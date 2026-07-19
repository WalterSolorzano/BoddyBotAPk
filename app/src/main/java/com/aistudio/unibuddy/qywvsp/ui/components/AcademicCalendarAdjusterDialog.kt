package com.aistudio.unibuddy.qywvsp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aistudio.unibuddy.qywvsp.ui.UniBuddyViewModel
import com.aistudio.unibuddy.qywvsp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicCalendarAdjusterDialog(
    viewModel: UniBuddyViewModel,
    onDismiss: () -> Unit
) {
    val calendarState by viewModel.academicCalendarState.collectAsStateWithLifecycle()
    val buddyColorStr by viewModel.buddyColor.collectAsStateWithLifecycle()
    val mainColor = try { Color(android.graphics.Color.parseColor(buddyColorStr)) } catch(e: Exception) { ProBlue }
    
    val scrollState = rememberScrollState()
    
    var selectedWeek by remember(calendarState.academicWeekNumber) {
        mutableStateOf(calendarState.academicWeekNumber)
    }
    
    var isSuspendedCurrentWeek by remember(calendarState.isRecessWeek, calendarState.recessReason) {
        mutableStateOf(calendarState.isRecessWeek && calendarState.recessReason == "Clases Suspendidas")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    viewModel.adjustWeekNumber(selectedWeek)
                    viewModel.dismissPrompt()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = mainColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar Cambios", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = SlateGray, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.EditCalendar,
                    contentDescription = null,
                    tint = mainColor,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Ajustar Calendario UNI 2026",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NavyBlue
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Intro text from Buddy
                Card(
                    colors = CardDefaults.cardColors(containerColor = mainColor.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            BuddyMascot(
                                pose = "idle",
                                isHappy = true,
                                mainColor = mainColor,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "¡Hola! Soy tu UniBuddy.",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyBlue
                            )
                            Text(
                                text = "El calendario base es el oficial de la UNI, pero la realidad cambia. ¡Ajústame según tu semana real!",
                                fontSize = 11.sp,
                                color = SlateGray,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                // Parity adjustment
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "PARIDAD DE LA SEMANA ACTUAL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SlateGray,
                        letterSpacing = 1.sp
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.forceParity("Impar") },
                            colors = CardDefaults.cardColors(
                                containerColor = if (!calendarState.isEvenWeek) mainColor.copy(alpha = 0.15f) else Color(0xFFF8FAFC)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (!calendarState.isEvenWeek) mainColor else Bone
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.FilterAlt,
                                    contentDescription = null,
                                    tint = if (!calendarState.isEvenWeek) mainColor else SlateGray,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Semana Impar",
                                    fontWeight = FontWeight.Bold,
                                    color = if (!calendarState.isEvenWeek) NavyBlue else SlateGray,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.forceParity("Par") },
                            colors = CardDefaults.cardColors(
                                containerColor = if (calendarState.isEvenWeek) mainColor.copy(alpha = 0.15f) else Color(0xFFF8FAFC)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (calendarState.isEvenWeek) mainColor else Bone
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.FilterCenterFocus,
                                    contentDescription = null,
                                    tint = if (calendarState.isEvenWeek) mainColor else SlateGray,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Semana Par",
                                    fontWeight = FontWeight.Bold,
                                    color = if (calendarState.isEvenWeek) NavyBlue else SlateGray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Week number adjustment
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NÚMERO DE SEMANA ACTUAL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SlateGray,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Semana $selectedWeek",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = mainColor
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { if (selectedWeek > 1) selectedWeek-- },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Bone)
                        ) {
                            Icon(Icons.Rounded.Remove, contentDescription = "Decrease week", tint = NavyBlue)
                        }
                        
                        Slider(
                            value = selectedWeek.toFloat(),
                            onValueChange = { selectedWeek = it.toInt() },
                            valueRange = 1f..17f,
                            steps = 15,
                            colors = SliderDefaults.colors(
                                thumbColor = mainColor,
                                activeTrackColor = mainColor,
                                inactiveTrackColor = Bone
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = { if (selectedWeek < 17) selectedWeek++ },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Bone)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Increase week", tint = NavyBlue)
                        }
                    }
                }

                // Freeze/suspension switch
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Bone),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PauseCircle,
                                contentDescription = null,
                                tint = if (isSuspendedCurrentWeek) Terracotta else SlateGray
                            )
                            Column {
                                Text(
                                    text = "¿Sin clases esta semana?",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyBlue
                                )
                                Text(
                                    text = "Congela el contador de semanas lectivas.",
                                    fontSize = 10.sp,
                                    color = SlateGray
                                )
                            }
                        }
                        Switch(
                            checked = isSuspendedCurrentWeek,
                            onCheckedChange = { checked ->
                                isSuspendedCurrentWeek = checked
                                viewModel.toggleCurrentWeekSuspended()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Terracotta,
                                uncheckedThumbColor = SlateGray,
                                uncheckedTrackColor = Bone
                            )
                        )
                    }
                }

                // Official Base Calendar Reference Block
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "CALENDARIO OFICIAL DE REFERENCIA (UNI 2026)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SlateGray,
                        letterSpacing = 1.sp
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CalendarInfoRow(title = "I Semestre clases", dates = "07 Mar - 01 May", isCurrent = calendarState.semesterName == "I Semestre 2026" && calendarState.academicWeekNumber <= 7)
                        CalendarInfoRow(title = "I Semestre exámenes", dates = "02 May - 15 May", isCurrent = calendarState.semesterName == "I Semestre 2026" && calendarState.academicWeekNumber in 8..9)
                        CalendarInfoRow(title = "II Semestre clases", dates = "25 Jul - 04 Sep", isCurrent = calendarState.semesterName == "II Semestre 2026" && calendarState.academicWeekNumber <= 6)
                        CalendarInfoRow(title = "II Semestre exámenes", dates = "05 Sep - 25 Sep", isCurrent = calendarState.semesterName == "II Semestre 2026" && calendarState.academicWeekNumber in 7..8)
                        CalendarInfoRow(title = "Semana Santa (Feriado)", dates = "30 Mar - 05 Abr", isCurrent = calendarState.isRecessWeek && calendarState.recessReason == "Semana Santa")
                        CalendarInfoRow(title = "Fiestas Patrias (Feriado)", dates = "12 Sep - 18 Sep", isCurrent = calendarState.isRecessWeek && calendarState.recessReason == "Fiestas Patrias")
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
fun CalendarInfoRow(
    title: String,
    dates: String,
    isCurrent: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (isCurrent) Color.White else Color.Transparent)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isCurrent) ProBlue else SlateGray.copy(alpha = 0.5f))
            )
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                color = if (isCurrent) NavyBlue else SlateGray
            )
        }
        Text(
            text = dates,
            fontSize = 9.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            color = if (isCurrent) ProBlue else SlateGray
        )
    }
}
