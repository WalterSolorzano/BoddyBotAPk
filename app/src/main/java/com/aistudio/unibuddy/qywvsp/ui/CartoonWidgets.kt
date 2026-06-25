package com.aistudio.unibuddy.qywvsp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.getValue
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import com.aistudio.unibuddy.qywvsp.ui.theme.*
import com.aistudio.unibuddy.qywvsp.data.Subject
import com.aistudio.unibuddy.qywvsp.data.ClassSessionDetails
import com.aistudio.unibuddy.qywvsp.data.parseSessions

@Composable
fun RainOverlay() {
    val infiniteTransition = rememberInfiniteTransition()
    val yOffset by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rain"
    )

    Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.1f))) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            for (i in 0..20) {
                val startX = (w / 20) * i + (i * 5)
                val dropY = (yOffset + (i * 40)) % h
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.6f),
                    start = Offset(startX, dropY),
                    end = Offset(startX - 5f, dropY + 15f),
                    strokeWidth = 3f
                )
            }
        }
    }
}

data class CartoonWidgetData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val bgColor: Color,
    val borderColor: Color,
    val iconBgColor: Color,
    val isAlert: Boolean = false
)

@Composable
fun DashboardCartoonWidgets(widgets: List<CartoonWidgetData>, isRaining: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth()) {
        val pagerState = rememberPagerState(pageCount = { widgets.size })
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 8.dp),
            pageSpacing = 16.dp
        ) { page ->
            val widget = widgets[page]
            val shadowModifier = if (widget.isAlert) {
                Modifier.shadow(16.dp, RoundedCornerShape(24.dp), ambientColor = widget.borderColor, spotColor = widget.borderColor)
            } else {
                Modifier.shadow(6.dp, RoundedCornerShape(24.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .then(shadowModifier)
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = widget.bgColor),
                    border = BorderStroke(if (widget.isAlert) 4.dp else 3.dp, widget.borderColor)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(widget.iconBgColor, RoundedCornerShape(16.dp))
                                    .border(2.dp, widget.borderColor, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = widget.icon,
                                    contentDescription = null,
                                    tint = widget.borderColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = widget.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = widget.borderColor,
                                    textAlign = TextAlign.Center,
                                    style = androidx.compose.ui.text.TextStyle(
                                        shadow = if (widget.isAlert) androidx.compose.ui.graphics.Shadow(
                                            color = widget.borderColor,
                                            blurRadius = 12f
                                        ) else null
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = widget.description,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = widget.borderColor,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp,
                                    maxLines = 3,
                                    style = androidx.compose.ui.text.TextStyle(
                                        shadow = if (widget.isAlert) androidx.compose.ui.graphics.Shadow(
                                            color = widget.borderColor.copy(alpha = 0.5f),
                                            blurRadius = 8f
                                        ) else null
                                    )
                                )
                            }
                        }

                        if (isRaining) {
                            RainOverlay()
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(widgets.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) NavyBlue else SlateGray.copy(alpha = 0.5f)
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }
    }
}

@Composable
fun CartoonScheduleWidget(
    subjects: List<com.aistudio.unibuddy.qywvsp.data.Subject>,
    modifier: Modifier = Modifier
) {
    val calendar = java.util.Calendar.getInstance()
    val todayCode = when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
        java.util.Calendar.MONDAY -> "Lu"
        java.util.Calendar.TUESDAY -> "Ma"
        java.util.Calendar.WEDNESDAY -> "Mi"
        java.util.Calendar.THURSDAY -> "Ju"
        java.util.Calendar.FRIDAY -> "Vi"
        java.util.Calendar.SATURDAY -> "Sá"
        else -> "Lu"
    }

    val todayClasses = remember(subjects) {
        val list = mutableListOf<Pair<com.aistudio.unibuddy.qywvsp.data.Subject, com.aistudio.unibuddy.qywvsp.data.ClassSessionDetails>>()
        subjects.forEach { sub ->
            sub.sessionsJson.parseSessions().forEach { session ->
                if (session.day.equals(todayCode, ignoreCase = true)) {
                    list.add(Pair(sub, session))
                }
            }
        }
        list.sortedBy { it.second.time }
    }

    // Interactive bounce/rotation animation for cartoon comic feel
    val infiniteTransition = rememberInfiniteTransition(label = "cartoon_bounce")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    val scaleFactor by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                rotationZ = rotationAngle
                scaleX = scaleFactor
                scaleY = scaleFactor
            }
            .shadow(12.dp, RoundedCornerShape(28.dp), ambientColor = NavyBlue, spotColor = NavyBlue)
            .background(Color(0xFFFFFDE7), RoundedCornerShape(28.dp)) // Vibrant comic yellow paper
            .border(4.dp, NavyBlue, RoundedCornerShape(28.dp))
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Terracotta, CircleShape)
                            .border(2.dp, NavyBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HORARIO DE HOY",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyBlue,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    color = MintGreen,
                    border = BorderStroke(2.dp, NavyBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = when(todayCode) {
                            "Lu" -> "LUNES"
                            "Ma" -> "MARTES"
                            "Mi" -> "MIÉRCOLES"
                            "Ju" -> "JUEVES"
                            "Vi" -> "VIERNES"
                            "Sá" -> "SÁBADO"
                            else -> "DOMINGO"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (todayClasses.isEmpty()) {
                // Free day state
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .border(2.dp, NavyBlue, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "¡DÍA LIBRE!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = ProBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No tienes materias registradas hoy.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateGray
                        )
                    }
                }
            } else {
                // Today's classes
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    todayClasses.forEach { pair ->
                        val sub = pair.first
                        val session = pair.second
                        val (bgColor, accentColor) = getSubjectColorPalette(sub.colorHex)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(16.dp))
                                .background(bgColor, RoundedCornerShape(16.dp))
                                .border(3.dp, NavyBlue, RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = sub.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = NavyBlue,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Aula: ${session.room}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SlateGray
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Surface(
                                        color = Color.White,
                                        border = BorderStroke(2.dp, NavyBlue),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = session.time,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = ProBlue,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

