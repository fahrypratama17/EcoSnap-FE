package com.example.ecosnap.features.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ecosnap.core.network.BackendException
import com.example.ecosnap.features.articles.components.ArticleCategory
import com.example.ecosnap.features.articles.components.ArticleSummary
import com.example.ecosnap.features.articles.data.ArticleRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ArticleListUiState(
  val query: String = "",
  val category: ArticleCategory? = null,
  val items: List<ArticleSummary> = emptyList(),
  val nextCursor: String? = null,
  val isLoading: Boolean = true,
  val isLoadingMore: Boolean = false,
  val error: String? = null,
  val loadMoreError: String? = null
) {
  val hasMore: Boolean get() = nextCursor != null
}

class ArticleListViewModel(
  private val repository: ArticleRepository
) : ViewModel() {
  private val _state = MutableStateFlow(ArticleListUiState())
  val state: StateFlow<ArticleListUiState> = _state.asStateFlow()

  private var started = false
  private var generation = 0
  private var loadJob: Job? = null
  private var moreJob: Job? = null

  /** First call loads the list; later calls (ON_RESUME) refresh the first page in place. */
  fun onResume() {
    if (!started) {
      started = true
      reload(showLoading = true)
    } else {
      reload(showLoading = false)
    }
  }

  fun retry() = reload(showLoading = true)

  fun onQueryChange(value: String) {
    val limited = value.take(MAX_QUERY_LENGTH)
    if (limited == _state.value.query) return
    _state.update { it.copy(query = limited) }
    reload(showLoading = true, debounce = true)
  }

  fun onCategoryChange(category: ArticleCategory?) {
    if (category == _state.value.category) return
    _state.update { it.copy(category = category) }
    reload(showLoading = true)
  }

  fun loadMore() {
    val current = _state.value
    val cursor = current.nextCursor ?: return
    if (current.isLoading || current.isLoadingMore) return
    val token = generation
    _state.update { it.copy(isLoadingMore = true, loadMoreError = null) }
    moreJob = viewModelScope.launch {
      try {
        val page = repository.list(current.query, current.category, cursor)
        if (token != generation) return@launch
        _state.update {
          val known = it.items.mapTo(HashSet()) { item -> item.id }
          it.copy(
            items = it.items + page.items.filter { item -> item.id !in known },
            nextCursor = page.nextCursor,
            isLoadingMore = false
          )
        }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        if (token != generation) return@launch
        _state.update { it.copy(isLoadingMore = false, loadMoreError = messageOf(e)) }
      }
    }
  }

  private fun reload(showLoading: Boolean, debounce: Boolean = false) {
    val token = ++generation
    loadJob?.cancel()
    moreJob?.cancel()
    _state.update {
      if (showLoading) {
        it.copy(
          items = emptyList(), nextCursor = null, isLoading = true,
          isLoadingMore = false, error = null, loadMoreError = null
        )
      } else {
        it.copy(isLoadingMore = false, loadMoreError = null)
      }
    }
    loadJob = viewModelScope.launch {
      try {
        if (debounce) delay(SEARCH_DEBOUNCE_MS)
        val snapshot = _state.value
        val page = repository.list(snapshot.query, snapshot.category, cursor = null)
        if (token != generation) return@launch
        _state.update {
          it.copy(
            items = page.items,
            nextCursor = page.nextCursor,
            isLoading = false,
            error = null
          )
        }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        if (token != generation) return@launch
        _state.update {
          // A silent refresh failure keeps the already displayed page.
          if (showLoading || it.items.isEmpty()) {
            it.copy(items = emptyList(), nextCursor = null, isLoading = false, error = messageOf(e))
          } else {
            it.copy(isLoading = false)
          }
        }
      }
    }
  }

  private fun messageOf(e: Exception): String =
    (e as? BackendException)?.message?.takeIf { it.isNotBlank() }
      ?: "Artikel belum dapat dimuat. Silakan coba lagi."

  private companion object {
    const val MAX_QUERY_LENGTH = 100
    const val SEARCH_DEBOUNCE_MS = 350L
  }
}

class ArticleListViewModelFactory(
  private val repository: ArticleRepository = ArticleRepository()
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    require(modelClass.isAssignableFrom(ArticleListViewModel::class.java))
    @Suppress("UNCHECKED_CAST")
    return ArticleListViewModel(repository) as T
  }
}
