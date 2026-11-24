package com.example.ecosnap.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.ecosnap.features.deposits.DepositDetailRoute
import com.example.ecosnap.features.scan.ScanQrRoute

fun NavGraphBuilder.scanNavGraph(
  navController: NavHostController,
  accountUid: String
) {
  composable(Routes.SCAN_QR) {
    MainTabScaffold(
      navController = navController,
      selectedRoute = Routes.SCAN
    ) {
      ScanQrRoute(
        accountUid = accountUid,
        onBack = { navController.popBackStack() },
        onClaimed = { depositId ->
          navController.navigate(Routes.scanQrResult(depositId)) {
            popUpTo(Routes.SCAN_QR) {
              inclusive = true
            }
          }
        }
      )
    }
  }

  // The argument is the deposit UUID; the QR token never appears in a route.
  composable("${Routes.SCAN_QR_RESULT}/{transactionId}") { backStackEntry ->
    val depositId = backStackEntry.arguments
      ?.getString("transactionId")
      .orEmpty()

    MainTabScaffold(
      navController = navController,
      selectedRoute = Routes.SCAN
    ) {
      DepositDetailRoute(
        accountUid = accountUid,
        depositId = depositId,
        isPartner = false,
        onBack = {
          navController.navigate(Routes.HOME) {
            popUpTo(Routes.HOME)
            launchSingleTop = true
          }
        },
        onNotifications = { navController.navigate(Routes.NOTIFICATION) },
        onProfile = { navController.navigate(Routes.PROFILE) }
      )
    }
  }
}
