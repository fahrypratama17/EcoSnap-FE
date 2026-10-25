package com.example.ecosnap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ecosnap.core.designsystem.theme.EcosnapTheme
import com.example.ecosnap.features.onboarding.OnboardingScreen
import com.example.ecosnap.features.onboarding.SplashScreen
import kotlinx.coroutines.delay

private enum class AppScreen {
  SPLASH,
  ONBOARDING,
  HOME
}

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      EcosnapTheme(
        darkTheme = false,
        dynamicColor = false
      ) {
        var currentScreen by rememberSaveable {
          mutableStateOf(AppScreen.SPLASH.name)
        }

        LaunchedEffect(currentScreen) {
          if (currentScreen == AppScreen.SPLASH.name) {
            delay(2000L)
            currentScreen = AppScreen.ONBOARDING.name
          }
        }

        when (currentScreen) {

          AppScreen.SPLASH.name -> {
            SplashScreen()
          }

          AppScreen.ONBOARDING.name -> {
            OnboardingScreen(
              onFinished = {
                currentScreen = AppScreen.HOME.name
              },
              OnSkip = {
                currentScreen = AppScreen.HOME.name
              }
            )
          }

          AppScreen.HOME.name -> {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Welcome to EcoSnap!",
                style = MaterialTheme.typography.titleLarge
              )
            }
          }
        }
      }
    }
  }
}
