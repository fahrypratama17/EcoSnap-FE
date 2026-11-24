package com.example.ecosnap.features.admincal.model

/** Hundredths of a kilogram avoid floating point drift while editing on the keypad. */
object WasteCalculator {
  const val POINTS_PER_KG = 100
  const val MAX_WEIGHT_HUNDREDTHS = 999_999 // 9,999.99 kg

  fun hundredths(input: String): Int =
    input.replace(',', '.').toBigDecimalOrNull()
      ?.takeIf { it.signum() >= 0 }
      ?.let { runCatching { it.movePointRight(2).intValueExact() }.getOrNull() }
      ?.takeIf { it <= MAX_WEIGHT_HUNDREDTHS } ?: 0

  fun enter(input: String, key: String): String {
    if (key == "delete") return input.dropLast(1).ifEmpty { "0" }
    if (key == ",") return if (',' in input) input else "$input,"
    if (key.length != 1 || key[0] !in '0'..'9') return input
    if (',' in input && input.substringAfter(',').length >= 2) return input
    if (',' !in input && input != "0" && input.length >= 4) return input
    return if (input == "0") key else input + key
  }

  fun adjust(input: String, deltaHundredths: Int): String =
    format((hundredths(input) + deltaHundredths).coerceIn(0, MAX_WEIGHT_HUNDREDTHS))

  fun format(hundredths: Int): String =
    "${hundredths / 100},${(hundredths % 100).toString().padStart(2, '0')}"

  fun points(hundredths: Int): Int = hundredths * POINTS_PER_KG / 100
}
