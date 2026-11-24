package com.example.ecosnap.features.scan.model

data class DepositTransaction(
  val id: String,
  val partnerName: String,
  val points: Int,
  val weightKg: Double,
  val date: String,
  val status: String
)
