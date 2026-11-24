package com.example.ecosnap.features.photos.data

import com.example.ecosnap.features.photos.model.RecyclingStatus
import com.example.ecosnap.features.photos.model.ReuseIdea
import com.example.ecosnap.features.photos.model.WastePhotosResult
import com.google.gson.JsonObject
import com.google.gson.JsonParser

object ScanResponseParser {
  fun parse(json: String): WastePhotosResult {
    try {
      val root = JsonParser.parseString(json).asJsonObject
      val status = root.text("status")
      if (status in listOf("invalid_image", "multiple_objects")) throw ScanException(root.text("message"))
      require(status == "success")
      val id = root.getAsJsonObject("identification")
      val recycling = when (id.text("recycling_status")) {
        "recyclable" -> RecyclingStatus.RECYCLABLE
        "conditionally_recyclable" -> RecyclingStatus.CONDITIONALLY_RECYCLABLE
        "non_recyclable" -> RecyclingStatus.NON_RECYCLABLE
        "unknown" -> RecyclingStatus.UNKNOWN
        else -> error("Unknown recycling status")
      }
      val expected = when (recycling) {
        RecyclingStatus.RECYCLABLE, RecyclingStatus.CONDITIONALLY_RECYCLABLE -> true
        RecyclingStatus.NON_RECYCLABLE -> false
        RecyclingStatus.UNKNOWN -> null
      }
      require(id.optionalBoolean("recyclable") == expected)
      val hazardous = id.boolean("hazardous")
      val upcycling = root.getAsJsonObject("upcycling")
      val ideas = upcycling.getAsJsonArray("ideas").mapIndexed { index, value ->
        val idea = value.asJsonObject
        ReuseIdea(
          id = "idea-$index", title = idea.text("title"), description = idea.text("description"),
          toolsAndMaterials = idea.strings("tools_and_materials").also { require(it.isNotEmpty()) },
          steps = idea.strings("steps").also { require(it.size in 3..6) },
          difficulty = idea.text("difficulty").also { require(it in listOf("Mudah", "Sedang", "Sulit")) },
          safetyWarning = idea.text("safety_warning")
        )
      }
      require(ideas.size <= 3 && upcycling.boolean("possible") == ideas.isNotEmpty())
      require(!hazardous || ideas.isEmpty())
      val reason = upcycling.optionalText("reason")
      require(ideas.isNotEmpty() || reason != null)
      val industrial = root.getAsJsonObject("industrial_recycling")
      val industryPossible = industrial.optionalBoolean("possible")
      val products = industrial.strings("potential_products")
      require(industryPossible == true || products.isEmpty())
      val deposit = root.getAsJsonObject("deposit_recommendation")
      val category = id.text("category").also { require(it in listOf("Organik", "Anorganik", "B3", "Residu", "Tidak Diketahui")) }
      require(category != "B3" || hazardous)
      val requiresVerification = id.boolean("requires_verification")
      val note = id.optionalText("verification_note")
      require(!requiresVerification || note != null)
      require(recycling != RecyclingStatus.UNKNOWN || requiresVerification)
      val recommended = deposit.boolean("recommended")
      val partnerType = deposit.optionalText("partner_type")
      val keyword = deposit.optionalText("search_keyword")
      require(!recommended || (partnerType != null && keyword != null))
      return WastePhotosResult(
        objectName = id.text("object_name"), category = category,
        material = id.optionalText("material") ?: "Material belum diketahui", tags = id.strings("tags"),
        description = id.text("description"), handlingSteps = root.strings("handling_steps").also { require(it.isNotEmpty()) },
        reuseIdeas = ideas, industryPotential = industrial.text("description"), recyclingStatus = recycling,
        verificationNote = note, hazardous = hazardous, upcyclingReason = reason, industryPossible = industryPossible,
        industryProducts = products, industryLimitations = industrial.strings("limitations"),
        depositRecommended = recommended, partnerType = partnerType, partnerSearchKeyword = keyword,
        depositPreparation = deposit.optionalText("preparation_note")
      )
    } catch (e: ScanException) {
      throw e
    } catch (_: Exception) {
      throw ScanException("Hasil analisis belum valid. Silakan coba lagi dengan foto yang jelas.")
    }
  }

  fun errorMessage(json: String, httpCode: Int): String {
    val code = runCatching { JsonParser.parseString(json).asJsonObject.getAsJsonObject("error").text("code") }.getOrNull()
    return when (code) {
      "missing_api_key", "invalid_api_key", "model_unavailable" -> "Layanan analisis belum siap. Hubungi pengelola EcoSnap."
      "image_too_large" -> "Foto terlalu besar. Gunakan foto maksimal 10 MB dan 25 megapiksel."
      "unsupported_image", "unsupported_upload" -> "Gunakan foto JPEG, PNG, atau WebP yang tidak rusak."
      "invalid_upload", "missing_image" -> "Foto tidak dapat diunggah. Pilih atau ambil foto kembali."
      "invalid_scan_id", "scan_id_conflict" -> "Foto atau identitas scan berubah. Pilih atau ambil foto kembali."
      "rate_limit", "provider_rate_limit" -> "Batas pemindaian sedang tercapai. Tunggu sebentar lalu coba lagi."
      "busy" -> "Layanan sedang sibuk. Tunggu sebentar lalu coba lagi."
      "provider_timeout", "upload_timeout" -> "Analisis terlalu lama. Periksa koneksi lalu coba lagi."
      "invalid_ai_response", "truncated_response" -> "Hasil analisis belum lengkap. Silakan coba lagi."
      else -> if (httpCode == 429) "Terlalu banyak pemindaian. Tunggu sebentar lalu coba lagi." else "Layanan analisis sedang mengalami gangguan. Silakan coba lagi."
    }
  }

  private fun JsonObject.text(key: String): String = optionalText(key) ?: error("Missing $key")
  private fun JsonObject.optionalText(key: String): String? {
    val value = get(key) ?: error("Missing $key")
    if (value.isJsonNull) return null
    require(value.isJsonPrimitive && value.asJsonPrimitive.isString)
    return value.asString.trim().also { require(it.isNotEmpty() && it.length <= 1000) }
  }
  private fun JsonObject.boolean(key: String): Boolean = optionalBoolean(key) ?: error("Missing $key")
  private fun JsonObject.optionalBoolean(key: String): Boolean? {
    val value = get(key) ?: error("Missing $key")
    if (value.isJsonNull) return null
    require(value.isJsonPrimitive && value.asJsonPrimitive.isBoolean)
    return value.asBoolean
  }
  private fun JsonObject.strings(key: String): List<String> = getAsJsonArray(key).map { value ->
    require(value.isJsonPrimitive && value.asJsonPrimitive.isString)
    value.asString.trim().also { require(it.isNotEmpty() && it.length <= 1000) }
  }.also { require(it.size <= 12) }
}
