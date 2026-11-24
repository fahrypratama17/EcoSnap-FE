package com.example.ecosnap.features.photos.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor

/** Titled block of short server-provided lines; [warning] renders them in the error color. */
@Composable
fun PhotoNoticeCard(
  title: String,
  lines: List<String>,
  warning: Boolean,
  modifier: Modifier = Modifier
) {
  EcoCard(modifier = modifier.fillMaxWidth()) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      color = if (warning) EcoColor.TextError else EcoColor.Green900
    )

    lines.forEach { line ->
      Spacer(Modifier.height(6.dp))

      Text(
        text = line,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontSize = 12.sp,
          lineHeight = 18.sp
        ),
        color = if (warning) EcoColor.TextError else EcoColor.Green700
      )
    }
  }
}
