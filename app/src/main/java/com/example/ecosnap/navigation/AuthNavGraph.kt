package com.example.ecosnap.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.ecosnap.features.auth.ForgotPasswordScreen
import com.example.ecosnap.features.auth.LoginScreen
import com.example.ecosnap.features.auth.RegisterScreen
import com.example.ecosnap.features.auth.AuthViewModel

fun NavGraphBuilder.authNavGraph(
  navController: NavHostController,
  auth: AuthViewModel,
  onExit: () -> Unit
) {
  navigation(
    route = Routes.AUTH_GRAPH,
    startDestination = Routes.LOGIN
  ) {
    composable(Routes.LOGIN) {
      val state by auth.state.collectAsStateWithLifecycle()
      LoginScreen(
        onBack = { if (!auth.state.value.busy) onExit() },
        onRegisterClick = {
          if (!auth.state.value.busy) {
            auth.clearError()
            navController.navigate(Routes.REGISTER)
          }
        },
        onForgotPassword = {
          if (!auth.state.value.busy) {
            auth.clearError()
            navController.navigate(Routes.FORGOT_PASSWORD)
          }
        },
        onHelp = {
          if (!auth.state.value.busy) navController.navigate(Routes.HELP)
        },
        onLogin = auth::signIn,
        busy = state.busy,
        error = state.error,
        onInputChanged = auth::clearError
      )
    }

    composable(Routes.REGISTER) {
      val state by auth.state.collectAsStateWithLifecycle()
      val back: () -> Unit = {
        if (!auth.state.value.busy) {
          auth.clearError()
          navController.popBackStack()
        }
      }
      BackHandler(enabled = !state.busy, onBack = back)
      RegisterScreen(
        onBack = back,
        onLoginClick = back,
        onHelp = {
          if (!auth.state.value.busy) navController.navigate(Routes.HELP)
        },
        onRegister = auth::register,
        busy = state.busy,
        error = state.error,
        onInputChanged = auth::clearError
      )
    }

    composable(Routes.FORGOT_PASSWORD) {
      ForgotPasswordScreen(
        onBack = { navController.popBackStack() },
        onHelp = { navController.navigate(Routes.HELP) }
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