package com.example.ecosnap.features.leaderboard.data

import com.example.ecosnap.features.leaderboard.model.LeaderboardEntry
import com.example.ecosnap.features.leaderboard.model.LeaderboardMode
import com.example.ecosnap.features.leaderboard.model.RankedLeaderboardEntry

object LocalLeaderboard {
  private val depositEntries = listOf(
    LeaderboardEntry("king-marji", "King Marji", 1200),
    LeaderboardEntry("onetwotri", "OneTwoTriii", 1120),
    LeaderboardEntry("theheru", "TheHeru123", 985),
    LeaderboardEntry("aditya", "Aditya Akbar", 870),
    LeaderboardEntry("ghazy", "Ghazy Humaidi", 860),
    LeaderboardEntry("nafis", "Nafis Naufal", 850),
    LeaderboardEntry("king-m", "King M", 840),
    LeaderboardEntry("putri", "Putri Amelia", 790),
    LeaderboardEntry("dimas", "Dimas Pratama", 710),
    LeaderboardEntry("nabila", "Nabila Putri", 650),
    LeaderboardEntry("rizky", "Rizky Ramadhan", 530),
    LeaderboardEntry(
      id = "current-user",
      name = "Fahry Pratama",
      score = 410,
      isCurrentUser = true
    ),
    LeaderboardEntry("arif", "Arif Nugraha", 350),
    LeaderboardEntry("sinta", "Sinta Maharani", 290)
  )

  private val gamesEntries = listOf(
    LeaderboardEntry("king-marji", "King Marji", 1200),
    LeaderboardEntry("onetwotri", "OneTwoTriii", 1120),
    LeaderboardEntry("theheru", "TheHeru123", 985),
    LeaderboardEntry("aditya", "Aditya Akbar", 870),
    LeaderboardEntry("ghazy", "Ghazy Humaidi", 860),
    LeaderboardEntry("nafis", "Nafis Naufal", 850),
    LeaderboardEntry("king-m", "King M", 840),
    LeaderboardEntry("putri", "Putri Amelia", 790),
    LeaderboardEntry("dimas", "Dimas Pratama", 710),
    LeaderboardEntry("nabila", "Nabila Putri", 650),
    LeaderboardEntry("rizky", "Rizky Ramadhan", 530),
    LeaderboardEntry(
      id = "current-user",
      name = "Fahry Pratama",
      score = 410,
      isCurrentUser = true
    ),
    LeaderboardEntry("arif", "Arif Nugraha", 350),
    LeaderboardEntry("sinta", "Sinta Maharani", 290)
  )

  fun getRanking(
    mode: LeaderboardMode
  ) : List<RankedLeaderboardEntry> {
    val entries = when (mode) {
      LeaderboardMode.DEPOSIT -> depositEntries
      LeaderboardMode.GAMES -> gamesEntries
    }

    return entries.sortedWith(
      compareByDescending<LeaderboardEntry> {
        it.score
      }.thenBy { it.name }
    )
      .mapIndexed { index, entry ->
        RankedLeaderboardEntry(
          rank = index + 1,
          entry = entry
        )
      }
  }
}