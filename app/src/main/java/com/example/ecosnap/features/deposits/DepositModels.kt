package com.example.ecosnap.features.deposits

import com.example.ecosnap.core.network.BackendException
import com.example.ecosnap.core.network.invalidResponse
import com.example.ecosnap.core.network.requireLong
import com.example.ecosnap.core.network.requireString
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeParseException

/** One priced line of an estimate, claim preview or deposit snapshot. Decimals stay strings from the server. */
data class DepositItem(
  val categoryId: String,
  val categoryName: String,
  val weightKg: String,
  val pointsPerKg: String,
  val exactPoints: String,
  val isDemo: Boolean,
)

data class PointEstimate(
  val totalWeightKg: String,
  val estimatedPoints: Long,
  val usesDemoRates: Boolean,
  val asOf: String,
  val items: List<DepositItem>,
)

data class DepositDetail(
  val id: String,
  val partnerId: String,
  val status: String,
  val totalWeightKg: String,
  val totalPoints: Long,
  val usesDemoRates: Boolean,
  val ratesAsOf: String,
  val createdAt: String,
  val items: List<DepositItem>,
)

data class DepositSummary(
  val id: String,
  val status: String,
  val totalWeightKg: String,
  val totalPoints: Long,
  val usesDemoRates: Boolean,
  val createdAt: String,
  val claimedAt: String?,
)

data class DepositPage(val items: List<DepositSummary>, val nextCursor: String?)

data class ClaimPreview(
  val depositId: String,
  val partnerName: String,
  val totalWeightKg: String,
  val totalPoints: Long,
  val usesDemoRates: Boolean,
  val items: List<DepositItem>,
)

data class ClaimResult(val depositId: String, val points: Long, val walletBalance: Long)

/**
 * A freshly issued partner QR. [token] exists only in memory. The remaining time is `expires_at -
 * server_time`, counted down with the monotonic clock from [receivedAtElapsedMs] (taken before the
 * request was sent, so latency can only shorten it).
 */
data class IssuedQr(
  val depositId: String,
  val token: String,
  val warning: String,
  val remainingMs: Long,
  val receivedAtElapsedMs: Long,
)

data class WasteOption(val id: String, val name: String)

object DepositJson {
  private val uuidPattern =
    Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

  fun isUuid(value: String) = uuidPattern.matches(value)

  fun data(body: JsonObject?): JsonObject = body?.get("data") as? JsonObject ?: throw invalidResponse()

  private fun JsonObject.bool(name: String): Boolean {
    val p = get(name) as? JsonPrimitive
    if (p == null || !p.isBoolean) throw invalidResponse()
    return p.asBoolean
  }

  private fun JsonObject.optionalString(name: String): String? {
    val e: JsonElement = get(name) ?: return null
    if (e.isJsonNull) return null
    return requireString(name)
  }

  private fun JsonElement.obj(): JsonObject = this as? JsonObject ?: throw invalidResponse()

  private fun JsonObject.decimal(name: String): String {
    val value = requireString(name)
    try {
      BigDecimal(value)
    } catch (_: NumberFormatException) {
      throw invalidResponse()
    }
    return value
  }

  fun item(o: JsonObject) = DepositItem(
    categoryId = o.requireString("category_id"),
    categoryName = o.requireString("category_name"),
    weightKg = o.decimal("weight_kg"),
    pointsPerKg = o.decimal("points_per_kg"),
    exactPoints = o.decimal("exact_points"),
    isDemo = o.bool("is_demo"),
  )

  private fun items(o: JsonObject): List<DepositItem> {
    val array = o.get("items") as? JsonArray ?: throw invalidResponse()
    return array.map { item(it.obj()) }
  }

  fun estimate(o: JsonObject) = PointEstimate(
    totalWeightKg = o.decimal("total_weight_kg"),
    estimatedPoints = o.requireLong("estimated_points"),
    usesDemoRates = o.bool("uses_demo_rates"),
    asOf = o.requireString("as_of"),
    items = items(o),
  )

  fun detail(o: JsonObject) = DepositDetail(
    id = o.requireString("id"),
    partnerId = o.requireString("partner_id"),
    status = o.requireString("status"),
    totalWeightKg = o.decimal("total_weight_kg"),
    totalPoints = o.requireLong("total_points"),
    usesDemoRates = o.bool("uses_demo_rates"),
    ratesAsOf = o.requireString("rates_as_of"),
    createdAt = o.requireString("created_at"),
    items = items(o),
  )

  fun summary(o: JsonObject) = DepositSummary(
    id = o.requireString("id"),
    status = o.requireString("status"),
    totalWeightKg = o.decimal("total_weight_kg"),
    totalPoints = o.requireLong("total_points"),
    usesDemoRates = o.bool("uses_demo_rates"),
    createdAt = o.requireString("created_at"),
    claimedAt = o.optionalString("claimed_at"),
  )

  fun preview(o: JsonObject) = ClaimPreview(
    depositId = o.requireString("deposit_id"),
    partnerName = o.requireString("partner_name"),
    totalWeightKg = o.decimal("total_weight_kg"),
    totalPoints = o.requireLong("total_points"),
    usesDemoRates = o.bool("uses_demo_rates"),
    items = items(o),
  )

  fun claim(o: JsonObject) = ClaimResult(
    depositId = o.requireString("deposit_id"),
    points = o.requireLong("points"),
    walletBalance = o.requireLong("wallet_balance"),
  )

  fun issuedQr(o: JsonObject, requestStartedElapsedMs: Long): IssuedQr {
    val expires = instant(o.requireString("expires_at"))
    val serverTime = instant(o.requireString("server_time"))
    return IssuedQr(
      depositId = o.requireString("deposit_id"),
      token = o.requireString("token"),
      warning = o.requireString("warning"),
      remainingMs = Duration.between(serverTime, expires).toMillis().coerceAtLeast(0L),
      receivedAtElapsedMs = requestStartedElapsedMs,
    )
  }

  private fun instant(text: String): Instant = try {
    java.time.OffsetDateTime.parse(text).toInstant()
  } catch (_: DateTimeParseException) {
    throw invalidResponse()
  }
}

/** Shared presentation helpers for deposit screens. */
object DepositText {
  fun status(raw: String) = when (raw) {
    "created" -> "Dibuat"
    "qr_issued" -> "QR diterbitkan"
    "claimed" -> "Sudah diklaim"
    else -> raw
  }

  fun kg(value: String) = "${value.replace('.', ',')} kg"

  /** Server timestamps are RFC 3339 with an explicit offset; show them in the user's zone. */
  fun dateTime(iso: String): String = try {
    val zoned = java.time.OffsetDateTime.parse(iso).atZoneSameInstant(java.time.ZoneId.systemDefault())
    java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy HH:mm", java.util.Locale.forLanguageTag("id-ID")).format(zoned)
  } catch (_: DateTimeParseException) {
    iso
  }

  fun message(e: Throwable): String {
    if (e !is BackendException) return "Terjadi kesalahan. Silakan coba lagi."
    return when (e.code) {
      "account_forbidden" -> "Akun Anda ditangguhkan atau dinonaktifkan."
      "deposit_forbidden" -> "Hanya akun mitra aktif dengan lokasi terverifikasi yang dapat menggunakan fitur setoran ini."
      "claim_forbidden" -> "Hanya akun pengguna yang dapat mengklaim setoran."
      "account_not_found" -> "Profil akun belum dibuat."
      "deposit_not_found" -> "Setoran tidak ditemukan."
      "deposit_already_claimed" -> "Setoran ini sudah diklaim."
      "qr_invalid" -> "Kode QR tidak dikenali."
      "qr_revoked" -> "Kode QR ini sudah diganti dengan yang baru."
      "qr_expired" -> "Kode QR sudah kedaluwarsa. Minta mitra menerbitkan ulang."
      "waste_not_accepted" -> "Lokasi mitra tidak menerima salah satu kategori."
      "waste_category_not_found" -> "Salah satu kategori tidak ditemukan."
      "point_rate_unavailable" -> "Tarif aktif belum tersedia untuk salah satu kategori."
      "zero_points" -> "Berat terlalu kecil: total poin hasil hitung 0."
      "request_timeout" -> "Server terlalu lama menjawab. Coba lagi."
      else -> e.message ?: "Terjadi kesalahan. Silakan coba lagi."
    }
  }
}

/** Weight entry helpers: comma or dot, at most 3 decimals, never through Double. */
object WeightInput {
  private val pattern = Regex("^\\d{1,4}([.,]\\d{1,3})?$")
  const val MAX_ITEMS = 50
  private val MAX_TOTAL = BigDecimal("1000")
  private val MIN = BigDecimal("0.001")

  /** Canonical dotted string with exactly three decimals, or null when invalid. */
  fun canonical(text: String): String? {
    val t = text.trim()
    if (!pattern.matches(t)) return null
    val value = BigDecimal(t.replace(',', '.'))
    if (value < MIN || value > MAX_TOTAL) return null
    return value.setScale(3).toPlainString()
  }

  fun total(canonicals: List<String>): BigDecimal =
    canonicals.fold(BigDecimal.ZERO) { acc, v -> acc + BigDecimal(v) }

  fun withinLimit(canonicals: List<String>) = total(canonicals) <= MAX_TOTAL

  private val typing = Regex("^\\d{1,4}(,\\d{0,3})?$")

  /** Comma-decimal display text; an empty weight reads as 0. */
  fun display(text: String): String = text.trim().replace('.', ',').ifEmpty { "0" }

  /** Applies one keypad key ("0".."9", ",", "delete") to [current]; keys that break the format are ignored. */
  fun enter(current: String, key: String): String {
    val text = current.trim().replace('.', ',')
    return when (key) {
      "delete" -> text.dropLast(1)
      "," -> when {
        text.contains(',') -> text
        text.isEmpty() -> "0,"
        else -> "$text,"
      }
      else -> {
        val next = if (text == "0") key else text + key
        if (key.length == 1 && key[0].isDigit() && typing.matches(next)) next else text
      }
    }
  }

  /** Adds [delta] (plain decimal, may be negative) to [current], clamped to 0..1000 kg; zero becomes empty. */
  fun adjust(current: String, delta: String): String {
    val base = current.trim().replace(',', '.').trimEnd('.').ifEmpty { "0" }.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val result = (base + BigDecimal(delta)).max(BigDecimal.ZERO).min(MAX_TOTAL)
    if (result.signum() == 0) return ""
    return result.setScale(3, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString().replace('.', ',')
  }
}
