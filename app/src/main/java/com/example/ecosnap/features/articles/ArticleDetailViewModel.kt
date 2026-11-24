package com.example.ecosnap.features.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ecosnap.core.network.BackendException
import com.example.ecosnap.features.articles.components.Article
import com.example.ecosnap.features.articles.data.ArticleRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ArticleDetailUiState(
  val article: Article? = null,
  val isLoading: Boolean = true,
  val error: String? = null,
  val notFound: Boolean = false
)

class ArticleDetailViewModel(
  private val articleId: String,
  private val repository: ArticleRepository
) : ViewModel() {
  private val _state = MutableStateFlow(ArticleDetailUiState())
  val state: StateFlow<ArticleDetailUiState> = _state.asStateFlow()

  private var job: Job? = null

  /** Initial load and ON_RESUME refresh; existing content stays visible while refreshing. */
  fun refresh() {
    load(showLoading = _state.value.article == null)
  }

  fun retry() = load(showLoading = true)

  private fun load(showLoading: Boolean) {
    job?.cancel()
    if (showLoading) _state.update { ArticleDetailUiState(isLoading = true) }
    job = viewModelScope.launch {
      try {
        val article = repository.detail(articleId)
        _state.value = ArticleDetailUiState(article = article, isLoading = false)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        val notFound = e is BackendException && e.status == 404
        if (notFound) {
          _state.value = ArticleDetailUiState(isLoading = false, notFound = true)
        } else if (_state.value.article == null) {
          _state.value = ArticleDetailUiState(
            isLoading = false,
            error = (e as? BackendException)?.message?.takeIf { it.isNotBlank() }
              ?: "Artikel belum dapat dimuat. Silakan coba lagi."
          )
        }
      }
    }
  }
}

class ArticleDetailViewModelFactory(
  private val articleId: String,
  private val repository: ArticleRepository = ArticleRepository()
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    require(modelClass.isAssignableFrom(ArticleDetailViewModel::class.java))
    @Suppress("UNCHECKED_CAST")
    return ArticleDetailViewModel(articleId, repository) as T
  }
}
