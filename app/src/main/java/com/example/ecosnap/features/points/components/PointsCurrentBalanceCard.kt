package com.example.ecosnap.features.points.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Leaf
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Sprout
import com.example.ecosnap.features.points.data.LocalPointsData

@Composable
fun PointsCurrentBalanceCard(
  points: Int,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = Color(0xFF254031),
    shadowElevation = 4.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(
          horizontal = 16.dp,
          vertical = 14.dp
        )
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = CircleShape,
          color = Color(0xFF537D66),
          modifier = Modifier.size(52.dp)
        ) {
          Box(
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Lucide.Sprout,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(28.dp)
            )
          }
        }

        Spacer(Modifier.width(14.dp))

        Column {
          Text(
            text = "Poinmu saat ini",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontSize = 11.sp,
              lineHeight = 14.sp,
              fontWeight = FontWeight.Normal,
              letterSpacing = 0.sp
            ),
            color = Color.White.copy(alpha = 0.9f)
          )

          Spacer(Modifier.height(2.dp))

          Row(
            verticalAlignment = Alignment.Bottom
          ) {
            Text(
              text = LocalPointsData.formatPoints(points),
              style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 27.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp
              ),
              color = Color.White
            )

            Spacer(Modifier.width(5.dp))

            Text(
              text = "poin",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.sp
              ),
              color = Color.White,
              modifier = Modifier.padding(bottom = 3.dp)
            )
          }
        }
      }

      Spacer(Modifier.height(12.dp))

      HorizontalDivider(
        thickness = 1.dp,
        color = Color.White.copy(alpha = 0.45f)
      )

      Spacer(Modifier.height(10.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Lucide.Leaf,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(14.dp)
        )

        Spacer(Modifier.width(6.dp))

        Text(
          text = "Terus kumpulkan poin untuk bumi yang lebih bersih!",
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp
          ),
          color = Color.White
        )
      }
    }
  }
}
