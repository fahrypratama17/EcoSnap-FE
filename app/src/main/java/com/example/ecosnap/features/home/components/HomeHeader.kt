package com.example.ecosnap.features.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.UserRound
import com.example.ecosnap.R
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun HomeHeader(
  onNotification: () -> Unit,
  onProfile: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(64.dp)
      .background(
        Brush.horizontalGradient(
          colors = listOf(
            EcoColor.Green200,
            EcoColor.GreenSoft,
            EcoColor.Green300
          )
        )
      )
      .padding(horizontal = 16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Image(
      painter = painterResource(
        R.drawable.splash_screen_logo
      ),
      contentDescription = "Logo EcoSnap",
      modifier = Modifier.size(28.dp)
    )

    Spacer(Modifier.width(6.dp))

    Text(
      text = "Snap",
      style = MaterialTheme.typography.titleLarge.copy(
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Bold
      ),
      color = EcoColor.TextBrand
    )

    Spacer(Modifier.weight(1f))

    IconButton(
      onClick = onNotification,
      modifier = Modifier
        .size(44.dp)
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
        .size(44.dp)
        .background(
          EcoColor.Green300,
          CircleShape
        )
    ) {
      Icon(
        imageVector = Lucide.UserRound,
        contentDescription = "Profil",
        tint = EcoColor.Green900
      )
    }
  }
}