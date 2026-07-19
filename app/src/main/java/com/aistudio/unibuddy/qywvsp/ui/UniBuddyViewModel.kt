package com.aistudio.unibuddy.qywvsp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aistudio.unibuddy.qywvsp.data.*
import com.aistudio.unibuddy.qywvsp.ui.UpdateManager
import com.google.firebase.auth.FirebaseUser
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
    private val firebaseManager = FirebaseManager()
    val currentUser: FirebaseUser? get() = firebaseManager.currentUser

    private val sentNotifications = mutableSetOf<String>()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    private val _updateInfo = MutableStateFlow<UpdateManager.UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateManager.UpdateInfo?> = _updateInfo.asStateFlow()

    init {
        checkForUpdates()
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
                repository.getSetting("weekly_streak_count")?.let { _weeklyStreak.value = it.toIntOrNull() ?: 0 }
                try { repository.getSetting("onboarding_completed")?.let { _isOnboardingCompleted.value = it.toBoolean() } } catch(e: Exception) {}
                try { repository.getSetting("dark_mode")?.let { _isDarkMode.value = it.toBoolean() } } catch(e: Exception) {}
                repository.getSetting("arrival_margin_preference")?.let { _arrivalMarginPreference.value = it }
                repository.getSetting("weather_desc")?.let { _weatherDescription.value = it }
                try { repository.getSetting("is_raining")?.let { _isRaining.value = it.toBoolean() } } catch(e: Exception) {}
                try { repository.getSetting("semester_start_date")?.let { _semesterStartDate.value = it.toLong() } } catch(e: Exception) {}
                try { repository.getSetting("auto_checkin_enabled")?.let { _autoCheckinEnabled.value = it.toBoolean() } } catch(e: Exception) {}
                try { repository.getSetting("smart_silence_enabled")?.let { _smartSilenceEnabled.value = it.toBoolean() } } catch(e: Exception) {}
                try { repository.getSetting("default_exam_percentage")?.let { _defaultExamPercentage.value = it.toDouble() } } catch(e: Exception) {}
                try { repository.getSetting("default_test_percentage")?.let { _defaultTestPercentage.value = it.toDouble() } } catch(e: Exception) {}
                repository.getSetting("passed_subjects")?.let { _passedSubjects.value = it.split(",").filter { s -> s.isNotEmpty() }.toSet() }
                repository.getSetting("focus_objectives")?.let { _focusObjectivesJson.value = it }
                repository.getSetting("focus_sessions_history")?.let { _focusSessionsHistoryJson.value = it }
                repository.getSetting("career")?.let { _career.value = it }
                repository.getSetting("buddy_pose")?.let { _buddyPose.value = it }
                try { repository.getSetting("buddy_xp")?.let { _buddyXp.value = it.toInt() } } catch(e: Exception) {}
                try { repository.getSetting("cancelled_classes")?.let { _cancelledClasses.value = it.split(",").filter { s -> s.isNotBlank() }.toSet() } } catch(e: Exception) {}
                try { repository.getSetting("custom_holidays")?.let {
                    if (it.isNotEmpty()) _customHolidays.value = it.split(",").mapNotNull { ms -> ms.toLongOrNull() }
                } } catch(e: Exception) {}
                
                try { repository.getSetting("custom_suspended_weeks")?.let {
                    if (it.isNotEmpty()) _customSuspendedWeeks.value = it.split(",").mapNotNull { idx -> idx.trim().toIntOrNull() }.toSet()
                } } catch(e: Exception) {}
                try { repository.getSetting("parity_override_week")?.let { _parityOverrideWeek.value = it.toInt() } } catch(e: Exception) {}
                try { repository.getSetting("parity_override_val")?.let { _parityOverrideVal.value = it } } catch(e: Exception) {}
                try { repository.getSetting("user_custom_week_offset")?.let { _userCustomWeekOffset.value = it.toInt() } } catch(e: Exception) {}
                try { repository.getSetting("last_calendar_query_time")?.let { _lastCalendarQueryTime.value = it.toLong() } } catch(e: Exception) {}
                
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


    fun checkForUpdates() {
        viewModelScope.launch {
            val info = UpdateManager.checkForUpdates()
            if (info != null) {
                _updateInfo.value = info
                // Aplicar configuraciones dinámicas si hay alguna (Opción 2)
                info.dynamicConfig?.forEach { (key, value) ->
                    repository.saveSetting(key, value)
                }
            }
        }
    }

    private fun notifyWidgets() {
        viewModelScope.launch {
            try {
                val calendar = java.util.Calendar.getInstance()
                val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                val dayOfYear = calendar.get(java.util.Calendar.DAY_OF_YEAR)
                val weatherState = if (currentHour in 6..18) {
                    if (dayOfYear % 3 == 0) "rainy" else "sunny"
                } else {
                    "night"
                }
                
                com.aistudio.unibuddy.qywvsp.ui.widget.PetBitmapRenderer.generateAndSavePetBitmap(
                    context = getApplication(),
                    pose = _buddyPose.value,
                    accessory = _buddyAccessory.value,
                    isHappy = true,
                    isWorried = false,
                    weatherState = weatherState,
                    mainColorHex = _buddyColor.value
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            com.aistudio.unibuddy.qywvsp.ui.widget.WidgetUpdater.updateAllWidgets(getApplication())
        }
    }

    private suspend fun checkAndTriggerProactiveNotifications() {
        // Evaluate today's classes and exams for notifications
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val todayFullStr = sdf.format(Date())
        
        // Find proper dayOfWeek code for matching subject sessions (Lu, Ma, Mi, Ju, Vi, Sá, Do)
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val dayCode = when (dayOfWeek) {
            Calendar.MONDAY -> "Lu"
            Calendar.TUESDAY -> "Ma"
            Calendar.WEDNESDAY -> "Mi"
            Calendar.THURSDAY -> "Ju"
            Calendar.FRIDAY -> "Vi"
            Calendar.SATURDAY -> "Sá"
            else -> "Do"
        }

        val currentSubjects = repository.subjects.first()
        val todayClasses = currentSubjects.filter { it.schedule.contains(dayCode, ignoreCase = true) }
        
        // 0. Anticipated Risk Notification (A una falta del riesgo)
        for (sub in currentSubjects) {
            val allAbs = repository.attendanceLogs.first().filter { it.subjectId == sub.id && !it.isPresent }
            val maxAbs = sub.totalClasses - kotlin.math.ceil(sub.totalClasses * (sub.requiredAttendancePercent / 100.0)).toInt()
            val remaining = (maxAbs - allAbs.size).coerceAtLeast(0)
            
            val dbKey = "risk_notified_${sub.id}"
            val lastNotifiedRemaining = repository.getSetting(dbKey)?.toIntOrNull()

            if (remaining == 3) {
                if (lastNotifiedRemaining != 3) {
                    repository.saveSetting(dbKey, "3")
                    NotificationHelper.sendNotification(
                        getApplication(),
                        "Riesgo Anticipado",
                        "Estás a una falta de entrar en zona de riesgo en ${sub.name}. ¡No faltes!"
                    )
                }
            } else {
                // Reset the flag if they go below or above the threshold
                if (lastNotifiedRemaining == 3) {
                    repository.saveSetting(dbKey, remaining.toString())
                }
            }
        }

        // 1. Tienes Examen Hoy
        val allAssessments = repository.assessments.first()
        val todayExams = allAssessments.filter { it.examDate == todayFullStr }
        if (todayExams.isNotEmpty()) {
            val examNames = todayExams.joinToString(", ") { it.name }
            val examNotifKey = "exam_${todayFullStr}_${examNames}"
            if (!sentNotifications.contains(examNotifKey)) {
                sentNotifications.add(examNotifKey)
                NotificationHelper.sendNotification(
                    getApplication(), 
                    "¡Día de Examen!", 
                    "Hoy tienes examen de: $examNames. ¡Mucho éxito!"
                )
            }
        }

        // 2. Próxima Clase Reminders: "tiempo antes" (15-45m) and "hora de entrada" (-15 to 5m)
        val todayCalendar = Calendar.getInstance()
        val hour = todayCalendar.get(Calendar.HOUR_OF_DAY)
        val min = todayCalendar.get(Calendar.MINUTE)
        val currentTotalMin = hour * 60 + min
        val todayStrShort = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())
        val todayDateKey = SimpleDateFormat("dd_MM_yyyy", Locale.getDefault()).format(Date())

        for (sub in todayClasses) {
            val sessionsForToday = sub.sessions.filter { it.day.equals(dayCode, ignoreCase = true) }
            for (session in sessionsForToday) {
                try {
                    val range = com.aistudio.unibuddy.qywvsp.ui.parseTimeRange(session.time)
                    if (range != null) {
                        val startHour = range.first.first
                        val startMin = range.first.second
                        val startTotalMin = startHour * 60 + startMin
                        
                        val diffMinutes = startTotalMin - currentTotalMin
                        val travelMins = _locationBasedTravelMinutes.value ?: _baseTravelTime.value
                        val destName = _destination.value
                        
                        val allAbsences = repository.attendanceLogs.first().filter { it.subjectId == sub.id && !it.isPresent }
                        val maxAbs = sub.totalClasses - kotlin.math.ceil(sub.totalClasses * (sub.requiredAttendancePercent / 100.0)).toInt()
                        val remainingAbsences = (maxAbs - allAbsences.size).coerceAtLeast(0)
                        val isAbsencesCritical = remainingAbsences <= 2

                        // Reminder 1: "Tiempo Antes" (Alert 15 to 45 mins before class start)
                        if (diffMinutes in 15..45) {
                            val beforeNotifKey = "${sub.id}_before_${todayDateKey}"
                            if (!sentNotifications.contains(beforeNotifKey)) {
                                sentNotifications.add(beforeNotifKey)
                                NotificationHelper.sendNextClassNotification(
                                    getApplication(),
                                    sub.id,
                                    destName,
                                    if (isAbsencesCritical) "¡URGENTE! Clase de ${sub.name}" else "Prepárate: Clase de ${sub.name}",
                                    if (isAbsencesCritical) "Empieza en $diffMinutes min. ¡Casi repruebas por faltas, no faltes!" else "Empieza en $diffMinutes min (a las ${session.time}). Tiempo estimado de viaje: $travelMins min.",
                                    isCritical = isAbsencesCritical
                                )
                            }
                        }

                        // Reminder 2: "A la Hora de Entrada" (Alert from -15 mins up to class start time)
                        if (diffMinutes in -15..1) {
                            val atNotifKey = "${sub.id}_at_${todayDateKey}"
                            if (!sentNotifications.contains(atNotifKey)) {
                                // Double check if user has already checked in for today
                                val existingLogs = repository.attendanceLogs.first().filter { 
                                    it.subjectId == sub.id && it.date.startsWith(todayStrShort) 
                                }
                                if (existingLogs.isEmpty()) {
                                    sentNotifications.add(atNotifKey)
                                    NotificationHelper.sendNextClassNotification(
                                        getApplication(),
                                        sub.id,
                                        destName,
                                        if (isAbsencesCritical) "¡ASISTE AHORA! ${sub.name}" else "¡Hora de Entrada a ${sub.name}!",
                                        if (isAbsencesCritical) "¡Últimas faltas! Registra asistencia." else "Tu clase ya está iniciando (hora: ${session.time}). Toca aquí para registrar asistencia.",
                                        isCritical = isAbsencesCritical
                                    )
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
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
        repository.saveSetting("buddy_color", "#0F172A")
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

    // Real-time trip status state & stopwatch controller
        // Pomodoro State
    private val _pomodoroSecondsLeft = MutableStateFlow(1500)
    val pomodoroSecondsLeft: StateFlow<Int> = _pomodoroSecondsLeft.asStateFlow()

    private val _isPomodoroActive = MutableStateFlow(false)
    val isPomodoroActive: StateFlow<Boolean> = _isPomodoroActive.asStateFlow()

    private var pomodoroJob: kotlinx.coroutines.Job? = null

    fun togglePomodoro() {
        if (_isPomodoroActive.value) {
            _isPomodoroActive.value = false
            pomodoroJob?.cancel()
        } else {
            if (_pomodoroSecondsLeft.value <= 0) _pomodoroSecondsLeft.value = 1500
            _isPomodoroActive.value = true
            pomodoroJob = viewModelScope.launch {
                while (_pomodoroSecondsLeft.value > 0 && _isPomodoroActive.value) {
                    kotlinx.coroutines.delay(1000L)
                    _pomodoroSecondsLeft.value -= 1
                }
                _isPomodoroActive.value = false
            }
        }
    }

    fun resetPomodoro() {
        _isPomodoroActive.value = false
        pomodoroJob?.cancel()
        _pomodoroSecondsLeft.value = 1500
    }

    private val _isTripActive = MutableStateFlow(false)
    val isTripActive: StateFlow<Boolean> = _isTripActive.asStateFlow()

    private val _tripElapsedSeconds = MutableStateFlow(0)
    val tripElapsedSeconds: StateFlow<Int> = _tripElapsedSeconds.asStateFlow()

    private var tripTimerJob: kotlinx.coroutines.Job? = null

    fun startTrip() {
        if (_isTripActive.value) return
        _isTripActive.value = true
        _tripElapsedSeconds.value = 0
        tripTimerJob?.cancel()
        tripTimerJob = viewModelScope.launch(Dispatchers.Default) {
            while (_isTripActive.value) {
                kotlinx.coroutines.delay(1000)
                _tripElapsedSeconds.value += 1
            }
        }
    }

    fun endTrip(finalMinutes: Int) {
        _isTripActive.value = false
        tripTimerJob?.cancel()
        tripTimerJob = null
        recordTripRealTime(finalMinutes)
        _tripElapsedSeconds.value = 0
    }

    val assessments: StateFlow<List<Assessment>> = repository.assessments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<Task>> = repository.tasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val seasonRecaps: StateFlow<List<com.aistudio.unibuddy.qywvsp.data.SeasonRecap>> = repository.seasonRecaps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _showSeasonRecap = kotlinx.coroutines.flow.MutableStateFlow<com.aistudio.unibuddy.qywvsp.data.SeasonRecap?>(null)
    val showSeasonRecap: kotlinx.coroutines.flow.StateFlow<com.aistudio.unibuddy.qywvsp.data.SeasonRecap?> = _showSeasonRecap.asStateFlow()
    fun dismissSeasonRecap() { _showSeasonRecap.value = null }

    val allProfessors: Flow<List<com.aistudio.unibuddy.qywvsp.data.Professor>> = repository.getAllProfessors()

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

    private val _defaultExamPercentage = MutableStateFlow(15.0)
    val defaultExamPercentage: StateFlow<Double> = _defaultExamPercentage.asStateFlow()

    private val _defaultTestPercentage = MutableStateFlow(7.0)
    val defaultTestPercentage: StateFlow<Double> = _defaultTestPercentage.asStateFlow()

    fun saveDefaultExamPercentage(percent: Double) {
        viewModelScope.launch {
            _defaultExamPercentage.value = percent
            repository.saveSetting("default_exam_percentage", percent.toString())
        }
    }

    fun saveDefaultTestPercentage(percent: Double) {
        viewModelScope.launch {
            _defaultTestPercentage.value = percent
            repository.saveSetting("default_test_percentage", percent.toString())
        }
    }

    private fun getEasterDate(year: Int): Calendar {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = ((h + l - 7 * m + 114) % 31) + 1
        
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    private val _customSuspendedWeeks = MutableStateFlow<Set<Int>>(emptySet())
    val customSuspendedWeeks: StateFlow<Set<Int>> = _customSuspendedWeeks.asStateFlow()

    private val _parityOverrideWeek = MutableStateFlow<Int>(-1)
    val parityOverrideWeek: StateFlow<Int> = _parityOverrideWeek.asStateFlow()

    private val _parityOverrideVal = MutableStateFlow<String>("")
    val parityOverrideVal: StateFlow<String> = _parityOverrideVal.asStateFlow()

    private val _userCustomWeekOffset = MutableStateFlow<Int>(0)
    val userCustomWeekOffset: StateFlow<Int> = _userCustomWeekOffset.asStateFlow()

    private val _lastCalendarQueryTime = MutableStateFlow<Long>(0L)
    val lastCalendarQueryTime: StateFlow<Long> = _lastCalendarQueryTime.asStateFlow()

    private val _semesterStartDate = MutableStateFlow<Long?>(null)
    val semesterStartDate: StateFlow<Long?> = _semesterStartDate.asStateFlow()

    fun updateSemesterStartDate(millis: Long) {
        viewModelScope.launch {
            _semesterStartDate.value = millis
            repository.saveSetting("semester_start_date", millis.toString())
        }
    }

    // 2026 Academic Calendar state and data structures
    data class SemesterWeekState(
        val semesterName: String,
        val calendarWeekNumber: Int,
        val academicWeekNumber: Int,
        val isRecessWeek: Boolean,
        val recessReason: String,
        val isEvenWeek: Boolean,
        val weekStateLabel: String
    )

    data class CalendarPrompt(
        val id: String,
        val question: String,
        val pose: String,
        val options: List<Pair<String, String>>
    )

    fun getAcademicCalendarStateAt(timeMs: Long): SemesterWeekState {
        // Boundaries for 2026
        val sem1Start = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 7, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val sem1End = Calendar.getInstance().apply {
            set(2026, Calendar.JULY, 3, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val sem2Start = Calendar.getInstance().apply {
            set(2026, Calendar.JULY, 25, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val sem2End = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 20, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }
        
        if (timeMs < sem1Start.timeInMillis) {
            return SemesterWeekState(
                semesterName = "Antes del Semestre I",
                calendarWeekNumber = 1,
                academicWeekNumber = 1,
                isRecessWeek = false,
                recessReason = "",
                isEvenWeek = false,
                weekStateLabel = "Clases"
            )
        }
        
        val isSem1 = timeMs <= sem1End.timeInMillis
        val isIntersemestral = timeMs > sem1End.timeInMillis && timeMs < sem2Start.timeInMillis
        val isSem2 = timeMs >= sem2Start.timeInMillis && timeMs <= sem2End.timeInMillis
        val isChristmas = timeMs > sem2End.timeInMillis
        
        val semesterName = when {
            isSem1 -> "I Semestre 2026"
            isIntersemestral -> "Receso Intersemestral"
            isSem2 -> "II Semestre 2026"
            else -> "Vacaciones Navideñas"
        }
        
        val baseStart = if (isSem2 || isChristmas) sem2Start else sem1Start
        val oneWeekMillis = 7L * 24L * 60L * 60L * 1000L
        
        val elapsedMillis = timeMs - baseStart.timeInMillis
        val calendarWeekIndex = (elapsedMillis / oneWeekMillis).toInt().coerceAtLeast(0)
        
        var academicWeekCount = 0
        var isCurrentWeekRecess = false
        var currentRecessReason = ""
        
        val suspendedSet = _customSuspendedWeeks.value
        
        for (w in 0..calendarWeekIndex) {
            var recess = false
            var reason = ""
            
            if (isSem1) {
                if (w == 3) { // Semana Santa relative to Mar 07 (Week 4: Mar 28 - Apr 03)
                    recess = true
                    reason = "Semana Santa"
                }
            } else {
                if (w == 7) { // Fiestas Patrias relative to Jul 25 (Week 8: Sep 12 - Sep 18)
                    recess = true
                    reason = "Fiestas Patrias"
                }
            }
            
            if (suspendedSet.contains(w)) {
                recess = true
                reason = "Clases Suspendidas"
            }
            
            if (w == calendarWeekIndex) {
                isCurrentWeekRecess = recess
                currentRecessReason = reason
            }
            
            if (!recess) {
                academicWeekCount++
            }
        }
        
        val finalAcademicWeek = (academicWeekCount + _userCustomWeekOffset.value).coerceAtLeast(1)
        
        val weekStateLabel = when {
            isIntersemestral -> "Vacaciones"
            isChristmas -> "Vacaciones"
            isCurrentWeekRecess -> "Receso"
            else -> {
                if (isSem1) {
                    when (finalAcademicWeek) {
                        in 1..7 -> "Clases"
                        in 8..9 -> "Exámenes"
                        in 10..14 -> "Clases"
                        15 -> "Exámenes"
                        16 -> "Recuperación"
                        else -> "Vacaciones"
                    }
                } else {
                    when (finalAcademicWeek) {
                        in 1..6 -> "Clases"
                        in 7..8 -> "Exámenes"
                        in 9..14 -> "Clases"
                        15 -> "Exámenes"
                        16 -> "Recuperación"
                        else -> "Vacaciones"
                    }
                }
            }
        }
        
        var isEven = finalAcademicWeek % 2 == 0
        val overrideW = _parityOverrideWeek.value
        val overrideV = _parityOverrideVal.value
        
        if (overrideW != -1 && finalAcademicWeek >= overrideW) {
            val diff = finalAcademicWeek - overrideW
            val isDiffEven = diff % 2 == 0
            val overrideIsEven = overrideV.equals("Par", ignoreCase = true)
            isEven = if (isDiffEven) overrideIsEven else !overrideIsEven
        }
        
        return SemesterWeekState(
            semesterName = semesterName,
            calendarWeekNumber = calendarWeekIndex + 1,
            academicWeekNumber = finalAcademicWeek,
            isRecessWeek = isCurrentWeekRecess,
            recessReason = currentRecessReason,
            isEvenWeek = isEven,
            weekStateLabel = weekStateLabel
        )
    }

    val academicCalendarState = combine(
        _semesterStartDate,
        _customSuspendedWeeks,
        _parityOverrideWeek,
        _parityOverrideVal,
        _userCustomWeekOffset
    ) { _, _, _, _, _ ->
        getAcademicCalendarStateAt(System.currentTimeMillis())
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SemesterWeekState(
            semesterName = "I Semestre 2026",
            calendarWeekNumber = 1,
            academicWeekNumber = 1,
            isRecessWeek = false,
            recessReason = "",
            isEvenWeek = false,
            weekStateLabel = "Clases"
        )
    )

    val currentWeekOfSemester = academicCalendarState.map { state ->
        state.academicWeekNumber
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val semesterState = academicCalendarState.map { state ->
        state.weekStateLabel
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Clases")

    val isEvenWeek = academicCalendarState.map { state ->
        state.isEvenWeek
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleParity() {
        viewModelScope.launch {
            val currentState = academicCalendarState.value
            val currentW = currentState.academicWeekNumber
            val currentIsEven = currentState.isEvenWeek
            val newVal = if (currentIsEven) "Impar" else "Par"
            
            _parityOverrideWeek.value = currentW
            _parityOverrideVal.value = newVal
            
            repository.saveSetting("parity_override_week", currentW.toString())
            repository.saveSetting("parity_override_val", newVal)
        }
    }

    fun forceParity(valStr: String) {
        viewModelScope.launch {
            val currentW = academicCalendarState.value.academicWeekNumber
            _parityOverrideWeek.value = currentW
            _parityOverrideVal.value = valStr
            
            repository.saveSetting("parity_override_week", currentW.toString())
            repository.saveSetting("parity_override_val", valStr)
        }
    }

    fun adjustWeekNumber(targetWeek: Int) {
        viewModelScope.launch {
            val tempOffset = _userCustomWeekOffset.value
            _userCustomWeekOffset.value = 0
            val baseState = getAcademicCalendarStateAt(System.currentTimeMillis())
            val baseWeek = baseState.academicWeekNumber
            
            val newOffset = targetWeek - baseWeek
            _userCustomWeekOffset.value = newOffset
            
            repository.saveSetting("user_custom_week_offset", newOffset.toString())
        }
    }

    fun setWeekSuspended(relativeWeekIndex: Int, suspended: Boolean) {
        viewModelScope.launch {
            val current = _customSuspendedWeeks.value.toMutableSet()
            if (suspended) {
                current.add(relativeWeekIndex)
            } else {
                current.remove(relativeWeekIndex)
            }
            _customSuspendedWeeks.value = current
            repository.saveSetting("custom_suspended_weeks", current.joinToString(","))
        }
    }

    fun toggleCurrentWeekSuspended() {
        val currentState = academicCalendarState.value
        val relativeIndex = currentState.calendarWeekNumber - 1
        val isSuspended = _customSuspendedWeeks.value.contains(relativeIndex)
        setWeekSuspended(relativeIndex, !isSuspended)
    }

    fun dismissPrompt() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            _lastCalendarQueryTime.value = now
            repository.saveSetting("last_calendar_query_time", now.toString())
        }
    }

    val activeCalendarPrompt = combine(
        academicCalendarState,
        _lastCalendarQueryTime
    ) { state, lastQueryTime ->
        val now = System.currentTimeMillis()
        val daysSinceLastQuery = (now - lastQueryTime) / (24 * 60 * 60 * 1000L)
        
        // Show prompt if daysSinceLastQuery >= 14 (every 2 weeks)
        // OR if critical milestone (Academic Week 1, Week 8, Week 13, Week 15) and daysSinceLastQuery >= 7
        // Also if lastQueryTime is 0, we show it (first time ever)
        val isCriticalMilestone = state.academicWeekNumber in listOf(1, 8, 13, 15)
        val shouldShow = lastQueryTime == 0L || daysSinceLastQuery >= 14 || (isCriticalMilestone && daysSinceLastQuery >= 7)
        
        if (shouldShow) {
            when {
                state.semesterName == "Receso Intersemestral" || state.semesterName == "Vacaciones Navideñas" -> {
                    CalendarPrompt(
                        id = "vacation_confirm",
                        question = "¡Hola de nuevo! Mis sensores dicen que estamos en vacaciones universitarias. ¿Es correcto o ya inició tu semestre?",
                        pose = "sleeping",
                        options = listOf("Sí, de vacaciones" to "confirm", "Ya inició semestre" to "start_semester")
                    )
                }
                state.isRecessWeek -> {
                    CalendarPrompt(
                        id = "recess_confirm",
                        question = "¡Hola! Estamos en receso por '${state.recessReason}'. ¿Estás descansando bien o se reanudaron las clases?",
                        pose = "idle",
                        options = listOf("Sí, en receso" to "confirm", "Hubo clases normales" to "unsuspend")
                    )
                }
                state.academicWeekNumber == 13 -> {
                    CalendarPrompt(
                        id = "week_13_cheer",
                        question = "¡Semana 13! ¡Ya casi acabamos esto! ¿Llevas al día tus materias par/impar?",
                        pose = "celebrating",
                        options = listOf("¡Sí, al día!" to "confirm", "Ajustar par/impar" to "toggle_parity")
                    )
                }
                state.weekStateLabel.startsWith("Exámenes") -> {
                    CalendarPrompt(
                        id = "exams_confirm",
                        question = "¡Hola! ¿Ya empezó tu semana de exámenes o se retrasaron las fechas?",
                        pose = "exam",
                        options = listOf("Sí, en exámenes" to "confirm", "Se retrasaron" to "suspend_week")
                    )
                }
                else -> {
                    CalendarPrompt(
                        id = "parity_confirm",
                        question = "Hola volví. ¿Estamos en semana ${if(state.isEvenWeek) "PAR" else "IMPAR"}? Ajustemos todo hacia adelante.",
                        pose = "greeting",
                        options = listOf(
                            "Es ${if(state.isEvenWeek) "PAR" else "IMPAR"} (Correcto)" to "confirm",
                            "Cambiar a ${if(state.isEvenWeek) "IMPAR" else "PAR"}" to "toggle_parity",
                            "Esta semana no hubo clases" to "suspend_week"
                        )
                    )
                }
            }
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun startNewSemester() {
        viewModelScope.launch {
            val currentSubjects = repository.subjects.first()
            val allAssessments = repository.assessments.first()
            
            // Block A: Capture date range
            val startDate = _semesterStartDate.value ?: System.currentTimeMillis()
            val endDate = System.currentTimeMillis()
            
            // Block B: Gather data
            val sdf = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
            // Note: date formats in our app are typically "dd MMM" or "dd MMM yyyy"
            
            val allLogs = repository.attendanceLogs.first() // We'll just take all logs as approximation if they don't have year, but better to check if they belong to active subjects.
            val activeSubjectIds = currentSubjects.map { it.id }
            val logs = allLogs.filter { activeSubjectIds.contains(it.subjectId) }
            
            val totalLogs = logs.size
            val presentLogs = logs.count { it.isPresent }
            val attendancePercentage = if (totalLogs > 0) (presentLogs.toDouble() / totalLogs) * 100 else 100.0
            
            val subjectAbsences = logs.filter { !it.isPresent }.groupBy { it.subjectId }.mapValues { it.value.size }
            val mostAbsencesSubjectId = subjectAbsences.maxByOrNull { it.value }?.key
            val mostAbsencesSubjectName = currentSubjects.find { it.id == mostAbsencesSubjectId }?.name
            
            // Focus sessions
            val focusSessions = _focusSessionsHistoryJson.value.parseSessionsHistory() // We need to parse json
            // Try to filter focus sessions loosely
            // For now, we'll just take all that we haven't cleared.
            val focusHoursTotal = focusSessions.sumOf { it.duration } / 60.0
            val focusSessionsCompleted = focusSessions.count { !it.interrupted }
            val focusSessionsInterrupted = focusSessions.count { it.interrupted }
            
            val timeOfDayGroups = focusSessions.filter { !it.interrupted }.groupBy { it.timeOfDay }
            val mostProductiveTimeOfDay = timeOfDayGroups.maxByOrNull { it.value.sumOf { s -> s.duration } }?.key
            
            val subjectFocusGroups = focusSessions.groupBy { it.label }
            val mostFocusedSubject = subjectFocusGroups.maxByOrNull { it.value.sumOf { s -> s.duration } }?.key
            
            val badges = repository.badges.first()
            val badgesUnlockedCount = badges.count { it.isUnlocked } // Maybe count only ones unlocked during this time, but we don't have clear timestamps for all.
            
            val maxStreak = repository.getSetting("max_streak")?.toIntOrNull() ?: 0
            val averageStreak = maxStreak / 2.0 // Approximation
            
            val trips = repository.tripRecords.first()
            val totalTripDistance = trips.sumOf { it.durationMinutes.toDouble() } // We don't have distance, using duration as proxy
            
            // Tasks completed
            val allTasks = repository.tasks.first()
            val completedTasksCount = allTasks.count { it.isCompleted && activeSubjectIds.contains(it.subjectId) }
            
            var bestSubjectName: String? = null
            var worstSubjectName: String? = null
            var bestGrade = -1.0
            var worstGrade = 101.0
            
            // Archive current subjects into AcademicRecord
            if (currentSubjects.isNotEmpty()) {
                val careerId = repository.getOrCreateCareer(
                    universityName = _userUniversity.value.takeIf { it.isNotBlank() } ?: "UNI",
                    campusName = "Campus Central",
                    careerName = _career.value.takeIf { it.isNotBlank() } ?: "Carrera"
                )
                
                val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR).toString()
                
                for (sub in currentSubjects) {
                    val subAssessments = allAssessments.filter { it.subjectId == sub.id }
                    val finalGrade = if (subAssessments.isNotEmpty()) {
                        val grade = subAssessments.filter { it.grade != null }.sumOf { (it.grade!! / 100.0) * it.percentage }
                        if (grade > bestGrade) { bestGrade = grade; bestSubjectName = sub.name }
                        if (grade < worstGrade) { worstGrade = grade; worstSubjectName = sub.name }
                        grade
                                            } else {
                        0.0
                    }
                    
                    // Match with PensumSubject or create one
                    val pensumSubjects = repository.getPensumForCareer(careerId).first()
                    var matchedPensum = pensumSubjects.find { it.name.equals(sub.name, ignoreCase = true) || it.code.equals(sub.name, ignoreCase = true) }
                    
                    val pensumSubjectId = matchedPensum?.id ?: repository.insertPensumSubject(
                        com.aistudio.unibuddy.qywvsp.data.PensumSubject(
                            careerId = careerId,
                            code = sub.name.take(5).uppercase(),
                            name = sub.name,
                            semester = "Extracurricular",
                            credits = 3.0,
                            isNumbers = false
                        )
                    ).toInt()

                    val status = if (subAssessments.isEmpty()) com.aistudio.unibuddy.qywvsp.data.AssessmentStatus.UNKNOWN
                                 else if (finalGrade >= 60.0) com.aistudio.unibuddy.qywvsp.data.AssessmentStatus.NF_R
                                 else com.aistudio.unibuddy.qywvsp.data.AssessmentStatus.NF_R // NF_R means Regular, grade determines pass/fail
                                 
                    repository.insertAcademicRecord(
                        com.aistudio.unibuddy.qywvsp.data.AcademicRecord(
                            pensumSubjectId = pensumSubjectId,
                            grade = finalGrade,
                            status = status,
                            year = currentYear,
                            academicGroup = "A",
                            professorId = null,
                            rating = null
                        )
                    )
                }
            }

            // Generate Recap highlight
            val highlightText = if (attendancePercentage > 80.0 && focusHoursTotal > 10.0) {
                "¡Increíble dedicación! Tuviste gran asistencia y mucho tiempo de enfoque."
            } else if (attendancePercentage > 90.0) {
                "¡Asistencia casi perfecta! Eres muy constante."
            } else if (focusHoursTotal > 20.0) {
                "¡Máquina de concentrarte! Tu enfoque fue tu punto más fuerte."
            } else {
                "¡Semestre completado! Sigue esforzándote para mejorar."
            }
            
            // Calculate best mood (Approximation based on attendance)
            // Just some placeholder logic since we don't have exact historical daily mood for the whole semester easily
            val bestMoodDaysCount = presentLogs
            val worriedMoodDaysCount = totalLogs - presentLogs
            val bestDayOfWeek = "Miércoles" // Placeholder

            val recap = com.aistudio.unibuddy.qywvsp.data.SeasonRecap(
                startDate = startDate,
                endDate = endDate,
                attendancePercentage = attendancePercentage,
                focusHoursTotal = focusHoursTotal,
                focusSessionsCompleted = focusSessionsCompleted,
                focusSessionsInterrupted = focusSessionsInterrupted,
                bestMoodDaysCount = bestMoodDaysCount,
                worriedMoodDaysCount = worriedMoodDaysCount,
                maxStreak = maxStreak,
                badgesUnlockedCount = badgesUnlockedCount,
                bestSubjectName = bestSubjectName,
                worstSubjectName = worstSubjectName,
                highlightText = highlightText,
                subjectWithMostAbsences = mostAbsencesSubjectName,
                mostProductiveTimeOfDay = mostProductiveTimeOfDay,
                mostFocusedSubject = mostFocusedSubject,
                bestDayOfWeek = bestDayOfWeek,
                totalTripDistance = totalTripDistance,
                completedTasksCount = completedTasksCount,
                averageStreak = averageStreak
            )
            
            val recapId = repository.insertSeasonRecap(recap)
            
            // Trigger UI
            _showSeasonRecap.value = recap

            // Clear focus sessions so they don't roll over
            _focusSessionsHistoryJson.value = "[]"
            repository.saveSetting("focus_sessions_history", "[]")

            // Delete old subjects (Assessments, Tasks, Absences cascade. AttendanceLogs are safe)
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
    
    fun checkFailedByAbsences(): Flow<List<String>> = flow {
        val currentSubjects = repository.subjects.first()
        val allAssessments = repository.assessments.first()
        val failed = mutableListOf<String>()

        for (sub in currentSubjects) {
            val maxAbs = sub.totalClasses - Math.ceil(sub.totalClasses * (sub.requiredAttendancePercent / 100.0)).toInt()
            val currentAbs = repository.getAbsencesForSubject(sub.id).first().size
            if (currentAbs > maxAbs) {
                failed.add(sub.name)
            }
        }
        emit(failed)
    }

    fun endSemester(forcePassAbsences: Boolean = false) {
        viewModelScope.launch {
            val currentSubjects = repository.subjects.first()
            val allAssessments = repository.assessments.first()
            val passed = _passedSubjects.value.toMutableSet()
            
            for (sub in currentSubjects) {
                val subAssessments = allAssessments.filter { it.subjectId == sub.id }
                val currentPercentage = subAssessments.sumOf { it.percentage }
                val currentWeighted = subAssessments.sumOf { it.grade ?: 0.0 }
                
                // Calculate absences limit
                val maxAbs = sub.totalClasses - Math.ceil(sub.totalClasses * (sub.requiredAttendancePercent / 100.0)).toInt()
                val currentAbs = repository.getAbsencesForSubject(sub.id).first().size
                val failedByAbsences = !forcePassAbsences && (currentAbs > maxAbs)

                // If they passed (>=60.0 points in Nicaragua), or if no exams were registered (assume passed) AND they didn't fail by absences
                if (!failedByAbsences && (currentPercentage == 0.0 || currentWeighted >= 60.0)) {
                    val curriculumMatch = com.aistudio.unibuddy.qywvsp.data.CurriculumData.getSubjectsFor(_userUniversity.value, _career.value).find { 
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

    private fun setWeeklyStreak(newValue: Int) {
        viewModelScope.launch {
            _weeklyStreak.value = newValue.coerceAtLeast(0)
            repository.saveSetting("weekly_streak_count", _weeklyStreak.value.toString())
            
            if (_weeklyStreak.value >= 5) {
                unlockBadge("Estudiante Responsable")
            }
        }
    }
    private val _weeklyStreak = MutableStateFlow(0)
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

    private val _buddyColor = MutableStateFlow("#0F172A") // Default Navy Blue
    val buddyColor: StateFlow<String> = _buddyColor.asStateFlow()

    private val _career = MutableStateFlow("Ingeniería Industrial")
    val career: StateFlow<String> = _career.asStateFlow()

    private val _profilePhotoUri = MutableStateFlow<String?>(null)
    val profilePhotoUri: StateFlow<String?> = _profilePhotoUri.asStateFlow()

    private val _showRouteSettingsRequested = MutableStateFlow(false)
    val showRouteSettingsRequested: StateFlow<Boolean> = _showRouteSettingsRequested.asStateFlow()

    fun requestRouteSettings(requested: Boolean) {
        _showRouteSettingsRequested.value = requested
    }

    private val _buddyPose = MutableStateFlow("idle")
    val buddyPose: StateFlow<String> = _buddyPose.asStateFlow()
    
    private val _buddyXp = MutableStateFlow(0)
    val buddyXp: StateFlow<Int> = _buddyXp.asStateFlow()

    private val _subjectImportanceMap = MutableStateFlow<Map<Int, String>>(emptyMap())
    val subjectImportanceMap: StateFlow<Map<Int, String>> = _subjectImportanceMap.asStateFlow()

    private val _focusObjectivesJson = MutableStateFlow("")
    val focusObjectivesJson: StateFlow<String> = _focusObjectivesJson.asStateFlow()

    private val _focusSessionsHistoryJson = MutableStateFlow("")
    val focusSessionsHistoryJson: StateFlow<String> = _focusSessionsHistoryJson.asStateFlow()
    
    private val _customHolidays = MutableStateFlow<List<Long>>(emptyList())
    val customHolidays: StateFlow<List<Long>> = _customHolidays.asStateFlow()
    
    private val _cancelledClasses = MutableStateFlow<Set<String>>(emptySet())
    val cancelledClasses: StateFlow<Set<String>> = _cancelledClasses.asStateFlow()

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

    private val _currentDistanceToCollege = MutableStateFlow<Double?>(null)
    val currentDistanceToCollege: StateFlow<Double?> = _currentDistanceToCollege.asStateFlow()

    private val _locationBasedTravelMinutes = MutableStateFlow(30)
    val locationBasedTravelMinutes: StateFlow<Int> = _locationBasedTravelMinutes.asStateFlow()
    
    private val _isOutOfRange = MutableStateFlow(false)
    val isOutOfRange: StateFlow<Boolean> = _isOutOfRange.asStateFlow()

    private val _currentLocationName = MutableStateFlow("Buscando...")
    val currentLocationName: StateFlow<String> = _currentLocationName.asStateFlow()
    
    private val _currentLat = MutableStateFlow<Double?>(null)
    val currentLat: StateFlow<Double?> = _currentLat.asStateFlow()
    
    private val _currentLon = MutableStateFlow<Double?>(null)
    val currentLon: StateFlow<Double?> = _currentLon.asStateFlow()

    fun addCustomHoliday(dateMs: Long) {
        val current = _customHolidays.value.toMutableList()
        current.add(dateMs)
        _customHolidays.value = current
        viewModelScope.launch {
            repository.saveSetting("custom_holidays", current.joinToString(","))
        }
    }

    fun removeCustomHoliday(dateMs: Long) {
        val current = _customHolidays.value.toMutableList()
        current.remove(dateMs)
        _customHolidays.value = current
        viewModelScope.launch {
            repository.saveSetting("custom_holidays", current.joinToString(","))
        }
    }

    fun isNicaraguaHoliday(dateMs: Long): Boolean {
        val cal = Calendar.getInstance()
        cal.timeInMillis = dateMs
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1 // 1-12
        val year = cal.get(Calendar.YEAR)
        
        // Fixed holidays
        if (day == 1 && month == 1) return true // Año Nuevo
        if (day == 1 && month == 5) return true // Día del Trabajo
        if (day == 19 && month == 7) return true // Revolución
        if (day == 14 && month == 9) return true // San Jacinto
        if (day == 15 && month == 9) return true // Independencia
        if (day == 8 && month == 12) return true // Inmaculada Concepción
        if (day == 25 && month == 12) return true // Navidad
        
        // Check custom holidays
        for (customMs in _customHolidays.value) {
            val customCal = Calendar.getInstance().apply { timeInMillis = customMs }
            if (customCal.get(Calendar.DAY_OF_MONTH) == day && customCal.get(Calendar.MONTH) + 1 == month && customCal.get(Calendar.YEAR) == year) {
                return true
            }
        }
        
        return false
    }

    fun updateLocationStatus(available: Boolean, name: String = "", lat: Double? = null, lon: Double? = null) {
        _isLocationAvailable.value = available
        if (name.isNotEmpty()) _currentLocationName.value = name
        
        if (available && lat != null && lon != null) {
            _currentLat.value = lat
            _currentLon.value = lon
            viewModelScope.launch(Dispatchers.IO) {
                // Determine saved destination lat/lon
                val savedLatStr = repository.getSetting("destination_lat")
                val savedLonStr = repository.getSetting("destination_lon")
                
                var targetLat = savedLatStr?.toDoubleOrNull()
                var targetLon = savedLonStr?.toDoubleOrNull()
                
                // If no saved setting, try to get from selected university campus
                if (targetLat == null || targetLon == null) {
                    val uniCoords = getSelectedUniversityCoords()
                    targetLat = uniCoords?.first
                    targetLon = uniCoords?.second
                }
                
                if (targetLat != null && targetLon != null) {
                    val distance = calculateDistance(lat, lon, targetLat!!, targetLon!!)
                    _currentDistanceToCollege.value = distance
                    
                    // If within 1.5 km (accommodating GPS errors), consider it "En la universidad"
                    if (distance <= 1.5) {
                        _currentLocationName.value = "En la universidad"
                        
                        if (_autoCheckinEnabled.value && !isNicaraguaHoliday(System.currentTimeMillis())) {
                            autoCheckinToCurrentClass()
                        }
                    }
                    
                    // Check if user is out of range (>100km)
                    val outOfRange = distance > 50.0
                    _isOutOfRange.value = outOfRange
                    
                    // Estimate travel time: 3.5 mins per km + 12 mins buffer.
                    val mins = if (outOfRange) 0 else (distance * 3.5).toInt() + 12
                    _locationBasedTravelMinutes.value = mins
                    
                    // Save to Room for widgets
                    viewModelScope.launch {
                        repository.saveSetting("widget_out_of_range", outOfRange.toString())
                        repository.saveSetting("widget_travel_time", mins.toString())
                    }
                } else {
                    _currentDistanceToCollege.value = null
                    _locationBasedTravelMinutes.value = _baseTravelTime.value
                }
                
                checkAndTriggerProactiveNotifications()
            }
        } else {
            _currentDistanceToCollege.value = null
            _locationBasedTravelMinutes.value = _baseTravelTime.value
        }
    }

    private suspend fun autoCheckinToCurrentClass() {
        val currentSubjects = repository.subjects.first()
        val todayStrShort = SimpleDateFormat("Lu", Locale("es", "ES")).format(Date()) // need proper mapping
        val todayCalendar = Calendar.getInstance()
        val dayOfWeek = todayCalendar.get(Calendar.DAY_OF_WEEK)
        val dayCode = when (dayOfWeek) {
            Calendar.MONDAY -> "Lu"
            Calendar.TUESDAY -> "Ma"
            Calendar.WEDNESDAY -> "Mi"
            Calendar.THURSDAY -> "Ju"
            Calendar.FRIDAY -> "Vi"
            Calendar.SATURDAY -> "Sá"
            else -> "Do"
        }
        
        val hour = todayCalendar.get(Calendar.HOUR_OF_DAY)
        val min = todayCalendar.get(Calendar.MINUTE)
        val currentTotalMin = hour * 60 + min
        
        val todayStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())
        
        for (sub in currentSubjects) {
            val sessionsForToday = sub.sessions.filter { it.day == dayCode }
            for (session in sessionsForToday) {
                try {
                    val range = com.aistudio.unibuddy.qywvsp.ui.parseTimeRange(session.time)
                    if (range != null) {
                        val startHour = range.first.first
                        val startMin = range.first.second
                        val endHour = range.second.first
                        val endMin = range.second.second
                        val startTotalMin = startHour * 60 + startMin
                        val endTotalMin = endHour * 60 + endMin
                        
                        // If within class time (-15 mins to end time)
                        if (currentTotalMin in (startTotalMin - 15)..endTotalMin) {
                            val isCancelled = _cancelledClasses.value.contains("${sub.id}_$todayStr")
                            if (!isCancelled) {
                                val existingLogs = repository.attendanceLogs.first().filter { it.subjectId == sub.id && it.date.startsWith(todayStr) }
                                if (existingLogs.isEmpty()) {
                                    repository.insertAttendanceLog(com.aistudio.unibuddy.qywvsp.data.AttendanceLog(subjectId = sub.id, isPresent = true, date = "$todayStr (Automático)"))
                                }
                                
                                // Auto Silence
                                if (_smartSilenceEnabled.value) {
                                    try {
                                        val audioManager = getApplication<Application>().getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
                                        val notificationManager = getApplication<Application>().getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                                        if (notificationManager.isNotificationPolicyAccessGranted) {
                                            audioManager.ringerMode = android.media.AudioManager.RINGER_MODE_VIBRATE
                                        }
                                    } catch(e: Exception) {}
                                }
                            }
                        }
                    }
                } catch (e: Exception) {}
            }
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    fun getSelectedUniversityCoords(): Pair<Double, Double>? {
        val uni = universities.find { it.name == _destination.value }
        val campus = uni?.campuses?.firstOrNull()
        return if (campus != null) Pair(campus.lat, campus.lng) else null
    }

    fun getSystemLog(): String {
        val totalSubjects = subjects.value.size
        val totalAssessments = assessments.value.size
        val totalAbsences = absences.value.size
        return """
            UniBuddy System Log
            Version: 1.5 (Build 6)
            Time: ${Calendar.getInstance().time}
            Stats: Subjects($totalSubjects), Assessments($totalAssessments), Absences($totalAbsences)
            Location Available: ${_isLocationAvailable.value}
            Distance to Target: ${_currentDistanceToCollege.value ?: "Unknown"} km
            Weather: ${_weatherDescription.value}
        """.trimIndent()
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
            if (completed && _semesterStartDate.value == null) {
                val now = System.currentTimeMillis()
                _semesterStartDate.value = now
                repository.saveSetting("semester_start_date", now.toString())
            }
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

    fun saveProfilePhoto(uri: String) {
        viewModelScope.launch {
            _profilePhotoUri.value = uri
            repository.saveSetting("profile_photo_uri", uri)
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

    fun addBuddyXp(xp: Int) {
        viewModelScope.launch {
            val newXp = _buddyXp.value + xp
            _buddyXp.value = newXp
            repository.saveSetting("buddy_xp", newXp.toString())
            notifyWidgets()
        }
    }

    fun toggleCancelledClass(subjectId: Int, dateStr: String) {
        viewModelScope.launch {
            val key = "${subjectId}_${dateStr}"
            val current = _cancelledClasses.value.toMutableSet()
            if (current.contains(key)) {
                current.remove(key)
            } else {
                current.add(key)
            }
            _cancelledClasses.value = current
            repository.saveSetting("cancelled_classes", current.joinToString(","))
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
            
            val staticSubjects = com.aistudio.unibuddy.qywvsp.data.CurriculumData.getSubjectsFor(
                _userUniversity.value ?: "UNI",
                _career.value ?: "Ing. Industrial"
            )

            val passingGrade = if (_userUniversity.value == "UAM" || _userUniversity.value == "UCA" || _userUniversity.value == "Keiser") 70.0 else 60.0
            val currentPassed = _passedSubjects.value.toMutableSet()

            subjects.forEach { parsed ->
                // Try matching with static curriculum
                val matchedStatic = staticSubjects.find {
                    it.code.equals(parsed.code, ignoreCase = true) ||
                    it.name.equals(parsed.name, ignoreCase = true)
                }

                val subjectCode = matchedStatic?.code ?: parsed.code
                val subjectName = matchedStatic?.name ?: parsed.name
                val subjectSemId = matchedStatic?.let { "Semestre ${it.semester}" } ?: "S/D"

                // Check and mark as passed if grade is passing
                val isPassing = parsed.grade >= passingGrade
                if (isPassing) {
                    currentPassed.add(subjectCode)
                    matchedStatic?.let { currentPassed.add(it.code) }
                }

                // Construct a beautiful, readable academic term string for the year column
                val formattedSem = when (parsed.semester.uppercase()) {
                    "PRIMER" -> "1er Semestre"
                    "SEGUNDO" -> "2do Semestre"
                    "TERCER" -> "3er Semestre"
                    "CUARTO" -> "4to Semestre"
                    "QUINTO" -> "5to Semestre"
                    "SEXTO" -> "6to Semestre"
                    "SEPTIMO", "SÉPTIMO" -> "7mo Semestre"
                    "OCTAVO" -> "8vo Semestre"
                    "NOVENO" -> "9no Semestre"
                    "DECIMO", "DÉCIMO" -> "10mo Semestre"
                    "VERANO" -> "Curso de Verano"
                    else -> parsed.semester
                }
                val academicTerm = if (formattedSem.isNotEmpty() && formattedSem != "S/D") {
                    "$formattedSem ${parsed.year}"
                } else {
                    "Sin Semestre"
                }

                val subjectId = repository.insertPensumSubject(
                    PensumSubject(
                        careerId = careerId,
                        code = subjectCode,
                        name = subjectName,
                        semester = subjectSemId,
                        credits = parsed.credits,
                        isNumbers = false
                    )
                )
                var profId: Int? = null
                if (parsed.professorName.isNotBlank()) {
                    profId = repository.insertProfessor(
                        com.aistudio.unibuddy.qywvsp.data.Professor(
                            name = parsed.professorName.trim(),
                            avatarSeed = parsed.professorName.trim().take(2).uppercase()
                        )
                    ).toInt()
                }

                repository.insertAcademicRecord(
                    AcademicRecord(
                        pensumSubjectId = subjectId.toInt(),
                        grade = parsed.grade,
                        status = parsed.status,
                        year = academicTerm,
                        academicGroup = parsed.group,
                        professorId = profId,
                        rating = null
                    )
                )
            }

            _passedSubjects.value = currentPassed
            repository.saveSetting("passed_subjects", currentPassed.joinToString(","))
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

    fun addDummyProfessor() {
        viewModelScope.launch {
            repository.insertProfessor(com.aistudio.unibuddy.qywvsp.data.Professor(name = "Nuevo Profesor", avatarSeed = "NP"))
        }
    }

    fun addSubject(name: String, schedule: String, sessions: List<com.aistudio.unibuddy.qywvsp.data.ClassSessionDetails>, requiredAttendancePercent: Int, totalClasses: Int, colorHex: String = "#FF4CAF50") {
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
            val examP = _defaultExamPercentage.value
            val testP = _defaultTestPercentage.value
            // Corte 1 (50)
            repository.insertAssessment(Assessment(subjectId = subjectId, name = "C1: Examen", grade = null, percentage = examP))
            for (i in 1..5) {
                repository.insertAssessment(Assessment(subjectId = subjectId, name = "C1: Tarea $i", grade = null, percentage = testP))
            }
            // Corte 2 (50)
            repository.insertAssessment(Assessment(subjectId = subjectId, name = "C2: Examen", grade = null, percentage = examP))
            for (i in 1..5) {
                repository.insertAssessment(Assessment(subjectId = subjectId, name = "C2: Tarea $i", grade = null, percentage = testP))
            }
                notifyWidgets()
}
    }

    fun updateSubject(subject: Subject) {
        viewModelScope.launch {
            repository.updateSubject(subject)
                notifyWidgets()
}
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            // Manually delete dependent records to prevent any orphaning
            val allTasks = repository.tasks.first().filter { it.subjectId == subject.id }
            val allAssessments = repository.assessments.first().filter { it.subjectId == subject.id }
            val allAbsences = repository.getAbsencesForSubject(subject.id).first()
            
            allTasks.forEach { repository.deleteTaskById(it.id) }
            allAssessments.forEach { repository.deleteAssessmentById(it.id) }
            allAbsences.forEach { repository.deleteAbsenceById(it.id) }
            
            repository.deleteSubject(subject)
                notifyWidgets()
}
    }

    fun registerAbsence(subjectId: Int, dateStr: String? = null) {
        viewModelScope.launch {
            val date = dateStr ?: SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())
            repository.insertAbsence(Absence(subjectId = subjectId, date = date))
            // Increment streak or update list
            setWeeklyStreak(_weeklyStreak.value + 1)
                notifyWidgets()
}
    }

    fun deleteAbsence(absenceId: Int) {
        viewModelScope.launch {
            repository.deleteAbsenceById(absenceId)
            notifyWidgets()
            if (_weeklyStreak.value > 0) {
                setWeeklyStreak(_weeklyStreak.value - 1)
            }
                notifyWidgets()
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
            val existing = repository.getAssessmentsForSubject(subjectId).first()
            val currentSum = existing.sumOf { it.percentage }
            if (currentSum + percentage > 100.0) {
                _snackbarEvent.emit("La suma total de evaluaciones no puede superar el 100% (Actual: $currentSum%, Nuevo: ${currentSum + percentage}%)")
                return@launch
            }
            repository.insertAssessment(Assessment(subjectId = subjectId, name = name, grade = grade, percentage = percentage, examDate = examDate))
            notifyWidgets()
                notifyWidgets()
}
    }

    fun deleteAssessment(assessmentId: Int) {
        viewModelScope.launch {
            repository.deleteAssessmentById(assessmentId)
            notifyWidgets()
                notifyWidgets()
}
    }

    fun addTask(subjectId: Int, title: String, type: String, dueDate: String) {
        viewModelScope.launch {
            repository.insertTask(Task(subjectId = subjectId, title = title, type = type, dueDate = dueDate))
                notifyWidgets()
}
    }

    private val _buddyCelebrationEvent = kotlinx.coroutines.flow.MutableSharedFlow<Unit>()
    val buddyCelebrationEvent: kotlinx.coroutines.flow.SharedFlow<Unit> = _buddyCelebrationEvent.asSharedFlow()

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            val newIsCompleted = !task.isCompleted
            repository.updateTask(task.copy(isCompleted = newIsCompleted))
            if (newIsCompleted) {
                _buddyCelebrationEvent.emit(Unit)
            }
            notifyWidgets()
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            repository.deleteTaskById(taskId)
                notifyWidgets()
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
            if (isPresent) {
                _buddyCelebrationEvent.emit(Unit)
            }

            // For backwards compatibility with standard dashboard cards:
            if (!isPresent) {
                repository.insertAbsence(Absence(subjectId = subjectId, date = date))
            } else {
                setWeeklyStreak(_weeklyStreak.value + 1)
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
                notifyWidgets()
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
                        setWeeklyStreak(_weeklyStreak.value - 1)
                    }
                }
            }
            lastInsertedLogId = null
                notifyWidgets()
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
                        setWeeklyStreak(_weeklyStreak.value - 1)
                    }
                }
            }
                notifyWidgets()
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
            setWeeklyStreak(0)
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
                    saveBuddyCustomization(s.optString("buddy_accessory", "none"), s.optString("buddy_color", "#0F172A"))
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
                            colorHex = subObj.optString("colorHex", "#FF4CAF50")
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
    fun checkpointDatabase() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                repository.db.query(androidx.sqlite.db.SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL)"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    suspend fun exportDatabaseToUri(context: android.content.Context, uri: android.net.Uri): Boolean {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                repository.db.query(androidx.sqlite.db.SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL)"))
                val dbFile = context.getDatabasePath("unibuddy_database")
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    dbFile.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun importDatabaseFromUri(context: android.content.Context, uri: android.net.Uri): Boolean {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val dbFile = context.getDatabasePath("unibuddy_database")
                val walFile = context.getDatabasePath("unibuddy_database-wal")
                val shmFile = context.getDatabasePath("unibuddy_database-shm")
                
                // Close the DB to allow replacement
                repository.db.close()
                
                context.contentResolver.openInputStream(uri)?.use { input ->
                    dbFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                // Delete wal and shm since we replaced the main db file
                if (walFile.exists()) walFile.delete()
                if (shmFile.exists()) shmFile.delete()
                
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
class UniBuddyViewModelFactory(private val application: android.app.Application) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UniBuddyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UniBuddyViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


