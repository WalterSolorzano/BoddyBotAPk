package com.aistudio.unibuddy.qywvsp.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

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
    val sessionsJson: String = "[]", // e.g. [{"day":"Lu","time":"10:00 AM","room":"Aula 1"}]
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: KeyValueSetting)
}

// Room Database
@Database(
    entities = [
        Subject::class,
        Absence::class,
        Assessment::class,
        TripRecord::class,
        KeyValueSetting::class,
        AttendanceLog::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun absenceDao(): AbsenceDao
    abstract fun assessmentDao(): AssessmentDao
    abstract fun tripRecordDao(): TripRecordDao
    abstract fun settingDao(): SettingDao
    abstract fun attendanceLogDao(): AttendanceLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

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

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "unibuddy_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
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
    suspend fun saveSetting(key: String, value: String) = db.settingDao().insertSetting(KeyValueSetting(key, value))
}
