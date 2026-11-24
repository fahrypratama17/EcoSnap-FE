package com.example.ecosnap.core.data

import com.example.ecosnap.core.model.GeoPoint
import com.example.ecosnap.core.model.Partner
import com.example.ecosnap.core.model.PartnerHours
import com.example.ecosnap.core.model.WasteCategory
import com.example.ecosnap.core.network.BackendException
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

/** Strict mapper for the partner wire format. Unknown fields are ignored; anything malformed throws. */
object PartnerJson {
  fun parse(value: JsonObject): Partner {
    val location = value.get("location")?.takeIf { it.isJsonObject }?.asJsonObject ?: invalid()
    val latitude = number(location, "latitude")
    val longitude = number(location, "longitude")
    if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) invalid()

    return Partner(
      id = uuid(value, "id").lowercase(),
      name = requiredText(value, "name"),
      area = text(value, "area"),
      address = requiredText(value, "address"),
      phone = text(value, "phone").trim(),
      openingHours = text(value, "opening_hours").trim(),
      description = text(value, "description").trim(),
      location = GeoPoint(latitude, longitude),
      acceptedMaterials = array(value, "accepted_materials").map { parseCategory(it.asObjectOrInvalid()) },
      hours = array(value, "hours").map { parseHours(it.asObjectOrInvalid()) },
    )
  }

  fun parseCategory(value: JsonObject): WasteCategory = WasteCategory(
    id = uuid(value, "id"),
    name = requiredText(value, "name"),
    slug = requiredText(value, "slug"),
    code = text(value, "code"),
    handlingGuide = text(value, "handling_guide").trim(),
    requiresSpecialHandling = bool(value, "requires_special_handling"),
  )

  private fun parseHours(value: JsonObject): PartnerHours {
    val day = number(value, "day_of_week")
    if (day != Math.floor(day) || day < 0 || day > 6) invalid()
    val closed = bool(value, "is_closed")
    val opens = text(value, "opens_at")
    val closes = text(value, "closes_at")
    if (!closed && (opens.isBlank() || closes.isBlank())) invalid()
    return PartnerHours(day.toInt(), opens, closes, closed)
  }

  private val uuidPattern = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

  private fun invalid(): Nothing =
    throw BackendException(null, "invalid_response", "Respons server tidak dapat dibaca. Silakan coba lagi.")

  private fun JsonElement.asObjectOrInvalid(): JsonObject = if (isJsonObject) asJsonObject else invalid()

  private fun primitive(obj: JsonObject?, key: String): JsonPrimitive? {
    val element = obj?.get(key) ?: return null
    return if (element.isJsonPrimitive) element.asJsonPrimitive else null
  }

  private fun text(obj: JsonObject?, key: String): String {
    val element = obj?.get(key)
    if (element == null || element.isJsonNull) return ""
    val p = primitive(obj, key) ?: invalid()
    if (!p.isString) invalid()
    return p.asString
  }

  private fun requiredText(obj: JsonObject?, key: String): String =
    text(obj, key).trim().ifEmpty { invalid() }

  private fun uuid(obj: JsonObject?, key: String): String =
    requiredText(obj, key).also { if (!uuidPattern.matches(it)) invalid() }

  private fun number(obj: JsonObject?, key: String): Double {
    val p = primitive(obj, key) ?: invalid()
    if (!p.isNumber) invalid()
    return p.asDouble.also { if (!it.isFinite()) invalid() }
  }

  private fun bool(obj: JsonObject?, key: String): Boolean {
    val p = primitive(obj, key) ?: invalid()
    if (!p.isBoolean) invalid()
    return p.asBoolean
  }

  private fun array(obj: JsonObject, key: String): JsonArray {
    val element = obj.get(key)
    if (element == null || element.isJsonNull) return JsonArray()
    return if (element.isJsonArray) element.asJsonArray else invalid()
  }
}
