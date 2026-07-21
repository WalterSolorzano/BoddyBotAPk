package com.aistudio.unibuddy.qywvsp.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShareHelper {
    fun shareBitmap(context: Context, bitmap: Bitmap) {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val stream = FileOutputStream(cachePath.absolutePath + "/recap.png")
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val imagePath = File(context.cacheDir, "images")
        val newFile = File(imagePath, "recap.png")
        val contentUri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", newFile)

        if (contentUri != null) {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            shareIntent.setDataAndType(contentUri, context.contentResolver.getType(contentUri))
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
            context.startActivity(Intent.createChooser(shareIntent, "Compartir Season Recap"))
        }
    }

    fun shareAcademicHistoryPdf(
        context: Context,
        subjects: List<com.aistudio.unibuddy.qywvsp.data.Subject>,
        assessments: List<com.aistudio.unibuddy.qywvsp.data.Assessment>,
        attendanceLogs: List<com.aistudio.unibuddy.qywvsp.data.AttendanceLog>
    ) {
        val pdfDocument = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Paints
        val paintTitle = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#0B1B3D") // NavyBlue
            textSize = 14f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val paintSubtitle = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#5A6B82") // SlateGray
            textSize = 10f
            isAntiAlias = true
        }

        val paintHeaderBg = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#0B1B3D")
            style = android.graphics.Paint.Style.FILL
        }

        val paintHeaderTextColor = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 20f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val paintBodyText = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#0B1B3D")
            textSize = 10f
            isAntiAlias = true
        }

        val paintBodyTextBold = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#0B1B3D")
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val paintLine = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#E0E4EC")
            strokeWidth = 1f
            style = android.graphics.Paint.Style.STROKE
        }

        // Draw header
        canvas.drawRect(0f, 0f, 595f, 90f, paintHeaderBg)
        canvas.drawText("UniBuddy - Reporte Académico", 30f, 45f, paintHeaderTextColor)
        
        paintHeaderTextColor.textSize = 11f
        paintHeaderTextColor.isFakeBoldText = false
        canvas.drawText("Historial de materias, calificaciones y asistencias", 30f, 68f, paintHeaderTextColor)

        var y = 120f

        // Stats summary block
        val totalSubjects = subjects.size
        val presentCount = attendanceLogs.count { it.isPresent }
        val totalClasses = attendanceLogs.size
        val attendanceRate = if (totalClasses > 0) (presentCount.toFloat() / totalClasses.toFloat() * 100f).toInt() else 100

        val gradedAssessments = assessments.filter { it.grade != null }
        val avgGrade = if (gradedAssessments.isNotEmpty()) gradedAssessments.map { it.grade ?: 0.0 }.average() else 0.0

        canvas.drawText("Resumen General:", 30f, y, paintTitle)
        y += 20f
        canvas.drawText("Total de Materias: $totalSubjects", 40f, y, paintBodyText)
        canvas.drawText("Promedio de Calificaciones: ${String.format("%.2f", avgGrade)}", 200f, y, paintBodyText)
        canvas.drawText("Asistencia Promedio: $attendanceRate%", 420f, y, paintBodyText)

        y += 30f
        canvas.drawLine(30f, y, 565f, y, paintLine)
        y += 20f

        // Table Header
        canvas.drawText("Materia", 35f, y, paintBodyTextBold)
        canvas.drawText("Calificación", 280f, y, paintBodyTextBold)
        canvas.drawText("Asistencia", 380f, y, paintBodyTextBold)
        canvas.drawText("Estado", 480f, y, paintBodyTextBold)
        y += 10f
        canvas.drawLine(30f, y, 565f, y, paintLine)
        y += 20f

        // Table Body
        subjects.forEach { subject ->
            canvas.drawText(subject.name, 35f, y, paintBodyTextBold)

            // Calculate subject average
            val subAssessments = assessments.filter { it.subjectId == subject.id && it.grade != null }
            val subAverage = if (subAssessments.isNotEmpty()) subAssessments.map { it.grade ?: 0.0 }.average() else null
            val averageStr = if (subAverage != null) String.format("%.2f", subAverage) else "Sin notas"
            canvas.drawText(averageStr, 280f, y, paintBodyText)

            // Calculate subject attendance
            val subAttendance = attendanceLogs.filter { it.subjectId == subject.id && !it.isCancelled }
            val subPresent = subAttendance.count { it.isPresent }
            val subTotal = subAttendance.size
            val subRate = if (subTotal > 0) (subPresent.toFloat() / subTotal.toFloat() * 100f).toInt() else 100
            canvas.drawText("$subRate% ($subPresent/$subTotal)", 380f, y, paintBodyText)

            // Status based on limits
            val isAttendanceLow = subRate < subject.requiredAttendancePercent
            val statusStr = if (isAttendanceLow) "Riesgo Faltas" else if (subAverage != null && subAverage < 51.0) "Reprobando" else "Aprobando"
            canvas.drawText(statusStr, 480f, y, paintBodyText)

            y += 20f
            canvas.drawLine(30f, y, 565f, y, paintLine)
            y += 20f
        }

        // Add a footer note
        canvas.drawText("Generado por UniBuddy - Tu Compañero Académico", 30f, 810f, paintSubtitle)

        pdfDocument.finishPage(page)

        // Write the PDF file to cache
        val cachePath = File(context.cacheDir, "documents")
        cachePath.mkdirs()
        val pdfFile = File(cachePath, "historial_academico.pdf")
        val stream = java.io.FileOutputStream(pdfFile)
        pdfDocument.writeTo(stream)
        stream.close()
        pdfDocument.close()

        val contentUri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(contentUri, "application/pdf")
            putExtra(Intent.EXTRA_STREAM, contentUri)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Exportar Historial Académico"))
    }
}
