package com.example.ecosnap.features.deposits

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.theme.EcoColor
import java.math.BigDecimal

/** Bordered white card, the partner (branch12 MitraCard) container used by calculator, create and QR screens. */
@Composable
internal fun DepositCard(
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(16.dp),
  content: @Composable ColumnScope.() -> Unit,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = EcoColor.White,
    shape = RoundedCornerShape(20.dp),
    border = BorderStroke(1.dp, EcoColor.Green900),
    shadowElevation = 3.dp,
  ) {
    Column(Modifier.padding(contentPadding), content = content)
  }
}

/** Centered page title and subtitle (branch12 MitraPageTitle). */
@Composable
internal fun DepositPageTitle(title: String, subtitle: String) {
  Column(
    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Text(
      title,
      style = MaterialTheme.typography.headlineMedium.copy(fontSize = 23.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold),
      color = EcoColor.Green900,
      textAlign = TextAlign.Center,
    )
    Text(
      subtitle,
      style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, lineHeight = 17.sp),
      color = EcoColor.Green800,
      textAlign = TextAlign.Center,
    )
  }
}

/** Compact back control for screens that sit under a parent header and have no top bar of their own. */
@Composable
internal fun DepositBackLink(onBack: () -> Unit) {
  Surface(onClick = onBack, color = Color.Transparent, shape = RoundedCornerShape(50)) {
    Row(
      Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Icon(Lucide.ArrowLeft, contentDescription = null, modifier = Modifier.size(18.dp), tint = EcoColor.Green900)
      Text("Kembali", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = EcoColor.Green900)
    }
  }
}

/** Green hero panel with the large weight figure from the original calculator input. */
@Composable
internal fun WeightHero(weightText: String) {
  Surface(
    color = EcoColor.Green700,
    border = BorderStroke(1.dp, EcoColor.Green900),
    shape = RoundedCornerShape(21.dp),
  ) {
    Row(
      Modifier.fillMaxWidth().padding(horizontal = 26.dp, vertical = 22.dp)
        .semantics { contentDescription = "Total berat sampah $weightText kilogram" },
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        weightText,
        modifier = Modifier.weight(1f),
        textAlign = TextAlign.Center,
        fontSize = 38.sp,
        lineHeight = 48.sp,
        fontWeight = FontWeight.Bold,
        color = EcoColor.White,
      )
      Text("Kg", fontSize = 20.sp, color = EcoColor.White)
    }
  }
}

/** Sum of the valid weight rows, display only; the server stays the source of truth for points. */
@Composable
internal fun rememberTotalWeightText(rows: List<WeightRow>): String = remember(rows) {
  var total = BigDecimal.ZERO
  rows.forEach { row -> WeightInput.canonical(row.weight)?.let { total += BigDecimal(it) } }
  total.setScale(3).toPlainString().replace('.', ',')
}

/** Light green information card with a leading icon (original QR "Info" card). */
@Composable
internal fun DepositInfoCard(
  title: String,
  text: String,
  modifier: Modifier = Modifier,
  icon: ImageVector = Lucide.Info,
  error: Boolean = false,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = if (error) Color(0xFFFBE9E7) else EcoColor.Green100,
    shape = RoundedCornerShape(22.dp),
  ) {
    Row(
      Modifier.fillMaxWidth().padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Icon(icon, null, Modifier.size(34.dp), tint = if (error) EcoColor.TextError else EcoColor.Green800)
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, color = EcoColor.Green900)
        Text(text, fontSize = 11.sp, lineHeight = 16.sp, color = if (error) EcoColor.TextError else EcoColor.Green800)
      }
    }
  }
}

/** Original deposit summary card: total weight, divider, points. */
@Composable
internal fun DepositSummaryCard(weightKg: String, points: Long, pointsLabel: String = "Poin setoran") {
  DepositCard {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
      Text("Total Berat Sampah", fontSize = 17.sp, color = EcoColor.Green900)
      Text(weightKg, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EcoColor.Green800)
      HorizontalDivider(Modifier.padding(vertical = 5.dp), color = EcoColor.Green900)
      Text(pointsLabel, fontSize = 17.sp, color = EcoColor.Green900)
      Text("+$points Poin", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EcoColor.Green800)
    }
  }
}
