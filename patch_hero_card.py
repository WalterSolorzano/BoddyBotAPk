with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/HeroNextClassCard.kt", "r") as f:
    content = f.read()

sig_old = """    isExamMode: Boolean,
    importanceLevel: String,
    estimatedTravelMinutes: Int
) {"""
sig_new = """    isExamMode: Boolean,
    importanceLevel: String,
    estimatedTravelMinutes: Int,
    isOutOfRange: Boolean = false
) {"""
content = content.replace(sig_old, sig_new)

logic_old = """    if (parsedStartTime != null) {
        val (startHour, startMin) = parsedStartTime"""
logic_new = """    if (parsedStartTime != null) {
        val (startHour, startMin) = parsedStartTime
        
        if (isOutOfRange) {
            statusText = "Modo Fuera de Rango (suspendido)"
            statusColor = Terracotta
            statusBgColor = Terracotta.copy(alpha = 0.1f)
            departureTimeStr = "--:--"
        } else {"""
content = content.replace(logic_old, logic_new)

logic_end = """                statusBgColor = MintGreen.copy(alpha = 0.1f)
            }
        }
    }"""
logic_end_new = """                statusBgColor = MintGreen.copy(alpha = 0.1f)
            }
        }
        }
    }"""
content = content.replace(logic_end, logic_end_new)

ui_old = """                    Text(
                        text = "Trayecto estimado de $estimatedTravelMinutes min de puerta a puerta.",
                        fontSize = 12.sp,
                        color = SlateGray,
                        lineHeight = 15.sp
                    )"""
ui_new = """                    Text(
                        text = if (isOutOfRange) "Estás muy lejos. Cálculo de ruta automático suspendido temporalmente." else "Trayecto estimado de $estimatedTravelMinutes min de puerta a puerta.",
                        fontSize = 12.sp,
                        color = SlateGray,
                        lineHeight = 15.sp
                    )"""
content = content.replace(ui_old, ui_new)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/HeroNextClassCard.kt", "w") as f:
    f.write(content)
