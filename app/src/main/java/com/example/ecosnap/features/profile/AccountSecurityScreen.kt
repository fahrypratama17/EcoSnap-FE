package com.example.ecosnap.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.components.EcoTextField
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.profile.components.ProfileBackTextButton
import com.example.ecosnap.features.profile.components.ProfileCardContainer
import com.example.ecosnap.features.profile.components.ProfilePageHeader
import com.example.ecosnap.features.profile.components.ProfileTopBar
import com.example.ecosnap.features.profile.data.ProfileInputException
import com.example.ecosnap.features.profile.data.ProfileRepository

/**
 * Edits the only fields the backend allows (name, phone) and requests a Firebase reset email.
 * Email change, profile photo and account deletion have no backend support, so none is offered.
 */
@Composable
fun AccountSecurityScreen(
  state: AccountUiState,
  onRetry: () -> Unit,
  onSave: (String, String) -> Unit,
  onResetPassword: () -> Unit,
  onBack: () -> Unit,
  onSettingsClick: () -> Unit,
  onProfileClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val profile = state.profile
  var name by rememberSaveable(profile?.fullName) { mutableStateOf(profile?.fullName.orEmpty()) }
  var phone by rememberSaveable(profile?.phone) { mutableStateOf(profile?.phone.orEmpty()) }
  val nameError = validationError { ProfileRepository.validName(name) }
  val phoneError = validationError { ProfileRepository.validPhone(phone) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White)
  ) {
    ProfileTopBar(
      onSettingsClick = onSettingsClick,
      onProfileClick = onProfileClick
    )

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        ProfileBackTextButton(
          text = "Kembali ke Profil",
          onClick = onBack
        )
      }

      item {
        ProfilePageHeader(
          title = "Akun & Keamanan",
          subtitle = "Ubah data profil dan atur ulang kata sandi."
        )
      }

      if (state.loading) {
        item {
          Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = EcoColor.Green700)
          }
        }
      }

      state.loadError?.let { message ->
        item {
          Column {
            Text(message, color = EcoColor.TextError, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onRetry) { Text("Coba lagi", color = EcoColor.Green700) }
          }
        }
      }

      if (profile != null) {
        item {
          ProfileCardContainer {
            Text("Email", style = MaterialTheme.typography.labelSmall, color = EcoColor.Green900)
            Text(
              text = profile.email.ifEmpty { "Tidak tersedia" },
              style = MaterialTheme.typography.bodyMedium,
              color = EcoColor.Green700
            )
            Text(
              text = "Email dikelola oleh akun Firebase dan belum dapat diubah dari aplikasi.",
              style = MaterialTheme.typography.labelSmall,
              color = EcoColor.Green700
            )
          }
        }

        item {
          ProfileCardContainer {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
              EcoTextField(
                value = name,
                onValueChange = { name = it; },
                label = "Nama lengkap",
                errorMessage = nameError.takeIf { name != profile.fullName }
              )
              EcoTextField(
                value = phone,
                onValueChange = { phone = it },
                label = "Nomor telepon",
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done,
                errorMessage = phoneError.takeIf { phone != profile.phone }
              )

              state.saveError?.let {
                Text(it, color = EcoColor.TextError, style = MaterialTheme.typography.bodyMedium)
              }
              if (state.saved) {
                Text("Profil berhasil disimpan.", color = EcoColor.Green700, style = MaterialTheme.typography.bodyMedium)
              }

              val changed = name.trim() != profile.fullName ||
                phone.trim().filterNot { it == ' ' || it == '-' } != profile.phone
              EcoButton(
                text = if (state.saving) "Menyimpan..." else "Simpan Perubahan",
                onClick = { onSave(name, phone) },
                enabled = changed && !state.saving && nameError == null && phoneError == null,
                modifier = Modifier.fillMaxWidth()
              )
            }
          }
        }

        item {
          ProfileCardContainer {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text("Kata sandi", style = MaterialTheme.typography.titleMedium, color = EcoColor.Green900)
              Text(
                text = "Kami mengirim tautan dari Firebase ke email akunmu. Kata sandi baru diatur di halaman yang terbuka dari tautan itu.",
                style = MaterialTheme.typography.bodyMedium,
                color = EcoColor.Green700
              )
              state.resetMessage?.let {
                Text(
                  it,
                  color = if (state.resetFailed) EcoColor.TextError else EcoColor.Green700,
                  style = MaterialTheme.typography.bodyMedium
                )
              }
              EcoButton(
                text = if (state.resetBusy) "Mengirim..." else "Kirim Email Reset Kata Sandi",
                onClick = onResetPassword,
                enabled = !state.resetBusy && profile.email.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
              )
            }
          }
        }
      }
    }
  }
}

private inline fun validationError(check: () -> Unit): String? = try {
  check()
  null
} catch (e: ProfileInputException) {
  e.message
}
