package com.example.ecosnap.features.gamification

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Medal
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.home.formatServerTime
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BadgesUiState(
  val board: BadgeBoard? = null,
  val isLoading: Boolean = true,
  val error: String? = null
)

class BadgesViewModel(private val repository: GamificationRepository) : ViewModel() {
  private val _state = MutableStateFlow(BadgesUiState())
  val state: StateFlow<BadgesUiState> = _state.asStateFlow()

  private var generation = 0
  private var job: Job? = null

  fun refresh() = load()

  fun retry() {
    _state.update { it.copy(isLoading = true, error = null) }
    load()
  }

  private fun load() {
    val token = ++generation
    job?.cancel()
    job = viewModelScope.launch {
      try {
        val board = repository.badges()
        if (token != generation) return@launch
        _state.update { BadgesUiState(board = board, isLoading = false) }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        if (token != generation) return@launch
        val message = e.gamificationMessage("Badge belum dapat dimuat. Silakan coba lagi.")
        // A failed background refresh keeps the last server view on screen.
        _state.update { it.copy(isLoading = false, error = message) }
      }
    }
  }
}

class BadgesViewModelFactory(
  private val repository: GamificationRepository = GamificationRepository()
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    require(modelClass.isAssignableFrom(BadgesViewModel::class.java))
    @Suppress("UNCHECKED_CAST")
    return BadgesViewModel(repository) as T
  }
}

/** Permanent badges derived by the server from claimed deposits; there is nothing to claim here. */
@Composable
fun BadgesRoute(accountUid: String, onBack: () -> Unit) {
  val viewModel: BadgesViewModel = viewModel(key = "badges-$accountUid", factory = BadgesViewModelFactory())
  val state by viewModel.state.collectAsStateWithLifecycle()

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }

  BadgesScreen(state = state, onBack = onBack, onRetry = viewModel::retry)
}

@Composable
fun BadgesScreen(
  state: BadgesUiState,
  onBack: () -> Unit,
  onRetry: () -> Unit,
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
        Icon(Lucide.ArrowLeft, contentDescription = "Kembali", tint = EcoColor.Green900)
      }
      Spacer(Modifier.width(4.dp))
      Text(
        text = "Pencapaian",
        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
        color = EcoColor.Green900
      )
    }

    val board = state.board
    when {
      board == null && state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
        CircularProgressIndicator(color = EcoColor.Green700)
      }

      board == null -> Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = state.error ?: "Badge belum dapat dimuat.",
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
              Text(state.error, style = MaterialTheme.typography.bodyMedium, color = EcoColor.TextError)
              Spacer(Modifier.height(6.dp))
              EcoButton(text = "Muat ulang", onClick = onRetry, height = 40.dp)
            }
          }
        }

        item {
          Column {
            Text(
              text = "${board.earnedCount} dari ${board.badges.size} badge diraih",
              style = MaterialTheme.typography.titleMedium,
              color = EcoColor.Green900
            )
            Text(
              text = "Setoran terklaim: ${board.claimedDeposits} • Berat terklaim: ${formatWeight(board.claimedWeightGrams)}",
              fontSize = 12.sp,
              color = EcoColor.Green700
            )
            Text(
              text = "Badge bersifat permanen dan dihitung server dari setoran yang sudah diklaim. Tidak ada XP atau poin tambahan dari badge.",
              fontSize = 12.sp,
              color = EcoColor.Green700
            )
          }
        }

        if (board.badges.isEmpty()) {
          item {
            Text(
              text = "Belum ada badge yang tersedia.",
              style = MaterialTheme.typography.bodyMedium,
              color = EcoColor.Green700
            )
          }
        }

        items(board.badges, key = { it.id }) { badge -> BadgeCard(badge) }
      }
    }
  }
}

@Composable
private fun BadgeCard(badge: Badge) {
  val fraction = if (badge.target > 0) {
    (badge.progress.toFloat() / badge.target).coerceIn(0f, 1f)
  } else if (badge.earned) 1f else 0f

  EcoCard(
    modifier = Modifier.fillMaxWidth(),
    containerColor = if (badge.earned) EcoColor.Green100 else Color.White
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = Lucide.Medal,
        contentDescription = null,
        tint = if (badge.earned) EcoColor.Green700 else EcoColor.Green300,
        modifier = Modifier.size(32.dp)
      )
      Spacer(Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(badge.title, style = MaterialTheme.typography.titleMedium, color = EcoColor.Green900)
        if (badge.description.isNotEmpty()) {
          Text(badge.description, fontSize = 12.sp, color = EcoColor.Green700)
        }
      }
    }
    Spacer(Modifier.height(10.dp))
    LinearProgressIndicator(
      progress = { fraction },
      modifier = Modifier
        .fillMaxWidth()
        .height(6.dp),
      color = EcoColor.Green600,
      trackColor = EcoColor.Green100
    )
    Spacer(Modifier.height(6.dp))
    val status = if (badge.earned) {
      val date = badge.unlockedAt?.let { formatServerTime(it, "d MMMM yyyy") }
      if (date != null) "Diraih $date" else "Sudah diraih"
    } else {
      val amount = if (badge.id.startsWith("weight")) {
        "${formatWeight(badge.progress)}/${formatWeight(badge.target)}"
      } else {
        "${badge.progress}/${badge.target}"
      }
      "Belum diraih • $amount"
    }
    Text(status, fontSize = 12.sp, color = EcoColor.Green800)
  }
}
