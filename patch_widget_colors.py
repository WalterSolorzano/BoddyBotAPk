import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/widget/TamagotchiWidget.kt", "r") as f:
    content = f.read()

# val themeBgColor = Color(0xFF1A237E) // Beautiful Indigo Navy
content = content.replace("Color(0xFF1A237E)", "Color(0xFF001D36)") # NavyBlue
# val textColorSecondary = Color(0xFF94A3B8) // Slate Gray for secondary details
content = content.replace("Color(0xFF94A3B8)", "Color(0xFF44474E)") # SlateGray
# Color(0xFFEF4444)
content = content.replace("Color(0xFFEF4444)", "Color(0xFFBA1A1A)") # ProRed/Terracotta
# Color(0xFF475569)
content = content.replace("Color(0xFF475569)", "Color(0xFF44474E)") # SlateGray
# Color(0xFFFCA5A5)
content = content.replace("Color(0xFFFCA5A5)", "Color(0xFFF7F9FF)") # BackgroundGray

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/widget/TamagotchiWidget.kt", "w") as f:
    f.write(content)
