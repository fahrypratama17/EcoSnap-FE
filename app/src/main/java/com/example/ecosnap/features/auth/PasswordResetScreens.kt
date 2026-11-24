package com.example.ecosnap.features.auth

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mail
import com.example.ecosnap.R
import com.example.ecosnap.core.designsystem.components.*
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.auth.components.AuthDivider
import com.example.ecosnap.features.auth.components.AuthLayout
import com.example.ecosnap.features.auth.model.isValidEmail

/**
 * Real Firebase password reset: the email is sent by the SDK and the new password is set on the page
 * the link opens. After a send the screen shows the check-email state; there is no in-app new-password
 * form because no reset code ever reaches the app.
 */
@Composable
fun ForgotPasswordScreen(onBack: () -> Unit, onHelp: () -> Unit) {
  val viewModel: PasswordResetViewModel = viewModel()
  val state by viewModel.state.collectAsStateWithLifecycle()
  var email by rememberSaveable { mutableStateOf("") }
  var submitted by rememberSaveable { mutableStateOf(false) }
  val sentTo = state.sentTo

  AuthLayout(onBack = { if (!state.busy) onBack() }, onHelp = onHelp) {
    if (sentTo == null) {
      Spacer(Modifier.height(80.dp))
      Text("Lupa Kata Sandi", style = MaterialTheme.typography.titleLarge, color = EcoColor.Green900)
      Spacer(Modifier.height(4.dp))
      Text("Masukkan email akun Anda. Kami mengirim tautan untuk membuat kata sandi baru.", style = MaterialTheme.typography.bodyMedium, color = EcoColor.Green700)
      Spacer(Modifier.height(16.dp))
      EcoTextField(
        value = email,
        onValueChange = { email = it; viewModel.clearError() },
        label = "Email",
        placeholder = "nama@email.com",
        keyboardType = KeyboardType.Email,
        errorMessage = if (submitted && !isValidEmail(email)) "Masukkan email yang valid" else null,
        trailiingIcon = { Icon(Lucide.Mail, contentDescription = null, modifier = Modifier.size(18.dp)) }
      )
      Spacer(Modifier.height(16.dp))
      Text("Gunakan email yang terhubung ke akun Anda", style = MaterialTheme.typography.labelSmall, color = EcoColor.Green700)
      state.error?.let {
        Spacer(Modifier.height(8.dp))
        Text(it, style = MaterialTheme.typography.labelSmall, color = EcoColor.TextError)
      }
      Spacer(Modifier.height(12.dp))
      ResetButton(if (state.busy) "Mengirim..." else "Kirim Tautan Reset", enabled = !state.busy) {
        submitted = true
        if (isValidEmail(email)) viewModel.send(email)
      }
      ResetFooter()
    } else {
      CheckEmailContent(
        email = sentTo,
        busy = state.busy,
        error = state.error,
        onResend = { viewModel.send(sentTo) },
        onChangeEmail = viewModel::editEmail
      )
    }
  }
}

@Composable
private fun ColumnScope.CheckEmailContent(
  email: String,
  busy: Boolean,
  error: String?,
  onResend: () -> Unit,
  onChangeEmail: () -> Unit
) {
  val context = LocalContext.current
  var noMailApp by rememberSaveable { mutableStateOf(false) }
  Spacer(Modifier.height(32.dp))
  Image(painterResource(R.drawable.verified_mail_icon), contentDescription = null, modifier = Modifier.size(108.dp).align(Alignment.CenterHorizontally))
  Spacer(Modifier.height(28.dp))
  Text("Cek Email Anda", style = MaterialTheme.typography.titleLarge, color = EcoColor.Green900, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
  Spacer(Modifier.height(4.dp))
  Text("Jika $email terdaftar, tautan untuk membuat kata sandi baru sudah dikirim.", style = MaterialTheme.typography.bodyMedium, color = EcoColor.Green700, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
  Spacer(Modifier.height(20.dp))
  Text("Buka tautan di email, atur kata sandi baru di halaman yang terbuka, lalu masuk kembali ke EcoSnap. Periksa juga folder spam.", style = MaterialTheme.typography.bodyMedium, color = EcoColor.Green700, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().border(1.dp, EcoColor.Green400, RoundedCornerShape(16.dp)).background(EcoColor.White, RoundedCornerShape(16.dp)).padding(16.dp))
  (error ?: if (noMailApp) "Tidak ada aplikasi email di perangkat ini." else null)?.let {
    Spacer(Modifier.height(8.dp))
    Text(it, style = MaterialTheme.typography.labelSmall, color = EcoColor.TextError, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
  }
  Spacer(Modifier.height(32.dp))
  ResetButton("Buka Email", enabled = !busy) {
    val intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_EMAIL).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    noMailApp = try {
      context.startActivity(intent)
      false
    } catch (_: ActivityNotFoundException) {
      true
    }
  }
  Spacer(Modifier.height(12.dp))
  TextButton(onClick = onResend, enabled = !busy, modifier = Modifier.align(Alignment.CenterHorizontally)) {
    Text(if (busy) "Mengirim ulang..." else "Kirim ulang tautan", color = EcoColor.Green700)
  }
  TextButton(onClick = onChangeEmail, enabled = !busy, modifier = Modifier.align(Alignment.CenterHorizontally)) {
    Text("Gunakan email lain", color = EcoColor.Green700)
  }
  ResetFooter()
}

@Composable
private fun ResetButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
  EcoButton(text = text, onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth(), endIcon = { Icon(Lucide.ArrowRight, contentDescription = null, modifier = Modifier.size(18.dp)) })
}

@Composable
private fun ResetFooter() {
  Spacer(Modifier.height(24.dp))
  AuthDivider()
  Spacer(Modifier.height(20.dp))
  Text("Jaga keamanan akun EcoSnap Anda", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = EcoColor.Green700)
}
