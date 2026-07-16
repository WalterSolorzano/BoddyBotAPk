import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

def insert_after(content, search_str, insert_str):
    return content.replace(search_str, search_str + "\n            " + insert_str)

content = insert_after(content, "repository.insertAbsence(Absence(subjectId = subjectId, date = actualDate))", "updateWidgets()")
content = insert_after(content, "repository.deleteAbsenceById(absenceId)", "updateWidgets()")
content = insert_after(content, "repository.insertAssessment(Assessment(subjectId = subjectId, name = name, grade = grade, percentage = percentage, examDate = examDate))", "updateWidgets()")
content = insert_after(content, "repository.deleteAssessmentById(assessmentId)", "updateWidgets()")

# And updateAssessment? Let's check if there is an update method
