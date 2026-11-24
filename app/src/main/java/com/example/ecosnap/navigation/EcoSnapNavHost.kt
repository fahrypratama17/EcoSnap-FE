package com.example.ecosnap.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.ecosnap.core.auth.AccountRole
import com.example.ecosnap.core.auth.SessionOutcome
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.auth.AuthViewModel
import com.example.ecosnap.features.auth.AuthViewModelFactory
import com.example.ecosnap.features.auth.PartnerAccountScreen
import com.example.ecosnap.features.auth.RegisterScreen
import com.example.ecosnap.features.auth.SessionUnavailableScreen
import com.example.ecosnap.features.onboarding.OnboardingScreen
import com.example.ecosnap.features.onboarding.SplashScreen

@Composable
fun EcoSnapNavHost(
  modifier: Modifier = Modifier,
  onExit: () -> Unit = {}
) {
  val context = LocalContext.current
  val factory = remember(context.applicationContext) { AuthViewModelFactory(context) }
  val auth: AuthViewModel = viewModel(factory = factory)
  val state by auth.state.collectAsStateWithLifecycle()

  // Protected graphs do not exist until restore succeeds. Replacing the branch
  // disposes its controller and all saved tab/back-stack state on logout.
  Box(modifier = modifier.fillMaxSize()) {
    if (!state.initialized) {
      if (state.busy || state.error == null) {
        SplashScreen()
      } else {
        SessionUnavailableScreen("", state.busy, state.error, auth::retry, auth::signOut)
      }
    } else {
      when (val session = state.session) {
        SessionOutcome.SignedOut -> {
          if (!state.onboardingCompleted) {
            OnboardingScreen(onFinished = auth::finishOnboarding, onSkip = auth::finishOnboarding)
            Column(Modifier.align(Alignment.BottomCenter).padding(24.dp)) {
              state.error?.let { Text(it, color = EcoColor.TextError) }
              if (state.busy) CircularProgressIndicator(color = EcoColor.Green700)
            }
          } else {
            key("signed-out") {
              val controller = rememberNavController()
              NavHost(controller, startDestination = Routes.AUTH_GRAPH) {
                authNavGraph(controller, auth, onExit)
              }
            }
          }
        }
        is SessionOutcome.NeedsProfile -> key("complete", session.email) {
          BackHandler(enabled = !state.busy, onBack = auth::signOut)
          var showHelp by remember { mutableStateOf(false) }
          RegisterScreen(
            onBack = auth::signOut,
            onLoginClick = auth::signOut,
            onHelp = { showHelp = true },
            onRegister = auth::register,
            completion = session,
            busy = state.busy,
            error = state.error,
            onInputChanged = auth::clearError
          )
          if (showHelp) {
            AlertDialog(
              onDismissRequest = { showHelp = false },
              title = { Text("Bantuan akun") },
              text = { Text("Lengkapi nama dan nomor telepon yang valid. Untuk mitra, isi data usaha. Jika layanan tidak tersedia, coba lagi atau keluar lalu masuk kembali dengan email akun yang sama.") },
              confirmButton = {
                TextButton(onClick = { showHelp = false }) { Text("Mengerti") }
              }
            )
          }
        }
        is SessionOutcome.Unavailable -> SessionUnavailableScreen(
          session.email, state.busy, state.error, auth::retry, auth::signOut
        )
        is SessionOutcome.Authenticated -> key(session.account.uid, session.account.role) {
          if (session.account.role == AccountRole.PARTNER) {
            PartnerAccountScreen(session.account, state.busy, state.error, auth::retry, auth::signOut)
          } else {
            val controller = rememberNavController()
            NavHost(controller, startDestination = Routes.MAIN_GRAPH) {
              mainNavGraph(controller, session.account, auth::signOut)
            }
          }
        }
      }
    }
  }
  BackHandler(enabled = state.busy) {}
}