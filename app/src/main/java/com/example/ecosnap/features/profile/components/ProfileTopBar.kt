package com.example.ecosnap.features.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.User
import com.example.ecosnap.R
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun ProfileTopBar(
  onSettingsClick: () -> Unit,
  onProfileClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .background(EcoColor.Green200)
      .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        painter = painterResource(R.drawable.splash_screen_logo),
        contentDescription = "EcoSnap Logo",
        tint = Color.Unspecified,
        modifier = Modifier.size(28.dp)
      )

      Spacer(Modifier.width(8.dp))

      Text(
        text = "EcoSnap",
        style = MaterialTheme.typography.titleLarge.copy(
          fontSize = 18.sp,
          lineHeight = 24.sp
        ),
        color = EcoColor.Green900
      )
    }

    Spacer(Modifier.weight(1f))

    ProfileCircleActionButton(
      icon = Lucide.Settings,
      contentDescription = "Pengaturan",
      onClick = onSettingsClick
    )

    Spacer(Modifier.width(10.dp))

    ProfileCircleActionButton(
      icon = Lucide.User,
      contentDescription = "Profile",
      onClick = onProfileClick
    )
  }
}

@Composable
private fun ProfileCircleActionButton(
  icon: ImageVector,
  contentDescription: String,
  onClick: () -> Unit
) {
  Surface(
    onClick = onClick,
    shape = CircleShape,
    color = EcoColor.Green100
  ) {
    Box(
      modifier = Modifier.size(36.dp),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = EcoColor.Green800,
        modifier = Modifier.size(18.dp)
      )
    }
  }
}