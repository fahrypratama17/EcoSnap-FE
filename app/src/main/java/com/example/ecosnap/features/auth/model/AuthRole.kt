package com.example.ecosnap.features.auth.model

import android.util.Patterns

enum class AuthRole {
  USER,
  PARTNER
}

data class RegistrationData(
  val role: AuthRole,
  val fullName: String,
  val email: String,
  val phone: String,
  val password: String,
  val businessName: String? = null,
  val businessCategory: String? = null
)

fun isValidEmail(value: String): Boolean {
  val email = value.trim()
  if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return false
  var bytes = 0
  var index = 0
  while (index < email.length) {
    val point = email.codePointAt(index)
    bytes += when {
      point <= 0x7f -> 1
      point <= 0x7ff -> 2
      point <= 0xffff -> 3
      else -> 4
    }
    if (bytes > 255) return false
    index += Character.charCount(point)
  }
  return true
}

private val phonePattern = Regex("^\\+?[0-9]{9,15}$")

fun isValidPhone(value: String): Boolean =
  phonePattern.matches(value.trim())

fun isValidProfileText(value: String, maxLength: Int): Boolean {
  val normalized = value.trim()
  return normalized.isNotEmpty() &&
    normalized.codePointCount(0, normalized.length) <= maxLength
}