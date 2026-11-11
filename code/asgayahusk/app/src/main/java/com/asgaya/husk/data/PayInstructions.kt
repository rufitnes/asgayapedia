package com.asgaya.husk.data

/**
 * Phase-0 **payment-instructions exchange** over Nostr DMs (`ui-rethink/07`).
 *
 * The sender, having picked an ad, asks the seller **how to pay**; the reply is the
 * method-specific instruction. It doubles as a **liveness check** — no reply → don't pay.
 *
 * Cash (the universal method) needs no private data (the location is already in the listing),
 * but the same channel carries Bizum / SEPA details later, so it is built once here.
 */
data class PayInstructionsRequest(
    val adId: String,
    val amount: String?,
    val currency: String?,
    val requestId: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMessage(): String = buildString {
        appendLine("[PAY_INSTRUCTIONS_REQUEST]")
        appendLine("adId=$adId")
        amount?.let { appendLine("amount=$it") }
        currency?.let { appendLine("currency=$it") }
        appendLine("requestId=$requestId")
        appendLine("timestamp=$timestamp")
        appendLine("[/PAY_INSTRUCTIONS_REQUEST]")
    }

    companion object {
        fun fromMessage(message: String): PayInstructionsRequest? {
            if (!message.contains("[PAY_INSTRUCTIONS_REQUEST]")) return null
            val p = parseKeyValues(message)
            return try {
                PayInstructionsRequest(
                    adId = p["adId"] ?: return null,
                    amount = p["amount"],
                    currency = p["currency"],
                    requestId = p["requestId"] ?: java.util.UUID.randomUUID().toString(),
                    timestamp = p["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

data class PayInstructions(
    val requestId: String,
    val adId: String,
    val method: String,               // "cash" | "bizum" | …
    val fields: Map<String, String>,  // method-specific: cash → location/hours; bizum → name/phone/concept
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMessage(): String = buildString {
        appendLine("[PAY_INSTRUCTIONS]")
        appendLine("requestId=$requestId")
        appendLine("adId=$adId")
        appendLine("method=$method")
        fields.forEach { (k, v) -> appendLine("$k=$v") }
        appendLine("timestamp=$timestamp")
        appendLine("[/PAY_INSTRUCTIONS]")
    }

    companion object {
        private val ENVELOPE = setOf("requestId", "adId", "method", "timestamp")

        fun fromMessage(message: String): PayInstructions? {
            if (!message.contains("[PAY_INSTRUCTIONS]")) return null
            val p = parseKeyValues(message)
            val method = p["method"] ?: return null
            return try {
                PayInstructions(
                    requestId = p["requestId"] ?: "",
                    adId = p["adId"] ?: "",
                    method = method,
                    fields = p.filterKeys { it !in ENVELOPE },
                    timestamp = p["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

private fun parseKeyValues(message: String): Map<String, String> {
    val out = mutableMapOf<String, String>()
    message.lines().forEach { line ->
        val t = line.trim()
        if (t.contains("=")) {
            val (k, v) = t.split("=", limit = 2)
            out[k.trim()] = v.trim()
        }
    }
    return out
}
