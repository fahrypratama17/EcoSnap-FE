package com.example.ecosnap.features.profile.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronUp
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.profile.model.FaqItem

@Composable
fun FaqAccordionCard(
  item: FaqItem,
  expanded: Boolean,
  onToggle: () -> Unit,
  modifier: Modifier = Modifier
) {
  ProfileCardContainer(
    modifier = modifier
      .clickable { onToggle() },
    contentPadding = PaddingValues(14.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.Top
    ) {
      Text(
        text = item.question,
        modifier = Modifier.weight(1f),
        style = MaterialTheme.typography.titleMedium.copy(
          fontSize = 14.sp,
          lineHeight = 20.sp
        ),
        color = EcoColor.Green900
      )

      Spacer(Modifier.width(8.dp))

      Icon(
        imageVector = if (expanded) {
          Lucide.ChevronUp
        } else {
          Lucide.ChevronDown
        },
        contentDescription = null,
        tint = EcoColor.Green700,
        modifier = Modifier.size(18.dp)
      )
    }

    AnimatedVisibility(visible = expanded) {
      Column {
        Spacer(Modifier.height(12.dp))
        Text(
          text = item.answer,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 12.sp,
            lineHeight = 18.sp
          ),
          color = EcoColor.Green700
        )
      }
    }
  }
}