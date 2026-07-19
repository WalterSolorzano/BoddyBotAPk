import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/widget/AcademicWeatherWidget.kt", "r") as f:
    content = f.read()

import_code = "import androidx.glance.ImageProvider\n"
new_import = import_code + "import java.io.File\nimport android.graphics.BitmapFactory\n"
if "import java.io.File" not in content:
    content = content.replace(import_code, new_import)

old_hud = """            val iconRes = when (weatherState) {
                "sunny" -> R.drawable.buddy_widget_soleado
                "rainy" -> R.drawable.buddy_widget_lluvia
                else -> R.drawable.buddy_widget_noche
            }

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(bgColor)
                    .clickable(actionStartActivity<MainActivity>()),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                    modifier = GlanceModifier.padding(16.dp)
                ) {
                    Image(
                        provider = ImageProvider(iconRes),
                        contentDescription = "Academic Weather",
                        modifier = GlanceModifier.size(100.dp)
                    )"""

new_hud = """            val iconRes = when (weatherState) {
                "sunny" -> R.drawable.buddy_widget_soleado
                "rainy" -> R.drawable.buddy_widget_lluvia
                else -> R.drawable.buddy_widget_noche
            }

            val widgetFile = File(context.filesDir, "widget_pet_current.png")
            val imageProvider = if (widgetFile.exists()) {
                try {
                    val bitmap = BitmapFactory.decodeFile(widgetFile.absolutePath)
                    if (bitmap != null) {
                        ImageProvider(bitmap)
                    } else {
                        ImageProvider(iconRes)
                    }
                } catch (e: Exception) {
                    ImageProvider(iconRes)
                }
            } else {
                ImageProvider(iconRes)
            }

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(bgColor)
                    .clickable(actionStartActivity<MainActivity>()),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                    modifier = GlanceModifier.padding(16.dp)
                ) {
                    Image(
                        provider = imageProvider,
                        contentDescription = "Academic Weather",
                        modifier = GlanceModifier.size(100.dp)
                    )"""

content = content.replace(old_hud, new_hud)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/widget/AcademicWeatherWidget.kt", "w") as f:
    f.write(content)
