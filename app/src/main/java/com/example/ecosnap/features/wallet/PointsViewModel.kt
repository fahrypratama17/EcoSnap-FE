package com.example.ecosnap.features.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ecosnap.core.network.BackendException
import com.example.ecosnap.core.network.PendingMutation
import com.example.ecosnap.core.network.PendingMutationStore
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.JsonObject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** The single durable redemption intent of this account: unknown outcome (`voucherId == null`) or completed. */
data class PendingRedemption(val rewardId: String, val voucherId: String?)

data class PointsUiState(
  val wallet: Wallet? = null,
  val walletError: String? = null,
  val rewards: List<Reward> = emptyList(),
  val rewardsLoaded: Boolean = false,
  val rewardsError: String? = null,
  val isLoading: Boolean = true,
  /** False until the durable intent was read; redemption stays blocked until then. */
  val pendingLoaded: Boolean = false,
  val pendingError: String? = null,
  val pending: PendingRedemption? = null,
  val redeemingRewardId: String? = null,
  val message: String? = null,
  val selectedRewardId: String? = null,
  val selectedReward: Reward? = null,
  val detailLoading: Boolean = false,
  val detailError: String? = null
) {
  val isRedeeming: Boolean get() = redeemingRewardId != null
}

class PointsViewModel(
  private val uid: String,
  private val repository: WalletRepository,
  private val store: PendingMutationStore,
  private val currentUid: () -> String?
) : ViewModel() {
  private val _state = MutableStateFlow(PointsUiState())
  val state: StateFlow<PointsUiState> = _state.asStateFlow()

  private var generation = 0
  private var loadJob: Job? = null
  private var detailJob: Job? = null

  fun onResume() = load()

  fun retry() = load()

  fun dismissMessage() = _state.update { it.copy(message = null) }

  fun openReward(id: String) {
    _state.update { it.copy(selectedRewardId = id, selectedReward = it.rewards.firstOrNull { r -> r.id == id }, detailError = null) }
    loadDetail(id)
  }

  fun closeReward() {
    detailJob?.cancel()
    _state.update { it.copy(selectedRewardId = null, selectedReward = null, detailLoading = false, detailError = null) }
  }

  fun retryDetail() {
    _state.value.selectedRewardId?.let(::loadDetail)
  }

  private fun loadDetail(id: String) {
    detailJob?.cancel()
    _state.update { it.copy(detailLoading = true, detailError = null) }
    detailJob = viewModelScope.launch {
      val result = attempt { repository.reward(id) }
      if (_state.value.selectedRewardId != id) return@launch
      result.onSuccess { reward ->
        _state.update { it.copy(selectedReward = reward, detailLoading = false) }
      }.onFailure { e ->
        _state.update {
          it.copy(detailLoading = false, detailError = e.userMessage("Detail reward belum dapat dimuat."))
        }
      }
    }
  }

  private fun load() {
    val token = ++generation
    loadJob?.cancel()
    _state.update { it.copy(isLoading = true) }
    loadJob = viewModelScope.launch {
      val (wallet, rewards, pending) = coroutineScope {
        val w = async { attempt { repository.wallet() } }
        val r = async { attempt { repository.rewards() } }
        val p = async { attempt { store.read(uid, SCOPE) } }
        Triple(w.await(), r.await(), p.await())
      }
      if (token != generation) return@launch
      _state.update { s ->
        var next = s.copy(isLoading = false)
        wallet.onSuccess { next = next.copy(wallet = it, walletError = null) }
          .onFailure { e -> next = next.copy(walletError = e.userMessage("Saldo belum dapat dimuat.")) }
        rewards.onSuccess { next = next.copy(rewards = it, rewardsLoaded = true, rewardsError = null) }
          .onFailure { e -> next = next.copy(rewardsError = e.userMessage("Daftar reward belum dapat dimuat.")) }
        // Do not overwrite an in-flight redemption's own state with a stale read.
        if (!s.isRedeeming) {
          pending.onSuccess { next = next.copy(pending = it?.toPending(), pendingLoaded = true, pendingError = null) }
            .onFailure { next = next.copy(pendingError = "Status penukaran belum dapat dibaca. Coba muat ulang.") }
        }
        next
      }
      // Keep an open detail's stock/availability current as well.
      _state.value.selectedRewardId?.let { loadDetail(it) }
    }
  }

  /** Starts a new redemption, or resumes the existing intent when it is for the same reward. */
  fun redeem(rewardId: String) {
    val s = _state.value
    if (s.isRedeeming || !s.pendingLoaded) return
    s.pending?.let { pending ->
      if (pending.voucherId != null || pending.rewardId != rewardId) {
        _state.update { it.copy(message = "Selesaikan penukaran sebelumnya terlebih dahulu.") }
        return
      }
    }
    start(rewardId)
  }

  /** Retries the unknown-outcome intent with exactly the persisted key. */
  fun retryPending() {
    val s = _state.value
    val pending = s.pending ?: return
    if (s.isRedeeming || pending.voucherId != null) return
    start(pending.rewardId)
  }

  /** Acknowledges a completed redemption; [onOpen] receives the voucher ID when the user wants to see it. */
  fun acknowledgeCompleted(onOpen: ((String) -> Unit)?) {
    val s = _state.value
    val pending = s.pending ?: return
    val voucherId = pending.voucherId ?: return
    if (s.isRedeeming) return
    _state.update { it.copy(redeemingRewardId = pending.rewardId) }
    viewModelScope.launch {
      val cleared = attempt {
        withContext(NonCancellable) {
          store.read(uid, SCOPE)?.let { store.clear(uid, SCOPE, it.key) }
        }
      }
      cleared.onSuccess {
        _state.update { it.copy(pending = null, redeemingRewardId = null) }
        onOpen?.invoke(voucherId)
      }.onFailure {
        _state.update { it.copy(redeemingRewardId = null, message = "Status penukaran belum dapat disimpan. Coba lagi.") }
      }
    }
  }

  private fun start(rewardId: String) {
    _state.update { it.copy(redeemingRewardId = rewardId, message = null) }
    viewModelScope.launch {
      if (currentUid() != uid) {
        _state.update { it.copy(redeemingRewardId = null, message = "Sesi berubah. Silakan masuk kembali.") }
        return@launch
      }
      val payload = JsonObject().apply { addProperty("reward_id", rewardId) }
      val mutation = try {
        store.read(uid, SCOPE)?.let { existing ->
          if (existing.resultId != null || existing.payload != payload) {
            _state.update { it.copy(pending = existing.toPending(), redeemingRewardId = null, message = "Selesaikan penukaran sebelumnya terlebih dahulu.") }
            return@launch
          }
        }
        store.begin(uid, SCOPE, payload)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _state.update { it.copy(redeemingRewardId = null, message = "Penukaran tidak dapat dimulai. Coba lagi.") }
        return@launch
      }
      // From here the intent is durable: whatever happens, the next entry recovers it.
      _state.update { it.copy(pending = PendingRedemption(rewardId, null)) }
      try {
        val result = repository.redeem(rewardId, mutation.key)
        withContext(NonCancellable) { store.resolve(uid, SCOPE, mutation.key, result.voucherId) }
        _state.update { s ->
          s.copy(
            pending = PendingRedemption(rewardId, result.voucherId),
            redeemingRewardId = null,
            wallet = s.wallet?.copy(balance = result.balance) ?: s.wallet,
            selectedRewardId = null,
            selectedReward = null,
            message = null
          )
        }
        load()
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        if (e.isDefinitiveRejection()) {
          val cleared = attempt { withContext(NonCancellable) { store.clear(uid, SCOPE, mutation.key) } }.isSuccess
          _state.update {
            it.copy(
              pending = if (cleared) null else PendingRedemption(rewardId, null),
              redeemingRewardId = null,
              message = rejectionMessage(e)
            )
          }
          load()
        } else {
          _state.update {
            it.copy(
              redeemingRewardId = null,
              message = e.userMessage("Hasil penukaran belum pasti.") +
                " Penukaran tersimpan; coba lagi untuk memastikan, poin tidak akan terpotong dua kali."
            )
          }
        }
      }
    }
  }

  private fun rejectionMessage(e: Throwable): String = when ((e as? BackendException)?.code) {
    "insufficient_points" -> "Poin tidak cukup untuk reward ini."
    "out_of_stock" -> "Stok reward habis."
    "reward_not_found" -> "Reward tidak tersedia lagi."
    "redeem_forbidden" -> "Akun ini tidak dapat menukar reward."
    else -> e.userMessage("Penukaran ditolak server.")
  }

  private fun PendingMutation.toPending(): PendingRedemption {
    val rewardId = payload.get("reward_id")?.takeIf { it.isJsonPrimitive }?.asString.orEmpty()
    return PendingRedemption(rewardId, resultId)
  }

  companion object {
    const val SCOPE = "reward_redeem"
  }
}

class PointsViewModelFactory(
  private val uid: String,
  private val store: PendingMutationStore,
  private val repository: WalletRepository = WalletRepository()
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    require(modelClass.isAssignableFrom(PointsViewModel::class.java))
    @Suppress("UNCHECKED_CAST")
    return PointsViewModel(uid, repository, store) { FirebaseAuth.getInstance().currentUser?.uid } as T
  }
}
