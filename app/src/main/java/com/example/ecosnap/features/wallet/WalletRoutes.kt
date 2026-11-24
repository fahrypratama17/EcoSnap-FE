package com.example.ecosnap.features.wallet

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecosnap.core.network.PendingMutationStore

/** One store per process: the durable file must never be opened by two instances. */
private object WalletStores {
  @Volatile private var instance: PendingMutationStore? = null

  fun get(context: Context): PendingMutationStore =
    instance ?: synchronized(this) {
      instance ?: PendingMutationStore(context.applicationContext).also { instance = it }
    }
}

/** Balance, reward catalogue/detail and redemption. State and the redemption intent are keyed per account. */
@Composable
fun PointsRoute(
  accountUid: String,
  onVoucher: (String) -> Unit,
  onHistory: () -> Unit,
  onVouchers: () -> Unit,
  onEstimate: () -> Unit,
  onLeaderboard: () -> Unit
) {
  val context = LocalContext.current
  val store = remember(context) { WalletStores.get(context) }
  val viewModel: PointsViewModel = viewModel(
    key = "points-$accountUid",
    factory = PointsViewModelFactory(accountUid, store)
  )
  val state by viewModel.state.collectAsStateWithLifecycle()

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }
  BackHandler(enabled = state.selectedRewardId != null) { viewModel.closeReward() }

  PointsScreen(
    state = state,
    onRetry = viewModel::retry,
    onOpenReward = viewModel::openReward,
    onCloseReward = viewModel::closeReward,
    onRetryDetail = viewModel::retryDetail,
    onRedeem = viewModel::redeem,
    onRetryPending = viewModel::retryPending,
    onViewCompleted = { viewModel.acknowledgeCompleted(onVoucher) },
    onDismissCompleted = { viewModel.acknowledgeCompleted(null) },
    onDismissMessage = viewModel::dismissMessage,
    onHistory = onHistory,
    onVouchers = onVouchers,
    onEstimate = onEstimate,
    onLeaderboard = onLeaderboard
  )
}

@Composable
fun PointHistoryRoute(
  accountUid: String,
  onBack: () -> Unit,
  onDeposit: (String) -> Unit,
  onVoucher: (String) -> Unit
) {
  val viewModel: PointHistoryViewModel = viewModel(
    key = "point-history-$accountUid",
    factory = PointHistoryViewModelFactory()
  )
  val state by viewModel.state.collectAsStateWithLifecycle()

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }

  PointHistoryScreen(
    state = state,
    onBack = onBack,
    onRetry = viewModel::retry,
    onLoadMore = viewModel::loadMore,
    onType = viewModel::setType,
    onRange = viewModel::setRange,
    onDeposit = onDeposit,
    onVoucher = onVoucher
  )
}

@Composable
fun VoucherListRoute(
  accountUid: String,
  onBack: () -> Unit,
  onVoucher: (String) -> Unit
) {
  val viewModel: VoucherListViewModel = viewModel(
    key = "voucher-list-$accountUid",
    factory = VoucherListViewModelFactory()
  )
  val state by viewModel.state.collectAsStateWithLifecycle()

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }

  VoucherListScreen(
    state = state,
    onBack = onBack,
    onRetry = viewModel::retry,
    onLoadMore = viewModel::loadMore,
    onStatus = viewModel::setStatus,
    onVoucher = onVoucher
  )
}

@Composable
fun VoucherDetailRoute(
  accountUid: String,
  voucherId: String,
  onBack: () -> Unit
) {
  val viewModel: VoucherDetailViewModel = viewModel(
    key = "voucher-$accountUid-$voucherId",
    factory = VoucherDetailViewModelFactory(voucherId)
  )
  val state by viewModel.state.collectAsStateWithLifecycle()

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }

  VoucherDetailScreen(
    state = state,
    onBack = onBack,
    onRetry = viewModel::retry,
    onUse = viewModel::use,
    onDismissMessage = viewModel::dismissMessage
  )
}
