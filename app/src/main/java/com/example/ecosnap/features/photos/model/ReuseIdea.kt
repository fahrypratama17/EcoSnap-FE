package com.example.ecosnap.features.photos.model

data class ReuseIdea(
  val id: String,
  val title: String,
  val description: String,
  val toolsAndMaterials: List<String>,
  val steps: List<String>
)