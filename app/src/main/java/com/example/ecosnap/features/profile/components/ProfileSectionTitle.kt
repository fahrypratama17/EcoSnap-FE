package com.example.ecosnap.features.profile.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun ProfileSectionTitle(
  title: String,
  modifier: Modifier = Modifier
) {
  Text(
    text = title,
    modifier = modifier.fillMaxWidth(),
    style = MaterialTheme.typography.titleMedium.copy(
      fontSize = 16.sp,
      lineHeight = 22.sp
    ),
    color = EcoColor.Green900
  )
}