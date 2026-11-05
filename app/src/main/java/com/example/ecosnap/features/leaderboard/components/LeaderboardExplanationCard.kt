package com.example.ecosnap.features.leaderboard.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.leaderboard.model.LeaderboardMode

@Composable
fun LeaderboardExplanationCard(
  mode: LeaderboardMode,
  modifier: Modifier = Modifier
) {
  EcoCard(modifier = modifier.fillMaxWidth()) {
    Text(
      text = "Bagaimana peringkat dihitung?",
      style = MaterialTheme.typography.titleSmall.copy(
        fontSize = 14.sp,
        lineHeight = 20.sp
      ),
      color = EcoColor.Green900
    )

    Spacer(Modifier.height(8.dp))

    Text(
      text = mode.explanation,
      style = MaterialTheme.typography.bodyMedium.copy(
        fontSize = 12.sp,
        lineHeight = 18.sp
      ),
      color = EcoColor.Green700
    )
  }
}