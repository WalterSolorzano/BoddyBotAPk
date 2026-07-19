package com.aistudio.unibuddy.qywvsp.data

class HistorialParser {

    data class ParsedSubject(
        val code: String,
        val name: String,
        val credits: Double,
        val grade: Double,
        val status: AssessmentStatus,
        val group: String,
        val semester: String,
        val year: String,
        var matchedCode: String? = null,
        var matchedName: String? = null,
        var professorName: String = ""
    )

    fun parsePdfText(rawText: String): List<ParsedSubject> {
        // 1. Extracción de Datos del Estudiante
        val studentRegex = Regex("""Estudiante:\s+(\d{4}-\d{4}[a-zA-Z])\s+(.+)""")
        val studentMatch = studentRegex.find(rawText)
        val carnet = studentMatch?.groupValues?.get(1)
        val nombre = studentMatch?.groupValues?.get(2)

        // 2. Detección de Semestres y Clases
        val lines = rawText.split('\n')
        var currentSemester = ""
        var currentYear = ""
        
        val semesterRegex = Regex("""^(PRIMER|SEGUNDO|TERCER|CUARTO|QUINTO|SEXTO|SEPTIMO|OCTAVO|NOVENO|DECIMO) SEMESTRE (\d{4})$""")
        val veranoRegex = Regex("""^CURSO DE VERANO (\d{4})$""")
        
        // 3. Regex Maestro para Clases (Más flexible para decimales opcionales)
        val classRegex = Regex("""^([A-Z0-9]{5,7})\s+(.+?)\s+(\d+\.?\d*)\s+(\d+\.?\d*)\s+->([A-Z_]+)\s+([A-Z0-9\-]+)\s+(\d+)$""")

        val parsedSubjects = mutableListOf<ParsedSubject>()

        for (line in lines) {
            val trimLine = line.trim()
            if (trimLine.isBlank()) continue

            try {
                // Revisar si cambiamos de semestre
                val semesterMatch = semesterRegex.find(trimLine)
                if (semesterMatch != null) {
                    currentSemester = semesterMatch.groupValues[1]
                    currentYear = semesterMatch.groupValues[2]
                    continue
                }

                val veranoMatch = veranoRegex.find(trimLine)
                if (veranoMatch != null) {
                    currentSemester = "VERANO"
                    currentYear = veranoMatch.groupValues[1]
                    continue
                }

                // Extraer la clase
                val classMatch = classRegex.find(trimLine)
                if (classMatch != null) {
                    val rawStatus = classMatch.groupValues[5]
                    val status = when(rawStatus) {
                        "NF_R" -> AssessmentStatus.NF_R
                        "IC_R" -> AssessmentStatus.IC_R
                        "IIC_R" -> AssessmentStatus.IIC_R
                        "NF_CV" -> AssessmentStatus.NF_CV
                        else -> AssessmentStatus.UNKNOWN
                    }

                    parsedSubjects.add(
                        ParsedSubject(
                            code = classMatch.groupValues[1],
                            name = classMatch.groupValues[2].trim(),
                            credits = classMatch.groupValues[3].toDoubleOrNull() ?: 0.0,
                            grade = classMatch.groupValues[4].toDoubleOrNull() ?: 0.0,
                            status = status,
                            group = classMatch.groupValues[6],
                            semester = currentSemester.ifEmpty { "S/D" },
                            year = currentYear.ifEmpty { "S/D" }
                        )
                    )
                }
            } catch (e: Exception) {
                // Ignorar línea problemática pero no crashear
                e.printStackTrace()
            }
        }
        
        return parsedSubjects
    }
}
