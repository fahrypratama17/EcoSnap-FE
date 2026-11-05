package com.example.ecosnap.features.leaderboard.model

data class LeaderboardEntry(
  val id: String,
  val name: String,
  val score: Int,
  val isCurrentUser: Boolean = false
) {
  val initials: String get() = name
    .trim()
    .split(Regex("\\s+"))
    .filter { it.isNotBlank() }
    .take(2)
    .map { it.first().uppercase() }
    .joinToString("")
}