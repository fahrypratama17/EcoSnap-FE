package com.example.ecosnap.features.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Scale
import com.example.ecosnap.core.designsystem.theme.EcoColor

/** Outlined white card used across the partner (mitra) home, matching the original mitra design. */
@Composable
fun PartnerCard(
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(16.dp),
  content: @Composable ColumnScope.() -> Unit
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = EcoColor.White,
    shape = RoundedCornerShape(20.dp),
    border = BorderStroke(1.dp, EcoColor.Green900),
    shadowElevation = 3.dp
  ) {
    Column(Modifier.padding(contentPadding), content = content)
  }
}

/** Green "PANEL MITRA" hero card; [content] fills the light inner summary box. */
@Composable
fun PartnerPanel(
  title: String,
  subtitle: String,
  statusLabel: String?,
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit
) {
  Surface(modifier = modifier.fillMaxWidth(), color = EcoColor.Green800, shape = RoundedCornerShape(22.dp)) {
    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("PANEL MITRA", fontSize = 10.sp, color = EcoColor.White)
          Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EcoColor.White)
          Text(subtitle, fontSize = 11.sp, lineHeight = 15.sp, color = EcoColor.Green100)
          if (statusLabel != null) {
            Surface(color = EcoColor.Green100, shape = RoundedCornerShape(50)) {
              Text(
                statusLabel,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = EcoColor.Green800
              )
            }
          }
        }
        Surface(
          modifier = Modifier.padding(start = 10.dp).size(68.dp),
          color = EcoColor.Green200.copy(alpha = 0.55f),
          shape = RoundedCornerShape(16.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(Lucide.Scale, null, Modifier.size(42.dp), tint = EcoColor.White)
          }
        }
      }
      Surface(color = EcoColor.Green100, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.fillMaxWidth().padding(12.dp), content = content)
      }
    }
  }
}

/** One big figure with a caption, used for points/weight in the panel summary. */
@Composable
fun PartnerStat(label: String, value: String, unit: String, modifier: Modifier = Modifier, end: Boolean = false) {
  Column(
    modifier = modifier,
    horizontalAlignment = if (end) Alignment.End else Alignment.Start,
    verticalArrangement = Arrangement.spacedBy(3.dp)
  ) {
    Text(label, fontSize = 10.sp, color = EcoColor.Green800)
    Text(value, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = EcoColor.Green900)
    Text(unit, fontSize = 10.sp, color = EcoColor.Green900)
  }
}

/** Feature menu card: icon tile, title, description and action label. */
@Composable
fun PartnerMenuCard(
  title: String,
  description: String,
  action: String,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    border = BorderStroke(1.dp, EcoColor.Green900),
    shadowElevation = 3.dp,
    color = EcoColor.White
  ) {
    Row(
      Modifier.fillMaxWidth().padding(13.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
      Surface(color = EcoColor.Green100, shape = RoundedCornerShape(12.dp), modifier = Modifier.size(66.dp)) {
        Box(contentAlignment = Alignment.Center) {
          Icon(icon, null, Modifier.size(38.dp), tint = EcoColor.Green800)
        }
      }
      Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, lineHeight = 18.sp, color = EcoColor.Green900)
        Text(description, fontSize = 10.sp, lineHeight = 14.sp, color = EcoColor.Green800)
        Text(action, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = EcoColor.Green800)
      }
      Icon(Lucide.ChevronRight, null, Modifier.size(16.dp), tint = EcoColor.Green900)
    }
  }
}
