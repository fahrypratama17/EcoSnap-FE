package com.example.ecosnap.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.composables.icons.lucide.Gamepad2
import com.composables.icons.lucide.Gift
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Map
import com.composables.icons.lucide.ScanLine
import com.example.ecosnap.core.designsystem.components.EcoBottomItem
import com.example.ecosnap.core.designsystem.components.EcoBottomNavigation
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.features.home.HomeScreen
import com.example.ecosnap.features.home.model.HomeUiState

private val bottomItems = listOf(
  EcoBottomItem(
    route = Routes.HOME,
    label = "Beranda",
    icon = Lucide.House
  ),
  EcoBottomItem(
    route = Routes.MAP,
    label = "Peta",
    icon = Lucide.Map
  ),
  EcoBottomItem(
    route = Routes.SCAN,
    label = "Scan",
    icon = Lucide.ScanLine
  ),
  EcoBottomItem(
    route = Routes.POINTS,
    label = "Point",
    icon = Lucide.Gift
  ),
  EcoBottomItem(
    route = Routes.GAMES,
    label = "Games",
    icon = Lucide.Gamepad2
  ),
)

fun NavGraphBuilder.mainNavGraph(
  navController: NavHostController
) {
  navigation(
    route = Routes.MAIN_GRAPH,
    startDestination = Routes.HOME
  ) {
    composable(Routes.HOME) {
      MainTabScaffold(
        navController = navController,
        selectedRoute = Routes.HOME
      ) {
        HomeScreen(
          uiState = HomeUiState.demo(),

          onNotification = {
            navController.navigate(
              Routes.NOTIFICATION
            )
          },

          onProfile = {
            navController.navigate(
              Routes.PROFILE
            )
          },

          onRedeemPoints = {
            navController.navigate(
              Routes.POINTS
            )
          },

          onScanQr = {
            navController.navigate(
              Routes.SCAN
            )
          },

          onFindPartner = {
            navController.navigate(
              Routes.MAP
            )
          },

          onHistory = {
            navController.navigate(
              Routes.HISTORY
            )
          },

          onScanWaste = {
            navController.navigate(
              Routes.SCAN
            )
          },

          onSeeALlMission = {
            navController.navigate(
              Routes.MISSIONS
            )
          },

          onSeeMap = {
            navController.navigate(
              Routes.MAP
            )
          },

          onPartnerClick = {
            navController.navigate(
              Routes.PARTNER_DETAIL
            )
          }
        )
      }
    }

    listOf(
      Routes.MAP to "Peta Mitra",
      Routes.SCAN to "Scan Sampah",
      Routes.POINTS to "Poin & Reward",
      Routes.GAMES to "Games",
    ).forEach { (route, title) ->
      composable(route) {
        MainTabScaffold(
          navController = navController,
          selectedRoute = route
        ) {
          MainPlaceholder(
            title = title
          )
        }
      }
    }

    listOf(
      Routes.NOTIFICATION to "Notifikasi",
      Routes.PROFILE to "Profil",
      Routes.HISTORY to "Riwayat Setoran",
      Routes.MISSIONS to "Semua Misi",
      Routes.PARTNER_DETAIL to "Detail Mitra",
    ).forEach { (route, title) ->
      composable(route) {
        MainPlaceholder(
          title = title,
          onBack = {
            navController.popBackStack()
          }
        )
      }
    }
  }
}

@Composable
private fun MainTabScaffold(
  navController: NavHostController,
  selectedRoute: String,
  content: @Composable () -> Unit
) {
  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    bottomBar = {
      EcoBottomNavigation(
        items = bottomItems,
        selectedRoute = selectedRoute,

        onNavigate = { route ->
          navController.navigate(route) {
            popUpTo(Routes.HOME) {
              saveState = true
            }
            launchSingleTop = true
            restoreState = true
          }
        }
      )
    }
  ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        content()
      }
  }
}

@Composable
private fun MainPlaceholder(
  title: String,
  onBack: (() -> Unit)? = null
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleLarge
    )

    if (onBack != null){
      Spacer(Modifier.height(16.dp))

      EcoButton(
        text = "Kembali",
        onClick = onBack
      )
    }
  }
}