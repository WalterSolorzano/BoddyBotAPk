import java.util.Calendar

fun main() {
    val daysOfWeekCodes = listOf("Do", "Lu", "Ma", "Mi", "Ju", "Vi", "Sá")
    val currentDayIndex = 6 // Saturday
    for (i in 0..6) {
        val checkIndex = (currentDayIndex + i) % 7
        println("Day $i ahead is ${daysOfWeekCodes[checkIndex]}")
    }
}
