package com.example.ecosnap.core.designsystem.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Eye
import com.composables.icons.lucide.EyeOff
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun EcoPasswordField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  modifier: Modifier = Modifier,
  placeholder: String = "Masukkan kata sandi",
  errorMessage: String?= null
) {
  var isVisible by remember { mutableStateOf(false) }

  EcoTextField(
    value = value,
    onValueChange = onValueChange,
    label = label,
    modifier = modifier,
    placeholder = placeholder,
    keyboardType = KeyboardType.Password,
    errorMessage = errorMessage,
    visualTransformation = if (isVisible) {
      VisualTransformation.None
    } else {
      PasswordVisualTransformation()
    },
    trailiingIcon = {
      IconButton(
        onClick = { isVisible = !isVisible }
      ) {
        Icon(
          imageVector = if (isVisible) {
            Lucide.Eye
          } else {
            Lucide.EyeOff
          },
          contentDescription = if (isVisible) {
            "Sembunyikan kata sandi"
          } else {
            "Tampilkan kata sandi"
          },
          modifier = Modifier.size(20.dp),
          tint = EcoColor.Green700
        )
      }
    }
  )
}