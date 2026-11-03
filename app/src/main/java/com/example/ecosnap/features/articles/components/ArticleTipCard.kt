package com.example.ecosnap.features.articles.components

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ScanLine
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun ArticleTipCard(
  title: String,
  description: String,
  modifier: Modifier = Modifier,
  actionText: String? = null,
  onClick: (() -> Unit)? = null,
) {
  val clickModifier = if (onClick != null) {
    modifier.clickable(onClick = onClick)
  } else {
    modifier
  }

  EcoCard(
    modifier = clickModifier.fillMaxWidth(),
    containerColor = EcoColor.Green300,
    borderColor = EcoColor.Green300,
    shadowElevation = 0.dp,
    contentPadding = PaddingValues(12.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = Lucide.ScanLine,
        contentDescription = null,
        tint = EcoColor.Green800,
        modifier = Modifier.size(22.dp)
      )

      Spacer(Modifier.width(8.dp))

      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
          fontSize = 14.sp,
          lineHeight = 20.sp
        ),
        color = EcoColor.Green900
      )
    }

    Spacer(Modifier.height(6.dp))

    Text(
      text = description,
      style = MaterialTheme.typography.bodyMedium.copy(
        fontSize = 12.sp,
        lineHeight = 18.sp,
      ),
      color = EcoColor.Green800
    )

    if (actionText != null) {
      Spacer(Modifier.height(8.dp))

      Text(
        text = actionText,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = EcoColor.Green900
      )
    }
  }
}