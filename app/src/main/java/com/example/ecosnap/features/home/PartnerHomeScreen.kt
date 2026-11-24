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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecosnap.core.auth.AccountProfile
import com.example.ecosnap.core.auth.BusinessProfile
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.home.model.PartnerHomeData

/**
 * Partner Home: business identity from the account plus the `GET /partner/home` summary. A 403
 * `deposit_forbidden` is shown as an explanation (pending / no location), never as zero counts.
 */
@Composable
fun PartnerHomeRoute(
  account: AccountProfile,
  onRefreshAccount: () -> Unit,
  onBrowsePartners: () -> Unit,
  onLogout: () -> Unit,
  onCreateDeposit: () -> Unit,
  onDeposits: () -> Unit,
  onNotifications: () -> Unit,
  onProfile: () -> Unit,
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
    onNotifications = onNotifications,
    onProfile = onProfile,
    onEstimate = onEstimate
  )
}

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
  onNotifications: () -> Unit,
  onProfile: () -> Unit,
  onEstimate: () -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Column {
        Text(text = "Selamat datang kembali!", fontSize = 12.sp, color = EcoColor.Green700)
        Spacer(Modifier.height(3.dp))
        Text(
          text = "Halo, ${account.displayName.ifBlank { account.fullName }}!",
          style = MaterialTheme.typography.titleLarge.copy(fontSize = 23.sp, lineHeight = 29.sp),
          color = EcoColor.Green900
        )
      }
    }

    account.business?.let { business ->
      item { BusinessCard(business) }
    }

    when {
      state.isLoading -> item {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
          CircularProgressIndicator(color = EcoColor.Green700)
        }
      }

      state.depositForbidden -> item {
        EcoCard(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Ringkasan setoran belum tersedia",
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
            color = EcoColor.Green900
          )
          Spacer(Modifier.height(4.dp))
          Text(
            text = "Akun mitra Anda belum memiliki lokasi terverifikasi, atau masih menunggu verifikasi. " +
              "Ringkasan akan muncul setelah akun diverifikasi. Gunakan tombol muat ulang akun untuk " +
              "memeriksa status terbaru.",
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = EcoColor.Green700
          )
        }
      }

      state.data != null -> item { SummaryCard(state.data) }

      else -> item {
        Column {
          Text(
            text = state.error ?: "Ringkasan mitra belum dapat dimuat.",
            style = MaterialTheme.typography.bodyMedium,
            color = EcoColor.TextError
          )
          Spacer(Modifier.height(6.dp))
          EcoButton(text = "Coba lagi", onClick = onRetry, height = 40.dp, modifier = Modifier.fillMaxWidth())
        }
      }
    }

    if (state.data != null && state.error != null) {
      item {
        Column {
          Text(text = state.error, style = MaterialTheme.typography.bodyMedium, color = EcoColor.TextError)
          Spacer(Modifier.height(6.dp))
          EcoButton(text = "Muat ulang ringkasan", onClick = onRetry, height = 40.dp)
        }
      }
    }

    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (state.data?.partnerStatus == "verified") {
          EcoButton(text = "Buat setoran", onClick = onCreateDeposit, modifier = Modifier.fillMaxWidth())
        }
        EcoButton(text = "Riwayat setoran", onClick = onDeposits, modifier = Modifier.fillMaxWidth())
        EcoButton(text = "Kalkulator estimasi", onClick = onEstimate, modifier = Modifier.fillMaxWidth())
        EcoButton(text = "Notifikasi", onClick = onNotifications, modifier = Modifier.fillMaxWidth())
        EcoButton(text = "Profil & pengaturan", onClick = onProfile, modifier = Modifier.fillMaxWidth())
        EcoButton(text = "Muat ulang akun", onClick = onRefreshAccount, modifier = Modifier.fillMaxWidth())
        EcoButton(text = "Telusuri mitra", onClick = onBrowsePartners, modifier = Modifier.fillMaxWidth())
        EcoButton(
          text = "Keluar",
          onClick = onLogout,
          modifier = Modifier.fillMaxWidth(),
          containerColor = EcoColor.Green100,
          contentColor = EcoColor.Green900
        )
      }
    }
  }
}

@Composable
private fun BusinessCard(business: BusinessProfile) {
  EcoCard(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = business.name,
      style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
      color = EcoColor.Green900
    )
    Text(text = business.category, fontSize = 12.sp, color = EcoColor.Green700)
    Spacer(Modifier.height(4.dp))
    Text(text = "Status akun: ${partnerStatusLabel(business.status)}", fontSize = 12.sp, color = EcoColor.Green800)
  }
}

@Composable
private fun SummaryCard(data: PartnerHomeData) {
  EcoCard(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = "Ringkasan mitra",
      style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
      color = EcoColor.Green900
    )
    Spacer(Modifier.height(4.dp))
    Text(
      text = "Status lokasi: ${partnerStatusLabel(data.partnerStatus)}",
      fontSize = 12.sp,
      color = EcoColor.Green800
    )
    if (data.partnerStatus != "verified") {
      Text(
        text = "Pembuatan setoran dan QR hanya tersedia untuk lokasi terverifikasi.",
        fontSize = 12.sp,
        color = EcoColor.Green700
      )
    }
    Spacer(Modifier.height(8.dp))
    SummaryLine("Setoran dibuat", data.created.toString())
    SummaryLine("QR diterbitkan", data.qrIssued.toString())
    SummaryLine("Setoran diklaim", data.claimed.toString())
    SummaryLine("Berat terklaim", "${data.claimedWeightKg} kg")
    SummaryLine("Poin terklaim", data.claimedPoints.toString())

    Spacer(Modifier.height(10.dp))
    Text(text = "Setoran terbaru", fontSize = 13.sp, color = EcoColor.Green900)
    if (data.recentDeposits.isEmpty()) {
      Text(text = "Belum ada setoran.", fontSize = 12.sp, color = EcoColor.Green700)
    } else {
      data.recentDeposits.forEach { deposit ->
        val date = formatServerTime(deposit.createdAt, "d MMM yyyy HH.mm")
        Spacer(Modifier.height(4.dp))
        Text(
          text = "${depositStatusLabel(deposit.status)} • ${deposit.totalWeightKg} kg • " +
            "${deposit.totalPoints} poin" + if (date != null) " • $date" else "",
          fontSize = 12.sp,
          lineHeight = 16.sp,
          color = EcoColor.Green700
        )
      }
    }
  }
}

@Composable
private fun SummaryLine(label: String, value: String) {
  Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    Text(text = label, fontSize = 12.sp, color = EcoColor.Green700, modifier = Modifier.weight(1f))
    Text(text = value, fontSize = 13.sp, color = EcoColor.Green900)
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
