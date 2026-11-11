package com.asgaya.husk.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asgaya.husk.AppDatabase
import com.asgaya.husk.CovenantWebView
import com.asgaya.husk.ElectrumClient
import com.asgaya.husk.remittance.Remittance
import com.asgaya.husk.remittance.RemittanceStatus
import com.asgaya.husk.remittance.expectedFundingBch
import com.asgaya.husk.transaction.PendingTransaction
import com.asgaya.husk.transaction.TransactionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * SendViewModel - Transaction State Management
 *
 * Manages covenant creation and funding with proper lifecycle handling.
 * Uses viewModelScope instead of lifecycleScope, so transactions survive
 * activity destruction.
 *
 * Based on RS083 pattern from Selene wallet.
 *
 * SELLER AUTO-FUNDING SUPPORT (2026-09-06):
 * - createCovenantOnly: Creates unfunded covenant (seller will fund via Bizum)
 * - createAndFundCovenant: Self-funded path (sender funds immediately)
 */
class SendViewModel(
    private val database: AppDatabase,
    private val covenantWebView: CovenantWebView,
    private val electrumClient: ElectrumClient,
    /** Delivers the `[COVENANT_V25]` offer when this monitor detects the seller funding. */
    private val deliverOffer: suspend (com.asgaya.husk.remittance.Remittance) -> Unit = {}
) : ViewModel() {

    private val _txState = MutableStateFlow<TxState>(TxState.Idle)
    val txState: StateFlow<TxState> = _txState.asStateFlow()

    companion object {
        private const val TAG = "SendViewModel"
        private const val COVENANT_DURATION_SECONDS = 8 * 60 * 60L  // 8 hours
    }

    /** Signed oracle quote: pubkey + BCH/EUR price + oracle timestamp. */
    data class OracleQuote(
        val pubkey: String,
        val priceCents: Int,
        val timestamp: Long
    )

    /**
     * Create covenant ONLY (no funding) - for seller auto-funding flow
     *
     * Creates a covenant address and saves to database with status AWAITING_FUNDING.
     * The seller will fund this covenant when they receive the Bizum payment.
     *
     * Returns CovenantParams for sharing with seller via [FUND_COVENANT] message.
     *
     * SELLER AUTO-FUNDING FLOW:
     * 1. Sender calls createCovenantOnly (this function)
     * 2. Sender sends [FUND_COVENANT] message to seller (Nostr/Telegram)
     * 3. Sender pays €X via Bizum with concept "Elena#142"
     * 4. Seller's NotificationListener detects Bizum → auto-funds covenant
     * 5. Sender's monitorConfirmation sees UTXO → updates status to CONFIRMED
     */
    fun createCovenantOnly(
        senderPubkey: String,
        recipientPubkey: String,
        recipientAddress: String,
        recipientName: String?,
        eurCents: Int,
        amountBch: Double,
        amountFiat: Double,
        currency: String,
        amountRecipient: Double,
        recipientCurrency: String,
        funderPubkey: String,  // BCH seller's pubkey (from bulletin board or hardcoded)
        funderNostrPubkey: String? = null,  // Funder's npub (from the board ad) → sender→funder notify
        adId: String? = null,           // the chosen seller's ad id (the seller's ad checklist, `22 §D`)
        feePercent: Double? = null,     // the chosen seller's ad fee
        adMinPricePercent: Int = 93,    // the chosen seller's ad buffer
        cashAccount: String,   // "Elena#142" - matching key for Bizum
        payAmount: Double? = null, // what the sender commits to pay, in payCurrency (the payment datum, `ui-rethink/40`)
        payCurrency: String? = null
    ) = viewModelScope.launch {
        _txState.value = TxState.FetchingOracleKey

        try {
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "📤 CREATING COVENANT (unfunded, seller will fund)")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "Funder: ${funderPubkey.take(20)}... (seller)")
            Log.d(TAG, "Cash account: $cashAccount")

            // Fetch oracle quote (pubkey + price + timestamp) from API (10s timeout)
            val oracle = try {
                kotlinx.coroutines.withTimeout(10_000) {
                    fetchOracleQuote()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch oracle quote", e)
                _txState.value = TxState.Error("Could not fetch oracle: ${e.message}")
                return@launch
            }
            val oraclePubkey = oracle.pubkey

            Log.d(TAG, "✅ Oracle: pubkey=${oracle.pubkey.take(20)}..., price=€${oracle.priceCents / 100.0}, ts=${oracle.timestamp}")

            // Covenant parameters — price AND time come from the oracle (no hardcoded price,
            // no device clock: the covenant validates the oracle timestamp).
            val currentBchPriceInCents = oracle.priceCents
            val expiryOracleTime = oracle.timestamp + COVENANT_DURATION_SECONDS

            _txState.value = TxState.CreatingCovenant

            // Create covenant address (10s timeout)
            val covenantAddress = try {
                kotlinx.coroutines.withTimeout(10_000) {
                    covenantWebView.createCovenant(
                        senderPubkey = senderPubkey,
                        recipientPubkey = recipientPubkey,
                        funderPubkey = funderPubkey,  // Seller's pubkey
                        oraclePubkey = oraclePubkey,
                        eurCents = eurCents,
                        expiryOracleTime = expiryOracleTime,
                        initialBchPriceInCents = currentBchPriceInCents,
                        minPricePercent = adMinPricePercent
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create covenant", e)
                _txState.value = TxState.Error("Covenant creation failed: ${e.message}")
                return@launch
            }

            Log.d(TAG, "✅ Covenant address: $covenantAddress")

            // Calculate buffered amount (from the seller's ad buffer)
            val minPricePercent = adMinPricePercent
            val bufferedBchAmount = amountBch * (100.0 / minPricePercent)

            // Save remittance to database with status AWAITING_FUNDING
            val remittance = Remittance(
                recipientAddress = recipientAddress,
                recipientName = recipientName,
                amountBch = bufferedBchAmount,
                amountPaymentCurrency = amountFiat,
                paymentCurrency = currency,
                amountRecipientCurrency = amountRecipient,
                recipientCurrency = recipientCurrency,
                networkFee = 0.0000025,
                fundingMethod = "funder_funded",  // Seller auto-funding
                status = RemittanceStatus.AWAITING_FUNDING,  // ⚠️6: New status
                txid = null,  // No txid yet (seller will fund)
                covenantAddress = covenantAddress,
                cashAccount = cashAccount,  // reference: recipient's CashAccount (`ui-rethink/34`)
                senderPubkey = senderPubkey,
                recipientPubkey = recipientPubkey,
                funderPubkey = funderPubkey,  // Seller's pubkey
                funderNostrPubkey = funderNostrPubkey,  // Seller's npub (board ad) → sender→funder notify
                oraclePubkey = oraclePubkey,
                eurCents = eurCents,
                expiryOracleTime = expiryOracleTime,
                initialBchPriceInCents = currentBchPriceInCents,
                minPricePercent = minPricePercent,
                redeemScriptHex = ""
            )

            val remittanceId = database.remittanceDao().insertRemittance(remittance)
            Log.d(TAG, "Saved remittance with ID: $remittanceId (status: AWAITING_FUNDING)")

            // Save to covenant_requests table (seller-side funding ledger)
            val covenantRequest = com.asgaya.husk.CovenantRequest(
                cashAccount = cashAccount,
                covenantAddress = covenantAddress,
                eurCents = eurCents,
                expiryTime = expiryOracleTime,
                initialBchPrice = currentBchPriceInCents,
                minPricePercent = minPricePercent,
                status = "awaiting_payment",  // Seller will fund when Bizum arrives
                redeemScript = null,  // Not needed for funding
                txid = null
            )
            database.covenantDao().insert(covenantRequest)
            Log.d(TAG, "Saved covenant_request: $cashAccount → ${covenantAddress.take(20)}...")

            // Update state to success - triggers navigation in Activity
            val covenantParams = CovenantParams(
                covenantAddress = covenantAddress,
                senderPubkey = senderPubkey,
                recipientPubkey = recipientPubkey,
                funderPubkey = funderPubkey,
                oraclePubkey = oraclePubkey,
                eurCents = eurCents,
                expiryOracleTime = expiryOracleTime,
                initialBchPriceInCents = currentBchPriceInCents,
                minPricePercent = minPricePercent,
                cashAccount = cashAccount,
                bufferedBchAmount = bufferedBchAmount,
                adId = adId,
                feePercent = feePercent,
                payAmount = payAmount,
                payCurrency = payCurrency
            )

            // Generate [FUND_COVENANT] message for sharing with seller
            val fundCovenantMessage = generateFundCovenantMessage(covenantParams)

            _txState.value = TxState.CovenantCreated(
                covenantParams = covenantParams,
                remittance = remittance,
                fundCovenantMessage = fundCovenantMessage  // For sharing with seller
            )

            Log.d(TAG, "✅ Covenant created (unfunded)!")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "Next steps:")
            Log.d(TAG, "1. Share [FUND_COVENANT] message with seller (Telegram/Nostr)")
            Log.d(TAG, "2. Sender pays €${eurCents / 100.0} via Bizum, concept: $cashAccount")
            Log.d(TAG, "3. Seller receives Bizum → auto-funds covenant")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "")
            Log.d(TAG, "📋 [FUND_COVENANT] Message to share:")
            Log.d(TAG, fundCovenantMessage)
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Background monitoring for when seller funds
            launch {
                monitorCovenantFunding(covenantAddress, remittanceId)
            }

        } catch (e: kotlinx.coroutines.CancellationException) {
            Log.d(TAG, "Covenant creation cancelled (ViewModel scope cancelled)")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in createCovenantOnly", e)
            _txState.value = TxState.Error("Unexpected error: ${e.message}")
        }
    }

    /**
     * Create and fund a covenant (self-funded path)
     *
     * This is the main entry point for self-funded covenant creation. It:
     * 1. Fetches oracle pubkey from API
     * 2. Creates covenant address
     * 3. Funds the covenant immediately
     * 4. Saves to database
     * 5. Updates state to Success
     *
     * The transaction survives activity destruction because it runs in viewModelScope.
     */
    fun createAndFundCovenant(
        senderWif: String,
        senderPubkey: String,
        senderAddress: String,
        recipientPubkey: String,
        recipientAddress: String,
        recipientName: String?,
        eurCents: Int,
        amountBch: Double,
        amountFiat: Double,
        currency: String,
        amountRecipient: Double,
        recipientCurrency: String,
        fundingModel: String = "self_funded",
        funderPubkey: String? = null,
        cashAccount: String? = null
    ) = viewModelScope.launch {
        _txState.value = TxState.FetchingOracleKey

        try {
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "📤 CREATING COVENANT (ViewModel)")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Determine the funder pubkey:
            // - self_funded: funder = sender (default)
            // - funder_funded: funder = selected BCH seller (bulletin board)
            //   If no seller selected yet (bulletin board pending), fall back to sender.
            val effectiveFunderPubkey = when {
                fundingModel == "funder_funded" && !funderPubkey.isNullOrEmpty() -> funderPubkey!!
                else -> senderPubkey
            }
            Log.d(TAG, "Funding model: $fundingModel | Funder: ${effectiveFunderPubkey.take(20)}...")

            // Fetch oracle quote (pubkey + price + timestamp) from API (10s timeout)
            val oracle = try {
                kotlinx.coroutines.withTimeout(10_000) {
                    fetchOracleQuote()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch oracle quote", e)
                _txState.value = TxState.Error("Could not fetch oracle: ${e.message}")
                return@launch
            }
            val oraclePubkey = oracle.pubkey

            Log.d(TAG, "✅ Oracle: pubkey=${oracle.pubkey.take(20)}..., price=€${oracle.priceCents / 100.0}, ts=${oracle.timestamp}")

            // Covenant parameters — price AND time come from the oracle (no hardcoded price,
            // no device clock: the covenant validates the oracle timestamp).
            val currentBchPriceInCents = oracle.priceCents
            val expiryOracleTime = oracle.timestamp + COVENANT_DURATION_SECONDS

            _txState.value = TxState.CreatingCovenant

            // Create covenant address (10s timeout)
            val covenantAddress = try {
                kotlinx.coroutines.withTimeout(10_000) {
                    covenantWebView.createCovenant(
                        senderPubkey = senderPubkey,
                        recipientPubkey = recipientPubkey,
                        funderPubkey = effectiveFunderPubkey,  // Seller (funder) or sender if self-funded
                        oraclePubkey = oraclePubkey,
                        eurCents = eurCents,
                        expiryOracleTime = expiryOracleTime,
                        initialBchPriceInCents = currentBchPriceInCents,
                        minPricePercent = 93
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create covenant", e)
                _txState.value = TxState.Error("Covenant creation failed: ${e.message}")
                return@launch
            }

            Log.d(TAG, "✅ Covenant address: $covenantAddress")

            // Calculate amount with volatility buffer (7%)
            val minPricePercent = 93
            val bufferedBchAmount = amountBch * (100.0 / minPricePercent)
            val amountSats = (bufferedBchAmount * 100000000).toLong()

            _txState.value = TxState.Broadcasting

            // Fund covenant (30s timeout)
            // v0.2 HYBRID: Use sendBchHybrid (Kotlin network, WebView compute)
            val txid = try {
                kotlinx.coroutines.withTimeout(30_000) {
                    covenantWebView.sendBchHybrid(
                        senderWIF = senderWif,
                        senderAddress = senderAddress,
                        recipientAddress = covenantAddress,
                        amountSats = amountSats
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to broadcast transaction", e)
                _txState.value = TxState.Error("Broadcast failed: ${e.message}")
                return@launch
            }

            Log.d(TAG, "✅ Transaction broadcast! TXID: $txid")

            // Save remittance to database
            val remittance = Remittance(
                recipientAddress = recipientAddress,
                recipientName = recipientName,
                amountBch = bufferedBchAmount,
                amountPaymentCurrency = amountFiat,
                paymentCurrency = currency,
                amountRecipientCurrency = amountRecipient,
                recipientCurrency = recipientCurrency,
                networkFee = 0.0000025,
                fundingMethod = fundingModel,  // "self_funded" or "funder_funded"
                status = RemittanceStatus.PENDING,
                txid = txid,
                covenantAddress = covenantAddress,
                cashAccount = cashAccount,  // reference: recipient's CashAccount (`ui-rethink/34`)
                senderPubkey = senderPubkey,
                recipientPubkey = recipientPubkey,
                funderPubkey = effectiveFunderPubkey,  // Seller (funder) or sender if self-funded
                oraclePubkey = oraclePubkey,
                eurCents = eurCents,
                expiryOracleTime = expiryOracleTime,
                initialBchPriceInCents = currentBchPriceInCents,
                minPricePercent = 93,
                redeemScriptHex = ""
            )

            val remittanceId = database.remittanceDao().insertRemittance(remittance)
            Log.d(TAG, "Saved remittance with ID: $remittanceId")

            // Funder ledger (`ui-rethink/39`): track the SELF-FUNDED covenant too, so the always-on
            // recovery sweep (`CovenantRecovery.recoverAllDue`, `checkTimeouts` B) and the card can
            // see it. Previously only the funder-funded flow (`createCovenantOnly`) inserted a row.
            database.covenantDao().insert(
                com.asgaya.husk.CovenantRequest(
                    cashAccount = cashAccount ?: recipientAddress,
                    covenantAddress = covenantAddress,
                    eurCents = eurCents,
                    expiryTime = expiryOracleTime,
                    initialBchPrice = currentBchPriceInCents,
                    minPricePercent = 93,
                    status = "funded",
                    redeemScript = null,
                    txid = txid,
                    fundingTxid = txid,
                    senderPubkey = senderPubkey,
                    recipientPubkey = recipientPubkey,
                    funderPubkey = effectiveFunderPubkey,
                    oraclePubkey = oraclePubkey,
                    fundedAt = System.currentTimeMillis()
                )
            )
            Log.d(TAG, "Saved covenant_request (funder ledger, status=funded)")

            // Save pending transaction (RS083 pattern)
            val pendingTx = PendingTransaction(
                txid = txid,
                tx_hex = "",
                status = TransactionStatus.BROADCAST,
                covenant_address = covenantAddress,
                remittance_id = remittanceId
            )
            database.pendingTransactionDao().insert(pendingTx)
            Log.d(TAG, "Saved pending transaction: $txid")

            // Update state to success - triggers navigation in Activity
            _txState.value = TxState.Success(
                txid = txid,
                covenantAddress = covenantAddress,
                remittance = remittance
            )

            Log.d(TAG, "✅ Covenant creation complete!")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Background confirmation monitoring
            launch {
                monitorConfirmation(txid)
            }

        } catch (e: kotlinx.coroutines.CancellationException) {
            // Activity destroyed - this is expected, don't treat as error
            Log.d(TAG, "Transaction cancelled (ViewModel scope cancelled)")
            throw e  // Re-throw to properly cancel coroutine
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in createAndFundCovenant", e)
            _txState.value = TxState.Error("Unexpected error: ${e.message}")
        }
    }

    /**
     * Monitor covenant funding (seller auto-funding flow)
     *
     * Polls for covenant UTXO to appear (seller funded it), then updates remittance status.
     * Runs in background independently of UI.
     *
     * SELLER AUTO-FUNDING: The seller's NotificationListener will fund the covenant
     * when Bizum arrives. This function detects the funding UTXO and updates the sender's DB.
     */
    private suspend fun monitorCovenantFunding(covenantAddress: String, remittanceId: Long) {
        var attempts = 0
        val maxAttempts = 60  // Check for up to 1 hour

        Log.d(TAG, "🔍 Monitoring for seller funding: $covenantAddress")

        while (attempts < maxAttempts) {
            delay(60_000)  // Check every minute
            attempts++

            try {
                // Query Electrum for UTXOs at covenant address
                val utxos = electrumClient.getUTXOsForAddress(covenantAddress)

                if (utxos.isNotEmpty()) {
                    // Only accept funding once the collateral covers the EUR face + the buffer
                    // (`ui-rethink/27`): an under-funded covenant must not be surfaced as live.
                    val rem0 = database.remittanceDao().getRemittanceById(remittanceId)
                    val expected = rem0?.expectedFundingBch()
                    val received = utxos.sumOf { it.value } / 1e8
                    if (expected != null && received < expected * 0.95) {
                        Log.w(TAG, "  ⏳ Under-funded (${"%.8f".format(received)} < ${"%.8f".format(expected)} BCH) — waiting")
                        continue
                    }
                    // Covenant funded! Extract txid
                    val txid = utxos[0].txHash

                    Log.i(TAG, "✅ Covenant funded by seller! TXID: $txid")

                    // Update remittance status
                    val remittance = database.remittanceDao().getRemittanceById(remittanceId)
                    if (remittance != null) {
                        database.remittanceDao().updateRemittance(
                            remittance.copy(
                                status = RemittanceStatus.PENDING,  // Now pending confirmation
                                txid = txid
                            )
                        )
                        Log.i(TAG, "  📝 Updated remittance $remittanceId to PENDING")

                        // Deliver the [COVENANT_V25] offer (`ui-rethink/35`): the always-on listener
                        // sends it for `awaiting_funding` rows, but this monitor just moved the row to
                        // PENDING — so the offer must go out from here (otherwise it is never sent).
                        runCatching {
                            deliverOffer(remittance.copy(status = RemittanceStatus.PENDING, txid = txid))
                        }
                    }

                    // Now monitor for confirmation
                    monitorConfirmation(txid)
                    break
                }

                Log.d(TAG, "  ⏳ Still awaiting seller funding: $covenantAddress (attempt $attempts/$maxAttempts)")

            } catch (e: Exception) {
                Log.w(TAG, "Funding check failed for $covenantAddress (attempt $attempts)", e)
            }
        }

        if (attempts >= maxAttempts) {
            Log.w(TAG, "⏱️ Seller funding timeout for $covenantAddress after 1 hour")
        }
    }

    /**
     * Monitor transaction confirmation in background
     *
     * Polls Electrum for transaction status and updates database when confirmed.
     * Runs independently of UI - won't block user.
     *
     * This runs in viewModelScope, so it survives activity destruction.
     * Based on RS083 pattern from Selene wallet.
     */
    private suspend fun monitorConfirmation(txid: String) {
        var confirmations = 0
        var attempts = 0
        val maxAttempts = 60  // Check for up to 1 hour

        Log.d(TAG, "🔍 Starting background confirmation monitoring for $txid")

        while (confirmations == 0 && attempts < maxAttempts) {
            delay(60_000)  // Check every minute
            attempts++

            try {
                // Query Electrum for transaction status
                val txInfo = electrumClient.getTransaction(txid)

                if (txInfo != null) {
                    confirmations = txInfo.confirmations

                    if (confirmations > 0) {
                        // Transaction confirmed!
                        database.pendingTransactionDao().updateConfirmation(
                            txid = txid,
                            status = TransactionStatus.CONFIRMED,
                            confirmedAt = System.currentTimeMillis(),
                            blockHeight = txInfo.blockHeight,
                            confirmations = confirmations
                        )

                        // Update linked remittance status
                        val pendingTx = database.pendingTransactionDao().getByTxid(txid)
                        pendingTx?.remittance_id?.let { remittanceId ->
                            val remittance = database.remittanceDao().getRemittanceById(remittanceId)
                            if (remittance != null && remittance.status == RemittanceStatus.PENDING) {
                                database.remittanceDao().updateRemittance(
                                    remittance.copy(status = RemittanceStatus.CONFIRMED)
                                )
                                Log.i(TAG, "  📝 Updated remittance $remittanceId to CONFIRMED")
                            }
                        }

                        Log.i(TAG, "✅ Transaction confirmed in background: $txid ($confirmations confirmations)")
                        break
                    } else if (txInfo.inMempool) {
                        // Still in mempool, update status
                        database.pendingTransactionDao().updateStatus(txid, TransactionStatus.MEMPOOL)
                        Log.d(TAG, "  📡 Transaction still in mempool: $txid (attempt $attempts/$maxAttempts)")
                    }
                } else {
                    // Transaction not found - might have been dropped from mempool
                    Log.w(TAG, "  ⚠️ Transaction not found on Electrum: $txid (attempt $attempts/$maxAttempts)")
                }

            } catch (e: Exception) {
                Log.w(TAG, "Confirmation check failed for $txid (attempt $attempts)", e)
            }
        }

        if (attempts >= maxAttempts && confirmations == 0) {
            Log.w(TAG, "⏱️ Confirmation timeout for $txid after 1 hour")
        }
    }

    /**
     * Generate [FUND_COVENANT] message for sharing with seller
     *
     * SELLER AUTO-FUNDING: Sender shares this message with seller (Telegram/Nostr).
     * Seller's NotificationListener parses it and stores in covenant_requests table.
     *
     * Message format matches NotificationListener.parseFundCovenant() expectations.
     */
    private fun generateFundCovenantMessage(params: CovenantParams): String {
        return """
[FUND_COVENANT]
cashAccount: ${params.cashAccount}
covenantAddress: ${params.covenantAddress}
senderPubkey: ${params.senderPubkey}
recipientPubkey: ${params.recipientPubkey}
funderPubkey: ${params.funderPubkey}
oraclePubkey: ${params.oraclePubkey}
eurCents: ${params.eurCents}
expiryOracleTime: ${params.expiryOracleTime}
initialBchPriceInCents: ${params.initialBchPriceInCents}
minPricePercent: ${params.minPricePercent}
payWindowSeconds: ${com.asgaya.husk.covenant.CovenantRules.PAY_WINDOW_SECONDS_DEFAULT}
adId: ${params.adId ?: ""}
feePercent: ${params.feePercent ?: ""}
amount: ${params.payAmount ?: ""}
currency: ${params.payCurrency ?: ""}
[/FUND_COVENANT]

📋 Covenant Details:
Amount: €${params.eurCents / 100.0}
Price: €${params.initialBchPriceInCents / 100.0}/BCH
Buffer: ${params.minPricePercent}% (${100 - params.minPricePercent}% drop protection)
Expiry: ${params.expiryOracleTime} (Unix timestamp)

💰 To fund this covenant:
1. Wait for Bizum payment: €${params.eurCents / 100.0}
2. Bizum concept must be: ${params.cashAccount}
3. Your device will auto-fund when payment arrives
        """.trimIndent()
    }

    /**
     * Fetch oracle pubkey from API
     *
     * Queries the Pi-chan oracle API for the current oracle public key.
     * This avoids hardcoded keys in the app.
     */
    private suspend fun fetchOracleQuote(): OracleQuote {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val url = java.net.URL("http://localhost:3001/oracle/price")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = org.json.JSONObject(response)
                val quote = OracleQuote(
                    pubkey = json.getString("pubkey"),
                    priceCents = json.getInt("price"),
                    timestamp = json.getLong("timestamp")
                )

                Log.d(TAG, "✅ Fetched oracle: pubkey=${quote.pubkey.take(20)}..., price=€${quote.priceCents / 100.0}, ts=${quote.timestamp}")
                quote
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to fetch oracle quote from API", e)
                throw Exception("Could not fetch oracle quote: ${e.message}")
            }
        }
    }

    /**
     * Reset state to Idle
     *
     * Call this after navigating away from success screen to prepare for next transaction.
     */
    fun reset() {
        _txState.value = TxState.Idle
    }
}

/**
 * Transaction State
 *
 * Represents the current state of covenant creation/funding.
 * Activity observes this and updates UI accordingly.
 */
sealed class TxState {
    object Idle : TxState()
    object FetchingOracleKey : TxState()
    object CreatingCovenant : TxState()
    object Broadcasting : TxState()
    data class Success(
        val txid: String,
        val covenantAddress: String,
        val remittance: Remittance
    ) : TxState()
    data class CovenantCreated(
        val covenantParams: CovenantParams,
        val remittance: Remittance,
        val fundCovenantMessage: String  // [FUND_COVENANT] message to share with seller
    ) : TxState()  // Seller auto-funding: covenant created but not funded yet
    data class Error(val message: String) : TxState()
}

/**
 * Covenant Parameters (for sharing with seller)
 *
 * Contains all params needed for seller to:
 * 1. Verify the covenant is genuine (reconstruct address)
 * 2. Calculate funding amount
 * 3. Match Bizum payment to covenant
 */
data class CovenantParams(
    val covenantAddress: String,
    val senderPubkey: String,
    val recipientPubkey: String,
    val funderPubkey: String,
    val oraclePubkey: String,
    val eurCents: Int,
    val expiryOracleTime: Long,
    val initialBchPriceInCents: Int,
    val minPricePercent: Int,
    val cashAccount: String,  // "Elena#142" - Bizum matching key
    val bufferedBchAmount: Double,
    val adId: String? = null,      // the chosen seller's ad id → the seller's ad checklist (`22 §D`)
    val feePercent: Double? = null, // the chosen seller's ad fee
    val payAmount: Double? = null,  // what the sender commits to pay, in payCurrency (the payment datum, `ui-rethink/40`)
    val payCurrency: String? = null
)
