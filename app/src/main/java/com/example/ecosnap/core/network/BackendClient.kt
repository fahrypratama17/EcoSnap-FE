package com.example.ecosnap.core.network

import com.example.ecosnap.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.IOException
import java.io.InterruptedIOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import okhttp3.CacheControl
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

/**
 * Failure of a backend call. [status] is the HTTP status, or null when no usable HTTP answer exists.
 * [code] is the server's error code, or a client code: `network`, `timeout`, `unauthenticated`,
 * `session_changed`, `invalid_response`. [message] is always user-safe Indonesian text.
 */
class BackendException(
    val status: Int?,
    val code: String,
    message: String,
    val requestId: String? = null,
) : Exception(message)

/**
 * Authenticated JSON transport for `api/v1/...`. Every call captures the current Firebase UID, fetches
 * the SDK token (never cached here), and sends it only in the Authorization header. One 401 forces a
 * token refresh and replays the call once; nothing else is retried. A terminal 401 or 403
 * `account_forbidden` signs the captured user out. A result that arrives after the Firebase account
 * changed is discarded, so one account's data can never reach another account's screens.
 */
class BackendClient(client: OkHttpClient, baseUrl: String, private val firebaseAuth: FirebaseAuth) {
    private val http: OkHttpClient = client.newBuilder()
        .cache(null)
        .followRedirects(false)
        .followSslRedirects(false)
        .retryOnConnectionFailure(false)
        .build()
    private val base: HttpUrl = baseUrl.toHttpUrl()

    /**
     * Returns the full JSON envelope, or null only for 204. [path] is relative (`api/v1/...`) and must
     * already contain validated IDs; its segments are encoded, never interpreted.
     */
    suspend fun request(
        method: String,
        path: String,
        query: Map<String, String> = emptyMap(),
        body: JsonObject? = null,
        expectedStatuses: Set<Int> = setOf(200),
    ): JsonObject? {
        val url = buildUrl(path, query)
        val payload = buildBody(method, body)
        val user = firebaseAuth.currentUser ?: throw unauthenticated()
        val uid = user.uid

        var raw = exchange(user, uid, method, url, payload, forceRefresh = false)
        if (raw.status == 401) {
            ensureCurrent(uid)
            raw = exchange(user, uid, method, url, payload, forceRefresh = true)
        }
        ensureCurrent(uid)

        if (raw.status in expectedStatuses) {
            if (raw.status == 204) return null
            val text = raw.body?.takeIf { it.isNotBlank() } ?: throw invalidResponse(raw.requestId)
            return try {
                JsonParser.parseString(text).asJsonObject
            } catch (_: RuntimeException) {
                throw invalidResponse(raw.requestId)
            }
        }
        throw failure(raw, uid)
    }

    private suspend fun exchange(
        user: FirebaseUser,
        uid: String,
        method: String,
        url: HttpUrl,
        payload: RequestBody?,
        forceRefresh: Boolean,
    ): Raw {
        val token = token(user, uid, forceRefresh)
        val request = Request.Builder()
            .url(url)
            .method(method, payload)
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/json")
            .cacheControl(CacheControl.Builder().noStore().build())
            .build()
        return try {
            execute(request)
        } catch (e: InterruptedIOException) {
            throw BackendException(null, "timeout", NETWORK_MESSAGE)
        } catch (e: IOException) {
            throw BackendException(null, "network", NETWORK_MESSAGE)
        }
    }

    private suspend fun token(user: FirebaseUser, uid: String, forceRefresh: Boolean): String = try {
        user.getIdToken(forceRefresh).await().token?.takeIf { it.isNotEmpty() }
            ?: throw BackendException(null, "network", NETWORK_MESSAGE)
    } catch (e: CancellationException) {
        throw e
    } catch (e: BackendException) {
        throw e
    } catch (e: FirebaseAuthInvalidUserException) {
        // Disabled/deleted in Firebase or credentials revoked: the session cannot recover.
        signOutIfCurrent(uid)
        throw unauthenticated()
    } catch (e: Exception) {
        throw BackendException(null, "network", NETWORK_MESSAGE)
    }

    /** The body is read inside the callback so the call stays cancellable and the socket always closes. */
    private suspend fun execute(request: Request): Raw = suspendCancellableCoroutine { cont ->
        val call = http.newCall(request)
        cont.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                cont.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                val raw = try {
                    response.use { read(it) }
                } catch (e: IOException) {
                    cont.resumeWithException(e)
                    return
                } catch (e: RuntimeException) {
                    cont.resumeWithException(IOException("Unreadable response"))
                    return
                }
                cont.resume(raw)
            }
        })
    }

    private fun read(response: Response): Raw {
        val requestId = response.header("X-Request-Id")?.safeId()
        val body = response.body
        if (response.code == 204 || response.code == 205) return Raw(response.code, null, false, requestId)
        if (body.contentLength() > MAX_BODY_BYTES) return Raw(response.code, null, true, requestId)
        val source = body.source()
        source.request(MAX_BODY_BYTES + 1)
        if (source.buffer.size > MAX_BODY_BYTES) return Raw(response.code, null, true, requestId)
        return Raw(response.code, source.buffer.readUtf8(), false, requestId)
    }

    private class Raw(val status: Int, val body: String?, val tooLarge: Boolean, val requestId: String?)

    private fun failure(raw: Raw, uid: String): BackendException {
        val root = if (raw.tooLarge) null else runCatching { JsonParser.parseString(raw.body).asJsonObject }.getOrNull()
        val error = root?.get("error") as? JsonObject
        val code = error?.stringOrNull("code")?.takeIf { it.isNotEmpty() && it.length <= 64 }
            ?: if (raw.status in 200..299) "unexpected_status" else "http_${raw.status}"
        val requestId = (error?.stringOrNull("request_id") ?: root?.stringOrNull("request_id"))?.safeId() ?: raw.requestId
        if (raw.status == 401 || (raw.status == 403 && code == "account_forbidden")) signOutIfCurrent(uid)
        val message = when (raw.status) {
            400, 422 -> error?.stringOrNull("message")?.trim()?.takeIf { it.isNotEmpty() && it.codePointCount(0, it.length) <= 200 }
                ?: "Permintaan ditolak server. Periksa kembali data Anda."
            401 -> "Sesi tidak valid. Silakan masuk kembali."
            403 -> "Akses ditolak."
            404 -> "Data tidak ditemukan."
            409 -> "Terjadi konflik data. Muat ulang lalu coba lagi."
            410 -> "Data sudah kedaluwarsa. Muat ulang lalu coba lagi."
            429 -> "Terlalu banyak permintaan. Coba lagi beberapa saat lagi."
            in 500..599 -> "Server EcoSnap sedang bermasalah. Coba lagi nanti."
            else -> "Permintaan tidak dapat diproses."
        }
        return BackendException(raw.status, code, message, requestId)
    }

    private fun ensureCurrent(uid: String) {
        if (firebaseAuth.currentUser?.uid != uid) {
            throw BackendException(null, "session_changed", "Sesi berubah. Silakan coba lagi.")
        }
    }

    private fun signOutIfCurrent(uid: String) {
        if (firebaseAuth.currentUser?.uid == uid) firebaseAuth.signOut()
    }

    private fun unauthenticated() =
        BackendException(401, "unauthenticated", "Sesi tidak valid. Silakan masuk kembali.")

    private fun buildUrl(path: String, query: Map<String, String>): HttpUrl {
        val segments = path.trimStart('/').split('/')
        require(segments.none { it.isEmpty() || it == "." || it == ".." }) { "Invalid API path" }
        val builder = base.newBuilder()
        segments.forEach { builder.addPathSegment(it) }
        query.forEach { (name, value) -> builder.addQueryParameter(name, value) }
        return builder.build()
    }

    private fun buildBody(method: String, body: JsonObject?): RequestBody? {
        require(method in METHODS) { "Unsupported method" }
        return when (method) {
            "GET", "DELETE" -> {
                require(body == null) { "$method must not have a body" }
                null
            }
            else -> (body?.toString() ?: "").toRequestBody(JSON)
        }
    }

    private fun JsonObject.stringOrNull(name: String): String? =
        (get(name) as? com.google.gson.JsonPrimitive)?.takeIf { it.isString }?.asString

    private fun String.safeId(): String? = takeIf { it.length <= 128 && it.all { c -> c.isLetterOrDigit() || c == '-' || c == '_' || c == '.' } }

    companion object {
        private val JSON = "application/json; charset=utf-8".toMediaType()
        private val METHODS = setOf("GET", "POST", "PUT", "PATCH", "DELETE")
        private const val MAX_BODY_BYTES = 1L * 1024 * 1024
        private const val NETWORK_MESSAGE = "Tidak dapat terhubung. Periksa koneksi internet Anda."

        val default: BackendClient by lazy {
            BackendClient(
                OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .callTimeout(30, TimeUnit.SECONDS)
                    .build(),
                BuildConfig.API_BASE_URL,
                FirebaseAuth.getInstance(),
            )
        }
    }
}
