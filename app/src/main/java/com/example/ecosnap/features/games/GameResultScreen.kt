package com.example.ecosnap.features.games

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.RotateCcw
import com.composables.icons.lucide.Trophy
import com.composables.icons.lucide.Zap
import com.example.ecosnap.core.designsystem.components.EcoAppHeader
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.core.designsystem.theme.EcoShape
import com.example.ecosnap.features.games.model.GameResult
import com.example.ecosnap.features.games.model.MiniGame

@Composable
fun GameResultScreen(
  game: MiniGame,
  result: GameResult,
  onReplay: () -> Unit,
  onAllGames: () -> Unit,
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
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(170.dp)
              .background(
                EcoColor.Green200,
                CircleShape
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Lucide.Trophy,
              contentDescription = null,
              modifier = Modifier.size(92.dp),
              tint = EcoColor.Green800
            )
          }
        }
      }

      item {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "Kerja Bagus!",
            style = MaterialTheme.typography.headlineLarge,
            color = EcoColor.Green900
          )

          Spacer(Modifier.height(6.dp))

          Text(
            text = "Kamu telah menyelesaikan ${game.title}.",
            style = MaterialTheme.typography.bodyMedium,
            color = EcoColor.Green700
          )
        }
      }

      item {
        EcoCard(
          containerColor = EcoColor.Green800,
          borderColor = EcoColor.Green800,
          contentPadding = PaddingValues(16.dp)
        ) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "Skor Akhir",
              style = MaterialTheme.typography.bodyMedium,
              color = EcoColor.Green100
            )

            Spacer(Modifier.height(8.dp))

            Text(
              text = "${result.correctAnswers} / ${result.totalQuestions}",
              style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 36.sp,
                lineHeight = 44.sp,
              ),
              color = Color.White
            )
          }

          Spacer(Modifier.height(18.dp))

          HorizontalDivider(color = Color.White.copy(alpha = 0.3f))

          Spacer(Modifier.height(16.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Lucide.Zap,
              contentDescription = null,
              tint = EcoColor.Green100
            )

            Spacer(Modifier.width(8.dp))

            Text(
              text = "XP Diperoleh",
              style = MaterialTheme.typography.bodyMedium,
              color = EcoColor.Green100
            )

            Spacer(Modifier.weight(1f))

            Text(
              text = "+${result.earnedXp} XP",
              style = MaterialTheme.typography.titleMedium,
              color = Color.White
            )
          }
        }
      }

      item {
        EcoButton(
          text = "Main Lagi",
          onClick = onReplay,
          containerColor = EcoColor.Green700,
          contentColor = Color.White,
          endIcon = {
            Icon(
              imageVector = Lucide.RotateCcw,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
          }
        )
      }

      item {
        OutlinedButton(
          onClick = onAllGames,
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
          shape = EcoShape.large,
          border = BorderStroke(1.dp, EcoColor.Green700),
          colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = EcoColor.Green800
          )
        ) {
          Text("Kembali ke Semua Games")
        }
      }
    }
  }
}