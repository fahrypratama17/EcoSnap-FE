package com.example.ecosnap.features.articles.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowUpRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Recycle
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun ArticleListCard(
  article: Article,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    color = Color.White,
    border = BorderStroke(
      1.dp,
      EcoColor.Green300
    ),
    shadowElevation = 2.dp
  ) {
    Row(
      modifier = Modifier.padding(10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(58.dp),
        contentAlignment = Alignment.Center
      ) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          shape = RoundedCornerShape(10.dp),
          color = EcoColor.Green100
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Lucide.Recycle,
              contentDescription = null,
              tint = EcoColor.Green800,
              modifier = Modifier.size(30.dp)
            )
          }
        }
      }

      Spacer(Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = article.category.label,
          fontSize = 11.sp,
          color = EcoColor.Green700
        )

        Spacer(Modifier.height(3.dp))

        Text(
          text = article.title,
          style = MaterialTheme.typography.titleMedium.copy(
            fontSize = 14.sp,
            lineHeight = 19.sp,
          ),
          color = EcoColor.Green900,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(5.dp))

        ArticleReadTime(readingMinutes = article.readingMinutes)
      }
    }

    Icon(
      imageVector = Lucide.ArrowUpRight,
      contentDescription = null,
      tint = EcoColor.Green700,
      modifier = Modifier.size(17.dp)
    )
  }
}