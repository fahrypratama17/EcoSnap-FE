package com.example.ecosnap.core.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.UserRound
import com.example.ecosnap.R
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.core.designsystem.theme.EcoGradient

@Composable
fun EcoAppHeader(
  onNotifications: () -> Unit,
  onProfile: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .background(EcoGradient.horizontal)
      .padding(
        horizontal = 16.dp,
        vertical = 12.dp,
      ),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Image(
      painter = painterResource(
        R.drawable.splash_screen_logo
      ),
      contentDescription = "EcoSnap",
      modifier = Modifier.size(28.dp)
    )

    Spacer(Modifier.width(8.dp))

    Text(
      text = "Eco",
      style = MaterialTheme.typography.titleLarge.copy(
        fontSize = 18.sp,
        lineHeight = 24.sp
      ),
      color = EcoColor.Green900
    )

    Text(
      text = "Snap",
      style = MaterialTheme.typography.titleLarge.copy(
        fontSize = 18.sp,
        lineHeight = 24.sp
      ),
      color = EcoColor.TextBrand
    )

    Spacer(Modifier.weight(1f))

    IconButton(
      onClick = onNotifications,
      modifier = Modifier
        .size(42.dp)
        .background(
          EcoColor.White,
          CircleShape
        )
    ) {
      Icon(
        imageVector = Lucide.Bell,
        contentDescription = "Notifikasi",
        tint = EcoColor.Green900
      )
    }

    Spacer(Modifier.width(8.dp))

    IconButton(
      onClick = onProfile,
      modifier = Modifier
        .size(42.dp)
        .background(
          EcoColor.Green300,
          CircleShape
        )
    ) {
      Icon(
        imageVector = Lucide.UserRound,
        contentDescription = "Profile",
        tint = EcoColor.Green900
      )
    }
  }
}