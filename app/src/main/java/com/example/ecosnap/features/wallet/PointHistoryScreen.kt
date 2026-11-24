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
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PointHistoryScreen(
  state: PointHistoryUiState,
  onBack: () -> Unit,
  onRetry: () -> Unit,
  onLoadMore: () -> Unit,
  onType: (HistoryType) -> Unit,
  onRange: (LocalDate?, LocalDate?) -> Unit,
  onDeposit: (String) -> Unit,
  onVoucher: (String) -> Unit
) {
  var picking by remember { mutableStateOf(false) }
  val listState = rememberLazyListState()

  // Fire when the last row becomes visible; one request at a time is enforced by the view model.
  val atEnd by remember {
    androidx.compose.runtime.derivedStateOf {
      val info = listState.layoutInfo
      val last = info.visibleItemsInfo.lastOrNull()?.index ?: -1
      info.totalItemsCount > 0 && last >= info.totalItemsCount - 2
    }
  }
  LaunchedEffect(atEnd, state.nextCursor, state.items.size) {
    if (atEnd && state.moreError == null) onLoadMore()
  }

  Column(Modifier.fillMaxSize()) {
    WalletTopBar("Riwayat Poin", onBack)
    Row(
      Modifier.fillMaxWidth().padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      HistoryType.entries.forEach { type ->
        FilterChip(
          selected = state.filter.type == type,
          onClick = { onType(type) },
          label = { Text(type.label) }
        )
      }
    }
    Row(
      Modifier.fillMaxWidth().padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      val from = state.filter.fromDate
      val to = state.filter.toDate
      TextButton(onClick = { picking = true }) {
        Text(
          when {
            from == null -> "Pilih rentang tanggal"
            to == null || to == from -> formatDay(from)
            else -> "${formatDay(from)} – ${formatDay(to)}"
          }
        )
      }
      if (from != null) TextButton(onClick = { onRange(null, null) }) { Text("Hapus") }
    }

    LazyColumn(
      state = listState,
      modifier = Modifier.weight(1f),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      when {
        state.isLoading && !state.loaded -> item { LoadingBox() }
        state.error != null && !state.loaded -> item { ErrorBox(state.error, onRetry) }
        state.loaded && state.items.isEmpty() -> item {
          EmptyBox(
            if (state.filter.type != HistoryType.All || state.filter.fromDate != null)
              "Tidak ada riwayat untuk filter ini."
            else "Belum ada riwayat poin."
          )
        }
        else -> {
          state.error?.let { error -> item { ErrorBox(error, onRetry) } }
          items(state.items, key = { it.id }) { entry -> EntryRow(entry, onDeposit, onVoucher) }
          if (state.isLoadingMore) item { LoadingBox() }
          state.moreError?.let { error -> item { ErrorBox(error, onLoadMore) } }
        }
      }
    }
  }

  if (picking) {
    val pickerState = rememberDateRangePickerState(
      initialSelectedStartDateMillis = state.filter.fromDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
      initialSelectedEndDateMillis = state.filter.toDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
    )
    DatePickerDialog(
      onDismissRequest = { picking = false },
      confirmButton = {
        TextButton(
          enabled = pickerState.selectedStartDateMillis != null,
          onClick = {
            val start = pickerState.selectedStartDateMillis?.let(::pickerMillisToDate)
            val end = pickerState.selectedEndDateMillis?.let(::pickerMillisToDate) ?: start
            picking = false
            onRange(start, end)
          }
        ) { Text("Terapkan") }
      },
      dismissButton = { TextButton(onClick = { picking = false }) { Text("Batal") } }
    ) {
      DateRangePicker(state = pickerState, modifier = Modifier.height(480.dp))
    }
  }
}

@Composable
private fun EntryRow(entry: PointEntry, onDeposit: (String) -> Unit, onVoucher: (String) -> Unit) {
  val source = entry.source
  val (title, subtitle) = when (source) {
    is PointSource.Deposit -> "Setoran di ${source.partnerName.ifBlank { "mitra" }}" to "${source.weightKg} kg"
    is PointSource.Redemption -> "Penukaran ${source.rewardName.ifBlank { "reward" }}" to "Voucher simulasi"
    is PointSource.Other -> "Transaksi poin (${source.source})" to ""
  }
  val onClick: (() -> Unit)? = when (source) {
    is PointSource.Deposit -> ({ onDeposit(source.depositId) })
    is PointSource.Redemption -> ({ onVoucher(source.voucherId) })
    is PointSource.Other -> null
  }
  EcoCard(Modifier.fillMaxWidth().let { if (onClick != null) it.clickable(onClick = onClick) else it }) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      Column(Modifier.weight(1f)) {
        Text(title, fontWeight = FontWeight.SemiBold, color = EcoColor.Green900)
        if (subtitle.isNotEmpty()) Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(formatTimestamp(entry.createdAt), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (onClick != null) {
          Text(
            if (source is PointSource.Deposit) "Lihat detail setoran" else "Lihat voucher",
            fontSize = 12.sp,
            color = EcoColor.TextBrand
          )
        }
      }
      Spacer(Modifier.width(8.dp))
      Text(
        formatSigned(entry.amount),
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = if (entry.amount < 0) EcoColor.TextError else EcoColor.TextBrand
      )
    }
  }
}
