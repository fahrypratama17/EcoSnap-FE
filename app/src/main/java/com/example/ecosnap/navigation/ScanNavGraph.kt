package com.example.ecosnap.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.scan.ScanQrScreen
import com.example.ecosnap.features.scan.ScanSuccessScreen
import com.example.ecosnap.features.scan.data.LocalDeposits

fun NavGraphBuilder.scanNavGraph(
  navController: NavHostController
) {
  composable(Routes.SCAN_QR) {
    MainTabScaffold(
      navController = navController,
      selectedRoute = Routes.SCAN
    ) {
      ScanQrScreen(
        onBack = { navController.popBackStack() },
        onQrScanned = { payload ->
          navController.navigate(Routes.scanQrResult(payload)) {
            popUpTo(Routes.SCAN_QR) {
              inclusive = true
            }
          }
        }
      )
    }
  }

  composable("${Routes.SCAN_QR_RESULT}/{transactionId}") { backStackEntry ->
    val transactionId = backStackEntry.arguments
      ?.getString("transactionId")
      .orEmpty()

    val transaction = LocalDeposits.findById(transactionId)

    MainTabScaffold(
      navController = navController,
      selectedRoute = Routes.SCAN
    ) {
      if (transaction != null) {
        ScanSuccessScreen(
          transaction = transaction,
          onSeeHistory = { navController.navigate(Routes.HISTORY) },
          onBackToHome = {
            navController.navigate(Routes.HOME) {
              popUpTo(Routes.HOME)
              launchSingleTop = true
            }
          },
          onNotifications = { navController.navigate(Routes.NOTIFICATION) },
          onProfile = { navController.navigate(Routes.PROFILE) }
        )
      } else {
        InvalidQrScreen(
          onRescan = {
            navController.popBackStack()
            navController.navigate(Routes.SCAN_QR)
          }
        )
      }
    }
  }
}

@Composable
private fun InvalidQrScreen(
  onRescan: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = "QR tidak dikenali",
      style = MaterialTheme.typography.titleLarge,
      color = EcoColor.Green900
    )

    Spacer(Modifier.height(8.dp))

    Text(
      text = "Pastikan kamu memindai QR transaksi dari mitra EcoSnap.",
      style = MaterialTheme.typography.bodyMedium,
      color = EcoColor.Green700,
      textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(16.dp))

    EcoButton(
      text = "Scan Ulang",
      onClick = onRescan
    )
  }
}
