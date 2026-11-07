package com.example.ecosnap.features.profile.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.profile.model.NotificationPreferenceItem

@Composable
fun NotificationPreferenceGroup(
  items: List<NotificationPreferenceItem>,
  modifier: Modifier = Modifier
) {
  ProfileCardContainer(
    modifier = modifier,
    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
  ) {
    items.forEachIndexed { index, item ->
      NotificationPreferenceRow(item = item)

      if (index < items.lastIndex) {
        HorizontalDivider(
          modifier = Modifier.padding(vertical = 10.dp),
          color = EcoColor.Green200
        )
      }
    }
  }
}