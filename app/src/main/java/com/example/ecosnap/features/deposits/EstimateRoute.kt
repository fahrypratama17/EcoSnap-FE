package com.example.ecosnap.features.deposits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstimateUiState(
  val loadingCategories: Boolean = true,
  val categories: List<WasteOption> = emptyList(),
  val categoriesError: String? = null,
  val rows: List<WeightRow> = listOf(WeightRow(0)),
  val validation: String? = null,
  val estimating: Boolean = false,
  val estimate: PointEstimate? = null,
  val error: String? = null,
)

class EstimateViewModel(private val repository: DepositRepository) : ViewModel() {
  private val _state = MutableStateFlow(EstimateUiState())
  val state: StateFlow<EstimateUiState> = _state.asStateFlow()
  private var nextKey = 1L
  private var categoriesJob: Job? = null
  private var estimateJob: Job? = null

  init {
    loadCategories()
  }

  fun loadCategories() {
    categoriesJob?.cancel()
    _state.update { it.copy(loadingCategories = true, categoriesError = null) }
    categoriesJob = viewModelScope.launch {
      try {
        val list = repository.categories()
        _state.update { it.copy(loadingCategories = false, categories = list) }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _state.update { it.copy(loadingCategories = false, categoriesError = DepositText.message(e)) }
      }
    }
  }

  fun change(key: Long, row: WeightRow) = edit { rows -> rows.map { if (it.key == key) row else it } }
  fun add() = edit { it + WeightRow(nextKey++) }
  fun remove(key: Long) = edit { rows -> rows.filterNot { it.key == key }.ifEmpty { listOf(WeightRow(nextKey++)) } }

  private fun edit(transform: (List<WeightRow>) -> List<WeightRow>) {
    estimateJob?.cancel()
    // Any edit makes the previous result stale; it must not be shown next to different input.
    _state.update { it.copy(rows = transform(it.rows), estimate = null, estimating = false, error = null, validation = null) }
  }

  fun calculate() {
    val snapshot = _state.value
    when (val result = WeightRows.validate(snapshot.rows, snapshot.categories)) {
      is RowsResult.Invalid -> _state.update { it.copy(validation = result.message ?: "Lengkapi kategori dan berat.") }
      is RowsResult.Valid -> {
        estimateJob?.cancel()
        _state.update { it.copy(estimating = true, error = null, validation = null, estimate = null) }
        estimateJob = viewModelScope.launch {
          try {
            val estimate = repository.estimate(result.items)
            _state.update { it.copy(estimating = false, estimate = estimate) }
          } catch (e: CancellationException) {
            throw e
          } catch (e: Exception) {
            _state.update { it.copy(estimating = false, error = DepositText.message(e)) }
          }
        }
      }
    }
  }

  class Factory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = EstimateViewModel(DepositRepository()) as T
  }
}

@Composable
fun EstimateRoute(accountUid: String, onBack: () -> Unit) {
  val vm: EstimateViewModel = viewModel(key = "estimate-$accountUid", factory = EstimateViewModel.Factory())
  val state by vm.state.collectAsStateWithLifecycle()

  Column(Modifier.fillMaxSize().background(Color.White)) {
    DepositTopBar("Estimasi Poin", onBack)
    when {
      state.loadingCategories -> LoadingBox("Memuat kategori...")
      state.categoriesError != null -> ErrorBox(state.categoriesError!!, vm::loadCategories, onBack)
      state.categories.isEmpty() -> ErrorBox("Server belum memiliki kategori sampah.", vm::loadCategories, onBack)
      else -> Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Notice("Ini hanya perkiraan dari tarif server saat ini. Poin sebenarnya dihitung ulang saat setoran dibuat dan baru masuk setelah QR diklaim.")
        DepositItemsEditor(
          rows = state.rows,
          categories = state.categories,
          enabled = !state.estimating,
          onChange = vm::change,
          onAdd = vm::add,
          onRemove = vm::remove,
        )
        state.validation?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = EcoColor.TextError) }
        EcoButton(
          text = if (state.estimating) "Menghitung..." else "Hitung Estimasi",
          onClick = vm::calculate,
          enabled = !state.estimating,
          modifier = Modifier.fillMaxWidth(),
        )
        state.error?.let { Notice(it, error = true) }
        state.estimate?.let { estimate ->
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              "Perkiraan ${estimate.estimatedPoints} poin",
              style = MaterialTheme.typography.titleLarge,
              color = EcoColor.Green900,
            )
            LabelValue("Total berat", DepositText.kg(estimate.totalWeightKg))
            LabelValue("Tarif per", DepositText.dateTime(estimate.asOf))
            if (estimate.usesDemoRates) Notice("Memakai tarif demo/simulasi, bukan nilai tukar nyata.")
            ItemLines(estimate.items)
          }
        }
      }
    }
  }
}
