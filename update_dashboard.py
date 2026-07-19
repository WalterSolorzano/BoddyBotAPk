import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

# 1. Remove AdviceCarouselWidget call
content = content.replace("AdviceCarouselWidget(subjects, assessments, onNavigateToDetails)", "")

# 2. Remove BuddyMascotRoomWidget call entirely from activeTab == "hoy"
pattern_mascot_room = r"""            BuddyMascotRoomWidget\(.*?\)"""
content = re.sub(pattern_mascot_room, "", content, flags=re.DOTALL)

# 3. Replace the Dashboard Header
old_header = r"""        // Dashboard Header
        Row\(
            modifier = Modifier\.fillMaxWidth\(\),
            horizontalArrangement = Arrangement\.SpaceBetween,
            verticalAlignment = Alignment\.CenterVertically
        \) \{
.*?
        \}
        
        // Stylized Tab Segment Switcher"""

new_header = """        // Hero Mascot Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Speech Bubble
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "¡Hola $username! Qué bueno verte hoy. Recuerda revisar tu horario y tareas.",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp,
                        color = NavyBlue,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                // Giant Mascot
                com.aistudio.unibuddy.qywvsp.ui.components.BuddyMascot(
                    pose = if (isCelebrating) "celebrating" else "greeting",
                    modifier = Modifier.size(160.dp),
                    isHappy = true,
                    mainColor = ProBlue
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Compact secondary info (Compact Row)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Outing Info
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Map, contentDescription = null, tint = SlateGray, modifier = Modifier.size(20.dp))
                        Text("Salida", fontSize = 10.sp, color = SlateGray, fontWeight = FontWeight.Bold)
                    }
                    // Mood Info
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Mood, contentDescription = null, tint = SlateGray, modifier = Modifier.size(20.dp))
                        Text("Buen ánimo", fontSize = 10.sp, color = SlateGray, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        // Stylized Tab Segment Switcher"""

content = re.sub(old_header, new_header, content, flags=re.DOTALL)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
