package com.example.ecosnap.features.reports

import android.content.ActivityNotFoundException
import android.content.Context
import androidx.compose.foundation.background
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.theme.EcoColor
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ActivityReportRoute(
    accountUid: String,
    isPartner: Boolean,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: ActivityReportViewModel = viewModel(
        key = "activity-report-$accountUid-$isPartner",
        factory = ActivityReportViewModelFactory(context.applicationContext, accountUid, isPartner),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val resume by rememberUpdatedState(viewModel::onResume)
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) resume() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    ActivityReportScreen(
        state = state,
        onBack = onBack,
        onRetry = viewModel::retry,
        onMode = viewModel::selectMode,
        onStart = viewModel::setStart,
        onEnd = viewModel::setEnd,
        onApply = viewModel::applyRange,
        onDownload = viewModel::downloadPdf,
    )
}

private val DISPLAY_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("id", "ID"))
private val NUMBER = java.text.NumberFormat.getIntegerInstance(Locale("id", "ID"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityReportScreen(
    state: ActivityReportUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onMode: (ReportMode) -> Unit,
    onStart: (LocalDate) -> Unit,
    onEnd: (LocalDate) -> Unit,
    onApply: () -> Unit,
    onDownload: () -> Unit,
) {
    val context = LocalContext.current
    var picking by rememberSaveable { mutableStateOf<String?>(null) }
    var launchMessage by remember { mutableStateOf<String?>(null) }
    val report = state.report

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
                Icon(Lucide.ArrowLeft, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(5.dp))
                Text("Kembali", fontSize = 12.sp)
            }
            Text(
                "Laporan Aktivitas",
                style = MaterialTheme.typography.titleLarge,
                color = EcoColor.Green900,
            )
            Text(
                "Rekap setoran yang sudah diklaim. Bukan dokumen keuangan atau bukti resmi.",
                fontSize = 12.sp,
                color = EcoColor.Green700,
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.mode == ReportMode.CurrentMonth,
                    onClick = { onMode(ReportMode.CurrentMonth) },
                    label = { Text("Bulan ini") },
                )
                FilterChip(
                    selected = state.mode == ReportMode.CustomRange,
                    onClick = { onMode(ReportMode.CustomRange) },
                    label = { Text("Rentang tanggal") },
                )
            }
        }

        if (state.mode == ReportMode.CustomRange) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { picking = "start" }, modifier = Modifier.weight(1f)) {
                            Text("Mulai: " + (state.startDate?.format(DISPLAY_DATE) ?: "pilih"), fontSize = 12.sp)
                        }
                        OutlinedButton(onClick = { picking = "end" }, modifier = Modifier.weight(1f)) {
                            Text("Akhir: " + (state.endDate?.format(DISPLAY_DATE) ?: "pilih"), fontSize = 12.sp)
                        }
                    }
                    Text(
                        "Kedua tanggal wajib diisi, dihitung per hari WIB, tanggal akhir ikut dihitung, maksimum 366 hari.",
                        fontSize = 11.sp,
                        color = EcoColor.Green700,
                    )
                    state.rangeError?.let { Text(it, color = EcoColor.TextError, fontSize = 12.sp) }
                    Button(
                        onClick = onApply,
                        enabled = !state.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = EcoColor.Green800),
                    ) { Text("Tampilkan laporan") }
                }
            }
        }

        if (state.isLoading) {
            item {
                Row(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(color = EcoColor.Green700)
                }
            }
        }

        state.error?.let { message ->
            item {
                Column(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Laporan belum dapat dimuat.", color = EcoColor.Green900, fontWeight = FontWeight.SemiBold)
                    Text(message, color = EcoColor.Green700, textAlign = TextAlign.Center)
                    TextButton(onClick = onRetry) { Text("Coba lagi") }
                }
            }
        }

        if (report != null) {
            item { ReportHeader(report) }
            item { TotalsCard(report) }
            item { PdfCard(state.pdf, onDownload, context) { launchMessage = it } }
            launchMessage?.let { message -> item { Text(message, color = EcoColor.TextError, fontSize = 12.sp) } }

            item { SectionTitle("Rincian harian (WIB)") }
            items(report.daily, key = { it.date }) { day -> DayRow(day, report.role) }

            item { SectionTitle("Per kategori") }
            if (report.categories.isEmpty()) {
                item { Text("Belum ada setoran yang diklaim pada periode ini.", color = EcoColor.Green700, fontSize = 12.sp) }
            } else {
                item {
                    Text(
                        "Jumlah setoran per kategori tidak dijumlahkan: satu setoran dapat memuat beberapa kategori.",
                        fontSize = 11.sp,
                        color = EcoColor.Green700,
                    )
                }
                items(report.categories, key = { it.id }) { category ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(category.name, fontWeight = FontWeight.SemiBold, color = EcoColor.Green900)
                            Text("${category.depositCount} setoran memuat kategori ini", fontSize = 11.sp, color = EcoColor.Green700)
                        }
                        Text("${category.weightKg} kg", color = EcoColor.Green900)
                    }
                }
            }
        }
    }

    if (picking != null) {
        val isStart = picking == "start"
        val initial = (if (isStart) state.startDate else state.endDate)
            ?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initial)
        DatePickerDialog(
            onDismissRequest = { picking = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            val date = java.time.Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            if (isStart) onStart(date) else onEnd(date)
                        }
                        picking = null
                    },
                ) { Text("Pilih") }
            },
            dismissButton = { TextButton(onClick = { picking = null }) { Text("Batal") } },
        ) { DatePicker(state = pickerState) }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = EcoColor.Green900)
}

@Composable
private fun ReportHeader(report: ActivityReport) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        val name = report.businessName?.let { "$it (${report.fullName})" } ?: report.fullName
        Text(name.ifBlank { "Akun EcoSnap" }, fontWeight = FontWeight.SemiBold, color = EcoColor.Green900)
        Text(
            "Periode ${report.firstDate} s.d. ${report.lastDate} (${report.days} hari, ${report.timezone})",
            fontSize = 12.sp,
            color = EcoColor.Green700,
        )
        Text("Dibuat server: ${report.generatedAt}", fontSize = 11.sp, color = EcoColor.Green700)
    }
}

@Composable
private fun TotalsCard(report: ActivityReport) {
    val totals = report.totals
    Column(
        Modifier
            .fillMaxWidth()
            .background(EcoColor.GreenSoft, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        StatLine("Setoran diklaim", NUMBER.format(totals.claimedDeposits))
        StatLine("Total berat", "${totals.weightKg} kg")
        when (report.role) {
            ReportRole.User -> {
                StatLine("Poin diperoleh", NUMBER.format(totals.points.earned ?: 0))
                StatLine("Poin ditukar", NUMBER.format(totals.points.spent ?: 0))
                Text(
                    "Poin diperoleh dan ditukar tidak dinetkan, dan bukan saldo dompet.",
                    fontSize = 11.sp,
                    color = EcoColor.Green700,
                )
            }
            ReportRole.Partner -> {
                StatLine("Poin diterima pengguna dari lokasi Anda", NUMBER.format(totals.points.credited ?: 0))
            }
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = EcoColor.Green800, modifier = Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.SemiBold, color = EcoColor.Green900)
    }
}

@Composable
private fun DayRow(day: ReportDay, role: ReportRole) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(day.date, color = EcoColor.Green900, fontSize = 13.sp)
            Text("${day.claimedDeposits} setoran · ${day.weightKg} kg", color = EcoColor.Green800, fontSize = 13.sp)
        }
        val points = when (role) {
            ReportRole.User -> "Poin +${NUMBER.format(day.points.earned ?: 0)} / ditukar ${NUMBER.format(day.points.spent ?: 0)}"
            ReportRole.Partner -> "Poin diterima pengguna ${NUMBER.format(day.points.credited ?: 0)}"
        }
        Text(points, fontSize = 11.sp, color = EcoColor.Green700)
        HorizontalDivider(Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun PdfCard(pdf: PdfState, onDownload: () -> Unit, context: Context, onLaunchError: (String?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "PDF dibuat server saat Anda menekan tombol dan disimpan sementara di cache aplikasi.",
            fontSize = 11.sp,
            color = EcoColor.Green700,
        )
        Button(
            onClick = {
                onLaunchError(null)
                onDownload()
            },
            enabled = pdf !is PdfState.Downloading,
            colors = ButtonDefaults.buttonColors(containerColor = EcoColor.Green800),
        ) {
            if (pdf is PdfState.Downloading) {
                CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("Mengunduh PDF...")
            } else {
                Text(if (pdf is PdfState.Ready) "Unduh ulang PDF" else "Unduh PDF")
            }
        }
        when (pdf) {
            is PdfState.Failed -> Text(pdf.message, color = EcoColor.TextError, fontSize = 12.sp)
            is PdfState.Ready -> {
                Text("Tersimpan: ${pdf.file.fileName}", fontSize = 12.sp, color = EcoColor.Green800)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        launch(context, ReportPdfIntents.view(pdf.file), "Tidak ada aplikasi pembaca PDF di perangkat ini. Anda masih dapat membagikan berkasnya.", onLaunchError)
                    }) { Text("Buka") }
                    OutlinedButton(onClick = {
                        launch(context, ReportPdfIntents.share(pdf.file), "Tidak ada aplikasi yang dapat menerima berkas PDF ini.", onLaunchError)
                    }) { Text("Bagikan") }
                }
            }
            else -> Unit
        }
    }
}

private fun launch(context: Context, intent: Intent, unavailable: String, onError: (String?) -> Unit) {
    try {
        context.startActivity(intent)
        onError(null)
    } catch (_: ActivityNotFoundException) {
        onError(unavailable)
    } catch (_: SecurityException) {
        onError("Berkas tidak dapat dibuka oleh aplikasi tujuan.")
    }
}
