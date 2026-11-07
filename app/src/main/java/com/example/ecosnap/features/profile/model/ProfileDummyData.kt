package com.example.ecosnap.features.profile.model

import com.composables.icons.lucide.CircleQuestionMark
import com.composables.icons.lucide.Clock3
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.Gift
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Shield
import com.composables.icons.lucide.Trophy

object ProfileDummyData {

  val userProfile = UserProfile(
    name = "Fahry",
    email = "m.fahry.pratama.putra@gmail.com"
  )

  val greenStats = GreenStats(
    ecoPoints = "1.250",
    totalDeposits = "3",
    totalBadges = "4"
  )

  val activityMenus = listOf(
    ProfileMenuItemData(
      title = "Riwayat Aktivitas",
      subtitle = "Setoran sampah dan transaksi poin",
      icon = Lucide.Clock3
    ),
    ProfileMenuItemData(
      title = "Reward Saya",
      subtitle = "Lihat voucher dan hadiah yang dimiliki",
      icon = Lucide.Gift
    ),
    ProfileMenuItemData(
      title = "Pencapaian",
      subtitle = "Lihat badge dan progress aktivitas",
      icon = Lucide.Trophy
    ),
    ProfileMenuItemData(
      title = "Laporan Aktivitas",
      subtitle = "Lihat ringkasan aktivitas dan transaksi",
      icon = Lucide.FileText
    )
  )

  val supportMenus = listOf(
    ProfileMenuItemData(
      title = "Pengaturan Akun",
      subtitle = "Notifikasi dan privasi",
      icon = Lucide.Settings
    ),
    ProfileMenuItemData(
      title = "Pusat Bantuan",
      subtitle = "Pertanyaan umum dan informasi aplikasi",
      icon = Lucide.CircleQuestionMark
    ),
    ProfileMenuItemData(
      title = "Privasi & Keamanan",
      subtitle = "Pengaturan akun dan data",
      icon = Lucide.Shield
    )
  )

  val notificationPreferences = listOf(
    NotificationPreferenceItem(
      title = "Notifikasi Transaksi",
      description = "Informasi setoran sampah dan penukaran poin"
    ),
    NotificationPreferenceItem(
      title = "Pengingat Misi Mingguan",
      description = "Pengingat tantangan dan pencapaian"
    )
  )

  val faqItems = listOf(
    FaqItem(
      question = "Bagaimana mendapatkan EcoPoints?",
      answer = "Lakukan penyetoran sampah di mitra EcoSnap, kemudian skor akan masuk setelah transaksi tervalidasi."
    ),
    FaqItem(
      question = "Mengapa poin saya belum masuk?",
      answer = "Periksa status transaksi melalui History. Transaksi yang masih menunggu atau gagal belum menambahkan poin ke saldo."
    ),
    FaqItem(
      question = "Apakah Scan AI langsung menghasilkan poin?",
      answer = "Tidak. Scan AI digunakan untuk mengenali sampah. Poin penyetoran diperoleh melalui transaksi mitra yang valid."
    )
  )
}