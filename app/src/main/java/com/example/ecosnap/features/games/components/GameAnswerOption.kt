package com.example.ecosnap.features.games.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ListChecks
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.games.model.AnswerOptionState

@Composable
fun GameAnswerOption(
  text: String,
  state: AnswerOptionState,
  enabled: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val background = when (state) {
    AnswerOptionState.DEFAULT -> Color.White
    AnswerOptionState.CORRECT -> EcoColor.Green200
    AnswerOptionState.WRONG -> Color(0xFFFFEFEC)
  }

  val border = when (state) {
    AnswerOptionState.DEFAULT -> EcoColor.Green300
    AnswerOptionState.CORRECT -> EcoColor.Green800
    AnswerOptionState.WRONG -> Color(0xFFC57469)
  }

  Surface(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier.heightIn(min = 92.dp),
    shape = RoundedCornerShape(14.dp),
    color = background,
    border = BorderStroke(1.dp, border),
    shadowElevation = 2.dp
  ) {
    Column(
      modifier = Modifier.padding(10.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = when (state) {
          AnswerOptionState.CORRECT -> Lucide.Check
          AnswerOptionState.WRONG -> Lucide.X
          AnswerOptionState.DEFAULT -> Lucide.ListChecks
        },
        contentDescription = null,
        tint = EcoColor.Green800,
        modifier = Modifier.size(23.dp)
      )

      Spacer(Modifier.height(6.dp))

      Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = EcoColor.Green900,
        textAlign = TextAlign.Center,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}