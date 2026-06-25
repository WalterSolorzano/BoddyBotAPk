package com.aistudio.unibuddy.qywvsp.ui.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import androidx.glance.unit.ColorProvider
import com.aistudio.unibuddy.qywvsp.MainActivity
import com.aistudio.unibuddy.qywvsp.R

class TamagotchiWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        
        provideContent {
            val isHealthy = true 
            
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(Color(0xFFF8F9FA))) // Bone / Light Gray
                    .padding(16.dp)
                    .clickable(actionStartActivity<MainActivity>()),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_launcher_foreground),
                        contentDescription = "Buddy Mascot",
                        modifier = GlanceModifier.size(64.dp)
                    )
                    
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .padding(top = 8.dp)
                            .background(ColorProvider(Color(0xFFE0E0E0))) // Empty bar
                    ) {
                         Box(
                             modifier = GlanceModifier
                                 .fillMaxWidth()
                                 .height(12.dp)
                                 .background(ColorProvider(Color(0xFF4CAF50))) // Green health
                         ) {}
                    }
                    
                    Text(
                        text = "¡Al 100%!",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF2C3E50)), // NavyBlue
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = GlanceModifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
