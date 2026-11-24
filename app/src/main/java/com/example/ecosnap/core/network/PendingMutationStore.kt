package com.example.ecosnap.core.network

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first

/**
 * One financial/idempotent intent of a feature [scope]. [key] is a valid `Idempotency-Key`; [payload] is
 * the exact non-secret request body (never a QR token, voucher code or SDK token); [resultId] is set once
 * the server confirmed the mutation and names the created resource.
 */
data class PendingMutation(val key: String, val payload: JsonObject, val resultId: String? = null)

/** Thrown by [PendingMutationStore.begin] when the scope still holds a different unacknowledged intent. */
class PendingMutationConflictException(val existing: PendingMutation) :
    IllegalStateException("A different mutation is still unresolved for this scope.")

/**
 * Durable per-UID store for idempotent mutations whose outcome may be unknown (timeouts, process death).
 * Persist with [begin] BEFORE sending the HTTP call, replay the same [PendingMutation.key] and payload
 * until the outcome is known, then [resolve] (success, result id kept) or [clear] (final known
 * non-executed 4xx, or a completed result the user acknowledged). Network errors, 5xx, 504 and
 * cancellation must leave the entry untouched. Entries survive sign-out on purpose. The file lives in
 * noBackupFilesDir, so it is excluded from Auto Backup and device transfer. Only the result id is stored,
 * never a response body, code or token.
 *
 * All methods throw [IOException] when storage is unreadable or unwritable; an unreadable entry is never
 * treated as absent, because that could trigger a duplicate mutation.
 */
class PendingMutationStore(context: Context) {
    private val dataStore = sharedStore(context.applicationContext)

    suspend fun read(uid: String, scope: String): PendingMutation? {
        val raw = dataStore.data.first()[keyFor(uid, scope)] ?: return null
        return decode(raw)
    }

    /**
     * Returns the existing intent when [payload] is identical (including a completed one), otherwise stores
     * a new intent with a fresh key. A different payload while an entry exists throws
     * [PendingMutationConflictException]; the feature must resolve or acknowledge the old one first.
     */
    suspend fun begin(uid: String, scope: String, payload: JsonObject): PendingMutation {
        val storeKey = keyFor(uid, scope)
        var result: PendingMutation? = null
        var conflict: PendingMutation? = null
        dataStore.edit { prefs ->
            val existing = prefs[storeKey]?.let { decode(it) }
            if (existing == null) {
                val created = PendingMutation(UUID.randomUUID().toString(), payload.deepCopy())
                prefs[storeKey] = encode(created)
                result = created
            } else if (existing.payload == payload) {
                result = existing
            } else {
                conflict = existing
            }
        }
        conflict?.let { throw PendingMutationConflictException(it) }
        return result!!
    }

    /** Records the server-confirmed [resultId] for [key]; no-op if the entry was replaced or removed. */
    suspend fun resolve(uid: String, scope: String, key: String, resultId: String) {
        require(resultId.isNotBlank() && resultId.length <= MAX_RESULT_ID) { "Invalid result id" }
        val storeKey = keyFor(uid, scope)
        dataStore.edit { prefs ->
            val existing = prefs[storeKey]?.let { decode(it) } ?: return@edit
            if (existing.key == key) prefs[storeKey] = encode(existing.copy(resultId = resultId))
        }
    }

    /** Removes the entry only when it still carries [key]. */
    suspend fun clear(uid: String, scope: String, key: String) {
        val storeKey = keyFor(uid, scope)
        dataStore.edit { prefs ->
            val existing = prefs[storeKey]?.let { decode(it) } ?: return@edit
            if (existing.key == key) prefs.remove(storeKey)
        }
    }

    private fun encode(mutation: PendingMutation): String = JsonObject().apply {
        addProperty("key", mutation.key)
        add("payload", mutation.payload)
        mutation.resultId?.let { addProperty("result_id", it) }
    }.toString()

    private fun decode(raw: String): PendingMutation = try {
        val root = JsonParser.parseString(raw).asJsonObject
        val key = root.get("key").asString
        val resultId = root.get("result_id")?.takeIf { !it.isJsonNull }?.asString
        PendingMutation(key, root.getAsJsonObject("payload"), resultId)
    } catch (e: RuntimeException) {
        throw IOException("Unreadable pending mutation", e)
    }

    // Length-prefixed UID keeps (uid, scope) pairs unambiguous.
    private fun keyFor(uid: String, scope: String) = stringPreferencesKey("m_${uid.length}_${uid}_$scope")

    private companion object {
        const val MAX_RESULT_ID = 128

        // DataStore forbids two active instances on one file, so share a single one per process.
        @Volatile
        var instance: DataStore<Preferences>? = null

        fun sharedStore(context: Context): DataStore<Preferences> =
            instance ?: synchronized(this) {
                instance ?: PreferenceDataStoreFactory.create(
                    scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
                    produceFile = { File(context.noBackupFilesDir, "datastore/ecosnap_pending_mutations.preferences_pb") },
                ).also { instance = it }
            }
    }
}
