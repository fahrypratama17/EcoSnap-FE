package com.example.ecosnap.features.photos.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun PhotoFrameOverlay(
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier) {
    val corner = 25.dp.toPx()
    val stroke = 3.dp.toPx()

    val w = size.width
    val h = size.height

    val corners = listOf(
      Offset(0f, corner) to Offset(0f, 0f),
      Offset(0f, 0f) to Offset(corner, 0f),

      Offset(w - corner, 0f) to Offset(w, 0f),
      Offset(w, 0f) to Offset(w, corner),

      Offset(0f, h - corner) to Offset(0f, h),
      Offset(0f, h) to Offset(corner, h),

      Offset(w - corner, h) to Offset(w, h),
      Offset(w, h - corner) to Offset(w, h)
    )

    corners.forEach { (start, end) ->
      drawLine(
        color = Color.White,
        start = start,
        end = end,
        strokeWidth = stroke,
        cap = StrokeCap.Round
      )
    }
  }
}