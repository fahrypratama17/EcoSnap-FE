package com.example.ecosnap.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ecosnap.core.network.BackendException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Destinations a notification may open. Anything the app cannot honestly resolve stays text-only. */
sealed interface NotificationTarget {
  data class Deposit(val id: String) : NotificationTarget
  data class Voucher(val id: String) : NotificationTarget
  object Missions : NotificationTarget
}

/**
 * Role-aware: a partner only follows `deposit_claimed` to a deposit; a user follows `deposit_credited`,
 * `voucher_issued` and `mission_xp_claimed`. The event type and the target type must agree, and IDs
 * must be well formed; otherwise null (no invented route).
 */
data class NotificationAccess(
  val isPartner: Boolean,
  /** False when the caller supplied no voucher handler: voucher notifications stay text-only. */
  val canOpenVoucher: Boolean,
  val canOpenMissions: Boolean
)

fun resolveNotificationTarget(item: AppNotification, access: NotificationAccess): NotificationTarget? {
  val isPartner = access.isPartner
  val type = item.targetType ?: return null
  val id = item.targetId ?: return null
  val validId = NotificationsRepository.UUID_PATTERN.matches(id)
  return when {
    type == "deposit" && validId && item.type == "deposit_credited" && !isPartner -> NotificationTarget.Deposit(id)
    type == "deposit" && validId && item.type == "deposit_claimed" && isPartner -> NotificationTarget.Deposit(id)
    type == "voucher" && validId && item.type == "voucher_issued" && !isPartner && access.canOpenVoucher ->
      NotificationTarget.Voucher(id)
    type == "mission" && item.type == "mission_xp_claimed" && !isPartner && access.canOpenMissions ->
      NotificationTarget.Missions
    else -> null
  }
}

data class NotificationsUiState(
  val unreadOnly: Boolean = false,
  val items: List<AppNotification> = emptyList(),
  val unreadCount: Long = 0,
  val nextCursor: String? = null,
  val isLoading: Boolean = true,
  val isLoadingMore: Boolean = false,
  val error: String? = null,
  val loadMoreError: String? = null,
  /** Notification whose read PATCH is in flight; at most one at a time. */
  val openingId: String? = null,
  val actionError: String? = null
) {
  val hasMore: Boolean get() = nextCursor != null
}

class NotificationsViewModel(
  private val repository: NotificationsRepository,
  private val access: NotificationAccess
) : ViewModel() {
  private val _state = MutableStateFlow(NotificationsUiState())
  val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

  private val _targets = Channel<NotificationTarget>(Channel.BUFFERED)
  val targets: Flow<NotificationTarget> = _targets.receiveAsFlow()

  private var started = false
  private var generation = 0
  private var loadJob: Job? = null
  private var moreJob: Job? = null

  /** First call shows the loading state; later calls (ON_RESUME) refresh the first page in place. */
  fun onResume() {
    if (!started) {
      started = true
      reload(showLoading = true)
    } else {
      reload(showLoading = false)
    }
  }

  fun retry() = reload(showLoading = true)

  fun setUnreadOnly(value: Boolean) {
    if (value == _state.value.unreadOnly) return
    _state.update { it.copy(unreadOnly = value) }
    reload(showLoading = true)
  }

  fun dismissActionError() = _state.update { it.copy(actionError = null) }

  fun loadMore() {
    val current = _state.value
    val cursor = current.nextCursor ?: return
    if (current.isLoading || current.isLoadingMore) return
    val token = generation
    _state.update { it.copy(isLoadingMore = true, loadMoreError = null) }
    moreJob = viewModelScope.launch {
      try {
        val page = repository.list(cursor, current.unreadOnly)
        if (token != generation) return@launch
        _state.update {
          val known = it.items.mapTo(HashSet()) { item -> item.id }
          it.copy(
            items = it.items + page.items.filter { item -> item.id !in known },
            unreadCount = page.unreadCount,
            nextCursor = page.nextCursor,
            isLoadingMore = false
          )
        }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        if (token != generation) return@launch
        _state.update { it.copy(isLoadingMore = false, loadMoreError = messageOf(e, "Notifikasi lainnya belum dapat dimuat.")) }
      }
    }
  }

  /**
   * Marks an unread notification read on the server first; the target is emitted only after that
   * PATCH succeeded (already-read items need no PATCH). A failed PATCH never navigates.
   */
  fun open(item: AppNotification) {
    if (_state.value.openingId != null) return
    val target = resolveNotificationTarget(item, access)
    if (!item.isUnread) {
      target?.let { _targets.trySend(it) }
      return
    }
    _state.update { it.copy(openingId = item.id, actionError = null) }
    viewModelScope.launch {
      try {
        val read = repository.markRead(item.id)
        _state.update { s ->
          s.copy(
            openingId = null,
            items = s.items.map { if (it.id == read.id) read else it },
            unreadCount = if (s.items.any { it.id == read.id && it.isUnread }) {
              (s.unreadCount - 1).coerceAtLeast(0)
            } else {
              s.unreadCount
            }
          )
        }
        target?.let { _targets.trySend(it) }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _state.update {
          it.copy(openingId = null, actionError = messageOf(e, "Notifikasi belum dapat ditandai dibaca. Silakan coba lagi."))
        }
      }
    }
  }

  private fun reload(showLoading: Boolean) {
    val token = ++generation
    loadJob?.cancel()
    moreJob?.cancel()
    _state.update {
      if (showLoading) {
        it.copy(
          items = emptyList(), nextCursor = null, isLoading = true,
          isLoadingMore = false, error = null, loadMoreError = null
        )
      } else {
        it.copy(isLoadingMore = false, loadMoreError = null)
      }
    }
    loadJob = viewModelScope.launch {
      try {
        val page = repository.list(cursor = null, unreadOnly = _state.value.unreadOnly)
        if (token != generation) return@launch
        _state.update {
          it.copy(
            items = page.items,
            unreadCount = page.unreadCount,
            nextCursor = page.nextCursor,
            isLoading = false,
            error = null
          )
        }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        if (token != generation) return@launch
        _state.update {
          // A silent refresh failure keeps the already displayed page.
          if (showLoading || it.items.isEmpty()) {
            it.copy(
              items = emptyList(), nextCursor = null, isLoading = false,
              error = messageOf(e, "Notifikasi belum dapat dimuat. Silakan coba lagi.")
            )
          } else {
            it.copy(isLoading = false)
          }
        }
      }
    }
  }

  private fun messageOf(e: Exception, fallback: String): String =
    (e as? BackendException)?.message?.takeIf { it.isNotBlank() } ?: fallback
}

class NotificationsViewModelFactory(
  private val access: NotificationAccess,
  private val repository: NotificationsRepository = NotificationsRepository()
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    require(modelClass.isAssignableFrom(NotificationsViewModel::class.java))
    @Suppress("UNCHECKED_CAST")
    return NotificationsViewModel(repository, access) as T
  }
}
