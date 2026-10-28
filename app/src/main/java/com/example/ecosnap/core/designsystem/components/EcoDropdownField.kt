package com.example.ecosnap.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.core.designsystem.theme.EcoShape

@Composable
fun EcoDropdownField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  options: List<String>,
  modifier: Modifier = Modifier,
  placeholder: String = "Pilih kategori",
  errorMessage: String? = null
) {
  var expanded by remember { mutableStateOf(false) }

  Column(modifier = modifier) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = EcoColor.Green900
    )

    Spacer(Modifier.height(6.dp))

    Box {
      OutlinedButton(
        onClick = { expanded = true },
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp),
        shape = EcoShape.medium,
        border = BorderStroke(
          1.dp,
          if (errorMessage != null) {
            EcoColor.TextError
          } else {
            EcoColor.Green400
          }
        ),
        colors = ButtonDefaults.outlinedButtonColors(
          containerColor = EcoColor.White,
          contentColor = EcoColor.Green900
        ),
        contentPadding = PaddingValues(
          horizontal = 12.dp
        )
      ) {
        Text(
          text = value.ifEmpty { placeholder },
          modifier = Modifier.weight(1f),
          maxLines = 1,
          style = MaterialTheme.typography.bodyMedium
        )

        Icon(
          imageVector = Lucide.ChevronDown,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
      }

      DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
      ) {
        options.forEach { option ->
          DropdownMenuItem(
            text = { Text(option) },
            onClick = {
              onValueChange(option)
              expanded = false
            }
          )
        }
      }
    }

    if (errorMessage != null) {
      Spacer(Modifier.height(4.dp))
      Text(
        text = errorMessage,
        style = MaterialTheme.typography.labelSmall,
        color = EcoColor.TextError
      )
    }
  }
}