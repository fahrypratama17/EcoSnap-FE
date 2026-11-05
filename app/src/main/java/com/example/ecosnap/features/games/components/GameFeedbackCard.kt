package com.example.ecosnap.features.games.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun GameFeedbackCard(
  isCorrect: Boolean,
  explanation: String,
  earnedXp: Int,
  modifier: Modifier = Modifier
) {
  val background = if (isCorrect) {
    EcoColor.Green200
  } else {
    Color(0xFFFFE9E5)
  }

  EcoCard(
    modifier = modifier.fillMaxWidth(),
    containerColor = background,
    borderColor = if (isCorrect) {
      EcoColor.Green600
    } else {
      Color(0xFFDFA299)
    },
    shadowElevation = 0.dp
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = if (isCorrect) {
          Lucide.Check
        } else {
          Lucide.X
        },
        contentDescription = null,
        tint = EcoColor.Green900
      )

      Spacer(Modifier.width(8.dp))

      Text(
        text = if (isCorrect) {
          "Jawaban Benar! +$earnedXp XP"
        } else {
          "Belum Tepat"
        },
        style = MaterialTheme.typography.titleMedium,
        color = EcoColor.Green900
      )
    }

    Spacer(Modifier.height(5.dp))

    Text(
      text = explanation,
      style = MaterialTheme.typography.bodyMedium,
      color = EcoColor.Green800
    )
  }
}