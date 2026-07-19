with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/PensumStatsTab.kt", "r") as f:
    content = f.read()

content = content.replace("import com.aistudio.unibuddy.qywvsp.ui.BuddyMascot", "import com.aistudio.unibuddy.qywvsp.ui.components.BuddyMascot")
content = content.replace("import com.aistudio.unibuddy.qywvsp.ui.MascotPose", "import com.aistudio.unibuddy.qywvsp.ui.components.MascotPose")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/PensumStatsTab.kt", "w") as f:
    f.write(content)
