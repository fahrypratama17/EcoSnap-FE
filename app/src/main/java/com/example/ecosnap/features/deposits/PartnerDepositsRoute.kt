package com.example.ecosnap.features.deposits

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.theme.EcoColor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import com.example.ecosnap.core.designsystem.components.MitraCard
import com.example.ecosnap.core.designsystem.components.MitraOutlinedButton
import com.example.ecosnap.core.designsystem.components.MitraPageTitle
import com.example.ecosnap.core.designsystem.components.MitraPill
import com.example.ecosnap.core.designsystem.components.MitraInfoCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.QrCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PartnerDepositsUiState(
  val status: String? = null,
  val items: List<DepositSummary> = emptyList(),
  val nextCursor: String? = null,
  val loading: Boolean = true,
  val loadingMore: Boolean = false,
  val error: String? = null,
  val loadMoreError: String? = null,
)

class PartnerDepositsViewModel(private val repository: DepositRepository) : ViewModel() {
  private val _state = MutableStateFlow(PartnerDepositsUiState())
  val state: StateFlow<PartnerDepositsUiState> = _state.asStateFlow()
  private var started = false
  private var generation = 0
  private var job: Job? = null

  /** First resume loads; later resumes refresh the first page in place. */
  fun onResume() {
    if (!started) {
      started = true
      reload(showLoading = true)
    } else {
      reload(showLoading = false)
    }
  }

  fun retry() = reload(showLoading = true)

  fun selectStatus(status: String?) {
    if (status == _state.value.status) return
    _state.update { it.copy(status = status) }
    reload(showLoading = true)
  }

  private fun reload(showLoading: Boolean) {
    val token = ++generation
    job?.cancel()
    if (showLoading) {
      _state.update {
        it.copy(items = emptyList(), nextCursor = null, loading = true, loadingMore = false, error = null, loadMoreError = null)
      }
    } else {
      _state.update { it.copy(loadingMore = false, loadMoreError = null) }
    }
    val status = _state.value.status
    job = viewModelScope.launch {
      try {
        val page = repository.partnerDeposits(status, null)
        if (token != generation) return@launch
        _state.update { it.copy(items = page.items, nextCursor = page.nextCursor, loading = false, error = null) }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        if (token != generation) return@launch
        _state.update {
          if (showLoading || it.items.isEmpty()) it.copy(items = emptyList(), nextCursor = null, loading = false, error = DepositText.message(e))
          else it.copy(loading = false)
        }
      }
    }
  }

  fun loadMore() {
    val current = _state.value
    val cursor = current.nextCursor ?: return
    if (current.loading || current.loadingMore) return
    val token = generation
    _state.update { it.copy(loadingMore = true, loadMoreError = null) }
    job = viewModelScope.launch {
      try {
        val page = repository.partnerDeposits(current.status, cursor)
        if (token != generation) return@launch
        _state.update {
          val known = it.items.mapTo(HashSet()) { item -> item.id }
          it.copy(
            items = it.items + page.items.filter { item -> item.id !in known },
            nextCursor = page.nextCursor,
            loadingMore = false,
          )
        }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        if (token != generation) return@launch
        _state.update { it.copy(loadingMore = false, loadMoreError = DepositText.message(e)) }
      }
    }
  }

  class Factory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = PartnerDepositsViewModel(DepositRepository()) as T
  }
}

private val statusFilters = listOf<Pair<String?, String>>(
  null to "Semua",
  "created" to "Dibuat",
  "qr_issued" to "QR diterbitkan",
  "claimed" to "Diklaim",
)

@Composable
fun PartnerDepositsRoute(accountUid: String, onCreate: () -> Unit, onOpen: (String) -> Unit) {
  val vm: PartnerDepositsViewModel = viewModel(
    key = "partner-deposits-$accountUid",
    factory = PartnerDepositsViewModel.Factory(),
  )
  val state by vm.state.collectAsStateWithLifecycle()
  OnResume(vm) { vm.onResume() }

  LazyColumn(
    Modifier.fillMaxSize().background(Color.White),
    contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item { MitraPageTitle("Setoran & QR", "Buat setoran, terbitkan QR, dan pantau klaim pengguna") }
    item {
      EcoButton(
        text = "Buat Setoran Baru",
        onClick = onCreate,
        modifier = Modifier.fillMaxWidth(),
        containerColor = EcoColor.Green700,
        endIcon = { Icon(Lucide.ArrowRight, null, Modifier.size(20.dp)) },
      )
    }
    item {
      Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        statusFilters.forEach { (value, label) ->
          MitraPill(label, selected = state.status == value, onClick = { vm.selectStatus(value) })
        }
      }
    }
    when {
      state.loading -> item { MitraLoadingCard("Memuat setoran...") }
      state.error != null -> item { MitraErrorCard(state.error!!, vm::retry) }
      state.items.isEmpty() -> item {
        MitraInfoCard(
          "Belum ada setoran",
          if (state.status == null) "Setoran yang Anda buat akan muncul di sini."
          else "Tidak ada setoran dengan status ini.",
          icon = Lucide.QrCode,
        )
      }
      else -> {
        items(state.items, key = { it.id }) { item -> DepositRow(item) { onOpen(item.id) } }
        item {
          when {
            state.loadingMore -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
              CircularProgressIndicator(color = EcoColor.Green700)
            }
            state.loadMoreError != null -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Notice(state.loadMoreError!!, error = true)
              MitraOutlinedButton("Coba lagi", vm::loadMore, Modifier.fillMaxWidth())
            }
            state.nextCursor != null -> MitraOutlinedButton("Muat lebih banyak", vm::loadMore, Modifier.fillMaxWidth())
          }
        }
      }
    }
  }
}

@Composable
private fun DepositRow(item: DepositSummary, onClick: () -> Unit) {
  MitraCard(onClick = onClick) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
      Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
          "${item.totalPoints} poin",
          modifier = Modifier.weight(1f),
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = EcoColor.Green800,
        )
        Surface(color = EcoColor.Green100, shape = RoundedCornerShape(50)) {
          Text(
            DepositText.status(item.status),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = EcoColor.Green800,
          )
        }
      }
      Text(DepositText.kg(item.totalWeightKg), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = EcoColor.Green900)
      Text("Dibuat ${DepositText.dateTime(item.createdAt)}", fontSize = 11.sp, color = EcoColor.Green800)
      item.claimedAt?.let { Text("Diklaim ${DepositText.dateTime(it)}", fontSize = 11.sp, color = EcoColor.Green800) }
      if (item.usesDemoRates) Text("Tarif demo", fontSize = 11.sp, color = EcoColor.Green700)
    }
  }
}
