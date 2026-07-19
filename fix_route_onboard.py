import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/OnboardingScreen.kt", "r") as f:
    content = f.read()

# Add destInput state
content = content.replace("var originInput by remember { mutableStateOf(\"Casa\") }", 
                          "var originInput by remember { mutableStateOf(\"Casa\") }\n    var destInput by remember { mutableStateOf(\"Campus Principal\") }")

# Replace saveRoute call
content = content.replace("viewModel.saveRoute(originInput, \"Universidad\")", "viewModel.saveRoute(originInput, destInput)")

# Add destInput parameter to Step5Route
step5_sig_old = """@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Step5Route(
    originInput: String,
    onOriginChange: (String) -> Unit,
    baseTravelMinutes: String,
    onTravelMinutesChange: (String) -> Unit
) {"""

step5_sig_new = """@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Step5Route(
    originInput: String,
    onOriginChange: (String) -> Unit,
    destInput: String,
    onDestChange: (String) -> Unit,
    baseTravelMinutes: String,
    onTravelMinutesChange: (String) -> Unit
) {"""
content = content.replace(step5_sig_old, step5_sig_new)

# Update Step5Route call
step5_call_old = """                Step5Route(
                    originInput = originInput,
                    onOriginChange = { originInput = it },
                    baseTravelMinutes = baseTravelMinutes,
                    onTravelMinutesChange = { baseTravelMinutes = it }
                )"""

step5_call_new = """                Step5Route(
                    originInput = originInput,
                    onOriginChange = { originInput = it },
                    destInput = destInput,
                    onDestChange = { destInput = it },
                    baseTravelMinutes = baseTravelMinutes,
                    onTravelMinutesChange = { baseTravelMinutes = it }
                )"""
content = content.replace(step5_call_old, step5_call_new)

# Update Step5Route UI to look better and include Destination
step5_ui_old = """    OutlinedTextField(
        value = originInput,
        onValueChange = onOriginChange,
        label = { Text("Nombre de tu ubicación") },
        placeholder = { Text("Ej. Casa, Trabajo") },
        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = DarkGreen) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NavyBlue,
            focusedLabelColor = NavyBlue
        )
    )
        
    Spacer(modifier = Modifier.height(12.dp))"""

step5_ui_new = """    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = originInput,
            onValueChange = onOriginChange,
            label = { Text("Origen") },
            placeholder = { Text("Ej. Casa") },
            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = NavyBlue) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyBlue, focusedLabelColor = NavyBlue)
        )
        OutlinedTextField(
            value = destInput,
            onValueChange = onDestChange,
            label = { Text("Destino") },
            placeholder = { Text("Ej. Campus") },
            leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = DarkGreen) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyBlue, focusedLabelColor = NavyBlue)
        )
    }
    Spacer(modifier = Modifier.height(12.dp))"""
content = content.replace(step5_ui_old, step5_ui_new)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/OnboardingScreen.kt", "w") as f:
    f.write(content)
