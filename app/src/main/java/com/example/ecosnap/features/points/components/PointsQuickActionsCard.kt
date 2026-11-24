package com.example.ecosnap.features.points.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Calculator
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.QrCode

@Composable
fun PointsQuickActionsCard(
  onScanQr: () -> Unit,
  onCalculator: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = Color(0xFF254031),
    shadowElevation = 4.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(
          horizontal = 24.dp,
          vertical = 22.dp
        ),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically
    ) {
      PointsQuickActionItem(
        title = "Scan QR",
        icon = Lucide.QrCode,
        onClick = onScanQr,
        modifier = Modifier.weight(1f)
      )

      PointsQuickActionItem(
        title = "Kalkulator",
        icon = Lucide.Calculator,
        onClick = onCalculator,
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun PointsQuickActionItem(
  title: String,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .padding(vertical = 4.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .size(58.dp)
        .background(
          color = Color(0xFF56836B),
          shape = RoundedCornerShape(12.dp)
        ),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = Color.White,
        modifier = Modifier.size(30.dp)
      )
    }

    Spacer(Modifier.height(10.dp))

    Text(
      text = title,
      style = MaterialTheme.typography.bodyMedium.copy(
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp
      ),
      color = Color.White
    )
  }
}
