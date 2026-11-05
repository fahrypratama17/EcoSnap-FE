package com.example.ecosnap.features.games.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Brain
import com.composables.icons.lucide.Leaf
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Package
import com.composables.icons.lucide.Recycle
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.games.model.GameVisual

@Composable
fun GameQuestionVisual(
  visual: GameVisual,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .background(
        EcoColor.Green700,
        RoundedCornerShape(18.dp)
      ),
    contentAlignment = Alignment.Center
  ) {
    when (visual) {
      GameVisual.BOTTLE -> BottleOutline(
        modifier = Modifier.size(86.dp)
      )

      else -> {
        val imageVector: ImageVector = when (visual) {
          GameVisual.PAPER -> Lucide.BookOpen
          GameVisual.CAN -> Lucide.Package
          GameVisual.ORGANIC -> Lucide.Leaf
          GameVisual.BOX -> Lucide.Package
          GameVisual.RECYCLE -> Lucide.Recycle
          GameVisual.BRAIN -> Lucide.Brain
          GameVisual.BOTTLE -> Lucide.Recycle
        }

        Icon(
          imageVector = imageVector,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(66.dp)
        )
      }
    }
  }
}

@Composable
private fun BottleOutline(modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    val bottle = Path().apply {
      moveTo(w * 0.39f, h * 0.12f)
      lineTo(w * 0.61f, h * 0.12f)
      lineTo(w * 0.61f, h * 0.33f)
      lineTo(w * 0.70f, h * 0.45f)
      lineTo(w * 0.70f, h * 0.85f)
      quadraticBezierTo(
        w * 0.70f, h * 0.91f,
        w * 0.63f, h * 0.91f
      )
      lineTo(w * 0.37f, h * 0.91f)
      quadraticBezierTo(
        w * 0.30f, h * 0.91f,
        w * 0.30f, h * 0.85f
      )
      lineTo(w * 0.30f, h * 0.45f)
      lineTo(w * 0.39f, h * 0.33f)
      close()
    }

    drawPath(
      path = bottle,
      color = Color.White.copy(alpha = 0.85f),
      style = Stroke(
        width = 2.dp.toPx(),
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
      )
    )

    val wave = Path().apply {
      moveTo(w * 0.31f, h * 0.67f)
      quadraticBezierTo(
        w * 0.48f, h * 0.62f,
        w * 0.56f, h * 0.69f
      )
      quadraticBezierTo(
        w * 0.63f, h * 0.73f,
        w * 0.69f, h * 0.68f
      )
    }

    drawPath(
      path = wave,
      color = Color.White.copy(alpha = 0.75f),
      style = Stroke(width = 1.5.dp.toPx())
    )
  }
}