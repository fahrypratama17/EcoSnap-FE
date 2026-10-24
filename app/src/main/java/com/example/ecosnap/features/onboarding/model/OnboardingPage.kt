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

val onboardingPage = listOf(
  OnboardingPage(
    title = "Kenali Sampahmu",
    description = "Foto sampahmu, kenali jenisnya dengan AI, dan temukan cara menanganinya.",
    icon = Lucide.ScanLine
  ),
  OnboardingPage(
    title = "Temukan Mitra Terdekat",
    description = "Temukan lokasi pengumpulan sampah di sekitarmu dan setor sesuai jenis material.",
    icon = Lucide.MapPin
  ),
  OnboardingPage(
    title = "Kumpulkan Poin",
    description = "Setor sampah, kumpulkan poin, dan tukarkan dengan berbagai hadiah menarik.",
    icon = Lucide.ScanLine
  )
)