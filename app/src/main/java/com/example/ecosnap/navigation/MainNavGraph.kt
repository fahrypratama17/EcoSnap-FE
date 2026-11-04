package com.example.ecosnap.navigation

import android.content.ActivityNotFoundException
import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
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
import com.example.ecosnap.features.articles.ArticleDetailScreen
import com.example.ecosnap.features.articles.ArticleListScreen
import com.example.ecosnap.features.articles.data.LocalArticles
import com.example.ecosnap.features.home.HomeScreen
import com.example.ecosnap.features.home.model.HomeUiState
import com.example.ecosnap.features.map.PartnerDetailScreen
import com.example.ecosnap.features.map.PartnerMapScreen
import com.example.ecosnap.features.map.model.DummyPartners

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
      val news = LocalArticles.latestNews

      MainTabScaffold(
        navController = navController,
        selectedRoute = Routes.HOME
      ) {
        HomeScreen(
          uiState = HomeUiState.demo(),

          onNotification = {
            navController.navigate(Routes.NOTIFICATION)
          },

          onProfile = {
            navController.navigate(Routes.PROFILE)
          },

          onRedeemPoints = {
            navController.navigate(Routes.POINTS)
          },

          onScanQr = {
            navController.navigate(Routes.SCAN)
          },

          onFindPartner = {
            navController.navigate(Routes.MAP)
          },

          onHistory = {
            navController.navigate(Routes.HISTORY)
          },

          onScanWaste = {
            navController.navigate(Routes.SCAN)
          },

          onSeeALlMission = {
            navController.navigate(Routes.MISSIONS)
          },

          onSeeMap = {
            navController.navigate(Routes.MAP)
          },

          onPartnerClick = {
            navController.navigate(Routes.partnerDetail("tunas"))
          },

          newsTitle = news.title,
          newsSummary = news.summary,
          newsReadingMinutes = news.readingMinutes,

          onNewsClick = {
            navController.navigate(Routes.articleDetail(news.id))
          },

          onSeeAllArticles = {
            navController.navigate(Routes.ARTICLES)
          }
        )
      }
    }

    composable(Routes.MAP) {
      MainTabScaffold(
        navController = navController,
        selectedRoute = Routes.MAP
      ) {
        PartnerMapScreen(onPartnerDetail = {
          id -> navController.navigate(Routes.partnerDetail(id))
        })
      }
    }

    composable(route = "${Routes.PARTNER_DETAIL}/{partnerId}") { backStackEntry ->
      val partnerId = backStackEntry.arguments
        ?.getString("partnerId")
        .orEmpty()

      val partner = DummyPartners.findById(partnerId)
      val context = LocalContext.current

      MainTabScaffold(
        navController = navController,
        selectedRoute = Routes.MAP
      ) {
        if (partner != null) {
          PartnerDetailScreen(
            partner = partner,

            onBack = {navController.popBackStack()},

            onNotifications = {navController.navigate(Routes.NOTIFICATION)},

            onProfile = {navController.navigate(Routes.PROFILE)},

            onContact = {
              val phone = partner.phone.filter {
                it.isDigit() || it == '+'
              }

              val intent = Intent(
                Intent.ACTION_DIAL,
                "tel:$phone".toUri()
              )

              try {
                context.startActivity(intent)
              } catch (_: ActivityNotFoundException) {
              }
            },onDirections = {
              val lat = partner.location.latitude
              val lon = partner.location.longitude

              val geoIntent = Intent(
                Intent.ACTION_VIEW,
                "geo:0,0?q=$lat,$lon".toUri()
              )

              try {
                context.startActivity(geoIntent)
              } catch (_: ActivityNotFoundException) {

                val osmUrl =
                  "https://www.openstreetmap.org/" +
                          "?mlat=$lat&mlon=$lon" +
                          "#map=17/$lat/$lon"

                context.startActivity(
                  Intent(
                    Intent.ACTION_VIEW,
                    osmUrl.toUri()
                  )
                )
              }
            }
          )
        } else {
          MainPlaceholder(
            title = "Mitra tidak ditemukan",
            onBack = {
              navController.popBackStack()
            }
          )
        }
      }
    }

    composable(Routes.ARTICLES) {
      MainTabScaffold(
        navController = navController,
        selectedRoute = Routes.HOME
      ) {
        ArticleListScreen(
          onBack = { navController.popBackStack() },

          onArticleClick = { articleId ->
            navController.navigate(
              Routes.articleDetail(articleId)
            )
          },

          onOpenScan = { navController.navigate(Routes.SCAN) },

          onNotifications = { navController.navigate(Routes.NOTIFICATION) },

          onProfile = { navController.navigate(Routes.PROFILE) },
        )
      }
    }

    composable(route = "${Routes.ARTICLE_DETAIL}/{articleId}") {
      backStackEntry ->

      val articleId = backStackEntry.arguments?.getString("articleId").orEmpty()

      val article = LocalArticles.findById(articleId)

      MainTabScaffold(
        navController = navController,
        selectedRoute = Routes.HOME
      ) {
        if (article != null) {
          ArticleDetailScreen(
            article = article,

            onBack = { navController.popBackStack() },

            onFinished = { navController.popBackStack() },

            onNotifications = { navController.navigate(Routes.NOTIFICATION) },

            onProfile = { navController.navigate(Routes.PROFILE) }
          )
        } else {
          MainPlaceholder(
            title =  "Artikel tidak ditemukan",
            onBack = { navController.popBackStack() }
          )
        }
      }
    }

    listOf(
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
fun MainTabScaffold(
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