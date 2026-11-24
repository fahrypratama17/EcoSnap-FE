package com.example.ecosnap.features.leaderboard

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecosnap.features.gamification.GamificationRepository
import com.example.ecosnap.features.gamification.LeaderboardBoard
import com.example.ecosnap.features.gamification.gamificationMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LeaderboardUiState(
  val board: LeaderboardBoard? = null,
  /** Monotonic time at which [board] arrived; used for the remaining-time label. */
  val loadedAtElapsedMs: Long = 0L,
  val isLoading: Boolean = true,
  val error: String? = null
)

class LeaderboardViewModel(private val repository: GamificationRepository) : ViewModel() {
  private val _state = MutableStateFlow(LeaderboardUiState())
  val state: StateFlow<LeaderboardUiState> = _state.asStateFlow()

  private var generation = 0
  private var job: Job? = null

  fun refresh() = load()

  fun retry() {
    _state.update { it.copy(isLoading = true, error = null) }
    load()
  }

  private fun load() {
    val token = ++generation
    job?.cancel()
    job = viewModelScope.launch {
      try {
        val board = repository.leaderboard()
        if (token != generation) return@launch
        _state.value = LeaderboardUiState(
          board = board,
          loadedAtElapsedMs = SystemClock.elapsedRealtime(),
          isLoading = false
        )
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        if (token != generation) return@launch
        val message = e.gamificationMessage("Leaderboard belum dapat dimuat. Silakan coba lagi.")
        _state.update { it.copy(isLoading = false, error = message) }
      }
    }
  }
}

class LeaderboardViewModelFactory(
  private val repository: GamificationRepository = GamificationRepository()
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    require(modelClass.isAssignableFrom(LeaderboardViewModel::class.java))
    @Suppress("UNCHECKED_CAST")
    return LeaderboardViewModel(repository) as T
  }
}

/** Weekly XP leaderboard from `GET /leaderboard`; the only leaderboard the backend supports. */
@Composable
fun LeaderboardRoute(
  accountUid: String,
  onBack: () -> Unit,
  onNotifications: () -> Unit,
  onProfile: () -> Unit
) {
  val viewModel: LeaderboardViewModel = viewModel(
    key = "leaderboard-$accountUid",
    factory = LeaderboardViewModelFactory()
  )
  val state by viewModel.state.collectAsStateWithLifecycle()

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }

  LeaderboardScreen(
    state = state,
    onBack = onBack,
    onRetry = viewModel::retry,
    onNotifications = onNotifications,
    onProfile = onProfile
  )
}
