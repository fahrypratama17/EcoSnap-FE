package com.example.ecosnap.features.games

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecosnap.core.designsystem.components.EcoAppHeader
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.games.components.GamesSectionHeader
import com.example.ecosnap.features.games.components.MiniGameCard
import com.example.ecosnap.features.games.data.LocalGames
import com.example.ecosnap.features.gamification.remainingPeriodLabel
import com.example.ecosnap.features.home.components.HomeWeeklyMissionCard

@Composable
fun GamesHomeScreen(
  state: GamesUiState,
  onRetry: () -> Unit,
  onAllGames: () -> Unit,
  onGameDetail: (String) -> Unit,
  onViewMission: () -> Unit,
  onBadges: () -> Unit,
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
            text = "Misi, badge, dan XP berasal dari server. Mini game di bawah hanya latihan offline.",
            style = MaterialTheme.typography.bodyMedium,
            color = EcoColor.Green700,
          )
        }
      }

      item { GamesSectionHeader(title = "Misi & XP Mingguan") }

      item {
        val missions = state.missions
        when {
          missions == null && state.isLoading -> Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            contentAlignment = Alignment.Center
          ) { CircularProgressIndicator(color = EcoColor.Green700) }

          missions == null -> ErrorBlock(
            message = state.missionsError ?: "Misi belum dapat dimuat.",
            onRetry = onRetry
          )

          else -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            EcoCard(modifier = Modifier.fillMaxWidth(), containerColor = EcoColor.Green100) {
              Text(
                text = "XP minggu ini: ${missions.weeklyXp} • Total XP: ${missions.totalXp}",
                style = MaterialTheme.typography.titleMedium,
                color = EcoColor.Green900
              )
              val remaining = remainingPeriodLabel(
                missions.periodEnd,
                missions.serverTime,
                SystemClock.elapsedRealtime() - state.missionsLoadedAtElapsedMs
              )
              if (remaining != null) {
                Text(text = "Periode misi: $remaining", fontSize = 12.sp, color = EcoColor.Green700)
              }
              Text(
                text = "XP terpisah dari EcoPoints dan hanya berasal dari klaim hadiah misi.",
                fontSize = 12.sp,
                color = EcoColor.Green700
              )
            }

            val featured = missions.featured()
            if (featured != null) {
              HomeWeeklyMissionCard(mission = featured)
            } else {
              Text(
                text = "Belum ada misi minggu ini.",
                style = MaterialTheme.typography.bodyMedium,
                color = EcoColor.Green700
              )
            }

            if (state.missionsError != null) {
              Text(state.missionsError, fontSize = 12.sp, color = EcoColor.TextError)
            }

            EcoButton(
              text = "Lihat Semua Misi",
              onClick = onViewMission,
              height = 44.dp,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }

      item { GamesSectionHeader(title = "Badge") }

      item {
        val badges = state.badges
        when {
          badges == null && state.isLoading -> Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            contentAlignment = Alignment.Center
          ) { CircularProgressIndicator(color = EcoColor.Green700) }

          badges == null -> ErrorBlock(
            message = state.badgesError ?: "Badge belum dapat dimuat.",
            onRetry = onRetry
          )

          else -> EcoCard(modifier = Modifier.fillMaxWidth()) {
            Text(
              text = "${badges.earnedCount} dari ${badges.badges.size} badge diraih",
              style = MaterialTheme.typography.titleMedium,
              color = EcoColor.Green900
            )
            Text(
              text = "Badge dihitung server dari setoran yang sudah diklaim.",
              fontSize = 12.sp,
              color = EcoColor.Green700
            )
            if (state.badgesError != null) {
              Text(state.badgesError, fontSize = 12.sp, color = EcoColor.TextError)
            }
            Spacer(Modifier.height(8.dp))
            EcoButton(
              text = "Lihat Badge",
              onClick = onBadges,
              height = 44.dp,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }

      item { GamesSectionHeader(title = "Leaderboard XP Mingguan") }

      item {
        EcoCard(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Peringkat pengguna berdasarkan XP misi minggu ini (bukan EcoPoints atau skor game).",
            fontSize = 12.sp,
            color = EcoColor.Green700
          )
          Spacer(Modifier.height(8.dp))
          EcoButton(
            text = "Lihat Leaderboard",
            onClick = onLeaderboard,
            height = 44.dp,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }

      item {
        GamesSectionHeader(
          title = "Mini Games (latihan offline)",
          actionText = "Lihat Semua",
          onAction = onAllGames
        )
      }

      item {
        Text(
          text = "Berjalan di perangkat tanpa server: tidak memberi XP, EcoPoints, badge, atau peringkat.",
          fontSize = 12.sp,
          color = EcoColor.Green700
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
    }
  }
}

@Composable
private fun ErrorBlock(message: String, onRetry: () -> Unit) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(text = message, style = MaterialTheme.typography.bodyMedium, color = EcoColor.TextError)
    Spacer(Modifier.height(6.dp))
    EcoButton(text = "Coba lagi", onClick = onRetry, height = 40.dp)
  }
}
