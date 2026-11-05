package com.example.ecosnap.features.leaderboard.model

enum class LeaderboardMode(
  val label: String,
  val scoreUnit: String,
  val explanation: String,
  val podiumSubtitle: String,
  val tip: String,
) {
  DEPOSIT(
    label = "Setoran Sampah",
    scoreUnit = "Poin",
    explanation = "Peringkat berdasarkan poin aktivitas setoran sampah yang telah divalidasi.",
    podiumSubtitle = "Peringkat aktivitas penyetoran sampah",
    tip = "Setorkan sampah ke mitra EcoSnap dan kumpulkan poin dari transaksi yang telah diverifikasi.",
  ),

  GAMES(
    label = "Games",
    scoreUnit = "XP",
    explanation = "Peringkat berdasarkan XP dari mini games dan tantangan yang telah diselesaikan.",
    podiumSubtitle = "Peringkat aktivitas mini games",
    tip = "Mainkan mini games, selesaikan tantangan, dan raih skor tertinggi.",
  )
}