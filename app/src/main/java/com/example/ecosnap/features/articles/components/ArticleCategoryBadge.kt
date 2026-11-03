package com.example.ecosnap.features.articles.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun ArticleCategoryBadge(
  category: ArticleCategory,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(20.dp),
    color = EcoColor.Green100
  ) {
    Text(
      text = category.label,
      fontSize = 11.sp,
      color = EcoColor.Green800,
      modifier = Modifier.padding(
        horizontal = 10.dp,
        vertical = 5.dp
      )
    )
  }
}