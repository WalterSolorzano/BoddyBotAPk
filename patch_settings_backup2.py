import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()


backup_ui = """
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)
                Text("Respaldo en la Nube (Google Drive)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyBlue)
                Text("Crea una copia de seguridad local que puedes guardar en Google Drive mediante el menú de compartir de Android, o restaura una copia anterior.", fontSize = 11.sp, color = SlateGray)
                
                val context = LocalContext.current
                val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
                    uri?.let {
                        coroutineScope.launch {
                            val success = viewModel.exportDatabaseToUri(context, it)
                            if (success) {
                                Toast.makeText(context, "Respaldo exportado exitosamente. Súbelo a Drive.", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Error al exportar.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                
                val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                    uri?.let {
                        coroutineScope.launch {
                            val success = viewModel.importDatabaseFromUri(context, it)
                            if (success) {
                                Toast.makeText(context, "Respaldo restaurado. Cierra y vuelve a abrir la app.", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Error al restaurar el respaldo.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { exportLauncher.launch("unibuddy_backup.db") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ProBlue)
                    ) {
                        Icon(androidx.compose.material.icons.Icons.Default.Upload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Exportar", color = Color.White, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { importLauncher.launch(arrayOf("application/octet-stream", "application/x-sqlite3", "*/*")) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                    ) {
                        Icon(androidx.compose.material.icons.Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restaurar", color = Color.White, fontSize = 12.sp)
                    }
                }
"""

content = content.replace("""                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)
                Text("Valores por Defecto para Evaluaciones", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyBlue)""", backup_ui + """                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)
                Text("Valores por Defecto para Evaluaciones", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyBlue)""")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
