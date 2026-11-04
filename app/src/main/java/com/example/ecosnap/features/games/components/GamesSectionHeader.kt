package com.example.ecosnap.features.games.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun GameSectionHeader(
  title: String,
  actionText: String? = null,
  onAction: (() -> Unit)? = null
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      color = EcoColor.Green900,
      modifier = Modifier.weight(1f)
    )

    if (actionText != null && onAction != null) {
      Text(
        text = "$actionText →",
        modifier = Modifier
          .clickable(onClick = onAction)
          .padding(6.dp),
        fontSize = 12.sp,
        color = EcoColor.Green700
      )
    }
  }
}