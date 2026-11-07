package com.example.ecosnap.features.photos

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ecosnap.features.photos.data.DemoWastePhotosRepository
import com.example.ecosnap.features.photos.model.WastePhotosResult

@Composable
fun PhotoResultScreen(
  photoUri: Uri,
  onBack: () -> Unit,
  onFindPartner: () -> Unit,
  onNotifications: () -> Unit,
  onProfile: () -> Unit,
  modifier: Modifier = Modifier
) {
  val repository = remember {
    DemoWastePhotosRepository()
  }

  var scanResult by remember(photoUri) {
    mutableStateOf<WastePhotosResult?>(null)
  }

  var errorMessage by remember(photoUri) {
    mutableStateOf<String?>(null)
  }

  var expandedIdeaId by rememberSaveable {
    mutableStateOf<String?>(null)
  }

  LaunchedEffect(photoUri) {
    scanResult = null
    errorMessage = null

    try {
      scanResult = repository.analyze(photoUri)
    } catch (e: Exception) {
      errorMessage = "Gagal memuat hasil scan. Silakan coba lagi."
    }
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

        Spacer(Modifier.height(7.dp))

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFFFFF1DE)
        ) {
          Text(
            text = "MODE DEMO — hasil belum dianalisis AI",
            fontSize = 11.sp,
            color = Color(0xFF855420),
            modifier = Modifier.padding(
              horizontal = 10.dp,
              vertical = 7.dp
            )
          )
        }
      }

      if (errorMessage != null) {
        item {
          Text(
            text = errorMessage.orEmpty(),
            color = EcoColor.TextError
          )
        }
      } else if (scanResult == null) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(150.dp),
            contentAlignment = Alignment.Center
          ) {
            CircularProgressIndicator(
              color = EcoColor.Green700
            )
          }
        }
      } else {
        val result = scanResult!!

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

        item {
          ScanIndustryCard(
            description = result.industryPotential
          )
        }

        item {
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
}