package com.example.ecosnap.features.photos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
private fun PhotoHandlingStep(
  number: Int,
  description: String,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(26.dp)
        .background(
          EcoColor.Green100,
          CircleShape
        ),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = number.toString(),
        color = EcoColor.Green800,
        fontSize = 12.sp
      )
    }

    Spacer(Modifier.width(10.dp))

    Text(
      text = description,
      style = MaterialTheme.typography.bodyMedium.copy(
        fontSize = 12.sp,
        lineHeight = 18.sp
      ),
      color = EcoColor.Green700,
      modifier = Modifier.weight(1f)
    )
  }
}

@Composable
fun PhotoHandlingCard(
  steps: List<String>,
  modifier: Modifier = Modifier
) {
  EcoCard(modifier = modifier.fillMaxWidth()) {
    Text(
      text = "Cara Menangani Sampah",
      style = MaterialTheme.typography.titleMedium,
      color = EcoColor.Green900
    )

    Spacer(Modifier.height(12.dp))

    steps.forEachIndexed { index, step ->
      PhotoHandlingStep(
        number = index + 1,
        description = step
      )

      if (index < steps.lastIndex) {
        Spacer(Modifier.height(12.dp))
      }
    }
  }
}