package com.example.ecosnap.features.photos

import android.app.Application
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import coil3.compose.AsyncImage
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.components.EcoAppHeader
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.photos.components.PhotoHandlingCard
import com.example.ecosnap.features.photos.components.PhotoIdentificationCard
import com.example.ecosnap.features.photos.components.PhotoNoticeCard
import com.example.ecosnap.features.photos.components.PhotoReuseIdeaCard
import com.example.ecosnap.features.photos.components.ScanIndustryCard
import com.example.ecosnap.features.photos.model.NotIdentifiedStatus
import com.example.ecosnap.features.photos.model.WastePhotosResult
import kotlinx.coroutines.delay

@Composable
fun PhotoResultScreen(
  photoUri: Uri,
  onBack: () -> Unit,
  onFindPartner: () -> Unit,
  onNotifications: () -> Unit,
  onProfile: () -> Unit,
  modifier: Modifier = Modifier
) {
  val viewModel: PhotoScanViewModel = viewModel(
    key = "photo_scan_$photoUri",
    factory = viewModelFactory {
      initializer {
        PhotoScanViewModel(this[APPLICATION_KEY] as Application, photoUri)
      }
    }
  )
  val state by viewModel.state.collectAsStateWithLifecycle()

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

      when (val current = state) {
        PhotoScanUiState.Loading -> item {
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

        PhotoScanUiState.Unavailable -> item {
          ScanProblemCard(
            photoUri = photoUri,
            title = "Analisis AI belum tersedia",
            message = "Layanan analisis AI belum dikonfigurasi pada aplikasi ini, sehingga foto tidak dapat diidentifikasi.",
            primaryText = "Kembali ke Kamera",
            onPrimary = onBack,
            retryText = null,
            onRetry = {}
          )
        }

        is PhotoScanUiState.Failed -> item {
          var waitSeconds by remember(current) {
            mutableIntStateOf(current.retryAfterSeconds ?: 0)
          }
          LaunchedEffect(current) {
            while (waitSeconds > 0) {
              delay(1_000L)
              waitSeconds -= 1
            }
          }
          ScanProblemCard(
            photoUri = photoUri,
            title = "Gagal menganalisis foto",
            message = current.message +
              if (waitSeconds > 0) " Coba lagi dalam $waitSeconds detik." else "",
            primaryText = "Ambil Ulang Foto",
            onPrimary = onBack,
            retryText = if (current.retryable && waitSeconds == 0) "Coba Lagi" else null,
            onRetry = viewModel::retry
          )
        }

        is PhotoScanUiState.NotIdentified -> item {
          ScanProblemCard(
            photoUri = photoUri,
            title = when (current.status) {
              NotIdentifiedStatus.InvalidImage -> "Foto belum bisa dikenali"
              NotIdentifiedStatus.MultipleObjects -> "Terdeteksi lebih dari satu objek"
            },
            message = current.message,
            primaryText = "Ambil Ulang Foto",
            onPrimary = onBack,
            retryText = null,
            onRetry = {}
          )
        }

        is PhotoScanUiState.Success -> scanSuccessItems(
          result = current.result,
          photoUri = photoUri,
          expandedIdeaId = expandedIdeaId,
          onToggleIdea = { id ->
            expandedIdeaId = if (expandedIdeaId == id) null else id
          },
          onFindPartner = onFindPartner
        )
      }
    }
  }
}

private fun androidx.compose.foundation.lazy.LazyListScope.scanSuccessItems(
  result: WastePhotosResult,
  photoUri: Uri,
  expandedIdeaId: String?,
  onToggleIdea: (String) -> Unit,
  onFindPartner: () -> Unit
) {
  item {
    PhotoIdentificationCard(
      photoUri = photoUri,
      result = result
    )
  }

  if (result.hazardous) {
    item {
      PhotoNoticeCard(
        title = "Sampah Berbahaya (B3)",
        lines = listOf("Jangan buang bersama sampah biasa. Serahkan ke fasilitas yang menangani limbah berbahaya."),
        warning = true
      )
    }
  }

  if (result.requiresVerification) {
    item {
      PhotoNoticeCard(
        title = "Perlu Verifikasi",
        lines = listOfNotNull(result.verificationNote),
        warning = true
      )
    }
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
      text = if (result.reuseIdeas.isNotEmpty()) {
        "Temukan ide untuk memanfaatkan kembali sampahmu."
      } else {
        result.upcyclingReason ?: "Tidak ada ide pemanfaatan kembali yang aman untuk sampah ini."
      },
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
      onToggle = { onToggleIdea(idea.id) }
    )
  }

  item {
    ScanIndustryCard(
      description = result.industrialDescription,
      potentialProducts = result.potentialProducts,
      limitations = result.industrialLimitations
    )
  }

  if (result.depositRecommended) {
    item {
      PhotoNoticeCard(
        title = "Rekomendasi Penyetoran",
        lines = listOfNotNull(
          result.depositPartnerType?.let { "Jenis mitra: $it" },
          result.depositSearchKeyword?.let { "Kata kunci pencarian: $it" },
          result.depositPreparationNote
        ),
        warning = false
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
  } else if (result.depositPreparationNote != null) {
    item {
      PhotoNoticeCard(
        title = "Penyetoran",
        lines = listOf(result.depositPreparationNote),
        warning = false
      )
    }
  }

  item {
    Spacer(Modifier.height(6.dp))
  }
}

@Composable
private fun ScanProblemCard(
  photoUri: Uri,
  title: String,
  message: String,
  primaryText: String,
  onPrimary: () -> Unit,
  retryText: String?,
  onRetry: () -> Unit
) {
  EcoCard(modifier = Modifier.fillMaxWidth()) {
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
      text = title,
      style = MaterialTheme.typography.titleMedium,
      color = EcoColor.Green900
    )

    Spacer(Modifier.height(6.dp))

    Text(
      text = message,
      style = MaterialTheme.typography.bodyMedium,
      color = EcoColor.TextError
    )

    Spacer(Modifier.height(12.dp))

    if (retryText != null) {
      EcoButton(
        text = retryText,
        onClick = onRetry,
        containerColor = EcoColor.Green700,
        contentColor = Color.White
      )

      Spacer(Modifier.height(8.dp))
    }

    TextButton(onClick = onPrimary) {
      Text(
        text = primaryText,
        color = EcoColor.Green700
      )
    }
  }
}
