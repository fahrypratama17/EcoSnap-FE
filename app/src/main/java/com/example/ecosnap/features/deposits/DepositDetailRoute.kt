package com.example.ecosnap.features.deposits

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.os.SystemClock
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecosnap.core.designsystem.components.EcoAppHeader
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.core.network.BackendException
import com.example.ecosnap.features.scan.components.ScanPointsCard
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DepositDetailUiState(
  val loading: Boolean = true,
  val deposit: DepositDetail? = null,
  val notFound: Boolean = false,
  val error: String? = null,
  val refreshing: Boolean = false,
  val qr: IssuedQr? = null,
  val qrBusy: Boolean = false,
  val qrError: String? = null,
)

class DepositDetailViewModel(
  private val depositId: String,
  private val isPartner: Boolean,
  private val repository: DepositRepository,
) : ViewModel() {
  private val _state = MutableStateFlow(DepositDetailUiState())
  val state: StateFlow<DepositDetailUiState> = _state.asStateFlow()
  private var started = false
  private var loadJob: Job? = null
  private var qrJob: Job? = null

  fun onResume() {
    if (!started) {
      started = true
      load(showLoading = true)
    } else {
      load(showLoading = false)
    }
  }

  fun retry() = load(showLoading = true)

  fun refresh() = load(showLoading = _state.value.deposit == null)

  private fun load(showLoading: Boolean) {
    if (!DepositJson.isUuid(depositId)) {
      _state.update { it.copy(loading = false, notFound = true) }
      return
    }
    loadJob?.cancel()
    _state.update {
      if (showLoading) it.copy(loading = true, error = null, notFound = false) else it.copy(refreshing = true)
    }
    loadJob = viewModelScope.launch {
      try {
        val deposit = if (isPartner) repository.partnerDeposit(depositId) else repository.claimedDeposit(depositId)
        _state.update {
          it.copy(
            loading = false,
            refreshing = false,
            deposit = deposit,
            error = null,
            notFound = false,
            // A claimed deposit's QR is dead; drop the in-memory token.
            qr = if (deposit.status == "claimed") null else it.qr,
          )
        }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        val notFound = e is BackendException && e.status == 404
        _state.update {
          when {
            notFound -> it.copy(loading = false, refreshing = false, deposit = null, notFound = true, qr = null)
            it.deposit != null -> it.copy(loading = false, refreshing = false, error = DepositText.message(e))
            else -> it.copy(loading = false, refreshing = false, error = DepositText.message(e))
          }
        }
      }
    }
  }

  /** Issues the first QR or reissues it; reissue invalidates the previous token at once. */
  fun issueQr() {
    if (!isPartner || _state.value.qrBusy) return
    val startedAt = SystemClock.elapsedRealtime()
    _state.update { it.copy(qrBusy = true, qrError = null) }
    qrJob = viewModelScope.launch {
      try {
        val qr = repository.issueQr(depositId, startedAt)
        _state.update { it.copy(qrBusy = false, qr = qr) }
        load(showLoading = false)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        val claimed = e is BackendException && e.code == "deposit_already_claimed"
        _state.update { it.copy(qrBusy = false, qrError = DepositText.message(e), qr = if (claimed) null else it.qr) }
        if (claimed) load(showLoading = false)
      }
    }
  }

  fun dismissQr() = _state.update { it.copy(qr = null) }

  class Factory(private val depositId: String, private val isPartner: Boolean) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
      DepositDetailViewModel(depositId, isPartner, DepositRepository()) as T
  }
}

@Composable
fun DepositDetailRoute(
  accountUid: String,
  depositId: String,
  isPartner: Boolean,
  onBack: () -> Unit,
  onNotifications: () -> Unit,
  onProfile: () -> Unit,
) {
  val vm: DepositDetailViewModel = viewModel(
    key = "deposit-detail-$accountUid-$isPartner-$depositId",
    factory = DepositDetailViewModel.Factory(depositId, isPartner),
  )
  val state by vm.state.collectAsStateWithLifecycle()
  OnResume(vm) { vm.onResume() }
  var confirmReissue by remember { mutableStateOf(false) }

  Column(Modifier.fillMaxSize().background(Color.White)) {
    EcoAppHeader(onNotifications = onNotifications, onProfile = onProfile)
    val deposit = state.deposit
    when {
      state.loading -> LoadingBox("Memuat setoran...")
      state.notFound -> ErrorBox("Setoran tidak ditemukan atau Anda tidak memiliki akses.", null, onBack)
      deposit == null -> ErrorBox(state.error ?: "Setoran tidak dapat dimuat.", vm::retry, onBack)
      else -> Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Text(
          if (isPartner) "Detail Setoran" else "Setoran Berhasil",
          style = MaterialTheme.typography.headlineMedium,
          color = EcoColor.Green900,
        )
        state.error?.let { Notice(it, error = true) }
        ScanPointsCard(
          label = if (isPartner) "Poin Setoran" else "Poin Diperoleh",
          points = deposit.totalPoints,
          weightKg = DepositText.kg(deposit.totalWeightKg),
        )
        if (deposit.usesDemoRates) Notice("Memakai tarif demo/simulasi, bukan nilai tukar nyata.")
        LabelValue("ID Setoran", deposit.id)
        LabelValue("Status", DepositText.status(deposit.status))
        LabelValue("Dibuat", DepositText.dateTime(deposit.createdAt))
        LabelValue("Tarif per", DepositText.dateTime(deposit.ratesAsOf))
        ItemLines(deposit.items)

        if (isPartner && deposit.status != "claimed") {
          state.qr?.let { QrPanel(it, onExpired = vm::refresh) }
          state.qrError?.let { Notice(it, error = true) }
          EcoButton(
            text = when {
              state.qrBusy -> "Memproses..."
              state.qr != null || deposit.status == "qr_issued" -> "Terbitkan Ulang QR"
              else -> "Terbitkan QR"
            },
            onClick = { if (state.qr != null || deposit.status == "qr_issued") confirmReissue = true else vm.issueQr() },
            enabled = !state.qrBusy,
            modifier = Modifier.fillMaxWidth(),
          )
        }
        EcoButton(
          text = if (state.refreshing) "Memuat..." else "Muat Ulang Status",
          onClick = vm::refresh,
          enabled = !state.refreshing,
          modifier = Modifier.fillMaxWidth(),
          containerColor = EcoColor.Green700,
        )
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Kembali") }
      }
    }
  }

  if (confirmReissue) {
    AlertDialog(
      onDismissRequest = { confirmReissue = false },
      title = { Text("Terbitkan ulang QR?") },
      text = { Text("QR sebelumnya langsung tidak berlaku. Token baru berlaku 10 menit.") },
      confirmButton = {
        TextButton(onClick = {
          confirmReissue = false
          vm.issueQr()
        }) { Text("Terbitkan Ulang") }
      },
      dismissButton = { TextButton(onClick = { confirmReissue = false }) { Text("Batal") } },
    )
  }
}

@Composable
private fun QrPanel(qr: IssuedQr, onExpired: () -> Unit) {
  var remainingMs by remember(qr) { mutableLongStateOf(remaining(qr)) }
  LaunchedEffect(qr) {
    while (true) {
      remainingMs = remaining(qr)
      if (remainingMs <= 0L) {
        onExpired()
        return@LaunchedEffect
      }
      delay(1000)
    }
  }
  val expired = remainingMs <= 0L
  // The QR is bearer value: keep it out of screenshots and the recents thumbnail while shown.
  SecureWindow(!expired)

  Column(
    Modifier.fillMaxWidth().background(EcoColor.GreenSoft, RoundedCornerShape(20.dp)).padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Notice(qr.warning, error = true)
    if (expired) {
      Text("QR kedaluwarsa. Terbitkan ulang untuk membuat QR baru.", color = EcoColor.TextError, textAlign = TextAlign.Center)
    } else {
      val bitmap by produceState<Bitmap?>(null, qr.token) {
        value = withContext(Dispatchers.Default) { QrBitmap.create(qr.token, 640) }
      }
      bitmap?.let {
        Image(
          bitmap = it.asImageBitmap(),
          contentDescription = "Kode QR setoran",
          modifier = Modifier.size(240.dp).background(Color.White),
        )
      } ?: Spacer(Modifier.height(240.dp))
      val seconds = (remainingMs + 999) / 1000
      Text(
        "Berlaku %d:%02d".format(seconds / 60, seconds % 60),
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = EcoColor.Green900,
      )
    }
  }
}

private fun remaining(qr: IssuedQr): Long =
  (qr.remainingMs - (SystemClock.elapsedRealtime() - qr.receivedAtElapsedMs)).coerceAtLeast(0L)

@Composable
private fun SecureWindow(enabled: Boolean) {
  val context = LocalContext.current
  DisposableEffect(enabled) {
    val window = context.findActivity()?.window
    if (enabled) window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    onDispose { if (enabled) window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
  }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
  is Activity -> this
  is ContextWrapper -> baseContext.findActivity()
  else -> null
}

internal object QrBitmap {
  fun create(content: String, size: Int): Bitmap {
    val hints = mapOf(EncodeHintType.MARGIN to 2, EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M)
    val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
    val pixels = IntArray(matrix.width * matrix.height)
    for (y in 0 until matrix.height) {
      val row = y * matrix.width
      for (x in 0 until matrix.width) pixels[row + x] = if (matrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE
    }
    return Bitmap.createBitmap(pixels, matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
  }
}
