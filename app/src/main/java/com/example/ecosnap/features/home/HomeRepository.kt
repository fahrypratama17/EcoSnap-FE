package com.example.ecosnap.features.home

import com.example.ecosnap.core.data.PartnerJson
import com.example.ecosnap.core.network.BackendClient
import com.example.ecosnap.core.network.invalidResponse
import com.example.ecosnap.core.network.optionalObject
import com.example.ecosnap.core.network.requireLong
import com.example.ecosnap.core.network.requireObject
import com.example.ecosnap.core.network.requireString
import com.example.ecosnap.features.home.model.HomeArticle
import com.example.ecosnap.features.home.model.HomeData
import com.example.ecosnap.features.home.model.Mission
import com.example.ecosnap.features.home.model.MissionBoard
import com.example.ecosnap.features.home.model.MissionClaimResult
import com.example.ecosnap.features.home.model.PartnerDepositSummary
import com.example.ecosnap.features.home.model.PartnerHomeData
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

/** Server calls behind Home, partner Home and missions. Parsers throw `invalid_response`, never invent values. */
class HomeRepository(private val backend: BackendClient = BackendClient.default) {

  suspend fun home(): HomeData {
    val data = envelope(backend.request("GET", "api/v1/me/home"))
    return HomeData(
      userName = data.requireString("user_name", allowEmpty = true).trim(),
      balance = data.requireObject("wallet").requireLong("balance"),
      monthlyDeposits = data.requireLong("monthly_deposits"),
      unreadCount = data.requireLong("unread_count"),
      missions = data.optionalObject("missions")?.let(::parseBoard),
      partner = data.optionalObject("partner")?.let(PartnerJson::parse),
      article = data.optionalObject("article")?.let(::parseArticle)
    )
  }

  suspend fun partnerHome(): PartnerHomeData {
    val data = envelope(backend.request("GET", "api/v1/partner/home"))
    val deposits = data.requireObject("deposits")
    return PartnerHomeData(
      businessName = data.requireString("business_name"),
      partnerStatus = data.requireString("partner_status"),
      created = deposits.requireLong("created"),
      qrIssued = deposits.requireLong("qr_issued"),
      claimed = deposits.requireLong("claimed"),
      claimedWeightKg = data.requireString("claimed_weight_kg"),
      claimedPoints = data.requireLong("claimed_points"),
      recentDeposits = array(data, "recent_deposits").map { element ->
        val item = element as? JsonObject ?: throw invalidResponse()
        PartnerDepositSummary(
          id = item.requireString("id"),
          status = item.requireString("status"),
          totalWeightKg = item.requireString("total_weight_kg"),
          totalPoints = item.requireLong("total_points"),
          createdAt = item.requireString("created_at")
        )
      }
    )
  }

  suspend fun missions(): MissionBoard =
    parseBoard(envelope(backend.request("GET", "api/v1/me/missions")))

  /** [periodStart] must be the exact token from [missions]; retries reuse the same value. */
  suspend fun claim(missionId: String, periodStart: String): MissionClaimResult {
    require(MISSION_ID.matches(missionId))
    val body = JsonObject().apply { addProperty("period_start", periodStart) }
    val data = envelope(
      backend.request(
        "POST", "api/v1/me/missions/$missionId/claim", body = body, expectedStatuses = setOf(200, 201)
      )
    )
    return MissionClaimResult(
      mission = parseMission(data.requireObject("mission")),
      xpAwarded = data.requireLong("xp_awarded"),
      weeklyXp = data.requireLong("weekly_xp"),
      totalXp = data.requireLong("total_xp")
    )
  }

  private fun envelope(root: JsonObject?): JsonObject =
    (root ?: throw invalidResponse()).requireObject("data")

  private fun array(obj: JsonObject, name: String): JsonArray = obj.get(name) as? JsonArray ?: throw invalidResponse()

  private fun parseBoard(data: JsonObject) = MissionBoard(
    periodStart = data.requireString("period_start"),
    periodEnd = data.requireString("period_end"),
    serverTime = data.requireString("server_time"),
    weeklyXp = data.requireLong("weekly_xp"),
    totalXp = data.requireLong("total_xp"),
    missions = array(data, "missions").map { parseMission(it as? JsonObject ?: throw invalidResponse()) }
  )

  private fun parseMission(item: JsonObject): Mission {
    val claimedAt = item.get("claimed_at")
    return Mission(
      id = item.requireString("id"),
      title = item.requireString("title"),
      description = item.requireString("description", allowEmpty = true),
      unit = item.requireString("unit"),
      target = item.requireLong("target"),
      progress = item.requireLong("progress"),
      rewardXp = item.requireLong("reward_xp"),
      status = item.requireString("status"),
      claimedAt = if (claimedAt == null || claimedAt.isJsonNull) null else item.requireString("claimed_at")
    )
  }

  private fun parseArticle(item: JsonObject): HomeArticle {
    val isDemo = (item.get("is_demo") as? JsonPrimitive)?.takeIf { it.isBoolean }?.asBoolean ?: throw invalidResponse()
    return HomeArticle(
      id = item.requireString("id"),
      title = item.requireString("title"),
      summary = item.requireString("summary", allowEmpty = true),
      readingMinutes = item.requireLong("reading_minutes").coerceIn(0, Int.MAX_VALUE.toLong()).toInt(),
      isDemo = isDemo
    )
  }

  private companion object {
    val MISSION_ID = Regex("^[A-Za-z0-9_-]{1,64}$")
  }
}
