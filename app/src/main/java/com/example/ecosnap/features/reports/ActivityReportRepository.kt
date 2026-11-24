package com.example.ecosnap.features.reports

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.ecosnap.core.network.BackendClient
import com.example.ecosnap.core.network.invalidResponse
import com.example.ecosnap.core.network.optionalObject
import com.example.ecosnap.core.network.requireLong
import com.example.ecosnap.core.network.requireObject
import com.example.ecosnap.core.network.requireString
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** A downloaded report in app cache, ready to be handed to a PDF viewer through FileProvider. */
class DownloadedReport(val fileName: String, val uri: Uri)

class ActivityReportRepository(
    context: Context,
    private val uid: String,
    private val backend: BackendClient = BackendClient.default,
) {
    private val appContext = context.applicationContext

    /** [range] null = server's current calendar month (WIB) up to now. */
    suspend fun report(range: ReportRange?): ActivityReport {
        val root = backend.request("GET", REPORT_PATH, queryOf(range)) ?: throw invalidResponse()
        return parse(root.requireObject("data"))
    }

    /**
     * Downloads exactly the period of the already displayed [report], writes it under cache/reports
     * and returns its FileProvider URI. Only one report file is kept at a time.
     */
    suspend fun downloadPdf(report: ActivityReport): DownloadedReport {
        val bytes = backend.downloadPdf(PDF_PATH, mapOf("from" to report.periodFrom, "to" to report.periodTo))
        val name = "ecosnap-activity-report-${report.firstDate.replace("-", "")}-${report.lastDate.replace("-", "")}.pdf"
        val owner = java.security.MessageDigest.getInstance("SHA-256").digest(uid.toByteArray(Charsets.UTF_8))
            .take(8).joinToString("") { "%02x".format(it) }
        return withContext(Dispatchers.IO) {
            val dir = File(appContext.cacheDir, REPORT_DIR)
            if (!dir.isDirectory && !dir.mkdirs()) throw IOException("Cache directory unavailable")
            dir.listFiles()?.forEach { it.delete() }
            val target = File(dir, "$owner-$name")
            val temp = File(dir, "$owner-$name.part")
            try {
                temp.writeBytes(bytes)
                if (!temp.renameTo(target)) throw IOException("Cannot finalize file")
            } finally {
                if (temp.exists()) temp.delete()
            }
            DownloadedReport(name, FileProvider.getUriForFile(appContext, AUTHORITY, target))
        }
    }

    private fun queryOf(range: ReportRange?): Map<String, String> =
        if (range == null) emptyMap() else linkedMapOf("from" to range.from, "to" to range.to)

    private fun parse(data: JsonObject): ActivityReport {
        val account = data.requireObject("account")
        val role = when (account.requireString("role")) {
            "user" -> ReportRole.User
            "mitra" -> ReportRole.Partner
            else -> throw invalidResponse()
        }
        val period = data.requireObject("period")
        val totalsJson = data.requireObject("totals")
        val daily = (data.get("daily") as? JsonArray ?: throw invalidResponse()).map { element ->
            val day = element as? JsonObject ?: throw invalidResponse()
            val date = day.requireString("date")
            if (!DATE.matches(date)) throw invalidResponse()
            ReportDay(
                date = date,
                claimedDeposits = day.requireNonNegative("claimed_deposits"),
                weightGrams = day.requireNonNegative("total_weight_grams"),
                weightKg = day.requireString("total_weight_kg"),
                points = points(day, role),
            )
        }
        if (daily.isEmpty()) throw invalidResponse()
        val categories = (data.get("categories") as? JsonArray ?: throw invalidResponse()).map { element ->
            val category = element as? JsonObject ?: throw invalidResponse()
            ReportCategory(
                id = category.requireString("category_id"),
                name = category.requireString("category_name"),
                depositCount = category.requireNonNegative("deposit_count"),
                weightGrams = category.requireNonNegative("total_weight_grams"),
                weightKg = category.requireString("total_weight_kg"),
            )
        }
        val days = period.requireLong("days")
        if (days != daily.size.toLong()) throw invalidResponse()
        return ActivityReport(
            role = role,
            fullName = account.requireString("full_name", allowEmpty = true),
            businessName = account.optString("business_name"),
            periodFrom = period.requireString("from"),
            periodTo = period.requireString("to"),
            timezone = period.requireString("timezone"),
            days = days.toInt(),
            generatedAt = data.requireString("generated_at"),
            totals = ReportTotals(
                claimedDeposits = totalsJson.requireNonNegative("claimed_deposits"),
                weightGrams = totalsJson.requireNonNegative("total_weight_grams"),
                weightKg = totalsJson.requireString("total_weight_kg"),
                points = points(totalsJson, role),
            ),
            daily = daily,
            categories = categories,
        )
    }

    /** Users must carry earned+spent and no credited; partners must carry credited and no earned/spent. */
    private fun points(json: JsonObject, role: ReportRole): ReportPoints {
        val earned = json.optionalLong("points_earned")
        val credited = json.optionalLong("points_credited")
        val spent = json.optionalLong("points_spent")
        val valid = when (role) {
            ReportRole.User -> earned != null && spent != null && credited == null
            ReportRole.Partner -> credited != null && earned == null && spent == null
        }
        if (!valid) throw invalidResponse()
        return ReportPoints(earned, credited, spent)
    }

    private fun JsonObject.requireNonNegative(name: String): Long =
        requireLong(name).also { if (it < 0) throw invalidResponse() }

    private fun JsonObject.optionalLong(name: String): Long? {
        val element: JsonElement = get(name) ?: return null
        if (element.isJsonNull) return null
        val number = (element as? JsonPrimitive)?.takeIf { it.isNumber } ?: throw invalidResponse()
        val value = try {
            number.asBigDecimal.longValueExact()
        } catch (_: ArithmeticException) {
            throw invalidResponse()
        }
        if (value < 0) throw invalidResponse()
        return value
    }

    private fun JsonObject.optString(name: String): String? {
        val element = get(name) ?: return null
        if (element.isJsonNull) return null
        return (element as? JsonPrimitive)?.takeIf { it.isString }?.asString?.takeIf { it.isNotBlank() }
            ?: throw invalidResponse()
    }

    companion object {
        const val AUTHORITY = "com.example.ecosnap.fileprovider"
        const val REPORT_DIR = "reports"
        private const val REPORT_PATH = "api/v1/me/activity-report"
        private const val PDF_PATH = "api/v1/me/activity-report.pdf"
        private val DATE = Regex("^\\d{4}-\\d{2}-\\d{2}$")
    }
}

/** Intents for an already downloaded report; the caller starts them and handles missing viewers. */
object ReportPdfIntents {
    fun view(report: DownloadedReport): Intent =
        Intent(Intent.ACTION_VIEW)
            .setDataAndType(report.uri, "application/pdf")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

    fun share(report: DownloadedReport): Intent {
        val send = Intent(Intent.ACTION_SEND)
            .setType("application/pdf")
            .putExtra(Intent.EXTRA_STREAM, report.uri)
            .putExtra(Intent.EXTRA_SUBJECT, report.fileName)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        send.clipData = android.content.ClipData.newRawUri(report.fileName, report.uri)
        return Intent.createChooser(send, "Bagikan laporan aktivitas")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
}
