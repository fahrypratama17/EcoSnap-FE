package com.example.ecosnap.features.articles.components

/** Filterable categories supported by the server. Unknown server values render with [GENERIC_LABEL]. */
enum class ArticleCategory(val code: String, val label: String) {
  GUIDE("GUIDE", "Panduan"),
  SORTING("SORTING", "Pilah Sampah"),
  RECYCLING("RECYCLING", "Daur Ulang"),
  SPECIAL_WASTE("SPECIAL_WASTE", "Limbah Khusus"),
  NEWS("NEWS", "Berita");

  companion object {
    const val GENERIC_LABEL = "Artikel"

    fun fromCode(code: String): ArticleCategory? = entries.firstOrNull { it.code == code }
  }
}
