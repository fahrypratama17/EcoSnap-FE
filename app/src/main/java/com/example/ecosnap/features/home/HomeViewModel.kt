package com.example.ecosnap.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ecosnap.features.home.model.HomeUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** One instance per account UID (the route keys it), so Home data never crosses accounts. */
class HomeViewModel(private val repository: HomeRepository) : ViewModel() {
  private val _state = MutableStateFlow(HomeUiState())
  val state: StateFlow<HomeUiState> = _state.asStateFlow()

  private var generation = 0
  private var job: Job? = null

  /** Called on every ON_RESUME: first call shows loading, later calls refresh in place. */
  fun onResume() = load()

  fun retry() = load()

  private fun load() {
    val token = ++generation
    job?.cancel()
    _state.update { it.copy(isLoading = it.data == null, error = null) }
    job = viewModelScope.launch {
      try {
        val data = repository.home()
        if (token != generation) return@launch
        _state.value = HomeUiState(data = data, isLoading = false, error = null)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        if (token != generation) return@launch
        _state.update {
          it.copy(isLoading = false, error = e.userMessage("Beranda belum dapat dimuat. Silakan coba lagi."))
        }
      }
    }
  }
}

class HomeViewModelFactory(
  private val repository: HomeRepository = HomeRepository()
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    require(modelClass.isAssignableFrom(HomeViewModel::class.java))
    @Suppress("UNCHECKED_CAST")
    return HomeViewModel(repository) as T
  }
}
