package com.example.ecosnap.features.wallet

import com.example.ecosnap.core.network.BackendClient
import com.example.ecosnap.core.network.requireLong
import com.example.ecosnap.core.network.requireObject
import com.example.ecosnap.core.network.requireString
import com.google.gson.JsonObject

class WalletRepository(private val client: BackendClient = BackendClient.default) {
  suspend fun wallet(): Wallet =
    client.request("GET", "api/v1/me/wallet")!!.requireObject("data").toWallet()

  suspend fun rewards(): List<Reward> =
    client.request("GET", "api/v1/rewards")!!.requireArray("data").map {
      (it as? JsonObject ?: throw com.example.ecosnap.core.network.invalidResponse()).toReward()
    }

  suspend fun reward(id: String): Reward =
    client.request("GET", "api/v1/rewards/$id")!!.requireObject("data").toReward()

  /** One POST per call; [key] is the persisted idempotency key and is reused verbatim for every retry. */
  suspend fun redeem(rewardId: String, key: String): RedeemResult {
    val data = client.request(
      method = "POST",
      path = "api/v1/rewards/$rewardId/redeem",
      expectedStatuses = setOf(200, 201),
      idempotencyKey = key
    )!!.requireObject("data")
    val voucher = data.requireObject("voucher")
    return RedeemResult(voucher.requireString("id"), data.requireLong("wallet_balance"))
  }

  suspend fun history(type: String?, from: String?, to: String?, cursor: String?, limit: Int = PAGE_SIZE): Page<PointEntry> {
    val query = LinkedHashMap<String, String>()
    query["limit"] = limit.toString()
    cursor?.let { query["cursor"] = it }
    type?.let { query["type"] = it }
    from?.let { query["from"] = it }
    to?.let { query["to"] = it }
    return client.request("GET", "api/v1/me/point-history", query)!!.toPage { it.toPointEntry() }
  }

  suspend fun vouchers(status: VoucherStatus?, cursor: String?, limit: Int = PAGE_SIZE): Page<Voucher> {
    val query = LinkedHashMap<String, String>()
    query["limit"] = limit.toString()
    cursor?.let { query["cursor"] = it }
    status?.let { query["status"] = it.wire }
    return client.request("GET", "api/v1/me/vouchers", query)!!.toPage { it.toVoucher().copy(code = null) }
  }

  suspend fun voucher(id: String): Voucher =
    client.request("GET", "api/v1/me/vouchers/$id")!!.requireObject("data").toVoucher()

  suspend fun useVoucher(id: String): Voucher =
    client.request("POST", "api/v1/me/vouchers/$id/use")!!.requireObject("data").toVoucher()

  companion object {
    const val PAGE_SIZE = 20
  }
}
