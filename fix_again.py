with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyApp.kt", "r") as f:
    lines = f.readlines()

part1 = lines[:1814] # Up to '}' of new SubjectGradeGridCard

# Find "fun GradesOverviewScreen"
start_grades = -1
for i in range(1814, len(lines)):
    if lines[i].startswith("fun GradesOverviewScreen("):
        start_grades = i - 1 # Include the @Composable before it if there is one
        if not lines[i-1].startswith("@Composable"):
            start_grades = i
        break

if start_grades != -1:
    part2 = lines[start_grades:]
    with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyApp.kt", "w") as f:
        f.writelines(part1)
        f.writelines(part2)
    print("Fixed!")
else:
    print("Not found")

