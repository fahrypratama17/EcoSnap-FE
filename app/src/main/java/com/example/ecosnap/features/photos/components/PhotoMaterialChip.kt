package com.example.ecosnap.features.photos.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun PhotoMaterialChip(
  label: String,
  modifier: Modifier = Modifier,
  containerColor: Color = EcoColor.Green100,
  contentColor: Color = EcoColor.Green800
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(8.dp),
    color = containerColor
  ) {
    Text(
      text = label,
      fontSize = 11.sp,
      color = contentColor,
      modifier = Modifier.padding(
        horizontal = 9.dp,
        vertical = 5.dp
      )
    )
  }
}
