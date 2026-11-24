package com.example.ecosnap.features.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.home.formatServerTime

/**
 * In-app inbox for the signed-in account. A notification is marked read on the server before any
 * target callback runs; targets the app cannot resolve for this role stay text-only.
 */
@Composable
fun NotificationsRoute(
  accountUid: String,
  isPartner: Boolean,
  onBack: () -> Unit,
  onDeposit: (String) -> Unit,
  onVoucher: ((String) -> Unit)?,
  onMissions: (() -> Unit)?
) {
  val access = NotificationAccess(
    isPartner = isPartner,
    canOpenVoucher = onVoucher != null,
    canOpenMissions = onMissions != null
  )
  val viewModel: NotificationsViewModel = viewModel(
    key = "notifications-$accountUid-$isPartner-${access.canOpenVoucher}-${access.canOpenMissions}",
    factory = NotificationsViewModelFactory(access)
  )
  val state by viewModel.state.collectAsStateWithLifecycle()
  val currentOnDeposit by rememberUpdatedState(onDeposit)
  val currentOnVoucher by rememberUpdatedState(onVoucher)
  val currentOnMissions by rememberUpdatedState(onMissions)

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }

  LaunchedEffect(viewModel) {
    viewModel.targets.collect { target ->
      when (target) {
        is NotificationTarget.Deposit -> currentOnDeposit(target.id)
        is NotificationTarget.Voucher -> currentOnVoucher?.invoke(target.id)
        NotificationTarget.Missions -> currentOnMissions?.invoke()
      }
    }
  }

  NotificationsScreen(
    state = state,
    access = access,
    onBack = onBack,
    onRetry = viewModel::retry,
    onUnreadOnlyChange = viewModel::setUnreadOnly,
    onLoadMore = viewModel::loadMore,
    onOpen = viewModel::open
  )
}

@Composable
fun NotificationsScreen(
  state: NotificationsUiState,
  access: NotificationAccess,
  onBack: () -> Unit,
  onRetry: () -> Unit,
  onUnreadOnlyChange: (Boolean) -> Unit,
  onLoadMore: () -> Unit,
  onOpen: (AppNotification) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onBack) {
        Icon(Lucide.ArrowLeft, contentDescription = "Kembali", tint = EcoColor.Green900)
      }
      Spacer(Modifier.width(4.dp))
      Column {
        Text(
          text = "Notifikasi",
          style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
          color = EcoColor.Green900
        )
        if (!state.isLoading && state.error == null) {
          Text(
            text = "${state.unreadCount} belum dibaca",
            fontSize = 12.sp,
            color = EcoColor.Green700
          )
        }
      }
    }

    Row(
      modifier = Modifier.padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      FilterChip(
        selected = !state.unreadOnly,
        onClick = { onUnreadOnlyChange(false) },
        label = { Text("Semua") }
      )
      FilterChip(
        selected = state.unreadOnly,
        onClick = { onUnreadOnlyChange(true) },
        label = { Text("Belum dibaca") }
      )
    }

    when {
      state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
        CircularProgressIndicator(color = EcoColor.Green700)
      }

      state.error != null -> Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = state.error,
            style = MaterialTheme.typography.bodyMedium,
            color = EcoColor.Green900,
            textAlign = TextAlign.Center
          )
          Spacer(Modifier.height(12.dp))
          EcoButton(text = "Coba lagi", onClick = onRetry, modifier = Modifier.fillMaxWidth())
        }
      }

      state.items.isEmpty() -> Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
        Text(
          text = if (state.unreadOnly) "Tidak ada notifikasi yang belum dibaca." else "Belum ada notifikasi.",
          style = MaterialTheme.typography.bodyMedium,
          color = EcoColor.Green700,
          textAlign = TextAlign.Center
        )
      }

      else -> LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        if (state.actionError != null) {
          item {
            Text(state.actionError, style = MaterialTheme.typography.bodyMedium, color = EcoColor.TextError)
          }
        }

        items(state.items, key = { it.id }) { item ->
          NotificationCard(
            item = item,
            opening = state.openingId == item.id,
            enabled = state.openingId == null,
            navigable = resolveNotificationTarget(item, access) != null,
            onClick = { onOpen(item) }
          )
        }

        if (state.loadMoreError != null) {
          item {
            Text(state.loadMoreError, style = MaterialTheme.typography.bodyMedium, color = EcoColor.TextError)
          }
        }

        if (state.hasMore) {
          item {
            EcoButton(
              text = if (state.isLoadingMore) "Memuat..." else "Muat lebih banyak",
              onClick = onLoadMore,
              enabled = !state.isLoadingMore,
              height = 44.dp,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }
    }
  }
}

@Composable
private fun NotificationCard(
  item: AppNotification,
  opening: Boolean,
  enabled: Boolean,
  navigable: Boolean,
  onClick: () -> Unit
) {
  // Every item is tappable while unread (the tap marks it read); read, text-only items are inert.
  val clickable = enabled && (item.isUnread || navigable)
  EcoCard(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(enabled = clickable, onClick = onClick),
    containerColor = if (item.isUnread) EcoColor.Green100 else Color.White
  ) {
    Row(verticalAlignment = Alignment.Top) {
      if (item.isUnread) {
        Box(
          modifier = Modifier
            .padding(top = 6.dp, end = 8.dp)
            .size(8.dp)
            .background(EcoColor.Green700, CircleShape)
        )
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = item.title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = if (item.isUnread) FontWeight.Bold else FontWeight.Normal,
          color = EcoColor.Green900
        )
        if (item.body.isNotEmpty()) {
          Spacer(Modifier.height(2.dp))
          Text(item.body, fontSize = 13.sp, color = EcoColor.Green800)
        }
        Spacer(Modifier.height(4.dp))
        val time = formatServerTime(item.createdAt, "d MMM yyyy HH.mm")
        val footer = listOfNotNull(
          time?.let { "$it WIB" },
          if (opening) "Menandai dibaca..." else if (navigable) "Ketuk untuk membuka" else null
        ).joinToString(" • ")
        if (footer.isNotEmpty()) {
          Text(footer, fontSize = 11.sp, color = EcoColor.Green700)
        }
      }
    }
  }
}
