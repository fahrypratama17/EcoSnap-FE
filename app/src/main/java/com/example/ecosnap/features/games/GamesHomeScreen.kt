package com.example.ecosnap.features.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ecosnap.core.designsystem.components.EcoAppHeader
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.games.components.GamesLeaderboardCard
import com.example.ecosnap.features.games.components.GamesSectionHeader
import com.example.ecosnap.features.games.components.MiniGameCard
import com.example.ecosnap.features.games.components.WeeklyChallengeCard
import com.example.ecosnap.features.games.data.LocalGames

@Composable
fun GamesHomeScreen(
  onAllGames: () -> Unit,
  onGameDetail: (String) -> Unit,
  onViewMission: () -> Unit,
  onLeaderboard: () -> Unit,
  onNotifications: () -> Unit,
  onProfile: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White)
  ) {
    EcoAppHeader(
      onNotifications = onNotifications,
      onProfile = onProfile
    )

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        Column {
          Text(
            text = "Games & Challenges",
            style = MaterialTheme.typography.headlineLarge,
            color = EcoColor.Green900
          )

          Spacer(Modifier.height(4.dp))

          Text(
            text = "Belajar, bermain, dan lakukan aksi hijau!",
            style = MaterialTheme.typography.bodyMedium,
            color = EcoColor.Green700,
          )
        }
      }

      item {
        WeeklyChallengeCard(
          progress = 1,
          target = 3,
          daysRemaining = 5,
          onViewMission = onViewMission
        )
      }

      item {
        GamesSectionHeader(
          title = "Mini Games",
          actionText = "Lihat Semua",
          onAction = onAllGames
        )
      }

      item {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          LocalGames.games.take(2).forEach { game ->
            MiniGameCard(
              game = game,
              onClick = { onGameDetail(game.id) },
              modifier = Modifier.weight(1f)
            )
          }
        }
      }

      item {
        GamesSectionHeader(
          title = "Leaderboard Mingguan"
        )
      }

      item {
        GamesLeaderboardCard(
          onViewLeaderboard = onLeaderboard
        )
      }
    }
  }
}