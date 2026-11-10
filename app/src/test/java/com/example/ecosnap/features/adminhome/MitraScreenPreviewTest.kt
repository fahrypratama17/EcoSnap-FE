package com.example.ecosnap.features.adminhome

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.example.ecosnap.core.designsystem.theme.EcosnapTheme
import com.example.ecosnap.features.admincal.AdminCalculatorScreen
import com.example.ecosnap.features.generate.GenerateQrScreen
import com.example.ecosnap.features.generate.model.DepositQr
import com.example.ecosnap.navigation.Routes
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Static screen renders avoid transition layers when drawing to an offscreen software canvas. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w400dp-h950dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MitraScreenPreviewTest {
  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

  @Test fun home() {
    show(Routes.ADMIN_HOME) { AdminHomeScreen(250, {}, {}, it) }
    compose.onNodeWithText("Halo, Petugas!").assertIsDisplayed()
    capture("mitra-home")
  }

  @Test fun calculator() {
    show(Routes.ADMIN_CALCULATOR) { AdminCalculatorScreen("2,50", {}, {}, it) }
    compose.onNodeWithText("250 poin").assertIsDisplayed()
    capture("mitra-calculator")
  }

  @Test
  @Config(qualifiers = "w400dp-h1300dp-xhdpi")
  fun qr() {
    show(Routes.ADMIN_GENERATE) {
      GenerateQrScreen(DepositQr("TRX-201124123123-0087", 250, 300_000L), 299, {}, {}, {}, it)
    }
    compose.waitUntil(10_000) {
      compose.onAllNodesWithContentDescription("QR transaksi demo EcoSnap").fetchSemanticsNodes().isNotEmpty()
    }
    capture("mitra-qr")
  }

  private fun show(route: String, content: @Composable (Modifier) -> Unit) {
    compose.setContent {
      EcosnapTheme(darkTheme = false, dynamicColor = false) {
        MitraScaffold(route, {}, {}, {}) { padding -> content(Modifier.padding(padding)) }
      }
    }
  }

  private fun capture(name: String) {
    val output = File("build/reports/mitra-preview/$name.png")
    output.parentFile?.mkdirs()
    compose.runOnIdle {
      val root = compose.activity.window.decorView
      invalidateComposeLayers(root)
      val bitmap = Bitmap.createBitmap(root.width, root.height, Bitmap.Config.ARGB_8888)
      root.draw(Canvas(bitmap))
      output.outputStream().use { stream -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream) }
      bitmap.recycle()
    }
  }

  private fun invalidateComposeLayers(view: View) {
    // Software captures need fresh Compose display lists after asynchronous QR generation.
    view.javaClass.methods.firstOrNull { it.name == "invalidateDescendants" && it.parameterCount == 0 }?.invoke(view)
    if (view is ViewGroup) {
      repeat(view.childCount) { invalidateComposeLayers(view.getChildAt(it)) }
    }
  }
}
