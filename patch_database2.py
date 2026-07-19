import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/data/Database.kt", "r") as f:
    content = f.read()

# 1. Update version
content = re.sub(r'version = \d+', 'version = 13', content)

# 2. Update entities list
content = content.replace("Badge::class,", "Badge::class,\n        Professor::class,")

# 3. Replace AcademicRecord
old_academic = """@Entity(
    tableName = "academic_records",
    foreignKeys = [ForeignKey(entity = PensumSubject::class, parentColumns = ["id"], childColumns = ["pensumSubjectId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("pensumSubjectId")]
)
data class AcademicRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pensumSubjectId: Int,
    val grade: Double,
    val status: AssessmentStatus,
    val year: String,
    val academicGroup: String
)"""

new_academic = """@Entity(tableName = "professors")
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
    @ColumnInfo(name = "professorId") val professorId: Int? = null,
    @ColumnInfo(name = "rating") val rating: Int? = null
)"""
content = content.replace(old_academic, new_academic)

# 4. Add MIGRATION_12_13
old_migration = "        val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {"
new_migration = """        val MIGRATION_12_13 = object : androidx.room.migration.Migration(12, 13) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `professors` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `avatarSeed` TEXT NOT NULL)")
                db.execSQL("ALTER TABLE `academic_records` ADD COLUMN `professorId` INTEGER")
                db.execSQL("ALTER TABLE `academic_records` ADD COLUMN `rating` INTEGER")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_academic_records_professorId` ON `academic_records` (`professorId`)")
            }
        }

        val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {"""
content = content.replace(old_migration, new_migration)

# 5. Add MIGRATION_12_13 to addMigrations
old_add_migrations = ".addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)"
new_add_migrations = ".addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_12_13)"
content = content.replace(old_add_migrations, new_add_migrations)

# 6. Add ProfessorDao
old_dao = "    abstract fun careerDao(): CareerDao"
new_dao = """    abstract fun careerDao(): CareerDao
    abstract fun professorDao(): ProfessorDao"""
content = content.replace(old_dao, new_dao)

dao_code = """
@Dao
interface ProfessorDao {
    @Query("SELECT * FROM professors")
    fun getAllProfessors(): Flow<List<Professor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfessor(professor: Professor): Long

    @Query("SELECT * FROM professors WHERE id = :id")
    suspend fun getProfessorById(id: Int): Professor?
}
"""
content = content + dao_code

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/data/Database.kt", "w") as f:
    f.write(content)
