package com.example.ecosnap.features.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ecosnap.core.model.WasteCategory
import com.example.ecosnap.core.network.BackendClient
import com.example.ecosnap.core.network.BackendException
import com.example.ecosnap.features.map.PartnerRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUiState(
  val status: RegistrationStatus = RegistrationStatus.PENDING,
  val items: List<PartnerRegistration> = emptyList(),
  val isLoading: Boolean = true,
  val error: String? = null,
  val selected: PartnerRegistration? = null,
  val categories: List<WasteCategory> = emptyList(),
  val categoriesLoading: Boolean = false,
  val categoriesError: String? = null,
  /** True while an approve/reject call is in flight; every control is locked. */
  val busy: Boolean = false,
  val actionError: String? = null,
  /** One-shot confirmation shown after a successful action. */
  val message: String? = null
)

class AdminViewModel(
  private val repository: AdminRepository,
  private val partners: PartnerRepository
) : ViewModel() {

  private val mutableState = MutableStateFlow(AdminUiState())
  val state: StateFlow<AdminUiState> = mutableState.asStateFlow()

  private var listJob: Job? = null
  private var categoriesJob: Job? = null

  init {
    load()
  }

  fun selectStatus(status: RegistrationStatus) {
    val current = mutableState.value
    if (current.busy || current.status == status) return
    mutableState.update { it.copy(status = status, items = emptyList()) }
    load()
  }

  fun refresh() {
    if (mutableState.value.busy) return
    load()
  }

  fun open(registration: PartnerRegistration) {
    if (mutableState.value.busy) return
    mutableState.update { it.copy(selected = registration, actionError = null) }
    if (registration.status == RegistrationStatus.PENDING.apiValue) loadCategories()
  }

  fun dismiss() {
    if (mutableState.value.busy) return
    mutableState.update { it.copy(selected = null, actionError = null) }
  }

  fun messageShown() = mutableState.update { it.copy(message = null) }

  fun retryCategories() = loadCategories()

  fun approve(input: ApprovalInput) {
    val target = mutableState.value.selected ?: return
    act(target, "Mitra ${target.businessName} disetujui.") { repository.approve(target.mitraUid, input) }
  }

  fun reject() {
    val target = mutableState.value.selected ?: return
    act(target, "Pendaftaran ${target.businessName} ditolak.") { repository.reject(target.mitraUid) }
  }

  private fun act(target: PartnerRegistration, success: String, call: suspend () -> Unit) {
    if (mutableState.value.busy) return
    mutableState.update { it.copy(busy = true, actionError = null) }
    viewModelScope.launch {
      try {
        call()
        mutableState.update { it.copy(busy = false, selected = null, message = success) }
        load()
      } catch (e: CancellationException) {
        throw e
      } catch (e: BackendException) {
        mutableState.update { it.copy(busy = false, actionError = actionMessage(e)) }
        // The registration may already have been handled elsewhere; refresh the list behind the dialog.
        if (e.code == "already_processed" || e.code == "registration_not_found") load()
      } catch (e: Exception) {
        mutableState.update { it.copy(busy = false, actionError = GENERIC_ERROR) }
      }
    }
  }

  private fun load() {
    listJob?.cancel()
    val status = mutableState.value.status
    mutableState.update { it.copy(isLoading = true, error = null) }
    listJob = viewModelScope.launch {
      try {
        val items = repository.registrations(status)
        mutableState.update { if (it.status == status) it.copy(items = items, isLoading = false) else it }
      } catch (e: CancellationException) {
        throw e
      } catch (e: BackendException) {
        mutableState.update { if (it.status == status) it.copy(isLoading = false, error = e.message) else it }
      } catch (e: Exception) {
        mutableState.update { if (it.status == status) it.copy(isLoading = false, error = GENERIC_ERROR) else it }
      }
    }
  }

  private fun loadCategories() {
    if (mutableState.value.categories.isNotEmpty() || categoriesJob?.isActive == true) return
    mutableState.update { it.copy(categoriesLoading = true, categoriesError = null) }
    categoriesJob = viewModelScope.launch {
      try {
        val categories = partners.categories()
        mutableState.update { it.copy(categories = categories, categoriesLoading = false) }
      } catch (e: CancellationException) {
        throw e
      } catch (e: BackendException) {
        mutableState.update { it.copy(categoriesLoading = false, categoriesError = e.message) }
      } catch (e: Exception) {
        mutableState.update { it.copy(categoriesLoading = false, categoriesError = GENERIC_ERROR) }
      }
    }
  }

  private fun actionMessage(e: BackendException): String = when (e.code) {
    "already_processed" -> "Pendaftaran ini sudah diproses."
    "registration_not_found" -> "Pendaftaran tidak ditemukan."
    else -> e.message ?: GENERIC_ERROR
  }

  class Factory(
    private val repository: AdminRepository = AdminRepository(BackendClient.default),
    private val partners: PartnerRepository = PartnerRepository(BackendClient.default)
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AdminViewModel(repository, partners) as T
  }

  private companion object {
    const val GENERIC_ERROR = "Terjadi kesalahan. Silakan coba lagi."
  }
}
