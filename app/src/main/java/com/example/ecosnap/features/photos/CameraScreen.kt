package com.example.ecosnap.features.photos

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.composables.icons.lucide.Camera
import com.composables.icons.lucide.Flashlight
import com.composables.icons.lucide.FlashlightOff
import com.composables.icons.lucide.Images
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.SwitchCamera
import com.composables.icons.lucide.X
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.photos.components.CameraControlButton
import com.example.ecosnap.features.photos.components.CameraPreview
import com.example.ecosnap.features.photos.components.CameraShutterButton
import com.example.ecosnap.features.photos.components.PhotoFrameOverlay
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
      errorMessage = "Tidak dapat mengatur lampu kamera."
    }
  }

  fun switchCamera() {
    if (!hasPermission || !cameraReady) return

    try {
      cameraController.enableTorch(false)
      torchEnabled = false

      val nextFront = !usingFrontCamera

      cameraController.cameraSelector =
        if (nextFront) {
          CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
          CameraSelector.DEFAULT_BACK_CAMERA
        }

      usingFrontCamera = nextFront
      errorMessage = null
    } catch (e: Exception) {
      errorMessage = "Kamera yang dipilih tidak tersedia."
    }
  }

  val busy = isCapturing || isImporting

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF172A24))
      .padding(12.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onBack) {
        Icon(
          imageVector = Lucide.X,
          contentDescription = "Tutup kamera",
          tint = Color.White
        )
      }

      Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Foto sampah",
          style = MaterialTheme.typography.titleMedium,
          color = Color.White
        )

        Text(
          text = "Kenali jenisnya dengan mudah",
          fontSize = 11.sp,
          color = EcoColor.Green200
        )
      }

      IconButton(
        onClick = { toggleTorch() },
        enabled = hasPermission && cameraReady && !usingFrontCamera
      ) {
        Icon(
          imageVector = if (torchEnabled) {
            Lucide.FlashlightOff
          } else {
            Lucide.Flashlight
          },
          contentDescription =
            if (torchEnabled) "Matikan lampu"
            else "Nyalakan lampu",
          tint = Color.White
        )
      }
    }

    Spacer(Modifier.height(10.dp))

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .clip(RoundedCornerShape(18.dp))
        .background(EcoColor.Green800),
      contentAlignment = Alignment.Center
    ) {
      if (hasPermission) {
        CameraPreview(
          controller = cameraController,
          modifier = Modifier.fillMaxSize()
        )

        PhotoFrameOverlay(
          modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
        )

        Surface(
          modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 14.dp),
          shape = CircleShape,
          color = Color.Black.copy(alpha = 0.45f)
        ) {
          Text(
            text = "Satu objek dalam bingkai",
            fontSize = 11.sp,
            color = Color.White,
            modifier = Modifier.padding(
              horizontal = 12.dp,
              vertical = 7.dp
            )
          )
        }

      } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Lucide.Camera,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Color.White
          )

          Spacer(Modifier.height(10.dp))

          Text(
            text = "Izin kamera diperlukan",
            color = Color.White
          )

          Spacer(Modifier.height(8.dp))

          TextButton(
            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
          ) {
            Text(
              text = "Izinkan Kamera",
              color = EcoColor.Green100
            )
          }
        }
      }

      if (busy) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Color.Black.copy(alpha = 0.25f)
            ),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator(
            color = Color.White
          )
        }
      }
    }

    Spacer(Modifier.height(12.dp))

    Text(
      text = "Pastikan sampah terlihat jelas",
      style = MaterialTheme.typography.labelLarge,
      color = Color.White
    )

    Text(
      text = "Gunakan cahaya cukup dan hindari foto buram",
      fontSize = 11.sp,
      color = EcoColor.Green200
    )

    if (errorMessage != null) {
      Spacer(Modifier.height(8.dp))

      Text(
        text = errorMessage.orEmpty(),
        color = Color(0xFFFFCCBF),
        fontSize = 12.sp
      )
    }

    Spacer(Modifier.height(18.dp))

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically
    ) {
      CameraControlButton(
        icon = Lucide.Images,
        label = "Galeri",
        onClick = {
          galleryLauncher.launch(
            PickVisualMediaRequest(
              ActivityResultContracts.PickVisualMedia.ImageOnly
            )
          )
        },
        enabled = !busy
      )

      CameraShutterButton(
        onClick = { capturePhoto() },
        loading = busy,
        enabled = hasPermission && cameraReady
      )

      CameraControlButton(
        icon = Lucide.SwitchCamera,
        label = "Balik",
        onClick = { switchCamera() },
        enabled = hasPermission && cameraReady && !busy
      )
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