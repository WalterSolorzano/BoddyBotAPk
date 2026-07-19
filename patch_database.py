import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/data/Database.kt", "r") as f:
    content = f.read()

# 1. Update version
content = re.sub(r'version = \d+', 'version = 13', content)

# 2. Add Professor entity and update AcademicRecord
old_academic_record = """data class AcademicRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pensumSubjectId: Int,
    val grade: Double,
    val status: AssessmentStatus,
    val year: String,
    val academicGroup: String
)"""

new_academic_record = """@Entity(tableName = "professors")
data class Professor(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val avatarSeed: String
)

@Entity(
    tableName = "academic_records",
    foreignKeys = [
        ForeignKey(entity = PensumSubject::class, parentColumns = ["id"], childColumns = ["pensumSubjectId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Professor::class, parentColumns = ["id"], childColumns = ["professorId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("pensumSubjectId"), Index("professorId")]
)
data class AcademicRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pensumSubjectId: Int,
    val grade: Double,
    val status: AssessmentStatus,
    val year: String,
    val academicGroup: String,
    val professorId: Int? = null,
    val rating: Int? = null
)"""

# In Database.kt, AcademicRecord already has the @Entity annotation. Let's find it carefully.
