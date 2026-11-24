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
import androidx.compose.runtime.DisposableEffect
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
import com.example.ecosnap.core.auth.AccountRole
import com.example.ecosnap.core.auth.SessionOutcome
import com.example.ecosnap.features.auth.components.AuthDivider
import com.example.ecosnap.features.auth.components.AuthLayout
import com.example.ecosnap.features.auth.components.AuthRoleSelector
import com.example.ecosnap.features.auth.model.AuthRole
import com.example.ecosnap.features.auth.model.RegistrationData
import com.example.ecosnap.features.auth.model.isValidEmail
import com.example.ecosnap.features.auth.model.isValidPhone
import com.example.ecosnap.features.auth.model.isValidProfileText

@Composable
fun RegisterScreen(
  onBack: () -> Unit,
  onLoginClick: () -> Unit,
  onHelp: () -> Unit,
  onRegister: (RegistrationData) -> Unit,
  initialRole: AuthRole = AuthRole.USER,
  completion: SessionOutcome.NeedsProfile? = null,
  busy: Boolean = false,
  error: String? = null,
  onInputChanged: () -> Unit = {},
) {
  var roleName by rememberSaveable {
    mutableStateOf(
      completion?.draft?.role?.let {
        if (it == AccountRole.USER) AuthRole.USER.name else AuthRole.PARTNER.name
      } ?: initialRole.name
    )
  }

  val role = AuthRole.valueOf(roleName)

  var fullName by rememberSaveable {
    mutableStateOf(completion?.draft?.fullName.orEmpty())
  }

  var email by rememberSaveable {
    mutableStateOf(completion?.email.orEmpty())
  }

  var phone by rememberSaveable {
    mutableStateOf(completion?.draft?.phone.orEmpty())
  }

  var businessName by rememberSaveable {
    mutableStateOf(completion?.draft?.businessName.orEmpty())
  }

  var businessCategory by rememberSaveable {
    mutableStateOf(completion?.draft?.businessCategory.orEmpty())
  }

  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }

  var submitted by rememberSaveable {
    mutableStateOf(false)
  }

  DisposableEffect(Unit) {
    onDispose { password = ""; confirmPassword = "" }
  }

  AuthLayout(
    onBack = { if (!busy) onBack() },
    onHelp = { if (!busy) onHelp() }
  ) {
    Text(
      text = if (completion != null) {
        "Lengkapi profil akun"
      } else if (role == AuthRole.USER) {
        "Buat akun Pengguna"
      } else {
        "Buat akun Mitra"
      },
      style = MaterialTheme.typography.titleLarge,
      color = EcoColor.Green900
    )

    Spacer(Modifier.height(4.dp))

    Text(
      text = if (completion != null) "Email sudah terdaftar. Lengkapi profil untuk melanjutkan."
        else "Untuk Anda yang ingin menggunakan layanan di EcoSnap",
      style = MaterialTheme.typography.bodyMedium,
      color = EcoColor.Green700
    )

    Spacer(Modifier.height(20.dp))

    AuthOperationStatus(busy = busy, error = error)

    AuthRoleSelector(
      selectedRole = role,
      onRoleSelected = { selected ->
        if (!busy) {
          roleName = selected.name
          submitted = false
          onInputChanged()
        }
      }
    )

    Spacer(Modifier.height(20.dp))

    EcoTextField(
      value = fullName,
      onValueChange = { if (!busy) { fullName = it; onInputChanged() } },
      label = "Nama Lengkap",
      placeholder = "Masukkan nama lengkap",
      errorMessage = if (submitted && !isValidProfileText(fullName, 150)) "Isi nama, maksimal 150 karakter" else null
    )

    Spacer(Modifier.height(12.dp))

    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      if (completion != null) {
        Text(
          text = "Email akun\n${completion.email}",
          style = MaterialTheme.typography.bodyMedium,
          color = EcoColor.Green900,
          modifier = Modifier.weight(1f)
        )
      } else {
        EcoTextField(
          value = email,
          onValueChange = { if (!busy) { email = it; onInputChanged() } },
          label = "Email",
          placeholder = "nama@gmail.com",
          keyboardType = KeyboardType.Email,
          modifier = Modifier.weight(1f),
          errorMessage = if (submitted && !isValidEmail(email)) "Email tidak valid (maksimal 255 byte)" else null
        )
      }

      EcoTextField(
        value = phone,
        onValueChange = { if (!busy) { phone = it; onInputChanged() } },
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
          onValueChange = { if (!busy) { businessName = it; onInputChanged() } },
          label = "Nama usaha",
          placeholder = "Nama usaha",
          modifier = Modifier.weight(1f),
          errorMessage = if (submitted && !isValidProfileText(businessName, 150)) "Isi nama usaha, maksimal 150 karakter" else null
        )

        EcoDropdownField(
          value = businessCategory,
          onValueChange = { if (!busy) { businessCategory = it; onInputChanged() } },
          label = "Kategori usaha",
          placeholder = "Pilih kategori",
          options = listOf("Sampah Kayu", "Plastik", "Sampah Berbahaya"),
          modifier = Modifier.weight(1f),
          errorMessage = if (submitted && !isValidProfileText(businessCategory, 80)) "Pilih kategori, maksimal 80 karakter" else null
        )
      }
    }

    if (completion == null) {
    Spacer(Modifier.height(12.dp))

    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      EcoPasswordField(
        value = password,
        onValueChange = { if (!busy) { password = it; onInputChanged() } },
        label = "Kata sandi",
        placeholder = "Buat kata sandi",
        modifier = Modifier.weight(1f),
        errorMessage = if (submitted && password.length < 8) "Minimal 8 karakter" else null
      )

      EcoPasswordField(
        value = confirmPassword,
        onValueChange = { if (!busy) { confirmPassword = it; onInputChanged() } },
        label = "Konfirmasi kata sandi",
        placeholder = "Ulangi kata sandi",
        modifier = Modifier.weight(1f),
        errorMessage = if (submitted && confirmPassword != password) "Tidak cocok" else null
      )
    }
    }

    Spacer(Modifier.height(24.dp))

    EcoButton(
      enabled = !busy,
      text = if (busy) {
        "Sedang memproses…"
      } else if (completion != null) {
        "Simpan profil"
      } else if (role == AuthRole.USER) {
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

        val commonValid = isValidProfileText(fullName, 150) &&
          isValidEmail(completion?.email ?: email) && isValidPhone(phone) &&
          (completion != null || (password.length >= 8 && password == confirmPassword))

        val businessValid = role == AuthRole.USER ||
          (isValidProfileText(businessName, 150) && isValidProfileText(businessCategory, 80))

        if (!busy && commonValid && businessValid) {
          onRegister(
            RegistrationData(
              role = role,
              fullName = fullName.trim(),
              email = completion?.email ?: email.trim(),
              phone = phone.trim(),
              password = if (completion == null) password else "",
              businessName = if (role == AuthRole.PARTNER) businessName.trim() else null,
              businessCategory = if (role == AuthRole.PARTNER) businessCategory.trim() else null
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
        text = if (completion == null) "Sudah punya akun?" else "Gunakan akun lain?",
        style = MaterialTheme.typography.bodyMedium,
        color = EcoColor.Green900
      )

      TextButton(
        onClick = onLoginClick,
        enabled = !busy
      ) {
        Text(
          text = if (completion == null) "Masuk" else "Keluar",
          color = EcoColor.Green700,
          style = MaterialTheme.typography.labelLarge
        )
      }
    }

    Spacer(Modifier.height(12.dp))

    AuthDivider()
  }
}