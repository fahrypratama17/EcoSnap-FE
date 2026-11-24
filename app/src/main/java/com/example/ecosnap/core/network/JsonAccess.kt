package com.example.ecosnap.core.network

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

/** Strict accessors for server JSON: any missing/mistyped value is an unusable response, never a default. */
fun invalidResponse(requestId: String? = null): BackendException =
    BackendException(null, "invalid_response", "Respons server tidak dapat dibaca. Silakan coba lagi.", requestId)

fun JsonObject.requireString(name: String, allowEmpty: Boolean = false): String {
    val value = (get(name) as? JsonPrimitive)?.takeIf { it.isString }?.asString ?: throw invalidResponse()
    if (!allowEmpty && value.isEmpty()) throw invalidResponse()
    return value
}

fun JsonObject.requireObject(name: String): JsonObject = get(name) as? JsonObject ?: throw invalidResponse()

/** Missing or JSON null gives null; any other non-object is invalid. */
fun JsonObject.optionalObject(name: String): JsonObject? {
    val element: JsonElement = get(name) ?: return null
    if (element.isJsonNull) return null
    return element as? JsonObject ?: throw invalidResponse()
}

/** Integral JSON number only; fractional values are invalid. */
fun JsonObject.requireLong(name: String): Long {
    val number = (get(name) as? JsonPrimitive)?.takeIf { it.isNumber } ?: throw invalidResponse()
    return try {
        number.asBigDecimal.longValueExact()
    } catch (_: ArithmeticException) {
        throw invalidResponse()
    }
}
