package com.asgaya.husk.remittance

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.asgaya.husk.AdvancedMode
import com.asgaya.husk.AppDatabase
import com.asgaya.husk.CovenantRequest
import com.asgaya.husk.CovenantWebView
import com.asgaya.husk.ElectrumClient
import com.asgaya.husk.MainActivity
import com.asgaya.husk.databinding.ActivityRemittanceBinding
import com.asgaya.husk.wallet.WalletManager
import com.asgaya.husk.transaction.PendingTransaction
import com.asgaya.husk.transaction.TransactionStatus
import com.asgaya.husk.covenant.CovenantRecovery
import com.asgaya.husk.data.SimpleCashoutRequest
import com.asgaya.husk.data.MerchantCosignResponse
import com.asgaya.husk.data.CashoutMessage
import com.asgaya.husk.nostr.NIP17GiftWrap
import com.asgaya.husk.nostr.NostrClient
import com.asgaya.husk.nostr.NostrKeyManager
import com.asgaya.husk.SupportedFiats
import com.asgaya.husk.FiatRateSource
import com.asgaya.husk.AutoRefundPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import org.json.JSONObject
import java.net.URL
import android.graphics.Bitmap
import android.graphics.Color
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.view.Gravity
import androidx.appcompat.app.AlertDialog
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Remittance Activity - Tab 2
 *
 * Sender flow for sending money cross-border via BCH covenants
 *
 * Two funding paths:
 * 1. Self-funded: User has BCH, creates & funds covenant directly (free)
 * 2. Buy BCH: User buys from bulletin board seller (0.5% fee)
 */
class RemittanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRemittanceBinding
    private lateinit var database: AppDatabase
    private lateinit var walletManager: WalletManager
    private lateinit var covenantWebView: CovenantWebView
    private lateinit var adapter: RemittanceAdapter
    private var nostrKeyManager: NostrKeyManager? = null

    /** Periodic timeout watch (auto-refund, `ui-rethink/09`); runs while the screen is resumed. */
    private var timeoutJob: Job? = null
    // Active cash-out context (merchant-first), set when the cosign quote arrives.
    private var activeCashoutCovenantAddress: String? = null
    private var activeCashoutQuoteId: String? = null
    private var activeCashoutRequest: SimpleCashoutRequest? = null
    private var activeCashoutMerchantNpub: String? = null

    // QR scanner for merchant's co-sign response (merchant-first flow)
    private var pendingCashoutRequest: SimpleCashoutRequest? = null
    private val merchantQRScanner = registerForActivityResult(
        com.journeyapps.barcodescanner.ScanContract()
    ) { result ->
        if (result.contents != null) {
            Log.d(TAG, "📷 Merchant QR scanned: ${result.contents.length} chars")
            pendingCashoutRequest?.let { request ->
                handleMerchantCosignQR(result.contents, request)
            }
        } else {
            Log.d(TAG, "📷 Merchant QR scan cancelled")
            Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val TAG = "RemittanceActivity"
        const val PREFS_NAME = "RemittancePrefs"
        const val PAYMENT_CURRENCY_KEY = "payment_currency"  // EUR, USD
        const val RECIPIENT_CURRENCY_KEY = "recipient_currency"  // VES, ARS
        const val PAYMENT_METHODS_KEY = "payment_methods"  // JSON array: ["bizum", "sepa", "cash"]
        // Cash-out handed off from the Market tab (`ui-rethink/41`): the covenant + chosen merchant.
        const val EXTRA_CASHOUT_COVENANT = "cashout_covenant"
        const val EXTRA_CASHOUT_MERCHANT_ID = "cashout_merchant_id"
        const val EXTRA_CASHOUT_MERCHANT_NPUB = "cashout_merchant_npub"

        // Oracle configuration (Phase 0 - Development Infrastructure)
        private const val ORACLE_URL = "http://localhost:3001/oracle/price"
        private const val ORACLE_PUBKEY = "0387053eebfb132fc6219ada6cb726fc672a15fc9729b36783bf6b057b83f122b0"
    }

    /**
     * Oracle price response from Asgaya Oracle API
     */
    data class OraclePrice(
        val price: Int,           // Price in cents
        val timestamp: Long,      // Unix timestamp (seconds)
        val message: String,      // 16-byte message (hex)
        val signature: String,    // DER-encoded signature (hex)
        val pubkey: String        // Oracle public key (hex)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemittanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        walletManager = WalletManager.getInstance(this)
        covenantWebView = CovenantWebView.getInstance(this)

        setupUI()
        setupRecyclerView()
        setupBottomNavigation()
        loadRemittanceHistory()
        handleCashOutIntent(intent)
    }

    /** A cash-out handed off from the Market tab (`ui-rethink/41`): resolve the covenant + merchant. */
    private fun handleCashOutIntent(intent: Intent?) {
        val covenant = intent?.getStringExtra(EXTRA_CASHOUT_COVENANT) ?: return
        val merchantId = intent.getStringExtra(EXTRA_CASHOUT_MERCHANT_ID)
        val npub = intent.getStringExtra(EXTRA_CASHOUT_MERCHANT_NPUB) ?: return
        lifecycleScope.launch {
            val remittance = withContext(Dispatchers.IO) {
                database.remittanceDao().findByCovenantAddress(covenant)
            }
            // Resolve by the ad id first — one wallet's ads share the npub (ambiguous) (`ui-rethink/41`).
            val merchant = withContext(Dispatchers.IO) {
                val all = com.asgaya.husk.data.NostrBoardSource(database.boardEventDao()).listings()
                all.firstOrNull { it.id == merchantId } ?: all.firstOrNull { it.nostrPubkey == npub }
            }
            if (remittance == null || merchant == null) {
                android.widget.Toast.makeText(
                    this@RemittanceActivity, "❌ Could not start the cash-out", android.widget.Toast.LENGTH_LONG
                ).show()
                return@launch
            }
            showCashOutInstructions(remittance, merchant)
        }
    }

    private fun setupUI() {
        // Send New Remittance button
        binding.sendNewButton.setOnClickListener {
            val intent = Intent(this, SendRemittanceActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        adapter = RemittanceAdapter(
            remittances = emptyList(),
            onRefundClick = { remittance -> handleRefund(remittance) },
            onAbortClick = { remittance -> handleAbort(remittance) },
            onClaimClick = { remittance -> handleClaim(remittance) },
            onCashOutClick = { remittance -> handleCashOut(remittance) },  // NEW: Cash out at merchant
            onBuyClick = { remittance -> handleBuy(remittance) },          // Buy ⟨main⟩ at Market (`ui-rethink/41`)
            onShareParamsClick = { remittance -> handleShareParams(remittance) },
            onSendAgainClick = { remittance -> startSendAgain(remittance) }
        )
        // The cards show the amount in the device's selected currency (`FiatDisplay`).
        adapter.mainCurrency = getSharedPreferences("BizumParserPrefs", android.content.Context.MODE_PRIVATE)
            .getString("fiat_currency", "EUR") ?: "EUR"
        lifecycleScope.launch {
            adapter.eurPerMain = com.asgaya.husk.FiatRateSource.eurPer(adapter.mainCurrency)
            runOnUiThread { adapter.notifyDataSetChanged() }
        }
        binding.remittanceList.apply {
            layoutManager = LinearLayoutManager(this@RemittanceActivity)
            adapter = this@RemittanceActivity.adapter
        }
    }

    /**
     * `Buy ⟨main currency⟩` (`ui-rethink/41`): open the Market tab scoped to this covenant, with the
     * main currency preselected as the asset and Fee ascending — ready to cash out.
     */
    private fun handleBuy(remittance: Remittance) {
        val covenant = remittance.covenantAddress ?: return
        val mainCurrency = getSharedPreferences("BizumParserPrefs", android.content.Context.MODE_PRIVATE)
            .getString("fiat_currency", "EUR") ?: "EUR"
        startActivity(
            Intent(this, com.asgaya.husk.trade.MarketActivity::class.java).apply {
                putExtra(com.asgaya.husk.trade.MarketActivity.EXTRA_CASHOUT_COVENANT, covenant)
                putExtra(com.asgaya.husk.trade.MarketActivity.EXTRA_PRESELECT_ASSET, mainCurrency)
            }
        )
    }

    /**
     * `Send again` (`ui-rethink/34`) — open the send flow prefilled to repeat this remittance.
     * It recreates with a **fresh oracle quote** (never re-sends a stale request); the old card
     * stays as history.
     */
    private fun startSendAgain(remittance: Remittance) {
        val model = if (remittance.fundingMethod == "buy_bch")
            SendRemittanceActivity.FUNDING_MODEL_FUNDER else SendRemittanceActivity.FUNDING_MODEL_SELF
        val intent = Intent(this, SendRemittanceActivity::class.java).apply {
            putExtra(SendRemittanceActivity.EXTRA_PREFILL, true)
            putExtra(SendRemittanceActivity.EXTRA_RECIPIENT, remittance.recipientAddress)
            putExtra(SendRemittanceActivity.EXTRA_RECIPIENT_NAME, remittance.recipientName)
            putExtra(SendRemittanceActivity.EXTRA_RECIPIENT_PUBKEY, remittance.recipientPubkey)
            putExtra(SendRemittanceActivity.EXTRA_AMOUNT_FIAT, remittance.amountPaymentCurrency)
            putExtra(SendRemittanceActivity.EXTRA_CURRENCY, remittance.paymentCurrency)
            putExtra(SendRemittanceActivity.EXTRA_RECIPIENT_CURRENCY, remittance.recipientCurrency)
            putExtra(SendRemittanceActivity.EXTRA_FUNDING_MODEL, model)
            putExtra(SendRemittanceActivity.EXTRA_FUNDER_PUBKEY, remittance.funderPubkey)
            putExtra(SendRemittanceActivity.EXTRA_RECIPIENT_CASH_ACCOUNT, remittance.cashAccount)
        }
        Log.d(TAG, "🔁 Send again: ${remittance.recipientName ?: remittance.recipientAddress}")
        startActivity(intent)
    }

    /**
     * Copy the [COVENANT_V25] parameters to the clipboard so the sender can send
     * the notification to the recipient (e.g. via Telegram).
     *
     * The sender may have missed the auto-copy window after creating the covenant
     * (it shows briefly then navigates away). This gives them another chance to
     * notify the recipient at any time.
     */
    private fun handleShareParams(remittance: Remittance) {
        Log.d(TAG, "✈️ Re-share requested for remittance: ${remittance.id}")

        // Always copy to clipboard (fallback path)
        val message = formatCovenantMessage(remittance)
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Asgaya Covenant", message)
            clipboard.setPrimaryClip(clip)
            Log.d(TAG, "✈️ Covenant notification copied to clipboard (fallback)")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to copy covenant params", e)
        }

        // Also try to deliver to the recipient over Nostr (manual resend)
        resendCovenantToRecipient(remittance, message)
    }

    /**
     * Manually (re)send the [COVENANT_V25] block to the recipient over Nostr.
     *
     * The recipient's Nostr pubkey is looked up from the contact list (keyed by the
     * covenant's recipientPubkey) — the production shape. Test contacts are seeded
     * with deterministic identities (TestNostrKeys).
     *
     * NOTE: the offer is now sent AUTOMATICALLY once the covenant is funded
     * (`NotificationListener.monitorAwaitingFunding`). This button is the manual
     * fallback (e.g. the auto-send failed, or the sender wants to re-nudge).
     * A QR-based contact exchange (`collaborative_workspace/contact-exchange/`) will
     * populate the contact npub without hardcoding. Clipboard remains the universal fallback.
     */
    private fun resendCovenantToRecipient(remittance: Remittance, message: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val recipientBchPubkey = remittance.recipientPubkey
                if (recipientBchPubkey.isNullOrBlank()) {
                    Log.w(TAG, "No recipientPubkey on remittance — cannot send via Nostr")
                    return@launch
                }

                val contact = database.contactDao().getContactByPublicKey(recipientBchPubkey)
                val recipientNostrPubkey = contact?.nostrPubkey
                if (recipientNostrPubkey.isNullOrBlank()) {
                    Log.w(TAG, "No Nostr pubkey for recipient ${recipientBchPubkey.take(20)}... — clipboard only")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@RemittanceActivity,
                            "✈️ Copied — no Nostr key for this recipient",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }

                val activeWallet = walletManager.getActiveWallet()
                if (activeWallet == null) {
                    Log.e(TAG, "No active wallet — cannot send via Nostr")
                    return@launch
                }

                val manager = nostrKeyManager ?: NostrKeyManager(this@RemittanceActivity).also { nostrKeyManager = it }
                val myKeys = manager.ensureNostrKeys(activeWallet)

                // Sender only publishes here; don't clobber any existing receive handler.
                val client = NostrClient.getInstance(myKeys.publicKeyHex, null)
                client.connect()

                val giftWrap = NIP17GiftWrap.wrapDM(
                    content = message,
                    senderPrivKeyHex = myKeys.privateKeyHex,
                    senderPubKeyHex = myKeys.publicKeyHex,
                    recipientPubKeyHex = recipientNostrPubkey
                )

                val sentCount = client.publishEvent(giftWrap)
                Log.i(TAG, "✅ COVENANT_V25 sent to recipient (${contact.name}) via $sentCount relays")

                withContext(Dispatchers.Main) {
                    val text = if (sentCount > 0) {
                        "✈️ Sent to ${contact.name} via Nostr ($sentCount relays)"
                    } else {
                        "⚠️ Relays connecting — copied to clipboard"
                    }
                    Toast.makeText(this@RemittanceActivity, text, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to send covenant via Nostr", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RemittanceActivity,
                        "⚠️ Nostr send failed — copied to clipboard",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    /**
     * Format covenant parameters as shareable message (shared with the service via [CovenantOffer]).
     * Recipient will receive this via Nostr (or Telegram) and claim the covenant.
     */
    private fun formatCovenantMessage(remittance: Remittance): String =
        CovenantOffer.message(remittance)

    private fun handleRefund(remittance: Remittance) {
        Log.d(TAG, "🔄 Refund requested for remittance: ${remittance.id}")
        Log.d(TAG, "  Covenant address: ${remittance.covenantAddress}")
        Log.d(TAG, "  EUR: €${remittance.amountPaymentCurrency}")

        // Show confirmation dialog
        android.app.AlertDialog.Builder(this)
            .setTitle("🔄 Refund Covenant")
            .setMessage(
                "Refund €${remittance.amountPaymentCurrency}?\n\n" +
                "You will receive the EUR amount back at current BCH price.\n\n" +
                "Covenant: ${remittance.covenantAddress?.take(30)}..."
            )
            .setPositiveButton("Refund") { _, _ ->
                executeRefund(remittance)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun executeRefund(remittance: Remittance) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.d(TAG, "🔄 EXECUTING REFUND")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RemittanceActivity, "🔄 Refunding covenant...", Toast.LENGTH_SHORT).show()
                }

                // Get active wallet (should be sender wallet)
                val activeWallet = walletManager.getActiveWallet()
                if (activeWallet == null) {
                    throw Exception("No active wallet")
                }

                // Get sender WIF
                val senderWIF = walletManager.getWIF(activeWallet)
                Log.d(TAG, "Sender address: ${activeWallet.address}")

                // Fetch current price from oracle
                val oraclePrice = fetchOraclePrice()
                Log.d(TAG, "Current BCH price: €${oraclePrice.price / 100.0}/BCH")

                // Build oracle signature map from API response
                val oracleSig = mapOf(
                    "message" to oraclePrice.message,
                    "signature" to oraclePrice.signature,
                    "timestamp" to oraclePrice.timestamp,
                    "price" to oraclePrice.price
                )

                // Build covenant parameters map
                val covenantParams = mapOf(
                    "senderPubkey" to (remittance.senderPubkey ?: ""),
                    "recipientPubkey" to (remittance.recipientPubkey ?: ""),
                    "funderPubkey" to (remittance.funderPubkey ?: ""),
                    "oraclePubkey" to (remittance.oraclePubkey ?: ""),
                    "eurCents" to (remittance.eurCents ?: 0),
                    "expiryOracleTime" to (remittance.expiryOracleTime ?: 0L),
                    "initialBchPriceInCents" to (remittance.initialBchPriceInCents ?: 0),
                    "minPricePercent" to (remittance.minPricePercent ?: 93)
                )

                Log.d(TAG, "Calling CovenantWebView.refundCovenantHybrid() [v0.2]...")

                // Call refund (v0.2 Hybrid: WebView builds, Kotlin broadcasts)
                // NOTE: funderAddress derived from funderPubkey in JS (covenant enforces
                // output[1] = P2PKH(hash160(funder))). The sender's device does NOT need the
                // funder's wallet — critical for funder≠sender flow (buffer → seller/funder).
                val refundFunderAddress = remittance.funderPubkey?.let { fp ->
                    walletManager.findWalletByPubkey(fp)?.address ?: ""
                } ?: ""
                val txid = covenantWebView.refundCovenantHybrid(
                    covenantParams = covenantParams,
                    oracleSig = oracleSig,
                    senderWIF = senderWIF,
                    senderAddress = activeWallet.address,
                    funderAddress = refundFunderAddress, // Empty → JS derives from funderPubkey
                    covenantAddress = remittance.covenantAddress ?: throw Exception("Covenant address not found"),
                    fulcrumHost = "localhost",
                    fulcrumPort = 60003  // No longer used (UTXOs via Kotlin)
                )

                Log.d(TAG, "✅ ✅ ✅ REFUND SUCCESSFUL!")
                Log.d(TAG, "Transaction ID: $txid")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // Save pending transaction (RS083 pattern)
                val pendingTx = PendingTransaction(
                    txid = txid,
                    tx_hex = "",  // WebView doesn't provide hex
                    status = TransactionStatus.BROADCAST,
                    covenant_address = remittance.covenantAddress,
                    remittance_id = remittance.id
                )
                database.pendingTransactionDao().insert(pendingTx)
                Log.d(TAG, "Saved pending transaction: $txid")

                // Update remittance status in database
                val updatedRemittance = remittance.copy(
                    status = RemittanceStatus.REFUNDED,
                    txid = txid
                )
                database.remittanceDao().updateRemittance(updatedRemittance)
                Log.d(TAG, "Updated remittance status to REFUNDED")

                // Tell the FUNDER it was refunded — a hint; the funder still verifies on-chain (`58`)
                com.asgaya.husk.covenant.CovenantResolutionNotifier.notifyNpub(
                    this@RemittanceActivity,
                    remittance.covenantAddress ?: "",
                    remittance.funderNostrPubkey,
                    com.asgaya.husk.data.CovenantResolved.PATH_REFUND,
                    txid
                )

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RemittanceActivity,
                        "✅ Refund successful!\n\nTXID: ${txid.take(20)}...",
                        Toast.LENGTH_LONG
                    ).show()
                }

                // Reload remittances to refresh UI
                loadRemittanceHistory()

            } catch (e: Exception) {
                Log.e(TAG, "❌ Refund failed", e)
                Log.e(TAG, "  Error: ${e.message}")

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RemittanceActivity,
                        "❌ Refund failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    /**
     * Seller (funder) recovers its buffer after the covenant expired with the sender offline.
     * Covenant path: `sellerRecoverBuffer` (the funder signs). `ui-rethink/10`; covenant v2.3
     * (seller buffer recovery). Output 0 = payment → sender, output 1 = buffer → seller.
     */
    private fun executeSellerRecover(
        covenant: CovenantRequest,
        oracle: CovenantRecovery.OraclePrice? = null
    ) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.d(TAG, "🛡️ EXECUTING SELLER BUFFER RECOVERY — ${covenant.cashAccount}")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // The active wallet must be the funder (seller) of this covenant
                val activeWallet = walletManager.getActiveWallet()
                    ?: throw Exception("No active wallet")
                if (covenant.funderPubkey != activeWallet.publicKey) {
                    throw Exception("Active wallet is not this covenant's funder")
                }

                val sellerWIF = walletManager.getWIF(activeWallet)

                val txid = CovenantRecovery.recoverSellerBuffer(
                    covenant = covenant,
                    activeWallet = activeWallet,
                    wif = sellerWIF,
                    covenantWebView = covenantWebView,
                    oracle = oracle
                )

                Log.d(TAG, "✅ Buffer recovered! TXID: $txid")

                // Mark the covenant resolved
                database.covenantDao().updateFunded(
                    covenant.id, "buffer_recovered", txid, System.currentTimeMillis()
                )

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RemittanceActivity,
                        "🛡️ Buffer recovered\n\nTXID: ${txid.take(20)}...",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Seller buffer recovery failed", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RemittanceActivity,
                        "❌ Buffer recovery failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun handleAbort(remittance: Remittance) {
        Log.d(TAG, "⚠️ Abort requested for remittance: ${remittance.id}")
        Log.d(TAG, "  Covenant address: ${remittance.covenantAddress}")
        Log.d(TAG, "  EUR: €${remittance.amountPaymentCurrency}")

        // Show confirmation dialog
        android.app.AlertDialog.Builder(this)
            .setTitle("⚠️ Abort Covenant")
            .setMessage(
                "Emergency abort €${remittance.amountPaymentCurrency} covenant?\n\n" +
                "⚠️ WARNING:\n" +
                "- Only works if BCH price dropped ≥ 6.5%\n" +
                "- You get all funds back\n" +
                "- Funder gets nothing (buffer consumed)\n\n" +
                "Covenant: ${remittance.covenantAddress?.take(30)}..."
            )
            .setPositiveButton("Abort") { _, _ ->
                executeAbort(remittance)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun executeAbort(remittance: Remittance) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.d(TAG, "⚠️ EXECUTING ABORT")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RemittanceActivity, "⚠️ Aborting covenant...", Toast.LENGTH_SHORT).show()
                }

                // Get active wallet (should be sender wallet)
                val activeWallet = walletManager.getActiveWallet()
                if (activeWallet == null) {
                    throw Exception("No active wallet")
                }

                // Get sender WIF
                val senderWIF = walletManager.getWIF(activeWallet)
                Log.d(TAG, "Sender address: ${activeWallet.address}")

                // Fetch current price from oracle
                // NOTE: To test abort, use oracle API to set price first:
                // curl -X POST http://localhost:3001/oracle/set-price -d '{"price":60450}'
                val oraclePrice = fetchOraclePrice()
                val initialPrice = remittance.initialBchPriceInCents
                    ?: throw Exception("Cannot abort: the covenant's initial BCH price is missing (no hardcoded fallback)")
                val priceDrop = ((initialPrice - oraclePrice.price) * 100.0 / initialPrice)

                Log.d(TAG, "⚠️ ABORT: Checking price drop")
                Log.d(TAG, "  Initial: €${initialPrice / 100}/BCH")
                Log.d(TAG, "  Current: €${oraclePrice.price / 100}/BCH (oracle)")
                Log.d(TAG, "  Drop: ${priceDrop.toInt()}% (abort needs ≥6.5%)")

                // Build oracle signature map from API response
                val oracleSig = mapOf(
                    "message" to oraclePrice.message,
                    "signature" to oraclePrice.signature,
                    "price" to oraclePrice.price  // Price in cents (e.g., 59800 = €598.00)
                )

                // Build covenant parameters map
                val covenantParams = mapOf(
                    "senderPubkey" to (remittance.senderPubkey ?: ""),
                    "recipientPubkey" to (remittance.recipientPubkey ?: ""),
                    "funderPubkey" to (remittance.funderPubkey ?: ""),
                    "oraclePubkey" to (remittance.oraclePubkey ?: ""),
                    "eurCents" to (remittance.eurCents ?: 0),
                    "expiryOracleTime" to (remittance.expiryOracleTime ?: 0L),
                    "initialBchPriceInCents" to (remittance.initialBchPriceInCents ?: 0),
                    "minPricePercent" to (remittance.minPricePercent ?: 93)
                )

                Log.d(TAG, "Calling CovenantWebView.executeAbortHybrid() [v0.2]...")

                // Call abort (v0.2 hybrid: Kotlin fetches UTXOs, JS builds, Kotlin broadcasts)
                val txid = covenantWebView.executeAbortHybrid(
                    covenantAddress = remittance.covenantAddress ?: throw Exception("Covenant address not found"),
                    covenantParams = covenantParams,
                    oracleSig = oracleSig,
                    senderWIF = senderWIF,
                    senderAddress = activeWallet.address
                )

                Log.d(TAG, "✅ ✅ ✅ ABORT SUCCESSFUL!")
                Log.d(TAG, "Transaction ID: $txid")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // Save pending transaction (RS083 pattern)
                val pendingTx = PendingTransaction(
                    txid = txid,
                    tx_hex = "",
                    status = TransactionStatus.BROADCAST,
                    covenant_address = remittance.covenantAddress,
                    remittance_id = remittance.id
                )
                database.pendingTransactionDao().insert(pendingTx)
                Log.d(TAG, "Saved pending transaction: $txid")

                // Update remittance status in database
                val updatedRemittance = remittance.copy(
                    status = RemittanceStatus.ABORTED,
                    txid = txid
                )
                database.remittanceDao().updateRemittance(updatedRemittance)
                Log.d(TAG, "Updated remittance status to ABORTED")

                // Tell the FUNDER it was aborted — a hint; the funder still verifies on-chain (`58`)
                com.asgaya.husk.covenant.CovenantResolutionNotifier.notifyNpub(
                    this@RemittanceActivity,
                    remittance.covenantAddress ?: "",
                    remittance.funderNostrPubkey,
                    com.asgaya.husk.data.CovenantResolved.PATH_ABORT,
                    txid
                )

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RemittanceActivity,
                        "✅ Abort successful!\n\nTXID: ${txid.take(20)}...",
                        Toast.LENGTH_LONG
                    ).show()
                }

                // Reload remittances to refresh UI
                loadRemittanceHistory()

            } catch (e: Exception) {
                Log.e(TAG, "❌ Abort failed", e)
                Log.e(TAG, "  Error: ${e.message}")

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RemittanceActivity,
                        "❌ Abort failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun handleCashOut(remittance: Remittance) {
        continueCashOut(remittance)
    }

    /**
     * Recipient-side caveat (`supported-fiats/01 §5`; Suso 2026-10-06): the **cash-out currency**
     * (the merchant's asset) may be the official rate, not the street — the recipient is the
     * risk-bearer AND the best source of the real rate. (The sender only gets the inline caveat.)
     */
    private fun showProvisionalRateWarning(currency: String, eurCents: Int?, onProceed: () -> Unit) {
        val label = SupportedFiats.label(currency)
        android.app.AlertDialog.Builder(this)
            .setTitle("⚠️ Unverified rate")
            .setMessage(
                "We haven't verified the rate for $label.\n\n" +
                    "It may be the official rate, not the street rate — you could get less " +
                    "$currency than shown. Check the real rate with the merchant first."
            )
            .setCancelable(false)
            .setPositiveButton("I understand — continue") { _, _ -> onProceed() }
            .setNeutralButton("Report the real rate") { _, _ ->
                reportRate(currency, eurCents); showProvisionalRateWarning(currency, eurCents, onProceed)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /** Publish a signed `[ASGAYA_RATE_REPORT]` note to the relays — a *signal*, not a source. */
    private fun reportRate(currency: String, eurCents: Int?) {
        lifecycleScope.launch {
            try {
                val wallet = walletManager.getActiveWallet() ?: return@launch
                val manager = nostrKeyManager ?: NostrKeyManager(this@RemittanceActivity).also { nostrKeyManager = it }
                val keys = manager.ensureNostrKeys(wallet)
                val content = "[ASGAYA_RATE_REPORT]\ncurrency=$currency\n" +
                    "eurCents=$eurCents\ntimestamp=${System.currentTimeMillis() / 1000}\n[/ASGAYA_RATE_REPORT]"
                val event = com.asgaya.husk.nostr.NostrEvent.buildEvent(
                    pubkey = keys.publicKeyHex,
                    createdAt = System.currentTimeMillis() / 1000,
                    kind = 1,
                    tags = listOf(listOf("t", "asgaya-rate-report")),
                    content = content,
                    privKey = keys.privateKeyHex
                )
                val client = NostrClient.getInstance(keys.publicKeyHex, null)
                client.connect()
                val sent = client.publishEvent(event)
                Toast.makeText(this@RemittanceActivity, "Thanks — rate report sent to $sent relay(s)", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e(TAG, "Rate report failed", e)
            }
        }
    }

    private fun continueCashOut(remittance: Remittance) {
        Log.d(TAG, "🏪 Cash out at merchant requested for remittance: ${remittance.id}")
        Log.d(TAG, "  Covenant address: ${remittance.covenantAddress}")
        Log.d(TAG, "  EUR: €${remittance.amountPaymentCurrency}")

        lifecycleScope.launch {
            // Show the amount in the recipient's own currency (the covenant EUR face is internal).
            val cur = getSharedPreferences("BizumParserPrefs", android.content.Context.MODE_PRIVATE)
                .getString("fiat_currency", "EUR") ?: "EUR"
            val amountStr = com.asgaya.husk.FiatDisplay.amount(
                remittance.eurCents ?: 0, cur,
                if (cur == "EUR") 1.0 else com.asgaya.husk.FiatRateSource.eurPer(cur)
            )
            // MERCHANT-FIRST FLOW: Nostr (primary) or QR (offline fallback)
            android.app.AlertDialog.Builder(this@RemittanceActivity)
                .setTitle("🏪 Sell BCH at a merchant")
                .setMessage(
                    "Sell $amountStr worth of BCH for fiat?\n\n" +
                    "📡 Send to merchant (Nostr): pick a merchant, they quote a fresh price, " +
                    "you co-sign, they broadcast and pay you.\n\n" +
                    "📷 Show QR (offline): for when you have no data; the merchant scans your QR."
                )
                .setPositiveButton("📡 Send to merchant") { _, _ ->
                    pickMerchantAndSend(remittance)
                }
                .setNeutralButton("📷 Show QR (offline)") { _, _ ->
                    generateSimpleCashoutQR(remittance)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    /** Build the [BCH_FOR_SALE] request from a remittance. */
    private fun buildSimpleCashoutRequest(remittance: Remittance, merchant: com.asgaya.husk.data.Listing): SimpleCashoutRequest =
        SimpleCashoutRequest(
            covenantAddress = remittance.covenantAddress ?: throw Exception("No covenant address"),
            senderPubkey = remittance.senderPubkey ?: throw Exception("No sender pubkey"),
            recipientPubkey = remittance.recipientPubkey ?: throw Exception("No recipient pubkey"),
            funderPubkey = remittance.funderPubkey ?: throw Exception("No funder pubkey"),
            oraclePubkey = remittance.oraclePubkey ?: throw Exception("No oracle pubkey"),
            eurCents = remittance.eurCents ?: throw Exception("No eurCents"),
            expiryOracleTime = remittance.expiryOracleTime ?: throw Exception("No expiry time"),
            initialBchPriceInCents = remittance.initialBchPriceInCents ?: throw Exception("No initial price"),
            minPricePercent = remittance.minPricePercent ?: throw Exception("No min price percent"),
            assetCurrency = merchant.assets.firstOrNull()  // the merchant's ad asset → the exchange currency (`ui-rethink/40`)
        )

    /**
     * Recipient Nostr path: pick a merchant from the **bulletin board** (mock for Phase 0 —
     * same source as the sender leg), send [BCH_FOR_SALE], then observe for the merchant's
     * [BCH_PURCHASE_COSIGN] and co-sign it. Swapping the mock for the real on-chain board
     * later changes only the listing source, not this flow.
     */
    private fun pickMerchantAndSend(remittance: Remittance) {
        // The recipient's wanted cash-out currency = the app's main (local) currency; the method =
        // the user's methods. Shared `asset × method` filter (`bulletin-board/16`/`17`).
        val wantedCurrency = getSharedPreferences("BizumParserPrefs", android.content.Context.MODE_PRIVATE)
            .getString("fiat_currency", "EUR") ?: "EUR"
        val myMethods = com.asgaya.husk.IdentityPrefs.load(this).methods
        lifecycleScope.launch {
            val listings = withContext(Dispatchers.IO) {
                try {
                    val active = walletManager.getActiveWallet()
                    val manager = nostrKeyManager ?: NostrKeyManager(this@RemittanceActivity).also { nostrKeyManager = it }
                    val myNpub = active?.let { manager.ensureNostrKeys(it).publicKeyHex }
                    // F3 (file 61): never offer the user their OWN ad. A merchant ad needs an npub
                    // (Nostr coordination). Shared filter: asset (wanted local currency) × method.
                    com.asgaya.husk.data.BoardFilter.matching(
                        com.asgaya.husk.data.NostrBoardSource(database.boardEventDao()).listings()
                            .filter { !it.nostrPubkey.isNullOrBlank() },
                        com.asgaya.husk.data.AdType.BUYER,
                        asset = wantedCurrency,
                        methods = myMethods,
                        myPublicKey = active?.publicKey,
                        myNostrPubkey = myNpub
                    ).sortedBy { it.fee }  // best (lowest) fee for the recipient first (`bulletin-board/20`)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load bulletin board listings", e)
                    emptyList()
                }
            }
            if (listings.isEmpty()) {
                showNoMerchantsDialog(wantedCurrency)
                return@launch
            }
            val labels = listings.map { "${it.displayName}  (${it.cashAccount}) · Fee ${it.feePercent}" }.toTypedArray()
            android.app.AlertDialog.Builder(this@RemittanceActivity)
                .setTitle("Choose a merchant (bulletin board)")
                .setItems(labels) { _, which ->
                    val merchant = listings[which]
                    // The cash-out currency = the merchant's asset (what they pay). If it's PROVISIONAL,
                    // the local amount may be the official (not street) rate → warn the recipient
                    // (`supported-fiats/01 §5`; Suso 2026-10-06).
                    val cashOutCurrency = merchant.assets.firstOrNull() ?: "EUR"
                    if (SupportedFiats.isProvisional(cashOutCurrency)) {
                        showProvisionalRateWarning(cashOutCurrency, remittance.eurCents) {
                            showCashOutInstructions(remittance, merchant)
                        }
                    } else {
                        showCashOutInstructions(remittance, merchant)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    /**
     * F3 (file 61): a user must never pick their OWN ad. Show a full, blocking message — never a
     * small toast — so a self-selection cannot slip through unnoticed.
     */
    private fun showSelfDealDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("⚠️ You selected your own ad")
            .setMessage(
                "You cannot sell BCH to yourself.\n\n" +
                "This listing belongs to one of YOUR wallets. Pick a different merchant — " +
                "your own ads are never valid counterparties."
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showNoMerchantsDialog(currency: String) {
        android.app.AlertDialog.Builder(this)
            .setTitle("⚠️ No merchants for $currency")
            .setMessage(
                "No merchants for $currency available, select other currency.\n\n" +
                    "The bulletin board has no merchant selling $currency right now.\n\n" +
                    "Your own ads are never shown here — you can't trade with yourself. " +
                    "Try again later, or use another discovery method."
            )
            .setPositiveButton("OK", null)
            .show()
    }

    /**
     * Cash-in-person instructions card, built from the merchant's **board listing** (📍 location +
     * ⏰ hours): the payment method is cash, so the details live in the listing itself — no private
     * DM needed (`ui-rethink/07`). "Send request" starts the merchant-first flow.
     */
    private fun showCashOutInstructions(remittance: Remittance, merchant: com.asgaya.husk.data.Listing) {
        // The cash-out currency = the merchant's asset (what they pay), NOT the covenant's EUR
        // (`remittance.recipientCurrency` is forced to EUR at parse).
        val cashOutCurrency = merchant.assets.firstOrNull()?.takeIf { it.isNotBlank() }
            ?: remittance.recipientCurrency
        val location = merchant.location?.takeIf { it.isNotBlank() } ?: "(location not provided)"
        val hours = merchant.availableHours?.takeIf { it.isNotBlank() }
        lifecycleScope.launch {
            // Estimate in the cash-out currency: EUR face → local, **net of the merchant's fee**
            // (its declared margin, `bulletin-board/20`; a negative fee pays the recipient a premium).
            val rate = if (cashOutCurrency == "EUR") 1.0 else FiatRateSource.eurPer(cashOutCurrency)
            val amountOut = rate?.let { remittance.amountPaymentCurrency * it * (1.0 - merchant.fee) }
            val cash = amountOut?.let { "%.2f".format(it) } ?: "—"
            val message = buildString {
                appendLine("👤 ${merchant.establishment ?: merchant.displayName}")
                appendLine("📍 Where:  $location")
                hours?.let { appendLine("⏰ Hours:  $it") }
                merchant.contactPhone?.let { appendLine("📱 Phone:  $it") }
                appendLine()
                appendLine("You sell  ${"%.8f".format(remittance.amountBch)} BCH")
                appendLine("and receive  $cash $cashOutCurrency in cash")
                if (merchant.fee != 0.0) appendLine("· merchant commission ${merchant.feePercent}")
                merchant.notes?.takeIf { it.isNotBlank() }?.let { appendLine(); appendLine("📝 $it") }
                appendLine()
                appendLine("At the shop, the app walks you through it:")
                appendLine("1. Tap “Send request” — the merchant's app is notified.")
                appendLine("2. The merchant quotes a fresh price; you co-sign.")
                appendLine("3. The merchant broadcasts, then hands you the cash.")
            }
            android.app.AlertDialog.Builder(this@RemittanceActivity)
                .setTitle("🏪 Cash out at ${merchant.displayName}")
                .setMessage(message)
                .setPositiveButton("📡 Send request") { _, _ -> sendBchForSale(remittance, merchant) }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun sendBchForSale(remittance: Remittance, merchant: com.asgaya.husk.data.Listing) {
        val request = try {
            buildSimpleCashoutRequest(remittance, merchant)
        } catch (e: Exception) {
            Toast.makeText(this, "❌ ${e.message}", Toast.LENGTH_LONG).show()
            return
        }
        activeCashoutRequest = request
        activeCashoutMerchantNpub = merchant.nostrPubkey
        Log.i(TAG, "📤 [BCH_FOR_SALE] assetCurrency='${request.assetCurrency}' merchant.assets=${merchant.assets} adId=${merchant.id}")

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val merchantNpub = merchant.nostrPubkey
                if (merchantNpub.isNullOrBlank()) {
                    withContext(Dispatchers.Main) { Toast.makeText(this@RemittanceActivity, "Merchant has no Nostr key", Toast.LENGTH_LONG).show() }
                    return@launch
                }
                val activeWallet = walletManager.getActiveWallet()
                if (activeWallet == null) {
                    withContext(Dispatchers.Main) { Toast.makeText(this@RemittanceActivity, "No active wallet", Toast.LENGTH_LONG).show() }
                    return@launch
                }
                val manager = nostrKeyManager ?: NostrKeyManager(this@RemittanceActivity).also { nostrKeyManager = it }
                val myKeys = manager.ensureNostrKeys(activeWallet)
                // F3 (file 61): loud, blocking guard — the listing's wallet must not be our own.
                if (com.asgaya.husk.data.BoardFilter.isSelf(merchant, activeWallet.publicKey, myKeys.publicKeyHex)) {
                    withContext(Dispatchers.Main) { showSelfDealDialog() }
                    return@launch
                }
                val client = NostrClient.getInstance(myKeys.publicKeyHex, null)
                client.connect()
                val giftWrap = NIP17GiftWrap.wrapDM(request.toMessage(), myKeys.privateKeyHex, myKeys.publicKeyHex, merchantNpub)
                val sent = client.publishEvent(giftWrap)
                Log.i(TAG, "✅ [BCH_FOR_SALE] sent to ${merchant.displayName} via $sent relays")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RemittanceActivity, "📡 Sent to ${merchant.displayName} — waiting for a quote…", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send [BCH_FOR_SALE]", e)
                withContext(Dispatchers.Main) { Toast.makeText(this@RemittanceActivity, "❌ Send failed: ${e.message}", Toast.LENGTH_LONG).show() }
            }
        }

        observeCashoutQuote(request)
    }

    /** Observe for the merchant's quote for this covenant and co-sign it. */
    private fun observeCashoutQuote(request: SimpleCashoutRequest) {
        lifecycleScope.launch {
            database.cashoutMessageDao()
                .observePending(CashoutMessage.TYPE_PURCHASE_COSIGN)
                .collect { rows ->
                    val row = rows.firstOrNull { it.covenantAddress == request.covenantAddress } ?: return@collect
                    database.cashoutMessageDao().markConsumed(row.id)
                    Log.i(TAG, "📩 Quote received for ${request.covenantAddress.take(20)}…")
                    handleMerchantCosignQR(row.payload, request)
                }
        }
    }

    /**
     * MERCHANT-FIRST FLOW: Generate simple QR with just covenant params
     * (No signatures! Merchant will fetch fresh oracle and pre-sign)
     */
    private fun generateSimpleCashoutQR(remittance: Remittance) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.d(TAG, "🏪 GENERATING SIMPLE CASHOUT REQUEST (MERCHANT-FIRST)")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RemittanceActivity, "📱 Generating QR code...", Toast.LENGTH_SHORT).show()
                }

                // Create SimpleCashoutRequest (just covenant params, NO signatures!)
                val cur = getSharedPreferences("BizumParserPrefs", android.content.Context.MODE_PRIVATE)
                    .getString("fiat_currency", "EUR") ?: "EUR"
                val request = SimpleCashoutRequest(
                    covenantAddress = remittance.covenantAddress ?: throw Exception("No covenant address"),
                    senderPubkey = remittance.senderPubkey ?: throw Exception("No sender pubkey"),
                    recipientPubkey = remittance.recipientPubkey ?: throw Exception("No recipient pubkey"),
                    funderPubkey = remittance.funderPubkey ?: throw Exception("No funder pubkey"),
                    oraclePubkey = remittance.oraclePubkey ?: throw Exception("No oracle pubkey"),
                    eurCents = remittance.eurCents ?: throw Exception("No eurCents"),
                    expiryOracleTime = remittance.expiryOracleTime ?: throw Exception("No expiry time"),
                    initialBchPriceInCents = remittance.initialBchPriceInCents ?: throw Exception("No initial price"),
                    minPricePercent = remittance.minPricePercent ?: throw Exception("No min price percent"),
                    assetCurrency = cur
                )

                // Generate [BCH_FOR_SALE] message (simple! ~400 chars)
                val message = request.toMessage()

                Log.d(TAG, "✅ Generated [BCH_FOR_SALE] message")
                Log.d(TAG, "Message length: ${message.length} chars (very scannable!)")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // Copy to clipboard (fallback)
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Asgaya Cashout Request", message)
                clipboard.setPrimaryClip(clip)

                Log.d(TAG, "📋 Copied to clipboard")

                // Show QR code dialog (merchant will scan this!)
                val localEstimate = com.asgaya.husk.FiatDisplay.amount(
                    request.eurCents, cur,
                    if (cur == "EUR") 1.0 else com.asgaya.husk.FiatRateSource.eurPer(cur)
                )
                withContext(Dispatchers.Main) {
                    showSimpleCashoutQRDialog(message, request, localEstimate)
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Cashout QR generation failed", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RemittanceActivity,
                        "❌ Failed to generate QR: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun handleClaim(remittance: Remittance) {
        Log.d(TAG, "💰 Claim requested for remittance: ${remittance.id}")
        Log.d(TAG, "  Covenant address: ${remittance.covenantAddress}")
        Log.d(TAG, "  EUR: €${remittance.amountPaymentCurrency}")

        // Show confirmation dialog
        android.app.AlertDialog.Builder(this)
            .setTitle("💰 Claim Covenant")
            .setMessage(
                "Claim €${remittance.amountPaymentCurrency}?\n\n" +
                "You will receive the payment amount at current BCH price.\n\n" +
                "Covenant: ${remittance.covenantAddress?.take(30)}..."
            )
            .setPositiveButton("Claim") { _, _ ->
                executeClaim(remittance)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun executeClaim(remittance: Remittance) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.d(TAG, "💰 EXECUTING CLAIM")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RemittanceActivity, "💰 Claiming covenant...", Toast.LENGTH_SHORT).show()
                }

                // Find wallet that matches the recipientPubkey in the covenant
                val recipientPubkey = remittance.recipientPubkey
                    ?: throw Exception("No recipientPubkey in remittance")

                Log.d(TAG, "Looking for wallet with pubkey: $recipientPubkey")
                val recipientWallet = walletManager.findWalletByPubkey(recipientPubkey)
                    ?: throw Exception("You don't have the recipient wallet.\n\nThe covenant was created for pubkey:\n${recipientPubkey.take(20)}...\n\nPlease import the recipient wallet to claim.")

                Log.d(TAG, "✅ Found recipient wallet: ${recipientWallet.label}")
                Log.d(TAG, "Recipient address: ${recipientWallet.address}")

                // Get recipient WIF
                val recipientWIF = walletManager.getWIF(recipientWallet)

                // Find seller wallet (for remainder/volatility buffer)
                // NOTE: The funder address is derived from funderPubkey in the JS claim
                // (covenant enforces output[1] = P2PKH(hash160(funder))). The recipient
                // device does NOT need the funder's wallet to claim — the buffer goes to
                // the funder automatically. We only look up the wallet here as a convenience
                // fallback (e.g., sender claiming own covenant on sender device).
                val funderPubkey = remittance.funderPubkey
                    ?: throw Exception("No funderPubkey in remittance")

                Log.d(TAG, "Looking for seller wallet with pubkey: $funderPubkey (optional)")
                val sellerWallet = walletManager.findWalletByPubkey(funderPubkey)
                val funderAddress = sellerWallet?.address ?: ""
                if (sellerWallet != null) {
                    Log.d(TAG, "✅ Found seller wallet: ${sellerWallet.label}")
                } else {
                    Log.d(TAG, "⚠️ Funder wallet not on this device — JS will derive address from funderPubkey")
                }

                // Fetch current price from oracle
                val oraclePrice = fetchOraclePrice()
                Log.d(TAG, "Current BCH price: €${oraclePrice.price / 100.0}/BCH")

                // Build oracle signature map from API response
                val oracleSig = mapOf(
                    "message" to oraclePrice.message,
                    "signature" to oraclePrice.signature,
                    "timestamp" to oraclePrice.timestamp,
                    "price" to oraclePrice.price
                )

                // Build covenant parameters map
                val covenantParams = mapOf(
                    "senderPubkey" to (remittance.senderPubkey ?: ""),
                    "recipientPubkey" to (remittance.recipientPubkey ?: ""),
                    "funderPubkey" to (remittance.funderPubkey ?: ""),
                    "oraclePubkey" to (remittance.oraclePubkey ?: ""),
                    "eurCents" to (remittance.eurCents ?: 0),
                    "expiryOracleTime" to (remittance.expiryOracleTime ?: 0L),
                    "initialBchPriceInCents" to (remittance.initialBchPriceInCents ?: 0),
                    "minPricePercent" to (remittance.minPricePercent ?: 93)
                )

                Log.d(TAG, "━━━ Covenant Parameters ━━━")
                Log.d(TAG, "senderPubkey: ${remittance.senderPubkey}")
                Log.d(TAG, "recipientPubkey: ${remittance.recipientPubkey}")
                Log.d(TAG, "funderPubkey: ${remittance.funderPubkey}")
                Log.d(TAG, "oraclePubkey: ${remittance.oraclePubkey}")
                Log.d(TAG, "eurCents: ${remittance.eurCents}")
                Log.d(TAG, "initialBchPriceInCents: ${remittance.initialBchPriceInCents}")
                Log.d(TAG, "Expected oracle pubkey: 02f2c7e020377863ca973d9b68b20dc40b8da38fcb77536333cde0e886c8400651")

                Log.d(TAG, "Calling CovenantWebView.claimCovenantHybrid() [v0.2]...")

                // Call claim (v0.2 hybrid: Kotlin fetches UTXOs, JS builds, Kotlin broadcasts)
                val txid = covenantWebView.claimCovenantHybrid(
                    covenantAddress = remittance.covenantAddress ?: throw Exception("Covenant address not found"),
                    covenantParams = covenantParams,
                    oracleSig = oracleSig,
                    recipientWIF = recipientWIF,
                    recipientAddress = recipientWallet.address,
                    funderAddress = funderAddress  // May be empty → JS derives from funderPubkey
                )

                Log.d(TAG, "✅ ✅ ✅ CLAIM SUCCESSFUL!")
                Log.d(TAG, "Transaction ID: $txid")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // Save pending transaction (RS083 pattern)
                val pendingTx = PendingTransaction(
                    txid = txid,
                    tx_hex = "",
                    status = TransactionStatus.BROADCAST,
                    covenant_address = remittance.covenantAddress,
                    remittance_id = remittance.id
                )
                database.pendingTransactionDao().insert(pendingTx)
                Log.d(TAG, "Saved pending transaction: $txid")

                // Update remittance status in database
                val updatedRemittance = remittance.copy(
                    status = RemittanceStatus.CLAIMED,
                    txid = txid
                )
                database.remittanceDao().updateRemittance(updatedRemittance)
                Log.d(TAG, "Updated remittance status to CLAIMED")

                // Tell the SENDER it was claimed — a hint; the sender still verifies on-chain (`58`)
                com.asgaya.husk.covenant.CovenantResolutionNotifier.notifyNpub(
                    this@RemittanceActivity,
                    remittance.covenantAddress ?: "",
                    remittance.senderNostrPubkey,
                    com.asgaya.husk.data.CovenantResolved.PATH_CLAIM,
                    txid
                )

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RemittanceActivity,
                        "✅ Claim successful!\n\nTXID: ${txid.take(20)}...",
                        Toast.LENGTH_LONG
                    ).show()
                }

                // Reload remittances to refresh UI
                loadRemittanceHistory()

            } catch (e: Exception) {
                Log.e(TAG, "❌ Claim failed", e)
                Log.e(TAG, "  Error: ${e.message}")

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RemittanceActivity,
                        "❌ Claim failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun setupBottomNavigation() {
        // Activity is no longer a tab (`ui-rethink/41`): Home · Market · Trade.
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                com.asgaya.husk.R.id.nav_wallet -> {
                    // Back to Home (Tab 1)
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }
                com.asgaya.husk.R.id.nav_market -> {
                    startActivity(Intent(this, com.asgaya.husk.trade.MarketActivity::class.java))
                    finish()
                    true
                }
                com.asgaya.husk.R.id.nav_trade -> {
                    // Trade is the passive/merchant surface: require Advanced mode (`ui-rethink/08`).
                    if (!AdvancedMode.isEnabled(this)) {
                        showAdvancedRequiredDialog()
                        false
                    } else {
                        startActivity(Intent(this, com.asgaya.husk.trade.TradeActivity::class.java))
                        finish()
                        true
                    }
                }
                else -> false
            }
        }
    }

    /** The Trade tab needs Advanced mode; warn instead of hiding it. */
    private fun showAdvancedRequiredDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("⚙️ Advanced mode required")
            .setMessage("The Trade tab is for merchants / passive use. Enable Advanced mode in Settings to open it.")
            .setPositiveButton("Open Settings") { _, _ ->
                startActivity(Intent(this, com.asgaya.husk.SettingsActivity::class.java))
            }
            .setNegativeButton("Not now", null)
            .show()
    }

    private fun loadRemittanceHistory() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Loading remittance history...")
                database.remittanceDao().getAllRemittances().collectLatest { remittances ->
                    if (remittances.isEmpty()) {
                        // Show empty state
                        binding.emptyStateText.visibility = View.VISIBLE
                        binding.remittanceList.visibility = View.GONE
                        binding.emptyStateText.text = "No remittances yet.\nTap [+ Send New Remittance] to start!"
                    } else {
                        // Show remittance list
                        binding.emptyStateText.visibility = View.GONE
                        binding.remittanceList.visibility = View.VISIBLE
                        adapter.updateRemittances(remittances)
                        Log.d(TAG, "Loaded ${remittances.size} remittances")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load remittances", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RemittanceActivity,
                        "Failed to load history: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    /**
     * Fetch current BCH price from Asgaya Oracle
     *
     * Returns oracle signature ready for covenant validation
     * Phase 0: Fetches from Pi-chan oracle at http://localhost:3001
     */
    private suspend fun fetchOraclePrice(): OraclePrice = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📡 Fetching price from oracle: $ORACLE_URL")

            // Fetch from oracle API
            val response = URL(ORACLE_URL).readText()
            val json = JSONObject(response)

            val oraclePrice = OraclePrice(
                price = json.getInt("price"),
                timestamp = json.getLong("timestamp"),
                message = json.getString("message"),
                signature = json.getString("signature"),
                pubkey = json.getString("pubkey")
            )

            Log.d(TAG, "✅ Oracle price fetched: €${oraclePrice.price / 100.0}/BCH")
            Log.d(TAG, "   Timestamp: ${oraclePrice.timestamp}")
            Log.d(TAG, "   Message: ${oraclePrice.message}")
            Log.d(TAG, "   Signature: ${oraclePrice.signature.take(32)}...")

            // Verify pubkey matches expected oracle
            if (oraclePrice.pubkey != ORACLE_PUBKEY) {
                Log.w(TAG, "⚠️ Oracle pubkey mismatch!")
                Log.w(TAG, "   Expected: $ORACLE_PUBKEY")
                Log.w(TAG, "   Received: ${oraclePrice.pubkey}")
            }

            oraclePrice
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to fetch oracle price", e)
            throw Exception("Oracle unreachable: ${e.message}")
        }
    }

    /**
     * Generate QR code from text content
     * Uses error correction M (15% recovery) as recommended by TightDS
     */
    private fun generateQRCode(content: String): Bitmap {
        val hints = hashMapOf<EncodeHintType, Any>().apply {
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M)  // 15% recovery (TightDS recommended)
            put(EncodeHintType.MARGIN, 1)  // Thin margin (default is 4)
        }

        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(
            content,
            BarcodeFormat.QR_CODE,
            512,  // Will be scaled by ImageView
            512,
            hints
        )

        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }

        return bitmap
    }

    /**
     * Show QR code dialog for cashout request
     * Boosts screen brightness for better scanning
     */
    private fun showCashoutQRDialog(message: String, remittance: Remittance) {
        val amountFiat = (remittance.eurCents ?: 0) / 100.0
        val amountBCH = (remittance.eurCents ?: 0).toDouble() / (remittance.initialBchPriceInCents ?: 1)

        // Generate QR code
        val qrBitmap = generateQRCode(message)

        // Create dialog view
        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 24, 24, 24)

            // Amount display
            addView(TextView(this@RemittanceActivity).apply {
                text = "€${String.format("%.2f", amountFiat)} for ${String.format("%.8f", amountBCH)} BCH"
                textSize = 18f
                setTypeface(null, android.graphics.Typeface.BOLD)
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#1A1A1A"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 16
                }
            })

            // QR Code image
            addView(ImageView(this@RemittanceActivity).apply {
                setImageBitmap(qrBitmap)
                layoutParams = LinearLayout.LayoutParams(512, 512)
                scaleType = ImageView.ScaleType.FIT_CENTER
            })

            // Instructions
            addView(TextView(this@RemittanceActivity).apply {
                text = "Show this to the merchant"
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#666666"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 16
                }
            })
        }

        // Create dialog
        val dialog = AlertDialog.Builder(this)
            .setTitle("Cash Out Request")
            .setView(dialogView)
            .setPositiveButton("Done", null)
            .setNeutralButton("Copy Text") { _, _ ->
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Asgaya Cashout", message)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "📋 Copied! Paste in Telegram if needed", Toast.LENGTH_SHORT).show()
            }
            .create()

        // Boost brightness when dialog shows, restore when dismissed
        val originalBrightness = window.attributes.screenBrightness
        dialog.setOnShowListener {
            window.attributes = window.attributes.apply {
                screenBrightness = 1.0f  // Max brightness
            }
        }
        dialog.setOnDismissListener {
            window.attributes = window.attributes.apply {
                screenBrightness = originalBrightness
            }
        }

        dialog.show()
    }

    /**
     * MERCHANT-FIRST FLOW: Show simple QR dialog with scanner button
     */
    private fun showSimpleCashoutQRDialog(message: String, request: SimpleCashoutRequest, localEstimate: String) {
        // Generate QR code
        val qrBitmap = generateQRCode(message)

        // Create dialog layout
        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            gravity = Gravity.CENTER

            // Amount text
            addView(TextView(this@RemittanceActivity).apply {
                text = "$localEstimate Cashout"
                textSize = 20f
                setTypeface(null, android.graphics.Typeface.BOLD)
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#333333"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 16
                }
            })

            // QR Code image
            addView(ImageView(this@RemittanceActivity).apply {
                setImageBitmap(qrBitmap)
                layoutParams = LinearLayout.LayoutParams(512, 512)
                scaleType = ImageView.ScaleType.FIT_CENTER
            })

            // Instructions
            addView(TextView(this@RemittanceActivity).apply {
                text = "1. Show this QR to merchant\n2. Merchant will scan & show you their QR\n3. Tap 'Scan Merchant QR' below"
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#666666"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 16
                }
            })
        }

        // Create dialog
        val dialog = AlertDialog.Builder(this)
            .setTitle("Cash Out Request")
            .setView(dialogView)
            .setPositiveButton("Scan Merchant QR") { _, _ ->
                // Launch QR scanner to scan merchant's response
                launchMerchantQRScanner(request)
            }
            .setNeutralButton("Copy Text") { _, _ ->
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Asgaya Cashout Request", message)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "📋 Copied!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()

        // Boost brightness when dialog shows, restore when dismissed
        val originalBrightness = window.attributes.screenBrightness
        dialog.setOnShowListener {
            window.attributes = window.attributes.apply {
                screenBrightness = 1.0f  // Max brightness
            }
        }
        dialog.setOnDismissListener {
            window.attributes = window.attributes.apply {
                screenBrightness = originalBrightness
            }
        }

        dialog.show()
    }

    /**
     * MERCHANT-FIRST FLOW: Launch QR scanner to scan merchant's response
     */
    private fun launchMerchantQRScanner(originalRequest: SimpleCashoutRequest) {
        Log.d(TAG, "📷 Launching QR scanner for merchant response...")
        pendingCashoutRequest = originalRequest  // Store for scanner callback

        val options = com.journeyapps.barcodescanner.ScanOptions()
        options.setDesiredBarcodeFormats(com.journeyapps.barcodescanner.ScanOptions.QR_CODE)
        options.setPrompt("Scan merchant's QR code")
        options.setCameraId(0)
        options.setBeepEnabled(false)
        options.setBarcodeImageEnabled(false)
        options.setTimeout(30000)  // 30 second timeout
        options.setOrientationLocked(true)

        merchantQRScanner.launch(options)
    }

    /**
     * MERCHANT-FIRST FLOW: Handle merchant's cosign QR response
     *
     * TightDS Amendment 1: Fresh-price validation by RECIPIENT
     * - Extract oracle timestamp from message
     * - Validate < 60 seconds old
     * - Validate price is fair (>= minPricePercent floor)
     * - Show acceptance dialog with price
     */
    private fun handleMerchantCosignQR(qrContent: String, originalRequest: SimpleCashoutRequest) {
        Log.d(TAG, "📷 Processing merchant cosign QR: ${qrContent.length} chars")

        try {
            // Parse merchant's response
            val response = MerchantCosignResponse.fromMessage(qrContent)
            if (response == null) {
                Log.e(TAG, "❌ Failed to parse merchant QR: Invalid format")
                Toast.makeText(this, "❌ Invalid merchant QR code", Toast.LENGTH_LONG).show()
                return
            }

            Log.d(TAG, "✅ Parsed merchant response:")
            Log.d(TAG, "  merchantPubkey: ${response.merchantPubkey}")
            Log.d(TAG, "  currentPrice: €${response.currentPrice}/BCH")
            Log.d(TAG, "  amountCash: ${response.currency ?: "EUR"} ${response.amountCash}")
            Log.d(TAG, "  amountBCH: ${response.amountBCH} BCH")
            Log.d(TAG, "  oracleMessage: ${response.oracleMessage}")
            Log.d(TAG, "  transactionHex length: ${response.transactionHex.length} chars")

            // Track the active quote for the returned [SIGNED_TX]
            activeCashoutCovenantAddress = response.covenantAddress
            activeCashoutQuoteId = response.quoteId

            // TightDS Amendment 1: RECIPIENT validates oracle freshness
            val (isFresh, oracleAge) = validateOracleFreshness(response.oracleMessage)
            Log.d(TAG, "  Oracle age: ${oracleAge}s (fresh: $isFresh)")

            if (!isFresh) {
                Toast.makeText(
                    this,
                    "❌ Merchant's oracle is too old (${oracleAge}s)! Ask for fresh price.",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            // Validate price against floor
            val minPriceCents = (originalRequest.initialBchPriceInCents * originalRequest.minPricePercent) / 100
            val minPriceEuros = minPriceCents / 100.0
            Log.d(TAG, "  Price floor: €${minPriceEuros} (${originalRequest.minPricePercent}% of initial €${originalRequest.initialBchPriceInCents / 100.0})")

            if (response.currentPrice < minPriceEuros) {
                Toast.makeText(
                    this,
                    "❌ Merchant's price (€${response.currentPrice}) is below floor (€${minPriceEuros})!",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            Log.d(TAG, "✅ Price validation passed: €${response.currentPrice} >= €${minPriceEuros}")

            // Show acceptance dialog
            showCosignAcceptanceDialog(response, originalRequest, oracleAge)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to parse merchant QR", e)
            Toast.makeText(
                this,
                "❌ Invalid merchant QR: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Validate oracle freshness (TightDS Amendment 1)
     * Returns (isFresh, ageInSeconds)
     */
    private fun validateOracleFreshness(oracleMessage: String): Pair<Boolean, Int> {
        if (oracleMessage.length < 16) {
            return Pair(false, -1)
        }

        try {
            // Oracle message format: 8 bytes timestamp (little-endian) + 8 bytes price
            val timestampHex = oracleMessage.substring(0, 16)

            // Convert from little-endian hex to Unix timestamp
            val timestamp = java.lang.Long.parseLong(
                timestampHex.chunked(2).reversed().joinToString(""),
                16
            )

            val currentTime = System.currentTimeMillis() / 1000
            val age = (currentTime - timestamp).toInt()
            val isFresh = age < 60  // < 60 seconds for face-to-face

            return Pair(isFresh, age)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse oracle timestamp", e)
            return Pair(false, -1)
        }
    }

    /**
     * Show dialog for recipient to accept merchant's price and co-sign
     */
    private fun showCosignAcceptanceDialog(
        response: MerchantCosignResponse,
        originalRequest: SimpleCashoutRequest,
        oracleAge: Int
    ) {
        val cur = response.currency ?: "EUR"
        val eurAmount = (originalRequest.eurCents / 100.0).takeIf { it > 0.0 } ?: 1.0
        val rateLine = if (cur.equals("EUR", ignoreCase = true)) {
            "📈 Rate: €${"%.2f".format(response.currentPrice)}/BCH"
        } else {
            "📈 Rate: 1 EUR = $cur ${"%.2f".format(response.amountCash / eurAmount)}"
        }
        AlertDialog.Builder(this)
            .setTitle("Accept Merchant's Offer?")
            .setMessage(
                "Merchant offers:\n\n" +
                "💶 Cash: $cur ${"%.2f".format(response.amountCash)}\n" +
                "💰 BCH: ${response.amountBCH} BCH\n" +
                "$rateLine\n" +
                "⏱️ Oracle: ${oracleAge}s old (fresh!)\n\n" +
                "Co-sign this transaction?"
            )
            .setPositiveButton("Accept & Co-Sign") { _, _ ->
                lifecycleScope.launch {
                    performCosigning(response, originalRequest)
                }
            }
            .setNegativeButton("Reject") { _, _ ->
                Toast.makeText(this, "Offer rejected", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    /**
     * Perform recipient co-signing
     */
    private suspend fun performCosigning(
        response: MerchantCosignResponse,
        originalRequest: SimpleCashoutRequest
    ) {
        try {
            Log.d(TAG, "=== Recipient Co-Signing ===")

            // Get recipient's WIF
            val walletManager = WalletManager.getInstance(this)
            val wallet = walletManager.getActiveWallet()
            if (wallet == null) {
                Toast.makeText(this, "❌ No active wallet", Toast.LENGTH_SHORT).show()
                return
            }

            val recipientWIF = walletManager.getWIF(wallet)
            val recipientAddress = wallet.address ?: ""
            Log.d(TAG, "Got recipient WIF and address: $recipientAddress")

            // Step 1: Fetch UTXOs for covenant (same as merchant used)
            Log.d(TAG, "📡 Fetching covenant UTXOs...")
            val utxos = withContext(Dispatchers.IO) {
                com.asgaya.husk.covenant.CovenantBuildService.fetchUTXOs(originalRequest.covenantAddress)
            }
            Log.d(TAG, "✅ Found ${utxos.size} UTXOs")

            if (utxos.isEmpty()) {
                throw Exception("No UTXOs found - covenant already spent or not funded")
            }

            // Step 2: Build covenant params from original request
            val covenantParams = buildCovenantParamsMap(originalRequest)

            // Step 3: Call JavaScript to co-sign
            Log.d(TAG, "🔨 Calling JavaScript to co-sign...")
            val result = suspendCancellableCoroutine<String> { continuation ->

                val covenantWebView = CovenantWebView.getInstance(this@RemittanceActivity)

                // Set up listener
                covenantWebView.recipientCoSignListener = object : CovenantWebView.TransactionBuildListener {
                    override fun onBuilt(resultJson: String) {
                        Log.d(TAG, "✅ Recipient co-sign complete!")
                        covenantWebView.recipientCoSignListener = null
                        continuation.resume(resultJson)
                    }

                    override fun onError(errorMessage: String) {
                        Log.e(TAG, "❌ Recipient co-sign failed: $errorMessage")
                        covenantWebView.recipientCoSignListener = null
                        continuation.resumeWithException(Exception("Co-sign failed: $errorMessage"))
                    }
                }

                // Convert UTXOs to JSON
                val utxosJson = org.json.JSONArray()
                utxos.forEach { utxo ->
                    utxosJson.put(org.json.JSONObject().apply {
                        put("txHash", utxo.txHash)
                        put("txPos", utxo.txPos)
                        put("value", utxo.value)
                        put("height", utxo.height)
                    })
                }

                // Get addresses from merchant's response
                // The merchant already built the transaction with specific addresses
                val merchantAddress = response.merchantAddress
                val funderAddress = response.funderAddress

                Log.d(TAG, "Using addresses from merchant:")
                Log.d(TAG, "  Merchant: $merchantAddress")
                Log.d(TAG, "  Funder: $funderAddress")

                // Build parameters
                val params = org.json.JSONObject().apply {
                    put("merchantTxHex", response.transactionHex)
                    put("covenantParams", org.json.JSONObject(covenantParams))
                    put("recipientWIF", recipientWIF)
                    put("merchantPubkey", response.merchantPubkey)
                    put("oracleSig", org.json.JSONObject().apply {
                        put("signature", response.oracleSig)
                        put("message", response.oracleMessage)
                        put("price", (response.currentPrice * 100).toInt()) // Convert to cents
                    })
                    put("utxos", utxosJson)
                    put("merchantAddress", merchantAddress)
                    put("funderAddress", funderAddress)
                }

                val paramsJson = params.toString()
                val jsCode = "buildRecipientCoSignTransaction('${paramsJson.replace("'", "\\'")}')"

                Log.d(TAG, "Calling buildRecipientCoSignTransaction()...")

                // Call JavaScript
                covenantWebView.webView.post {
                    covenantWebView.webView.evaluateJavascript(jsCode) { result ->
                        Log.d(TAG, "JavaScript call initiated: $result")
                    }
                }

                // Handle cancellation
                continuation.invokeOnCancellation {
                    covenantWebView.recipientCoSignListener = null
                }
            }

            // Step 4: Parse result
            val resultObj = org.json.JSONObject(result)
            val fullySignedTxHex = resultObj.getString("txHex")

            Log.d(TAG, "✅ Transaction fully signed!")
            Log.d(TAG, "  txHex length: ${fullySignedTxHex.length} chars")

            // Nostr path: send the signed tx back to the merchant automatically
            sendSignedTxViaNostr(fullySignedTxHex)

            // Step 5: Show fully-signed transaction to merchant (QR fallback)
            showFullySignedTransactionDialog(fullySignedTxHex, response.amountCash, response.currency ?: "EUR")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Co-signing failed", e)
            Toast.makeText(
                this,
                "❌ Co-signing failed: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Nostr path: send the fully-signed transaction back to the merchant.
     * No-op on the QR path (merchantNpub null → the QR dialog handles it).
     */
    private fun sendSignedTxViaNostr(txHex: String) {
        val merchantNpub = activeCashoutMerchantNpub ?: return
        val message = buildString {
            append("[SIGNED_TX]\n")
            append("covenantAddress=${activeCashoutCovenantAddress ?: ""}\n")
            append("quoteId=${activeCashoutQuoteId ?: ""}\n")
            append("txHex=$txHex\n")
            append("[/SIGNED_TX]")
        }
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val activeWallet = walletManager.getActiveWallet() ?: return@launch
                val manager = nostrKeyManager ?: NostrKeyManager(this@RemittanceActivity).also { nostrKeyManager = it }
                val myKeys = manager.ensureNostrKeys(activeWallet)
                val client = NostrClient.getInstance(myKeys.publicKeyHex, null)
                client.connect()
                val gw = NIP17GiftWrap.wrapDM(message, myKeys.privateKeyHex, myKeys.publicKeyHex, merchantNpub)
                val sent = client.publishEvent(gw)
                Log.i(TAG, "✅ [SIGNED_TX] sent to merchant via $sent relays")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RemittanceActivity, "📡 Signed tx sent — waiting for the merchant to pay", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send [SIGNED_TX]", e)
            }
        }
    }

    private fun buildCovenantParamsMap(request: SimpleCashoutRequest): Map<String, Any> {
        return mapOf(
            "senderPubkey" to request.senderPubkey,
            "recipientPubkey" to request.recipientPubkey,
            "funderPubkey" to request.funderPubkey,
            "oraclePubkey" to request.oraclePubkey,
            "eurCents" to request.eurCents,
            "expiryOracleTime" to request.expiryOracleTime,
            "initialBchPriceInCents" to request.initialBchPriceInCents,
            "minPricePercent" to request.minPricePercent
        )
    }

    private fun showFullySignedTransactionDialog(txHex: String, amountCash: Double, currency: String) {
        Log.d(TAG, "Showing fully-signed transaction dialog")

        // Create a dialog with the signed transaction hex
        val message = "✅ Co-Signed Successfully!\n\n" +
                "📱 Show this to merchant\n" +
                "💰 You'll receive: $currency ${"%.2f".format(amountCash)} cash\n\n" +
                "Transaction (tap to copy):\n${txHex.take(60)}...\n\n" +
                "Merchant will broadcast this transaction."

        AlertDialog.Builder(this)
            .setTitle("✅ Ready for Merchant")
            .setMessage(message)
            .setPositiveButton("Copy Transaction") { _, _ ->
                // Copy to clipboard
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Signed Transaction", txHex)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "📋 Transaction copied!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Show QR") { _, _ ->
                // TODO: Show QR code with the transaction hex
                showTransactionQR(txHex, amountCash, currency)
            }
            .setNeutralButton("Done", null)  // stay on Activity — dispute if the cash isn't handed over
            .show()
    }

    private fun showTransactionQR(txHex: String, amountCash: Double, currency: String) {
        Log.d(TAG, "📱 Generating QR for fully-signed transaction...")

        // Generate QR code on background thread
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                // Format the message for the merchant to broadcast
                val qrMessage = buildString {
                    append("[SIGNED_TX]\n")
                    append("covenantAddress=${activeCashoutCovenantAddress ?: ""}\n")
                    append("quoteId=${activeCashoutQuoteId ?: ""}\n")
                    append("txHex=$txHex\n")
                    append("[/SIGNED_TX]")
                }

                val qrBitmap = com.google.zxing.qrcode.QRCodeWriter().encode(
                    qrMessage,
                    com.google.zxing.BarcodeFormat.QR_CODE,
                    800, // width
                    800  // height
                ).let { bitMatrix ->
                    val width = bitMatrix.width
                    val height = bitMatrix.height
                    val pixels = IntArray(width * height)
                    for (y in 0 until height) {
                        val offset = y * width
                        for (x in 0 until width) {
                            pixels[offset + x] = if (bitMatrix[x, y]) {
                                android.graphics.Color.BLACK
                            } else {
                                android.graphics.Color.WHITE
                            }
                        }
                    }
                    android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.RGB_565).apply {
                        setPixels(pixels, 0, width, 0, 0, width, height)
                    }
                }

                // Show dialog on main thread
                withContext(Dispatchers.Main) {
                    showSignedTransactionQRDialog(qrBitmap, txHex, amountCash, currency)
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ QR generation failed", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RemittanceActivity, "QR generation failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showSignedTransactionQRDialog(qrBitmap: android.graphics.Bitmap, txHex: String, amountCash: Double, currency: String) {
        if (isFinishing || isDestroyed) {
            Log.w(TAG, "⚠️ Activity is finishing/destroyed, cannot show QR dialog")
            return
        }

        // Create dialog view programmatically
        val dialogView = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            gravity = android.view.Gravity.CENTER

            // Title
            addView(android.widget.TextView(this@RemittanceActivity).apply {
                text = "✅ Ready for Merchant"
                textSize = 20f
                setTypeface(null, android.graphics.Typeface.BOLD)
                gravity = android.view.Gravity.CENTER
                setTextColor(android.graphics.Color.parseColor("#333333"))
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 16 }
            })

            // Details
            addView(android.widget.TextView(this@RemittanceActivity).apply {
                text = "Show this QR to the merchant to broadcast.\n\n" +
                       "Amount: $currency ${String.format("%.2f", amountCash)}\n" +
                       "Transaction hex: ${txHex.length} chars"
                textSize = 14f
                gravity = android.view.Gravity.CENTER
                setTextColor(android.graphics.Color.parseColor("#666666"))
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 24 }
            })

            // QR Code
            addView(android.widget.ImageView(this@RemittanceActivity).apply {
                setImageBitmap(qrBitmap)
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    800,
                    800
                )
            })
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Copy Hex") { _, _ ->
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Signed Transaction", txHex)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "📋 Transaction hex copied!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Done", null)
            .setCancelable(true)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Refresh history when returning from send flow
        loadRemittanceHistory()
        startTimeoutWatch()
    }

    override fun onPause() {
        super.onPause()
        timeoutJob?.cancel()
        timeoutJob = null
    }

    /** Periodic timeout watch (auto-refund, `ui-rethink/09`) while this screen is resumed. */
    private fun startTimeoutWatch() {
        timeoutJob?.cancel()
        timeoutJob = lifecycleScope.launch {
            while (isActive) {
                checkTimeouts()
                delay(30_000)
            }
        }
    }

    private suspend fun checkTimeouts() {
        val prefs = getSharedPreferences("BizumParserPrefs", Context.MODE_PRIVATE)
        val notified = (prefs.getStringSet("timeout_notified", emptySet()) ?: emptySet()).toMutableSet()
        val mode = AutoRefundPrefs.load(this)
        var added = false

        // (A) Every involved covenant that expired unclaimed → notify the device's user
        // (`ui-rethink/39`): sent (refund available) and received (no longer claimable). Same
        // dedup as the always-on service, so we never double-notify.
        com.asgaya.husk.covenant.CovenantResolutionNotifier.notifyExpiredOnce(this, database)

        // (B) Seller side — covenants WE funded (funderPubkey == active wallet) that expired
        // with the sender offline: auto-recover our buffer (`sellerRecoverBuffer`, `ui-rethink/10`).
        // The covenant's clock is the ORACLE, so decide the expiry with the oracle timestamp.
        val activePubkey = walletManager.getActiveWallet()?.publicKey
        if (activePubkey != null) {
            val fundedMine = database.covenantDao().getAll().filter { c ->
                c.status == "funded" &&
                    c.funderPubkey == activePubkey &&
                    c.senderPubkey != null && c.recipientPubkey != null && c.oraclePubkey != null
            }
            if (fundedMine.isNotEmpty()) {
                val oracle = runCatching { CovenantRecovery.fetchOraclePrice() }.getOrNull()
                if (oracle != null) {
                    fundedMine.filter { oracle.timestamp >= it.expiryTime }.forEach { c ->
                        val key = "seller:${c.id}"
                        if (key !in notified) {
                            notified.add(key)
                            added = true
                            if (mode == AutoRefundPrefs.Mode.MANUAL) {
                                runOnUiThread {
                                    Toast.makeText(this, "⏰ Expired covenant — buffer recoverable", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this, "🛡️ Expired covenant — recovering buffer…", Toast.LENGTH_LONG).show()
                                }
                                executeSellerRecover(c, oracle)
                            }
                        }
                    }
                }
            }
        }

        if (added) prefs.edit().putStringSet("timeout_notified", notified).apply()
    }
}
