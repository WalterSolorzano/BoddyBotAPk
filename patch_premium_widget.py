import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/widget/PremiumWidgets.kt", "r") as f:
    content = f.read()

# Add imports
new_imports = """
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionRunCallback
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.currentState
import androidx.glance.state.PreferencesGlanceStateDefinition
import kotlinx.coroutines.delay
"""
content = content.replace("import java.util.Calendar\n", "import java.util.Calendar\n" + new_imports)

old_quick_commute = """class QuickCommuteWidget : GlanceAppWidget() {
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
}"""

new_quick_commute = """class QuickCommuteWidget : GlanceAppWidget() {
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
                    
                    Text(weather, style = TextStyle(color = ColorProvider(Color(0xFFB3E5FC)), fontSize = 10.sp))
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
"""

content = content.replace(old_quick_commute, new_quick_commute)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/widget/PremiumWidgets.kt", "w") as f:
    f.write(content)

