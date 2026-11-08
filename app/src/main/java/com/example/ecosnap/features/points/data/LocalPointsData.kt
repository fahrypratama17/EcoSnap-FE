package com.example.ecosnap.features.points.data

import com.example.ecosnap.features.points.model.VoucherCategorySection
import com.example.ecosnap.features.points.model.VoucherDetailUiModel
import com.example.ecosnap.features.points.model.VoucherItem
import java.text.NumberFormat
import java.util.Locale

object LocalPointsData {
  private val indonesianLocale: Locale = Locale.forLanguageTag("id-ID")

  const val CURRENT_USER_POINTS: Int = 1250

  val voucherSections: List<VoucherCategorySection> = listOf(
    VoucherCategorySection(
      id = "popular",
      title = "Voucher Populer",
      vouchers = listOf(
        VoucherItem(
          id = "popular_dana_500",
          title = "Saldo Dana",
          subtitle = "Rp25.000,00",
          pointsCost = 500
        ),
        VoucherItem(
          id = "popular_dana_3000",
          title = "Saldo Dana",
          subtitle = "Rp25.000,00",
          pointsCost = 3000
        )
      )
    ),
    VoucherCategorySection(
      id = "ewallet",
      title = "E - Wallet",
      vouchers = listOf(
        VoucherItem(
          id = "ewallet_dana_500",
          title = "Saldo Dana",
          subtitle = "Rp25.000,00",
          pointsCost = 500
        ),
        VoucherItem(
          id = "ewallet_dana_3000",
          title = "Saldo Dana",
          subtitle = "Rp25.000,00",
          pointsCost = 3000
        )
      )
    ),
    VoucherCategorySection(
      id = "food_drink",
      title = "Voucher Makanan & Minuman",
      vouchers = listOf(
        VoucherItem(
          id = "food_dana_500",
          title = "Saldo Dana",
          subtitle = "Rp25.000,00",
          pointsCost = 500
        ),
        VoucherItem(
          id = "food_dana_3000",
          title = "Saldo Dana",
          subtitle = "Rp25.000,00",
          pointsCost = 3000
        )
      )
    )
  )

  val defaultVoucherDetail = VoucherDetailUiModel(
    id = "voucher_minuman",
    title = "Voucher Minuman",
    subtitle = "Diskon Rp10.000",
    pointsCost = 500,
    termsParagraphs = listOf(
      "Potongan pembelian minuman sesuai ketentuan voucher.",
      "Contoh katalog ini belum terhubung ke penyedia voucher atau sistem penukaran sungguhan."
    )
  )

  fun findDetailById(id: String): VoucherDetailUiModel {
    return defaultVoucherDetail.copy(id = id.ifBlank { defaultVoucherDetail.id })
  }

  fun formatPoints(points: Int): String {
    return NumberFormat.getIntegerInstance(indonesianLocale).format(points)
  }
}
