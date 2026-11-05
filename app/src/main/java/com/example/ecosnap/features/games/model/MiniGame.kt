package com.example.ecosnap.features.games.model

data class MiniGame(
  val id: String,
  val title: String,
  val description: String,
  val about: String,
  val type: GameType,
  val difficulty: String = "Pemula",
  val xpPerCorrect: Int = 10,
  val instructions: List<String>,
  val questions: List<GameQuestion>
) {
  val totalQuestions: Int get() = questions.size

  val maximumXp: Int get() = totalQuestions * xpPerCorrect
}