import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

old_func_start = """fun BuddyCustomizationDialog(viewModel: UniBuddyViewModel, onDismiss: () -> Unit) {
    val accessory by viewModel.buddyAccessory.collectAsStateWithLifecycle()
    val buddyColorStr by viewModel.buddyColor.collectAsStateWithLifecycle()
    val buddyPose by viewModel.buddyPose.collectAsStateWithLifecycle()"""

new_func_start = """fun BuddyCustomizationDialog(viewModel: UniBuddyViewModel, onDismiss: () -> Unit) {
    val accessory by viewModel.buddyAccessory.collectAsStateWithLifecycle()
    val buddyColorStr by viewModel.buddyColor.collectAsStateWithLifecycle()
    val buddyPose by viewModel.buddyPose.collectAsStateWithLifecycle()
    val badges by viewModel.badges.collectAsStateWithLifecycle()
    
    val accessoryRequirements = mapOf(
        "hat" to "Primeros Pasos",
        "cap" to "Estudiante Responsable",
        "glasses" to "Concentración Total",
        "sunglasses" to "En el Top"
    )
    
    val isAccessoryUnlocked: (String) -> Boolean = { acc ->
        if (acc == "none") true
        else {
            val reqBadge = accessoryRequirements[acc]
            if (reqBadge != null) {
                badges.find { it.name == reqBadge }?.isUnlocked == true
            } else {
                true
            }
        }
    }"""

content = content.replace(old_func_start, new_func_start)

old_chip = """                    items(accessories) { acc ->
                        FilterChip(
                            selected = accessory == acc,
                            onClick = { viewModel.saveBuddyCustomization(acc, buddyColorStr) },
                            label = { Text(accessoriesLabels[acc] ?: acc.replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NavyBlue.copy(alpha = 0.15f),
                                selectedLabelColor = NavyBlue
                            )
                        )
                    }"""

new_chip = """                    items(accessories) { acc ->
                        val unlocked = isAccessoryUnlocked(acc)
                        FilterChip(
                            selected = accessory == acc && unlocked,
                            onClick = { 
                                if (unlocked) {
                                    viewModel.saveBuddyCustomization(acc, buddyColorStr)
                                }
                            },
                            label = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!unlocked) {
                                        Icon(Icons.Default.Lock, contentDescription = "Locked", modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(accessoriesLabels[acc] ?: acc.replaceFirstChar { it.uppercase() })
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NavyBlue.copy(alpha = 0.15f),
                                selectedLabelColor = NavyBlue
                            )
                        )
                    }"""

content = content.replace(old_chip, new_chip)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
