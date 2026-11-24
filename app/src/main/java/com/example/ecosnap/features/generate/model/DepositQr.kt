package com.example.ecosnap.features.generate.model

import com.example.ecosnap.features.admincal.model.WasteCalculator
import java.util.UUID

/** Local preview contract only. A real claim must be issued and verified by the backend. */
data class DepositQr(
  val id: String,
  val weightHundredths: Int,
  val expiresAtMillis: Long
) {
  init {
    require(weightHundredths in 1..WasteCalculator.MAX_WEIGHT_HUNDREDTHS)
  }

  val points: Int get() = WasteCalculator.points(weightHundredths)

  fun remainingSeconds(nowMillis: Long): Int =
    ((expiresAtMillis - nowMillis).coerceAtLeast(0) / 1000.0)
      .let { kotlin.math.ceil(it).toInt().coerceAtMost(VALIDITY_SECONDS) }

  fun payload(): String =
    """{"type":"ecosnap.deposit.demo","version":1,"id":"$id","weightKg":${weightHundredths.toBigDecimal().movePointLeft(2)},"points":$points,"expiresAt":$expiresAtMillis,"demo":true}"""

  companion object {
    const val VALIDITY_SECONDS = 300

    fun create(weightHundredths: Int, nowMillis: Long = System.currentTimeMillis()): DepositQr =
      DepositQr(
        id = "TRX-${UUID.randomUUID().toString().replace("-", "").take(16).uppercase()}",
        weightHundredths = weightHundredths,
        expiresAtMillis = nowMillis + VALIDITY_SECONDS * 1000L
      )
  }
}
