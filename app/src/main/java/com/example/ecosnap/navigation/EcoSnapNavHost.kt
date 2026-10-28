package com.example.ecosnap.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ecosnap.features.onboarding.OnboardingScreen
import com.example.ecosnap.features.onboarding.SplashScreen
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun EcoSnapNavHost(
  modifier: Modifier = Modifier
) {
  val navController = rememberNavController()

  NavHost(
    navController = navController,
    startDestination = Routes.SPLASH,
    modifier = modifier
  ) {
    composable(Routes.SPLASH) {
      LaunchedEffect(Unit) {
        delay(2000L.milliseconds)

        navController.navigate(
          Routes.ONBOARDING
        ) {
          popUpTo(Routes.SPLASH) {
            inclusive = true
          }
          launchSingleTop = true
        }
      }

      SplashScreen()
    }

    composable(Routes.ONBOARDING) {
      val goToLogin: () -> Unit = {
        navController.navigate(
          Routes.AUTH_GRAPH
        ) {
          popUpTo(Routes.ONBOARDING) {
            inclusive = true
          }
          launchSingleTop = true
        }
      }

      OnboardingScreen(
        onFinished = goToLogin,
        onSkip = goToLogin
      )
    }

    authNavGraph(
      navController = navController
    )

    mainNavGraph(
      navController = navController
    )
  }
}