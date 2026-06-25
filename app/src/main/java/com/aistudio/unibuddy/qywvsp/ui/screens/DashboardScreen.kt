package com.aistudio.unibuddy.qywvsp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aistudio.unibuddy.qywvsp.ui.*
import com.aistudio.unibuddy.qywvsp.ui.components.*
import java.util.Calendar

@Composable
fun DashboardScreen(
    viewModel: UniBuddyViewModel,
    onNavigateToDetails: (Int) -> Unit,
    onNavigateToGrades: (Int) -> Unit,
    onConfigureRoute: () -> Unit,
    onNavigateToFocus: () -> Unit
) {
    val username by viewModel.username.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val absences by viewModel.absences.collectAsStateWithLifecycle()
    val isRaining by viewModel.isRaining.collectAsStateWithLifecycle()
    val buddyAccessory by viewModel.buddyAccessory.collectAsStateWithLifecycle()
    val buddyColorStr by viewModel.buddyColor.collectAsStateWithLifecycle()
    val baseTravelTime by viewModel.baseTravelTime.collectAsStateWithLifecycle()
    val tripRecords by viewModel.tripRecords.collectAsStateWithLifecycle()
    val arrivalMarginPref by viewModel.arrivalMarginPreference.collectAsStateWithLifecycle()
    val attendanceLogs by viewModel.attendanceLogs.collectAsStateWithLifecycle()
    val assessments by viewModel.assessments.collectAsStateWithLifecycle()
    val subjectImportanceMap by viewModel.subjectImportanceMap.collectAsStateWithLifecycle()

    val todayExam = remember(assessments) {
        val calendar = Calendar.getInstance()
        val currentDayCode = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Lu"
            Calendar.TUESDAY -> "Ma"
            Calendar.WEDNESDAY -> "Mi"
            Calendar.THURSDAY -> "Ju"
            Calendar.FRIDAY -> "Vi"
            Calendar.SATURDAY -> "Sá"
            else -> "Do"
        }
        assessments.firstOrNull { it.grade == null && it.examDate.trim().equals(currentDayCode, ignoreCase = true) }
    }

    val currentDayCode = when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Lu"
        Calendar.TUESDAY -> "Ma"
        Calendar.WEDNESDAY -> "Mi"
        Calendar.THURSDAY -> "Ju"
        Calendar.FRIDAY -> "Vi"
        Calendar.SATURDAY -> "Sá"
        else -> "Do"
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeroNextClassCard(
            subject = subjects.firstOrNull(),
            classTime = "10:00 AM", 
            faltasRestantes = 3,
            isExamMode = todayExam != null
        )

        WellnessWidget(
            upcomingExamsCount = assessments.count { it.grade == null },
            absencesCount = absences.size,
            calculatedStress = 50f,
            statusText = "Estable"
        )

        TodayClassesListWidget(
            subjects = subjects,
            currentDayCode = currentDayCode
        )

        GPSAndStopwatchWidget(
            currentDistanceToCollege = null,
            locationBasedTravelMinutes = 0,
            baseTravelTimeSource = baseTravelTime,
            isTripActive = false,
            tripElapsedSeconds = 0,
            onRequestGPS = {},
            onStartTrip = {},
            onEndTrip = {}
        )

        GradesHistoryWidget(
            assessments = assessments,
            currentWeighted = 0.0
        )
    }
}
