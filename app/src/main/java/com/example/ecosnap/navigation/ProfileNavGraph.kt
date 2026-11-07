package com.example.ecosnap.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.ecosnap.features.profile.AccountSettingsScreen
import com.example.ecosnap.features.profile.HelpCenterScreen
import com.example.ecosnap.features.profile.ProfilePlaceholderScreen
import com.example.ecosnap.features.profile.ProfileScreen
import com.example.ecosnap.features.profile.model.ProfileMenuItemData

fun NavGraphBuilder.profileNavGraph(
  navController: NavHostController
) {

  composable(Routes.PROFILE) {
    MainTabScaffold(
      navController = navController,
      selectedRoute = Routes.PROFILE
    ) {
      ProfileScreen(
        onSettingsClick = {
          navController.navigate(
            Routes.PROFILE_ACCOUNT_SETTINGS
          )
        },
        onProfileClick = {

        },
        onEditProfileClick = {
          navController.navigate(
            Routes.PROFILE_PRIVACY_SECURITY
          )
        },
        onActivityMenuClick = { item ->
          navigateProfileActivityMenu(
            navController = navController,
            item = item
          )
        },
        onSupportMenuClick = { item ->
          navigateProfileSupportMenu(
            navController = navController,
            item = item
          )
        },
        onLogoutClick = {
          // TODO: Buat Nanti kalo udah nyambung auth
        }
      )
    }
  }

  composable(Routes.PROFILE_ACCOUNT_SETTINGS) {
    MainTabScaffold(
      navController = navController,
      selectedRoute = Routes.PROFILE
    ) {
      AccountSettingsScreen(
        onBack = {
          navController.popBackStack()
        },
        onSettingsClick = {},
        onProfileClick = {
          navController.navigate(Routes.PROFILE) {
            launchSingleTop = true
          }
        }
      )
    }
  }

  composable(Routes.PROFILE_HELP_CENTER) {
    MainTabScaffold(
      navController = navController,
      selectedRoute = Routes.PROFILE
    ) {
      HelpCenterScreen(
        onBack = {
          navController.popBackStack()
        },
        onSettingsClick = {
          navController.navigate(
            Routes.PROFILE_ACCOUNT_SETTINGS
          )
        },
        onProfileClick = {
          navController.navigate(Routes.PROFILE) {
            launchSingleTop = true
          }
        }
      )
    }
  }

  composable(Routes.PROFILE_ACTIVITY_HISTORY) {
    MainTabScaffold(
      navController = navController,
      selectedRoute = Routes.PROFILE
    ) {
      ProfilePlaceholderScreen(
        title = "Riwayat Aktivitas",
        subtitle = "Lihat aktivitas setoran dan transaksi poin.",
        onBack = { navController.popBackStack() },
        onSettingsClick = {
          navController.navigate(Routes.PROFILE_ACCOUNT_SETTINGS)
        },
        onProfileClick = {
          navController.navigate(Routes.PROFILE) {
            launchSingleTop = true
          }
        }
      )
    }
  }

  composable(Routes.PROFILE_REWARDS) {
    MainTabScaffold(
      navController = navController,
      selectedRoute = Routes.PROFILE
    ) {
      ProfilePlaceholderScreen(
        title = "Reward Saya",
        subtitle = "Lihat hadiah, voucher, dan reward yang dimiliki.",
        onBack = { navController.popBackStack() },
        onSettingsClick = {
          navController.navigate(Routes.PROFILE_ACCOUNT_SETTINGS)
        },
        onProfileClick = {
          navController.navigate(Routes.PROFILE) {
            launchSingleTop = true
          }
        }
      )
    }
  }

  composable(Routes.PROFILE_ACHIEVEMENTS) {
    MainTabScaffold(
      navController = navController,
      selectedRoute = Routes.PROFILE
    ) {
      ProfilePlaceholderScreen(
        title = "Pencapaian",
        subtitle = "Lihat badge dan progress aksi hijaumu.",
        onBack = { navController.popBackStack() },
        onSettingsClick = {
          navController.navigate(Routes.PROFILE_ACCOUNT_SETTINGS)
        },
        onProfileClick = {
          navController.navigate(Routes.PROFILE) {
            launchSingleTop = true
          }
        }
      )
    }
  }

  composable(Routes.PROFILE_ACTIVITY_REPORT) {
    MainTabScaffold(
      navController = navController,
      selectedRoute = Routes.PROFILE
    ) {
      ProfilePlaceholderScreen(
        title = "Laporan Aktivitas",
        subtitle = "Ringkasan aktivitas dan transaksi.",
        onBack = { navController.popBackStack() },
        onSettingsClick = {
          navController.navigate(Routes.PROFILE_ACCOUNT_SETTINGS)
        },
        onProfileClick = {
          navController.navigate(Routes.PROFILE) {
            launchSingleTop = true
          }
        }
      )
    }
  }

  composable(Routes.PROFILE_PRIVACY_SECURITY) {
    MainTabScaffold(
      navController = navController,
      selectedRoute = Routes.PROFILE
    ) {
      ProfilePlaceholderScreen(
        title = "Privasi & Keamanan",
        subtitle = "Kelola akun, keamanan, dan data.",
        onBack = { navController.popBackStack() },
        onSettingsClick = {
          navController.navigate(Routes.PROFILE_ACCOUNT_SETTINGS)
        },
        onProfileClick = {
          navController.navigate(Routes.PROFILE) {
            launchSingleTop = true
          }
        }
      )
    }
  }
}

private fun navigateProfileActivityMenu(
  navController: NavHostController,
  item: ProfileMenuItemData
) {
  when (item.title) {
    "Riwayat Aktivitas" -> {
      navController.navigate(Routes.PROFILE_ACTIVITY_HISTORY)
    }
    "Reward Saya" -> {
      navController.navigate(Routes.PROFILE_REWARDS)
    }
    "Pencapaian" -> {
      navController.navigate(Routes.PROFILE_ACHIEVEMENTS)
    }
    "Laporan Aktivitas" -> {
      navController.navigate(Routes.PROFILE_ACTIVITY_REPORT)
    }
  }
}

private fun navigateProfileSupportMenu(
  navController: NavHostController,
  item: ProfileMenuItemData
) {
  when (item.title) {
    "Pengaturan Akun" -> {
      navController.navigate(Routes.PROFILE_ACCOUNT_SETTINGS)
    }
    "Pusat Bantuan" -> {
      navController.navigate(Routes.PROFILE_HELP_CENTER)
    }
    "Privasi & Keamanan" -> {
      navController.navigate(Routes.PROFILE_PRIVACY_SECURITY)
    }
  }
}