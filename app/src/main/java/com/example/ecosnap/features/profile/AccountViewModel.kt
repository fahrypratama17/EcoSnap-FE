package com.example.ecosnap.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ecosnap.features.auth.PasswordResetResult
import com.example.ecosnap.features.auth.PasswordResetSender
import com.example.ecosnap.features.profile.data.ProfileData
import com.example.ecosnap.features.profile.data.ProfileRepository
import com.example.ecosnap.features.profile.data.profileMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountUiState(
  val profile: ProfileData? = null,
  val loading: Boolean = true,
  val loadError: String? = null,
  val saving: Boolean = false,
  val saveError: String? = null,
  val saved: Boolean = false,
  /** Increments on every server-confirmed save. */
  val saveCount: Int = 0,
  val resetBusy: Boolean = false,
  val resetMessage: String? = null,
  val resetFailed: Boolean = false
)

/** Account and security: fresh GET /me, PATCH of name/phone, and Firebase reset-email request. */
class AccountViewModel(private val repository: ProfileRepository) : ViewModel() {
  private val _state = MutableStateFlow(AccountUiState())
  val state: StateFlow<AccountUiState> = _state.asStateFlow()

  private var generation = 0
  private var job: Job? = null

  fun onResume() = load()

  fun retry() = load()

  private fun load() {
    if (_state.value.saving) return
    val token = ++generation
    job?.cancel()
    _state.update { it.copy(loading = it.profile == null, loadError = null) }
    job = viewModelScope.launch {
      try {
        val profile = repository.profile()
        if (token != generation) return@launch
        _state.update { it.copy(profile = profile, loading = false, loadError = null) }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        if (token != generation) return@launch
        _state.update {
          it.copy(loading = false, loadError = e.profileMessage("Profil belum dapat dimuat. Silakan coba lagi."))
        }
      }
    }
  }

  fun clearSaved() = _state.update { it.copy(saved = false, saveError = null) }

  /** Sends only the fields that differ from the stored profile. */
  fun save(fullName: String, phone: String) {
    val current = _state.value
    val stored = current.profile ?: return
    if (current.saving) return
    val newName = fullName.trim().takeIf { it != stored.fullName }
    val newPhone = phone.trim().filterNot { it == ' ' || it == '-' }.takeIf { it != stored.phone }
    generation++
    job?.cancel()
    _state.update { it.copy(saving = true, saveError = null, saved = false, loading = false) }
    job = viewModelScope.launch {
      try {
        val updated = repository.updateProfile(newName, newPhone)
        _state.update { it.copy(profile = updated, saving = false, saved = true, saveCount = it.saveCount + 1) }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _state.update {
          it.copy(saving = false, saveError = e.profileMessage("Perubahan belum dapat disimpan. Silakan coba lagi."))
        }
      }
    }
  }

  /** Reset mail goes to the account's own email through Firebase; nothing is changed in the app. */
  fun sendPasswordReset() {
    val email = _state.value.profile?.email.orEmpty()
    if (_state.value.resetBusy) return
    if (email.isEmpty()) {
      _state.update { it.copy(resetFailed = true, resetMessage = "Akun ini tidak memiliki email untuk reset kata sandi.") }
      return
    }
    _state.update { it.copy(resetBusy = true, resetMessage = null, resetFailed = false) }
    viewModelScope.launch {
      when (val result = PasswordResetSender.send(email)) {
        PasswordResetResult.Sent -> _state.update {
          it.copy(resetBusy = false, resetFailed = false, resetMessage = "Tautan reset kata sandi dikirim ke $email. Periksa kotak masuk dan folder spam.")
        }
        is PasswordResetResult.Failure -> _state.update {
          it.copy(resetBusy = false, resetFailed = true, resetMessage = result.message)
        }
      }
    }
  }
}

class AccountViewModelFactory(
  private val repository: ProfileRepository = ProfileRepository()
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    require(modelClass.isAssignableFrom(AccountViewModel::class.java))
    @Suppress("UNCHECKED_CAST")
    return AccountViewModel(repository) as T
  }
}
