package com.example.ecosnap.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.ecosnap.core.auth.AccountProfile
import com.example.ecosnap.core.auth.AccountRole
import com.example.ecosnap.features.gamification.BadgesRoute
import com.example.ecosnap.features.profile.AccountSecurityRoute
import com.example.ecosnap.features.profile.AccountSettingsRoute
import com.example.ecosnap.features.profile.ProfileRoute
import com.example.ecosnap.features.reports.ActivityReportRoute
import com.example.ecosnap.features.support.SupportRoute
import com.example.ecosnap.features.wallet.PointHistoryRoute
import com.example.ecosnap.features.wallet.VoucherDetailRoute
import com.example.ecosnap.features.wallet.VoucherListRoute

/**
 * Account routes for both roles. Point history, vouchers and badges are registered only for users, so
 * a mitra NavHost cannot reach them. Mitra screens render without the user bottom tabs.
 */
fun NavGraphBuilder.profileNavGraph(
  navController: NavHostController,
  account: AccountProfile,
  onRefreshAccount: () -> Unit,
  onLogout: () -> Unit
) {
  val isUser = account.role == AccountRole.USER
  val back: () -> Unit = { navController.popBackStack() }
  val openSettings: () -> Unit = {
    navController.navigate(Routes.PROFILE_ACCOUNT_SETTINGS) { launchSingleTop = true }
  }
  val openProfile: () -> Unit = {
    if (!navController.popBackStack(Routes.PROFILE, false)) {
      navController.navigate(Routes.PROFILE) { launchSingleTop = true }
    }
  }

  composable(Routes.PROFILE) {
    ProfileScaffold(navController, isUser, Routes.PROFILE) {
      ProfileRoute(
        account = account,
        onBack = if (isUser) null else back,
        onSettingsClick = openSettings,
        onProfileClick = openProfile,
        onEditProfileClick = {
          navController.navigate(Routes.PROFILE_PRIVACY_SECURITY) { launchSingleTop = true }
        },
        onMenuClick = { item ->
          navController.navigate(item.route) { launchSingleTop = true }
        },
        onLogoutClick = onLogout
      )
    }
  }

  composable(Routes.PROFILE_ACCOUNT_SETTINGS) {
    ProfileScaffold(navController, isUser, Routes.PROFILE) {
      AccountSettingsRoute(
        account = account,
        onBack = back,
        onSettingsClick = openSettings,
        onProfileClick = openProfile
      )
    }
  }

  composable(Routes.PROFILE_PRIVACY_SECURITY) {
    ProfileScaffold(navController, isUser, Routes.PROFILE) {
      AccountSecurityRoute(
        account = account,
        onBack = back,
        onSettingsClick = openSettings,
        onProfileClick = openProfile,
        onProfileSaved = onRefreshAccount
      )
    }
  }

  composable(Routes.PROFILE_HELP_CENTER) {
    ProfileScaffold(navController, isUser, Routes.PROFILE) {
      SupportRoute(accountUid = account.uid, onBack = back)
    }
  }

  composable(Routes.PROFILE_ACTIVITY_REPORT) {
    ProfileScaffold(navController, isUser, Routes.PROFILE) {
      ActivityReportRoute(accountUid = account.uid, isPartner = !isUser, onBack = back)
    }
  }

  if (!isUser) return

  composable(Routes.PROFILE_ACHIEVEMENTS) {
    ProfileScaffold(navController, true, Routes.PROFILE) {
      BadgesRoute(accountUid = account.uid, onBack = back)
    }
  }

  composable(Routes.PROFILE_ACTIVITY_HISTORY) {
    ProfileScaffold(navController, true, Routes.POINTS) {
      PointHistoryRoute(
        accountUid = account.uid,
        onBack = back,
        onDeposit = { id -> navController.navigate(Routes.scanQrResult(id)) },
        onVoucher = { id -> navController.navigate(Routes.voucherDetail(id)) }
      )
    }
  }

  composable(Routes.VOUCHERS) {
    ProfileScaffold(navController, true, Routes.POINTS) {
      VoucherListRoute(
        accountUid = account.uid,
        onBack = back,
        onVoucher = { id -> navController.navigate(Routes.voucherDetail(id)) }
      )
    }
  }

  composable("${Routes.VOUCHER_DETAIL}/{voucherId}") { entry ->
    ProfileScaffold(navController, true, Routes.POINTS) {
      VoucherDetailRoute(
        accountUid = account.uid,
        voucherId = entry.arguments?.getString("voucherId").orEmpty(),
        onBack = back
      )
    }
  }
}

@Composable
private fun ProfileScaffold(
  navController: NavHostController,
  isUser: Boolean,
  selectedRoute: String,
  content: @Composable () -> Unit
) {
  if (isUser) {
    MainTabScaffold(
      navController = navController,
      selectedRoute = selectedRoute,
      showHeader = selectedRoute == Routes.POINTS,
      content = content
    )
  } else {
    PartnerTabScaffold(navController, Routes.PARTNER_HOME, showHeader = false, content = content)
  }
}
