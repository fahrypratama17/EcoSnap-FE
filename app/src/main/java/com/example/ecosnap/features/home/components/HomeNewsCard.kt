package com.example.ecosnap.features.home.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Newspaper
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.core.designsystem.theme.EcoShape

@Composable
fun HomeNewsCard(
  title: String,
  summary: String,
  readingMinutes: Int,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
    shape = EcoShape.large,
    color = Color.White,
    border = BorderStroke(1.dp, EcoColor.Green300),
    shadowElevation = 2.dp
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(70.dp)
            .background(
              EcoColor.Green100,
              RoundedCornerShape(12.dp)
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Lucide.Newspaper,
            contentDescription = null,
            tint = EcoColor.Green800,
            modifier = Modifier.size(32.dp)
          )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "BERITA LINGKUNGAN",
            fontSize = 10.sp,
            color = EcoColor.Green700
          )

          Spacer(Modifier.height(4.dp))

          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
              fontSize = 14.sp,
              lineHeight = 19.sp,
            ),
            color = EcoColor.Green900,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )

          Spacer(Modifier.height(5.dp))

          Text(
            text = "$readingMinutes menit baca",
            fontSize = 11.sp,
            color = EcoColor.Green700
          )
        }
      }

      Spacer(Modifier.height(10.dp))

      Text(
        text = summary,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontSize = 12.sp,
          lineHeight = 18.sp
        ),
        color = EcoColor.Green700,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(Modifier.height(10.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "Baca selengkapnya",
          fontSize = 12.sp,
          color = EcoColor.Green700
        )

        Spacer(Modifier.width(5.dp))

        Icon(
          imageVector = Lucide.ArrowRight,
          contentDescription = null,
          tint = EcoColor.Green700,
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}