package com.example.ecosnap.features.leaderboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

  }
}