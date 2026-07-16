import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

content = content.replace("AdviceCarouselWidget(subjects, assessments, onSubjectClick)", "AdviceCarouselWidget(subjects, assessments, onNavigateToDetails)")
content = content.replace("onSubjectClick(missingGrades.first().subjectId)", "onNavigateToDetails(missingGrades.first().subjectId)")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)

