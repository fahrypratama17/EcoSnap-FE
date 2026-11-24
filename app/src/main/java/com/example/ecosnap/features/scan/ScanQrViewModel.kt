package com.example.ecosnap.features.scan

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ecosnap.core.network.BackendException
import com.example.ecosnap.features.deposits.ClaimPreview
import com.example.ecosnap.features.deposits.DepositRepository
import com.example.ecosnap.features.deposits.DepositText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ScanPhase { Scanning, DecodingImage, LoadingPreview, Preview, Claiming, Failed, Claimed }

data class ScanUiState(
  val phase: ScanPhase = ScanPhase.Scanning,
  val preview: ClaimPreview? = null,
  val message: String? = null,
  /** When [phase] is Failed: the same request may be repeated with the same in-memory token. */
  val canRetry: Boolean = false,
  val failedDuringClaim: Boolean = false,
  val claimedDepositId: String? = null,
)

/**
 * Scan -> preview -> explicit claim. The QR token lives only in [token] (memory): it is never saved,
 * logged or put in navigation. Decoding never claims; the user must press claim after the preview.
 */
class ScanQrViewModel(private val repository: DepositRepository) : ViewModel() {
  private val _state = MutableStateFlow(ScanUiState())
  val state: StateFlow<ScanUiState> = _state.asStateFlow()
  private var token: String? = null
  private var lastRejected: String? = null

  @Synchronized
  fun onDecoded(raw: String) {
    if (_state.value.phase != ScanPhase.Scanning) return
    val candidate = raw.trim()
    if (!TOKEN.matches(candidate)) {
      if (candidate != lastRejected) {
        lastRejected = candidate
        _state.update { it.copy(message = "Kode ini bukan QR setoran EcoSnap.") }
      }
      return
    }
    lastRejected = null
    token = candidate
    loadPreview()
  }

  fun submitManual(text: String) {
    val candidate = text.trim()
    if (_state.value.phase != ScanPhase.Scanning) return
    if (!TOKEN.matches(candidate)) {
      _state.update { it.copy(message = "Kode tidak valid. Periksa kembali kode dari mitra.") }
      return
    }
    onDecoded(candidate)
  }

  fun onImagePicked(resolver: ContentResolver, uri: Uri) {
    if (_state.value.phase != ScanPhase.Scanning) return
    _state.update { it.copy(phase = ScanPhase.DecodingImage, message = null) }
    viewModelScope.launch {
      val outcome = QrDecoder.decodeImage(resolver, uri)
      _state.update { it.copy(phase = ScanPhase.Scanning) }
      when (outcome) {
        is ImageDecode.Found -> onDecoded(outcome.text)
        ImageDecode.NoQr -> _state.update { it.copy(message = "Tidak ada kode QR yang terbaca pada gambar ini.") }
        ImageDecode.Unreadable -> _state.update { it.copy(message = "Gambar tidak dapat dibaca.") }
      }
    }
  }

  fun onGalleryUnavailable() = _state.update { it.copy(message = "Tidak ada aplikasi galeri untuk memilih gambar.") }

  fun retry() {
    val current = _state.value
    if (current.phase != ScanPhase.Failed || !current.canRetry || token == null) return
    if (current.failedDuringClaim) claim() else loadPreview()
  }

  /** Forget the token and return to the camera. */
  fun rescan() {
    token = null
    lastRejected = null
    _state.value = ScanUiState()
  }

  private fun loadPreview() {
    val t = token ?: return
    _state.update { it.copy(phase = ScanPhase.LoadingPreview, message = null, canRetry = false, failedDuringClaim = false) }
    viewModelScope.launch {
      try {
        val preview = repository.previewClaim(t)
        _state.update { it.copy(phase = ScanPhase.Preview, preview = preview, message = null) }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        fail(e, duringClaim = false)
      }
    }
  }

  /** Only reachable from the explicit claim button. */
  fun claim() {
    val t = token ?: return
    val phase = _state.value.phase
    if (phase != ScanPhase.Preview && !(phase == ScanPhase.Failed && _state.value.failedDuringClaim)) return
    _state.update { it.copy(phase = ScanPhase.Claiming, message = null, canRetry = false) }
    viewModelScope.launch {
      try {
        val result = repository.claim(t)
        token = null
        _state.update { it.copy(phase = ScanPhase.Claimed, claimedDepositId = result.depositId, message = null) }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        fail(e, duringClaim = true)
      }
    }
  }

  private fun fail(e: Exception, duringClaim: Boolean) {
    // Without a final HTTP answer (offline, timeout, 5xx, bad body) the exact same token can be resent.
    val final = e is BackendException && (e.status ?: 0) in 400..499 && e.status != 408 && e.status != 429
    if (final) token = null
    _state.update {
      it.copy(
        phase = ScanPhase.Failed,
        message = DepositText.message(e),
        canRetry = !final,
        failedDuringClaim = duringClaim,
        preview = if (final) null else it.preview,
      )
    }
  }

  class Factory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ScanQrViewModel(DepositRepository()) as T
  }

  private companion object {
    val TOKEN = Regex("^[A-Za-z0-9_-]{32,128}$")
  }
}
