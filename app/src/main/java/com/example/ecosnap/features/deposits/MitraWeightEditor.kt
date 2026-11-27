package com.example.ecosnap.features.deposits

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.X
import com.example.ecosnap.core.designsystem.components.EcoDropdownField
import com.example.ecosnap.core.designsystem.components.MitraCard
import com.example.ecosnap.core.designsystem.components.MitraKeypad
import com.example.ecosnap.core.designsystem.theme.EcoColor
import androidx.compose.foundation.layout.height

/**
 * Mitra calculator input: one card per server category row, tap a row's weight to make it the
 * active row, then enter it with the shared keypad. Validation stays in [WeightRows] / the server.
 */
@Composable
internal fun MitraWeightEditor(
  rows: List<WeightRow>,
  categories: List<WasteOption>,
  enabled: Boolean,
  onChange: (Long, WeightRow) -> Unit,
  onAdd: () -> Unit,
  onRemove: (Long) -> Unit,
  modifier: Modifier = Modifier,
) {
  var activeKey by remember { mutableLongStateOf(rows.firstOrNull()?.key ?: 0L) }
  val active = rows.firstOrNull { it.key == activeKey } ?: rows.firstOrNull()

  Column(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
    rows.forEachIndexed { index, row ->
      val selected = row.key == active?.key
      MitraCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
              "Item ${index + 1}",
              modifier = Modifier.weight(1f),
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = EcoColor.Green900,
            )
            if (enabled && rows.size > 1) {
              IconButton(onClick = { onRemove(row.key) }, modifier = Modifier.size(32.dp)) {
                Icon(Lucide.X, contentDescription = "Hapus baris ${index + 1}", tint = EcoColor.TextError, modifier = Modifier.size(18.dp))
              }
            }
          }
          EcoDropdownField(
            value = categories.firstOrNull { it.id == row.categoryId }?.name.orEmpty(),
            onValueChange = { name ->
              if (enabled) categories.firstOrNull { it.name == name }?.let { onChange(row.key, row.copy(categoryId = it.id)) }
            },
            label = "Kategori",
            options = categories.map { it.name },
            modifier = Modifier.fillMaxWidth(),
          )
          Surface(
            onClick = { activeKey = row.key },
            modifier = Modifier.fillMaxWidth().semantics {
              contentDescription = "Berat item ${index + 1} ${WeightInput.display(row.weight)} kilogram" +
                if (selected) ", sedang diisi" else ""
            },
            color = if (selected) EcoColor.Green700 else EcoColor.Green100,
            border = BorderStroke(1.dp, EcoColor.Green900),
            shape = RoundedCornerShape(16.dp),
          ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
              Text(
                WeightInput.display(row.weight),
                modifier = Modifier.weight(1f),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) EcoColor.White else EcoColor.Green900,
              )
              Text("kg", fontSize = 16.sp, color = if (selected) EcoColor.White else EcoColor.Green800)
            }
          }
        }
      }
    }
    if (enabled && rows.size < WeightInput.MAX_ITEMS) {
      OutlinedButton(
        onClick = onAdd,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, EcoColor.Green900),
      ) {
        Icon(Lucide.Plus, contentDescription = null, modifier = Modifier.size(16.dp), tint = EcoColor.Green900)
        Text("  Tambah kategori", fontSize = 12.sp, color = EcoColor.Green900)
      }
    }
    if (active != null) {
      MitraKeypad(
        enabled = enabled,
        onAdjust = { delta -> onChange(active.key, active.copy(weight = WeightInput.adjust(active.weight, delta))) },
        onKey = { key -> onChange(active.key, active.copy(weight = WeightInput.enter(active.weight, key))) },
      )
    }
  }
}
