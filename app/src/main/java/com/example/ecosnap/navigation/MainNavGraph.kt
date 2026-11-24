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
import com.example.ecosnap.core.auth.AccountProfile
import com.example.ecosnap.core.designsystem.components.EcoBottomItem
import com.example.ecosnap.core.designsystem.components.EcoBottomNavigation
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.features.articles.ArticleDetailRoute
import com.example.ecosnap.features.articles.ArticleListRoute
import com.example.ecosnap.features.home.HomeRoute
import com.example.ecosnap.features.home.MissionsRoute
import com.example.ecosnap.features.map.PartnerDetailRoute
import com.example.ecosnap.features.map.PartnerMapRoute

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
      MainTabScaffold(
        navController = navController,
        selectedRoute = Routes.POINTS
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Text(
            text = "Poin & Reward",
            style = MaterialTheme.typography.titleLarge
          )

          Spacer(Modifier.height(16.dp))

          EcoButton(
            text = "Lihat Leaderboard Setoran",
            onClick = {
              navController.navigate(
                Routes.DEPOSIT_LEADERBOARD
              )
            }
          )
        }
      }
    }

    gamesNavGraph(navController)

    leaderboardNavGraph(navController)

    photoNavGraph(navController)

    scanNavGraph(navController)

    profileNavGraph(navController, account, onLogout)

    listOf(
      Routes.NOTIFICATION to "Notifikasi",
      Routes.HISTORY to "Riwayat Setoran",
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