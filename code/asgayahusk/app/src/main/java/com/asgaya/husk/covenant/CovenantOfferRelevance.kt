package com.asgaya.husk.covenant

/**
 * Relevance of an incoming `[COVENANT_V25]` offer (`ui-rethink/37`).
 *
 * Nostr relays replay **all** historical offers on (re)connect. An offer is only worth surfacing
 * to the recipient if the covenant can still be claimed:
 *   - its **pay window** hasn't closed (`expiryOracleTime` in the future), and
 *   - it still holds a **live UTXO** on-chain (spent → claimed/refunded/aborted).
 *
 * Pure (no I/O) → unit-tested. The caller supplies the on-chain liveness; `null` = couldn't check
 * (offline / hiccup) → **keep**, so a real payment is never dropped on a transient failure.
 */
object CovenantOfferRelevance {

    fun shouldSurface(expiryOracleTime: Long?, nowSeconds: Long, hasLiveUtxo: Boolean?): Boolean {
        if (expiryOracleTime != null && nowSeconds > expiryOracleTime) return false
        return hasLiveUtxo != false
    }
}
