package com.example.ecosnap.features.photos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor
import kotlinx.coroutines.delay

@Composable
fun PhotoReviewScreen(state: WasteScanState, onAnalyze: () -> Unit, onRetake: () -> Unit) {
  var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
  LaunchedEffect(state.retryAtMillis) {
    while (state.retryAtMillis > now) {
      delay(500)
      now = System.currentTimeMillis()
    }
  }
  val wait = ((state.retryAtMillis - now).coerceAtLeast(0) + 999) / 1000
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item { Text("Konfirmasi Foto Sampah", style = MaterialTheme.typography.titleLarge, color = EcoColor.Green900) }
    item {
      EcoCard {
        AsyncImage(state.photoUri, "Foto sampah yang akan dianalisis", Modifier.fillMaxWidth().height(300.dp), contentScale = ContentScale.Fit)
      }
    }
    item {
      Text("Pastikan satu objek sampah terlihat jelas. Foto akan dikirim ke layanan AI untuk dianalisis.", style = MaterialTheme.typography.bodyMedium, color = EcoColor.Green700)
    }
    if (state.loading) item {
      Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CircularProgressIndicator(color = EcoColor.Green700)
        Text("Mengidentifikasi jenis sampah...", color = EcoColor.Green700)
      }
    }
    state.error?.let { message -> item { Text(message, color = EcoColor.TextError, style = MaterialTheme.typography.bodyMedium) } }
    item {
      EcoButton(
        text = when {
          state.loading -> "Sedang menganalisis..."
          wait > 0 -> "Coba lagi dalam $wait detik"
          state.error != null -> "Coba Lagi"
          else -> "Analisis Foto"
        },
        enabled = !state.loading && state.photoUri != null && wait == 0L,
        onClick = onAnalyze,
        containerColor = EcoColor.Green700
      )
      TextButton(onClick = onRetake) { Text("Ambil atau Pilih Foto Lain", color = EcoColor.Green700) }
    }
  }
}
