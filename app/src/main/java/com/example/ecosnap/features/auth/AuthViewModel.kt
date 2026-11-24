package com.example.ecosnap.features.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ecosnap.core.auth.AccountApi
import com.example.ecosnap.core.network.BackendClient
import com.example.ecosnap.core.auth.AccountRole
import com.example.ecosnap.core.auth.AuthFailure
import com.example.ecosnap.core.auth.AuthRepository
import com.example.ecosnap.core.auth.PendingRegistrationStore
import com.example.ecosnap.core.auth.RegistrationProfile
import com.example.ecosnap.core.auth.SessionOutcome
import com.example.ecosnap.features.auth.model.AuthRole
import com.example.ecosnap.features.auth.model.RegistrationData
import com.example.ecosnap.features.onboarding.data.OnboardingPreferences
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AuthUiState(
  val initialized: Boolean = false,
  val onboardingCompleted: Boolean = false,
  val session: SessionOutcome = SessionOutcome.SignedOut,
  val busy: Boolean = false,
  val error: String? = null
)

class AuthViewModel(
  private val repository: AuthRepository,
  private val onboarding: OnboardingPreferences
) : ViewModel() {
  private val mutableState = MutableStateFlow(AuthUiState())
  val state: StateFlow<AuthUiState> = mutableState.asStateFlow()

  init {
    // Backend-forced or SDK sign-out (null) must end the session even outside an operation. Non-null
    // events are ignored so an active sign-in/sign-up pipeline is never raced or cancelled; the
    // operation's own outcome (checked against the live Firebase user) decides the session then.
    viewModelScope.launch {
      repository.authUid.collect { uid ->
        if (uid == null && mutableState.value.session !is SessionOutcome.SignedOut) {
          mutableState.value = mutableState.value.copy(session = SessionOutcome.SignedOut)
        }
      }
    }
    retry()
  }

  fun clearError() {
    if (!mutableState.value.busy) mutableState.value = mutableState.value.copy(error = null)
  }

  fun retry() = operate {
    if (!mutableState.value.initialized) {
      val completed = onboarding.isCompleted.first()
      mutableState.value = mutableState.value.copy(onboardingCompleted = completed)
    }
    repository.restore()
  }

  fun finishOnboarding() = operate {
    onboarding.markCompleted()
    mutableState.value = mutableState.value.copy(onboardingCompleted = true)
    mutableState.value.session
  }

  fun signIn(email: String, password: String) = operate {
    repository.signIn(email.trim(), password)
  }

  fun register(data: RegistrationData) = operate {
    val profile = RegistrationProfile(
      role = if (data.role == AuthRole.USER) AccountRole.USER else AccountRole.PARTNER,
      fullName = data.fullName.trim(),
      phone = data.phone.trim(),
      businessName = data.businessName?.trim(),
      businessCategory = data.businessCategory?.trim()
    )
    if (mutableState.value.session is SessionOutcome.NeedsProfile) {
      repository.completeProfile(profile)
    } else {
      repository.register(data.email.trim(), data.password, profile)
    }
  }

  fun signOut() {
    if (mutableState.value.busy) return
    repository.signOut()
    mutableState.value = mutableState.value.copy(
      initialized = true, session = SessionOutcome.SignedOut, error = null
    )
  }

  private fun operate(operation: suspend () -> SessionOutcome) {
    if (mutableState.value.busy) return
    mutableState.value = mutableState.value.copy(busy = true, error = null)
    viewModelScope.launch {
      try {
        val outcome = operation()
        mutableState.value = mutableState.value.copy(
          initialized = true, session = outcome, busy = false
        )
      } catch (cancelled: CancellationException) {
        throw cancelled
      } catch (failure: AuthFailure) {
        mutableState.value = mutableState.value.copy(
          initialized = true,
          session = failure.recovery ?: mutableState.value.session,
          busy = false,
          error = failure.message
        )
      } catch (_: Exception) {
        mutableState.value = mutableState.value.copy(
          busy = false,
          error = "Layanan akun belum dapat dihubungi. Silakan coba lagi."
        )
      }
    }
  }
}

class AuthViewModelFactory(context: Context) : ViewModelProvider.Factory {
  private val applicationContext = context.applicationContext

  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    require(modelClass.isAssignableFrom(AuthViewModel::class.java))
    val backend = BackendClient.default
    @Suppress("UNCHECKED_CAST")
    return AuthViewModel(
      AuthRepository(
        FirebaseAuth.getInstance(),
        AccountApi(backend),
        PendingRegistrationStore(applicationContext)
      ),
      OnboardingPreferences(applicationContext)
    ) as T
  }
}
