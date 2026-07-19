with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyApp.kt", "r") as f:
    lines = f.readlines()

new_lines = []
for i in range(len(lines)):
    if "Column(" in lines[i] and i+1 < len(lines):
        if "onDismissRequest =" in lines[i+1] or "title =" in lines[i+1] or "title =" in lines[i+2]:
            lines[i] = lines[i].replace("Column(", "AlertDialog(")
    new_lines.append(lines[i])

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyApp.kt", "w") as f:
    f.writelines(new_lines)
