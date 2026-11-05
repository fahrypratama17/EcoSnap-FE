package com.example.ecosnap.features.leaderboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun LeaderboardAvatar(
  initials: String,
  modifier: Modifier = Modifier,
  backgroundColor: Color = EcoColor.Green100,
  size: Dp = 44.dp
) {
  Box(
    modifier = modifier
      .size(size)
      .background(backgroundColor, CircleShape),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = initials,
      style = MaterialTheme.typography.titleMedium.copy(
        fontSize = 14.sp,
        lineHeight = 20.sp
      ),
      color = EcoColor.Green900
    )
  }
}