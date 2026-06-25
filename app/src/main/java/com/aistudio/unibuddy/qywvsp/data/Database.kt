package com.aistudio.unibuddy.qywvsp.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONArray
import org.json.JSONObject
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ClassSessionDetails(val day: String, val time: String, val room: String)

fun String.parseSessions(): List<ClassSessionDetails> {
    val list = mutableListOf<ClassSessionDetails>()
    try {
        if (this.isBlank() || this == "[]") return list
        val root = JSONArray(this)
        for (i in 0 until root.length()) {
            val obj = root.getJSONObject(i)
            val timeCode = obj.optString("time", "M1")
            val readableTime = when (timeCode) {
                "M1" -> "08:00 - 10:00"
                "M2" -> "10:00 - 12:00"
                "M3" -> "12:00 - 14:00"
                "T1" -> "14:00 - 16:00"
                "T2" -> "16:00 - 18:00"
                "T3" -> "18:00 - 20:00"
                else -> timeCode
            }
            list.add(ClassSessionDetails(obj.getString("day"), readableTime, obj.optString("room", "Aula sin definir")))
        }
    } catch (e: Exception) { e.printStackTrace() }
    return list
}

fun List<ClassSessionDetails>.toJsonString(): String {
    val arr = JSONArray()
    this.forEach { s ->
        val obj = JSONObject()
        obj.put("day", s.day)
        obj.put("time", s.time)
        obj.put("room", s.room)
        arr.put(obj)
    }
    return arr.toString()
}

// Entities
@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val schedule: String, // Keep "Lu, Mi" for quick filtering
    val sessions: List<ClassSessionDetails> = emptyList(),
    val requiredAttendancePercent: Int,
    val totalClasses: Int,
    val colorHex: String = "#FFB3E5FC"
)

@Entity(
    tableName = "absences",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId")]
)
data class Absence(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectId: Int,
    val date: String // e.g. "2026-10-12" or "12 Oct"
)

@Entity(
    tableName = "assessments",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId")]
)
data class Assessment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectId: Int,
    val name: String, // e.g., "Parcial 1"
    val grade: Double?, // Nullable if pending
    val percentage: Double, // e.g., 30.0 for 30%
    val examDate: String = "" // e.g. "Lu", "Ma", "Mi", "Ju", "Vi", "Sá"
)

enum class AssessmentStatus {
    NF_R, // Regular
    IC_R, // Recuperación 1
    IIC_R, // Recuperación 2
    NF_CV, // Curso de Verano
    UNKNOWN
}

@Entity(tableName = "careers")
data class Career(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val universityName: String,
    val campusName: String,
    val careerName: String,
    val totalCredits: Int = 0
)

@Entity(
    tableName = "pensum_subjects",
    foreignKeys = [ForeignKey(entity = Career::class, parentColumns = ["id"], childColumns = ["careerId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("careerId")]
)
data class PensumSubject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val careerId: Int,
    val code: String,
    val name: String,
    val semester: String,
    val credits: Double,
    val isNumbers: Boolean = false
)

@Entity(
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
)

data class AcademicRecordWithSubject(
    @Embedded val record: AcademicRecord,
    @ColumnInfo(name = "name") val subjectName: String,
    @ColumnInfo(name = "semester") val semester: String,
    @ColumnInfo(name = "credits") val credits: Double,
    @ColumnInfo(name = "isNumbers") val isNumbers: Boolean
)

@Entity(tableName = "trip_records")
data class TripRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val durationMinutes: Int,
    val wasRaining: Boolean
)

@Entity(tableName = "settings")
data class KeyValueSetting(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "badges")
data class Badge(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val iconId: String,
    val category: String, // "Attendance", "Grades", "Focus"
    val isUnlocked: Boolean = false,
    val dateUnlocked: String = ""
)

@Entity(
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
)
data class AttendanceLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectId: Int,
    val date: String, // e.g., "12 Oct"
    val isPresent: Boolean // true = Presente, false = Ausente/Falta
)

// DAOs
@Dao
interface BadgeDao {
    @Query("SELECT * FROM badges ORDER BY category ASC")
    fun getAllBadges(): Flow<List<Badge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badge: Badge)

    @Update
    suspend fun updateBadge(badge: Badge)

    @Query("SELECT * FROM badges WHERE name = :name LIMIT 1")
    suspend fun getBadgeByName(name: String): Badge?
}

@Dao
interface AttendanceLogDao {
    @Query("SELECT * FROM attendance_logs ORDER BY id DESC")
    fun getAllLogs(): Flow<List<AttendanceLog>>

    @Query("SELECT * FROM attendance_logs WHERE subjectId = :subjectId ORDER BY id DESC")
    fun getLogsForSubject(subjectId: Int): Flow<List<AttendanceLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AttendanceLog): Long

    @Query("DELETE FROM attendance_logs WHERE id = :id")
    suspend fun deleteLogById(id: Int)
}
@Dao
interface CareerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCareer(career: Career): Long

    @Query("SELECT * FROM careers")
    fun getAllCareers(): Flow<List<Career>>
}

@Dao
interface PensumSubjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPensumSubject(subject: PensumSubject): Long
    
    @Query("SELECT * FROM pensum_subjects WHERE careerId = :careerId")
    fun getSubjectsForCareer(careerId: Int): Flow<List<PensumSubject>>
}

@Dao
interface AcademicRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAcademicRecord(record: AcademicRecord): Long

    @Query("SELECT * FROM academic_records")
    fun getAllRecords(): Flow<List<AcademicRecord>>

    @Query("""
        SELECT ar.*, ps.name as name, ps.semester as semester, ps.credits as credits, ps.isNumbers as isNumbers 
        FROM academic_records ar 
        INNER JOIN pensum_subjects ps ON ar.pensumSubjectId = ps.id
    """)
    fun getFullAcademicHistory(): Flow<List<AcademicRecordWithSubject>>
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    suspend fun getSubjectById(id: Int): Subject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject): Long

    @Update
    suspend fun updateSubject(subject: Subject)

    @Delete
    suspend fun deleteSubject(subject: Subject)
}

@Dao
interface AbsenceDao {
    @Query("SELECT * FROM absences ORDER BY id DESC")
    fun getAllAbsences(): Flow<List<Absence>>

    @Query("SELECT * FROM absences WHERE subjectId = :subjectId ORDER BY id DESC")
    fun getAbsencesForSubject(subjectId: Int): Flow<List<Absence>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAbsence(absence: Absence): Long

    @Query("DELETE FROM absences WHERE id = :id")
    suspend fun deleteAbsenceById(id: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM absences WHERE subjectId = :subjectId AND date = :date LIMIT 1)")
    suspend fun hasAbsenceOnDate(subjectId: Int, date: String): Boolean
}

@Dao
interface AssessmentDao {
    @Query("SELECT * FROM assessments ORDER BY id ASC")
    fun getAllAssessments(): Flow<List<Assessment>>

    @Query("SELECT * FROM assessments WHERE subjectId = :subjectId ORDER BY id ASC")
    fun getAssessmentsForSubject(subjectId: Int): Flow<List<Assessment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessment(assessment: Assessment): Long

    @Update
    suspend fun updateAssessment(assessment: Assessment)

    @Query("DELETE FROM assessments WHERE id = :id")
    suspend fun deleteAssessmentById(id: Int)
}

@Dao
interface TripRecordDao {
    @Query("SELECT * FROM trip_records ORDER BY id DESC")
    fun getAllTrips(): Flow<List<TripRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripRecord): Long

    @Query("SELECT AVG(durationMinutes) FROM trip_records")
    suspend fun getAverageDuration(): Double?
}

@Dao
interface SettingDao {
    @Query("SELECT * FROM settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): KeyValueSetting?

    @Query("SELECT * FROM settings")
    suspend fun getAllSettings(): List<KeyValueSetting>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: KeyValueSetting)
}

class Converters {
    private val moshi = Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
    private val type = Types.newParameterizedType(List::class.java, ClassSessionDetails::class.java)
    private val adapter = moshi.adapter<List<ClassSessionDetails>>(type)

    @TypeConverter
    fun fromString(value: String): List<ClassSessionDetails> {
        return adapter.fromJson(value) ?: emptyList()
    }

    @TypeConverter
    fun fromList(list: List<ClassSessionDetails>): String {
        return adapter.toJson(list)
    }

    @TypeConverter
    fun fromAssessmentStatus(status: AssessmentStatus): String = status.name

    @TypeConverter
    fun toAssessmentStatus(status: String): AssessmentStatus = try {
        AssessmentStatus.valueOf(status)
    } catch (e: Exception) {
        AssessmentStatus.UNKNOWN
    }
}

// Room Database
@Database(
    entities = [
        Subject::class,
        Absence::class,
        Assessment::class,
        TripRecord::class,
        KeyValueSetting::class,
        AttendanceLog::class,
        Badge::class,
        Career::class,
        PensumSubject::class,
        AcademicRecord::class
    ],
    version = 11,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun careerDao(): CareerDao
    abstract fun pensumSubjectDao(): PensumSubjectDao
    abstract fun academicRecordDao(): AcademicRecordDao
    abstract fun subjectDao(): SubjectDao
    abstract fun absenceDao(): AbsenceDao
    abstract fun assessmentDao(): AssessmentDao
    abstract fun tripRecordDao(): TripRecordDao
    abstract fun settingDao(): SettingDao
    abstract fun attendanceLogDao(): AttendanceLogDao
    abstract fun badgeDao(): BadgeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `careers` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `universityName` TEXT NOT NULL,
                        `campusName` TEXT NOT NULL,
                        `careerName` TEXT NOT NULL,
                        `totalCredits` INTEGER NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `pensum_subjects` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `careerId` INTEGER NOT NULL,
                        `code` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `semester` TEXT NOT NULL,
                        `credits` REAL NOT NULL,
                        `isNumbers` INTEGER NOT NULL,
                        FOREIGN KEY(`careerId`) REFERENCES `careers`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_pensum_subjects_careerId` ON `pensum_subjects` (`careerId`)")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `academic_records` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `pensumSubjectId` INTEGER NOT NULL,
                        `grade` REAL NOT NULL,
                        `status` TEXT NOT NULL,
                        `year` TEXT NOT NULL,
                        `academicGroup` TEXT NOT NULL,
                        FOREIGN KEY(`pensumSubjectId`) REFERENCES `pensum_subjects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_academic_records_pensumSubjectId` ON `academic_records` (`pensumSubjectId`)")
            }
        }

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Example empty migration, actual schema changes would go here
            }
        }
        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            }
        }
        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            }
        }
        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            }
        }
        val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            }
        }
        val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `badges` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `iconEmoji` TEXT NOT NULL, `category` TEXT NOT NULL, `isUnlocked` INTEGER NOT NULL, `dateUnlocked` TEXT NOT NULL)")
            }
        }
        val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            }
        }
        val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `badges_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `iconId` TEXT NOT NULL, `category` TEXT NOT NULL, `isUnlocked` INTEGER NOT NULL, `dateUnlocked` TEXT NOT NULL)")
                db.execSQL("INSERT INTO `badges_new` (`id`, `name`, `description`, `iconId`, `category`, `isUnlocked`, `dateUnlocked`) SELECT `id`, `name`, `description`, 'ic_badge_default', `category`, `isUnlocked`, `dateUnlocked` FROM `badges`")
                db.execSQL("DROP TABLE `badges`")
                db.execSQL("ALTER TABLE `badges_new` RENAME TO `badges`")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "unibuddy_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// Repository Pattern
class UniBuddyRepository(private val db: AppDatabase) {
    val subjects: Flow<List<Subject>> = db.subjectDao().getAllSubjects()
    val absences: Flow<List<Absence>> = db.absenceDao().getAllAbsences()
    val tripRecords: Flow<List<TripRecord>> = db.tripRecordDao().getAllTrips()
    val attendanceLogs: Flow<List<AttendanceLog>> = db.attendanceLogDao().getAllLogs()
    val assessments: Flow<List<Assessment>> = db.assessmentDao().getAllAssessments()
    val badges: Flow<List<Badge>> = db.badgeDao().getAllBadges()

    suspend fun getSubjectById(id: Int): Subject? = db.subjectDao().getSubjectById(id)
    suspend fun insertSubject(subject: Subject): Long = db.subjectDao().insertSubject(subject)
    suspend fun updateSubject(subject: Subject) = db.subjectDao().updateSubject(subject)
    suspend fun deleteSubject(subject: Subject) = db.subjectDao().deleteSubject(subject)

    fun getLogsForSubject(subjectId: Int): Flow<List<AttendanceLog>> = db.attendanceLogDao().getLogsForSubject(subjectId)
    suspend fun insertAttendanceLog(log: AttendanceLog): Long = db.attendanceLogDao().insertLog(log)
    suspend fun deleteAttendanceLogById(id: Int) = db.attendanceLogDao().deleteLogById(id)

    fun getAbsencesForSubject(subjectId: Int): Flow<List<Absence>> = db.absenceDao().getAbsencesForSubject(subjectId)
    suspend fun insertAbsence(absence: Absence) {
        if (!db.absenceDao().hasAbsenceOnDate(absence.subjectId, absence.date)) {
            db.absenceDao().insertAbsence(absence)
        }
    }
    suspend fun deleteAbsenceById(id: Int) = db.absenceDao().deleteAbsenceById(id)

    fun getAssessmentsForSubject(subjectId: Int): Flow<List<Assessment>> = db.assessmentDao().getAssessmentsForSubject(subjectId)
    suspend fun insertAssessment(assessment: Assessment): Long = db.assessmentDao().insertAssessment(assessment)
    suspend fun updateAssessment(assessment: Assessment) = db.assessmentDao().updateAssessment(assessment)
    suspend fun deleteAssessmentById(id: Int) = db.assessmentDao().deleteAssessmentById(id)

    suspend fun insertTrip(trip: TripRecord): Long = db.tripRecordDao().insertTrip(trip)
    suspend fun getAverageTripDuration(): Double? = db.tripRecordDao().getAverageDuration()

    suspend fun getSetting(key: String): String? = db.settingDao().getSetting(key)?.value
    suspend fun getAllSettings(): List<KeyValueSetting> = db.settingDao().getAllSettings()
    suspend fun saveSetting(key: String, value: String) = db.settingDao().insertSetting(KeyValueSetting(key, value))

    suspend fun getBadgeByName(name: String): Badge? = db.badgeDao().getBadgeByName(name)
    suspend fun insertBadge(badge: Badge) = db.badgeDao().insertBadge(badge)
    suspend fun updateBadge(badge: Badge) = db.badgeDao().updateBadge(badge)

    suspend fun getOrCreateCareer(universityName: String, campusName: String, careerName: String): Int {
        val existing = db.careerDao().getAllCareers().firstOrNull()?.find {
            it.universityName == universityName && it.campusName == campusName && it.careerName == careerName
        }
        if (existing != null) return existing.id
        return db.careerDao().insertCareer(Career(universityName = universityName, campusName = campusName, careerName = careerName)).toInt()
    }

    suspend fun insertPensumSubject(subject: PensumSubject): Long = db.pensumSubjectDao().insertPensumSubject(subject)
    suspend fun insertAcademicRecord(record: AcademicRecord): Long = db.academicRecordDao().insertAcademicRecord(record)
    
    val fullAcademicHistory = db.academicRecordDao().getFullAcademicHistory()
}
