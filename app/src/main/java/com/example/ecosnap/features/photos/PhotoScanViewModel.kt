package com.example.ecosnap.features.photos

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecosnap.features.photos.data.AiScanException
import com.example.ecosnap.features.photos.data.RemoteWastePhotosRepository
import com.example.ecosnap.features.photos.data.WastePhotosRepository
import com.example.ecosnap.features.photos.model.NotIdentifiedStatus
import com.example.ecosnap.features.photos.model.WastePhotosResult
import com.example.ecosnap.features.photos.model.WasteScanOutcome
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PhotoScanUiState {
  /** No AI service is configured for this build; nothing is ever classified. */
  data object Unavailable : PhotoScanUiState

  data object Loading : PhotoScanUiState

  data class Success(val result: WastePhotosResult) : PhotoScanUiState

  data class NotIdentified(val status: NotIdentifiedStatus, val message: String) : PhotoScanUiState

  data class Failed(
    val message: String,
    val retryable: Boolean,
    val retryAfterSeconds: Int?
  ) : PhotoScanUiState
}

/**
 * One scan of one image. Survives rotation; leaving the screen clears it, which cancels the in-flight
 * HTTP call and deletes the app's own cache copy of the image.
 */
class PhotoScanViewModel(
  application: Application,
  private val imageUri: Uri,
  private val repository: WastePhotosRepository = RemoteWastePhotosRepository(application)
) : AndroidViewModel(application) {
  private val _state = MutableStateFlow<PhotoScanUiState>(
    if (repository.isAvailable) PhotoScanUiState.Loading else PhotoScanUiState.Unavailable
  )
  val state: StateFlow<PhotoScanUiState> = _state.asStateFlow()

  private var scanId = UUID.randomUUID().toString()
  private var job: Job? = null

  init {
    if (repository.isAvailable) start()
  }

  /** Re-submits the same image (same scan id unless the service rejected it). No-op while running. */
  fun retry() {
    if (job?.isActive == true || !repository.isAvailable) return
    _state.value = PhotoScanUiState.Loading
    start()
  }

  private fun start() {
    job = viewModelScope.launch {
      _state.value = try {
        when (val outcome = repository.analyze(imageUri, scanId)) {
          is WasteScanOutcome.Identified -> PhotoScanUiState.Success(outcome.result)
          is WasteScanOutcome.NotIdentified -> {
            // A new photo is a new scan; this id must never be reused for different bytes.
            scanId = UUID.randomUUID().toString()
            PhotoScanUiState.NotIdentified(outcome.status, outcome.message)
          }
        }
      } catch (e: CancellationException) {
        throw e
      } catch (e: AiScanException) {
        if (e.freshScanId) scanId = UUID.randomUUID().toString()
        PhotoScanUiState.Failed(e.message.orEmpty(), e.retryable, e.retryAfterSeconds)
      }
    }
  }

  override fun onCleared() {
    if (imageUri.scheme == "file") {
      val path = imageUri.path
      if (path != null) {
        val file = File(path)
        val cache = getApplication<Application>().cacheDir
        if (file.parentFile == cache && file.name.startsWith("ecosnap_")) file.delete()
      }
    }
  }
}
