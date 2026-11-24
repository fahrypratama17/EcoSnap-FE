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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
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
  partnerDestination(Routes.PARTNER_HOME, navController) {
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
  partnerDestination(Routes.MAP, navController) {
    PartnerBrowseScaffold(onBack = { navController.popBackStack() }) {
      PartnerMapRoute(accountUid = account.uid) { id ->
        navController.navigate(Routes.partnerDetail(id))
      }
    }
  }
  partnerDestination("${Routes.PARTNER_DETAIL}/{partnerId}", navController, showHeader = false) { entry ->
    PartnerDetailRoute(
      accountUid = account.uid,
      partnerId = entry.arguments?.getString("partnerId").orEmpty(),
      onBack = { navController.popBackStack() },
      onNotifications = { navController.navigate(Routes.NOTIFICATION) },
      onProfile = { navController.navigate(Routes.PROFILE) }
    )
  }
  partnerDestination(Routes.PARTNER_DEPOSITS, navController, Routes.PARTNER_DEPOSITS) {
    PartnerDepositsRoute(
      accountUid = account.uid,
      onBack = { navController.popBackStack() },
      onCreate = { navController.navigate(Routes.PARTNER_DEPOSIT_CREATE) },
      onOpen = { navController.navigate(Routes.partnerDepositDetail(it)) }
    )
  }
  partnerDestination(Routes.PARTNER_DEPOSIT_CREATE, navController, Routes.PARTNER_DEPOSITS) { entry ->
    val initialItems = remember(entry) {
      entry.savedStateHandle.get<String>(PARTNER_DEPOSIT_DRAFT)
        ?: navController.previousBackStackEntry?.savedStateHandle
          ?.remove<String>(PARTNER_DEPOSIT_DRAFT)
          ?.also { entry.savedStateHandle[PARTNER_DEPOSIT_DRAFT] = it }
    }
    PartnerDepositCreateRoute(
      accountUid = account.uid,
      onBack = { navController.popBackStack() },
      onCreated = { id ->
        navController.navigate(Routes.partnerDepositDetail(id)) {
          popUpTo(Routes.PARTNER_DEPOSIT_CREATE) { inclusive = true }
          launchSingleTop = true
        }
      },
      initialItemsJson = initialItems
    )
  }
  partnerDestination("${Routes.PARTNER_DEPOSIT_DETAIL}/{depositId}", navController, Routes.PARTNER_DEPOSITS, showHeader = false) { entry ->
    DepositDetailRoute(
      accountUid = account.uid,
      depositId = entry.arguments?.getString("depositId").orEmpty(),
      isPartner = true,
      onBack = { navController.popBackStack() },
      onNotifications = { navController.navigate(Routes.NOTIFICATION) },
      onProfile = { navController.navigate(Routes.PROFILE) }
    )
  }
  partnerDestination(Routes.ESTIMATE, navController, Routes.ESTIMATE) {
    EstimateRoute(
      accountUid = account.uid,
      onBack = { navController.popBackStack() },
      onCreateDeposit = { items ->
        navController.currentBackStackEntry?.savedStateHandle?.set(PARTNER_DEPOSIT_DRAFT, items)
        navController.navigate(Routes.PARTNER_DEPOSIT_CREATE) { launchSingleTop = true }
      }
    )
  }
  partnerDestination(Routes.NOTIFICATION, navController) {
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

private const val PARTNER_DEPOSIT_DRAFT = "partner_deposit_draft"
