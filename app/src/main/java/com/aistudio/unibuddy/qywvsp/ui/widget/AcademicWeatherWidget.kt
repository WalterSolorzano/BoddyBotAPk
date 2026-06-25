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
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.aistudio.unibuddy.qywvsp.MainActivity
import com.aistudio.unibuddy.qywvsp.R
import com.aistudio.unibuddy.qywvsp.data.AppDatabase
import kotlinx.coroutines.flow.first
import java.util.Calendar

class AcademicWeatherWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        
        // Fetch subjects from database
        val dao = AppDatabase.getDatabase(context).subjectDao()
        val subjects = dao.getAllSubjects().first()
        
        // Find next class today
        val calendar = Calendar.getInstance()
        val todayInt = calendar.get(Calendar.DAY_OF_WEEK)
        val todayStr = when (todayInt) {
            Calendar.MONDAY -> "Lu"
            Calendar.TUESDAY -> "Ma"
            Calendar.WEDNESDAY -> "Mi"
            Calendar.THURSDAY -> "Ju"
            Calendar.FRIDAY -> "Vi"
            Calendar.SATURDAY -> "Sa"
            else -> "Do"
        }
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentTime = String.format("%02d:%02d", currentHour, currentMinute)
        
        var nextClassSession: com.aistudio.unibuddy.qywvsp.data.ClassSessionDetails? = null
        var nextClassSubject: com.aistudio.unibuddy.qywvsp.data.Subject? = null
        
        subjects.forEach { subject ->
            subject.sessions.forEach { session ->
                if (session.day == todayStr) {
                    val startTime = session.time.substringBefore(" - ")
                    if (startTime > currentTime) {
                        if (nextClassSession == null || startTime < nextClassSession!!.time.substringBefore(" - ")) {
                            nextClassSession = session
                            nextClassSubject = subject
                        }
                    }
                }
            }
        }
            
        // Calculate when to leave (e.g. 30 mins before)
        val leaveMessage = if (nextClassSubject != null && nextClassSession != null) {
            val startTime = nextClassSession!!.time.substringBefore(" - ")
            val parts = startTime.split(":")
            if (parts.size == 2) {
                var hour = parts[0].toIntOrNull() ?: 0
                var minute = parts[1].toIntOrNull() ?: 0
                minute -= 30
                if (minute < 0) {
                    minute += 60
                    hour -= 1
                }
                val leaveTime = String.format("%02d:%02d", hour, minute)
                "Sal de casa a las $leaveTime para ${nextClassSubject!!.name}"
            } else {
                "Próxima clase: $startTime"
            }
        } else {
            "Día libre. ¡Relájate!"
        }
        
        // State machine for weather
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        
        val weatherState = if (currentHour in 6..18) {
            if (dayOfYear % 3 == 0) "rainy" else "sunny"
        } else {
            "night"
        }

        provideContent {
            val bgColor = when (weatherState) {
                "sunny" -> ColorProvider(Color(0xFF87CEEB)) // Light blue sky
                "rainy" -> ColorProvider(Color(0xFF90A4AE)) // Gray sky
                else -> ColorProvider(Color(0xFF1A237E)) // Dark Navy
            }
            
            val textColor = when (weatherState) {
                "night" -> ColorProvider(Color.White)
                else -> ColorProvider(Color(0xFF2C3E50)) // NavyBlue
            }
            
            val iconRes = when (weatherState) {
                "sunny" -> R.drawable.buddy_widget_soleado
                "rainy" -> R.drawable.buddy_widget_lluvia
                else -> R.drawable.buddy_widget_noche
            }

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(bgColor)
                    .clickable(actionStartActivity<MainActivity>()),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                    modifier = GlanceModifier.padding(16.dp)
                ) {
                    Image(
                        provider = ImageProvider(iconRes),
                        contentDescription = "Academic Weather",
                        modifier = GlanceModifier.size(100.dp)
                    )
                    
                    Text(
                        text = leaveMessage,
                        style = TextStyle(
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = GlanceModifier.padding(top = 12.dp)
                    )
                }
            }
        }
    }
}
