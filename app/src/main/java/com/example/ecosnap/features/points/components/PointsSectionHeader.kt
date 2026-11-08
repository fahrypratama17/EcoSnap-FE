package com.example.ecosnap.features.points.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun PointsSectionHeader(
  title: String,
  onSeeAll: () -> Unit,
  modifier: Modifier = Modifier,
  actionText: String = "Lihat Semua"
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium.copy(
        fontSize = 15.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
      ),
      color = EcoColor.Green900
    )

    Row(
      modifier = Modifier
        .clickable(onClick = onSeeAll)
        .padding(vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = actionText,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontSize = 11.sp,
          lineHeight = 14.sp,
          fontWeight = FontWeight.Normal,
          letterSpacing = 0.sp
        ),
        color = EcoColor.Green700
      )

      Spacer(Modifier.width(3.dp))

      Icon(
        imageVector = Lucide.ArrowRight,
        contentDescription = null,
        tint = EcoColor.Green700,
        modifier = Modifier.size(12.dp)
      )
    }
  }
}
