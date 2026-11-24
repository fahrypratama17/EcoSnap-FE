package com.example.ecosnap.features.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.components.EcoTextField
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.core.model.WasteCategory

@Composable
fun AdminRoute(accountUid: String, onLogout: () -> Unit) {
  val viewModel: AdminViewModel = viewModel(
    key = "admin-$accountUid",
    factory = AdminViewModel.Factory()
  )
  val state by viewModel.state.collectAsStateWithLifecycle()
  AdminScreen(
    state = state,
    onSelectStatus = viewModel::selectStatus,
    onRefresh = viewModel::refresh,
    onOpen = viewModel::open,
    onDismiss = viewModel::dismiss,
    onApprove = viewModel::approve,
    onReject = viewModel::reject,
    onRetryCategories = viewModel::retryCategories,
    onMessageShown = viewModel::messageShown,
    onLogout = onLogout
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
  state: AdminUiState,
  onSelectStatus: (RegistrationStatus) -> Unit,
  onRefresh: () -> Unit,
  onOpen: (PartnerRegistration) -> Unit,
  onDismiss: () -> Unit,
  onApprove: (ApprovalInput) -> Unit,
  onReject: () -> Unit,
  onRetryCategories: () -> Unit,
  onMessageShown: () -> Unit,
  onLogout: () -> Unit
) {
  Column(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp)) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      Text(
        "Admin EcoSnap",
        style = MaterialTheme.typography.titleLarge,
        color = EcoColor.Green900,
        modifier = Modifier.weight(1f)
      )
      TextButton(onClick = onRefresh, enabled = !state.busy) { Text("Muat ulang", color = EcoColor.Green700) }
      TextButton(onClick = onLogout, enabled = !state.busy) { Text("Keluar", color = EcoColor.TextError) }
    }
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      RegistrationStatus.entries.forEach { status ->
        FilterChip(
          selected = state.status == status,
          onClick = { onSelectStatus(status) },
          enabled = !state.busy,
          label = { Text(status.label) }
        )
      }
    }
    state.message?.let { message ->
      Text(message, color = EcoColor.TextBrand, modifier = Modifier.padding(bottom = 8.dp))
      LaunchedEffect(message) { onMessageShown() }
    }

    Box(Modifier.weight(1f).fillMaxWidth()) {
      when {
        state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = EcoColor.Green700)
        state.error != null -> Column(
          Modifier.align(Alignment.Center),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(state.error, color = EcoColor.TextError)
          EcoButton(text = "Coba lagi", onClick = onRefresh, modifier = Modifier.fillMaxWidth())
        }
        state.items.isEmpty() -> Text(
          "Tidak ada pendaftaran ${state.status.label.lowercase()}.",
          color = EcoColor.Green700,
          modifier = Modifier.align(Alignment.Center)
        )
        else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          items(state.items, key = { it.mitraUid }) { item ->
            RegistrationCard(item, enabled = !state.busy, onClick = { onOpen(item) })
          }
        }
      }
    }
  }

  state.selected?.let { registration ->
    RegistrationDialog(
      registration = registration,
      state = state,
      onDismiss = onDismiss,
      onApprove = onApprove,
      onReject = onReject,
      onRetryCategories = onRetryCategories
    )
  }
}

@Composable
private fun RegistrationCard(item: PartnerRegistration, enabled: Boolean, onClick: () -> Unit) {
  Card(
    modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick),
    colors = CardDefaults.cardColors(containerColor = EcoColor.GreenSoft)
  ) {
    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Text(item.businessName, style = MaterialTheme.typography.titleMedium, color = EcoColor.Green900)
      Text(item.businessCategory, color = EcoColor.Green700)
      Text("Pemilik: ${item.fullName}", color = EcoColor.Green800)
      Text(item.email, color = EcoColor.Green800)
      Text(item.phone, color = EcoColor.Green800)
    }
  }
}

@Composable
private fun RegistrationDialog(
  registration: PartnerRegistration,
  state: AdminUiState,
  onDismiss: () -> Unit,
  onApprove: (ApprovalInput) -> Unit,
  onReject: () -> Unit,
  onRetryCategories: () -> Unit
) {
  val pending = registration.status == RegistrationStatus.PENDING.apiValue
  val key = registration.mitraUid
  var area by rememberSaveable(key) { mutableStateOf("") }
  var address by rememberSaveable(key) { mutableStateOf("") }
  var latitude by rememberSaveable(key) { mutableStateOf("") }
  var longitude by rememberSaveable(key) { mutableStateOf("") }
  var phone by rememberSaveable(key) { mutableStateOf(registration.phone) }
  var hours by rememberSaveable(key) { mutableStateOf("") }
  var description by rememberSaveable(key) { mutableStateOf("") }
  var selectedSlugs by rememberSaveable(key) { mutableStateOf(emptyList<String>()) }
  var localError by rememberSaveable(key) { mutableStateOf<String?>(null) }
  var confirmReject by rememberSaveable(key) { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(registration.businessName) },
    text = {
      Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("${registration.businessCategory} · ${registration.fullName}", color = EcoColor.Green700)
        Text("${registration.email} · ${registration.phone}", color = EcoColor.Green700)
        if (!pending) {
          Text("Status: ${statusLabel(registration.status)}", color = EcoColor.Green800)
        } else {
          EcoTextField(area, { area = it; localError = null }, "Area (kota/kecamatan)", Modifier.fillMaxWidth())
          EcoTextField(address, { address = it; localError = null }, "Alamat", Modifier.fillMaxWidth())
          EcoTextField(
            latitude, { latitude = it; localError = null }, "Latitude (-90 sampai 90)",
            Modifier.fillMaxWidth(), keyboardType = KeyboardType.Decimal
          )
          EcoTextField(
            longitude, { longitude = it; localError = null }, "Longitude (-180 sampai 180)",
            Modifier.fillMaxWidth(), keyboardType = KeyboardType.Decimal
          )
          EcoTextField(
            phone, { phone = it; localError = null }, "Telepon usaha",
            Modifier.fillMaxWidth(), keyboardType = KeyboardType.Phone
          )
          EcoTextField(hours, { hours = it; localError = null }, "Jam buka", Modifier.fillMaxWidth())
          EcoTextField(description, { description = it; localError = null }, "Deskripsi", Modifier.fillMaxWidth())
          Text("Sampah yang diterima", style = MaterialTheme.typography.titleSmall, color = EcoColor.Green900)
          when {
            state.categoriesLoading -> CircularProgressIndicator(color = EcoColor.Green700)
            state.categoriesError != null -> {
              Text(state.categoriesError, color = EcoColor.TextError)
              TextButton(onClick = onRetryCategories) { Text("Coba lagi") }
            }
            else -> state.categories.forEach { category ->
              CategoryRow(
                category = category,
                checked = category.slug in selectedSlugs,
                enabled = !state.busy,
                onToggle = {
                  selectedSlugs = if (category.slug in selectedSlugs) selectedSlugs - category.slug
                  else selectedSlugs + category.slug
                  localError = null
                }
              )
            }
          }
        }
        (localError ?: state.actionError)?.let { Text(it, color = EcoColor.TextError) }
        if (state.busy) CircularProgressIndicator(color = EcoColor.Green700)
        Spacer(Modifier.height(4.dp))
      }
    },
    confirmButton = {
      if (pending) {
        EcoButton(
          text = "Setujui",
          enabled = !state.busy && !state.categoriesLoading,
          modifier = Modifier.padding(start = 8.dp),
          onClick = {
            val result = validate(area, address, latitude, longitude, phone, hours, description, selectedSlugs)
            when (result) {
              is Validation.Error -> localError = result.message
              is Validation.Ok -> onApprove(result.input)
            }
          }
        )
      } else {
        TextButton(onClick = onDismiss) { Text("Tutup") }
      }
    },
    dismissButton = {
      Row {
        if (pending) {
          TextButton(onClick = { confirmReject = true }, enabled = !state.busy) {
            Text("Tolak", color = EcoColor.TextError)
          }
        }
        if (pending) TextButton(onClick = onDismiss, enabled = !state.busy) { Text("Batal") }
      }
    }
  )

  if (confirmReject) {
    AlertDialog(
      onDismissRequest = { if (!state.busy) confirmReject = false },
      title = { Text("Tolak pendaftaran?") },
      text = { Text("Pendaftaran ${registration.businessName} akan ditolak dan tidak dapat diproses lagi.") },
      confirmButton = {
        TextButton(
          onClick = { confirmReject = false; onReject() },
          enabled = !state.busy
        ) { Text("Tolak", color = EcoColor.TextError) }
      },
      dismissButton = {
        TextButton(onClick = { confirmReject = false }, enabled = !state.busy) { Text("Batal") }
      }
    )
  }
}

@Composable
private fun CategoryRow(category: WasteCategory, checked: Boolean, enabled: Boolean, onToggle: () -> Unit) {
  Row(
    Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onToggle),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Checkbox(checked = checked, onCheckedChange = { onToggle() }, enabled = enabled)
    Text(category.name, color = EcoColor.Green900)
  }
}

private sealed interface Validation {
  data class Ok(val input: ApprovalInput) : Validation
  data class Error(val message: String) : Validation
}

private fun validate(
  area: String,
  address: String,
  latitude: String,
  longitude: String,
  phone: String,
  hours: String,
  description: String,
  slugs: List<String>
): Validation {
  val areaText = area.trim()
  val addressText = address.trim()
  val phoneText = phone.trim()
  val hoursText = hours.trim()
  val lat = latitude.trim().replace(',', '.').toDoubleOrNull()
  val lon = longitude.trim().replace(',', '.').toDoubleOrNull()
  return when {
    areaText.isEmpty() || areaText.length > 150 -> Validation.Error("Area wajib diisi, maksimal 150 karakter.")
    addressText.isEmpty() -> Validation.Error("Alamat wajib diisi.")
    lat == null || lat < -90.0 || lat > 90.0 || lat.isNaN() -> Validation.Error("Latitude harus angka antara -90 dan 90.")
    lon == null || lon < -180.0 || lon > 180.0 || lon.isNaN() -> Validation.Error("Longitude harus angka antara -180 dan 180.")
    phoneText.length > 32 -> Validation.Error("Telepon maksimal 32 karakter.")
    hoursText.length > 200 -> Validation.Error("Jam buka maksimal 200 karakter.")
    slugs.isEmpty() -> Validation.Error("Pilih minimal satu jenis sampah.")
    else -> Validation.Ok(
      ApprovalInput(areaText, addressText, lat, lon, phoneText, hoursText, description.trim(), slugs)
    )
  }
}

private fun statusLabel(status: String): String =
  RegistrationStatus.entries.firstOrNull { it.apiValue == status }?.label ?: status
