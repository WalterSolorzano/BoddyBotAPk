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
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
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
import java.util.Calendar

// 1. RPG Progress Widget
class RPGProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = RPGProgressWidget()
}
class RPGProgressWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Box(modifier = GlanceModifier.fillMaxSize().background(Color(0xFF2C3E50)).padding(16.dp).clickable(actionStartActivity<MainActivity>())) {
                Column(modifier = GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("EXP SEMESTRAL", style = TextStyle(color = ColorProvider(Color.White), fontSize = 12.sp, fontWeight = FontWeight.Bold))
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Box(modifier = GlanceModifier.fillMaxWidth().height(16.dp).background(Color(0xFF34495E))) {
                        Box(modifier = GlanceModifier.width(80.dp).height(16.dp).background(Color(0xFF2ECC71))) {}
                    }
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Text("NIVEL 4 - SEMANA 8", style = TextStyle(color = ColorProvider(Color(0xFFBDC3C7)), fontSize = 10.sp, fontWeight = FontWeight.Medium))
                }
            }
        }
    }
}

// 2. Next Class Hero Widget
class NextClassHeroWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NextClassHeroWidget()
}
class NextClassHeroWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getDatabase(context)
        val assessments = db.assessmentDao().getAllAssessments().first()
        val nextAssessment = assessments.filter { it.grade == null }.minByOrNull { it.examDate ?: "" }

        provideContent {
            Box(modifier = GlanceModifier.fillMaxSize().background(Color(0xFFE74C3C)).padding(16.dp).clickable(actionStartActivity<MainActivity>())) {
                Column(modifier = GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(provider = ImageProvider(R.drawable.ic_widget_school), contentDescription = null, modifier = GlanceModifier.size(24.dp))
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Text("PRÓXIMA MISIÓN", style = TextStyle(color = ColorProvider(Color.White), fontSize = 10.sp, fontWeight = FontWeight.Bold))
                    Text(nextAssessment?.name ?: "Sin misiones", style = TextStyle(color = ColorProvider(Color.White), fontSize = 14.sp, fontWeight = FontWeight.Bold))
                    if (nextAssessment != null) {
                        Text(nextAssessment.examDate.ifEmpty { "Pronto" }, style = TextStyle(color = ColorProvider(Color(0xFFFFCDD2)), fontSize = 12.sp))
                    }
                }
            }
        }
    }
}

// 3. Streak Tracker Widget
class StreakTrackerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StreakTrackerWidget()
}
class StreakTrackerWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = context.getSharedPreferences("unibuddy_prefs", Context.MODE_PRIVATE)
        val startMillisStr = prefs.getString("semester_start_date", "")
        val startMillis = startMillisStr?.toLongOrNull() ?: System.currentTimeMillis()
        val diffDays = ((System.currentTimeMillis() - startMillis) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
        
        val level = (diffDays / 7) + 1
        val xpInLevel = diffDays % 7
        val progressText = "$xpInLevel / 7 Días"

        provideContent {
            Box(modifier = GlanceModifier.fillMaxSize().background(Color(0xFF2C3E50)).padding(16.dp).clickable(actionStartActivity<MainActivity>())) {
                Column(modifier = GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(provider = ImageProvider(R.drawable.ic_widget_flame), contentDescription = null, modifier = GlanceModifier.size(20.dp))
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        Text("RACHA ACTIVA", style = TextStyle(color = ColorProvider(Color(0xFFF39C12)), fontSize = 10.sp, fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text("NIVEL $level", style = TextStyle(color = ColorProvider(Color.White), fontSize = 18.sp, fontWeight = FontWeight.Bold))
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Box(modifier = GlanceModifier.fillMaxWidth().height(8.dp).background(Color(0xFF34495E))) {
                        Text(progressText, style = TextStyle(color = ColorProvider(Color(0xFFBDC3C7)), fontSize = 8.sp, fontWeight = FontWeight.Medium))
                    }
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(progressText, style = TextStyle(color = ColorProvider(Color(0xFFBDC3C7)), fontSize = 10.sp, fontWeight = FontWeight.Medium))
                }
            }
        }
    }
}

// 4. Upcoming Assessments Widget
class UpcomingAssessmentsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = UpcomingAssessmentsWidget()
}
class UpcomingAssessmentsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getDatabase(context)
        val assessments = db.assessmentDao().getAllAssessments().first()
        val upcoming = assessments.filter { it.grade == null }.sortedBy { it.examDate ?: "" }.take(2)

        provideContent {
            Box(modifier = GlanceModifier.fillMaxSize().background(Color(0xFF8E44AD)).padding(16.dp).clickable(actionStartActivity<MainActivity>())) {
                Column(modifier = GlanceModifier.fillMaxSize()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(provider = ImageProvider(R.drawable.ic_widget_event), contentDescription = null, modifier = GlanceModifier.size(16.dp))
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        Text("MISIONES ACTIVAS", style = TextStyle(color = ColorProvider(Color.White), fontSize = 12.sp, fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = GlanceModifier.height(12.dp))
                    if (upcoming.isEmpty()) {
                        Text("¡Todo al día!", style = TextStyle(color = ColorProvider(Color.White), fontSize = 12.sp))
                    } else {
                        for (it in upcoming) { 
                            Text("${it.name} (${it.examDate.ifEmpty { "Pronto" }})", style = TextStyle(color = ColorProvider(Color.White), fontSize = 12.sp))
                            Spacer(modifier = GlanceModifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

// 5. Quick Commute Widget
class QuickCommuteWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickCommuteWidget()
}
class QuickCommuteWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Box(modifier = GlanceModifier.fillMaxSize().background(Color(0xFF2980B9)).padding(16.dp).clickable(actionStartActivity<MainActivity>())) {
                Column(modifier = GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(provider = ImageProvider(R.drawable.ic_widget_run), contentDescription = null, modifier = GlanceModifier.size(32.dp))
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Text("VIAJE RAPIDO", style = TextStyle(color = ColorProvider(Color.White), fontSize = 10.sp, fontWeight = FontWeight.Bold))
                    Text("25 MIN", style = TextStyle(color = ColorProvider(Color.White), fontSize = 18.sp, fontWeight = FontWeight.Bold))
                    Text("Clima despejado", style = TextStyle(color = ColorProvider(Color(0xFFB3E5FC)), fontSize = 10.sp))
                }
            }
        }
    }
}
