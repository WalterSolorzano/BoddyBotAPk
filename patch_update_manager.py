with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UpdateManager.kt", "r") as f:
    content = f.read()

old_code = """        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }"""

new_code = """        androidx.core.content.ContextCompat.registerReceiver(context, receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), androidx.core.content.ContextCompat.RECEIVER_EXPORTED)"""

content = content.replace(old_code, new_code)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UpdateManager.kt", "w") as f:
    f.write(content)
