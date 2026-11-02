package com.example.ecosnap.features.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun HomeSectionHeader(
  title: String,
  actionText: String,
  onAction: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium.copy(
        fontSize = 17.sp,
        lineHeight = 24.sp
      ),
      color = EcoColor.Green900
    )

    Row(
      modifier = Modifier
        .clickable(onClick = onAction)
        .padding(vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = actionText,
        fontSize = 12.sp,
        color = EcoColor.Green700
      )

      Icon(
        imageVector = Lucide.ArrowRight,
        contentDescription = null,
        modifier = Modifier.size(14.dp),
        tint =  EcoColor.Green700
      )
    }
  }
}