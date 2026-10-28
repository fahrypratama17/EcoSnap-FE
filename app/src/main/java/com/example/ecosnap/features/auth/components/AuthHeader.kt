package com.example.ecosnap.features.auth.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.R
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.core.designsystem.theme.EcoGradient
import com.example.ecosnap.core.designsystem.theme.EcoSpacing

@Composable
fun AuthLayout(
  onBack: () -> Unit,
  onHelp: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(EcoGradient.vertical)
      .windowInsetsPadding(WindowInsets.safeDrawing)
      .imePadding()
      .verticalScroll(rememberScrollState())
      .padding(
        horizontal = EcoSpacing.xl,
        vertical = EcoSpacing.lg
      )
  ) {
    AuthHeader(
      onBack = onBack,
      onHelp = onHelp
    )

    Spacer(Modifier.height(12.dp))

    content()
  }
}

@Composable
fun AuthHeader(
  onBack: () -> Unit,
  onHelp: () -> Unit
) {
  Column{
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Image(
        painter = painterResource(
          R.drawable.splash_screen_logo
        ),
        contentDescription = "EcoSnap",
        modifier = Modifier.size(28.dp)
      )

      Spacer(Modifier.width(6.dp))

      Text(
        text = buildAnnotatedString {
          withStyle(
            SpanStyle(color = EcoColor.Green900)
          ) {
            append("Eco")
          }
          withStyle(
            SpanStyle(color = EcoColor.TextBrand)
          ) {
            append("Snap")
          }
        },
        style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp)
      )

      Spacer(Modifier.weight(1f))

      TextButton(
        onClick = onHelp
      ) {
        Text(
          "Butuh bantuan? Hubungi kami",
          fontSize = 10.sp,
          color = EcoColor.Green700
        )
      }
    }

    Spacer(Modifier.height(20.dp))

    TextButton(
      onClick = onBack,
      contentPadding = PaddingValues(0.dp)
    ) {
      Icon(
        imageVector = Lucide.ArrowLeft,
        contentDescription = null,
        modifier = Modifier.size(16.dp),
        tint = EcoColor.Green700
      )

      Spacer(Modifier.width(4.dp))

      Text(
        text = "Kembali",
        fontSize = 12.sp,
        color = EcoColor.Green700
      )
    }
  }
}