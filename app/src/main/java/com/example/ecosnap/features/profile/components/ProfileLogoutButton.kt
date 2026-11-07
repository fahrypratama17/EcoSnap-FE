package com.example.ecosnap.features.profile.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.LogOut
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun ProfileLogoutButton(
  onLogoutClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  OutlinedButton(
    onClick = onLogoutClick,
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(999.dp),
    colors = ButtonDefaults.outlinedButtonColors(
      contentColor = EcoColor.TextError
    )
  ) {
    Icon(
      imageVector = Lucide.LogOut,
      contentDescription = null,
      modifier = Modifier.size(16.dp)
    )

    Spacer(Modifier.width(8.dp))

    Text(
      text = "Keluar Akun",
      style = MaterialTheme.typography.labelLarge
    )
  }
}