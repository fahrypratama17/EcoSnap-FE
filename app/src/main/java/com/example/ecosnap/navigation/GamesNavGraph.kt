package com.example.ecosnap.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.ecosnap.core.designsystem.components.EcoAppHeader
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.games.GameDetailScreen
import com.example.ecosnap.features.games.GamePlayScreen
import com.example.ecosnap.features.games.GamesCatalogScreen
import com.example.ecosnap.features.games.GamesHomeScreen
import com.example.ecosnap.features.games.data.LocalGames

fun NavGraphBuilder.gamesNavGraph(
  navController: NavHostController
) {
  composable(Routes.GAMES) {
    MainTabScaffold(
      navController = navController,
      selectedRoute = Routes.GAMES
    ) {
      GamesHomeScreen(
        onAllGames = {
          navController.navigate(Routes.GAMES_ALL)
        },

        onGameDetail = { id ->
          navController.navigate(Routes.gameDetail(id))
        },

        onViewMission = {
          navController.navigate(Routes.MISSIONS)
        },

        onLeaderboard = {
          navController.navigate(Routes.GAME_LEADERBOARD)
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

  composable(Routes.GAMES_ALL) {
    MainTabScaffold(
      navController = navController,
      selectedRoute = Routes.GAMES
    ) {
      GamesCatalogScreen(
        onBack = { navController.popBackStack() },

        onGameDetail = { id ->
          navController.navigate(Routes.gameDetail(id))
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

  composable(route = "${Routes.GAME_DETAIL}/{gameId}") { entry ->
    val gameId = entry.arguments?.getString("gameId").orEmpty()

    val game = LocalGames.findById(gameId)

    MainTabScaffold(
      navController = navController,
      selectedRoute = Routes.GAMES
    ) {
      if (game != null) {
        GameDetailScreen(
          game = game,

          onBack = { navController.popBackStack() },

          onStartGame = {navController.navigate(Routes.gamePlay(game.id))},

          onNotifications = { navController.navigate(Routes.NOTIFICATION)},

          onProfile = { navController.navigate(Routes.PROFILE)}
        )
      } else {
          GameNotFound(
            onBack = { navController.popBackStack() }
        )
      }
    }
  }

  composable(route = "${Routes.GAME_PLAY}/{gameId}") { entry ->
    val gameId = entry.arguments?.getString("gameId").orEmpty()
    val game = LocalGames.findById(gameId)

    MainTabScaffold(
      navController = navController,
      selectedRoute = Routes.GAMES
    ) {
      if (game != null) {
        GamePlayScreen(
          game = game,

          onBack = {navController.popBackStack()},

          onAllGames = {
            navController.navigate(Routes.GAMES_ALL) {
              popUpTo(Routes.GAMES) {
                inclusive = false
              }
              launchSingleTop = true
            }
          },

          onNotifications = {
            navController.navigate(Routes.NOTIFICATION)
          },

          onProfile = {
            navController.navigate(Routes.PROFILE)
          }
        )
      } else {
        GameNotFound( onBack = {
          navController.popBackStack()
        })
      }
    }
  }

  composable(Routes.GAME_LEADERBOARD) {
    MainTabScaffold(
      navController = navController,
      selectedRoute = Routes.GAMES
    ) {
      GamesLeaderboardPlaceholder(
        onBack = { navController.popBackStack() },
        onNotifications = {navController.navigate(Routes.NOTIFICATION)},
        onProfile = {navController.navigate(Routes.PROFILE)}
      )
    }
  }
}

@Composable
private fun GameNotFound(
  onBack: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = "Game tidak ditemukan",
      style = MaterialTheme.typography.titleMedium
    )

    Spacer(Modifier.height(12.dp))

    TextButton(onClick = onBack) {
      Text("Kembali")
    }
  }
}

@Composable
private fun GamesLeaderboardPlaceholder(
  onBack: () -> Unit,
  onNotifications: () -> Unit,
  onProfile: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.White)
  ) {
    EcoAppHeader(
      onNotifications = onNotifications,
      onProfile = onProfile
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = "Leaderboard Mingguan",
        style = MaterialTheme.typography.titleLarge,
        color = EcoColor.Green900
      )

      Spacer(Modifier.height(12.dp))

      Text(
        text = "Perangkat belum tersedia karena data pemain masih lokal,",
        style = MaterialTheme.typography.bodyMedium,
        color = EcoColor.Green700
      )

      Spacer(Modifier.height(16.dp))

      TextButton(onClick = onBack) {
        Text("Kembali ke Games")
      }
    }
  }
}