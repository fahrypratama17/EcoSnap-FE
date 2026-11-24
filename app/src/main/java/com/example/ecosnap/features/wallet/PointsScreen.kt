package com.example.ecosnap.features.wallet

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Recycle
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
internal fun PointsScreen(
  state: PointsUiState,
  onRetry: () -> Unit,
  onOpenReward: (String) -> Unit,
  onCloseReward: () -> Unit,
  onRetryDetail: () -> Unit,
  onRedeem: (String) -> Unit,
  onRetryPending: () -> Unit,
  onViewCompleted: () -> Unit,
  onDismissCompleted: () -> Unit,
  onDismissMessage: () -> Unit,
  onHistory: () -> Unit,
  onVouchers: () -> Unit,
  onEstimate: () -> Unit,
  onLeaderboard: () -> Unit,
  onScanQr: () -> Unit
) {
  if (state.selectedRewardId != null) {
    RewardDetail(state, onCloseReward, onRetryDetail, onRedeem, onRetryPending, onViewCompleted, onDismissCompleted)
    return
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item { PointsHeading() }
    item { PointsBalanceHero(state, onRetry) }
    item { PointsSimulationBanner() }
    item {
      PointsQuickActions(
        primary = walletPrimaryActions(onScanQr, onEstimate),
        secondary = walletSecondaryActions(onHistory, onVouchers, onLeaderboard)
      )
    }
    state.message?.let { message ->
      item {
        EcoCard(Modifier.fillMaxWidth(), borderColor = EcoColor.Green600) {
          Text(message, fontSize = 13.sp)
          Spacer(Modifier.height(4.dp))
          TextButton(onClick = onDismissMessage) { Text("Tutup") }
        }
      }
    }
    state.pendingError?.let { error ->
      item { ErrorBox(error, onRetry) }
    }
    state.pending?.let { pending ->
      item { PendingBanner(state, pending, onRetryPending, onViewCompleted, onDismissCompleted) }
    }
    when {
      state.rewardsError != null && !state.rewardsLoaded -> item { ErrorBox(state.rewardsError, onRetry) }
      !state.rewardsLoaded -> item { LoadingBox() }
      state.rewards.isEmpty() -> item { EmptyBox("Belum ada reward yang tersedia.") }
      else -> {
        state.rewardsError?.let { error -> item { ErrorBox(error, onRetry) } }
        // The server has no reward categories; group by what the user can actually do right now.
        val soldOut = state.rewards.filter { !it.available || it.stock <= 0 }
        val inStock = state.rewards - soldOut.toSet()
        val affordable = inStock.filter { pointsShortfall(state.wallet, it.costPoints) == null }
        val short = inStock.filter { pointsShortfall(state.wallet, it.costPoints) != null }
        rewardGroup("ready", if (state.wallet == null) "Reward Tersedia" else "Bisa Ditukar Sekarang", affordable, state.wallet, onOpenReward)
        rewardGroup("short", "Perlu Poin Lebih", short, state.wallet, onOpenReward)
        rewardGroup("soldout", "Stok Habis", soldOut, state.wallet, onOpenReward)
      }
    }
  }
}

private fun LazyListScope.rewardGroup(
  id: String,
  title: String,
  rewards: List<Reward>,
  wallet: Wallet?,
  onOpenReward: (String) -> Unit
) {
  if (rewards.isEmpty()) return
  item(key = "header_$id") {
    Column {
      Spacer(Modifier.height(4.dp))
      PointsSectionTitle(title, rewards.size)
    }
  }
  rewards.chunked(2).forEach { row ->
    item(key = "row_${id}_${row.first().id}") {
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        row.forEach { reward ->
          RewardGridCard(reward, wallet, onClick = { onOpenReward(reward.id) }, modifier = Modifier.weight(1f))
        }
        if (row.size == 1) Box(Modifier.weight(1f))
      }
    }
  }
}

@Composable
private fun PendingBanner(
  state: PointsUiState,
  pending: PendingRedemption,
  onRetryPending: () -> Unit,
  onViewCompleted: () -> Unit,
  onDismissCompleted: () -> Unit
) {
  val name = state.rewards.firstOrNull { it.id == pending.rewardId }?.name ?: "reward"
  EcoCard(Modifier.fillMaxWidth(), borderColor = EcoColor.Green600, containerColor = EcoColor.GreenSoft) {
    if (pending.voucherId == null) {
      Text("Penukaran $name belum terkonfirmasi", fontWeight = FontWeight.SemiBold, color = EcoColor.Green900)
      Spacer(Modifier.height(4.dp))
      Text(
        "Hasilnya belum pasti. Coba lagi untuk memastikan; kunci penukaran yang sama dipakai sehingga poin tidak terpotong dua kali.",
        fontSize = 13.sp
      )
      Spacer(Modifier.height(8.dp))
      EcoButton(
        text = if (state.isRedeeming) "Memproses…" else "Coba lagi",
        onClick = onRetryPending,
        enabled = !state.isRedeeming,
        height = 40.dp
      )
    } else {
      Text("Penukaran $name berhasil", fontWeight = FontWeight.SemiBold, color = EcoColor.Green900)
      Spacer(Modifier.height(4.dp))
      Text("Voucher simulasi sudah terbit. Poin sudah dipotong sesuai data server.", fontSize = 13.sp)
      Spacer(Modifier.height(8.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        EcoButton(
          "Lihat voucher",
          onViewCompleted,
          Modifier.weight(1f),
          enabled = !state.isRedeeming,
          height = 40.dp
        )
        EcoButton(
          "Selesai",
          onDismissCompleted,
          Modifier.weight(1f),
          enabled = !state.isRedeeming,
          height = 40.dp,
          containerColor = EcoColor.Green600
        )
      }
    }
  }
}

@Composable
private fun RewardDetail(
  state: PointsUiState,
  onBack: () -> Unit,
  onRetryDetail: () -> Unit,
  onRedeem: (String) -> Unit,
  onRetryPending: () -> Unit,
  onViewCompleted: () -> Unit,
  onDismissCompleted: () -> Unit
) {
  var confirm by rememberSaveable { mutableStateOf(false) }
  val reward = state.selectedReward
  Column(Modifier.fillMaxSize()) {
    LazyColumn(
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.weight(1f)
    ) {
      item { WalletBackLink("Kembali ke Reward", onBack) }
      state.message?.let { message -> item { Text(message, color = EcoColor.TextError, fontSize = 13.sp) } }
      state.detailError?.let { error -> item { ErrorBox(error, onRetryDetail) } }
      if (reward == null) {
        if (state.detailLoading) item { LoadingBox() }
        return@LazyColumn
      }
      val soldOut = !reward.available || reward.stock <= 0
      val shortfall = pointsShortfall(state.wallet, reward.costPoints)
      item { WalletHeroTile(Lucide.Recycle) }
      item {
        Column(Modifier.fillMaxWidth()) {
          Text(
            reward.name,
            fontSize = 17.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = EcoColor.Green900
          )
          if (reward.description.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(reward.description, fontSize = 12.sp, lineHeight = 16.sp, color = EcoColor.Green800)
          }
          Spacer(Modifier.height(10.dp))
          WalletCostLine(reward.costPoints)
          Spacer(Modifier.height(8.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (reward.isDemo) Tag("Demo", EcoColor.GreenSoft, EcoColor.Green900)
            if (soldOut) {
              Tag("Stok habis", EcoColor.TextError, EcoColor.White)
            } else {
              Tag("Stok ${formatNumber(reward.stock)}", EcoColor.Green100, EcoColor.Green800)
            }
            if (shortfall != null) Tag("Poin tidak cukup", EcoColor.TextError, EcoColor.White)
          }
        }
      }
      item {
        WalletOutlinedCard {
          Text("Ketentuan Reward", fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold, color = EcoColor.Green900)
          Spacer(Modifier.height(8.dp))
          val paragraphs = reward.terms.lines().map { it.trim() }.filter { it.isNotEmpty() }
          paragraphs.forEach { paragraph ->
            Text(paragraph, fontSize = 11.sp, lineHeight = 16.sp, color = EcoColor.Green700)
            Spacer(Modifier.height(8.dp))
          }
          Text(
            "$VOUCHER_SIMULATION_NOTE Voucher berlaku 7 hari sejak penukaran.",
            fontSize = 11.sp,
            lineHeight = 16.sp,
            color = EcoColor.Green700
          )
        }
      }
      item {
        WalletOutlinedCard {
          Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Saldo EcoPoints", fontSize = 11.sp, lineHeight = 15.sp, fontWeight = FontWeight.Medium, color = EcoColor.Green800)
            Text(
              state.wallet?.let { "${formatNumber(it.balance)} poin" } ?: "-",
              fontSize = 12.sp,
              lineHeight = 16.sp,
              fontWeight = FontWeight.Bold,
              color = EcoColor.Green900
            )
          }
          if (shortfall != null) {
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Kurang", fontSize = 11.sp, lineHeight = 15.sp, fontWeight = FontWeight.Medium, color = EcoColor.TextError)
              Text(
                "${formatNumber(shortfall)} poin",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
                color = EcoColor.TextError
              )
            }
          }
        }
      }
      state.pending?.let { pending ->
        item { PendingBanner(state, pending, onRetryPending, onViewCompleted, onDismissCompleted) }
      }
    }
    val blocked = state.pending != null || !state.pendingLoaded
    val canRedeem = reward != null && reward.available && reward.stock > 0 && !insufficientFor(state, reward) &&
      !blocked && !state.isRedeeming
    WalletPillButton(
      text = when {
        state.isRedeeming -> "Memproses…"
        reward == null -> "Konfirmasi Penukaran"
        !reward.available || reward.stock <= 0L -> "Stok habis"
        insufficientFor(state, reward) -> "Poin tidak cukup"
        blocked -> "Selesaikan penukaran sebelumnya"
        else -> "Konfirmasi Penukaran"
      },
      enabled = canRedeem,
      onClick = { confirm = true },
      modifier = Modifier.padding(16.dp)
    )
  }
  if (confirm && reward != null) {
    AlertDialog(
      onDismissRequest = { confirm = false },
      title = { Text("Tukar reward?") },
      text = {
        Text(
          "${formatNumber(reward.costPoints)} poin akan dipotong untuk ${reward.name}. " +
            "Penukaran tidak dapat dibatalkan dan poin tidak dikembalikan."
        )
      },
      confirmButton = {
        TextButton(onClick = { confirm = false; onRedeem(reward.id) }) { Text("Tukar") }
      },
      dismissButton = { TextButton(onClick = { confirm = false }) { Text("Batal") } }
    )
  }
}

private fun insufficientFor(state: PointsUiState, reward: Reward): Boolean =
  pointsShortfall(state.wallet, reward.costPoints) != null
