package com.example.ecosnap.features.photos

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.SavedStateHandle
import com.example.ecosnap.core.designsystem.theme.EcosnapTheme
import com.example.ecosnap.features.photos.data.HttpWastePhotosRepository
import com.example.ecosnap.features.photos.data.ScanException
import com.example.ecosnap.features.photos.data.ScanResponseParser
import com.example.ecosnap.features.photos.data.WastePhotosRepository
import com.example.ecosnap.features.photos.model.RecyclingStatus
import com.google.gson.JsonParser
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w400dp-h1000dp-xhdpi")
class WasteScanTest {
  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

  private fun fixture(): String = javaClass.classLoader!!.getResource("waste_scan_success.json")!!.readText()

  @Test fun confirmationDuplicateClicksExpansionAndStateRestorationUseOneRequest() {
    var requests = 0
    val reply = CompletableDeferred<String>()
    val repository = WastePhotosRepository { _, _ -> requests++; reply.await() }
    val handle = SavedStateHandle()
    lateinit var model: WasteScanViewModel
    compose.runOnUiThread {
      model = WasteScanViewModel(repository, handle)
      model.selectImage(Uri.parse("file:///scan-test.jpg"))
    }
    compose.setContent {
      val state by model.state.collectAsState()
      EcosnapTheme(darkTheme = false, dynamicColor = false) {
        val result = state.result
        if (result == null) PhotoReviewScreen(state, model::analyze, {})
        else PhotoResultScreen(Uri.parse(state.photoUri), result, {}, {}, {}, {})
      }
    }
    assertEquals(0, requests)
    compose.onNodeWithText("Analisis Foto").performScrollTo().performClick()
    compose.runOnUiThread { repeat(3) { model.analyze() } }
    compose.waitUntil(10_000) { requests == 1 }
    compose.onNodeWithText("Sedang menganalisis...").assertIsNotEnabled()
    compose.runOnUiThread { reply.complete(fixture()) }
    compose.waitUntil(10_000) { model.state.value.result != null }
    compose.onNodeWithText("Botol Plastik Minuman").assertIsDisplayed()
    compose.onNodeWithText("Daur Ulang Bersyarat").assertIsDisplayed()
    compose.onNodeWithText("Lihat Cara Membuat").performScrollTo().performClick()
    compose.onNodeWithText("Alat & Bahan").performScrollTo().assertIsDisplayed()
    compose.onNodeWithText("Tutup Panduan").performScrollTo().performClick()
    assertEquals(1, requests)
    compose.runOnUiThread {
      val restored = WasteScanViewModel(repository, SavedStateHandle(mapOf(
        "photo_uri" to handle.get<String>("photo_uri"),
        "scan_id" to handle.get<String>("scan_id"),
        "scan_response" to handle.get<String>("scan_response")
      )))
      assertNotNull(restored.state.value.result)
      restored.analyze()
    }
    assertEquals(1, requests)
  }

  @Test fun uploadUsesActualImageAndScanIdWithoutGroqCredentials() = runBlocking {
    val app = RuntimeEnvironment.getApplication()
    val file = File(app.cacheDir, "scan-upload-test.png")
    val original = byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 1, 2, 3)
    file.writeBytes(original)
    var uploads = 0
    val client = OkHttpClient.Builder().addInterceptor { chain ->
      val request = chain.request()
      uploads++
      assertEquals("http://localhost:8000/api/scan", request.url.toString())
      assertEquals("test-scan-id", request.header("X-Scan-Id"))
      assertNull(request.header("Authorization"))
      val buffer = Buffer()
      request.body!!.writeTo(buffer)
      val body = buffer.readByteArray()
      assertTrue(body.toList().windowed(original.size).any { it == original.toList() })
      Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(200).message("OK")
        .body(fixture().toResponseBody("application/json".toMediaType())).build()
    }.build()
    val repository = HttpWastePhotosRepository(app, "http://localhost:8000", client)
    val response = repository.analyze(Uri.fromFile(file), "test-scan-id")
    assertEquals("Botol Plastik Minuman", ScanResponseParser.parse(response).objectName)
    assertEquals(1, uploads)
    file.delete()
    Unit
  }

  @Test fun unknownMaterialAndUnsafeGuidesAreValidated() {
    val root = JsonParser.parseString(fixture()).asJsonObject
    val id = root.getAsJsonObject("identification")
    id.addProperty("recycling_status", "unknown")
    id.add("recyclable", com.google.gson.JsonNull.INSTANCE)
    assertEquals(RecyclingStatus.UNKNOWN, ScanResponseParser.parse(root.toString()).recyclingStatus)
    id.addProperty("hazardous", true)
    assertThrows(ScanException::class.java) { ScanResponseParser.parse(root.toString()) }
    assertThrows(ScanException::class.java) { ScanResponseParser.parse("{broken") }
  }

  @Test fun invalidImagesHaveHelpfulMessages() {
    for (status in listOf("invalid_image", "multiple_objects")) {
      val json = """{"status":"$status","message":"Foto ulang satu objek dengan cahaya cukup."}"""
      val error = assertThrows(ScanException::class.java) { ScanResponseParser.parse(json) }
      assertEquals("Foto ulang satu objek dengan cahaya cukup.", error.userMessage)
    }
  }

  @Test fun hazardousWasteShowsReasonAndNoDiyCards() {
    val root = JsonParser.parseString(fixture()).asJsonObject
    root.getAsJsonObject("identification").addProperty("hazardous", true)
    val upcycling = root.getAsJsonObject("upcycling")
    upcycling.addProperty("possible", false)
    upcycling.addProperty("reason", "Sampah berbahaya tidak aman untuk dibuat kerajinan.")
    upcycling.add("ideas", com.google.gson.JsonArray())
    val result = ScanResponseParser.parse(root.toString())
    compose.setContent {
      EcosnapTheme(darkTheme = false, dynamicColor = false) {
        PhotoResultScreen(Uri.parse("file:///scan.jpg"), result, {}, {}, {}, {})
      }
    }
    compose.onNodeWithText("Sampah berbahaya tidak aman untuk dibuat kerajinan.").performScrollTo().assertIsDisplayed()
    compose.onNodeWithText("Lihat Cara Membuat").assertDoesNotExist()
  }

  @Test fun oneTwoOrThreeIdeasAreMappedDynamically() {
    for (count in 1..3) {
      val root = JsonParser.parseString(fixture()).asJsonObject
      val original = root.getAsJsonObject("upcycling").getAsJsonArray("ideas")[0]
      val ideas = com.google.gson.JsonArray()
      repeat(count) { index ->
        val idea = original.deepCopy().asJsonObject
        idea.addProperty("title", "Ide Pemanfaatan ${index + 1}")
        ideas.add(idea)
      }
      root.getAsJsonObject("upcycling").add("ideas", ideas)
      assertEquals(count, ScanResponseParser.parse(root.toString()).reuseIdeas.size)
    }
  }

  @Test fun retryAfterPreventsImmediateNewRequests() {
    var calls = 0
    val repository = WastePhotosRepository { _, _ ->
      calls++
      throw ScanException("Tunggu sebentar.", retryAfterSeconds = 60)
    }
    lateinit var model: WasteScanViewModel
    compose.runOnUiThread {
      model = WasteScanViewModel(repository, SavedStateHandle())
      model.selectImage(Uri.parse("file:///scan.jpg"))
      model.analyze()
    }
    compose.waitUntil(10_000) { model.state.value.error != null }
    compose.runOnUiThread { model.analyze() }
    assertEquals(1, calls)
    assertTrue(model.state.value.retryAtMillis > System.currentTimeMillis())
  }

  @Test fun cancelledScanCannotReplaceANewPhotoResult() {
    val first = CompletableDeferred<String>()
    val second = CompletableDeferred<String>()
    var calls = 0
    var oldFinished = false
    val repository = WastePhotosRepository { _, _ ->
      calls++
      if (calls == 1) withContext(NonCancellable) {
        first.await().also { oldFinished = true }
      } else second.await()
    }
    lateinit var model: WasteScanViewModel
    compose.runOnUiThread {
      model = WasteScanViewModel(repository, SavedStateHandle())
      model.selectImage(Uri.parse("file:///first.jpg"))
      model.analyze()
    }
    compose.waitUntil(10_000) { calls == 1 }
    compose.runOnUiThread {
      model.cancel()
      assertTrue(model.selectImage(Uri.parse("file:///second.jpg")))
      model.analyze()
      first.complete(fixture())
    }
    compose.waitUntil(10_000) { oldFinished && calls == 2 }
    compose.runOnUiThread {
      assertTrue(model.state.value.loading)
      model.analyze()
      second.complete(fixture())
    }
    compose.waitUntil(10_000) { model.state.value.result != null }
    assertEquals("file:///second.jpg", model.state.value.photoUri)
    assertEquals(2, calls)
  }
}
