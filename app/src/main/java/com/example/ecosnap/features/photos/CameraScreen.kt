package com.example.ecosnap.features.photos

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

@Composable
fun CameraScreen(
  onBack: () -> Unit,
  onImageReady: (Uri) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val scope = rememberCoroutineScope()

  val cameraController = remember(context) {
    LifecycleCameraController(
      context.applicationContext
    ).apply {
      setEnabledUseCases(
        CameraController.IMAGE_CAPTURE
      )
      cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    }
  }

  var hasPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
      ) == PackageManager.PERMISSION_GRANTED
    )
  }

  var cameraReady by remember {
    mutableStateOf(false)
  }

  var usingFrontCamera by remember {
    mutableStateOf(false)
  }

  var torchEnabled by remember {
    mutableStateOf(false)
  }

  var isCapturing by remember {
    mutableStateOf(false)
  }

  var isImporting by remember {
    mutableStateOf(false)
  }

  var errorMessage by remember {
    mutableStateOf<String?>(null)
  }

  val permissionLauncher =
    rememberLauncherForActivityResult(
      ActivityResultContracts.RequestPermission()
    ) { granted ->
      hasPermission = granted

      if (!granted) {
        errorMessage = "Izin kamera ditolak. Galeri tetap bisa digunakan."
      } else {
        errorMessage = null
      }
    }

  val galleryLauncher =
    rememberLauncherForActivityResult(
      ActivityResultContracts.PickVisualMedia()
    ) { selectedUri ->
      if (selectedUri != null && !isImporting) {
        scope.launch {
          isImporting = true
          errorMessage = null

          try {
            val localUri = withContext(
              Dispatchers.IO
            ) {
              copyImageToCache(
                context,
                selectedUri
              )
            }

            onImageReady(localUri)
          } catch (e: Exception) {
            errorMessage = "Gagal membaca gambar dari galeri."
          } finally {
            isImporting = false
          }
        }
      }
    }

  LaunchedEffect(Unit) {
    if (!hasPermission) {
      permissionLauncher.launch(
        Manifest.permission.CAMERA
      )
    }
  }

  DisposableEffect(
    cameraController,
    lifecycleOwner,
    hasPermission
  ) {
    if (hasPermission) {
      try {
        cameraController.bindToLifecycle(lifecycleOwner)
        cameraReady = true
      } catch (e: Exception) {
        cameraReady = false
        errorMessage = "Kamera tidak dapat dibuka pada perangkat ini."
      }
    }

    onDispose {
      cameraReady = false
      cameraController.enableTorch(false)
      cameraController.unbind()
    }
  }

  fun capturePhoto() {
    if (!hasPermission || !cameraReady || isCapturing || isImporting ) {
      return
    }

    isCapturing = true
    errorMessage = null

    val photoFile = File(
      context.cacheDir,
      "ecosnap_${UUID.randomUUID()}.jpg"
    )

    val outputOption =
      ImageCapture.OutputFileOptions
        .Builder(photoFile)
        .build()

    try {
      cameraController.takePicture(
        outputOption,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {

          override fun onImageSaved(
            outputFileResult: ImageCapture.OutputFileResults
          ) {
            isCapturing = false
            onImageReady(Uri.fromFile(photoFile))
          }

          override fun onError(
            exception: ImageCaptureException
          ) {
            isCapturing = false
            photoFile.delete()
            errorMessage = "Gagal mengambil foto. Silakan coba lagi."
          }
        }
      )
    } catch (e: Exception) {
      isCapturing = false
      photoFile.delete()
      errorMessage = "Kamera belum siap mengambil foto."
    }
  }

  fun toggleTorch() {
    if (!hasPermission || !cameraReady || usingFrontCamera) {
      return
    }

    if (cameraController.cameraInfo?.hasFlashUnit() != true) {
      errorMessage = "Perangkat ini tidak memiliki lampu kamera."
      return
    }

    try {
      val nextState = !torchEnabled
      cameraController.enableTorch(nextState)
      torchEnabled = nextState
      errorMessage = null
    } catch (e: Exception) {
      errorMessage = "Tidak dapat mengatur lampu kamera".
    }
  }


}

private fun copyImageToCache(
  context: Context,
  sourceUri: Uri
): Uri {
  val mimeType = context.contentResolver.getType(sourceUri)

  val extension = when (mimeType) {
    "image/png" -> "png"
    "image/webp" -> "webp"
    "image/heic", "image/heif" -> "heic"
    else -> "jpg"
  }

  val destination = File(
    context.cacheDir,
    "ecosnap_${UUID.randomUUID()}.$extension"
  )

  try {
    val input = context.contentResolver
      .openInputStream(sourceUri)
      ?: error("Tidak dapat membuka gambar")

    input.use { source ->
      destination.outputStream().use { output ->
        source.copyTo(output)
      }
    }

    return Uri.fromFile(destination)
  } catch (e: Exception) {
    destination.delete()
    throw e
  }
}










