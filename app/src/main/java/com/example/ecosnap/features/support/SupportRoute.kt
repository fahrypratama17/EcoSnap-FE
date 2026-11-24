package com.example.ecosnap.features.support

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
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
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SupportRoute(
    accountUid: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: SupportViewModel = viewModel(
        key = "support-$accountUid",
        factory = SupportViewModelFactory(context.applicationContext, accountUid),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val resume by rememberUpdatedState(viewModel::onResume)
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) resume() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    BackHandler(enabled = state.screen != SupportScreen.Hub) { viewModel.back() }

    Column(Modifier.fillMaxSize().background(Color.White)) {
        TextButton(
            onClick = { if (!viewModel.back()) onBack() },
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp),
        ) {
            Icon(Lucide.ArrowLeft, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(5.dp))
            Text(if (state.screen == SupportScreen.Hub) "Kembali" else "Bantuan", fontSize = 12.sp)
        }
        when (state.screen) {
            SupportScreen.Hub -> HubScreen(state, viewModel)
            SupportScreen.Compose -> ComposeScreen(state, viewModel)
            SupportScreen.List -> TicketListScreen(state, viewModel)
            SupportScreen.Detail -> TicketDetailScreen(state, viewModel)
        }
    }
}

private val PAGE_PADDING = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp)
private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy HH:mm", Locale("id", "ID"))

private fun formatTime(iso: String): String = try {
    OffsetDateTime.parse(iso).atZoneSameInstant(java.time.ZoneId.systemDefault()).format(TIME_FORMAT)
} catch (_: RuntimeException) {
    iso
}

@Composable
private fun Title(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge, color = EcoColor.Green900)
}

@Composable
private fun Note(text: String, error: Boolean = false) {
    Text(text, fontSize = 12.sp, lineHeight = 17.sp, color = if (error) EcoColor.TextError else EcoColor.Green700)
}

@Composable
private fun Loading() {
    Row(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalArrangement = Arrangement.Center) {
        CircularProgressIndicator(color = EcoColor.Green700)
    }
}

@Composable
private fun ErrorBlock(title: String, message: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = EcoColor.Green900, fontWeight = FontWeight.SemiBold)
        Text(message, color = EcoColor.Green700, textAlign = TextAlign.Center)
        TextButton(onClick = onRetry) { Text("Coba lagi") }
    }
}

@Composable
private fun PrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = EcoColor.Green800),
    ) { Text(text) }
}

// ---------------------------------------------------------------- hub + FAQ

@Composable
private fun HubScreen(state: SupportUiState, vm: SupportViewModel) {
    var expanded by rememberSaveable { mutableStateOf<String?>(null) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PAGE_PADDING, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Title("Pusat Bantuan")
            Spacer(Modifier.size(4.dp))
            Note(
                "Satu-satunya jalur bantuan adalah tiket di aplikasi. Tidak ada email, telepon, atau chat publik. " +
                    "Pengelola membalas secara manual tanpa jaminan waktu, dan balasan hanya terlihat saat Anda membuka ulang tiket. " +
                    "Tidak ada notifikasi push atau email.",
            )
        }
        item {
            PrimaryButton("Buat tiket (masalah atau masukan)") { vm.openCompose() }
            Spacer(Modifier.size(8.dp))
            OutlinedButton(onClick = vm::openList, modifier = Modifier.fillMaxWidth()) { Text("Tiket saya") }
        }
        item {
            Text("Pertanyaan umum", style = MaterialTheme.typography.titleMedium, color = EcoColor.Green900)
            Note("Isi FAQ adalah teks demo dari server, bukan kebijakan final.")
        }
        if (state.faqLoading) item { Loading() }
        state.faqError?.let { message -> item { ErrorBlock("Bantuan belum dapat dimuat.", message, vm::retryFaqs) } }
        items(state.faqs, key = { it.id }) { faq ->
            val open = expanded == faq.id
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(EcoColor.GreenSoft, RoundedCornerShape(12.dp))
                    .clickable { expanded = if (open) null else faq.id }
                    .padding(14.dp),
            ) {
                Text(faq.question, fontWeight = FontWeight.SemiBold, color = EcoColor.Green900)
                if (open) {
                    Spacer(Modifier.size(6.dp))
                    Text(faq.answer, color = EcoColor.Green800, fontSize = 13.sp, lineHeight = 19.sp)
                    if (faq.isDemo) {
                        Spacer(Modifier.size(4.dp))
                        Note("Teks demo, bukan kebijakan final.")
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- composer

@Composable
private fun ComposeScreen(state: SupportUiState, vm: SupportViewModel) {
    val subjectLength = TicketRules.length(state.subject)
    val messageLength = TicketRules.length(state.message)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PAGE_PADDING, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Title("Buat Tiket")
            Spacer(Modifier.size(4.dp))
            Note(
                "Tidak ada lampiran dan alamat email tidak dikirim. Tiket tidak dapat diedit atau dihapus, tidak mengubah poin, XP, voucher, atau saldo, " +
                    "dan tidak ada banding finansial. Maksimum 20 tiket yang belum selesai.",
            )
        }

        state.created?.let { ticket ->
            item {
                Column(
                    Modifier.fillMaxWidth().background(EcoColor.Green100, RoundedCornerShape(12.dp)).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        if (state.createdRecovered) "Tiket sebelumnya ternyata sudah terkirim." else "Tiket terkirim.",
                        fontWeight = FontWeight.SemiBold,
                        color = EcoColor.Green900,
                    )
                    Text(ticket.subject, color = EcoColor.Green800)
                    Note("Belum ada balasan. Buka ulang tiket nanti untuk memeriksa; tidak ada notifikasi atau perkiraan waktu balasan.")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { vm.dismissCreated(); vm.openDetail(ticket.id) }) { Text("Lihat tiket") }
                        TextButton(onClick = vm::dismissCreated) { Text("Tutup") }
                    }
                }
            }
        }

        if (state.recovering) item { Loading() }
        state.recoveryError?.let { message ->
            item {
                Column {
                    Note("Status tiket sebelumnya belum dapat dipastikan: $message", error = true)
                    Note("Pembuatan tiket baru ditahan sampai status tiket sebelumnya jelas.")
                    TextButton(onClick = vm::retryRecovery) { Text("Periksa lagi") }
                }
            }
        }
        if (state.pending != null) {
            item {
                Note(
                    "Pengiriman tiket sebelumnya belum diketahui hasilnya (jaringan terputus atau aplikasi tertutup). " +
                        "Isi di bawah dikunci. Tekan Kirim ulang: server tidak akan membuat tiket ganda.",
                    error = true,
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TicketKind.entries.forEach { kind ->
                    FilterChip(
                        selected = state.kind == kind,
                        onClick = { vm.setKind(kind) },
                        enabled = !state.formLocked,
                        label = { Text(kind.label) },
                    )
                }
            }
        }
        item {
            OutlinedTextField(
                value = state.subject,
                onValueChange = vm::setSubject,
                label = { Text("Subjek") },
                singleLine = true,
                enabled = !state.formLocked,
                isError = state.fieldErrors.subject != null,
                supportingText = {
                    Text(state.fieldErrors.subject ?: "$subjectLength/${TicketRules.SUBJECT_MAX}")
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            OutlinedTextField(
                value = state.message,
                onValueChange = vm::setMessage,
                label = { Text("Pesan") },
                minLines = 5,
                enabled = !state.formLocked,
                isError = state.fieldErrors.message != null,
                supportingText = {
                    Text(state.fieldErrors.message ?: "$messageLength/${TicketRules.MESSAGE_MAX}")
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        state.submitError?.let { message -> item { Note(message, error = true) } }
        item {
            val label = when {
                state.submitting -> "Mengirim..."
                state.pending != null -> "Kirim ulang"
                else -> "Kirim tiket"
            }
            PrimaryButton(
                label,
                enabled = !state.submitting && !state.recovering && state.recoveryError == null,
                onClick = vm::submit,
            )
        }
    }
}

// ---------------------------------------------------------------- list

@Composable
private fun TicketListScreen(state: SupportUiState, vm: SupportViewModel) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PAGE_PADDING, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Title("Tiket Saya")
            Spacer(Modifier.size(4.dp))
            Note("Balasan pengelola hanya muncul saat daftar ini dibuka ulang. Tidak ada notifikasi.")
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = state.filter == null, onClick = { vm.setFilter(null) }, label = { Text("Semua") })
                TicketStatus.entries.forEach { status ->
                    FilterChip(selected = state.filter == status, onClick = { vm.setFilter(status) }, label = { Text(status.label) })
                }
            }
        }
        if (state.listLoading) item { Loading() }
        state.listError?.let { message -> item { ErrorBlock("Tiket belum dapat dimuat.", message, vm::retryList) } }
        if (!state.listLoading && state.listError == null && state.tickets.isEmpty()) {
            item { Note("Belum ada tiket.") }
        }
        items(state.tickets, key = { it.id }) { ticket ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(EcoColor.GreenSoft, RoundedCornerShape(12.dp))
                    .clickable { vm.openDetail(ticket.id) }
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(ticket.subject, fontWeight = FontWeight.SemiBold, color = EcoColor.Green900, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${ticket.kind.label} · ${ticket.status.label} · ${formatTime(ticket.createdAt)}", fontSize = 11.sp, color = EcoColor.Green700)
                Text(
                    if (ticket.staffResponse == null) "Belum ada balasan" else "Ada balasan pengelola",
                    fontSize = 12.sp,
                    color = EcoColor.Green800,
                )
            }
        }
        if (state.loadingMore) item { Loading() }
        state.loadMoreError?.let { message -> item { Note(message, error = true) } }
        if (state.nextCursor != null && !state.loadingMore && !state.listLoading) {
            item { OutlinedButton(onClick = vm::loadMore, modifier = Modifier.fillMaxWidth()) { Text("Muat lebih banyak") } }
        }
    }
}

// ---------------------------------------------------------------- detail

@Composable
private fun TicketDetailScreen(state: SupportUiState, vm: SupportViewModel) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PAGE_PADDING, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (state.detailLoading) item { Loading() }
        state.detailError?.let { message -> item { ErrorBlock("Tiket belum dapat dimuat.", message, vm::retryDetail) } }
        state.detail?.let { ticket ->
            item {
                Title(ticket.subject)
                Spacer(Modifier.size(4.dp))
                Note("${ticket.kind.label} · ${ticket.status.label}")
                Note("Dikirim ${formatTime(ticket.createdAt)} · Diperbarui ${formatTime(ticket.updatedAt)}")
            }
            item {
                Column(Modifier.fillMaxWidth().background(EcoColor.GreenSoft, RoundedCornerShape(12.dp)).padding(14.dp)) {
                    Text("Pesan Anda", fontWeight = FontWeight.SemiBold, color = EcoColor.Green900)
                    Spacer(Modifier.size(4.dp))
                    Text(ticket.message, color = EcoColor.Green800)
                }
            }
            item {
                Column(Modifier.fillMaxWidth().background(EcoColor.Green100, RoundedCornerShape(12.dp)).padding(14.dp)) {
                    Text("Balasan pengelola", fontWeight = FontWeight.SemiBold, color = EcoColor.Green900)
                    Spacer(Modifier.size(4.dp))
                    if (ticket.staffResponse == null) {
                        Text("Belum ada balasan.", color = EcoColor.Green800)
                        Spacer(Modifier.size(4.dp))
                        Note("Balasan tidak dikirim lewat email atau notifikasi, dan tidak ada perkiraan waktu. Buka ulang halaman ini untuk memeriksa.")
                    } else {
                        Text(ticket.staffResponse, color = EcoColor.Green800)
                        ticket.respondedAt?.let { Note("Dibalas ${formatTime(it)}") }
                    }
                }
            }
        }
    }
}
