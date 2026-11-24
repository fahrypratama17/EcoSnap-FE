package com.example.ecosnap.core.auth

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.CacheControl
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody

internal enum class ApiErrorKind {
    /** 401: the token was refused. The repository force-refreshes once before treating it as terminal. */
    Unauthorized,

    /** 403 account_forbidden or a suspended/deleted profile. Terminal for this session. */
    Forbidden,

    /** 404 account_not_found on GET /me: Firebase user exists but has no backend profile yet. */
    NotFound,

    /** 409 account_conflict: a different profile already exists; GET /me is authoritative. */
    Conflict,

    /** 400: definite rejection of the submitted data; [AccountApiException.message] is safe to show. */
    Rejected,

    /** Network, timeout, 5xx, redirect or malformed response. Outcome of the call is unknown. */
    Unavailable,
}

internal class AccountApiException(val kind: ApiErrorKind, message: String? = null) : Exception(message)

/**
 * Minimal client for the EcoSnap account endpoints. Callers supply a fresh Firebase ID token for every
 * call; it is only ever placed in the Authorization header. Nothing is logged or cached.
 */
class AccountApi(private val client: OkHttpClient, baseUrl: String) {
    private val meUrl: HttpUrl = baseUrl.toHttpUrl().newBuilder().addPathSegments("api/v1/me").build()
    private val accountsUrl: HttpUrl = baseUrl.toHttpUrl().newBuilder().addPathSegments("api/v1/accounts").build()

    internal suspend fun me(token: String): AccountProfile {
        val request = baseRequest(meUrl, token).get().build()
        return parseProfile(send(request, setOf(200)))
    }

    /** Accepts 201 (created) and 200 (identical retry); both return the stored profile. */
    internal suspend fun register(token: String, profile: RegistrationProfile): AccountProfile {
        val payload = JsonObject().apply {
            addProperty("role", if (profile.role == AccountRole.PARTNER) "mitra" else "user")
            addProperty("full_name", profile.fullName)
            addProperty("phone", profile.phone)
            if (profile.role == AccountRole.PARTNER) {
                addProperty("business_name", profile.businessName)
                addProperty("business_category", profile.businessCategory)
            }
        }
        val request = baseRequest(accountsUrl, token)
            .post(payload.toString().toRequestBody(JSON))
            .build()
        return parseProfile(send(request, setOf(200, 201)))
    }

    private fun baseRequest(url: HttpUrl, token: String): Request.Builder = Request.Builder()
        .url(url)
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .cacheControl(CacheControl.Builder().noStore().build())

    private suspend fun send(request: Request, okStatuses: Set<Int>): String {
        val raw = try {
            execute(request)
        } catch (_: IOException) {
            throw AccountApiException(ApiErrorKind.Unavailable)
        }
        if (raw.status in okStatuses) return raw.body ?: throw AccountApiException(ApiErrorKind.Unavailable)
        throw errorFor(raw)
    }

    /**
     * Reading the body happens inside the callback, before the continuation resumes, so the call stays
     * cancellable (and the socket closes) for the whole exchange. The response is always closed.
     */
    private suspend fun execute(request: Request): Raw = suspendCancellableCoroutine { cont ->
        val call = client.newCall(request)
        cont.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                cont.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                val raw = try {
                    response.use { Raw(it.code, readBounded(it.body)) }
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

    private class Raw(val status: Int, val body: String?)

    private fun readBounded(body: ResponseBody): String? {
        val source = body.source()
        source.request(MAX_BODY_BYTES + 1)
        if (source.buffer.size > MAX_BODY_BYTES) return null
        return source.buffer.readUtf8()
    }

    private fun errorFor(raw: Raw): AccountApiException {
        val error = runCatching {
            JsonParser.parseString(raw.body).asJsonObject.getAsJsonObject("error")
        }.getOrNull()
        val code = error?.string("code")
        val kind = when (raw.status) {
            400 -> return AccountApiException(ApiErrorKind.Rejected, safeMessage(error?.string("message")))
            401 -> ApiErrorKind.Unauthorized
            403 -> if (code == "account_forbidden") ApiErrorKind.Forbidden else ApiErrorKind.Unavailable
            404 -> if (code == "account_not_found") ApiErrorKind.NotFound else ApiErrorKind.Unavailable
            409 -> if (code == "account_conflict") ApiErrorKind.Conflict else ApiErrorKind.Unavailable
            else -> ApiErrorKind.Unavailable
        }
        return AccountApiException(kind)
    }

    private fun safeMessage(message: String?): String {
        val text = message?.trim().orEmpty()
        return if (text.isNotEmpty() && text.codePointCount(0, text.length) <= 200) {
            text
        } else {
            "Data pendaftaran ditolak server. Periksa kembali isian Anda."
        }
    }

    /**
     * Every required field is validated explicitly; there is no default/fake profile. Unknown role or
     * status values are unsafe and treated as an unusable response. A partner must carry a business
     * block; only a user may omit it (missing or null).
     */
    private fun parseProfile(body: String): AccountProfile {
        try {
            val root = JsonParser.parseString(body).asJsonObject
            val data = root.get("data") as? JsonObject ?: malformed()
            val role = when (data.requireString("role")) {
                "user" -> AccountRole.USER
                "mitra" -> AccountRole.PARTNER
                else -> malformed()
            }
            when (data.requireString("status")) {
                "active" -> Unit
                "suspended", "deleted" -> throw AccountApiException(ApiErrorKind.Forbidden)
                else -> malformed()
            }
            val businessElement: JsonElement? = data.get("business")
            val business = when {
                businessElement == null || businessElement.isJsonNull -> null
                businessElement is JsonObject -> BusinessProfile(
                    name = businessElement.requireString("name"),
                    category = businessElement.requireString("category"),
                    status = businessElement.requireString("status"),
                )
                else -> malformed()
            }
            if ((role == AccountRole.PARTNER) != (business != null)) malformed()
            val wallet = data.get("wallet") as? JsonObject ?: malformed()
            val balance = (wallet.get("balance") as? JsonPrimitive)?.takeIf { it.isNumber }
                ?.let { it.asBigDecimal.longValueExact() } ?: malformed()
            if (balance < 0) malformed()
            return AccountProfile(
                uid = data.requireString("uid"),
                role = role,
                fullName = data.requireString("full_name"),
                displayName = data.requireString("display_name"),
                email = data.requireString("email", allowEmpty = true),
                phone = data.requireString("phone"),
                walletBalance = balance,
                business = business,
            )
        } catch (e: AccountApiException) {
            throw e
        } catch (e: Exception) {
            // JsonParseException, IllegalStateException, ArithmeticException, NumberFormatException.
            throw AccountApiException(ApiErrorKind.Unavailable)
        }
    }

    private fun malformed(): Nothing = throw AccountApiException(ApiErrorKind.Unavailable)

    private fun JsonObject.string(name: String): String? =
        (get(name) as? JsonPrimitive)?.takeIf { it.isString }?.asString

    private fun JsonObject.requireString(name: String, allowEmpty: Boolean = false): String {
        val value = string(name) ?: malformed()
        if (!allowEmpty && value.isEmpty()) malformed()
        return value
    }

    private companion object {
        val JSON = "application/json; charset=utf-8".toMediaType()
        const val MAX_BODY_BYTES = 64L * 1024
    }
}
