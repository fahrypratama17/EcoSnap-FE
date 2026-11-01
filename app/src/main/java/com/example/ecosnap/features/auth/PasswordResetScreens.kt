package com.example.ecosnap.features.auth

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
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mail
import com.example.ecosnap.R
import com.example.ecosnap.core.designsystem.components.*
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.auth.components.AuthDivider
import com.example.ecosnap.features.auth.components.AuthLayout
import com.example.ecosnap.features.auth.model.isValidEmail

@Composable
fun ForgotPasswordScreen(onBack: () -> Unit, onHelp: () -> Unit, onContinue: () -> Unit) {
  var email by rememberSaveable { mutableStateOf("") }
  var submitted by rememberSaveable { mutableStateOf(false) }
  AuthLayout(onBack = onBack, onHelp = onHelp) {
    Spacer(Modifier.height(80.dp))
    Text("Lupa Kata Sandi", style = MaterialTheme.typography.titleLarge, color = EcoColor.Green900)
    Spacer(Modifier.height(4.dp))
    Text("Masukkan email yang terdaftar di EcoSnap. Kami akan mengirimkan tautan untuk reset kata sandi Anda", style = MaterialTheme.typography.bodyMedium, color = EcoColor.Green700)
    Spacer(Modifier.height(12.dp))
    Text("Fitur reset kata sandi belum terhubung ke layanan akun.", style = MaterialTheme.typography.labelSmall, color = EcoColor.Green700)
    Spacer(Modifier.height(16.dp))
    EcoTextField(value = email, onValueChange = { email = it }, label = "Email", placeholder = "nama@email.com", keyboardType = KeyboardType.Email, errorMessage = if (submitted && !isValidEmail(email)) "Masukkan email yang valid" else null, trailiingIcon = { Icon(Lucide.Mail, contentDescription = null, modifier = Modifier.size(18.dp)) })
    Spacer(Modifier.height(16.dp))
    Text("Gunakan email yang terhubung ke akun Anda", style = MaterialTheme.typography.labelSmall, color = EcoColor.Green700)
    Spacer(Modifier.height(12.dp))
    ResetButton("Kirim Tautan Reset") { submitted = true; if (isValidEmail(email)) onContinue() }
    ResetFooter()
  }
}

@Composable
fun CheckEmailScreen(onBack: () -> Unit, onHelp: () -> Unit, onContinue: () -> Unit) {
  val context = LocalContext.current
  AuthLayout(onBack = onBack, onHelp = onHelp) {
    Spacer(Modifier.height(32.dp))
    Image(painterResource(R.drawable.verified_mail_icon), contentDescription = null, modifier = Modifier.size(108.dp).align(Alignment.CenterHorizontally))
    Spacer(Modifier.height(28.dp))
    Text("Cek Email Anda", style = MaterialTheme.typography.titleLarge, color = EcoColor.Green900, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    Spacer(Modifier.height(4.dp))
    Text("Pratinjau: tautan reset belum dikirim. Layanan akun belum terhubung.", style = MaterialTheme.typography.bodyMedium, color = EcoColor.Green700, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    Spacer(Modifier.height(20.dp))
    Text("Periksa kotak masuk atau folder spam", style = MaterialTheme.typography.bodyMedium, color = EcoColor.Green700, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().border(1.dp, EcoColor.Green400, RoundedCornerShape(16.dp)).background(EcoColor.White, RoundedCornerShape(16.dp)).padding(16.dp))
    Spacer(Modifier.height(40.dp))
    ResetButton("Buka Email") {
      val intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_EMAIL).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      if (intent.resolveActivity(context.packageManager) != null) context.startActivity(intent)
    }
    Spacer(Modifier.height(12.dp))
    Text("Belum menerima email?", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = EcoColor.Green900)
    TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Kirim ulang tautan", color = EcoColor.Green700) }
    TextButton(onClick = onContinue, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Buat kata sandi baru", color = EcoColor.Green700) }
    ResetFooter()
  }
}

@Composable
fun NewPasswordScreen(onBack: () -> Unit, onHelp: () -> Unit, onSave: () -> Unit) {
  var password by rememberSaveable { mutableStateOf("") }
  var confirmation by rememberSaveable { mutableStateOf("") }
  var submitted by rememberSaveable { mutableStateOf(false) }
  AuthLayout(onBack = onBack, onHelp = onHelp) {
    Spacer(Modifier.height(48.dp))
    Text("Buat Kata Sandi Baru", style = MaterialTheme.typography.titleLarge, color = EcoColor.Green900)
    Spacer(Modifier.height(4.dp))
    Text("Buat kata sandi yang kuat dan mudah Anda ingat untuk akun EcoSnap", style = MaterialTheme.typography.bodyMedium, color = EcoColor.Green700)
    Spacer(Modifier.height(20.dp))
    EcoPasswordField(value = password, onValueChange = { password = it }, label = "Kata sandi baru", placeholder = "Minimal 8 karakter", errorMessage = if (submitted && password.length < 8) "Minimal 8 karakter" else null)
    Spacer(Modifier.height(16.dp))
    EcoPasswordField(value = confirmation, onValueChange = { confirmation = it }, label = "Konfirmasi kata sandi", placeholder = "Ulangi kata sandi baru", errorMessage = if (submitted && (confirmation.isEmpty() || confirmation != password)) "Kata sandi tidak cocok" else null)
    Spacer(Modifier.height(16.dp))
    Text("Gunakan minimal 8 karakter", style = MaterialTheme.typography.labelSmall, color = EcoColor.Green700)
    Spacer(Modifier.height(16.dp))
    Text("Pratinjau: kata sandi belum dapat disimpan.", style = MaterialTheme.typography.labelSmall, color = EcoColor.Green700)
    Spacer(Modifier.height(8.dp))
    ResetButton("Simpan Kata Sandi") { submitted = true }
    ResetFooter()
  }
}

@Composable
private fun ResetButton(text: String, onClick: () -> Unit) {
  EcoButton(text = text, onClick = onClick, modifier = Modifier.fillMaxWidth(), endIcon = { Icon(Lucide.ArrowRight, contentDescription = null, modifier = Modifier.size(18.dp)) })
}

@Composable
private fun ResetFooter() {
  Spacer(Modifier.height(24.dp))
  AuthDivider()
  Spacer(Modifier.height(20.dp))
  Text("Jaga keamanan akun EcoSnap Anda", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = EcoColor.Green700)
}
