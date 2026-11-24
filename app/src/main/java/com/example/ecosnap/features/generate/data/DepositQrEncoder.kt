package com.example.ecosnap.features.generate.data

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object DepositQrEncoder {
  fun encode(payload: String): BitMatrix = QRCodeWriter().encode(
    payload,
    BarcodeFormat.QR_CODE,
    0,
    0,
    mapOf(
      EncodeHintType.CHARACTER_SET to "UTF-8",
      EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.Q,
      EncodeHintType.MARGIN to 4
    )
  )
}
