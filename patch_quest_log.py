import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/widget/PremiumWidgets.kt", "r") as f:
    content = f.read()

old_widget = """class UpcomingAssessmentsWidget : GlanceAppWidget() {
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
}"""

new_widget = """class UpcomingAssessmentsWidget : GlanceAppWidget() {
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
}"""

content = content.replace(old_widget, new_widget)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/widget/PremiumWidgets.kt", "w") as f:
    f.write(content)
