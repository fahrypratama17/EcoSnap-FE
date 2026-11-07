package com.example.ecosnap.features.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Leaf
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Medal
import com.composables.icons.lucide.Recycle
import com.composables.icons.lucide.Trophy
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.profile.model.GreenStats

@Composable
fun GreenAchievementCard(
  stats: GreenStats,
  modifier: Modifier = Modifier
) {
  ProfileCardContainer(
    modifier = modifier,
    containerColor = EcoColor.Green800
  ) {
    Text(
      text = "Pencapaian Hijauku",
      style = MaterialTheme.typography.titleMedium.copy(
        fontSize = 13.sp
      ),
      color = EcoColor.Green100
    )

    Spacer(Modifier.height(14.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      GreenStatItem(
        value = stats.ecoPoints,
        label = "EcoPoints",
        icon = Lucide.Leaf,
        modifier = Modifier.weight(1f)
      )
      GreenStatItem(
        value = stats.totalDeposits,
        label = "Setoran",
        icon = Lucide.Recycle,
        modifier = Modifier.weight(1f)
      )
      GreenStatItem(
        value = stats.totalBadges,
        label = "Lencana",
        icon = Lucide.Medal,
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(Modifier.height(14.dp))

    HorizontalDivider(
      color = EcoColor.Green700
    )

    Spacer(Modifier.height(10.dp))

    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Lucide.Trophy,
        contentDescription = null,
        tint = EcoColor.Green100,
        modifier = Modifier.size(14.dp)
      )

      Spacer(Modifier.width(6.dp))

      Text(
        text = "Terus lakukan aksi hijau dan tingkatkan pencapaianmu!",
        style = MaterialTheme.typography.bodyMedium.copy(
          fontSize = 11.sp,
          lineHeight = 16.sp
        ),
        color = EcoColor.Green100
      )
    }
  }
}

@Composable
private fun GreenStatItem(
  value: String,
  label: String,
  icon: ImageVector,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.Start
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = EcoColor.Green100,
      modifier = Modifier.size(14.dp)
    )

    Spacer(Modifier.height(8.dp))

    Text(
      text = value,
      style = MaterialTheme.typography.titleLarge.copy(
        fontSize = 26.sp,
        lineHeight = 30.sp
      ),
      color = Color.White
    )

    Spacer(Modifier.height(2.dp))

    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium.copy(
        fontSize = 11.sp
      ),
      color = EcoColor.Green100
    )
  }
}