package com.example.ecosnap.features.map

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecosnap.core.designsystem.components.EcoAppHeader
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.core.model.Partner
import com.example.ecosnap.core.network.BackendClient
import com.example.ecosnap.core.network.BackendException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PartnerDetailUiState {
  data object Loading : PartnerDetailUiState
  data class Loaded(val partner: Partner) : PartnerDetailUiState
  data object NotFound : PartnerDetailUiState
  data class Error(val message: String) : PartnerDetailUiState
}

class PartnerDetailViewModel(
  val accountUid: String,
  val partnerId: String,
  private val repository: PartnerRepository,
) : ViewModel() {
  private val _state = MutableStateFlow<PartnerDetailUiState>(PartnerDetailUiState.Loading)
  val state: StateFlow<PartnerDetailUiState> = _state.asStateFlow()

  private var job: Job? = null
  private var resumedOnce = false

  init {
    load(showLoading = true)
  }

  fun retry() = load(showLoading = true)

  /** Refresh on return to the screen; keeps the shown partner visible unless the server says it is gone. */
  fun onResume() {
    if (!resumedOnce) {
      resumedOnce = true
      return
    }
    load(showLoading = _state.value !is PartnerDetailUiState.Loaded)
  }

  private fun load(showLoading: Boolean) {
    if (!PartnerRepository.isUuid(partnerId)) {
      _state.value = PartnerDetailUiState.NotFound
      return
    }
    job?.cancel()
    if (showLoading) _state.value = PartnerDetailUiState.Loading
    job = viewModelScope.launch {
      try {
        _state.value = PartnerDetailUiState.Loaded(repository.detail(partnerId))
      } catch (e: CancellationException) {
        throw e
      } catch (e: BackendException) {
        _state.value = when {
          e.status == 404 -> PartnerDetailUiState.NotFound
          _state.value is PartnerDetailUiState.Loaded -> _state.value
          else -> PartnerDetailUiState.Error(e.message?.takeIf { it.isNotBlank() } ?: NETWORK_ERROR)
        }
      } catch (e: Exception) {
        if (_state.value !is PartnerDetailUiState.Loaded) _state.value = PartnerDetailUiState.Error(NETWORK_ERROR)
      }
    }
  }

  class Factory(
    private val accountUid: String,
    private val partnerId: String,
    private val repository: PartnerRepository = PartnerRepository(BackendClient.default),
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
      PartnerDetailViewModel(accountUid, partnerId, repository) as T
  }

  private companion object {
    const val NETWORK_ERROR = "Tidak dapat terhubung ke server. Periksa koneksi dan coba lagi."
  }
}

@Composable
fun PartnerDetailRoute(
  accountUid: String,
  partnerId: String,
  onBack: () -> Unit,
  onNotifications: () -> Unit,
  onProfile: () -> Unit
) {
  val viewModel: PartnerDetailViewModel = viewModel(
    key = "partner-detail-$accountUid-$partnerId",
    factory = PartnerDetailViewModel.Factory(accountUid, partnerId)
  )
  val state by viewModel.state.collectAsStateWithLifecycle()
  val lifecycleOwner = LocalLifecycleOwner.current
  val context = LocalContext.current

  DisposableEffect(lifecycleOwner, viewModel) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) viewModel.onResume()
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  when (val current = state) {
    is PartnerDetailUiState.Loaded -> PartnerDetailScreen(
      partner = current.partner,
      onBack = onBack,
      onContact = { dial(context, current.partner) },
      onDirections = { openDirections(context, current.partner) },
      onNotifications = onNotifications,
      onProfile = onProfile
    )

    else -> Column(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.White)
    ) {
      EcoAppHeader(
        onNotifications = onNotifications,
        onProfile = onProfile
      )

      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        when (current) {
          PartnerDetailUiState.Loading -> {
            CircularProgressIndicator(
              modifier = Modifier.size(32.dp),
              color = EcoColor.Green700
            )
            Spacer(Modifier.height(12.dp))
            Text(
              text = "Memuat detail mitra...",
              style = MaterialTheme.typography.bodyMedium,
              color = EcoColor.Green700
            )
          }

          PartnerDetailUiState.NotFound -> {
            Text(
              text = "Mitra tidak ditemukan",
              style = MaterialTheme.typography.titleMedium,
              color = EcoColor.Green900
            )
            Spacer(Modifier.height(6.dp))
            Text(
              text = "Mitra ini tidak tersedia atau sudah tidak aktif.",
              style = MaterialTheme.typography.bodyMedium,
              color = EcoColor.Green700,
              textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            EcoButton(
              text = "Kembali",
              onClick = onBack,
              containerColor = EcoColor.Green700,
              contentColor = Color.White
            )
          }

          is PartnerDetailUiState.Error -> {
            Text(
              text = current.message,
              style = MaterialTheme.typography.bodyMedium,
              color = EcoColor.TextError,
              textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = viewModel::retry) { Text("Coba lagi") }
            TextButton(onClick = onBack) { Text("Kembali") }
          }

          is PartnerDetailUiState.Loaded -> Unit
        }
      }
    }
  }
}

private fun toast(context: Context, message: String) =
  Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

private fun dial(context: Context, partner: Partner) {
  val phone = partner.phone.filter { it.isDigit() || it == '+' }
  if (phone.isEmpty()) {
    toast(context, "Nomor telepon mitra belum tersedia.")
    return
  }
  try {
    context.startActivity(Intent(Intent.ACTION_DIAL, "tel:$phone".toUri()))
  } catch (_: ActivityNotFoundException) {
    toast(context, "Tidak ada aplikasi telepon untuk menghubungi mitra.")
  }
}

private fun openDirections(context: Context, partner: Partner) {
  val lat = partner.location.latitude
  val lon = partner.location.longitude
  val label = Uri.encode(partner.name)
  val geoIntent = Intent(Intent.ACTION_VIEW, "geo:$lat,$lon?q=$lat,$lon($label)".toUri())
  try {
    context.startActivity(geoIntent)
    return
  } catch (_: ActivityNotFoundException) {
  }

  val osmUrl = "https://www.openstreetmap.org/?mlat=$lat&mlon=$lon#map=17/$lat/$lon"
  try {
    context.startActivity(Intent(Intent.ACTION_VIEW, osmUrl.toUri()))
  } catch (_: ActivityNotFoundException) {
    toast(context, "Tidak ada aplikasi peta atau peramban untuk membuka petunjuk arah.")
  }
}
