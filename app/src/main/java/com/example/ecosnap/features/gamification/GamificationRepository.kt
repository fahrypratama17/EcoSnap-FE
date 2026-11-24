package com.example.ecosnap.features.gamification

import com.example.ecosnap.core.network.BackendClient
import com.example.ecosnap.core.network.BackendException
import com.example.ecosnap.core.network.invalidResponse
import com.example.ecosnap.core.network.optionalObject
import com.example.ecosnap.core.network.requireLong
import com.example.ecosnap.core.network.requireObject
import com.example.ecosnap.core.network.requireString
import com.example.ecosnap.features.home.HomeRepository
import com.example.ecosnap.features.home.model.MissionBoard
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

data class Badge(
  val id: String,
  val title: String,
  val description: String,
  val target: Long,
  val progress: Long,
  val earned: Boolean,
  val unlockedAt: String?
)

data class BadgeBoard(
  val serverTime: String,
  val claimedDeposits: Long,
  val claimedWeightGrams: Long,
  val badges: List<Badge>
) {
  val earnedCount: Int get() = badges.count { it.earned }
}

data class LeaderboardEntry(
  val rank: Long,
  val displayName: String,
  val xp: Long
)

data class LeaderboardMe(val rank: Long, val xp: Long)

data class LeaderboardBoard(
  val periodStart: String,
  val periodEnd: String,
  val serverTime: String,
  val entries: List<LeaderboardEntry>,
  val me: LeaderboardMe?
)

/**
 * Read-only server views behind the Games home, badges and weekly-XP leaderboard. XP only ever comes
 * from mission claims on the server; nothing here computes or stores XP locally. Mission reads reuse
 * [HomeRepository] so there is a single parser for the mission contract.
 */
class GamificationRepository(
  private val backend: BackendClient = BackendClient.default,
  private val missionSource: HomeRepository = HomeRepository(backend)
) {
  suspend fun missions(): MissionBoard = missionSource.missions()

  suspend fun badges(): BadgeBoard {
    val data = envelope(backend.request("GET", "api/v1/me/badges"))
    val list = data.get("badges") as? JsonArray ?: throw invalidResponse()
    return BadgeBoard(
      serverTime = data.requireString("server_time"),
      claimedDeposits = data.requireLong("claimed_deposits"),
      claimedWeightGrams = data.requireLong("claimed_weight_grams"),
      badges = list.map { parseBadge(it as? JsonObject ?: throw invalidResponse()) }
    )
  }

  suspend fun leaderboard(limit: Int = LEADERBOARD_LIMIT): LeaderboardBoard {
    val data = envelope(
      backend.request("GET", "api/v1/leaderboard", mapOf("limit" to limit.toString()))
    )
    val list = data.get("entries") as? JsonArray ?: throw invalidResponse()
    val me = data.optionalObject("me")
    return LeaderboardBoard(
      periodStart = data.requireString("period_start"),
      periodEnd = data.requireString("period_end"),
      serverTime = data.requireString("server_time"),
      entries = list.map {
        val item = it as? JsonObject ?: throw invalidResponse()
        LeaderboardEntry(
          rank = item.requireLong("rank"),
          displayName = item.requireString("display_name"),
          xp = item.requireLong("xp")
        )
      },
      me = me?.let { LeaderboardMe(rank = it.requireLong("rank"), xp = it.requireLong("xp")) }
    )
  }

  private fun parseBadge(item: JsonObject): Badge {
    val earned = (item.get("earned") as? JsonPrimitive)?.takeIf { it.isBoolean }?.asBoolean
      ?: throw invalidResponse()
    val unlocked = item.get("unlocked_at")
    return Badge(
      id = item.requireString("id"),
      title = item.requireString("title"),
      description = item.requireString("description", allowEmpty = true),
      target = item.requireLong("target"),
      progress = item.requireLong("progress"),
      earned = earned,
      unlockedAt = if (unlocked == null || unlocked.isJsonNull) null else item.requireString("unlocked_at")
    )
  }

  private fun envelope(root: JsonObject?): JsonObject = (root ?: throw invalidResponse()).requireObject("data")

  companion object {
    const val LEADERBOARD_LIMIT = 50
  }
}

/** Server message for known failures; the gamification role gate gets a specific explanation. */
fun Throwable.gamificationMessage(fallback: String): String {
  val e = this as? BackendException ?: return fallback
  if (e.code == "gamification_forbidden") return "Fitur ini hanya tersedia untuk akun pengguna."
  return e.message?.takeIf { it.isNotBlank() } ?: fallback
}
