import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/NotificationHelper.kt", "r") as f:
    content = f.read()

old_fun = """    fun sendNextClassNotification(context: Context, subjectId: Int, destinationName: String, title: String, message: String) {"""

new_fun = """    fun sendNextClassNotification(context: Context, subjectId: Int, destinationName: String, title: String, message: String, isCritical: Boolean = false) {"""

content = content.replace(old_fun, new_fun)

old_builder = """        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setColor(android.graphics.Color.parseColor("#F97316")) // Amber
            .setAutoCancel(true)
            .setLights(android.graphics.Color.parseColor("#F97316"), 1000, 1000)
            .addAction(android.R.drawable.ic_menu_today, "Marcar Asistencia", attendPendingIntent)
            .addAction(android.R.drawable.ic_menu_send, "Avisar Retraso", latePendingIntent)"""

new_builder = """        val colorHex = if (isCritical) "#BA1A1A" else "#F97316"
        val mascotMessage = if (isCritical) "$message (ಠ_ಠ)" else "$message (•‿•)"
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(mascotMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mascotMessage))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setColor(android.graphics.Color.parseColor(colorHex))
            .setAutoCancel(true)
            .setLights(android.graphics.Color.parseColor(colorHex), 1000, 1000)
            .addAction(android.R.drawable.ic_menu_today, "Marcar Asistencia", attendPendingIntent)
            .addAction(android.R.drawable.ic_menu_send, "Avisar Retraso", latePendingIntent)
            
        if (isCritical) {
            builder.setColorized(true)
        }"""

content = content.replace(old_builder, new_builder)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/NotificationHelper.kt", "w") as f:
    f.write(content)
