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
import android.app.DatePickerDialog

@Composable
fun AssessmentFormWidget(
    subject: Subject,
    onSave: (name: String, grade: Double?, percent: Double, dateStr: String) -> Unit
) {
    val context = LocalContext.current
    var isExamType by remember { mutableStateOf(true) } // true: Examen, false: Trabajo
    var examName by remember { mutableStateOf("") }
    var examGrade by remember { mutableStateOf("") }
    
    // Auto percentage based on type
    var customPercent by remember { mutableStateOf("") }
    val effectivePercent = customPercent.ifBlank { if (isExamType) "15" else "7" }
    
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var selectedDateStr by remember { mutableStateOf(dateFormatter.format(Calendar.getInstance().time)) }
    
    var examNameError by remember { mutableStateOf(false) }

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
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                        .background(if (isExamType) DarkGreen else Bone)
                        .clickable { isExamType = true }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Examen", color = if (isExamType) Bone else SlateGray, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                        .background(if (!isExamType) DarkGreen else Bone)
                        .clickable { isExamType = false }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Trabajo", color = if (!isExamType) Bone else SlateGray, fontWeight = FontWeight.Bold)
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
                    IconButton(onClick = {
                        val cal = Calendar.getInstance()
                        DatePickerDialog(context, { _, year, month, dayOfMonth ->
                            val selectedCal = Calendar.getInstance()
                            selectedCal.set(year, month, dayOfMonth)
                            selectedDateStr = dateFormatter.format(selectedCal.time)
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                    }) {
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
                    onValueChange = { examGrade = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Nota (ej: 75.0)") },
                    placeholder = { Text("Vacío si pendiente") },
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
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    examNameError = examName.isBlank()

                    if (examName.isNotBlank()) {
                        val percentVal = effectivePercent.toDoubleOrNull() ?: if (isExamType) 15.0 else 7.0
                        val gradeVal = examGrade.toDoubleOrNull()
                        val finalName = if (isExamType) "Examen: $examName" else "Trabajo: $examName"
                        onSave(finalName, gradeVal, percentVal, selectedDateStr)
                        examName = ""
                        examGrade = ""
                        customPercent = ""
                        examNameError = false
                    }
                },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
            ) {
                Text("Guardar", color = Bone)
            }
        }
    }
}
