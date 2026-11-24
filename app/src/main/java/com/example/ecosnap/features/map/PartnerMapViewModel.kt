package com.example.ecosnap.features.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ecosnap.core.model.Partner
import com.example.ecosnap.core.model.WasteCategory
import com.example.ecosnap.core.network.BackendClient
import com.example.ecosnap.core.network.BackendException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PartnerMapUiState(
  val query: String = "",
  val categories: List<WasteCategory> = emptyList(),
  val categoriesError: String? = null,
  /** Slug of the selected category, or null for "Semua". */
  val selectedSlug: String? = null,
  val partners: List<Partner> = emptyList(),
  val isLoading: Boolean = true,
  val error: String? = null,
  val hasMore: Boolean = false,
  val isLoadingMore: Boolean = false,
  val loadMoreError: String? = null,
  val selectedPartnerId: String? = null,
  val favoriteIds: Set<String> = emptySet(),
  /** False until the first complete favorites read succeeds; toggling is disabled meanwhile. */
  val favoritesLoaded: Boolean = false,
  val favoritesError: String? = null,
  val favoritesInFlight: Set<String> = emptySet(),
  /** One-shot feedback (e.g. a failed favorite toggle); cleared by [PartnerMapViewModel.messageShown]. */
  val message: String? = null,
)

class PartnerMapViewModel(
  val accountUid: String,
  private val repository: PartnerRepository,
) : ViewModel() {
  private val _state = MutableStateFlow(PartnerMapUiState())
  val state: StateFlow<PartnerMapUiState> = _state.asStateFlow()

  private var searchJob: Job? = null
  private var loadMoreJob: Job? = null
  private var favoritesJob: Job? = null
  private var categoriesJob: Job? = null
  private var resumedOnce = false

  init {
    loadCategories()
    loadFavorites()
    reload(clear = true)
  }

  fun onQueryChange(value: String) {
    if (value.codePointCount(0, value.length) > PartnerRepository.MAX_QUERY_CODE_POINTS) return
    if (value == _state.value.query) return
    _state.update { it.copy(query = value) }
    reload(clear = true, debounce = true)
  }

  fun onCategorySelected(slug: String?) {
    if (slug == _state.value.selectedSlug) return
    _state.update { it.copy(selectedSlug = slug) }
    reload(clear = true)
  }

  fun onPartnerSelected(id: String) {
    _state.update { s -> if (s.partners.any { it.id == id }) s.copy(selectedPartnerId = id) else s }
  }

  fun retry() = reload(clear = true)

  fun retryCategories() = loadCategories()

  fun retryFavorites() = loadFavorites()

  fun messageShown() = _state.update { it.copy(message = null) }

  /** Call on every ON_RESUME. The first call is skipped because the constructor already loaded everything. */
  fun onResume() {
    if (!resumedOnce) {
      resumedOnce = true
      return
    }
    loadFavorites()
    if (_state.value.categories.isEmpty()) loadCategories()
    reload(clear = _state.value.partners.isEmpty())
  }

  private fun loadCategories() {
    categoriesJob?.cancel()
    categoriesJob = viewModelScope.launch {
      try {
        val categories = repository.categories()
        _state.update { s ->
          val selected = s.selectedSlug?.takeIf { slug -> categories.any { it.slug == slug } }
          s.copy(categories = categories, categoriesError = null, selectedSlug = selected)
        }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _state.update { it.copy(categoriesError = errorText(e)) }
      }
    }
  }

  /** Replaces the list with page one of the current filters. Cancels any stale search or pagination. */
  private fun reload(clear: Boolean, debounce: Boolean = false) {
    searchJob?.cancel()
    loadMoreJob?.cancel()
    _state.update {
      it.copy(
        isLoading = true,
        isLoadingMore = false,
        loadMoreError = null,
        error = null,
        partners = if (clear) emptyList() else it.partners,
        hasMore = if (clear) false else it.hasMore,
      )
    }
    searchJob = viewModelScope.launch {
      if (debounce) delay(SEARCH_DEBOUNCE_MS)
      val snapshot = _state.value
      try {
        val page = repository.list(snapshot.query, snapshot.selectedSlug, 0)
        _state.update { s ->
          s.copy(
            partners = page.items,
            hasMore = page.hasMore && page.nextOffset <= PartnerRepository.MAX_OFFSET,
            isLoading = false,
            error = null,
            selectedPartnerId = s.selectedPartnerId?.takeIf { id -> page.items.any { it.id == id } }
              ?: page.items.firstOrNull()?.id,
          )
        }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _state.update { it.copy(isLoading = false, error = errorText(e)) }
      }
    }
  }

  fun loadMore() {
    val s = _state.value
    if (!s.hasMore || s.isLoading || s.isLoadingMore) return
    val offset = s.partners.size
    if (offset > PartnerRepository.MAX_OFFSET) return
    _state.update { it.copy(isLoadingMore = true, loadMoreError = null) }
    loadMoreJob = viewModelScope.launch {
      try {
        val page = repository.list(s.query, s.selectedSlug, offset)
        _state.update { cur ->
          val known = cur.partners.mapTo(HashSet()) { it.id }
          val merged = cur.partners + page.items.filter { it.id !in known }
          cur.copy(
            partners = merged,
            hasMore = page.hasMore && page.items.isNotEmpty() && merged.size <= PartnerRepository.MAX_OFFSET,
            isLoadingMore = false,
          )
        }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _state.update { it.copy(isLoadingMore = false, loadMoreError = errorText(e)) }
      }
    }
  }

  private fun loadFavorites() {
    favoritesJob?.cancel()
    favoritesJob = viewModelScope.launch {
      try {
        val ids = repository.favoriteIds()
        _state.update { s ->
          // Optimistic values of in-flight toggles stay; the server will confirm or we roll back.
          val merged = ids.filterNot { it in s.favoritesInFlight }.toMutableSet()
          merged += s.favoriteIds.filter { it in s.favoritesInFlight }
          s.copy(favoriteIds = merged, favoritesLoaded = true, favoritesError = null)
        }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _state.update { it.copy(favoritesError = errorText(e)) }
      }
    }
  }

  fun toggleFavorite(partnerId: String) {
    val s = _state.value
    if (!s.favoritesLoaded || partnerId in s.favoritesInFlight) return
    val target = partnerId !in s.favoriteIds
    _state.update {
      it.copy(
        favoritesInFlight = it.favoritesInFlight + partnerId,
        favoriteIds = if (target) it.favoriteIds + partnerId else it.favoriteIds - partnerId,
      )
    }
    viewModelScope.launch {
      var refreshList = false
      var failure: String? = null
      try {
        repository.setFavorite(partnerId, target)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        refreshList = e is BackendException && e.status == 404
        failure = if (refreshList) "Mitra ini sudah tidak tersedia. Daftar diperbarui." else errorText(e)
      }
      _state.update {
        it.copy(
          favoritesInFlight = it.favoritesInFlight - partnerId,
          favoriteIds = if (failure == null) {
            it.favoriteIds
          } else if (target) {
            it.favoriteIds - partnerId
          } else {
            it.favoriteIds + partnerId
          },
          message = failure ?: it.message,
        )
      }
      // A favorites read that started before this write landed may be stale: re-read.
      loadFavorites()
      if (refreshList) reload(clear = false)
    }
  }

  private fun errorText(e: Exception): String =
    if (e is BackendException && e.message?.isNotBlank() == true) {
      e.message!!
    } else {
      "Tidak dapat terhubung ke server. Periksa koneksi dan coba lagi."
    }

  class Factory(
    private val accountUid: String,
    private val repository: PartnerRepository = PartnerRepository(BackendClient.default),
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
      PartnerMapViewModel(accountUid, repository) as T
  }

  private companion object {
    const val SEARCH_DEBOUNCE_MS = 350L
  }
}
