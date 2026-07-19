package com.aistudio.unibuddy.qywvsp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.aistudio.unibuddy.qywvsp.ui.theme.NavyBlue

@Composable
fun BuddyMascot(
    modifier: Modifier = Modifier,
    isWorried: Boolean = false,
    isHappy: Boolean = true,
    pose: String = "idle", // "idle", "greeting", "working", "sleeping", "celebrating", "exam"
    accessory: String = "none", // "none", "hat", "glasses", "scarf", "sunglasses"
    weatherState: String = "clear", // "clear", "rainy", "sunny", "night"
    mainColor: Color = NavyBlue
) {
    val infiniteTransition = rememberInfiniteTransition(label = "buddy_bob")
    val duration = when (pose) {
        "sleeping" -> 3000
        "celebrating", "working" -> 1000
        else -> 1800
    }
    val maxOffset = when (pose) {
        "sleeping" -> 1.5f
        "celebrating" -> 5f
        else -> 3f
    }
    
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = -maxOffset,
        targetValue = maxOffset,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )

    // Blinking animation
    val blinkProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                0f at 0
                0f at 3800
                1f at 3900 // close eyes
                0f at 4000 // open eyes
            }
        ),
        label = "blink"
    )

    Box(
        modifier = modifier
            .size(100.dp)
            .background(Color.White, shape = CircleShape)
            .border(2.dp, mainColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp).offset(y = bobOffset.dp)) {
            val w = size.width
            val h = size.height

            // Ears
            drawCircle(color = Color.White, radius = w * 0.15f, center = Offset(w * 0.25f, h * 0.25f))
            drawCircle(color = mainColor, radius = w * 0.15f, center = Offset(w * 0.25f, h * 0.25f), style = Stroke(width = 4f))
            drawCircle(color = Color(0xFFFFB7B2), radius = w * 0.08f, center = Offset(w * 0.25f, h * 0.25f))

            drawCircle(color = Color.White, radius = w * 0.15f, center = Offset(w * 0.75f, h * 0.25f))
            drawCircle(color = mainColor, radius = w * 0.15f, center = Offset(w * 0.75f, h * 0.25f), style = Stroke(width = 4f))
            drawCircle(color = Color(0xFFFFB7B2), radius = w * 0.08f, center = Offset(w * 0.75f, h * 0.25f))

            // Draw arms in back for certain poses
            if (pose == "greeting" || pose == "celebrating") {
                val armPath = Path().apply {
                    moveTo(w * 0.8f, h * 0.6f)
                    quadraticTo(w * 1.0f, h * 0.4f, w * 0.9f, h * (if(pose=="celebrating") 0.1f else 0.2f))
                }
                drawPath(path = armPath, color = mainColor, style = Stroke(width = 12f, cap = StrokeCap.Round))
                drawPath(path = armPath, color = Color.White, style = Stroke(width = 6f, cap = StrokeCap.Round))
                
                if (pose == "celebrating") {
                    val armPath2 = Path().apply {
                        moveTo(w * 0.2f, h * 0.6f)
                        quadraticTo(w * 0.0f, h * 0.4f, w * 0.1f, h * 0.1f)
                    }
                    drawPath(path = armPath2, color = mainColor, style = Stroke(width = 12f, cap = StrokeCap.Round))
                    drawPath(path = armPath2, color = Color.White, style = Stroke(width = 6f, cap = StrokeCap.Round))
                }
            }

            // Head Base
            drawCircle(color = Color.White, radius = w * 0.35f, center = Offset(w * 0.5f, h * 0.52f))
            drawCircle(color = mainColor, radius = w * 0.35f, center = Offset(w * 0.5f, h * 0.52f), style = Stroke(width = 4.0f))

            // Yellow Construction Helmet
            if (accessory == "hat") {
                val helmetPath = Path().apply {
                    arcTo(
                        rect = Rect(w * 0.18f, h * 0.15f, w * 0.82f, h * 0.48f),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 180f,
                        forceMoveTo = false
                    )
                    lineTo(w * 0.85f, h * 0.40f)
                    lineTo(w * 0.15f, h * 0.40f)
                    close()
                }
                drawPath(path = helmetPath, color = Color(0xFFF9C74F))
                drawPath(path = helmetPath, color = mainColor, style = Stroke(width = 4.0f))

                // Helmet Top ridge
                drawRect(
                    color = Color(0xFFF9C74F),
                    topLeft = Offset(w * 0.43f, h * 0.12f),
                    size = Size(w * 0.14f, h * 0.12f)
                )
                drawRect(
                    color = mainColor,
                    topLeft = Offset(w * 0.43f, h * 0.12f),
                    size = Size(w * 0.14f, h * 0.12f),
                    style = Stroke(width = 4.0f)
                )
            } else if (accessory == "cap") {
                val capColor = Color(0xFFEF5350)
                val capPath = Path().apply {
                    arcTo(
                        rect = Rect(w * 0.2f, h * 0.18f, w * 0.8f, h * 0.48f),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 180f,
                        forceMoveTo = false
                    )
                    lineTo(w * 0.8f, h * 0.40f)
                    lineTo(w * 0.2f, h * 0.40f)
                    close()
                }
                drawPath(path = capPath, color = capColor)
                drawPath(path = capPath, color = mainColor, style = Stroke(width = 3.0f))
                
                // Visor
                val visorPath = Path().apply {
                    moveTo(w * 0.8f, h * 0.38f)
                    quadraticTo(w * 0.95f, h * 0.35f, w * 0.98f, h * 0.40f)
                    lineTo(w * 0.8f, h * 0.40f)
                }
                drawPath(path = visorPath, color = capColor)
                drawPath(path = visorPath, color = mainColor, style = Stroke(width = 3.0f))
            }

            if (pose == "working" || pose == "exam" || accessory == "glasses") {
                // Draw some glasses
                drawCircle(color = mainColor, radius = w * 0.12f, center = Offset(w * 0.38f, h * 0.48f), style = Stroke(width = 4f))
                drawCircle(color = mainColor, radius = w * 0.12f, center = Offset(w * 0.62f, h * 0.48f), style = Stroke(width = 4f))
                drawLine(color = mainColor, start = Offset(w * 0.44f, h * 0.48f), end = Offset(w * 0.56f, h * 0.48f), strokeWidth = 4f)
            } else if (accessory == "sunglasses") {
                // Sunglasses: filled dark lenses with reflective details!
                val darkGlassColor = Color(0xFF212121)
                drawCircle(color = darkGlassColor, radius = w * 0.12f, center = Offset(w * 0.38f, h * 0.48f))
                drawCircle(color = darkGlassColor, radius = w * 0.12f, center = Offset(w * 0.62f, h * 0.48f))
                drawLine(color = darkGlassColor, start = Offset(w * 0.44f, h * 0.48f), end = Offset(w * 0.56f, h * 0.48f), strokeWidth = 6f)
                
                // Reflection glare effect
                drawLine(color = Color.White.copy(alpha = 0.8f), start = Offset(w * 0.35f, h * 0.44f), end = Offset(w * 0.40f, h * 0.52f), strokeWidth = 3f)
                drawLine(color = Color.White.copy(alpha = 0.8f), start = Offset(w * 0.59f, h * 0.44f), end = Offset(w * 0.64f, h * 0.52f), strokeWidth = 3f)
            }

            // Scarf
            if (accessory == "scarf") {
                val scarfPath = Path().apply {
                    moveTo(w * 0.15f, h * 0.65f)
                    lineTo(w * 0.85f, h * 0.65f)
                    lineTo(w * 0.80f, h * 0.75f)
                    lineTo(w * 0.20f, h * 0.75f)
                    close()
                }
                drawPath(path = scarfPath, color = Color(0xFFE57373))
                drawPath(path = scarfPath, color = mainColor, style = Stroke(width = 3f))
            }

            // Eyes
            val eyeRadius = w * 0.045f
            if (pose == "sleeping" || blinkProgress > 0.5f) {
                drawLine(color = mainColor, start = Offset(w * 0.33f, h * 0.48f), end = Offset(w * 0.43f, h * 0.48f), strokeWidth = 5f)
                drawLine(color = mainColor, start = Offset(w * 0.57f, h * 0.48f), end = Offset(w * 0.67f, h * 0.48f), strokeWidth = 5f)
            } else if (isWorried) {
                // Worried/Sad eyes: slanted lines (/\ or \/)
                drawLine(color = mainColor, start = Offset(w * 0.34f, h * 0.51f), end = Offset(w * 0.42f, h * 0.47f), strokeWidth = 5f)
                drawLine(color = mainColor, start = Offset(w * 0.66f, h * 0.51f), end = Offset(w * 0.58f, h * 0.47f), strokeWidth = 5f)
            } else {
                drawCircle(color = mainColor, radius = eyeRadius, center = Offset(w * 0.38f, h * 0.48f))
                drawCircle(color = mainColor, radius = eyeRadius, center = Offset(w * 0.62f, h * 0.48f))
                drawCircle(color = Color.White, radius = eyeRadius * 0.4f, center = Offset(w * 0.36f, h * 0.46f))
                drawCircle(color = Color.White, radius = eyeRadius * 0.4f, center = Offset(w * 0.60f, h * 0.46f))
            }

            // Snout
            drawOval(
                color = Color(0xFFF1EEE5),
                topLeft = Offset(w * 0.41f, h * 0.53f),
                size = Size(w * 0.18f, h * 0.13f)
            )
            drawOval(
                color = mainColor,
                topLeft = Offset(w * 0.41f, h * 0.53f),
                size = Size(w * 0.18f, h * 0.13f),
                style = Stroke(width = 3.0f)
            )

            // Nose
            drawCircle(color = mainColor, radius = w * 0.04f, center = Offset(w * 0.5f, h * 0.56f))

            // Mouth
            if (isHappy && !isWorried && pose != "sleeping") {
                val mouthPath = Path().apply {
                    moveTo(w * 0.45f, h * 0.59f)
                    quadraticTo(w * 0.5f, h * 0.64f, w * 0.55f, h * 0.59f)
                }
                drawPath(path = mouthPath, color = mainColor, style = Stroke(width = 3.0f))
            } else if (isWorried) {
                // Sad mouth curve!
                val mouthPath = Path().apply {
                    moveTo(w * 0.45f, h * 0.62f)
                    quadraticTo(w * 0.5f, h * 0.58f, w * 0.55f, h * 0.62f)
                }
                drawPath(path = mouthPath, color = mainColor, style = Stroke(width = 3.0f))
            } else if (pose == "sleeping") {
                drawCircle(color = mainColor, radius = w * 0.03f, center = Offset(w * 0.5f, h * 0.62f), style = Stroke(width = 2f))
            } else {
                drawLine(color = mainColor, start = Offset(w * 0.46f, h * 0.60f), end = Offset(w * 0.54f, h * 0.60f), strokeWidth = 3f)
            }

            // Draw Book if in Exam mode
            if (pose == "exam") {
                val bookPath = Path().apply {
                    moveTo(w * 0.35f, h * 0.72f)
                    lineTo(w * 0.5f, h * 0.77f)
                    lineTo(w * 0.65f, h * 0.72f)
                    lineTo(w * 0.62f, h * 0.85f)
                    lineTo(w * 0.5f, h * 0.89f)
                    lineTo(w * 0.38f, h * 0.85f)
                    close()
                }
                drawPath(path = bookPath, color = Color(0xFFF28B82)) // Pretty warm red book
                drawPath(path = bookPath, color = mainColor, style = Stroke(width = 3f))
                
                // Book middle line spine
                drawLine(color = mainColor, start = Offset(w * 0.5f, h * 0.77f), end = Offset(w * 0.5f, h * 0.89f), strokeWidth = 3f)
                
                // Page text lines
                drawLine(color = Color.White.copy(alpha = 0.85f), start = Offset(w * 0.40f, h * 0.78f), end = Offset(w * 0.47f, h * 0.80f), strokeWidth = 2f)
                drawLine(color = Color.White.copy(alpha = 0.85f), start = Offset(w * 0.40f, h * 0.82f), end = Offset(w * 0.47f, h * 0.84f), strokeWidth = 2f)
                drawLine(color = Color.White.copy(alpha = 0.85f), start = Offset(w * 0.53f, h * 0.80f), end = Offset(w * 0.60f, h * 0.78f), strokeWidth = 2f)
                drawLine(color = Color.White.copy(alpha = 0.85f), start = Offset(w * 0.53f, h * 0.84f), end = Offset(w * 0.60f, h * 0.82f), strokeWidth = 2f)
            }

            // Weather skin/traje
            if (weatherState == "rainy") {
                // Draw a cute yellow raincoat (poncho style to not cover too much)
                val raincoatPath = Path().apply {
                    moveTo(w * 0.25f, h * 0.58f) // left shoulder
                    quadraticTo(w * 0.15f, h * 0.75f, w * 0.18f, h * 0.85f)
                    lineTo(w * 0.82f, h * 0.85f) // bottom hem
                    quadraticTo(w * 0.85f, h * 0.75f, w * 0.75f, h * 0.58f) // right shoulder
                    quadraticTo(w * 0.50f, h * 0.68f, w * 0.25f, h * 0.58f) // collar
                    close()
                }
                drawPath(path = raincoatPath, color = Color(0xFFFFD54F)) // Yellow
                drawPath(path = raincoatPath, color = mainColor, style = Stroke(width = 3f))
                
                // Raincoat buttons
                drawCircle(color = mainColor, radius = w * 0.02f, center = Offset(w * 0.5f, h * 0.68f))
                drawCircle(color = mainColor, radius = w * 0.02f, center = Offset(w * 0.5f, h * 0.76f))
            } else if (weatherState == "night") {
                // Cute sleeping cap or moon symbol on chest? Let's keep it simple.
                // Just a tiny star on the chest.
                val starPath = Path().apply {
                    moveTo(w * 0.5f, h * 0.68f)
                    lineTo(w * 0.52f, h * 0.72f)
                    lineTo(w * 0.57f, h * 0.72f)
                    lineTo(w * 0.53f, h * 0.75f)
                    lineTo(w * 0.55f, h * 0.80f)
                    lineTo(w * 0.5f, h * 0.77f)
                    lineTo(w * 0.45f, h * 0.80f)
                    lineTo(w * 0.47f, h * 0.75f)
                    lineTo(w * 0.43f, h * 0.72f)
                    lineTo(w * 0.48f, h * 0.72f)
                    close()
                }
                drawPath(path = starPath, color = Color(0xFFF9C74F))
            }
        }
    }
}
