package com.example.ecosnap.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.profile.components.NotificationPreferenceGroup
import com.example.ecosnap.features.profile.components.ProfileBackTextButton
import com.example.ecosnap.features.profile.components.ProfilePageHeader
import com.example.ecosnap.features.profile.components.ProfileTopBar
import com.example.ecosnap.features.profile.model.ProfileDummyData

@Composable
fun AccountSettingsScreen(
  onBack: () -> Unit,
  onSettingsClick: () -> Unit,
  onProfileClick: () -> Unit,
  modifier: Modifier = Modifier
) {
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
          subtitle = "Atur preferensi yang sering digunakan."
        )
      }

      item {
        Text(
          text = "Preferensi Notifikasi",
          style = MaterialTheme.typography.titleMedium,
          color = EcoColor.Green900
        )
      }

      item {
        NotificationPreferenceGroup(
          items = ProfileDummyData.notificationPreferences
        )
      }
    }
  }
}