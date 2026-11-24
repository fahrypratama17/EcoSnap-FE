package com.example.ecosnap.features.photos

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecosnap.features.photos.data.ScanException
import com.example.ecosnap.features.photos.data.ScanResponseParser
import com.example.ecosnap.features.photos.data.WastePhotosRepository
import com.example.ecosnap.features.photos.model.WastePhotosResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

data class WasteScanState(
  val photoUri: String? = null,
  val loading: Boolean = false,
  val result: WastePhotosResult? = null,
  val error: String? = null,
  val retryAtMillis: Long = 0
)

class WasteScanViewModel(
  private val repository: WastePhotosRepository,
  private val savedState: SavedStateHandle
) : ViewModel() {
  private val inFlight = AtomicBoolean(false)
  private var scanJob: Job? = null
  private var generation = 0
  private val mutableState = MutableStateFlow(
    WasteScanState(
      photoUri = savedState["photo_uri"],
      result = savedState.get<String>("scan_response")?.let { runCatching { ScanResponseParser.parse(it) }.getOrNull() }
    )
  )
  val state = mutableState.asStateFlow()

  fun selectImage(uri: Uri): Boolean {
    if (inFlight.get()) return false
    generation++
    savedState["photo_uri"] = uri.toString()
    savedState["scan_response"] = null
    savedState["scan_id"] = UUID.randomUUID().toString()
    mutableState.value = WasteScanState(photoUri = uri.toString())
    return true
  }

  fun analyze() {
    val current = mutableState.value
    val uri = current.photoUri ?: return
    if (current.result != null || current.retryAtMillis > System.currentTimeMillis()) return
    if (!inFlight.compareAndSet(false, true)) return
    val scanId = savedState.get<String>("scan_id") ?: UUID.randomUUID().toString().also {
      savedState["scan_id"] = it
    }
    val requestGeneration = generation
    mutableState.value = current.copy(loading = true, error = null)
    scanJob = viewModelScope.launch {
      try {
        val json = repository.analyze(Uri.parse(uri), scanId)
        val result = ScanResponseParser.parse(json)
        if (generation != requestGeneration) return@launch
        savedState["scan_response"] = json
        mutableState.value = WasteScanState(photoUri = uri, result = result)
      } catch (e: CancellationException) {
        throw e
      } catch (e: ScanException) {
        if (generation == requestGeneration) {
          mutableState.value = WasteScanState(
            photoUri = uri,
            error = e.userMessage,
            retryAtMillis = System.currentTimeMillis() + e.retryAfterSeconds * 1000L
          )
        }
      } catch (_: Exception) {
        if (generation == requestGeneration) {
          mutableState.value = WasteScanState(
            photoUri = uri,
            error = "Pemindaian gagal. Periksa koneksi dan coba lagi."
          )
        }
      } finally {
        if (generation == requestGeneration) inFlight.set(false)
      }
    }
  }

  fun cancel() {
    generation++
    scanJob?.cancel()
    inFlight.set(false)
    mutableState.value = mutableState.value.copy(loading = false)
  }
}
