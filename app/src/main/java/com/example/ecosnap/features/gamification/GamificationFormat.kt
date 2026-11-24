package com.example.ecosnap.features.gamification

import java.time.Duration
import java.time.OffsetDateTime
import java.util.Locale

/**
 * Time left until [periodEnd], measured as `period_end - server_time` minus the monotonic time spent
 * since that server time was received; the phone's wall clock is never consulted. Null when either
 * timestamp cannot be read.
 */
fun remainingPeriodLabel(periodEnd: String, serverTime: String, elapsedSinceLoadMs: Long): String? {
  val remaining = try {
    Duration.between(
      OffsetDateTime.parse(serverTime).toInstant(),
      OffsetDateTime.parse(periodEnd).toInstant()
    ).minusMillis(elapsedSinceLoadMs.coerceAtLeast(0L))
  } catch (_: RuntimeException) {
    return null
  }
  if (remaining.isNegative || remaining.isZero) return "Periode telah berakhir. Muat ulang untuk periode terbaru."
  val days = remaining.toDays()
  val hours = remaining.minusDays(days).toHours()
  return when {
    days > 0 -> "$days hari $hours jam lagi"
    hours > 0 -> "$hours jam lagi"
    else -> "Kurang dari 1 jam lagi"
  }
}

fun formatWeight(grams: Long): String {
  if (grams < 1000L) return "$grams g"
  val kg = grams / 1000.0
  return String.format(Locale.forLanguageTag("id-ID"), "%.1f kg", kg)
}
