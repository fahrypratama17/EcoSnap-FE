package com.example.ecosnap.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.core.designsystem.theme.EcoShape

@Composable
fun EcoButton (
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  startIcon: (@Composable () -> Unit)? = null,
  endIcon: (@Composable () -> Unit)? = null,
  height: Dp = 48.dp,
  shape: Shape = EcoShape.large,
  enabled: Boolean = true,
  containerColor: Color = EcoColor.Green800,
  contentColor: Color = EcoColor.White,
) {
  Button(
    onClick = onClick,
    modifier = modifier.height(height),
    enabled = enabled,
    shape = shape,
    colors = ButtonDefaults.buttonColors(
      containerColor = containerColor,
      contentColor = contentColor
    ),
    contentPadding = PaddingValues(0.dp)
  ) {
    Box(
      modifier = Modifier.fillMaxWidth(),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
          .fillMaxWidth()
          .padding(
            horizontal = if (
              startIcon != null || endIcon != null
            ) 52.dp else 16.dp
          )
      )

      startIcon?.let { icon ->
        Box(
          modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(start = 16.dp),
          contentAlignment = Alignment.Center
        ) {
          icon()
        }
      }

      endIcon?.let { icon ->
        Box(
          modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = 16.dp),
          contentAlignment = Alignment.Center
        ) {
          icon()
        }
      }
    }
  }
}