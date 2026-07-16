with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/GPSAndStopwatchWidget.kt", "r") as f:
    content = f.read()

# Update signature
old_sig = """@Composable
fun GPSAndStopwatchWidget(
    currentDistanceToCollege: Double?,
    locationBasedTravelMinutes: Int,
    baseTravelTimeSource: Int,
    isTripActive: Boolean,
    tripElapsedSeconds: Int,
    onRequestGPS: () -> Unit,
    onStartTrip: () -> Unit,
    onEndTrip: (Int) -> Unit
) {"""
new_sig = """@Composable
fun GPSAndStopwatchWidget(
    currentDistanceToCollege: Double?,
    locationBasedTravelMinutes: Int,
    baseTravelTimeSource: Int,
    isOutOfRange: Boolean,
    isTripActive: Boolean,
    tripElapsedSeconds: Int,
    onRequestGPS: () -> Unit,
    onStartTrip: () -> Unit,
    onEndTrip: (Int) -> Unit
) {"""

content = content.replace(old_sig, new_sig)

# Update color logic for the time
content = content.replace("color = if (currentDistanceToCollege != null && currentDistanceToCollege > 100.0) Terracotta else DarkGreen", "color = if (isOutOfRange) Terracotta else DarkGreen")
content = content.replace("val isTooFar = currentDistanceToCollege != null && currentDistanceToCollege > 100.0", "val isTooFar = isOutOfRange")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/GPSAndStopwatchWidget.kt", "w") as f:
    f.write(content)
