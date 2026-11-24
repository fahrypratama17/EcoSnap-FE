package com.example.ecosnap.features.wallet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
  onLeaderboard: () -> Unit
) {
  if (state.selectedRewardId != null) {
    RewardDetail(state, onCloseReward, onRetryDetail, onRedeem, onRetryPending, onViewCompleted, onDismissCompleted)
    return
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Text("Poin & Reward", style = MaterialTheme.typography.titleLarge, color = EcoColor.Green900)
    }
    item { BalanceCard(state, onRetry) }
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          EcoButton("Riwayat Poin", onHistory, Modifier.weight(1f), height = 44.dp)
          EcoButton("Voucher Saya", onVouchers, Modifier.weight(1f), height = 44.dp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          EcoButton("Kalkulator", onEstimate, Modifier.weight(1f), height = 44.dp)
          EcoButton("XP Mingguan", onLeaderboard, Modifier.weight(1f), height = 44.dp)
        }
      }
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
    item {
      Text("Reward", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = EcoColor.Green900)
    }
    when {
      state.rewardsError != null && !state.rewardsLoaded -> item { ErrorBox(state.rewardsError, onRetry) }
      !state.rewardsLoaded -> item { LoadingBox() }
      state.rewards.isEmpty() -> item { EmptyBox("Belum ada reward yang tersedia.") }
      else -> {
        state.rewardsError?.let { error -> item { ErrorBox(error, onRetry) } }
        items(state.rewards, key = { it.id }) { reward ->
          RewardCard(reward, state.wallet, onClick = { onOpenReward(reward.id) })
        }
      }
    }
  }
}

@Composable
private fun BalanceCard(state: PointsUiState, onRetry: () -> Unit) {
  EcoCard(
    modifier = Modifier.fillMaxWidth(),
    containerColor = EcoColor.Green800,
    borderColor = EcoColor.Green800,
    contentPadding = PaddingValues(16.dp)
  ) {
    Text("Saldo EcoPoints", fontSize = 13.sp, color = EcoColor.Green200)
    val wallet = state.wallet
    when {
      wallet != null -> {
        Text(formatNumber(wallet.balance), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = EcoColor.White)
        Spacer(Modifier.height(4.dp))
        Text(
          "Didapat ${formatNumber(wallet.totalEarned)} • Terpakai ${formatNumber(wallet.totalSpent)}",
          fontSize = 12.sp,
          color = EcoColor.Green200
        )
        if (state.walletError != null) {
          Spacer(Modifier.height(4.dp))
          Text("Saldo mungkin belum terbaru: ${state.walletError}", fontSize = 12.sp, color = EcoColor.White)
        }
      }
      state.walletError != null -> {
        Text(state.walletError, fontSize = 13.sp, color = EcoColor.White)
        TextButton(onClick = onRetry) { Text("Coba lagi", color = EcoColor.White) }
      }
      else -> Text("Memuat…", fontSize = 16.sp, color = EcoColor.White)
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
private fun RewardCard(reward: Reward, wallet: Wallet?, onClick: () -> Unit) {
  val insufficient = wallet != null && wallet.balance < reward.costPoints
  EcoCard(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      Text(
        reward.name,
        fontWeight = FontWeight.SemiBold,
        color = EcoColor.Green900,
        modifier = Modifier.weight(1f)
      )
      Spacer(Modifier.width(8.dp))
      Text("${formatNumber(reward.costPoints)} poin", fontWeight = FontWeight.Bold, color = EcoColor.TextBrand)
    }
    if (reward.description.isNotBlank()) {
      Spacer(Modifier.height(4.dp))
      Text(reward.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
    }
    Spacer(Modifier.height(8.dp))
    RewardTags(reward, insufficient)
  }
}

@Composable
private fun RewardTags(reward: Reward, insufficient: Boolean) {
  Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
    if (reward.isDemo) Tag("Demo", EcoColor.GreenSoft, EcoColor.Green900)
    if (!reward.available || reward.stock == 0L) {
      Tag("Stok habis", EcoColor.TextError, EcoColor.White)
    } else {
      Tag("Stok ${formatNumber(reward.stock)}", EcoColor.Green100, EcoColor.Green800)
    }
    if (insufficient) Tag("Poin tidak cukup", EcoColor.TextError, EcoColor.White)
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
    WalletTopBar("Detail Reward", onBack)
    LazyColumn(
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.weight(1f)
    ) {
      state.message?.let { message -> item { Text(message, color = EcoColor.TextError, fontSize = 13.sp) } }
      state.detailError?.let { error -> item { ErrorBox(error, onRetryDetail) } }
      if (reward == null) {
        if (state.detailLoading) item { LoadingBox() }
        return@LazyColumn
      }
      val insufficient = state.wallet != null && state.wallet.balance < reward.costPoints
      item {
        EcoCard(Modifier.fillMaxWidth()) {
          Text(reward.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = EcoColor.Green900)
          Spacer(Modifier.height(6.dp))
          RewardTags(reward, insufficient)
          Spacer(Modifier.height(10.dp))
          InfoLine("Harga", "${formatNumber(reward.costPoints)} poin")
          InfoLine("Stok", if (reward.available && reward.stock > 0) formatNumber(reward.stock) else "Habis")
          state.wallet?.let { InfoLine("Saldo Anda", "${formatNumber(it.balance)} poin") }
        }
      }
      if (reward.description.isNotBlank()) {
        item {
          Column {
            Text("Deskripsi", fontWeight = FontWeight.SemiBold, color = EcoColor.Green900)
            Text(reward.description, fontSize = 14.sp)
          }
        }
      }
      if (reward.terms.isNotBlank()) {
        item {
          Column {
            Text("Syarat & ketentuan", fontWeight = FontWeight.SemiBold, color = EcoColor.Green900)
            Text(reward.terms, fontSize = 14.sp)
          }
        }
      }
      item {
        Text(
          "$VOUCHER_SIMULATION_NOTE Voucher berlaku 7 hari sejak penukaran.",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      state.pending?.let { pending ->
        item { PendingBanner(state, pending, onRetryPending, onViewCompleted, onDismissCompleted) }
      }
    }
    val blocked = state.pending != null || !state.pendingLoaded
    val canRedeem = reward != null && reward.available && reward.stock > 0 && !insufficientFor(state, reward) &&
      !blocked && !state.isRedeeming
    EcoButton(
      text = when {
        state.isRedeeming -> "Memproses…"
        reward == null -> "Tukar"
        !reward.available || reward.stock == 0L -> "Stok habis"
        insufficientFor(state, reward) -> "Poin tidak cukup"
        blocked -> "Selesaikan penukaran sebelumnya"
        else -> "Tukar ${formatNumber(reward.costPoints)} poin"
      },
      onClick = { confirm = true },
      enabled = canRedeem,
      modifier = Modifier.fillMaxWidth().padding(16.dp)
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
  state.wallet != null && state.wallet.balance < reward.costPoints
