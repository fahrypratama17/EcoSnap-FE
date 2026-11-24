package com.example.ecosnap.features.support

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ecosnap.core.network.BackendException
import com.example.ecosnap.core.network.PendingMutation
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SupportScreen { Hub, Compose, List, Detail }

data class SupportUiState(
    val screen: SupportScreen = SupportScreen.Hub,

    val faqs: List<Faq> = emptyList(),
    val faqLoading: Boolean = true,
    val faqError: String? = null,

    val kind: TicketKind = TicketKind.Problem,
    val subject: String = "",
    val message: String = "",
    val fieldErrors: TicketFieldErrors = TicketFieldErrors(),
    val submitting: Boolean = false,
    val submitError: String? = null,
    /** Unresolved intent: its payload is locked into the form and may only be replayed with the same key. */
    val pending: PendingMutation? = null,
    val recovering: Boolean = false,
    val recoveryError: String? = null,
    val created: Ticket? = null,
    val createdRecovered: Boolean = false,

    val filter: TicketStatus? = null,
    val tickets: List<Ticket> = emptyList(),
    val nextCursor: String? = null,
    val listLoading: Boolean = false,
    val loadingMore: Boolean = false,
    val listError: String? = null,
    val loadMoreError: String? = null,

    val detailId: String? = null,
    val detail: Ticket? = null,
    val detailLoading: Boolean = false,
    val detailError: String? = null,
) {
    val formLocked: Boolean get() = pending != null || submitting || recovering
}

class SupportViewModel(private val repository: SupportRepository) : ViewModel() {
    private val _state = MutableStateFlow(SupportUiState())
    val state: StateFlow<SupportUiState> = _state.asStateFlow()

    private var started = false
    private var faqJob: Job? = null
    private var recoverJob: Job? = null
    private var submitJob: Job? = null
    private var listJob: Job? = null
    private var moreJob: Job? = null
    private var detailJob: Job? = null
    private var listGeneration = 0
    private var detailGeneration = 0

    /** First call loads everything needed; later calls (ON_RESUME) silently refresh the visible screen. */
    fun onResume() {
        if (!started) {
            started = true
            loadFaqs(showLoading = true)
            recover()
            return
        }
        when (_state.value.screen) {
            SupportScreen.Hub -> loadFaqs(showLoading = false)
            SupportScreen.Compose -> recover()
            SupportScreen.List -> loadList(showLoading = false)
            SupportScreen.Detail -> _state.value.detailId?.let { loadDetail(it, showLoading = false) }
        }
    }

    // ---- navigation (internal to the support route) ----

    fun openCompose() {
        _state.update { it.copy(screen = SupportScreen.Compose, submitError = null) }
        recover()
    }

    fun openList() {
        _state.update { it.copy(screen = SupportScreen.List) }
        loadList(showLoading = true)
    }

    fun openDetail(id: String) {
        _state.update { it.copy(screen = SupportScreen.Detail, detailId = id, detail = null, detailError = null) }
        loadDetail(id, showLoading = true)
    }

    /** Returns false when already at the hub (caller leaves the route). */
    fun back(): Boolean {
        val current = _state.value
        return when (current.screen) {
            SupportScreen.Hub -> false
            SupportScreen.Detail -> {
                detailJob?.cancel()
                _state.update { it.copy(screen = SupportScreen.List, detailId = null, detail = null) }
                loadList(showLoading = false)
                true
            }
            else -> {
                _state.update { it.copy(screen = SupportScreen.Hub) }
                true
            }
        }
    }

    // ---- FAQ ----

    fun retryFaqs() = loadFaqs(showLoading = true)

    private fun loadFaqs(showLoading: Boolean) {
        faqJob?.cancel()
        if (showLoading) _state.update { it.copy(faqLoading = true, faqError = null) }
        faqJob = viewModelScope.launch {
            try {
                val faqs = repository.faqs()
                _state.update { it.copy(faqs = faqs, faqLoading = false, faqError = null) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    if (it.faqs.isEmpty() || showLoading) it.copy(faqLoading = false, faqs = emptyList(), faqError = messageOf(e, "Bantuan belum dapat dimuat."))
                    else it.copy(faqLoading = false)
                }
            }
        }
    }

    // ---- composer ----

    fun setKind(kind: TicketKind) {
        if (!_state.value.formLocked) _state.update { it.copy(kind = kind) }
    }

    fun setSubject(value: String) {
        if (_state.value.formLocked) return
        _state.update { it.copy(subject = value.take(TicketRules.INPUT_CAP), fieldErrors = it.fieldErrors.copy(subject = null), submitError = null) }
    }

    fun setMessage(value: String) {
        if (_state.value.formLocked) return
        _state.update { it.copy(message = value.take(TicketRules.INPUT_CAP), fieldErrors = it.fieldErrors.copy(message = null), submitError = null) }
    }

    fun dismissCreated() = _state.update { it.copy(created = null, createdRecovered = false) }

    fun retryRecovery() = recover()

    /** Sends a new ticket, or replays the exact locked unresolved one with its original key. */
    fun submit() {
        val current = _state.value
        if (current.submitting || current.recovering) return
        val pending = current.pending
        val valid: ValidTicket?
        if (pending == null) {
            val (ticket, errors) = TicketRules.validate(current.kind, current.subject, current.message)
            if (ticket == null) {
                _state.update { it.copy(fieldErrors = errors) }
                return
            }
            valid = ticket
        } else {
            valid = null
        }
        _state.update { it.copy(submitting = true, submitError = null, created = null, fieldErrors = TicketFieldErrors()) }
        submitJob = viewModelScope.launch {
            try {
                val ticket = if (valid != null) repository.submit(valid) else repository.resume(pending!!)
                completeCreation(ticket, recovered = false)
            } catch (e: CancellationException) {
                throw e
            } catch (e: TicketIntentPendingException) {
                _state.update { it.copy(submitting = false, submitError = "Ada tiket sebelumnya yang belum selesai diproses. Selesaikan dulu sebelum membuat tiket baru.") }
                recover()
            } catch (e: BackendException) {
                val kept = readPending()
                _state.update {
                    it.copy(submitting = false, submitError = submitMessage(e, kept != null))
                }
                applyPending(kept)
            } catch (e: IOException) {
                _state.update { it.copy(submitting = false, submitError = "Penyimpanan perangkat tidak dapat dibaca, tiket tidak dikirim. Coba lagi.") }
            }
        }
    }

    private fun recover() {
        if (_state.value.submitting) return
        recoverJob?.cancel()
        _state.update { it.copy(recovering = true, recoveryError = null) }
        recoverJob = viewModelScope.launch {
            try {
                val stored = repository.pending()
                when {
                    stored == null -> _state.update { it.copy(recovering = false, pending = null) }
                    stored.resultId == null -> {
                        _state.update { it.copy(recovering = false) }
                        applyPending(stored)
                    }
                    else -> {
                        // Server created it before the app lost the answer; show it, then acknowledge.
                        val ticket = repository.resume(stored)
                        completeCreation(ticket, recovered = true)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val gone = e is BackendException && e.status == 404
                _state.update {
                    it.copy(recovering = false, recoveryError = if (gone) null else messageOf(e, "Status tiket sebelumnya belum dapat dipastikan."))
                }
            }
        }
    }

    private suspend fun completeCreation(ticket: Ticket, recovered: Boolean) {
        _state.update {
            it.copy(
                submitting = false,
                recovering = false,
                pending = null,
                recoveryError = null,
                submitError = null,
                created = ticket,
                createdRecovered = recovered,
                subject = "",
                message = "",
                fieldErrors = TicketFieldErrors(),
            )
        }
        try {
            repository.pending()?.takeIf { it.resultId == ticket.id }?.let { repository.acknowledge(it) }
        } catch (_: IOException) {
            // Left completed; the next entry recovers the same ticket and acknowledges again.
        }
    }

    private suspend fun readPending(): PendingMutation? = try {
        repository.pending()
    } catch (_: IOException) {
        null
    }

    private fun applyPending(mutation: PendingMutation?) {
        if (mutation == null || mutation.resultId != null) {
            if (mutation == null) _state.update { it.copy(pending = null) }
            return
        }
        val payload = mutation.payload
        val kind = payload.get("kind")?.takeIf { it.isJsonPrimitive }?.asString?.let { TicketKind.fromWire(it) }
        val subject = payload.get("subject")?.takeIf { it.isJsonPrimitive }?.asString
        val message = payload.get("message")?.takeIf { it.isJsonPrimitive }?.asString
        _state.update {
            it.copy(
                pending = mutation,
                kind = kind ?: it.kind,
                subject = subject ?: it.subject,
                message = message ?: it.message,
            )
        }
    }

    private fun submitMessage(e: BackendException, retained: Boolean): String = when {
        e.status == 429 || e.code == "ticket_limit_reached" ->
            "Anda sudah memiliki 20 tiket yang belum selesai. Tunggu pengelola menyelesaikannya sebelum membuat tiket baru."
        e.status == 400 || e.status == 413 || e.status == 415 ->
            "Tiket ditolak server dan tidak dikirim: ${e.message}"
        e.status == 401 -> "Sesi tidak valid. Masuk kembali; tiket yang tertunda akan dikirim ulang tanpa menggandakan."
        retained -> "Hasil pengiriman belum pasti (${e.message}). Tekan Kirim ulang: tiket tidak akan terkirim ganda."
        else -> e.message ?: "Tiket belum dapat dikirim."
    }

    // ---- list ----

    fun setFilter(status: TicketStatus?) {
        if (status == _state.value.filter) return
        _state.update { it.copy(filter = status) }
        loadList(showLoading = true)
    }

    fun retryList() = loadList(showLoading = true)

    private fun loadList(showLoading: Boolean) {
        val token = ++listGeneration
        listJob?.cancel()
        moreJob?.cancel()
        _state.update {
            if (showLoading) it.copy(tickets = emptyList(), nextCursor = null, listLoading = true, loadingMore = false, listError = null, loadMoreError = null)
            else it.copy(loadingMore = false, loadMoreError = null)
        }
        val filter = _state.value.filter
        listJob = viewModelScope.launch {
            try {
                val page = repository.tickets(filter, null)
                if (token != listGeneration) return@launch
                _state.update { it.copy(tickets = page.items, nextCursor = page.nextCursor, listLoading = false, listError = null) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (token != listGeneration) return@launch
                _state.update {
                    if (showLoading || it.tickets.isEmpty()) it.copy(tickets = emptyList(), nextCursor = null, listLoading = false, listError = messageOf(e, "Tiket belum dapat dimuat."))
                    else it.copy(listLoading = false)
                }
            }
        }
    }

    fun loadMore() {
        val current = _state.value
        val cursor = current.nextCursor ?: return
        if (current.listLoading || current.loadingMore) return
        val token = listGeneration
        _state.update { it.copy(loadingMore = true, loadMoreError = null) }
        moreJob = viewModelScope.launch {
            try {
                val page = repository.tickets(current.filter, cursor)
                if (token != listGeneration) return@launch
                _state.update {
                    val known = it.tickets.mapTo(HashSet()) { t -> t.id }
                    it.copy(tickets = it.tickets + page.items.filter { t -> t.id !in known }, nextCursor = page.nextCursor, loadingMore = false)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (token != listGeneration) return@launch
                _state.update { it.copy(loadingMore = false, loadMoreError = messageOf(e, "Tiket berikutnya belum dapat dimuat.")) }
            }
        }
    }

    // ---- detail ----

    fun retryDetail() = _state.value.detailId?.let { loadDetail(it, showLoading = true) }

    private fun loadDetail(id: String, showLoading: Boolean) {
        val token = ++detailGeneration
        detailJob?.cancel()
        if (showLoading) _state.update { it.copy(detailLoading = true, detailError = null, detail = null) }
        detailJob = viewModelScope.launch {
            try {
                val ticket = repository.ticket(id)
                if (token != detailGeneration) return@launch
                _state.update { it.copy(detail = ticket, detailLoading = false, detailError = null) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (token != detailGeneration) return@launch
                _state.update {
                    if (showLoading || it.detail == null) it.copy(detail = null, detailLoading = false, detailError = messageOf(e, "Tiket belum dapat dimuat."))
                    else it.copy(detailLoading = false)
                }
            }
        }
    }

    private fun messageOf(e: Exception, fallback: String): String =
        (e as? BackendException)?.message?.takeIf { it.isNotBlank() } ?: fallback
}

class SupportViewModelFactory(private val context: Context, private val uid: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(SupportViewModel::class.java))
        @Suppress("UNCHECKED_CAST")
        return SupportViewModel(SupportRepository(context, uid)) as T
    }
}
