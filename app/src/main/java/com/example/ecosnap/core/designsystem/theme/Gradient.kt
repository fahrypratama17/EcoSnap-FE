package com.example.ecosnap.core.designsystem.theme

import androidx.compose.ui.graphics.Brush

object EcoGradient {
  val vertical = Brush.verticalGradient(
    colors = listOf(
      EcoColor.Green200,
      EcoColor.Green100,
      EcoColor.GreenSoft
    )
  )

  val horizontal = Brush.horizontalGradient(
    colors = listOf(
      EcoColor.Green200,
      EcoColor.GreenSoft,
      EcoColor.Green200
    )
  )
}