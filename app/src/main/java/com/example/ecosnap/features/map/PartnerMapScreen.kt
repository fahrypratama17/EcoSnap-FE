package com.example.ecosnap.features.map

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.core.designsystem.theme.EcoGradient
import com.example.ecosnap.features.map.components.MapLibrePartnerMap
import com.example.ecosnap.features.map.components.PartnerSummaryCard

@Composable
fun PartnerMapRoute(
  accountUid: String,
  onPartnerDetail: (String) -> Unit
) {
  val viewModel: PartnerMapViewModel = viewModel(
    key = "partner-map-$accountUid",
    factory = PartnerMapViewModel.Factory(accountUid)
  )
  val state by viewModel.state.collectAsStateWithLifecycle()
  val lifecycleOwner = LocalLifecycleOwner.current

  DisposableEffect(lifecycleOwner, viewModel) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) viewModel.onResume()
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  val context = LocalContext.current
  LaunchedEffect(state.message) {
    state.message?.let {
      Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
      viewModel.messageShown()
    }
  }

  PartnerMapScreen(
    state = state,
    onQueryChange = viewModel::onQueryChange,
    onCategorySelected = viewModel::onCategorySelected,
    onPartnerSelected = viewModel::onPartnerSelected,
    onFavoriteClick = viewModel::toggleFavorite,
    onLoadMore = viewModel::loadMore,
    onRetry = viewModel::retry,
    onRetryCategories = viewModel::retryCategories,
    onRetryFavorites = viewModel::retryFavorites,
    onPartnerDetail = onPartnerDetail
  )
}

@Composable
fun PartnerMapScreen(
  state: PartnerMapUiState,
  onQueryChange: (String) -> Unit,
  onCategorySelected: (String?) -> Unit,
  onPartnerSelected: (String) -> Unit,
  onFavoriteClick: (String) -> Unit,
  onLoadMore: () -> Unit,
  onRetry: () -> Unit,
  onRetryCategories: () -> Unit,
  onRetryFavorites: () -> Unit,
  onPartnerDetail: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val selectedPartner = state.partners.firstOrNull { it.id == state.selectedPartnerId }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(EcoGradient.vertical)
        .padding(
          start = 16.dp,
          end = 16.dp,
          top = 16.dp,
          bottom = 12.dp,
        )
    ) {
      Text(
        text = "Temukan mitra",
        style = MaterialTheme.typography.titleLarge,
        color = EcoColor.Green900
      )

      Spacer(Modifier.height(3.dp))

      Text(
        text = "Tempat baik untuk sampah terpilahmu.",
        style = MaterialTheme.typography.bodyMedium,
        color = EcoColor.Green700
      )

      Spacer(Modifier.height(12.dp))

      OutlinedTextField(
        value = state.query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
          Text(
            text = "Cari area atau nama mitra...",
            style = MaterialTheme.typography.bodyMedium
          )
        },
        leadingIcon = {
          Icon(
            imageVector = Lucide.Search,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
          )
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(
          imeAction = ImeAction.Search
        ),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color.White,
          unfocusedContainerColor = Color.White,
          focusedBorderColor = EcoColor.Green700,
          unfocusedBorderColor = EcoColor.Green300
        )
      )
    }

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
    ) {
      MapLibrePartnerMap(
        partners = state.partners,
        selectedPartnerId = state.selectedPartnerId,
        onPartnerSelected = onPartnerSelected,
        modifier = Modifier.fillMaxSize()
      )

      LazyRow(
        modifier = Modifier
          .align(Alignment.TopStart)
          .fillMaxWidth()
          .padding(top = 10.dp),
        contentPadding = PaddingValues(
          horizontal = 12.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        item {
          FilterChip(
            selected = state.selectedSlug == null,
            onClick = { onCategorySelected(null) },
            label = { Text("Semua") },
            colors = filterColors()
          )
        }

        items(state.categories, key = { it.id }) { category ->
          FilterChip(
            selected = state.selectedSlug == category.slug,
            onClick = { onCategorySelected(category.slug) },
            label = { Text(category.name) },
            colors = filterColors()
          )
        }

        if (state.categoriesError != null) {
          item {
            FilterChip(
              selected = false,
              onClick = onRetryCategories,
              label = { Text("Muat ulang filter") },
              colors = filterColors()
            )
          }
        }
      }

      Text(
        text = "@ OpenStreetMap contributors",
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(top = 70.dp, end = 10.dp)
          .background(
            Color.White.copy(alpha = 0.92f),
            RoundedCornerShape(6.dp)
          )
          .clickable {
            try {
              context.startActivity(
                Intent(
                  Intent.ACTION_VIEW,
                  "https://www.openstreetmap.org/copyright".toUri()
                )
              )
            } catch (_: ActivityNotFoundException) {
              Toast.makeText(
                context,
                "Tidak ada aplikasi peramban untuk membuka tautan.",
                Toast.LENGTH_SHORT
              ).show()
            }
          }
          .padding(6.dp),
        color = EcoColor.Green900,
        fontSize = 10.sp
      )

      Column(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        if (state.favoritesError != null) {
          StatusSurface {
            Text(
              text = if (state.favoritesLoaded) {
                "Favorit belum dapat diperbarui."
              } else {
                "Favorit belum dapat dimuat, tombol favorit dinonaktifkan."
              },
              modifier = Modifier.weight(1f),
              style = MaterialTheme.typography.bodyMedium,
              color = EcoColor.Green900
            )
            TextButton(onClick = onRetryFavorites) { Text("Coba lagi") }
          }
        }

        if (state.loadMoreError != null) {
          StatusSurface {
            Text(
              text = state.loadMoreError,
              modifier = Modifier.weight(1f),
              style = MaterialTheme.typography.bodyMedium,
              color = EcoColor.TextError
            )
            TextButton(onClick = onLoadMore) { Text("Coba lagi") }
          }
        } else if (state.hasMore && state.partners.isNotEmpty()) {
          StatusSurface {
            if (state.isLoadingMore) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = EcoColor.Green700
              )
              Spacer(Modifier.size(10.dp))
              Text(
                text = "Memuat mitra lainnya...",
                style = MaterialTheme.typography.bodyMedium,
                color = EcoColor.Green900
              )
            } else {
              Text(
                text = "Menampilkan ${state.partners.size} mitra.",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = EcoColor.Green900
              )
              TextButton(onClick = onLoadMore) { Text("Muat lebih banyak") }
            }
          }
        }

        when {
          state.isLoading && state.partners.isEmpty() -> StatusSurface {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              strokeWidth = 2.dp,
              color = EcoColor.Green700
            )
            Spacer(Modifier.size(10.dp))
            Text(
              text = "Memuat mitra...",
              style = MaterialTheme.typography.bodyMedium,
              color = EcoColor.Green900
            )
          }

          state.error != null && state.partners.isEmpty() -> StatusSurface {
            Text(
              text = state.error,
              modifier = Modifier.weight(1f),
              style = MaterialTheme.typography.bodyMedium,
              color = EcoColor.TextError
            )
            TextButton(onClick = onRetry) { Text("Coba lagi") }
          }

          selectedPartner != null -> PartnerSummaryCard(
            partner = selectedPartner,
            isFavorite = selectedPartner.id in state.favoriteIds,
            favoriteEnabled = state.favoritesLoaded &&
              selectedPartner.id !in state.favoritesInFlight,
            onFavoriteClick = { onFavoriteClick(selectedPartner.id) },
            onDetailClick = { onPartnerDetail(selectedPartner.id) }
          )

          !state.isLoading -> StatusSurface {
            Text(
              text = "Tidak ada mitra yang cocok dengan pencarian atau filter.",
              modifier = Modifier.padding(vertical = 8.dp),
              style = MaterialTheme.typography.bodyMedium,
              color = EcoColor.Green900
            )
          }
        }
      }
    }
  }
}

@Composable
private fun filterColors() = FilterChipDefaults.filterChipColors(
  selectedContainerColor = EcoColor.Green700,
  selectedLabelColor = Color.White,
  containerColor = EcoColor.Green100
)

@Composable
private fun StatusSurface(content: @Composable RowScope.() -> Unit) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    color = Color.White,
    shadowElevation = 3.dp
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      content()
    }
  }
}
