package com.example.ecosnap.features.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.components.EcoAppHeader
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.games.data.LocalGames

@Composable
fun GamesCatalogScreen(
  onBack: () -> Unit,
  onGameDetail: (String) -> Unit,
  onNotifications: () -> Unit,
  onProfile: () -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by rememberSaveable {
    mutableStateOf("")
  }

  val filteredGames = remember(searchQuery) {
    LocalGames.games.filter { game ->
      game.title.contains(
        searchQuery.trim(),
        ignoreCase = true
      ) || game.description.contains(
        searchQuery.trim(),
        ignoreCase = true
      )
    }
  }

  val gameRows = filteredGames.chunked(2)

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White)
  ) {
    EcoAppHeader(
      onNotifications = onNotifications,
      onProfile = onProfile
    )

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        TextButton(
          onClick = onBack,
          contentPadding = PaddingValues(0.dp)
        ) {
          Icon(
            imageVector = Lucide.ArrowLeft,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(Modifier.width(6.dp))
          Text("Kembali")
        }
      }

      item {
        Column {
          Text(
            text = "Jelajahi Mini Games",
            style = MaterialTheme.typography.titleLarge,
            color = EcoColor.Green900
          )

          Spacer(Modifier.height(4.dp))

          Text(
            text = "Belajar tentang lingkungan dengan cara yang menyenangkan.",
            style = MaterialTheme.typography.bodyMedium,
            color = EcoColor.Green700
          )
        }
      }

      item {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = EcoColor.Green800
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "ECOSNAP GAME CENTER",
                fontSize = 11.sp,
                color = EcoColor.Green100
              )

              Spacer(Modifier.height(6.dp))

              Text(
                text = "Pilih tantangan, kumpulkan XP, dan tingkatkan pengetahuanmu!",
                style = MaterialTheme.typography.bodyMedium,
                color = EcoColor.Green100
              )
            }

            Box(
              modifier = Modifier
                .size(62.dp)
                .background(
                  EcoColor.Green600,
                  RoundedCornerShape(12.dp)
                ),
              contentAlignment = Alignment.Center
            ) {}
          }
        }
      }
    }
  }
}














