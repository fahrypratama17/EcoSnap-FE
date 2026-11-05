package com.example.ecosnap.features.leaderboard

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.components.EcoAppHeader
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.leaderboard.components.LeaderboardExplanationCard
import com.example.ecosnap.features.leaderboard.components.LeaderboardModeSelector
import com.example.ecosnap.features.leaderboard.components.LeaderboardMyRankCard
import com.example.ecosnap.features.leaderboard.components.LeaderboardPodiumCard
import com.example.ecosnap.features.leaderboard.components.LeaderboardRankingCard
import com.example.ecosnap.features.leaderboard.components.LeaderboardTipCard
import com.example.ecosnap.features.leaderboard.data.LocalLeaderboard
import com.example.ecosnap.features.leaderboard.model.LeaderboardMode

@Composable
fun LeaderboardScreen(
  initialMode: LeaderboardMode,
  onBack: () -> Unit,
  onNotifications: () -> Unit,
  onProfile: () -> Unit,
  modifier: Modifier = Modifier
) {
  var modeName by rememberSaveable(initialMode.name) {
    mutableStateOf(initialMode.name)
  }

  val selectedMode = LeaderboardMode.valueOf(
    modeName
  )

  var expanded by rememberSaveable(modeName) {
    mutableStateOf(false)
  }

  var showExplanation by rememberSaveable {
    mutableStateOf(true)
  }

  val ranking = remember(selectedMode) {
    LocalLeaderboard.getRanking(selectedMode)
  }

  val myRank = ranking.firstOrNull {
    it.entry.isCurrentUser
  }

  val remainingRanking = ranking.filter {
    it.rank > 3
  }

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
      contentPadding = PaddingValues(
        horizontal = 16.dp,
        vertical = 14.dp
      ),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        TextButton(
          onClick = onBack,
          contentPadding = PaddingValues(0.dp)
        ) {
          Icon(
            imageVector = Lucide.ArrowLeft,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )

          Spacer(Modifier.width(6.dp))

          Text(
            text = "Kembali",
            style = MaterialTheme.typography.labelSmall,
            color = EcoColor.Green700
          )
        }
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Leaderboard",
              style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 22.sp,
                lineHeight = 28.sp
              ),
              color = EcoColor.Green900
            )

            Spacer(Modifier.height(4.dp))

            Text(
              text = "Lihat para EcoHeroes yang paling aktif!",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                lineHeight = 18.sp
              ),
              color = EcoColor.Green700
            )
          }

          IconButton(
            onClick = { showExplanation = !showExplanation}
          ) {
            Surface(
              color = EcoColor.Green100,
              shape = CircleShape
            ) {
              Box(
                modifier = Modifier.size(34.dp),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Lucide.Info,
                  contentDescription = "Tampilkan penjelasan peringkat",
                  tint = EcoColor.Green800,
                  modifier = Modifier.size(19.dp)
                )
              }
            }
          }
        }
      }

      if (showExplanation) {
        item {
          LeaderboardExplanationCard(
            mode = selectedMode
          )
        }
      }

      item {
        LeaderboardModeSelector(
          selectedMode = selectedMode,
          onModeSelected = { newMode ->
            modeName = newMode.name
          }
        )
      }

      item {
        Text(
          text = "Peringkat EcoSnap",
          style = MaterialTheme.typography.titleMedium.copy(
            fontSize = 17.sp,
            lineHeight = 23.sp
          ),
          color = EcoColor.Green900
        )
      }

      item {
        LeaderboardPodiumCard(
          ranking = ranking,
          mode = selectedMode
        )
      }

      item {
        LeaderboardMyRankCard(
          myRank = myRank,
          mode = selectedMode
        )
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Daftar Peringkat",
            style = MaterialTheme.typography.titleMedium.copy(
              fontSize = 17.sp,
              lineHeight = 23.sp
            ),
            color = EcoColor.Green900,
            modifier = Modifier.weight(1f)
          )

          Text(
            text = "Skor tertinggi",
            style = MaterialTheme.typography.labelSmall,
            color = EcoColor.Green700
          )
        }
      }

      item {
        LeaderboardRankingCard(
          players = remainingRanking,
          mode = selectedMode,
          expanded = expanded,
          onExpandChange = { expanded = it }
        )
      }

      item {
        LeaderboardTipCard(
          mode = selectedMode
        )
      }

      item {
        Spacer(Modifier.height(4.dp))
      }
    }
  }
}