package com.aistudio.unibuddy.qywvsp.data

data class PensumSubject(
    val semester: Int,
    val code: String,
    val name: String,
    val prereqs: List<String>
)

object CurriculumData {
    val industrialEngineering = listOf(
        PensumSubject(1, "6558", "GEOMETRÍA DESCRIPTIVA", listOf()),
        PensumSubject(1, "93ING101", "INGLES I", listOf()),
        PensumSubject(1, "CIND010", "INTRODUCCIÓN A LA INGENIERÍA INDUSTRIAL", listOf()),
        PensumSubject(1, "CMAT181", "MATEMÁTICA I", listOf()),
        PensumSubject(1, "COM-5528", "COMPUTACION BASICA", listOf()),
        PensumSubject(1, "CUL-5566", "CULTURA DE PAZ Y DERECHOS HUMANOS", listOf()),
        PensumSubject(1, "ESP-4942", "REDACCION TECNICA", listOf()),

        PensumSubject(2, "93ING102", "INGLES II", listOf("93ING101")),
        PensumSubject(2, "CFIL010", "FILOSOFÍA", listOf()),
        PensumSubject(2, "CFIS021", "FÍSICA I", listOf("CMAT181")),
        PensumSubject(2, "CHIS450", "HISTORIA E IDENTIDAD NACIONAL", listOf()),
        PensumSubject(2, "CMAT182", "MATEMÁTICA II", listOf("CMAT181")),
        PensumSubject(2, "DIB-6496", "DIBUJO TÉCNICO COMPUTARIZADO", listOf("6558")),

        PensumSubject(3, "99IND016", "MECANICA GENERAL", listOf("CFIS021")),
        PensumSubject(3, "CFIS022", "FÍSICA II", listOf("CFIS021")),
        PensumSubject(3, "CMAT183", "MATEMÁTICA III", listOf("CMAT182")),
        PensumSubject(3, "CPRG011", "PROGRAMACIÓN I", listOf("COM-5528")),
        PensumSubject(3, "CQUI009", "QUÍMICA GENERAL", listOf()),
        PensumSubject(3, "DIB-6498", "DIBUJO TÉCNICO II", listOf("DIB-6496")),

        PensumSubject(4, "6661", "METALURGIA Y TECNOLOGÍA MECÁNICA", listOf("CQUI009")),
        PensumSubject(4, "99COM202", "PROGRAMACION II", listOf("CPRG011")),
        PensumSubject(4, "99FIS206", "FISICA III", listOf("CFIS022", "CMAT183")),
        PensumSubject(4, "99MAT104", "MATEMATICA IV", listOf("CMAT183")),
        PensumSubject(4, "99MCE301", "ESTADISTICA I AI", listOf()),
        PensumSubject(4, "CECO009", "ECONOMÍA", listOf()),

        PensumSubject(5, "6527", "ERGONOMÍA, SEGURIDAD E HIGIENE INDUSTRIAL", listOf("99FIS206")),
        PensumSubject(5, "6635", "MÁQUINAS, MECANISMOS Y SU MANTENIMIENTO", listOf("99IND016")),
        PensumSubject(5, "99IND039", "ESTUDIO DEL TRABAJO I", listOf("99MCE301")),
        PensumSubject(5, "CEST012", "ESTADISTICA II", listOf()),
        PensumSubject(5, "CMAT090", "MÉTODOS NUMÉRICOS", listOf("CPRG011", "99MAT104")),
        PensumSubject(5, "MNM09", "METODOLOGÍA DE LA INVESTIGACIÓN", listOf("ESP-4942")),

        PensumSubject(6, "13EDT022", "ESTUDIO DEL TRABAJO II", listOf("99IND039")),
        PensumSubject(6, "99IND306", "ELECTROTECNIA", listOf("CFIS022")),
        PensumSubject(6, "99IND310", "INVESTIGACION DE OPERACIONES I", listOf("CPRG011")),
        PensumSubject(6, "CCON090", "CONTABILIDAD BASICA Y DE COSTOS", listOf()),
        PensumSubject(6, "CPRO080", "PROCESOS DE MANUFACTURA", listOf("6527")),
        PensumSubject(6, "CTER010", "TERMODINÁMICA", listOf("99FIS206")),

        PensumSubject(7, "99ADM506", "ADMINISTRACIÓN DE RECURSOS HUMANOS", listOf("13EDT022")),
        PensumSubject(7, "99IND302", "MERCADOTECNIA", listOf("CEST012")),
        PensumSubject(7, "99IND410", "INVESTIGACION DE OPERACIONES II", listOf("99IND310")),
        PensumSubject(7, "99IND411", "DISEÑO DE SISTEMAS PRODUCTIVOS", listOf("13EDT022")),
        PensumSubject(7, "99MCE201", "CONTABILIDAD GERENCIAL", listOf("CCON090")),
        PensumSubject(7, "CSIM010", "SIMULACIÓN", listOf("99IND310")),
        PensumSubject(7, "CSOC010", "SOCIOLOGÍA", listOf()),

        PensumSubject(8, "13PLF091", "PLANIFICACIÓN Y CONTROL DE LA PRODUCCIÓN I", listOf("99IND411")),
        PensumSubject(8, "99EST508", "CONTROL ESTADÍSTICO DE LA CALIDAD", listOf("CEST012")),
        PensumSubject(8, "CECO140", "INGENIERÍA ECONÓMICA", listOf("99MCE201")),
        PensumSubject(8, "CELE790", "INGENIERÍA DE SISTEMAS", listOf("99IND410")),
        PensumSubject(8, "OPT-6683", "OPTATIVA I: MICROECONOMÍA", listOf()),
        PensumSubject(8, "OPT-6684", "OPTATIVA I: TÉCNICAS AVANZADAS DE MERCADEO", listOf()),
        PensumSubject(8, "TEC-6385", "TECNOLOGIA Y MEDIO AMBIENTE", listOf()),

        PensumSubject(9, "13PLF092", "PLANIFICACIÓN Y CONTROL DE LA PRODUCCIÓN II", listOf("13PLF091")),
        PensumSubject(9, "99CPF409", "FORMULACION Y EVALUACION DE PROYECTO", listOf("CECO140", "99IND411")),
        PensumSubject(9, "ADM-6442", "ADMINISTRACIÓN DE CALIDAD TOTAL", listOf("99EST508")),
        PensumSubject(9, "ADM-6692", "OPTATIVA III (ADMINISTRACIÓN DE PROYECTOS)", listOf()),
        PensumSubject(9, "CIND120", "ADMINISTRACIÓN DEL MANTENIMIENTO INDUSTRIAL", listOf("99ADM506")),
        PensumSubject(9, "OPT-6686", "OPTATIVA II (INGENIERÍA DE SERVICIOS)", listOf()),
        PensumSubject(9, "OPT-6687", "OPTATIVA II (MACROECONOMÍA)", listOf()),
        PensumSubject(9, "OPT-6693", "OPTATIVA III (INGENIERÍA DE FIABILIDAD)", listOf()),

        PensumSubject(10, "FRM-11082", "FORMAS DE CULMINACION DE ESTUDIOS", listOf())
    )
}
