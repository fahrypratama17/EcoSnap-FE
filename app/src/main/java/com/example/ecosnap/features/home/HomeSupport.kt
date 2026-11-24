package com.example.ecosnap.features.home

import com.example.ecosnap.core.network.BackendException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

internal fun Throwable.userMessage(fallback: String): String =
  (this as? BackendException)?.message?.takeIf { it.isNotBlank() } ?: fallback

/**
 * An outcome that does not prove the server rejected the request: the client may retry with the same
 * intent token. Definitive 4xx answers (except 408/429) are not ambiguous.
 */
internal fun Throwable.isAmbiguous(): Boolean {
  val e = this as? BackendException ?: return true
  val status = e.status ?: return true
  return status >= 500 || status == 408 || status == 429
}

private val FRACTION = Regex("(\\.\\d+)")

/** Formats an RFC 3339 server timestamp in WIB for display; null when it cannot be read. */
internal fun formatServerTime(value: String, pattern: String): String? {
  val normalized = FRACTION.replace(value.trim(), "").let {
    if (it.endsWith("Z")) it.dropLast(1) + "+00:00" else it
  }
  return try {
    val parsed = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).parse(normalized) ?: return null
    SimpleDateFormat(pattern, Locale.forLanguageTag("id-ID")).apply {
      timeZone = TimeZone.getTimeZone("Asia/Jakarta")
    }.format(parsed)
  } catch (_: Exception) {
    null
  }
}
