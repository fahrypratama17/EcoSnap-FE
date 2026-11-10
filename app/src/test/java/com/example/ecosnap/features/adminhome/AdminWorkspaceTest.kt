package com.example.ecosnap.features.adminhome

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.ecosnap.core.designsystem.theme.EcosnapTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Runs the real Compose flow on the JVM, with no emulator or connected phone. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w400dp-h1100dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AdminWorkspaceTest {
  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

  @Test fun weightAndQrSurviveNavigationAndRestorationThenFinishClearsTheDraft() {
    val restoration = StateRestorationTester(compose)
    restoration.setContent {
      EcosnapTheme(darkTheme = false, dynamicColor = false) { AdminWorkspace(onLogout = {}) }
    }
    compose.onNodeWithText("Halo, Petugas!").assertIsDisplayed()
    compose.onNodeWithText("Kalkulator Sampah").performScrollTo().performClick()
    compose.onNodeWithText("Generate QR").assertIsNotEnabled()
    listOf("2", ",", "5", "0").forEach { compose.onNodeWithText(it).performScrollTo().performClick() }
    compose.onNodeWithText("250 poin").performScrollTo().assertIsDisplayed()
    compose.onNodeWithText("Beranda", useUnmergedTree = true).performClick()
    compose.onNodeWithText("Kalkulator Sampah").performScrollTo().performClick()
    compose.onNodeWithText("Generate QR").performScrollTo().performClick()
    compose.waitUntil(10_000) {
      compose.onAllNodesWithContentDescription("QR transaksi demo EcoSnap").fetchSemanticsNodes().isNotEmpty()
    }
    val transactionId = currentTransactionId()
    restoration.emulateSavedInstanceStateRestore()
    assertEquals(transactionId, currentTransactionId())
    compose.onNodeWithText("Kalkulator", useUnmergedTree = true).performClick()
    compose.onNodeWithText("2,50").assertIsDisplayed()
    compose.onNodeWithText("QR", useUnmergedTree = true).performClick()
    assertEquals(transactionId, currentTransactionId())
    compose.onNodeWithText("Buat Ulang QR").performScrollTo().performClick()
    assertNotEquals(transactionId, currentTransactionId())
    compose.onNodeWithText("Selesai").performScrollTo().performClick()
    compose.onNodeWithText("Halo, Petugas!").assertIsDisplayed()
    compose.onNodeWithText("Kalkulator Sampah").performScrollTo().performClick()
    compose.onNodeWithText("Generate QR").assertIsNotEnabled()
    compose.onNodeWithText("QR", useUnmergedTree = true).performClick()
    compose.onNodeWithText("Belum ada QR transaksi").assertIsDisplayed()
  }

  private fun currentTransactionId(): String = compose.onNode(
    SemanticsMatcher("transaction ID") { node ->
      node.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.Text)
        ?.any { it.text.matches(Regex("TRX-[A-F0-9]{16}")) } == true
    }
  ).fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.Text].first().text

}
