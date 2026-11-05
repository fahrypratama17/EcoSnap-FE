package com.example.ecosnap.features.leaderboard.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronUp
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.leaderboard.model.LeaderboardMode
import com.example.ecosnap.features.leaderboard.model.RankedLeaderboardEntry

@Composable
private fun LeaderboardRankRow(
  player: RankedLeaderboardEntry,
  mode: LeaderboardMode
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 11.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = "${player.rank}",
      modifier = Modifier.width(28.dp),
      textAlign = TextAlign.Center,
      color = EcoColor.Green900,
      fontWeight = FontWeight.SemiBold,
      fontSize = 14.sp
    )

    Spacer(Modifier.width(8.dp))

    LeaderboardAvatar(
      initials = player.entry.initials,
      size = 40.dp
    )

    Spacer(Modifier.width(10.dp))

    Text(
      text = player.entry.name +
      if (player.entry.isCurrentUser) {
        " (Kamu)"
      } else {
        ""
      },
      modifier = Modifier.weight(1f),
      style = MaterialTheme.typography.labelLarge.copy(
        fontSize = 12.sp,
        lineHeight = 17.sp
      ),
      color = EcoColor.Green900,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis
    )

    Spacer(Modifier.width(8.dp))

    Column(horizontalAlignment = Alignment.End) {
      Text(
        text = formatScore(player.entry.score),
        style = MaterialTheme.typography.labelLarge,
        color = EcoColor.Green900
      )

      Text(
        text = mode.scoreUnit,
        fontSize = 10.sp,
        color = EcoColor.Green700
      )
    }
  }
}

@Composable
fun LeaderboardRankingCard(
  players: List<RankedLeaderboardEntry>,
  mode: LeaderboardMode,
  expanded: Boolean,
  onExpandChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  val visiblePlayers = if (expanded) {
    players
  } else {
    players.take(4)
  }

  EcoCard(
    modifier = modifier.fillMaxWidth(),
    contentPadding = PaddingValues(
      horizontal = 12.dp,
      vertical = 8.dp
    )
  ) {
    visiblePlayers.forEachIndexed { index, player ->
      if (index > 0) {
        HorizontalDivider(
          color = EcoColor.Green200
        )
      }

      LeaderboardRankRow(
        player = player,
        mode = mode
      )
    }

    if (players.isEmpty()) {
      Text(
        text = "Belum ada peringkat lainnya.",
        style = MaterialTheme.typography.bodyMedium,
        color = EcoColor.Green700,
        modifier = Modifier.padding(12.dp)
      )
    }

    if (players.size > 4) {
      HorizontalDivider(
        color = EcoColor.Green200
      )

      TextButton(
        onClick = { onExpandChange(!expanded) },
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = if (expanded) {
            "Tampilkan Lebih Sedikit"
          } else {
            "Lihat Peringkat Lainnya"
          },
          color = EcoColor.Green900
        )

        Spacer(Modifier.width(5.dp))

        Icon(
          imageVector = if (expanded) {
            Lucide.ChevronUp
          } else {
            Lucide.ChevronDown
          },
          contentDescription = null,
          modifier = Modifier.size(18.dp),
          tint = EcoColor.Green900
        )
      }
    }
  }
}