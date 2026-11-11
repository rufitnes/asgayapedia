package com.asgaya.husk.data

/**
 * `[COVENANT_RESOLVED]` — the actor that resolved a covenant tells the counterparties *when*
 * to look and *what* happened. It is a **hint, not the truth**: the receiver must still verify
 * on-chain (the covenant is spent + the tx exists) before concluding (`ui-rethink/10`, `58`).
 *
 * Paths: `claim` | `refund` | `abort` | `merchantCashout` | `sellerRecoverBuffer`.
 */
data class CovenantResolved(
    val covenantAddress: String,
    val path: String,
    val txid: String,
    val timestamp: Long = System.currentTimeMillis() / 1000
) {
    fun toMessage(): String = buildString {
        appendLine("[COVENANT_RESOLVED]")
        appendLine("covenantAddress=$covenantAddress")
        appendLine("path=$path")
        appendLine("txid=$txid")
        appendLine("timestamp=$timestamp")
        appendLine("[/COVENANT_RESOLVED]")
    }

    companion object {
        const val PATH_CLAIM = "claim"
        const val PATH_REFUND = "refund"
        const val PATH_ABORT = "abort"
        const val PATH_MERCHANT_CASHOUT = "merchantCashout"
        const val PATH_SELLER_RECOVER = "sellerRecoverBuffer"

        fun fromMessage(text: String): CovenantResolved? {
            if (!text.contains("[COVENANT_RESOLVED]")) return null
            val kv = mutableMapOf<String, String>()
            text.lineSequence().forEach { line ->
                val i = line.indexOf('=')
                if (i > 0 && !line.startsWith("[")) kv[line.substring(0, i)] = line.substring(i + 1)
            }
            val addr = kv["covenantAddress"]?.takeIf { it.isNotBlank() } ?: return null
            val path = kv["path"]?.takeIf { it.isNotBlank() } ?: return null
            val txid = kv["txid"]?.takeIf { it.isNotBlank() } ?: return null
            val ts = kv["timestamp"]?.toLongOrNull() ?: (System.currentTimeMillis() / 1000)
            return CovenantResolved(addr, path, txid, ts)
        }
    }
}
