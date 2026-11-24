package com.example.ecosnap.features.home

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.home.components.HomeWeeklyMissionCard

/** Weekly missions from `GET /me/missions`; XP is only ever what the server returns. */
@Composable
fun MissionsRoute(accountUid: String, onBack: () -> Unit) {
  val viewModel: MissionsViewModel = viewModel(key = "missions-$accountUid", factory = MissionsViewModelFactory())
  val state by viewModel.state.collectAsStateWithLifecycle()

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }

  MissionsScreen(
    state = state,
    onBack = onBack,
    onRetry = viewModel::retry,
    onClaim = viewModel::claim
  )
}

@Composable
fun MissionsScreen(
  state: MissionsUiState,
  onBack: () -> Unit,
  onRetry: () -> Unit,
  onClaim: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onBack) {
        Icon(
          imageVector = Lucide.ArrowLeft,
          contentDescription = "Kembali",
          tint = EcoColor.Green900
        )
      }
      Spacer(Modifier.width(4.dp))
      Text(
        text = "Misi Mingguan",
        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
        color = EcoColor.Green900
      )
    }

    val board = state.board
    when {
      board == null && state.isLoading -> CenteredBox { CircularProgressIndicator(color = EcoColor.Green700) }

      board == null -> CenteredBox {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = state.error ?: "Misi belum dapat dimuat.",
            style = MaterialTheme.typography.bodyMedium,
            color = EcoColor.Green900,
            textAlign = TextAlign.Center
          )
          Spacer(Modifier.height(12.dp))
          EcoButton(text = "Coba lagi", onClick = onRetry, modifier = Modifier.fillMaxWidth())
        }
      }

      else -> LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        if (state.error != null) {
          item {
            Column {
              Text(text = state.error, style = MaterialTheme.typography.bodyMedium, color = EcoColor.TextError)
              Spacer(Modifier.height(6.dp))
              EcoButton(text = "Muat ulang", onClick = onRetry, height = 40.dp)
            }
          }
        }

        if (state.claimMessage != null) {
          item {
            Text(
              text = state.claimMessage,
              style = MaterialTheme.typography.bodyMedium,
              color = EcoColor.Green800
            )
          }
        }

        item {
          val end = formatServerTime(board.periodEnd, "EEEE, d MMMM yyyy HH.mm")
          Column {
            Text(
              text = "XP minggu ini: ${board.weeklyXp} • Total XP: ${board.totalXp}",
              fontSize = 12.sp,
              color = EcoColor.Green700
            )
            if (end != null) {
              Text(
                text = "Periode berakhir $end WIB. Hadiah yang belum diklaim hangus.",
                fontSize = 12.sp,
                color = EcoColor.Green700
              )
            }
          }
        }

        if (board.missions.isEmpty()) {
          item {
            Text(
              text = "Belum ada misi minggu ini",
              style = MaterialTheme.typography.bodyMedium,
              color = EcoColor.Green700
            )
          }
        }

        items(board.missions, key = { it.id }) { mission ->
          HomeWeeklyMissionCard(
            mission = mission,
            footer = {
              if (mission.isClaimable) {
                Spacer(Modifier.height(6.dp))
                EcoButton(
                  text = if (state.claimingId == mission.id) "Mengklaim..." else "Klaim ${mission.rewardXp} XP",
                  onClick = { onClaim(mission.id) },
                  enabled = state.claimingId == null,
                  height = 40.dp,
                  modifier = Modifier.fillMaxWidth()
                )
              }
            }
          )
        }
      }
    }
  }
}

@Composable
private fun CenteredBox(content: @Composable () -> Unit) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) { content() }
}
