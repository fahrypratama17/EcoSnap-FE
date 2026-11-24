package com.example.ecosnap.features.wallet

import com.example.ecosnap.core.network.BackendException
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.CancellationException

internal fun Throwable.userMessage(fallback: String): String =
  (this as? BackendException)?.message?.takeIf { it.isNotBlank() } ?: fallback

/**
 * True only when the server provably did not execute the request (a final 4xx answer). Network failures,
 * timeouts, 5xx, 408/429 and session changes leave the outcome unknown.
 */
internal fun Throwable.isDefinitiveRejection(): Boolean {
  val status = (this as? BackendException)?.status ?: return false
  return status in 400..499 && status != 408 && status != 429
}

internal inline fun <T> attempt(block: () -> T): Result<T> = try {
  Result.success(block())
} catch (e: CancellationException) {
  throw e
} catch (e: Exception) {
  Result.failure(e)
}

private val ID = Locale.forLanguageTag("id-ID")

internal fun formatNumber(value: Long): String = NumberFormat.getIntegerInstance(ID).format(value)

internal fun formatSigned(value: Long): String = (if (value > 0) "+" else "") + formatNumber(value)

private val DISPLAY = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", ID)

/** Formats an RFC 3339 server timestamp in the user's zone; the raw text is never shown as a date. */
internal fun formatTimestamp(value: String): String =
  parseInstant(value)?.let { DISPLAY.format(it.atZone(ZoneId.systemDefault())) } ?: "-"

internal fun parseInstant(value: String): Instant? = try {
  OffsetDateTime.parse(value).toInstant()
} catch (_: Exception) {
  null
}

private val DAY = DateTimeFormatter.ofPattern("d MMM yyyy", ID)

internal fun formatDay(date: LocalDate): String = DAY.format(date)

/** Local-zone start of [date] as an RFC 3339 instant (inclusive `from`). */
internal fun startOfDayInstant(date: LocalDate): String =
  date.atStartOfDay(ZoneId.systemDefault()).toInstant().toString()

/** Local-zone start of the day after [date] (exclusive `to`). */
internal fun endExclusiveInstant(date: LocalDate): String =
  date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toString()

/** DatePicker returns UTC midnight millis; this maps them to the calendar date the user picked. */
internal fun pickerMillisToDate(millis: Long): LocalDate =
  Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

internal val VoucherStatus.label: String
  get() = when (this) {
    VoucherStatus.Issued -> "Aktif"
    VoucherStatus.Used -> "Sudah dipakai"
    VoucherStatus.Expired -> "Kedaluwarsa"
  }

internal const val VOUCHER_SIMULATION_NOTE =
  "Voucher ini simulasi untuk demo EcoSnap dan tidak dapat ditukarkan di merchant nyata. Poin tidak dikembalikan."
