import re

with open("app/src/main/AndroidManifest.xml", "r") as f:
    content = f.read()

old_perms = """    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />"""

new_perms = """    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
    <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />"""

content = content.replace(old_perms, new_perms)

with open("app/src/main/AndroidManifest.xml", "w") as f:
    f.write(content)
