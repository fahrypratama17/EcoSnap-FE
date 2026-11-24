package com.example.ecosnap.features.deposits

import com.example.ecosnap.core.data.PartnerJson
import com.example.ecosnap.core.network.BackendClient
import com.example.ecosnap.core.network.invalidResponse
import com.google.gson.JsonArray
import com.google.gson.JsonObject

/** Server-backed deposits, QR, claims and estimates. No local data source. */
class DepositRepository(private val backend: BackendClient = BackendClient.default) {

  suspend fun categories(): List<WasteOption> {
    val body = backend.request("GET", "api/v1/waste-categories") ?: throw invalidResponse()
    val data = body.get("data") as? JsonArray ?: throw invalidResponse()
    return data.map {
      val category = PartnerJson.parseCategory(it as? JsonObject ?: throw invalidResponse())
      WasteOption(category.id, category.name)
    }
  }

  /** [items] are canonical (category id, "x.xxx" weight) pairs, already unique. */
  suspend fun estimate(items: JsonArray): PointEstimate {
    val payload = JsonObject().apply { add("items", items) }
    val body = backend.request("POST", "api/v1/point-estimates", body = payload)
    return DepositJson.estimate(DepositJson.data(body))
  }

  /** Sends the exact persisted payload with its idempotency key; 201 new, 200 replay. */
  suspend fun create(payload: JsonObject, idempotencyKey: String): DepositDetail {
    val body = backend.request(
      "POST", "api/v1/deposits",
      body = payload, expectedStatuses = setOf(200, 201), idempotencyKey = idempotencyKey,
    )
    return DepositJson.detail(DepositJson.data(body))
  }

  suspend fun partnerDeposit(id: String): DepositDetail {
    require(DepositJson.isUuid(id))
    return DepositJson.detail(DepositJson.data(backend.request("GET", "api/v1/deposits/$id")))
  }

  suspend fun claimedDeposit(id: String): DepositDetail {
    require(DepositJson.isUuid(id))
    return DepositJson.detail(DepositJson.data(backend.request("GET", "api/v1/me/deposits/$id")))
  }

  suspend fun partnerDeposits(status: String?, cursor: String?, limit: Int = PAGE_SIZE): DepositPage {
    val query = linkedMapOf("limit" to limit.toString())
    if (status != null) query["status"] = status
    if (cursor != null) query["cursor"] = cursor
    val body = backend.request("GET", "api/v1/partner/deposits", query) ?: throw invalidResponse()
    val data = body.get("data") as? JsonArray ?: throw invalidResponse()
    val pagination = body.get("pagination") as? JsonObject ?: throw invalidResponse()
    val next = pagination.get("next_cursor")
    val nextCursor = when {
      next == null || next.isJsonNull -> null
      next.isJsonPrimitive && next.asJsonPrimitive.isString && next.asString.isNotEmpty() -> next.asString
      else -> throw invalidResponse()
    }
    return DepositPage(data.map { DepositJson.summary(it as? JsonObject ?: throw invalidResponse()) }, nextCursor)
  }

  /** Issues, or reissues (the previous token stops working), the QR. [startedElapsedMs] is monotonic. */
  suspend fun issueQr(depositId: String, startedElapsedMs: Long): IssuedQr {
    require(DepositJson.isUuid(depositId))
    val body = backend.request("POST", "api/v1/deposits/$depositId/qr", expectedStatuses = setOf(201))
    return DepositJson.issuedQr(DepositJson.data(body), startedElapsedMs)
  }

  suspend fun previewClaim(token: String): ClaimPreview {
    val body = backend.request("POST", "api/v1/deposit-claims/preview", body = tokenBody(token))
    return DepositJson.preview(DepositJson.data(body))
  }

  suspend fun claim(token: String): ClaimResult {
    val body = backend.request(
      "POST", "api/v1/deposit-claims", body = tokenBody(token), expectedStatuses = setOf(200, 201),
    )
    return DepositJson.claim(DepositJson.data(body))
  }

  private fun tokenBody(token: String) = JsonObject().apply { addProperty("token", token) }

  companion object {
    const val PAGE_SIZE = 20
  }
}
