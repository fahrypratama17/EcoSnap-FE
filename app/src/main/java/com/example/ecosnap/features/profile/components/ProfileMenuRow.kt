package com.example.ecosnap.features.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.profile.model.ProfileMenuItemData

@Composable
fun ProfileMenuRow(
  item: ProfileMenuItemData,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .padding(vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(42.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(EcoColor.Green100),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = item.icon,
        contentDescription = item.title,
        tint = EcoColor.Green700,
        modifier = Modifier.size(18.dp)
      )
    }

    Spacer(Modifier.width(12.dp))

    Column(
      modifier = Modifier.weight(1f)
    ) {
      Text(
        text = item.title,
        style = MaterialTheme.typography.titleMedium.copy(
          fontSize = 14.sp
        ),
        color = EcoColor.Green900
      )

      Spacer(Modifier.height(2.dp))

      Text(
        text = item.subtitle,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontSize = 11.sp,
          lineHeight = 16.sp
        ),
        color = EcoColor.Green700
      )
    }

    Spacer(Modifier.width(8.dp))

    Icon(
      imageVector = Lucide.ChevronRight,
      contentDescription = null,
      tint = EcoColor.Green700,
      modifier = Modifier.size(18.dp)
    )
  }
}