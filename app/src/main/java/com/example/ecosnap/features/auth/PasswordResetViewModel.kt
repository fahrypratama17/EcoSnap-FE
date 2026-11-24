package com.example.ecosnap.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecosnap.features.auth.model.isValidEmail
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/** Outcome of asking Firebase to email a password-reset link. [Failure.message] is user-safe text. */
sealed interface PasswordResetResult {
  data object Sent : PasswordResetResult
  data class Failure(val message: String) : PasswordResetResult
}

/**
 * Firebase `sendPasswordResetEmail` only. The new password is chosen on Firebase's hosted page via
 * the emailed link; the app never sees a reset code or a new password. An unknown account is reported
 * like a sent request so the form cannot be used to probe which emails are registered. Messages never
 * contain SDK exception text.
 */
object PasswordResetSender {
  suspend fun send(email: String, auth: FirebaseAuth = FirebaseAuth.getInstance()): PasswordResetResult {
    val address = email.trim()
    if (!isValidEmail(address)) return PasswordResetResult.Failure("Masukkan email yang valid.")
    return try {
      auth.sendPasswordResetEmail(address).await()
      PasswordResetResult.Sent
    } catch (e: CancellationException) {
      throw e
    } catch (_: FirebaseAuthInvalidUserException) {
      PasswordResetResult.Sent
    } catch (_: FirebaseAuthInvalidCredentialsException) {
      PasswordResetResult.Failure("Format email tidak valid.")
    } catch (_: FirebaseTooManyRequestsException) {
      PasswordResetResult.Failure("Terlalu banyak permintaan. Coba lagi beberapa saat lagi.")
    } catch (_: FirebaseNetworkException) {
      PasswordResetResult.Failure("Tidak dapat terhubung. Periksa koneksi internet Anda.")
    } catch (_: IOException) {
      PasswordResetResult.Failure("Tidak dapat terhubung. Periksa koneksi internet Anda.")
    } catch (_: Exception) {
      PasswordResetResult.Failure("Email reset belum dapat dikirim. Silakan coba lagi.")
    }
  }
}

data class PasswordResetUiState(
  val busy: Boolean = false,
  /** Address the request was accepted for; null while the form is shown. */
  val sentTo: String? = null,
  val error: String? = null
)

class PasswordResetViewModel : ViewModel() {
  private val mutableState = MutableStateFlow(PasswordResetUiState())
  val state: StateFlow<PasswordResetUiState> = mutableState.asStateFlow()

  fun send(email: String) {
    if (mutableState.value.busy) return
    val address = email.trim()
    mutableState.update { it.copy(busy = true, error = null) }
    viewModelScope.launch {
      when (val result = PasswordResetSender.send(address)) {
        PasswordResetResult.Sent -> mutableState.value = PasswordResetUiState(sentTo = address)
        is PasswordResetResult.Failure ->
          mutableState.update { it.copy(busy = false, error = result.message) }
      }
    }
  }

  fun editEmail() {
    if (!mutableState.value.busy) mutableState.value = PasswordResetUiState()
  }

  fun clearError() {
    if (!mutableState.value.busy) mutableState.update { it.copy(error = null) }
  }
}
