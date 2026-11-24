package com.example.ecosnap.core.auth

enum class AccountRole { USER, PARTNER, ADMIN }

/**
 * Non-secret registration payload. It is persisted per Firebase UID before the backend POST so an
 * ambiguous failure cannot lose retry intent. It never contains a password, email or token.
 */
data class RegistrationProfile(
    val role: AccountRole,
    val fullName: String,
    val phone: String,
    val businessName: String? = null,
    val businessCategory: String? = null,
)

data class BusinessProfile(val name: String, val category: String, val status: String)

data class AccountProfile(
    val uid: String,
    val role: AccountRole,
    val fullName: String,
    val displayName: String,
    val email: String,
    val phone: String,
    val walletBalance: Long,
    val business: BusinessProfile?,
)

sealed interface SessionOutcome {
    data object SignedOut : SessionOutcome
    data class NeedsProfile(val email: String, val draft: RegistrationProfile? = null) : SessionOutcome
    data class Authenticated(val account: AccountProfile) : SessionOutcome
    data class Unavailable(val email: String) : SessionOutcome
}

/** [message] is always user-safe Indonesian text; [recovery] tells the UI which state to show next. */
class AuthFailure(message: String, val recovery: SessionOutcome? = null) : Exception(message)

private const val MAX_FULL_NAME = 150
private const val MAX_BUSINESS_NAME = 150
private const val MAX_BUSINESS_CATEGORY = 80

/**
 * Mirrors the backend validation (EcoSnap-BE service/account_registration.go) so obviously invalid
 * input is rejected locally. Lengths are counted in Unicode code points, like Go's rune count.
 * The result is the immutable normalized payload that is stored and sent.
 */
internal fun RegistrationProfile.normalized(): RegistrationProfile {
    val name = fullName.trim()
    val number = phone.trim().filterNot { it == ' ' || it == '-' }
    if (!name.validText(MAX_FULL_NAME)) throw AuthFailure("Nama lengkap wajib diisi, maksimal 150 karakter.")
    val digits = number.removePrefix("+")
    if (digits.length !in 9..15 || digits.any { it !in '0'..'9' }) {
        throw AuthFailure("Nomor telepon harus 9-15 digit, boleh diawali tanda +.")
    }
    return when (role) {
        AccountRole.ADMIN -> throw AuthFailure("Akun admin tidak dapat didaftarkan dari aplikasi.")
        AccountRole.USER -> RegistrationProfile(role, name, number)
        AccountRole.PARTNER -> {
            val business = businessName.orEmpty().trim()
            val category = businessCategory.orEmpty().trim()
            if (!business.validText(MAX_BUSINESS_NAME)) {
                throw AuthFailure("Nama usaha wajib diisi, maksimal 150 karakter.")
            }
            if (!category.validText(MAX_BUSINESS_CATEGORY)) {
                throw AuthFailure("Kategori usaha wajib diisi, maksimal 80 karakter.")
            }
            RegistrationProfile(role, name, number, business, category)
        }
    }
}

private fun String.validText(maxCodePoints: Int): Boolean {
    if (isEmpty() || codePointCount(0, length) > maxCodePoints) return false
    var i = 0
    while (i < length) {
        val c = this[i]
        when {
            Character.isHighSurrogate(c) -> {
                if (i + 1 >= length || !Character.isLowSurrogate(this[i + 1])) return false
                i += 2
            }
            Character.isLowSurrogate(c) -> return false
            else -> i++
        }
    }
    return true
}
