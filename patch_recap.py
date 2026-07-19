import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/data/Database.kt", "r") as f:
    content = f.read()

recap_entity = """
@Entity(tableName = "season_recaps")
data class SeasonRecap(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startDate: Long,
    val endDate: Long,
    val attendancePercentage: Double,
    val focusHoursTotal: Double,
    val focusSessionsCompleted: Int,
    val focusSessionsInterrupted: Int,
    val bestMoodDaysCount: Int,
    val worriedMoodDaysCount: Int,
    val maxStreak: Int,
    val badgesUnlockedCount: Int,
    val bestSubjectName: String?,
    val worstSubjectName: String?,
    val highlightText: String,
    val subjectWithMostAbsences: String?,
    val mostProductiveTimeOfDay: String?,
    val mostFocusedSubject: String?,
    val bestDayOfWeek: String?,
    val totalTripDistance: Double,
    val completedTasksCount: Int,
    val averageStreak: Double
)

@Dao
interface SeasonRecapDao {
    @Query("SELECT * FROM season_recaps ORDER BY endDate DESC")
    fun getAllRecaps(): kotlinx.coroutines.flow.Flow<List<SeasonRecap>>

    @Insert
    suspend fun insertRecap(recap: SeasonRecap): Long
}
"""

content = content.replace("data class Task", recap_entity + "\n@Entity(\n    tableName = \"tasks\",\n    foreignKeys = [\n        ForeignKey(\n            entity = Subject::class,\n            parentColumns = [\"id\"],\n            childColumns = [\"subjectId\"],\n            onDelete = ForeignKey.CASCADE\n        )\n    ],\n    indices = [Index(\"subjectId\")]\n)\ndata class Task")
content = content.replace("abstract fun taskDao(): TaskDao", "abstract fun taskDao(): TaskDao\n    abstract fun seasonRecapDao(): SeasonRecapDao")
content = content.replace("Task::class", "Task::class,\n        SeasonRecap::class")
content = content.replace("version = 14", "version = 15")

migration_14_15 = """        val MIGRATION_14_15 = object : androidx.room.migration.Migration(14, 15) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `season_recaps` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `startDate` INTEGER NOT NULL, `endDate` INTEGER NOT NULL, `attendancePercentage` REAL NOT NULL, `focusHoursTotal` REAL NOT NULL, `focusSessionsCompleted` INTEGER NOT NULL, `focusSessionsInterrupted` INTEGER NOT NULL, `bestMoodDaysCount` INTEGER NOT NULL, `worriedMoodDaysCount` INTEGER NOT NULL, `maxStreak` INTEGER NOT NULL, `badgesUnlockedCount` INTEGER NOT NULL, `bestSubjectName` TEXT, `worstSubjectName` TEXT, `highlightText` TEXT NOT NULL, `subjectWithMostAbsences` TEXT, `mostProductiveTimeOfDay` TEXT, `mostFocusedSubject` TEXT, `bestDayOfWeek` TEXT, `totalTripDistance` REAL NOT NULL, `completedTasksCount` INTEGER NOT NULL, `averageStreak` REAL NOT NULL)")
            }
        }

        fun getDatabase"""

content = content.replace("fun getDatabase", migration_14_15)
content = content.replace("MIGRATION_13_14)", "MIGRATION_13_14, MIGRATION_14_15)")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/data/Database.kt", "w") as f:
    f.write(content)

