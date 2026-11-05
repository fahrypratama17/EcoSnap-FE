package com.example.ecosnap.features.games.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.CalendarDays
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Trophy
import com.composables.icons.lucide.Zap
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun WeeklyChallengeCard(
  progress: Int,
  target: Int,
  daysRemaining: Int,
  onViewMission: () -> Unit
) {
  val safeTarget = target.coerceAtLeast(1)
  val fraction = (progress.toFloat() / safeTarget).coerceIn(0f, 1f)

  EcoCard(
    modifier = Modifier.fillMaxWidth(),
    containerColor = EcoColor.Green800,
    borderColor = EcoColor.Green800,
    contentPadding = PaddingValues(14.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = Lucide.CalendarDays,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(19.dp)
      )

      Spacer(Modifier.width(6.dp))

      Text(
        text = "WEEKLY CHALLENGE",
        fontSize = 11.sp,
        color = Color.White,
        fontWeight = FontWeight.SemiBold
      )

      Spacer(Modifier.weight(1f))

      Surface(
        color = EcoColor.Green200,
        shape = RoundedCornerShape(50)
      ) {
        Text(
          text = "$daysRemaining hari tersisa",
          fontSize = 10.sp,
          color = EcoColor.Green900,
          modifier = Modifier.padding(
            horizontal = 9.dp,
            vertical = 5.dp
          )
        )
      }
    }

    Spacer(Modifier.height(17.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Eco Warrior Mission",
          style = MaterialTheme.typography.titleLarge.copy(
            fontSize = 18.sp,
            lineHeight = 24.sp,
          ),
          color = Color.White
        )

        Spacer(Modifier.height(5.dp))

        Text(
          text = "Setorkan 3 jenis sampah berbeda minggu ini.",
          style = MaterialTheme.typography.bodyMedium,
          color = EcoColor.Green100
        )
      }

      Box(
        modifier = Modifier
          .size(60.dp)
          .background(
            EcoColor.Green600,
            RoundedCornerShape(12.dp)
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Lucide.Trophy,
          contentDescription = null,
          modifier = Modifier.size(34.dp),
          tint = Color.White
        )
      }
    }

    Spacer(Modifier.height(16.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = "Progress tantangan",
        fontSize = 11.sp,
        color = EcoColor.Green100
      )

      Spacer(Modifier.weight(1f))

      Text(
        text = "$progress/$safeTarget",
        fontSize = 12.sp,
        color = Color.White
      )
    }

    Spacer(Modifier.height(7.dp))

    LinearProgressIndicator(
      progress = { fraction },
      modifier = Modifier
        .fillMaxWidth()
        .height(7.dp),
      color = EcoColor.Green400,
      trackColor = EcoColor.Green600,
      strokeCap = StrokeCap.Round
    )

    Spacer(Modifier.height(12.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = Lucide.Zap,
        contentDescription = null,
        tint = EcoColor.Green100,
        modifier = Modifier.size(16.dp)
      )

      Spacer(Modifier.width(5.dp))

      Text(
        text = "Hadiah: +50 EcoPoints",
        fontSize = 11.sp,
        color = EcoColor.Green100,
        modifier = Modifier.weight(1f)
      )

      Surface(
        onClick = onViewMission,
        color = EcoColor.Green100,
        shape = RoundedCornerShape(50)
      ) {
        Text(
          text = "Lihat Misi",
          fontSize = 12.sp,
          color = EcoColor.Green900,
          modifier = Modifier.padding(
            horizontal = 12.dp,
            vertical = 8.dp
          )
        )
      }
    }
  }
}