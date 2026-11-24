package com.example.ecosnap.core.model

data class GeoPoint(val latitude: Double, val longitude: Double)

/** Server-defined waste category. Never enumerate these in the app: unknown ones must still render. */
data class WasteCategory(
  val id: String,
  val name: String,
  val slug: String,
  val code: String,
  val handlingGuide: String,
  val requiresSpecialHandling: Boolean,
)

/**
 * One day of structured opening hours. [dayOfWeek] is 0 = Sunday .. 6 = Saturday.
 * [opensAt]/[closesAt] are "HH:MM", and are empty strings only when [isClosed].
 */
data class PartnerHours(
  val dayOfWeek: Int,
  val opensAt: String,
  val closesAt: String,
  val isClosed: Boolean,
)

data class Partner(
  val id: String,
  val name: String,
  val area: String,
  val address: String,
  val phone: String,
  val openingHours: String,
  val description: String,
  val location: GeoPoint,
  val acceptedMaterials: List<WasteCategory>,
  val hours: List<PartnerHours>,
)
