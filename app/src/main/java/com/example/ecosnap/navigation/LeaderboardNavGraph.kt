package com.example.ecosnap.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.ecosnap.features.leaderboard.LeaderboardRoute

/**
 * Both legacy leaderboard routes open the same server weekly-XP leaderboard: the backend has no
 * deposit or game-score ranking, so neither entry point may present one.
 */
fun NavGraphBuilder.leaderboardNavGraph(
  navController: NavHostController,
  accountUid: String
) {
  composable(Routes.DEPOSIT_LEADERBOARD) {
    MainTabScaffold(
      navController = navController,
      selectedRoute = Routes.POINTS
    ) {
      LeaderboardRoute(
        accountUid = accountUid,
        onBack = { navController.popBackStack() },
        onNotifications = { navController.navigate(Routes.NOTIFICATION) },
        onProfile = { navController.navigate(Routes.PROFILE) }
      )
    }
  }

  composable(Routes.GAME_LEADERBOARD) {
    MainTabScaffold(
      navController = navController,
      selectedRoute = Routes.GAMES
    ) {
      LeaderboardRoute(
        accountUid = accountUid,
        onBack = { navController.popBackStack() },
        onNotifications = { navController.navigate(Routes.NOTIFICATION) },
        onProfile = { navController.navigate(Routes.PROFILE) }
      )
    }
  }
}
