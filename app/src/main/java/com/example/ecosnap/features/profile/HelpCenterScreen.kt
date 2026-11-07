package com.example.ecosnap.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ecosnap.features.profile.components.FaqAccordionCard
import com.example.ecosnap.features.profile.components.ProfileBackTextButton
import com.example.ecosnap.features.profile.components.ProfilePageHeader
import com.example.ecosnap.features.profile.components.ProfileTopBar
import com.example.ecosnap.features.profile.model.ProfileDummyData

@Composable
fun HelpCenterScreen(
  onBack: () -> Unit,
  onSettingsClick: () -> Unit,
  onProfileClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val faqItems = ProfileDummyData.faqItems
  var expandedIndex by rememberSaveable {
    mutableIntStateOf(0)
  }

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
      contentPadding = PaddingValues(
        horizontal = 16.dp,
        vertical = 14.dp
      ),
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
          title = "Pusat Bantuan",
          subtitle = "Pertanyaan yang sering diajukan."
        )
      }

      itemsIndexed(faqItems) { index, item ->
        FaqAccordionCard(
          item = item,
          expanded = expandedIndex == index,
          onToggle = { expandedIndex = if (expandedIndex == index) -1 else index }
        )
      }
    }
  }
}