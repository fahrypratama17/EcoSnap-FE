package com.example.ecosnap.features.photos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun CameraControlButton(
  icon: ImageVector,
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(5.dp)
  ) {
    IconButton(
      onClick = onClick,
      enabled = enabled,
      modifier = Modifier
        .size(48.dp)
        .background(
          Color.White.copy(
            alpha = if (enabled) 0.12f else 0.0f
          ),
          CircleShape
        )
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = Color.White.copy(
          alpha = if (enabled) 1f else 0.4f
        ),
        modifier = Modifier.size(21.dp)
      )
    }

    Text(
      text = label,
      fontSize = 11.sp,
      color = Color.White.copy(
        alpha = if (enabled) 1f else 0.4f
      )
    )
  }
}

@Composable
fun CameraShutterButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  loading: Boolean = false
) {
  IconButton(
    onClick = onClick,
    enabled = enabled && !loading,
    modifier = modifier
      .size(78.dp)
      .border(
        width = 3.dp,
        color = EcoColor.Green200,
        shape = CircleShape
      )
      .padding(6.dp)
      .background(
        color = if (enabled) Color.White else Color.LightGray,
        shape = CircleShape
      )
  ) {
    if (loading) {
      CircularProgressIndicator(
        modifier = Modifier.size(30.dp),
        color = EcoColor.Green800,
        strokeWidth = 3.dp
      )
    } else {
      Box(
        modifier = Modifier
          .size(48.dp)
          .background(
            Color.White,
            CircleShape
          )
      )
    }
  }
}