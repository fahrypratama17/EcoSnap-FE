package com.example.ecosnap.features.profile.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun ProfilePageHeader(
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleLarge.copy(
        fontSize = 28.sp,
        lineHeight = 34.sp
      ),
      color = EcoColor.Green900
    )

    Spacer(Modifier.height(4.dp))

    Text(
      text = subtitle,
      style = MaterialTheme.typography.bodyMedium.copy(
        fontSize = 12.sp,
        lineHeight = 18.sp
      ),
      color = EcoColor.Green700
    )
  }
}