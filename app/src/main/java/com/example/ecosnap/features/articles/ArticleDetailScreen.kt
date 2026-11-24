package com.example.ecosnap.features.articles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.components.EcoAppHeader
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.articles.components.ArticleCategoryBadge
import com.example.ecosnap.features.articles.components.ArticleCover
import com.example.ecosnap.features.articles.components.ArticleReadTime
import com.example.ecosnap.features.articles.components.ArticleTipCard

@Composable
fun ArticleDetailScreen(
  state: ArticleDetailUiState,
  onRetry: () -> Unit,
  onBack: () -> Unit,
  onFinished: () -> Unit,
  onNotifications: () -> Unit,
  onProfile: () -> Unit,
  modifier: Modifier = Modifier
) {
  val article = state.article

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

      if (article == null) {
        item {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            if (state.isLoading) {
              CircularProgressIndicator(color = EcoColor.Green700)
            } else {
              Text(
                text = if (state.notFound) "Artikel tidak ditemukan." else "Artikel belum dapat dimuat.",
                style = MaterialTheme.typography.titleMedium,
                color = EcoColor.Green900
              )

              Text(
                text = if (state.notFound) {
                  "Artikel ini mungkin sudah tidak tersedia."
                } else {
                  state.error.orEmpty()
                },
                color = EcoColor.Green700,
                textAlign = TextAlign.Center
              )

              if (!state.notFound) {
                TextButton(onClick = onRetry) { Text("Coba lagi") }
              }
            }
          }
        }
      } else {
        item {
          ArticleCategoryBadge(label = article.summary.badgeLabel)
        }

        item {
          Text(
            text = article.summary.title,
            style = MaterialTheme.typography.headlineLarge.copy(
              fontSize = 24.sp,
              lineHeight = 30.sp,
              letterSpacing = 0.sp
            ),
            color = EcoColor.Green900
          )
        }

        item {
          ArticleReadTime(readingMinutes = article.summary.readingMinutes)
        }

        item {
          ArticleCover(height = 156.dp)
        }

        if (article.introduction.isNotEmpty()) {
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
}
