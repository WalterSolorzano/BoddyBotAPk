with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/SmartRouteReminderCard.kt", "r") as f:
    content = f.read()
content = content.replace("replaceFirstChar { it.uppercase() }", "replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }")
with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/SmartRouteReminderCard.kt", "w") as f:
    f.write(content)
