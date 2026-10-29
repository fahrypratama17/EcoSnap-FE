package com.example.ecosnap.features.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lightbulb
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun HomeTipCard(
  title: String,
  description: String,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    color = EcoColor.Green300
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Lucide.Lightbulb,
        contentDescription = null,
        modifier = Modifier.size(40.dp),
        tint = EcoColor.Green800
      )

      Spacer(Modifier.width(14.dp))

      Column{
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium.copy(
            fontSize = 14.sp,
            lineHeight = 20.sp
          ),
          color = EcoColor.Green900
        )

        Text(
          text = description,
          fontSize = 11.sp,
          lineHeight = 16.sp,
          color = EcoColor.Green800
        )
      }
    }
  }
}