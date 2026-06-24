package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class UniBuddyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UniBuddyRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = UniBuddyRepository(db)
        
        viewModelScope.launch {
            // Load settings
            repository.getSetting("origin")?.let { _origin.value = it }
            repository.getSetting("destination")?.let { _destination.value = it }
            repository.getSetting("base_travel_time")?.let { _baseTravelTime.value = it.toIntOrNull() ?: 25 }
            repository.getSetting("username")?.let { _username.value = it }
            repository.getSetting("google_maps_api_key")?.let { _googleMapsApiKey.value = it }
            repository.getSetting("onboarding_completed")?.let { _isOnboardingCompleted.value = it.toBoolean() }
            repository.getSetting("arrival_margin_preference")?.let { _arrivalMarginPreference.value = it }
            repository.getSetting("weather_desc")?.let { _weatherDescription.value = it }
            repository.getSetting("is_raining")?.let { _isRaining.value = it.toBoolean() }
            repository.getSetting("semester_start_date")?.let { _semesterStartDate.value = it.toLongOrNull() }
            repository.getSetting("passed_subjects")?.let { _passedSubjects.value = it.split(",").filter { s -> s.isNotEmpty() }.toSet() }
            
            // Populate with defaults if empty
            if (repository.getSetting("onboarding_completed") == null) {
                prepopulateDatabase()
            }
            
            refreshWeather()
        }
    }

    private suspend fun prepopulateDatabase() {
        repository.saveSetting("onboarding_completed", "false")
        repository.saveSetting("username", "Estudiante")
        repository.saveSetting("origin", "Casa")
        repository.saveSetting("destination", "Facultad")
        repository.saveSetting("semester_state", "Vacaciones")
    }

    // Dynamic UI State
    val subjects: StateFlow<List<Subject>> = repository.subjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val absences: StateFlow<List<Absence>> = repository.absences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendanceLogs: StateFlow<List<AttendanceLog>> = repository.attendanceLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tripRecords: StateFlow<List<TripRecord>> = repository.tripRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assessments: StateFlow<List<Assessment>> = repository.assessments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settings flows
    private val _origin = MutableStateFlow("Casa")
    val origin: StateFlow<String> = _origin.asStateFlow()

    private val _destination = MutableStateFlow("Facultad de Ingeniería")
    val destination: StateFlow<String> = _destination.asStateFlow()

    private val _passedSubjects = MutableStateFlow<Set<String>>(emptySet())
    val passedSubjects: StateFlow<Set<String>> = _passedSubjects.asStateFlow()

    private val _baseTravelTime = MutableStateFlow(25) // in minutes
    val baseTravelTime: StateFlow<Int> = _baseTravelTime.asStateFlow()

    private val _isRaining = MutableStateFlow(false)
    val isRaining: StateFlow<Boolean> = _isRaining.asStateFlow()

    private val _semesterStartDate = MutableStateFlow<Long?>(null)
    val semesterStartDate: StateFlow<Long?> = _semesterStartDate.asStateFlow()

    // 1-14: Clases, 15-16: Exámenes, 17-18: Convocatorias, >18: Vacaciones
    val currentWeekOfSemester = _semesterStartDate.map { start ->
        if (start == null) return@map -1
        val diff = System.currentTimeMillis() - start
        val weeks = (diff / (1000L * 60 * 60 * 24 * 7)).toInt() + 1
        weeks
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1)
    
    val semesterState = currentWeekOfSemester.map { week ->
        when {
            week < 0 -> "Vacaciones"
            week in 1..14 -> "Clases"
            week in 15..16 -> "Exámenes"
            week in 17..18 -> "Convocatorias"
            else -> "Vacaciones"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Vacaciones")

    fun startNewSemester() {
        viewModelScope.launch {
            // Delete old subjects, assessments, logs
            val currentSubjects = repository.subjects.first()
            for (sub in currentSubjects) {
                repository.deleteSubject(sub)
            }
            // Start a new semester by resetting start date and asking for subjects (onboarding)
            val now = System.currentTimeMillis()
            _semesterStartDate.value = now
            repository.saveSetting("semester_start_date", now.toString())
            
            _isOnboardingCompleted.value = false
            repository.saveSetting("onboarding_completed", "false")
        }
    }
    
    fun endSemester() {
        viewModelScope.launch {
            val currentSubjects = repository.subjects.first()
            val allAssessments = repository.assessments.first()
            val passed = _passedSubjects.value.toMutableSet()
            
            for (sub in currentSubjects) {
                val subAssessments = allAssessments.filter { it.subjectId == sub.id }
                val currentPercentage = subAssessments.sumOf { it.percentage }
                val currentWeighted = subAssessments.sumOf { (it.grade ?: 0.0) * (it.percentage / 100.0) }
                
                // If they passed (>=6.0) or if no exams were registered (assume passed)
                if (currentPercentage == 0.0 || currentWeighted >= 6.0) {
                    val curriculumMatch = com.example.data.CurriculumData.industrialEngineering.find { 
                        it.name.equals(sub.name, ignoreCase = true) 
                    }
                    if (curriculumMatch != null) {
                        passed.add(curriculumMatch.code)
                    } else {
                        passed.add(sub.name) // fallback
                    }
                }
            }
            
            _passedSubjects.value = passed
            repository.saveSetting("passed_subjects", passed.joinToString(","))
            
            _semesterStartDate.value = null
            repository.saveSetting("semester_start_date", "")
        }
    }

    fun updateSemesterStartByWeek(week: Int) {
        viewModelScope.launch {
            val millisInWeek = 7L * 24L * 60L * 60L * 1000L
            // week 1 means 0 weeks have passed
            val offsetMillis = (week - 1).coerceAtLeast(0) * millisInWeek
            val newStartDate = System.currentTimeMillis() - offsetMillis
            _semesterStartDate.value = newStartDate
            repository.saveSetting("semester_start_date", newStartDate.toString())
        }
    }

    private val _arrivalMarginPreference = MutableStateFlow("normal") // "normal" or "temprano" (+10 min)
    val arrivalMarginPreference: StateFlow<String> = _arrivalMarginPreference.asStateFlow()

    private val _weatherDescription = MutableStateFlow("Clima Despejado")
    val weatherDescription: StateFlow<String> = _weatherDescription.asStateFlow()

    private val _lastWeatherUpdateMsg = MutableStateFlow("Sin actualizar")
    val lastWeatherUpdateMsg: StateFlow<String> = _lastWeatherUpdateMsg.asStateFlow()

    private val _isFetchingWeather = MutableStateFlow(false)
    val isFetchingWeather: StateFlow<Boolean> = _isFetchingWeather.asStateFlow()

    private val _weeklyStreak = MutableStateFlow(12)
    val weeklyStreak: StateFlow<Int> = _weeklyStreak.asStateFlow()

    private val _username = MutableStateFlow("Estudiante")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _googleMapsApiKey = MutableStateFlow("")
    val googleMapsApiKey: StateFlow<String> = _googleMapsApiKey.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    // Methods
    fun updateOnboardingStatus(completed: Boolean) {
        viewModelScope.launch {
            _isOnboardingCompleted.value = completed
            repository.saveSetting("onboarding_completed", completed.toString())
        }
    }

    fun loadDemoData() {
        viewModelScope.launch {
            // Delete old data
            val currentSubjects = repository.subjects.first()
            for (sub in currentSubjects) {
                repository.deleteSubject(sub)
            }
            
            // Insert dummy subjects
            val s1 = Subject(name = "Álgebra Lineal", schedule = "Lu, Mi", sessionsJson = "[{\"day\":\"Lu\",\"block\":\"M1\",\"room\":\"Aula 1\"},{\"day\":\"Mi\",\"block\":\"M1\",\"room\":\"Aula 1\"}]", requiredAttendancePercent = 70, totalClasses = 28, colorHex = "#FFCDD2")
            val s2 = Subject(name = "Física General", schedule = "Ma, Ju", sessionsJson = "[{\"day\":\"Ma\",\"block\":\"M3\",\"room\":\"Lab 2\"},{\"day\":\"Ju\",\"block\":\"M3\",\"room\":\"Lab 2\"}]", requiredAttendancePercent = 80, totalClasses = 28, colorHex = "#E1BEE7")
            val s3 = Subject(name = "Programación I", schedule = "Vi", sessionsJson = "[{\"day\":\"Vi\",\"block\":\"M5\",\"room\":\"Lab Comp\"}]", requiredAttendancePercent = 75, totalClasses = 14, colorHex = "#BBDEFB")
            
            val id1 = repository.insertSubject(s1).toInt()
            val id2 = repository.insertSubject(s2).toInt()
            val id3 = repository.insertSubject(s3).toInt()
            
            // Assessments
            repository.insertAssessment(Assessment(subjectId = id1, name = "Parcial 1", grade = 85.0, percentage = 30.0))
            repository.insertAssessment(Assessment(subjectId = id1, name = "Parcial 2", grade = 90.0, percentage = 30.0))
            repository.insertAssessment(Assessment(subjectId = id2, name = "Lab 1", grade = 100.0, percentage = 20.0))
            repository.insertAssessment(Assessment(subjectId = id3, name = "Proyecto Final", grade = 95.0, percentage = 50.0))
            
            // Logs
            val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
            val date1 = sdf.format(Date(System.currentTimeMillis() - 86400000L * 2))
            val date2 = sdf.format(Date(System.currentTimeMillis() - 86400000L * 1))
            
            repository.insertAttendanceLog(AttendanceLog(subjectId = id1, date = date1, isPresent = true))
            repository.insertAttendanceLog(AttendanceLog(subjectId = id2, date = date1, isPresent = false))
            repository.insertAbsence(Absence(subjectId = id2, date = date1))
            repository.insertAttendanceLog(AttendanceLog(subjectId = id3, date = date2, isPresent = true))
            
            val now = System.currentTimeMillis()
            _semesterStartDate.value = now
            repository.saveSetting("semester_start_date", now.toString())
            _isOnboardingCompleted.value = true
            repository.saveSetting("onboarding_completed", "true")
        }
    }

    fun saveRoute(originVal: String, destVal: String) {
        viewModelScope.launch {
            _origin.value = originVal
            _destination.value = destVal
            repository.saveSetting("origin", originVal)
            repository.saveSetting("destination", destVal)
        }
    }

    fun saveUsername(name: String) {
        viewModelScope.launch {
            _username.value = name
            repository.saveSetting("username", name)
        }
    }

    fun saveBaseTravelTime(minutes: Int) {
        viewModelScope.launch {
            _baseTravelTime.value = minutes
            repository.saveSetting("base_travel_time", minutes.toString())
        }
    }

    fun togglePassedSubject(code: String) {
        viewModelScope.launch {
            val current = _passedSubjects.value.toMutableSet()
            if (current.contains(code)) {
                current.remove(code)
            } else {
                current.add(code)
            }
            _passedSubjects.value = current
            repository.saveSetting("passed_subjects", current.joinToString(","))
        }
    }

    fun setArrivalMarginPreference(pref: String) {
        viewModelScope.launch {
            _arrivalMarginPreference.value = pref
            repository.saveSetting("arrival_margin_preference", pref)
        }
    }

    fun refreshWeather() {
        viewModelScope.launch {
            _isFetchingWeather.value = true
            try {
                // Determine saved destination lat/lon
                val savedLat = repository.getSetting("destination_lat")?.toDoubleOrNull() ?: -34.6037
                val savedLon = repository.getSetting("destination_lon")?.toDoubleOrNull() ?: -58.3816
                
                val response = GeocodingServiceClient.fetchCurrentWeather(savedLat, savedLon)
                if (response != null && response.currentWeather != null) {
                    val code = response.currentWeather.weathercode
                    val isRain = code in 51..67 || code in 80..82 || code >= 95
                    _isRaining.value = isRain
                    
                    val desc = when (code) {
                        0 -> "Despejado"
                        1, 2, 3 -> "Parcialmente Nublado"
                        45, 48 -> "Neblina"
                        51, 53, 55 -> "Llovizna Leve"
                        56, 57 -> "Llovizna Helada"
                        61, 63, 65 -> "Lluvia Fuerte"
                        66, 67 -> "Lluvia Helada"
                        71, 73, 75 -> "Nieve"
                        80, 81, 82 -> "Chubascos / Lluvia Pasajera"
                        95, 96, 99 -> "Tormenta Electrica"
                        else -> "Templado"
                    }
                    _weatherDescription.value = desc
                    _lastWeatherUpdateMsg.value = "Último: ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())}"
                    
                    repository.saveSetting("is_raining", isRain.toString())
                    repository.saveSetting("weather_desc", desc)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isFetchingWeather.value = false
            }
        }
    }

    fun toggleRaining() {
        _isRaining.value = !_isRaining.value
    }

    fun addSubject(name: String, schedule: String, sessionsJson: String, requiredAttendancePercent: Int, totalClasses: Int, colorHex: String = "#FFB3E5FC") {
        viewModelScope.launch {
            val subject = Subject(
                name = name,
                schedule = schedule,
                sessionsJson = sessionsJson,
                requiredAttendancePercent = requiredAttendancePercent,
                totalClasses = totalClasses,
                colorHex = colorHex
            )
            val subjectId = repository.insertSubject(subject).toInt()
            
            // Auto-generate Grading System (0-100 total)
            // Unit 1 (50)
            repository.insertAssessment(Assessment(subjectId = subjectId, name = "U1: Examen", grade = null, percentage = 10.0))
            for (i in 1..5) {
                repository.insertAssessment(Assessment(subjectId = subjectId, name = "U1: Prueba $i", grade = null, percentage = 8.0))
            }
            // Unit 2 (50)
            repository.insertAssessment(Assessment(subjectId = subjectId, name = "U2: Examen", grade = null, percentage = 10.0))
            for (i in 1..5) {
                repository.insertAssessment(Assessment(subjectId = subjectId, name = "U2: Prueba $i", grade = null, percentage = 8.0))
            }
        }
    }

    fun updateSubject(subject: Subject) {
        viewModelScope.launch {
            repository.updateSubject(subject)
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
        }
    }

    fun registerAbsence(subjectId: Int, dateStr: String? = null) {
        viewModelScope.launch {
            val date = dateStr ?: SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())
            repository.insertAbsence(Absence(subjectId = subjectId, date = date))
            // Increment streak or update list
            _weeklyStreak.value = _weeklyStreak.value + 1
        }
    }

    fun deleteAbsence(absenceId: Int) {
        viewModelScope.launch {
            repository.deleteAbsenceById(absenceId)
            if (_weeklyStreak.value > 0) {
                _weeklyStreak.value = _weeklyStreak.value - 1
            }
        }
    }

    fun getAbsencesForSubject(subjectId: Int): Flow<List<Absence>> {
        return repository.getAbsencesForSubject(subjectId)
    }

    fun getAssessmentsForSubject(subjectId: Int): Flow<List<Assessment>> {
        return repository.getAssessmentsForSubject(subjectId)
    }

    fun addAssessment(subjectId: Int, name: String, grade: Double?, percentage: Double, examDate: String = "") {
        viewModelScope.launch {
            repository.insertAssessment(Assessment(subjectId = subjectId, name = name, grade = grade, percentage = percentage, examDate = examDate))
        }
    }

    fun deleteAssessment(assessmentId: Int) {
        viewModelScope.launch {
            repository.deleteAssessmentById(assessmentId)
        }
    }

    fun recordTripRealTime(minutes: Int) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repository.insertTrip(TripRecord(date = dateStr, durationMinutes = minutes, wasRaining = _isRaining.value))
            
            // Recalculate average travel time based on last 10 entries if present
            val trips = repository.tripRecords.first()
            if (trips.isNotEmpty()) {
                val last10 = trips.take(10)
                val avg = last10.map { it.durationMinutes }.average().toInt()
                saveBaseTravelTime(avg)
            }
        }
    }

    // Geocoding properties & triggers
    private val _locationSearchResults = MutableStateFlow<List<GeocodingResult>>(emptyList())
    val locationSearchResults: StateFlow<List<GeocodingResult>> = _locationSearchResults.asStateFlow()

    private val _isSearchingLocations = MutableStateFlow(false)
    val isSearchingLocations: StateFlow<Boolean> = _isSearchingLocations.asStateFlow()

    fun searchLocations(query: String) {
        viewModelScope.launch {
            if (query.trim().length < 2) {
                _locationSearchResults.value = emptyList()
                return@launch
            }
            _isSearchingLocations.value = true
            try {
                val results = GeocodingServiceClient.searchLocation(query.trim(), _googleMapsApiKey.value)
                _locationSearchResults.value = results
            } catch (e: Exception) {
                _locationSearchResults.value = emptyList()
            } finally {
                _isSearchingLocations.value = false
            }
        }
    }

    fun clearLocationSearchResults() {
        _locationSearchResults.value = emptyList()
    }

    fun saveGoogleMapsApiKey(key: String) {
        viewModelScope.launch {
            _googleMapsApiKey.value = key
            repository.saveSetting("google_maps_api_key", key)
        }
    }

    // Modern Attendance Log properties & triggers
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    fun registerAttendanceLog(subjectId: Int, isPresent: Boolean, dateStr: String? = null) {
        viewModelScope.launch {
            val date = dateStr ?: SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())
            
            // Prevent duplicates for the same day
            val existingLogs = repository.getLogsForSubject(subjectId).first()
            if (existingLogs.any { it.date == date }) {
                return@launch
            }

            val insertedId = repository.insertAttendanceLog(AttendanceLog(subjectId = subjectId, date = date, isPresent = isPresent))
            lastInsertedLogId = insertedId
            
            _snackbarEvent.emit("Asistencia registrada")

            // For backwards compatibility with standard dashboard cards:
            if (!isPresent) {
                repository.insertAbsence(Absence(subjectId = subjectId, date = date))
            } else {
                _weeklyStreak.value = _weeklyStreak.value + 1
            }

            // Fire real high-priority Android notifications
            val subject = repository.getSubjectById(subjectId)
            if (subject != null) {
                val currentAbsQty = repository.getAbsencesForSubject(subjectId).first().size
                val maxAbs = subject.totalClasses - Math.ceil(subject.totalClasses * (subject.requiredAttendancePercent / 100.0)).toInt()
                val remaining = maxAbs - currentAbsQty
                
                val title = if (isPresent) "Presente registrado" else "Falta registrada"
                val description = if (isPresent) {
                    "Marcaste asistencia de la materia '${subject.name}' para el día $date. ¡Genial!"
                } else {
                    if (remaining <= 0) {
                        "¡PELIGRO! Superaste el límite de faltas de ${subject.name} ($maxAbs). Podrías quedar libre."
                    } else if (remaining <= 2) {
                        "¡Atención! Te quedan solo $remaining faltas permitidas en la materia ${subject.name}."
                    } else {
                        "Falta cargada para ${subject.name}. Registraste $currentAbsQty de un máximo de $maxAbs faltas."
                    }
                }
                NotificationHelper.sendNotification(getApplication(), title, description)
            }
        }
    }

    private var lastInsertedLogId: Long? = null

    fun undoLastAttendanceLog() {
        viewModelScope.launch {
            lastInsertedLogId?.let { logId ->
                val logs = repository.attendanceLogs.first()
                val log = logs.find { it.id == logId.toInt() }
                if (log != null) {
                    repository.deleteAttendanceLogById(log.id)
                    if (!log.isPresent) {
                        val absences = repository.absences.first()
                        val absence = absences.find { it.subjectId == log.subjectId && it.date == log.date }
                        if (absence != null) {
                            repository.deleteAbsenceById(absence.id)
                        }
                    } else {
                        _weeklyStreak.value = (_weeklyStreak.value - 1).coerceAtLeast(0)
                    }
                }
            }
            lastInsertedLogId = null
        }
    }

    fun deleteAttendanceLog(logId: Int) {
        viewModelScope.launch {
            val logs = repository.attendanceLogs.first()
            val log = logs.find { it.id == logId }
            if (log != null) {
                repository.deleteAttendanceLogById(logId)
                if (!log.isPresent) {
                    val matchingAbsence = repository.getAbsencesForSubject(log.subjectId).first()
                        .find { it.date == log.date }
                    if (matchingAbsence != null) {
                        repository.deleteAbsenceById(matchingAbsence.id)
                    }
                } else {
                    if (_weeklyStreak.value > 0) {
                        _weeklyStreak.value = _weeklyStreak.value - 1
                    }
                }
            }
        }
    }

    fun getLogsForSubject(subjectId: Int): Flow<List<AttendanceLog>> {
        return repository.getLogsForSubject(subjectId)
    }

    fun clearAllData() {
        viewModelScope.launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val db = AppDatabase.getDatabase(getApplication())
                db.clearAllTables()
            }
            saveUsername("Estudiante")
            saveRoute("Casa", "Facultad")
            saveBaseTravelTime(25)
            saveGoogleMapsApiKey("")
            _isRaining.value = false
            _weeklyStreak.value = 0
            _passedSubjects.value = emptySet()
            repository.saveSetting("passed_subjects", "")
            _semesterStartDate.value = null
            repository.saveSetting("semester_start_date", "")
            _isOnboardingCompleted.value = false
            repository.saveSetting("onboarding_completed", "false")
            prepopulateDatabase()
        }
    }
}

class UniBuddyViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UniBuddyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UniBuddyViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
