package com.example.ecosnap.features.leaderboard.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Medal
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.leaderboard.model.LeaderboardMode
import com.example.ecosnap.features.leaderboard.model.RankedLeaderboardEntry

@Composable
fun LeaderboardMyRankCard(
  myRank: RankedLeaderboardEntry?,
  mode: LeaderboardMode,
  modifier: Modifier = Modifier
) {
  EcoCard(
    modifier = modifier.fillMaxWidth(),
    contentPadding = PaddingValues(14.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = Lucide.Medal,
        contentDescription = null,
        tint = EcoColor.Green700,
        modifier = Modifier.size(17.dp)
      )

      Spacer(Modifier.width(7.dp))

      Text(
        text = "Peringkat Kamu",
        style = MaterialTheme.typography.titleMedium.copy(
          fontSize = 14.sp,
          lineHeight = 20.sp
        ),
        color = EcoColor.Green900
      )
    }

    Spacer(Modifier.height(12.dp))

    if (myRank != null) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.width(78.dp)) {
          Text(
            text = "#${myRank.rank}",
            style = MaterialTheme.typography.headlineLarge.copy(
              fontSize = 30.sp,
              lineHeight = 36.sp
            ),
            color = EcoColor.Green900
          )

          Text(
            text = "Peringkat",
            fontSize = 11.sp,
            color = EcoColor.Green700
          )
        }

        VerticalDivider(
          modifier = Modifier.height(72.dp),
          color = EcoColor.Green200
        )

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Kamu (${myRank.entry.name})",
            style = MaterialTheme.typography.labelLarge,
            color = EcoColor.Green900
          )

          Spacer(Modifier.height(6.dp))

          Text(
            text = "${formatScore(myRank.entry.score)} " + mode.scoreUnit,
            style = MaterialTheme.typography.titleMedium.copy(
              lineHeight = 22.sp
            ),
            color = EcoColor.Green900
          )

          Spacer(Modifier.height(5.dp))

          Text(
            text = "Terus kumpulkan skor untuk naik peringkat",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontSize = 11.sp,
              lineHeight = 16.sp
            ),
            color = EcoColor.Green700
          )
        }
      }
    } else {
      Text(
        text = "Kamu belum masuk daftar peringkat.",
        color = EcoColor.Green700,
        style = MaterialTheme.typography.bodyMedium
      )
    }
  }
}