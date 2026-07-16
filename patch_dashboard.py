with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

# Add isOutOfRange state
prop = "    val isOutOfRange by viewModel.isOutOfRange.collectAsStateWithLifecycle()\n"
content = content.replace("    val currentDistanceToCollege by viewModel.currentDistanceToCollege.collectAsStateWithLifecycle()\n", "    val currentDistanceToCollege by viewModel.currentDistanceToCollege.collectAsStateWithLifecycle()\n" + prop)

# Add to HeroNextClassCard
content = content.replace("estimatedTravelMinutes = estimatedTravelMins", "estimatedTravelMinutes = estimatedTravelMins,\n                    isOutOfRange = isOutOfRange")

# Add to SmartRouteReminderCard
# distanceKm = currentDistanceToCollege ?: 0.0,
content = content.replace("distanceKm = currentDistanceToCollege ?: 0.0,", "distanceKm = currentDistanceToCollege ?: 0.0,\n                isOutOfRange = isOutOfRange,")

# Add to GPSAndStopwatchWidget
content = content.replace("currentDistanceToCollege = currentDistance,", "currentDistanceToCollege = currentDistance,\n                isOutOfRange = isOutOfRange,")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
