package com.example.ecosnap.features.profile.model

data class UserProfile(
  val name: String,
  val email: String,
  /** Optional extra line, such as the business of a mitra. */
  val detail: String? = null
)
