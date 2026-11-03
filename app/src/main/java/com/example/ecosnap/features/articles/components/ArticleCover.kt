package com.example.ecosnap.features.articles.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Recycle
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun ArticleCover(
  modifier: Modifier = Modifier,
  height: Dp = 140.dp
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .height(height),
    shape = RoundedCornerShape(12.dp),
    color = EcoColor.Green400
  ) {
    Box(
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Lucide.Recycle,
        contentDescription = null,
        modifier = Modifier.size(76.dp),
        tint = Color.White.copy(alpha = 0.9f)
      )
    }
  }
}