import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/widget/PremiumWidgets.kt", "r") as f:
    content = f.read()

old_hero = """class NextClassHeroWidget : GlanceAppWidget() {
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
}"""

if old_hero not in content:
    print("Could not find the new hero code to replace. Let's see what's actually there.")
else:
    print("Wait, old_hero is the code I tried to insert before.")

# Let's find out what's in the file right now
