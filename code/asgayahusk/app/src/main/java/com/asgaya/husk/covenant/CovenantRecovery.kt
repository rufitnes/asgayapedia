package com.asgaya.husk.covenant

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.asgaya.husk.AppDatabase
import com.asgaya.husk.AutoRefundPrefs
import com.asgaya.husk.CovenantRequest
import com.asgaya.husk.CovenantWebView
import com.asgaya.husk.CovenantExpiryReceiver
import com.asgaya.husk.ElectrumClient
import com.asgaya.husk.data.CovenantResolved
import com.asgaya.husk.data.Wallet
import com.asgaya.husk.wallet.WalletManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

/**
 * Shared helpers for the funder's buffer recovery — the `sellerRecoverBuffer` covenant path
 * (`ui-rethink/10`; covenant v2.3, "seller buffer recovery").
 *
 * Used by the active UI (RemittanceActivity) and, above all, by the always-on monitor
 * (NotificationListener) so a passive seller recovers its buffer without opening the app.
 */
object CovenantRecovery {

    private const val TAG = "CovenantRecovery"

    // Oracle configuration (Phase 0 - Development Infrastructure) — mirrors RemittanceActivity
    private const val ORACLE_URL = "http://localhost:3001/oracle/price"
    private const val ORACLE_PUBKEY = "0387053eebfb132fc6219ada6cb726fc672a15fc9729b36783bf6b057b83f122b0"

    // Wake up a few seconds AFTER the expiry so the ORACLE is unambiguously past it — the
    // covenant validates `oracleTimestamp >= expiryOracleTime` (a same-second attempt races).
    private const val EXPIRY_MARGIN_SECONDS = 5L

    // Retry backoff (ms) after a failed / not-yet recovery: covers cold start (WebView not
    // ready), Doze deferral and network hiccups. One entry per attempt; when exhausted we stop
    // (the always-on service sweep + opening the app remain as fallbacks).
    private val RETRY_DELAYS_MS = longArrayOf(
        30_000L, 60_000L, 5 * 60_000L, 15 * 60_000L, 60 * 60_000L, 3 * 60 * 60_000L, 12 * 60 * 60_000L
    )
    const val RETRY_PREF = "covenant_retry_attempt"
    private const val RETRY_REQUEST_CODE = 0x5E1100

    data class OraclePrice(
        val price: Int,        // Price in cents
        val timestamp: Long,   // Unix timestamp (seconds)
        val message: String,   // 16-byte message (hex)
        val signature: String, // DER-encoded signature (hex)
        val pubkey: String     // Oracle public key (hex)
    )

    suspend fun fetchOraclePrice(): OraclePrice = withContext(Dispatchers.IO) {
        val json = JSONObject(URL(ORACLE_URL).readText())
        val pubkey = json.optString("pubkey", "")
        if (pubkey.isNotEmpty() && pubkey != ORACLE_PUBKEY) {
            Log.w(TAG, "⚠️ Oracle pubkey mismatch: $pubkey")
        }
        OraclePrice(
            price = json.getInt("price"),
            timestamp = json.getLong("timestamp"),
            message = json.getString("message"),
            signature = json.getString("signature"),
            pubkey = pubkey
        )
    }

    /**
     * The boundary the covenant enforces for `sellerRecoverBuffer`: recoverable once the ORACLE
     * time has reached the expiry (`>=`, not `>`).
     */
    fun isRecoverable(oracleTimestamp: Long, expiryOracleTime: Long): Boolean =
        oracleTimestamp >= expiryOracleTime

    /** Does the covenant address still hold funds? `null` = couldn't tell (network). */
    suspend fun hasFunds(covenantAddress: String): Boolean? =
        runCatching {
            withContext(Dispatchers.IO) { CovenantBuildService.fetchUTXOs(covenantAddress) }
        }.getOrNull()?.isNotEmpty()

    /** The on-chain state of a covenant, as established by a positive-evidence audit. */
    private sealed interface Audit {
        object Funded : Audit
        object Unknown : Audit
        /** Spent; [spendHeight] > 0 means the spending tx is mined (≥1 confirmation). */
        data class Spent(val spendHeight: Int) : Audit
    }

    /**
     * Is the covenant spent? Answer **only on positive evidence** (`ui-rethink/16` P0-1; `18 §2.1`):
     * - `false` — the funding outpoint is **present** (still funded);
     * - `true` — the outpoint is absent **and** a **spending tx** exists on the chain;
     * - `null` — can't be established (retry), e.g. a freshly-broadcast outpoint not yet indexed.
     *
     * Absence alone is NOT "spent": a just-broadcast (unindexed) outpoint also reads as absent —
     * treating that as spent wrongly locked a covenant (the #613 false `resolved_externally`).
     */
    suspend fun isCovenantSpent(covenant: CovenantRequest): Boolean? = when (val a = auditCovenant(covenant)) {
        is Audit.Funded -> false
        is Audit.Spent -> true
        Audit.Unknown -> null
    }

    private suspend fun auditCovenant(c: CovenantRequest): Audit = try {
        val electrum = ElectrumClient()
        val txid = c.fundingTxid
        val vout = c.fundingVout
        val present = if (txid != null && vout != null) {
            electrum.getOutpointInfo(txid, vout) != null
        } else {
            // Legacy rows with no recorded outpoint: the address query is the best we have.
            withContext(Dispatchers.IO) { CovenantBuildService.fetchUTXOs(c.covenantAddress) }.isNotEmpty()
        }
        when {
            present -> Audit.Funded
            // Absent → require positive evidence of a spend (a tx whose input is the outpoint).
            else -> CovenantChainResolution.resolveFromChain(electrum, c.covenantAddress)
                ?.let { Audit.Spent(it.spendHeight) } ?: Audit.Unknown
        }
    } catch (e: Exception) {
        Log.e(TAG, "❌ Covenant audit failed for ${c.cashAccount} - retry later", e)
        Audit.Unknown
    }

    /**
     * Resolve + persist the funding outpoint (`txid:vout`) so later audits ask the chain about
     * THIS output (`ui-rethink/16` P0-1). Best-effort — a miss leaves it null and the audit falls
     * back to the address query.
     */
    suspend fun recordFundingOutpoint(database: AppDatabase, covenant: CovenantRequest, fundingTxid: String) {
        val client = ElectrumClient()
        repeat(5) { attempt ->
            val vout = runCatching {
                client.getUTXOsForAddress(covenant.covenantAddress)
                    .firstOrNull { it.txHash == fundingTxid }?.txPos
            }.getOrNull()
            if (vout != null) {
                database.covenantDao().updateFundingOutpoint(covenant.id, fundingTxid, vout)
                Log.d(TAG, "📍 Funding outpoint recorded for ${covenant.cashAccount}: ${fundingTxid.take(16)}:$vout")
                return
            }
            if (attempt < 4) kotlinx.coroutines.delay(2_000)
        }
        Log.w(TAG, "⚠️ Could not resolve the funding vout for ${covenant.cashAccount} (audit falls back to the address query)")
    }

    /**
     * Recover the funder's buffer after the covenant expired with the sender offline:
     * build + broadcast a `sellerRecoverBuffer` tx (output 0 = payment → sender,
     * output 1 = buffer → seller). The active wallet must be the covenant's funder.
     *
     * @return the broadcast transaction id
     */
    suspend fun recoverSellerBuffer(
        covenant: CovenantRequest,
        activeWallet: Wallet,
        wif: String,
        covenantWebView: CovenantWebView,
        oracle: OraclePrice? = null
    ): String {
        val o = oracle ?: fetchOraclePrice()
        val oracleSig = mapOf(
            "message" to o.message,
            "signature" to o.signature,
            "timestamp" to o.timestamp,
            "price" to o.price
        )
        val covenantParams = mapOf(
            "senderPubkey" to (covenant.senderPubkey ?: throw Exception("No sender pubkey")),
            "recipientPubkey" to (covenant.recipientPubkey ?: throw Exception("No recipient pubkey")),
            "funderPubkey" to (covenant.funderPubkey ?: throw Exception("No funder pubkey")),
            "oraclePubkey" to (covenant.oraclePubkey ?: throw Exception("No oracle pubkey")),
            "eurCents" to covenant.eurCents,
            "expiryOracleTime" to covenant.expiryTime,
            "initialBchPriceInCents" to covenant.initialBchPrice,
            "minPricePercent" to covenant.minPricePercent
        )
        return covenantWebView.sellerRecoverBufferHybrid(
            covenantParams = covenantParams,
            oracleSig = oracleSig,
            sellerWIF = wif,
            sellerAddress = activeWallet.address,
            senderAddress = "",  // Empty → JS derives from senderPubkey
            covenantAddress = covenant.covenantAddress
        )
    }

    /**
     * Sweep: recover the buffer for every funded covenant of the *active* wallet whose
     * oracle-time expiry has passed. Returns the number recovered.
     *
     * @param waitForReady when true (long-lived service) waits for the WebView bridge;
     *   when false (alarm receiver, short window) skips if the bridge isn't ready yet.
     */
    suspend fun recoverAllDue(
        context: Context,
        database: AppDatabase,
        walletManager: WalletManager,
        covenantWebView: CovenantWebView,
        waitForReady: Boolean = true
    ): Int {
        if (AutoRefundPrefs.load(context) == AutoRefundPrefs.Mode.MANUAL) return 0

        val activeWallet = walletManager.getActiveWallet() ?: return 0
        // Include `resolved_externally` (0-conf) so a FALSE resolved can still be reversed — but once
        // the spend is confirmed the status becomes the terminal `resolved` (excluded here), so the
        // sweep stops re-auditing it (`Suso 2026-10-03`).
        val funded = database.covenantDao().getAll().filter { c ->
            (c.status == "funded" || c.status == "resolved_externally") &&
                c.funderPubkey == activeWallet.publicKey &&
                c.senderPubkey != null && c.recipientPubkey != null && c.oraclePubkey != null
        }
        if (funded.isEmpty()) return 0

        // The chain is the truth — asked about the EXACT funding outpoint (`txid:vout`), not a
        // (possibly lagging) address query. present → recover; null → spent → conclude; a read
        // failure → retry + surface (never read a failure as "spent") (`ui-rethink/16` P0-1; `58`).
        val present = mutableListOf<CovenantRequest>()
        var auditFailed = 0
        funded.forEach { c ->
            when (val audit = auditCovenant(c)) {
                is Audit.Funded -> {
                    // Reverse a previous FALSE "resolved" (a not-yet-indexed outpoint looked absent).
                    if (c.status == "resolved_externally") {
                        Log.d(TAG, "♻️ ${c.cashAccount} is still funded — reversing a false 'resolved'")
                        database.covenantDao().updateStatus(c.id, "funded")
                    }
                    present.add(c)
                }
                is Audit.Spent -> {
                    if (CovenantRules.isSpendFinal(audit.spendHeight)) {
                        // ≥1 confirmation → final: a terminal status so the sweep stops re-auditing it.
                        if (c.status != "resolved") {
                            Log.d(TAG, "✅ ${c.cashAccount} spend confirmed (h=${audit.spendHeight}) — final")
                            database.covenantDao().updateStatus(c.id, "resolved")
                        }
                    } else if (c.status != "resolved_externally") {
                        // 0-conf: keep it reversible until the spend is mined (a false positive is possible).
                        Log.d(TAG, "⏳ ${c.cashAccount} spend unconfirmed — resolved_externally (will re-check)")
                        database.covenantDao().updateStatus(c.id, "resolved_externally")
                    }
                }
                Audit.Unknown -> auditFailed++
            }
        }
        if (auditFailed > 0) CovenantResolutionNotifier.notifyAuditFailure(context, auditFailed)
        if (present.isEmpty()) return 0

        if (!covenantWebView.isReady()) {
            if (!waitForReady) {
                Log.d(TAG, "WebView not ready - deferring to the sweep/retry")
                return 0
            }
            try {
                covenantWebView.awaitReady(timeoutMs = 30_000)
            } catch (e: Exception) {
                Log.e(TAG, "WebView not ready for buffer recovery", e)
                return 0
            }
        }

        // The covenant's clock is the ORACLE, not the device clock
        val oracle = try {
            fetchOraclePrice()
        } catch (e: Exception) {
            Log.e(TAG, "Oracle fetch failed - skipping recovery", e)
            return 0
        }
        val due = present.filter { isRecoverable(oracle.timestamp, it.expiryTime) }
        if (due.isEmpty()) return 0

        val wif = walletManager.getWIF(activeWallet)
        var recovered = 0
        due.forEach { covenant ->
            try {
                Log.d(TAG, "🛡️ Recovering buffer for ${covenant.cashAccount}")
                val txid = recoverSellerBuffer(covenant, activeWallet, wif, covenantWebView, oracle)
                database.covenantDao().updateStatusAndTxid(covenant.id, "buffer_recovered", txid)
                CovenantResolutionNotifier.notify(context, covenant, CovenantResolved.PATH_SELLER_RECOVER, txid)
                Log.d(TAG, "✅ Buffer recovered: $txid")
                recovered++
            } catch (e: Exception) {
                Log.e(TAG, "❌ Buffer recovery failed for ${covenant.cashAccount}", e)
            }
        }
        return recovered
    }

    /**
     * Schedule a wake-up at the covenant's expiry (`AlarmManager`). Exact when permitted,
     * otherwise inexact-but-Doze-safe. The receiver re-runs the sweep immediately.
     */
    fun scheduleExpiryAlarm(context: Context, covenant: CovenantRequest) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, CovenantExpiryReceiver::class.java)
            .putExtra(CovenantExpiryReceiver.EXTRA_COVENANT_ID, covenant.id)
        val pi = PendingIntent.getBroadcast(
            context,
            covenant.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = (covenant.expiryTime + EXPIRY_MARGIN_SECONDS) * 1000
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
                Log.d(TAG, "⏰ Alarm (inexact) set for ${covenant.cashAccount} @ $triggerAt")
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
                Log.d(TAG, "⏰ Alarm (exact) set for ${covenant.cashAccount} @ $triggerAt")
            }
        } catch (e: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            Log.d(TAG, "⏰ Alarm (fallback) set for ${covenant.cashAccount} @ $triggerAt")
        }
    }

    /** Cancel the scheduled expiry alarm (the covenant is already resolved). */
    fun cancelExpiryAlarm(context: Context, covenant: CovenantRequest) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, CovenantExpiryReceiver::class.java)
            .putExtra(CovenantExpiryReceiver.EXTRA_COVENANT_ID, covenant.id)
        val pi = PendingIntent.getBroadcast(
            context,
            covenant.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
        Log.d(TAG, "⏰ Cancelled expiry alarm for ${covenant.cashAccount}")
    }

    /** Is there still a funded covenant of the active wallet that we haven't concluded? */
    suspend fun hasUnresolvedFunded(database: AppDatabase, walletManager: WalletManager): Boolean {
        val active = walletManager.getActiveWallet() ?: return false
        return database.covenantDao().getAll().any {
            it.status == "funded" && it.funderPubkey == active.publicKey
        }
    }

    /**
     * Re-arm a retry alarm with backoff after a failed / not-yet recovery. `attempt` is the
     * 0-based retry index; returns false once the schedule is exhausted.
     */
    fun scheduleRetryAlarm(context: Context, attempt: Int): Boolean {
        val delay = if (attempt in RETRY_DELAYS_MS.indices) RETRY_DELAYS_MS[attempt] else return false
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, CovenantExpiryReceiver::class.java)
            .putExtra(CovenantExpiryReceiver.EXTRA_RETRY, true)
        val pi = PendingIntent.getBroadcast(
            context,
            RETRY_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = System.currentTimeMillis() + delay
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        } catch (e: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
        Log.d(TAG, "⏰ Retry alarm #${attempt + 1} in ${delay / 1000}s")
        return true
    }
}
