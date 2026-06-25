package com.aistudio.unibuddy.qywvsp.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.unibuddy.qywvsp.data.Subject
import com.aistudio.unibuddy.qywvsp.ui.theme.*
import java.util.Calendar

// Dynamic quotes for the funny notes widget
val FUNNY_COLLEGE_NOTES = listOf(
    "La cafeína no reemplaza 8 horas de sueño... pero sí 4 y un examen.",
    "Tu profesor de matemáticas tiene una variable constante: tu cara de confusión.",
    "La distancia más corta entre dos puntos es el camino que tomas cuando vas tarde a clases.",
    "En mi defensa, el examen tenía preguntas que no estaban en la copia que no leí.",
    "Si pestañeas en la clase de las 7:00 am, despiertas en la tercera semana de exámenes parciales.",
    "Mi billetera se siente como mi promedio: vacía y luchando por existir.",
    "Si no entiendes al profesor, asiente intensamente y di 'ahhh, ya veo'. Funciona el 90% de las veces.",
    "Estudiar 5 minutos antes del examen es confiarle tu futuro al Espíritu Santo y la suerte.",
    "El cerebro es asombroso: funciona 24/7 desde que naces hasta que abres la hoja de examen parcial."
)

// Escape plans / humorous excuses
val ESCAPE_PLANS = listOf(
    "Plan Tos Intensa: Toser 3 veces, mirar el techo y salir corriendo al baño.",
    "Plan Llamada de Emergencia: Vibrar tu celular manualmente, simular pánico y retirarse.",
    "Plan Apagón Mental: Decir que el brillo de la pizarra te causó amnesia temporal.",
    "Plan Perro Hambriento: Asegurar que tu perro se comió el WiFi de tu casa y debes ir a buscarlo.",
    "Plan Clon de Sombra: Dejar una casaca inflada en el asiento y gatear silenciosamente hacia la puerta."
)

/**
 * 1. RUTA DE ESCAPE WIDGET (Escape Route with Animated Driving Car & Mascot)
 */
@Composable
fun CartoonEscapeRouteWidget(
    subjects: List<Subject>,
    modifier: Modifier = Modifier
) {
    var escapePlanIndex by remember { mutableIntStateOf(0) }
    
    // Animation for car bobbing and landscape scrolling
    val infiniteTransition = rememberInfiniteTransition(label = "escape_animation")
    val carBounce by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "car_bounce"
    )
    
    val roadOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -100f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "road_offset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(24.dp), ambientColor = NavyBlue, spotColor = NavyBlue)
            .background(Color(0xFFE0F7FA), RoundedCornerShape(24.dp)) // Anime sky blue
            .border(4.dp, NavyBlue, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFFFB74D), CircleShape)
                            .border(2.dp, NavyBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RUTA DE ESCAPE ACTIVA",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyBlue,
                        letterSpacing = 1.sp
                    )
                }
                
                Surface(
                    color = Color(0xFFFF8A65),
                    border = BorderStroke(2.dp, NavyBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "MODO ESCAPE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Animated landscape with driving car
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(Color(0xFF81C784), RoundedCornerShape(16.dp)) // Cartoon green fields
                    .border(3.dp, NavyBlue, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
            ) {
                // Drawing scenery (mountains, road, scrolling clouds)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // 1. Draw cartoon mountains
                    val mountPath = Path().apply {
                        moveTo(0f, h)
                        lineTo(w * 0.25f, h * 0.3f)
                        lineTo(w * 0.5f, h)
                        moveTo(w * 0.4f, h)
                        lineTo(w * 0.7f, h * 0.4f)
                        lineTo(w * 0.95f, h)
                    }
                    drawPath(path = mountPath, color = Color(0xFF4CAF50))
                    drawPath(path = mountPath, color = NavyBlue, style = Stroke(width = 3f))

                    // 2. Road surface at bottom
                    drawRect(
                        color = Color(0xFF546E7A),
                        topLeft = Offset(0f, h * 0.65f),
                        size = androidx.compose.ui.geometry.Size(w, h * 0.35f)
                    )
                    // Road borders
                    drawLine(
                        color = NavyBlue,
                        start = Offset(0f, h * 0.65f),
                        end = Offset(w, h * 0.65f),
                        strokeWidth = 4f
                    )

                    // Scrolling dotted road lane lines
                    val dotY = h * 0.8f
                    val dotLength = 40f
                    val dotGap = 30f
                    var currentX = roadOffset % (dotLength + dotGap)
                    while (currentX < w) {
                        drawLine(
                            color = Color.White,
                            start = Offset(currentX, dotY),
                            end = Offset(currentX + dotLength, dotY),
                            strokeWidth = 4f
                        )
                        currentX += dotLength + dotGap
                    }
                }

                // Scrolling Clouds
                Box(
                    modifier = Modifier
                        .offset(x = (roadOffset * 0.5f).dp, y = 8.dp)
                        .padding(start = 24.dp)
                ) {
                    Text("             ", fontSize = 18.sp, color = Color.White.copy(alpha = 0.8f))
                }

                // Mascot riding in the cartoon car bouncing
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(x = (-30).dp, y = (carBounce - 4).dp)
                        .width(130.dp)
                        .height(55.dp)
                ) {
                    // Custom drawn cute cartoon sports car + mascot peak
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cw = size.width
                        val ch = size.height

                        // Mascot head popping out of window
                        drawCircle(
                            color = Color.White,
                            radius = 12f,
                            center = Offset(cw * 0.4f, ch * 0.28f)
                        )
                        drawCircle(
                            color = DarkGreen,
                            radius = 12f,
                            center = Offset(cw * 0.4f, ch * 0.28f),
                            style = Stroke(width = 3f)
                        )
                        // Mascot ears
                        drawCircle(color = DarkGreen, radius = 4f, center = Offset(cw * 0.35f, ch * 0.16f))
                        drawCircle(color = DarkGreen, radius = 4f, center = Offset(cw * 0.45f, ch * 0.16f))
                        // Mascot eyes
                        drawCircle(color = NavyBlue, radius = 2f, center = Offset(cw * 0.38f, ch * 0.26f))
                        drawCircle(color = NavyBlue, radius = 2f, center = Offset(cw * 0.42f, ch * 0.26f))

                        // Car Body (Bright red cartoon style)
                        val bodyPath = Path().apply {
                            moveTo(cw * 0.1f, ch * 0.8f)
                            lineTo(cw * 0.1f, ch * 0.55f)
                            quadraticTo(cw * 0.25f, ch * 0.48f, cw * 0.35f, ch * 0.48f)
                            lineTo(cw * 0.55f, ch * 0.48f)
                            quadraticTo(cw * 0.7f, ch * 0.48f, cw * 0.85f, ch * 0.65f)
                            lineTo(cw * 0.92f, ch * 0.65f)
                            quadraticTo(cw * 0.98f, ch * 0.75f, cw * 0.95f, ch * 0.82f)
                            lineTo(cw * 0.95f, ch * 0.88f)
                            lineTo(cw * 0.05f, ch * 0.88f)
                            close()
                        }
                        drawPath(path = bodyPath, color = Color(0xFFEF5350)) // Cartoon Coral Red
                        drawPath(path = bodyPath, color = NavyBlue, style = Stroke(width = 4f))

                        // Car Window
                        val windowPath = Path().apply {
                            moveTo(cw * 0.38f, ch * 0.52f)
                            lineTo(cw * 0.55f, ch * 0.52f)
                            lineTo(cw * 0.62f, ch * 0.62f)
                            lineTo(cw * 0.38f, ch * 0.62f)
                            close()
                        }
                        drawPath(path = windowPath, color = Color(0xFF80DEEA))
                        drawPath(path = windowPath, color = NavyBlue, style = Stroke(width = 3f))

                        // Wheels
                        drawCircle(color = NavyBlue, radius = 10f, center = Offset(cw * 0.25f, ch * 0.88f))
                        drawCircle(color = Color.White, radius = 4f, center = Offset(cw * 0.25f, ch * 0.88f))
                        drawCircle(color = NavyBlue, radius = 10f, center = Offset(cw * 0.75f, ch * 0.88f))
                        drawCircle(color = Color.White, radius = 4f, center = Offset(cw * 0.75f, ch * 0.88f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Humorous excuse section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .border(2.dp, NavyBlue, RoundedCornerShape(12.dp))
                    .clickable {
                        escapePlanIndex = (escapePlanIndex + 1) % ESCAPE_PLANS.size
                    }
                    .padding(10.dp)
            ) {
                Text(
                    text = "PLAN DE ESCAPE SUGERIDO (Toca para cambiar):",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Terracotta
                )
                Spacer(modifier = Modifier.height(4.dp))
                AnimatedContent(
                    targetState = ESCAPE_PLANS[escapePlanIndex],
                    transitionSpec = {
                        slideInVertically { h -> h } + fadeIn() togetherWith
                        slideOutVertically { h -> -h } + fadeOut()
                    },
                    label = "escape_text"
                ) { text ->
                    Text(
                        text = text,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyBlue
                    )
                }
            }
        }
    }
}

/**
 * 2. APUNTES GRACIOSOS WIDGET (Comic Crumbled Notebook Sheet with Jokes)
 */
@Composable
fun CartoonFunnyNotesWidget(
    modifier: Modifier = Modifier
) {
    var noteIndex by remember { mutableIntStateOf(0) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    
    val scope = rememberCoroutineScope()
    val animatedRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                rotationZ = animatedRotation
            }
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .background(Color(0xFFFFFDE7), RoundedCornerShape(16.dp)) // Yellow notebook style
            .border(3.dp, NavyBlue, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        // Spiral binder effect on the left
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .padding(start = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(4) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color.White, CircleShape)
                        .border(1.5.dp, NavyBlue, CircleShape)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "APUNTES DE SUPERVIVENCIA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SlateGray,
                    letterSpacing = 0.5.sp
                )

                IconButton(
                    onClick = {
                        noteIndex = (noteIndex + 1) % FUNNY_COLLEGE_NOTES.size
                        rotationAngle += if (rotationAngle >= 360f) -360f + 10f else 10f
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Cambiar apunte",
                        tint = ProBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Crumbled text / Relatable joke
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    .border(2.dp, NavyBlue, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    AnimatedContent(
                        targetState = FUNNY_COLLEGE_NOTES[noteIndex],
                        transitionSpec = {
                            scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut()
                        },
                        label = "note_text"
                    ) { quote ->
                        Text(
                            text = quote,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyBlue,
                            lineHeight = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "— Mascota Sabia",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = DarkGreen,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

/**
 * 3. EL CALCULADOR SALVADOR WIDGET (Interactive grading simulator following the user's explicit rules)
 */
@Composable
fun CartoonSaviorCalculatorWidget(
    modifier: Modifier = Modifier
) {
    // 0 to 100 points logic:
    // Divided into 2 units: 50 each.
    // Each 50 points unit consists of:
    // - 1 Exam of 15 pts
    // - 5 Quizzes (pruebas) of 7 pts each (35 pts total)
    // Minimum to pass: 51 pts

    var unit1Exams by remember { mutableIntStateOf(0) } // Max 1
    var unit1Quizzes by remember { mutableIntStateOf(0) } // Max 5
    var unit2Exams by remember { mutableIntStateOf(0) } // Max 1
    var unit2Quizzes by remember { mutableIntStateOf(0) } // Max 5

    // Total calculated points
    val totalScore = remember(unit1Exams, unit1Quizzes, unit2Exams, unit2Quizzes) {
        val u1Score = (unit1Exams * 15) + (unit1Quizzes * 7)
        val u2Score = (unit2Exams * 15) + (unit2Quizzes * 7)
        u1Score + u2Score
    }

    val isPassed = totalScore >= 51

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = NavyBlue, spotColor = NavyBlue)
            .background(Color(0xFFECEFF1), RoundedCornerShape(24.dp)) // Retro Calculator grey
            .border(4.dp, NavyBlue, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF80DEEA), CircleShape)
                            .border(2.dp, NavyBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EL CALCULADOR SALVADOR",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyBlue,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "Regla: 2 Unids x 50pts",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateGray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Neon calculator display screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF263238), RoundedCornerShape(12.dp)) // Dark Screen
                    .border(3.dp, NavyBlue, RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PUNTAJE ACUMULADO:",
                            fontSize = 9.sp,
                            color = Color(0xFF80DFB6),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$totalScore / 100 PTS",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isPassed) Color(0xFF69F0AE) else Color(0xFFFF5252),
                            letterSpacing = 1.sp
                        )
                    }

                    // Mascot icon status reaction
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isPassed) "¡SALVADO!" else "BAJO LÍMITE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isPassed) Color(0xFF69F0AE) else Color(0xFFFF5252)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isPassed) "Mascota: Feliz" else "Mascota: Preocupada",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Grid simulation controls
            Text(
                text = "Simular Evaluaciones Registradas:",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NavyBlue
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Unit 1 Controllers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Unit 1
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(2.dp, NavyBlue, RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "UNIDAD 1 (Max 50)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = ProBlue,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Exam (15pts)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Parcial (15p)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "–",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .clickable { if (unit1Exams > 0) unit1Exams-- }
                                    .padding(horizontal = 6.dp)
                            )
                            Text("$unit1Exams", fontSize = 11.sp, fontWeight = FontWeight.Black, color = NavyBlue)
                            Text(
                                text = "+",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .clickable { if (unit1Exams < 1) unit1Exams++ }
                                    .padding(horizontal = 6.dp)
                            )
                        }
                    }

                    // Quizzes (7pts)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Pruebas (7p)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "–",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .clickable { if (unit1Quizzes > 0) unit1Quizzes-- }
                                    .padding(horizontal = 6.dp)
                            )
                            Text("$unit1Quizzes", fontSize = 11.sp, fontWeight = FontWeight.Black, color = NavyBlue)
                            Text(
                                text = "+",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .clickable { if (unit1Quizzes < 5) unit1Quizzes++ }
                                    .padding(horizontal = 6.dp)
                            )
                        }
                    }
                }

                // Unit 2
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(2.dp, NavyBlue, RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "UNIDAD 2 (Max 50)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = ProBlue,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Exam (15pts)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Parcial (15p)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "–",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .clickable { if (unit2Exams > 0) unit2Exams-- }
                                    .padding(horizontal = 6.dp)
                            )
                            Text("$unit2Exams", fontSize = 11.sp, fontWeight = FontWeight.Black, color = NavyBlue)
                            Text(
                                text = "+",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .clickable { if (unit2Exams < 1) unit2Exams++ }
                                    .padding(horizontal = 6.dp)
                            )
                        }
                    }

                    // Quizzes (7pts)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Pruebas (7p)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "–",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .clickable { if (unit2Quizzes > 0) unit2Quizzes-- }
                                    .padding(horizontal = 6.dp)
                            )
                            Text("$unit2Quizzes", fontSize = 11.sp, fontWeight = FontWeight.Black, color = NavyBlue)
                            Text(
                                text = "+",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .clickable { if (unit2Quizzes < 5) unit2Quizzes++ }
                                    .padding(horizontal = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Savior Formula Calculation tip
            val remainingNeeded = (51 - totalScore).coerceAtLeast(0)
            Surface(
                color = if (isPassed) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, NavyBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isPassed) {
                        "¡Estás aprobado! Asegúrate de mantener este ritmo."
                    } else {
                        "Necesitas $remainingNeeded pts para salvarte (Mín: 51). Ej: ${if (remainingNeeded <= 15) "1 Parcial" else "${remainingNeeded / 7 + 1} Pruebas"}."
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue,
                    modifier = Modifier.padding(8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 4. RETRO GAME HEALTH BAR WIDGET (With custom animated fighting mascot & coffee potion HP booster)
 */
@Composable
fun CartoonPixelLifebarWidget(
    modifier: Modifier = Modifier
) {
    var coffeeCupsSimulated by remember { mutableIntStateOf(3) }
    
    // Pixel Mascot fighting animation (swaying/bouncing)
    val infiniteTransition = rememberInfiniteTransition(label = "pixel_mascot")
    val mascotXOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot_x"
    )

    val mascotYOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot_y"
    )

    // Calculate energy / academic hp percentage
    val hpPercentage = remember(coffeeCupsSimulated) {
        (40 + (coffeeCupsSimulated * 20)).coerceIn(10, 100)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = NavyBlue, spotColor = NavyBlue)
            .background(Color(0xFF1A1A24), RoundedCornerShape(24.dp)) // Retro Dark arcade
            .border(4.dp, NavyBlue, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nivel de Estrés / Energía",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00E676),
                    letterSpacing = 1.sp
                )

                Surface(
                    color = Color(0xFFEF5350),
                    border = BorderStroke(1.dp, Color.White),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "LVL. $coffeeCupsSimulated",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mascot fighting visual arena (Retro gaming arcade)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .background(Color(0xFF2C2C35), RoundedCornerShape(12.dp))
                    .border(2.dp, NavyBlue, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Battle Mascot with interactive animation
                Row(
                    modifier = Modifier
                        .offset(x = mascotXOffset.dp, y = mascotYOffset.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small custom vector pixel-style buddy mascot drawing
                    Canvas(modifier = Modifier.size(45.dp)) {
                        val mw = size.width
                        val mh = size.height

                        // Pixelized outer boundary
                        drawRect(color = Color.White, topLeft = Offset(mw * 0.1f, mh * 0.2f), size = androidx.compose.ui.geometry.Size(mw * 0.8f, mh * 0.7f))
                        drawRect(color = DarkGreen, topLeft = Offset(mw * 0.1f, mh * 0.2f), size = androidx.compose.ui.geometry.Size(mw * 0.8f, mh * 0.7f), style = Stroke(width = 4f))

                        // Pixels for ears
                        drawRect(color = DarkGreen, topLeft = Offset(mw * 0.15f, 0f), size = androidx.compose.ui.geometry.Size(mw * 0.2f, mh * 0.25f))
                        drawRect(color = DarkGreen, topLeft = Offset(mw * 0.65f, 0f), size = androidx.compose.ui.geometry.Size(mw * 0.2f, mh * 0.25f))

                        // Pixels for angry eyes
                        drawRect(color = NavyBlue, topLeft = Offset(mw * 0.25f, mh * 0.45f), size = androidx.compose.ui.geometry.Size(6f, 6f))
                        drawRect(color = NavyBlue, topLeft = Offset(mw * 0.6f, mh * 0.45f), size = androidx.compose.ui.geometry.Size(6f, 6f))
                        
                        // Boxing Gloves or swords
                        drawCircle(color = Color(0xFFFF5252), radius = 8f, center = Offset(mw * 0.85f, mh * 0.6f))
                        drawCircle(color = Color(0xFFFF5252), radius = 8f, center = Offset(mw * 0.15f, mh * 0.6f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "¡LUCHANDO POR PASAR!",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (hpPercentage > 50) "Estado: Resistiendo con café" else "Estado: ¡Alerta de sueño!",
                            color = Color.LightGray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Retro segment health bar
            Text(
                text = "BARRA DE VIDA ACADÉMICA:",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            // Outer container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .background(Color.Black, RoundedCornerShape(4.dp))
                    .border(2.dp, Color.White, RoundedCornerShape(4.dp))
                    .padding(2.dp)
            ) {
                // Segmented HP Bar
                val filledColor = when {
                    hpPercentage > 60 -> Color(0xFF00E676) // Healthy Green
                    hpPercentage > 30 -> Color(0xFFFFD600) // Warning Yellow
                    else -> Color(0xFFD50000) // Dangerous Red
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(hpPercentage / 100f)
                        .background(filledColor)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Items
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Añadir Boosters de Energía:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Potion 1: Coffee
                    Button(
                        onClick = {
                            if (coffeeCupsSimulated < 5) {
                                coffeeCupsSimulated++
                            } else {
                                coffeeCupsSimulated = 0
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ProBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Café", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    // Potion 2: Nap
                    Button(
                        onClick = {
                            coffeeCupsSimulated = 3
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Siesta", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
