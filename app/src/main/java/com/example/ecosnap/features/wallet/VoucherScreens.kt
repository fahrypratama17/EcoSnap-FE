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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Ticket
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor

/** Server derives `expired` from time; this keeps a stale `issued` row honest between refreshes. */
private fun Voucher.effectiveStatus(): VoucherStatus {
  if (status != VoucherStatus.Issued) return status
  val expiry = parseInstant(expiresAt) ?: return status
  return if (!java.time.Instant.now().isBefore(expiry)) VoucherStatus.Expired else status
}

@Composable
private fun StatusTag(status: VoucherStatus) {
  when (status) {
    VoucherStatus.Issued -> Tag(status.label, EcoColor.Green100, EcoColor.Green800)
    VoucherStatus.Used -> Tag(status.label, EcoColor.GreenSoft, EcoColor.Green900)
    VoucherStatus.Expired -> Tag(status.label, EcoColor.TextError, EcoColor.White)
  }
}

@Composable
internal fun VoucherListScreen(
  state: VoucherListUiState,
  onBack: () -> Unit,
  onRetry: () -> Unit,
  onLoadMore: () -> Unit,
  onStatus: (VoucherStatus?) -> Unit,
  onVoucher: (String) -> Unit
) {
  val listState = rememberLazyListState()
  val atEnd by remember {
    derivedStateOf {
      val info = listState.layoutInfo
      val last = info.visibleItemsInfo.lastOrNull()?.index ?: -1
      info.totalItemsCount > 0 && last >= info.totalItemsCount - 2
    }
  }
  LaunchedEffect(atEnd, state.nextCursor, state.items.size) {
    if (atEnd && state.moreError == null) onLoadMore()
  }

  Column(Modifier.fillMaxSize()) {
    WalletBackLink("Kembali", onBack, Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
    Text(
      "Voucher Saya",
      fontSize = 22.sp,
      lineHeight = 28.sp,
      fontWeight = FontWeight.Bold,
      color = EcoColor.Green900,
      modifier = Modifier.padding(horizontal = 16.dp)
    )
    Spacer(Modifier.height(8.dp))
    Row(
      Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      FilterChip(selected = state.status == null, onClick = { onStatus(null) }, label = { Text("Semua") })
      VoucherStatus.entries.forEach { status ->
        FilterChip(selected = state.status == status, onClick = { onStatus(status) }, label = { Text(status.label) })
      }
    }
    LazyColumn(
      state = listState,
      modifier = Modifier.weight(1f),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      item {
        Text(VOUCHER_SIMULATION_NOTE, fontSize = 11.sp, lineHeight = 16.sp, color = EcoColor.Green700)
      }
      when {
        state.isLoading && !state.loaded -> item { LoadingBox() }
        state.error != null && !state.loaded -> item { ErrorBox(state.error, onRetry) }
        state.loaded && state.items.isEmpty() -> item {
          EmptyBox(if (state.status == null) "Belum ada voucher. Tukar poin di halaman Poin & Reward." else "Tidak ada voucher dengan status ini.")
        }
        else -> {
          state.error?.let { error -> item { ErrorBox(error, onRetry) } }
          items(state.items, key = { it.id }) { voucher ->
            EcoCard(Modifier.fillMaxWidth().clickable { onVoucher(voucher.id) }) {
              Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                  voucher.rewardName,
                  fontWeight = FontWeight.SemiBold,
                  color = EcoColor.Green900,
                  modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                StatusTag(voucher.effectiveStatus())
              }
              Spacer(Modifier.height(4.dp))
              Text("Berlaku sampai ${formatTimestamp(voucher.expiresAt)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text("${formatNumber(voucher.costPoints)} poin", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
          if (state.isLoadingMore) item { LoadingBox() }
          state.moreError?.let { error -> item { ErrorBox(error, onLoadMore) } }
        }
      }
    }
  }
}

@Composable
internal fun VoucherDetailScreen(
  state: VoucherDetailUiState,
  onBack: () -> Unit,
  onRetry: () -> Unit,
  onUse: () -> Unit,
  onDismissMessage: () -> Unit
) {
  var confirm by rememberSaveable { mutableStateOf(false) }
  Column(Modifier.fillMaxSize()) {
    WalletBackLink("Kembali", onBack, Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
    val voucher = state.voucher
    Column(
      Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      state.message?.let { message ->
        EcoCard(Modifier.fillMaxWidth(), borderColor = EcoColor.Green600) {
          Text(message, fontSize = 13.sp)
          TextButton(onClick = onDismissMessage) { Text("Tutup") }
        }
      }
      when {
        voucher == null && state.isLoading -> LoadingBox()
        voucher == null -> ErrorBox(state.error ?: "Voucher belum dapat dimuat.", onRetry)
        else -> {
          state.error?.let { ErrorBox(it, onRetry) }
          val status = voucher.effectiveStatus()
          WalletHeroTile(Lucide.Ticket)
          Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
              Text(
                voucher.rewardName,
                fontSize = 17.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = EcoColor.Green900,
                modifier = Modifier.weight(1f)
              )
              Spacer(Modifier.width(8.dp))
              StatusTag(status)
            }
            Spacer(Modifier.height(10.dp))
            WalletCostLine(voucher.costPoints)
          }
          // The code is shown only here, only for a still-usable voucher.
          if (status == VoucherStatus.Issued) {
            voucher.code?.let { code ->
              WalletOutlinedCard {
                Text("Kode voucher", fontSize = 12.sp, color = EcoColor.Green700)
                Spacer(Modifier.height(4.dp))
                Text(
                  code,
                  fontSize = 24.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  color = EcoColor.Green800
                )
              }
            }
          }
          WalletOutlinedCard {
            Text("Detail Voucher", fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold, color = EcoColor.Green900)
            Spacer(Modifier.height(6.dp))
            InfoLine("Ditukar", formatTimestamp(voucher.createdAt))
            InfoLine("Berlaku sampai", formatTimestamp(voucher.expiresAt))
            voucher.usedAt?.let { InfoLine("Dipakai", formatTimestamp(it)) }
            Spacer(Modifier.height(8.dp))
            Text(VOUCHER_SIMULATION_NOTE, fontSize = 11.sp, lineHeight = 16.sp, color = EcoColor.Green700)
          }
        }
      }
    }
    val usable = state.voucher?.effectiveStatus() == VoucherStatus.Issued
    if (state.voucher != null) {
      WalletPillButton(
        text = when {
          state.isUsing -> "Memproses…"
          usable -> "Tandai sudah dipakai"
          else -> state.voucher.effectiveStatus().label
        },
        enabled = usable && !state.isUsing,
        onClick = { confirm = true },
        modifier = Modifier.padding(16.dp)
      )
    }
  }
  if (confirm) {
    AlertDialog(
      onDismissRequest = { confirm = false },
      title = { Text("Tandai sudah dipakai?") },
      text = { Text("Voucher yang sudah dipakai tidak dapat dikembalikan menjadi aktif dan poin tidak dikembalikan.") },
      confirmButton = { TextButton(onClick = { confirm = false; onUse() }) { Text("Tandai") } },
      dismissButton = { TextButton(onClick = { confirm = false }) { Text("Batal") } }
    )
  }
}
