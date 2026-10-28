package com.example.ecosnap.features.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mail
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.components.EcoPasswordField
import com.example.ecosnap.core.designsystem.components.EcoTextField
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.auth.components.AuthDivider
import com.example.ecosnap.features.auth.components.AuthLayout
import com.example.ecosnap.features.auth.model.isValidEmail

@Composable
fun LoginScreen(
  onBack: () -> Unit,
  onRegisterClick: () -> Unit,
  onForgotPassword: () -> Unit,
  onHelp: () -> Unit,
  onLogin: (String, String) -> Unit
) {
  var email by remember { mutableStateOf("") }

  var password by remember { mutableStateOf("") }

  var submitted by rememberSaveable { mutableStateOf(false) }

  AuthLayout(
    onBack = onBack,
    onHelp = onHelp
  ) {
    Text(
      text = "Masuk ke akun Anda",
      style = MaterialTheme.typography.titleLarge,
      color = EcoColor.Green900
    )

    Spacer(Modifier.height(4.dp))

    Text(
      text = "Gunakan email dan kata sandi yang terdaftar.",
      style = MaterialTheme.typography.bodyMedium,
      color = EcoColor.Green700
    )

    Spacer(Modifier.height(28.dp))

    EcoTextField(
      value = email,
      onValueChange = { email = it },
      label = "Email",
      placeholder =   "nama@gmail.com",
      keyboardType = KeyboardType.Email,
      errorMessage = if (submitted && !isValidEmail(email)) "Masukkan email yang valid" else null,
      trailiingIcon = {
        Icon(
          imageVector = Lucide.Mail,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
      }
    )

    Spacer(Modifier.height(16.dp))

    EcoPasswordField(
      value = password,
      onValueChange = { password = it },
      label = "Kata sandi",
      errorMessage = if (submitted && password.isBlank()) "Kata sandi wajib diisi" else null
    )

    TextButton(
      onClick = onForgotPassword,
      modifier = Modifier.align(Alignment.End)
    ) {
      Text(
        text = "Lupa kata sandi?",
        color = EcoColor.Green700,
        style = MaterialTheme.typography.labelSmall
      )
    }

    Spacer(Modifier.height(24.dp))

    EcoButton(
      text = "Masuk",
      onClick = {
        submitted = true

        if (isValidEmail(email) && password.isNotBlank()) {
          onLogin(email.trim(), password)
        }
      },

      containerColor = EcoColor.Green800,
      contentColor = EcoColor.White,
      endIcon = {
        Icon(
          imageVector = Lucide.ArrowRight,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
      }
    )

    Spacer(Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
      ) {
      Text(
        text = "Belum punya akun?",
        style = MaterialTheme.typography.bodyMedium,
        color = EcoColor.Green900
      )

      TextButton(
        onClick = onRegisterClick
      ) {
        Text(
          text = "Daftar sekarang",
          color = EcoColor.Green700,
          style = MaterialTheme.typography.labelLarge
        )
      }
    }

    Spacer(Modifier.height(12.dp))

    AuthDivider()

    Spacer(Modifier.height(16.dp))

    Text(
      text = "Masuk untuk melanjutkan ke akun Anda.",
      style = MaterialTheme.typography.labelSmall,
      color = EcoColor.Green700,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth()
    )
  }
}