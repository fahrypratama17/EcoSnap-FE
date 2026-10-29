package com.example.ecosnap.features.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Trophy
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun HomeWeeklyMissionCard(
  title: String,
  description: String,
  progress: Int,
  target: Int,
  reward: Int,
  modifier: Modifier = Modifier
) {
  val safeTarget = target.coerceAtLeast(1)

  val franction = (progress.toFloat() / safeTarget).coerceIn(0f, 1f)

  EcoCard(
    modifier = modifier.fillMaxWidth()
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(52.dp)
          .background(
            EcoColor.Green100,
            RoundedCornerShape(12.dp)
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Lucide.Trophy,
          contentDescription = null,
          tint = EcoColor.Green900,
          modifier = Modifier.size(26.dp)
        )
      }

      Spacer(Modifier.width(10.dp))

      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium.copy(
            fontSize = 14.sp,
            lineHeight = 19.sp
          ),
          color = EcoColor.Green900,
          maxLines = 2
        )


        Spacer(Modifier.height(3.dp))

        Text(
          text = description,
          fontSize = 11.sp,
          lineHeight = 15.sp,
          color = EcoColor.Green700,
          maxLines = 2
        )
      }

      Spacer(Modifier.width(8.dp))

      Text(
        text = "+$reward",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = EcoColor.Green700
      )
    }

    Spacer(Modifier.height(12.dp))

    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Progress mingguan",
        fontSize = 13.sp,
        color = EcoColor.Green900
      )

      Spacer(Modifier.weight(1f))

      Text(
        text = "${progress.coerceAtLeast(0)}/$safeTarget",
        fontSize = 12.sp,
        color = EcoColor.Green700
      )
    }

    Spacer(Modifier.height(7.dp))

    LinearProgressIndicator(
      progress = { franction },
      modifier = Modifier
        .fillMaxWidth()
        .height(7.dp),
      color = EcoColor.Green600,
      trackColor = EcoColor.Green200,
      strokeCap = StrokeCap.Round
    )

    Spacer(Modifier.height(6.dp))
  }
}