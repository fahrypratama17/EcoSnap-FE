package com.example.ecosnap.features.admincal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Delete
import com.composables.icons.lucide.Gamepad2
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.core.designsystem.theme.EcosnapTheme
import com.example.ecosnap.features.admincal.model.WasteCalculator
import com.example.ecosnap.features.adminhome.components.MitraCard
import com.example.ecosnap.features.adminhome.components.MitraPageTitle

@Composable
fun AdminCalculatorScreen(
  weightInput: String,
  onInputChange: (String) -> Unit,
  onGenerateQr: () -> Unit,
  modifier: Modifier = Modifier
) {
  val weight = WasteCalculator.hundredths(weightInput)
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(20.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp)
  ) {
    item { MitraPageTitle("Input Berat Sampah", "Masukkan total berat sampah yang ditimbang") }
    item {
      Text(
        "Masukkan berat sampah",
        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp, lineHeight = 30.sp)
      )
    }
    item {
      Surface(
        color = EcoColor.Green700,
        border = BorderStroke(1.dp, EcoColor.Green900),
        shape = RoundedCornerShape(21.dp)
      ) {
        Row(
          Modifier.fillMaxWidth().padding(horizontal = 26.dp, vertical = 22.dp)
            .semantics { contentDescription = "Berat sampah $weightInput kilogram" },
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            weightInput,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            fontSize = 38.sp,
            lineHeight = 48.sp,
            fontWeight = FontWeight.Bold,
            color = EcoColor.White
          )
          Text("Kg", fontSize = 20.sp, color = EcoColor.White)
        }
      }
    }
    item {
      Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          listOf("−0,5" to -50, "+0,1" to 10, "+0,5" to 50, "+1,0" to 100).forEach { (label, delta) ->
            Surface(
              onClick = { onInputChange(WasteCalculator.adjust(weightInput, delta)) },
              modifier = Modifier.weight(1f).height(48.dp)
                .semantics { contentDescription = "$label kilogram" },
              color = EcoColor.White,
              border = BorderStroke(1.dp, EcoColor.Green900),
              shape = RoundedCornerShape(50)
            ) {
              androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                Text(label, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
              }
            }
          }
        }
        listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf(",", "0", "delete")).forEach { keys ->
          Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            keys.forEach { key ->
              KeypadButton(key, { onInputChange(WasteCalculator.enter(weightInput, key)) }, Modifier.weight(1f))
            }
          }
        }
        Text("Maks. 9.999,99 kg · 2 angka desimal", fontSize = 10.sp, color = EcoColor.Green700)
      }
    }
    item {
      MitraCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
          Icon(Lucide.Gamepad2, null, Modifier.size(48.dp))
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Perkiraan poin yang akan diberikan", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, lineHeight = 18.sp)
            Text("Tarif demo: ${WasteCalculator.POINTS_PER_KG} poin / kg", fontSize = 10.sp, color = EcoColor.Green800)
            Text("${WasteCalculator.points(weight)} poin", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = EcoColor.Green800)
            Text("dari ${WasteCalculator.format(weight)} kg", fontSize = 10.sp, color = EcoColor.Green800)
          }
        }
      }
    }
    item {
      EcoButton(
        text = "Generate QR",
        onClick = onGenerateQr,
        enabled = weight > 0,
        containerColor = EcoColor.Green700,
        endIcon = { Icon(Lucide.ArrowRight, null, Modifier.size(20.dp)) }
      )
      if (weight == 0) {
        Text("Masukkan berat lebih dari 0 kg untuk membuat QR.", Modifier.padding(top = 8.dp), fontSize = 10.sp, color = EcoColor.Green700)
      }
    }
  }
}

@Composable
private fun KeypadButton(key: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
  Surface(
    onClick = onClick,
    modifier = modifier.height(48.dp),
    color = if (key == "," || key == "delete") EcoColor.Green400 else EcoColor.Green100,
    shape = RoundedCornerShape(50)
  ) {
    androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
      if (key == "delete") {
        Icon(Lucide.Delete, "Hapus digit terakhir", Modifier.size(23.dp))
      } else {
        Text(key, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = EcoColor.Green900)
      }
    }
  }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 850)
@Composable
private fun AdminCalculatorPreview() {
  EcosnapTheme(darkTheme = false, dynamicColor = false) {
    AdminCalculatorScreen("2,50", {}, {})
  }
}
