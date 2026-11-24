package com.example.ecosnap.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ecosnap.features.profile.data.ProfileRepository
import com.example.ecosnap.features.profile.data.ProfileSettings
import com.example.ecosnap.features.profile.data.profileMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
  val settings: ProfileSettings? = null,
  val loading: Boolean = true,
  val loadError: String? = null,
  /** A toggle write is in flight; both switches are disabled so writes never overlap. */
  val saving: Boolean = false,
  val saveError: String? = null
)

/**
 * Switch values only ever come from a server answer; a failed write leaves the previous value shown
 * with an error. One instance per account UID.
 */
class SettingsViewModel(
  private val isUser: Boolean,
  private val repository: ProfileRepository
) : ViewModel() {
  private val _state = MutableStateFlow(SettingsUiState())
  val state: StateFlow<SettingsUiState> = _state.asStateFlow()

  private var generation = 0
  private var job: Job? = null

  fun onResume() = load()

  fun retry() = load()

  private fun load() {
    if (_state.value.saving) return
    val token = ++generation
    job?.cancel()
    _state.update { it.copy(loading = it.settings == null, loadError = null) }
    job = viewModelScope.launch {
      try {
        val settings = repository.settings()
        if (token != generation) return@launch
        _state.update { it.copy(settings = settings, loading = false, loadError = null) }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        if (token != generation) return@launch
        _state.update {
          it.copy(loading = false, loadError = e.profileMessage("Pengaturan belum dapat dimuat. Silakan coba lagi."))
        }
      }
    }
  }

  fun setNotificationEnabled(enabled: Boolean) = write("Pengaturan notifikasi belum dapat disimpan.") {
    val stored = repository.setNotificationEnabled(enabled)
    it.copy(settings = stored)
  }

  fun setShowOnLeaderboard(visible: Boolean) {
    if (!isUser) return
    write("Pengaturan leaderboard belum dapat disimpan.") {
      val stored = repository.setLeaderboardVisible(visible)
      it.copy(settings = it.settings?.copy(showOnLeaderboard = stored))
    }
  }

  private fun write(fallback: String, block: suspend (SettingsUiState) -> SettingsUiState) {
    val current = _state.value
    if (current.saving || current.settings == null) return
    generation++
    job?.cancel()
    _state.update { it.copy(saving = true, saveError = null, loading = false) }
    job = viewModelScope.launch {
      try {
        val next = block(_state.value)
        _state.value = next.copy(saving = false, saveError = null)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _state.update { it.copy(saving = false, saveError = e.profileMessage(fallback)) }
      }
    }
  }
}

class SettingsViewModelFactory(
  private val isUser: Boolean,
  private val repository: ProfileRepository = ProfileRepository()
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    require(modelClass.isAssignableFrom(SettingsViewModel::class.java))
    @Suppress("UNCHECKED_CAST")
    return SettingsViewModel(isUser, repository) as T
  }
}
