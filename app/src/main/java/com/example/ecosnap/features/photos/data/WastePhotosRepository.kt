package com.example.ecosnap.features.photos.data

import android.content.Context
import android.net.Uri
import com.example.ecosnap.BuildConfig
import com.example.ecosnap.features.photos.model.WasteScanOutcome
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.IOException
import java.io.InterruptedIOException
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

/**
 * A failed AI scan. [message] is user-safe Indonesian text, [code] the service error code or a client
 * code (`network`, `timeout`, `invalid_response`, `bad_image`, `not_configured`). [retryAfterSeconds]
 * is only the service's own Retry-After. [freshScanId] means the same scan id must not be reused.
 */
class AiScanException(
  val code: String,
  message: String,
  val retryable: Boolean,
  val retryAfterSeconds: Int? = null,
  val freshScanId: Boolean = false
) : Exception(message)

interface WastePhotosRepository {
  /** False when no AI service origin is configured for this build; [analyze] then always fails. */
  val isAvailable: Boolean

  /**
   * Uploads [imageUri] to the AI service as one multipart `image` part. [scanId] is the idempotency
   * UUID: reuse it only to retry the same image. Cancelling the coroutine cancels the HTTP call.
   * @throws AiScanException on any failure
   */
  suspend fun analyze(imageUri: Uri, scanId: String): WasteScanOutcome
}

/**
 * Consumer of the separate FastAPI service (`POST {AI_SCAN_BASE_URL}api/scan`). It carries no API key and
 * never sends the Firebase token or any credential: the service is unauthenticated and its host is
 * independent of the Go backend.
 */
class RemoteWastePhotosRepository(
  context: Context,
  baseUrl: String = BuildConfig.AI_SCAN_BASE_URL,
  allowCleartext: Boolean = BuildConfig.DEBUG,
  client: OkHttpClient = defaultClient
) : WastePhotosRepository {
  private val appContext = context.applicationContext
  private val endpoint: HttpUrl? = resolveEndpoint(baseUrl, allowCleartext)
  private val http: OkHttpClient = client

  override val isAvailable: Boolean get() = endpoint != null

  override suspend fun analyze(imageUri: Uri, scanId: String): WasteScanOutcome {
    val url = endpoint ?: throw AiScanException(
      "not_configured",
      "Layanan analisis AI belum dikonfigurasi pada aplikasi ini.",
      retryable = false
    )
    val id = canonicalUuid(scanId) ?: throw AiScanException("bad_image", "Permintaan pemindaian tidak valid.", retryable = false)

    val bytes = try {
      ScanImagePreparer.prepare(appContext, imageUri)
    } catch (e: UnusableImageException) {
      throw AiScanException("bad_image", e.message ?: "Gambar tidak dapat dibaca.", retryable = false)
    }

    val body = MultipartBody.Builder()
      .setType(MultipartBody.FORM)
      .addFormDataPart("image", "scan.jpg", bytes.toRequestBody("image/jpeg".toMediaType()))
      .build()
    val request = Request.Builder()
      .url(url)
      .header("X-Scan-Id", id)
      .header("Accept", "application/json")
      .post(body)
      .build()

    val raw = try {
      execute(request)
    } catch (e: CancellationException) {
      throw e
    } catch (e: InterruptedIOException) {
      throw AiScanException("timeout", "Analisis memakan waktu terlalu lama. Silakan coba lagi.", retryable = true)
    } catch (e: IOException) {
      throw AiScanException("network", "Tidak dapat terhubung ke layanan analisis. Periksa koneksi internet Anda.", retryable = true)
    }

    if (raw.status == 200) {
      if (raw.tooLarge || raw.body == null || !raw.isJson) throw invalidResponse()
      return try {
        ScanResponseParser.parse(raw.body)
      } catch (e: InvalidScanResponse) {
        throw invalidResponse()
      }
    }
    throw failure(raw)
  }

  private fun invalidResponse() = AiScanException(
    "invalid_response",
    "Layanan analisis memberikan jawaban yang tidak dapat dipahami. Silakan coba lagi.",
    retryable = true
  )

  private fun failure(raw: Raw): AiScanException {
    val error = if (raw.tooLarge || raw.body == null || !raw.isJson) null else {
      runCatching { JsonParser.parseString(raw.body).asJsonObject.get("error") as? JsonObject }.getOrNull()
    }
    val code = error?.get("code")?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }
      ?.asString?.takeIf { it.length in 1..64 && it.all { c -> c.isLowerCase() || c == '_' } }
      ?: "http_${raw.status}"
    val retry = raw.retryAfter

    return when (code) {
      "invalid_upload", "missing_image", "unsupported_upload" ->
        AiScanException(code, "Gambar tidak dapat dikirim. Ambil atau pilih foto lain.", false)
      "invalid_scan_id" -> AiScanException(code, "Permintaan pemindaian tidak valid. Silakan coba lagi.", true, freshScanId = true)
      "scan_id_conflict" -> AiScanException(code, "Pemindaian bentrok dengan permintaan sebelumnya. Silakan coba lagi.", true, freshScanId = true)
      "upload_timeout" -> AiScanException(code, "Unggahan terlalu lama. Periksa koneksi lalu coba lagi.", true)
      "image_too_large" -> AiScanException(code, "Gambar terlalu besar. Pilih gambar yang lebih kecil.", false)
      "unsupported_image" -> AiScanException(code, "Format gambar tidak didukung. Gunakan JPEG, PNG, atau WebP statis.", false)
      "rate_limit", "provider_rate_limit" ->
        AiScanException(code, "Terlalu banyak pemindaian. Tunggu sebentar lalu coba lagi.", true, retry)
      "busy" -> AiScanException(code, "Layanan analisis sedang sibuk. Coba lagi sebentar lagi.", true, retry)
      "provider_timeout" -> AiScanException(code, "Analisis memakan waktu terlalu lama. Silakan coba lagi.", true)
      "provider_error", "truncated_response", "invalid_ai_response", "provider_network", "internal_error" ->
        AiScanException(code, "Layanan analisis gagal memproses foto. Silakan coba lagi.", true)
      "missing_api_key", "invalid_api_key", "model_unavailable" ->
        AiScanException(code, "Layanan analisis belum siap digunakan. Silakan coba lagi nanti.", false)
      else -> when (raw.status) {
        in 500..599 -> AiScanException(code, "Layanan analisis sedang bermasalah. Silakan coba lagi nanti.", true, retry)
        else -> AiScanException(code, "Permintaan analisis ditolak layanan.", false)
      }
    }
  }

  private class Raw(
    val status: Int,
    val body: String?,
    val tooLarge: Boolean,
    val isJson: Boolean,
    val retryAfter: Int?
  )

  private suspend fun execute(request: Request): Raw = suspendCancellableCoroutine { cont ->
    val call = http.newCall(request)
    cont.invokeOnCancellation { call.cancel() }
    call.enqueue(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        if (cont.isActive) cont.resumeWithException(e)
      }

      override fun onResponse(call: Call, response: Response) {
        val raw = try {
          response.use { read(it) }
        } catch (e: IOException) {
          if (cont.isActive) cont.resumeWithException(e)
          return
        }
        if (cont.isActive) cont.resume(raw)
      }
    })
  }

  private fun read(response: Response): Raw {
    val retryAfter = response.header("Retry-After")?.trim()?.toIntOrNull()?.takeIf { it in 1..3600 }
    val isJson = response.body.contentType()?.let { it.type == "application" && it.subtype == "json" } == true
    val body = response.body
    if (body.contentLength() > MAX_BODY_BYTES) return Raw(response.code, null, true, isJson, retryAfter)
    val source = body.source()
    source.request(MAX_BODY_BYTES + 1)
    if (source.buffer.size > MAX_BODY_BYTES) return Raw(response.code, null, true, isJson, retryAfter)
    return Raw(response.code, source.buffer.readUtf8(), false, isJson, retryAfter)
  }

  companion object {
    private const val MAX_BODY_BYTES = 512L * 1024

    /** Dedicated client: no interceptors, no auth, no cache, no redirects, no automatic retry. */
    private val defaultClient: OkHttpClient by lazy {
      OkHttpClient.Builder()
        .cache(null)
        .followRedirects(false)
        .followSslRedirects(false)
        .retryOnConnectionFailure(false)
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(95, TimeUnit.SECONDS)
        .callTimeout(100, TimeUnit.SECONDS)
        .build()
    }

    /** Pure origin only (no credentials/path/query/fragment); HTTPS unless [allowCleartext]. Null disables scanning. */
    internal fun resolveEndpoint(raw: String, allowCleartext: Boolean): HttpUrl? {
      val trimmed = raw.trim()
      if (trimmed.isEmpty()) return null
      val base = trimmed.toHttpUrlOrNull() ?: return null
      if (!base.isHttps && !allowCleartext) return null
      if (base.username.isNotEmpty() || base.password.isNotEmpty()) return null
      if (base.encodedPath != "/" || base.query != null || base.fragment != null) return null
      return base.newBuilder().addPathSegments("api/scan").build()
    }

    private fun canonicalUuid(value: String): String? = try {
      UUID.fromString(value).toString().takeIf { it.equals(value, ignoreCase = true) }
    } catch (_: IllegalArgumentException) {
      null
    }
  }
}
