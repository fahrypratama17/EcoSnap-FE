package com.example.ecosnap.features.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
private fun DepositStep(
  number: Int,
  title: String,
  description: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.Top
  ) {
    Box(
      modifier = Modifier
        .size(24.dp)
        .background(
          EcoColor.Green100,
          CircleShape
        ),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = number.toString(),
        style = MaterialTheme.typography.labelSmall,
        color = EcoColor.Green800
      )
    }

    Spacer(Modifier.width(10.dp))

    Column{
      Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = EcoColor.Green900
      )

      Spacer(Modifier.height(3.dp))

      Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        color = EcoColor.Green700
      )
    }
  }
}

@Composable
fun PartnerDepositsStepsCard(
  modifier: Modifier = Modifier
) {
  EcoCard(
    modifier = modifier.fillMaxWidth()
  ) {
    Text(
      text = "Cara Menyetorkan",
      style = MaterialTheme.typography.titleMedium,
      color = EcoColor.Green900
    )

    Spacer(Modifier.height(16.dp))

    DepositStep(
      number = 1,
      title = "Pilah sampah",
      description = "Pisahkan sampah berdasarkan jenis material."
    )

    Spacer(Modifier.height(14.dp))

    DepositStep(
      number = 2,
      title = "Datang ke mitra",
      description = "Bawa material ke lokasi mitra pada jam layanan"
    )

    Spacer(Modifier.height(14.dp))

    DepositStep(
      number = 3,
      title = "Timbang sampah",
      description = "Mitra memeriksa dan memasukkan berat hasil penimbangan"
    )

    Spacer(Modifier.height(14.dp))

    DepositStep(
      number = 4,
      title = "Scan QR transaksi",
      description = "Pindai QR dari mitra menggunakan EcoSnap untuk validasi transaksi."
    )
  }
}