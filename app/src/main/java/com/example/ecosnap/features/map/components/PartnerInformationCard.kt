package com.example.ecosnap.features.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Clock3
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Phone
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.map.model.WastePartner

@Composable
fun PartnerInfoRow(
  icon: ImageVector,
  title: String,
  description: String,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(44.dp)
        .background(
          EcoColor.Green100,
          RoundedCornerShape(12.dp)
        ),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = EcoColor.Green700,
        modifier = Modifier.size(22.dp)
      )
    }

    Spacer(Modifier.height(12.dp))

    Column(
      modifier = Modifier.weight(1f)
    ) {
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
fun PartnerInformationCard(
  partner: WastePartner,
  modifier: Modifier = Modifier
) {
  EcoCard(
    modifier = modifier.fillMaxWidth()
  ) {
    Text(
      text = "Informasi Lokasi",
      style = MaterialTheme.typography.titleMedium,
      color = EcoColor.Green900
    )

    Spacer(Modifier.height(12.dp))

    PartnerInfoRow(
      icon = Lucide.MapPin,
      title = "Alamat Mitra",
      description = partner.address
    )

    HorizontalDivider(
      modifier = Modifier.padding(vertical = 12.dp),
      color = EcoColor.Green200
    )

    PartnerInfoRow(
      icon = Lucide.Clock3,
      title = "Jam Operasional",
      description = partner.openingHours
    )

    HorizontalDivider(
      modifier = modifier.padding(vertical = 12.dp),
      color = EcoColor.Green200
    )

    PartnerInfoRow(
      icon = Lucide.Phone,
      title = "Kontak Mitra",
      description = partner.phone
    )
  }
}