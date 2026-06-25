package com.aistudio.unibuddy.qywvsp.ui

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.aistudio.unibuddy.qywvsp.data.Subject
import com.aistudio.unibuddy.qywvsp.data.parseSessions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ScheduleExporter {
    suspend fun exportToGallery(context: Context, subjects: List<Subject>, universityName: String): Uri? = withContext(Dispatchers.IO) {
        val width = 1200
        val height = 1800
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Draw a beautiful warm pastel comic background
        val bgPaint = Paint().apply {
            color = Color.parseColor("#F4F7FC")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Cute decorative grid lines (graph paper school style)
        val gridPaint = Paint().apply {
            color = Color.parseColor("#E1EBF7")
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        val step = 80f
        for (x in 0..(width / step.toInt())) {
            canvas.drawLine(x * step, 0f, x * step, height.toFloat(), gridPaint)
        }
        for (y in 0..(height / step.toInt())) {
            canvas.drawLine(0f, y * step, width.toFloat(), y * step, gridPaint)
        }

        // Draw a thick bold navy-blue outline for the cartoon aesthetic
        val borderPaint = Paint().apply {
            color = Color.parseColor("#0F1A24") // Solid Dark Navy
            strokeWidth = 24f
            style = Paint.Style.STROKE
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), borderPaint)

        // 2. Draw Header Area
        val titlePaint = Paint().apply {
            color = Color.parseColor("#0F1A24")
            textSize = 52f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        
        val subtitlePaint = Paint().apply {
            color = Color.parseColor("#0288D1") // Bright blue
            textSize = 32f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        canvas.drawText("MI HORARIO UNIBUDDY", 100f, 150f, titlePaint)
        canvas.drawText(universityName.ifBlank { "Mi Universidad" }.uppercase(), 100f, 212f, subtitlePaint)

        // Comic-style divider line
        val dividerPaint = Paint().apply {
            color = Color.parseColor("#0F1A24")
            strokeWidth = 10f
            style = Paint.Style.FILL_AND_STROKE
        }
        canvas.drawLine(100f, 248f, width - 100f, 248f, dividerPaint)

        // 3. Render Cards for each day
        val days = listOf(
            "Lu" to "LUNES",
            "Ma" to "MARTES",
            "Mi" to "MIÉRCOLES",
            "Ju" to "JUEVES",
            "Vi" to "VIERNES",
            "Sá" to "SÁBADO"
        )
        var currentY = 285f

        val cardBgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        
        val cardOutlinePaint = Paint().apply {
            color = Color.parseColor("#0F1A24")
            strokeWidth = 6f
            style = Paint.Style.STROKE
        }

        val textPaintClass = Paint().apply {
            color = Color.parseColor("#0F1A24")
            textSize = 28f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val textPaintDetails = Paint().apply {
            color = Color.parseColor("#4B5563")
            textSize = 24f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        days.forEach { (code, name) ->
            val daySessions = mutableListOf<Pair<Subject, com.aistudio.unibuddy.qywvsp.data.ClassSessionDetails>>()
            subjects.forEach { sub ->
                sub.sessions.forEach { s ->
                    if (s.day.equals(code, ignoreCase = true)) {
                        daySessions.add(Pair(sub, s))
                    }
                }
            }
            daySessions.sortBy { it.second.time }

            // Dynamic height based on sessions
            val cardHeight = if (daySessions.isEmpty()) 96f else 64f + (daySessions.size * 62f)
            
            // Check boundaries
            if (currentY + cardHeight < height - 120f) {
                // Cartoon drop-shadow effect (offset card rectangle in dark blue/slate)
                val shadowPaint = Paint().apply {
                    color = Color.parseColor("#D0D9E6")
                    style = Paint.Style.FILL
                }
                canvas.drawRoundRect(
                    108f, currentY + 10f, width - 92f, currentY + cardHeight + 10f,
                    20f, 20f, shadowPaint
                )

                // Main card surface
                canvas.drawRoundRect(
                    100f, currentY, width - 100f, currentY + cardHeight,
                    20f, 20f, cardBgPaint
                )
                canvas.drawRoundRect(
                    100f, currentY, width - 100f, currentY + cardHeight,
                    20f, 20f, cardOutlinePaint
                )

                // Sticker Badge for Day Header
                val badgeColors = mapOf(
                    "LUNES" to "#3F51B5",
                    "MARTES" to "#009688",
                    "MIÉRCOLES" to "#FF9800",
                    "JUEVES" to "#E91E63",
                    "VIERNES" to "#9C27B0",
                    "SÁBADO" to "#4CAF50"
                )
                val badgeBgColor = Color.parseColor(badgeColors[name] ?: "#3F51B5")
                val badgePaint = Paint().apply {
                    color = badgeBgColor
                    style = Paint.Style.FILL
                }
                canvas.drawRoundRect(
                    130f, currentY + 12f, 340f, currentY + 56f,
                    12f, 12f, badgePaint
                )
                canvas.drawRoundRect(
                    130f, currentY + 12f, 340f, currentY + 56f,
                    12f, 12f, cardOutlinePaint
                )
                
                val badgeTextPaint = Paint().apply {
                    color = Color.WHITE
                    textSize = 23f
                    isAntiAlias = true
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
                canvas.drawText(name, 160f, currentY + 44f, badgeTextPaint)

                if (daySessions.isEmpty()) {
                    canvas.drawText("¡Día libre de clases!", 370f, currentY + 46f, textPaintDetails)
                } else {
                    daySessions.forEachIndexed { index, (sub, session) ->
                        val itemY = currentY + 98f + (index * 62f)
                        
                        // Draw bullet circle
                        canvas.drawCircle(370f, itemY - 8f, 7f, Paint().apply {
                            color = Color.parseColor(sub.colorHex.ifBlank { "#0288D1" })
                            style = Paint.Style.FILL
                        })
                        canvas.drawCircle(370f, itemY - 8f, 7f, Paint().apply {
                            color = Color.parseColor("#0F1A24")
                            style = Paint.Style.STROKE
                            strokeWidth = 2f
                        })

                        canvas.drawText(
                            "${sub.name}  —  Aula: ${session.room}",
                            395f, itemY, textPaintClass
                        )
                        canvas.drawText(
                            session.time,
                            width - 290f, itemY, textPaintDetails
                        )
                    }
                }
                currentY += cardHeight + 36f
            }
        }

        // 4. Footer info
        val footerPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 24f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText("Organizado con UniBuddy Mascot — ¡Mucho Éxito!", 150f, height - 70f, footerPaint)

        // Save to public Pictures Gallery or Downloads
        val filename = "UniBuddy_Horario_${System.currentTimeMillis()}.png"
        var fos: java.io.OutputStream? = null
        var imageUri: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/UniBuddy")
                }
                val contentResolver = context.contentResolver
                imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    fos = contentResolver.openOutputStream(imageUri)
                }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/UniBuddy"
                val dir = java.io.File(imagesDir)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val file = java.io.File(imagesDir, filename)
                fos = java.io.FileOutputStream(file)
                imageUri = Uri.fromFile(file)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "¡Horario guardado en Imágenes/UniBuddy!", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error al guardar el horario: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        imageUri
    }
}
