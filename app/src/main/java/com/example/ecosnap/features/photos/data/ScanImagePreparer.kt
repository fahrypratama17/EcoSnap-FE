package com.example.ecosnap.features.photos.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.ensureActive

/** The picked or captured image cannot be uploaded; [message] is user-safe Indonesian text. */
internal class UnusableImageException(message: String) : Exception(message)

/**
 * Turns the camera/gallery image into a bounded static JPEG for upload: validates the container,
 * decodes at reduced size (never the full bitmap), applies EXIF orientation, flattens transparency
 * onto white and re-encodes without metadata. The server limit is 10 MiB / 25 MP; the output is far below.
 */
internal object ScanImagePreparer {
  const val MAX_UPLOAD_BYTES = 10 * 1024 * 1024
  private const val MAX_SOURCE_BYTES = 64L * 1024 * 1024
  private const val MAX_SOURCE_PIXELS = 100_000_000L
  private const val TARGET_LONG_SIDE = 1280
  private const val JPEG_QUALITY = 85
  private val allowedMime = setOf("image/jpeg", "image/png", "image/webp", "image/heic", "image/heif")

  suspend fun prepare(context: Context, uri: Uri): ByteArray {
    val source = SourceOpener(context.applicationContext, uri)
    source.validate()

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    source.open().use { BitmapFactory.decodeStream(it, null, bounds) }
    coroutineContext.ensureActive()
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw unusable()
    if (bounds.outMimeType !in allowedMime) {
      throw UnusableImageException("Format gambar tidak didukung. Gunakan JPEG, PNG, atau WebP.")
    }
    if (bounds.outWidth.toLong() * bounds.outHeight > MAX_SOURCE_PIXELS) {
      throw UnusableImageException("Resolusi gambar terlalu besar. Pilih gambar yang lebih kecil.")
    }

    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= TARGET_LONG_SIDE) sample *= 2
    val decodeOptions = BitmapFactory.Options().apply {
      inSampleSize = sample
      inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    val decoded = try {
      source.open().use { BitmapFactory.decodeStream(it, null, decodeOptions) }
    } catch (e: OutOfMemoryError) {
      throw UnusableImageException("Gambar terlalu besar untuk diproses perangkat ini.")
    } ?: throw unusable()

    var current = decoded
    try {
      coroutineContext.ensureActive()
      val rotation = orientationMatrix(source)
      if (rotation != null) {
        val rotated = Bitmap.createBitmap(current, 0, 0, current.width, current.height, rotation, true)
        if (rotated !== current) {
          current.recycle()
          current = rotated
        }
      }
      if (current.hasAlpha()) {
        val flat = Bitmap.createBitmap(current.width, current.height, Bitmap.Config.ARGB_8888)
        Canvas(flat).apply {
          drawColor(Color.WHITE)
          drawBitmap(current, 0f, 0f, null)
        }
        current.recycle()
        current = flat
      }
      coroutineContext.ensureActive()
      val out = ByteArrayOutputStream()
      if (!current.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)) throw unusable()
      val bytes = out.toByteArray()
      if (bytes.isEmpty() || bytes.size > MAX_UPLOAD_BYTES) {
        throw UnusableImageException("Gambar terlalu besar untuk dikirim.")
      }
      return bytes
    } catch (e: OutOfMemoryError) {
      throw UnusableImageException("Gambar terlalu besar untuk diproses perangkat ini.")
    } finally {
      current.recycle()
    }
  }

  private fun orientationMatrix(source: SourceOpener): Matrix? {
    val orientation = try {
      source.open().use {
        ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
      }
    } catch (_: Exception) {
      ExifInterface.ORIENTATION_NORMAL
    }
    val m = Matrix()
    when (orientation) {
      ExifInterface.ORIENTATION_ROTATE_90 -> m.postRotate(90f)
      ExifInterface.ORIENTATION_ROTATE_180 -> m.postRotate(180f)
      ExifInterface.ORIENTATION_ROTATE_270 -> m.postRotate(270f)
      ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> m.postScale(-1f, 1f)
      ExifInterface.ORIENTATION_FLIP_VERTICAL -> m.postScale(1f, -1f)
      ExifInterface.ORIENTATION_TRANSPOSE -> { m.postRotate(90f); m.postScale(-1f, 1f) }
      ExifInterface.ORIENTATION_TRANSVERSE -> { m.postRotate(270f); m.postScale(-1f, 1f) }
      else -> return null
    }
    return m
  }

  private fun unusable() = UnusableImageException("Gambar tidak dapat dibaca. Pilih atau ambil foto lain.")

  /** Opens a fresh bounded stream per call; accepts only app-cache files or content URIs. */
  private class SourceOpener(private val context: Context, private val uri: Uri) {
    fun validate() {
      when (uri.scheme) {
        "file" -> {
          val file = File(uri.path ?: throw unusable())
          val cache = context.cacheDir.canonicalFile
          val canonical = try { file.canonicalFile } catch (_: IOException) { throw unusable() }
          if (canonical.parentFile != cache || !canonical.name.startsWith("ecosnap_")) throw unusable()
          if (!canonical.isFile) throw UnusableImageException("Foto tidak ditemukan. Silakan ambil atau pilih foto kembali.")
          if (canonical.length() > MAX_SOURCE_BYTES) throw tooLarge()
        }
        "content" -> Unit
        else -> throw unusable()
      }
    }

    fun open(): InputStream {
      val raw = try {
        if (uri.scheme == "file") File(uri.path!!).inputStream() else context.contentResolver.openInputStream(uri)
      } catch (_: Exception) {
        null
      } ?: throw unusable()
      return LimitedInputStream(raw, MAX_SOURCE_BYTES)
    }

    private fun tooLarge() = UnusableImageException("Ukuran gambar terlalu besar. Pilih gambar yang lebih kecil.")
    private fun unusable() = UnusableImageException("Gambar tidak dapat dibaca. Pilih atau ambil foto lain.")
  }

  private class LimitedInputStream(private val delegate: InputStream, private var remaining: Long) : InputStream() {
    override fun read(): Int {
      if (remaining <= 0) throw IOException("limit")
      val b = delegate.read()
      if (b >= 0) remaining--
      return b
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
      if (remaining <= 0) throw IOException("limit")
      val n = delegate.read(b, off, minOf(len.toLong(), remaining).toInt())
      if (n > 0) remaining -= n
      return n
    }

    override fun close() = delegate.close()
  }
}
