package com.example.ecosnap.features.articles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ArticleListRoute(
  accountUid: String,
  onBack: () -> Unit,
  onArticleClick: (String) -> Unit,
  onOpenScan: () -> Unit,
  onNotifications: () -> Unit,
  onProfile: () -> Unit
) {
  val viewModel: ArticleListViewModel = viewModel(
    key = "articles-list-$accountUid",
    factory = ArticleListViewModelFactory()
  )
  val state by viewModel.state.collectAsStateWithLifecycle()

  OnResumeEffect(viewModel) { viewModel.onResume() }

  ArticleListScreen(
    state = state,
    onQueryChange = viewModel::onQueryChange,
    onCategorySelected = viewModel::onCategoryChange,
    onRetry = viewModel::retry,
    onLoadMore = viewModel::loadMore,
    onBack = onBack,
    onArticleClick = onArticleClick,
    onOpenScan = onOpenScan,
    onNotifications = onNotifications,
    onProfile = onProfile
  )
}

@Composable
fun ArticleDetailRoute(
  accountUid: String,
  articleId: String,
  onBack: () -> Unit,
  onFinished: () -> Unit,
  onNotifications: () -> Unit,
  onProfile: () -> Unit
) {
  val viewModel: ArticleDetailViewModel = viewModel(
    key = "article-detail-$accountUid-$articleId",
    factory = ArticleDetailViewModelFactory(articleId)
  )
  val state by viewModel.state.collectAsStateWithLifecycle()

  OnResumeEffect(viewModel) { viewModel.refresh() }

  ArticleDetailScreen(
    state = state,
    onRetry = viewModel::retry,
    onBack = onBack,
    onFinished = onFinished,
    onNotifications = onNotifications,
    onProfile = onProfile
  )
}

@Composable
private fun OnResumeEffect(owner: Any, action: () -> Unit) {
  val lifecycleOwner = LocalLifecycleOwner.current
  val currentAction by rememberUpdatedState(action)

  DisposableEffect(lifecycleOwner, owner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) currentAction()
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }
}
