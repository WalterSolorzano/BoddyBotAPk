import re
with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

content = content.replace("            hasRecentAttendance = attendanceLogs.any { it.date == todayStr || it.date == yesterdayStr }\n\n\n            )\n               \n            BuddyMoodHistoryWidget", "            hasRecentAttendance = attendanceLogs.any { it.date == todayStr || it.date == yesterdayStr }\n\n            BuddyMoodHistoryWidget")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
