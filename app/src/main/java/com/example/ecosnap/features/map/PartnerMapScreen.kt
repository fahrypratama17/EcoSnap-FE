package com.example.ecosnap.features.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.core.designsystem.theme.EcoGradient
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


    }
  }
}










