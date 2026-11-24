package com.example.ecosnap.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ecosnap.core.network.BackendException
import com.example.ecosnap.features.home.model.PartnerHomeData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PartnerHomeUiState(
  val data: PartnerHomeData? = null,
  val isLoading: Boolean = true,
  /** 403 `deposit_forbidden`: the business is not (yet) a location-backed partner. Not an error. */
  val depositForbidden: Boolean = false,
  val error: String? = null
)

class PartnerHomeViewModel(private val repository: HomeRepository) : ViewModel() {
  private val _state = MutableStateFlow(PartnerHomeUiState())
  val state: StateFlow<PartnerHomeUiState> = _state.asStateFlow()

  private var generation = 0
  private var job: Job? = null

  fun refresh() {
    val token = ++generation
    job?.cancel()
    _state.update { it.copy(isLoading = it.data == null && !it.depositForbidden, error = null) }
    job = viewModelScope.launch {
      try {
        val data = repository.partnerHome()
        if (token != generation) return@launch
        _state.value = PartnerHomeUiState(data = data, isLoading = false)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        if (token != generation) return@launch
        val forbidden = e is BackendException && e.status == 403 && e.code == "deposit_forbidden"
        _state.value = if (forbidden) {
          PartnerHomeUiState(isLoading = false, depositForbidden = true)
        } else {
          _state.value.copy(
            isLoading = false,
            error = e.userMessage("Ringkasan mitra belum dapat dimuat. Silakan coba lagi.")
          )
        }
      }
    }
  }
}

class PartnerHomeViewModelFactory(
  private val repository: HomeRepository = HomeRepository()
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    require(modelClass.isAssignableFrom(PartnerHomeViewModel::class.java))
    @Suppress("UNCHECKED_CAST")
    return PartnerHomeViewModel(repository) as T
  }
}
