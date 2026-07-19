import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/PensumMapTab.kt", "r") as f:
    content = f.read()

content = content.replace("fun PensumMapTab(staticPensum: List<StaticPensumSubject>, history: List<AcademicRecordWithSubject>, professors: List<Professor>)", "fun PensumMapTab(staticPensum: List<StaticPensumSubject>, history: List<AcademicRecordWithSubject>, professors: List<Professor>, ongoingSubjects: List<String>)")

content = content.replace("fun determineNodeState(subjectCode: String, history: List<AcademicRecordWithSubject>, staticPensum: List<StaticPensumSubject>): NodeState {", "fun determineNodeState(subjectCode: String, subjectName: String, history: List<AcademicRecordWithSubject>, staticPensum: List<StaticPensumSubject>, ongoingSubjects: List<String>): NodeState {")

logic = """
    val records = history.filter { it.subjectName == subjectCode || it.subjectName.equals(subjectName, ignoreCase = true) }
    
    val passedRecords = records.filter { it.record.grade >= 60.0 }
    if (passedRecords.isNotEmpty()) {
        val anyFirst = passedRecords.any { it.record.status == AssessmentStatus.NF_R }
        return if (anyFirst) NodeState.PASSED_FIRST else NodeState.PASSED_RECOVERY
    }
    
    if (ongoingSubjects.any { it.equals(subjectName, ignoreCase = true) }) {
        return NodeState.ONGOING
    }
    
    val subject = staticPensum.find { it.code == subjectCode } ?: return NodeState.NO_DATA
    val prereqs = subject.prereqs
    if (prereqs.isEmpty()) return NodeState.NO_DATA
    
    val prereqsPassed = prereqs.all { prereqCode ->
        val prereqSubject = staticPensum.find { it.code == prereqCode }
        val prereqName = prereqSubject?.name ?: ""
        val pRecords = history.filter { it.subjectName == prereqCode || it.subjectName.equals(prereqName, ignoreCase = true) }
        pRecords.any { it.record.grade >= 60.0 }
    }
    
    return if (prereqsPassed) NodeState.NO_DATA else NodeState.LOCKED
"""

content = re.sub(r"    val records = history\.filter \{ it\.subjectName == subjectCode \}.*?return if \(prereqsPassed\) NodeState\.NO_DATA else NodeState\.LOCKED", logic, content, flags=re.DOTALL)

content = content.replace("determineNodeState(prereqCode, history, staticPensum)", "determineNodeState(prereqCode, staticPensum.find { it.code == prereqCode }?.name ?: \"\", history, staticPensum, ongoingSubjects)")

content = content.replace("determineNodeState(subject.code, history, staticPensum)", "determineNodeState(subject.code, subject.name, history, staticPensum, ongoingSubjects)")

content = content.replace("import androidx.compose.ui.text.font.FontWeight", "")
content = content.replace("package com.aistudio.unibuddy.qywvsp.ui.screens\n", "package com.aistudio.unibuddy.qywvsp.ui.screens\n\nimport androidx.compose.ui.text.font.FontWeight\n")

content = content.replace("LegendItem(Color(0xFF2196F3), \"Cursando\", true)", "LegendItem(Color(0xFF2196F3), \"Cursando\")")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/PensumMapTab.kt", "w") as f:
    f.write(content)
