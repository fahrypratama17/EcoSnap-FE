package com.example.ecosnap.features.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
internal fun AuthOperationStatus(busy: Boolean, error: String?) {
  if (busy) {
    CircularProgressIndicator(color = EcoColor.Green700)
    Spacer(Modifier.height(12.dp))
  }
  if (error != null) {
    Text(error, color = EcoColor.TextError, style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(12.dp))
  }
}

@Composable
fun SessionUnavailableScreen(
  email: String,
  busy: Boolean,
  error: String?,
  onRetry: () -> Unit,
  onSignOut: () -> Unit
) {
  SessionPanel {
    Text("Akun belum dapat dimuat", style = MaterialTheme.typography.titleLarge, color = EcoColor.Green900)
    Text(email, color = EcoColor.Green700)
    Text(
      if (email.isBlank()) "Pemulihan sesi belum selesai. Coba lagi atau keluar."
      else "Sesi Anda tersimpan, tetapi layanan akun belum tersedia. Coba lagi atau keluar."
    )
    AuthOperationStatus(busy, error)
    EcoButton("Coba lagi", onRetry, enabled = !busy)
    EcoButton("Keluar", onSignOut, enabled = !busy)
  }
}


@Composable
private fun SessionPanel(content: @Composable () -> Unit) {
  Column(
    modifier = Modifier.fillMaxSize().safeDrawingPadding()
      .verticalScroll(rememberScrollState()).padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    horizontalAlignment = Alignment.Start
  ) {
    content()
  }
}
