package com.example.ecosnap.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.ecosnap.core.auth.AccountProfile
import com.example.ecosnap.features.home.PartnerHomeRoute
import com.example.ecosnap.features.map.PartnerDetailRoute
import com.example.ecosnap.features.map.PartnerMapRoute
import kotlinx.coroutines.launch

// Mitra can browse locations and favorites, but never enter user-only reward/game/claim routes.
fun NavGraphBuilder.partnerNavGraph(
  navController: NavHostController,
  account: AccountProfile,
  onRefreshAccount: () -> Unit,
  onLogout: () -> Unit
) {
  composable(Routes.PARTNER_HOME) {
    PartnerHomeRoute(
      account = account,
      onRefreshAccount = onRefreshAccount,
      onBrowsePartners = { navController.navigate(Routes.MAP) },
      onLogout = onLogout
    )
  }
  composable(Routes.MAP) {
    PartnerBrowseScaffold(onBack = { navController.popBackStack() }) {
      PartnerMapRoute(accountUid = account.uid) { id ->
        navController.navigate(Routes.partnerDetail(id))
      }
    }
  }
  composable("${Routes.PARTNER_DETAIL}/{partnerId}") { entry ->
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
      Box(Modifier.fillMaxSize().padding(padding)) {
        PartnerDetailRoute(
          accountUid = account.uid,
          partnerId = entry.arguments?.getString("partnerId").orEmpty(),
          onBack = { navController.popBackStack() },
          onNotifications = {
            scope.launch { snackbar.showSnackbar("Inbox notifikasi belum terhubung pada versi ini.") }
          },
          onProfile = {
            navController.navigate(Routes.PARTNER_HOME) {
              popUpTo(Routes.PARTNER_HOME)
              launchSingleTop = true
            }
          }
        )
      }
    }
  }
}

@Composable
private fun PartnerBrowseScaffold(onBack: () -> Unit, content: @Composable () -> Unit) {
  Column(Modifier.fillMaxSize()) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
      TextButton(onClick = onBack) { Text("Kembali") }
      Text("Data Mitra", style = MaterialTheme.typography.titleMedium)
    }
    Box(Modifier.weight(1f)) { content() }
  }
}
