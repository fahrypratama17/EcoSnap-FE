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
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.components.EcoAppHeader
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.games.components.GameAnswerOption
import com.example.ecosnap.features.games.components.GameFeedbackCard
import com.example.ecosnap.features.games.components.GameQuestionVisual
import com.example.ecosnap.features.games.model.AnswerOptionState
import com.example.ecosnap.features.games.model.GameResult
import com.example.ecosnap.features.games.model.GameType
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

  if (isFinished) {
    GameResultScreen(
      game = game,
      result = GameResult(
        correctAnswers = correctCount,
        totalQuestions = questions.size,
        earnedXp = earnedXp
      ),
      onReplay = {
        currentIndex = 0
        selectedAnswer = null
        correctCount = 0
        earnedXp = 0
        isFinished = false
      },
      onAllGames = onAllGames,
      onNotifications = onNotifications,
      onProfile = onProfile,
      modifier = modifier
    )
    return
  }

  val question = questions[currentIndex]

  val progress = ((currentIndex + 1).toFloat() / questions.size).coerceIn(0f, 1f)

  val hasAnswered = selectedAnswer != null
  val isCorrect = selectedAnswer == question.correctAnswer

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
      contentPadding = PaddingValues(14.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
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

          Spacer(Modifier.width(5.dp))

          Text("Kembali")
        }
      }

      item {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = game.title,
            style = MaterialTheme.typography.titleLarge,
            color = EcoColor.Green900
          )

          Spacer(Modifier.height(4.dp))

          Text(
            text = "Soal ${currentIndex + 1} dari ${questions.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = EcoColor.Green700
          )
        }
      }

      item {
        LinearProgressIndicator(
          progress = { progress },
          modifier = Modifier
            .fillMaxWidth()
            .height(7.dp),
          color = EcoColor.Green400,
          trackColor = EcoColor.Green100,
          strokeCap = StrokeCap.Round
        )
      }

      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          color = Color(0xFF75C6A5)
        ) {
          Column(
            modifier = Modifier.padding(
              horizontal = 16.dp,
              vertical = 22.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = if (game.type == GameType.SORTING) {
                "TEBAL JENIS SAMPAH"
              } else {
                "ECO QUIZ"
              },
              style = MaterialTheme.typography.labelLarge,
              color = EcoColor.Green900
            )

            Spacer(Modifier.height(18.dp))

            GameQuestionVisual(
              visual = question.visual,
              modifier = Modifier.size(142.dp)
            )

            Spacer(Modifier.height(17.dp))

            Text(
              text = question.title,
              style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 16.sp,
                lineHeight = 22.sp
              ),
              color = EcoColor.Green900,
              textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Text(
              text = question.prompt,
              style = MaterialTheme.typography.bodyMedium,
              color = EcoColor.Green800,
              textAlign = TextAlign.Center
            )
          }
        }
      }

      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          question.options.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              row.forEach { option ->
                val state = when {
                  selectedAnswer == null -> AnswerOptionState.DEFAULT

                  option == question.correctAnswer -> AnswerOptionState.CORRECT

                  option == selectedAnswer -> AnswerOptionState.WRONG

                  else -> AnswerOptionState.DEFAULT
                }

                GameAnswerOption(
                  text = option,
                  state = state,
                  enabled = !hasAnswered,
                  onClick = {
                    if (selectedAnswer == null) {
                      selectedAnswer = option

                      if (
                        option == question.correctAnswer
                      ) {
                        correctCount += 1
                        earnedXp += game.xpPerCorrect
                      }
                    }
                  },
                  modifier = Modifier.weight(1f)
                )
              }

              if (row.size == 1) {
                Spacer(Modifier.weight(1f))
              }
            }
          }
        }
      }

      item {
        if (hasAnswered) {
          GameFeedbackCard(
            isCorrect = isCorrect,
            explanation = question.explanation,
            earnedXp = if (isCorrect) {
              game.xpPerCorrect
            } else {
              0
            }
          )
        } else {
          EcoCard (
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 0.dp
          ) {
            Text(
              text = "Pilih satu jawaban untuk melihat penjelasan.",
              style = MaterialTheme.typography.bodyMedium,
              color = EcoColor.Green700,
              modifier = Modifier.fillMaxWidth(),
              textAlign = TextAlign.Center
            )
          }
        }
      }

      if (hasAnswered) {
        item {
          EcoButton(
            text = if (
              currentIndex == questions.lastIndex
            ) {
              "Lihat Hasil"
            } else {
              "Soal Berikutnya"
            },
            onClick = {
              if (
                currentIndex == questions.lastIndex
              ) {
                isFinished = true
              } else {
                currentIndex += 1
                selectedAnswer = null
              }
            },
            containerColor = EcoColor.Green700,
            contentColor = Color.White,
            endIcon = {
              Icon(
                imageVector = Lucide.ArrowRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
            }
          )
        }
      }
    }
  }
}