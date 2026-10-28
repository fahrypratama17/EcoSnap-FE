package com.example.ecosnap.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.ecosnap.features.auth.LoginScreen
import com.example.ecosnap.features.auth.RegisterScreen

fun NavGraphBuilder.authNavGraph(
  navController: NavHostController
) {
  navigation(
    route = Routes.AUTH_GRAPH,
    startDestination = Routes.LOGIN
  ) {
    composable(Routes.LOGIN) {
      LoginScreen(
        onBack = {
          navController.navigate(
            Routes.ONBOARDING
          ) {
            popUpTo(Routes.AUTH_GRAPH) {
              inclusive = true
            }
            launchSingleTop = true
          }
        },

        onRegisterClick = {
          navController.navigate(
            Routes.REGISTER
          )
        },

        onForgotPassword = {
          navController.navigate(
            Routes.FORGOT_PASSWORD
          )
        },

        onHelp = {
          navController.navigate(
            Routes.HELP
          )
        },

        onLogin = { _, _ ->
          navController.navigate(
            Routes.MAIN_GRAPH
          ) {
            popUpTo(Routes.AUTH_GRAPH) {
              inclusive = true
            }
            launchSingleTop = true
          }
        }
      )
    }

    composable(Routes.REGISTER) {
      RegisterScreen(
        onBack = {
          navController.popBackStack()
        },

        onLoginClick = {
          navController.popBackStack()
        },

        onHelp = {
          navController.navigate(
            Routes.HELP
          )
        },

        onRegister = { _ ->
          navController.popBackStack()
        }
      )
    }

    composable(Routes.FORGOT_PASSWORD) {
      AuthPlaceholderScreen(
        title = "Hubungi Kami",
        onBack = {
          navController.popBackStack()
        }
      )
    }

    composable(Routes.HELP) {
      AuthPlaceholderScreen(
        title = "Hubungi Kami",
        onBack = {
          navController.popBackStack()
        }
      )
    }
  }
}

@Composable
private fun AuthPlaceholderScreen(
  title: String,
  onBack: () -> Unit
) {
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge
      )

      Button(onClick = onBack) {
        Text("Kembali")
      }
    }
  }
}