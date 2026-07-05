package com.aistudio.unibuddy.qywvsp.ui.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.aistudio.unibuddy.qywvsp.MainActivity
import com.aistudio.unibuddy.qywvsp.data.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import kotlin.math.roundToInt

class GradesWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = AppDatabase.getDatabase(context)
        val subjectsFlow = database.subjectDao().getAllSubjects()
        val assessmentsFlow = database.assessmentDao().getAllAssessments()
        
        val summaryList = combine(subjectsFlow, assessmentsFlow) { subjects, assessments ->
            subjects.map { subject ->
                val subjectAssessments = assessments.filter { it.subjectId == subject.id }
                val currentWeighted = subjectAssessments.filter { it.grade != null }
                    .sumOf { (it.grade ?: 0.0) * it.percentage / 100.0 }
                SubjectSummary(
                    id = subject.id,
                    name = subject.name,
                    weightedGrade = currentWeighted
                )
            }
        }.first()

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A)) // Sleek premium dark background
                    .cornerRadius(16.dp) // Beautiful rounded corners for the widget
                    .padding(14.dp)
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Calificaciones",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Text(
                    text = "Promedios Ponderados de Materias",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF94A3B8)), // Soft slate gray
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = GlanceModifier.padding(bottom = 8.dp)
                )

                if (summaryList.isEmpty()) {
                    Box(
                        modifier = GlanceModifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay materias registradas",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF64748B)),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                } else {
                    LazyColumn {
                        items(summaryList) { summary ->
                            SubjectRow(summary)
                        }
                    }
                }
            }
        }
    }
}

data class SubjectSummary(
    val id: Int,
    val name: String,
    val weightedGrade: Double
)

@androidx.compose.runtime.Composable
fun SubjectRow(summary: SubjectSummary) {
    val progressColor = if (summary.weightedGrade >= 51.0) Color(0xFF10B981) else if (summary.weightedGrade >= 30.0) Color(0xFFF59E0B) else Color(0xFFEF4444)
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFF1E293B)) // Premium Card background container
            .cornerRadius(12.dp) // Beautiful rounded corners for each subject card
            .padding(10.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = summary.name,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.height(6.dp))
            androidx.glance.appwidget.LinearProgressIndicator(
                progress = (summary.weightedGrade / 100.0).toFloat().coerceIn(0f, 1f),
                modifier = GlanceModifier.fillMaxWidth().height(6.dp).cornerRadius(3.dp),
                color = ColorProvider(progressColor),
                backgroundColor = ColorProvider(Color(0xFF334155))
            )
        }
        Spacer(modifier = GlanceModifier.width(12.dp))
        Column(
            horizontalAlignment = Alignment.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${(summary.weightedGrade * 10).roundToInt() / 10.0}",
                style = TextStyle(
                    color = ColorProvider(progressColor),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = if (summary.weightedGrade >= 51.0) "Aprobado" else "En riesgo",
                style = TextStyle(
                    color = ColorProvider(if (summary.weightedGrade >= 51.0) Color(0xFF34D399) else Color(0xFFF87171)),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier.padding(top = 1.dp)
            )
        }
    }
}
