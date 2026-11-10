package com.example.ecosnap.features.admincal

import com.example.ecosnap.features.admincal.model.WasteCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class WasteCalculatorTest {
  @Test fun keypadHandlesLeadingZeroCommaAndDeletion() {
    var input = "0"
    listOf("0", "2", ",", ",", "5", "0", "9").forEach { input = WasteCalculator.enter(input, it) }
    assertEquals("2,50", input)
    assertEquals(250, WasteCalculator.hundredths(input))
    repeat(4) { input = WasteCalculator.enter(input, "delete") }
    assertEquals("0", input)
    assertEquals("0", WasteCalculator.enter(input, "delete"))
    assertEquals("0,", WasteCalculator.enter(input, ","))
  }

  @Test fun quickAdjustmentsAreExactAndClampAtTheLimits() {
    var input = "0"
    repeat(25) { input = WasteCalculator.adjust(input, 10) }
    assertEquals("2,50", input)
    assertEquals(250, WasteCalculator.points(WasteCalculator.hundredths(input)))
    assertEquals("2,00", WasteCalculator.adjust(input, -50))
    assertEquals("0,00", WasteCalculator.adjust("0,10", -50))
    assertEquals("9999,99", WasteCalculator.adjust("9999,90", 100))
  }

  @Test fun keypadLimitsWholeDigitsAndDecimals() {
    assertEquals("9999", WasteCalculator.enter("9999", "9"))
    assertEquals("9999,99", WasteCalculator.enter("9999,99", "9"))
    assertEquals("1,23", WasteCalculator.enter("1,23", "7"))
    assertEquals("1", WasteCalculator.enter("1", "-"))
  }

  @Test fun oneHundredthOfAKilogramEqualsOnePoint() {
    assertEquals(1, WasteCalculator.points(WasteCalculator.hundredths("0,01")))
    assertEquals(105, WasteCalculator.points(WasteCalculator.hundredths("1,05")))
    assertEquals("0,01", WasteCalculator.format(1))
    assertEquals(0, WasteCalculator.hundredths("-2"))
    assertEquals(0, WasteCalculator.hundredths("invalid"))
    assertEquals(0, WasteCalculator.hundredths("10000"))
  }
}
