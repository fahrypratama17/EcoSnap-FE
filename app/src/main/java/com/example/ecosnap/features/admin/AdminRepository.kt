package com.example.ecosnap.features.admin

import com.example.ecosnap.core.network.BackendClient
import com.example.ecosnap.core.network.invalidResponse
import com.example.ecosnap.core.network.requireObject
import com.example.ecosnap.core.network.requireString
import com.google.gson.JsonArray
import com.google.gson.JsonObject

enum class RegistrationStatus(val apiValue: String, val label: String) {
  PENDING("pending", "Menunggu"),
  APPROVED("approved", "Disetujui"),
  REJECTED("rejected", "Ditolak")
}

data class PartnerRegistration(
  val mitraUid: String,
  val fullName: String,
  val email: String,
  val phone: String,
  val businessName: String,
  val businessCategory: String,
  val status: String,
  val createdAt: String
)

/** Already validated by the form; sent as-is. [acceptedWaste] holds category slugs. */
data class ApprovalInput(
  val area: String,
  val address: String,
  val latitude: Double,
  val longitude: Double,
  val phone: String,
  val openingHours: String,
  val description: String,
  val acceptedWaste: List<String>
)

/** Admin-only partner registration endpoints. Waste categories come from PartnerRepository.categories(). */
class AdminRepository(private val backend: BackendClient = BackendClient.default) {

  suspend fun registrations(status: RegistrationStatus, limit: Int = LIST_LIMIT): List<PartnerRegistration> {
    val query = mapOf("status" to status.apiValue, "limit" to limit.toString())
    val root = backend.request("GET", "api/v1/admin/partner-registrations", query) ?: throw invalidResponse()
    val data = root.get("data") as? JsonArray ?: throw invalidResponse()
    return data.map { parse(it as? JsonObject ?: throw invalidResponse()) }
  }

  /** Returns the created partner id. */
  suspend fun approve(mitraUid: String, input: ApprovalInput): String {
    val payload = JsonObject().apply {
      addProperty("area", input.area)
      addProperty("address", input.address)
      addProperty("latitude", input.latitude)
      addProperty("longitude", input.longitude)
      addProperty("phone", input.phone)
      addProperty("opening_hours", input.openingHours)
      addProperty("description", input.description)
      add("accepted_waste", JsonArray().also { array -> input.acceptedWaste.forEach { array.add(it) } })
    }
    val root = backend.request(
      "POST", "api/v1/admin/partner-registrations/$mitraUid/approve", body = payload
    ) ?: throw invalidResponse()
    val data = root.requireObject("data")
    if (data.requireString("status") != "verified") throw invalidResponse()
    return data.requireString("partner_id")
  }

  suspend fun reject(mitraUid: String) {
    val root = backend.request("POST", "api/v1/admin/partner-registrations/$mitraUid/reject")
      ?: throw invalidResponse()
    if (root.requireObject("data").requireString("status") != "rejected") throw invalidResponse()
  }

  private fun parse(item: JsonObject) = PartnerRegistration(
    mitraUid = item.requireString("mitra_uid"),
    fullName = item.requireString("full_name", allowEmpty = true),
    email = item.requireString("email", allowEmpty = true),
    phone = item.requireString("phone", allowEmpty = true),
    businessName = item.requireString("business_name"),
    businessCategory = item.requireString("business_category", allowEmpty = true),
    status = item.requireString("status"),
    createdAt = item.requireString("created_at", allowEmpty = true)
  )

  private companion object {
    const val LIST_LIMIT = 100
  }
}
