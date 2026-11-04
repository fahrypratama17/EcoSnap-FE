package com.example.ecosnap.features.games.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Medal
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun GamesLeaderboardCard(
  onViewLeaderboard: () -> Unit
) {
  EcoCard(modifier = Modifier.fillMaxWidth()) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(54.dp)
          .background(
            Color(0xFFF6C66F),
            RoundedCornerShape(12.dp)
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Lucide.Medal,
          contentDescription = null,
          tint = EcoColor.Green800,
          modifier = Modifier.size(28.dp)
        )
      }

      Spacer(Modifier.width(12.dp))

      Column{
        Text(
          text = "Jadilah Eco Champion!",
          style = MaterialTheme.typography.titleMedium,
          color = EcoColor.Green900
        )

        Spacer(Modifier.height(4.dp))

        Text(
          text = "Selesaikan misi dan kumpulkan skor untuk naik peringkat.",
          style = MaterialTheme.typography.bodyMedium,
          color = EcoColor.Green700
        )
      }
    }
  }
}