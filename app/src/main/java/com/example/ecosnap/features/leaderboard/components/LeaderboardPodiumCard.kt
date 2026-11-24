package com.example.ecosnap.features.leaderboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Trophy
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.gamification.LeaderboardEntry
import java.text.NumberFormat
import java.util.Locale

internal fun formatXp(value: Long): String =
  NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).format(value)

internal fun initialsOf(name: String): String {
  val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
  if (parts.isEmpty()) return "?"
  val letters = parts.take(2).mapNotNull {
    it.codePoints().findFirst().let { cp -> if (cp.isPresent) String(Character.toChars(cp.asInt)) else null }
  }
  return letters.joinToString("").uppercase()
}

@Composable
private fun PodiumPlace(
  entry: LeaderboardEntry,
  pedestalHeight: Dp,
  pedestalColor: Color,
  avatarColor: Color,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
    LeaderboardAvatar(initials = initialsOf(entry.displayName), backgroundColor = avatarColor, size = 48.dp)
    Spacer(Modifier.height(7.dp))
    Text(
      text = entry.displayName,
      fontSize = 11.sp,
      lineHeight = 14.sp,
      fontWeight = FontWeight.SemiBold,
      color = Color.White,
      textAlign = TextAlign.Center,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.fillMaxWidth()
    )
    Text(
      text = "${formatXp(entry.xp)} XP",
      fontSize = 10.sp,
      lineHeight = 14.sp,
      color = EcoColor.Green100,
      textAlign = TextAlign.Center,
      maxLines = 1,
      modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(pedestalHeight)
        .background(pedestalColor, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "#${entry.rank}",
        style = MaterialTheme.typography.titleLarge.copy(fontSize = 21.sp, lineHeight = 27.sp),
        color = EcoColor.Green900
      )
    }
  }
}

/**
 * Podium for the first (up to) three server entries, showing the server rank and weekly mission XP.
 * Pedestal height follows the server rank so ties look equal. Fewer than three entries leave empty
 * space instead of placeholder participants.
 */
@Composable
fun LeaderboardPodiumCard(
  topEntries: List<LeaderboardEntry>,
  modifier: Modifier = Modifier
) {
  val top = topEntries.take(3)
  if (top.isEmpty()) return
  // Classic arrangement: 2nd | 1st | 3rd by list position.
  val order = when (top.size) {
    3 -> listOf(1, 0, 2)
    2 -> listOf(1, 0)
    else -> listOf(0)
  }
  val sideSpace = (3 - top.size) / 2f

  EcoCard(
    modifier = modifier.fillMaxWidth(),
    containerColor = EcoColor.Green800,
    borderColor = EcoColor.Green800,
    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 0.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(Lucide.Trophy, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
      Spacer(Modifier.width(8.dp))
      Text(
        text = if (top.size >= 3) "Top 3 XP Mingguan" else "Teratas XP Mingguan",
        style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp, lineHeight = 20.sp),
        color = Color.White
      )
    }
    Spacer(Modifier.height(9.dp))
    Text(
      text = "XP dari klaim misi minggu ini",
      style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, lineHeight = 16.sp),
      color = EcoColor.Green100,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(22.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.Bottom,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      if (sideSpace > 0f) Spacer(Modifier.weight(sideSpace))
      order.forEach { index ->
        val entry = top[index]
        val (height, pedestal, avatar) = when {
          entry.rank <= 1L -> Triple(115.dp, Color(0xFFE6EAD9), Color(0xFFFFD88E))
          entry.rank == 2L -> Triple(76.dp, Color(0xFF75C9A5), Color(0xFFE7EBDC))
          else -> Triple(66.dp, Color(0xFF95D5B2), Color(0xFF52B788))
        }
        PodiumPlace(
          entry = entry,
          pedestalHeight = height,
          pedestalColor = pedestal,
          avatarColor = avatar,
          modifier = Modifier.weight(1f)
        )
      }
      if (sideSpace > 0f) Spacer(Modifier.weight(sideSpace))
    }
  }
}
