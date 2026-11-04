package com.example.ecosnap.features.articles.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun ArticleFeaturedCard(
  article: Article,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    color = EcoColor.Green800,
    shadowElevation = 3.dp
  ) {
    Column(
      modifier = Modifier.padding(10.dp)
    ) {
      ArticleCover(height = 136.dp)

      Spacer(Modifier.height(10.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        ArticleCategoryBadge(category = article.category)

        Spacer(Modifier.weight(1f))

        ArticleReadTime(
          readingMinutes = article.readingMinutes,
          color = EcoColor.Green100
        )
      }

      Spacer(Modifier.height(8.dp))

      Text(
        text = article.title,
        style = MaterialTheme.typography.titleLarge.copy(
          fontSize = 18.sp,
          lineHeight = 24.sp
        ),
        color = Color.White
      )

      Spacer(Modifier.height(5.dp))

      Text(
        text = article.summary,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        color = EcoColor.Green100,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(Modifier.height(10.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "Baca Selengkapnya",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color.White
        )

        Spacer(Modifier.width(5.dp))

        Icon(
          imageVector = Lucide.ArrowRight,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(15.dp)
        )
      }
    }
  }
}