package com.example.ecosnap.features.scan

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.LuminanceSource
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.ReaderException
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface ImageDecode {
  data class Found(val text: String) : ImageDecode
  data object NoQr : ImageDecode
  data object Unreadable : ImageDecode
}

/** ZXing-based QR decoding for camera frames and gallery images. Decoded text is never logged. */
object QrDecoder {
  private fun hints(tryHarder: Boolean): Map<DecodeHintType, Any> = buildMap {
    put(DecodeHintType.POSSIBLE_FORMATS, listOf(BarcodeFormat.QR_CODE))
    if (tryHarder) put(DecodeHintType.TRY_HARDER, true)
  }

  /** Tries the image as-is and then inverted (light-on-dark codes). */
  fun decode(source: LuminanceSource, tryHarder: Boolean): String? {
    for (candidate in arrayOf(source, source.invert())) {
      try {
        return QRCodeReader().decode(BinaryBitmap(HybridBinarizer(candidate)), hints(tryHarder)).text
      } catch (_: ReaderException) {
        // Not found in this orientation of polarity; try the next.
      }
    }
    return null
  }

  fun decodeFrame(image: ImageProxy): String? {
    val plane = image.planes.firstOrNull() ?: return null
    val width = image.width
    val height = image.height
    val rowStride = plane.rowStride
    val buffer = plane.buffer
    if (plane.pixelStride != 1 || rowStride < width) return null
    val data = ByteArray(width * height)
    for (row in 0 until height) {
      buffer.position(row * rowStride)
      buffer.get(data, row * width, width)
    }
    return decode(PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false), tryHarder = false)
  }

  suspend fun decodeImage(resolver: ContentResolver, uri: Uri): ImageDecode = withContext(Dispatchers.Default) {
    try {
      for (maxSide in intArrayOf(2048, 1024)) {
        val bitmap = load(resolver, uri, maxSide) ?: return@withContext ImageDecode.Unreadable
        try {
          val pixels = IntArray(bitmap.width * bitmap.height)
          bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
          decode(RGBLuminanceSource(bitmap.width, bitmap.height, pixels), tryHarder = true)
            ?.let { return@withContext ImageDecode.Found(it) }
        } finally {
          bitmap.recycle()
        }
      }
      ImageDecode.NoQr
    } catch (_: IOException) {
      ImageDecode.Unreadable
    } catch (_: SecurityException) {
      ImageDecode.Unreadable
    } catch (_: OutOfMemoryError) {
      ImageDecode.Unreadable
    }
  }

  private fun load(resolver: ContentResolver, uri: Uri, maxSide: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) } ?: return null
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxSide) sample *= 2
    val options = BitmapFactory.Options().apply {
      inSampleSize = sample
      inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
  }
}

/** Feeds camera frames to ZXing while [enabled]; every frame is closed. */
class QrFrameAnalyzer(
  private val enabled: () -> Boolean,
  private val onResult: (String) -> Unit,
) : ImageAnalysis.Analyzer {
  override fun analyze(image: ImageProxy) {
    try {
      if (!enabled()) return
      QrDecoder.decodeFrame(image)?.let(onResult)
    } catch (_: RuntimeException) {
      // A bad frame is skipped; the next frame is tried.
    } finally {
      image.close()
    }
  }
}
