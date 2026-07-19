package com.aistudio.unibuddy.qywvsp.data

data class StaticPensumSubject(
    val semester: Int,
    val code: String,
    val name: String,
    val prereqs: List<String>,
    val category: String = "General"
)

object CurriculumData {
    val industrialEngineering = listOf(
        StaticPensumSubject(1, "6558", "GEOMETRÍA DESCRIPTIVA", listOf(), "Matemáticas"),
        StaticPensumSubject(1, "93ING101", "INGLES I", listOf(), "Idiomas y Comunicación"),
        StaticPensumSubject(1, "CIND010", "INTRODUCCIÓN A LA INGENIERÍA INDUSTRIAL", listOf(), "Ingeniería"),
        StaticPensumSubject(1, "CMAT181", "MATEMÁTICA I", listOf(), "Matemáticas"),
        StaticPensumSubject(1, "COM-5528", "COMPUTACION BASICA", listOf(), "Computación"),
        StaticPensumSubject(1, "CUL-5566", "CULTURA DE PAZ Y DERECHOS HUMANOS", listOf(), "Humanidades"),
        StaticPensumSubject(1, "ESP-4942", "REDACCION TECNICA", listOf(), "Idiomas y Comunicación"),

        StaticPensumSubject(2, "93ING102", "INGLES II", listOf("93ING101"), "Idiomas y Comunicación"),
        StaticPensumSubject(2, "CFIL010", "FILOSOFÍA", listOf(), "Humanidades"),
        StaticPensumSubject(2, "CFIS021", "FÍSICA I", listOf("CMAT181"), "Física"),
        StaticPensumSubject(2, "CHIS450", "HISTORIA E IDENTIDAD NACIONAL", listOf(), "Humanidades"),
        StaticPensumSubject(2, "CMAT182", "MATEMÁTICA II", listOf("CMAT181"), "Matemáticas"),
        StaticPensumSubject(2, "DIB-6496", "DIBUJO TÉCNICO COMPUTARIZADO", listOf("6558"), "Diseño"),

        StaticPensumSubject(3, "99IND016", "MECANICA GENERAL", listOf("CFIS021"), "Física"),
        StaticPensumSubject(3, "CFIS022", "FÍSICA II", listOf("CFIS021"), "Física"),
        StaticPensumSubject(3, "CMAT183", "MATEMÁTICA III", listOf("CMAT182"), "Matemáticas"),
        StaticPensumSubject(3, "CPRG011", "PROGRAMACIÓN I", listOf("COM-5528"), "Computación"),
        StaticPensumSubject(3, "CQUI009", "QUÍMICA GENERAL", listOf(), "Química"),
        StaticPensumSubject(3, "DIB-6498", "DIBUJO TÉCNICO II", listOf("DIB-6496"), "Diseño"),

        StaticPensumSubject(4, "6661", "METALURGIA Y TECNOLOGÍA MECÁNICA", listOf("CQUI009"), "Ingeniería"),
        StaticPensumSubject(4, "99COM202", "PROGRAMACION II", listOf("CPRG011"), "Ingeniería"),
        StaticPensumSubject(4, "99FIS206", "FISICA III", listOf("CFIS022", "CMAT183"), "Ingeniería"),
        StaticPensumSubject(4, "99MAT104", "MATEMATICA IV", listOf("CMAT183"), "Ingeniería"),
        StaticPensumSubject(4, "99MCE301", "ESTADISTICA I AI", listOf(), "Ingeniería"),
        StaticPensumSubject(4, "CECO009", "ECONOMÍA", listOf(), "Economía y Negocios"),

        StaticPensumSubject(5, "6527", "ERGONOMÍA, SEGURIDAD E HIGIENE INDUSTRIAL", listOf("99FIS206"), "Ingeniería Industrial"),
        StaticPensumSubject(5, "6635", "MÁQUINAS, MECANISMOS Y SU MANTENIMIENTO", listOf("99IND016"), "Ingeniería Industrial"),
        StaticPensumSubject(5, "99IND039", "ESTUDIO DEL TRABAJO I", listOf("99MCE301"), "Ingeniería"),
        StaticPensumSubject(5, "CEST012", "ESTADISTICA II", listOf(), "Ingeniería"),
        StaticPensumSubject(5, "CMAT090", "MÉTODOS NUMÉRICOS", listOf("CPRG011", "99MAT104"), "Ingeniería"),
        StaticPensumSubject(5, "MNM09", "METODOLOGÍA DE LA INVESTIGACIÓN", listOf("ESP-4942"), "Ingeniería"),

        StaticPensumSubject(6, "13EDT022", "ESTUDIO DEL TRABAJO II", listOf("99IND039"), "Ingeniería"),
        StaticPensumSubject(6, "99IND306", "ELECTROTECNIA", listOf("CFIS022"), "Ingeniería"),
        StaticPensumSubject(6, "99IND310", "INVESTIGACION DE OPERACIONES I", listOf("CPRG011"), "Ingeniería Industrial"),
        StaticPensumSubject(6, "CCON090", "CONTABILIDAD BASICA Y DE COSTOS", listOf(), "Ingeniería"),
        StaticPensumSubject(6, "CPRO080", "PROCESOS DE MANUFACTURA", listOf("6527"), "Ingeniería Industrial"),
        StaticPensumSubject(6, "CTER010", "TERMODINÁMICA", listOf("99FIS206"), "Ingeniería"),

        StaticPensumSubject(7, "99ADM506", "ADMINISTRACIÓN DE RECURSOS HUMANOS", listOf("13EDT022"), "Ingeniería Industrial"),
        StaticPensumSubject(7, "99IND302", "MERCADOTECNIA", listOf("CEST012"), "Ingeniería"),
        StaticPensumSubject(7, "99IND410", "INVESTIGACION DE OPERACIONES II", listOf("99IND310"), "Ingeniería Industrial"),
        StaticPensumSubject(7, "99IND411", "DISEÑO DE SISTEMAS PRODUCTIVOS", listOf("13EDT022"), "Computación"),
        StaticPensumSubject(7, "99MCE201", "CONTABILIDAD GERENCIAL", listOf("CCON090"), "Ingeniería Industrial"),
        StaticPensumSubject(7, "CSIM010", "SIMULACIÓN", listOf("99IND310"), "Ingeniería"),
        StaticPensumSubject(7, "CSOC010", "SOCIOLOGÍA", listOf(), "Humanidades"),

        StaticPensumSubject(8, "13PLF091", "PLANIFICACIÓN Y CONTROL DE LA PRODUCCIÓN I", listOf("99IND411"), "Ingeniería Industrial"),
        StaticPensumSubject(8, "99EST508", "CONTROL ESTADÍSTICO DE LA CALIDAD", listOf("CEST012"), "Ingeniería Industrial"),
        StaticPensumSubject(8, "CECO140", "INGENIERÍA ECONÓMICA", listOf("99MCE201"), "Ingeniería"),
        StaticPensumSubject(8, "CELE790", "INGENIERÍA DE SISTEMAS", listOf("99IND410"), "Computación"),
        StaticPensumSubject(8, "OPT-6683", "OPTATIVA I: MICROECONOMÍA", listOf(), "Economía y Negocios"),
        StaticPensumSubject(8, "OPT-6684", "OPTATIVA I: TÉCNICAS AVANZADAS DE MERCADEO", listOf(), "Economía y Negocios"),
        StaticPensumSubject(8, "TEC-6385", "TECNOLOGIA Y MEDIO AMBIENTE", listOf(), "Humanidades"),

        StaticPensumSubject(9, "13PLF092", "PLANIFICACIÓN Y CONTROL DE LA PRODUCCIÓN II", listOf("13PLF091"), "Ingeniería Industrial"),
        StaticPensumSubject(9, "99CPF409", "FORMULACION Y EVALUACION DE PROYECTO", listOf("CECO140", "99IND411"), "Proyectos"),
        StaticPensumSubject(9, "ADM-6442", "ADMINISTRACIÓN DE CALIDAD TOTAL", listOf("99EST508"), "Ingeniería Industrial"),
        StaticPensumSubject(9, "ADM-6692", "OPTATIVA III (ADMINISTRACIÓN DE PROYECTOS)", listOf(), "Ingeniería Industrial"),
        StaticPensumSubject(9, "CIND120", "ADMINISTRACIÓN DEL MANTENIMIENTO INDUSTRIAL", listOf("99ADM506"), "Ingeniería Industrial"),
        StaticPensumSubject(9, "OPT-6686", "OPTATIVA II (INGENIERÍA DE SERVICIOS)", listOf(), "Ingeniería"),
        StaticPensumSubject(9, "OPT-6687", "OPTATIVA II (MACROECONOMÍA)", listOf(), "Economía y Negocios"),
        StaticPensumSubject(9, "OPT-6693", "OPTATIVA III (INGENIERÍA DE FIABILIDAD)", listOf(), "Ingeniería Industrial"),

        StaticPensumSubject(10, "FRM-11082", "FORMAS DE CULMINACION DE ESTUDIOS", listOf(), "Proyectos")
    )

    fun getSubjectsFor(university: String, career: String): List<StaticPensumSubject> {
        if (university == "UNI" && career == "Ing. Industrial") {
            return industrialEngineering
        }
        // Generate a customized curriculum for the selected university and career
        val list = mutableListOf<StaticPensumSubject>()
        val careerPrefix = when (career) {
            "Ing. de Sistemas" -> "SIS"
            "Ing. Civil" -> "CIV"
            "Ing. Industrial" -> "IND"
            "Ing. Química" -> "QMC"
            "Arquitectura" -> "ARQ"
            "Medicina" -> "MED"
            "Derecho" -> "DER"
            "Psicología" -> "PSI"
            "Administración" -> "ADM"
            "Diseño Gráfico" -> "DSG"
            "Comunicación" -> "COM"
            "Odontología" -> "ODO"
            "Relaciones Internacionales" -> "RRI"
            "Business Administration" -> "BUS"
            "Software Engineering" -> "SEN"
            "Management Info Systems" -> "MIS"
            "Cybersecurity" -> "CYB"
            "Medicina Veterinaria" -> "VET"
            "Contaduría" -> "CON"
            else -> "GEN"
        }
        
        val subjectNames = mapOf(
            "SIS" to listOf("Matemática I", "Programación I", "Introducción a Sistemas", "Física General", "Inglés Técnico", "Cálculo", "Estructuras de Datos", "Base de Datos I", "Análisis de Sistemas", "Redes de Computadoras", "Sistemas Operativos", "Ingeniería de Software", "Base de Datos II", "Desarrollo Web", "Seguridad Informática", "Administración de Proyectos", "Inteligencia Artificial", "Proyecto de Fin de Carrera"),
            "CIV" to listOf("Matemática I", "Dibujo Técnico", "Introducción a Ingeniería Civil", "Física General", "Geometría Descriptiva", "Cálculo", "Topografía I", "Mecánica de Sólidos", "Hidráulica", "Materiales de Construcción", "Análisis Estructural I", "Geotecnia", "Diseño de Concreto I", "Ingeniería Ambiental", "Planificación de Obras", "Diseño Sismorresistente", "Formulación de Proyectos Civil", "Proyecto de Graduación"),
            "IND" to listOf("Geometría Descriptiva", "Inglés I", "Introducción a Ingeniería Industrial", "Matemática I", "Computación Básica", "Física I", "Química General", "Estadística I", "Estudio del Trabajo I", "Procesos de Manufactura", "Investigación de Operaciones I", "Ergonomía y Seguridad", "Control de Calidad", "Planificación de Producción", "Ingeniería Económica", "Formulación de Proyectos", "Administración de Proyectos", "Proyecto Final"),
            "QMC" to listOf("Química General I", "Matemática I", "Física I", "Introducción a Ing. Química", "Química Orgánica I", "Cálculo I", "Química Analítica", "Termodinámica I", "Fisicoquímica", "Transferencia de Calor", "Operaciones Unitarias I", "Cinética Química", "Diseño de Reactores", "Control de Procesos", "Seguridad Industrial", "Diseño de Plantas", "Proyectos Químicos", "Trabajo de Grado"),
            "ARQ" to listOf("Diseño Arquitectónico I", "Geometría Descriptiva", "Historia de la Arquitectura", "Expresión Gráfica", "Diseño Arquitectónico II", "Teoría del Espacio", "Sistemas Estructurales I", "Materiales de Construcción", "Diseño Urbano", "Instalaciones en Edificaciones", "Arquitectura Sostenible", "Presupuestos y Costos", "Portafolio de Diseño", "Proyecto de Tesis"),
            "MED" to listOf("Anatomía Humana", "Biología Celular", "Histología", "Bioquímica Médica", "Fisiología I", "Embriología", "Microbiología", "Fisiopatología", "Farmacología", "Semiología Médica", "Medicina Interna I", "Cirugía General", "Pediatría", "Ginecología y Obstetricia", "Salud Pública", "Ética Médica", "Internado Rotatorio"),
            "DER" to listOf("Introducción al Derecho", "Derecho Romano", "Derecho Civil I", "Derecho Constitucional", "Derecho Penal I", "Derecho Civil II", "Derecho Penal II", "Derecho Procesal I", "Derecho Laboral", "Derecho Mercantil I", "Derecho Internacional", "Filosofía del Derecho", "Técnicas de Litigación", "Derecho Notarial", "Prácticas Forenses", "Tesis de Grado"),
            "PSI" to listOf("Introducción a la Psicología", "Neuroanatomía", "Psicología del Desarrollo I", "Teorías de la Personalidad", "Psicopatología I", "Métodos de Investigación", "Psicología Social", "Evaluación Psicológica", "Psicología Clínica", "Psicología Educativa", "Psicofarmacología", "Terapia Cognitivo Conductual", "Psicología Organizacional", "Ética Profesional", "Prácticas Clínicas"),
            "ADM" to listOf("Principios de Administración", "Contabilidad I", "Microeconomía", "Matemática Financiera", "Macroeconomía", "Contabilidad de Costos", "Administración Financiera", "Mercadotecnia I", "Comportamiento Organizacional", "Investigación de Mercados", "Gestión del Talento Humano", "Administración Estratégica", "Formulación de Proyectos", "Auditoría Administrativa", "Creación de Empresas"),
            "DSG" to listOf("Fundamentos del Diseño", "Historia del Arte", "Dibujo Artístico", "Fotografía Digital", "Tipografía", "Ilustración Vectorial", "Diseño de Identidad", "Edición de Imagen", "Diseño Editorial", "Diseño de Empaques", "Animación 2D", "Diseño Web", "Diseño de Campañas", "Portafolio Profesional"),
            "COM" to listOf("Introducción a la Comunicación", "Redacción Periodística", "Teorías de la Comunicación", "Fotoperiodismo", "Comunicación Audiovisual", "Radio y Locución", "Periodismo Digital", "Relaciones Públicas", "Producción de Televisión", "Comunicación Organizacional", "Opinión Pública", "Campañas de Comunicación", "Seminario de Tesis")
        )
        
        val names = subjectNames[careerPrefix] ?: listOf("Introducción a la Especialidad", "Matemática Aplicada", "Metodología de Estudio", "Inglés Técnico", "Computación", "Seminario de Formación", "Prácticas Profesionales", "Formulación de Proyectos", "Examen de Grado")
        
        var nameIdx = 0
        for (sem in 1..10) {
            val numSubjects = if (sem == 10) 1 else 4
            for (i in 1..numSubjects) {
                val subjectName = if (nameIdx < names.size) names[nameIdx] else "Materia Electiva $i"
                nameIdx++
                list.add(
                    StaticPensumSubject(
                        semester = sem,
                        code = "${careerPrefix}${sem}0${i}",
                        name = subjectName.uppercase(),
                        prereqs = emptyList()
                    )
                )
            }
        }
        return list
    }
}
