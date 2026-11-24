package com.example.ecosnap.features.games

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
import com.example.ecosnap.features.gamification.BadgeBoard
import com.example.ecosnap.features.gamification.GamificationRepository
import com.example.ecosnap.features.gamification.gamificationMessage
import com.example.ecosnap.features.home.model.MissionBoard
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Server-backed part of the Games home. Missions and badges load independently so one failure does
 * not hide the other; an unreadable section keeps its last good value and shows its own error.
 */
data class GamesUiState(
  val missions: MissionBoard? = null,
  val missionsLoadedAtElapsedMs: Long = 0L,
  val missionsError: String? = null,
  val badges: BadgeBoard? = null,
  val badgesError: String? = null,
  val isLoading: Boolean = true
)

class GamesViewModel(private val repository: GamificationRepository) : ViewModel() {
  private val _state = MutableStateFlow(GamesUiState())
  val state: StateFlow<GamesUiState> = _state.asStateFlow()

  private var generation = 0
  private var job: Job? = null

  fun refresh() = load()

  fun retry() {
    _state.update { it.copy(isLoading = true) }
    load()
  }

  private fun load() {
    val token = ++generation
    job?.cancel()
    job = viewModelScope.launch {
      try {
        coroutineScope {
          val missions = async { runCatchingServer { repository.missions() } }
          val badges = async { runCatchingServer { repository.badges() } }
          val missionResult = missions.await()
          val badgeResult = badges.await()
          if (token != generation) return@coroutineScope
          _state.update { current ->
            current.copy(
              missions = missionResult.getOrNull() ?: current.missions,
              missionsLoadedAtElapsedMs = if (missionResult.isSuccess) {
                SystemClock.elapsedRealtime()
              } else {
                current.missionsLoadedAtElapsedMs
              },
              missionsError = missionResult.exceptionOrNull()
                ?.gamificationMessage("Misi belum dapat dimuat."),
              badges = badgeResult.getOrNull() ?: current.badges,
              badgesError = badgeResult.exceptionOrNull()
                ?.gamificationMessage("Badge belum dapat dimuat."),
              isLoading = false
            )
          }
        }
      } catch (e: CancellationException) {
        throw e
      }
    }
  }

  /** Cancellation must propagate; every other failure becomes a per-section error. */
  private suspend fun <T> runCatchingServer(block: suspend () -> T): Result<T> = try {
    Result.success(block())
  } catch (e: CancellationException) {
    throw e
  } catch (e: Exception) {
    Result.failure(e)
  }
}

class GamesViewModelFactory(
  private val repository: GamificationRepository = GamificationRepository()
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    require(modelClass.isAssignableFrom(GamesViewModel::class.java))
    @Suppress("UNCHECKED_CAST")
    return GamesViewModel(repository) as T
  }
}

@Composable
fun GamesRoute(
  accountUid: String,
  onAllGames: () -> Unit,
  onGameDetail: (String) -> Unit,
  onViewMission: () -> Unit,
  onBadges: () -> Unit,
  onLeaderboard: () -> Unit,
  onNotifications: () -> Unit,
  onProfile: () -> Unit
) {
  val viewModel: GamesViewModel = viewModel(key = "games-$accountUid", factory = GamesViewModelFactory())
  val state by viewModel.state.collectAsStateWithLifecycle()

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }

  GamesHomeScreen(
    state = state,
    onRetry = viewModel::retry,
    onAllGames = onAllGames,
    onGameDetail = onGameDetail,
    onViewMission = onViewMission,
    onBadges = onBadges,
    onLeaderboard = onLeaderboard,
    onNotifications = onNotifications,
    onProfile = onProfile
  )
}
