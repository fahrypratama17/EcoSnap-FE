package com.example.ecosnap.features.leaderboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.leaderboard.model.RankedLeaderboardEntry
import java.text.NumberFormat
import java.util.Locale

private fun formatScore(value: Int): String =
  NumberFormat.getNumberInstance(
    Locale.forLanguageTag("id-ID")
  ).format(value)

@Composable
private fun PodiumPlace(
  player: RankedLeaderboardEntry,
  scoreUnit: String,
  pedestalHeight: Dp,
  pedestalColor: Color,
  avatarColor: Color,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    LeaderboardAvatar(
      initials = player.entry.initials,
      backgroundColor = avatarColor,
      size = 48.dp
    )

    Spacer(Modifier.height(7.dp))

    Text(
      text = player.entry.name,
      fontSize = 11.sp,
      lineHeight = 14.sp,
      fontWeight = FontWeight.SemiBold,
      color = Color.White,
      textAlign = TextAlign.Center,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.fillMaxWidth()
    )

    Text(
      text = "${formatScore(player.entry.score)} $scoreUnit",
      fontSize = 10.sp,
      lineHeight = 14.sp,
      color = EcoColor.Green100,
      textAlign = TextAlign.Center,
      maxLines = 1,
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(8.dp))

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(pedestalHeight)
        .background(
          pedestalColor,
          RoundedCornerShape(
            topStart = 12.dp,
            topEnd = 12.dp
          )
        ),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "#${player.rank}",
        style = MaterialTheme.typography.titleLarge.copy(
          fontSize = 21.sp,
          lineHeight = 27.sp
        ),
        color = EcoColor.Green900
      )
    }
  }
}
















