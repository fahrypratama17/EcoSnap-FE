package com.example.ecosnap.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.lucide.History
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.LogOut
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Map
import com.composables.icons.lucide.QrCode
import com.composables.icons.lucide.RotateCcw
import com.composables.icons.lucide.Scale
import com.composables.icons.lucide.TriangleAlert
import com.example.ecosnap.core.auth.AccountProfile
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.components.MitraCard
import com.example.ecosnap.core.designsystem.components.MitraInfoCard
import com.example.ecosnap.core.designsystem.components.MitraMenuCard
import com.example.ecosnap.core.designsystem.components.MitraPanel
import com.example.ecosnap.core.designsystem.components.MitraStat
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.home.model.PartnerHomeData

/**
 * Partner Home: business identity from the account plus the `GET /partner/home` summary. A 403
 * `deposit_forbidden` is shown as an explanation (pending / no location), never as zero counts.
 * Notifications and profile live in the shared app header, so they are not repeated here.
 */
@Composable
fun PartnerHomeRoute(
  account: AccountProfile,
  onRefreshAccount: () -> Unit,
  onBrowsePartners: () -> Unit,
  onLogout: () -> Unit,
  onCreateDeposit: () -> Unit,
  onDeposits: () -> Unit,
  onEstimate: () -> Unit
) {
  val viewModel: PartnerHomeViewModel =
    viewModel(key = "partner-home-${account.uid}", factory = PartnerHomeViewModelFactory())
  val state by viewModel.state.collectAsStateWithLifecycle()

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }

  // A refreshed account may carry a new business status; reload the summary when it changes.
  var firstStatus by remember { mutableStateOf(true) }
  LaunchedEffect(account.business?.status) {
    if (firstStatus) firstStatus = false else viewModel.refresh()
  }

  PartnerHomeScreen(
    account = account,
    state = state,
    onRetry = viewModel::refresh,
    onRefreshAccount = onRefreshAccount,
    onBrowsePartners = onBrowsePartners,
    onLogout = onLogout,
    onCreateDeposit = onCreateDeposit,
    onDeposits = onDeposits,
    onEstimate = onEstimate
  )
}

private class PartnerMenu(
  val title: String,
  val description: String,
  val action: String,
  val icon: ImageVector,
  val onClick: () -> Unit
)

@Composable
fun PartnerHomeScreen(
  account: AccountProfile,
  state: PartnerHomeUiState,
  onRetry: () -> Unit,
  onRefreshAccount: () -> Unit,
  onBrowsePartners: () -> Unit,
  onLogout: () -> Unit,
  onCreateDeposit: () -> Unit,
  onDeposits: () -> Unit,
  onEstimate: () -> Unit,
  modifier: Modifier = Modifier
) {
  val statusRaw = state.data?.partnerStatus ?: account.business?.status
  val canCreate = statusRaw == "verified"
  val features = buildList {
    add(
      PartnerMenu(
        "Kalkulator Sampah",
        "Hitung berat sampah dan estimasi poin yang diberikan",
        "Buka Kalkulator",
        Lucide.Scale,
        onEstimate
      )
    )
    if (canCreate) {
      add(
        PartnerMenu(
          "Generate QR",
          "Buat setoran dan QR transaksi agar pengguna bisa mengklaim poin",
          "Buat Setoran",
          Lucide.QrCode,
          onCreateDeposit
        )
      )
    }
    add(
      PartnerMenu(
        "Riwayat Setoran",
        "Lihat setoran yang dibuat, QR diterbitkan, dan yang sudah diklaim",
        "Buka Riwayat",
        Lucide.History,
        onDeposits
      )
    )
    add(
      PartnerMenu(
        "Telusuri Mitra",
        "Lihat daftar dan lokasi mitra EcoSnap",
        "Buka Peta Mitra",
        Lucide.Map,
        onBrowsePartners
      )
    )
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text("Selamat datang kembali!", fontSize = 11.sp, color = EcoColor.Green900)
        Text(
          text = "Halo, ${account.displayName.ifBlank { account.fullName }}!",
          style = MaterialTheme.typography.headlineMedium.copy(fontSize = 23.sp, lineHeight = 30.sp),
          color = EcoColor.Green900
        )
        Text(
          text = "Kelola penyetoran sampah dengan mudah melalui EcoSnap.",
          style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, lineHeight = 17.sp),
          color = EcoColor.Green700
        )
      }
    }

    partnerStatusNotice(statusRaw, state.depositForbidden)?.let { notice ->
      item { MitraInfoCard(notice.title, notice.text, icon = notice.icon, error = notice.error) }
    }

    item {
      MitraPanel(
        title = state.data?.businessName?.ifBlank { null } ?: account.business?.name ?: "Siap menerima setoran?",
        subtitle = account.business?.category?.ifBlank { null }
          ?: "Timbang sampah, hitung poin, lalu buat QR untuk pengguna.",
        statusLabel = statusRaw?.let { "Status: ${partnerStatusLabel(it)}" }
      ) {
        PartnerPanelSummary(state = state, onRetry = onRetry)
      }
    }

    state.data?.let { data ->
      if (state.error != null) {
        item {
          Column {
            Text(text = state.error, style = MaterialTheme.typography.bodyMedium, color = EcoColor.TextError)
            Spacer(Modifier.height(6.dp))
            EcoButton(text = "Muat ulang ringkasan", onClick = onRetry, height = 40.dp)
          }
        }
      }
      item { RecentDepositsCard(data) }
    }

    item {
      Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Menu Utama", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = EcoColor.Green900)
        Spacer(Modifier.weight(1f))
        Text("${features.size} Fitur", fontSize = 11.sp, color = EcoColor.Green700)
      }
    }
    features.forEach { menu ->
      item { MitraMenuCard(menu.title, menu.description, menu.action, menu.icon, menu.onClick) }
    }

    item {
      Text("Akun", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = EcoColor.Green900)
    }
    item {
      MitraMenuCard(
        title = "Muat Ulang Akun",
        description = "Periksa status verifikasi dan data akun terbaru",
        action = "Muat Ulang",
        icon = Lucide.RotateCcw,
        onClick = onRefreshAccount
      )
    }
    item {
      MitraMenuCard(
        title = "Keluar",
        description = "Akhiri sesi mitra di perangkat ini",
        action = "Keluar dari Akun",
        icon = Lucide.LogOut,
        onClick = onLogout
      )
    }

    item {
      MitraCard {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Lucide.Info, null, Modifier.size(20.dp), tint = EcoColor.Green900)
          Text("Cara Kerja EcoSnap Mitra", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = EcoColor.Green900)
        }
        Spacer(Modifier.height(14.dp))
        listOf(
          "1. Masukkan hasil penimbangan sampah.",
          "2. Periksa jumlah poin yang akan diberikan.",
          "3. Generate QR dan minta pengguna memindainya."
        ).forEach {
          Text(it, fontSize = 11.sp, lineHeight = 22.sp, color = EcoColor.Green800)
        }
      }
    }
  }
}

private class StatusNotice(val title: String, val text: String, val icon: ImageVector, val error: Boolean)

/** Banner for every partner state that cannot create deposits; null for a verified partner. */
private fun partnerStatusNotice(status: String?, forbidden: Boolean): StatusNotice? = when {
  status == "verified" -> null
  status == "suspended" -> StatusNotice(
    "Akun mitra ditangguhkan",
    "Pembuatan setoran dan QR dinonaktifkan sementara. Hubungi tim EcoSnap untuk informasi lebih lanjut.",
    Lucide.TriangleAlert,
    error = true
  )
  status == "rejected" -> StatusNotice(
    "Verifikasi ditolak",
    "Pengajuan lokasi mitra ditolak sehingga setoran dan QR belum dapat dibuat. Periksa data akun atau hubungi tim EcoSnap.",
    Lucide.TriangleAlert,
    error = true
  )
  status == "pending" -> StatusNotice(
    "Menunggu verifikasi",
    "Akun mitra Anda masih diverifikasi. Pembuatan setoran dan QR tersedia setelah lokasi terverifikasi.",
    Lucide.Info,
    error = false
  )
  forbidden -> StatusNotice(
    "Ringkasan belum tersedia",
    "Akun mitra Anda belum memiliki lokasi terverifikasi. Gunakan Muat Ulang Akun untuk memeriksa status terbaru.",
    Lucide.Info,
    error = false
  )
  else -> null
}

/** Inner summary of the PANEL MITRA card: loading, forbidden/pending, error, or real server counters. */
@Composable
private fun PartnerPanelSummary(state: PartnerHomeUiState, onRetry: () -> Unit) {
  val data = state.data
  when {
    state.isLoading -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
      CircularProgressIndicator(color = EcoColor.Green700)
    }

    state.depositForbidden -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(
        text = "Ringkasan setoran belum tersedia",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = EcoColor.Green900
      )
      Text(
        text = "Ringkasan akan muncul setelah akun mitra memiliki lokasi terverifikasi.",
        fontSize = 11.sp,
        lineHeight = 16.sp,
        color = EcoColor.Green800
      )
    }

    data != null -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        MitraStat("Poin terklaim", data.claimedPoints.toString(), "Poin", Modifier.weight(1f))
        MitraStat("Berat terklaim", "${data.claimedWeightKg} kg", "Total", end = true)
      }
      Row(Modifier.fillMaxWidth()) {
        MitraStat("Dibuat", data.created.toString(), "Setoran", Modifier.weight(1f))
        MitraStat("QR diterbitkan", data.qrIssued.toString(), "Setoran", Modifier.weight(1f))
        MitraStat("Diklaim", data.claimed.toString(), "Setoran", Modifier.weight(1f), end = true)
      }
    }

    else -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text(
        text = state.error ?: "Ringkasan mitra belum dapat dimuat.",
        style = MaterialTheme.typography.bodyMedium,
        color = EcoColor.TextError
      )
      EcoButton(text = "Coba lagi", onClick = onRetry, height = 40.dp, modifier = Modifier.fillMaxWidth())
    }
  }
}

@Composable
private fun RecentDepositsCard(data: PartnerHomeData) {
  MitraCard {
    Text("Setoran terbaru", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = EcoColor.Green900)
    if (data.recentDeposits.isEmpty()) {
      Spacer(Modifier.height(4.dp))
      Text(text = "Belum ada setoran.", fontSize = 11.sp, color = EcoColor.Green700)
    } else {
      data.recentDeposits.forEach { deposit ->
        val date = formatServerTime(deposit.createdAt, "d MMM yyyy HH.mm")
        Spacer(Modifier.height(6.dp))
        Text(
          text = "${depositStatusLabel(deposit.status)} • ${deposit.totalWeightKg} kg • " +
            "${deposit.totalPoints} poin" + if (date != null) " • $date" else "",
          fontSize = 11.sp,
          lineHeight = 16.sp,
          color = EcoColor.Green800
        )
      }
    }
  }
}

private fun partnerStatusLabel(status: String): String = when (status) {
  "verified" -> "Terverifikasi"
  "pending" -> "Menunggu verifikasi"
  "rejected" -> "Ditolak"
  "suspended" -> "Ditangguhkan"
  else -> status
}

private fun depositStatusLabel(status: String): String = when (status) {
  "created" -> "Dibuat"
  "qr_issued" -> "QR diterbitkan"
  "claimed" -> "Diklaim"
  else -> status
}
