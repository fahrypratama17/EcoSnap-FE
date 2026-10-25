package com.example.ecosnap.features.onboarding.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.ScanLine

data class OnboardingPage(
  val title: String,
  val description: String,
  val icon: ImageVector
)

val onboardingPages = listOf(
  OnboardingPage(
    title = "Kenali Sampahmu",
    description = "Foto sampah dan temukan cara Foto sampahmu, kenali jenisnya dengan AI, dan temukan cara menanganinya.",
    icon = Lucide.ScanLine
  ),
  OnboardingPage(
    title = "Cari Mitra",
    description = "Temukan tempat penyetoran terdekat yang menerima jenis sampahmu.",
    icon = Lucide.MapPin
  ),
  OnboardingPage(
    title = "Dapatkan Poin",
    description = "Setorkan sampah, scan QR dari mitra, lalu tukarkan poin dengan reward.",
    icon = Lucide.ScanLine
  )
)