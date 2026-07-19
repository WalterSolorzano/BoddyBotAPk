def circle(cx, cy, r):
    return f"M {cx},{cy-r} A {r},{r} 0 1,0 {cx},{cy+r} A {r},{r} 0 1,0 {cx},{cy-r}"

def ellipse(cx, cy, rx, ry):
    return f"M {cx},{cy-ry} A {rx},{ry} 0 1,0 {cx},{cy+ry} A {rx},{ry} 0 1,0 {cx},{cy-ry}"

main_color = "#1B5E20"

paths = []
# Ear L
paths.append(f'<path android:fillColor="#FFFFFF" android:strokeColor="{main_color}" android:strokeWidth="2.64" android:pathData="{circle(37.5, 37.5, 9.9)}" />')
paths.append(f'<path android:fillColor="#FFB7B2" android:pathData="{circle(37.5, 37.5, 5.28)}" />')
# Ear R
paths.append(f'<path android:fillColor="#FFFFFF" android:strokeColor="{main_color}" android:strokeWidth="2.64" android:pathData="{circle(70.5, 37.5, 9.9)}" />')
paths.append(f'<path android:fillColor="#FFB7B2" android:pathData="{circle(70.5, 37.5, 5.28)}" />')

# Head
paths.append(f'<path android:fillColor="#FFFFFF" android:strokeColor="{main_color}" android:strokeWidth="2.64" android:pathData="{circle(54, 55.32, 23.1)}" />')

# Eyes L
paths.append(f'<path android:fillColor="{main_color}" android:pathData="{circle(46.08, 52.68, 2.97)}" />')
paths.append(f'<path android:fillColor="#FFFFFF" android:pathData="{circle(44.76, 51.36, 1.19)}" />')

# Eyes R
paths.append(f'<path android:fillColor="{main_color}" android:pathData="{circle(61.92, 52.68, 2.97)}" />')
paths.append(f'<path android:fillColor="#FFFFFF" android:pathData="{circle(60.6, 51.36, 1.19)}" />')

# Snout
paths.append(f'<path android:fillColor="#F1EEE5" android:strokeColor="{main_color}" android:strokeWidth="1.98" android:pathData="{ellipse(54, 60.27, 5.94, 4.29)}" />')

# Nose
paths.append(f'<path android:fillColor="{main_color}" android:pathData="{circle(54, 57.96, 2.64)}" />')

# Mouth (Quadratic bezier)
paths.append(f'<path android:fillColor="#00000000" android:strokeColor="{main_color}" android:strokeWidth="1.98" android:strokeLineCap="round" android:pathData="M50.7,59.94 Q54,63.24 57.3,59.94" />')


xml = f"""<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    
    {"".join(paths)}
    
</vector>
"""

with open("app/src/main/res/drawable/ic_launcher_foreground.xml", "w") as f:
    f.write(xml)

