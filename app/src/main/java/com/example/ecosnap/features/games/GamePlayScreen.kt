package com.example.ecosnap.features.games

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ecosnap.features.games.model.MiniGame

@Composable
fun GamePlayScreen(
  game: MiniGame,
  onBack: () -> Unit,
  onAllGames: () -> Unit,
  onNotifications: () -> Unit,
  onProfile: () -> Unit,
  modifier: Modifier = Modifier
) {
  var currentIndex by rememberSaveable(game.id) {
    mutableStateOf(0)
  }

  var selectedAnswer by rememberSaveable(game.id) {
    mutableStateOf<String?>(null)
  }

  var correctCount by rememberSaveable(game.id) {
    mutableStateOf(0)
  }

  var earnedXp by rememberSaveable(game.id) {
    mutableStateOf(0)
  }

  var isFinished by rememberSaveable(game.id) {
    mutableStateOf(false)
  }

  val questions = game.questions

  if (questions.isEmpty()) {
    Box(
      modifier = Modifier.fillMaxWidth(),
      contentAlignment = Alignment.Center,
    ) {
      Text("Belum ada pertanyaan untuk game ini.")
    }
    return
  }


}






























