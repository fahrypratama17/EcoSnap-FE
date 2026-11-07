package com.example.ecosnap.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ecosnap.features.profile.components.ProfileBackTextButton
import com.example.ecosnap.features.profile.components.ProfilePageHeader
import com.example.ecosnap.features.profile.components.ProfileTopBar

@Composable
fun ProfilePlaceholderScreen(
  title: String,
  subtitle: String,
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

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
      ProfileBackTextButton(
        text = "Kembali ke Profil",
        onClick = onBack
      )

      Spacer(Modifier.height(12.dp))

      ProfilePageHeader(
        title = title,
        subtitle = subtitle
      )

      Spacer(Modifier.height(24.dp))

      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Halaman ini masih placeholder.",
          style = MaterialTheme.typography.bodyLarge
        )
      }
    }
  }
}