package com.example.ecosnap.features.deposits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.X
import com.example.ecosnap.core.designsystem.components.EcoDropdownField
import com.example.ecosnap.core.designsystem.components.EcoTextField
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.google.gson.JsonArray
import com.google.gson.JsonObject

data class WeightRow(val key: Long, val categoryId: String? = null, val weight: String = "")

/** Outcome of validating the entered rows into the exact wire items. */
sealed interface RowsResult {
  data class Valid(val items: JsonArray) : RowsResult
  data class Invalid(val message: String?) : RowsResult
}

object WeightRows {
  /** Rows are sorted by category id so identical input always yields an identical (idempotent) payload. */
  fun validate(rows: List<WeightRow>, categories: List<WasteOption>): RowsResult {
    if (rows.isEmpty()) return RowsResult.Invalid(null)
    val known = categories.mapTo(HashSet()) { it.id }
    val pairs = ArrayList<Pair<String, String>>()
    for (row in rows) {
      val category = row.categoryId ?: return RowsResult.Invalid(null)
      if (category !in known) return RowsResult.Invalid("Kategori tidak dikenal.")
      val weight = WeightInput.canonical(row.weight)
        ?: return RowsResult.Invalid(if (row.weight.isBlank()) null else "Berat harus 0,001-1000 dengan maksimal 3 desimal.")
      pairs += category to weight
    }
    if (pairs.map { it.first }.toSet().size != pairs.size) {
      return RowsResult.Invalid("Satu kategori hanya boleh muncul sekali. Gabungkan beratnya.")
    }
    if (pairs.size > WeightInput.MAX_ITEMS) return RowsResult.Invalid("Maksimal ${WeightInput.MAX_ITEMS} kategori.")
    if (!WeightInput.withinLimit(pairs.map { it.second })) return RowsResult.Invalid("Total berat maksimal 1000 kg.")
    val array = JsonArray()
    pairs.sortedBy { it.first }.forEach { (category, weight) ->
      array.add(JsonObject().apply {
        addProperty("category_id", category)
        addProperty("weight_kg", weight)
      })
    }
    return RowsResult.Valid(array)
  }

  fun fromPayload(items: JsonArray, startKey: Long): List<WeightRow> =
    items.mapIndexed { index, element ->
      val o = element.asJsonObject
      WeightRow(startKey + index, o.get("category_id").asString, o.get("weight_kg").asString)
    }
}

/** Category + weight rows backed by server categories. */
@Composable
fun DepositItemsEditor(
  rows: List<WeightRow>,
  categories: List<WasteOption>,
  enabled: Boolean,
  onChange: (Long, WeightRow) -> Unit,
  onAdd: () -> Unit,
  onRemove: (Long) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
    rows.forEach { row ->
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
        EcoDropdownField(
          value = categories.firstOrNull { it.id == row.categoryId }?.name.orEmpty(),
          onValueChange = { name ->
            if (enabled) categories.firstOrNull { it.name == name }?.let { onChange(row.key, row.copy(categoryId = it.id)) }
          },
          label = "Kategori",
          options = categories.map { it.name },
          modifier = Modifier.weight(1.2f),
        )
        EcoTextField(
          value = row.weight,
          onValueChange = { if (enabled) onChange(row.key, row.copy(weight = it.take(9))) },
          label = "Berat (kg)",
          placeholder = "2,5",
          keyboardType = KeyboardType.Decimal,
          modifier = Modifier.weight(1f),
        )
        if (enabled && rows.size > 1) {
          IconButton(onClick = { onRemove(row.key) }, modifier = Modifier.size(40.dp)) {
            Icon(Lucide.X, contentDescription = "Hapus baris", tint = EcoColor.TextError)
          }
        }
      }
    }
    if (enabled && rows.size < WeightInput.MAX_ITEMS) {
      TextButton(onClick = onAdd) {
        Icon(Lucide.Plus, contentDescription = null, modifier = Modifier.size(16.dp), tint = EcoColor.Green700)
        Text("  Tambah kategori", color = EcoColor.Green700)
      }
    }
  }
}
