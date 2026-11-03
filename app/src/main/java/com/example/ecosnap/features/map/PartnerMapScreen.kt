package com.example.ecosnap.features.map

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.core.designsystem.theme.EcoGradient
import com.example.ecosnap.features.map.components.MapLibrePartnerMap
import com.example.ecosnap.features.map.components.PartnerSummaryCard
import com.example.ecosnap.features.map.model.DummyPartners
import com.example.ecosnap.features.map.model.WasteMaterial

@Composable
fun PartnerMapScreen(
  onPartnerDetail: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val partners = DummyPartners.partners

  var query by rememberSaveable{
    mutableStateOf("")
  }

  var selectedMaterial by remember {
    mutableStateOf<WasteMaterial?>(null)
  }

  var selectedPartnerId by rememberSaveable {
    mutableStateOf(partners.firstOrNull()?.id)
  }

  var favoriteIds by remember {
    mutableStateOf(setOf<String>())
  }

  val visiblePartners = remember(query, selectedMaterial) {
    partners.filter { partner ->
      val matchesQuery = partner.name.contains(
        query.trim(),
        ignoreCase = true) || partner.area.contains(
          query.trim(), ignoreCase = true
        )

      val matchesMaterial = selectedMaterial == null || selectedMaterial in partner.acceptedMaterials

      matchesQuery && matchesMaterial
    }
  }

  LaunchedEffect(visiblePartners) {
    if (visiblePartners.none {
      it.id == selectedPartnerId
    }) {
      selectedPartnerId = visiblePartners.firstOrNull()?.id
    }
  }

  val selectedPartner = visiblePartners.firstOrNull {
    it.id == selectedPartnerId
  }

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
        value = query,
        onValueChange = { query = it },
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
        partners = visiblePartners,
        selectedPartnerId = selectedPartnerId,
        onPartnerSelected = {id ->
          selectedPartnerId = id
        },
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
            selected = selectedMaterial == null,
            onClick = { selectedMaterial = null },
            label = { Text("Semua") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = EcoColor.Green700,
              selectedLabelColor = Color.White,
              containerColor = EcoColor.Green100
            )
          )
        }

        items(WasteMaterial.entries) { material ->
          FilterChip(
            selected = selectedMaterial == material,
            onClick = { selectedMaterial = material },
            label = { Text(material.label)},
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = EcoColor.Green700,
              selectedLabelColor = Color.White,
              containerColor = EcoColor.Green100
            )
          )
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
          .clickable{
            context.startActivity(
              Intent(
                Intent.ACTION_VIEW,
                "https://www.openstreetmap.org/copyright".toUri()
              )
            )
          }
          .padding(6.dp),
        color = EcoColor.Green900,
        fontSize = 10.sp
      )

      if (selectedPartner != null) {
        PartnerSummaryCard(
          partner = selectedPartner,
          isFavorite = selectedPartner.id in favoriteIds,
          onFavoriteClick = {
            favoriteIds = if (selectedPartner.id in favoriteIds) {
              favoriteIds - selectedPartner.id
            } else {
              favoriteIds + selectedPartner.id
            }
          },
          onDetailClick = {
            onPartnerDetail(selectedPartner.id)
          },
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(12.dp)
        )
      } else {
        Surface(
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(12.dp),
          shape = RoundedCornerShape(18.dp),
          color = Color.White,
          shadowElevation = 3.dp
        ) {
          Text(
            text = "Tidak ada mitra yang cocok dengan pencarian atau filter.",
            modifier = Modifier.padding(20.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = EcoColor.Green900
          )
        }
      }
    }
  }
}










