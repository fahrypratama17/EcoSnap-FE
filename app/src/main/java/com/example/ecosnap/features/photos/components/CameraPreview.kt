package com.example.ecosnap.features.photos.components

import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CameraPreview(
  controller: LifecycleCameraController,
  modifier: Modifier = Modifier
) {
  AndroidView(
    modifier = modifier,
    factory = { context ->
      PreviewView(context).apply {
        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        scaleType = PreviewView.ScaleType.FILL_CENTER
        this.controller = controller
      }
    },
    update = { previewView ->
      previewView.controller = controller
    }
  )
}