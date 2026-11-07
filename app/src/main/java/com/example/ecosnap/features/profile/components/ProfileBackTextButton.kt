package com.example.ecosnap.features.profile.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun ProfileBackTextButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  TextButton(
    onClick = onClick,
    modifier = modifier,
    contentPadding = PaddingValues(0.dp)
  ) {
    Icon(
      imageVector = Lucide.ArrowLeft,
      contentDescription = null,
      tint = EcoColor.Green700,
      modifier = Modifier.size(16.dp)
    )

    Spacer(Modifier.width(6.dp))

    Text(
      text = text,
      color = EcoColor.Green700,
      style = MaterialTheme.typography.labelLarge.copy(
        fontSize = 12.sp
      )
    )
  }
}