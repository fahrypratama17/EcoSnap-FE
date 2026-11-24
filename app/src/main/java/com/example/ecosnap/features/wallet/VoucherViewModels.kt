package com.example.ecosnap.features.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ecosnap.core.network.BackendException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VoucherListUiState(
  val status: VoucherStatus? = null,
  val items: List<Voucher> = emptyList(),
  val nextCursor: String? = null,
  val isLoading: Boolean = true,
  val isLoadingMore: Boolean = false,
  val error: String? = null,
  val moreError: String? = null,
  val loaded: Boolean = false
)

class VoucherListViewModel(private val repository: WalletRepository) : ViewModel() {
  private val _state = MutableStateFlow(VoucherListUiState())
  val state: StateFlow<VoucherListUiState> = _state.asStateFlow()

  private var generation = 0
  private var job: Job? = null

  fun onResume() = reload()

  fun retry() = reload()

  fun setStatus(status: VoucherStatus?) {
    if (_state.value.status == status && _state.value.loaded) return
    _state.update { it.copy(status = status, items = emptyList(), nextCursor = null, loaded = false, moreError = null) }
    reload()
  }

  private fun reload() {
    val token = ++generation
    job?.cancel()
    val status = _state.value.status
    _state.update { it.copy(isLoading = true, isLoadingMore = false, error = null, moreError = null) }
    job = viewModelScope.launch {
      val result = attempt { repository.vouchers(status, null) }
      if (token != generation) return@launch
      result.onSuccess { page ->
        _state.update {
          it.copy(items = page.items.distinctBy { v -> v.id }, nextCursor = page.nextCursor, isLoading = false, loaded = true)
        }
      }.onFailure { e ->
        _state.update { it.copy(isLoading = false, error = e.userMessage("Voucher belum dapat dimuat.")) }
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
      val result = attempt { repository.vouchers(s.status, cursor) }
      if (token != generation) return@launch
      result.onSuccess { page ->
        _state.update { cur ->
          val known = cur.items.mapTo(HashSet()) { it.id }
          cur.copy(items = cur.items + page.items.filter { known.add(it.id) }, nextCursor = page.nextCursor, isLoadingMore = false)
        }
      }.onFailure { e ->
        _state.update { it.copy(isLoadingMore = false, moreError = e.userMessage("Gagal memuat lebih banyak.")) }
      }
    }
  }
}

class VoucherListViewModelFactory(
  private val repository: WalletRepository = WalletRepository()
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    require(modelClass.isAssignableFrom(VoucherListViewModel::class.java))
    @Suppress("UNCHECKED_CAST")
    return VoucherListViewModel(repository) as T
  }
}

/** [voucher.code] lives only in this state; it is never persisted, logged or copied elsewhere. */
data class VoucherDetailUiState(
  val voucher: Voucher? = null,
  val isLoading: Boolean = true,
  val error: String? = null,
  val isUsing: Boolean = false,
  val message: String? = null
)

class VoucherDetailViewModel(
  private val voucherId: String,
  private val repository: WalletRepository
) : ViewModel() {
  private val _state = MutableStateFlow(VoucherDetailUiState())
  val state: StateFlow<VoucherDetailUiState> = _state.asStateFlow()

  private var generation = 0
  private var job: Job? = null

  fun onResume() = load()

  fun retry() = load()

  fun dismissMessage() = _state.update { it.copy(message = null) }

  private fun load() {
    val token = ++generation
    job?.cancel()
    _state.update { it.copy(isLoading = it.voucher == null, error = null) }
    job = viewModelScope.launch {
      val result = attempt { repository.voucher(voucherId) }
      if (token != generation) return@launch
      result.onSuccess { v -> _state.update { it.copy(voucher = v, isLoading = false, error = null) } }
        .onFailure { e ->
          _state.update { it.copy(isLoading = false, error = e.userMessage("Voucher belum dapat dimuat.")) }
        }
    }
  }

  /** Marks the voucher as used. The server treats repeats as the same result, so a retry after an unknown outcome is safe. */
  fun use() {
    val s = _state.value
    if (s.isUsing || s.voucher?.status != VoucherStatus.Issued) return
    _state.update { it.copy(isUsing = true, message = null) }
    viewModelScope.launch {
      val result = attempt { repository.useVoucher(voucherId) }
      result.onSuccess { v ->
        _state.update { it.copy(voucher = v, isUsing = false, message = "Voucher ditandai sudah dipakai.") }
      }.onFailure { e ->
        val expired = e is BackendException && e.code == "voucher_expired"
        _state.update {
          it.copy(
            isUsing = false,
            message = if (expired) "Voucher sudah kedaluwarsa." else e.userMessage("Voucher belum dapat ditandai. Coba lagi.")
          )
        }
        // Re-read the actual server state after any failure (unknown outcome, expiry, ...).
        load()
      }
    }
  }
}

class VoucherDetailViewModelFactory(
  private val voucherId: String,
  private val repository: WalletRepository = WalletRepository()
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    require(modelClass.isAssignableFrom(VoucherDetailViewModel::class.java))
    @Suppress("UNCHECKED_CAST")
    return VoucherDetailViewModel(voucherId, repository) as T
  }
}
