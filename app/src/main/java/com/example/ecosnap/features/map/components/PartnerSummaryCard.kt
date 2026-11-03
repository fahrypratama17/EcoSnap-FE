package com.example.ecosnap.features.map.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.map.model.WastePartner

@Composable
fun PartnerSummaryCard(
  partner: WastePartner,
  isFavorite: Boolean,
  onFavoriteClick: () -> Unit,
  onDetailClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  EcoCard(
    modifier = modifier.fillMaxWidth(),
    contentPadding = PaddingValues(12.dp),
    shadowElevation = 5.dp
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "MITRA",
        style = MaterialTheme.typography.labelSmall,
        color = EcoColor.Green700
      )

      Spacer(Modifier.weight(1f))

      IconButton(
        onClick = onFavoriteClick,
        modifier = Modifier.size(32.dp)
      ) {
        Icon(
          imageVector = Lucide.Heart,
          contentDescription = if (isFavorite) {
            "Hapus favorit"
          } else {
            "Tambahkan favorit"
          },
          tint = if (isFavorite) {
            EcoColor.TextError
          } else {
            EcoColor.Green800
          }
        )
      }
    }

    Spacer(Modifier.height(3.dp))

    Text(
      text = partner.name,
      style = MaterialTheme.typography.titleMedium.copy(
        fontSize = 16.sp,
        lineHeight = 22.sp
      ),
      color = EcoColor.Green900
    )

    Text(
      text = partner.area,
      style = MaterialTheme.typography.bodyMedium,
      color = EcoColor.Green700
    )

    Spacer(Modifier.height(7.dp))

    Row(
      horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
      partner.acceptedMaterials.take(3).forEach {
        PartnerMaterialChip(material = it)
      }
    }

    Spacer(Modifier.height(12.dp))

    EcoButton(
      text = "Lihat detail mitra",
      onClick = onDetailClick,
      containerColor = EcoColor.Green700,
      contentColor = Color.White,
      endIcon = {
        Icon(
          imageVector = Lucide.ArrowRight,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
      }
    )
  }
}