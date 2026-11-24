package com.example.ecosnap.features.reports

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ecosnap.core.network.BackendException
import java.io.IOException
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ReportMode { CurrentMonth, CustomRange }

sealed interface PdfState {
    data object Idle : PdfState
    data object Downloading : PdfState
    data class Ready(val file: DownloadedReport) : PdfState
    data class Failed(val message: String) : PdfState
}

data class ActivityReportUiState(
    val mode: ReportMode = ReportMode.CurrentMonth,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val rangeError: String? = null,
    val isLoading: Boolean = true,
    val report: ActivityReport? = null,
    val error: String? = null,
    val pdf: PdfState = PdfState.Idle,
)

class ActivityReportViewModel(
    private val repository: ActivityReportRepository,
    private val isPartner: Boolean,
) : ViewModel() {
    private val _state = MutableStateFlow(ActivityReportUiState())
    val state: StateFlow<ActivityReportUiState> = _state.asStateFlow()

    private var started = false
    private var generation = 0
    private var loadJob: Job? = null
    private var pdfJob: Job? = null

    /** The range of the report currently shown; null = current month. Resume refreshes this one. */
    private var activeRange: ReportRange? = null

    fun onResume() {
        if (!started) {
            started = true
            load(activeRange, showLoading = true)
        } else if (!_state.value.isLoading) {
            load(activeRange, showLoading = false)
        }
    }

    fun retry() = load(activeRange, showLoading = true)

    fun selectMode(mode: ReportMode) {
        if (mode == _state.value.mode) return
        _state.update { it.copy(mode = mode, rangeError = null) }
        if (mode == ReportMode.CurrentMonth) {
            activeRange = null
            load(null, showLoading = true)
        }
    }

    fun setStart(date: LocalDate) = _state.update { it.copy(startDate = date, rangeError = null) }

    fun setEnd(date: LocalDate) = _state.update { it.copy(endDate = date, rangeError = null) }

    fun applyRange() {
        val current = _state.value
        val (range, problem) = ReportRanges.build(current.startDate, current.endDate)
        if (range == null) {
            _state.update { it.copy(rangeError = problem) }
            return
        }
        activeRange = range
        load(range, showLoading = true)
    }

    fun downloadPdf() {
        val report = _state.value.report ?: return
        if (_state.value.pdf is PdfState.Downloading) return
        pdfJob?.cancel()
        _state.update { it.copy(pdf = PdfState.Downloading) }
        pdfJob = viewModelScope.launch {
            val result = try {
                PdfState.Ready(repository.downloadPdf(report))
            } catch (e: CancellationException) {
                throw e
            } catch (e: BackendException) {
                PdfState.Failed(e.message ?: "PDF belum dapat diunduh.")
            } catch (e: IOException) {
                PdfState.Failed("PDF diunduh tetapi tidak dapat disimpan di perangkat.")
            } catch (e: IllegalArgumentException) {
                PdfState.Failed("PDF tersimpan tetapi aplikasi belum dapat membagikan berkas (FileProvider belum dikonfigurasi).")
            }
            _state.update { it.copy(pdf = result) }
        }
    }

    private fun load(range: ReportRange?, showLoading: Boolean) {
        val token = ++generation
        loadJob?.cancel()
        _state.update {
            if (showLoading) it.copy(isLoading = true, error = null, report = null, pdf = PdfState.Idle)
            else it
        }
        loadJob = viewModelScope.launch {
            try {
                val report = repository.report(range)
                if (token != generation) return@launch
                val expected = if (isPartner) ReportRole.Partner else ReportRole.User
                if (report.role != expected) {
                    _state.update {
                        it.copy(isLoading = false, report = null, error = "Peran akun pada laporan tidak sesuai dengan sesi ini.")
                    }
                    return@launch
                }
                _state.update {
                    // A refreshed report for the same period keeps a ready PDF; a different one invalidates it.
                    val keepPdf = it.report?.let { old -> old.periodFrom == report.periodFrom && old.periodTo == report.periodTo } == true
                    it.copy(
                        isLoading = false,
                        report = report,
                        error = null,
                        pdf = if (keepPdf) it.pdf else PdfState.Idle,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (token != generation) return@launch
                val message = (e as? BackendException)?.message?.takeIf { it.isNotBlank() }
                    ?: "Laporan belum dapat dimuat. Silakan coba lagi."
                _state.update {
                    // A failed silent refresh keeps the report already on screen.
                    if (showLoading || it.report == null) it.copy(isLoading = false, report = null, error = message)
                    else it.copy(isLoading = false)
                }
            }
        }
    }
}

class ActivityReportViewModelFactory(
    private val context: Context,
    private val uid: String,
    private val isPartner: Boolean,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ActivityReportViewModel::class.java))
        @Suppress("UNCHECKED_CAST")
        return ActivityReportViewModel(ActivityReportRepository(context, uid), isPartner) as T
    }
}
