package com.example.ecosnap.features.articles.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun ArticleCategoryFilters(
  selectedCategory: ArticleCategory?,
  onCategorySelected: (ArticleCategory?) -> Unit,
  modifier: Modifier = Modifier
) {
  LazyRow(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(7.dp),
    contentPadding = PaddingValues(
      vertical = 2.dp
    )
  ) {
    item {
      ArticleFilterChip(
        text = "Semua",
        selected = selectedCategory == null,
        onClick = { onCategorySelected(null) }
      )
    }

    items(ArticleCategory.entries) { category ->
      ArticleFilterChip(
        text = category.label,
        selected = selectedCategory == category,
        onClick = { onCategorySelected(category) }
      )
    }
  }
}

@Composable
private fun ArticleFilterChip(
  text: String,
  selected: Boolean,
  onClick: () -> Unit,
) {
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(20.dp),
    color = if (selected) {
      EcoColor.Green100
    } else {
      Color.White
    },
    border = BorderStroke(
      1.dp,
      if (selected) {
        EcoColor.Green700
      } else {
        EcoColor.Green400
      }
    )
  ) {
    Box(
      modifier = Modifier
        .height(34.dp)
        .padding(horizontal = 12.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = text,
        fontSize = 12.sp,
        color = EcoColor.Green900,
        fontWeight = if (selected) {
          FontWeight.SemiBold
        } else {
          FontWeight.Normal
        }
      )
    }
  }
}