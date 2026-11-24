package com.example.ecosnap.features.map.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Recycle
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.core.model.WasteCategory

@Composable
fun PartnerMaterialChip(
  material: WasteCategory,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(8.dp),
    color = EcoColor.Green100
  ) {
    Text(
      text = material.name,
      color = EcoColor.Green800,
      style = MaterialTheme.typography.labelSmall,
      modifier = Modifier.padding(
        horizontal = 9.dp,
        vertical = 5.dp
      )
    )
  }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun PartnerAcceptedMaterialCard(
  materials: List<WasteCategory>,
  modifier: Modifier = Modifier
) {
  EcoCard(
    modifier = modifier.fillMaxWidth()
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Lucide.Recycle,
        contentDescription = null,
        tint = EcoColor.Green800,
        modifier = Modifier.size(20.dp)
      )

      Spacer(Modifier.width(8.dp))

      Text(
        text = "Jenis Sampah Diterima",
        style = MaterialTheme.typography.titleMedium,
        color = EcoColor.Green900
      )
    }

    Spacer(Modifier.height(10.dp))

    if (materials.isEmpty()) {
      Text(
        text = "Mitra belum mencantumkan jenis sampah yang diterima.",
        style = MaterialTheme.typography.bodyMedium,
        color = EcoColor.Green700
      )
    } else {
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        materials.forEach {
          PartnerMaterialChip(it)
        }
      }

      materials.filter { it.handlingGuide.isNotBlank() }.forEach { material ->
        Spacer(Modifier.height(10.dp))

        Text(
          text = material.name,
          style = MaterialTheme.typography.labelLarge,
          color = EcoColor.Green900
        )

        Text(
          text = if (material.requiresSpecialHandling) {
            "Penanganan khusus: ${material.handlingGuide}"
          } else {
            material.handlingGuide
          },
          style = MaterialTheme.typography.bodyMedium,
          color = EcoColor.Green700
        )
      }
    }

    Spacer(Modifier.height(10.dp))

    Text(
      text = "Pastikan kondisi dan jenis material sesuai dengan ketentuan mitra sebelum menyetor.",
      style = MaterialTheme.typography.bodyMedium,
      color = EcoColor.Green700
    )
  }
}