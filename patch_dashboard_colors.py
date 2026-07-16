with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

content = content.replace("Color(0xFFFFB74D)", "Amber")
content = content.replace("Color(0xFFF57C00)", "Terracotta")
content = content.replace("Color(0xFFE65100)", "Terracotta")
content = content.replace("Color(0xFF4F46E5)", "ProBlue")
content = content.replace("Color(0xFFF1F5F9)", "BackgroundGray")
content = content.replace("Color(0xFFE0F7FA)", "MintGreen.copy(alpha=0.1f)")
content = content.replace("Color(0xFFEDE7F6)", "ProBlue.copy(alpha=0.1f)")
content = content.replace("Color(0xFFFFEBEE)", "Terracotta.copy(alpha=0.1f)")
content = content.replace("Color(0xFF006064)", "MintGreen")
content = content.replace("Color(0xFF311B92)", "ProBlue")
content = content.replace("Color(0xFFB71C1C)", "Terracotta")
content = content.replace("Color(0xFFE8F0FE)", "ProBlue.copy(alpha=0.05f)")
content = content.replace("Color(0xFF1967D2)", "ProBlue")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
