package com.example.ecosnap.features.photos.data

import android.content.Context
import android.net.Uri
import com.example.ecosnap.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ScanException(val userMessage: String, val retryAfterSeconds: Int = 0) : Exception(userMessage)

fun interface WastePhotosRepository {
  suspend fun analyze(imageUri: Uri, scanId: String): String
}

class HttpWastePhotosRepository(
  private val context: Context,
  private val baseUrl: String = BuildConfig.SCAN_API_BASE_URL,
  private val client: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(95, TimeUnit.SECONDS)
    .callTimeout(100, TimeUnit.SECONDS)
    .retryOnConnectionFailure(false)
    .followRedirects(false)
    .build()
) : WastePhotosRepository {
  override suspend fun analyze(imageUri: Uri, scanId: String): String = withContext(Dispatchers.IO) {
    if (baseUrl.isBlank() || (!BuildConfig.DEBUG && !baseUrl.startsWith("https://"))) {
      throw ScanException("Alamat layanan analisis belum dikonfigurasi. Hubungi pengelola EcoSnap.")
    }
    val bytes = try {
      context.contentResolver.openInputStream(imageUri)?.use { input ->
        val buffer = java.io.ByteArrayOutputStream()
        val chunk = ByteArray(8192)
        while (true) {
          val count = input.read(chunk)
          if (count < 0) break
          if (buffer.size() + count > MAX_IMAGE_BYTES) throw ScanException("Gunakan gambar berukuran maksimal 10 MB.")
          buffer.write(chunk, 0, count)
        }
        buffer.toByteArray()
      } ?: throw ScanException("Foto tidak dapat dibaca. Pilih atau ambil foto kembali.")
    } catch (e: ScanException) {
      throw e
    } catch (_: IOException) {
      throw ScanException("Foto tidak dapat dibaca. Pilih atau ambil foto kembali.")
    }
    val body = MultipartBody.Builder().setType(MultipartBody.FORM)
      .addFormDataPart("image", "scan-image", bytes.toRequestBody("application/octet-stream".toMediaType()))
      .build()
    val request = Request.Builder().url(baseUrl.trimEnd('/') + "/api/scan")
      .header("X-Scan-Id", scanId).post(body).build()
    try {
      client.newCall(request).awaitText()
    } catch (e: ScanException) {
      throw e
    } catch (_: java.io.InterruptedIOException) {
      throw ScanException("Analisis terlalu lama. Periksa koneksi lalu coba lagi.")
    } catch (_: IOException) {
      throw ScanException("Tidak dapat terhubung ke layanan analisis. Periksa koneksi lalu coba lagi.")
    }
  }

  companion object { const val MAX_IMAGE_BYTES = 10 * 1024 * 1024 }
}

private suspend fun Call.awaitText(): String = suspendCancellableCoroutine { continuation ->
  continuation.invokeOnCancellation { cancel() }
  enqueue(object : Callback {
    override fun onFailure(call: Call, e: IOException) {
      if (continuation.isActive) continuation.resumeWithException(e)
    }

    override fun onResponse(call: Call, response: Response) {
      response.use {
        if (!continuation.isActive) return
        try {
          val body = response.body ?: throw ScanException("Layanan analisis mengirim hasil kosong. Coba lagi.")
          val source = body.source()
          source.request(128_001)
          if (source.buffer.size > 128_000) throw ScanException("Hasil analisis tidak valid. Silakan coba lagi.")
          val text = source.readUtf8()
          if (!response.isSuccessful) {
            throw ScanException(ScanResponseParser.errorMessage(text, response.code), response.header("Retry-After")?.toIntOrNull()?.coerceIn(0, 3600) ?: 0)
          }
          continuation.resume(text)
        } catch (e: Exception) {
          if (continuation.isActive) continuation.resumeWithException(e)
        }
      }
    }
  })
}
