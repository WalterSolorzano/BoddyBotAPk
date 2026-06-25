# Plan de Implementación: Refactorización de Pensum, Importador PDF y Estadísticas Avanzadas

## 1. Arquitectura de la Base de Datos (Room)

Para soportar múltiples carreras y un historial académico rico, necesitamos rediseñar la estructura de datos:

### Entidades Propuestas

**1. `Career` (Carrera)**
- `id` (PK)
- `universityName` (ej. "UNI")
- `campusName` (ej. "RUPAP")
- `careerName` (ej. "Ingeniería Industrial")
- `totalCredits`

**2. `PensumSubject` (Materia del Pensum)**
- `id` (PK)
- `careerId` (FK -> Career)
- `code` (ej. "971A02")
- `name` (ej. "GEOMETRIA DESCRIPTIVA")
- `semester` (ej. 1, 2, 3...)
- `credits`

**3. `AcademicRecord` (Historial Académico del Estudiante)**
- `id` (PK)
- `pensumSubjectId` (FK -> PensumSubject)
- `grade` (ej. 64.00)
- `status` (Enum: `NF_R` [Regular], `IC_R` [Recuperación 1], `IIC_R` [Recuperación 2], `NF_CV` [Verano])
- `year` (ej. 2024)
- `group` (ej. "1M1-IND")

### DAO (Data Access Object)
- Se añadirá `CareerDao` y `AcademicRecordDao`.
- Consultas clave:
  - `getAcademicHistoryByCareer(careerId)`
  - `getFailedSubjectsRate()`
  - `getAverageGradePerSemester()`

## 2. Librería Recomendada para PDF

Para la extracción de texto en Android con Jetpack Compose, se recomienda encarecidamente utilizar **iText7 (iText Core)** o **PdfBox-Android** de Tom Roush.

**PdfBox-Android** es preferible por ser más ligera y compatible con las restricciones de Android (no requiere de `java.awt`):
- `implementation("com.tomroush:pdfbox-android:2.0.27.0")`

Permite cargar el `Uri` del documento PDF seleccionado con el `ActivityResultContracts.GetContent()`, procesar las páginas y extraer el texto crudo en un `String` gigante.

## 3. Implementación del Servicio de Regex (`HistorialParser`)

El servicio `HistorialParser` consumirá el texto extraído del PDF.

```kotlin
class HistorialParser {

    data class ParsedSubject(
        val code: String,
        val name: String,
        val credits: Double,
        val grade: Double,
        val status: String,
        val group: String
    )

    fun parsePdfText(rawText: String) {
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
        
        // 3. Regex Maestro para Clases
        val classRegex = Regex("""^([A-Z0-9]{5,7})\s+(.+?)\s+(\d+\.\d{2})\s+(\d+\.\d{2})\s+->([A-Z_]+)\s+([A-Z0-9\-]+)\s+(\d+)$""")

        val parsedSubjects = mutableListOf<ParsedSubject>()

        for (line in lines) {
            val trimLine = line.trim()
            
            // Revisar si cambiamos de semestre
            semesterRegex.find(trimLine)?.let {
                currentSemester = it.groupValues[1]
                currentYear = it.groupValues[2]
                return@for // continue
            }
            veranoRegex.find(trimLine)?.let {
                currentSemester = "VERANO"
                currentYear = it.groupValues[1]
                return@for // continue
            }

            // Extraer la clase
            classRegex.find(trimLine)?.let { match ->
                parsedSubjects.add(
                    ParsedSubject(
                        code = match.groupValues[1],
                        name = match.groupValues[2].trim(),
                        credits = match.groupValues[3].toDouble(),
                        grade = match.groupValues[4].toDouble(),
                        status = match.groupValues[5],
                        group = match.groupValues[6]
                    )
                )
            }
        }
        
        // Aquí se inyectan los resultados a la Base de Datos Room...
    }
}
```

## 4. Siguientes Pasos (Estadísticas)

1. En `SettingsScreen`, añadir un botón "Importar PDF".
2. Abrir selector de archivos nativo y pasar el `Uri` a `HistorialParser`.
3. Llenar la base de datos `AcademicRecord`.
4. En `StatisticsScreen`, leer de Room:
   - Calcular Promedio Global: `SUM(grade) / COUNT(grade) WHERE grade > 0`.
   - Calcular Tasa de Supervivencia: `(COUNT WHERE status == 'NF_R') / TOTAL`.
   - Mostrar un Canvas (Gráfico de Barras) por Semestre.
