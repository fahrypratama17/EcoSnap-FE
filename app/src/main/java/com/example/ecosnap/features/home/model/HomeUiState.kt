package com.example.ecosnap.features.home.model

import com.example.ecosnap.core.model.Partner

/** One weekly mission exactly as the server reports it. [unit] is `deposit` or `gram`. */
data class Mission(
  val id: String,
  val title: String,
  val description: String,
  val unit: String,
  val target: Long,
  val progress: Long,
  val rewardXp: Long,
  val status: String,
  val claimedAt: String?
) {
  val isClaimable: Boolean get() = status == STATUS_CLAIMABLE
  val isClaimed: Boolean get() = status == STATUS_CLAIMED

  companion object {
    const val STATUS_IN_PROGRESS = "in_progress"
    const val STATUS_CLAIMABLE = "claimable"
    const val STATUS_CLAIMED = "claimed"
  }
}

/** [periodStart] is the opaque claim token: it is sent back byte-for-byte, never reformatted. */
data class MissionBoard(
  val periodStart: String,
  val periodEnd: String,
  val serverTime: String,
  val weeklyXp: Long,
  val totalXp: Long,
  val missions: List<Mission>
) {
  /** Claimable first, then in progress, then claimed; null when the server sent no mission. */
  fun featured(): Mission? =
    missions.firstOrNull { it.status == Mission.STATUS_CLAIMABLE }
      ?: missions.firstOrNull { it.status == Mission.STATUS_IN_PROGRESS }
      ?: missions.firstOrNull()
}

data class HomeArticle(
  val id: String,
  val title: String,
  val summary: String,
  val readingMinutes: Int,
  val isDemo: Boolean
)

/** Everything on the user Home, from one `GET /me/home`. */
data class HomeData(
  val userName: String,
  val balance: Long,
  val monthlyDeposits: Long,
  val unreadCount: Long,
  val missions: MissionBoard?,
  val partner: Partner?,
  val article: HomeArticle?
)

data class HomeUiState(
  val data: HomeData? = null,
  val isLoading: Boolean = true,
  val error: String? = null
)

data class PartnerDepositSummary(
  val id: String,
  val status: String,
  val totalWeightKg: String,
  val totalPoints: Long,
  val createdAt: String
)

/** `GET /partner/home`. */
data class PartnerHomeData(
  val businessName: String,
  val partnerStatus: String,
  val created: Long,
  val qrIssued: Long,
  val claimed: Long,
  val claimedWeightKg: String,
  val claimedPoints: Long,
  val recentDeposits: List<PartnerDepositSummary>
)

data class MissionClaimResult(
  val mission: Mission,
  val xpAwarded: Long,
  val weeklyXp: Long,
  val totalXp: Long
)
