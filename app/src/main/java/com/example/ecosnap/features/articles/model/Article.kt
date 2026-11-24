package com.example.ecosnap.features.articles.components

data class ArticleSummary(
  val id: String,
  val title: String,
  val summary: String,
  val categoryCode: String,
  val readingMinutes: Int,
  val isFeatured: Boolean,
  val isDemo: Boolean
) {
  val category: ArticleCategory? = ArticleCategory.fromCode(categoryCode)
  val categoryLabel: String = category?.label ?: ArticleCategory.GENERIC_LABEL
  val badgeLabel: String = if (isDemo) "$categoryLabel · Demo" else categoryLabel
}

data class Article(
  val summary: ArticleSummary,
  val introduction: String,
  val sections: List<ArticleSection>,
  val tip: String?
)
