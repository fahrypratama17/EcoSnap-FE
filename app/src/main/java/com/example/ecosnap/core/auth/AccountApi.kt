package com.example.ecosnap.core.auth

import com.example.ecosnap.core.network.BackendClient
import com.example.ecosnap.core.network.BackendException
import com.example.ecosnap.core.network.invalidResponse
import com.example.ecosnap.core.network.optionalObject
import com.example.ecosnap.core.network.requireLong
import com.example.ecosnap.core.network.requireObject
import com.example.ecosnap.core.network.requireString
import com.google.gson.JsonObject

/**
 * Account endpoints over the shared [BackendClient], which owns tokens, 401 refresh/replay and
 * sign-out. Failures surface as [BackendException]; the repository interprets status/code.
 */
class AccountApi(private val backend: BackendClient) {
    internal suspend fun me(): AccountProfile {
        val root = backend.request("GET", "api/v1/me") ?: throw invalidResponse()
        return parseProfile(root)
    }

    /** Accepts 201 (created) and 200 (identical retry); both return the stored profile. */
    internal suspend fun register(profile: RegistrationProfile): AccountProfile {
        val payload = JsonObject().apply {
            addProperty("role", if (profile.role == AccountRole.PARTNER) "mitra" else "user")
            addProperty("full_name", profile.fullName)
            addProperty("phone", profile.phone)
            if (profile.role == AccountRole.PARTNER) {
                addProperty("business_name", profile.businessName)
                addProperty("business_category", profile.businessCategory)
            }
        }
        val root = backend.request("POST", "api/v1/accounts", body = payload, expectedStatuses = setOf(200, 201))
            ?: throw invalidResponse()
        return parseProfile(root)
    }

    /**
     * Every required field is validated explicitly; there is no default/fake profile. Unknown role or
     * status values are unsafe and treated as an unusable response. A partner must carry a business
     * block; only a user may omit it (missing or null).
     */
    private fun parseProfile(root: JsonObject): AccountProfile {
        val data = root.requireObject("data")
        val role = when (data.requireString("role")) {
            "user" -> AccountRole.USER
            "mitra" -> AccountRole.PARTNER
            "admin" -> AccountRole.ADMIN
            else -> throw invalidResponse()
        }
        when (data.requireString("status")) {
            "active" -> Unit
            "suspended", "deleted" -> throw BackendException(
                403, "account_forbidden", "Akun tidak aktif. Hubungi dukungan EcoSnap.",
            )
            else -> throw invalidResponse()
        }
        val business = data.optionalObject("business")?.let {
            BusinessProfile(
                name = it.requireString("name"),
                category = it.requireString("category"),
                status = it.requireString("status"),
            )
        }
        if ((role == AccountRole.PARTNER) != (business != null)) throw invalidResponse()
        // Admin accounts have no wallet; every other role must carry a valid one.
        val balance = if (role == AccountRole.ADMIN) {
            data.optionalObject("wallet")?.requireLong("balance") ?: 0L
        } else {
            data.requireObject("wallet").requireLong("balance")
        }
        if (balance < 0) throw invalidResponse()
        return AccountProfile(
            uid = data.requireString("uid"),
            role = role,
            fullName = data.requireString("full_name"),
            displayName = data.requireString("display_name"),
            email = data.requireString("email", allowEmpty = true),
            phone = data.requireString("phone", allowEmpty = role == AccountRole.ADMIN),
            walletBalance = balance,
            business = business,
        )
    }
}
