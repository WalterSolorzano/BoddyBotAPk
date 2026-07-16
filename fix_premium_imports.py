import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/widget/PremiumWidgets.kt", "r") as f:
    content = f.read()

# Remove the bad imports I appended
bad_imports = """
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.glance.appwidget.state.updateAppWidgetState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Tasks
import android.annotation.SuppressLint"""

content = content.replace(bad_imports, "")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/widget/PremiumWidgets.kt", "w") as f:
    f.write(content)

