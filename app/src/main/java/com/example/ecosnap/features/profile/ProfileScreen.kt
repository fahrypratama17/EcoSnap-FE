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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.profile.components.GreenAchievementCard
import com.example.ecosnap.features.profile.components.ProfileLogoutButton
import com.example.ecosnap.features.profile.components.ProfileMenuGroup
import com.example.ecosnap.features.profile.components.ProfilePageHeader
import com.example.ecosnap.features.profile.components.ProfileSectionTitle
import com.example.ecosnap.features.profile.components.ProfileSummaryCard
import com.example.ecosnap.features.profile.components.ProfileTopBar
import com.example.ecosnap.features.profile.model.ProfileDummyData
import com.example.ecosnap.features.profile.model.ProfileMenuItemData

@Composable
fun ProfileScreen(
  onSettingsClick: () -> Unit,
  onProfileClick: () -> Unit,
  onEditProfileClick: () -> Unit,
  onActivityMenuClick: (ProfileMenuItemData) -> Unit,
  onSupportMenuClick: (ProfileMenuItemData) -> Unit,
  onLogoutClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val activityMenus = ProfileDummyData.activityMenus
  val supportMenus = ProfileDummyData.supportMenus

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
        ProfilePageHeader(
          title = "Profil Saya",
          subtitle = "Kelola akun, lihat pencapaian hijaumu."
        )
      }

      item {
        ProfileSummaryCard(
          userProfile = ProfileDummyData.userProfile,
          onEditProfile = onEditProfileClick
        )
      }

      item {
        GreenAchievementCard(
          stats = ProfileDummyData.greenStats
        )
      }

      item {
        ProfileSectionTitle(
          title = "Aktivitas Saya"
        )
      }

      item {
        ProfileMenuGroup(
          items = activityMenus,
          onItemClick = onActivityMenuClick
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
          onItemClick = onSupportMenuClick
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
            text = "EcoSnap • Versi 1.0",
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