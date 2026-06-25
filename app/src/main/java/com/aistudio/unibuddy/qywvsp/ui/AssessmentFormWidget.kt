package com.aistudio.unibuddy.qywvsp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.unibuddy.qywvsp.data.Subject
import com.aistudio.unibuddy.qywvsp.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentFormWidget(
    subject: Subject,
    onSave: suspend (name: String, grade: Double?, percent: Double, dateStr: String) -> Unit
) {
    val context = LocalContext.current
    var isExamType by rememberSaveable { mutableStateOf(true) } // true: Examen, false: Trabajo
    var examName by rememberSaveable { mutableStateOf("") }
    var examGrade by rememberSaveable { mutableStateOf("") }
    
    // Auto percentage based on type
    var customPercent by rememberSaveable { mutableStateOf("") }
    val effectivePercent = customPercent.ifBlank { if (isExamType) "15" else "7" }
    
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var selectedDateStr by rememberSaveable { mutableStateOf(dateFormatter.format(Calendar.getInstance().time)) }
    
    var examNameError by rememberSaveable { mutableStateOf(false) }
    var examGradeError by rememberSaveable { mutableStateOf(false) }
    var isSaving by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Calendar.getInstance().timeInMillis
    )

    if (showDatePicker) {
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let {
                        val cal = Calendar.getInstance().apply { timeInMillis = it }
                        selectedDateStr = dateFormatter.format(cal.time)
                    }
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            androidx.compose.material3.DatePicker(state = datePickerState)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, Bone)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Registrar Evaluación", style = MaterialTheme.typography.titleMedium, color = NavyBlue, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // Type Selector
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = isExamType,
                    onClick = { if (!isSaving) isExamType = true },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("Examen")
                }
                SegmentedButton(
                    selected = !isExamType,
                    onClick = { if (!isSaving) isExamType = false },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("Trabajo")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = examName,
                onValueChange = { 
                    examName = it
                    examNameError = it.isBlank()
                },
                label = { Text(if (isExamType) "Nombre del examen" else "Nombre del trabajo") },
                isError = examNameError,
                supportingText = { if (examNameError) Text("El nombre es requerido", color = Terracotta) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DarkGreen, focusedLabelColor = DarkGreen)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Date Picker Field
            OutlinedTextField(
                value = selectedDateStr,
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha programada") },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (!isSaving) showDatePicker = true
                        }
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = "Seleccionar Fecha", tint = DarkGreen)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DarkGreen, focusedLabelColor = DarkGreen)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = examGrade,
                    onValueChange = { 
                        val filtered = it.filter { char -> char.isDigit() || char == '.' }
                        if (filtered.count { char -> char == '.' } <= 1) {
                            examGrade = filtered
                            examGradeError = examGrade.isNotEmpty() && examGrade.toDoubleOrNull() == null
                        }
                    },
                    label = { Text("Nota (ej: 75.0)") },
                    placeholder = { Text("Vacío si pendiente") },
                    isError = examGradeError,
                    supportingText = { if (examGradeError) Text("Formato inválido", color = Terracotta) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DarkGreen, focusedLabelColor = DarkGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = effectivePercent,
                    onValueChange = { customPercent = it.filter { char -> char.isDigit() } },
                    label = { Text("Porcentaje (%)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DarkGreen, focusedLabelColor = DarkGreen)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val hapticFeedback = LocalHapticFeedback.current
            Button(
                onClick = {
                    if (isSaving) return@Button
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    examNameError = examName.isBlank()
                    examGradeError = examGrade.isNotEmpty() && examGrade.toDoubleOrNull() == null

                    if (!examNameError && !examGradeError) {
                        isSaving = true
                        coroutineScope.launch {
                            try {
                                val percentVal = effectivePercent.toDoubleOrNull() ?: if (isExamType) 15.0 else 7.0
                                val gradeVal = examGrade.toDoubleOrNull()
                                val finalName = if (isExamType) "Examen: $examName" else "Trabajo: $examName"
                                onSave(finalName, gradeVal, percentVal, selectedDateStr)
                                
                                // Reset state
                                examName = ""
                                examGrade = ""
                                customPercent = ""
                                examNameError = false
                                examGradeError = false
                                delay(500) // simple debounce
                            } finally {
                                isSaving = false
                            }
                        }
                    }
                },
                modifier = Modifier.align(Alignment.End),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
            ) {
                Text(if (isSaving) "Guardando..." else "Guardar", color = Bone)
            }
        }
    }
}
