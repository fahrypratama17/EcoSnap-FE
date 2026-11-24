package com.example.ecosnap.features.leaderboard.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronUp
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.gamification.LeaderboardEntry

private const val COLLAPSED_COUNT = 4

@Composable
private fun LeaderboardRankRow(entry: LeaderboardEntry) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 11.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = "${entry.rank}",
      modifier = Modifier.width(36.dp),
      textAlign = TextAlign.Center,
      color = EcoColor.Green900,
      fontWeight = FontWeight.SemiBold,
      fontSize = 14.sp
    )
    Spacer(Modifier.width(8.dp))
    LeaderboardAvatar(initials = initialsOf(entry.displayName), size = 40.dp)
    Spacer(Modifier.width(10.dp))
    Text(
      text = entry.displayName,
      modifier = Modifier.weight(1f),
      style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp, lineHeight = 17.sp),
      color = EcoColor.Green900,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis
    )
    Spacer(Modifier.width(8.dp))
    Column(horizontalAlignment = Alignment.End) {
      Text(
        text = formatXp(entry.xp),
        style = MaterialTheme.typography.labelLarge,
        color = EcoColor.Green900
      )
      Text(text = "XP", fontSize = 10.sp, color = EcoColor.Green700)
    }
  }
}

/** Remaining server entries (after the podium). Renders nothing when there are none. */
@Composable
fun LeaderboardRankingCard(
  entries: List<LeaderboardEntry>,
  expanded: Boolean,
  onExpandChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  if (entries.isEmpty()) return
  val visible = if (expanded) entries else entries.take(COLLAPSED_COUNT)

  EcoCard(
    modifier = modifier.fillMaxWidth(),
    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
  ) {
    visible.forEachIndexed { index, entry ->
      if (index > 0) HorizontalDivider(color = EcoColor.Green200)
      LeaderboardRankRow(entry)
    }
    if (entries.size > COLLAPSED_COUNT) {
      HorizontalDivider(color = EcoColor.Green200)
      TextButton(onClick = { onExpandChange(!expanded) }, modifier = Modifier.fillMaxWidth()) {
        Text(
          text = if (expanded) "Tampilkan Lebih Sedikit" else "Lihat Peringkat Lainnya",
          color = EcoColor.Green900
        )
        Spacer(Modifier.width(5.dp))
        Icon(
          imageVector = if (expanded) Lucide.ChevronUp else Lucide.ChevronDown,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
          tint = EcoColor.Green900
        )
      }
    }
  }
}
