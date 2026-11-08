package com.example.ecosnap.features.scan.data

import com.example.ecosnap.features.scan.model.DepositTransaction

object LocalDeposits {
  private val transactions = listOf(
    DepositTransaction(
      id = "TRX-20261021-0087",
      partnerName = "Bank Sampah Tunas",
      points = 250,
      weightKg = 2.5,
      date = "21 Okt 2026",
      status = "Berhasil"
    )
  )

  // QR mitra belum dibuat, jadi isi QR sementara dianggap = ID transaksi.
  val demoQrPayload: String = transactions.first().id

  fun findById(id: String): DepositTransaction? {
    return transactions.firstOrNull { it.id == id }
  }
}
