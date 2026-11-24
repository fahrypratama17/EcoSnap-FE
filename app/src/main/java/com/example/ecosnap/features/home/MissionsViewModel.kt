package com.example.ecosnap.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ecosnap.core.network.BackendException
import com.example.ecosnap.features.home.model.MissionBoard
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MissionsUiState(
  val board: MissionBoard? = null,
  val isLoading: Boolean = true,
  val error: String? = null,
  /** Mission whose claim request is in flight; at most one at a time. */
  val claimingId: String? = null,
  val claimMessage: String? = null
)

class MissionsViewModel(private val repository: HomeRepository) : ViewModel() {
  private val _state = MutableStateFlow(MissionsUiState())
  val state: StateFlow<MissionsUiState> = _state.asStateFlow()

  private var generation = 0
  private var loadJob: Job? = null

  /** Claim tokens kept after an ambiguous failure so the retry sends the same `period_start`. */
  private val pendingTokens = HashMap<String, String>()

  fun onResume() = load()

  fun retry() = load()

  fun dismissClaimMessage() = _state.update { it.copy(claimMessage = null) }

  fun claim(missionId: String) {
    val current = _state.value
    if (current.claimingId != null) return
    val board = current.board ?: return
    val mission = board.missions.firstOrNull { it.id == missionId && it.isClaimable } ?: return
    val token = pendingTokens.getOrPut(mission.id) { board.periodStart }
    _state.update { it.copy(claimingId = mission.id, claimMessage = null) }
    viewModelScope.launch {
      try {
        val result = repository.claim(mission.id, token)
        pendingTokens.remove(mission.id)
        _state.update { s ->
          s.copy(
            claimingId = null,
            claimMessage = "Klaim berhasil: +${result.xpAwarded} XP.",
            board = s.board?.let { b ->
              b.copy(
                weeklyXp = result.weeklyXp,
                totalXp = result.totalXp,
                missions = b.missions.map { if (it.id == result.mission.id) result.mission else it }
              )
            }
          )
        }
        load()
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        val expired = e is BackendException && e.status == 410
        if (!e.isAmbiguous()) pendingTokens.remove(mission.id)
        val message = when {
          expired -> "Periode misi sudah berganti. Daftar misi dimuat ulang."
          e.isAmbiguous() -> e.userMessage("Hasil klaim belum pasti. Coba lagi; klaim tidak akan dihitung dua kali.")
          else -> e.userMessage("Klaim misi gagal. Silakan coba lagi.")
        }
        _state.update { it.copy(claimingId = null, claimMessage = message) }
        if (!e.isAmbiguous()) load()
      }
    }
  }

  private fun load() {
    val token = ++generation
    loadJob?.cancel()
    _state.update { it.copy(isLoading = it.board == null, error = null) }
    loadJob = viewModelScope.launch {
      try {
        val board = repository.missions()
        if (token != generation) return@launch
        _state.update { it.copy(board = board, isLoading = false, error = null) }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        if (token != generation) return@launch
        _state.update {
          it.copy(isLoading = false, error = e.userMessage("Misi belum dapat dimuat. Silakan coba lagi."))
        }
      }
    }
  }
}

class MissionsViewModelFactory(
  private val repository: HomeRepository = HomeRepository()
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    require(modelClass.isAssignableFrom(MissionsViewModel::class.java))
    @Suppress("UNCHECKED_CAST")
    return MissionsViewModel(repository) as T
  }
}
