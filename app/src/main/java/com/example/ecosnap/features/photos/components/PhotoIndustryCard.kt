package com.example.ecosnap.features.photos.components

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Factory
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun ScanIndustryCard(
  description: String,
  modifier: Modifier = Modifier
) {
  EcoCard(modifier = modifier.fillMaxWidth()) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = Lucide.Factory,
        contentDescription = null,
        tint = EcoColor.Green800,
        modifier = Modifier.size(24.dp)
      )

      Spacer(Modifier.width(9.dp))

      Text(
        text = "Potensi Daur Ulang Industri",
        style = MaterialTheme.typography.titleMedium,
        color = EcoColor.Green900
      )
    }

    Spacer(Modifier.height(8.dp))

    Text(
      text = description,
      style = MaterialTheme.typography.bodyMedium.copy(
        fontSize = 12.sp,
        lineHeight = 18.sp
      ),
      color = EcoColor.Green700
    )
  }
}