package com.example.ecosnap.features.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/** User Home backed by one `GET /me/home`; reloads on every resume. State is keyed per account. */
@Composable
fun HomeRoute(
  accountUid: String,
  onNotification: () -> Unit,
  onProfile: () -> Unit,
  onRedeemPoints: () -> Unit,
  onScanQr: () -> Unit,
  onFindPartner: () -> Unit,
  onHistory: () -> Unit,
  onScanWaste: () -> Unit,
  onSeeAllMissions: () -> Unit,
  onSeeMap: () -> Unit,
  onPartnerClick: (String) -> Unit,
  onNewsClick: (String) -> Unit,
  onSeeAllArticles: () -> Unit
) {
  val viewModel: HomeViewModel = viewModel(key = "home-$accountUid", factory = HomeViewModelFactory())
  val state by viewModel.state.collectAsStateWithLifecycle()

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }

  HomeScreen(
    uiState = state,
    onRetry = viewModel::retry,
    onNotification = onNotification,
    onProfile = onProfile,
    onRedeemPoints = onRedeemPoints,
    onScanQr = onScanQr,
    onFindPartner = onFindPartner,
    onHistory = onHistory,
    onScanWaste = onScanWaste,
    onSeeAllMissions = onSeeAllMissions,
    onSeeMap = onSeeMap,
    onPartnerClick = onPartnerClick,
    onNewsClick = onNewsClick,
    onSeeAllArticles = onSeeAllArticles
  )
}
