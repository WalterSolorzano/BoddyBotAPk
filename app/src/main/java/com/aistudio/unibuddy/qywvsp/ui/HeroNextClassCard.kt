package com.aistudio.unibuddy.qywvsp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.unibuddy.qywvsp.data.Subject
import com.aistudio.unibuddy.qywvsp.ui.theme.*
import java.util.Calendar

@Composable
fun HeroNextClassCard(
    subject: Subject?,
    classTime: String,
    faltasRestantes: Int,
    isExamMode: Boolean
) {
    if (subject == null) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = SlateGray, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Día libre", fontSize = 24.sp, fontWeight = FontWeight.Black, color = NavyBlue)
                Text("No tienes más clases programadas para hoy.", color = SlateGray, textAlign = TextAlign.Center)
            }
        }
        return
    }

    val isLate = false // Mocked for now, in reality based on current time vs classTime
    val isCritical = faltasRestantes <= 2
    
    val bgColor = if (isExamMode) Color(0xFFFFF1F1) else if (isLate) Color(0xFFFFF7ED) else Color(0xFFF0FDF4)
    val borderColor = if (isExamMode) Terracotta else if (isLate) Amber else DarkGreen
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .shadow(if (isExamMode) 12.dp else 4.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(2.dp, borderColor),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isExamMode) Icons.Default.Warning else Icons.Default.Schedule,
                        contentDescription = null,
                        tint = borderColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isExamMode) "¡HOY TIENES EXAMEN!" else if (isLate) "¡VAS TARDE!" else "PRÓXIMA CLASE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = borderColor
                    )
                }
                Text(
                    text = classTime,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue,
                    modifier = Modifier.background(Color.White.copy(alpha=0.6f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Subject Name
            Text(
                text = subject.name,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = NavyBlue,
                lineHeight = 32.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Quick Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Should I go? (Attendance Context)
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (isCritical) Icons.Default.Warning else Icons.Default.CheckCircle, null, tint = if (isCritical) Terracotta else DarkGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isCritical) "¡Asiste sí o sí!" else "Faltas: Ok ($faltasRestantes disp)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if(isCritical) Terracotta else DarkGreen)
                    }
                    Text(
                        text = if (isCritical) "Estás al borde de perder por inasistencias." else "Tienes margen para faltar, pero se recomienda ir.",
                        fontSize = 11.sp,
                        color = SlateGray,
                        lineHeight = 14.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Weather Forecast for the Class Time
                val weatherText = "🌤️ 18°C"
                val weatherDesc = "Lleva chaqueta ligera. 10% lluvia."
                Column(
                    modifier = Modifier.weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha=0.5f))
                        .padding(8.dp)
                ) {
                    Text(weatherText, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                    Text(weatherDesc, fontSize = 11.sp, color = SlateGray, lineHeight = 13.sp)
                }
            }
        }
    }
}
