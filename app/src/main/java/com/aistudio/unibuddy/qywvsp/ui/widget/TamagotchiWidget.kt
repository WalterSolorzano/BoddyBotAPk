package com.aistudio.unibuddy.qywvsp.ui.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.aistudio.unibuddy.qywvsp.MainActivity
import com.aistudio.unibuddy.qywvsp.R
import com.aistudio.unibuddy.qywvsp.data.AppDatabase
import kotlinx.coroutines.flow.first
import kotlin.math.roundToInt

class TamagotchiWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = AppDatabase.getDatabase(context)
        val logs = database.attendanceLogDao().getAllLogs().first()
        val absences = database.absenceDao().getAllAbsences().first()

        // Calculate actual attendance rate
        val totalPres = logs.count { it.isPresent }
        val totalAbs = logs.count { !it.isPresent } + absences.size
        val totalLogs = totalPres + totalAbs
        
        val attendanceRate = if (totalLogs > 0) {
            (totalPres.toDouble() / totalLogs.toDouble()) * 100.0
        } else {
            100.0 // Default to 100% if no history yet
        }

        val statusMsg = when {
            attendanceRate >= 90.0 -> "Buddy: ¡Súper Feliz!"
            attendanceRate >= 80.0 -> "Buddy: Todo en orden"
            attendanceRate >= 70.0 -> "Buddy: Algo cansado"
            attendanceRate >= 50.0 -> "Buddy: ¡Enfermo!"
            else -> "Buddy: ¡Grave! Ve a clase"
        }

        // Color palette matching the gorgeous night blue theme of AcademicWeatherWidget
        val themeBgColor = Color(0xFF1A237E) // Beautiful Indigo Navy
        val textColorSecondary = Color(0xFF94A3B8) // Slate Gray for secondary details
        val textWhite = Color.White

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(themeBgColor)
                    .cornerRadius(20.dp) // Beautiful rounded corners to match modern device widgets
                    .padding(12.dp)
                    .clickable(actionStartActivity<MainActivity>()),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Text(
                        text = "UniBuddy Mascota",
                        style = TextStyle(
                            color = ColorProvider(textColorSecondary),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = GlanceModifier.height(6.dp))

                    Image(
                        provider = ImageProvider(R.drawable.buddy_tamagotchi_happy_avatar),
                        contentDescription = "Buddy Mascot",
                        modifier = GlanceModifier.size(80.dp) // Large hero layout size - extremely visible!
                    )

                    Spacer(modifier = GlanceModifier.height(8.dp))

                    // Life bar HUD layout (HP-style layout)
                    Row(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.Vertical.CenterVertically,
                        modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 4.dp)
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_widget_heart_full),
                            contentDescription = "Heart Life Icon",
                            modifier = GlanceModifier.size(14.dp)
                        )
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        
                        androidx.glance.appwidget.LinearProgressIndicator(
                            progress = (attendanceRate / 100.0).toFloat().coerceIn(0f, 1f),
                            modifier = GlanceModifier
                                .defaultWeight()
                                .height(8.dp)
                                .cornerRadius(4.dp),
                            color = ColorProvider(Color(0xFFEF4444)), // Vibrant HP bar red
                            backgroundColor = ColorProvider(Color(0xFF475569)) // Dark Slate BG
                        )
                        
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        
                        Text(
                            text = "${attendanceRate.roundToInt()}%",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFFFCA5A5)), // Rose light color
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(6.dp))

                    Text(
                        text = statusMsg,
                        style = TextStyle(
                            color = ColorProvider(textWhite),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
