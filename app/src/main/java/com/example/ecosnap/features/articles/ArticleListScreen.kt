package com.example.ecosnap.features.articles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.SearchX
import com.example.ecosnap.core.designsystem.components.EcoAppHeader
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.articles.components.ArticleCategory
import com.example.ecosnap.features.articles.components.ArticleCategoryFilters
import com.example.ecosnap.features.articles.components.ArticleFeaturedCard
import com.example.ecosnap.features.articles.components.ArticleListCard
import com.example.ecosnap.features.articles.components.ArticleSearchField
import com.example.ecosnap.features.articles.components.ArticleTipCard
import com.example.ecosnap.features.articles.data.LocalArticles

@Composable
fun ArticleListScreen(
  onBack: () -> Unit,
  onArticleClick: (String) -> Unit,
  onOpenScan: () -> Unit,
  onNotifications: () -> Unit,
  onProfile: () -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by rememberSaveable {
    mutableStateOf("")
  }

  var selectedCategoryName by rememberSaveable {
    mutableStateOf("All")
  }

  val selectedCategory = ArticleCategory.entries.firstOrNull {
    it.name == selectedCategoryName
  }

  val filteredArticles = remember(searchQuery, selectedCategoryName) {
    val keyword = searchQuery.trim()

    LocalArticles.articles.filter { article ->
      val matchesCategory = selectedCategory == null || article.category == selectedCategory

      val matchesSearch = keyword.isEmpty() || article.title.contains(keyword, ignoreCase = true)
              ||  article.summary.contains(keyword, ignoreCase = true)

      matchesSearch && matchesCategory
    }
  }

  val featured = filteredArticles.firstOrNull { it.isFeatured } ?: filteredArticles.firstOrNull()

  val regularArticles = filteredArticles.filter { it.id != featured?.id }

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
            text = "Beranda",
            fontSize = 12.sp
          )
        }
      }

      item {
        Column {
          Text(
            text = "Artikel & Edukasi",
            style = MaterialTheme.typography.titleLarge.copy(
              fontSize = 22.sp,
              lineHeight = 28.sp
            ),
            color = EcoColor.Green900
          )

          Spacer(Modifier.height(4.dp))

          Text(
            text = "Temukan informasi dan tips untuk mengelola sampah dengna lebih bijak",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontSize = 12.sp,
              lineHeight = 17.sp
            ),
            color = EcoColor.Green700
          )
        }
      }

      item {
        ArticleSearchField(
          value = searchQuery,
          onValueChange = { searchQuery = it }
        )
      }

      item {
        ArticleCategoryFilters(
          selectedCategory = selectedCategory,
          onCategorySelected = { category -> selectedCategoryName = category?.name ?: "All"}
        )
      }

      if (featured != null) {
        item {
          Text(
            text = "Artikel Pilihan",
            style = MaterialTheme.typography.titleMedium.copy(
              fontSize = 16.sp,
              lineHeight = 22.sp
            ),
            color = EcoColor.Green900
          )
        }

        item {
          ArticleFeaturedCard(
            article = featured,
            onClick = { onArticleClick(featured.id) }
          )
        }
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Jelajahi Artikel",
            style = MaterialTheme.typography.titleMedium.copy(
              fontSize = 16.sp,
              lineHeight = 22.sp
            ),
            color = EcoColor.Green900
          )

          Spacer(Modifier.weight(1f))

          Text(
            text = "${regularArticles.size} artikel",
            fontSize = 12.sp,
            color = EcoColor.Green700
          )
        }
      }

      items(
        count = regularArticles.size,
        key = { index -> regularArticles[index].id }
      ) { index ->
        val article = regularArticles[index]

        ArticleListCard(
          article = article,
          onClick = { onArticleClick(article.id) }
        )
      }

      if(filteredArticles.isEmpty()) {
        item {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Lucide.SearchX,
              contentDescription = null,
              modifier = Modifier.size(36.dp),
              tint = EcoColor.Green700
            )

            Spacer(Modifier.height(8.dp))

            Text(
              text = "Artikel tidak ditemukan.",
              style = MaterialTheme.typography.titleMedium,
              color = EcoColor.Green900,
            )

            Text(
              text = "Coba kata kunci atau kategori lainnya.",
              color = EcoColor.Green700,
              textAlign = TextAlign.Center
            )
          }
        }
      }

      item {
        ArticleTipCard(
          title = "Bingung dengan sampahmu?",
          description = "Gunakan Scan AI EcoSnap untuk mengenali sampah dan mendapatkan saran penanganan",
          actionText = "Tersedia di menuScan",
          onClick = onOpenScan
        )
      }
    }
  }
}