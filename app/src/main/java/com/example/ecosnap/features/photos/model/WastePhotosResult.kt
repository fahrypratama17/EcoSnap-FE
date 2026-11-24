package com.example.ecosnap.features.photos.model

data class WastePhotosResult(
  val objectName: String,
  val category: String,
  val material: String,
  val tags: List<String>,
  val description: String,
  val handlingSteps: List<String>,
  val reuseIdeas: List<ReuseIdea>,
  val industryPotential: String,
  val recyclingStatus: RecyclingStatus,
  val verificationNote: String?,
  val hazardous: Boolean,
  val upcyclingReason: String?,
  val industryPossible: Boolean?,
  val industryProducts: List<String>,
  val industryLimitations: List<String>,
  val depositRecommended: Boolean,
  val partnerType: String?,
  val partnerSearchKeyword: String?,
  val depositPreparation: String?
)

enum class RecyclingStatus(val label: String) {
  RECYCLABLE("Berpotensi Didaur Ulang"),
  CONDITIONALLY_RECYCLABLE("Daur Ulang Bersyarat"),
  NON_RECYCLABLE("Tidak Dapat Didaur Ulang"),
  UNKNOWN("Perlu Verifikasi")
}
