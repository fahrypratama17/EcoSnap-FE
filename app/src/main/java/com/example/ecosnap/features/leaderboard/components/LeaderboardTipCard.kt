package com.example.ecosnap.features.leaderboard.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Leaf
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.leaderboard.model.LeaderboardMode

@Composable
fun LeaderboardTipCard(
  mode: LeaderboardMode,
  modifier: Modifier = Modifier
) {
  EcoCard(
    modifier = modifier.fillMaxWidth(),
    containerColor = EcoColor.Green300,
    borderColor = EcoColor.Green300,
    shadowElevation = 0.dp
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = Lucide.Leaf,
        contentDescription = null,
        modifier = Modifier.size(20.dp),
        tint = EcoColor.Green800
      )

      Spacer(Modifier.width(8.dp))

      Text(
        text = "Setiap Aksi Hijau Berarti",
        style = MaterialTheme.typography.titleMedium.copy(
          fontSize = 14.sp,
          lineHeight = 20.sp
        ),
        color = EcoColor.Green900
      )
    }

    Spacer(Modifier.height(7.dp))

    Text(
      text = mode.tip,
      style = MaterialTheme.typography.bodyMedium.copy(
        fontSize = 12.sp,
        lineHeight = 18.sp
      ),
      color = EcoColor.Green800
    )
  }
}