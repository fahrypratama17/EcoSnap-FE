package com.example.ecosnap.features.wallet

import com.example.ecosnap.core.network.invalidResponse
import com.example.ecosnap.core.network.optionalObject
import com.example.ecosnap.core.network.requireLong
import com.example.ecosnap.core.network.requireObject
import com.example.ecosnap.core.network.requireString
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

data class Wallet(val balance: Long, val totalEarned: Long, val totalSpent: Long)

data class Reward(
  val id: String,
  val name: String,
  val description: String,
  val terms: String,
  val costPoints: Long,
  val stock: Long,
  val available: Boolean,
  val isDemo: Boolean
)

enum class VoucherStatus(val wire: String) {
  Issued("issued"), Used("used"), Expired("expired");

  companion object {
    fun fromWire(value: String): VoucherStatus = entries.firstOrNull { it.wire == value } ?: throw invalidResponse()
  }
}

/** [code] is only filled by the detail/use endpoints and is never persisted or logged. */
data class Voucher(
  val id: String,
  val rewardId: String,
  val rewardName: String,
  val costPoints: Long,
  val status: VoucherStatus,
  val createdAt: String,
  val expiresAt: String,
  val usedAt: String?,
  val code: String? = null
)

sealed interface PointSource {
  data class Deposit(val depositId: String, val partnerName: String, val weightKg: String) : PointSource
  data class Redemption(val voucherId: String, val rewardName: String) : PointSource
  /** A source this app version does not know; shown generically. */
  data class Other(val source: String) : PointSource
}

data class PointEntry(val id: String, val amount: Long, val createdAt: String, val source: PointSource)

data class Page<T>(val items: List<T>, val nextCursor: String?)

data class RedeemResult(val voucherId: String, val balance: Long)

internal fun JsonObject.requireBool(name: String): Boolean =
  (get(name) as? JsonPrimitive)?.takeIf { it.isBoolean }?.asBoolean ?: throw invalidResponse()

internal fun JsonObject.optionalString(name: String): String? {
  val element: JsonElement = get(name) ?: return null
  if (element.isJsonNull) return null
  return (element as? JsonPrimitive)?.takeIf { it.isString }?.asString ?: throw invalidResponse()
}

internal fun JsonObject.requireArray(name: String): JsonArray = get(name) as? JsonArray ?: throw invalidResponse()

internal fun JsonObject.requireNonNegative(name: String): Long = requireLong(name).also { if (it < 0) throw invalidResponse() }

internal fun JsonObject.toWallet() = Wallet(
  balance = requireLong("balance"),
  totalEarned = requireNonNegative("total_earned"),
  totalSpent = requireNonNegative("total_spent")
)

internal fun JsonObject.toReward() = Reward(
  id = requireString("id"),
  name = requireString("name"),
  description = requireString("description", allowEmpty = true),
  terms = requireString("terms", allowEmpty = true),
  costPoints = requireNonNegative("cost_points"),
  stock = requireNonNegative("stock"),
  available = requireBool("available"),
  isDemo = requireBool("is_demo")
)

internal fun JsonObject.toVoucher() = Voucher(
  id = requireString("id"),
  rewardId = requireString("reward_id"),
  rewardName = requireString("reward_name"),
  costPoints = requireNonNegative("cost_points"),
  status = VoucherStatus.fromWire(requireString("status")),
  createdAt = requireString("created_at"),
  expiresAt = requireString("expires_at"),
  usedAt = optionalString("used_at"),
  code = optionalString("code")
)

internal fun JsonObject.toPointEntry(): PointEntry {
  val source = requireString("source")
  val detail: PointSource = when (source) {
    "deposit" -> requireObject("deposit").let {
      PointSource.Deposit(
        depositId = it.requireString("id"),
        partnerName = it.requireString("partner_name", allowEmpty = true),
        weightKg = it.requireString("total_weight_kg")
      )
    }
    "redemption" -> requireObject("redemption").let {
      PointSource.Redemption(voucherId = it.requireString("id"), rewardName = it.requireString("reward_name", allowEmpty = true))
    }
    else -> {
      optionalObject("deposit")
      PointSource.Other(source)
    }
  }
  return PointEntry(id = requireString("id"), amount = requireLong("amount"), createdAt = requireString("created_at"), source = detail)
}

internal fun <T> JsonObject.toPage(parse: (JsonObject) -> T): Page<T> {
  val items = requireArray("data").map { (it as? JsonObject ?: throw invalidResponse()).let(parse) }
  val cursor = requireObject("pagination").optionalString("next_cursor")?.takeIf { it.isNotEmpty() }
  return Page(items, cursor)
}
