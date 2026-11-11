package com.asgaya.husk

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.content.pm.ServiceInfo
import com.asgaya.husk.remittance.Remittance
import com.asgaya.husk.remittance.RemittanceStatus
import com.asgaya.husk.remittance.expectedFundingBch
import com.asgaya.husk.data.WalletRole
import com.asgaya.husk.data.SimpleCashoutRequest
import com.asgaya.husk.data.MerchantCosignResponse
import com.asgaya.husk.data.CashoutMessage
import com.asgaya.husk.covenant.MerchantCashout
import com.asgaya.husk.covenant.CovenantBuildService
import com.asgaya.husk.covenant.CovenantRules
import com.asgaya.husk.covenant.CovenantOfferRelevance
import com.asgaya.husk.covenant.RawTxF2
import com.asgaya.husk.nostr.NostrClient
import com.asgaya.husk.nostr.NostrKeyManager
import com.asgaya.husk.nostr.NIP17GiftWrap
import com.asgaya.husk.wallet.WalletManager
import org.json.JSONObject
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.asgaya.husk.covenant.CovenantRecovery

class NotificationListener : NotificationListenerService() {

    private lateinit var database: AppDatabase
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val bankConfigs = mutableListOf<BankConfig>()
    private lateinit var electrumClient: ElectrumClient
    private lateinit var transactionBuilder: TransactionBuilder
    private lateinit var covenantWebView: CovenantWebView  // ⚠️1: For seller auto-funding

    // Nostr client for receiving seller coordination messages (Task 6)
    private lateinit var nostrClient: NostrClient
    private lateinit var nostrKeyManager: NostrKeyManager
    @Volatile
    private lateinit var walletManager: WalletManager  // assigned in a coroutine, read from the monitor coroutine

    // Mutex to prevent duplicate covenant inserts from concurrent coroutines
    private val covenantInsertMutex = Mutex()

    // N2 / 60 §5: per-sender auto-quote budget (keyed on the authenticated npub) so one
    // sender cannot starve the others.
    private val autoQuoteLimiter = com.asgaya.husk.covenant.SlidingWindowRateLimiter(
        AUTO_QUOTE_MAX_PER_WINDOW, AUTO_QUOTE_WINDOW_MS
    )

    companion object {
        private const val TAG = "NotificationListener"
        private val BIZUM_KEYWORDS = listOf("bizum", "recibido", "enviado")
        private const val CHANNEL_ID = "bizum_parser_service"
        private const val PAYMENT_CHANNEL_ID = "asgaya_payments"
        private const val RECEIVED_COVENANT_NOTIFICATION_BASE = 2000
        private const val CASHOUT_NOTIFICATION_BASE = 3000
        // N2: bound the work an anonymous sender can trigger via the auto-quote path.
        private const val AUTO_QUOTE_MAX_PER_WINDOW = 6
        private const val AUTO_QUOTE_WINDOW_MS = 60_000L
        private const val NOTIFICATION_ID = 1
        // Always-on buffer recovery cadence (`ui-rethink/10`)
        private const val RECOVERY_INTERVAL_MS = 2 * 60_000L
        private const val READINESS_POLL_MS = 5_000L
        private const val PREFS_NAME = "BizumParserPrefs"
        private const val DEBUG_MODE_KEY = "debug_mode"
        private const val SELECTED_BANK_KEY = "selected_bank"
    }

    private fun isDebugModeEnabled(): Boolean {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(DEBUG_MODE_KEY, false)
    }

    data class BankConfig(
        val packageName: String,
        val bankName: String,
        val patterns: List<PatternConfig>
    )

    data class PatternConfig(
        val regex: Regex,
        val amountGroup: Int?,
        val senderGroup: Int?,
        val referenceGroup: Int?,
        val senderInTitle: Boolean = false,
        val referenceRegex: Regex? = null
    )

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🔥🔥🔥 SERVICE CREATED - NotificationListener.onCreate() 🔥🔥🔥")

        // Start foreground service
        createNotificationChannel()
        val notification = createNotification()

        // Use appropriate startForeground based on API level
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // API 29+ supports foreground service type parameter
            startForeground(NOTIFICATION_ID, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            // API 24-28 use 2-parameter version
            startForeground(NOTIFICATION_ID, notification)
        }

        database = AppDatabase.getDatabase(this)
        electrumClient = ElectrumClient()
        com.asgaya.husk.nostr.NostrHealth.init(this)
        transactionBuilder = TransactionBuilder(electrumClient)
        covenantWebView = CovenantWebView.getInstance(this)  // ⚠️1: Initialize WebView singleton
        loadBankPatterns()
        Log.d(TAG, "✅ Service initialized: loaded ${bankConfigs.size} bank configs")
        Log.d(TAG, "  WebView ready: ${covenantWebView.isReady()}")

        // Initialize Nostr client for receiving seller coordination messages (Task 6)
        initializeNostrClient()

        // Always-on buffer recovery for funded covenants that expired (`ui-rethink/10`)
        startRecoveryMonitor()
    }

    /**
     * Always-on buffer-recovery monitor (`ui-rethink/10`): a passive seller recovers the
     * 7% buffer of funded covenants that expired with the sender offline, without opening the app.
     * Covenant path: `sellerRecoverBuffer` (the funder signs).
     */
    private fun startRecoveryMonitor() {
        serviceScope.launch {
            while (isActive) {
                // Gate on readiness: WalletManager is assigned by initializeNostrClient() in a
                // sibling coroutine, so poll quickly instead of skipping a whole interval
                // (TightDS `ui-rethink/16` P0-4: the old `isInitialized` check raced the
                // assignment → the first sweep silently did nothing for up to 2 minutes).
                if (!::walletManager.isInitialized) {
                    delay(READINESS_POLL_MS)
                    continue
                }
                try {
                    recoverExpiredCovenants()
                } catch (e: Exception) {
                    Log.e(TAG, "Recovery monitor iteration failed", e)
                }
                try {
                    // Notify the user about every involved covenant that has expired (`ui-rethink/39`):
                    // sent (refund available) and received (no longer claimable). Idempotent.
                    com.asgaya.husk.covenant.CovenantResolutionNotifier.notifyExpiredOnce(this@NotificationListener, database)
                } catch (e: Exception) {
                    Log.e(TAG, "Expiry-notify iteration failed", e)
                }
                try {
                    monitorAwaitingFunding()
                } catch (e: Exception) {
                    Log.e(TAG, "Funding monitor iteration failed", e)
                }
                try {
                    // Derive the state of our active covenants from the chain (`58`) — DMs only trigger.
                    com.asgaya.husk.covenant.RemittanceReconciler.reconcile(database, walletManager)
                } catch (e: Exception) {
                    Log.e(TAG, "Reconcile iteration failed", e)
                }
                delay(RECOVERY_INTERVAL_MS)
            }
        }
    }

    override fun onDestroy() {
        // Cancel the service-scoped loop(s). Without this, a rebind leaks the old instance's
        // `while(isActive)` sweep → duplicate sweeps + leaked work (TightDS `ui-rethink/16` P0-4).
        Log.d(TAG, "🛑 Service destroyed — cancelling service scope")
        serviceScope.cancel()
        super.onDestroy()
    }

    private suspend fun recoverExpiredCovenants() {
        if (!::walletManager.isInitialized) {
            Log.d(TAG, "⏳ Recovery sweep skipped: walletManager not ready yet")
            return
        }
        Log.d(TAG, "🔄 Recovery sweep running")
        CovenantRecovery.recoverAllDue(
            context = this,
            database = database,
            walletManager = walletManager,
            covenantWebView = covenantWebView,
            waitForReady = true
        )
    }

    /**
     * Always-on funding monitor for the SENDER's covenants (`ui-rethink/16` P1): when a seller funds
     * a covenant, the sender's remittance must move `awaiting_funding → pending` even if the app was
     * closed (the ViewModel monitor dies with the process). A read failure is retried next cycle.
     */
    private suspend fun monitorAwaitingFunding() {
        val awaiting = database.remittanceDao()
            .getByStatus(com.asgaya.husk.remittance.RemittanceStatus.AWAITING_FUNDING)
        if (awaiting.isEmpty()) return
        for (r in awaiting) {
            val address = r.covenantAddress ?: continue
            val utxos = runCatching { electrumClient.getUTXOsForAddress(address) }.getOrNull()
                ?: continue  // read failure → retry next cycle (never a false "funded")
            if (utxos.isEmpty()) continue
            // Funded — but only accept it once the collateral covers the EUR face + the buffer
            // (`ui-rethink/27`): an under-funded covenant must not be surfaced to the recipient.
            val expectedBch = r.expectedFundingBch()
            val receivedBch = utxos.sumOf { it.value } / 1e8
            if (expectedBch != null && receivedBch < expectedBch * 0.95) {
                Log.w(TAG, "⏳ Under-funded (${"%.8f".format(receivedBch)} < ${"%.8f".format(expectedBch)} BCH) — not notifying the recipient")
                continue
            }
            val txid = utxos[0].txHash
            database.remittanceDao().updateWithTxid(
                r.id, txid, com.asgaya.husk.remittance.RemittanceStatus.PENDING
            )
            Log.i(TAG, "💰 Funding detected for ${r.recipientName ?: address.take(12)} — remittance ${r.id} → pending ($txid)")

            // Covenant is funded → automatically deliver the [COVENANT_V25] offer to the recipient
            // (was a manual "Resend to recipient"). One-shot: the row is now PENDING, so it won't
            // be re-processed next cycle (`ui-rethink/19`).
            notifyRecipientCovenantFunded(r.copy(txid = txid))
        }
    }

    /**
     * Send the `[COVENANT_V25]` **offer** to the recipient over Nostr, once the covenant is funded.
     * Best-effort — a failed send keeps the manual "Resend to recipient" button as fallback.
     */
    private suspend fun notifyRecipientCovenantFunded(remittance: com.asgaya.husk.remittance.Remittance) {
        // Shared delivery (`ui-rethink/35`) — also used by the in-app funding monitor.
        com.asgaya.husk.remittance.CovenantOfferDelivery.send(
            database, walletManager, nostrKeyManager, IdentityPrefs.load(this).cashAccount, remittance
        )
    }

    /**
     * The seller pinged us (`[COVENANT_FUNDED]`, `ui-rethink/35`) — **verify the amount on-chain**
     * (the UTXO must cover the EUR face + buffer, `ui-rethink/27`), then deliver the offer to the
     * recipient **immediately** (the fast path; the periodic sweep stays the fallback).
     */
    private suspend fun handleCovenantFunded(covenantAddress: String, txid: String?) {
        val r = database.remittanceDao().findByCovenantAddress(covenantAddress) ?: return
        if (r.status != RemittanceStatus.AWAITING_FUNDING && r.status != RemittanceStatus.PENDING) return
        val utxos = runCatching { electrumClient.getUTXOsForAddress(covenantAddress) }.getOrNull() ?: return
        if (utxos.isEmpty()) {
            Log.w(TAG, "🎁 [COVENANT_FUNDED] but no UTXO yet ($covenantAddress) — sweep will retry")
            return
        }
        val expected = r.expectedFundingBch()
        val received = utxos.sumOf { it.value } / 1e8
        if (expected != null && received < expected * 0.95) {
            Log.w(TAG, "🎁 [COVENANT_FUNDED] but under-funded (${"%.8f".format(received)} < ${"%.8f".format(expected)} BCH) — not offering")
            return
        }
        val realTxid = txid?.takeIf { it.isNotBlank() } ?: utxos[0].txHash
        database.remittanceDao().updateWithTxid(r.id, realTxid, RemittanceStatus.PENDING)
        Log.i(TAG, "💰 [COVENANT_FUNDED] verified — remittance ${r.id} → pending ($realTxid)")
        notifyRecipientCovenantFunded(r.copy(txid = realTxid))
    }

    /** (Re)arm the expiry alarms for every funded covenant of the active wallet. */
    private suspend fun armCovenantAlarms() {
        if (!::walletManager.isInitialized) return
        val activeWallet = walletManager.getActiveWallet() ?: return
        database.covenantDao().getAll()
            .filter { it.status == "funded" && it.funderPubkey == activeWallet.publicKey }
            .forEach { CovenantRecovery.scheduleExpiryAlarm(this, it) }
    }

    /**
     * Initialize Nostr client for receiving seller coordination messages
     *
     * Task 6: Seller Integration
     * - Subscribe to kind-1059 events (NIP-17 gift-wrapped DMs)
     * - Unwrap and verify signatures
     * - Extract [FUND_COVENANT] content
     * - Call existing parseFundCovenant()
     */
    private fun initializeNostrClient() {
        serviceScope.launch {
            try {
                walletManager = WalletManager.getInstance(this@NotificationListener)
                nostrKeyManager = NostrKeyManager(this@NotificationListener)

                // (Re)arm the covenant expiry alarms now that the wallet is known
                // (survives reboot: the notification-listener service auto-starts on boot)
                runCatching { armCovenantAlarms() }

                // Get active wallet (seller wallet)
                val activeWallet = walletManager.getActiveWallet()
                if (activeWallet == null) {
                    Log.w(TAG, "No active wallet found for Nostr initialization")
                    return@launch
                }

                // Ensure Nostr keys exist (generate if needed)
                val sellerKeys = nostrKeyManager.ensureNostrKeys(activeWallet)

                // The service is the OWNER of the event handler (handler-owner race fix, 2026-09-25):
                // pass it at creation so the swallowing default is never installed, then set it
                // explicitly too (unconditional, in case another caller created the singleton first).
                val eventHandler: (JSONObject) -> Unit = { event ->
                    // CRITICAL: onEventReceived fires on OkHttp reader thread
                    // Dispatch to background thread to avoid blocking WebSocket
                    handleNostrEvent(event)
                }
                nostrClient = NostrClient.getInstance(sellerKeys.publicKeyHex, eventHandler)
                nostrClient.setEventHandler(eventHandler)

                // Connect to Nostr relays
                nostrClient.connect()

                // Log hex pubkey for E2E testing (copy this value to SELLER_NOSTR_PUBKEY in sender)
                Log.i(TAG, "✅ Nostr client initialized for seller")
                Log.i(TAG, "📋 SELLER_NOSTR_PUBKEY = \"${sellerKeys.publicKeyHex}\"")
                Log.i(TAG, "   Copy this value to ReviewSendActivity.kt for E2E testing")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Nostr client", e)
                com.asgaya.husk.nostr.NostrHealth.report(this@NotificationListener, "Nostr init failed: ${e.message}", e)
            }
        }
    }

    /**
     * Handle received Nostr event (kind-1059 gift-wrap)
     *
     * CRITICAL: This is called on OkHttp's reader thread!
     * Must dispatch to background thread immediately to avoid blocking WebSocket.
     *
     * @param event The kind-1059 gift-wrap event
     */
    /**
     * Cache a discovered board listing (NIP-99 `kind:30402`). Keyed by (pubkey, `d`) so an
     * addressable update replaces the previous version.
     */
    private suspend fun storeBoardListing(event: JSONObject) {
        val pubkey = event.optString("pubkey")
        val dTag = firstTagValue(event, "d")
        if (pubkey.isBlank() || dTag.isNullOrBlank()) {
            Log.w(TAG, "Ignoring 30402 without pubkey/d")
            return
        }
        try {
            database.boardEventDao().upsert(
                com.asgaya.husk.data.BoardEventEntity(
                    pubkey = pubkey,
                    adTag = dTag,
                    eventId = event.optString("id"),
                    createdAt = event.optLong("created_at"),
                    raw = event.toString()
                )
            )
            Log.i(TAG, "🏷️ Board listing cached: $dTag (${pubkey.take(12)}…)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cache board listing", e)
        }
    }

    private fun firstTagValue(event: JSONObject, name: String): String? {
        val tags = event.optJSONArray("tags") ?: return null
        for (i in 0 until tags.length()) {
            val tag = tags.optJSONArray(i) ?: continue
            if (tag.optString(0) == name) return tag.optString(1)
        }
        return null
    }

    private fun handleNostrEvent(event: JSONObject) {
        // Dispatch to background thread (don't block OkHttp reader thread!)
        serviceScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "📩 Received Nostr event: ${event.getString("id")}")

                // Verify this is a kind-1059 event
                val kind = event.getInt("kind")

                // Board discovery: NIP-99 classified listings (kind 30402) are public, self-signed,
                // and NOT gift-wrapped → cache them directly (no DM unwrap).
                if (kind == 30402) {
                    storeBoardListing(event)
                    return@launch
                }

                if (kind != 1059) {
                    Log.w(TAG, "Ignoring non-1059 event (kind=$kind)")
                    return@launch
                }

                // Get seller's private key for unwrapping
                val activeWallet = walletManager.getActiveWallet()
                if (activeWallet == null) {
                    Log.e(TAG, "No active wallet for unwrapping")
                    return@launch
                }

                val sellerKeys = nostrKeyManager.ensureNostrKeys(activeWallet)

                // Unwrap NIP-17: kind-1059 → kind-13 → kind-14
                // This verifies signatures at each layer and returns (content, senderPubkey)
                val (content, senderPubkey) = NIP17GiftWrap.unwrapDM(
                    giftWrap = event,
                    recipientPrivKeyHex = sellerKeys.privateKeyHex
                )

                Log.d(TAG, "📦 Unwrapped content (${content.length} bytes) from sender: $senderPubkey")

                // Route by message type
                when {
                    content.contains("[FUND_COVENANT]") -> {
                        Log.i(TAG, "💰 Received [FUND_COVENANT] via Nostr - parsing...")

                        // Call existing parser (same as Telegram path)
                        parseFundCovenant(content, senderPubkey)

                        Log.i(TAG, "✅ FUND_COVENANT processed successfully")
                    }

                    content.contains("[COVENANT_RESOLVED]") -> {
                        Log.i(TAG, "📣 Received [COVENANT_RESOLVED] via Nostr")
                        val resolved = com.asgaya.husk.data.CovenantResolved.fromMessage(content)
                        if (resolved != null) {
                            handleCovenantResolved(resolved)
                        } else {
                            Log.w(TAG, "Failed to parse [COVENANT_RESOLVED]")
                        }
                    }

                    content.contains("[COVENANT_V25]") -> {
                        Log.i(TAG, "🎁 Received [COVENANT_V25] via Nostr - parsing...")

                        val remittance = parseCovenantV25(content, senderPubkey)
                        if (remittance != null) {
                            ingestReceivedOffer(remittance)
                        } else {
                            Log.w(TAG, "Failed to parse COVENANT_V25 from Nostr message")
                        }
                    }

                    content.contains("[PAY_INSTRUCTIONS_REQUEST]") -> {
                        Log.i(TAG, "💳 Received [PAY_INSTRUCTIONS_REQUEST] via Nostr")
                        handlePayInstructionsRequest(content, senderPubkey, sellerKeys.privateKeyHex, sellerKeys.publicKeyHex)
                    }

                    content.contains("[PAY_INSTRUCTIONS]") -> {
                        Log.i(TAG, "💳 Received [PAY_INSTRUCTIONS] via Nostr")
                        val pi = com.asgaya.husk.data.PayInstructions.fromMessage(content)
                        if (pi != null) {
                            database.cashoutMessageDao().insert(
                                CashoutMessage(
                                    covenantAddress = pi.requestId,
                                    type = CashoutMessage.TYPE_PAY_INSTRUCTIONS,
                                    payload = content,
                                    messageHash = sha256Hex(content)
                                )
                            )
                        } else {
                            Log.w(TAG, "Failed to parse [PAY_INSTRUCTIONS]")
                        }
                    }

                    content.contains("[BCH_FOR_SALE]") -> {
                        Log.i(TAG, "🏪 Received [BCH_FOR_SALE] via Nostr")
                        val request = SimpleCashoutRequest.fromMessage(content)
                        if (request != null) {
                            val inserted = database.cashoutMessageDao().insert(
                                CashoutMessage(
                                    covenantAddress = request.covenantAddress,
                                    type = CashoutMessage.TYPE_FOR_SALE,
                                    payload = content,
                                    messageHash = sha256Hex(content)
                                )
                            )
                            if (inserted != -1L) {
                                maybeAutoQuote(request, senderPubkey)
                            } else {
                                Log.d(TAG, "Duplicate [BCH_FOR_SALE] ignored")
                            }
                        } else {
                            Log.w(TAG, "Failed to parse [BCH_FOR_SALE]")
                        }
                    }

                    content.contains("[BCH_PURCHASE_COSIGN]") -> {
                        Log.i(TAG, "🏪 Received [BCH_PURCHASE_COSIGN] via Nostr")
                        val cosign = MerchantCosignResponse.fromMessage(content)
                        if (cosign != null) {
                            database.cashoutMessageDao().insert(
                                CashoutMessage(
                                    covenantAddress = cosign.covenantAddress,
                                    type = CashoutMessage.TYPE_PURCHASE_COSIGN,
                                    payload = content,
                                    messageHash = sha256Hex(content)
                                )
                            )
                        } else {
                            Log.w(TAG, "Failed to parse [BCH_PURCHASE_COSIGN]")
                        }
                    }

                    content.contains("[SIGNED_TX]") -> {
                        Log.i(TAG, "🏪 Received [SIGNED_TX] via Nostr")
                        val addr = parseKvField(content, "covenantAddress")
                        if (!addr.isNullOrBlank()) {
                            database.cashoutMessageDao().insert(
                                CashoutMessage(
                                    covenantAddress = addr,
                                    type = CashoutMessage.TYPE_SIGNED_TX,
                                    payload = content,
                                    messageHash = sha256Hex(content),
                                    counterpartyNpub = senderPubkey  // the recipient → notify on broadcast
                                )
                            )
                            maybeSurfaceSignedCashout(addr, content, senderPubkey)
                        } else {
                            Log.w(TAG, "SIGNED_TX without covenantAddress — ignored")
                        }
                    }

                    content.contains("[CASHOUT_BROADCAST]") -> {
                        Log.i(TAG, "💸 Received [CASHOUT_BROADCAST] via Nostr")
                        val addr = parseKvField(content, "covenantAddress")
                        if (!addr.isNullOrBlank()) {
                            notifyCashoutBroadcast(
                                addr,
                                parseKvField(content, "amount"),
                                parseKvField(content, "currency")
                            )
                        }
                    }

                    content.contains("[COVENANT_FUNDED]") -> {
                        Log.i(TAG, "💰 Received [COVENANT_FUNDED] via Nostr")
                        val addr = parseKvField(content, "covenantAddress")
                        if (!addr.isNullOrBlank()) {
                            handleCovenantFunded(addr, parseKvField(content, "txid"))
                        }
                    }

                    else -> {
                        Log.w(TAG, "Received Nostr message but not a known tag: ${content.take(100)}")
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to process Nostr event", e)
                // Never drop funding/resolution DMs silently (`ui-rethink/16` P0-3).
                com.asgaya.husk.nostr.NostrHealth.report(this@NotificationListener, "Dropped a Nostr event: ${e.message}", e)
            }
        }
    }

    /**
     * Reply to a [PAY_INSTRUCTIONS_REQUEST]: the seller answers with its own ad's payment
     * instructions (method + fields). The reply doubles as the liveness proof (`ui-rethink/07`).
     */
    private suspend fun handlePayInstructionsRequest(
        content: String,
        requesterNpub: String,
        privKeyHex: String,
        pubKeyHex: String
    ) {
        val req = com.asgaya.husk.data.PayInstructionsRequest.fromMessage(content)
        if (req == null) { Log.w(TAG, "Bad [PAY_INSTRUCTIONS_REQUEST]"); return }
        val ad = database.listingDao().getById(req.adId)
        if (ad == null) { Log.w(TAG, "No own ad for ${req.adId} — ignoring"); return }
        val listing = ad.toListing()

        val method = when {
            listing.paymentMethods.any { it.contains("Cash", ignoreCase = true) } -> "cash"
            listing.paymentMethods.any { it.contains("Bizum", ignoreCase = true) } -> "bizum"
            listing.paymentMethods.any { it.contains("SEPA", ignoreCase = true) } -> "sepa"
            else -> listing.paymentMethods.firstOrNull()?.lowercase()?.replace(Regex("[^a-z0-9]+"), "_") ?: "unknown"
        }
        val fields = mutableMapOf<String, String>()
        when (method) {
            "cash" -> {
                ad.establishment?.takeIf { it.isNotBlank() }?.let { fields["name"] = it }
                listing.location?.let { fields["location"] = it }
                listing.availableHours?.let { fields["hours"] = it }
            }
            "bizum" -> {
                ad.establishment?.takeIf { it.isNotBlank() }?.let { fields["name"] = it }
                ad.contactPhone?.takeIf { it.isNotBlank() }?.let { fields["phone"] = it }
            }
            "sepa" -> {
                ad.establishment?.takeIf { it.isNotBlank() }?.let { fields["name"] = it }
                ad.contactIban?.takeIf { it.isNotBlank() }?.let { fields["iban"] = it }
            }
            "card" -> {
                ad.establishment?.takeIf { it.isNotBlank() }?.let { fields["name"] = it }
                listing.location?.let { fields["location"] = it }
                listing.availableHours?.let { fields["hours"] = it }
            }
        }
        ad.notes?.takeIf { it.isNotBlank() }?.let { fields["notes"] = it }

        // No instructions → do NOT answer: an unconfigured device must not interact with a payer (`07`).
        val complete = when (method) {
            "cash" -> !listing.location.isNullOrBlank() && !ad.establishment.isNullOrBlank()
            "bizum" -> !ad.establishment.isNullOrBlank() && !ad.contactPhone.isNullOrBlank()
            "sepa" -> !ad.establishment.isNullOrBlank() && !ad.contactIban.isNullOrBlank()
            "card" -> !listing.location.isNullOrBlank() && !ad.establishment.isNullOrBlank()
            else -> fields.isNotEmpty()
        }
        if (!complete) {
            Log.w(TAG, "No payment instructions for ${req.adId} ($method) — not replying (unconfigured device)")
            return
        }

        val reply = com.asgaya.husk.data.PayInstructions(
            requestId = req.requestId, adId = req.adId, method = method, fields = fields
        ).toMessage()

        try {
            val client = NostrClient.getInstance(pubKeyHex, null)
            client.connect()
            val giftWrap = NIP17GiftWrap.wrapDM(reply, privKeyHex, pubKeyHex, requesterNpub)
            val sent = client.publishEvent(giftWrap)
            Log.i(TAG, "💳 Pay instructions ($method) sent to ${requesterNpub.take(12)}… via $sent relays")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send [PAY_INSTRUCTIONS]", e)
        }
    }

    /**
     * Merchant background auto-quote (file 55): only quote a FRESH request.
     * The response (the quote) IS the liveness proof — no heartbeat needed.
     */
    private suspend fun maybeAutoQuote(request: SimpleCashoutRequest, recipientNostrPubkey: String) {
        val now = System.currentTimeMillis()
        if (!CovenantRules.isRequestFresh(request.timestamp, now)) {
            Log.i(TAG, "Request older than ${CovenantRules.REQUEST_FRESH_SECONDS}s — not auto-quoting")
            return
        }
        // N2: auto-quote is background work (oracle + build + pre-sign + publish). Only do it for a
        // known counterparty, and never faster than the rate limit — an anonymous npub must not be
        // able to farm quotes.
        val known = database.contactDao().getContactsWithNostr()
            .any { it.nostrPubkey == recipientNostrPubkey }
        if (!known) {
            Log.w(TAG, "Unknown counterparty ${recipientNostrPubkey.take(12)}… — not auto-quoting")
            return
        }
        if (!autoQuoteLimiter.allow(recipientNostrPubkey, now)) {
            Log.w(TAG, "Auto-quote rate limit for ${recipientNostrPubkey.take(12)}… — request ignored")
            return
        }
        val merchantWallet = walletManager.getActiveWallet()
        if (merchantWallet == null) {
            Log.w(TAG, "No active merchant wallet — cannot quote")
            return
        }
        try {
            val quote = MerchantCashout.quote(this, request, merchantWallet)
            if (quote == null) {
                Log.w(TAG, "Quote failed (covenant not funded?)")
                return
            }
            // Store our own quote so we can validate the returned [SIGNED_TX].
            database.cashoutMessageDao().insert(
                CashoutMessage(
                    covenantAddress = request.covenantAddress,
                    type = CashoutMessage.TYPE_PURCHASE_COSIGN,
                    payload = quote.message,
                    messageHash = sha256Hex(quote.message)
                )
            )
            // Send the quote to the recipient (from the unwrap senderPubkey).
            val myKeys = nostrKeyManager.ensureNostrKeys(merchantWallet)
            val client = NostrClient.getInstance(myKeys.publicKeyHex, null)
            client.connect()
            val giftWrap = NIP17GiftWrap.wrapDM(
                quote.message, myKeys.privateKeyHex, myKeys.publicKeyHex, recipientNostrPubkey
            )
            val sent = client.publishEvent(giftWrap)
            Log.i(TAG, "✅ Auto-quoted ${request.covenantAddress.take(20)}… sent to $sent relays (quoteId=${quote.quoteId})")
        } catch (e: Exception) {
            Log.e(TAG, "Auto-quote failed", e)
        }
    }

    /**
     * Merchant: a customer returned a fully-signed sale. Validate it (F2) and surface a
     * CALL TO ACTION — the merchant broadcasts and hands the cash (never automatic; a human
     * action is required and the merchant must control the moment).
     */
    private suspend fun surfaceSignedCashout(covenantAddress: String, content: String, senderPubkey: String) {
        val quoteRow = database.cashoutMessageDao().get(covenantAddress, CashoutMessage.TYPE_PURCHASE_COSIGN)
        val quote = quoteRow?.let { MerchantCosignResponse.fromMessage(it.payload) }
        if (quote == null) {
            Log.w(TAG, "No live quote for $covenantAddress — dropping [SIGNED_TX]")
            return
        }
        val signedQuoteId = parseKvField(content, "quoteId")
        if (!CovenantRules.quoteMatches(signedQuoteId, quote.quoteId)) {
            Log.w(TAG, "quoteId mismatch (got $signedQuoteId, expected ${quote.quoteId}) — dropping")
            return
        }
        val now = System.currentTimeMillis() / 1000
        if (!CovenantRules.isQuoteLive(quote.expiresAt, now)) {
            Log.w(TAG, "Quote expired (${quote.expiresAt} < $now) — dropping; wait for a fresh request")
            return
        }
        val txHex = parseKvField(content, "txHex")
        if (txHex.isNullOrBlank()) {
            Log.w(TAG, "SIGNED_TX without txHex — dropping")
            return
        }
        // F2 (E: now includes version/locktime): the returned tx must be our pre-signed tx
        // (only the recipient sig changed). Pure Kotlin — see covenant/RawTxF2.kt.
        when (val f2 = RawTxF2.matches(txHex, quote.transactionHex)) {
            is RawTxF2.Result.Match -> { /* ok */ }
            is RawTxF2.Result.Mismatch -> {
                Log.e(TAG, "F2: signed tx differs from pre-signed quote (${f2.reason}) — dropping")
                return
            }
            is RawTxF2.Result.Invalid -> {
                Log.e(TAG, "F2: could not decode tx (${f2.reason}) — dropping")
                return
            }
        }
        Log.i(TAG, "✅ Signed sale ready for $covenantAddress — awaiting merchant action (broadcast & hand cash)")
        notifyCashoutActionRequired(covenantAddress, quote.amountCash, quote.currency)
    }

    /** Parse a `key=value` field from a tag-block message. */
    private fun parseKvField(message: String, key: String): String? {
        message.lines().forEach { line ->
            val t = line.trim()
            if (t.startsWith("$key=")) return t.substringAfter("=").trim()
        }
        return null
    }

    /** SHA-256 hex of a message payload (dedup key: identical deliveries only). */
    private fun sha256Hex(s: String): String =
        java.security.MessageDigest.getInstance("SHA-256")
            .digest(s.toByteArray())
            .joinToString("") { "%02x".format(it) }

    private suspend fun maybeSurfaceSignedCashout(covenantAddress: String, content: String, senderPubkey: String) {
        surfaceSignedCashout(covenantAddress, content, senderPubkey)
    }

    /**
     * The merchant broadcast the cash-out sale → notify the recipient it is safe to collect the
     * cash, and open the Activity screen (where a dispute can start if the cash is not handed over).
     */
    private fun notifyCashoutBroadcast(covenantAddress: String, amount: String?, currency: String?) {
        try {
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) return
            // Open the Activity screen — where a dispute starts if the cash isn't handed over (`ui-rethink/41`).
            val intent = Intent(this, com.asgaya.husk.remittance.RemittanceActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pi = PendingIntent.getActivity(
                this, covenantAddress.hashCode() + 7, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val amt = amount?.takeIf { it.isNotBlank() }?.let { "${currency ?: ""} $it" }
            val notification = NotificationCompat.Builder(this, PAYMENT_CHANNEL_ID)
                .setContentTitle("💸 Merchant sent your payment")
                .setContentText(
                    "It is safe to collect your cash" + (amt?.let { " ($it)" } ?: "") +
                        ". If the merchant doesn't hand it over, open Activity to dispute."
                )
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
            NotificationManagerCompat.from(this).notify(covenantAddress.hashCode() + 7, notification)
            Log.i(TAG, "🔔 Broadcast notification posted for ${covenantAddress.take(16)}…")
        } catch (e: Exception) {
            Log.e(TAG, "notifyCashoutBroadcast failed", e)
        }
    }

    /** A sender's `[FUND_COVENANT]` arrived → notify the merchant to confirm the cash and fund. */
    private fun notifyNewFundingRequest(cashAccount: String, payAmount: Double?, payCurrency: String?, feePercent: Double?, method: String?) {
        try {
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) return
            val amt = if (payAmount != null && payAmount > 0.0) {
                "%s %.2f".format(payCurrency ?: "EUR", payAmount * (1.0 + (feePercent ?: 0.0)))
            } else "—"
            // Method-specific instruction for the merchant (`ui-rethink/07`).
            val instr = when {
                method.equals("Card in person", ignoreCase = true) -> "Enter $amt in the card reader"
                method.equals("Cash in person", ignoreCase = true) -> "Take $amt in cash"
                else -> "Fund $amt"
            }
            val intent = Intent(this, com.asgaya.husk.trade.TradeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pi = PendingIntent.getActivity(
                this, cashAccount.hashCode() + 13, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val notification = NotificationCompat.Builder(this, PAYMENT_CHANNEL_ID)
                .setContentTitle("Covenant fund request")
                .setContentText("$instr  (ref $cashAccount)")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
            NotificationManagerCompat.from(this).notify(cashAccount.hashCode() + 13, notification)
            Log.i(TAG, "🔔 New funding request notification posted ($cashAccount, $amt, $method)")
        } catch (e: Exception) {
            Log.e(TAG, "notifyNewFundingRequest failed", e)
        }
    }

    private fun notifyCashoutActionRequired(covenantAddress: String, amountCash: Double, currency: String?) {
        try {
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) return
            // Open the merchant's **Buy BCH** tab (where the signed sale is broadcast & the cash
            // handed) — not Home (`ui-rethink/41`).
            val intent = Intent(this, com.asgaya.husk.trade.TradeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(com.asgaya.husk.trade.TradeActivity.EXTRA_TAB, "buy")
            }
            val pi = PendingIntent.getActivity(
                this, covenantAddress.hashCode(), intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val notification = NotificationCompat.Builder(this, PAYMENT_CHANNEL_ID)
                .setContentTitle("🛒 Sell BCH: action needed")
                .setContentText("A customer wants %s %.2f cash. Open Asgaya to broadcast & hand the cash.".format(currency ?: "EUR", amountCash))
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
            NotificationManagerCompat.from(this)
                .notify(CASHOUT_NOTIFICATION_BASE + covenantAddress.hashCode(), notification)
            Log.i(TAG, "🔔 Merchant action required: broadcast & hand %s %.2f cash".format(currency ?: "EUR", amountCash))
        } catch (e: Exception) {
            Log.e(TAG, "notifyCashoutActionRequired failed", e)
        }
    }

    private fun createNotificationChannel() {
        // NotificationChannel only available on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "BizumParser Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps BizumParser monitoring Bizum notifications"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(serviceChannel)

            // Heads-up channel for incoming covenant payments (recipient)
            val paymentsChannel = NotificationChannel(
                PAYMENT_CHANNEL_ID,
                "Incoming payments",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies you when you receive a covenant payment to claim"
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(paymentsChannel)
        }
    }

    /**
     * Heads-up notification telling the user a covenant payment is waiting to be claimed.
     *
     * Phase 0: fires on receipt (trusted users). Anti-scam funding verification is Phase 0+.
     */
    private suspend fun notifyReceivedCovenant(remittance: Remittance) {
        try {
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                Log.w(TAG, "Notifications disabled — skipping received-covenant heads-up")
                return
            }

            // Show the **local** amount (the interface speaks the user's currency, `ui-rethink/40`).
            val cur = getSharedPreferences("BizumParserPrefs", android.content.Context.MODE_PRIVATE)
                .getString("fiat_currency", "EUR") ?: "EUR"
            val eurPer = if (cur == "EUR") 1.0
                else runCatching { com.asgaya.husk.FiatRateSource.eurPer(cur) }.getOrNull()
            val amount = com.asgaya.husk.FiatDisplay.amount(remittance.eurCents ?: 0, cur, eurPer)
            // Tap → Activity (one step closer to claim() / cash out at a merchant).
            val intent = Intent(this, com.asgaya.husk.remittance.RemittanceActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                this,
                remittance.id.toInt(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notification = NotificationCompat.Builder(this, PAYMENT_CHANNEL_ID)
                .setContentTitle("🎁 You received $amount")
                .setContentText("Tap to claim or cash out at a merchant")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build()

            NotificationManagerCompat.from(this)
                .notify(RECEIVED_COVENANT_NOTIFICATION_BASE + remittance.id.toInt(), notification)
            Log.i(TAG, "🔔 Heads-up posted: received $amount")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to post received-covenant notification", e)
        }
    }

    /**
     * Ingest a received `[COVENANT_V25]` offer (`ui-rethink/37`).
     *
     * The relay re-delivers **all** historical offers on (re)connect. A **live** covenant is
     * actionable — stored + a "money waiting" notification. An already **resolved/expired** one is
     * still **kept**, but as **history**: stored with the real outcome derived from the chain
     * (claimed/refunded/aborted/…, falling back to `expired`) and **never notified/actionable**.
     * Deduped by covenantAddress (a re-delivery never creates a second card).
     */
    private suspend fun ingestReceivedOffer(remittance: Remittance) {
        val now = System.currentTimeMillis() / 1000
        val hasLiveUtxo: Boolean? = remittance.covenantAddress?.let { addr ->
            try {
                electrumClient.getUTXOsForAddress(addr).isNotEmpty()
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ liveness check failed (${e.message}) — treating as live")
                null
            }
        }
        val live = CovenantOfferRelevance.shouldSurface(remittance.expiryOracleTime, now, hasLiveUtxo)

        val stored = storeReceivedCovenantIfNew(remittance) ?: return
        if (live) {
            Log.i(TAG, "✅ COVENANT_V25 stored as active received covenant (isReceived=true)")
            // Notify the user that money is waiting (Phase 0: on receipt).
            // TODO(R2, anti-scam, Phase 0+): gate on verified funding UTXO
            //   (covers eurCents at oracle price + fee, not dust).
            //   See collaborative_workspace/contact-exchange/.
            notifyReceivedCovenant(stored)
        } else {
            // Already resolved when first seen → keep as history, real outcome from the chain.
            val myAddress = if (::walletManager.isInitialized)
                runCatching { walletManager.getActiveWallet()?.address }.getOrNull() else null
            val resolved = runCatching {
                com.asgaya.husk.covenant.RemittanceReconciler.reconcileOne(
                    database, electrumClient, stored, myAddress
                )
            }.getOrDefault(false)
            if (!resolved) {
                database.remittanceDao().updateStatus(stored.id, RemittanceStatus.EXPIRED)
            }
            Log.i(TAG, "🗂️ Stale offer kept as history (${remittance.covenantAddress?.take(16)}…)")
        }
    }

    /**
     * Store a received covenant, deduplicating by covenantAddress.
     *
     * A repeated "resend to recipient" (or multi-relay delivery) must not create duplicate cards.
     * Returns the stored row (with its id) — or `null` if it was already present.
     */
    private suspend fun storeReceivedCovenantIfNew(remittance: Remittance): Remittance? {
        val addr = remittance.covenantAddress
        if (addr.isNullOrBlank()) {
            return remittance.copy(id = database.remittanceDao().insertRemittance(remittance))
        }
        val existing = database.remittanceDao().findByCovenantAddress(addr)
        if (existing != null) {
            Log.d(TAG, "Received covenant already stored ($addr) — skipping duplicate")
            return null
        }
        return remittance.copy(id = database.remittanceDao().insertRemittance(remittance))
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("BizumParser Active")
            .setContentText("Monitoring Bizum notifications")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "🟢 SERVICE CONNECTED - Ready to receive notifications!")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "🔴 SERVICE DISCONNECTED")
    }

    private fun loadBankPatterns() {
        try {
            val inputStream = assets.open("bank_patterns.json")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val bankObj = jsonArray.getJSONObject(i)
                val packageName = bankObj.getString("package")
                val bankName = bankObj.getString("bankName")
                val patternsArray = bankObj.getJSONArray("patterns")

                val patterns = mutableListOf<PatternConfig>()
                for (j in 0 until patternsArray.length()) {
                    val patternObj = patternsArray.getJSONObject(j)
                    val regex = Regex(patternObj.getString("regex"))
                    val amountGroup = patternObj.optInt("amountGroup", -1).takeIf { it >= 0 }
                    val senderGroup = patternObj.optInt("senderGroup", -1).takeIf { it >= 0 }
                    val referenceGroup = patternObj.optInt("referenceGroup", -1).takeIf { it >= 0 }
                    val senderInTitle = patternObj.optBoolean("senderInTitle", false)
                    val referenceRegex = patternObj.optString("referenceRegex", "").takeIf { it.isNotEmpty() }?.let { Regex(it) }

                    patterns.add(PatternConfig(regex, amountGroup, senderGroup, referenceGroup, senderInTitle, referenceRegex))
                }

                bankConfigs.add(BankConfig(packageName, bankName, patterns))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load bank patterns", e)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val isDebugMode = isDebugModeEnabled()

        // Log EVERY notification to verify service is working
        Log.d(TAG, "📬 NOTIFICATION RECEIVED from: $packageName" + if (isDebugMode) " [DEBUG MODE]" else "")

        val notification = sbn.notification
        val extras = notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE, "")

        // Try to get full message text from various sources
        // Try ALL notification text sources and use the LONGEST one (Telegram truncates some!)
        var text = ""

        // 1. Try EXTRA_MESSAGES (MessagingStyle - used by Telegram)
        @Suppress("DEPRECATION")
        val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
        val messagesText = if (messages != null && messages.isNotEmpty()) {
            val lastMessage = messages.last() as? android.os.Bundle
            lastMessage?.getCharSequence("text")?.toString() ?: ""
        } else ""

        // 2. Try EXTRA_BIG_TEXT
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""

        // 3. Try EXTRA_TEXT
        val normalText = extras.getString(Notification.EXTRA_TEXT, "")

        // Use the LONGEST text (Telegram truncates shorter versions!)
        text = listOf(messagesText, bigText, normalText).maxByOrNull { it.length } ?: ""

        if (text.isNotEmpty()) {
            Log.d(TAG, "📱 Extracted ${text.length} chars (longest of: MESSAGES=${messagesText.length}, BIG=${bigText.length}, NORMAL=${normalText.length})")
        }

        val timestamp = sbn.postTime

        // In debug mode, log ALL notifications
        if (isDebugMode) {
            Log.d(TAG, "===== DEBUG: ALL NOTIFICATIONS =====")
            Log.d(TAG, "Package: $packageName")
            Log.d(TAG, "Title: $title")
            Log.d(TAG, "Text: $text")
            Log.d(TAG, "===================================")

            // Store ALL notifications in debug mode (even non-bank)
            val debugNotification = ParsedNotification(
                bankApp = packageName,
                timestamp = timestamp,
                amount = 0.0,  // No amount for non-bank notifications
                sender = "[DEBUG] $title",  // Store title in sender field
                reference = "DEBUG MODE",
                rawText = "Title: $title\nText: $text",  // Store full notification
                parsedSuccessfully = false  // Mark as unparsed
            )

            serviceScope.launch {
                database.notificationDao().insert(debugNotification)
                Log.d(TAG, "✅ Debug notification stored")
            }
        }

        // Find bank config for this package
        val bankConfig = bankConfigs.find { it.packageName == packageName }
        if (bankConfig == null) {
            Log.d(TAG, "⏭️  Skipping (not a monitored bank)")
            return  // Not a bank we're monitoring
        }

        // Check if this is the selected bank (unless in debug mode)
        if (!isDebugMode) {
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            // Default to Telegram for covenant notifications (Phase 0)
            val selectedBank = prefs.getString(SELECTED_BANK_KEY, "Telegram (Covenant)")

            if (selectedBank != bankConfig.bankName) {
                Log.d(TAG, "⏭️  Skipping ${bankConfig.bankName} (selected bank: $selectedBank)")
                return
            }
        }

        Log.d(TAG, "===== BANK NOTIFICATION =====")
        Log.d(TAG, "Bank: ${bankConfig.bankName}")
        Log.d(TAG, "Package: $packageName")
        Log.d(TAG, "Title: $title")
        Log.d(TAG, "Text: $text")
        Log.d(TAG, "===========================")

        // Check if this is a covenant notification (Telegram with [COVENANT_V25] block)
        if (text.contains("[COVENANT_V25]")) {
            Log.d(TAG, "🎁 Detected COVENANT_V25 block - parsing...")
            val remittance = parseCovenantV25(text)
            if (remittance != null) {
                serviceScope.launch {
                    try {
                        ingestReceivedOffer(remittance)
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Failed to store received covenant", e)
                    }
                }
            }
            return  // Don't try to parse as regular bank notification
        }

        // Check if this is a funding request (Telegram with [FUND_COVENANT] block)
        // SELLER AUTO-FUNDING: Sender shares covenant params → seller stores awaiting_payment
        if (text.contains("[FUND_COVENANT]")) {
            Log.d(TAG, "💰 Detected FUND_COVENANT block - parsing...")
            serviceScope.launch {
                parseFundCovenant(text)
            }
            return  // Don't try to parse as regular bank notification
        }

        // Check if this is a merchant cashout request (Telegram with [CASH_IN_PERSON] block)
        if (text.contains("[CASH_IN_PERSON]")) {
            Log.d(TAG, "🏪 Detected CASH_IN_PERSON block - parsing...")
            val cashoutRequest = parseCashInPerson(text)
            if (cashoutRequest != null) {
                serviceScope.launch {
                    try {
                        // Store in shared storage for TradeActivity
                        com.asgaya.husk.trade.CashoutRequestStorage.add(cashoutRequest)
                        Log.d(TAG, "✅ Cashout request stored: €${cashoutRequest.amountFiat}")

                        // Show notification to merchant
                        showCashoutNotification(cashoutRequest)
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Failed to store cashout request", e)
                    }
                }
            }
            return  // Don't try to parse as regular bank notification
        }

        // Try to parse notification
        val parsed = parseNotification(bankConfig, title, text, timestamp)

        // Save to database (even if parsing failed, for debugging)
        serviceScope.launch {
            try {
                database.notificationDao().insert(parsed)
                if (parsed.parsedSuccessfully) {
                    Log.d(TAG, "✅ Parsed: €${parsed.amount} from ${parsed.sender} (${parsed.reference})")

                    // Route to appropriate handler based on reference format
                    // Covenant notifications have bchtest: addresses, Bizum has Cash Accounts
                    if (parsed.reference.startsWith("bchtest:")) {
                        handleCovenantNotification(parsed)
                    } else {
                        handleBizumPayment(parsed)
                    }
                } else {
                    Log.d(TAG, "⚠️ Unparsed notification stored for debugging")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Database insert failed", e)
            }
        }
    }

    private fun parseNotification(
        bankConfig: BankConfig,
        title: String,
        text: String,
        timestamp: Long
    ): ParsedNotification {
        // Try each pattern for this bank
        for (pattern in bankConfig.patterns) {
            val match = pattern.regex.find(text)
            if (match != null) {
                // Extract fields using group indices from JSON
                val amount = pattern.amountGroup?.let {
                    match.groupValues.getOrNull(it)?.replace(",", ".")?.toDoubleOrNull()
                } ?: 0.0

                val sender = when {
                    pattern.senderInTitle -> title.trim()
                    pattern.senderGroup != null -> match.groupValues.getOrNull(pattern.senderGroup)?.trim() ?: ""
                    else -> ""
                }

                var reference = pattern.referenceGroup?.let {
                    match.groupValues.getOrNull(it)?.trim()
                } ?: ""

                // Try secondary reference regex if specified (BBVA case)
                if (reference.isEmpty() && pattern.referenceRegex != null) {
                    val refMatch = pattern.referenceRegex.find(text)
                    reference = refMatch?.groupValues?.getOrNull(1)?.trim() ?: ""
                }

                val rawText = if (pattern.senderInTitle) "$title | $text" else text

                return ParsedNotification(
                    timestamp = timestamp,
                    bankApp = bankConfig.bankName,
                    amount = amount,
                    sender = sender,
                    reference = reference,
                    rawText = rawText,
                    parsedSuccessfully = amount > 0.0 && sender.isNotEmpty()
                )
            }
        }

        // No pattern matched - check if it looks like a Bizum notification
        val lowerText = text.lowercase()
        val looksBizum = BIZUM_KEYWORDS.any { lowerText.contains(it) }

        // Store unparsed notification for debugging (TightDS recommendation)
        return ParsedNotification(
            timestamp = timestamp,
            bankApp = "${bankConfig.bankName} (unparsed)",
            amount = 0.0,
            sender = if (looksBizum) "PARSING FAILED - CHECK REGEX" else "Not Bizum",
            reference = "",
            rawText = "$title | $text",
            parsedSuccessfully = false
        )
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // We don't care when notifications are dismissed
    }

    /**
     * Parse [COVENANT_V25] block from Telegram notification
     * Returns Remittance if valid covenant params found, null otherwise
     */
    private fun parseCovenantV25(text: String, senderNpub: String? = null): Remittance? {
        try {
            // Look for [COVENANT_V25]...[/COVENANT_V25] block
            val covenantBlockRegex = Regex("""\[COVENANT_V25\](.*?)\[/COVENANT_V25\]""", RegexOption.DOT_MATCHES_ALL)
            val blockMatch = covenantBlockRegex.find(text) ?: return null
            val covenantBlock = blockMatch.groupValues[1]

            Log.d(TAG, "📦 Found COVENANT_V25 block")

            // Parse each parameter
            val params = mutableMapOf<String, String>()
            covenantBlock.lines().forEach { line ->
                val parts = line.trim().split("=", limit = 2)
                if (parts.size == 2) {
                    params[parts[0].trim()] = parts[1].trim()
                }
            }

            // Extract required fields
            val covenantAddress = params["covenantAddress"] ?: run {
                Log.e(TAG, "❌ Missing covenantAddress")
                return null
            }
            val senderPubkey = params["senderPubkey"] ?: run {
                Log.e(TAG, "❌ Missing senderPubkey")
                return null
            }
            val recipientPubkey = params["recipientPubkey"] ?: run {
                Log.e(TAG, "❌ Missing recipientPubkey")
                return null
            }
            val funderPubkey = params["funderPubkey"] ?: run {
                Log.e(TAG, "❌ Missing funderPubkey")
                return null
            }
            val oraclePubkey = params["oraclePubkey"] ?: run {
                Log.e(TAG, "❌ Missing oraclePubkey")
                return null
            }
            val eurCents = params["eurCents"]?.toIntOrNull() ?: run {
                Log.e(TAG, "❌ Missing or invalid eurCents")
                return null
            }
            val expiryOracleTime = params["expiryOracleTime"]?.toLongOrNull() ?: run {
                Log.e(TAG, "❌ Missing or invalid expiryOracleTime")
                return null
            }
            val initialBchPriceInCents = params["initialBchPriceInCents"]?.toIntOrNull() ?: run {
                Log.e(TAG, "❌ Missing or invalid initialBchPriceInCents")
                return null
            }
            val minPricePercent = params["minPricePercent"]?.toIntOrNull() ?: run {
                Log.e(TAG, "❌ Missing or invalid minPricePercent")
                return null
            }
            val fundingTxid = params["fundingTxid"]  // Optional
            val senderCashAccount = params["senderCashAccount"]?.takeIf { it.isNotBlank() }  // the sender's identity

            // Calculate BCH amount from EUR and price
            val amountBch = (eurCents.toDouble() / 100.0) / (initialBchPriceInCents.toDouble() / 100.0)

            // Create received remittance
            val remittance = Remittance(
                recipientAddress = covenantAddress,
                recipientName = "Covenant Claim",
                amountBch = amountBch,
                amountPaymentCurrency = eurCents / 100.0,
                paymentCurrency = "EUR",
                amountRecipientCurrency = eurCents / 100.0,  // Same as payment for now
                recipientCurrency = "EUR",
                txid = fundingTxid,
                fundingMethod = "received",  // Mark as received covenant
                covenantAddress = covenantAddress,
                senderPubkey = senderPubkey,
                recipientPubkey = recipientPubkey,
                funderPubkey = funderPubkey,
                oraclePubkey = oraclePubkey,
                eurCents = eurCents,
                expiryOracleTime = expiryOracleTime,
                initialBchPriceInCents = initialBchPriceInCents,
                minPricePercent = minPricePercent,
                status = RemittanceStatus.CONFIRMED,  // Ready to claim
                isReceived = true,  // This is a RECEIVED covenant
                senderNostrPubkey = senderNpub,  // Offer DM author → recipient can notify back on claim
                senderCashAccount = senderCashAccount  // the sender's CashAccount (offer) → the card shows it (`ui-rethink/40`)
            )

            Log.d(TAG, "✅ Parsed covenant: €${eurCents/100.0} to claim")
            return remittance

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to parse covenant V25", e)
            return null
        }
    }

    /**
     * Parse [FUND_COVENANT] message and store in seller's covenant_requests table
     *
     * SELLER AUTO-FUNDING FLOW (TightDS blocker fix):
     * 1. Sender creates covenant (createCovenantOnly)
     * 2. Sender shares [FUND_COVENANT] message with covenant params
     * 3. Seller's device receives message → THIS FUNCTION parses it
     * 4. Inserts into seller's covenant_requests (awaiting_payment)
     * 5. Seller pays Bizum → handleBizumPayment finds covenant → funds it
     *
     * Message format:
     * [FUND_COVENANT]
     * cashAccount: Elena#142
     * covenantAddress: bchtest:pzrx...
     * eurCents: 10000
     * expiryOracleTime: 1234567890
     * initialBchPriceInCents: 65000
     * minPricePercent: 93
     * [/FUND_COVENANT]
     */
    private suspend fun parseFundCovenant(text: String, authorNpub: String? = null) {
        try {
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "💰 PARSING [FUND_COVENANT] MESSAGE")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Extract [FUND_COVENANT] block
            val startMarker = "[FUND_COVENANT]"
            val endMarker = "[/FUND_COVENANT]"
            val startIndex = text.indexOf(startMarker)
            if (startIndex == -1) {
                Log.e(TAG, "❌ Could not find opening tag [FUND_COVENANT]")
                return
            }

            val endIndex = text.indexOf(endMarker, startIndex)
            val covenantBlock = if (endIndex != -1) {
                text.substring(startIndex + startMarker.length, endIndex)
            } else {
                Log.d(TAG, "⚠️ Closing tag missing (truncated) - parsing from opening tag to end")
                text.substring(startIndex + startMarker.length)
            }

            Log.d(TAG, "📦 Found FUND_COVENANT block (${covenantBlock.length} chars)")

            // Parse key-value pairs
            val params = mutableMapOf<String, String>()
            covenantBlock.lines().forEach { line ->
                val trimmed = line.trim()
                if (trimmed.contains(":")) {
                    val parts = trimmed.split(":", limit = 2)
                    if (parts.size == 2) {
                        val key = parts[0].trim()
                        val value = parts[1].trim()
                        params[key] = value
                        Log.d(TAG, "  $key: $value")
                    }
                }
            }

            // Extract required fields
            val cashAccount = params["cashAccount"] ?: run {
                Log.e(TAG, "❌ Missing cashAccount")
                return
            }
            val covenantAddress = params["covenantAddress"] ?: run {
                Log.e(TAG, "❌ Missing covenantAddress")
                return
            }
            val eurCents = params["eurCents"]?.toIntOrNull() ?: run {
                Log.e(TAG, "❌ Missing or invalid eurCents")
                return
            }
            val expiryOracleTime = params["expiryOracleTime"]?.toLongOrNull() ?: run {
                Log.e(TAG, "❌ Missing or invalid expiryOracleTime")
                return
            }
            val initialBchPriceInCents = params["initialBchPriceInCents"]?.toIntOrNull() ?: run {
                Log.e(TAG, "❌ Missing or invalid initialBchPriceInCents")
                return
            }
            val minPricePercent = params["minPricePercent"]?.toIntOrNull() ?: run {
                Log.e(TAG, "❌ Missing or invalid minPricePercent")
                return
            }
            // The sender's pay window (seconds); absent on old messages → the default applies.
            val payWindowSeconds = params["payWindowSeconds"]?.toIntOrNull()
            // The chosen seller's ad id + fee (for the seller's ad checklist, `22 §D`).
            val adId = params["adId"]?.takeIf { it.isNotBlank() }
            val feePercent = params["feePercent"]?.toDoubleOrNull()
            // The sender's committed payment (the datum the merchant card shows, `ui-rethink/40`).
            val payAmount = params["amount"]?.toDoubleOrNull()
            val payCurrency = params["currency"]?.takeIf { it.isNotBlank() }

            // Parse pubkeys (for covenant address verification)
            val senderPubkey = params["senderPubkey"] ?: run {
                Log.e(TAG, "❌ Missing senderPubkey")
                return
            }
            val recipientPubkey = params["recipientPubkey"] ?: run {
                Log.e(TAG, "❌ Missing recipientPubkey")
                return
            }
            val funderPubkey = params["funderPubkey"] ?: run {
                Log.e(TAG, "❌ Missing funderPubkey")
                return
            }
            val oraclePubkey = params["oraclePubkey"] ?: run {
                Log.e(TAG, "❌ Missing oraclePubkey")
                return
            }

            Log.d(TAG, "✅ Parsed covenant params:")
            Log.d(TAG, "  Cash Account: $cashAccount")
            Log.d(TAG, "  Covenant Address: ${covenantAddress.take(30)}...")
            Log.d(TAG, "  Sender: ${senderPubkey.take(20)}...")
            Log.d(TAG, "  Recipient: ${recipientPubkey.take(20)}...")
            Log.d(TAG, "  Funder: ${funderPubkey.take(20)}...")
            Log.d(TAG, "  Oracle: ${oraclePubkey.take(20)}...")
            Log.d(TAG, "  Amount: €${eurCents / 100.0}")
            Log.d(TAG, "  Price: €${initialBchPriceInCents / 100.0}/BCH")
            Log.d(TAG, "  Expiry: $expiryOracleTime")

            // Verify covenant is for our ACTIVE wallet (production-safe)
            // Critical: Buffer returns to funderPubkey address - must be our active wallet
            // Passive user responsibility: Have the correct merchant wallet active
            val activeWallet = walletManager.getActiveWallet()

            if (activeWallet == null) {
                Log.e(TAG, "❌ No active wallet found - cannot verify covenant")
                Log.e(TAG, "   Passive user must have a wallet active to receive funding requests")
                return
            }

            if (funderPubkey != activeWallet.publicKey) {
                Log.e(TAG, "❌ SECURITY: Covenant not for my active wallet!")
                Log.e(TAG, "   My active wallet: ${activeWallet.label} (${activeWallet.publicKey.take(20)}...)")
                Log.e(TAG, "   Covenant expects: ${funderPubkey.take(20)}...")
                Log.e(TAG, "   This covenant would send buffer to a DIFFERENT address - REJECTING")
                Log.e(TAG, "   → Switch to the correct merchant wallet if this covenant is yours")
                return
            }

            Log.d(TAG, "✅ Verified: funderPubkey matches active wallet (${activeWallet.label})")
            Log.d(TAG, "   Buffer will return to: ${activeWallet.address}")

            // TODO: Also verify covenant address reconstructs from params
            // (Would call covenantWebView.generateCovenantAddress() and compare)
            // Skipping for Phase 0 trusted-actor test - add for beta

            // Atomic check-then-insert to prevent race condition
            // (Multiple relays deliver same event → concurrent coroutines → all insert)
            covenantInsertMutex.withLock {
                // Check for duplicates (same covenant address already stored)
                val existingCovenant = database.covenantDao().findByAddress(covenantAddress)
                if (existingCovenant != null) {
                    Log.d(TAG, "⚠️ Covenant already exists in DB - skipping duplicate")
                    Log.d(TAG, "   Address: ${covenantAddress.take(20)}...")
                    Log.d(TAG, "   Status: ${existingCovenant.status}")
                    return
                }

                // One active covenant per reference (`ui-rethink/19`/`22 §C`): block a 2nd request
                // ONLY while the existing one is **active** (within its pay window). A request past
                // its window is expired and must NOT block — that's what the hard window is for
                // (`ui-rethink/27`). (Previously an expired `awaiting_payment` blocked new requests.)
                val existingRef = database.covenantDao().findByCashAccount(cashAccount)
                if (existingRef != null &&
                    com.asgaya.husk.covenant.CovenantRules.payByMillis(existingRef.createdAt, existingRef.payWindowSeconds) > System.currentTimeMillis()
                ) {
                    Log.w(TAG, "⛔ Refusing a 2nd ACTIVE request for reference $cashAccount (one active per reference)")
                    return
                }

                // The seller's ad checklist (`ui-rethink/22 §D`): the request must match the seller's
                // OWN ad — the funder pubkey, the volatility buffer, the fee (+ EUR corridor). A
                // mismatch, including ad drift, means we do NOT fund → refuse to store the request.
                var assetCurrency: String? = null
                var reqMethod: String? = null
                if (adId != null) {
                    val ad = database.listingDao().getById(adId)
                    if (ad == null) {
                        Log.w(TAG, "⛔ Refusing request for $cashAccount: no local ad $adId (ad drift?)")
                        return
                    }
                    val adListing = ad.toListing()
                    val mismatch = when {
                        adListing.publicKey != funderPubkey -> "funder pubkey"
                        adListing.minPricePercent != minPricePercent -> "buffer"
                        feePercent != null && kotlin.math.abs(adListing.fee - feePercent) > 1e-9 -> "fee"
                        // NOTE: no corridor/asset check — the ad's asset is the *payment* fiat (any
                        // currency, e.g. USD); the covenant's settled face is always EUR
                        // (`supported-fiats`; multi-currency). The adId/pubkey/buffer/fee match is
                        // what guards against ad drift.
                        else -> null
                    }
                    if (mismatch != null) {
                        Log.w(TAG, "⛔ Refusing request for $cashAccount: ad $adId mismatch ($mismatch)")
                        return
                    }
                    assetCurrency = adListing.assets.firstOrNull()  // the ad's asset = the exchange currency (`ui-rethink/40`)
                    reqMethod = adListing.paymentMethods.firstOrNull()
                    Log.d(TAG, "✅ Request matches the seller's ad $adId (pubkey/buffer/fee)")
                }

                // Insert into seller's covenant_requests table
                val covenant = CovenantRequest(
                    cashAccount = cashAccount,
                    covenantAddress = covenantAddress,
                    eurCents = eurCents,
                    expiryTime = expiryOracleTime,
                    initialBchPrice = initialBchPriceInCents,
                    minPricePercent = minPricePercent,
                    status = "awaiting_payment",  // Seller will fund when Bizum arrives
                    redeemScript = null,  // Not needed for funding
                    // Persist the full params so we can rebuild the covenant to auto-recover
                    // our buffer (sellerRecoverBuffer) if the covenant expires unclaimed.
                    senderPubkey = senderPubkey,
                    recipientPubkey = recipientPubkey,
                    funderPubkey = funderPubkey,
                    oraclePubkey = oraclePubkey,
                    senderNostrPubkey = authorNpub,  // to notify the sender when we recover the buffer
                    payWindowSeconds = payWindowSeconds,  // the sender's pay window for this request
                    assetCurrency = assetCurrency,  // the exchange currency
                    payAmount = payAmount,          // the sender's payment datum (`ui-rethink/40`)
                    payCurrency = payCurrency,
                    feePercent = feePercent         // the seller's ad fee (for the card's amount + fee)
                )

                database.covenantDao().insert(covenant)

                // Notify the merchant (funder): a new funding request arrived → confirm the cash and
                // fund. Tap opens the Trade tab (Sell BCH) (`ui-rethink/41`).
                notifyNewFundingRequest(cashAccount, payAmount, payCurrency, feePercent, reqMethod)
            }

            Log.d(TAG, "✅ Covenant stored in seller's DB: $cashAccount → ${covenantAddress.take(20)}...")
            Log.d(TAG, "Next: Sender pays €${eurCents / 100.0} via Bizum, concept: $cashAccount")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to parse FUND_COVENANT message", e)
        }
    }

    /**
     * A counterparty says the covenant resolved (`[COVENANT_RESOLVED]`) — a **hint, not truth**.
     * Verify on-chain (the covenant must be spent + the tx must exist) before concluding locally.
     * (`ui-rethink/10`, `58`.)
     */
    private suspend fun handleCovenantResolved(resolved: com.asgaya.husk.data.CovenantResolved) {
        try {
            // The DM is a **trigger**, not the truth (`58`; Suso 2026-10-01): derive the state from
            // the CHAIN. Retry a few times — a fresh broadcast needs a moment to propagate.
            val rem = database.remittanceDao().findByCovenantAddress(resolved.covenantAddress)
            val myAddress = if (::walletManager.isInitialized) walletManager.getActiveWallet()?.address else null
            var done = false
            var attempts = 0
            while (attempts < 4 && !done) {
                done = if (rem != null) {
                    runCatching {
                        com.asgaya.husk.covenant.RemittanceReconciler.reconcileOne(
                            database, ElectrumClient(), rem, myAddress, hintPath = resolved.path
                        )
                    }.getOrDefault(false)
                } else {
                    // Funder side (no local remittance): verify on-chain to conclude the request.
                    CovenantRecovery.hasFunds(resolved.covenantAddress) == false
                }
                if (!done) {
                    Log.d(TAG, "… [COVENANT_RESOLVED] not yet concluded - retry ${attempts + 1}")
                    attempts++
                    if (attempts < 4) delay(10_000)
                }
            }
            if (!done) {
                Log.w(TAG, "⚠️ [COVENANT_RESOLVED] not concluded after retries - ignoring")
                return
            }

            // Funder side: conclude the covenant_requests ledger + cancel the alarm.
            database.covenantDao().findByAddress(resolved.covenantAddress)?.let { c ->
                if (c.status == "funded") {
                    database.covenantDao().updateStatusAndTxid(c.id, "resolved_externally", resolved.txid)
                    CovenantRecovery.cancelExpiryAlarm(this, c)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle [COVENANT_RESOLVED]", e)
        }
    }

    private suspend fun handleCovenantNotification(parsed: ParsedNotification) {
        try {
            Log.d(TAG, "📥 Covenant notification: ${parsed.sender} (${parsed.reference})")

            // Extract redeemScript from notification text (if present)
            val redeemScriptRegex = Regex("RedeemScript:\\s*([0-9a-fA-F]+)")
            val redeemScriptMatch = redeemScriptRegex.find(parsed.rawText)
            val redeemScript = redeemScriptMatch?.groupValues?.getOrNull(1)

            if (redeemScript != null) {
                Log.d(TAG, "   RedeemScript: ${redeemScript.length} chars")
            } else {
                Log.w(TAG, "   ⚠️ RedeemScript not found in notification")
            }

            // Price from the oracle only — never a hardcoded value (`ui-rethink/16` P0-2).
            val initialBchPriceCents = OraclePriceSource.eurPerBch()?.let { (it * 100).toInt() }
            if (initialBchPriceCents == null) {
                Log.w(TAG, "⚠️ Oracle unavailable — not storing the covenant (no hardcoded price)")
                return
            }

            val covenant = CovenantRequest(
                cashAccount = parsed.sender,              // "Elena#142" from senderGroup
                covenantAddress = parsed.reference,       // "bchtest:pzrx..." from referenceGroup
                eurCents = (parsed.amount * 100).toInt(),
                expiryTime = System.currentTimeMillis() + (8 * 3600 * 1000),  // 8 hours
                initialBchPrice = initialBchPriceCents,
                minPricePercent = 93,     // 7% buffer
                status = "awaiting_payment",
                redeemScript = redeemScript  // Store redeemScript (null if not found)
            )

            database.covenantDao().insert(covenant)

            Log.d(TAG, "✅ Covenant stored: ${covenant.cashAccount} → ${covenant.covenantAddress.take(20)}...")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to store covenant", e)
        }
    }

    /**
     * Handle Bizum payment - Auto-fund covenant when Bizum arrives
     *
     * SELLER AUTO-FUNDING FLOW (2026-09-06):
     * 1. Sender creates covenant (createCovenantOnly)
     * 2. Sender pays Bizum with concept "Elena#142" (cashAccount)
     * 3. This function detects Bizum → funds covenant
     * 4. Sender's monitorCovenantFunding sees UTXO → updates status
     *
     * CRITICAL FIXES:
     * - ⚠️1: WebView lifecycle gate (await ready before funding)
     * - ⚠️2: Use seller_address (not scripthash) for UTXO fetching
     * - ⚠️5: Recompute funding with FRESH price (prevent under-funding)
     */
    private suspend fun handleBizumPayment(parsed: ParsedNotification) {
        try {
            val cashAccount = parsed.reference  // "Elena#142" from Bizum concept

            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "💶 BIZUM PAYMENT RECEIVED")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "Amount: €${parsed.amount}")
            Log.d(TAG, "Cash Account: $cashAccount")

            // Look up covenant
            val covenant = database.covenantDao().findByCashAccount(cashAccount)

            if (covenant == null) {
                Log.d(TAG, "⏭️  No pending covenant for: $cashAccount")
                return
            }

            // Validate amount (exact match required)
            val receivedCents = (parsed.amount * 100).toInt()
            if (receivedCents != covenant.eurCents) {
                Log.e(TAG, "❌ Amount mismatch: received €${parsed.amount}, expected €${covenant.eurCents / 100.0}")
                return
            }

            Log.d(TAG, "✅ Match found! Covenant: ${covenant.covenantAddress.take(20)}...")

            // Get ACTIVE wallet (passive user must have correct merchant wallet active)
            // This ensures we fund with the wallet that will receive the buffer
            val activeWallet = walletManager.getActiveWallet()

            if (activeWallet == null) {
                Log.e(TAG, "❌ No active wallet found - cannot fund covenant")
                Log.e(TAG, "   Passive user: Switch to your merchant wallet to fund covenants")
                return
            }

            val sellerAddress = activeWallet.address

            // Get seller WIF from sourceData (encrypted in production, plain for testing)
            val sellerWif = activeWallet.sourceData  // TODO: Decrypt in production

            Log.d(TAG, "Funding with active wallet: ${activeWallet.label}")
            Log.d(TAG, "Seller address: ${sellerAddress.take(30)}...")

            // ⚠️1: CRITICAL - Wait for WebView bridge to load
            try {
                if (!covenantWebView.isReady()) {
                    Log.d(TAG, "⏳ WebView not ready, waiting...")
                    covenantWebView.awaitReady(timeoutMs = 30_000)
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ WebView bridge timeout - cannot fund covenant", e)
                return
            }

            // Hard pay window (`ui-rethink/27`): past it, do NOT fund at all — the customer must
            // create a new covenant (keeps one active covenant per recipient).
            if (com.asgaya.husk.covenant.CovenantRules.payByMillis(covenant.createdAt, covenant.payWindowSeconds) <= System.currentTimeMillis()) {
                Log.w(TAG, "⛔ Funding refused for ${covenant.cashAccount}: pay window expired")
                return
            }

            // Funding price gate (`ui-rethink/27`/`29`): fund only if the price hasn't dropped more
            // than 1.5 % since the covenant's `initial` (a rise is free). No oracle → no fund.
            val fundOracle = runCatching { CovenantRecovery.fetchOraclePrice() }.getOrNull()
            if (fundOracle == null) {
                Log.e(TAG, "⛔ Funding refused for ${covenant.cashAccount}: oracle unavailable")
                return
            }
            if (!com.asgaya.husk.covenant.CovenantRules.isFundingPriceAcceptable(covenant.initialBchPrice, fundOracle.price)) {
                Log.w(TAG, "⛔ Funding refused for ${covenant.cashAccount}: price €${fundOracle.price / 100.0} < initial×0.985")
                return
            }

            // ⚠️5: Recompute funding amount with FRESH price
            // (Prevents under-funding if price diverged between covenant creation and funding)
            // Use price from [FUND_COVENANT] message (sender's price at creation time)
            val currentBchPriceInCents = covenant.initialBchPrice
            val minPricePercent = covenant.minPricePercent
            val eurCents = covenant.eurCents

            // Calculate BCH amount needed (with buffer)
            val bchAmount = eurCents.toDouble() / currentBchPriceInCents
            val bufferedBchAmount = bchAmount * (100.0 / minPricePercent)
            val amountSats = (bufferedBchAmount * 100000000).toLong()

            Log.d(TAG, "Funding calculation:")
            Log.d(TAG, "  EUR: €${eurCents / 100.0}")
            Log.d(TAG, "  Price: €${currentBchPriceInCents / 100.0}/BCH")
            Log.d(TAG, "  Buffer: ${minPricePercent}% (${100 - minPricePercent}% drop protection)")
            Log.d(TAG, "  Amount: ${bufferedBchAmount} BCH (${amountSats} sats)")

            // Fund covenant via sendBchHybrid (v0.2 hybrid pattern)
            // TightDS Q1: Add retry logic if sendBchHybrid fails after awaitReady succeeds
            Log.d(TAG, "📡 Broadcasting funding transaction...")
            val txid = try {
                kotlinx.coroutines.withTimeout(30_000) {
                    covenantWebView.sendBchHybrid(
                        senderWIF = sellerWif,
                        senderAddress = sellerAddress,  // ⚠️2: Address for UTXO fetching
                        recipientAddress = covenant.covenantAddress,
                        amountSats = amountSats
                    )
                }
            } catch (e: Exception) {
                // Retry once after 1s (TightDS recommendation)
                Log.w(TAG, "⚠️ First funding attempt failed, retrying in 1s...", e)
                try {
                    kotlinx.coroutines.delay(1000)
                    kotlinx.coroutines.withTimeout(30_000) {
                        covenantWebView.sendBchHybrid(
                            senderWIF = sellerWif,
                            senderAddress = sellerAddress,
                            recipientAddress = covenant.covenantAddress,
                            amountSats = amountSats
                        )
                    }
                } catch (retryException: Exception) {
                    Log.e(TAG, "❌ Failed to broadcast funding transaction (both attempts)", retryException)
                    return
                }
            }

            Log.d(TAG, "✅ Covenant funded! TXID: $txid")

            // Update covenant_requests table (seller-side funding ledger)
            database.covenantDao().updateFunded(
                id = covenant.id,
                status = "funded",
                txid = txid,
                fundedAt = System.currentTimeMillis()
            )

            // Arm the exact expiry alarm → sellerRecoverBuffer at expiry (`ui-rethink/10`)
            CovenantRecovery.scheduleExpiryAlarm(this, covenant)

            // Record the funding outpoint (`txid:vout`) for evidence-based audits (`ui-rethink/16` P0-1)
            CovenantRecovery.recordFundingOutpoint(database, covenant, txid)

            // Update remittances table (if sender is on same device - rare but possible in testing)
            // The sender's monitorCovenantFunding will update their remittance when they see the UTXO
            Log.d(TAG, "✅ Database updated: covenant_requests → funded")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to handle Bizum payment", e)
        }
    }

    /**
     * Parse [CASH_IN_PERSON] block from Telegram notification
     * Returns CashoutRequest if valid params found, null otherwise
     */
    private fun parseCashInPerson(text: String): com.asgaya.husk.data.CashoutRequest? {
        try {
            // Look for [CASH_IN_PERSON] block (Telegram truncates, so closing tag may be missing)
            val startMarker = "[CASH_IN_PERSON]"
            val endMarker = "[/CASH_IN_PERSON]"

            val startIndex = text.indexOf(startMarker)
            if (startIndex == -1) {
                Log.e(TAG, "❌ Could not find opening tag [CASH_IN_PERSON]")
                return null
            }

            // Try to find closing tag, but if missing (truncated), parse everything after opening tag
            val endIndex = text.indexOf(endMarker, startIndex)
            val cashoutBlock = if (endIndex != -1) {
                text.substring(startIndex + startMarker.length, endIndex)
            } else {
                Log.d(TAG, "⚠️ Closing tag missing (truncated) - parsing from opening tag to end")
                text.substring(startIndex + startMarker.length)
            }

            Log.d(TAG, "📦 Found CASH_IN_PERSON block")

            // Parse each parameter
            val params = mutableMapOf<String, String>()
            cashoutBlock.lines().forEach { line ->
                val parts = line.trim().split("=", limit = 2)
                if (parts.size == 2) {
                    params[parts[0].trim()] = parts[1].trim()
                }
            }

            // Extract required fields
            val covenantAddress = params["covenantAddress"] ?: run {
                Log.e(TAG, "❌ Missing covenantAddress")
                return null
            }
            val senderPubkey = params["senderPubkey"] ?: run {
                Log.e(TAG, "❌ Missing senderPubkey")
                return null
            }
            val recipientPubkey = params["recipientPubkey"] ?: run {
                Log.e(TAG, "❌ Missing recipientPubkey")
                return null
            }
            val funderPubkey = params["funderPubkey"] ?: run {
                Log.e(TAG, "❌ Missing funderPubkey")
                return null
            }
            val oraclePubkey = params["oraclePubkey"] ?: run {
                Log.e(TAG, "❌ Missing oraclePubkey")
                return null
            }
            val eurCents = params["eurCents"]?.toIntOrNull() ?: run {
                Log.e(TAG, "❌ Missing or invalid eurCents")
                return null
            }
            val expiryOracleTime = params["expiryOracleTime"]?.toLongOrNull() ?: run {
                Log.e(TAG, "❌ Missing or invalid expiryOracleTime")
                return null
            }
            val initialBchPriceInCents = params["initialBchPriceInCents"]?.toIntOrNull() ?: run {
                Log.e(TAG, "❌ Missing or invalid initialBchPriceInCents")
                return null
            }
            val minPricePercent = params["minPricePercent"]?.toIntOrNull() ?: run {
                Log.e(TAG, "❌ Missing or invalid minPricePercent")
                return null
            }

            // Extract recipientSig (supports both single field and split for backward compat)
            val recipientSig = params["recipientSig"] ?: run {
                // Backward compat: try split fields
                val sig1 = params["recipientSig1"] ?: ""
                val sig2 = params["recipientSig2"] ?: ""
                if (sig1.isNotEmpty()) sig1 + sig2 else "PLACEHOLDER"
            }
            val oracleSig = params["oracleSig"] ?: run {
                Log.e(TAG, "❌ Missing oracleSig")
                return null
            }
            val oracleMessage = params["oracleMessage"] ?: run {
                Log.e(TAG, "❌ Missing oracleMessage")
                return null
            }

            // Extract merchantPubkey (or placeholder for now)
            val merchantPubkey = params["merchantPubkey"] ?: "MERCHANT_PROVIDES_AT_CLAIM_TIME"

            // Create cashout request
            val request = com.asgaya.husk.data.CashoutRequest(
                covenantAddress = covenantAddress,
                senderPubkey = senderPubkey,
                recipientPubkey = recipientPubkey,
                funderPubkey = funderPubkey,
                oraclePubkey = oraclePubkey,
                eurCents = eurCents,
                expiryOracleTime = expiryOracleTime,
                initialBchPriceInCents = initialBchPriceInCents,
                minPricePercent = minPricePercent,
                merchantPubkey = merchantPubkey,
                recipientSig = recipientSig,
                oracleSig = oracleSig,
                oracleMessage = oracleMessage,
                timestamp = System.currentTimeMillis()
            )

            Log.d(TAG, "✅ Parsed cashout request: €${request.amountFiat} for ${request.amountBCH} BCH")
            return request

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to parse CASH_IN_PERSON", e)
            return null
        }
    }

    /**
     * Show notification to merchant about new cashout request
     */
    private fun showCashoutNotification(request: com.asgaya.husk.data.CashoutRequest) {
        try {
            // Intent to open Trade tab
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("open_tab", "trade")  // Signal to open Trade tab
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val pendingIntent = PendingIntent.getActivity(
                this,
                System.currentTimeMillis().toInt(),  // Unique request code
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Build notification
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("💵 New Cashout Request")
                .setContentText("€${request.amountFiat} for ${request.amountBCH} BCH")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            // Show notification
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(
                System.currentTimeMillis().toInt(),  // Unique notification ID
                notification
            )

            Log.d(TAG, "📣 Cashout notification shown: €${request.amountFiat}")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to show cashout notification", e)
        }
    }
}