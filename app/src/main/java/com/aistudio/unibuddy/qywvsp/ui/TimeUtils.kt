package com.aistudio.unibuddy.qywvsp.ui

import android.content.Context
import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

fun formatTimeRange(context: Context, timeRange: String): String {
    val is24h = DateFormat.is24HourFormat(context)
    if (is24h) return timeRange

    try {
        val parts = timeRange.split(" - ")
        if (parts.size == 2) {
            val format24 = SimpleDateFormat("HH:mm", Locale.getDefault())
            val format12 = SimpleDateFormat("hh:mm a", Locale.getDefault())

            val start = format24.parse(parts[0])
            val end = format24.parse(parts[1])

            if (start != null && end != null) {
                return "${format12.format(start)} - ${format12.format(end)}"
            }
        }
    } catch (e: Exception) {
        // Ignored, just return original
    }
    return timeRange
}

fun getSubjectInitials(name: String): String {
    val cleanName = name.trim()
    if (cleanName.isEmpty()) return "SUB"
    
    // Check if the name matches a code-like pattern, like "99IND411" or "CSOC010" or "OPT-6683"
    val words = cleanName.split(Regex("""[\s_/\-]+""")).filter { it.isNotEmpty() }
    
    if (words.size == 1) {
        val word = words[0]
        if (word.any { it.isDigit() }) {
            // It has numbers, e.g. "99IND411" -> let's take the first letters or up to 4 characters
            return if (word.length <= 4) word.uppercase() else word.take(4).uppercase()
        }
        // Just take first 3 letters of the word
        return if (word.length <= 3) word.uppercase() else word.take(3).uppercase()
    }
    
    // Multiple words! Let's get the initials of significant words (skip Spanish prepositions/conjunctions)
    val skipWords = setOf("DE", "LA", "EL", "E", "Y", "EN", "PARA", "CON", "DEL", "O", "I", "II", "III", "IV", "V", "VI", "VII")
    val initials = words
        .filter { it.uppercase() !in skipWords }
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        
    if (initials.length >= 2) {
        return if (initials.length <= 4) initials else initials.take(4)
    }
    
    // Fallback if we only got 1 initial (e.g. because of skipped words)
    val firstWord = words[0]
    return if (firstWord.length <= 3) firstWord.uppercase() else firstWord.take(3).uppercase()
}
