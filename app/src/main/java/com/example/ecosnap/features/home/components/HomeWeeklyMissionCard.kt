package com.example.ecosnap.features.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import com.example.ecosnap.features.home.model.Mission
import java.util.Locale

/** Progress text in the mission's own unit: `gram` is shown as kg, `deposit` as a count. */
internal fun Mission.progressLabel(): String = when (unit) {
  "gram" -> "${kilograms(progress)}/${kilograms(target)} kg"
  "deposit" -> "$progress/$target setoran"
  else -> "$progress/$target"
}

private fun kilograms(grams: Long): String {
  val kg = grams / 1000.0
  return if (grams % 1000L == 0L) (grams / 1000L).toString() else String.format(Locale.forLanguageTag("id-ID"), "%.1f", kg)
}

internal fun Mission.statusLabel(): String = when (status) {
  Mission.STATUS_CLAIMABLE -> "Siap diklaim"
  Mission.STATUS_CLAIMED -> "Sudah diklaim"
  else -> "Sedang berjalan"
}

@Composable
fun HomeWeeklyMissionCard(
  mission: Mission,
  modifier: Modifier = Modifier,
  footer: (@Composable ColumnScope.() -> Unit)? = null
) {
  val franction = if (mission.target > 0) {
    (mission.progress.toFloat() / mission.target).coerceIn(0f, 1f)
  } else {
    0f
  }

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
          text = mission.title,
          style = MaterialTheme.typography.titleMedium.copy(
            fontSize = 14.sp,
            lineHeight = 19.sp
          ),
          color = EcoColor.Green900,
          maxLines = 2
        )

        Spacer(Modifier.height(3.dp))

        Text(
          text = mission.description,
          fontSize = 11.sp,
          lineHeight = 15.sp,
          color = EcoColor.Green700,
          maxLines = 3
        )
      }

      Spacer(Modifier.width(8.dp))

      Text(
        text = "+${mission.rewardXp} XP",
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
        text = mission.statusLabel(),
        fontSize = 13.sp,
        color = EcoColor.Green900
      )

      Spacer(Modifier.weight(1f))

      Text(
        text = mission.progressLabel(),
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

    footer?.invoke(this)
  }
}
