package com.example.ecosnap.features.leaderboard

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.components.EcoAppHeader
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.gamification.remainingPeriodLabel
import com.example.ecosnap.features.home.formatServerTime
import com.example.ecosnap.features.leaderboard.components.LeaderboardMyRankCard
import com.example.ecosnap.features.leaderboard.components.LeaderboardPodiumCard
import com.example.ecosnap.features.leaderboard.components.LeaderboardRankingCard

@Composable
fun LeaderboardScreen(
  state: LeaderboardUiState,
  onBack: () -> Unit,
  onRetry: () -> Unit,
  onNotifications: () -> Unit,
  onProfile: () -> Unit,
  modifier: Modifier = Modifier
) {
  var rankingExpanded by rememberSaveable { mutableStateOf(false) }
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White)
  ) {
    EcoAppHeader(onNotifications = onNotifications, onProfile = onProfile)

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onBack) {
        Icon(Lucide.ArrowLeft, contentDescription = "Kembali", tint = EcoColor.Green900)
      }
      Spacer(Modifier.width(4.dp))
      Text(
        text = "Leaderboard XP Mingguan",
        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
        color = EcoColor.Green900
      )
    }

    val board = state.board
    when {
      board == null && state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
        CircularProgressIndicator(color = EcoColor.Green700)
      }

      board == null -> Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = state.error ?: "Leaderboard belum dapat dimuat.",
            style = MaterialTheme.typography.bodyMedium,
            color = EcoColor.Green900,
            textAlign = TextAlign.Center
          )
          Spacer(Modifier.height(12.dp))
          EcoButton(text = "Coba lagi", onClick = onRetry, modifier = Modifier.fillMaxWidth())
        }
      }

      else -> LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        if (state.error != null) {
          item {
            Column {
              Text(state.error, style = MaterialTheme.typography.bodyMedium, color = EcoColor.TextError)
              Spacer(Modifier.height(6.dp))
              EcoButton(text = "Muat ulang", onClick = onRetry, height = 40.dp)
            }
          }
        }

        item {
          val remaining = remainingPeriodLabel(
            board.periodEnd,
            board.serverTime,
            SystemClock.elapsedRealtime() - state.loadedAtElapsedMs
          )
          val end = formatServerTime(board.periodEnd, "EEEE, d MMMM yyyy HH.mm")
          EcoCard(modifier = Modifier.fillMaxWidth(), containerColor = EcoColor.Green100) {
            Text(
              text = "Peringkat berdasarkan XP dari klaim misi minggu ini. Ini bukan EcoPoints, bukan skor game, dan tidak ada hadiah EcoPoints. Hanya pengguna yang bersedia tampil yang masuk daftar.",
              fontSize = 12.sp,
              color = EcoColor.Green800
            )
            if (end != null) {
              Spacer(Modifier.height(6.dp))
              Text(
                text = "Periode berakhir $end WIB" + (remaining?.let { " • $it" } ?: ""),
                fontSize = 12.sp,
                color = EcoColor.Green700
              )
            }
          }
        }

        if (board.entries.isNotEmpty()) {
          item { LeaderboardPodiumCard(topEntries = board.entries) }
        }

        item { LeaderboardMyRankCard(me = board.me) }

        if (board.entries.isEmpty()) {
          item {
            Text(
              text = "Belum ada peringkat minggu ini. Klaim hadiah misi untuk mendapatkan XP.",
              style = MaterialTheme.typography.bodyMedium,
              color = EcoColor.Green700
            )
          }
        } else if (board.entries.size > 3) {
          item {
            LeaderboardRankingCard(
              entries = board.entries.drop(3),
              expanded = rankingExpanded,
              onExpandChange = { rankingExpanded = it }
            )
          }
        }
      }
    }
  }
}
