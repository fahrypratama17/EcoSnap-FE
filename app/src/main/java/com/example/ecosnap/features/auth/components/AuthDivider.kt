package com.example.ecosnap.features.auth.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Leaf
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun AuthDivider(
  modifier : Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    HorizontalDivider(
      modifier = Modifier.weight(1f),
      color = EcoColor.Green400
    )

    Icon(
      imageVector = Lucide.Leaf,
      contentDescription = null,
      tint = EcoColor.Green600,
      modifier = Modifier.size(18.dp)
    )

    HorizontalDivider(
      modifier = Modifier.weight(1f),
      color = EcoColor.Green400
    )
  }
}