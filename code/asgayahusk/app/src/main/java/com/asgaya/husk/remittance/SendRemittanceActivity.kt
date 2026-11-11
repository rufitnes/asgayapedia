package com.asgaya.husk.remittance

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.asgaya.husk.AppDatabase
import com.asgaya.husk.CovenantWebView
import com.asgaya.husk.ElectrumClient
import com.asgaya.husk.R
import com.asgaya.husk.AdvancedMode
import com.asgaya.husk.OraclePriceSource
import com.asgaya.husk.FiatRateSource
import com.asgaya.husk.SupportedFiats
import com.asgaya.husk.covenant.CovenantConstants
import com.asgaya.husk.covenant.CovenantRecovery
import com.asgaya.husk.databinding.ActivitySendRemittanceBinding
import com.asgaya.husk.wallet.WalletManager
import com.asgaya.husk.nostr.NostrKeyManager
import com.asgaya.husk.nostr.NostrClient
import com.asgaya.husk.nostr.NIP17GiftWrap
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Send Remittance Screen
 *
 * User enters:
 * - Recipient (Cash Account or BCH address)
 * - Amount (in payment currency or recipient currency)
 * - Chooses funding model: self-funded (funder = sender) OR
 *   bulletin-board funded (funder = a BCH seller; bulletin board not yet implemented,
 *   but the decision point lives here so the plumbing is ready).
 */
class SendRemittanceActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SendRemittance"

        /** Funding model: sender funds their own covenant. */
        const val FUNDING_MODEL_SELF = "self_funded"

        /** Funding model: a BCH seller (funder) funds the covenant. */
        const val FUNDING_MODEL_FUNDER = "funder_funded"

        // "Send again" prefill (`ui-rethink/34`) — repeat a remittance with a fresh quote.
        const val EXTRA_PREFILL = "prefill"
        const val EXTRA_RECIPIENT = "prefill_recipient"
        const val EXTRA_RECIPIENT_NAME = "prefill_recipient_name"
        const val EXTRA_RECIPIENT_PUBKEY = "prefill_recipient_pubkey"
        const val EXTRA_AMOUNT_FIAT = "prefill_amount_fiat"
        const val EXTRA_CURRENCY = "prefill_currency"
        const val EXTRA_RECIPIENT_CURRENCY = "prefill_recipient_currency"
        const val EXTRA_FUNDING_MODEL = "prefill_funding_model"
        const val EXTRA_FUNDER_PUBKEY = "prefill_funder_pubkey"
        const val EXTRA_RECIPIENT_CASH_ACCOUNT = "prefill_recipient_cash_account"
        const val RECENT_CURRENCIES_KEY = "recent_currencies"
    }

    private lateinit var binding: ActivitySendRemittanceBinding
    private lateinit var database: AppDatabase
    private lateinit var walletManager: WalletManager
    private var nostrKeyManager: NostrKeyManager? = null
    private lateinit var covenantWebView: CovenantWebView

    private var paymentCurrency = "EUR"
    private var recipientCurrency = "VES"
    private var currentBalanceBch = 0.0
    private var amountPaymentCurrency = 0.0
    private var amountRecipientCurrency = 0.0
    private var isUpdating = false  // Flag to prevent infinite loop

    // Selected contact details
    private var selectedContactPubkey: String? = null
    private var selectedContactName: String? = null
    private var selectedContactCashAccount: String? = null

    /** `Find sellers` → the Market tab (Sell side); the chosen seller comes back here (`ui-rethink/41`). */
    private val pickSellerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult
        val data = result.data
        val id = data?.getStringExtra(com.asgaya.husk.trade.MarketActivity.EXTRA_PICKED_ID)
        val npub = data?.getStringExtra(com.asgaya.husk.trade.MarketActivity.EXTRA_PICKED_NPUB)
        if (!id.isNullOrBlank() || !npub.isNullOrBlank()) usePickedSeller(id, npub)
    }

    /**
     * Resolve the ad the sender picked in Market, then continue the funding flow. Resolve by the ad
     * **id** first — one wallet can publish several ads that **share the npub**, so the npub alone is
     * ambiguous (it would pick the wrong ad's fee).
     */
    private fun usePickedSeller(id: String?, npub: String?) {
        lifecycleScope.launch {
            val listing = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                runCatching {
                    val all = com.asgaya.husk.data.NostrBoardSource(database.boardEventDao()).listings()
                    all.firstOrNull { it.id == id }
                        ?: all.firstOrNull { !npub.isNullOrBlank() && it.nostrPubkey == npub }
                }.getOrNull()
            }
            if (listing == null) {
                Toast.makeText(this@SendRemittanceActivity, "Could not load that seller", Toast.LENGTH_LONG).show()
                return@launch
            }
            requestPaymentInstructions(listing)
        }
    }

    // Selected funding model (decision point for self-funded vs bulletin-board funded)
    private var selectedFundingModel = FUNDING_MODEL_SELF

    // Selected funder (bulletin board seller) — set when bulletin board is implemented.
    // For now always null → self-funded path used.
    private var selectedFunderPubkey: String? = null
    private var selectedFunderNpub: String? = null
    private var selectedFunderAdId: String? = null
    private var selectedFunderFee: Double? = null
    private var selectedFunderMinPricePercent: Int? = null

    // BCH/EUR price comes ONLY from the Asgaya oracle — a hardcoded price is a no even as a
    // fallback (`ui-rethink/16` P0-2). null → unavailable: callers block/warn, never guess.
    @Volatile private var bchToEur: Double? = null
    // EUR→recipient FX for the DISPLAY leg (`supported-fiats/01`, Phase-0 VES slice): async from
    // FiatRateSource. null → the local amount isn't shown — never a hardcoded fallback
    // (`ui-rethink/16` P0-2). The covenant's settled face stays EUR.
    @Volatile private var eurPerRecipient: Double? = null
    // Payment-currency units per 1 EUR for the "You send" currency (the Settings *main* currency).
    // 1.0 for EUR (the default); fetched from FiatRateSource otherwise. The canonical logic unit is
    // always EUR (`supported-fiats/01 §5`) — this only feeds the UI's display arithmetic.
    @Volatile private var eurPerPayment: Double? = 1.0

    // Contact selection launcher
    private val selectContactLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedAddress = result.data?.getStringExtra("address")
            val selectedLabel = result.data?.getStringExtra("name")
            val selectedPubkey = result.data?.getStringExtra("publicKey")
            val selectedCashAccount = result.data?.getStringExtra("cashAccount")

            if (selectedAddress != null) {
                binding.recipientInput.setText(selectedAddress)
                selectedContactPubkey = selectedPubkey
                selectedContactName = selectedLabel
                selectedContactCashAccount = selectedCashAccount
                Log.d(TAG, "Selected contact: $selectedLabel ($selectedAddress)")
                Log.d(TAG, "  Pubkey: $selectedPubkey")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendRemittanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        walletManager = WalletManager.getInstance(this)
        covenantWebView = CovenantWebView.getInstance(this)

        loadSettings()
        readPrefillExtras()
        setupUI()
        queryBalance()
        loadOracleRate()
        loadFiatRate()
        loadPaymentRate()
        applyPrefillFields()
    }

    // ---- "Send again" prefill (`ui-rethink/34`) ----
    private var isPrefill = false
    private var prefillRecipient: String? = null
    private var prefillName: String? = null
    private var prefillPubkey: String? = null
    private var prefillAmount: Double = 0.0
    private var prefillFundingModel: String? = null
    private var prefillFunderPubkey: String? = null
    private var prefillCashAccount: String? = null

    private fun readPrefillExtras() {
        if (!intent.getBooleanExtra(EXTRA_PREFILL, false)) return
        isPrefill = true
        prefillRecipient = intent.getStringExtra(EXTRA_RECIPIENT)
        prefillName = intent.getStringExtra(EXTRA_RECIPIENT_NAME)
        prefillPubkey = intent.getStringExtra(EXTRA_RECIPIENT_PUBKEY)
        prefillAmount = intent.getDoubleExtra(EXTRA_AMOUNT_FIAT, 0.0)
        prefillFundingModel = intent.getStringExtra(EXTRA_FUNDING_MODEL)
        prefillFunderPubkey = intent.getStringExtra(EXTRA_FUNDER_PUBKEY)
        prefillCashAccount = intent.getStringExtra(EXTRA_RECIPIENT_CASH_ACCOUNT)
        // Override the RECIPIENT (display) currency BEFORE setupUI so the spinner selects it.
        // (The PAYMENT currency is always EUR — `supported-fiats/01 §5` — so EXTRA_CURRENCY is ignored.)
        intent.getStringExtra(EXTRA_RECIPIENT_CURRENCY)?.let { recipientCurrency = it }
    }

    private fun applyPrefillFields() {
        if (!isPrefill) return
        prefillRecipient?.let { binding.recipientInput.setText(it) }
        selectedContactName = prefillName
        selectedContactPubkey = prefillPubkey
        prefillFundingModel?.let { selectedFundingModel = it }
        selectedFunderPubkey = prefillFunderPubkey
        // Preserve the reference (recipient's CashAccount) so the recreated covenant carries it (`ui-rethink/34`).
        selectedContactCashAccount = prefillCashAccount
        if (prefillAmount > 0.0) {
            // Triggers the amount watcher → recompute + funding options (fresh oracle rate).
            binding.amountPaymentInput.setText(String.format(Locale.US, "%.2f", prefillAmount))
        }
    }

    /**
     * Fetch the BCH/EUR price from the Asgaya oracle. Never falls back to a constant: if the
     * oracle is unavailable [bchToEur] stays null and the UI blocks/warns (`ui-rethink/16` P0-2).
     */
    private fun loadOracleRate() {
        lifecycleScope.launch {
            bchToEur = OraclePriceSource.eurPerBch()
            if (bchToEur == null) {
                Toast.makeText(
                    this@SendRemittanceActivity,
                    "⚠️ BCH price unavailable — check your connection",
                    Toast.LENGTH_LONG
                ).show()
            }
            updateFundingOptions()
        }
    }

    /**
     * Resolve the EUR→recipient **display** rate (`supported-fiats/01`, Phase-0 VES slice: DolarAPI
     * `euros/paralelo` for VES, the fawazahmed0 reference for the rest). `null` → the local amount
     * stays blank; the covenant face is EUR regardless — never a hardcoded fallback.
     */
    private fun loadFiatRate() {
        lifecycleScope.launch {
            eurPerRecipient = FiatRateSource.eurPer(recipientCurrency)
            if (eurPerRecipient == null && recipientCurrency != "EUR") {
                Log.w(TAG, "⚠️ No display rate for $recipientCurrency — local amount left blank")
            }
            updateRecipientAmount()
            updatePaymentAmount()
        }
    }

    /** Payment-currency units per 1 EUR for the "You send" currency (1.0 for EUR). */
    private fun loadPaymentRate() {
        if (paymentCurrency == "EUR") {
            eurPerPayment = 1.0
            return
        }
        lifecycleScope.launch {
            eurPerPayment = FiatRateSource.eurPer(paymentCurrency)
            updateRecipientAmount()
            updatePaymentAmount()
        }
    }

    /**
     * Canonical amount in EUR — the unit the covenant logic uses (`supported-fiats/01 §5`). The
     * "You send" field is in [paymentCurrency]; convert it. Returns 0.0 if a needed rate is missing.
     */
    private fun amountEur(): Double {
        if (paymentCurrency == "EUR") return amountPaymentCurrency
        val r = eurPerPayment
        return if (r != null && r > 0) amountPaymentCurrency / r else 0.0
    }

    /** Convert a EUR amount to the "You send" (main) currency for display (1.0 = EUR). */
    private fun toMain(eur: Double): Double =
        if (paymentCurrency == "EUR") eur else (eurPerPayment?.takeIf { it > 0 }?.let { eur * it } ?: eur)

    private fun loadSettings() {
        val prefs = getSharedPreferences(RemittanceActivity.PREFS_NAME, Context.MODE_PRIVATE)
        // "You send" currency = the Settings *main* currency (`fiat_currency`). It lives in the
        // shared BizumParserPrefs file (MainActivity/SettingsActivity), NOT RemittancePrefs.
        val mainPrefs = getSharedPreferences("BizumParserPrefs", Context.MODE_PRIVATE)
        paymentCurrency = mainPrefs.getString("fiat_currency", "EUR") ?: "EUR"
        recipientCurrency = prefs.getString(RemittanceActivity.RECIPIENT_CURRENCY_KEY, "VES") ?: "VES"
    }

    private fun setupUI() {
        // Advanced OFF → hide the self-fund option (remittance-first; `ui-rethink/08`)
        binding.selfFundedCard.visibility =
            if (AdvancedMode.isEnabled(this)) android.view.View.VISIBLE else android.view.View.GONE

        // Back button
        binding.backButton.setOnClickListener {
            finish()
        }

        // Select from Contacts button
        binding.selectContactButton.setOnClickListener {
            val intent = Intent(this, com.asgaya.husk.ContactsActivity::class.java)
            intent.putExtra("SELECT_MODE", true)
            selectContactLauncher.launch(intent)
        }

        // Setup currency buttons ("You send" = main currency; "recipient gets" = picker)
        setupCurrencyButtons()

        // Amount input listeners
        binding.amountPaymentInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return  // Prevent infinite loop
                val amount = s?.toString()?.toDoubleOrNull() ?: 0.0
                if (amount > 0) {
                    amountPaymentCurrency = amount
                    updateRecipientAmount()
                    updateFundingOptions()
                }
            }
        })

        binding.amountRecipientInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return  // Prevent infinite loop
                val amount = s?.toString()?.toDoubleOrNull() ?: 0.0
                if (amount > 0) {
                    amountRecipientCurrency = amount
                    updatePaymentAmount()
                    updateFundingOptions()
                }
            }
        })

        // Review & Send button - handles both self-funded and seller-funded
        binding.reviewSendButton.setOnClickListener {
            val rate = bchToEur
            if (rate == null || rate <= 0.0) {
                Toast.makeText(this, "⚠️ BCH price unavailable — check your connection", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val amountBch = amountEur() / rate

            // Validate amount
            if (amountBch == 0.0) {
                Toast.makeText(this, "⚠️ Enter amount first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val recipient = binding.recipientInput.text.toString()
            if (recipient.isEmpty()) {
                Toast.makeText(this, "⚠️ Enter recipient address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate recipient has pubkey (required for covenants)
            if (selectedContactPubkey == null) {
                Toast.makeText(
                    this,
                    "❌ Recipient public key required for guaranteed value transfers",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // Check balance ONLY for self-funded (seller-funded doesn't need sender balance)
            if (selectedFundingModel != FUNDING_MODEL_FUNDER && currentBalanceBch < amountBch) {
                Toast.makeText(this, "⚠️ Insufficient balance - select a seller or fund your wallet", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Decision point: If no seller selected, default to self-funded
            if (selectedFundingModel != FUNDING_MODEL_FUNDER) {
                selectedFundingModel = FUNDING_MODEL_SELF
                selectedFunderPubkey = null
            }

            // Navigate to Review & Send screen
            val intent = Intent(this, ReviewSendActivity::class.java).apply {
                putExtra(ReviewSendActivity.EXTRA_RECIPIENT, recipient)
                putExtra(ReviewSendActivity.EXTRA_RECIPIENT_NAME, selectedContactName)
            putExtra(ReviewSendActivity.EXTRA_RECIPIENT_CASH_ACCOUNT, selectedContactCashAccount)
                putExtra(ReviewSendActivity.EXTRA_RECIPIENT_PUBKEY, selectedContactPubkey)
                putExtra(ReviewSendActivity.EXTRA_AMOUNT_BCH, amountBch)
                // The covenant's settled face is EUR (`supported-fiats/01 §5`): pass the canonical
                // EUR amount, whatever currency the "You send" field displays.
                putExtra(ReviewSendActivity.EXTRA_AMOUNT_FIAT, amountEur())
                putExtra(ReviewSendActivity.EXTRA_CURRENCY, "EUR")
                putExtra(ReviewSendActivity.EXTRA_PAY_AMOUNT, amountPaymentCurrency)
                putExtra(ReviewSendActivity.EXTRA_PAY_CURRENCY, paymentCurrency)
                putExtra(ReviewSendActivity.EXTRA_AMOUNT_RECIPIENT, amountRecipientCurrency)
                putExtra(ReviewSendActivity.EXTRA_RECIPIENT_CURRENCY, recipientCurrency)
                putExtra(ReviewSendActivity.EXTRA_FUNDING_MODEL, selectedFundingModel)
            putExtra(ReviewSendActivity.EXTRA_FUNDER_PUBKEY, selectedFunderPubkey)
            putExtra(ReviewSendActivity.EXTRA_FUNDER_NPUB, selectedFunderNpub)
            putExtra(ReviewSendActivity.EXTRA_FUNDER_AD_ID, selectedFunderAdId)
            putExtra(ReviewSendActivity.EXTRA_FUNDER_FEE, selectedFunderFee ?: -1.0)
            putExtra(ReviewSendActivity.EXTRA_FUNDER_MIN_PRICE_PERCENT, selectedFunderMinPricePercent ?: -1)
            }
            guardActiveCovenant(recipient) { startActivity(intent) }
        }

        // Find sellers → the Market tab (Sell side), scoped to the currency I pay in (`ui-rethink/41`)
        binding.findSellersButton.setOnClickListener {
            if (amountPaymentCurrency > 0) {
                pickSellerLauncher.launch(
                    Intent(this, com.asgaya.husk.trade.MarketActivity::class.java)
                        .putExtra(com.asgaya.husk.trade.MarketActivity.EXTRA_PICK_SELLER, true)
                        .putExtra(com.asgaya.husk.trade.MarketActivity.EXTRA_PRESELECT_ASSET, paymentCurrency)
                )
            } else {
                Toast.makeText(this, "⚠️ Enter amount first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupCurrencyButtons() {
        // "You send" currency = the Settings *main* currency (read in loadSettings). Shown as a
        // button; tapping opens Settings to change it. The covenant logic stays EUR — the UI only
        // does the conversion arithmetic (`supported-fiats/01 §5`).
        updatePaymentCurrencyButton()
        binding.paymentCurrencyButton.setOnClickListener {
            startActivity(Intent(this, com.asgaya.husk.SettingsActivity::class.java))
        }

        // Recipient (display) currency — a searchable picker (`supported-fiats/01`). PROVISIONAL
        // currencies use the fawazahmed0 reference and are marked "· indicative" (step 4).
        updateCurrencyButton()
        binding.recipientCurrencyButton.setOnClickListener { showCurrencyPicker() }
    }

    private fun updatePaymentCurrencyButton() {
        binding.paymentCurrencyButton.text = "$paymentCurrency ▾"
    }

    private fun updateCurrencyButton() {
        binding.recipientCurrencyButton.text = "$recipientCurrency ▾"
        // Step 4 (`supported-fiats/01 §5`): a PROVISIONAL currency shows an inline caveat.
        binding.currencyWarning.visibility =
            if (SupportedFiats.isProvisional(recipientCurrency)) android.view.View.VISIBLE
            else android.view.View.GONE
    }

    private fun setRecipientCurrency(code: String) {
        recipientCurrency = code
        saveRecentCurrency(code)
        // Persist the last used recipient currency (so the screen reopens on it).
        getSharedPreferences(RemittanceActivity.PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(RemittanceActivity.RECIPIENT_CURRENCY_KEY, code).apply()
        updateCurrencyButton()
        loadFiatRate()
        updateRecipientAmount()
    }

    private fun recentCurrencies(): List<String> =
        (getSharedPreferences(RemittanceActivity.PREFS_NAME, Context.MODE_PRIVATE)
            .getString(RECENT_CURRENCIES_KEY, "") ?: "").split(",").filter { it.isNotBlank() }

    private fun saveRecentCurrency(code: String) {
        val recents = (listOf(code) + recentCurrencies().filter { it != code }).take(5)
        getSharedPreferences(RemittanceActivity.PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(RECENT_CURRENCIES_KEY, recents.joinToString(",")).apply()
    }

    /** Searchable currency picker: recents first, then the registry, filtered by code/name. */
    private fun showCurrencyPicker() {
        val pad = (16 * resources.displayMetrics.density).toInt()
        val root = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(pad, pad / 2, pad, 0)
        }
        val search = android.widget.EditText(this).apply {
            hint = "Search (code or name)"
            inputType = android.text.InputType.TYPE_CLASS_TEXT
        }
        val list = android.widget.ListView(this)
        root.addView(search); root.addView(list)
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Recipient currency")
            .setView(root)
            .setNegativeButton("Cancel", null)
            .create()

        fun refresh(query: String) {
            val q = query.trim().lowercase()
            val codes = if (q.isEmpty()) {
                val recents = recentCurrencies()
                recents + SupportedFiats.codes.filter { it !in recents }
            } else {
                SupportedFiats.ALL
                    .filter { it.code.lowercase().contains(q) || it.name.lowercase().contains(q) }
                    .map { it.code }
            }
            list.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, codes.map { SupportedFiats.label(it) })
            list.setOnItemClickListener { _, _, position, _ ->
                setRecipientCurrency(codes[position]); dialog.dismiss()
            }
        }
        search.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) { refresh(s?.toString() ?: "") }
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        })
        refresh("")
        dialog.show()
    }

    private fun updateRecipientAmount() {
        if (amountPaymentCurrency <= 0) return
        // EUR (canonical) → recipient local (display). No rate → leave blank; never a fallback.
        val eur = amountEur()
        if (eur <= 0.0) return
        val rate = eurPerRecipient ?: return
        if (rate <= 0.0) return
        amountRecipientCurrency = eur * rate
        isUpdating = true
        binding.amountRecipientInput.setText(String.format(Locale.US, "%.0f", amountRecipientCurrency))
        isUpdating = false
    }

    private fun updatePaymentAmount() {
        if (amountRecipientCurrency <= 0) return
        val rate = eurPerRecipient ?: return
        if (rate <= 0.0) return
        // recipient local → EUR (canonical) → the "You send" currency (main). No rate → leave blank.
        val eur = amountRecipientCurrency / rate
        val payment = if (paymentCurrency == "EUR") eur
            else (eurPerPayment?.takeIf { it > 0 }?.let { eur * it } ?: return)
        amountPaymentCurrency = payment
        isUpdating = true
        binding.amountPaymentInput.setText(String.format(Locale.US, "%.2f", amountPaymentCurrency))
        isUpdating = false
    }

    private fun updateFundingOptions() {
        val rate = bchToEur
        if (rate == null || rate <= 0.0) {
            // No sanctioned price → block, never a constant (`ui-rethink/16` P0-2).
            binding.selfFundedBalance.text = "Price unavailable"
            binding.selfFundedCard.alpha = 0.5f
            binding.selfFundedCard.isEnabled = false
            binding.selfFundedStatus.text = "⚠️ Waiting for the oracle price"
            binding.selfFundedStatus.setTextColor(getColor(R.color.orange))
            binding.buyBchAmount.text = ""
            binding.buyBchCost.text = "Price unavailable"
            return
        }
        val amountBch = amountEur() / rate

        // Self-funded option
        val balanceFiat = currentBalanceBch * rate
        binding.selfFundedBalance.text = String.format(Locale.US, "Your balance: %.8f BCH (%s %.2f)", currentBalanceBch, paymentCurrency, toMain(balanceFiat))

        if (currentBalanceBch >= amountBch) {
            binding.selfFundedCard.alpha = 1.0f
            binding.selfFundedCard.isEnabled = true
            binding.selfFundedStatus.text = "✅ Sufficient balance"
            binding.selfFundedStatus.setTextColor(getColor(R.color.green))
        } else {
            binding.selfFundedCard.alpha = 0.5f
            binding.selfFundedCard.isEnabled = false
            binding.selfFundedStatus.text = "⚠️ Insufficient balance"
            binding.selfFundedStatus.setTextColor(getColor(R.color.orange))
        }

        // Buy BCH option
        val estimatedFee = amountPaymentCurrency * 0.005  // 0.5% fee estimate
        val totalCost = amountPaymentCurrency + estimatedFee
        binding.buyBchAmount.text = String.format(Locale.US, "Buy %.8f BCH", amountBch)
        binding.buyBchCost.text = String.format(Locale.US, "Cost: ~%s %.2f (%s %.2f + %s %.2f fee)", paymentCurrency, totalCost, paymentCurrency, amountPaymentCurrency, paymentCurrency, estimatedFee)
    }

    private fun queryBalance() {
        lifecycleScope.launch {
            try {
                val activeWallet = walletManager.getActiveWallet()
                if (activeWallet != null) {
                    // Query actual balance from Electrum
                    val electrumClient = ElectrumClient()
                    currentBalanceBch = electrumClient.getBalance(activeWallet.address)
                    updateFundingOptions()
                    Log.d(TAG, "✅ Balance loaded: $currentBalanceBch BCH for ${activeWallet.address}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to query balance", e)
                Toast.makeText(this@SendRemittanceActivity, "⚠️ Failed to query balance: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Create covenant using WebView + CashScript SDK
     *
     * Called when user selects "Self-funded" option.
     * Creates covenant address that can be verified with scripts before funding.
     *
     * See: asgaya-docs/docs/implementation/error-prevention.md
     */
    private suspend fun createCovenant(
        recipientPubkey: String,
        eurAmount: Double
    ): String {
        val activeWallet = walletManager.getActiveWallet()
            ?: throw Exception("No active wallet")

        val senderPubkey = activeWallet.publicKey

        // Oracle quote: pubkey + price + timestamp (no hardcoded price, no device clock)
        val oracle = CovenantRecovery.fetchOraclePrice()

        // Covenant parameters
        val eurCents = (eurAmount * 100).toInt()
        val currentBchPriceInCents = oracle.price
        val expiryOracleTime = oracle.timestamp + (8 * 60 * 60)  // 8 hours
        val oraclePubkeyHex = oracle.pubkey

        Log.d(TAG, "Creating covenant:")
        Log.d(TAG, "  EUR: €$eurAmount ($eurCents cents)")
        Log.d(TAG, "  BCH price: €${oracle.price / 100.0}")
        Log.d(TAG, "  Expiry: $expiryOracleTime (8 hours)")

        // Create covenant via WebView
        val covenantAddress = covenantWebView.createCovenant(
            senderPubkey = senderPubkey,
            recipientPubkey = recipientPubkey,
            funderPubkey = senderPubkey,  // Self-funded
            oraclePubkey = oraclePubkeyHex,
            eurCents = eurCents,
            expiryOracleTime = expiryOracleTime,
            initialBchPriceInCents = currentBchPriceInCents,
            minPricePercent = 93  // 7% volatility buffer
        )

        return covenantAddress
    }

    /**
     * Show mock bulletin board with available sellers
     *
     * SELLER AUTO-FUNDING (Phase 0 Testing):
     * - Shows sellers from mock bulletin board (based on SELLER wallets)
     * - User selects a seller → sets selectedFunderPubkey
     * - Proceeds to ReviewSendActivity → calls createCovenantOnly()
     *
     * In production, this would query on-chain NFT listings (ASGAYA_SELLER_V1).
     * For Phase 0, we create mock listings from local SELLER wallets on Pixel 6a.
     */
    private fun showMockBulletinBoard() {
        lifecycleScope.launch {
            try {
                // Get all wallets (in production, this would be a blockchain query)
                val allWallets = AppDatabase.getDatabase(this@SendRemittanceActivity)
                    .walletDao()
                    .getAllWallets()

                // F3 (file 61): never offer the user their OWN ad as the counterparty. On the real
                // board your own ad is present like anyone else's, and picking it is self-dealing.
                val activeWallet = walletManager.getActiveWallet()
                val manager = nostrKeyManager ?: NostrKeyManager(this@SendRemittanceActivity).also { nostrKeyManager = it }
                val myNpub = activeWallet?.let { manager.ensureNostrKeys(it).publicKeyHex }
                val board = com.asgaya.husk.data.NostrBoardSource(
                    AppDatabase.getDatabase(this@SendRemittanceActivity).boardEventDao()
                ).listings()
                val myMethods = com.asgaya.husk.IdentityPrefs.load(this@SendRemittanceActivity).methods
                // Shared board filter (`bulletin-board/16`/`17`): asset (the currency I pay in) × method.
                val sellerListings = com.asgaya.husk.data.BoardFilter.matching(
                    board,
                    com.asgaya.husk.data.AdType.SELLER,
                    asset = paymentCurrency,
                    methods = myMethods,
                    myPublicKey = activeWallet?.publicKey,
                    myNostrPubkey = myNpub
                ).sortedBy { it.fee }  // cheapest fee first (`bulletin-board/16`/`17`)

                if (sellerListings.isEmpty()) {
                    runOnUiThread {
                        AlertDialog.Builder(this@SendRemittanceActivity)
                            .setTitle("⚠️ No sellers for $paymentCurrency")
                            .setMessage(
                                "No sellers for $paymentCurrency, select other currency.\n\n" +
                                    "There is no BCH seller accepting $paymentCurrency right now.\n\n" +
                                    "Your own ads are never shown here — you can't trade with yourself.\n\n" +
                                    "For Phase 0 testing:\n" +
                                    "1. Go to Pixel 6a (seller device)\n" +
                                    "2. Open Wallets tab\n" +
                                    "3. Create or switch a wallet to SELLER role\n" +
                                    "4. That wallet will appear as a seller listing here"
                            )
                            .setPositiveButton("OK", null)
                            .show()
                    }
                    return@launch
                }

                // The full board, not just one: list every matching seller, then show the
                // selected ad's payment instructions (built from the listing).
                runOnUiThread {
                    val labels = sellerListings.map {
                        val loc = it.location?.takeIf { l -> l.isNotBlank() }?.let { l -> " · 📍 $l" }.orEmpty()
                        "${it.displayName} · Sells ${it.assets.joinToString()} · ${it.paymentMethods.joinToString()} · Fee ${it.feePercent}$loc"
                    }.toTypedArray()
                    AlertDialog.Builder(this@SendRemittanceActivity)
                        .setTitle("💰 Sell BCH — pick a seller")
                        .setItems(labels) { _, which -> requestPaymentInstructions(sellerListings[which]) }
                        .setNegativeButton("Cancel", null)
                        .show()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to load bulletin board", e)
                runOnUiThread {
                    Toast.makeText(
                        this@SendRemittanceActivity,
                        "❌ Failed to load bulletin board: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    /**
     * Send a [PAY_INSTRUCTIONS_REQUEST] DM to the seller and show their reply (`ui-rethink/07`).
     * The exchange doubles as the liveness check; on timeout we fall back to the listing details.
     */
    private fun requestPaymentInstructions(seller: com.asgaya.husk.data.Listing) {
        val npub = seller.nostrPubkey
        if (npub.isNullOrBlank()) { showSellerInstructions(seller); return }

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@SendRemittanceActivity)
            val activeWallet = walletManager.getActiveWallet()
            if (activeWallet == null) { showSellerInstructions(seller); return@launch }
            val manager = nostrKeyManager ?: NostrKeyManager(this@SendRemittanceActivity).also { nostrKeyManager = it }
            val myKeys = manager.ensureNostrKeys(activeWallet)

            val req = com.asgaya.husk.data.PayInstructionsRequest(
                adId = seller.id,
                amount = "%.2f".format(amountPaymentCurrency),
                currency = paymentCurrency
            )

            try {
                val client = NostrClient.getInstance(myKeys.publicKeyHex, null)
                client.connect()
                val giftWrap = NIP17GiftWrap.wrapDM(req.toMessage(), myKeys.privateKeyHex, myKeys.publicKeyHex, npub)
                val sent = client.publishEvent(giftWrap)
                Log.i(TAG, "💳 [PAY_INSTRUCTIONS_REQUEST] → ${seller.displayName} via $sent relays (req=${req.requestId})")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send [PAY_INSTRUCTIONS_REQUEST]", e)
                showSellerInstructions(seller)
                return@launch
            }

            val progress = AlertDialog.Builder(this@SendRemittanceActivity)
                .setTitle("💳 Payment instructions")
                .setMessage("Contacting ${seller.displayName}…")
                .setCancelable(false)
                .create()
            progress.show()

            var handled = false
            val collector = lifecycleScope.launch {
                db.cashoutMessageDao().observePending(com.asgaya.husk.data.CashoutMessage.TYPE_PAY_INSTRUCTIONS)
                    .collect { rows ->
                        if (handled) return@collect
                        val row = rows.firstOrNull {
                            com.asgaya.husk.data.PayInstructions.fromMessage(it.payload)?.requestId == req.requestId
                        } ?: return@collect
                        handled = true
                        db.cashoutMessageDao().markConsumed(row.id)
                        val pi = com.asgaya.husk.data.PayInstructions.fromMessage(row.payload)
                        runOnUiThread {
                            progress.dismiss()
                            if (pi != null) showInstructionsFromResponse(seller, pi) else showSellerInstructions(seller)
                        }
                    }
            }
            launch {
                delay(20_000)
                if (!handled) {
                    handled = true
                    collector.cancel()
                    runOnUiThread { progress.dismiss(); showSellerInstructions(seller) }
                }
            }
        }
    }

    /** Render the payment instructions from the seller's DM reply. */
    private fun showInstructionsFromResponse(seller: com.asgaya.husk.data.Listing, pi: com.asgaya.husk.data.PayInstructions) {
        val amount = amountPaymentCurrency
        val feeAmount = amount * seller.fee
        val total = amount + feeAmount
        val message = buildString {
            appendLine("👤  ${seller.displayName}  (${seller.cashAccount})")
            appendLine("💳 Method:  ${pi.method}")
            pi.fields.forEach { (k, v) -> appendLine("• $k: $v") }
            appendLine()
            appendLine("Amount:      ${money(amount)} $paymentCurrency")
            appendLine("Seller fee:  ${money(feeAmount)}  (${seller.feePercent})")
            appendLine("You pay:     ${money(total)} $paymentCurrency")
            appendLine()
            appendLine("How it works:")
            val where = pi.fields["location"]?.let { " at $it" }.orEmpty()
            appendLine("1. Pay the seller ${money(total)} $paymentCurrency (${pi.method})$where.")
            appendLine("2. The seller confirms and locks the BCH.")
            appendLine("3. The recipient claims.")
        }
        AlertDialog.Builder(this@SendRemittanceActivity)
            .setTitle("💳 Payment instructions")
            .setMessage(message)
            .setPositiveButton("Continue") { _, _ -> proceedToReview(seller) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Payment instructions for the selected seller, built from the **board listing** (📍 location +
     * ⏰ hours + the payment method). The sender pays the seller; the seller then funds the covenant.
     */
    private fun money(v: Double) = String.format(java.util.Locale.US, "%.2f", v)

    private fun showSellerInstructions(seller: com.asgaya.husk.data.Listing) {
        val location = seller.location?.takeIf { it.isNotBlank() } ?: "(location not provided)"
        val hours = seller.availableHours?.takeIf { it.isNotBlank() }
        val amount = amountPaymentCurrency
        val feeAmount = amount * seller.fee
        val total = amount + feeAmount
        val message = buildString {
            appendLine("👤  ${seller.establishment ?: seller.displayName}  (${seller.cashAccount})")
            appendLine("📍 Where:  $location")
            hours?.let { appendLine("⏰ Hours:  $it") }
            seller.contactPhone?.let { appendLine("📱 Phone:  $it") }
            seller.contactIban?.let { appendLine("🏦 IBAN:  $it") }
            appendLine("💳 Pay via:  ${seller.paymentMethods.joinToString(", ")}")
            appendLine()
            appendLine("Amount:      ${money(amount)} $paymentCurrency")
            appendLine("Seller fee:  ${money(feeAmount)}  (${seller.feePercent})")
            appendLine("You pay:     ${money(total)} $paymentCurrency")
            seller.notes?.takeIf { it.isNotBlank() }?.let { appendLine(); appendLine("📝 $it") }
            appendLine()
            appendLine("How it works:")
            appendLine("1. Pay the seller ${money(total)} $paymentCurrency.")
            appendLine("2. The seller confirms and locks the BCH into the covenant.")
            appendLine("3. The recipient claims.")
        }
        AlertDialog.Builder(this@SendRemittanceActivity)
            .setTitle("💳 Payment instructions")
            .setMessage(message)
            .setPositiveButton("Continue") { _, _ -> proceedToReview(seller) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * One active covenant per recipient (`ui-rethink/19`): a second pending covenant would collide
     * on the reference (the CashAccount the seller correlates the bank notification to). Warn and
     * offer to cancel the pending one before creating another.
     */
    private fun guardActiveCovenant(recipientAddress: String, onProceed: () -> Unit) {
        lifecycleScope.launch {
            val existing = database.remittanceDao().findActiveByRecipient(recipientAddress)
            if (existing == null) {
                runOnUiThread { onProceed() }
                return@launch
            }
            // Informational only — the covenant's state lives on the Activity screen
            // (`RemittanceActivity`): there the user pays if unpaid, or refund()s the live covenant,
            // before creating a new one. The app does NOT guess or cancel from here (`ui-rethink/19`).
            val label = existing.recipientName ?: recipientAddress.take(16)
            runOnUiThread {
                AlertDialog.Builder(this@SendRemittanceActivity)
                    .setTitle("You already have a covenant")
                    .setMessage(
                        "You already have a covenant with $label " +
                            "(€${"%.2f".format(existing.amountPaymentCurrency)})."
                    )
                    .setPositiveButton("OK") { _, _ ->
                        finish()
                        startActivity(Intent(this@SendRemittanceActivity, RemittanceActivity::class.java))
                    }
                    .show()
            }
        }
    }

    /** Seller chosen + instructions seen → create the unfunded covenant on the Review screen. */
    private fun proceedToReview(seller: com.asgaya.husk.data.Listing) {        selectedFundingModel = FUNDING_MODEL_FUNDER
        selectedFunderPubkey = seller.publicKey
        selectedFunderNpub = seller.nostrPubkey
        selectedFunderAdId = seller.id
        selectedFunderFee = seller.fee
        selectedFunderMinPricePercent = seller.minPricePercent

        Log.d(TAG, "Seller selected from board: ${seller.cashAccount} · ${seller.feePercent}")
        Toast.makeText(this@SendRemittanceActivity, "✅ Seller: ${seller.displayName}", Toast.LENGTH_SHORT).show()

        val recipient = binding.recipientInput.text.toString()
        if (recipient.isEmpty()) {
            Toast.makeText(this@SendRemittanceActivity, "⚠️ Enter recipient address first", Toast.LENGTH_LONG).show()
            return
        }
        if (selectedContactPubkey == null) {
            Toast.makeText(this@SendRemittanceActivity, "❌ Recipient public key required - select from contacts", Toast.LENGTH_LONG).show()
            return
        }
        if (amountPaymentCurrency == 0.0) {
            Toast.makeText(this@SendRemittanceActivity, "⚠️ Enter amount first", Toast.LENGTH_LONG).show()
            return
        }

        val rate = bchToEur
        if (rate == null || rate <= 0.0) {
            Toast.makeText(this@SendRemittanceActivity, "⚠️ BCH price unavailable — check your connection", Toast.LENGTH_LONG).show()
            return
        }
        val amountBch = amountEur() / rate
        val intent = Intent(this@SendRemittanceActivity, ReviewSendActivity::class.java).apply {
            putExtra(ReviewSendActivity.EXTRA_RECIPIENT, recipient)
            putExtra(ReviewSendActivity.EXTRA_RECIPIENT_NAME, selectedContactName)
            putExtra(ReviewSendActivity.EXTRA_RECIPIENT_CASH_ACCOUNT, selectedContactCashAccount)
            putExtra(ReviewSendActivity.EXTRA_RECIPIENT_PUBKEY, selectedContactPubkey)
            putExtra(ReviewSendActivity.EXTRA_AMOUNT_BCH, amountBch)
            // Settled face is EUR (`supported-fiats/01 §5`): pass the canonical EUR amount + "EUR",
            // whatever the "You send" field displays (USD from the board path — step 0/bug B).
            putExtra(ReviewSendActivity.EXTRA_AMOUNT_FIAT, amountEur())
            putExtra(ReviewSendActivity.EXTRA_CURRENCY, "EUR")
            putExtra(ReviewSendActivity.EXTRA_PAY_AMOUNT, amountPaymentCurrency)
            putExtra(ReviewSendActivity.EXTRA_PAY_CURRENCY, paymentCurrency)
            putExtra(ReviewSendActivity.EXTRA_AMOUNT_RECIPIENT, amountRecipientCurrency)
            putExtra(ReviewSendActivity.EXTRA_RECIPIENT_CURRENCY, recipientCurrency)
            putExtra(ReviewSendActivity.EXTRA_FUNDING_MODEL, selectedFundingModel)
            putExtra(ReviewSendActivity.EXTRA_FUNDER_PUBKEY, selectedFunderPubkey)
            putExtra(ReviewSendActivity.EXTRA_FUNDER_NPUB, selectedFunderNpub)
            putExtra(ReviewSendActivity.EXTRA_FUNDER_AD_ID, selectedFunderAdId)
            putExtra(ReviewSendActivity.EXTRA_FUNDER_FEE, selectedFunderFee ?: -1.0)
            putExtra(ReviewSendActivity.EXTRA_FUNDER_MIN_PRICE_PERCENT, selectedFunderMinPricePercent ?: -1)
        }
        guardActiveCovenant(recipient) { startActivity(intent) }
    }
}
