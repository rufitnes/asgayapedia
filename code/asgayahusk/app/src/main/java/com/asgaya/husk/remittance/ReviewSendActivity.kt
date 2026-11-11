package com.asgaya.husk.remittance

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.activity.viewModels
import com.asgaya.husk.viewmodel.SendViewModel
import com.asgaya.husk.viewmodel.SendViewModelFactory
import com.asgaya.husk.viewmodel.TxState
import kotlinx.coroutines.flow.collectLatest
import com.asgaya.husk.AppDatabase
import com.asgaya.husk.CovenantWebView
import com.asgaya.husk.ElectrumClient
import com.asgaya.husk.covenant.CovenantConstants
import com.asgaya.husk.databinding.ActivityReviewSendBinding
import com.asgaya.husk.wallet.WalletManager
import com.asgaya.husk.transaction.PendingTransaction
import com.asgaya.husk.transaction.TransactionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale
import com.asgaya.husk.nostr.NostrClient
import com.asgaya.husk.nostr.NostrKeyManager
import com.asgaya.husk.nostr.NIP17GiftWrap
import org.json.JSONObject

/**
 * Review & Send Screen (Self-Funded Path)
 *
 * Shows transaction details and confirms sending BCH
 * directly from user's wallet (no bulletin board needed)
 *
 * COVENANT CREATION:
 * - Uses WebView + CashScript SDK ONLY (via CovenantWebView.createCovenant())
 * - DO NOT use manual bytecode construction (CovenantBuilder was removed)
 * - Why? WebView encoding matches test scripts exactly, enabling claim/refund
 *
 * See: RS079 - Mobile BCH Wallet Covenant Integration Patterns
 * See: /asgaya/archive/code/ARCHIVE_CovenantBuilder_20260808.kt for why it was removed
 * See: 2026-08-08_covenant-builder-removed-webview-only.md blog entry
 */
class ReviewSendActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReviewSendBinding
    private lateinit var walletManager: WalletManager
    private lateinit var covenantWebView: CovenantWebView
    private lateinit var database: AppDatabase
    private val electrumClient = ElectrumClient()

    // Nostr client for seller coordination (Task 5)
    private lateinit var nostrClient: NostrClient
    private lateinit var nostrKeyManager: NostrKeyManager

    // ViewModel for transaction state management (survives activity destruction)
    private val sendViewModel: SendViewModel by viewModels {
        SendViewModelFactory(database, covenantWebView, electrumClient, applicationContext)
    }

    private var recipientAddress = ""
    private var recipientName: String? = null
    private var recipientCashAccount: String? = null
    private var recipientPubkey: String? = null
    private var amountBch = 0.0
    private var amountFiat = 0.0
    private var currency = "EUR"
    // The sender's committed payment (the datum shown everywhere, `ui-rethink/40`).
    private var payAmount = 0.0
    private var payCurrency = "EUR"
    private var amountRecipient = 0.0
    private var recipientCurrency = "VES"
    private var covenantAddress = ""  // Received from SendRemittanceActivity
    // Selected currency + its rate, for rendering amounts in the user's currency (`FiatDisplay`).
    private var mainCurrency = "EUR"
    @Volatile private var eurPerMain: Double? = null

    // Funding model: "self_funded" (funder = sender) or "funder_funded" (bulletin board seller funds)
    private var fundingModel = FUNDING_MODEL_SELF
    private var funderPubkey: String? = null
    private var funderNpub: String? = null
    private var funderAdId: String? = null
    private var funderFee: Double? = null
    private var funderMinPricePercent: Int? = null

    companion object {
        const val FUNDING_MODEL_SELF = "self_funded"
        const val FUNDING_MODEL_FUNDER = "funder_funded"
        private const val TAG = "ReviewSend"

        // Seller/funder Nostr pubkey is resolved from the contact list by the covenant's
        // funderPubkey (production shape). The bulletin board (Phase 0.5+) will supply it
        // directly on the listing. No hardcoded constant.
        const val EXTRA_RECIPIENT = "recipient"
        const val EXTRA_RECIPIENT_NAME = "recipient_name"
        const val EXTRA_RECIPIENT_CASH_ACCOUNT = "recipient_cash_account"
        const val EXTRA_RECIPIENT_PUBKEY = "recipient_pubkey"
        const val EXTRA_AMOUNT_BCH = "amount_bch"
        const val EXTRA_AMOUNT_FIAT = "amount_fiat"
        const val EXTRA_CURRENCY = "currency"
        const val EXTRA_AMOUNT_RECIPIENT = "amount_recipient"
        const val EXTRA_RECIPIENT_CURRENCY = "recipient_currency"
        const val EXTRA_COVENANT_ADDRESS = "covenant_address"
        // The sender's committed payment amount + currency (the datum shown everywhere, `ui-rethink/40`).
        const val EXTRA_PAY_AMOUNT = "pay_amount"
        const val EXTRA_PAY_CURRENCY = "pay_currency"

        // Funding model: "self_funded" (funder = sender) or "funder_funded" (bulletin board seller funds)
        const val EXTRA_FUNDING_MODEL = "funding_model"
        const val EXTRA_FUNDER_PUBKEY = "funder_pubkey"
        const val EXTRA_FUNDER_NPUB = "funder_npub"
        const val EXTRA_FUNDER_AD_ID = "funder_ad_id"
        const val EXTRA_FUNDER_FEE = "funder_fee"
        const val EXTRA_FUNDER_MIN_PRICE_PERCENT = "funder_min_price_percent"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewSendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        walletManager = WalletManager.getInstance(this)
        covenantWebView = CovenantWebView.getInstance(this)
        database = AppDatabase.getDatabase(this)

        // Initialize Nostr components (Task 5 - sender integration)
        // Note: Initialization in coroutine because getActiveWallet() is suspend
        // NostrClient is application-scoped (singleton) to survive Activity lifecycle
        lifecycleScope.launch {
            nostrKeyManager = NostrKeyManager(this@ReviewSendActivity)
            val activeWallet = walletManager.getActiveWallet()
            if (activeWallet != null) {
                // ensureNostrKeys generates keys if they don't exist (non-nullable)
                val myKeys = nostrKeyManager.ensureNostrKeys(activeWallet)

                // Get or create singleton NostrClient instance
                // Sender passes null handler (doesn't need to receive, only publishes)
                // This prevents clobbering the seller's handler if both roles run in same process
                nostrClient = NostrClient.getInstance(
                    myPubkey = myKeys.publicKeyHex,
                    onEventReceived = null  // Sender only publishes, doesn't receive
                )
                // Connect to relays for publishing (safe to call multiple times)
                nostrClient.connect()
            } else {
                Log.w(TAG, "No active wallet found for Nostr initialization")
            }
        }

        // Get data from intent
        recipientAddress = intent.getStringExtra(EXTRA_RECIPIENT) ?: ""
        recipientName = intent.getStringExtra(EXTRA_RECIPIENT_NAME)
        recipientCashAccount = intent.getStringExtra(EXTRA_RECIPIENT_CASH_ACCOUNT)
        recipientPubkey = intent.getStringExtra(EXTRA_RECIPIENT_PUBKEY)
        amountBch = intent.getDoubleExtra(EXTRA_AMOUNT_BCH, 0.0)
        amountFiat = intent.getDoubleExtra(EXTRA_AMOUNT_FIAT, 0.0)
        currency = intent.getStringExtra(EXTRA_CURRENCY) ?: "EUR"
        payAmount = intent.getDoubleExtra(EXTRA_PAY_AMOUNT, 0.0)
        payCurrency = intent.getStringExtra(EXTRA_PAY_CURRENCY) ?: currency
        amountRecipient = intent.getDoubleExtra(EXTRA_AMOUNT_RECIPIENT, 0.0)
        recipientCurrency = intent.getStringExtra(EXTRA_RECIPIENT_CURRENCY) ?: "VES"

        // Funding model (self-funded default; funder pubkey may be empty → derived later)
        fundingModel = intent.getStringExtra(EXTRA_FUNDING_MODEL) ?: FUNDING_MODEL_SELF
        funderPubkey = intent.getStringExtra(EXTRA_FUNDER_PUBKEY)
        funderNpub = intent.getStringExtra(EXTRA_FUNDER_NPUB)
        funderAdId = intent.getStringExtra(EXTRA_FUNDER_AD_ID)
        funderFee = intent.getDoubleExtra(EXTRA_FUNDER_FEE, -1.0).takeIf { it >= 0 }
        funderMinPricePercent = intent.getIntExtra(EXTRA_FUNDER_MIN_PRICE_PERCENT, -1).takeIf { it > 0 }

        setupUI()
        displayTransactionDetails()
        observeTransactionState()
    }

    /**
     * Observe transaction state from ViewModel
     *
     * This updates the UI based on the current transaction state.
     * Because it uses viewModelScope, the transaction continues even if
     * the activity is destroyed and recreated.
     */
    private fun observeTransactionState() {
        lifecycleScope.launch {
            sendViewModel.txState.collect { state ->
                when (state) {
                    is TxState.Idle -> {
                        // Initial state - do nothing
                    }
                    is TxState.FetchingOracleKey -> {
                        Log.d(TAG, "Fetching oracle key...")
                        // Button already shows "Sending..."
                    }
                    is TxState.CreatingCovenant -> {
                        Log.d(TAG, "Creating covenant...")
                        // Button already shows "Sending..."
                    }
                    is TxState.Broadcasting -> {
                        Log.d(TAG, "Broadcasting transaction...")
                        // Button already shows "Sending..."
                    }
                    is TxState.CovenantCreated -> {
                        Log.d(TAG, "✅ Covenant created (unfunded)!")
                        Log.d(TAG, "Cash Account: ${state.covenantParams.cashAccount}")
                        Log.d(TAG, "Amount: €${state.covenantParams.eurCents / 100.0}")

                        // Copy [FUND_COVENANT] message to clipboard (fallback)
                        val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText(
                            "Fund Covenant Message",
                            state.fundCovenantMessage
                        )
                        clipboard.setPrimaryClip(clip)

                        // Send via Nostr (Task 5 - NIP-17 encrypted to seller)
                        sendViaNostr(state.fundCovenantMessage)

                        // Navigate to success screen with special message
                        Toast.makeText(
                            this@ReviewSendActivity,
                            "✅ Request sent to the seller…",
                            Toast.LENGTH_LONG
                        ).show()

                        // The datum = the sender's committed payment (`ui-rethink/40`). What the sender
                        // actually hands the seller = the datum **+ the seller's fee** (`bulletin-board/18`).
                        val datumCurrency = payCurrency.ifBlank { mainCurrency }
                        val datumAmount = if (payAmount > 0.0) payAmount else amountFiat * (eurPerMain ?: 1.0)
                        val sellerTotal = datumAmount * (1.0 + (funderFee ?: 0.0))
                        val msg = android.text.SpannableStringBuilder("Pay ").apply {
                            val a = length
                            append("$datumCurrency %.2f".format(sellerTotal))
                            setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                                a, length, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            append("  ·  reference ")
                            val r0 = length
                            append(state.covenantParams.cashAccount)
                            setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                                r0, length, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            append("\n\nThe seller needs to confirm the payment before funding the smart contract.")
                        }
                        android.app.AlertDialog.Builder(this@ReviewSendActivity)
                            .setTitle("Request sent to the seller")
                            .setMessage(msg)
                            .setPositiveButton("OK") { _, _ ->
                                // Go to Activity to wait for the funded notification (`ui-rethink/42`).
                                startActivity(
                                    Intent(this@ReviewSendActivity, RemittanceActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                    }
                                )
                                finish()
                            }
                            .show()
                    }
                    is TxState.Success -> {
                        Log.d(TAG, "✅ Success! TXID: ${state.txid}")

                        // Copy covenant details to clipboard
                        copyCovenantToClipboard(state.remittance)

                        // Self-funded: the covenant is already funded → send the recipient the
                        // [COVENANT_V25] offer (the funder-funded path does this in
                        // NotificationListener.monitorAwaitingFunding; `ui-rethink/35`).
                        sendOfferToRecipient(state.remittance)

                        // Show success toast
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@ReviewSendActivity,
                                "✅ Transaction broadcast!\n📋 Details copied - send to recipient via Telegram",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        // Navigate to success - DON'T reset button (RS083 pattern)
                        val intent = Intent(this@ReviewSendActivity, RemittanceActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent)
                        finish()

                        // Reset ViewModel for next transaction
                        sendViewModel.reset()
                    }
                    is TxState.Error -> {
                        Log.e(TAG, "❌ Error: ${state.message}")

                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@ReviewSendActivity,
                                "❌ Failed: ${state.message}",
                                Toast.LENGTH_LONG
                            ).show()

                            // Reset button on error
                            binding.confirmSendButton.isEnabled = true
                            binding.confirmSendButton.text = "Confirm & Send"
                        }

                        // Reset ViewModel
                        sendViewModel.reset()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Fix for stuck "Sending..." button after activity recreation
        // If activity was destroyed while transaction was pending, reset button
        if (binding.confirmSendButton.text == "Sending...") {
            Log.d(TAG, "⚠️ Detected stuck 'Sending...' state, resetting button")
            binding.confirmSendButton.isEnabled = true
            binding.confirmSendButton.text = "Confirm & Send"
        }
    }

    private fun setupUI() {
        // Back button
        binding.backButton.setOnClickListener {
            finish()
        }

        // Confirm & Send — the sender commits a fixed EUR amount, so the local-rate caveat is only
        // *informational* (shown inline in the send form). The **blocking acknowledge + report** live on
        // the **recipient** side (`supported-fiats/01 §5`; Suso 2026-10-06), where the local amount
        // actually matters and the recipient can supply the real rate.
        binding.confirmSendButton.setOnClickListener {
            sendTransaction()
        }
    }

    private fun displayTransactionDetails() {
        // Recipient — CashAccount → contact name → raw address (`ui-rethink/40`).
        val recipient = recipientCashAccount?.takeIf { it.isNotBlank() }
            ?: recipientName?.takeIf { it.isNotBlank() }
            ?: recipientAddress
        mainCurrency = getSharedPreferences("BizumParserPrefs", android.content.Context.MODE_PRIVATE)
            .getString("fiat_currency", "EUR") ?: "EUR"
        val selfFunded = fundingModel == FUNDING_MODEL_SELF

        // Provisional (before the rate arrives).
        binding.recipientText.text = "Sending …"
        binding.claimInfoText.text = "$recipient has until … to claim."
        binding.amountFiatText.text = "…"
        binding.amountBchText.text = String.format(Locale.US, "%.8f BCH locked in the smart contract", amountBch)
        binding.sellerFeeText.text = if (selfFunded) "—" else "…"
        binding.networkFeeText.text = if (selfFunded) "…" else "0"

        lifecycleScope.launch {
            val rate = com.asgaya.husk.FiatRateSource.eurPer(mainCurrency)  // main units per EUR
            eurPerMain = rate
            // Network fee (250 sats) in the selected currency needs the BCH price (self-funded only).
            val eurPerBch =
                if (selfFunded) runCatching { com.asgaya.husk.OraclePriceSource.eurPerBch() }.getOrNull() else null
            withContext(Dispatchers.Main) {
                // The datum = the sender's committed payment (shown everywhere; `ui-rethink/40`).
                val datumCurrency = payCurrency.ifBlank { mainCurrency }
                val derived = amountFiat * (rate ?: 1.0)                // rate-derived (double-check only)
                val amountMain = if (payAmount > 0.0) payAmount else derived
                val bufferMain = amountMain * (7.0 / 93.0)  // 7% buffer over the face -> collateral
                val totalMain = amountMain + bufferMain
                val collateralBch = amountBch * (100.0 / 93.0)
                val amountStr = "$datumCurrency %.2f".format(amountMain)
                binding.recipientText.text = android.text.SpannableStringBuilder("Sending ").apply {
                    val s = length; append(amountStr)
                    setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD), s, length,
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                val claimBy = java.text.SimpleDateFormat("MMM d, HH:mm", java.util.Locale.getDefault())
                    .format(java.util.Date((System.currentTimeMillis() / 1000 + 8 * 3600) * 1000))
                binding.claimInfoText.text = android.text.SpannableStringBuilder(recipient).apply {
                    setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, length,
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    // Same size as the amount (`Suso 2026-10-09`).
                    setSpan(android.text.style.AbsoluteSizeSpan(18, true), 0, length,
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    append(" has until $claimBy to claim.")
                }
                binding.amountBchText.text =
                    String.format(Locale.US, "%.8f BCH locked in the smart contract", collateralBch)
                binding.amountFiatText.text =
                    "%.2f + %.2f buffer = %.2f $datumCurrency".format(amountMain, bufferMain, totalMain)
                // Fees (selected currency): seller fee = amount × fee%; network fee only when self-funded.
                binding.sellerFeeText.text =
                    if (selfFunded) "—" else "$datumCurrency %.2f".format(amountMain * (funderFee ?: 0.0))
                binding.networkFeeText.text = if (selfFunded) {
                    "$datumCurrency %.4f".format((250.0 / 1e8) * (eurPerBch ?: 0.0) * (rate ?: 1.0))
                } else "0"
                // Double-check: the datum must be within ±0.02 of the rate-derived (rounding only).
                if (payAmount > 0.0 && kotlin.math.abs(payAmount - derived) > 0.02) {
                    Log.w(TAG, "⚠️ payment datum $payAmount ≠ rate-derived $derived (> ±0.02)")
                }
            }
        }
    }

    /**
     * Format covenant parameters as shareable message
     * Recipient will receive this via Telegram and claim the covenant
     */
    private fun formatCovenantMessage(remittance: Remittance): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val expiryDate = Date(remittance.expiryOracleTime!! * 1000)

        val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
        format.currency = Currency.getInstance(remittance.paymentCurrency)
        val amountFormatted = format.format(remittance.amountPaymentCurrency)

        return """
🎁 You received $amountFormatted via Asgaya BCH!

Covenant ready to claim - open Asgaya app to receive your payment.

[COVENANT_V25]
covenantAddress=${remittance.covenantAddress}
senderPubkey=${remittance.senderPubkey}
recipientPubkey=${remittance.recipientPubkey}
funderPubkey=${remittance.funderPubkey}
oraclePubkey=${remittance.oraclePubkey}
eurCents=${remittance.eurCents}
expiryOracleTime=${remittance.expiryOracleTime}
initialBchPriceInCents=${remittance.initialBchPriceInCents}
minPricePercent=${remittance.minPricePercent}
fundingTxid=${remittance.txid}
[/COVENANT_V25]

⏰ Expires: ${dateFormat.format(expiryDate)} UTC
💰 You will receive $amountFormatted worth of BCH at claim time

Open Asgaya app to claim!
        """.trimIndent()
    }

    /**
     * Copy covenant details to clipboard for sharing via Telegram
     */
    private fun copyCovenantToClipboard(remittance: Remittance) {
        val message = formatCovenantMessage(remittance)
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Asgaya Covenant", message)
        clipboard.setPrimaryClip(clip)
        Log.d(TAG, "📋 Covenant details copied to clipboard")
    }

    /**
     * Fetch oracle pubkey from Pi-chan oracle API
     * Replaces hardcoded oracle pubkey with dynamic fetch
     */
    /**
     * Send transaction (simplified with ViewModel)
     *
     * All the complex logic moved to SendViewModel. This just validates input
     * and triggers the ViewModel action. The ViewModel handles:
     * - Fetching oracle key
     * - Creating covenant
     * - Broadcasting transaction
     * - Saving to database
     * - Background confirmation monitoring
     *
     * State updates trigger UI changes via observeTransactionState().
     */
    private fun sendTransaction() {
        // Disable button to prevent double-tap
        binding.confirmSendButton.isEnabled = false
        binding.confirmSendButton.text = "Sending..."

        lifecycleScope.launch {
            // Validate wallet
            val activeWallet = walletManager.getActiveWallet()
            if (activeWallet == null) {
                Toast.makeText(this@ReviewSendActivity, "❌ No active wallet", Toast.LENGTH_LONG).show()
                binding.confirmSendButton.isEnabled = true
                binding.confirmSendButton.text = "Confirm & Send"
                return@launch
            }

            // Validate recipient pubkey
            if (recipientPubkey == null) {
                Toast.makeText(
                    this@ReviewSendActivity,
                    "❌ Recipient public key required for guaranteed value transfers",
                    Toast.LENGTH_LONG
                ).show()
                binding.confirmSendButton.isEnabled = true
                binding.confirmSendButton.text = "Confirm & Send"
                return@launch
            }

            // Get sender credentials
            val senderWif = walletManager.getWIF(activeWallet)
            val senderPubkey = activeWallet.publicKey
            val senderAddress = activeWallet.address

            // Calculate EUR cents
            val eurCents = (amountFiat * 100).toInt()

            // SELLER AUTO-FUNDING: Check if seller-funded or self-funded
            val effectiveFunderPubkey = funderPubkey  // Local val for smart cast
            if (fundingModel == "funder_funded" && effectiveFunderPubkey != null) {
                // Generate cash account for Bizum matching (e.g., "Elena#142")
                // The reference is the recipient's CashAccount (`Name#NNN`) — or the destination
                // address if we don't have one. It is human-readable + the Bizum concept; the
                // covenant ADDRESS is what identifies a covenant (Suso, 2026-09-27).
                val cashAccount = recipientCashAccount?.takeIf { it.isNotBlank() } ?: recipientAddress

                Log.d(TAG, "Creating UNFUNDED covenant (seller auto-funding)")
                Log.d(TAG, "Cash Account: $cashAccount")
                Log.d(TAG, "Seller pubkey: ${effectiveFunderPubkey.take(20)}...")

                // Create covenant ONLY (seller will fund it)
                sendViewModel.createCovenantOnly(
                    senderPubkey = senderPubkey,
                    recipientPubkey = recipientPubkey!!,
                    recipientAddress = recipientAddress,
                    recipientName = recipientName,
                    eurCents = eurCents,
                    amountBch = amountBch,
                    amountFiat = amountFiat,
                    currency = currency,
                    amountRecipient = amountRecipient,
                    recipientCurrency = recipientCurrency,
                    funderPubkey = effectiveFunderPubkey,
                    funderNostrPubkey = funderNpub,
                    adId = funderAdId,
                    feePercent = funderFee,
                    adMinPricePercent = funderMinPricePercent ?: 93,
                    cashAccount = cashAccount,
                    payAmount = payAmount,
                    payCurrency = payCurrency
                )
            } else {
                // Self-funded covenant (sender funds immediately)
                Log.d(TAG, "Creating SELF-FUNDED covenant")

                sendViewModel.createAndFundCovenant(
                    senderWif = senderWif,
                    senderPubkey = senderPubkey,
                    senderAddress = senderAddress,
                    recipientPubkey = recipientPubkey!!,
                    recipientAddress = recipientAddress,
                    recipientName = recipientName,
                    eurCents = eurCents,
                    amountBch = amountBch,
                    amountFiat = amountFiat,
                    currency = currency,
                    amountRecipient = amountRecipient,
                    recipientCurrency = recipientCurrency,
                    fundingModel = fundingModel,
                    funderPubkey = funderPubkey,
                    cashAccount = recipientCashAccount  // reference: recipient's CashAccount (`ui-rethink/34`)
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Note: Don't disconnect NostrClient here - it's application-scoped (singleton)
        // and should survive Activity lifecycle. Disconnecting would:
        // 1. Break the offline queue (queued messages lost)
        // 2. Require reconnection on next Activity (slow)
        // 3. Not needed for Task 6 (seller needs long-lived subscription)
    }

    /**
     * Send [FUND_COVENANT] message to seller via Nostr (NIP-17 encrypted)
     *
     * Task 5: Sender Integration
     * - Wraps fundCovenantMessage with NIP-17 three-layer encryption
     * - Publishes kind-1059 gift-wrap to Nostr relays
     * - Keeps clipboard copy as fallback
     *
     * @param fundCovenantMessage The [FUND_COVENANT] block text to send
     */
    /**
     * Self-funded only: the covenant is funded immediately, so send the recipient the
     * `[COVENANT_V25]` **offer** right here. (The funder-funded path sends it from
     * `NotificationListener.monitorAwaitingFunding`.) Best-effort: a failure is logged.
     * (`ui-rethink/35` + `37`.)
     */
    private fun sendOfferToRecipient(remittance: com.asgaya.husk.remittance.Remittance) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val recipientBchPubkey = remittance.recipientPubkey
                if (recipientBchPubkey.isNullOrBlank()) return@launch
                val recipientNostrPubkey = database.contactDao()
                    .getContactByPublicKey(recipientBchPubkey)?.nostrPubkey
                if (recipientNostrPubkey.isNullOrBlank()) {
                    Log.w(TAG, "🎁 No Nostr pubkey for recipient ${recipientBchPubkey.take(20)}… — cannot send offer")
                    return@launch
                }
                val activeWallet = walletManager.getActiveWallet() ?: return@launch
                val myKeys = nostrKeyManager.ensureNostrKeys(activeWallet)
                val giftWrap = NIP17GiftWrap.wrapDM(
                    content = com.asgaya.husk.remittance.CovenantOffer.message(remittance, com.asgaya.husk.IdentityPrefs.load(this@ReviewSendActivity).cashAccount),
                    senderPrivKeyHex = myKeys.privateKeyHex,
                    senderPubKeyHex = myKeys.publicKeyHex,
                    recipientPubKeyHex = recipientNostrPubkey
                )
                val sent = nostrClient.publishEvent(giftWrap)
                Log.i(TAG, "🎁 [COVENANT_V25] offer sent to recipient via $sent relays")
            } catch (e: Exception) {
                Log.e(TAG, "🎁 Failed to send offer to recipient", e)
            }
        }
    }

    private fun sendViaNostr(fundCovenantMessage: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Sending via Nostr: ${fundCovenantMessage.length} bytes")

                // Get sender's Nostr keys (generates if missing)
                val activeWallet = walletManager.getActiveWallet()
                if (activeWallet == null) {
                    Log.e(TAG, "No active wallet found for Nostr send")
                    return@launch
                }

                val myKeys = nostrKeyManager.ensureNostrKeys(activeWallet)

                // Resolve the funder's Nostr pubkey from the contact list (production shape).
                // The funder is identified by the covenant's funderPubkey (BCH).
                val funderNostrPubkey = funderPubkey?.let { bch ->
                    database.contactDao().getContactByPublicKey(bch)?.nostrPubkey
                }
                if (funderNostrPubkey.isNullOrBlank()) {
                    Log.w(TAG, "No Nostr pubkey for funder ${funderPubkey?.take(20)}... — clipboard only")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ReviewSendActivity,
                            "⚠️ No Nostr key for funder — copied to clipboard",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }

                // NIP-17 three-layer wrap: kind-14 → kind-13 → kind-1059
                val giftWrap = NIP17GiftWrap.wrapDM(
                    content = fundCovenantMessage,
                    senderPrivKeyHex = myKeys.privateKeyHex,
                    senderPubKeyHex = myKeys.publicKeyHex,
                    recipientPubKeyHex = funderNostrPubkey
                )

                // Publish to Nostr relays
                val sentCount = nostrClient.publishEvent(giftWrap)

                withContext(Dispatchers.Main) {
                    if (sentCount > 0) {
                        Log.i(TAG, "✅ Sent to $sentCount Nostr relays (event: ${giftWrap.getString("id")})")
                        Toast.makeText(
                            this@ReviewSendActivity,
                            "✅ Sent to seller via Nostr ($sentCount relays)",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.w(TAG, "⚠️ No relays connected, message queued")
                        Toast.makeText(
                            this@ReviewSendActivity,
                            "⚠️ Message queued (relays connecting...)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send via Nostr", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ReviewSendActivity,
                        "⚠️ Nostr send failed (clipboard has fallback)",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
