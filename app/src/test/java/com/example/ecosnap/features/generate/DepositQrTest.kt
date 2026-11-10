package com.example.ecosnap.features.generate

import com.example.ecosnap.features.generate.data.DepositQrEncoder
import com.example.ecosnap.features.generate.model.DepositQr
import com.google.gson.JsonParser
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DepositQrTest {
  @Test fun payloadKeepsWeightPointsAndDemoContractTogether() {
    val qr = DepositQr.create(250, 1_000L)
    val json = JsonParser.parseString(qr.payload()).asJsonObject
    assertEquals("ecosnap.deposit.demo", json["type"].asString)
    assertTrue(json["demo"].asBoolean)
    assertEquals("2.50", json["weightKg"].asString)
    assertEquals(250, json["points"].asInt)
    assertEquals(qr.id, json["id"].asString)
    assertEquals(301_000L, json["expiresAt"].asLong)
  }

  @Test fun expiryUsesTheDeadlineIncludingTimeAwayFromTheScreen() {
    val qr = DepositQr.create(250, 1_000L)
    assertEquals(300, qr.remainingSeconds(1_000L))
    assertEquals(299, qr.remainingSeconds(2_001L))
    assertEquals(1, qr.remainingSeconds(300_999L))
    assertEquals(0, qr.remainingSeconds(301_000L))
    assertEquals(0, qr.remainingSeconds(999_999L))
  }

  @Test fun regenerateCreatesANewIdAndDeadline() {
    val original = DepositQr.create(250, 1_000L)
    val replacement = DepositQr.create(original.weightHundredths, 10_000L)
    assertNotEquals(original.id, replacement.id)
    assertEquals(original.points, replacement.points)
    assertEquals(310_000L, replacement.expiresAtMillis)
  }

  @Test(expected = IllegalArgumentException::class)
  fun zeroWeightCannotCreateATransaction() {
    DepositQr.create(0)
  }

  @Test fun renderedGreenQrDecodesBackToTheExactPayload() {
    val qr = DepositQr("TRX-377FCA9EA3564E51", 250, 301_000L)
    val matrix = DepositQrEncoder.encode(qr.payload())
    val scale = 5
    val dimension = matrix.width * scale
    val pixels = IntArray(dimension * dimension) { index ->
      val x = (index % dimension) / scale
      val y = (index / dimension) / scale
      if (matrix[x, y]) 0xFF1B4332.toInt() else 0xFFFFFFFF.toInt()
    }
    val image = BinaryBitmap(HybridBinarizer(RGBLuminanceSource(dimension, dimension, pixels)))
    assertEquals(qr.payload(), QRCodeReader().decode(image).text)
  }
}
