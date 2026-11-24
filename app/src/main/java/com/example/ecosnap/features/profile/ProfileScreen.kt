package com.example.ecosnap.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Leaf
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Medal
import com.composables.icons.lucide.Recycle
import com.example.ecosnap.BuildConfig
import com.example.ecosnap.core.auth.AccountProfile
import com.example.ecosnap.core.auth.AccountRole
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.profile.components.GreenAchievementCard
import com.example.ecosnap.features.profile.components.ProfileBackTextButton
import com.example.ecosnap.features.profile.components.ProfileLogoutButton
import com.example.ecosnap.features.profile.components.ProfileMenuGroup
import com.example.ecosnap.features.profile.components.ProfilePageHeader
import com.example.ecosnap.features.profile.components.ProfileSectionTitle
import com.example.ecosnap.features.profile.components.ProfileSummaryCard
import com.example.ecosnap.features.profile.components.ProfileTopBar
import com.example.ecosnap.features.profile.model.GreenStats
import com.example.ecosnap.features.profile.model.ProfileMenuItemData
import com.example.ecosnap.features.profile.model.ProfileMenus
import com.example.ecosnap.features.profile.model.ProfileStatItem
import com.example.ecosnap.features.profile.model.UserProfile
import java.text.NumberFormat
import java.util.Locale

private val NO_VALUE = "—"

@Composable
fun ProfileScreen(
  account: AccountProfile,
  state: ProfileUiState,
  onRetry: () -> Unit,
  onBack: (() -> Unit)?,
  onSettingsClick: () -> Unit,
  onProfileClick: () -> Unit,
  onEditProfileClick: () -> Unit,
  onMenuClick: (ProfileMenuItemData) -> Unit,
  onLogoutClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isPartner = account.role == AccountRole.PARTNER
  val profile = state.profile
  val activityMenus = ProfileMenus.activity(account.role)
  val supportMenus = ProfileMenus.support()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White)
  ) {
    ProfileTopBar(
      onSettingsClick = onSettingsClick,
      onProfileClick = onProfileClick
    )

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(
        horizontal = 16.dp,
        vertical = 14.dp
      ),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      if (onBack != null) {
        item {
          ProfileBackTextButton(
            text = "Kembali",
            onClick = onBack
          )
        }
      }

      item {
        ProfilePageHeader(
          title = "Profil Saya",
          subtitle = if (isPartner) "Kelola akun dan data usaha mitra." else "Kelola akun, lihat pencapaian hijaumu."
        )
      }

      if (state.profileLoading) {
        item {
          Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = EcoColor.Green700)
          }
        }
      }

      state.profileError?.let { message ->
        item {
          Column {
            Text(message, color = EcoColor.TextError, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onRetry) { Text("Coba lagi", color = EcoColor.Green700) }
          }
        }
      }

      item {
        ProfileSummaryCard(
          userProfile = UserProfile(
            name = profile?.fullName ?: account.fullName,
            email = profile?.email ?: account.email,
            detail = (profile?.business ?: account.business)?.let {
              "${it.name} • ${it.category} • ${businessStatusLabel(it.status)}"
            }
          ),
          onEditProfile = onEditProfileClick
        )
      }

      item {
        GreenAchievementCard(stats = statsFor(account.role, state))
      }

      if (state.statsFailed) {
        item {
          Column {
            Text(
              "Sebagian statistik belum dapat dimuat.",
              color = EcoColor.TextError,
              style = MaterialTheme.typography.bodyMedium
            )
            TextButton(onClick = onRetry) { Text("Muat ulang", color = EcoColor.Green700) }
          }
        }
      }

      item {
        ProfileSectionTitle(
          title = if (isPartner) "Aktivitas Mitra" else "Aktivitas Saya"
        )
      }

      item {
        ProfileMenuGroup(
          items = activityMenus,
          onItemClick = onMenuClick
        )
      }

      item {
        ProfileSectionTitle(
          title = "Pengaturan & Bantuan"
        )
      }

      item {
        ProfileMenuGroup(
          items = supportMenus,
          onItemClick = onMenuClick
        )
      }

      item {
        ProfileLogoutButton(
          onLogoutClick = onLogoutClick
        )
      }

      item {
        Box(
          modifier = Modifier.fillMaxWidth(),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "EcoSnap • Versi ${BuildConfig.VERSION_NAME}",
            color = EcoColor.Green700,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontSize = 11.sp
            )
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(8.dp))
      }
    }
  }
}

private fun statsFor(role: AccountRole, state: ProfileUiState): GreenStats {
  val stats = state.stats
  return when (role) {
    AccountRole.USER -> {
      val user = (stats as? ProfileStats.User)?.value
      GreenStats(
        title = "Pencapaian Hijauku",
        items = listOf(
          ProfileStatItem(state.profile?.walletBalance.format(), "EcoPoints", Lucide.Leaf),
          ProfileStatItem(user?.depositsClaimed.format(), "Setoran", Lucide.Recycle),
          ProfileStatItem(user?.badgesEarned.format(), "Lencana", Lucide.Medal)
        ),
        footer = "Setoran dihitung dari setoran yang sudah kamu klaim."
      )
    }
    AccountRole.PARTNER -> {
      val partner = (stats as? ProfileStats.Partner)?.value
      GreenStats(
        title = "Ringkasan Mitra",
        items = listOf(
          ProfileStatItem(partner?.claimedDeposits.format(), "Setoran Diklaim", Lucide.Recycle),
          ProfileStatItem(partner?.claimedPoints.format(), "Poin Tersalur", Lucide.Leaf),
          ProfileStatItem(partner?.qrIssued.format(), "QR Terbit", Lucide.Medal)
        ),
        footer = null
      )
    }
  }
}

private fun Long?.format(): String =
  this?.let { NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID")).format(it) } ?: NO_VALUE

internal fun businessStatusLabel(status: String): String = when (status) {
  "pending" -> "Menunggu verifikasi"
  "approved" -> "Terverifikasi"
  "rejected" -> "Ditolak"
  else -> status
}
