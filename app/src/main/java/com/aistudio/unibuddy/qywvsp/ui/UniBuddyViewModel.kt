package com.aistudio.unibuddy.qywvsp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aistudio.unibuddy.qywvsp.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import org.json.JSONObject
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
class UniBuddyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UniBuddyRepository

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = UniBuddyRepository(db)
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Load settings with individual try-catch to prevent one bad setting from blocking others
                repository.getSetting("origin")?.let { _origin.value = it }
                repository.getSetting("destination")?.let { _destination.value = it }
                try { repository.getSetting("base_travel_time")?.let { _baseTravelTime.value = it.toInt() } } catch(e: Exception) {}
                repository.getSetting("username")?.let { _username.value = it }
                repository.getSetting("home_address")?.let { _homeAddress.value = it }
                repository.getSetting("work_address")?.let { _workAddress.value = it }
                repository.getSetting("user_university")?.let { _userUniversity.value = it }
                repository.getSetting("buddy_accessory")?.let { _buddyAccessory.value = it }
                repository.getSetting("buddy_color")?.let { _buddyColor.value = it }
                repository.getSetting("google_maps_api_key")?.let { _googleMapsApiKey.value = it }
                try { repository.getSetting("onboarding_completed")?.let { _isOnboardingCompleted.value = it.toBoolean() } } catch(e: Exception) {}
                try { repository.getSetting("dark_mode")?.let { _isDarkMode.value = it.toBoolean() } } catch(e: Exception) {}
                repository.getSetting("arrival_margin_preference")?.let { _arrivalMarginPreference.value = it }
                repository.getSetting("weather_desc")?.let { _weatherDescription.value = it }
                try { repository.getSetting("is_raining")?.let { _isRaining.value = it.toBoolean() } } catch(e: Exception) {}
                try { repository.getSetting("semester_start_date")?.let { _semesterStartDate.value = it.toLong() } } catch(e: Exception) {}
                try { repository.getSetting("auto_checkin_enabled")?.let { _autoCheckinEnabled.value = it.toBoolean() } } catch(e: Exception) {}
                try { repository.getSetting("smart_silence_enabled")?.let { _smartSilenceEnabled.value = it.toBoolean() } } catch(e: Exception) {}
                repository.getSetting("passed_subjects")?.let { _passedSubjects.value = it.split(",").filter { s -> s.isNotEmpty() }.toSet() }
                repository.getSetting("focus_objectives")?.let { _focusObjectivesJson.value = it }
                repository.getSetting("focus_sessions_history")?.let { _focusSessionsHistoryJson.value = it }
                repository.getSetting("career")?.let { _career.value = it }
                repository.getSetting("buddy_pose")?.let { _buddyPose.value = it }
                
                try {
                    val allSettings = repository.getAllSettings()
                    val importanceMap = mutableMapOf<Int, String>()
                    allSettings.forEach { setting ->
                        if (setting.key.startsWith("subject_importance_")) {
                            val subId = setting.key.replace("subject_importance_", "").toIntOrNull()
                            if (subId != null) {
                                importanceMap[subId] = setting.value
                            }
                        }
                    }
                    _subjectImportanceMap.value = importanceMap
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                
                // Populate with defaults if empty
                if (repository.getSetting("onboarding_completed") == null) {
                    prepopulateDatabase()
                }
                
                checkAndInitializeBadges()
                refreshWeather()
                checkAndTriggerProactiveNotifications()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isInitialized.value = true
            }
        }
    }

    private suspend fun checkAndTriggerProactiveNotifications() {
        // Evaluate today's classes and exams for notifications
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val todayStr = sdf.format(Date())
        val dayOfWeekFormat = SimpleDateFormat("EEEE", Locale("es", "ES"))
        var currentDayStr = dayOfWeekFormat.format(Date()).take(2).replaceFirstChar { it.uppercase() }
        if (currentDayStr == "Má" || currentDayStr == "Ma") currentDayStr = "Ma"
        if (currentDayStr == "Mi") currentDayStr = "Mi"
        if (currentDayStr == "Ju") currentDayStr = "Ju"
        if (currentDayStr == "Vi") currentDayStr = "Vi"
        if (currentDayStr == "Sá" || currentDayStr == "Sa") currentDayStr = "Sa"
        if (currentDayStr == "Do") currentDayStr = "Do"
        if (currentDayStr == "Lu") currentDayStr = "Lu"

        val currentSubjects = repository.subjects.first()
        val todayClasses = currentSubjects.filter { it.schedule.contains(currentDayStr, ignoreCase = true) }
        
        // 1. Tienes Examen Hoy
        val allAssessments = repository.assessments.first()
        val todayExams = allAssessments.filter { it.examDate == todayStr }
        if (todayExams.isNotEmpty()) {
            val examNames = todayExams.joinToString(", ") { it.name }
            NotificationHelper.sendNotification(
                getApplication(), 
                "¡Día de Examen!", 
                "Hoy tienes examen de: $examNames. ¡Mucho éxito!"
            )
        }

        // 2. Próxima Clase (Vas bien / Llegas tarde)
        if (todayClasses.isNotEmpty()) {
            // Find next class (mocking for simplicity, picking the first one)
            val nextClass = todayClasses.first()
            val time = nextClass.sessions.firstOrNull { it.day.equals(currentDayStr, ignoreCase = true) }?.time ?: ""
            val travelMins = _baseTravelTime.value
            
            // Assume the user has an arrival margin preference
            val destName = _destination.value
            NotificationHelper.sendNextClassNotification(
                getApplication(),
                nextClass.id,
                destName,
                "Tu próxima clase es ${nextClass.name}",
                "Empieza a las $time. Tu tiempo de viaje es de $travelMins min. Toca aquí para marcar asistencia o avisar retraso."
            )
        }
    }

    private suspend fun checkAndInitializeBadges() {
        val existing = repository.badges.first()
        if (existing.isEmpty()) {
            val initialBadges = listOf(
                Badge(name = "Primeros Pasos", description = "Completa el onboarding de la app.", iconId = "CheckCircle", category = "General", isUnlocked = _isOnboardingCompleted.value),
                Badge(name = "Estudiante Responsable", description = "Registra 5 días de asistencia perfecta.", iconId = "EventAvailable", category = "Attendance"),
                Badge(name = "En el Top", description = "Alcanza un promedio mayor a 90 en una materia.", iconId = "Star", category = "Grades"),
                Badge(name = "Concentración Total", description = "Usa el modo focus por más de 1 hora.", iconId = "SelfImprovement", category = "Focus")
            )
            for (badge in initialBadges) {
                repository.insertBadge(badge)
            }
        }
    }

    fun unlockBadge(name: String) {
        viewModelScope.launch {
            val badgeFlow = repository.badges.first()
            val badge = badgeFlow.find { it.name == name }
            if (badge != null && !badge.isUnlocked) {
                val updated = badge.copy(isUnlocked = true, dateUnlocked = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()))
                repository.updateBadge(updated)
                _snackbarEvent.emit("¡Insignia desbloqueada: ${badge.name}!")
            }
        }
    }

    fun saveHomeWorkAddresses(home: String, work: String) {
        viewModelScope.launch {
            _homeAddress.value = home
            _workAddress.value = work
            repository.saveSetting("home_address", home)
            repository.saveSetting("work_address", work)
        }
    }

    fun saveBuddyCustomization(accessory: String, color: String) {
        viewModelScope.launch {
            _buddyAccessory.value = accessory
            _buddyColor.value = color
            repository.saveSetting("buddy_accessory", accessory)
            repository.saveSetting("buddy_color", color)
        }
    }

    data class University(val name: String, val lat: Double, val lon: Double)

    private suspend fun prepopulateDatabase() {
        repository.saveSetting("onboarding_completed", "false")
        repository.saveSetting("username", "Estudiante")
        repository.saveSetting("origin", "Casa")
        repository.saveSetting("destination", "Facultad")
        repository.saveSetting("semester_state", "Vacaciones")
        repository.saveSetting("home_address", "Definir en perfil")
        repository.saveSetting("work_address", "Definir en perfil")
        repository.saveSetting("buddy_accessory", "none")
        repository.saveSetting("buddy_color", "#4CAF50")
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

    private val _autoCheckinEnabled = MutableStateFlow(false)
    val autoCheckinEnabled: StateFlow<Boolean> = _autoCheckinEnabled.asStateFlow()

    private val _smartSilenceEnabled = MutableStateFlow(false)
    val smartSilenceEnabled: StateFlow<Boolean> = _smartSilenceEnabled.asStateFlow()

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
                    val curriculumMatch = com.aistudio.unibuddy.qywvsp.data.CurriculumData.industrialEngineering.find { 
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

    fun setAutoCheckinEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _autoCheckinEnabled.value = enabled
            repository.saveSetting("auto_checkin_enabled", enabled.toString())
        }
    }

    fun setSmartSilenceEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _smartSilenceEnabled.value = enabled
            repository.saveSetting("smart_silence_enabled", enabled.toString())
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

    val academicHistory: StateFlow<List<AcademicRecordWithSubject>> = repository.fullAcademicHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _weeklyStreak = MutableStateFlow(12)
    val weeklyStreak: StateFlow<Int> = _weeklyStreak.asStateFlow()

    private val _username = MutableStateFlow("Estudiante")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _homeAddress = MutableStateFlow("")
    val homeAddress: StateFlow<String> = _homeAddress.asStateFlow()

    private val _workAddress = MutableStateFlow("")
    val workAddress: StateFlow<String> = _workAddress.asStateFlow()

    private val _userUniversity = MutableStateFlow("")
    val userUniversity: StateFlow<String> = _userUniversity.asStateFlow()

    private val _buddyAccessory = MutableStateFlow("none") // "none", "hat", "glasses", "scarf"
    val buddyAccessory: StateFlow<String> = _buddyAccessory.asStateFlow()

    private val _buddyColor = MutableStateFlow("#4CAF50") // Default Green
    val buddyColor: StateFlow<String> = _buddyColor.asStateFlow()

    private val _career = MutableStateFlow("Ingeniería Industrial")
    val career: StateFlow<String> = _career.asStateFlow()

    private val _buddyPose = MutableStateFlow("idle")
    val buddyPose: StateFlow<String> = _buddyPose.asStateFlow()

    private val _subjectImportanceMap = MutableStateFlow<Map<Int, String>>(emptyMap())
    val subjectImportanceMap: StateFlow<Map<Int, String>> = _subjectImportanceMap.asStateFlow()

    private val _focusObjectivesJson = MutableStateFlow("")
    val focusObjectivesJson: StateFlow<String> = _focusObjectivesJson.asStateFlow()

    private val _focusSessionsHistoryJson = MutableStateFlow("")
    val focusSessionsHistoryJson: StateFlow<String> = _focusSessionsHistoryJson.asStateFlow()

    fun saveFocusObjectives(json: String) {
        viewModelScope.launch {
            _focusObjectivesJson.value = json
            repository.saveSetting("focus_objectives", json)
        }
    }

    fun saveFocusSessionsHistory(json: String) {
        viewModelScope.launch {
            _focusSessionsHistoryJson.value = json
            repository.saveSetting("focus_sessions_history", json)
            
            // Check for badge unlock if focus sessions total exceeds 60 mins
            try {
                val array = org.json.JSONArray(json)
                var totalMins = 0
                for (i in 0 until array.length()) {
                    totalMins += array.getJSONObject(i).optInt("duration", 0)
                }
                if (totalMins >= 60) {
                    unlockBadge("Concentración Total")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val badges: StateFlow<List<Badge>> = repository.badges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    data class Campus(val name: String, val lat: Double, val lng: Double)
    data class UniversityData(val name: String, val shortName: String, val logoIcon: androidx.compose.ui.graphics.vector.ImageVector, val campuses: List<Campus>)

    val universities = listOf(
        UniversityData(
            name = "Universidad Nacional de Ingeniería",
            shortName = "UNI",
            logoIcon = Icons.Default.Home,
            campuses = listOf(
                Campus("RUPAP", 12.1475, -86.2208),
                Campus("RUSB", 12.1264, -86.2711)
            )
        ),
        UniversityData(
            name = "UNAN-Managua",
            shortName = "UNAN",
            logoIcon = Icons.Default.Place,
            campuses = listOf(
                Campus("Recinto Rubén Darío", 12.1192, -86.2644)
            )
        ),
        UniversityData(
            name = "Universidad Casimiro Sotelo",
            shortName = "UCS",
            logoIcon = Icons.Default.AccountBox,
            campuses = listOf(
                Campus("Sede Central", 12.1258, -86.2708)
            )
        ),
        UniversityData(
            name = "Universidad Politécnica de Nicaragua",
            shortName = "UPOLI",
            logoIcon = Icons.Default.Info,
            campuses = listOf(
                Campus("Sede Central", 12.1419, -86.2253)
            )
        ),
        UniversityData(
            name = "Universidad Americana",
            shortName = "UAM",
            logoIcon = Icons.Default.Star,
            campuses = listOf(
                Campus("Campus Principal", 12.1156, -86.2369)
            )
        )
    )

    private val _isLocationAvailable = MutableStateFlow(false)
    val isLocationAvailable: StateFlow<Boolean> = _isLocationAvailable.asStateFlow()

    private val _currentLocationName = MutableStateFlow("Buscando...")
    val currentLocationName: StateFlow<String> = _currentLocationName.asStateFlow()

    fun updateLocationStatus(available: Boolean, name: String = "") {
        _isLocationAvailable.value = available
        if (name.isNotEmpty()) _currentLocationName.value = name
    }

    fun getSelectedUniversityCoords(): Pair<Double, Double>? {
        val uni = universities.find { it.name == _destination.value }
        val campus = uni?.campuses?.firstOrNull()
        return if (campus != null) Pair(campus.lat, campus.lng) else null
    }

    private val _googleMapsApiKey = MutableStateFlow("")
    val googleMapsApiKey: StateFlow<String> = _googleMapsApiKey.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            _isDarkMode.value = enabled
            repository.saveSetting("dark_mode", enabled.toString())
        }
    }

    private val _shortcutDestination = MutableStateFlow<String?>(null)
    val shortcutDestination: StateFlow<String?> = _shortcutDestination.asStateFlow()

    fun handleShortcutDestination(destination: String) {
        _shortcutDestination.value = destination
    }

    fun clearShortcutDestination() {
        _shortcutDestination.value = null
    }

    // Methods
    fun updateOnboardingStatus(completed: Boolean) {
        viewModelScope.launch {
            _isOnboardingCompleted.value = completed
            repository.saveSetting("onboarding_completed", completed.toString())
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

    fun saveCareer(careerName: String) {
        viewModelScope.launch {
            _career.value = careerName
            repository.saveSetting("career", careerName)
        }
    }

    fun saveUserUniversity(universityName: String) {
        viewModelScope.launch {
            _userUniversity.value = universityName
            repository.saveSetting("user_university", universityName)
        }
    }

    fun saveBuddyPose(poseName: String) {
        viewModelScope.launch {
            _buddyPose.value = poseName
            repository.saveSetting("buddy_pose", poseName)
        }
    }

    fun saveSubjectImportance(subjectId: Int, importanceValue: String) {
        viewModelScope.launch {
            val currentMap = _subjectImportanceMap.value.toMutableMap()
            currentMap[subjectId] = importanceValue
            _subjectImportanceMap.value = currentMap
            repository.saveSetting("subject_importance_$subjectId", importanceValue)
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

    fun importAcademicRecords(subjects: List<HistorialParser.ParsedSubject>) {
        viewModelScope.launch {
            val careerId = repository.getOrCreateCareer(
                universityName = _userUniversity.value ?: "UNI",
                campusName = _destination.value,
                careerName = _career.value
            )
            
            subjects.forEach { parsed ->
                val subjectId = repository.insertPensumSubject(
                    PensumSubject(
                        careerId = careerId,
                        code = parsed.code,
                        name = parsed.name,
                        semester = "S/D", // Derived from parsed data if possible
                        credits = parsed.credits,
                        isNumbers = false
                    )
                )
                repository.insertAcademicRecord(
                    AcademicRecord(
                        pensumSubjectId = subjectId.toInt(),
                        grade = parsed.grade,
                        status = parsed.status,
                        year = "S/D",
                        academicGroup = parsed.group
                    )
                )
            }
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

    fun addSubject(name: String, schedule: String, sessions: List<com.aistudio.unibuddy.qywvsp.data.ClassSessionDetails>, requiredAttendancePercent: Int, totalClasses: Int, colorHex: String = "#FFB3E5FC") {
        viewModelScope.launch {
            val subject = Subject(
                name = name,
                schedule = schedule,
                sessions = sessions,
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

    fun exportBackup(): String {
        val root = JSONObject()
        try {
            root.put("version", 1)
            
            val settingsObj = JSONObject()
            settingsObj.put("username", _username.value)
            settingsObj.put("origin", _origin.value)
            settingsObj.put("destination", _destination.value)
            settingsObj.put("home_address", _homeAddress.value)
            settingsObj.put("work_address", _workAddress.value)
            settingsObj.put("buddy_accessory", _buddyAccessory.value)
            settingsObj.put("buddy_color", _buddyColor.value)
            settingsObj.put("base_travel_time", _baseTravelTime.value)
            settingsObj.put("arrival_margin_preference", _arrivalMarginPreference.value)
            root.put("settings", settingsObj)

            val subjectsArr = JSONArray()
            val currentSubjects = subjects.value
            val currentAssessments = assessments.value
            val currentAbsences = absences.value
            val currentLogs = attendanceLogs.value

            for (sub in currentSubjects) {
                val subObj = JSONObject()
                subObj.put("name", sub.name)
                subObj.put("schedule", sub.schedule)
                subObj.put("sessionsJson", com.aistudio.unibuddy.qywvsp.data.Converters().fromList(sub.sessions))
                subObj.put("requiredAttendancePercent", sub.requiredAttendancePercent)
                subObj.put("totalClasses", sub.totalClasses)
                subObj.put("colorHex", sub.colorHex)

                // Assessments
                val assArr = JSONArray()
                val subAss = currentAssessments.filter { it.subjectId == sub.id }
                for (ass in subAss) {
                    val assObj = JSONObject()
                    assObj.put("name", ass.name)
                    if (ass.grade != null) assObj.put("grade", ass.grade) else assObj.put("grade", JSONObject.NULL)
                    assObj.put("percentage", ass.percentage)
                    assObj.put("examDate", ass.examDate)
                    assArr.put(assObj)
                }
                subObj.put("assessments", assArr)

                // Absences
                val absArr = JSONArray()
                val subAbs = currentAbsences.filter { it.subjectId == sub.id }
                for (abs in subAbs) {
                    val absObj = JSONObject()
                    absObj.put("date", abs.date)
                    absArr.put(absObj)
                }
                subObj.put("absences", absArr)

                // Logs
                val logArr = JSONArray()
                val subLogs = currentLogs.filter { it.subjectId == sub.id }
                for (log in subLogs) {
                    val logObj = JSONObject()
                    logObj.put("date", log.date)
                    logObj.put("isPresent", log.isPresent)
                    logArr.put(logObj)
                }
                subObj.put("logs", logArr)

                subjectsArr.put(subObj)
            }
            root.put("subjects", subjectsArr)
            
            return root.toString(2)
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    fun importBackup(jsonStr: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val root = JSONObject(jsonStr)
                
                // 1. Restore settings
                if (root.has("settings")) {
                    val s = root.getJSONObject("settings")
                    saveUsername(s.optString("username", "Estudiante"))
                    saveRoute(s.optString("origin", "Casa"), s.optString("destination", "Facultad"))
                    saveHomeWorkAddresses(s.optString("home_address", ""), s.optString("work_address", ""))
                    saveBuddyCustomization(s.optString("buddy_accessory", "none"), s.optString("buddy_color", "#4CAF50"))
                    saveBaseTravelTime(s.optInt("base_travel_time", 25))
                    setArrivalMarginPreference(s.optString("arrival_margin_preference", "normal"))
                }

                // 2. Clear academic database tables first
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val db = AppDatabase.getDatabase(getApplication())
                    db.subjectDao().getAllSubjects().first().forEach {
                        db.subjectDao().deleteSubject(it)
                    }
                }

                // 3. Import subjects, assessments, absences, logs
                if (root.has("subjects")) {
                    val subArr = root.getJSONArray("subjects")
                    for (i in 0 until subArr.length()) {
                        val subObj = subArr.getJSONObject(i)
                        val sub = Subject(
                            name = subObj.getString("name"),
                            schedule = subObj.getString("schedule"),
                            sessions = com.aistudio.unibuddy.qywvsp.data.Converters().fromString(subObj.optString("sessionsJson", "[]")),
                            requiredAttendancePercent = subObj.getInt("requiredAttendancePercent"),
                            totalClasses = subObj.getInt("totalClasses"),
                            colorHex = subObj.optString("colorHex", "#FFB3E5FC")
                        )
                        val newSubId = repository.insertSubject(sub).toInt()

                        // Import Assessments
                        if (subObj.has("assessments")) {
                            val assArr = subObj.getJSONArray("assessments")
                            for (j in 0 until assArr.length()) {
                                val assObj = assArr.getJSONObject(j)
                                val gradeVal = if (assObj.isNull("grade")) null else assObj.getDouble("grade")
                                repository.insertAssessment(
                                    Assessment(
                                        subjectId = newSubId,
                                        name = assObj.getString("name"),
                                        grade = gradeVal,
                                        percentage = assObj.getDouble("percentage"),
                                        examDate = assObj.optString("examDate", "")
                                    )
                                )
                            }
                        }

                        // Import Absences
                        if (subObj.has("absences")) {
                            val absArr = subObj.getJSONArray("absences")
                            for (j in 0 until absArr.length()) {
                                val absObj = absArr.getJSONObject(j)
                                repository.insertAbsence(
                                    Absence(
                                        subjectId = newSubId,
                                        date = absObj.getString("date")
                                    )
                                )
                            }
                        }

                        // Import Logs
                        if (subObj.has("logs")) {
                            val logArr = subObj.getJSONArray("logs")
                            for (j in 0 until logArr.length()) {
                                val logObj = logArr.getJSONObject(j)
                                repository.insertAttendanceLog(
                                    AttendanceLog(
                                        subjectId = newSubId,
                                        date = logObj.getString("date"),
                                        isPresent = logObj.getBoolean("isPresent")
                                    )
                                )
                            }
                        }
                    }
                }

                // Ensure onboarding is marked completed after restoring
                _isOnboardingCompleted.value = true
                repository.saveSetting("onboarding_completed", "true")

                _snackbarEvent.emit("Respaldo restaurado con éxito")
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Error desconocido")
            }
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
