package com.example.ecosnap.features.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecosnap.core.auth.AccountProfile
import com.example.ecosnap.core.auth.AccountRole
import com.example.ecosnap.features.profile.model.ProfileMenuItemData

/** Profile with fresh GET /me plus real stats; reloads on every resume. State is keyed per account UID. */
@Composable
fun ProfileRoute(
  account: AccountProfile,
  onBack: (() -> Unit)?,
  onSettingsClick: () -> Unit,
  onProfileClick: () -> Unit,
  onEditProfileClick: () -> Unit,
  onMenuClick: (ProfileMenuItemData) -> Unit,
  onLogoutClick: () -> Unit
) {
  val context = LocalContext.current
  val viewModel: ProfileViewModel = viewModel(
    key = "profile-${account.uid}",
    factory = ProfileViewModelFactory(context, account.role)
  )
  val state by viewModel.state.collectAsStateWithLifecycle()
  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }

  ProfileScreen(
    account = account,
    state = state,
    onRetry = viewModel::retry,
    onBack = onBack,
    onSettingsClick = onSettingsClick,
    onProfileClick = onProfileClick,
    onEditProfileClick = onEditProfileClick,
    onMenuClick = onMenuClick,
    onLogoutClick = onLogoutClick
  )
}

@Composable
fun AccountSettingsRoute(
  account: AccountProfile,
  onBack: () -> Unit,
  onSettingsClick: () -> Unit,
  onProfileClick: () -> Unit
) {
  val isUser = account.role == AccountRole.USER
  val viewModel: SettingsViewModel = viewModel(
    key = "settings-${account.uid}",
    factory = SettingsViewModelFactory(isUser)
  )
  val state by viewModel.state.collectAsStateWithLifecycle()
  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }

  AccountSettingsScreen(
    state = state,
    isUser = isUser,
    onRetry = viewModel::retry,
    onNotificationChange = viewModel::setNotificationEnabled,
    onLeaderboardChange = viewModel::setShowOnLeaderboard,
    onBack = onBack,
    onSettingsClick = onSettingsClick,
    onProfileClick = onProfileClick
  )
}

@Composable
fun AccountSecurityRoute(
  account: AccountProfile,
  onBack: () -> Unit,
  onSettingsClick: () -> Unit,
  onProfileClick: () -> Unit,
  onProfileSaved: () -> Unit
) {
  val viewModel: AccountViewModel = viewModel(
    key = "account-${account.uid}",
    factory = AccountViewModelFactory()
  )
  val state by viewModel.state.collectAsStateWithLifecycle()
  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }
  // Refresh the root account only after the server confirmed a PATCH.
  LaunchedEffect(state.saveCount) { if (state.saveCount > 0) onProfileSaved() }

  AccountSecurityScreen(
    state = state,
    onRetry = viewModel::retry,
    onSave = viewModel::save,
    onResetPassword = viewModel::sendPasswordReset,
    onBack = onBack,
    onSettingsClick = onSettingsClick,
    onProfileClick = onProfileClick
  )
}
