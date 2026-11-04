package com.example.ecosnap.features.articles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.components.EcoAppHeader
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.articles.components.Article
import com.example.ecosnap.features.articles.components.ArticleCategoryBadge
import com.example.ecosnap.features.articles.components.ArticleCover
import com.example.ecosnap.features.articles.components.ArticleReadTime
import com.example.ecosnap.features.articles.components.ArticleTipCard

@Composable
fun ArticleDetailScreen(
    article: Article,
    onBack: () -> Unit,
    onFinished: () -> Unit,
    onNotifications: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier
  ) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White)
  ) {
    EcoAppHeader(
      onNotifications = onNotifications,
      onProfile = onProfile
    )

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(
        horizontal = 16.dp,
        vertical = 14.dp
      ),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        TextButton(
          onClick = onBack,
          contentPadding = PaddingValues(0.dp)
        ) {
          Icon(
            imageVector = Lucide.ArrowLeft,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )

          Spacer(Modifier.width(5.dp))

          Text(
            text = "Kembali ke Artikel",
            fontSize = 12.sp
          )
        }
      }

      item {
        ArticleCategoryBadge( category = article.category )
      }

      item {
        Text(
          text = article.title,
          style = MaterialTheme.typography.headlineLarge.copy(
            fontSize = 24.sp,
            lineHeight = 30.sp,
            letterSpacing = 0.sp
          ),
          color = EcoColor.Green900
        )
      }

      item {
        ArticleReadTime(readingMinutes = article.readingMinutes)
      }

      item {
        ArticleCover(height = 156.dp)
      }

      item {
        Text(
          text = article.introduction,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 13.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.sp
          ),
          color = EcoColor.Green700
        )
      }

      items(
        count = article.sections.size,
        key = { index -> "section-$index" }
      ) { index ->
        val section = article.sections[index]

        Column {
          Text(
            text = section.title,
            style = MaterialTheme.typography.titleMedium.copy(
              fontSize = 16.sp,
              lineHeight = 22.sp,
            ),
            color = EcoColor.Green900
          )

          Spacer(Modifier.height(6.dp))

          Text(
            text = section.body,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontSize = 13.sp,
              lineHeight = 20.sp,
              letterSpacing = 0.sp
            ),
            color = EcoColor.Green700
          )
        }
      }

      if (article.tip != null) {
        item {
          ArticleTipCard(
            title = "Tips EcoSnap",
            description = article.tip
          )
        }
      }

      item {
        Spacer(Modifier.height(4.dp))

        EcoButton(
          text = "Selesai Membaca",
          onClick = onFinished,
          modifier = Modifier.fillMaxWidth(),
          containerColor = EcoColor.Green800,
          contentColor = Color.White
        )
      }
    }
  }
}