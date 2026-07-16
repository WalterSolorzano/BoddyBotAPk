import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

# Update signature
content = content.replace("fun AdviceCarouselWidget(subjects: List<com.aistudio.unibuddy.qywvsp.data.Subject>, assessments: List<com.aistudio.unibuddy.qywvsp.data.Assessment>)", "fun AdviceCarouselWidget(subjects: List<com.aistudio.unibuddy.qywvsp.data.Subject>, assessments: List<com.aistudio.unibuddy.qywvsp.data.Assessment>, onSubjectClick: (Int) -> Unit)")
content = content.replace("AdviceCarouselWidget(subjects, assessments)", "AdviceCarouselWidget(subjects, assessments, onSubjectClick)")

# Update Card
old_card = """    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ProBlue.copy(alpha=0.05f)), // Light blue
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ProBlue.copy(alpha = 0.2f))
    ) {"""

new_card = """    val currentTip = tips[currentIndex]
    val mentionedSubject = subjects.find { currentTip.contains(it.name) }
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = mentionedSubject != null) {
            mentionedSubject?.let { onSubjectClick(it.id) }
        },
        colors = CardDefaults.cardColors(containerColor = ProBlue.copy(alpha=0.05f)), // Light blue
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ProBlue.copy(alpha = 0.2f))
    ) {"""

content = content.replace(old_card, new_card)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
