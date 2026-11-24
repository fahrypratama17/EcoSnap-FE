package com.example.ecosnap.features.profile.data

import android.content.Context
import com.example.ecosnap.core.auth.AccountRole
import com.example.ecosnap.core.auth.BusinessProfile
import com.example.ecosnap.core.network.BackendClient
import com.example.ecosnap.core.network.BackendException
import com.example.ecosnap.core.network.invalidResponse
import com.example.ecosnap.core.network.optionalObject
import com.example.ecosnap.core.network.requireLong
import com.example.ecosnap.core.network.requireObject
import com.example.ecosnap.core.network.requireString
import com.example.ecosnap.features.home.HomeRepository
import com.example.ecosnap.features.onboarding.data.OnboardingPreferences
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first

data class ProfileSettings(
  val notificationEnabled: Boolean,
  val showOnLeaderboard: Boolean,
  val onboardingCompleted: Boolean
)

data class ProfileData(
  val uid: String,
  val role: AccountRole,
  val fullName: String,
  val email: String,
  val phone: String,
  val walletBalance: Long,
  val business: BusinessProfile?,
  val settings: ProfileSettings
)

/** Null members mean that single figure could not be loaded; nothing is estimated locally. */
data class UserStats(val depositsClaimed: Long?, val badgesEarned: Long?)

data class PartnerStats(val claimedDeposits: Long, val claimedPoints: Long, val qrIssued: Long)

/** Rejected locally with a user-safe message before any request is made. */
class ProfileInputException(message: String) : Exception(message)

/** Profile, settings and stat calls. Every parser is strict: a missing or mistyped field is `invalid_response`. */
class ProfileRepository(
  private val backend: BackendClient = BackendClient.default,
  private val home: HomeRepository = HomeRepository(backend)
) {

  suspend fun profile(): ProfileData = parseProfile(data(backend.request("GET", "api/v1/me")))

  /** Sends only fields that changed; at least one is required. Returns the stored profile. */
  suspend fun updateProfile(fullName: String?, phone: String?): ProfileData {
    val body = JsonObject()
    fullName?.let { body.addProperty("full_name", validName(it)) }
    phone?.let { body.addProperty("phone", validPhone(it)) }
    if (body.size() == 0) throw ProfileInputException("Tidak ada perubahan untuk disimpan.")
    return parseProfile(data(backend.request("PATCH", "api/v1/me", body = body)))
  }

  suspend fun settings(): ProfileSettings = parseSettings(data(backend.request("GET", "api/v1/me/settings")))

  suspend fun setNotificationEnabled(enabled: Boolean): ProfileSettings {
    val body = JsonObject().apply { addProperty("notification_enabled", enabled) }
    return parseSettings(data(backend.request("PATCH", "api/v1/me/settings", body = body)))
  }

  /** User-only endpoint; the server answers 403 for mitra. Returns the stored visibility. */
  suspend fun setLeaderboardVisible(visible: Boolean): Boolean {
    val body = JsonObject().apply { addProperty("visible", visible) }
    val result = data(backend.request("PATCH", "api/v1/me/leaderboard-visibility", body = body))
    return result.requireBoolean("visible")
  }

  /** Each figure fails independently so one outage never hides the other. */
  suspend fun userStats(): UserStats = coroutineScope {
    val deposits = async {
      figure { data(backend.request("GET", "api/v1/me/home")).requireLong("deposits_claimed") }
    }
    val badges = async {
      figure {
        val badges = data(backend.request("GET", "api/v1/me/badges")).get("badges") as? JsonArray
          ?: throw invalidResponse()
        badges.count { (it as? JsonObject ?: throw invalidResponse()).requireBoolean("earned") }.toLong()
      }
    }
    UserStats(deposits.await(), badges.await())
  }

  suspend fun partnerStats(): PartnerStats {
    val summary = home.partnerHome()
    return PartnerStats(summary.claimed, summary.claimedPoints, summary.qrIssued)
  }

  /**
   * Best effort: mirrors a locally completed onboarding to the authenticated account once the server
   * says it is not yet recorded. Any failure is swallowed so it can never block a session.
   */
  suspend fun syncOnboarding(localCompleted: Boolean) {
    if (!localCompleted) return
    try {
      if (settings().onboardingCompleted) return
    } catch (e: CancellationException) {
      throw e
    } catch (_: Exception) {
      return
    }
    markOnboardingCompleted()
  }

  /** Best effort PATCH onboarding_completed=true; failures are swallowed. */
  suspend fun markOnboardingCompleted() {
    try {
      val body = JsonObject().apply { addProperty("onboarding_completed", true) }
      backend.request("PATCH", "api/v1/me/settings", body = body)
    } catch (e: CancellationException) {
      throw e
    } catch (_: Exception) {
    }
  }

  private suspend fun figure(block: suspend () -> Long): Long? = try {
    block()
  } catch (e: CancellationException) {
    throw e
  } catch (_: Exception) {
    null
  }

  private fun data(root: JsonObject?): JsonObject = (root ?: throw invalidResponse()).requireObject("data")

  private fun parseSettings(data: JsonObject) = ProfileSettings(
    notificationEnabled = data.requireBoolean("notification_enabled"),
    showOnLeaderboard = data.requireBoolean("show_on_leaderboard"),
    onboardingCompleted = data.requireBoolean("onboarding_completed")
  )

  private fun parseProfile(data: JsonObject): ProfileData {
    val role = when (data.requireString("role")) {
      "user" -> AccountRole.USER
      "mitra" -> AccountRole.PARTNER
      else -> throw invalidResponse()
    }
    if (data.requireString("status") != "active") throw invalidResponse()
    val business = data.optionalObject("business")?.let {
      BusinessProfile(it.requireString("name"), it.requireString("category"), it.requireString("status"))
    }
    if ((role == AccountRole.PARTNER) != (business != null)) throw invalidResponse()
    val balance = data.requireObject("wallet").requireLong("balance")
    if (balance < 0) throw invalidResponse()
    return ProfileData(
      uid = data.requireString("uid"),
      role = role,
      fullName = data.requireString("full_name"),
      email = data.requireString("email", allowEmpty = true),
      phone = data.requireString("phone"),
      walletBalance = balance,
      business = business,
      settings = parseSettings(data.requireObject("settings"))
    )
  }

  private fun JsonObject.requireBoolean(name: String): Boolean =
    (get(name) as? JsonPrimitive)?.takeIf { it.isBoolean }?.asBoolean ?: throw invalidResponse()

  companion object {
    private const val MAX_NAME = 150

    fun validName(value: String): String {
      val name = value.trim()
      if (name.isEmpty() || name.codePointCount(0, name.length) > MAX_NAME) {
        throw ProfileInputException("Nama lengkap wajib diisi, maksimal 150 karakter.")
      }
      return name
    }

    fun validPhone(value: String): String {
      val number = value.trim().filterNot { it == ' ' || it == '-' }
      val digits = number.removePrefix("+")
      if (digits.length !in 9..15 || digits.any { it !in '0'..'9' }) {
        throw ProfileInputException("Nomor telepon harus 9-15 digit, boleh diawali tanda +.")
      }
      return number
    }
  }
}

/** Reads the device's onboarding flag and mirrors it to the signed-in account (best effort). */
suspend fun syncOnboardingCompletion(context: Context, repository: ProfileRepository = ProfileRepository()) {
  val completed = try {
    OnboardingPreferences(context.applicationContext).isCompleted.first()
  } catch (e: CancellationException) {
    throw e
  } catch (_: Exception) {
    false
  }
  repository.syncOnboarding(completed)
}

internal fun Throwable.profileMessage(fallback: String): String = when (this) {
  is ProfileInputException -> message ?: fallback
  is BackendException -> message?.takeIf { it.isNotBlank() } ?: fallback
  else -> fallback
}
