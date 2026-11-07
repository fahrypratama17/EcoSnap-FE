package com.example.ecosnap.features.photos.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Recycle
import com.composables.icons.lucide.TriangleAlert
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.photos.model.ReuseIdea

@Composable
fun PhotoReuseIdeaCard(
  idea: ReuseIdea,
  expanded: Boolean,
  onToggle: () -> Unit,
  modifier: Modifier = Modifier
) {
  EcoCard(modifier = modifier.fillMaxWidth()) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(62.dp)
          .background(
            EcoColor.Green100,
            RoundedCornerShape(12.dp)
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Lucide.Recycle,
          contentDescription = null,
          tint = EcoColor.Green800,
          modifier = Modifier.size(32.dp)
        )
      }

      Spacer(Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = idea.title,
          style = MaterialTheme.typography.titleMedium,
          color = EcoColor.Green900
        )

        Spacer(Modifier.height(4.dp))

        Text(
          text = idea.description,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 12.sp,
            lineHeight = 17.sp
          ),
          color = EcoColor.Green700
        )
      }
    }

    Spacer(Modifier.height(12.dp))

    EcoButton(
      text = if (expanded) {
        "Tutup Panduan"
      } else {
        "Lihat Cara Membuat"
      },
      onClick = onToggle,
      containerColor = EcoColor.Green700,
      contentColor = Color.White
    )

    if (expanded) {
      Spacer(Modifier.height(14.dp))

      HorizontalDivider(color = EcoColor.Green200)

      Spacer(Modifier.height(12.dp))

      Text(
        text = "Alat & Bahan",
        style = MaterialTheme.typography.titleMedium,
        color = EcoColor.Green900
      )

      Spacer(Modifier.height(6.dp))

      Text(
        text = idea.toolsAndMaterials.joinToString(", "),
        style = MaterialTheme.typography.bodyMedium,
        color = EcoColor.Green700
      )

      Spacer(Modifier.height(14.dp))

      Text(
        text = "Langkah Pembuatan",
        style = MaterialTheme.typography.titleMedium,
        color = EcoColor.Green900
      )

      Spacer(Modifier.height(8.dp))

      idea.steps.forEachIndexed { index, step ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          verticalAlignment = Alignment.Top
        ) {
          Text(
            text = "${index + 1}.",
            style = MaterialTheme.typography.bodyMedium,
            color = EcoColor.Green700
          )

          Spacer(Modifier.width(7.dp))

          Text(
            text = step,
            style = MaterialTheme.typography.bodyMedium,
            color = EcoColor.Green700,
            modifier = Modifier.weight(1f)
          )
        }
      }

      Spacer(Modifier.height(12.dp))

      Row(verticalAlignment = Alignment.Top) {
        Icon(
          imageVector = Lucide.TriangleAlert,
          contentDescription = null,
          tint = EcoColor.TextError,
          modifier = Modifier.size(18.dp)
        )

        Spacer(Modifier.width(8.dp))

        Text(
          text = "Gunakan alat tajam dengan hati-hati. Anak-anak perlu pendampingan orang dewasa.",
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 11.sp,
            lineHeight = 16.sp
          ),
          color = EcoColor.TextError
        )
      }
    }
  }
}