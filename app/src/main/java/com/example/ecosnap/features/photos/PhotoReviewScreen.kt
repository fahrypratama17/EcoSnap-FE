package com.example.ecosnap.features.photos

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor
import java.io.File

/**
 * Photo confirmation step (original FE layout). Nothing is uploaded here: the photo is only sent to the
 * AI service after [onAnalyze] opens the result screen, whose [PhotoScanViewModel] starts the scan.
 *
 * [photoUri] null/blank/unreadable shows a recoverable message instead of the preview.
 */
@Composable
fun PhotoReviewScreen(
  photoUri: Uri?,
  onAnalyze: () -> Unit,
  onRetake: () -> Unit,
  onCancel: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val readable = remember(photoUri) { photoUri != null && isReviewImageReadable(context, photoUri) }
  var loadFailed by remember(photoUri) { mutableStateOf(false) }
  val usable = readable && !loadFailed

  BackHandler(onBack = onCancel)

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Text(
        "Konfirmasi Foto Sampah",
        style = MaterialTheme.typography.titleLarge,
        color = EcoColor.Green900
      )
    }
    item {
      EcoCard {
        if (usable) {
          AsyncImage(
            model = photoUri,
            contentDescription = "Foto sampah yang akan dianalisis",
            modifier = Modifier.fillMaxWidth().height(300.dp),
            contentScale = ContentScale.Fit,
            onError = { loadFailed = true }
          )
        } else {
          Text(
            "Foto tidak ditemukan atau tidak dapat dibaca. Silakan ambil atau pilih foto kembali.",
            style = MaterialTheme.typography.bodyMedium,
            color = EcoColor.TextError
          )
        }
      }
    }
    item {
      Text(
        "Pastikan satu objek sampah terlihat jelas. Foto akan dikirim ke layanan AI untuk dianalisis.",
        style = MaterialTheme.typography.bodyMedium,
        color = EcoColor.Green700
      )
    }
    item {
      EcoButton(
        text = "Analisis Foto",
        enabled = usable,
        onClick = onAnalyze,
        containerColor = EcoColor.Green700
      )
      TextButton(onClick = onRetake) {
        Text("Ambil atau Pilih Foto Lain", color = EcoColor.Green700)
      }
      TextButton(onClick = onCancel) {
        Text("Batal", color = EcoColor.Green700)
      }
    }
  }
}

/** True when the image exists and can be opened (file: must exist; content: must open). */
fun isReviewImageReadable(context: Context, uri: Uri): Boolean = try {
  when (uri.scheme) {
    "file" -> uri.path?.let { File(it) }?.let { it.isFile && it.length() > 0 } == true
    "content" -> context.contentResolver.openInputStream(uri)?.use { true } == true
    else -> false
  }
} catch (e: Exception) {
  false
}

/**
 * Deletes [uri] only when it is this app's own capture/import copy (direct child of cacheDir named
 * `ecosnap_*`), matching [PhotoScanViewModel.onCleared]. Anything else is left untouched.
 */
fun deleteOwnedReviewImage(context: Context, uri: Uri?) {
  if (uri?.scheme != "file") return
  val file = uri.path?.let { File(it) } ?: return
  if (file.parentFile == context.cacheDir && file.name.startsWith("ecosnap_")) file.delete()
}
