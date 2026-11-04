package com.example.ecosnap.features.games

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.ListChecks
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Trophy
import com.composables.icons.lucide.Zap
import com.example.ecosnap.core.designsystem.components.EcoAppHeader
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.games.components.GameArtwork
import com.example.ecosnap.features.games.model.MiniGame

@Composable
fun GameDetailScreen(
  game: MiniGame,
  onBack: () -> Unit,
  onStartGame: () -> Unit,
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
        TextButton(
          onClick = onBack,
          contentPadding = PaddingValues(0.dp)
        ) {
          Icon(
            imageVector = Lucide.ArrowLeft,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(Modifier.width(5.dp))
          Text("Detail Games")
        }
      }

      item {
        GameArtwork(
          type = game.type,
          height = 160.dp
        )
      }

      item {
        Text(
          text = game.title,
          style = MaterialTheme.typography.titleLarge,
          color = EcoColor.Green900
        )
      }

      item {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          GameStatisticCard(
            icon = Lucide.ListChecks,
            value = "${game.totalQuestions}",
            label = "Soal",
            modifier = Modifier.weight(1f)
          )

          GameStatisticCard(
            icon = Lucide.Zap,
            value = "${game.xpPerCorrect} XP",
            label = "Per benar",
            modifier = Modifier.weight(1f)
          )

          GameStatisticCard(
            icon = Lucide.BookOpen,
            value = game.difficulty,
            label = "Kesulitan",
            modifier = Modifier.weight(1f)
          )
        }
      }

      item {
        EcoCard {
          Text(
            text = "Tentang Game",
            style = MaterialTheme.typography.titleMedium,
            color = EcoColor.Green900
          )

          Spacer(Modifier.height(8.dp))

          Text(
            text = game.about,
            style = MaterialTheme.typography.bodyMedium,
            color = EcoColor.Green700
          )
        }
      }

      item {
        EcoCard {
          Text(
            text = "Cara Bermain",
            style = MaterialTheme.typography.titleMedium,
            color = EcoColor.Green900
          )

          Spacer(Modifier.height(14.dp))

          game.instructions.forEachIndexed { index, step ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                color = EcoColor.Green100,
                shape = androidx.compose.foundation.shape.CircleShape
              ) {
                Box(
                  modifier = Modifier.size(28.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "${index + 1}",
                    color = EcoColor.Green800,
                    style = MaterialTheme.typography.labelLarge
                  )
                }
              }

              Spacer(Modifier.width(10.dp))

              Text(
                text = step,
                style = MaterialTheme.typography.bodyMedium,
                color = EcoColor.Green700,
                modifier = Modifier.weight(1f)
              )
            }
          }
        }
      }

      item {
        EcoCard(
          containerColor = EcoColor.Green100,
          borderColor = EcoColor.Green100
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Lucide.Trophy,
              contentDescription = null,
              tint = EcoColor.Green800,
              modifier = Modifier.size(28.dp)
            )

            Spacer(Modifier.width(12.dp))

            Column {
              Text(
                text = "Hadiah XP",
                style = MaterialTheme.typography.titleMedium,
                color = EcoColor.Green900
              )

              Text(
                text = "Maksimal ${game.maximumXp} XP dalam mini game ini",
                style = MaterialTheme.typography.bodyMedium,
                color = EcoColor.Green700
              )
            }
          }
        }
      }

      item {
        EcoButton(
          text = "Mulai Bermain",
          onClick = onStartGame,
          containerColor = EcoColor.Green700,
          contentColor = Color.White,
          endIcon = {
            Icon(
              imageVector = Lucide.ArrowRight,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
          }
        )
      }
    }
  }
}

@Composable
private fun GameStatisticCard(
  icon: ImageVector,
  value: String,
  label: String,
  modifier: Modifier = Modifier
) {
  EcoCard(
    modifier = modifier,
    contentPadding = PaddingValues(9.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = EcoColor.Green700,
        modifier = Modifier.size(24.dp)
      )

      Spacer(Modifier.height(7.dp))

      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium,
        color = EcoColor.Green900,
        maxLines = 1
      )

      Text(
        text = label,
        fontSize = 11.sp,
        color = EcoColor.Green700
      )
    }
  }
}