package com.example.ecosnap.features.profile.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.profile.model.ProfileMenuItemData

@Composable
fun ProfileMenuGroup(
  items: List<ProfileMenuItemData>,
  onItemClick: (ProfileMenuItemData) -> Unit,
  modifier: Modifier = Modifier
) {
  ProfileCardContainer(
    modifier = modifier,
    containerColor = Color.White,
    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
  ) {
    items.forEachIndexed { index, item ->
      ProfileMenuRow(
        item = item,
        onClick = { onItemClick(item) }
      )

      if (index < items.lastIndex) {
        HorizontalDivider(
          color = EcoColor.Green200
        )
      }
    }
  }
}