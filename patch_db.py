import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/data/Database.kt", "r") as f:
    content = f.read()

# Update version
content = content.replace("version = 13", "version = 14")

# Update AttendanceLog entity
old_attendance_log = """@Entity(
    tableName = "attendance_logs",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId")]
)"""
new_attendance_log = """@Entity(
    tableName = "attendance_logs",
    indices = [Index("subjectId")]
)"""
content = content.replace(old_attendance_log, new_attendance_log)

# Add MIGRATION_13_14
migration_code = """        val MIGRATION_13_14 = object : androidx.room.migration.Migration(13, 14) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `attendance_logs_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `subjectId` INTEGER NOT NULL, `isPresent` INTEGER NOT NULL, `date` TEXT NOT NULL)")
                db.execSQL("INSERT INTO `attendance_logs_new` (`id`, `subjectId`, `isPresent`, `date`) SELECT `id`, `subjectId`, `isPresent`, `date` FROM `attendance_logs`")
                db.execSQL("DROP TABLE `attendance_logs`")
                db.execSQL("ALTER TABLE `attendance_logs_new` RENAME TO `attendance_logs`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_attendance_logs_subjectId` ON `attendance_logs` (`subjectId`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {"""
content = content.replace("fun getDatabase(context: Context): AppDatabase {", migration_code)

# Add to addMigrations
content = content.replace("MIGRATION_12_13)", "MIGRATION_12_13, MIGRATION_13_14)")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/data/Database.kt", "w") as f:
    f.write(content)
