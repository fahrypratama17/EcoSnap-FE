package com.example.ecosnap.features.profile.model

import com.composables.icons.lucide.CircleQuestionMark
import com.composables.icons.lucide.Clock3
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.Gift
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Shield
import com.composables.icons.lucide.Trophy
import com.example.ecosnap.core.auth.AccountRole
import com.example.ecosnap.navigation.Routes

/** Mitra never receives point, voucher or badge destinations; those exist only for users. */
object ProfileMenus {

  fun activity(role: AccountRole): List<ProfileMenuItemData> = when (role) {
    AccountRole.USER -> listOf(
      ProfileMenuItemData(
        title = "Riwayat Poin",
        subtitle = "Setoran yang diklaim dan penukaran poin",
        icon = Lucide.Clock3,
        route = Routes.PROFILE_ACTIVITY_HISTORY
      ),
      ProfileMenuItemData(
        title = "Voucher Saya",
        subtitle = "Lihat voucher hasil penukaran poin",
        icon = Lucide.Gift,
        route = Routes.VOUCHERS
      ),
      ProfileMenuItemData(
        title = "Pencapaian",
        subtitle = "Lihat badge dan progres aktivitas",
        icon = Lucide.Trophy,
        route = Routes.PROFILE_ACHIEVEMENTS
      ),
      ProfileMenuItemData(
        title = "Laporan Aktivitas",
        subtitle = "Ringkasan aktivitas dan unduhan PDF",
        icon = Lucide.FileText,
        route = Routes.PROFILE_ACTIVITY_REPORT
      )
    )
    AccountRole.ADMIN -> emptyList()
    AccountRole.PARTNER -> listOf(
      ProfileMenuItemData(
        title = "Laporan Aktivitas",
        subtitle = "Ringkasan setoran mitra dan unduhan PDF",
        icon = Lucide.FileText,
        route = Routes.PROFILE_ACTIVITY_REPORT
      )
    )
  }

  fun support(): List<ProfileMenuItemData> = listOf(
    ProfileMenuItemData(
      title = "Pengaturan Akun",
      subtitle = "Notifikasi dan privasi",
      icon = Lucide.Settings,
      route = Routes.PROFILE_ACCOUNT_SETTINGS
    ),
    ProfileMenuItemData(
      title = "Pusat Bantuan",
      subtitle = "Pertanyaan umum dan tiket bantuan",
      icon = Lucide.CircleQuestionMark,
      route = Routes.PROFILE_HELP_CENTER
    ),
    ProfileMenuItemData(
      title = "Akun & Keamanan",
      subtitle = "Ubah nama, telepon, dan reset kata sandi",
      icon = Lucide.Shield,
      route = Routes.PROFILE_PRIVACY_SECURITY
    )
  )
}
