package com.example.ecosnap.features.scan.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ControlLabelColor = Color(0xFFDCE4D8)
private val ShutterRingColor = Color(0xFFA3B18A)
private val ShutterFillColor = Color(0xFFF6F5EF)

@Composable
fun ScanQrControlButton(
  icon: ImageVector,
  label: String,
  onClick: () -> Unit,
  shape: Shape,
  modifier: Modifier = Modifier,
  containerColor: Color = Color.Transparent,
  borderColor: Color? = null,
  enabled: Boolean = true
) {
  val contentAlpha = if (enabled) 1f else 0.4f

  Column(
    modifier = modifier.width(64.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Box(
      modifier = Modifier
        .size(48.dp)
        .clip(shape)
        .background(containerColor, shape)
        .then(
          if (borderColor != null) {
            Modifier.border(1.dp, borderColor, shape)
          } else {
            Modifier
          }
        )
        .clickable(
          enabled = enabled,
          role = Role.Button,
          onClick = onClick
        ),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = Color.White.copy(alpha = contentAlpha),
        modifier = Modifier.size(25.dp)
      )
    }

    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium.copy(
        fontSize = 12.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.sp
      ),
      color = ControlLabelColor.copy(alpha = contentAlpha)
    )
  }
}

@Composable
fun ScanQrShutterButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  label: String = "Ambil foto",
  enabled: Boolean = true
) {
  val contentAlpha = if (enabled) 1f else 0.4f

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Box(
      modifier = Modifier
        .size(78.dp)
        .border(3.dp, ShutterRingColor, CircleShape)
        .clip(CircleShape)
        .clickable(
          enabled = enabled,
          role = Role.Button,
          onClickLabel = label,
          onClick = onClick
        ),
      contentAlignment = Alignment.Center
    ) {
      Box(
        modifier = Modifier
          .size(62.dp)
          .background(
            ShutterFillColor.copy(alpha = contentAlpha),
            CircleShape
          )
      )
    }

    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium.copy(
        fontSize = 12.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.sp
      ),
      color = Color.White.copy(alpha = contentAlpha)
    )
  }
}
