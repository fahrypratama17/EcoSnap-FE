package com.example.ecosnap.features.games.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Zap
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.games.model.MiniGame

@Composable
fun MiniGameCard(
  game: MiniGame,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  showDetails: Boolean = false
) {
  Surface(
    onClick = onClick,
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    color = Color.White,
    border = BorderStroke(1.dp, EcoColor.Green700),
    shadowElevation = 3.dp
  ) {
    Column(modifier = Modifier.padding(8.dp)) {
      GameArtwork(
        type = game.type,
        height = 112.dp
      )

      Spacer(Modifier.height(8.dp))

      Text(
        text = game.title,
        style = MaterialTheme.typography.titleMedium.copy(
          fontSize = 15.sp,
          lineHeight = 20.sp
        ),
        color = EcoColor.Green900
      )

      Spacer(Modifier.height(5.dp))

      Text(
        text = game.description,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontSize = 11.sp,
          lineHeight = 16.sp
        ),
        color = EcoColor.Green700,
        minLines = 2,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(Modifier.height(10.dp))

      if (showDetails) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "XP",
            fontSize = 11.sp,
            color = EcoColor.Green700
          )
          Text(
            text = "${game.totalQuestions} soal",
            fontSize = 11.sp,
            color = EcoColor.Green700
          )
        }

        Spacer(Modifier.height(5.dp))

        LinearProgressIndicator(
          progress = { 0f },
          modifier = Modifier
            .fillMaxWidth()
            .height(5.dp),
          color = EcoColor.Green600,
          trackColor = EcoColor.Green100
        )

        Spacer(Modifier.height(8.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              EcoColor.Green700,
              RoundedCornerShape(10.dp)
            )
            .padding(vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Lihat Detail",
            color = Color.White,
            fontSize = 12.sp
          )
        }
      } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Lucide.Zap,
            contentDescription = null,
            tint = EcoColor.Green700,
            modifier = Modifier.size(14.dp)
          )

          Spacer(Modifier.width(4.dp))

          Text(
            text = "Mainkan",
            color = EcoColor.Green700,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }
  }
}