package com.example.ecosnap.features.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Navigation
import com.composables.icons.lucide.Phone
import com.composables.icons.lucide.Recycle
import com.example.ecosnap.core.designsystem.components.EcoAppHeader
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.map.components.PartnerAcceptedMaterialCard
import com.example.ecosnap.features.map.components.PartnerDepositsStepsCard
import com.example.ecosnap.features.map.components.PartnerInformationCard
import com.example.ecosnap.features.map.model.WastePartner

@Composable
fun PartnerDetailScreen(
  partner: WastePartner,
  onBack: () -> Unit,
  onContact: () -> Unit,
  onDirections: () -> Unit,
  onNotifications: () -> Unit,
  onProfile: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White)
  ) {
    EcoAppHeader(
      onNotifications = onNotifications,
      onProfile = onProfile
    )

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(
        horizontal = 16.dp,
        vertical = 14.dp
      ),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        TextButton(
          onClick = onBack,
          contentPadding = PaddingValues(0.dp)
        ) {
          Icon(
            imageVector = Lucide.ArrowLeft,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )

          Spacer(Modifier.width(6.dp))

          Text(
            text = "Kembali ke Peta",
            style = MaterialTheme.typography.labelLarge
          )
        }
      }

      item {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
          color = EcoColor.Green400,
          shape = RoundedCornerShape(12.dp),
          shadowElevation = 3.dp
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Lucide.Recycle,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(82.dp)
            )
          }
        }
      }

      item {
        Text(
          text = partner.name,
          style = MaterialTheme.typography.titleLarge,
          color = EcoColor.Green900
        )
      }

      item {
        PartnerAcceptedMaterialCard(materials = partner.acceptedMaterials)
      }

      item {
        PartnerInformationCard(partner = partner)
      }

      item {
        PartnerDepositsStepsCard()
      }

      item {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          EcoButton(
            text = "Hubungi",
            onClick = onContact,
            modifier = Modifier.weight(1f),
            containerColor = Color.White,
            contentColor = EcoColor.Green800,
            startIcon = {
              Icon(
                imageVector = Lucide.Phone,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
            }
          )

          EcoButton(
            text = "Petunjuk Arah",
            onClick = onDirections,
            modifier = Modifier.weight(1f),
            containerColor = EcoColor.Green700,
            contentColor = Color.White,
            startIcon = {
              Icon(
                imageVector = Lucide.Navigation,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
            }
          )
        }
      }
    }
  }
}