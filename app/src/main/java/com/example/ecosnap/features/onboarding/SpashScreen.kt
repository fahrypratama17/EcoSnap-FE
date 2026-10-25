package com.example.ecosnap.features.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecosnap.R
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.core.designsystem.theme.EcoSpacing
import com.example.ecosnap.core.designsystem.theme.EcosnapTheme
import com.example.ecosnap.core.designsystem.theme.PlusJakartaSans

@Composable
fun SplashScreen(
  modifier: Modifier = Modifier
) {
  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(EcoColor.White)
  ) {
    val logoSize = (maxWidth * 0.6f)
      .coerceAtMost(260.dp)

    Box(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .offset(x = 100.dp, y = (-100).dp)
        .size(340.dp)
        .background(
          Brush.radialGradient(
            colors = listOf(
              EcoColor.Green100,
              EcoColor.Green100,
              EcoColor.Green100,
              Color.Transparent
            )
          )
        )
    )

    Box(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .offset(x = (-120).dp, y = (-110).dp)
        .size(340.dp)
        .background(
          Brush.radialGradient(
            colors = listOf(
              EcoColor.Green100,
              EcoColor.Green100,
              EcoColor.Green100,
              Color.Transparent
            )
          )
        )
    )

    Column(
      modifier = Modifier
        .align(Alignment.Center)
        .offset(y = 24.dp)
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Image(
        painter = painterResource(
          R.drawable.splash_screen_logo
        ),
        contentDescription = "Logo EcoSnap",
        modifier = Modifier.size(logoSize),
        contentScale = ContentScale.Fit
      )

      Spacer(modifier = Modifier.height(EcoSpacing.sm))

      Text(
        text = buildAnnotatedString {
          withStyle(
            SpanStyle(color = EcoColor.Green900)
          ) {
            append("Eco")
          }
          withStyle(
            SpanStyle(color = EcoColor.Green700)
          ) {
            append("Snap")
          }
        },
        style = MaterialTheme.typography.headlineLarge,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(EcoSpacing.xs))

      Text(
        text = "Snap. Sort. Sustain.",
        fontSize = 16.sp,
        fontFamily = PlusJakartaSans,
        color = EcoColor.Green900.copy(alpha = 0.7f),
        textAlign = TextAlign.Center
      )
    }

    CircularProgressIndicator(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .navigationBarsPadding()
        .padding(bottom = 40.dp)
        .size(26.dp),
      color = EcoColor.Green900,
      strokeWidth = 2.dp
    )
  }
}

@Preview(
  showBackground = true,
  showSystemUi = true
)

@Composable
private fun SplashScreenPreview() {
  EcosnapTheme(
    darkTheme = false,
    dynamicColor = false
  ) {
    SplashScreen()
  }
}