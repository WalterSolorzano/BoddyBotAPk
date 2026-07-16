import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

def inject_update_widgets(func_name, content):
    # Find the function definition
    func_idx = content.find("fun " + func_name)
    if func_idx == -1: return content
    
    # Find the next 'repository.saveSetting' or 'repository.insert' or 'repository.update' or 'repository.delete'
    # Actually, let's just find the closing brace of the launch block. This is tricky.
    
    # A simpler approach: replace known lines with the line + updateWidgets()
    return content

# Let's replace specific lines where we know the update happens.
content = content.replace("repository.saveSetting(\"buddy_xp\", newXp.toString())", "repository.saveSetting(\"buddy_xp\", newXp.toString())\n            updateWidgets()")
content = content.replace("repository.insertAssessment(assessment)", "repository.insertAssessment(assessment)\n            updateWidgets()")
content = content.replace("repository.updateAssessment(assessment)", "repository.updateAssessment(assessment)\n            updateWidgets()")
content = content.replace("repository.deleteAssessment(assessment)", "repository.deleteAssessment(assessment)\n            updateWidgets()")
content = content.replace("repository.insertAbsence(absence)", "repository.insertAbsence(absence)\n            updateWidgets()")
content = content.replace("repository.deleteAbsence(absence)", "repository.deleteAbsence(absence)\n            updateWidgets()")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)
