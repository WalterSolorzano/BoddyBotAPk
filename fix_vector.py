with open("app/src/main/res/drawable/ic_launcher_foreground.xml", "r") as f:
    content = f.read()

content = content.replace('<circle android:fillColor="#001D36" android:centerX="20" android:centerY="25" android:radius="4" />', 
                          '<path android:fillColor="#001D36" android:pathData="M20,21 A4,4 0 1 1 19.9,21 Z" />')
content = content.replace('<circle android:fillColor="#001D36" android:centerX="40" android:centerY="25" android:radius="4" />',
                          '<path android:fillColor="#001D36" android:pathData="M40,21 A4,4 0 1 1 39.9,21 Z" />')
content = content.replace('<circle android:fillColor="#001D36" android:centerX="30" android:centerY="31" android:radius="2" />',
                          '<path android:fillColor="#001D36" android:pathData="M30,29 A2,2 0 1 1 29.9,29 Z" />')

with open("app/src/main/res/drawable/ic_launcher_foreground.xml", "w") as f:
    f.write(content)
