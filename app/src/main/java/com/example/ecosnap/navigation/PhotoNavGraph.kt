package com.example.ecosnap.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ecosnap.features.photos.CameraScreen
import com.example.ecosnap.features.photos.PhotoResultScreen

fun NavGraphBuilder.photoNavGraph(
  navController: NavHostController
) {
  composable(Routes.SCAN) {
    MainTabScaffold(
      navController = navController,
      selectedRoute = Routes.SCAN
    ) {
      CameraScreen(
        onBack = {
          navController.popBackStack()
        },
        onImageReady = { photoUri ->
          navController.navigate(
            Routes.scanResult(photoUri)
          ) {
            launchSingleTop = true
          }
        }
      )
    }
  }
  composable(
    route = "${Routes.SCAN_RESULT}?photoUri={photoUri}",
    arguments = listOf(
      navArgument("photoUri") {
        type = NavType.StringType
        defaultValue = ""
      }
    )
  ) { backStackEntry ->

    val photoUriString = backStackEntry.arguments
      ?.getString("photoUri")
      .orEmpty()

    val photoUri = photoUriString
      .takeIf { it.isNotBlank() }
      ?.let(Uri::parse)

    MainTabScaffold(
      navController = navController,
      selectedRoute = Routes.SCAN
    ) {
      if (photoUri != null) {
        PhotoResultScreen(
          photoUri = photoUri,
          onBack = { navController.popBackStack() },
          onFindPartner = {
            navController.navigate(Routes.MAP) {
              popUpTo(Routes.HOME) {
                saveState = true
              }
              launchSingleTop = true
              restoreState = true
            }
          },
          onNotifications = {
            navController.navigate(
              Routes.NOTIFICATION
            )
          },
          onProfile = {
            navController.navigate(
              Routes.PROFILE
            )
          }
        )
      } else {
        MissingScanImageScreen(
          onBack = {
            navController.popBackStack()
          }
        )
      }
    }
  }
}

@Composable
private fun MissingScanImageScreen(
  onBack: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = "Foto tidak ditemukan",
      style = MaterialTheme.typography.titleLarge
    )

    Spacer(Modifier.height(8.dp))

    Text(
      text = "Silakan ambil atau pilih foto kembali.",
      style = MaterialTheme.typography.bodyMedium
    )

    Spacer(Modifier.height(16.dp))

    TextButton(onClick = onBack) {
      Text("Kembali ke Kamera")
    }
  }
}