package com.example.ecosnap.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.composables.icons.lucide.Calculator
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.QrCode
import com.example.ecosnap.core.designsystem.components.EcoAppHeader
import com.example.ecosnap.core.designsystem.components.EcoBottomItem
import com.example.ecosnap.core.designsystem.components.EcoBottomNavigation
import com.example.ecosnap.core.designsystem.theme.EcoColor

private val partnerTabs = listOf(
  EcoBottomItem(Routes.PARTNER_HOME, "Beranda", Lucide.House),
  EcoBottomItem(Routes.PARTNER_DEPOSITS, "QR", Lucide.QrCode),
  EcoBottomItem(Routes.ESTIMATE, "Kalkulator", Lucide.Calculator)
)

/** Original mitra workspace layout; tab changes only navigate, never create or issue transactions. */
@Composable
internal fun PartnerTabScaffold(
  navController: NavHostController,
  selectedRoute: String,
  showHeader: Boolean = true,
  content: @Composable () -> Unit
) {
  Scaffold(
    containerColor = EcoColor.White,
    topBar = {
      if (showHeader) {
        EcoAppHeader(
          onNotifications = { navController.navigate(Routes.NOTIFICATION) { launchSingleTop = true } },
          onProfile = { navController.navigate(Routes.PROFILE) { launchSingleTop = true } }
        )
      }
    },
    bottomBar = {
      EcoBottomNavigation(
        items = partnerTabs,
        selectedRoute = selectedRoute,
        onNavigate = { route ->
          navController.navigate(route) {
            popUpTo(Routes.PARTNER_HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
          }
        }
      )
    }
  ) { padding ->
    Box(Modifier.fillMaxSize().padding(padding)) { content() }
  }
}

internal fun NavGraphBuilder.partnerDestination(
  route: String,
  navController: NavHostController,
  selectedRoute: String = Routes.PARTNER_HOME,
  showHeader: Boolean = true,
  content: @Composable (NavBackStackEntry) -> Unit
) {
  composable(route) { entry ->
    PartnerTabScaffold(navController, selectedRoute, showHeader) { content(entry) }
  }
}
