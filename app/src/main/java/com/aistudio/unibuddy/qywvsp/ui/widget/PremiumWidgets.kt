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

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.currentState
import androidx.glance.state.PreferencesGlanceStateDefinition
import kotlinx.coroutines.delay

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
        val nextAssessment = assessments.filter { it.grade == null && it.examDate.isNotBlank() }.minByOrNull { 
            // Very basic sorting by string since it's dd/MM/yyyy, but proper would be parsing
            try {
                val parts = it.examDate.split("/")
                "${parts[2]}-${parts[1]}-${parts[0]}"
            } catch(e: Exception) { "9999-99-99" }
        }
        
        val subject = nextAssessment?.let { db.subjectDao().getSubjectById(it.subjectId) }
        
        // Calculate days left
        var daysLeftText = "Pronto"
        if (nextAssessment != null && nextAssessment.examDate.isNotBlank()) {
            try {
                val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                val examDate = format.parse(nextAssessment.examDate)
                val today = java.util.Date()
                val diffInMillis = examDate.time - today.time
                val diffInDays = java.util.concurrent.TimeUnit.DAYS.convert(diffInMillis, java.util.concurrent.TimeUnit.MILLISECONDS)
                daysLeftText = when {
                    diffInDays < 0 -> "¡VENCIDA!"
                    diffInDays == 0L -> "¡HOY!"
                    diffInDays == 1L -> "Mañana"
                    else -> "En $diffInDays días"
                }
            } catch (e: Exception) { }
        }

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFFBA1A1A)) // ProRed for high urgency 
                    .padding(16.dp)
                    .clickable(actionStartActivity<MainActivity>()) // Limits: Glance deep linking to a specific screen without Intent args is hard. We open the app.
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(), 
                    verticalAlignment = Alignment.CenterVertically, 
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Urgent Badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_widget_event), 
                            contentDescription = null, 
                            modifier = GlanceModifier.size(16.dp)
                        )
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        Text(
                            "PRÓXIMA MISIÓN", 
                            style = TextStyle(color = ColorProvider(Color.White), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                    
                    Spacer(modifier = GlanceModifier.height(12.dp))
                    
                    if (nextAssessment != null) {
                        // Title of the mission
                        Text(
                            nextAssessment.name.take(15) + if(nextAssessment.name.length > 15) "..." else "", 
                            style = TextStyle(color = ColorProvider(Color.White), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        )
                        // Subject Name
                        Text(
                            subject?.name ?: "Materia", 
                            style = TextStyle(color = ColorProvider(Color(0xFFFFCDD2)), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        )
                        Spacer(modifier = GlanceModifier.height(8.dp))
                        
                        // Remaining Days Pill (simulated with a background Box)
                        Box(
                            modifier = GlanceModifier
                                .background(Color(0xFF7F1D1D)) // Darker red
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                daysLeftText, 
                                style = TextStyle(color = ColorProvider(Color.White), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            )
                        }
                    } else {
                        Text("¡TODO DESPEJADO!", style = TextStyle(color = ColorProvider(Color.White), fontSize = 14.sp, fontWeight = FontWeight.Bold))
                        Text("Sin misiones pendientes", style = TextStyle(color = ColorProvider(Color(0xFFFFCDD2)), fontSize = 10.sp))
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
        val upcoming = assessments.filter { it.grade == null && it.examDate.isNotBlank() }.sortedBy { 
            try {
                val parts = it.examDate.split("/")
                "${parts[2]}-${parts[1]}-${parts[0]}"
            } catch(e: Exception) { "9999-99-99" }
        }.take(3)

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF1A1A1A)) // Dark Quest Log theme
                    .padding(16.dp)
                    .clickable(actionStartActivity<MainActivity>())
            ) {
                Column(modifier = GlanceModifier.fillMaxSize()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_widget_event), 
                            contentDescription = null, 
                            modifier = GlanceModifier.size(16.dp)
                        )
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        Text(
                            "MISIONES ACTIVAS", 
                            style = TextStyle(color = ColorProvider(Color(0xFFFFC107)), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                    
                    Spacer(modifier = GlanceModifier.height(12.dp))
                    
                    if (upcoming.isEmpty()) {
                        Text("¡Todo al día!", style = TextStyle(color = ColorProvider(Color(0xFFAAAAAA)), fontSize = 14.sp, fontWeight = FontWeight.Bold))
                        Text("No hay misiones pendientes.", style = TextStyle(color = ColorProvider(Color(0xFF777777)), fontSize = 12.sp))
                    } else {
                        upcoming.forEach { assessment ->
                            Row(
                                modifier = GlanceModifier.fillMaxWidth().padding(bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Square Checkbox simulation
                                Box(
                                    modifier = GlanceModifier
                                        .size(14.dp)
                                        .background(Color(0xFF333333))
                                ) {}
                                
                                Spacer(modifier = GlanceModifier.width(8.dp))
                                
                                Column {
                                    Text(
                                        assessment.name.take(20) + if(assessment.name.length > 20) "..." else "", 
                                        style = TextStyle(color = ColorProvider(Color.White), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    )
                                    Text(
                                        "Fecha límite: ${assessment.examDate}", 
                                        style = TextStyle(color = ColorProvider(Color(0xFFB0BEC5)), fontSize = 10.sp)
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

// 5. Quick Commute Widget
class QuickCommuteWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickCommuteWidget()
}
class QuickCommuteWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getDatabase(context)
        val travelTime = db.settingDao().getSetting("widget_travel_time")?.value ?: "25"
        val isOutOfRange = db.settingDao().getSetting("widget_out_of_range")?.value == "true"
        val weather = db.settingDao().getSetting("weather_desc")?.value ?: "Clima despejado"

        provideContent {
            val isRefreshing = currentState(key = booleanPreferencesKey("is_refreshing")) ?: false
            
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF0061A4)) // ProBlue for urgency/vibrancy
                    .padding(16.dp)
                    .clickable(actionRunCallback<RefreshCommuteAction>())
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(), 
                    verticalAlignment = Alignment.CenterVertically, 
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        provider = ImageProvider(if (isRefreshing) R.drawable.ic_widget_event else R.drawable.ic_widget_run), 
                        contentDescription = null, 
                        modifier = GlanceModifier.size(32.dp)
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Text(
                        if (isRefreshing) "ACTUALIZANDO..." else "VIAJE RÁPIDO", 
                        style = TextStyle(color = ColorProvider(Color.White), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    )
                    
                    if (isOutOfRange && !isRefreshing) {
                        Text("LEJOS", style = TextStyle(color = ColorProvider(Color.White), fontSize = 18.sp, fontWeight = FontWeight.Bold))
                    } else if (!isRefreshing) {
                        Text("$travelTime MIN", style = TextStyle(color = ColorProvider(Color.White), fontSize = 18.sp, fontWeight = FontWeight.Bold))
                    } else {
                        Text("-- MIN", style = TextStyle(color = ColorProvider(Color.White), fontSize = 18.sp, fontWeight = FontWeight.Bold))
                    }
                    
                    Text(weather, style = TextStyle(color = ColorProvider(Color(0xFFE2E8F0)), fontSize = 10.sp))
                }
            }
        }
    }
}

class RefreshCommuteAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs.toMutablePreferences().apply {
                this[booleanPreferencesKey("is_refreshing")] = true
            }
        }
        QuickCommuteWidget().update(context, glanceId)
        
        delay(1500) // Simular retardo de red/GPS. En un caso real, el ViewModel actualiza Room.
        
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs.toMutablePreferences().apply {
                this[booleanPreferencesKey("is_refreshing")] = false
            }
        }
        QuickCommuteWidget().update(context, glanceId)
    }
}