package com.example.ecosnap.features.articles.data

import com.example.ecosnap.core.network.BackendClient
import com.example.ecosnap.core.network.BackendException
import com.example.ecosnap.features.articles.components.Article
import com.example.ecosnap.features.articles.components.ArticleCategory
import com.example.ecosnap.features.articles.components.ArticleSection
import com.example.ecosnap.features.articles.components.ArticleSummary
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject

data class ArticlePage(val items: List<ArticleSummary>, val nextCursor: String?)

class ArticleRepository(private val backend: BackendClient = BackendClient.default) {
  suspend fun list(
    search: String,
    category: ArticleCategory?,
    cursor: String?,
    limit: Int = PAGE_SIZE
  ): ArticlePage {
    val query = linkedMapOf("limit" to limit.toString())
    val keyword = search.trim()
    if (keyword.isNotEmpty()) query["q"] = keyword
    if (category != null) query["category"] = category.code
    if (!cursor.isNullOrEmpty()) query["cursor"] = cursor

    val root = backend.request("GET", "api/v1/articles", query) ?: invalid()
    val data = root.get("data") as? JsonArray ?: invalid()
    val items = data.map { parseSummary(it as? JsonObject ?: invalid()) }
    val next = (root.get("pagination") as? JsonObject)?.optString("next_cursor")
    return ArticlePage(items, next)
  }

  suspend fun detail(id: String): Article {
    if (!UUID_PATTERN.matches(id)) throw notFound()
    val root = backend.request("GET", "api/v1/articles/${id.lowercase()}") ?: invalid()
    val data = root.get("data") as? JsonObject ?: invalid()
    val sections = (data.get("sections") as? JsonArray).orEmpty().map { element ->
      val section = element as? JsonObject ?: invalid()
      ArticleSection(title = section.requireString("title"), body = section.requireString("body"))
    }
    return Article(
      summary = parseSummary(data),
      introduction = data.optString("introduction").orEmpty(),
      sections = sections,
      tip = data.optString("tip")
    )
  }

  private fun parseSummary(json: JsonObject): ArticleSummary {
    val minutes = json.get("reading_minutes").asIntOrNull() ?: invalid()
    return ArticleSummary(
      id = json.requireString("id"),
      title = json.requireString("title"),
      summary = json.optString("summary").orEmpty(),
      categoryCode = json.optString("category").orEmpty(),
      readingMinutes = minutes.coerceAtLeast(0),
      isFeatured = json.optBoolean("is_featured"),
      isDemo = json.optBoolean("is_demo")
    )
  }

  private fun JsonArray?.orEmpty(): List<JsonElement> = this?.toList() ?: emptyList()

  private fun JsonElement?.isPresent() = this != null && this !is JsonNull

  private fun JsonElement?.asIntOrNull(): Int? = try {
    if (this != null && isJsonPrimitive && asJsonPrimitive.isNumber) asBigDecimal.intValueExact() else null
  } catch (_: RuntimeException) {
    null
  }

  private fun JsonObject.optString(name: String): String? {
    val element = get(name)
    if (!element.isPresent() || !element.isJsonPrimitive || !element.asJsonPrimitive.isString) return null
    return element.asString.takeIf { it.isNotBlank() }
  }

  private fun JsonObject.optBoolean(name: String): Boolean {
    val element = get(name)
    return element.isPresent() && element.isJsonPrimitive &&
      element.asJsonPrimitive.isBoolean && element.asBoolean
  }

  private fun JsonObject.requireString(name: String): String = optString(name) ?: invalid()

  private fun invalid(): Nothing =
    throw BackendException(null, "invalid_response", "Respons server tidak dapat dibaca. Silakan coba lagi.")

  private fun notFound() =
    BackendException(404, "article_not_found", "Artikel tidak ditemukan.")

  companion object {
    const val PAGE_SIZE = 20
    private val UUID_PATTERN = Regex(
      "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    )
  }
}
