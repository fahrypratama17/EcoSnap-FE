package com.example.ecosnap.features.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HistoryType(val wire: String?, val label: String) {
  All(null, "Semua"), Deposit("deposit", "Setoran"), Redemption("redemption", "Penukaran")
}

data class HistoryFilter(
  val type: HistoryType = HistoryType.All,
  val fromDate: LocalDate? = null,
  val toDate: LocalDate? = null
)

data class PointHistoryUiState(
  val filter: HistoryFilter = HistoryFilter(),
  val items: List<PointEntry> = emptyList(),
  val nextCursor: String? = null,
  val isLoading: Boolean = true,
  val isLoadingMore: Boolean = false,
  val error: String? = null,
  val moreError: String? = null,
  val loaded: Boolean = false
)

class PointHistoryViewModel(private val repository: WalletRepository) : ViewModel() {
  private val _state = MutableStateFlow(PointHistoryUiState())
  val state: StateFlow<PointHistoryUiState> = _state.asStateFlow()

  private var generation = 0
  private var job: Job? = null

  /** Always reloads from the first page: stale cursors are never reused after a resume. */
  fun onResume() = reload()

  fun retry() = reload()

  fun setType(type: HistoryType) {
    if (_state.value.filter.type == type) return
    applyFilter(_state.value.filter.copy(type = type))
  }

  fun setRange(from: LocalDate?, to: LocalDate?) {
    applyFilter(_state.value.filter.copy(fromDate = from, toDate = to))
  }

  private fun applyFilter(filter: HistoryFilter) {
    // A cursor belongs to one query; drop list and cursor together.
    _state.update { it.copy(filter = filter, items = emptyList(), nextCursor = null, loaded = false, moreError = null) }
    reload()
  }

  private fun reload() {
    val token = ++generation
    job?.cancel()
    val filter = _state.value.filter
    _state.update { it.copy(isLoading = true, isLoadingMore = false, error = null, moreError = null) }
    job = viewModelScope.launch {
      val result = attempt { fetch(filter, null) }
      if (token != generation) return@launch
      result.onSuccess { page ->
        _state.update {
          it.copy(items = page.items.distinctBy { e -> e.id }, nextCursor = page.nextCursor, isLoading = false, loaded = true)
        }
      }.onFailure { e ->
        _state.update { it.copy(isLoading = false, error = e.userMessage("Riwayat poin belum dapat dimuat.")) }
      }
    }
  }

  fun loadMore() {
    val s = _state.value
    val cursor = s.nextCursor ?: return
    if (s.isLoading || s.isLoadingMore) return
    val token = generation
    _state.update { it.copy(isLoadingMore = true, moreError = null) }
    job = viewModelScope.launch {
      val result = attempt { fetch(s.filter, cursor) }
      if (token != generation) return@launch
      result.onSuccess { page ->
        _state.update { cur ->
          val known = cur.items.mapTo(HashSet()) { it.id }
          cur.copy(
            items = cur.items + page.items.filter { known.add(it.id) },
            nextCursor = page.nextCursor,
            isLoadingMore = false
          )
        }
      }.onFailure { e ->
        _state.update { it.copy(isLoadingMore = false, moreError = e.userMessage("Gagal memuat lebih banyak.")) }
      }
    }
  }

  private suspend fun fetch(filter: HistoryFilter, cursor: String?): Page<PointEntry> = repository.history(
    type = filter.type.wire,
    from = filter.fromDate?.let(::startOfDayInstant),
    to = filter.toDate?.let(::endExclusiveInstant),
    cursor = cursor
  )
}

class PointHistoryViewModelFactory(
  private val repository: WalletRepository = WalletRepository()
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    require(modelClass.isAssignableFrom(PointHistoryViewModel::class.java))
    @Suppress("UNCHECKED_CAST")
    return PointHistoryViewModel(repository) as T
  }
}
