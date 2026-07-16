package com.aistudio.unibuddy.qywvsp.ui.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object WidgetUpdater {
    fun updateAllWidgets(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                TamagotchiWidget().updateAll(context)
                GradesWidget().updateAll(context)
                AcademicWeatherWidget().updateAll(context)
                RPGProgressWidget().updateAll(context)
                NextClassHeroWidget().updateAll(context)
                StreakTrackerWidget().updateAll(context)
                UpcomingAssessmentsWidget().updateAll(context)
                QuickCommuteWidget().updateAll(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
