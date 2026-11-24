package com.example.ecosnap.features.profile.model

import androidx.compose.ui.graphics.vector.ImageVector

/** [value] is already formatted; "—" means the server value is not available. */
data class ProfileStatItem(
  val value: String,
  val label: String,
  val icon: ImageVector
)

data class GreenStats(
  val title: String,
  val items: List<ProfileStatItem>,
  val footer: String? = null
)
