package com.example.ecosnap.features.profile.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecosnap.core.designsystem.theme.EcoColor

/** A setting whose value is the server's; [onCheckedChange] must write to the backend. */
@Composable
fun SettingToggleRow(
  title: String,
  description: String,
  checked: Boolean,
  enabled: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
        color = EcoColor.Green900
      )

      Spacer(Modifier.height(4.dp))

      Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontSize = 11.sp,
          lineHeight = 16.sp
        ),
        color = EcoColor.Green700
      )
    }

    Spacer(Modifier.width(12.dp))

    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      enabled = enabled
    )
  }
}
