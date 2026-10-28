package com.example.ecosnap.features.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecosnap.R
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.core.designsystem.theme.EcoGradient
import com.example.ecosnap.core.designsystem.theme.EcoSpacing
import com.example.ecosnap.core.designsystem.theme.EcosnapTheme
import com.example.ecosnap.core.designsystem.theme.ecoTypography
import com.example.ecosnap.features.onboarding.components.OnboardingPageContent
import com.example.ecosnap.features.onboarding.components.OnboardingPageIndicator
import com.example.ecosnap.features.onboarding.model.onboardingPages
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
  onFinished: () -> Unit,
  onSkip: () -> Unit = onFinished,
  modifier: Modifier = Modifier
) {
  val pages = onboardingPages

  val pagerState = rememberPagerState(
    initialPage = 0,
    pageCount = { pages.size }
  )

  val coroutineScope = rememberCoroutineScope()

  val currentPage = pagerState.currentPage
  val isLastPage = currentPage == pages.lastIndex

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(EcoGradient.vertical)
      .windowInsetsPadding(WindowInsets.safeDrawing)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(
          start = EcoSpacing.xl,
          end = EcoSpacing.xl,
          top = EcoSpacing.sm,
          bottom = EcoSpacing.lg
        ),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Image(
          painter = painterResource(
            R.drawable.splash_screen_logo
          ),
          contentDescription = "Logo EcoSnap",
          modifier = Modifier.size(28.dp)
        )

        Spacer(Modifier.width(8.dp))

        Text(
          text = buildAnnotatedString {
            withStyle(
              SpanStyle(
                color = EcoColor.Green900
              )
            ) {
              append("Eco")
            }

            withStyle(
              SpanStyle(
                color = EcoColor.TextBrand
              )
            ) {
              append("Snap")
            }
          },
          style = MaterialTheme.typography.titleLarge.copy(
            fontSize = 18.sp,
            lineHeight = 24.sp
          )
        )

        Spacer(Modifier.weight(1f))

        if (!isLastPage) {
          TextButton(
            onClick = onSkip,
            colors = ButtonDefaults.textButtonColors(
              contentColor = EcoColor.Green700,
              containerColor = Color.Transparent
            )
          ) {
            Text(
              text = "Lewati",
              style = ecoTypography.labelLarge
            )
          }
        }
      }

      HorizontalPager(
        state = pagerState,
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
      ) { pageIndex ->
        OnboardingPageContent(
          page = pages[pageIndex],
          modifier = Modifier.fillMaxSize()
        )
      }

      Spacer(Modifier.height(16.dp))

      OnboardingPageIndicator(
        pageCount = pages.size,
        currentPage = currentPage
      )

      Spacer(Modifier.height(24.dp))

      EcoButton(
        text = if (isLastPage) "Mulai" else "Lanjut",
        onClick = {
          if (isLastPage) {
            onFinished()
          } else {
            coroutineScope.launch {
              pagerState.animateScrollToPage(
                page = currentPage + 1
              )
            }
          }
        },
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}

@Preview(
  name = "EcoSnap Onboarding",
  showBackground = true,
  widthDp = 360,
  heightDp = 780
)

@Composable
private fun OnboardingPreview() {
  EcosnapTheme(
    darkTheme = false,
    dynamicColor = false
  ) {
    OnboardingScreen(
      onFinished = {},
      onSkip = {},
    )
  }
}