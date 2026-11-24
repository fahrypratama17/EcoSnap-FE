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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.ecosnap.features.photos.PhotoReviewScreen
import com.example.ecosnap.features.photos.WasteScanViewModel
import com.example.ecosnap.features.photos.data.HttpWastePhotosRepository
import androidx.navigation.NavBackStackEntry
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
    val scan = sharedWasteScanViewModel(navController, it)
    MainTabScaffold(
      navController = navController,
      selectedRoute = Routes.SCAN
    ) {
      CameraScreen(
        onBack = {
          navController.popBackStack()
        },
        onImageReady = { photoUri ->
          if (scan.selectImage(photoUri)) {
            navController.navigate(Routes.SCAN_REVIEW) { launchSingleTop = true }
          }
        }
      )
    }
  }
  composable(Routes.SCAN_REVIEW) { entry ->
    val scan = sharedWasteScanViewModel(navController, entry)
    val state by scan.state.collectAsStateWithLifecycle()
    val retake: () -> Unit = {
      scan.cancel()
      navController.popBackStack(Routes.SCAN, false)
    }
    BackHandler(onBack = retake)
    LaunchedEffect(state.result) {
      if (state.result != null && state.photoUri != null) {
        navController.navigate(Routes.scanResult(Uri.parse(state.photoUri))) { launchSingleTop = true }
      }
    }
    MainTabScaffold(navController, Routes.SCAN) {
      PhotoReviewScreen(state, scan::analyze, retake)
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
    val scan = sharedWasteScanViewModel(navController, backStackEntry)
    val state by scan.state.collectAsStateWithLifecycle()

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
      val result = state.result
      if (photoUri != null && state.photoUri == photoUriString && result != null) {
        BackHandler { navController.popBackStack(Routes.SCAN, false) }
        PhotoResultScreen(
          photoUri = photoUri,
          result = result,
          onBack = { navController.popBackStack(Routes.SCAN, false) },
          onFindPartner = {
            navController.getBackStackEntry(Routes.MAIN_GRAPH).savedStateHandle["scan_partner_query"] = result.partnerSearchKeyword
            navController.getBackStackEntry(Routes.MAIN_GRAPH).savedStateHandle["scan_partner_type"] = result.partnerType
            navController.navigate(Routes.MAP) {
              launchSingleTop = true
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
            navController.popBackStack(Routes.SCAN, false)
          }
        )
      }
    }
  }
}

@Composable
private fun sharedWasteScanViewModel(
  navController: NavHostController,
  entry: NavBackStackEntry
): WasteScanViewModel {
  val owner = remember(entry) { navController.getBackStackEntry(Routes.MAIN_GRAPH) }
  val context = LocalContext.current.applicationContext
  return viewModel(
    viewModelStoreOwner = owner,
    factory = viewModelFactory {
      initializer { WasteScanViewModel(HttpWastePhotosRepository(context), createSavedStateHandle()) }
    }
  )
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
