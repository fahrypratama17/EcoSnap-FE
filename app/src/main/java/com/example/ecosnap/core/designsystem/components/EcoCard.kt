package com.example.ecosnap.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ecosnap.core.designsystem.theme.EcoShape

@Composable
fun EcoCard(
  modifier: Modifier = Modifier,
  containerColor: Color = MaterialTheme.colorScheme.surface,
  borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
  shadowElevation: Dp = 2.dp,
  contentPadding: PaddingValues = PaddingValues(12.dp),
  content: @Composable ColumnScope.() -> Unit
) {
  Surface(
    modifier = modifier,
    shape = EcoShape.large,
    color = containerColor,
    border = BorderStroke(1.dp, borderColor),
    shadowElevation = shadowElevation
  ) {
    Column(
      modifier = Modifier.padding(contentPadding),
      content = content
    )
  }
}