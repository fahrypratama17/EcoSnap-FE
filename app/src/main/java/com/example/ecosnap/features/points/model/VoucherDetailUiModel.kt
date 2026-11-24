package com.example.ecosnap.features.points.model

data class VoucherDetailUiModel(
  val id: String,
  val title: String,
  val subtitle: String,
  val pointsCost: Int,
  val termsParagraphs: List<String>
)
