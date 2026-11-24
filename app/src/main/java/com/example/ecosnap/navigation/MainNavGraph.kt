package com.example.ecosnap.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.example.ecosnap.core.auth.AccountProfile
import com.example.ecosnap.core.designsystem.components.EcoBottomItem
import com.example.ecosnap.core.designsystem.components.EcoBottomNavigation
import com.example.ecosnap.features.articles.ArticleDetailRoute
import com.example.ecosnap.features.articles.ArticleListRoute
import com.example.ecosnap.features.deposits.EstimateRoute
import com.example.ecosnap.features.home.HomeRoute
import com.example.ecosnap.features.home.MissionsRoute
import com.example.ecosnap.features.map.PartnerDetailRoute
import com.example.ecosnap.features.map.PartnerMapRoute
import com.example.ecosnap.features.notifications.NotificationsRoute
import com.example.ecosnap.features.wallet.PointHistoryRoute
import com.example.ecosnap.features.wallet.PointsRoute

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
  navController: NavHostController,
  account: AccountProfile,
  onRefreshAccount: () -> Unit,
  onLogout: () -> Unit
) {
  navigation(
    route = Routes.MAIN_GRAPH,
    startDestination = Routes.HOME
  ) {
    composable(Routes.HOME) {
      MainTabScaffold(navController, Routes.HOME) {
        HomeRoute(
          accountUid = account.uid,
          onNotification = { navController.navigate(Routes.NOTIFICATION) },
          onProfile = { navController.navigate(Routes.PROFILE) },
          onRedeemPoints = { navController.navigate(Routes.POINTS) },
          onScanQr = { navController.navigate(Routes.SCAN_QR) },
          onFindPartner = { navController.navigate(Routes.MAP) },
          onHistory = { navController.navigate(Routes.HISTORY) },
          onScanWaste = { navController.navigate(Routes.SCAN) },
          onSeeAllMissions = { navController.navigate(Routes.MISSIONS) },
          onSeeMap = { navController.navigate(Routes.MAP) },
          onPartnerClick = { id -> navController.navigate(Routes.partnerDetail(id)) },
          onNewsClick = { id -> navController.navigate(Routes.articleDetail(id)) },
          onSeeAllArticles = { navController.navigate(Routes.ARTICLES) }
        )
      }
    }

    composable(Routes.MISSIONS) {
      MainTabScaffold(navController, Routes.HOME) {
        MissionsRoute(accountUid = account.uid, onBack = { navController.popBackStack() })
      }
    }

    composable(Routes.MAP) {
      MainTabScaffold(navController, Routes.MAP) {
        PartnerMapRoute(accountUid = account.uid) { id ->
          navController.navigate(Routes.partnerDetail(id))
        }
      }
    }

    composable(route = "${Routes.PARTNER_DETAIL}/{partnerId}") { entry ->
      val partnerId = entry.arguments?.getString("partnerId").orEmpty()
      MainTabScaffold(navController, Routes.MAP) {
        PartnerDetailRoute(
          accountUid = account.uid,
          partnerId = partnerId,
          onBack = { navController.popBackStack() },
          onNotifications = { navController.navigate(Routes.NOTIFICATION) },
          onProfile = { navController.navigate(Routes.PROFILE) }
        )
      }
    }

    composable(Routes.ARTICLES) {
      MainTabScaffold(navController, Routes.HOME) {
        ArticleListRoute(
          accountUid = account.uid,
          onBack = { navController.popBackStack() },
          onArticleClick = { id -> navController.navigate(Routes.articleDetail(id)) },
          onOpenScan = { navController.navigate(Routes.SCAN) },
          onNotifications = { navController.navigate(Routes.NOTIFICATION) },
          onProfile = { navController.navigate(Routes.PROFILE) }
        )
      }
    }

    composable(route = "${Routes.ARTICLE_DETAIL}/{articleId}") { entry ->
      val articleId = entry.arguments?.getString("articleId").orEmpty()
      MainTabScaffold(navController, Routes.HOME) {
        ArticleDetailRoute(
          accountUid = account.uid,
          articleId = articleId,
          onBack = { navController.popBackStack() },
          onFinished = { navController.popBackStack() },
          onNotifications = { navController.navigate(Routes.NOTIFICATION) },
          onProfile = { navController.navigate(Routes.PROFILE) }
        )
      }
    }

    composable(Routes.POINTS) {
      MainTabScaffold(navController, Routes.POINTS) {
        PointsRoute(
          accountUid = account.uid,
          onVoucher = { navController.navigate(Routes.voucherDetail(it)) },
          onHistory = { navController.navigate(Routes.HISTORY) },
          onVouchers = { navController.navigate(Routes.VOUCHERS) },
          onEstimate = { navController.navigate(Routes.ESTIMATE) },
          onLeaderboard = { navController.navigate(Routes.DEPOSIT_LEADERBOARD) }
        )
      }
    }

    composable(Routes.HISTORY) {
      MainTabScaffold(navController, Routes.HOME) {
        PointHistoryRoute(
          accountUid = account.uid,
          onBack = { navController.popBackStack() },
          onDeposit = { navController.navigate(Routes.scanQrResult(it)) },
          onVoucher = { navController.navigate(Routes.voucherDetail(it)) }
        )
      }
    }


    composable(Routes.ESTIMATE) {
      MainTabScaffold(navController, Routes.POINTS) {
        EstimateRoute(accountUid = account.uid, onBack = { navController.popBackStack() })
      }
    }

    composable(Routes.NOTIFICATION) {
      MainTabScaffold(navController, Routes.HOME) {
        NotificationsRoute(
          accountUid = account.uid,
          isPartner = false,
          onBack = { navController.popBackStack() },
          onDeposit = { navController.navigate(Routes.scanQrResult(it)) },
          onVoucher = { navController.navigate(Routes.voucherDetail(it)) },
          onMissions = { navController.navigate(Routes.MISSIONS) }
        )
      }
    }

    gamesNavGraph(navController, account.uid)
    leaderboardNavGraph(navController, account.uid)
    photoNavGraph(navController)
    scanNavGraph(navController, account.uid)
    profileNavGraph(navController, account, onRefreshAccount, onLogout)
  }
}

@Composable
internal fun MainTabScaffold(
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
