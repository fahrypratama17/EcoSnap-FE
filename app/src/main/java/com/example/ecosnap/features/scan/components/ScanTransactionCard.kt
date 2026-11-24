package com.example.ecosnap.features.scan.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.scan.model.DepositTransaction

@Composable
fun ScanTransactionCard(
  transaction: DepositTransaction,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp),
    color = Color.White,
    border = BorderStroke(1.dp, EcoColor.Green900),
    shadowElevation = 4.dp
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      TransactionRow("Mitra", transaction.partnerName)
      TransactionRow("ID Transaksi", transaction.id.replace("-", " - "))
      TransactionRow("Tanggal", transaction.date)
      TransactionRow("Status", transaction.status)
    }
  }
}

@Composable
private fun TransactionRow(
  label: String,
  value: String
) {
  val style = MaterialTheme.typography.bodyMedium.copy(
    fontSize = 12.sp,
    lineHeight = 15.sp,
    letterSpacing = 0.sp
  )

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Text(
      text = label,
      style = style.copy(fontWeight = FontWeight.Normal),
      color = EcoColor.Green900
    )

    Text(
      text = value,
      style = style.copy(fontWeight = FontWeight.SemiBold),
      color = EcoColor.Green900,
      textAlign = TextAlign.End,
      modifier = Modifier.weight(1f)
    )
  }
}
