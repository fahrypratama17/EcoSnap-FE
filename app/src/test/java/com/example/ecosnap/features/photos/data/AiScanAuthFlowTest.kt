package com.example.ecosnap.features.photos.data

import java.util.UUID
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class AiScanAuthFlowTest {
  private class Captured(val request: Request, val body: ByteArray)

  private class FakeAuth(var uid: String?) : AiScanAuth {
    val tokenCalls = mutableListOf<Boolean>()
    var signedOut = false
    var onToken: (Boolean) -> Unit = {}
    var tokenFailure: AiScanException? = null

    override fun currentUid(): String? = uid
    override suspend fun idToken(uid: String, forceRefresh: Boolean): String {
      tokenFailure?.let { throw it }
      tokenCalls += forceRefresh
      onToken(forceRefresh)
      return if (forceRefresh) "fresh-token" else "first-token"
    }

    override fun signOutIfCurrent(uid: String) {
      if (this.uid == uid) {
        this.uid = null
        signedOut = true
      }
    }
  }

  private val captured = mutableListOf<Captured>()

  private fun repo(
    auth: AiScanAuth,
    vararg replies: Pair<Int, String>,
    onRequest: () -> Unit = {}
  ): RemoteWastePhotosRepository {
    val queue = replies.toMutableList()
    val client = OkHttpClient.Builder()
      .addInterceptor(Interceptor { chain ->
        val request = chain.request()
        val buffer = Buffer()
        request.body!!.writeTo(buffer)
        captured += Captured(request, buffer.readByteArray())
        onRequest()
        val (status, json) = queue.removeAt(0)
        Response.Builder()
          .request(request)
          .protocol(Protocol.HTTP_1_1)
          .code(status)
          .message("test")
          .body(json.toResponseBody("application/json".toMediaType()))
          .build()
      })
      .build()
    return RemoteWastePhotosRepository(
      { throw AssertionError("image preparation not expected") },
      "http://ai.test/", true, client, auth
    )
  }

  private fun error(code: String) = """{"error":{"code":"$code","message":"x"}}"""

  private fun scan(repo: RemoteWastePhotosRepository, id: String = UUID.randomUUID().toString()): AiScanException {
    try {
      runBlocking { repo.analyzePrepared(byteArrayOf(1, 2, 3, 4), id) }
    } catch (e: AiScanException) {
      return e
    }
    fail("expected AiScanException")
    throw IllegalStateException()
  }

  @Test
  fun scanSendsBearerTokenFromSdkWithoutRefresh() {
    val auth = FakeAuth("uid-a")
    val id = UUID.randomUUID().toString()
    scan(repo(auth, 500 to error("internal_error")), id)

    assertEquals(1, captured.size)
    assertEquals("Bearer first-token", captured[0].request.header("Authorization"))
    assertEquals(id, captured[0].request.header("X-Scan-Id"))
    assertEquals(listOf(false), auth.tokenCalls)
  }

  @Test
  fun signedOutUserNeverReachesHttp() {
    val auth = FakeAuth(null)
    val e = scan(repo(auth))

    assertEquals("unauthenticated", e.code)
    assertFalse(e.retryable)
    assertTrue(captured.isEmpty())
  }

  @Test
  fun tokenRetrievalFailureIsDistinctFromNetworkFailureAndSendsNothing() {
    val auth = FakeAuth("uid-a")
    auth.tokenFailure = AiScanException("auth_token_unavailable", "x", retryable = true)
    val e = scan(repo(auth))

    assertEquals("auth_token_unavailable", e.code)
    assertTrue(captured.isEmpty())
  }

  @Test
  fun unauthorizedRefreshesTokenOnceAndReplaysSameScanIdAndBytes() {
    val auth = FakeAuth("uid-a")
    val id = UUID.randomUUID().toString()
    val e = scan(repo(auth, 401 to error("unauthorized"), 500 to error("internal_error")), id)

    assertEquals("internal_error", e.code)
    assertEquals(listOf(false, true), auth.tokenCalls)
    assertEquals(2, captured.size)
    assertEquals("Bearer first-token", captured[0].request.header("Authorization"))
    assertEquals("Bearer fresh-token", captured[1].request.header("Authorization"))
    assertEquals(id, captured[0].request.header("X-Scan-Id"))
    assertEquals(id, captured[1].request.header("X-Scan-Id"))
    assertTrue(captured[0].body.contentEquals(captured[1].body))
    assertFalse(auth.signedOut)
  }

  @Test
  fun repeatedUnauthorizedStopsWithNonRetryableErrorAndSignsOut() {
    val auth = FakeAuth("uid-a")
    val e = scan(repo(auth, 401 to error("unauthorized"), 401 to error("unauthorized"), 401 to error("unauthorized")))

    assertEquals("unauthorized", e.code)
    assertFalse(e.retryable)
    assertEquals(2, captured.size)
    assertTrue(auth.signedOut)
  }

  @Test
  fun accountSwitchBeforeReplayPreventsSecondRequest() {
    val auth = FakeAuth("uid-a")
    // Account changes while the first request is in flight.
    val e = scan(repo(auth, 401 to error("unauthorized"), 200 to "{}", onRequest = { auth.uid = "uid-b" }))

    assertEquals("session_changed", e.code)
    assertEquals(1, captured.size)
    assertEquals(listOf(false), auth.tokenCalls)
  }

  @Test
  fun accountSwitchDuringTokenFetchSendsNoHttp() {
    val auth = FakeAuth("uid-a")
    auth.onToken = { auth.uid = "uid-b" }
    val e = scan(repo(auth, 500 to error("internal_error")))

    assertEquals("session_changed", e.code)
    assertTrue(captured.isEmpty())
  }

  @Test
  fun authUnavailable503IsRetryableServerFailure() {
    val auth = FakeAuth("uid-a")
    val e = scan(repo(auth, 503 to error("auth_unavailable")))

    assertEquals("auth_unavailable", e.code)
    assertTrue(e.retryable)
    assertEquals(1, captured.size)
  }
}
