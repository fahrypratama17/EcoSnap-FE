package com.example.ecosnap.features.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
internal fun WalletTopBar(title: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 8.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    IconButton(onClick = onBack) {
      Icon(Lucide.ArrowLeft, contentDescription = "Kembali", tint = EcoColor.Green900)
    }
    Text(
      text = title,
      style = MaterialTheme.typography.titleLarge,
      color = EcoColor.Green900
    )
  }
}

@Composable
internal fun LoadingBox(modifier: Modifier = Modifier) {
  Box(modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
    CircularProgressIndicator(color = EcoColor.Green700)
  }
}

@Composable
internal fun ErrorBox(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
  EcoCard(modifier = modifier.fillMaxWidth(), containerColor = EcoColor.White, borderColor = EcoColor.TextError) {
    Text(message, color = EcoColor.TextError, fontSize = 14.sp)
    Spacer(Modifier.height(8.dp))
    EcoButton(text = "Coba lagi", onClick = onRetry, height = 40.dp)
  }
}

@Composable
internal fun EmptyBox(message: String, modifier: Modifier = Modifier) {
  Box(modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
    Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
  }
}

@Composable
internal fun Tag(text: String, background: Color, content: Color, modifier: Modifier = Modifier) {
  Text(
    text = text,
    color = content,
    fontSize = 11.sp,
    fontWeight = FontWeight.SemiBold,
    modifier = modifier
      .background(background, RoundedCornerShape(50))
      .padding(horizontal = 8.dp, vertical = 2.dp)
  )
}

@Composable
internal fun InfoLine(label: String, value: String) {
  Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
    Spacer(Modifier.width(12.dp))
    Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = EcoColor.Green900)
  }
}

@Composable
internal fun SmallSpinner() {
  CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = EcoColor.Green700)
}
