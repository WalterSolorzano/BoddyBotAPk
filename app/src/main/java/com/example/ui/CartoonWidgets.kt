package com.example.ui

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.*

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
