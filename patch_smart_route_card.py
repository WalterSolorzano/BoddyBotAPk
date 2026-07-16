with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/SmartRouteReminderCard.kt", "r") as f:
    content = f.read()

sig_old = """    onConfigureRoute: () -> Unit,
    weatherDescription: String = "Despejado",
    isRaining: Boolean = false,
    modifier: Modifier = Modifier
) {"""
sig_new = """    onConfigureRoute: () -> Unit,
    weatherDescription: String = "Despejado",
    isRaining: Boolean = false,
    isOutOfRange: Boolean = false,
    modifier: Modifier = Modifier
) {"""
content = content.replace(sig_old, sig_new)

dist_old = """                            if (distanceKm > 0.0) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = SlateGray, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "A ${String.format(java.util.Locale.US, "%.1f", distanceKm)} km de distancia",
                                        fontSize = 11.sp,
                                        color = SlateGray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }"""
dist_new = """                            if (isOutOfRange) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Terracotta, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "Fuera de Rango (>100 km)",
                                        fontSize = 11.sp,
                                        color = Terracotta,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else if (distanceKm > 0.0) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = SlateGray, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "A ${String.format(java.util.Locale.US, "%.1f", distanceKm)} km de distancia",
                                        fontSize = 11.sp,
                                        color = SlateGray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }"""
content = content.replace(dist_old, dist_new)


with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/SmartRouteReminderCard.kt", "w") as f:
    f.write(content)
