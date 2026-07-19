import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/data/PensumData.kt", "r") as f:
    content = f.read()

# 1. Update StaticPensumSubject class
old_class = """data class StaticPensumSubject(
    val semester: Int,
    val code: String,
    val name: String,
    val prereqs: List<String>
)"""

new_class = """data class StaticPensumSubject(
    val semester: Int,
    val code: String,
    val name: String,
    val prereqs: List<String>,
    val category: String = "General"
)"""
content = content.replace(old_class, new_class)

# 2. Add some logic to categorize subjects if we want, or just leave the default as "General" for everything and then customize industrialEngineering.
# Actually, the user says "categorizar manualmente cada materia del pensum ya cargado". I should replace the industrial engineering list.

def map_category(name):
    name = name.upper()
    if any(x in name for x in ["MATEMÁTICA", "GEOMETRÍA", "CÁLCULO"]): return "Matemáticas"
    if any(x in name for x in ["FÍSICA", "MECANICA"]): return "Física"
    if any(x in name for x in ["QUÍMICA"]): return "Química"
    if any(x in name for x in ["INGLES", "INGLÉS", "COMUNICACIÓN", "REDACCION", "REDACCIÓN"]): return "Idiomas y Comunicación"
    if any(x in name for x in ["PROGRAMACIÓN", "COMPUTACION", "COMPUTACIÓN", "SISTEMAS"]): return "Computación"
    if any(x in name for x in ["FILOSOFÍA", "HISTORIA", "CULTURA", "SOCIOLOGÍA", "MEDIO AMBIENTE"]): return "Humanidades"
    if any(x in name for x in ["ECONOMÍA", "MERCADEO", "ECONOMICA", "MACROECONOMÍA", "MICROECONOMÍA", "MERCADOS"]): return "Economía y Negocios"
    if any(x in name for x in ["PRODUCCIÓN", "CALIDAD", "OPERACIONES", "ERGONOMÍA", "MANTENIMIENTO", "FIABILIDAD", "PROCESOS", "ADMINISTRACIÓN", "GERENCIA", "TALENTO"]): return "Ingeniería Industrial"
    if any(x in name for x in ["DIBUJO"]): return "Diseño"
    if any(x in name for x in ["ESTADÍSTICA"]): return "Estadística"
    if any(x in name for x in ["PROYECTO", "CULMINACION"]): return "Proyectos"
    return "Ingeniería"

lines = content.split('\n')
new_lines = []
for line in lines:
    if "StaticPensumSubject(" in line and "listOf(" in line and "category" not in line:
        # extract name to map category
        match = re.search(r'StaticPensumSubject\(\d+,\s*"[^"]+",\s*"([^"]+)",', line)
        if match:
            cat = map_category(match.group(1))
            line = line.replace("listOf())", f'listOf(), "{cat}")')
            line = line.replace('listOf("', f'listOf("') # wait
            # We need a robust replacement
            line = re.sub(r'(listOf\([^)]*\))\)', r'\1, "' + cat + '")', line)
    new_lines.append(line)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/data/PensumData.kt", "w") as f:
    f.write("\n".join(new_lines))

