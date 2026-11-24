package com.example.ecosnap.features.photos.model

/** Outcome of one AI scan; mirrors the raw `ScanResponse` of the separate AI service. */
sealed interface WasteScanOutcome {
  data class Identified(val result: WastePhotosResult) : WasteScanOutcome

  /** Domain non-identification (HTTP 200): `invalid_image` or `multiple_objects`. */
  data class NotIdentified(
    val status: NotIdentifiedStatus,
    val message: String
  ) : WasteScanOutcome
}

enum class NotIdentifiedStatus { InvalidImage, MultipleObjects }

enum class RecyclingStatus { Recyclable, ConditionallyRecyclable, NonRecyclable, Unknown }

data class WastePhotosResult(
  val objectName: String,
  val category: String,
  val material: String?,
  val recyclable: Boolean?,
  val recyclingStatus: RecyclingStatus,
  val tags: List<String>,
  val description: String,
  val requiresVerification: Boolean,
  val verificationNote: String?,
  val hazardous: Boolean,
  val handlingSteps: List<String>,
  val upcyclingPossible: Boolean,
  val upcyclingReason: String?,
  val reuseIdeas: List<ReuseIdea>,
  val industrialDescription: String,
  val industrialPossible: Boolean?,
  val potentialProducts: List<String>,
  val industrialLimitations: List<String>,
  val depositRecommended: Boolean,
  val depositPartnerType: String?,
  val depositSearchKeyword: String?,
  val depositPreparationNote: String?
)
