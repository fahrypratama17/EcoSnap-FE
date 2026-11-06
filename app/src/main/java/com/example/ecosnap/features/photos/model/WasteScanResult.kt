package com.example.ecosnap.features.photos.model

data class WasteScanResult(
  val objectName: String,
  val category: String,
  val material: String,
  val tags: List<String>,
  val description: String,
  val handlingSteps: List<String>,
  val reuseIdeas: List<ReuseIdea>,
  val industryPotential: String
)