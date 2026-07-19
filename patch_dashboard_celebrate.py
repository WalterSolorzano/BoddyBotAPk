import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

import_code = "import kotlinx.coroutines.delay\n"
if "import kotlinx.coroutines.delay" not in content:
    content = content.replace("import androidx.compose.runtime.*", "import androidx.compose.runtime.*\nimport kotlinx.coroutines.delay\nimport kotlinx.coroutines.flow.collectLatest\n")

# In DashboardScreen
old_vm_vars = """    val isTripActive by viewModel.isTripActive.collectAsStateWithLifecycle()
    val tripElapsedSeconds by viewModel.tripElapsedSeconds.collectAsStateWithLifecycle()"""

new_vm_vars = """    val isTripActive by viewModel.isTripActive.collectAsStateWithLifecycle()
    val tripElapsedSeconds by viewModel.tripElapsedSeconds.collectAsStateWithLifecycle()
    var isCelebrating by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.buddyCelebrationEvent.collectLatest {
            isCelebrating = true
            delay(3000)
            isCelebrating = false
        }
    }"""

content = content.replace(old_vm_vars, new_vm_vars)

# In BuddyMascotRoomWidget call
old_widget_call = """            BuddyMascotRoomWidget(
                absencesCount = absences.size, 
                assessments = assessments,
                buddyXp = buddyXp,
                examTomorrow = examTomorrow
            )"""

new_widget_call = """            BuddyMascotRoomWidget(
                absencesCount = absences.size, 
                assessments = assessments,
                buddyXp = buddyXp,
                examTomorrow = examTomorrow,
                isCelebrating = isCelebrating
            )"""

content = content.replace(old_widget_call, new_widget_call)

# In BuddyMascotRoomWidget def
old_widget_def = """fun BuddyMascotRoomWidget(
    absencesCount: Int,
    assessments: List<com.aistudio.unibuddy.qywvsp.data.Assessment>,
    buddyXp: Int,
    examTomorrow: Boolean
) {"""

new_widget_def = """fun BuddyMascotRoomWidget(
    absencesCount: Int,
    assessments: List<com.aistudio.unibuddy.qywvsp.data.Assessment>,
    buddyXp: Int,
    examTomorrow: Boolean,
    isCelebrating: Boolean = false
) {"""

content = content.replace(old_widget_def, new_widget_def)

# In BuddyMascotRoomWidget pose logic
old_pose = """                        val finalPose = if (examTomorrow) "working" else if (absencesCount >= 3) "worried" else "normal"
                        BuddyMascot(
                            modifier = Modifier.size(50.dp),
                            isHappy = absencesCount < 3,"""

new_pose = """                        val finalPose = if (isCelebrating) "celebrating" else if (examTomorrow) "working" else if (absencesCount >= 3) "worried" else "normal"
                        BuddyMascot(
                            modifier = Modifier.size(50.dp),
                            isHappy = (absencesCount < 3) || isCelebrating,"""

content = content.replace(old_pose, new_pose)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
