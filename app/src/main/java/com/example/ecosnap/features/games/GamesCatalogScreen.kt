package com.example.ecosnap.features.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Gamepad2
import com.composables.icons.lucide.Lightbulb
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.example.ecosnap.core.designsystem.components.EcoAppHeader
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.core.designsystem.theme.EcosnapTheme
import com.example.ecosnap.features.games.components.GamesSectionHeader
import com.example.ecosnap.features.games.components.MiniGameCard
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
            ) {
              Icon(
                imageVector = Lucide.Gamepad2,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = Color.White
              )
            }
          }
        }
      }

      item {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          modifier = Modifier.fillMaxWidth(),
          placeholder = { Text("Cari mini game...") },
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
            unfocusedBorderColor = EcoColor.Green400
          )
        )
      }

      item {
        GamesSectionHeader(title = "Mini Games")
      }

      items(gameRows.size) { rowIndex ->
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          gameRows[rowIndex].forEach { game ->
            MiniGameCard(
              game = game,
              onClick = { onGameDetail(game.id) },
              showDetails = true,
              modifier = Modifier.weight(1f)
            )
          }

          if(gameRows[rowIndex].size == 1) {
            Spacer(Modifier.weight(1f))
          }
        }
      }

      if (filteredGames.isEmpty()) {
        item {
          Text(
            text = "Mini game tidak ditemukan.",
            color = EcoColor.Green700,
            modifier = Modifier.padding(16.dp)
          )
        }
      }

      item {
        EcoCard {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Lucide.Lightbulb,
              contentDescription = null,
              tint = EcoColor.Green700,
              modifier = Modifier.size(28.dp)
            )

            Spacer(Modifier.width(12.dp))

            Column {
              Text(
                text = "Belajar sambil bermain",
                style = MaterialTheme.typography.titleMedium,
                color = EcoColor.Green900
              )

              Spacer(Modifier.height(4.dp))

              Text(
                text = "XP dari mini games digunakan untuk pencapaian dan leaderboard Games, bukan saldo EcoPoints dari penyetoran.",
                style = MaterialTheme.typography.bodyMedium,
                color = EcoColor.Green700
              )
            }
          }
        }
      }
    }
  }
}

@Preview(
  showBackground = true,
  widthDp = 360,
  heightDp = 780
)
@Composable
private fun GamesCatalogPreview() {
  EcosnapTheme(
    darkTheme = false,
    dynamicColor = false
  ) {
    GamesCatalogScreen(
      onBack = {},
      onGameDetail = {},
      onNotifications = {},
      onProfile = {}
    )
  }
}












