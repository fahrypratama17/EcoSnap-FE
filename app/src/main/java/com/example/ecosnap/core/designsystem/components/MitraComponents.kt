package com.example.ecosnap.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Delete
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Scale
import com.example.ecosnap.core.designsystem.theme.EcoColor

/*
 * Partner (mitra) design language: outlined white cards, green panel, pill buttons and keypad.
 * Shared by the partner home, calculator, deposit create/history and the partner QR screen.
 */

private val MitraErrorSurface = Color(0xFFFBE9E7)

/** Outlined white card that contains every mitra content block. */
@Composable
fun MitraCard(
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

/** Same card, clickable (history rows). */
@Composable
fun MitraCard(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(16.dp),
  content: @Composable ColumnScope.() -> Unit
) {
  Surface(
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
    color = EcoColor.White,
    shape = RoundedCornerShape(20.dp),
    border = BorderStroke(1.dp, EcoColor.Green900),
    shadowElevation = 3.dp
  ) {
    Column(Modifier.padding(contentPadding), content = content)
  }
}

/** Centered page title and subtitle shown below the app header. */
@Composable
fun MitraPageTitle(title: String, subtitle: String) {
  Column(
    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Text(
      title,
      style = MaterialTheme.typography.headlineMedium.copy(
        fontSize = 23.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight.Bold
      ),
      color = EcoColor.Green900,
      textAlign = TextAlign.Center
    )
    Text(
      subtitle,
      style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, lineHeight = 17.sp),
      color = EcoColor.Green800,
      textAlign = TextAlign.Center
    )
  }
}

/** Back control for non-tab mitra screens; the app header stays the only header. */
@Composable
fun MitraBackLink(onBack: () -> Unit, modifier: Modifier = Modifier) {
  Surface(onClick = onBack, modifier = modifier, color = Color.Transparent, shape = RoundedCornerShape(50)) {
    Row(
      Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Icon(Lucide.ArrowLeft, contentDescription = null, modifier = Modifier.size(18.dp), tint = EcoColor.Green900)
      Text("Kembali", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = EcoColor.Green900)
    }
  }
}

/** Light green information block with a leading icon; [error] turns it into a warning. */
@Composable
fun MitraInfoCard(
  title: String,
  text: String,
  modifier: Modifier = Modifier,
  icon: ImageVector = Lucide.Info,
  error: Boolean = false
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = if (error) MitraErrorSurface else EcoColor.Green100,
    shape = RoundedCornerShape(22.dp)
  ) {
    Row(
      Modifier.fillMaxWidth().padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Icon(icon, null, Modifier.size(34.dp), tint = if (error) EcoColor.TextError else EcoColor.Green800)
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, color = EcoColor.Green900)
        Text(text, fontSize = 11.sp, lineHeight = 16.sp, color = if (error) EcoColor.TextError else EcoColor.Green800)
      }
    }
  }
}

/** Short status line (validation / request error) in the mitra palette. */
@Composable
fun MitraNotice(text: String, modifier: Modifier = Modifier, error: Boolean = false) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = if (error) MitraErrorSurface else EcoColor.Green100,
    shape = RoundedCornerShape(16.dp)
  ) {
    Text(
      text,
      modifier = Modifier.padding(14.dp),
      style = MaterialTheme.typography.bodyMedium,
      color = if (error) EcoColor.TextError else EcoColor.Green900
    )
  }
}

/** Total weight, divider, points. */
@Composable
fun MitraSummaryCard(weight: String, points: Long, pointsLabel: String) {
  MitraCard {
    Column(
      Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
      Text("Total Berat Sampah", fontSize = 17.sp, color = EcoColor.Green900)
      Text(weight, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EcoColor.Green800)
      androidx.compose.material3.HorizontalDivider(Modifier.padding(vertical = 5.dp), color = EcoColor.Green900)
      Text(pointsLabel, fontSize = 17.sp, color = EcoColor.Green900)
      Text("+$points Poin", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EcoColor.Green800)
    }
  }
}

/** Green hero panel with the large weight figure. */
@Composable
fun MitraWeightHero(weightText: String, modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = EcoColor.Green700,
    border = BorderStroke(1.dp, EcoColor.Green900),
    shape = RoundedCornerShape(21.dp)
  ) {
    Row(
      Modifier.fillMaxWidth().padding(horizontal = 26.dp, vertical = 22.dp)
        .semantics { contentDescription = "Total berat sampah $weightText kilogram" },
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        weightText,
        modifier = Modifier.weight(1f),
        textAlign = TextAlign.Center,
        fontSize = 38.sp,
        lineHeight = 48.sp,
        fontWeight = FontWeight.Bold,
        color = EcoColor.White
      )
      Text("Kg", fontSize = 20.sp, color = EcoColor.White)
    }
  }
}

/** Pill-shaped outlined secondary button. */
@Composable
fun MitraOutlinedButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true
) {
  OutlinedButton(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier.height(48.dp),
    shape = RoundedCornerShape(16.dp),
    border = BorderStroke(1.dp, EcoColor.Green900),
    contentPadding = PaddingValues(horizontal = 8.dp)
  ) {
    Text(text, fontSize = 12.sp, color = EcoColor.Green900)
  }
}

/** Filter pill: selected is solid green, otherwise outlined white. */
@Composable
fun MitraPill(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
  Surface(
    onClick = onClick,
    modifier = modifier,
    color = if (selected) EcoColor.Green700 else EcoColor.White,
    border = BorderStroke(1.dp, EcoColor.Green900),
    shape = RoundedCornerShape(50)
  ) {
    Text(
      label,
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold,
      color = if (selected) EcoColor.White else EcoColor.Green900
    )
  }
}

/** Green "PANEL MITRA" hero card; [content] fills the light inner summary box. */
@Composable
fun MitraPanel(
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

/** One figure with a caption inside the panel summary. */
@Composable
fun MitraStat(label: String, value: String, unit: String, modifier: Modifier = Modifier, end: Boolean = false) {
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
fun MitraMenuCard(
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

private val QuickAdjustments = listOf("−0,5" to "-0.5", "+0,1" to "0.1", "+0,5" to "0.5", "+1,0" to "1.0")
private val KeypadRows = listOf(
  listOf("1", "2", "3"),
  listOf("4", "5", "6"),
  listOf("7", "8", "9"),
  listOf(",", "0", DELETE_KEY)
)

/** Key name emitted by [MitraKeypad] for the backspace key. */
const val DELETE_KEY = "delete"

/**
 * Weight keypad: quick-adjust pills (delta passed as a plain decimal string, e.g. "-0.5") plus a
 * 4x3 digit pad that emits "0".."9", "," or [DELETE_KEY]. Pure input; the caller owns the value.
 */
@Composable
fun MitraKeypad(
  onAdjust: (String) -> Unit,
  onKey: (String) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true
) {
  Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      QuickAdjustments.forEach { (label, delta) ->
        Surface(
          onClick = { onAdjust(delta) },
          enabled = enabled,
          modifier = Modifier.weight(1f).height(48.dp).semantics { contentDescription = "$label kilogram" },
          color = EcoColor.White,
          border = BorderStroke(1.dp, EcoColor.Green900),
          shape = RoundedCornerShape(50)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = EcoColor.Green900)
          }
        }
      }
    }
    KeypadRows.forEach { keys ->
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        keys.forEach { key ->
          val special = key == "," || key == DELETE_KEY
          Surface(
            onClick = { onKey(key) },
            enabled = enabled,
            modifier = Modifier.weight(1f).height(48.dp),
            color = if (special) EcoColor.Green400 else EcoColor.Green100,
            shape = RoundedCornerShape(50)
          ) {
            Box(contentAlignment = Alignment.Center) {
              if (key == DELETE_KEY) {
                Icon(Lucide.Delete, "Hapus digit terakhir", Modifier.size(23.dp), tint = EcoColor.Green900)
              } else {
                Text(key, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = EcoColor.Green900)
              }
            }
          }
        }
      }
    }
  }
}
