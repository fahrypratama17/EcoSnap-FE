package com.example.ecosnap.core.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first

/**
 * Holds non-secret registration metadata keyed by Firebase UID so an interrupted signup can be
 * finished later without asking for the password again. The file lives in noBackupFilesDir, so it is
 * excluded from Auto Backup and device transfer. Entries survive sign-out on purpose; they are removed
 * only once the backend profile is resolved or the backend definitively rejected the data.
 */
class PendingRegistrationStore(context: Context) {
    private val dataStore = sharedStore(context.applicationContext)

    /** Throws [IOException] when the draft could not be durably written. */
    internal suspend fun save(uid: String, profile: RegistrationProfile) {
        val json = gson.toJson(
            StoredDraft(
                role = profile.role.name,
                fullName = profile.fullName,
                phone = profile.phone,
                businessName = profile.businessName,
                businessCategory = profile.businessCategory,
            ),
        )
        dataStore.edit { it[keyFor(uid)] = json }
    }

    /** Returns null when absent, unreadable or no longer valid; invalid entries are discarded. */
    internal suspend fun read(uid: String): RegistrationProfile? {
        val json: String? = try {
            dataStore.data.first()[keyFor(uid)]
        } catch (_: IOException) {
            null
        }
        if (json == null) return null
        val profile = try {
            val stored = gson.fromJson(json, StoredDraft::class.java)
            RegistrationProfile(
                role = AccountRole.valueOf(stored.role ?: ""),
                fullName = stored.fullName.orEmpty(),
                phone = stored.phone.orEmpty(),
                businessName = stored.businessName,
                businessCategory = stored.businessCategory,
            ).normalized()
        } catch (_: Exception) {
            null
        }
        if (profile == null) remove(uid)
        return profile
    }

    /** Best-effort cleanup; a leftover draft is harmless because GET /me is consulted first. */
    internal suspend fun remove(uid: String) {
        try {
            dataStore.edit { it.remove(keyFor(uid)) }
        } catch (_: IOException) {
        }
    }

    private fun keyFor(uid: String) = stringPreferencesKey("pending_$uid")

    private class StoredDraft(
        val role: String?,
        val fullName: String?,
        val phone: String?,
        val businessName: String?,
        val businessCategory: String?,
    )

    private companion object {
        val gson = Gson()

        // DataStore forbids two active instances on one file, so share a single one per process.
        @Volatile
        var instance: DataStore<Preferences>? = null

        fun sharedStore(context: Context): DataStore<Preferences> =
            instance ?: synchronized(this) {
                instance ?: PreferenceDataStoreFactory.create(
                    scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
                    produceFile = { File(context.noBackupFilesDir, "datastore/ecosnap_pending_registration.preferences_pb") },
                ).also { instance = it }
            }
    }
}
