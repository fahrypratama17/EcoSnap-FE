package com.example.ecosnap.features.articles.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Clock3
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun ArticleReadTime(
  readingMinutes: Int,
  modifier: Modifier = Modifier,
  color: Color = EcoColor.Green700
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Icon(
      imageVector = Lucide.Clock3,
      contentDescription = null,
      tint = color,
      modifier = Modifier.size(13.dp)
    )

    Text(
      text = "$readingMinutes menit baca",
      fontSize = 11.sp,
      color = color
    )
  }
}