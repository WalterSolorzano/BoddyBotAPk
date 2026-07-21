import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

# find the function definition
start_idx = content.find("fun SettingsScreen(")

new_func = """fun SettingsScreen(
    viewModel: UniBuddyViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateToCareer: () -> Unit,
    onNavigateToRoutes: () -> Unit,
    onNavigateToSystem: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(350)
        isLoading = false
    }
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize().background(BackgroundBone),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(color = ProBlue) }
        return
    }

    val username by viewModel.username.collectAsStateWithLifecycle()
    val buddyColorStr by viewModel.buddyColor.collectAsStateWithLifecycle()
    val mainBuddyColor = Color(android.graphics.Color.parseColor(buddyColorStr))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("config_column")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BuddyMascot(
                modifier = Modifier.size(80.dp),
                pose = "greeting",
                mainColor = mainBuddyColor,
                accessory = viewModel.buddyAccessory.collectAsStateWithLifecycle().value
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = "Hola, $username", style = MaterialTheme.typography.headlineMedium, color = NavyBlue, fontWeight = FontWeight.Bold)
                Text(text = "Tu Mochila Académica", style = MaterialTheme.typography.bodyMedium, color = SlateGray)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val gridItems = listOf(
            ConfigGridItem("Perfil y Personalización", Icons.Rounded.Face, "Nombre, Foto, Mascota") { onNavigateToProfile() },
            ConfigGridItem("Mi Carrera", Icons.Rounded.School, "Pensum, Historial, Semestre") { onNavigateToCareer() },
            ConfigGridItem("Rutas", Icons.Rounded.Explore, "Origen, Destino, Tiempos") { onNavigateToRoutes() },
            ConfigGridItem("Sistema", Icons.Rounded.Settings, "Backup, PDF, Reportes") { onNavigateToSystem() }
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(gridItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clickable { item.onClick() },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = NavyBlue,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = item.title, fontWeight = FontWeight.Bold, color = NavyBlue, fontSize = 14.sp, textAlign = TextAlign.Center)
                        Text(text = item.subtitle, fontSize = 10.sp, color = SlateGray, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}
"""

end_idx = content.find("data class ConfigGridItem", start_idx)

content = content[:start_idx] + new_func + "\n" + content[end_idx:]

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
