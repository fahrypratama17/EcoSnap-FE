package com.example.ecosnap.features.adminhome

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.composables.icons.lucide.Gift
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Map
import com.composables.icons.lucide.ScanLine
import com.example.ecosnap.core.designsystem.components.EcoAppHeader
import com.example.ecosnap.core.designsystem.components.EcoBottomItem
import com.example.ecosnap.core.designsystem.components.EcoBottomNavigation
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.admincal.AdminCalculatorScreen
import com.example.ecosnap.features.admincal.model.WasteCalculator
import com.example.ecosnap.features.generate.GenerateQrScreen
import com.example.ecosnap.features.generate.model.DepositQr
import com.example.ecosnap.navigation.Routes
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay

private val transactionSaver = listSaver<DepositQr?, Any>(
  save = { qr -> if (qr == null) emptyList() else listOf(qr.id, qr.weightHundredths, qr.expiresAtMillis) },
  restore = { values -> if (values.isEmpty()) null else DepositQr(values[0] as String, values[1] as Int, values[2] as Long) }
)

@Composable
fun AdminWorkspace(onLogout: () -> Unit) {
  val navController = rememberNavController()
  var weightInput by rememberSaveable { mutableStateOf("0") }
  var transaction by rememberSaveable(stateSaver = transactionSaver) { mutableStateOf<DepositQr?>(null) }
  var dialog by rememberSaveable { mutableStateOf<String?>(null) }
  val entry by navController.currentBackStackEntryAsState()
  val selectedRoute = entry?.destination?.route ?: Routes.ADMIN_HOME
  val now by produceState(System.currentTimeMillis(), transaction?.id) {
    do {
      value = System.currentTimeMillis()
      delay(500)
    } while (transaction?.remainingSeconds(value)?.let { it > 0 } == true)
  }

  fun navigate(route: String) {
    navController.navigate(route) {
      popUpTo(Routes.ADMIN_HOME)
      launchSingleTop = true
    }
  }

  fun openQr() {
    val weight = WasteCalculator.hundredths(weightInput)
    if (transaction == null && weight > 0) transaction = DepositQr.create(weight)
    navigate(Routes.ADMIN_GENERATE)
  }

  MitraScaffold(
    selectedRoute = selectedRoute,
    onNavigate = { if (it == Routes.ADMIN_GENERATE) openQr() else navigate(it) },
    onNotifications = { dialog = "notifications" },
    onProfile = { dialog = "profile" }
  ) { padding ->
    NavHost(navController, startDestination = Routes.ADMIN_HOME, modifier = Modifier.padding(padding)) {
      composable(Routes.ADMIN_HOME) {
        AdminHomeScreen(
          weightHundredths = WasteCalculator.hundredths(weightInput),
          onCalculator = { navigate(Routes.ADMIN_CALCULATOR) },
          onGenerateQr = { openQr() }
        )
      }
      composable(Routes.ADMIN_CALCULATOR) {
        AdminCalculatorScreen(
          weightInput = weightInput,
          onInputChange = {
            if (it != weightInput) {
              weightInput = it
              transaction = null
            }
          },
          onGenerateQr = { openQr() }
        )
      }
      composable(Routes.ADMIN_GENERATE) {
        GenerateQrScreen(
          transaction = transaction,
          remainingSeconds = transaction?.remainingSeconds(now) ?: 0,
          onRegenerate = { transaction = transaction?.let { DepositQr.create(it.weightHundredths) } },
          onFinished = {
            transaction = null
            weightInput = "0"
            navigate(Routes.ADMIN_HOME)
          },
          onCalculator = { navigate(Routes.ADMIN_CALCULATOR) }
        )
      }
    }
  }

  if (dialog != null) {
    val isProfile = dialog == "profile"
    AlertDialog(
      onDismissRequest = { dialog = null },
      title = { Text(if (isProfile) "Petugas Mitra" else "Notifikasi") },
      text = { Text(if (isProfile) "Panel EcoSnap Mitra · Mode demo\nKelola berat sampah, estimasi poin, dan QR setoran." else "Belum ada notifikasi. Konfirmasi setoran akan tersedia setelah layanan transaksi terhubung.") },
      confirmButton = { TextButton(onClick = { dialog = null }) { Text("Tutup") } },
      dismissButton = {
        if (isProfile) {
          TextButton(onClick = { dialog = null; onLogout() }) { Text("Keluar") }
        }
      }
    )
  }
}

@Composable
internal fun MitraScaffold(
  selectedRoute: String,
  onNavigate: (String) -> Unit,
  onNotifications: () -> Unit,
  onProfile: () -> Unit,
  content: @Composable (PaddingValues) -> Unit
) {
  Scaffold(
    modifier = Modifier.fillMaxSize().safeDrawingPadding(),
    containerColor = EcoColor.White,
    topBar = { EcoAppHeader(onNotifications, onProfile) },
    bottomBar = {
      EcoBottomNavigation(
        items = listOf(
          EcoBottomItem(Routes.ADMIN_HOME, "Beranda", Lucide.Map),
          EcoBottomItem(Routes.ADMIN_GENERATE, "QR", Lucide.ScanLine),
          EcoBottomItem(Routes.ADMIN_CALCULATOR, "Kalkulator", Lucide.Gift)
        ),
        selectedRoute = selectedRoute,
        onNavigate = onNavigate
      )
    },
    content = content
  )
}
