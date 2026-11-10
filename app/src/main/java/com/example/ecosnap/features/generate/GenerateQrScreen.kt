package com.example.ecosnap.features.generate

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Clock3
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.QrCode
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.core.designsystem.theme.EcosnapTheme
import com.example.ecosnap.features.admincal.model.WasteCalculator
import com.example.ecosnap.features.adminhome.components.MitraCard
import com.example.ecosnap.features.adminhome.components.MitraPageTitle
import com.example.ecosnap.features.generate.data.DepositQrEncoder
import com.example.ecosnap.features.generate.model.DepositQr
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.floor

@Composable
fun GenerateQrScreen(
  transaction: DepositQr?,
  remainingSeconds: Int,
  onRegenerate: () -> Unit,
  onFinished: () -> Unit,
  onCalculator: () -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(20.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp)
  ) {
    item { MitraPageTitle("Generate QR", "Tunjukkan QR ini kepada pengguna") }
    if (transaction == null) {
      item {
        MitraCard {
          Column(
            Modifier.fillMaxWidth().padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Icon(Lucide.QrCode, null, Modifier.size(64.dp), tint = EcoColor.Green700)
            Text("Belum ada QR transaksi", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(
              "Masukkan berat sampah dan periksa estimasi poin terlebih dahulu.",
              fontSize = 12.sp, lineHeight = 19.sp, textAlign = TextAlign.Center, color = EcoColor.Green800
            )
            EcoButton("Buka Kalkulator", onCalculator, containerColor = EcoColor.Green700)
          }
        }
      }
    } else {
      item { DepositSummary(transaction) }
      item {
        MitraCard(contentPadding = PaddingValues(16.dp)) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Text("Scan untuk Klaim Poin", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text(
              "Minta pengguna memindai QR berikut\ndengan aplikasi EcoSnap.",
              fontSize = 11.sp, lineHeight = 16.sp, textAlign = TextAlign.Center, color = EcoColor.Green800
            )
            Spacer(Modifier.height(2.dp))
            if (remainingSeconds > 0) {
              DepositQrCode(transaction.payload())
            } else {
              Surface(
                color = EcoColor.Green100,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().aspectRatio(1f)
              ) {
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center
                ) {
                  Icon(Lucide.Clock3, null, Modifier.size(52.dp), tint = EcoColor.Green700)
                  Spacer(Modifier.height(14.dp))
                  Text("QR sudah kedaluwarsa", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                  Text("Buat ulang QR untuk melanjutkan.", Modifier.padding(top = 8.dp), fontSize = 11.sp)
                }
              }
            }
            Text(transaction.id, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            Surface(color = EcoColor.Green700, shape = RoundedCornerShape(50)) {
              Row(
                Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(Lucide.Clock3, null, Modifier.size(15.dp), tint = EcoColor.White)
                val time = "${(remainingSeconds / 60).toString().padStart(2, '0')}:${(remainingSeconds % 60).toString().padStart(2, '0')}"
                Text(
                  if (remainingSeconds > 0) "Berlaku selama $time" else "Masa berlaku habis",
                  fontSize = 11.sp, color = EcoColor.White
                )
              }
            }
          }
        }
      }
      item {
        Surface(color = EcoColor.Green100, shape = RoundedCornerShape(22.dp)) {
          Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Icon(Lucide.Info, null, Modifier.size(34.dp), tint = EcoColor.Green800)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(
                if (remainingSeconds > 0) "QR demo siap dipindai" else "Transaksi belum dikonfirmasi",
                fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold
              )
              Text(
                "Klaim dan penambahan poin menunggu integrasi backend. Menutup QR tidak menambahkan poin.",
                fontSize = 10.sp, lineHeight = 16.sp, color = EcoColor.Green800
              )
            }
          }
        }
      }
      item {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
          OutlinedButton(
            onClick = onRegenerate,
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, EcoColor.Green900),
            contentPadding = PaddingValues(horizontal = 8.dp)
          ) {
            Text("Buat Ulang QR", fontSize = 12.sp, color = EcoColor.Green900)
          }
          EcoButton("Selesai", onFinished, Modifier.weight(1f), containerColor = EcoColor.Green700)
        }
      }
    }
  }
}

@Composable
private fun DepositSummary(transaction: DepositQr) {
  MitraCard {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
      Text("Total Berat Sampah", fontSize = 17.sp)
      Text("${WasteCalculator.format(transaction.weightHundredths)} kg", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EcoColor.Green800)
      HorizontalDivider(Modifier.padding(vertical = 5.dp), color = EcoColor.Green900)
      Text("Poin yang akan diterima", fontSize = 17.sp)
      Text("+${transaction.points} Poin", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EcoColor.Green800)
    }
  }
}

@Composable
private fun DepositQrCode(payload: String) {
  val result by produceState<Result<BitMatrix>?>(initialValue = null, key1 = payload) {
    value = withContext(Dispatchers.Default) { runCatching { DepositQrEncoder.encode(payload) } }
  }
  Surface(
    shape = RoundedCornerShape(14.dp),
    border = BorderStroke(2.dp, EcoColor.Green800),
    color = EcoColor.White,
    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
  ) {
    Box(contentAlignment = Alignment.Center) {
      val matrix = result?.getOrNull()
      if (matrix != null) {
        val qrColor = EcoColor.Green800
        Canvas(Modifier.fillMaxSize().semantics { contentDescription = "QR transaksi demo EcoSnap" }) {
          // Whole pixels and four quiet modules keep the displayed QR readable at any density.
          val module = floor(size.minDimension / matrix.width)
          val startX = (size.width - matrix.width * module) / 2f
          val startY = (size.height - matrix.height * module) / 2f
          for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
              if (matrix[x, y]) drawRect(qrColor, Offset(startX + x * module, startY + y * module), Size(module, module))
            }
          }
        }
      } else if (result == null) {
        CircularProgressIndicator(color = EcoColor.Green700)
      } else {
        Text("QR gagal dibuat.\nKetuk Buat Ulang QR untuk mencoba lagi.", textAlign = TextAlign.Center, fontSize = 12.sp)
      }
    }
  }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 1000)
@Composable
private fun GenerateQrPreview() {
  EcosnapTheme(darkTheme = false, dynamicColor = false) {
    GenerateQrScreen(DepositQr("TRX-201124123123-0087", 250, 300_000L), 299, {}, {}, {})
  }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
private fun ExpiredQrPreview() {
  EcosnapTheme(darkTheme = false, dynamicColor = false) {
    GenerateQrScreen(DepositQr("TRX-201124123123-0087", 250, 300_000L), 0, {}, {}, {})
  }
}
