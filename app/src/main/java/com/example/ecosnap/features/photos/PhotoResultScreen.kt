package com.example.ecosnap.features.photos

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.components.EcoAppHeader
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.photos.components.PhotoHandlingCard
import com.example.ecosnap.features.photos.components.PhotoIdentificationCard
import com.example.ecosnap.features.photos.components.PhotoReuseIdeaCard
import com.example.ecosnap.features.photos.components.ScanIndustryCard
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.features.photos.model.WastePhotosResult

@Composable
fun PhotoResultScreen(
  photoUri: Uri,
  result: WastePhotosResult,
  onBack: () -> Unit,
  onFindPartner: () -> Unit,
  onNotifications: () -> Unit,
  onProfile: () -> Unit,
  modifier: Modifier = Modifier
) {
  var expandedIdeaId by rememberSaveable {
    mutableStateOf<String?>(null)
  }

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
            text = "Foto Sampah",
            color = EcoColor.Green700,
            style = MaterialTheme.typography.labelLarge
          )
        }
      }

      item {
        Text(
          text = "Hasil Scan Sampah",
          style = MaterialTheme.typography.titleLarge,
          color = EcoColor.Green900
        )

        Spacer(Modifier.height(4.dp))

        Text(
          text = "Lihat hasil identifikasi dan manfaat sampahmu.",
          style = MaterialTheme.typography.bodyMedium,
          color = EcoColor.Green700
        )

      }

      item {
        PhotoIdentificationCard(
          photoUri = photoUri,
          result = result
        )
      }

      item {
        PhotoHandlingCard(
          steps = result.handlingSteps
        )
      }

      item {
        Text(
          text = "Sampah Ini Bisa Jadi Apa?",
          style = MaterialTheme.typography.titleMedium,
          color = EcoColor.Green900
        )

        Spacer(Modifier.height(4.dp))

        Text(
          text = "Temukan ide untuk memanfaatkan kembali sampahmu.",
          style = MaterialTheme.typography.bodyMedium,
          color = EcoColor.Green700
        )
      }

      items(
        items = result.reuseIdeas,
        key = { it.id }
      ) { idea ->
        PhotoReuseIdeaCard(
          idea = idea,
          expanded = expandedIdeaId == idea.id,
          onToggle = {
            expandedIdeaId =
              if (expandedIdeaId == idea.id) {
                null
              } else {
                idea.id
              }
          }
        )
      }

      if (result.reuseIdeas.isEmpty()) {
        item {
          EcoCard {
            Text(result.upcyclingReason.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = EcoColor.Green800)
          }
        }
      }

      item {
        ScanIndustryCard(
          description = result.industryPotential,
          possible = result.industryPossible,
          products = result.industryProducts,
          limitations = result.industryLimitations
        )
      }

      if (result.depositRecommended) item {
        result.partnerType?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
        result.depositPreparation?.let { Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 8.dp)) }
        EcoButton(
          text = "Cari Mitra Penyetoran",
          onClick = onFindPartner,
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

      item {
        Spacer(Modifier.height(6.dp))
      }
    }
  }
}
