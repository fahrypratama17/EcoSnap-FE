package com.example.ecosnap.features.points.model

data class VoucherCategorySection(
  val id: String,
  val title: String,
  val vouchers: List<VoucherItem>
)
