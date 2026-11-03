package com.example.ecosnap.features.articles.components

data class Article(
  val id: String,
  val title: String,
  val summary: String,
  val category: ArticleCategory,
  val readingMinutes: Int,
  val introduction: String,
  val sections: List<ArticleSection>,
  val tip: String? = null,
  val isFeatured: Boolean = false
)
