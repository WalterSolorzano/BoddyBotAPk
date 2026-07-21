package com.aistudio.unibuddy.qywvsp.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.aistudio.unibuddy.qywvsp.ui.theme.NavyBlue
import com.aistudio.unibuddy.qywvsp.ui.theme.ProBlue
import com.aistudio.unibuddy.qywvsp.ui.theme.Terracotta

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TutorialScreen(
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val (icon, title, desc, tint) = when (page) {
                    0 -> listOf(
                        Icons.Default.Favorite,
                        "Corazones de Asistencia",
                        "Cada materia tiene un límite de inasistencias. Se representan como corazones. Si pierdes todos, podrías reprobar por faltas. Sé estratégico al faltar.",
                        Terracotta
                    )
                    1 -> listOf(
                        Icons.Default.MonitorHeart,
                        "HP y Estado Crítico",
                        "Tu estrés y rendimiento dictan tus Puntos de Vida (HP). Demasiadas tareas o notas bajas pondrán tu estado y el de tu mascota en 'Crítico'.",
                        ProBlue
                    )
                    2 -> listOf(
                        Icons.Default.AddCircle,
                        "Botón Rápido",
                        "El botón '+' central te permite registrar evaluaciones y marcar faltas en segundos, sin tener que navegar por cada materia.",
                        NavyBlue
                    )
                    else -> listOf(
                        Icons.Default.CheckCircle,
                        "¡Todo Listo!",
                        "Con estos conceptos estás preparado para gestionar tu universidad de forma eficiente. ¡Mucho éxito en el semestre!",
                        Color(0xFF10B981)
                    )
                }

                Icon(
                    imageVector = icon as androidx.compose.ui.graphics.vector.ImageVector,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = tint as Color
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = title as String,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = desc as String,
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }
        }
        
        // Dots Indicator
        Row(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) NavyBlue else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }
        
        Button(
            onClick = {
                if (pagerState.currentPage < 3) {
                    coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                } else {
                    onFinish()
                }
            },
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
        ) {
            Text(if (pagerState.currentPage < 3) "Siguiente" else "Comenzar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
