package com.example.ecosnap.features.deposits

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.QrCode
import android.content.Context
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.components.MitraBackLink
import com.example.ecosnap.core.designsystem.components.MitraCard
import com.example.ecosnap.core.designsystem.components.MitraInfoCard
import com.example.ecosnap.core.designsystem.components.MitraPageTitle
import com.example.ecosnap.core.designsystem.components.MitraWeightHero
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.core.network.BackendException
import com.example.ecosnap.core.network.PendingMutation
import com.example.ecosnap.core.network.PendingMutationConflictException
import com.example.ecosnap.core.network.PendingMutationStore
import com.google.gson.JsonObject
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DepositCreateUiState(
  val recovering: Boolean = true,
  val recoverError: String? = null,
  val loadingCategories: Boolean = true,
  val categories: List<WasteOption> = emptyList(),
  val categoriesError: String? = null,
  val rows: List<WeightRow> = listOf(WeightRow(0)),
  /** An earlier attempt whose outcome is unknown: the form is locked to exactly that payload. */
  val unresolved: Boolean = false,
  /** Server already created this deposit but the app never navigated to it. */
  val completedId: String? = null,
  val validation: String? = null,
  val submitting: Boolean = false,
  val error: String? = null,
  val createdId: String? = null,
)

class PartnerDepositCreateViewModel(
  private val uid: String,
  private val repository: DepositRepository,
  private val store: PendingMutationStore,
  private val initialItemsJson: String? = null,
) : ViewModel() {
  private val _state = MutableStateFlow(DepositCreateUiState())
  val state: StateFlow<DepositCreateUiState> = _state.asStateFlow()
  private var nextKey = 1L
  private var submitJob: Job? = null
  private var draftConsumed = initialItemsJson == null

  private class Draft(val rows: List<WeightRow>?, val message: String?)

  /** Parses the estimate draft into rows; an invalid draft is ignored with a visible message. */
  private fun parseDraft(): Draft? {
    val json = initialItemsJson ?: return null
    return try {
      val array = com.google.gson.JsonParser.parseString(json).asJsonArray
      if (array.size() == 0 || array.size() > WeightInput.MAX_ITEMS) throw IllegalArgumentException()
      val rows = WeightRows.fromPayload(array, nextKey)
      if (rows.any { WeightInput.canonical(it.weight) == null }) throw IllegalArgumentException()
      nextKey += rows.size
      Draft(rows, null)
    } catch (_: RuntimeException) {
      Draft(null, "Draf dari kalkulator tidak valid dan diabaikan. Isi kategori dan berat secara manual.")
    }
  }

  init {
    recover()
    loadCategories()
  }

  fun recover() {
    _state.update { it.copy(recovering = true, recoverError = null) }
    viewModelScope.launch {
      try {
        val pending = store.read(uid, SCOPE)
        // A saved intent always wins over an incoming draft; the draft is applied at most once.
        val draft = if (pending == null && !draftConsumed) parseDraft() else null
        if (pending == null) draftConsumed = true
        _state.update { current ->
          when {
            pending == null && draft != null && draft.rows != null ->
              current.copy(recovering = false, rows = draft.rows)
            pending == null && draft != null -> current.copy(recovering = false, validation = draft.message)
            pending == null -> current.copy(recovering = false)
            pending.resultId != null -> current.copy(recovering = false, completedId = pending.resultId)
            else -> current.copy(
              recovering = false,
              unresolved = true,
              rows = restoreRows(pending),
              error = "Percobaan sebelumnya belum diketahui hasilnya. Kirim ulang untuk memastikan; setoran tidak akan terbuat dua kali.",
            )
          }
        }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _state.update {
          it.copy(recovering = false, recoverError = "Data setoran tertunda tidak dapat dibaca dari penyimpanan perangkat.")
        }
      }
    }
  }

  private fun restoreRows(pending: PendingMutation): List<WeightRow> = try {
    WeightRows.fromPayload(pending.payload.getAsJsonArray("items"), nextKey).also { nextKey += it.size }
  } catch (_: RuntimeException) {
    emptyList()
  }

  fun loadCategories() {
    _state.update { it.copy(loadingCategories = true, categoriesError = null) }
    viewModelScope.launch {
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
    _state.update {
      if (it.unresolved || it.submitting || it.completedId != null) it
      else it.copy(rows = transform(it.rows), validation = null, error = null)
    }
  }

  fun consumeCreated() = _state.update { it.copy(createdId = null) }

  /** The user acknowledges an already-created deposit: drop the completed intent, then open it. */
  fun openCompleted() {
    val id = _state.value.completedId ?: return
    viewModelScope.launch {
      try {
        store.read(uid, SCOPE)?.takeIf { it.resultId == id }?.let { store.clear(uid, SCOPE, it.key) }
        _state.update { it.copy(completedId = null, createdId = id) }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _state.update { it.copy(error = "Penyimpanan perangkat tidak dapat diperbarui. Coba lagi.") }
      }
    }
  }

  fun submit() {
    val snapshot = _state.value
    if (snapshot.submitting || snapshot.unresolved || snapshot.completedId != null ||
      snapshot.recovering || snapshot.recoverError != null
    ) return
    val payload = when (val result = WeightRows.validate(snapshot.rows, snapshot.categories)) {
      is RowsResult.Invalid -> {
        _state.update { it.copy(validation = result.message ?: "Lengkapi kategori dan berat.") }
        return
      }
      is RowsResult.Valid -> JsonObject().apply { add("items", result.items) }
    }
    submitJob = viewModelScope.launch {
      _state.update { it.copy(submitting = true, error = null, validation = null) }
      try {
        val pending = try {
          store.begin(uid, SCOPE, payload)
        } catch (e: PendingMutationConflictException) {
          _state.update { it.copy(submitting = false) }
          recover()
          return@launch
        }
        send(pending)
      } catch (e: CancellationException) {
        throw e
      } catch (e: IOException) {
        _state.update { it.copy(submitting = false, error = "Penyimpanan perangkat tidak dapat ditulis. Setoran belum dikirim.") }
      }
    }
  }

  private suspend fun send(pending: PendingMutation) {
    _state.update { it.copy(submitting = true, unresolved = true) }
    try {
      val deposit = repository.create(pending.payload, pending.key)
      withContext(NonCancellable) {
        try {
          store.resolve(uid, SCOPE, pending.key, deposit.id)
        } catch (_: IOException) {
          // The deposit exists server-side; a stale entry only shows the "already created" prompt later.
        }
      }
      _state.update { it.copy(submitting = false, unresolved = false, completedId = deposit.id) }
    } catch (e: CancellationException) {
      throw e
    } catch (e: BackendException) {
      if (isFinalRejection(e)) {
        withContext(NonCancellable) { runCatching { store.clear(uid, SCOPE, pending.key) } }
        _state.update { it.copy(submitting = false, unresolved = false, error = DepositText.message(e)) }
      } else {
        _state.update {
          it.copy(
            submitting = false,
            unresolved = true,
            error = DepositText.message(e) + " Hasil belum diketahui; kirim ulang dengan data yang sama.",
          )
        }
      }
    } catch (e: Exception) {
      _state.update {
        it.copy(submitting = false, unresolved = true, error = "Hasil belum diketahui; kirim ulang dengan data yang sama.")
      }
    }
  }

  /** Resends the persisted intent unchanged (same key, same payload). */
  fun retryUnresolved() {
    if (_state.value.submitting) return
    submitJob = viewModelScope.launch {
      _state.update { it.copy(submitting = true, error = null) }
      try {
        val pending = store.read(uid, SCOPE)
        if (pending == null) {
          _state.update { it.copy(submitting = false, unresolved = false) }
        } else if (pending.resultId != null) {
          _state.update { it.copy(submitting = false, completedId = pending.resultId) }
        } else {
          send(pending)
        }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _state.update { it.copy(submitting = false, error = "Data setoran tertunda tidak dapat dibaca. Coba lagi.") }
      }
    }
  }

  private fun isFinalRejection(e: BackendException): Boolean {
    val status = e.status ?: return false
    return status in 400..499 && status != 401 && status != 408 && status != 429
  }

  class Factory(private val uid: String, context: Context, private val initialItemsJson: String? = null) : ViewModelProvider.Factory {
    private val appContext = context.applicationContext

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
      PartnerDepositCreateViewModel(uid, DepositRepository(), PendingMutationStore(appContext), initialItemsJson) as T
  }

  private companion object {
    const val SCOPE = "deposit_create"
  }
}

@Composable
fun PartnerDepositCreateRoute(
  accountUid: String,
  onBack: () -> Unit,
  onCreated: (String) -> Unit,
  initialItemsJson: String? = null,
) {
  val context = LocalContext.current
  val vm: PartnerDepositCreateViewModel = viewModel(
    key = "partner-deposit-create-$accountUid",
    factory = PartnerDepositCreateViewModel.Factory(accountUid, context, initialItemsJson),
  )
  val state by vm.state.collectAsStateWithLifecycle()

  LaunchedEffect(state.createdId) {
    state.createdId?.let {
      vm.consumeCreated()
      onCreated(it)
    }
  }

  Column(Modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState())) {
    MitraBackLink(onBack, Modifier.padding(start = 12.dp, top = 8.dp))
    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
      when {
        state.recovering || state.loadingCategories -> {
          MitraPageTitle("Buat Setoran", "Pilih kategori dan masukkan berat sampah yang ditimbang")
          MitraLoadingCard("Memuat...")
        }
        state.recoverError != null -> {
          MitraPageTitle("Buat Setoran", "Data setoran tertunda tidak dapat dibaca")
          MitraErrorCard(state.recoverError!!, vm::recover, onBack)
        }
        state.categoriesError != null -> {
          MitraPageTitle("Buat Setoran", "Kategori sampah belum dapat dimuat")
          MitraErrorCard(state.categoriesError!!, vm::loadCategories, onBack)
        }
        state.completedId != null -> {
          MitraPageTitle("Generate QR", "Setoran sudah tercatat di server")
          MitraCard {
            Column(
              Modifier.fillMaxWidth().padding(vertical = 24.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
              Icon(Lucide.QrCode, null, Modifier.size(64.dp), tint = EcoColor.Green700)
              Text("Setoran sudah dibuat", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = EcoColor.Green900)
              Text(
                "Setoran sudah dibuat server. Buka setoran tersebut untuk menerbitkan QR.",
                fontSize = 12.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center,
                color = EcoColor.Green800,
              )
              EcoButton("Buka Setoran", vm::openCompleted, Modifier.fillMaxWidth(), containerColor = EcoColor.Green700)
            }
          }
          state.error?.let { Notice(it, error = true) }
        }
        else -> {
          MitraPageTitle("Buat Setoran", "Pilih kategori dan masukkan berat sampah yang ditimbang")
          MitraWeightHero(rememberTotalWeightText(state.rows))
          MitraWeightEditor(
            rows = state.rows,
            categories = state.categories,
            enabled = !state.unresolved && !state.submitting,
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
          state.error?.let { Notice(it, error = true) }
          MitraInfoCard(
            "Poin dihitung server",
            "Tarif poin dari server dapat berupa tarif demo dan dihitung ulang saat setoran dibuat. " +
              "Setelah QR terbit, setoran dikunci dan tidak dapat diubah.",
          )
          if (state.unresolved) {
            EcoButton(
              text = if (state.submitting) "Mengirim..." else "Kirim Ulang",
              onClick = vm::retryUnresolved,
              enabled = !state.submitting,
              modifier = Modifier.fillMaxWidth(),
              containerColor = EcoColor.Green700,
              endIcon = { Icon(Lucide.ArrowRight, null, Modifier.size(20.dp)) },
            )
          } else {
            EcoButton(
              text = if (state.submitting) "Membuat..." else "Buat Setoran",
              onClick = vm::submit,
              enabled = !state.submitting,
              modifier = Modifier.fillMaxWidth(),
              containerColor = EcoColor.Green700,
              endIcon = { Icon(Lucide.ArrowRight, null, Modifier.size(20.dp)) },
            )
          }
        }
      }
    }
  }
}
