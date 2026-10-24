package com.example.ecosnap.features.onboarding.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.onboarding.model.OnboardingPage

@Composable
fun OnboardingPageContent(
  page: OnboardingPage,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      contentAlignment = Alignment.Center
    ) {
      val cardSize = minOf(
        maxWidth * 0.74f,
        maxHeight * 0.82f,
        250.dp
      )

      Surface(
        modifier = Modifier.size(cardSize),
        shape = RoundedCornerShape(18.dp),
        color = EcoColor.White,
        border = BorderStroke(
          1.dp,
          EcoColor.Green400
        ),
      ) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Box(
            modifier = Modifier
              .size(cardSize * 0.68f)
              .background(
                color = EcoColor.Green100,
                shape = CircleShape
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = page.icon,
              contentDescription = null,
              tint = EcoColor.Green800,
              modifier = Modifier.size(
                cardSize * 0.26f
              )
            )
          }
        }
      }
    }
  }

  Spacer(Modifier.height(20.dp))

  Text(
    text = page.title,
    style = MaterialTheme.typography.titleLarge.copy(
      fontSize = 20.sp,
      lineHeight = 26.sp,
      letterSpacing = 0.sp
    ),
    color = EcoColor.Green900,
    textAlign = TextAlign.Center
  )

  Spacer(Modifier.height(8.dp))

  Text(
    text = page.description,
    style = MaterialTheme.typography.bodyMedium.copy(
      fontSize = 12.sp,
      lineHeight = 18.sp,
      letterSpacing = 0.sp
    ),
    color = EcoColor.Green700,
    textAlign = TextAlign.Center,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp)
      .heightIn(min = 58.dp)
  )
}