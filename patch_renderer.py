import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/widget/PetBitmapRenderer.kt", "r") as f:
    content = f.read()

old_sig = """    suspend fun generateAndSavePetBitmap(
        context: Context,
        pose: String,
        accessory: String,
        isHappy: Boolean,
        isWorried: Boolean,
        mainColorHex: String
    ): Boolean = withContext(Dispatchers.Main) {"""

new_sig = """    suspend fun generateAndSavePetBitmap(
        context: Context,
        pose: String,
        accessory: String,
        isHappy: Boolean,
        isWorried: Boolean,
        weatherState: String,
        mainColorHex: String
    ): Boolean = withContext(Dispatchers.Main) {"""

old_call = """                    BuddyMascot(
                        modifier = Modifier.size(120.dp),
                        isWorried = isWorried,
                        isHappy = isHappy,
                        pose = pose,
                        accessory = accessory,
                        mainColor = color
                    )"""

new_call = """                    BuddyMascot(
                        modifier = Modifier.size(120.dp),
                        isWorried = isWorried,
                        isHappy = isHappy,
                        pose = pose,
                        accessory = accessory,
                        weatherState = weatherState,
                        mainColor = color
                    )"""

content = content.replace(old_sig, new_sig).replace(old_call, new_call)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/widget/PetBitmapRenderer.kt", "w") as f:
    f.write(content)
