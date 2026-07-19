import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/data/Database.kt", "r") as f:
    content = f.read()

old_insert = "suspend fun insertAcademicRecord(record: AcademicRecord): Long = db.academicRecordDao().insertAcademicRecord(record)"
new_insert = """suspend fun insertAcademicRecord(record: AcademicRecord): Long = db.academicRecordDao().insertAcademicRecord(record)
    suspend fun insertProfessor(professor: Professor): Long = db.professorDao().insertProfessor(professor)
    fun getAllProfessors(): kotlinx.coroutines.flow.Flow<List<Professor>> = db.professorDao().getAllProfessors()"""
content = content.replace(old_insert, new_insert)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/data/Database.kt", "w") as f:
    f.write(content)
