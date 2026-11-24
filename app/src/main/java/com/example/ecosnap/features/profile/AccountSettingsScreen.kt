package com.example.ecosnap.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.profile.components.ProfileBackTextButton
import com.example.ecosnap.features.profile.components.ProfileCardContainer
import com.example.ecosnap.features.profile.components.ProfilePageHeader
import com.example.ecosnap.features.profile.components.ProfileTopBar
import com.example.ecosnap.features.profile.components.SettingToggleRow

@Composable
fun AccountSettingsScreen(
  state: SettingsUiState,
  isUser: Boolean,
  onRetry: () -> Unit,
  onNotificationChange: (Boolean) -> Unit,
  onLeaderboardChange: (Boolean) -> Unit,
  onBack: () -> Unit,
  onSettingsClick: () -> Unit,
  onProfileClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val settings = state.settings
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
      item {
        ProfileBackTextButton(
          text = "Kembali ke Profil",
          onClick = onBack
        )
      }

      item {
        ProfilePageHeader(
          title = "Pengaturan Akun",
          subtitle = "Nilai di bawah ini tersimpan di akun EcoSnap-mu."
        )
      }

      if (state.loading) {
        item {
          Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = EcoColor.Green700)
          }
        }
      }

      state.loadError?.let { message ->
        item {
          Column {
            Text(message, color = EcoColor.TextError, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onRetry) { Text("Coba lagi", color = EcoColor.Green700) }
          }
        }
      }

      state.saveError?.let { message ->
        item {
          Text(message, color = EcoColor.TextError, style = MaterialTheme.typography.bodyMedium)
        }
      }

      if (settings != null) {
        item {
          Text(
            text = "Notifikasi",
            style = MaterialTheme.typography.titleMedium,
            color = EcoColor.Green900
          )
        }

        item {
          ProfileCardContainer(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
          ) {
            SettingToggleRow(
              title = "Notifikasi inbox",
              description = "Jika dimatikan, notifikasi baru tidak dibuat di inbox. Notifikasi lama tetap bisa dibaca. Tidak ada notifikasi push.",
              checked = settings.notificationEnabled,
              enabled = !state.saving,
              onCheckedChange = onNotificationChange
            )
          }
        }

        if (isUser) {
          item {
            Text(
              text = "Privasi",
              style = MaterialTheme.typography.titleMedium,
              color = EcoColor.Green900
            )
          }

          item {
            ProfileCardContainer(
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
            ) {
              SettingToggleRow(
                title = "Tampil di leaderboard",
                description = "Jika dimatikan, namamu tidak muncul di papan peringkat XP mingguan.",
                checked = settings.showOnLeaderboard,
                enabled = !state.saving,
                onCheckedChange = onLeaderboardChange
              )
            }
          }
        }
      }
    }
  }
}
