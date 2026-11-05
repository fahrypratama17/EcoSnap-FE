package com.example.ecosnap.features.games.model

data class GameQuestion(
  val id: String,
  val title: String,
  val prompt: String,
  val visual: GameVisual,
  val options: List<String>,
  val correctAnswer: String,
  val explanation: String
)
