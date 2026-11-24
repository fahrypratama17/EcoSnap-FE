package com.example.ecosnap.features.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ecosnap.core.auth.AccountRole
import com.example.ecosnap.features.onboarding.data.OnboardingPreferences
import com.example.ecosnap.features.profile.data.PartnerStats
import com.example.ecosnap.features.profile.data.ProfileData
import com.example.ecosnap.features.profile.data.ProfileRepository
import com.example.ecosnap.features.profile.data.UserStats
import com.example.ecosnap.features.profile.data.profileMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ProfileStats {
  data class User(val value: UserStats) : ProfileStats
  data class Partner(val value: PartnerStats) : ProfileStats
}

data class ProfileUiState(
  val profile: ProfileData? = null,
  val profileLoading: Boolean = true,
  val profileError: String? = null,
  val stats: ProfileStats? = null,
  val statsFailed: Boolean = false
)

/** One instance per account UID (route keys it). [onResume] refreshes profile and stats from the server. */
class ProfileViewModel(
  private val role: AccountRole,
  private val repository: ProfileRepository,
  private val localOnboardingCompleted: suspend () -> Boolean
) : ViewModel() {
  private val _state = MutableStateFlow(ProfileUiState())
  val state: StateFlow<ProfileUiState> = _state.asStateFlow()

  private var generation = 0
  private var job: Job? = null
  private var onboardingAttempted = false

  fun onResume() = load()

  fun retry() = load()

  private fun load() {
    val token = ++generation
    job?.cancel()
    _state.update { it.copy(profileLoading = it.profile == null, profileError = null) }
    job = viewModelScope.launch {
      try {
        coroutineScope {
          val stats = async { loadStats() }
          try {
            val profile = repository.profile()
            if (token != generation) return@coroutineScope
            _state.update { it.copy(profile = profile, profileLoading = false, profileError = null) }
            syncOnboarding(profile)
          } catch (e: CancellationException) {
            throw e
          } catch (e: Exception) {
            if (token != generation) return@coroutineScope
            _state.update {
              it.copy(
                profileLoading = false,
                profileError = e.profileMessage("Profil belum dapat dimuat. Silakan coba lagi.")
              )
            }
          }
          val result = stats.await()
          if (token != generation) return@coroutineScope
          _state.update { it.copy(stats = result, statsFailed = result == null || result.isIncomplete()) }
        }
      } catch (e: CancellationException) {
        throw e
      }
    }
  }

  private suspend fun loadStats(): ProfileStats? = try {
    when (role) {
      AccountRole.USER -> ProfileStats.User(repository.userStats())
      AccountRole.PARTNER -> ProfileStats.Partner(repository.partnerStats())
    }
  } catch (e: CancellationException) {
    throw e
  } catch (_: Exception) {
    null
  }

  private fun ProfileStats.isIncomplete() =
    this is ProfileStats.User && (value.depositsClaimed == null || value.badgesEarned == null)

  private suspend fun syncOnboarding(profile: ProfileData) {
    if (onboardingAttempted || profile.settings.onboardingCompleted) return
    onboardingAttempted = true
    if (localOnboardingCompleted()) repository.markOnboardingCompleted()
  }
}

class ProfileViewModelFactory(
  context: Context,
  private val role: AccountRole,
  private val repository: ProfileRepository = ProfileRepository()
) : ViewModelProvider.Factory {
  private val appContext = context.applicationContext

  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    require(modelClass.isAssignableFrom(ProfileViewModel::class.java))
    @Suppress("UNCHECKED_CAST")
    return ProfileViewModel(role, repository) {
      try {
        OnboardingPreferences(appContext).isCompleted.first()
      } catch (e: CancellationException) {
        throw e
      } catch (_: Exception) {
        false
      }
    } as T
  }
}
