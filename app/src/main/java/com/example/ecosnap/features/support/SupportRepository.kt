package com.example.ecosnap.features.support

import android.content.Context
import com.example.ecosnap.core.network.BackendClient
import com.example.ecosnap.core.network.BackendException
import com.example.ecosnap.core.network.PendingMutation
import com.example.ecosnap.core.network.PendingMutationConflictException
import com.example.ecosnap.core.network.PendingMutationStore
import com.example.ecosnap.core.network.invalidResponse
import com.example.ecosnap.core.network.requireObject
import com.example.ecosnap.core.network.requireString
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.io.IOException

/** The scope already holds a different ticket intent that must be resolved first. */
class TicketIntentPendingException(val existing: PendingMutation) : Exception("Tiket sebelumnya belum selesai diproses.")

class SupportRepository(
    context: Context,
    private val uid: String,
    private val backend: BackendClient = BackendClient.default,
    private val store: PendingMutationStore = PendingMutationStore(context.applicationContext),
) {
    suspend fun faqs(): List<Faq> {
        val root = backend.request("GET", "api/v1/help/faqs") ?: throw invalidResponse()
        val data = root.get("data") as? JsonArray ?: throw invalidResponse()
        return data.map { element ->
            val faq = element as? JsonObject ?: throw invalidResponse()
            Faq(
                id = faq.requireString("id"),
                category = faq.requireString("category"),
                question = faq.requireString("question"),
                answer = faq.requireString("answer"),
                isDemo = (faq.get("is_demo") as? JsonPrimitive)?.takeIf { it.isBoolean }?.asBoolean ?: throw invalidResponse(),
            )
        }
    }

    suspend fun tickets(status: TicketStatus?, cursor: String?): TicketPage {
        val query = linkedMapOf("limit" to PAGE_SIZE.toString())
        if (status != null) query["status"] = status.wire
        if (!cursor.isNullOrEmpty()) query["cursor"] = cursor
        val root = backend.request("GET", TICKETS_PATH, query) ?: throw invalidResponse()
        val items = (root.get("data") as? JsonArray ?: throw invalidResponse()).map { parse(it as? JsonObject ?: throw invalidResponse()) }
        val pagination = root.requireObject("pagination")
        val next = pagination.get("next_cursor")?.takeIf { !it.isJsonNull }
            ?.let { (it as? JsonPrimitive)?.takeIf { p -> p.isString }?.asString ?: throw invalidResponse() }
            ?.takeIf { it.isNotEmpty() }
        return TicketPage(items, next)
    }

    suspend fun ticket(id: String): Ticket {
        if (!ID_PATTERN.matches(id)) throw BackendException(404, "ticket_not_found", "Tiket tidak ditemukan.")
        val root = backend.request("GET", "$TICKETS_PATH/$id") ?: throw invalidResponse()
        return parse(root.requireObject("data"))
    }

    /** Durable intent for this UID: unresolved (resultId null) or completed-but-unacknowledged. */
    suspend fun pending(): PendingMutation? = store.read(uid, SCOPE)

    /**
     * Persists the intent BEFORE any HTTP call, then sends it. Throws [TicketIntentPendingException] when a
     * different intent is still unresolved, [IOException] when storage fails (nothing was sent).
     */
    suspend fun submit(ticket: ValidTicket): Ticket {
        val mutation = try {
            store.begin(uid, SCOPE, ticket.toPayload())
        } catch (e: PendingMutationConflictException) {
            throw TicketIntentPendingException(e.existing)
        }
        return resume(mutation)
    }

    /**
     * Replays [mutation] with its original key and payload. A confirmed ticket is returned (and its id
     * recorded); a completed intent is recovered by GET. Only a final, known non-executed rejection clears
     * the intent; network, timeout, 5xx, 401 and cancellation keep it for a safe retry.
     */
    suspend fun resume(mutation: PendingMutation): Ticket {
        mutation.resultId?.let { id ->
            try {
                return ticket(id)
            } catch (e: BackendException) {
                if (e.status == 404) store.clear(uid, SCOPE, mutation.key)
                throw e
            }
        }
        val created = try {
            val root = backend.request(
                "POST",
                TICKETS_PATH,
                body = mutation.payload,
                expectedStatuses = setOf(200, 201),
                idempotencyKey = mutation.key,
            ) ?: throw invalidResponse()
            parse(root.requireObject("data"))
        } catch (e: BackendException) {
            if (isFinalRejection(e)) store.clear(uid, SCOPE, mutation.key)
            throw e
        }
        try {
            store.resolve(uid, SCOPE, mutation.key, created.id)
        } catch (_: IOException) {
            // The server already holds the ticket; an unresolved key just replays to the same ticket.
        }
        return created
    }

    /** The user has seen the completed ticket (or the intent is obsolete); drop the durable record. */
    suspend fun acknowledge(mutation: PendingMutation) {
        store.clear(uid, SCOPE, mutation.key)
    }

    private fun isFinalRejection(e: BackendException): Boolean =
        e.status in FINAL_REJECTIONS

    private fun parse(json: JsonObject): Ticket {
        val kind = TicketKind.fromWire(json.requireString("kind")) ?: throw invalidResponse()
        val status = TicketStatus.fromWire(json.requireString("status")) ?: throw invalidResponse()
        return Ticket(
            id = json.requireString("id"),
            kind = kind,
            subject = json.requireString("subject"),
            message = json.requireString("message"),
            status = status,
            staffResponse = json.optionalString("staff_response"),
            respondedAt = json.optionalString("responded_at"),
            createdAt = json.requireString("created_at"),
            updatedAt = json.requireString("updated_at"),
        )
    }

    private fun JsonObject.optionalString(name: String): String? {
        val element = get(name) ?: return null
        if (element.isJsonNull) return null
        return (element as? JsonPrimitive)?.takeIf { it.isString }?.asString ?: throw invalidResponse()
    }

    companion object {
        const val SCOPE = "support_ticket_create"
        const val PAGE_SIZE = 20
        private const val TICKETS_PATH = "api/v1/me/support-tickets"
        private val ID_PATTERN = Regex("^[A-Za-z0-9-]{1,64}$")
        // Statuses where the server definitively did not create the ticket.
        private val FINAL_REJECTIONS = setOf(400, 403, 404, 409, 413, 415, 429)
    }
}
