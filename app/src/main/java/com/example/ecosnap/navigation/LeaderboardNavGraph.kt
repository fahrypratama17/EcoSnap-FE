package com.example.ecosnap.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.ecosnap.features.leaderboard.LeaderboardScreen
import com.example.ecosnap.features.leaderboard.model.LeaderboardMode

fun NavGraphBuilder.leaderboardNavGraph(
  navController: NavHostController
) {
  composable(Routes.DEPOSIT_LEADERBOARD) {
    MainTabScaffold(
      navController = navController,
      selectedRoute = Routes.POINTS
    ) {
      LeaderboardScreen(
        initialMode = LeaderboardMode.DEPOSIT,
        onBack = { navController.popBackStack() },
        onNotifications = {
          navController.navigate(
            Routes.NOTIFICATION
          )
        },
        onProfile = {
          navController.navigate(
            Routes.PROFILE
          )
        }
      )
    }
  }

  composable(Routes.GAME_LEADERBOARD) {
    MainTabScaffold(
      navController = navController,
      selectedRoute = Routes.GAMES
    ) {
      LeaderboardScreen(
        initialMode = LeaderboardMode.GAMES,
        onBack = { navController.popBackStack() },
        onNotifications = {
          navController.navigate(
            Routes.NOTIFICATION
          )
        },
        onProfile = {
          navController.navigate(
            Routes.PROFILE
          )
        }
      )
    }
  }
}