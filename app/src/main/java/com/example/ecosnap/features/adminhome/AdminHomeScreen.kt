package com.example.ecosnap.features.adminhome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Recycle
import com.composables.icons.lucide.Scale
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.core.designsystem.theme.EcosnapTheme
import com.example.ecosnap.features.admincal.model.WasteCalculator
import com.example.ecosnap.features.adminhome.components.MitraCard

@Composable
fun AdminHomeScreen(
  weightHundredths: Int,
  onCalculator: () -> Unit,
  onGenerateQr: () -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text("Selamat datang kembali!", fontSize = 11.sp, color = EcoColor.Green900)
        Text(
          "Halo, Petugas!",
          style = MaterialTheme.typography.headlineMedium.copy(fontSize = 23.sp, lineHeight = 30.sp),
          color = EcoColor.Green900
        )
        Text(
          "Kelola penyetoran sampah dengan mudah melalui EcoSnap.",
          style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, lineHeight = 17.sp),
          color = EcoColor.Green700
        )
      }
    }
    item { PartnerPanel(weightHundredths) }
    item {
      Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Menu Utama", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        Text("2 Fitur", fontSize = 11.sp, color = EcoColor.Green700)
      }
    }
    item {
      PartnerMenuCard(
        title = "Kalkulator Sampah",
        description = "Hitung berat sampah dan estimasi poin yang diberikan",
        action = "Buka Kalkulator",
        onClick = onCalculator
      )
    }
    item {
      PartnerMenuCard(
        title = "Generate QR",
        description = "Buat QR transaksi agar pengguna bisa mengklaim poin",
        action = "Buka Generate QR",
        onClick = onGenerateQr
      )
    }
    item {
      MitraCard {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Lucide.Info, null, Modifier.size(20.dp))
          Text("Cara Kerja EcoSnap Mitra", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(14.dp))
        listOf(
          "1. Masukkan hasil penimbangan sampah.",
          "2. Periksa jumlah poin yang akan diberikan.",
          "3. Generate QR dan minta pengguna memindainya."
        ).forEach {
          Text(it, fontSize = 11.sp, lineHeight = 22.sp, color = EcoColor.Green800)
        }
      }
    }
  }
}

@Composable
private fun PartnerPanel(weightHundredths: Int) {
  Surface(color = EcoColor.Green800, shape = RoundedCornerShape(22.dp)) {
    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("PANEL MITRA", fontSize = 10.sp, color = EcoColor.White)
          Text("Siap menerima setoran?", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EcoColor.White)
          Text(
            "Timbang sampah, hitung poin, lalu buat QR untuk pengguna.",
            fontSize = 11.sp,
            lineHeight = 15.sp,
            color = EcoColor.Green100
          )
        }
        Surface(
          modifier = Modifier.padding(start = 10.dp).size(68.dp),
          color = EcoColor.Green200.copy(alpha = 0.55f),
          shape = RoundedCornerShape(16.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(Lucide.Scale, null, Modifier.size(42.dp), tint = EcoColor.White)
          }
        }
      }
      Surface(color = EcoColor.Green100, shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
          Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("Estimasi dari input saat ini", fontSize = 10.sp, color = EcoColor.Green800)
            Text("${WasteCalculator.points(weightHundredths)}", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text("Poin", fontSize = 10.sp)
          }
          Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Berat", fontSize = 10.sp, color = EcoColor.Green800)
            Text("${WasteCalculator.format(weightHundredths)} kg", fontSize = 15.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
private fun PartnerMenuCard(title: String, description: String, action: String, onClick: () -> Unit) {
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(18.dp),
    border = BorderStroke(1.dp, EcoColor.Green900),
    shadowElevation = 3.dp,
    color = EcoColor.White
  ) {
    Row(
      Modifier.fillMaxWidth().padding(13.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
      Surface(color = EcoColor.Green100, shape = RoundedCornerShape(12.dp), modifier = Modifier.size(66.dp)) {
        Box(contentAlignment = Alignment.Center) { Icon(Lucide.Recycle, null, Modifier.size(38.dp), tint = EcoColor.Green800) }
      }
      Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, lineHeight = 18.sp)
        Text(description, fontSize = 10.sp, lineHeight = 14.sp, color = EcoColor.Green800)
        Text(action, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = EcoColor.Green800)
      }
      Icon(Lucide.ChevronRight, null, Modifier.size(16.dp))
    }
  }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 850)
@Composable
private fun AdminHomePreview() {
  EcosnapTheme(darkTheme = false, dynamicColor = false) {
    AdminHomeScreen(weightHundredths = 250, onCalculator = {}, onGenerateQr = {})
  }
}
