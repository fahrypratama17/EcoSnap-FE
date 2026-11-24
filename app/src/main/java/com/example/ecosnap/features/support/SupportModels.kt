package com.example.ecosnap.features.support

import com.google.gson.JsonObject

data class Faq(val id: String, val category: String, val question: String, val answer: String, val isDemo: Boolean)

enum class TicketKind(val wire: String, val label: String) {
    Problem("problem", "Masalah"),
    Feedback("feedback", "Masukan");

    companion object {
        fun fromWire(value: String): TicketKind? = entries.firstOrNull { it.wire == value }
    }
}

enum class TicketStatus(val wire: String, val label: String) {
    Open("open", "Terbuka"),
    InProgress("in_progress", "Diproses"),
    Resolved("resolved", "Selesai");

    companion object {
        fun fromWire(value: String): TicketStatus? = entries.firstOrNull { it.wire == value }
    }
}

/** [staffResponse]/[respondedAt] are null until a staff member replies: that means "no reply yet". */
data class Ticket(
    val id: String,
    val kind: TicketKind,
    val subject: String,
    val message: String,
    val status: TicketStatus,
    val staffResponse: String?,
    val respondedAt: String?,
    val createdAt: String,
    val updatedAt: String,
)

data class TicketPage(val items: List<Ticket>, val nextCursor: String?)

/** A draft that already passed [TicketRules]; its values are exactly what is sent and hashed server-side. */
data class ValidTicket(val kind: TicketKind, val subject: String, val message: String) {
    fun toPayload(): JsonObject = JsonObject().apply {
        addProperty("kind", kind.wire)
        addProperty("subject", subject)
        addProperty("message", message)
    }
}

data class TicketFieldErrors(val subject: String? = null, val message: String? = null) {
    val any: Boolean get() = subject != null || message != null
}

object TicketRules {
    const val SUBJECT_MAX = 150
    const val MESSAGE_MAX = 4000
    const val MAX_BODY_BYTES = 64 * 1024
    /** Hard cap on typed/pasted characters so a huge paste cannot freeze the UI. */
    const val INPUT_CAP = 8000

    /** Server rule: trim edges, CRLF becomes LF, then count Unicode code points (not bytes or UTF-16 units). */
    fun normalize(text: String): String = text.replace("\r\n", "\n").trim()

    fun validate(kind: TicketKind, subjectRaw: String, messageRaw: String): Pair<ValidTicket?, TicketFieldErrors> {
        val subject = normalize(subjectRaw)
        val message = normalize(messageRaw)
        val subjectError = checkText(subject, SUBJECT_MAX, allowNewline = false, label = "Subjek")
        val messageError = checkText(message, MESSAGE_MAX, allowNewline = true, label = "Pesan")
        val errors = TicketFieldErrors(subjectError, messageError)
        if (errors.any) return null to errors
        val valid = ValidTicket(kind, subject, message)
        // Defensive: the server rejects bodies above 64 KiB; the character limits keep us far below it.
        val bytes = valid.toPayload().toString().toByteArray(Charsets.UTF_8).size
        if (bytes > MAX_BODY_BYTES) return null to TicketFieldErrors(message = "Pesan terlalu besar untuk dikirim.")
        return valid to errors
    }

    fun length(text: String): Int = normalize(text).let { it.codePointCount(0, it.length) }

    private fun checkText(text: String, max: Int, allowNewline: Boolean, label: String): String? {
        if (text.isEmpty()) return "$label wajib diisi."
        var count = 0
        var i = 0
        while (i < text.length) {
            val cp = text.codePointAt(i)
            val unit = Character.charCount(cp)
            if (unit == 1 && Character.isSurrogate(text[i])) return "$label memuat karakter tidak valid."
            if (cp == '\n'.code) {
                if (!allowNewline) return "$label harus satu baris."
            } else if (Character.isISOControl(cp)) {
                return "$label memuat karakter kontrol yang tidak diizinkan."
            }
            count++
            i += unit
        }
        if (count > max) return "$label maksimum $max karakter (sekarang $count)."
        return null
    }
}
