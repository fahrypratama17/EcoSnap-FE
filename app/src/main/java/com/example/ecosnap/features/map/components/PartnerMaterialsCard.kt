package com.example.ecosnap.features.map.components

import androidx.compose.foundation.layout.Arrangement
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
import com.example.ecosnap.features.map.model.WasteMaterial

@Composable
fun PartnerMaterialChip(
  material: WasteMaterial,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(8.dp),
    color = EcoColor.Green100
  ) {
    Text(
      text = material.label,
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
fun PartnerAcceptedMaterialCard(
  materials: Set<WasteMaterial>,
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

    Row(
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      materials.forEach {
        PartnerMaterialChip(it)
      }
    }

    Spacer(Modifier.height(10.dp))

    Text(
      text = "Pastikan kondisi dna jenis material sesuai dengan ketentuan mitra sebelum menyetor.",
      style = MaterialTheme.typography.bodyMedium,
      color = EcoColor.Green700
    )
  }
}