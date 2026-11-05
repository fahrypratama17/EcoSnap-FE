package com.example.ecosnap.features.leaderboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Trophy
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.leaderboard.model.LeaderboardMode
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

@Composable
fun LeaderboardPodiumCard(
  ranking: List<RankedLeaderboardEntry>,
  mode: LeaderboardMode,
  modifier: Modifier = Modifier
) {
  val topThree = ranking.take(3)

  EcoCard(
    modifier = modifier.fillMaxWidth(),
    containerColor = EcoColor.Green800,
    borderColor = EcoColor.Green800,
    contentPadding = PaddingValues(
      start = 12.dp,
      end = 12.dp,
      top = 16.dp,
      bottom = 0.dp
    )
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Lucide.Trophy,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(19.dp)
      )

      Spacer(Modifier.width(8.dp))

      Text(
        text = "Top 3 Eco Heroes",
        style = MaterialTheme.typography.titleMedium.copy(
          fontSize = 14.sp,
          lineHeight = 20.sp
        ),
        color = Color.White
      )
    }

    Spacer(Modifier.height(9.dp))

    Text(
      text = mode.podiumSubtitle,
      style = MaterialTheme.typography.bodyMedium.copy(
        fontSize = 11.sp,
        lineHeight = 16.sp,
      ),
      color = EcoColor.Green100,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(22.dp))

    if (topThree.size == 3) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        PodiumPlace(
          player = topThree[1],
          scoreUnit = mode.scoreUnit,
          pedestalHeight = 76.dp,
          pedestalColor = Color(0xFF75C9A5),
          avatarColor = Color(0xFFE7EBDC),
          modifier = Modifier.weight(1f)
        )

        PodiumPlace(
          player = topThree[0],
          scoreUnit = mode.scoreUnit,
          pedestalHeight = 115.dp,
          pedestalColor = Color(0xFFE6EAD9),
          avatarColor = Color(0xFFFFD88E),
          modifier = Modifier.weight(1f)
        )

        PodiumPlace(
          player = topThree[2],
          scoreUnit = mode.scoreUnit,
          pedestalHeight = 66.dp,
          pedestalColor = Color(0xFF95D5B2),
          avatarColor = Color(0xFF52B788),
          modifier = Modifier.weight(1f)
        )
      }
    } else {
      Text(
        text = "Belum cukup data untuk Top 3.",
        color = Color.White,
        modifier = Modifier.padding(bottom = 20.dp)
      )
    }
  }
}