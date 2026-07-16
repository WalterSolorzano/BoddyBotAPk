import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

# registerAbsence
content = re.sub(r"(repository\.insertAbsence\(Absence\(subjectId = subjectId, date = actualDate\)\))", r"\1\n            notifyWidgets()", content)
# deleteAbsence
content = re.sub(r"(repository\.deleteAbsenceById\(absenceId\))", r"\1\n            notifyWidgets()", content)
# addAssessment - line 1259
content = re.sub(r"(repository\.insertAssessment\(Assessment\(subjectId = subjectId, name = name, grade = grade, percentage = percentage, examDate = examDate\)\))", r"\1\n            notifyWidgets()", content)
# deleteAssessment
content = re.sub(r"(repository\.deleteAssessmentById\(assessmentId\))", r"\1\n            notifyWidgets()", content)
# updateAssessment if it exists, let's just make sure
content = re.sub(r"(repository\.updateAssessment\(.*?\))", r"\1\n            notifyWidgets()", content)

# toggleTask
content = re.sub(r"(repository\.updateTask\(task\.copy\(isCompleted = !task\.isCompleted\)\))", r"\1\n            notifyWidgets()", content)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
