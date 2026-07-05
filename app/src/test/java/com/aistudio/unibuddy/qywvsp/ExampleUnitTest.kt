package com.aistudio.unibuddy.qywvsp

import org.junit.Assert.*
import org.junit.Test
import com.aistudio.unibuddy.qywvsp.ui.parseSingleTime
import com.aistudio.unibuddy.qywvsp.ui.parseTimeRange

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testParseSingleTime_AM_PM() {
    // 12-hour AM formats
    assertEquals(Pair(8, 0), parseSingleTime("08:00 AM"))
    assertEquals(Pair(8, 0), parseSingleTime("8:00 am"))
    assertEquals(Pair(11, 30), parseSingleTime("11:30 am"))
    assertEquals(Pair(0, 0), parseSingleTime("12:00 AM"))
    assertEquals(Pair(0, 15), parseSingleTime("12:15 am"))
    
    // 12-hour PM formats
    assertEquals(Pair(14, 0), parseSingleTime("02:00 PM"))
    assertEquals(Pair(14, 30), parseSingleTime("2:30 pm"))
    assertEquals(Pair(12, 0), parseSingleTime("12:00 PM"))
    assertEquals(Pair(12, 45), parseSingleTime("12:45 pm"))
    assertEquals(Pair(23, 59), parseSingleTime("11:59 pm"))
    
    // 24-hour formats
    assertEquals(Pair(14, 0), parseSingleTime("14:00"))
    assertEquals(Pair(8, 0), parseSingleTime("08:00"))
    assertEquals(Pair(23, 0), parseSingleTime("23:00"))
    
    // Shorthand formats (no colon)
    assertEquals(Pair(8, 0), parseSingleTime("8am"))
    assertEquals(Pair(12, 0), parseSingleTime("12pm"))
    assertEquals(Pair(13, 0), parseSingleTime("1pm"))
    assertEquals(Pair(0, 0), parseSingleTime("12am"))
  }

  @Test
  fun testParseTimeRange() {
    val range1 = parseTimeRange("08:00 AM - 10:00 AM")
    assertNotNull(range1)
    assertEquals(Pair(8, 0), range1!!.first)
    assertEquals(Pair(10, 0), range1.second)

    val range2 = parseTimeRange("02:30 PM - 4:15 PM")
    assertNotNull(range2)
    assertEquals(Pair(14, 30), range2!!.first)
    assertEquals(Pair(16, 15), range2.second)

    val range3 = parseTimeRange("14:00-16:00")
    assertNotNull(range3)
    assertEquals(Pair(14, 0), range3!!.first)
    assertEquals(Pair(16, 0), range3.second)
  }
}
