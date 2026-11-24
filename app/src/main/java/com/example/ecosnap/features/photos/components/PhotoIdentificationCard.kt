package com.example.ecosnap.features.photos.components

import android.net.Uri
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.size
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Recycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ecosnap.features.photos.model.RecyclingStatus
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.photos.model.WastePhotosResult

@Composable
fun PhotoIdentificationCard(
  photoUri: Uri,
  result: WastePhotosResult,
  modifier: Modifier = Modifier
) {
  var imageUnavailable by remember(photoUri) { mutableStateOf(false) }
  EcoCard(modifier = modifier.fillMaxWidth()) {
    Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
      AsyncImage(
        model = photoUri,
        contentDescription = "Foto sampah yang dipilih",
        modifier = Modifier.fillMaxWidth().height(160.dp),
        contentScale = ContentScale.Crop,
        onError = { imageUnavailable = true },
        onSuccess = { imageUnavailable = false }
      )
      if (imageUnavailable) {
        Box(Modifier.fillMaxWidth().height(160.dp).background(EcoColor.Green100), contentAlignment = Alignment.Center) {
          Icon(Lucide.Recycle, "Pratinjau foto tidak tersedia", Modifier.size(64.dp), tint = EcoColor.Green700)
        }
      }
    }

    Spacer(Modifier.height(12.dp))

    Text(
      text = result.objectName,
      style = MaterialTheme.typography.titleMedium.copy(
        fontSize = 17.sp,
        lineHeight = 23.sp
      ),
      color = EcoColor.Green900
    )

    Spacer(Modifier.height(8.dp))

    Row(
      modifier = Modifier.horizontalScroll(
      rememberScrollState()
    ),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      listOf(result.material, result.category).distinct().forEach { tag ->
        PhotoMaterialChip(label = tag)
      }
      val colors = when (result.recyclingStatus) {
        RecyclingStatus.RECYCLABLE -> EcoColor.Green100 to EcoColor.Green800
        RecyclingStatus.CONDITIONALLY_RECYCLABLE -> Color(0xFFFFF1DE) to Color(0xFF855420)
        RecyclingStatus.NON_RECYCLABLE -> Color(0xFFFFE7E2) to EcoColor.TextError
        RecyclingStatus.UNKNOWN -> Color(0xFFEEEEEE) to Color(0xFF444444)
      }
      PhotoMaterialChip(result.recyclingStatus.label, containerColor = colors.first, contentColor = colors.second)
    }

    Spacer(Modifier.height(10.dp))

    Text(
      text = result.description,
      style = MaterialTheme.typography.bodyMedium.copy(
        fontSize = 12.sp,
        lineHeight = 18.sp
      ),
      color = EcoColor.Green700
    )
    if (result.hazardous) {
      Spacer(Modifier.height(8.dp))
      Text("Sampah berbahaya — hindari kontak langsung dan ikuti petunjuk penanganan.", color = EcoColor.TextError, style = MaterialTheme.typography.bodyMedium)
    }
    result.verificationNote?.let { note ->
      Spacer(Modifier.height(8.dp))
      Text(note, color = Color(0xFF855420), style = MaterialTheme.typography.bodyMedium)
    }
    if (result.recyclingStatus == RecyclingStatus.CONDITIONALLY_RECYCLABLE) {
      Spacer(Modifier.height(8.dp))
      Text("Penerimaan bergantung pada kondisi material dan ketentuan fasilitas setempat.", color = Color(0xFF855420), style = MaterialTheme.typography.bodyMedium)
    }
  }
}
