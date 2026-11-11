package com.asgaya.husk.covenant

/**
 * Pure, dependency-free decision rules for the covenant / cash-out flows.
 *
 * Extracted from NotificationListener, RemittanceAdapter and TradeActivity so they can be
 * unit-tested in the JVM without Android (conventions §7.4: tests exercise the shipping code).
 * No Android imports — safe to call from any layer.
 */
object CovenantRules {

    /**
     * Merchant only auto-quotes a request younger than this (file 55 §3.2).
     * Policy, not security: the request `timestamp` is self-reported and untrusted.
     */
    const val REQUEST_FRESH_SECONDS = 60L

    /**
     * RS082 §4.4: 0-conf is accepted up to €200. Above that we only ADVISE 1 confirmation —
     * it is the user's money and their call (we warn, we do not block).
     */
    const val ZERO_CONF_WARN_EUR = 200.0

    /** True when a request is recent enough to auto-quote (freshness = margin protection). */
    fun isRequestFresh(
        requestTimestampMillis: Long,
        nowMillis: Long,
        windowSeconds: Long = REQUEST_FRESH_SECONDS
    ): Boolean {
        val ageSeconds = (nowMillis - requestTimestampMillis) / 1000
        return ageSeconds <= windowSeconds
    }

    /**
     * True when a quote has not expired. `expiresAtSeconds <= 0` means "no TTL" (legacy / QR) and
     * is treated as live, preserving the pre-file-55 behaviour.
     */
    fun isQuoteLive(expiresAtSeconds: Long, nowSeconds: Long): Boolean =
        expiresAtSeconds <= 0L || nowSeconds <= expiresAtSeconds

    /** True when the returned [SIGNED_TX] carries the quoteId of the live quote (file 55 §3.5). */
    fun quoteMatches(signedQuoteId: String?, quoteId: String): Boolean =
        !signedQuoteId.isNullOrBlank() && signedQuoteId == quoteId

    /**
     * Neutral, honest label for a *spent* covenant (its funds have moved).
     *
     * We only assert a direction the local device actually recorded. A covenant's resolution path
     * is NOT knowable from a local role flag — deriving "Claimed vs Refunded" from `isReceived`
     * made the same covenant read differently on each device (Suso, 2026-09-13). Unknown stays
     * neutral instead of guessing. (On-chain resolution is the audit-trail work, file 58.)
     *
     * The literals mirror `RemittanceStatus` (asserted in CovenantRulesTest).
     */
    fun resolutionLabel(status: String): String = when (status) {
        "claimed"   -> "✅ Claimed"
        "refunded"  -> "✅ Refunded"
        "delivered" -> "✅ Cashed out"
        "expired"   -> "⏱️ Expired"
        else        -> "✅ Settled"
    }

    /** Should the merchant be warned before handing cash? (0-conf is fine ≤ €200 — RS082.) */
    fun shouldWarnZeroConf(amountCash: Double): Boolean = amountCash > ZERO_CONF_WARN_EUR

    /**
     * The exact path, read from the spending tx (`ui-rethink/16` P1; `58`; tightened per `18 §2.1`).
     * The covenant splits the UTXO differently per path:
     *
     * - **1 output → sender** .............. `abort` (the covenant's "abort detection signal")
     * - **2 outputs, output[0] → recipient** `claim` (payment + buffer)
     * - **2 outputs, output[0] → sender** ... `refund` (also `sellerRecoverBuffer` — same shape)
     * - **2 outputs, output[0] → a 3rd party** `merchantCashout`
     *
     * **Residual (`18 §2.1`):** the covenant constrains output[0]/[1] but not the total count, so a
     * crafted extra output on an `abort` would read as `refund`. The **money is safe** (the covenant
     * enforces destinations); only the displayed path can be wrong, and only for a crafted spend.
     * We therefore use the **exact counts** and return `null` (path uncertain) on any other shape —
     * our own client always emits 1 (abort) or 2 (the rest).
     */
    fun classifyResolution(
        output0Address: String?,
        outputCount: Int,
        recipientAddress: String?,
        senderAddress: String?
    ): String? = when {
        output0Address.isNullOrBlank() -> null
        outputCount == 1 -> "abort"
        outputCount == 2 -> when {
            recipientAddress != null && output0Address == recipientAddress -> "claim"
            senderAddress != null && output0Address == senderAddress -> "refund"
            recipientAddress != null || senderAddress != null -> "merchantCashout"
            else -> null
        }
        else -> null  // 0 or >2 outputs: matches no single path → path uncertain
    }

    /**
     * The remittance **status** for a **spent** covenant, from the chain (`ui-rethink/37`).
     *
     * For a **received** covenant only the positive signal *"the funds landed in our wallet"*
     * (`output0Address == myAddress` → claimed) is trusted: we can't tell refund from
     * merchant-cashout without the counterparties' addresses, so anything else stays **neutral**
     * (`settled`) — never a misleading label.
     *
     * For a **sent** covenant the classified `path` is authoritative.
     */
    fun resolutionStatus(
        isReceived: Boolean,
        path: String?,
        output0Address: String?,
        myAddress: String?
    ): String {
        val rs = com.asgaya.husk.remittance.RemittanceStatus
        if (isReceived) {
            return if (output0Address != null && output0Address == myAddress) rs.CLAIMED else rs.SETTLED
        }
        return when (path) {
            com.asgaya.husk.data.CovenantResolved.PATH_CLAIM -> rs.CLAIMED
            com.asgaya.husk.data.CovenantResolved.PATH_ABORT -> rs.ABORTED
            com.asgaya.husk.data.CovenantResolved.PATH_MERCHANT_CASHOUT -> rs.DELIVERED
            com.asgaya.husk.data.CovenantResolved.PATH_REFUND,
            com.asgaya.husk.data.CovenantResolved.PATH_SELLER_RECOVER -> rs.REFUNDED
            else -> rs.SETTLED  // spent but the path isn't derivable → neutral
        }
    }

    /**
     * A covenant whose spend is **mined** (`spendHeight >= SETTLE_CONFIRMATIONS`, i.e. ≥1
     * confirmation) is **final**: the sweep stops re-auditing it. Until then it stays
     * `resolved_externally` (0-conf), so a false positive can still be reversed (`Suso 2026-10-03`).
     */
    const val SETTLE_CONFIRMATIONS = 1
    fun isSpendFinal(spendHeight: Int): Boolean = spendHeight >= SETTLE_CONFIRMATIONS

    /**
     * The seller funds only if the price hasn't **dropped** more than this since creation
     * (`ui-rethink/27`/`29`). A **rise** is free (a bigger buffer, fewer BCH needed) → no gate;
     * only a fall erodes the recipient's floor margin.
     */
    const val FUNDING_MAX_DROP = 0.015

    /**
     * `true` when the funder may fund: `current ≥ initial × (1 − maxDrop)` — i.e. the price has not
     * fallen more than [maxDrop] since the covenant's `initial` was set. Below it, the seller asks
     * for a fresh covenant (Phase 0+).
     */
    fun isFundingPriceAcceptable(
        initialPriceCents: Int,
        currentPriceCents: Int,
        maxDrop: Double = FUNDING_MAX_DROP
    ): Boolean = initialPriceCents > 0 && currentPriceCents >= initialPriceCents * (1.0 - maxDrop)

    /** The default sender **pay window** on a fund request (cash in person — `ui-rethink/27` §1: 5 min). */
    const val PAY_WINDOW_SECONDS_DEFAULT = 300

    /** When a fund request's pay window closes (`createdAt + payWindowSeconds`, default when null). */
    fun payByMillis(createdAtMillis: Long, payWindowSeconds: Int?): Long =
        createdAtMillis + (payWindowSeconds ?: PAY_WINDOW_SECONDS_DEFAULT) * 1000L
}
