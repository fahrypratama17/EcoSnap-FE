package com.example.ecosnap.features.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.composables.icons.lucide.Camera
import com.composables.icons.lucide.Images
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.SwitchCamera
import com.composables.icons.lucide.X
import com.composables.icons.lucide.Zap
import com.composables.icons.lucide.ZapOff
import com.example.ecosnap.R
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.photos.components.CameraPreview
import com.example.ecosnap.features.scan.components.ScanQrControlButton
import com.example.ecosnap.features.scan.components.ScanQrShutterButton
import com.example.ecosnap.features.scan.data.LocalDeposits
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val ScanBackground = Color(0xFF17261F)
private val PreviewPlaceholder = Color(0xFFECEDE3)
private val SubtitleColor = Color(0xFFBAC9BC)
private val GalleryBorderColor = Color(0xFF849B88)
private val ErrorTextColor = Color(0xFFFFCCBF)

// Posisi bingkai di Figma: 85dp dari atas pada preview setinggi 560dp.
private val FrameGuideAlignment = BiasAlignment(0f, -0.35f)

@Composable
fun ScanQrScreen(
  onBack: () -> Unit,
  onQrScanned: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val scope = rememberCoroutineScope()

  val cameraController = remember(context) {
    LifecycleCameraController(
      context.applicationContext
    ).apply {
      // Nanti analyzer pembaca QR dipasang di use case ini.
      setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
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

  var cameraReady by remember { mutableStateOf(false) }
  var usingFrontCamera by remember { mutableStateOf(false) }
  var torchEnabled by remember { mutableStateOf(false) }
  var isVerifying by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  fun verifyQr() {
    if (isVerifying) return

    scope.launch {
      isVerifying = true
      errorMessage = null

      // Belum ada decoder QR: hasil scan disimulasikan dengan payload dummy.
      delay(800)
      onQrScanned(LocalDeposits.demoQrPayload)

      isVerifying = false
    }
  }

  val permissionLauncher =
    rememberLauncherForActivityResult(
      ActivityResultContracts.RequestPermission()
    ) { granted ->
      hasPermission = granted
      errorMessage = if (granted) {
        null
      } else {
        "Izin kamera ditolak. QR tetap bisa dipilih dari galeri."
      }
    }

  val galleryLauncher =
    rememberLauncherForActivityResult(
      ActivityResultContracts.PickVisualMedia()
    ) { selectedUri ->
      if (selectedUri != null) {
        verifyQr()
      }
    }

  LaunchedEffect(Unit) {
    if (!hasPermission) {
      permissionLauncher.launch(Manifest.permission.CAMERA)
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

  fun toggleTorch() {
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

  val cameraUsable = hasPermission && cameraReady

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(ScanBackground),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(76.dp)
        .padding(horizontal = 24.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      ScanHeaderButton(
        icon = Lucide.X,
        contentDescription = "Tutup scan QR",
        onClick = onBack
      )

      Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Text(
          text = "Scan QR",
          style = MaterialTheme.typography.titleLarge.copy(
            lineHeight = 23.sp,
            letterSpacing = 0.sp
          ),
          color = Color.White
        )

        Text(
          text = "Arahkan kamera ke kode QR mitra",
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 12.sp,
            lineHeight = 15.sp,
            letterSpacing = 0.sp
          ),
          color = SubtitleColor
        )
      }

      ScanHeaderButton(
        icon = if (torchEnabled) Lucide.Zap else Lucide.ZapOff,
        contentDescription = if (torchEnabled) {
          "Matikan lampu"
        } else {
          "Nyalakan lampu"
        },
        onClick = { toggleTorch() },
        enabled = cameraUsable && !usingFrontCamera
      )
    }

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .padding(horizontal = 16.dp)
        .clip(RoundedCornerShape(24.dp))
        .background(PreviewPlaceholder),
      contentAlignment = Alignment.Center
    ) {
      if (hasPermission) {
        CameraPreview(
          controller = cameraController,
          modifier = Modifier.fillMaxSize()
        )

        Image(
          painter = painterResource(R.drawable.scan_qr_frame_guide),
          contentDescription = null,
          modifier = Modifier
            .align(FrameGuideAlignment)
            .fillMaxWidth(311f / 396f)
            .aspectRatio(311f / 303f)
        )
      } else {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Lucide.Camera,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = EcoColor.Green800
          )

          Text(
            text = "Izin kamera diperlukan",
            style = MaterialTheme.typography.titleMedium,
            color = EcoColor.Green900
          )

          TextButton(
            onClick = {
              permissionLauncher.launch(Manifest.permission.CAMERA)
            }
          ) {
            Text(
              text = "Izinkan Kamera",
              color = EcoColor.Green700
            )
          }
        }
      }

      Box(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
          .heightIn(min = 32.dp)
          .background(
            ScanBackground.copy(alpha = 0.64f),
            RoundedCornerShape(16.dp)
          )
          .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = errorMessage ?: "Posisikan QR di dalam kotak",
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 12.sp,
            lineHeight = 15.sp,
            letterSpacing = 0.sp
          ),
          color = if (errorMessage != null) ErrorTextColor else Color.White,
          textAlign = TextAlign.Center
        )
      }

      if (isVerifying) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f)),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            CircularProgressIndicator(color = Color.White)

            Text(
              text = "Memverifikasi QR...",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                lineHeight = 15.sp,
                letterSpacing = 0.sp
              ),
              color = Color.White
            )
          }
        }
      }
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(134.dp)
        .padding(start = 49.dp, end = 49.dp, bottom = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      ScanQrControlButton(
        icon = Lucide.Images,
        label = "Galeri",
        onClick = {
          galleryLauncher.launch(
            PickVisualMediaRequest(
              ActivityResultContracts.PickVisualMedia.ImageOnly
            )
          )
        },
        shape = RoundedCornerShape(14.dp),
        borderColor = GalleryBorderColor,
        enabled = !isVerifying
      )

      ScanQrShutterButton(
        onClick = { verifyQr() },
        enabled = cameraUsable && !isVerifying
      )

      ScanQrControlButton(
        icon = Lucide.SwitchCamera,
        label = "Balik",
        onClick = { switchCamera() },
        shape = CircleShape,
        containerColor = Color.White.copy(alpha = 0.07f),
        enabled = cameraUsable && !isVerifying
      )
    }
  }
}

@Composable
private fun ScanHeaderButton(
  icon: ImageVector,
  contentDescription: String,
  onClick: () -> Unit,
  enabled: Boolean = true
) {
  IconButton(
    onClick = onClick,
    enabled = enabled,
    modifier = Modifier
      .size(44.dp)
      .background(
        Color.White.copy(alpha = 0.09f),
        CircleShape
      )
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = Color.White.copy(alpha = if (enabled) 1f else 0.4f),
      modifier = Modifier.size(21.dp)
    )
  }
}
