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
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.components.EcoDropdownField
import com.example.ecosnap.core.designsystem.components.EcoPasswordField
import com.example.ecosnap.core.designsystem.components.EcoTextField
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.auth.components.AuthDivider
import com.example.ecosnap.features.auth.components.AuthLayout
import com.example.ecosnap.features.auth.components.AuthRoleSelector
import com.example.ecosnap.features.auth.model.AuthRole
import com.example.ecosnap.features.auth.model.RegistrationData
import com.example.ecosnap.features.auth.model.isValidEmail
import com.example.ecosnap.features.auth.model.isValidPhone

@Composable
fun RegisterScreen(
  onBack: () -> Unit,
  onLoginClick: () -> Unit,
  onHelp: () -> Unit,
  onRegister: (RegistrationData) -> Unit,
  initialRole: AuthRole = AuthRole.USER,
) {
  var roleName by rememberSaveable {
    mutableStateOf(initialRole.name)
  }

  val role = AuthRole.valueOf(roleName)

  var fullName by rememberSaveable {
    mutableStateOf("")
  }

  var email by rememberSaveable {
    mutableStateOf("")
  }

  var phone by rememberSaveable {
    mutableStateOf("")
  }

  var businessName by rememberSaveable {
    mutableStateOf("")
  }

  var businessCategory by rememberSaveable {
    mutableStateOf("")
  }

  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }

  var submitted by rememberSaveable {
    mutableStateOf(false)
  }

  AuthLayout(
    onBack = onBack,
    onHelp = onHelp
  ) {
    Text(
      text = if (role == AuthRole.USER) {
        "Buat akun Pengguna"
      } else {
        "Buat akun Mitra"
      },
      style = MaterialTheme.typography.titleLarge,
      color = EcoColor.Green900
    )

    Spacer(Modifier.height(4.dp))

    Text(
      text = "Untuk Anda yang ingin menggunakan layanan di EcoSnap",
      style = MaterialTheme.typography.bodyMedium,
      color = EcoColor.Green700
    )

    Spacer(Modifier.height(20.dp))

    AuthRoleSelector(
      selectedRole = role,
      onRoleSelected = { selected ->
        roleName = selected.name
        submitted = false
      }
    )

    Spacer(Modifier.height(20.dp))

    EcoTextField(
      value = fullName,
      onValueChange = { fullName = it },
      label = "Nama Lengkap",
      placeholder = "Masukkan nama lengkap",
      errorMessage = if (submitted && fullName.isBlank()) "Nama lengkap wajib diisi" else null
    )

    Spacer(Modifier.height(12.dp))

    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      EcoTextField(
        value = email,
        onValueChange = { email = it },
        label = "Email",
        placeholder = "nama@gmail.com",
        keyboardType = KeyboardType.Email,
        modifier = Modifier.weight(1f),
        errorMessage = if (submitted && !isValidEmail(email)) "Email tidak valid" else null
      )

      EcoTextField(
        value = phone,
        onValueChange = { phone = it },
        label = "Nomor telepon",
        placeholder = "08xxxxxxxxxx",
        keyboardType = KeyboardType.Phone,
        modifier = Modifier.weight(1f),
        errorMessage = if (submitted && !isValidPhone(phone)) "Nomor tidak valid" else null
      )
    }

    if (role == AuthRole.PARTNER) {
      Spacer(Modifier.height(12.dp))

      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        EcoTextField(
          value = businessName,
          onValueChange = { businessName = it },
          label = "Nama usaha",
          placeholder = "Nama usaha",
          modifier = Modifier.weight(1f),
          errorMessage = if (submitted && businessName.isBlank()) "Wajib diisi" else null
        )

        EcoDropdownField(
          value = businessCategory,
          onValueChange = { businessCategory = it },
          label = "Kategori usaha",
          placeholder = "Pilih kategori",
          options = listOf("Sampah Kayu", "Plastik", "Sampah Berbahaya"),
          modifier = Modifier.weight(1f),
          errorMessage = if (submitted && businessCategory.isBlank()) "Wajib dipilih" else null
        )
      }
    }

    Spacer(Modifier.height(12.dp))

    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      EcoPasswordField(
        value = password,
        onValueChange = { password = it },
        label = "Kata sandi",
        placeholder = "Buat kata sandi",
        modifier = Modifier.weight(1f),
        errorMessage = if (submitted && password.length < 8) "Minimal 8 karakter" else null
      )

      EcoPasswordField(
        value = confirmPassword,
        onValueChange = { confirmPassword = it },
        label = "Konfirmasi kata sandi",
        placeholder = "Ulangi kata sandi",
        modifier = Modifier.weight(1f),
        errorMessage = if (submitted && confirmPassword != password) "Tidak cocok" else null
      )
    }

    Spacer(Modifier.height(24.dp))

    EcoButton(
      text = if (role == AuthRole.USER) {
        "Daftar sebagai Pengguna"
      } else {
        "Daftar sebagai Mitra"
      },
      containerColor = EcoColor.Green800,
      contentColor = EcoColor.White,
      endIcon = {
        Icon(
          imageVector = Lucide.ArrowRight,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
      },
      onClick = {
        submitted = true

        val commonValid =
          fullName.isNotBlank() && isValidPhone(email) && isValidPhone(phone) && password.length >= 8 && password == confirmPassword

        val businessValid = role == AuthRole.USER || (businessName.isNotBlank() && businessCategory.isNotBlank())

        if (commonValid && businessValid) {
          onRegister(
            RegistrationData(
              role = role,
              fullName = fullName.trim(),
              email = email.trim(),
              phone = phone.trim(),
              password = password,
              businessName = if (role == AuthRole.PARTNER) businessName.trim() else null,
              businessCategory = if (role == AuthRole.PARTNER) businessCategory else null
            )
          )
        }
      }
    )

    Spacer(Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Sudah punya akun?",
        style = MaterialTheme.typography.bodyMedium,
        color = EcoColor.Green900
      )

      TextButton(
        onClick = onLoginClick
      ) {
        Text(
          text = "Masuk",
          color = EcoColor.Green700,
          style = MaterialTheme.typography.labelLarge
        )
      }
    }

    Spacer(Modifier.height(12.dp))

    AuthDivider()
  }
}