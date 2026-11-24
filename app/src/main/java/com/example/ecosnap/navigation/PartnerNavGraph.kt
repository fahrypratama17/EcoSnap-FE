package com.example.ecosnap.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.ecosnap.core.auth.AccountProfile
import com.example.ecosnap.features.deposits.DepositDetailRoute
import com.example.ecosnap.features.deposits.EstimateRoute
import com.example.ecosnap.features.deposits.PartnerDepositCreateRoute
import com.example.ecosnap.features.deposits.PartnerDepositsRoute
import com.example.ecosnap.features.home.PartnerHomeRoute
import com.example.ecosnap.features.map.PartnerDetailRoute
import com.example.ecosnap.features.map.PartnerMapRoute
import com.example.ecosnap.features.notifications.NotificationsRoute

// Operational mitra routes stay separate from user-only QR claims, rewards and gamification.
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
      onLogout = onLogout,
      onCreateDeposit = { navController.navigate(Routes.PARTNER_DEPOSIT_CREATE) },
      onDeposits = { navController.navigate(Routes.PARTNER_DEPOSITS) },
      onNotifications = { navController.navigate(Routes.NOTIFICATION) },
      onProfile = { navController.navigate(Routes.PROFILE) },
      onEstimate = { navController.navigate(Routes.ESTIMATE) }
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
    PartnerDetailRoute(
      accountUid = account.uid,
      partnerId = entry.arguments?.getString("partnerId").orEmpty(),
      onBack = { navController.popBackStack() },
      onNotifications = { navController.navigate(Routes.NOTIFICATION) },
      onProfile = { navController.navigate(Routes.PROFILE) }
    )
  }
  composable(Routes.PARTNER_DEPOSITS) {
    PartnerDepositsRoute(
      accountUid = account.uid,
      onBack = { navController.popBackStack() },
      onCreate = { navController.navigate(Routes.PARTNER_DEPOSIT_CREATE) },
      onOpen = { navController.navigate(Routes.partnerDepositDetail(it)) }
    )
  }
  composable(Routes.PARTNER_DEPOSIT_CREATE) {
    PartnerDepositCreateRoute(
      accountUid = account.uid,
      onBack = { navController.popBackStack() },
      onCreated = { id ->
        navController.navigate(Routes.partnerDepositDetail(id)) {
          popUpTo(Routes.PARTNER_DEPOSIT_CREATE) { inclusive = true }
          launchSingleTop = true
        }
      }
    )
  }
  composable("${Routes.PARTNER_DEPOSIT_DETAIL}/{depositId}") { entry ->
    DepositDetailRoute(
      accountUid = account.uid,
      depositId = entry.arguments?.getString("depositId").orEmpty(),
      isPartner = true,
      onBack = { navController.popBackStack() },
      onNotifications = { navController.navigate(Routes.NOTIFICATION) },
      onProfile = { navController.navigate(Routes.PROFILE) }
    )
  }
  composable(Routes.ESTIMATE) {
    EstimateRoute(accountUid = account.uid, onBack = { navController.popBackStack() })
  }
  composable(Routes.NOTIFICATION) {
    NotificationsRoute(
      accountUid = account.uid,
      isPartner = true,
      onBack = { navController.popBackStack() },
      onDeposit = { navController.navigate(Routes.partnerDepositDetail(it)) },
      onVoucher = null,
      onMissions = null
    )
  }
  profileNavGraph(navController, account, onRefreshAccount, onLogout)
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
