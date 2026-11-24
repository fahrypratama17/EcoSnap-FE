package com.example.ecosnap.features.reports

import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class ReportRole { User, Partner }

/** Role-specific point figures. Users never show `credited`, partners never show `spent`. */
data class ReportPoints(val earned: Long?, val credited: Long?, val spent: Long?)

data class ReportTotals(
    val claimedDeposits: Long,
    val weightGrams: Long,
    val weightKg: String,
    val points: ReportPoints,
)

data class ReportDay(
    val date: String,
    val claimedDeposits: Long,
    val weightGrams: Long,
    val weightKg: String,
    val points: ReportPoints,
)

data class ReportCategory(
    val id: String,
    val name: String,
    val depositCount: Long,
    val weightGrams: Long,
    val weightKg: String,
)

data class ActivityReport(
    val role: ReportRole,
    val fullName: String,
    val businessName: String?,
    /** Period instants exactly as the server rendered them (WIB); reused verbatim for the PDF request. */
    val periodFrom: String,
    val periodTo: String,
    val timezone: String,
    val days: Int,
    val generatedAt: String,
    val totals: ReportTotals,
    val daily: List<ReportDay>,
    val categories: List<ReportCategory>,
) {
    val firstDate: String get() = daily.first().date
    val lastDate: String get() = daily.last().date
}

/** `from` inclusive, `to` exclusive, both explicit RFC 3339 instants. */
data class ReportRange(val from: String, val to: String)

object ReportRanges {
    const val MAX_DAYS = 366L
    private val WIB: ZoneOffset = ZoneOffset.ofHours(7)
    private val FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx")

    /**
     * Whole-day WIB range: [start 00:00, end+1 day 00:00). Null with a user-facing reason when invalid.
     * Both dates are required together, matching the server's paired filter.
     */
    fun build(start: LocalDate?, end: LocalDate?): Pair<ReportRange?, String?> {
        if (start == null || end == null) return null to "Pilih tanggal mulai dan tanggal akhir."
        if (end.isBefore(start)) return null to "Tanggal akhir tidak boleh sebelum tanggal mulai."
        val from = OffsetDateTime.of(start, java.time.LocalTime.MIDNIGHT, WIB)
        val to = OffsetDateTime.of(end.plusDays(1), java.time.LocalTime.MIDNIGHT, WIB)
        if (ChronoUnit.DAYS.between(from, to) > MAX_DAYS) return null to "Rentang maksimum 366 hari."
        return ReportRange(FORMAT.format(from), FORMAT.format(to)) to null
    }
}
