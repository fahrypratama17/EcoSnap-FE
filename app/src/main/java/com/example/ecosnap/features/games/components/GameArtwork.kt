package com.example.ecosnap.features.games.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Brain
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Recycle
import com.example.ecosnap.features.games.model.GameType

@Composable
fun GameArtwork(
  type: GameType,
  modifier: Modifier = Modifier,
  height: Dp = 110.dp
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(height)
      .background(
        color = if (type == GameType.SORTING) {
          Color(0xFF76C7A6)
        } else {
          Color(0xFF96D9B6)
        },
        shape = RoundedCornerShape(12.dp)
      ),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = if (type == GameType.SORTING) {
        Lucide.Recycle
      } else {
        Lucide.Brain
      },
      contentDescription = null,
      modifier = Modifier.size(65.dp),
      tint = Color.White.copy(alpha = 0.85f)
    )
  }
}