package com.example.ecosnap.features.map.model

data class WastePartner(
  val id: String,
  val name: String,
  val area: String,
  val address: String,
  val phone: String,
  val openingHours: String,
  val location: PartnerLocation,
  val acceptedMaterials: Set<WasteMaterial>
)


