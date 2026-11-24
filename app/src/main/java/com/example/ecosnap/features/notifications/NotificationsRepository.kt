package com.example.ecosnap.features.notifications

import com.example.ecosnap.core.network.BackendClient
import com.example.ecosnap.core.network.BackendException
import com.example.ecosnap.core.network.invalidResponse
import com.example.ecosnap.core.network.requireLong
import com.example.ecosnap.core.network.requireObject
import com.example.ecosnap.core.network.requireString
import com.google.gson.JsonArray
import com.google.gson.JsonObject

data class AppNotification(
  val id: String,
  val type: String,
  val title: String,
  val body: String,
  /** Null when the server sent no target; [targetType]/[targetId] stay as received, never reinterpreted. */
  val targetType: String?,
  val targetId: String?,
  val createdAt: String,
  val seenAt: String?
) {
  val isUnread: Boolean get() = seenAt == null
}

data class NotificationPage(
  val items: List<AppNotification>,
  val unreadCount: Long,
  val nextCursor: String?
)

/** In-app inbox (`/me/notifications`). Pull-only: the backend sends no push, email, or SMS. */
class NotificationsRepository(private val backend: BackendClient = BackendClient.default) {
  suspend fun list(cursor: String?, unreadOnly: Boolean, limit: Int = PAGE_SIZE): NotificationPage {
    val query = linkedMapOf("limit" to limit.toString())
    if (!cursor.isNullOrEmpty()) query["cursor"] = cursor
    if (unreadOnly) query["unread"] = "true"

    val root = backend.request("GET", "api/v1/me/notifications", query) ?: throw invalidResponse()
    val data = root.get("data") as? JsonArray ?: throw invalidResponse()
    val pagination = root.requireObject("pagination")
    val next = pagination.get("next_cursor")
    return NotificationPage(
      items = data.map { parse(it as? JsonObject ?: throw invalidResponse()) },
      unreadCount = root.requireLong("unread_count"),
      nextCursor = if (next == null || next.isJsonNull) null else pagination.requireString("next_cursor")
    )
  }

  /** Idempotent on the server: `seen_at` is set once and never changes. */
  suspend fun markRead(id: String): AppNotification {
    if (!UUID_PATTERN.matches(id)) {
      throw BackendException(404, "notification_not_found", "Notifikasi tidak ditemukan.")
    }
    val root = backend.request("PATCH", "api/v1/me/notifications/${id.lowercase()}/read")
      ?: throw invalidResponse()
    val notification = parse(root.requireObject("data"))
    if (!notification.id.equals(id, ignoreCase = true)) throw invalidResponse()
    return notification
  }

  private fun parse(item: JsonObject): AppNotification {
    val target = item.get("target")
    var targetType: String? = null
    var targetId: String? = null
    if (target != null && !target.isJsonNull) {
      val obj = target as? JsonObject ?: throw invalidResponse()
      targetType = obj.requireString("type")
      targetId = obj.requireString("id")
    }
    val seen = item.get("seen_at")
    return AppNotification(
      id = item.requireString("id"),
      type = item.requireString("type"),
      title = item.requireString("title"),
      body = item.requireString("body", allowEmpty = true),
      targetType = targetType,
      targetId = targetId,
      createdAt = item.requireString("created_at"),
      seenAt = if (seen == null || seen.isJsonNull) null else item.requireString("seen_at")
    )
  }

  companion object {
    const val PAGE_SIZE = 20
    internal val UUID_PATTERN = Regex(
      "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    )
  }
}
