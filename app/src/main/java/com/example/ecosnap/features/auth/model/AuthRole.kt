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

fun isValidEmail(value: String): Boolean =
  Patterns.EMAIL_ADDRESS.matcher(value.trim()).matches()

fun isValidPhone(value: String): Boolean =
  value.replace(" ", "").matches(
    Regex("^\\+?[0-9]{9,15}$}")
  )