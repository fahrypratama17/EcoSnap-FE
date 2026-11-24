package com.example.ecosnap.features.photos.data

import com.example.ecosnap.features.photos.model.NotIdentifiedStatus
import com.example.ecosnap.features.photos.model.RecyclingStatus
import com.example.ecosnap.features.photos.model.ReuseIdea
import com.example.ecosnap.features.photos.model.WasteScanOutcome
import com.example.ecosnap.features.photos.model.WastePhotosResult
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser

/** Thrown when the AI service answered 200 but the body violates the documented `ScanResponse` contract. */
internal class InvalidScanResponse(reason: String) : Exception(reason)

/**
 * Strict parser for the raw (unwrapped) `ScanResponse`: exact key sets, exact types, documented length
 * and cardinality bounds, and the documented semantic invariants. Anything else is rejected, never
 * partially shown.
 */
internal object ScanResponseParser {
  private val categories = setOf("Organik", "Anorganik", "B3", "Residu", "Tidak Diketahui")
  private val difficulties = setOf("Mudah", "Sedang", "Sulit")

  fun parse(body: String): WasteScanOutcome {
    val parsed: JsonElement = try {
      JsonParser.parseString(body)
    } catch (e: Exception) {
      bad("json")
    }
    val root = parsed as? JsonObject ?: bad("root")
    root.exact("status", "message", "identification", "handling_steps", "upcycling", "industrial_recycling", "deposit_recommendation")

    val statusText = root.string("status", 32, nullable = false)!!
    val message = root.string("message", 900, nullable = true)
    val handlingSteps = root.strings("handling_steps", min = 0, max = 6, itemMax = 900)
    val upcyclingObj = root.obj("upcycling", nullable = false)!!
    val identificationObj = root.obj("identification", nullable = true)
    val industrialObj = root.obj("industrial_recycling", nullable = true)
    val depositObj = root.obj("deposit_recommendation", nullable = true)

    val upcycling = parseUpcycling(upcyclingObj)

    if (statusText != "success") {
      val status = when (statusText) {
        "invalid_image" -> NotIdentifiedStatus.InvalidImage
        "multiple_objects" -> NotIdentifiedStatus.MultipleObjects
        else -> bad("status")
      }
      if (message == null || identificationObj != null || industrialObj != null || depositObj != null) bad("non-success shape")
      if (handlingSteps.isNotEmpty() || upcycling.possible || upcycling.ideas.isNotEmpty()) bad("non-success content")
      return WasteScanOutcome.NotIdentified(status, message)
    }

    if (identificationObj == null || industrialObj == null || depositObj == null) bad("success shape")
    if (handlingSteps.isEmpty()) bad("handling")
    val id = parseIdentification(identificationObj)
    if (id.hazardous && upcycling.ideas.isNotEmpty()) bad("hazardous ideas")
    val industrial = parseIndustrial(industrialObj)
    val deposit = parseDeposit(depositObj)

    when (id.status) {
      RecyclingStatus.NonRecyclable -> if (industrial.possible == true) bad("non-recyclable industrial")
      RecyclingStatus.ConditionallyRecyclable -> if (industrial.limitations.isEmpty()) bad("limitations")
      else -> Unit
    }

    return WasteScanOutcome.Identified(
      WastePhotosResult(
        objectName = id.objectName,
        category = id.category,
        material = id.material,
        recyclable = id.recyclable,
        recyclingStatus = id.status,
        tags = id.tags,
        description = id.description,
        requiresVerification = id.requiresVerification,
        verificationNote = id.verificationNote,
        hazardous = id.hazardous,
        handlingSteps = handlingSteps,
        upcyclingPossible = upcycling.possible,
        upcyclingReason = upcycling.reason,
        reuseIdeas = upcycling.ideas,
        industrialDescription = industrial.description,
        industrialPossible = industrial.possible,
        potentialProducts = industrial.potentialProducts,
        industrialLimitations = industrial.limitations,
        depositRecommended = deposit.recommended,
        depositPartnerType = deposit.partnerType,
        depositSearchKeyword = deposit.searchKeyword,
        depositPreparationNote = deposit.preparationNote
      )
    )
  }

  private class Ident(
    val objectName: String, val material: String?, val category: String, val recyclable: Boolean?,
    val status: RecyclingStatus, val tags: List<String>, val description: String,
    val requiresVerification: Boolean, val verificationNote: String?, val hazardous: Boolean
  )

  private fun parseIdentification(o: JsonObject): Ident {
    o.exact(
      "object_name", "material", "category", "recyclable", "recycling_status", "tags",
      "description", "requires_verification", "verification_note", "hazardous"
    )
    val name = o.string("object_name", 160, nullable = true) ?: bad("object_name")
    val material = o.string("material", 160, nullable = true)
    val category = o.string("category", 32, nullable = true) ?: bad("category")
    if (category !in categories) bad("category enum")
    val recyclable = o.bool("recyclable", nullable = true)
    val status = when (o.string("recycling_status", 40, nullable = false)) {
      "recyclable" -> RecyclingStatus.Recyclable
      "conditionally_recyclable" -> RecyclingStatus.ConditionallyRecyclable
      "non_recyclable" -> RecyclingStatus.NonRecyclable
      "unknown" -> RecyclingStatus.Unknown
      else -> bad("recycling_status")
    }
    val tags = o.strings("tags", min = 0, max = 5, itemMax = 160)
    val description = o.string("description", 900, nullable = true) ?: bad("description")
    val requiresVerification = o.bool("requires_verification", nullable = false)!!
    val verificationNote = o.string("verification_note", 900, nullable = true)
    val hazardous = o.bool("hazardous", nullable = false)!!

    val expectedRecyclable = when (status) {
      RecyclingStatus.Recyclable, RecyclingStatus.ConditionallyRecyclable -> true
      RecyclingStatus.NonRecyclable -> false
      RecyclingStatus.Unknown -> null
    }
    if (recyclable != expectedRecyclable) bad("recyclable mismatch")
    if (status == RecyclingStatus.Unknown && !requiresVerification) bad("unknown verification")
    if (requiresVerification && verificationNote == null) bad("verification note")
    if (category == "B3" && !hazardous) bad("B3 hazardous")
    return Ident(name, material, category, recyclable, status, tags, description, requiresVerification, verificationNote, hazardous)
  }

  private class Upcycle(val possible: Boolean, val reason: String?, val ideas: List<ReuseIdea>)

  private fun parseUpcycling(o: JsonObject): Upcycle {
    o.exact("possible", "reason", "ideas")
    val possible = o.bool("possible", nullable = false)!!
    val reason = o.string("reason", 900, nullable = true)
    val array = o.array("ideas")
    if (array.size() > 3) bad("ideas size")
    val ideas = array.mapIndexed { index, element ->
      val idea = element as? JsonObject ?: bad("idea")
      idea.exact("title", "description", "difficulty", "tools_and_materials", "steps", "safety_warning")
      val difficulty = idea.string("difficulty", 16, nullable = false)!!
      if (difficulty !in difficulties) bad("difficulty")
      ReuseIdea(
        id = "idea-$index",
        title = idea.string("title", 160, nullable = false)!!,
        description = idea.string("description", 900, nullable = false)!!,
        difficulty = difficulty,
        toolsAndMaterials = idea.strings("tools_and_materials", min = 1, max = 12, itemMax = 160),
        steps = idea.strings("steps", min = 3, max = 6, itemMax = 900),
        safetyWarning = idea.string("safety_warning", 900, nullable = false)!!
      )
    }
    if (ideas.map { it.title.lowercase() }.toSet().size != ideas.size) bad("duplicate idea titles")
    if (possible != ideas.isNotEmpty()) bad("upcycling possible mismatch")
    if (!possible && reason == null) bad("upcycling reason")
    return Upcycle(possible, reason, ideas)
  }

  private class Industrial(val possible: Boolean?, val description: String, val potentialProducts: List<String>, val limitations: List<String>)

  private fun parseIndustrial(o: JsonObject): Industrial {
    o.exact("possible", "description", "potential_products", "limitations")
    val possible = o.bool("possible", nullable = true)
    val description = o.string("description", 900, nullable = true) ?: bad("industrial description")
    val products = o.strings("potential_products", min = 0, max = 6, itemMax = 160)
    val limitations = o.strings("limitations", min = 0, max = 6, itemMax = 900)
    if (possible != true && products.isNotEmpty()) bad("products without possibility")
    return Industrial(possible, description, products, limitations)
  }

  private class Deposit(val recommended: Boolean, val partnerType: String?, val searchKeyword: String?, val preparationNote: String?)

  private fun parseDeposit(o: JsonObject): Deposit {
    o.exact("recommended", "partner_type", "search_keyword", "preparation_note")
    val recommended = o.bool("recommended", nullable = false)!!
    val partnerType = o.string("partner_type", 160, nullable = true)
    val keyword = o.string("search_keyword", 160, nullable = true)
    val note = o.string("preparation_note", 900, nullable = true)
    if (recommended && (partnerType == null || keyword == null)) bad("deposit fields")
    return Deposit(recommended, partnerType, keyword, note)
  }

  // --- primitives -----------------------------------------------------------------------------

  private fun bad(reason: String): Nothing = throw InvalidScanResponse(reason)

  private fun JsonObject.exact(vararg keys: String) {
    if (keySet() != keys.toSet()) bad("keys")
  }

  private fun JsonObject.value(name: String): JsonElement = get(name) ?: bad("missing $name")

  private fun JsonObject.string(name: String, max: Int, nullable: Boolean): String? {
    val element = value(name)
    if (element is JsonNull) return if (nullable) null else bad("null $name")
    val primitive = element.takeIf { it.isJsonPrimitive }?.asJsonPrimitive
    if (primitive == null || !primitive.isString) bad("type $name")
    return primitive.asString.checked(max)
  }

  private fun String.checked(max: Int): String {
    val text = trim()
    if (text.isEmpty() || text.codePointCount(0, text.length) > max) bad("length")
    return text
  }

  private fun JsonObject.bool(name: String, nullable: Boolean): Boolean? {
    val element = value(name)
    if (element is JsonNull) return if (nullable) null else bad("null $name")
    val primitive = element.takeIf { it.isJsonPrimitive }?.asJsonPrimitive
    if (primitive == null || !primitive.isBoolean) bad("type $name")
    return primitive.asBoolean
  }

  private fun JsonObject.obj(name: String, nullable: Boolean): JsonObject? {
    val element = value(name)
    if (element is JsonNull) return if (nullable) null else bad("null $name")
    return element as? JsonObject ?: bad("type $name")
  }

  private fun JsonObject.array(name: String): JsonArray = value(name) as? JsonArray ?: bad("type $name")

  private fun JsonObject.strings(name: String, min: Int, max: Int, itemMax: Int): List<String> {
    val array = array(name)
    if (array.size() < min || array.size() > max) bad("size $name")
    return array.map { item ->
      val primitive = item.takeIf { it.isJsonPrimitive }?.asJsonPrimitive
      if (primitive == null || !primitive.isString) bad("item $name")
      primitive.asString.checked(itemMax)
    }
  }
}
