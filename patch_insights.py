import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/SemesterHistoryView.kt", "r") as f:
    content = f.read()

# Add to imports
if "import com.aistudio.unibuddy.qywvsp.ui.screens.FocusHistoryChart" not in content:
    content = content.replace("import com.aistudio.unibuddy.qywvsp.ui.theme.*", "import com.aistudio.unibuddy.qywvsp.ui.theme.*\nimport com.aistudio.unibuddy.qywvsp.ui.screens.FocusHistoryChart\nimport com.aistudio.unibuddy.qywvsp.ui.screens.BuddyMoodHistoryWidget")

# Add the new tab
old_tabs = """listOf("oficial" to "Oficial", "asistencias" to "Asist.", "examenes" to "Notas", "gps" to "Viajes").forEach { (tabKey, tabLabel) ->"""
new_tabs = """listOf("insights" to "Insights", "oficial" to "Hist. Oficial", "asistencias" to "Asist.", "examenes" to "Notas", "gps" to "Viajes").forEach { (tabKey, tabLabel) ->"""
content = content.replace(old_tabs, new_tabs)

# Modify default state of tab
old_default_tab = """var logTab by remember { mutableStateOf("asistencias") } // "asistencias", "examenes", "gps\""""
new_default_tab = """var logTab by remember { mutableStateOf("insights") }"""
content = content.replace(old_default_tab, new_default_tab)


# Add insights handling
old_when = """        when (logTab) {
            "oficial" -> {"""

new_when = """        when (logTab) {
            "insights" -> {
                WeeklyInsightsSummary(viewModel = viewModel, attendanceLogs = attendanceLogs, assessments = assessments)
            }
            "oficial" -> {"""
content = content.replace(old_when, new_when)


# Append WeeklyInsightsSummary function at the bottom
new_func = """
@Composable
fun WeeklyInsightsSummary(
    viewModel: UniBuddyViewModel, 
    attendanceLogs: List<com.aistudio.unibuddy.qywvsp.data.AttendanceLog>,
    assessments: List<com.aistudio.unibuddy.qywvsp.data.Assessment>
) {
    val weeklyStreak by viewModel.weeklyStreak.collectAsStateWithLifecycle()
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
    ) {
        // Racha Semanal Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(containerColor = NavyBlue),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Whatshot, contentDescription = "Racha", tint = Amber, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Racha de Actividad", color = Bone, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("$weeklyStreak días consecutivos", color = MintGreen, fontSize = 14.sp)
                }
            }
        }
        
        BuddyMoodHistoryWidget(attendanceLogs = attendanceLogs, assessments = assessments)
        
        FocusHistoryChart(viewModel = viewModel)
    }
}
"""

content = content + new_func

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/SemesterHistoryView.kt", "w") as f:
    f.write(content)
