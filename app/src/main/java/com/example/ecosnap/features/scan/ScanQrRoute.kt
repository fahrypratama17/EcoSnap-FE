package com.example.ecosnap.features.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.deposits.ClaimPreview
import com.example.ecosnap.features.deposits.DepositText
import com.example.ecosnap.features.deposits.ItemLines
import com.example.ecosnap.features.deposits.Notice
import com.example.ecosnap.features.scan.components.ScanPointsCard

/** Scan, preview and explicitly claim a partner QR; [onClaimed] receives the deposit UUID. */
@Composable
fun ScanQrRoute(accountUid: String, onBack: () -> Unit, onClaimed: (String) -> Unit) {
  val vm: ScanQrViewModel = viewModel(key = "scan-qr-$accountUid", factory = ScanQrViewModel.Factory())
  val state by vm.state.collectAsStateWithLifecycle()

  LaunchedEffect(state.claimedDepositId) {
    state.claimedDepositId?.let(onClaimed)
  }

  when (state.phase) {
    ScanPhase.Scanning, ScanPhase.DecodingImage -> ScanQrScreen(
      state = state,
      onBack = onBack,
      onDecoded = vm::onDecoded,
      onManualToken = vm::submitManual,
      onImagePicked = vm::onImagePicked,
      onGalleryUnavailable = vm::onGalleryUnavailable,
    )

    ScanPhase.LoadingPreview, ScanPhase.Claiming, ScanPhase.Claimed -> Column(
      Modifier.fillMaxSize().background(Color.White).padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      CircularProgressIndicator(color = EcoColor.Green700)
      Text(
        when (state.phase) {
          ScanPhase.LoadingPreview -> "Memeriksa QR..."
          ScanPhase.Claiming -> "Mengklaim poin..."
          else -> "Membuka rincian setoran..."
        },
        modifier = Modifier.padding(top = 12.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = EcoColor.Green700,
      )
    }

    ScanPhase.Preview -> state.preview?.let { PreviewPanel(it, onClaim = vm::claim, onRescan = vm::rescan) }

    ScanPhase.Failed -> Column(
      Modifier.fillMaxSize().background(Color.White).padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Text(
        state.message ?: "Terjadi kesalahan.",
        style = MaterialTheme.typography.bodyMedium,
        color = EcoColor.TextError,
        textAlign = TextAlign.Center,
      )
      if (state.failedDuringClaim && state.canRetry) {
        Text(
          "Hasil klaim belum diketahui. Coba lagi dengan kode yang sama; poin tidak akan ditambahkan dua kali.",
          modifier = Modifier.padding(top = 8.dp),
          style = MaterialTheme.typography.bodyMedium,
          color = EcoColor.Green700,
          textAlign = TextAlign.Center,
        )
      }
      if (state.canRetry) TextButton(onClick = vm::retry) { Text("Coba lagi") }
      TextButton(onClick = vm::rescan) { Text("Pindai ulang") }
      TextButton(onClick = onBack) { Text("Tutup") }
    }
  }
}

@Composable
private fun PreviewPanel(preview: ClaimPreview, onClaim: () -> Unit, onRescan: () -> Unit) {
  Column(
    Modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState()).padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Text("Konfirmasi Setoran", style = MaterialTheme.typography.headlineMedium, color = EcoColor.Green900)
    Text("Poin baru masuk setelah Anda menekan Klaim Poin.", style = MaterialTheme.typography.bodyMedium, color = EcoColor.Green700)
    ScanPointsCard(
      label = "Poin Setoran",
      points = preview.totalPoints,
      weightKg = DepositText.kg(preview.totalWeightKg),
    )
    if (preview.usesDemoRates) Notice("Memakai tarif demo/simulasi, bukan nilai tukar nyata.")
    Text("Mitra: ${preview.partnerName}", style = MaterialTheme.typography.bodyMedium, color = EcoColor.Green900)
    ItemLines(preview.items)
    EcoButton("Klaim Poin", onClaim, Modifier.fillMaxWidth())
    TextButton(onClick = onRescan, modifier = Modifier.fillMaxWidth()) { Text("Pindai Ulang") }
  }
}
