package com.example.ecosnap.features.map

import com.example.ecosnap.core.data.PartnerJson
import com.example.ecosnap.core.model.Partner
import com.example.ecosnap.core.model.WasteCategory
import com.example.ecosnap.core.network.BackendClient
import com.example.ecosnap.core.network.BackendException
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

data class PartnerPage(val items: List<Partner>, val offset: Int, val hasMore: Boolean) {
  val nextOffset: Int get() = offset + items.size
}

/** Server-backed partner data. There is no local or fallback data source. */
class PartnerRepository(private val backend: BackendClient = BackendClient.default) {

  suspend fun categories(): List<WasteCategory> {
    val body = backend.request("GET", "api/v1/waste-categories") ?: invalid()
    return dataArray(body).map { PartnerJson.parseCategory(it.asObjectOrInvalid()) }
  }

  /** [material] is a category slug; null/blank omits the filter. */
  suspend fun list(query: String, material: String?, offset: Int, limit: Int = PAGE_SIZE): PartnerPage {
    val params = linkedMapOf<String, String>()
    if (query.isNotBlank()) params["q"] = query.trim()
    if (!material.isNullOrBlank()) params["material"] = material
    params["limit"] = limit.toString()
    params["offset"] = offset.toString()
    val body = backend.request("GET", "api/v1/partners", params) ?: invalid()
    val items = dataArray(body).map { PartnerJson.parse(it.asObjectOrInvalid()) }
    return PartnerPage(items, offset, hasMore(body))
  }

  suspend fun detail(partnerId: String): Partner {
    require(isUuid(partnerId))
    val body = backend.request("GET", "api/v1/partners/$partnerId") ?: invalid()
    val data = body.get("data")
    if (data == null || !data.isJsonObject) invalid()
    return PartnerJson.parse(data.asJsonObject)
  }

  /** Loads every favorites page. Throws if the server cannot be read completely; never returns a partial set. */
  suspend fun favoriteIds(): Set<String> {
    val ids = linkedSetOf<String>()
    var offset = 0
    while (true) {
      val body = backend.request(
        "GET",
        "api/v1/me/favorites",
        mapOf("limit" to FAVORITES_PAGE_SIZE.toString(), "offset" to offset.toString()),
      ) ?: invalid()
      val items = dataArray(body)
      for (element in items) {
        val id = element.asObjectOrInvalid().get("partner_id")
        if (id == null || !id.isJsonPrimitive || !isUuid(id.asString)) invalid()
        ids += id.asString.lowercase()
      }
      if (!hasMore(body)) return ids
      if (items.size() == 0) invalid()
      offset += items.size()
      if (offset > MAX_OFFSET) invalid()
    }
  }

  suspend fun setFavorite(partnerId: String, favorite: Boolean) {
    require(isUuid(partnerId))
    backend.request(
      if (favorite) "PUT" else "DELETE",
      "api/v1/me/favorites/$partnerId",
      expectedStatuses = setOf(204),
    )
  }

  private fun dataArray(body: JsonObject): JsonArray {
    val data = body.get("data")
    if (data == null || !data.isJsonArray) invalid()
    return data.asJsonArray
  }

  private fun hasMore(body: JsonObject): Boolean {
    val pagination = body.get("pagination")
    if (pagination == null || !pagination.isJsonObject) invalid()
    val flag = pagination.asJsonObject.get("has_more")
    if (flag == null || !flag.isJsonPrimitive || !flag.asJsonPrimitive.isBoolean) invalid()
    return flag.asBoolean
  }

  private fun JsonElement.asObjectOrInvalid(): JsonObject = if (isJsonObject) asJsonObject else invalid()

  private fun invalid(): Nothing =
    throw BackendException(null, "invalid_response", "Respons server tidak dapat dibaca. Silakan coba lagi.")

  companion object {
    const val PAGE_SIZE = 20
    const val FAVORITES_PAGE_SIZE = 100
    const val MAX_OFFSET = 10_000
    const val MAX_QUERY_CODE_POINTS = 100

    private val uuidPattern =
      Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

    fun isUuid(value: String): Boolean = uuidPattern.matches(value)
  }
}
