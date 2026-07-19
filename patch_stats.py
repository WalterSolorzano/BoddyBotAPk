import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/PensumStatsTab.kt", "r") as f:
    content = f.read()

# Fix imports
content = content.replace("import com.aistudio.unibuddy.qywvsp.data.AssessmentStatus\n", "")

# Fix logic for passedSubjects
content = content.replace("val passedSubjects = history.filter { it.record.status == AssessmentStatus.APROBADO || it.record.status == AssessmentStatus.CONVALIDADO }.distinctBy { it.subjectName }", "val passedSubjects = history.filter { it.record.grade >= 60.0 }.distinctBy { it.subjectName }")

# Fix logic for allGrades
content = content.replace("val allGrades = history.filter { (it.record.status == AssessmentStatus.APROBADO || it.record.status == AssessmentStatus.REPROBADO) && it.record.grade > 0 }", "val allGrades = history.filter { it.record.grade > 0.0 }")

# Fix timeline passed count
content = content.replace("val passed = records.count { it.record.status == AssessmentStatus.APROBADO }", "val passed = records.count { it.record.grade >= 60.0 }")

# Add MascotPose and BuddyMascot imports
content = content.replace("import com.aistudio.unibuddy.qywvsp.data.StaticPensumSubject", "import com.aistudio.unibuddy.qywvsp.data.StaticPensumSubject\nimport com.aistudio.unibuddy.qywvsp.ui.BuddyMascot\nimport com.aistudio.unibuddy.qywvsp.ui.MascotPose")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/PensumStatsTab.kt", "w") as f:
    f.write(content)
