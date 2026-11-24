package com.example.ecosnap.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.ecosnap.features.points.PointsScreen
import com.example.ecosnap.features.points.VoucherDetailScreen
import com.example.ecosnap.features.points.data.LocalPointsData

fun NavGraphBuilder.pointsNavGraph(
  navController: NavHostController
) {
  composable(Routes.POINTS) {
    MainTabScaffold(
      navController = navController,
      selectedRoute = Routes.POINTS
    ) {
      PointsScreen(
        onNotifications = {
          navController.navigate(Routes.NOTIFICATION)
        },
        onProfile = {
          navController.navigate(Routes.PROFILE)
        },
        onScanQr = {
          navController.navigate(Routes.SCAN)
        },
        onCalculator = {
          navController.navigate(Routes.CALCULATOR)
        },
        onVoucherClick = { voucherId ->
          navController.navigate(Routes.voucherDetail(voucherId))
        },
        onSeeAllCategory = { categoryId ->
          navController.navigate(Routes.voucherDetail(categoryId))
        }
      )
    }
  }

  composable(route = "${Routes.VOUCHER_DETAIL}/{voucherId}") { backStackEntry ->
    val voucherId = backStackEntry.arguments
      ?.getString("voucherId")
      .orEmpty()
    val voucherDetail = LocalPointsData.findDetailById(voucherId)

    MainTabScaffold(
      navController = navController,
      selectedRoute = Routes.POINTS
    ) {
      VoucherDetailScreen(
        voucherDetail = voucherDetail,
        onBack = {
          navController.popBackStack()
        },
        onConfirmRedeem = {
          navController.popBackStack()
        },
        onNotifications = {
          navController.navigate(Routes.NOTIFICATION)
        },
        onProfile = {
          navController.navigate(Routes.PROFILE)
        }
      )
    }
  }
}
