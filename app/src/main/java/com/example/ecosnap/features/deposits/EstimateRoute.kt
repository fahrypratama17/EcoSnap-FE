package com.example.ecosnap.features.deposits

import androidx.compose.runtime.remember
import com.example.ecosnap.core.designsystem.components.MitraBackLink
import com.example.ecosnap.core.designsystem.components.MitraCard
import com.example.ecosnap.core.designsystem.components.MitraInfoCard
import com.example.ecosnap.core.designsystem.components.MitraPageTitle
import com.example.ecosnap.core.designsystem.components.MitraWeightHero
import com.composables.icons.lucide.ArrowRight
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Gamepad2
import com.composables.icons.lucide.Lucide
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

/** JSON array of category_id + canonical weight_kg from the server-priced items; null if any weight is not canonical. */
private fun estimateDraftJson(items: List<DepositItem>): String? {
  if (items.isEmpty()) return null
  val array = com.google.gson.JsonArray()
  for (item in items) {
    val weight = WeightInput.canonical(item.weightKg) ?: return null
    array.add(com.google.gson.JsonObject().apply {
      addProperty("category_id", item.categoryId)
      addProperty("weight_kg", weight)
    })
  }
  return array.toString()
}

@Composable
fun EstimateRoute(accountUid: String, onBack: () -> Unit, onCreateDeposit: ((String) -> Unit)? = null) {
  val vm: EstimateViewModel = viewModel(key = "estimate-$accountUid", factory = EstimateViewModel.Factory())
  val state by vm.state.collectAsStateWithLifecycle()

  Column(Modifier.fillMaxSize().background(Color.White)) {
    Box(Modifier.padding(start = 12.dp, top = 8.dp)) { MitraBackLink(onBack) }
    when {
      state.loadingCategories -> LoadingBox("Memuat kategori...")
      state.categoriesError != null -> ErrorBox(state.categoriesError!!, vm::loadCategories, onBack)
      state.categories.isEmpty() -> ErrorBox("Server belum memiliki kategori sampah.", vm::loadCategories, onBack)
      else -> Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
      ) {
        MitraPageTitle("Kalkulator Poin", "Pilih kategori dan masukkan berat sampah yang ditimbang")
        Text(
          "Masukkan berat sampah",
          style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp, lineHeight = 30.sp),
          color = EcoColor.Green900,
        )
        MitraWeightHero(rememberTotalWeightText(state.rows))
        DepositItemsEditor(
          rows = state.rows,
          categories = state.categories,
          enabled = !state.estimating,
          onChange = vm::change,
          onAdd = vm::add,
          onRemove = vm::remove,
        )
        Text(
          "Maks. 1.000 kg total · 3 angka desimal · ${WeightInput.MAX_ITEMS} kategori",
          fontSize = 10.sp,
          color = EcoColor.Green700,
        )
        state.validation?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = EcoColor.TextError) }
        EcoButton(
          text = if (state.estimating) "Menghitung..." else "Hitung Estimasi",
          onClick = vm::calculate,
          enabled = !state.estimating,
          modifier = Modifier.fillMaxWidth(),
          containerColor = EcoColor.Green700,
        )
        state.error?.let { Notice(it, error = true) }
        state.estimate?.let { estimate ->
          EstimateResultCard(estimate)
          EstimateCreateButton(estimate, onCreateDeposit)
        }
        MitraInfoCard(
          "Hanya perkiraan",
          "Ini perkiraan dari tarif server saat ini. Poin sebenarnya dihitung ulang saat setoran dibuat dan baru masuk setelah QR diklaim.",
        )
      }
    }
  }
}

/**
 * Mitra calculator tab: the same server-priced estimate as [EstimateRoute], laid out like the
 * partner design (page title, weight hero, keypad). Points always come from the server estimate.
 */
@Composable
fun PartnerEstimateRoute(accountUid: String, onCreateDeposit: (String) -> Unit) {
  val vm: EstimateViewModel = viewModel(key = "estimate-$accountUid", factory = EstimateViewModel.Factory())
  val state by vm.state.collectAsStateWithLifecycle()

  Column(
    Modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState()).padding(20.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    MitraPageTitle("Input Berat Sampah", "Masukkan total berat sampah yang ditimbang")
    when {
      state.loadingCategories -> MitraLoadingCard("Memuat kategori...")
      state.categoriesError != null -> MitraErrorCard(state.categoriesError!!, vm::loadCategories)
      state.categories.isEmpty() -> MitraErrorCard("Server belum memiliki kategori sampah.", vm::loadCategories)
      else -> {
        Text(
          "Masukkan berat sampah",
          style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp, lineHeight = 30.sp),
          color = EcoColor.Green900,
        )
        MitraWeightHero(rememberTotalWeightText(state.rows))
        MitraWeightEditor(
          rows = state.rows,
          categories = state.categories,
          enabled = !state.estimating,
          onChange = vm::change,
          onAdd = vm::add,
          onRemove = vm::remove,
        )
        Text(
          "Maks. 1.000 kg total · 3 angka desimal · ${WeightInput.MAX_ITEMS} kategori",
          fontSize = 10.sp,
          color = EcoColor.Green700,
        )
        state.validation?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = EcoColor.TextError) }
        EcoButton(
          text = if (state.estimating) "Menghitung..." else "Hitung Estimasi",
          onClick = vm::calculate,
          enabled = !state.estimating,
          modifier = Modifier.fillMaxWidth(),
          containerColor = EcoColor.Green700,
        )
        state.error?.let { Notice(it, error = true) }
        state.estimate?.let { estimate ->
          EstimateResultCard(estimate)
          EstimateCreateButton(estimate, onCreateDeposit)
        }
        MitraInfoCard(
          "Hanya perkiraan",
          "Ini perkiraan dari tarif server saat ini. Poin sebenarnya dihitung ulang saat setoran dibuat dan baru masuk setelah QR diklaim.",
        )
      }
    }
  }
}

@Composable
private fun EstimateResultCard(estimate: PointEstimate) {
  MitraCard {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(Lucide.Gamepad2, null, Modifier.size(48.dp), tint = EcoColor.Green700)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("Perkiraan poin yang akan diberikan", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, lineHeight = 18.sp, color = EcoColor.Green900)
          Text("${estimate.estimatedPoints} poin", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = EcoColor.Green800)
          Text("dari ${DepositText.kg(estimate.totalWeightKg)}", fontSize = 10.sp, color = EcoColor.Green800)
        }
      }
      HorizontalDivider(color = EcoColor.Green100)
      LabelValue("Tarif per", DepositText.dateTime(estimate.asOf))
      if (estimate.usesDemoRates) Notice("Memakai tarif demo/simulasi, bukan nilai tukar nyata.")
      ItemLines(estimate.items)
    }
  }
}

@Composable
private fun EstimateCreateButton(estimate: PointEstimate, onCreateDeposit: ((String) -> Unit)?) {
  val draftJson = remember(estimate) { if (onCreateDeposit == null) null else estimateDraftJson(estimate.items) }
  if (draftJson != null) {
    EcoButton(
      text = "Lanjut Buat QR",
      onClick = { onCreateDeposit?.invoke(draftJson) },
      modifier = Modifier.fillMaxWidth(),
      containerColor = EcoColor.Green700,
      endIcon = { Icon(Lucide.ArrowRight, null, Modifier.size(20.dp)) },
    )
  }
}
