import re
with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if line == "            )\n":
        continue
    new_lines.append(line)

content = "".join(new_lines)

# Fix WellnessWidget
content = content.replace("                statusText = stressStatusText\n\n            UpcomingAssessmentsWidget(", "                statusText = stressStatusText\n            )\n\n            UpcomingAssessmentsWidget(")
# Fix UpcomingAssessmentsWidget
content = content.replace("                onNavigateToSubject = { onNavigateToDetails(it) }\n\n            UpcomingTasksWidget(", "                onNavigateToSubject = { onNavigateToDetails(it) }\n            )\n\n            UpcomingTasksWidget(")
# Fix UpcomingTasksWidget
content = content.replace("                onNavigateToSubject = { onNavigateToDetails(it) }\n            val currentWeighted", "                onNavigateToSubject = { onNavigateToDetails(it) }\n            )\n            val currentWeighted")
# Fix GradesHistoryWidget
content = content.replace("                currentWeighted = currentWeighted\n            \n            Spacer(modifier = Modifier.height(8.dp))", "                currentWeighted = currentWeighted\n            )\n            Spacer(modifier = Modifier.height(8.dp))")
# Fix SmartRouteReminderCard
content = content.replace("                isRaining = isRaining\n\n            GPSAndStopwatchWidget(", "                isRaining = isRaining\n            )\n\n            GPSAndStopwatchWidget(")
# Fix GPSAndStopwatchWidget
content = content.replace("                    android.widget.Toast.makeText(context, \"¡Viaje finalizado! Registrado: $mins min.\", android.widget.Toast.LENGTH_LONG).show()\n                }", "                    android.widget.Toast.makeText(context, \"¡Viaje finalizado! Registrado: $mins min.\", android.widget.Toast.LENGTH_LONG).show()\n                }\n            )")

# Also, there was another one: `TodayClassesListWidget`
content = content.replace("                onToggleCancelled = { viewModel.toggleCancelledClass(it, currentDateStr) }\n\n            val buddyXp", "                onToggleCancelled = { viewModel.toggleCancelledClass(it, currentDateStr) }\n            )\n\n            val buddyXp")

# Also, `BuddyMoodHistoryWidget`
content = content.replace("                hasRecentActivity = hasRecentAttendance || attendanceLogs.isEmpty()\n\n            BuddyMoodHistoryWidget", "                hasRecentActivity = hasRecentAttendance || attendanceLogs.isEmpty()\n            )\n\n            BuddyMoodHistoryWidget")

# `TripHistoryWidget`
content = content.replace("                tripRecords = tripRecords\n\n            Spacer(modifier = Modifier.height(16.dp))", "                tripRecords = tripRecords\n            )\n\n            Spacer(modifier = Modifier.height(16.dp))")

# `AdviceCarouselWidget` wait, I removed it.

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
