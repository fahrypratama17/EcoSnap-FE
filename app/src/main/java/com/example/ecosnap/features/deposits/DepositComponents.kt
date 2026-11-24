package com.example.ecosnap.features.deposits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
internal fun DepositTopBar(title: String, onBack: () -> Unit) {
  Row(
    Modifier.fillMaxWidth().background(EcoColor.Green100).padding(horizontal = 8.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    IconButton(onClick = onBack) {
      Icon(Lucide.ArrowLeft, contentDescription = "Kembali", tint = EcoColor.Green900)
    }
    Text(title, style = MaterialTheme.typography.titleMedium, color = EcoColor.Green900)
  }
}

@Composable
internal fun LoadingBox(text: String) {
  Column(
    Modifier.fillMaxSize().padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    CircularProgressIndicator(Modifier.size(32.dp), color = EcoColor.Green700)
    Spacer(Modifier.height(12.dp))
    Text(text, style = MaterialTheme.typography.bodyMedium, color = EcoColor.Green700)
  }
}

@Composable
internal fun ErrorBox(message: String, onRetry: (() -> Unit)?, onBack: (() -> Unit)? = null) {
  Column(
    Modifier.fillMaxSize().padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Text(message, style = MaterialTheme.typography.bodyMedium, color = EcoColor.TextError, textAlign = TextAlign.Center)
    if (onRetry != null) TextButton(onClick = onRetry) { Text("Coba lagi") }
    if (onBack != null) TextButton(onClick = onBack) { Text("Kembali") }
  }
}

@Composable
internal fun Notice(text: String, modifier: Modifier = Modifier, error: Boolean = false) {
  Text(
    text,
    modifier = modifier
      .fillMaxWidth()
      .background(if (error) Color(0xFFFBE9E7) else EcoColor.Green100, RoundedCornerShape(16.dp))
      .padding(14.dp),
    style = MaterialTheme.typography.bodyMedium,
    color = if (error) EcoColor.TextError else EcoColor.Green900,
  )
}

@Composable
internal fun ItemLines(items: List<DepositItem>) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    items.forEachIndexed { index, item ->
      if (index > 0) HorizontalDivider(color = EcoColor.Green100)
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
          Text(item.categoryName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp), color = EcoColor.Green900)
          Text(
            "${DepositText.kg(item.weightKg)} x ${item.pointsPerKg.replace('.', ',')} poin/kg" + if (item.isDemo) " (tarif demo)" else "",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
            color = EcoColor.Green800,
          )
        }
        Text(
          "${item.exactPoints.replace('.', ',')} poin",
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
          color = EcoColor.Green800,
        )
      }
    }
  }
}

@Composable
internal fun LabelValue(label: String, value: String) {
  Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
    Text(label, style = MaterialTheme.typography.bodyMedium, color = EcoColor.Green700)
    Text(
      value,
      modifier = Modifier.weight(1f),
      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
      color = EcoColor.Green900,
      textAlign = TextAlign.End,
    )
  }
}

/** Runs [action] on every RESUME of the host lifecycle, including the first. */
@Composable
internal fun OnResume(owner: Any, action: () -> Unit) {
  val lifecycleOwner = LocalLifecycleOwner.current
  val current by rememberUpdatedState(action)
  DisposableEffect(lifecycleOwner, owner) {
    val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) current() }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }
}
