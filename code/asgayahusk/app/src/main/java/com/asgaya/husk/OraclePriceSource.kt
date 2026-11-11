package com.asgaya.husk

import android.util.Log
import com.asgaya.husk.covenant.CovenantRecovery

/**
 * The single sanctioned BCH price source: the Asgaya oracle service
 * (`collaborative_workspace/oracle-production/00`; Suso `ui-rethink/15 §6`).
 *
 * A hardcoded price is a **no even as a fallback**. If the oracle is unavailable this returns
 * `null` and the caller must **block/warn** — never guess (`ui-rethink/16` P0-2).
 */
object OraclePriceSource {

    private const val TAG = "OraclePriceSource"
    private const val CACHE_TTL_MS = 60_000L

    @Volatile
    private var cached: Pair<Double, Long>? = null  // (EUR per BCH, fetchedAtMs)

    /** EUR per 1 BCH from the oracle; `null` if unavailable (no hardcoded fallback). */
    suspend fun eurPerBch(): Double? {
        val now = System.currentTimeMillis()
        cached?.let { (rate, at) -> if (now - at < CACHE_TTL_MS) return rate }
        return try {
            val rate = CovenantRecovery.fetchOraclePrice().price / 100.0  // oracle price is in cents
            cached = rate to now
            rate
        } catch (e: Exception) {
            Log.e(TAG, "❌ Oracle unavailable — refusing a hardcoded price fallback", e)
            null
        }
    }
}
