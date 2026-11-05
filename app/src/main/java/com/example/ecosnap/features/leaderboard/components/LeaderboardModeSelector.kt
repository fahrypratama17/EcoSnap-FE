package com.example.ecosnap.features.leaderboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.leaderboard.model.LeaderboardMode

@Composable
fun LeaderboardModeSelector(
  selectedMode: LeaderboardMode,
  onModeSelected: (LeaderboardMode) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    LeaderboardMode.entries.forEach { mode ->
      val selected = mode == selectedMode

      Surface(
        onClick = {onModeSelected(mode)},
        shape = CircleShape,
        color = if (selected) {
          EcoColor.Green100
        } else {
          Color.White
        },
        border = BorderStroke(
          1.dp,
          if (selected) {
            EcoColor.Green700
          } else {
            EcoColor.Green400
          }
        )
      ) {
        Text(
          text = mode.label,
          modifier = Modifier.padding(
            horizontal = 14.dp,
            vertical = 8.dp
          ),
          fontSize = 12.sp,
          fontWeight = if (selected) {
            FontWeight.SemiBold
          } else {
            FontWeight.Normal
          },
          color = EcoColor.Green900
        )
      }
    }
  }
}