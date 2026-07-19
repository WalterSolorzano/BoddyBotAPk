import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/PensumStatsTab.kt", "r") as f:
    content = f.read()

content = content.replace("BuddyMascot(\n                    pose = if (progress >= 1f) MascotPose.HAPPY else MascotPose.NEUTRAL,\n                    accessory = \"ninguno\", color = \"#FFB3E5FC\", size = 80\n                )", "BuddyMascot(\n                    modifier = Modifier.size(80.dp),\n                    pose = if (progress >= 1f) \"celebrating\" else \"idle\",\n                    isHappy = (progress >= 1f),\n                    isWorried = false,\n                    mainColor = Color(0xFFB3E5FC)\n                )")

content = content.replace("BuddyMascot(pose = MascotPose.HAPPY, accessory = \"ninguno\", color = \"#FFB3E5FC\", size = 24)", "BuddyMascot(modifier = Modifier.size(24.dp), pose = \"celebrating\", isHappy = true, isWorried = false, mainColor = Color(0xFFB3E5FC))")

content = content.replace("BuddyMascot(pose = MascotPose.WORRIED, accessory = \"ninguno\", color = \"#FFB3E5FC\", size = 24)", "BuddyMascot(modifier = Modifier.size(24.dp), pose = \"idle\", isHappy = false, isWorried = true, mainColor = Color(0xFFB3E5FC))")

content = content.replace("import com.aistudio.unibuddy.qywvsp.ui.components.MascotPose\n", "")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/PensumStatsTab.kt", "w") as f:
    f.write(content)
