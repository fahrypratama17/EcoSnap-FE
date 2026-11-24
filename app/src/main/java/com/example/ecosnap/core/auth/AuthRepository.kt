package com.example.ecosnap.core.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import java.io.IOException
import com.example.ecosnap.core.network.BackendException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firebase identity + EcoSnap backend profile.
 *
 * Account-recovery invariants (see also the backend contract in EcoSnap-BE docs/api/accounts.md):
 * - The backend profile (GET /me) is the only authority for role, status and balance. A login or
 *   restore never POSTs unless GET /me says account_not_found.
 * - A Firebase user can exist without a backend profile (process death, timeout, 5xx). The
 *   non-secret registration draft is therefore saved per UID BEFORE the POST and is kept until the
 *   profile is confirmed or the backend definitively rejects it (400). Ambiguous failures keep it.
 * - POST is idempotent for identical data (201/200). On 409 a different profile already exists: GET /me
 *   is authoritative and the profile is never overwritten or recreated.
 * - Passwords are used only for the Firebase call and are never stored or sent to the backend.
 * - Every backend call goes through the shared BackendClient: fresh SDK token per call, one forced
 *   refresh and replay on 401. A terminal 401 or 403 account_forbidden signs out of Firebase.
 * - Firebase users are never deleted on registration failure; the signed-in user can retry.
 */
class AuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val accountApi: AccountApi,
    private val pendingStore: PendingRegistrationStore,
) {
    /** Emits the Firebase UID on every SDK auth-state change (null = signed out, including backend-forced). */
    val authUid: Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    suspend fun restore(): SessionOutcome {
        val user = firebaseAuth.currentUser ?: return SessionOutcome.SignedOut
        return resolve(user)
    }

    suspend fun signIn(email: String, password: String): SessionOutcome {
        val trimmed = email.trim()
        if (trimmed.isEmpty() || password.isEmpty()) throw AuthFailure("Email dan kata sandi wajib diisi.")
        val user = try {
            firebaseAuth.signInWithEmailAndPassword(trimmed, password).await().user
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw AuthFailure(signInMessage(e))
        } ?: throw AuthFailure("Masuk gagal. Silakan coba lagi.")
        return resolve(user)
    }

    suspend fun register(email: String, password: String, profile: RegistrationProfile): SessionOutcome {
        val draft = profile.normalized()
        val trimmed = email.trim()
        if (trimmed.isEmpty() || password.isEmpty()) throw AuthFailure("Email dan kata sandi wajib diisi.")
        val user = try {
            firebaseAuth.createUserWithEmailAndPassword(trimmed, password).await().user
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw AuthFailure(registerMessage(e))
        } ?: throw AuthFailure("Pendaftaran gagal. Silakan coba lagi.")
        return saveThenSubmit(user, draft)
    }

    /** Authenticated user without a backend profile: submit the profile without a new signup. */
    suspend fun completeProfile(profile: RegistrationProfile): SessionOutcome {
        val user = firebaseAuth.currentUser
            ?: throw AuthFailure("Sesi berakhir. Silakan masuk kembali.", SessionOutcome.SignedOut)
        return saveThenSubmit(user, profile.normalized())
    }

    /** Signs out of Firebase only; pending drafts stay keyed by UID for later completion. */
    fun signOut() {
        firebaseAuth.signOut()
    }

    private suspend fun saveThenSubmit(user: FirebaseUser, draft: RegistrationProfile): SessionOutcome {
        try {
            pendingStore.save(user.uid, draft)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Without a durable draft an ambiguous POST could lose retry intent, so do not POST.
            throw AuthFailure(
                "Data pendaftaran belum dapat disimpan di perangkat. Silakan kirim ulang.",
                SessionOutcome.NeedsProfile(user.email.orEmpty(), draft),
            )
        }
        return submit(user, draft)
    }

    /** GET /me restore path; falls through to the stored draft only on account_not_found. */
    private suspend fun resolve(user: FirebaseUser): SessionOutcome {
        val profile = try {
            accountApi.me()
        } catch (e: BackendException) {
            if (!e.isNotFound()) throw failureFor(user, e)
            if (firebaseAuth.currentUser?.uid != user.uid) {
                throw AuthFailure("Sesi berubah. Silakan masuk kembali.", SessionOutcome.SignedOut)
            }
            val draft = pendingStore.read(user.uid)
                ?: return SessionOutcome.NeedsProfile(user.email.orEmpty(), null)
            return submit(user, draft)
        }
        return authenticated(user, profile)
    }

    private suspend fun submit(user: FirebaseUser, draft: RegistrationProfile): SessionOutcome {
        val profile = try {
            accountApi.register(draft)
        } catch (e: BackendException) {
            when {
                // A different profile exists already: the backend copy wins, never overwrite it.
                e.isConflict() -> try {
                    accountApi.me()
                } catch (g: BackendException) {
                    throw failureFor(user, g)
                }
                // Definite rejection: the draft is invalid, so drop it and let the user correct it.
                e.status == 400 -> {
                    pendingStore.remove(user.uid)
                    throw AuthFailure(
                        e.message ?: "Data pendaftaran ditolak server.",
                        SessionOutcome.NeedsProfile(user.email.orEmpty(), draft),
                    )
                }
                else -> throw failureFor(user, e)
            }
        }
        return authenticated(user, profile)
    }

    private suspend fun authenticated(user: FirebaseUser, profile: AccountProfile): SessionOutcome {
        if (firebaseAuth.currentUser?.uid != user.uid) {
            throw AuthFailure("Sesi berubah. Silakan masuk kembali.", SessionOutcome.SignedOut)
        }
        if (profile.uid != user.uid) {
            // Never trust a profile that belongs to another UID.
            throw AuthFailure(UNAVAILABLE_MESSAGE, SessionOutcome.Unavailable(user.email.orEmpty()))
        }
        pendingStore.remove(user.uid)
        return SessionOutcome.Authenticated(profile)
    }

    private fun failureFor(user: FirebaseUser, e: BackendException): AuthFailure {
        val email = user.email.orEmpty()
        return when {
            e.isUnauthorized() || e.isForbidden() -> {
                // The client already signed out for terminal 401/403; this covers profile-level 403.
                if (firebaseAuth.currentUser?.uid == user.uid) firebaseAuth.signOut()
                val message = if (e.isForbidden()) {
                    "Akun tidak aktif. Hubungi dukungan EcoSnap."
                } else {
                    "Sesi tidak valid. Silakan masuk kembali."
                }
                AuthFailure(message, SessionOutcome.SignedOut)
            }
            e.code == "session_changed" -> AuthFailure("Sesi berubah. Silakan masuk kembali.", SessionOutcome.SignedOut)
            else -> AuthFailure(UNAVAILABLE_MESSAGE, SessionOutcome.Unavailable(email))
        }
    }

    private fun BackendException.isUnauthorized() = status == 401
    private fun BackendException.isForbidden() = status == 403 && code == "account_forbidden"
    private fun BackendException.isNotFound() = status == 404 && code == "account_not_found"
    private fun BackendException.isConflict() = status == 409 && code == "account_conflict"

    // Messages never include Firebase exception text, so credentials and tokens cannot leak.
    private fun signInMessage(e: Exception): String = when (e) {
        is FirebaseAuthInvalidUserException ->
            if (e.errorCode == "ERROR_USER_DISABLED") "Akun ini dinonaktifkan." else "Email atau kata sandi salah."
        is FirebaseAuthInvalidCredentialsException -> "Email atau kata sandi salah."
        is FirebaseTooManyRequestsException -> "Terlalu banyak percobaan. Coba lagi beberapa saat lagi."
        is FirebaseNetworkException, is IOException -> NETWORK_MESSAGE
        else -> "Masuk gagal. Silakan coba lagi."
    }

    private fun registerMessage(e: Exception): String = when (e) {
        is FirebaseAuthUserCollisionException -> "Email sudah terdaftar. Silakan masuk."
        is FirebaseAuthWeakPasswordException -> "Kata sandi terlalu lemah. Gunakan minimal 6 karakter."
        is FirebaseAuthInvalidCredentialsException -> "Format email tidak valid."
        is FirebaseTooManyRequestsException -> "Terlalu banyak percobaan. Coba lagi beberapa saat lagi."
        is FirebaseNetworkException, is IOException -> NETWORK_MESSAGE
        else -> "Pendaftaran gagal. Silakan coba lagi."
    }

    private companion object {
        const val NETWORK_MESSAGE = "Tidak dapat terhubung. Periksa koneksi internet Anda."
        const val UNAVAILABLE_MESSAGE = "Server EcoSnap belum dapat dihubungi. Periksa koneksi, lalu coba lagi."
    }
}
