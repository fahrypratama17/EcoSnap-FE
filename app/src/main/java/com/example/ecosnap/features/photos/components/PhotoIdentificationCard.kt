package com.example.ecosnap.features.photos.components

import android.net.Uri
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
  EcoCard(modifier = modifier.fillMaxWidth()) {
    AsyncImage(
      model = photoUri,
      contentDescription = "Foto sampah yang dipilih",
      modifier = Modifier
        .fillMaxWidth()
        .height(160.dp),
      contentScale = ContentScale.Crop
    )

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
      result.tags.forEach { tag ->
        PhotoMaterialChip(label = tag)
      }
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
  }
}