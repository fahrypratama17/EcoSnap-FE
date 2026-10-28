package com.example.ecosnap.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.ecosnap.core.designsystem.components.EcoButton

fun NavGraphBuilder.mainNavGraph(
  navController: NavController
) {
  navigation(
    route = Routes.MAIN_GRAPH,
    startDestination = Routes.HOME
  ) {
    composable(Routes.HOME) {

    }
  }
}

@Composable
private fun HomePlaceholderScreen(
  onLogout: () -> Unit,
) {
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier.padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
      Text(
        text = "Welcome to EcoSnap!",
        style = MaterialTheme.typography.titleLarge
      )

      EcoButton(
        text = "Kembali ke Login",
        onClick = onLogout
      )
    }
  }
}