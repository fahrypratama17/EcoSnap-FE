package com.example.ecosnap.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.ecosnap.core.auth.AccountProfile
import com.example.ecosnap.features.admin.AdminRoute

// Admin only reviews partner registrations; none of the user or mitra graphs are reachable.
fun NavGraphBuilder.adminNavGraph(account: AccountProfile, onLogout: () -> Unit) {
  composable(Routes.ADMIN_HOME) {
    AdminRoute(accountUid = account.uid, onLogout = onLogout)
  }
}
