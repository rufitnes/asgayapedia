package com.asgaya.husk.trade

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.asgaya.husk.databinding.ActivityTradeBinding
import com.asgaya.husk.data.TestCashoutData
import com.asgaya.husk.data.CashoutRequest
import com.asgaya.husk.data.SimpleCashoutRequest
import com.asgaya.husk.data.MerchantCosignResponse
import com.asgaya.husk.wallet.WalletManager
import com.asgaya.husk.CovenantWebView
import com.asgaya.husk.FiatDisplay
import com.asgaya.husk.FiatRateSource
import com.asgaya.husk.AppDatabase
import com.asgaya.husk.covenant.CovenantRecovery
import com.asgaya.husk.covenant.CovenantResolutionNotifier
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import org.json.JSONObject
import org.json.JSONArray
import java.net.URL
import android.graphics.Bitmap
import android.graphics.Color
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.view.Gravity
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * TradeActivity - Merchant cashout and BCH trading interface
 *
 * This is the "Trade" tab where merchants can:
 * - View pending cashout requests from recipients
 * - Verify covenant parameters
 * - Co-sign and broadcast merchantCashout transactions
 * - (Future: Sell BCH by funding covenants)
 */
class TradeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTradeBinding
    private lateinit var cashoutRequestsAdapter: CashoutRequestsAdapter
    private val cashoutRequests = mutableListOf<CashoutRequest>()

    // Merchant-first flow dependencies
    private lateinit var walletManager: WalletManager
    private lateinit var covenantWebView: CovenantWebView
    private lateinit var database: AppDatabase

    // Seller auto-funding: Pending covenant funding requests (Sell BCH tab)
    private val fundingRequests = mutableListOf<com.asgaya.husk.CovenantRequest>()

    // Merchant card (Sell BCH): covenants funded by THIS device (active) + concluded ones (history)
    private val activeCovenants = mutableListOf<com.asgaya.husk.CovenantRequest>()
    private val covenantHistory = mutableListOf<com.asgaya.husk.CovenantRequest>()

    // Buy BCH: number of signed cash-out sales currently shown (for the tab empty state)
    private var currentSalesCount = 0
    // Selected currency (`fiat_currency`, `supported-fiats`): the funding cards show the amount in
    // it (big) + the guaranteed EUR face (small) — `FiatDisplay`.
    private var mainCurrency = "EUR"
    @Volatile private var eurPerMain: Double? = null

    companion object {
        private const val TAG = "TradeActivity"
        /** Which tab to open from a notification (`buy` = the cash-out sales to broadcast). */
        const val EXTRA_TAB = "trade_tab"
    }

    // QR Scanner launcher
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        handleScanResult(result)
    }

    // Camera permission launcher
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startQRScanner()
        } else {
            showCameraPermissionDeniedDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTradeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "Trade activity created")

        // Initialize merchant-first flow dependencies
        database = AppDatabase.getDatabase(this)
        walletManager = WalletManager.getInstance(this)
        covenantWebView = CovenantWebView.getInstance(this)

        setupToolbar()
        setupTabs()
        if (intent.getStringExtra(EXTRA_TAB) == "buy") {
            binding.tabLayout.getTabAt(1)?.select()
        }
        setupCashoutRequestsList()
        setupScanButton()

        // Resolve the selected currency + its display rate for the funding cards (`FiatDisplay`).
        mainCurrency = getSharedPreferences("BizumParserPrefs", Context.MODE_PRIVATE)
            .getString("fiat_currency", "EUR") ?: "EUR"
        lifecycleScope.launch {
            eurPerMain = FiatRateSource.eurPer(mainCurrency)
            runOnUiThread { refreshFundingRequests() }
        }

        // Merchant call-to-action: signed sales awaiting broadcast (Nostr cash-out)
        lifecycleScope.launch {
            database.cashoutMessageDao()
                .observePending(com.asgaya.husk.data.CashoutMessage.TYPE_SIGNED_TX)
                .collect { rows ->
                    val sales = withContext(Dispatchers.IO) {
                        rows.map { row ->
                            val q = database.cashoutMessageDao()
                                .get(row.covenantAddress, com.asgaya.husk.data.CashoutMessage.TYPE_PURCHASE_COSIGN)
                            val quote = q?.let { com.asgaya.husk.data.MerchantCosignResponse.fromMessage(it.payload) }
                            row to (quote?.amountCash ?: 0.0) to (quote?.currency ?: "EUR")
                        }
                    }
                    runOnUiThread { displayCashoutSales(sales) }
                }
        }
    }

    /** Rates for the ad-asset currencies (the exchange currency), lazily fetched (`ui-rethink/40`). */
    private val rates = java.util.concurrent.ConcurrentHashMap<String, Double>()

    /** The card amount: the sender's payment **datum** (`ui-rethink/40`), or the EUR face rendered
     *  in the ad's asset currency as a fallback. */
    private fun amountStr(request: com.asgaya.husk.CovenantRequest): String {
        val pa = request.payAmount
        val pc = request.payCurrency
        if (pa != null && pa > 0.0 && !pc.isNullOrBlank()) return "%s %.2f".format(pc, pa)
        val cur = request.assetCurrency ?: mainCurrency
        return FiatDisplay.amount(request.eurCents, cur, rates[cur] ?: if (cur == mainCurrency) eurPerMain else null)
    }

    /** The funding-request card line: what the merchant sells (BCH worth) vs what they receive
     *  (amount + the seller's fee) (`ui-rethink/40`). */
    private fun sellLine(r: com.asgaya.husk.CovenantRequest): String {
        val pa = r.payAmount
        if (pa != null && pa > 0.0) {
            val pc = r.payCurrency ?: r.assetCurrency ?: mainCurrency
            val badge = com.asgaya.husk.SupportedFiats.warningLevel(pc)?.let { "$it " } ?: ""
            return "${badge}Sell %s %.2f worth of BCH for %s %.2f".format(pc, pa, pc, pa * (1.0 + (r.feePercent ?: 0.0)))
        }
        return "Amount: ${amountStr(r)}"
    }

    /** What the merchant receives = the amount + the seller's ad fee (`ui-rethink/40`). */
    private fun totalStr(r: com.asgaya.husk.CovenantRequest): String {
        val pa = r.payAmount
        if (pa != null && pa > 0.0) {
            val pc = r.payCurrency ?: r.assetCurrency ?: mainCurrency
            return "%s %.2f".format(pc, pa * (1.0 + (r.feePercent ?: 0.0)))
        }
        return amountStr(r)
    }

    /** Fetch the missing rates for the requests' asset currencies, then re-render. */
    private fun ensureRatesFor(requests: List<com.asgaya.husk.CovenantRequest>) {
        val wanted = requests.mapNotNull { it.assetCurrency }.toSet() - rates.keys - "EUR"
        if (wanted.isEmpty()) return
        lifecycleScope.launch {
            wanted.forEach { rates[it] = FiatRateSource.eurPer(it) ?: 1.0 }
            runOnUiThread { refreshFundingRequests() }
        }
    }

    // Tick the active-covenant countdowns + surface the Recover button the moment a covenant expires.
    private val refreshHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val refreshTick = object : Runnable {
        override fun run() {
            refreshFundingRequests()
            refreshHandler.postDelayed(this, 30_000L)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh lists when activity comes to foreground (catches notifications received while away)
        Log.d(TAG, "Refreshing cashout requests and funding requests")
        refreshCashoutRequests()
        refreshFundingRequests()
        refreshHandler.postDelayed(refreshTick, 30_000L)
    }

    override fun onPause() {
        super.onPause()
        refreshHandler.removeCallbacks(refreshTick)
    }

    private fun refreshCashoutRequests() {
        runOnUiThread {
            cashoutRequests.clear()
            // TightDS Finding 2: Only show PENDING requests (hide COMPLETED)
            cashoutRequests.addAll(CashoutRequestStorage.getAllPending())
            cashoutRequestsAdapter.notifyDataSetChanged()
            applyTabVisibility()
            Log.d(TAG, "Loaded ${cashoutRequests.size} pending cashout requests from storage")
        }
    }

    /**
     * Load pending covenant funding requests (seller auto-funding)
     *
     * Shows covenant_requests with status="awaiting_payment".
     * These are from [FUND_COVENANT] messages parsed by NotificationListener.
     */
    private fun refreshFundingRequests() {
        lifecycleScope.launch {
            try {
                val all = database.covenantDao().getAll()
                val myPubkey = walletManager.getActiveWallet()?.publicKey
                val nowSec = System.currentTimeMillis() / 1000

                // Fund requests: BOTH active (pay window open) and expired (past it) — the seller
                // must still SEE an expired one to tell the customer to recreate (`ui-rethink/27`).
                val requests = all.filter { it.status == "awaiting_payment" }
                // Covenants THIS device funded — the merchant card (active while funded)
                val active = all.filter {
                    it.status == "funded" && (myPubkey == null || it.funderPubkey == myPubkey)
                }
                val history = all.filter {
                    it.status != "awaiting_payment" && it.status != "funded"
                }

                runOnUiThread {
                    fundingRequests.clear(); fundingRequests.addAll(requests)
                    activeCovenants.clear(); activeCovenants.addAll(active)
                    covenantHistory.clear(); covenantHistory.addAll(history)
                    ensureRatesFor(all)
                    displayFundingRequests()
                    Log.d(TAG, "Sell BCH: ${requests.size} requests, ${active.size} active, ${history.size} history")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load funding requests", e)
            }
        }
    }

    /**
     * Merchant call-to-action: show signed sales (Nostr cash-out) with a
     * "Broadcast & hand cash" button. Broadcasting is NEVER automatic — the merchant
     * decides the moment and hands the cash.
     */
    private fun displayCashoutSales(sales: List<Pair<Pair<com.asgaya.husk.data.CashoutMessage, Double>, String>>) {
        currentSalesCount = sales.size
        binding.buySection.findViewWithTag<LinearLayout>("cashoutSalesContainer")
            ?.let { binding.buySection.removeView(it) }

        if (sales.isNotEmpty()) {
            val salesContainer = LinearLayout(this).apply {
                tag = "cashoutSalesContainer"
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, 0, 0, 12)
            }

            salesContainer.addView(TextView(this).apply {
                text = "💶 BCH for sale (${sales.size})"
                textSize = 18f
                setTextColor(Color.parseColor("#A855F7"))
                setPadding(0, 0, 0, 16)
            })

            sales.forEach { e -> salesContainer.addView(createSaleCard(e.first.first, e.first.second, e.second)) }

            // Above the legacy cashout requests list.
            binding.buySection.addView(salesContainer, 0)
        }
        applyTabVisibility()
    }

    private fun createSaleCard(row: com.asgaya.husk.data.CashoutMessage, amountCash: Double, currency: String): android.view.View {
        val card = com.google.android.material.card.MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
            radius = 12f
            cardElevation = 4f
            setCardBackgroundColor(Color.parseColor("#F3E8FF"))
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }
        content.addView(TextView(this).apply {
            text = "💶 A customer wants %s %.2f cash".format(currency, amountCash)
            textSize = 16f
            setTextColor(Color.BLACK)
        })
        content.addView(TextView(this).apply {
            text = "Broadcast the BCH sale, then hand the cash. Covenant: ${row.covenantAddress.take(20)}…"
            textSize = 12f
            setTextColor(Color.DKGRAY)
            setPadding(0, 4, 0, 12)
        })
        val txHex = parseKvField(row.payload, "txHex")
        content.addView(com.google.android.material.button.MaterialButton(this).apply {
            text = "📡 Broadcast transaction"
            setOnClickListener {
                if (txHex.isNullOrBlank()) {
                    Toast.makeText(this@TradeActivity, "No tx in message", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                lifecycleScope.launch { database.cashoutMessageDao().markConsumed(row.id) }
                broadcastTransaction(txHex, amountCash, currency, row.counterpartyNpub, row.covenantAddress)
            }
        })
        card.addView(content)
        return card
    }

    private fun parseKvField(message: String, key: String): String? {
        message.lines().forEach { line ->
            val t = line.trim()
            if (t.startsWith("$key=")) return t.substringAfter("=").trim()
        }
        return null
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Trade"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupCashoutRequestsList() {
        // Load from storage (includes both test data and real notifications)
        // TightDS Finding 2: Only show PENDING requests (hide COMPLETED)
        cashoutRequests.clear()
        cashoutRequests.addAll(CashoutRequestStorage.getAllPending())

        Log.d(TAG, "Loaded ${cashoutRequests.size} pending cashout requests")

        // Setup RecyclerView
        cashoutRequestsAdapter = CashoutRequestsAdapter(
            requests = cashoutRequests,
            onVerifyClick = { request ->
                Log.d(TAG, "Verify clicked for request: ${request.id}")
                openVerificationScreen(request)
            }
        )

        binding.cashoutRequestsList.apply {
            layoutManager = LinearLayoutManager(this@TradeActivity)
            adapter = cashoutRequestsAdapter
        }

        applyTabVisibility()
    }

    /**
     * Two tabs: **Sell BCH** = covenant funding requests (the merchant funds = sells BCH);
     * **Buy BCH** = cash-out sales + cashout requests (the merchant buys BCH).
     */
    private fun setupTabs() {
        binding.tabLayout.removeAllTabs()
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("💸 Sell BCH"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("🛒 Buy BCH"))
        binding.tabLayout.addOnTabSelectedListener(
            object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) = applyTabVisibility()
                override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
                override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            }
        )
        applyTabVisibility()
    }

    /** Show the section for the selected tab and toggle the empty state. */
    private fun applyTabVisibility() {
        val sellSelected = binding.tabLayout.selectedTabPosition == 0
        val sellHas = fundingRequests.isNotEmpty() || activeCovenants.isNotEmpty() || covenantHistory.isNotEmpty()
        val buyHas = cashoutRequests.isNotEmpty() || currentSalesCount > 0

        binding.sellSection.visibility = if (sellSelected && sellHas) android.view.View.VISIBLE else android.view.View.GONE
        binding.buySection.visibility = if (!sellSelected && buyHas) android.view.View.VISIBLE else android.view.View.GONE

        val showEmpty = if (sellSelected) !sellHas else !buyHas
        if (showEmpty) {
            binding.emptyEmoji.text = if (sellSelected) "💸" else "📭"
            binding.emptyTitle.text = if (sellSelected) "No funding requests" else "No cashout requests"
            binding.emptySubtitle.text = if (sellSelected) {
                "You'll see covenant funding requests here when a sender asks you to fund one (you sell BCH)."
            } else {
                "You'll see cashout requests and BCH-for-sale offers here (you buy BCH)."
            }
            binding.pasteRequestButton.visibility = if (sellSelected) android.view.View.GONE else android.view.View.VISIBLE
        }
        binding.emptyState.visibility = if (showEmpty) android.view.View.VISIBLE else android.view.View.GONE
    }

    /**
     * Display pending covenant funding requests (Sell BCH tab).
     *
     * SELLER AUTO-FUNDING (Cash-in-person flow):
     * 1. Sender shares [FUND_COVENANT] message → parsed by NotificationListener
     * 2. Shows here with "Confirm Cash Received" button
     * 3. Seller clicks button → funds covenant
     */
    private fun displayFundingRequests() {
        binding.sellSection.findViewWithTag<LinearLayout>("fundingRequestsContainer")
            ?.let { binding.sellSection.removeView(it) }

        val container = LinearLayout(this).apply {
            tag = "fundingRequestsContainer"
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 0, 0, 12)
        }

        // 1) Funding requests — cash received, fund the covenant
        if (fundingRequests.isNotEmpty()) {
            container.addView(sectionHeader("💰 Funding requests (${fundingRequests.size})", "#F59E0B"))
            fundingRequests.forEach { container.addView(createFundingRequestCard(it)) }
        }

        // 2) Active covenants we funded — the merchant card (stays while the covenant is alive)
        if (activeCovenants.isNotEmpty()) {
            container.addView(sectionHeader("🟢 Active covenants (${activeCovenants.size})", "#16A34A"))
            activeCovenants.forEach { container.addView(createActiveCovenantCard(it)) }
        }

        // 3) Concluded covenants — history
        if (covenantHistory.isNotEmpty()) {
            container.addView(sectionHeader("📜 History (${covenantHistory.size})", "#64748B"))
            covenantHistory.forEach { container.addView(createHistoryCard(it)) }
        }

        if (container.childCount > 0) binding.sellSection.addView(container)
        applyTabVisibility()
    }

    private fun sectionHeader(text: String, color: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 18f
        setTextColor(Color.parseColor(color))
        setPadding(0, 8, 0, 16)
    }

    /**
     * Merchant card: a covenant THIS device funded that is still alive. Offers a manual
     * **Recover buffer** (the safety net, and the MANUAL-mode path — `ui-rethink/10`).
     */
    private fun createActiveCovenantCard(request: com.asgaya.husk.CovenantRequest): android.view.View {
        val card = androidx.cardview.widget.CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
            radius = 12f
            cardElevation = 4f
            setCardBackgroundColor(Color.parseColor("#F0FDF4"))  // light green
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        // 1) The covenant address is what tells one covenant from another in the list.
        val addressPreview = "${request.covenantAddress.take(14)}...${request.covenantAddress.takeLast(8)}"
        content.addView(TextView(this).apply {
            text = "📍 $addressPreview"
            textSize = 13f
            setTextColor(Color.parseColor("#64748B"))
            setPadding(0, 0, 0, 8)
        })
        // 2) A neutral reference: the CashAccount (`Name#NNN`), not the word "recipient".
        content.addView(TextView(this).apply {
            text = "reference ${request.cashAccount}"
            textSize = 16f
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, 8)
        })
        content.addView(TextView(this).apply {
            text = "${totalStr(request)} · funded"
            textSize = 20f
            setTextColor(Color.parseColor("#16A34A"))
            setPadding(0, 0, 0, 8)
        })
        content.addView(TextView(this).apply {
            text = "⏳ Expires ${formatExpiry(request.expiryTime)}"
            textSize = 13f
            setTextColor(Color.parseColor("#64748B"))
            setPadding(0, 0, 0, 8)
        })
        (request.fundingTxid ?: request.txid)?.takeIf { it.isNotBlank() }?.let { txid ->
            content.addView(TextView(this).apply {
                text = "txid: ${txid.take(16)}…"
                textSize = 11f
                setTextColor(Color.GRAY)
                setPadding(0, 0, 0, 12)
            })
        }

        if (request.expiryTime <= System.currentTimeMillis() / 1000) {
            content.addView(android.widget.Button(this).apply {
                text = "🛡️ Recover buffer"
                textSize = 15f
                setBackgroundColor(Color.parseColor("#16A34A"))
                setTextColor(Color.WHITE)
                setPadding(24, 16, 24, 16)
                setOnClickListener { executeSellerRecover(request) }
            })
        }
        // Before expiry there is no button (the buffer is only recoverable once expired).

        card.addView(content)
        return card
    }

    /** A concluded covenant — the merchant's history (audit trail, `58`). */
    private fun createHistoryCard(request: com.asgaya.husk.CovenantRequest): android.view.View {
        val card = androidx.cardview.widget.CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 12) }
            radius = 12f
            cardElevation = 2f
            setCardBackgroundColor(Color.parseColor("#F8FAFC"))
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 20, 24, 20)
        }

        val label = when (request.status) {
            "buffer_recovered" -> "🛡️ Buffer recovered"
            "resolved_externally" -> "✅ Settled (resolved on-chain)"
            "resolved" -> "✅ Settled (on-chain)"
            "claimed" -> "✅ Claimed"
            else -> "✅ ${request.status.replace('_', ' ')}"
        }
        val addressPreview = "${request.covenantAddress.take(14)}...${request.covenantAddress.takeLast(8)}"
        content.addView(TextView(this).apply {
            text = "📍 $addressPreview"
            textSize = 13f
            setTextColor(Color.parseColor("#64748B"))
            setPadding(0, 0, 0, 6)
        })
        content.addView(TextView(this).apply {
            text = "$label · €${"%.2f".format(request.eurCents / 100.0)}"
            textSize = 15f
            setTextColor(Color.parseColor("#334155"))
        })
        content.addView(TextView(this).apply {
            text = "reference ${request.cashAccount}"
            textSize = 12f
            setTextColor(Color.GRAY)
        })
        request.txid?.takeIf { it.isNotBlank() }?.let { txid ->
            content.addView(TextView(this).apply {
                text = "txid: ${txid.take(16)}…"
                textSize = 11f
                setTextColor(Color.GRAY)
            })
        }

        card.addView(content)
        return card
    }

    private fun formatExpiry(expirySec: Long): String {
        val mins = (expirySec - System.currentTimeMillis() / 1000) / 60
        return when {
            mins <= 0 -> "expired"
            mins < 60 -> "in ${mins}m"
            else -> "in ${mins / 60}h ${mins % 60}m"
        }
    }

    /**
     * Manual buffer recovery for a funded covenant. Bypasses the `AutoRefundPrefs.MANUAL` gate
     * that the automatic sweep honours — this IS the manual action.
     */
    private fun executeSellerRecover(request: com.asgaya.husk.CovenantRequest) {
        lifecycleScope.launch {
            try {
                val activeWallet = walletManager.getActiveWallet()
                    ?: throw Exception("No active wallet")
                if (request.funderPubkey != activeWallet.publicKey) {
                    throw Exception("Active wallet is not this covenant's funder")
                }

                // The covenant's clock is the ORACLE — never act before it is past the expiry
                val oracle = CovenantRecovery.fetchOraclePrice()
                if (!CovenantRecovery.isRecoverable(oracle.timestamp, request.expiryTime)) {
                    Toast.makeText(this@TradeActivity, "⏳ Not expired yet (oracle time)", Toast.LENGTH_LONG).show()
                    return@launch
                }

                // The chain is the truth: conclude only on positive spend evidence (`18`);
                // a not-yet-indexed outpoint is NOT "spent".
                if (CovenantRecovery.isCovenantSpent(request) == true) {
                    database.covenantDao().updateStatus(request.id, "resolved_externally")
                    Toast.makeText(
                        this@TradeActivity,
                        "✅ Already resolved on-chain (someone else acted)",
                        Toast.LENGTH_LONG
                    ).show()
                    refreshFundingRequests()
                    return@launch
                }

                Toast.makeText(this@TradeActivity, "🛡️ Recovering buffer…", Toast.LENGTH_SHORT).show()
                if (!covenantWebView.isReady()) covenantWebView.awaitReady(timeoutMs = 30_000)

                val wif = walletManager.getWIF(activeWallet)
                val txid = CovenantRecovery.recoverSellerBuffer(
                    covenant = request,
                    activeWallet = activeWallet,
                    wif = wif,
                    covenantWebView = covenantWebView,
                    oracle = oracle
                )
                database.covenantDao().updateStatusAndTxid(request.id, "buffer_recovered", txid)
                CovenantResolutionNotifier.notify(
                    this@TradeActivity, request,
                    com.asgaya.husk.data.CovenantResolved.PATH_SELLER_RECOVER, txid
                )
                Toast.makeText(
                    this@TradeActivity,
                    "🛡️ Buffer recovered\nTXID: ${txid.take(20)}…",
                    Toast.LENGTH_LONG
                ).show()
                refreshFundingRequests()
            } catch (e: Exception) {
                Log.e(TAG, "Manual buffer recovery failed", e)
                Toast.makeText(this@TradeActivity, "❌ Recovery failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Create UI card for a single funding request
     */
    private fun createFundingRequestCard(request: com.asgaya.husk.CovenantRequest): android.view.View {
        val card = androidx.cardview.widget.CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
            radius = 12f
            cardElevation = 4f
            setCardBackgroundColor(Color.parseColor("#FFF7ED"))  // Light orange background

            // TEST: Make entire card clickable
            isClickable = true
            setOnClickListener {
                Log.d(TAG, "🔥 CARD CLICKED! Testing if ANY clicks work...")
                confirmCashReceivedAndFund(request)
            }
        }

        val cardContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            isClickable = false  // Don't intercept clicks
            isFocusable = false
        }

        // Cash account (e.g., "Elena#142")
        cardContent.addView(TextView(this).apply {
            text = "💵 Cash Account: ${request.cashAccount}"
            textSize = 16f
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, 8)
        })

        // Amount — the merchant's selected currency (big) + the guaranteed EUR face (small).
        cardContent.addView(TextView(this).apply {
            text = sellLine(request)
            textSize = 20f
            setTextColor(Color.parseColor("#F59E0B"))  // Orange
            setPadding(0, 0, 0, 8)
        })

        // The sender's pay window (`ui-rethink/27`) — past it the request is expired (hard window):
        // the seller must ask the customer to create a new covenant.
        val payByMs = com.asgaya.husk.covenant.CovenantRules.payByMillis(request.createdAt, request.payWindowSeconds)
        val expired = payByMs <= System.currentTimeMillis()
        val payMins = ((payByMs - System.currentTimeMillis()) / 60_000).coerceAtLeast(0)
        cardContent.addView(TextView(this).apply {
            text = if (expired) "⚠️ Expired — pay window closed" else "⏳ Pay within $payMins min"
            textSize = 13f
            setTextColor(Color.parseColor(if (expired) "#B91C1C" else "#B45309"))
            setPadding(0, 0, 0, 8)
        })

        // Covenant address (preview)
        val addressPreview = "${request.covenantAddress.take(14)}...${request.covenantAddress.takeLast(8)}"
        cardContent.addView(TextView(this).apply {
            text = "Covenant: $addressPreview"
            textSize = 12f
            setTextColor(Color.GRAY)
            setPadding(0, 0, 0, 16)
        })

        // Button: fund an active request, or — if the pay window closed — tell the seller to ask
        // the customer to recreate (`ui-rethink/27`).
        val fundButton = android.widget.Button(this).apply {
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(24, 16, 24, 16)
            isClickable = true
            isFocusable = true
            if (expired) {
                text = "⚠️ EXPIRED"
                setBackgroundColor(Color.parseColor("#94A3B8"))  // grey
                setOnClickListener {
                    android.app.AlertDialog.Builder(this@TradeActivity)
                        .setTitle("Request expired")
                        .setMessage(
                            "More than ${(payByMs - request.createdAt) / 60_000} minutes since the " +
                                "covenant was created. Ask your customer to create a new covenant."
                        )
                        .setPositiveButton("OK", null)
                        .show()
                }
            } else {
                text = "✅ CONFIRM PAYMENT & FUND"
                setBackgroundColor(Color.parseColor("#F59E0B"))  // Orange
                setOnClickListener {
                    Log.d(TAG, "🔘 Funding request button: ${request.cashAccount}")
                    confirmCashReceivedAndFund(request)
                }
            }
        }
        cardContent.addView(fundButton)
        Log.d(TAG, "✅ Created funding button for: ${request.cashAccount} (expired=$expired)")

        card.addView(cardContent)
        return card
    }

    private fun setupScanButton() {
        // Empty state button - manual paste only
        binding.pasteRequestButton.setOnClickListener {
            handlePasteRequest()
        }

        // FAB - tap to scan, long-press for manual paste
        binding.scanQrFab.setOnClickListener {
            requestCameraPermissionAndScan()
        }

        binding.scanQrFab.setOnLongClickListener {
            handlePasteRequest()
            true
        }

        // My ads: manage your own listings (add / edit / publish / delete)
        binding.publishAdFab.setOnClickListener {
            startActivity(Intent(this, MyAdsActivity::class.java))
        }
    }

    /**
     * Confirm cash received and fund covenant (SELLER AUTO-FUNDING)
     *
     * CASH-IN-PERSON FLOW:
     * 1. Seller receives [FUND_COVENANT] message → shows in Trade tab
     * 2. Sender hands cash to seller in person
     * 3. Seller clicks this button → funds covenant
     * 4. Sender's app detects UTXO → updates status
     *
     * Reuses the same funding logic as NotificationListener.handleBizumPayment
     */
    private fun confirmCashReceivedAndFund(request: com.asgaya.husk.CovenantRequest) {
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "💰 CASH RECEIVED - FUNDING COVENANT")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "Cash Account: ${request.cashAccount}")
        Log.d(TAG, "Amount: €${request.eurCents / 100.0}")
        Log.d(TAG, "Covenant: ${request.covenantAddress.take(30)}...")

        // Confirmation dialog
        AlertDialog.Builder(this)
            .setTitle("Confirm payment & fund")
            .setMessage(
                "Confirm you received ${totalStr(request)}?\n\n" +
                "Cash Account: ${request.cashAccount}\n" +
                "Covenant: ${request.covenantAddress.take(20)}...\n\n" +
                "This will fund the covenant from your SELLER wallet."
            )
            .setPositiveButton("✅ Yes, Fund Covenant") { _, _ ->
                fundCovenantFromSellerWallet(request)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Fund covenant from SELLER wallet
     *
     * This is the core funding logic (same as NotificationListener.handleBizumPayment)
     */
    private fun fundCovenantFromSellerWallet(request: com.asgaya.husk.CovenantRequest) {
        lifecycleScope.launch {
            try {
                // Show progress
                runOnUiThread {
                    Toast.makeText(this@TradeActivity, "Funding covenant...", Toast.LENGTH_SHORT).show()
                }

                // Get ACTIVE wallet (passive user must have correct merchant wallet active)
                val activeWallet = com.asgaya.husk.wallet.WalletManager.getInstance(this@TradeActivity)
                    .getActiveWallet()

                if (activeWallet == null) {
                    runOnUiThread {
                        Toast.makeText(
                            this@TradeActivity,
                            "❌ No active wallet found. Switch to your merchant wallet in Wallets tab.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }

                Log.d(TAG, "Funding with active wallet: ${activeWallet.label}")
                val sellerWallet = activeWallet
                val sellerAddress = sellerWallet.address
                val sellerWif = sellerWallet.sourceData  // TODO: Decrypt in production

                Log.d(TAG, "Seller wallet: ${sellerWallet.label}")
                Log.d(TAG, "Seller address: ${sellerAddress.take(30)}...")

                // Funding price gate (`ui-rethink/27`/`29`): fund only if the price hasn't dropped
                // more than 1.5 % since the covenant's `initial` (a rise is free). If we can't read
                // the oracle we do NOT fund — loud, like every money path.
                val oracle = runCatching { CovenantRecovery.fetchOraclePrice() }.getOrNull()
                if (oracle == null) {
                    Log.e(TAG, "⛔ Funding refused: oracle unavailable")
                    runOnUiThread {
                        Toast.makeText(this@TradeActivity, "⛔ Oracle unavailable — retry in a moment", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }
                if (!com.asgaya.husk.covenant.CovenantRules.isFundingPriceAcceptable(request.initialBchPrice, oracle.price)) {
                    Log.w(TAG, "⛔ Funding refused: current €${oracle.price / 100.0} < initial×0.985 (€${request.initialBchPrice * 0.985 / 100.0})")
                    runOnUiThread {
                        Toast.makeText(this@TradeActivity, "⛔ Price moved too much — ask the sender for a fresh covenant", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

                // Wait for WebView bridge to be ready (⚠️1: Lifecycle gate)
                withContext(Dispatchers.Main) {
                    if (!covenantWebView.isReady()) {
                        Log.d(TAG, "⏳ WebView not ready, waiting...")
                        covenantWebView.awaitReady(timeoutMs = 30_000)
                    }
                }

                // Recompute funding amount with fresh price (⚠️5: Prevent under-funding)
                // Use price from [FUND_COVENANT] message (sender's price at creation time)
                val currentBchPriceInCents = request.initialBchPrice
                val minPricePercent = request.minPricePercent
                val eurCents = request.eurCents

                val bchAmount = eurCents.toDouble() / currentBchPriceInCents
                val bufferedBchAmount = bchAmount * (100.0 / minPricePercent)
                val amountSats = (bufferedBchAmount * 100000000).toLong()

                Log.d(TAG, "Funding calculation:")
                Log.d(TAG, "  EUR: €${eurCents / 100.0}")
                Log.d(TAG, "  Price: €${currentBchPriceInCents / 100.0}/BCH")
                Log.d(TAG, "  Buffer: ${minPricePercent}% (${100 - minPricePercent}% drop protection)")
                Log.d(TAG, "  Amount: $bufferedBchAmount BCH ($amountSats sats)")

                // Fund covenant via sendBchHybrid (v0.2 hybrid pattern)
                Log.d(TAG, "📡 Broadcasting funding transaction...")
                val txid = try {
                    kotlinx.coroutines.withTimeout(30_000) {
                        covenantWebView.sendBchHybrid(
                            senderWIF = sellerWif,
                            senderAddress = sellerAddress,
                            recipientAddress = request.covenantAddress,
                            amountSats = amountSats
                        )
                    }
                } catch (e: Exception) {
                    // Retry once after 1s
                    Log.w(TAG, "⚠️ First attempt failed, retrying in 1s...", e)
                    kotlinx.coroutines.delay(1000)
                    kotlinx.coroutines.withTimeout(30_000) {
                        covenantWebView.sendBchHybrid(
                            senderWIF = sellerWif,
                            senderAddress = sellerAddress,
                            recipientAddress = request.covenantAddress,
                            amountSats = amountSats
                        )
                    }
                }

                Log.d(TAG, "✅ Covenant funded! TXID: $txid")

                // Update database
                database.covenantDao().updateFunded(
                    id = request.id,
                    status = "funded",
                    txid = txid,
                    fundedAt = System.currentTimeMillis()
                )

                // Arm the expiry alarm → sellerRecoverBuffer at expiry (`ui-rethink/10`)
                CovenantRecovery.scheduleExpiryAlarm(this@TradeActivity, request)

                // Record the funding outpoint (`txid:vout`) for evidence-based audits (`ui-rethink/16` P0-1)
                CovenantRecovery.recordFundingOutpoint(database, request, txid)

                Log.d(TAG, "✅ Database updated: covenant_requests → funded")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // Tell the SENDER the covenant is funded (`[COVENANT_FUNDED]`) → the sender verifies the
                // amount and delivers the offer to the recipient **instantly** (vs the periodic sweep).
                runCatching {
                    val senderNpub = request.senderNostrPubkey
                    if (!senderNpub.isNullOrBlank()) {
                        withContext(Dispatchers.IO) {
                            val km = com.asgaya.husk.nostr.NostrKeyManager(this@TradeActivity)
                            val keys = km.ensureNostrKeys(activeWallet)
                            val msg = "[COVENANT_FUNDED]\ncovenantAddress=${request.covenantAddress}\ntxid=$txid\n[/COVENANT_FUNDED]"
                            val gw = com.asgaya.husk.nostr.NIP17GiftWrap.wrapDM(
                                msg, keys.privateKeyHex, keys.publicKeyHex, senderNpub
                            )
                            val client = com.asgaya.husk.nostr.NostrClient.getInstance(keys.publicKeyHex, null)
                            client.connect()
                            client.publishEvent(gw)
                            Log.i(TAG, "💰 [COVENANT_FUNDED] sent to sender ${senderNpub.take(12)}…")
                        }
                    } else {
                        Log.w(TAG, "No senderNostrPubkey on the request — sender not pinged (sweep will handle)")
                    }
                }

                // Show success and refresh UI
                runOnUiThread {
                    Toast.makeText(
                        this@TradeActivity,
                        "✅ Covenant funded! TXID: ${txid.take(16)}...",
                        Toast.LENGTH_LONG
                    ).show()

                    // Refresh lists
                    refreshFundingRequests()
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to fund covenant", e)
                runOnUiThread {
                    Toast.makeText(
                        this@TradeActivity,
                        "❌ Failed to fund covenant: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun requestCameraPermissionAndScan() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                startQRScanner()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                // Show explanation before requesting
                showCameraPermissionRationale()
            }

            else -> {
                // Request permission
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun showCameraPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Camera Permission Needed")
            .setMessage("To scan customer QR codes, Asgaya needs access to your camera. No photos are stored.")
            .setPositiveButton("Grant Permission") { _, _ ->
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "Use long-press for manual paste", Toast.LENGTH_LONG).show()
            }
            .show()
    }

    private fun showCameraPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Camera Permission Denied")
            .setMessage("QR scanning won't work without camera access. You can still use manual paste (long-press FAB).")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }
            .setNegativeButton("OK", null)
            .show()
    }

    private fun startQRScanner() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Scan customer's cashout QR code")
            setCameraId(0)  // Back camera
            setBeepEnabled(true)
            setBarcodeImageEnabled(false)
            setOrientationLocked(true)
            setTimeout(30000)  // 30 second timeout
        }

        Log.d(TAG, "📸 Launching QR scanner...")
        barcodeLauncher.launch(options)
        Log.d(TAG, "📸 Scanner launch initiated")
    }

    private fun handleScanResult(result: ScanIntentResult) {
        Log.d(TAG, "🎯 handleScanResult called! contents=${if (result.contents == null) "NULL" else "${result.contents.length} chars"}")

        when {
            result.contents == null -> {
                // User cancelled or timeout
                Log.w(TAG, "❌ Scan cancelled or timeout")
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show()
            }
            else -> {
                // QR code scanned successfully
                Log.d(TAG, "📷 QR code scanned: ${result.contents.length} chars")
                handleScannedQRCode(result.contents)
            }
        }
    }

    private fun handleScannedQRCode(message: String) {
        Log.d(TAG, "📷 Processing scanned QR code: ${message.length} chars")

        // MERCHANT-FIRST FLOW: Detect new [BCH_FOR_SALE] format (simple, no signatures)
        if (message.contains("[BCH_FOR_SALE]")) {
            Log.d(TAG, "🎯 MERCHANT-FIRST FLOW: Detected simple cashout request")
            handleMerchantFirstRequest(message)
            return
        }

        // MERCHANT-FIRST FLOW: Detect [SIGNED_TX] format (fully-signed transaction from recipient)
        if (message.contains("[SIGNED_TX]")) {
            Log.d(TAG, "🎯 MERCHANT-FIRST FLOW: Detected fully-signed transaction")
            handleSignedTransaction(message)
            return
        }

        // OLD FLOW: [CASH_IN_PERSON] with pre-signatures (backward compatibility)
        if (message.contains("[CASH_IN_PERSON]")) {
            Log.d(TAG, "⚠️ OLD FLOW: Detected pre-signed cashout request (backward compat)")
            handleLegacyCashoutRequest(message)
            return
        }

        // Invalid format
        Toast.makeText(
            this,
            "Invalid cashout QR code. Expected [BCH_FOR_SALE], [SIGNED_TX], or [CASH_IN_PERSON] format.",
            Toast.LENGTH_LONG
        ).show()
        Log.e(TAG, "❌ Failed to parse QR code: Unknown format")
    }

    /**
     * OLD FLOW (backward compatibility): Handle pre-signed [CASH_IN_PERSON] requests
     */
    private fun handleLegacyCashoutRequest(message: String) {
        val cashoutRequest = parseCashInPersonMessage(message)

        when {
            cashoutRequest == null -> {
                Toast.makeText(
                    this,
                    "Invalid cashout QR code. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
                Log.e(TAG, "❌ Failed to parse legacy format")
            }

            isDuplicate(cashoutRequest) -> {
                Toast.makeText(
                    this,
                    "This request is already in the list",
                    Toast.LENGTH_SHORT
                ).show()
                Log.w(TAG, "⚠️ Duplicate cashout request: ${cashoutRequest.covenantAddress}")
            }

            else -> {
                CashoutRequestStorage.add(cashoutRequest)
                Toast.makeText(
                    this,
                    "✅ Cashout request added: €${String.format("%.2f", cashoutRequest.amountFiat)}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.i(TAG, "✅ Added cashout request: €${cashoutRequest.amountFiat} for ${cashoutRequest.amountBCH} BCH")
            }
        }
    }

    private fun isDuplicate(request: CashoutRequest): Boolean {
        return cashoutRequests.any {
            it.covenantAddress == request.covenantAddress
        }
    }

    private fun handlePasteRequest() {
        Log.d(TAG, "📋 Paste request clicked")

        try {
            // Get clipboard content
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = clipboard.primaryClip

            if (clipData == null || clipData.itemCount == 0) {
                Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                return
            }

            val clipText = clipData.getItemAt(0).text?.toString()
            if (clipText.isNullOrBlank()) {
                Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                return
            }

            Log.d(TAG, "Clipboard content length: ${clipText.length}")
            Log.d(TAG, "First 100 chars: ${clipText.take(100)}")

            // Parse [CASH_IN_PERSON] message
            val parsedRequest = parseCashInPersonMessage(clipText)

            if (parsedRequest != null) {
                // Store in shared storage
                CashoutRequestStorage.add(parsedRequest)

                // Add to list
                cashoutRequests.add(0, parsedRequest)  // Add at top
                cashoutRequestsAdapter.notifyItemInserted(0)
                binding.cashoutRequestsList.scrollToPosition(0)
                applyTabVisibility()

                Toast.makeText(
                    this,
                    "✅ Cashout request added!\n€${String.format("%.2f", parsedRequest.amountFiat)}",
                    Toast.LENGTH_LONG
                ).show()

                Log.d(TAG, "✅ Parsed and added cashout request: ${parsedRequest.id}")
            } else {
                Toast.makeText(
                    this,
                    "❌ Invalid message format\n\nExpected [CASH_IN_PERSON] format",
                    Toast.LENGTH_LONG
                ).show()
                Log.e(TAG, "Failed to parse clipboard content")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error handling paste", e)
            Toast.makeText(this, "❌ Paste failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // ============================================================================
    // MERCHANT-FIRST FLOW (Phase 3)
    // ============================================================================

    /**
     * MERCHANT-FIRST FLOW: Handle simple [BCH_FOR_SALE] from recipient
     *
     * Flow:
     * 1. Parse recipient's simple request (just covenant params, no signatures)
     * 2. Fetch fresh oracle (at the counter!)
     * 3. Build transaction with full txHex
     * 4. Merchant pre-signs
     * 5. Show [BCH_PURCHASE_COSIGN] QR for recipient to scan
     * 6. Wait for recipient's co-signed tx
     * 7. Broadcast!
     */
    private fun handleMerchantFirstRequest(message: String) {
        Log.d(TAG, "🎯 MERCHANT-FIRST: Parsing simple cashout request...")

        // Parse recipient's simple request
        val simpleRequest = com.asgaya.husk.data.SimpleCashoutRequest.fromMessage(message)
        if (simpleRequest == null) {
            Toast.makeText(this, "❌ Invalid cashout request", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Failed to parse [BCH_FOR_SALE]")
            return
        }

        Log.d(TAG, "✅ Parsed simple request:")
        Log.i(TAG, "  parsed assetCurrency='${simpleRequest.assetCurrency}' (main=$mainCurrency)")
        Log.d(TAG, "  EUR: €${simpleRequest.amountFiat}")
        Log.d(TAG, "  Covenant: ${simpleRequest.covenantAddress}")
        Log.d(TAG, "  Recipient pubkey: ${simpleRequest.recipientPubkey}")

        // Show progress toast
        Toast.makeText(this, "Fetching fresh oracle...", Toast.LENGTH_SHORT).show()

        // Launch async flow
        lifecycleScope.launch {
            try {
                // Step 1: Fetch fresh oracle (at the counter!)
                val (oracleSig, oracleMessage, currentPrice) = fetchFreshOracle(
                    simpleRequest.oraclePubkey
                )
                Log.d(TAG, "✅ Oracle fetched: €${currentPrice}/BCH (${oracleMessage.length/2} bytes)")

                // Step 2: Get merchant wallet
                val merchantWallet = walletManager.getActiveWallet()
                if (merchantWallet == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@TradeActivity, "❌ No active merchant wallet", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }
                val merchantWIF = walletManager.getWIF(merchantWallet)
                val merchantPubkey = merchantWallet.publicKey
                Log.d(TAG, "  Merchant pubkey: $merchantPubkey")

                // Step 3: Build transaction with full txHex (TightDS Amendment 3)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TradeActivity, "Building transaction...", Toast.LENGTH_SHORT).show()
                }

                val txResult = buildAndSignMerchantTransaction(
                    simpleRequest = simpleRequest,
                    merchantPubkey = merchantPubkey,
                    merchantWIF = merchantWIF,
                    oracleSig = oracleSig,
                    oracleMessage = oracleMessage,
                    currentPrice = currentPrice
                )
                Log.d(TAG, "✅ Transaction built and pre-signed by merchant")
                Log.d(TAG, "  txHex length: ${txResult.txHex.length} chars")
                Log.d(TAG, "  merchantSig: ${txResult.merchantSig.take(20)}...")

                // Step 4: Calculate amounts for display — in the exchange currency the merchant hands over.
                val amountBCH = (simpleRequest.eurCents.toDouble() / currentPrice) / 100.0
                val exchangeCurrency = simpleRequest.assetCurrency ?: mainCurrency
                // The merchant's own ad fee for this currency (`bulletin-board/20`): applied on the
                // reference so the merchant sets its price (the reference is the guardrail). A negative
                // fee pays the recipient a premium.
                val fee = withContext(Dispatchers.IO) {
                    database.listingDao().getByPublicKey(merchantPubkey)
                        .map { it.toListing() }
                        .firstOrNull { l -> l.assets.any { a -> a.equals(exchangeCurrency, ignoreCase = true) } }
                        ?.fee ?: 0.0
                }
                val amountCash = (simpleRequest.eurCents / 100.0) *
                    (com.asgaya.husk.FiatRateSource.eurPer(exchangeCurrency) ?: 1.0) * (1.0 - fee)
                Log.d(TAG, "  Exchange: $exchangeCurrency fee=${"%.2f".format(fee * 100)}% → cash $amountCash")

                // Step 5: Create MerchantCosignResponse
                val cosignResponse = MerchantCosignResponse(
                    transactionHex = txResult.txHex,
                    covenantAddress = simpleRequest.covenantAddress,
                    merchantPubkey = merchantPubkey,
                    merchantAddress = txResult.merchantAddress,
                    funderAddress = txResult.funderAddress,
                    oracleSig = oracleSig,
                    oracleMessage = oracleMessage,
                    currentPrice = currentPrice,
                    amountCash = amountCash,
                    amountBCH = amountBCH,
                    currency = exchangeCurrency,
                    recipientSigIndex = 0  // Recipient inserts sig at position 0 in unlocking script
                )

                // Step 6: Show cosign QR for recipient to scan
                // Generate QR in background (can take time for dense QR)
                val message = cosignResponse.toMessage()
                val qrBitmap = withContext(Dispatchers.Default) {
                    generateQRCode(message, errorCorrection = ErrorCorrectionLevel.L)
                }

                // Show dialog on UI thread
                withContext(Dispatchers.Main) {
                    showMerchantCosignQR(cosignResponse, simpleRequest, qrBitmap)
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Merchant-first flow failed", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@TradeActivity,
                        "❌ Failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    /**
     * Fetch fresh oracle signature at the counter
     * Returns (oracleSig, oracleMessage, currentPrice)
     */
    private suspend fun fetchFreshOracle(oraclePubkey: String): Triple<String, String, Double> {
        return withContext(Dispatchers.IO) {
            // Call oracle HTTP service (Pi-chan oracle)
            val oracleUrl = "http://localhost:3001/oracle/price"
            Log.d(TAG, "Fetching oracle from: $oracleUrl")

            val response = URL(oracleUrl).readText()
            val json = JSONObject(response)

            // Oracle response format from Pi-chan
            val signature = json.getString("signature")       // DER signature (hex)
            val message = json.getString("message")           // Oracle message (hex)
            val price = json.getInt("price")                  // Price in cents
            val timestamp = json.getLong("timestamp")         // Unix timestamp

            val currentPrice = price / 100.0  // Convert cents to euros

            Log.d(TAG, "Oracle response:")
            Log.d(TAG, "  signature: ${signature.take(40)}...")
            Log.d(TAG, "  message: $message")
            Log.d(TAG, "  price: €${currentPrice}/BCH")
            Log.d(TAG, "  timestamp: $timestamp")

            Triple(signature, message, currentPrice)
        }
    }

    /**
     * Build transaction and merchant pre-signs
     * Returns (transactionHex, merchantSig)
     *
     * TightDS Amendment 3: Pass full transactionHex, not just params
     */
    data class MerchantTxResult(
        val txHex: String,
        val merchantSig: String,
        val merchantAddress: String,
        val funderAddress: String
    )

    private suspend fun buildAndSignMerchantTransaction(
        simpleRequest: SimpleCashoutRequest,
        merchantPubkey: String,
        merchantWIF: String,
        oracleSig: String,
        oracleMessage: String,
        currentPrice: Double
    ): MerchantTxResult {
        Log.d(TAG, "=== Building & Pre-Signing Merchant Transaction ===")

        // Step 1: Fetch UTXOs for covenant
        Log.d(TAG, "📡 Fetching covenant UTXOs...")
        val utxos = withContext(Dispatchers.IO) {
            com.asgaya.husk.covenant.CovenantBuildService.fetchUTXOs(simpleRequest.covenantAddress)
        }
        Log.d(TAG, "✅ Found ${utxos.size} UTXOs")

        if (utxos.isEmpty()) {
            throw Exception("No UTXOs found - covenant not funded or already spent")
        }

        // Get merchant address before entering coroutine
        val merchantAddress = getMerchantAddress()
        Log.d(TAG, "Using merchant address: $merchantAddress")

        // Step 2: Call JavaScript to build & sign
        Log.d(TAG, "🔨 Calling JavaScript to build & pre-sign...")
        val result = suspendCancellableCoroutine<String> { continuation ->

            val covenantWebView = CovenantWebView.getInstance(this@TradeActivity)

            // Set up listener
            covenantWebView.merchantPreSignListener = object : CovenantWebView.TransactionBuildListener {
                override fun onBuilt(resultJson: String) {
                    Log.d(TAG, "✅ Merchant pre-sign complete!")
                    covenantWebView.merchantPreSignListener = null
                    continuation.resume(resultJson)
                }

                override fun onError(errorMessage: String) {
                    Log.e(TAG, "❌ Merchant pre-sign failed: $errorMessage")
                    covenantWebView.merchantPreSignListener = null
                    continuation.resumeWithException(Exception("Pre-sign failed: $errorMessage"))
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

            // Build parameters
            val covenantParams = buildCovenantParamsMap(simpleRequest)
            val params = org.json.JSONObject().apply {
                put("covenantParams", org.json.JSONObject(covenantParams))
                put("merchantWIF", merchantWIF)
                put("merchantPubkey", merchantPubkey)
                put("merchantAddress", merchantAddress)
                // F4: the funder's address is derived in JS from covenantParams.funderPubkey.
                // Pass empty (not a hardcoded test address) so a derivation failure fails loudly.
                put("funderAddress", "")
                // F3: require the params to reconstruct the exact covenant address we fetch UTXOs for.
                put("expectedCovenantAddress", simpleRequest.covenantAddress)
                put("oracleSig", org.json.JSONObject().apply {
                    put("signature", oracleSig)
                    put("message", oracleMessage)
                    put("price", (currentPrice * 100).toInt()) // Convert to cents
                })
                put("utxos", utxosJson)
            }

            val paramsJson = params.toString()
            val jsCode = "buildMerchantPreSignTransaction('${paramsJson.replace("'", "\\'")}')"

            Log.d(TAG, "Calling buildMerchantPreSignTransaction()...")

            // Call JavaScript
            covenantWebView.webView.post {
                covenantWebView.webView.evaluateJavascript(jsCode) { result ->
                    Log.d(TAG, "JavaScript call initiated: $result")
                }
            }

            // Handle cancellation
            continuation.invokeOnCancellation {
                covenantWebView.merchantPreSignListener = null
            }
        }

        // Step 3: Parse result
        val resultObj = org.json.JSONObject(result)
        val txHex = resultObj.getString("txHex")
        val merchantSig = resultObj.getString("merchantSig")
        val merchantAddressUsed = resultObj.getString("merchantAddress")
        val funderAddressUsed = resultObj.getString("funderAddress")

        Log.d(TAG, "✅ Transaction built!")
        Log.d(TAG, "  txHex length: ${txHex.length} chars")
        Log.d(TAG, "  merchantSig: ${merchantSig.substring(0, 20)}...")
        Log.d(TAG, "  merchantAddress: $merchantAddressUsed")
        Log.d(TAG, "  funderAddress: $funderAddressUsed")

        // Return txHex, merchantSig, merchantAddress, funderAddress
        return MerchantTxResult(txHex, merchantSig, merchantAddressUsed, funderAddressUsed)
    }

    /**
     * Build covenant params map for JavaScript
     */
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

    /**
     * Get merchant's BCH address for receiving payment
     */
    private suspend fun getMerchantAddress(): String {
        // Get from active wallet using WalletManager
        val walletManager = WalletManager.getInstance(this)
        val wallet = walletManager.getActiveWallet()
        val address = wallet?.address ?: ""

        Log.d(TAG, "Merchant address: $address")
        Log.d(TAG, "Address format: ${if (address.contains(":")) "CashAddr" else "Legacy/Other"}")

        // Ensure CashAddr format (must have prefix like bchtest: or bitcoincash:)
        if (address.isNotEmpty() && !address.contains(":")) {
            Log.e(TAG, "❌ Address is not in CashAddr format! Got: $address")
            throw Exception("Merchant address must be in CashAddr format (bchtest:q... or bitcoincash:q...)")
        }

        return address
    }

    /**
     * Show merchant's cosign QR for recipient to scan
     * Dense QR (~1200-2000 chars)
     */
    private fun showMerchantCosignQR(
        response: MerchantCosignResponse,
        originalRequest: SimpleCashoutRequest,
        qrBitmap: Bitmap
    ) {
        // Safety check: don't show dialog if activity is finishing or destroyed
        if (isFinishing || isDestroyed) {
            Log.w(TAG, "⚠️ Activity is finishing/destroyed, cannot show QR dialog")
            return
        }

        Log.d(TAG, "Showing merchant cosign QR dialog...")

        val message = response.toMessage()
        Log.d(TAG, "Message length: ${message.length} chars (dense QR!)")

        // Create dialog
        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            gravity = Gravity.CENTER

            // Title
            addView(TextView(this@TradeActivity).apply {
                text = "Merchant Offer"
                textSize = 20f
                setTypeface(null, android.graphics.Typeface.BOLD)
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#333333"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 8 }
            })

            // Details
            addView(TextView(this@TradeActivity).apply {
                text = "${response.currency ?: "EUR"} ${String.format("%.2f", response.amountCash)} → ${String.format("%.8f", response.amountBCH)} BCH"
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#666666"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 16 }
            })

            // QR Code
            addView(ImageView(this@TradeActivity).apply {
                setImageBitmap(qrBitmap)
                layoutParams = LinearLayout.LayoutParams(512, 512)
                scaleType = ImageView.ScaleType.FIT_CENTER
            })

            // Instructions
            addView(TextView(this@TradeActivity).apply {
                text = "Customer scans this QR to co-sign"
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#666666"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 16 }
            })
        }

        AlertDialog.Builder(this)
            .setTitle("Show to Customer")
            .setView(dialogView)
            .setPositiveButton("Done") { dialog, _ ->
                dialog.dismiss()
                // TODO: Wait for recipient's co-signed tx back
            }
            .setNeutralButton("Copy Text") { _, _ ->
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Merchant Cosign", message)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "📋 Copied!", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    /**
     * Generate QR code bitmap
     */
    private fun generateQRCode(text: String, errorCorrection: ErrorCorrectionLevel = ErrorCorrectionLevel.M): Bitmap {
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to errorCorrection,
            EncodeHintType.MARGIN to 1
        )

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512, hints)

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

    // ============================================================================
    // LEGACY FLOW (Backward Compatibility)
    // ============================================================================

    /**
     * Parse [CASH_IN_PERSON] message into CashoutRequest
     */
    private fun parseCashInPersonMessage(message: String): CashoutRequest? {
        try {
            // Check if message contains [CASH_IN_PERSON] markers
            if (!message.contains("[CASH_IN_PERSON]") || !message.contains("[/CASH_IN_PERSON]")) {
                Log.e(TAG, "Message doesn't contain [CASH_IN_PERSON] markers")
                return null
            }

            // Extract content between markers
            val startMarker = "[CASH_IN_PERSON]"
            val endMarker = "[/CASH_IN_PERSON]"
            val startIndex = message.indexOf(startMarker) + startMarker.length
            val endIndex = message.indexOf(endMarker)

            if (startIndex >= endIndex) {
                Log.e(TAG, "Invalid marker positions")
                return null
            }

            val content = message.substring(startIndex, endIndex).trim()
            Log.d(TAG, "Extracted content: ${content.take(200)}...")

            // Parse key-value pairs
            val params = mutableMapOf<String, String>()
            content.lines().forEach { line ->
                val trimmed = line.trim()
                if (trimmed.contains("=")) {
                    val (key, value) = trimmed.split("=", limit = 2)
                    params[key.trim()] = value.trim()
                }
            }

            Log.d(TAG, "Parsed ${params.size} parameters")

            // Extract recipientSig (supports both single field and split for backward compat)
            val recipientSig = params["recipientSig"] ?: run {
                // Backward compat: try split fields (recipientSig1 + recipientSig2)
                val sig1 = params["recipientSig1"] ?: ""
                val sig2 = params["recipientSig2"] ?: ""
                if (sig1.isNotEmpty()) {
                    Log.d(TAG, "Using split recipientSig (backward compat): sig1=${sig1.length} chars, sig2=${sig2.length} chars")
                    sig1 + sig2
                } else {
                    ""
                }
            }

            // Build CashoutRequest
            return CashoutRequest(
                covenantAddress = params["covenantAddress"] ?: "",
                senderPubkey = params["senderPubkey"] ?: "",
                recipientPubkey = params["recipientPubkey"] ?: "",
                funderPubkey = params["funderPubkey"] ?: "",
                oraclePubkey = params["oraclePubkey"] ?: "",
                eurCents = params["eurCents"]?.toIntOrNull() ?: 0,
                expiryOracleTime = params["expiryOracleTime"]?.toLongOrNull() ?: 0L,
                initialBchPriceInCents = params["initialBchPriceInCents"]?.toIntOrNull() ?: 0,
                minPricePercent = params["minPricePercent"]?.toIntOrNull() ?: 93,
                merchantPubkey = params["merchantPubkey"] ?: "MERCHANT_PROVIDES_AT_CLAIM_TIME",
                recipientSig = recipientSig,
                oracleSig = params["oracleSig"] ?: "",
                oracleMessage = params["oracleMessage"] ?: "",
                timestamp = System.currentTimeMillis()
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing message", e)
            return null
        }
    }

    private fun openVerificationScreen(request: CashoutRequest) {
        val intent = android.content.Intent(this, MerchantVerificationActivity::class.java).apply {
            putExtra(MerchantVerificationActivity.EXTRA_REQUEST_ID, request.id)
        }
        startActivity(intent)
    }

    /**
     * MERCHANT-FIRST FLOW: Handle fully-signed transaction from recipient
     * Extract transaction hex and broadcast to network
     */
    private fun handleSignedTransaction(message: String) {
        Log.d(TAG, "📡 Parsing signed transaction message...")

        try {
            // Structured (covenantAddress/quoteId/txHex) or legacy ([SIGNED_TX]\n<hex>)
            val txHex: String
            if (message.contains("txHex=")) {
                val params = mutableMapOf<String, String>()
                message.lines().forEach { line ->
                    val t = line.trim()
                    if (t.contains("=")) {
                        val (k, v) = t.split("=", limit = 2)
                        params[k.trim()] = v.trim()
                    }
                }
                txHex = params["txHex"] ?: ""
            } else {
                txHex = message.substringAfter("[SIGNED_TX]\n").trim()
            }

            if (txHex.isEmpty()) {
                throw IllegalArgumentException("Transaction hex is empty")
            }

            Log.d(TAG, "✅ Extracted transaction hex: ${txHex.length} chars")

            // Show confirmation dialog before broadcasting
            AlertDialog.Builder(this)
                .setTitle("Broadcast Transaction?")
                .setMessage(
                    "Ready to broadcast the signed transaction.\n\n" +
                    "Transaction size: ${txHex.length} chars\n\n" +
                    "This will complete the sale and send BCH to you (the merchant)."
                )
                .setPositiveButton("Broadcast Now") { _, _ ->
                    broadcastTransaction(txHex)
                }
                .setNegativeButton("Cancel") { _, _ ->
                    Toast.makeText(this, "Broadcast cancelled", Toast.LENGTH_SHORT).show()
                }
                .setNeutralButton("Copy Hex") { _, _ ->
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = android.content.ClipData.newPlainText("Transaction Hex", txHex)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(this, "📋 Transaction hex copied!", Toast.LENGTH_SHORT).show()
                }
                .show()

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to parse signed transaction", e)
            Toast.makeText(
                this,
                "Failed to parse transaction: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Broadcast transaction to BCH network via Kotlin native TCP
     * Uses CovenantBuildService (100% reliable, no WebSocket)
     */
    private fun broadcastTransaction(
        txHex: String, saleCash: Double? = null, currency: String? = null,
        recipientNpub: String? = null, covenantAddress: String? = null
    ) {
        Log.d(TAG, "📡 Broadcasting transaction via Kotlin native TCP...")

        lifecycleScope.launch {
            try {
                // Use Kotlin native TCP broadcast (100% reliable, no WebSocket like WebView)
                val txid = withContext(Dispatchers.IO) {
                    com.asgaya.husk.covenant.CovenantBuildService.broadcastTransaction(txHex)
                }

                Log.d(TAG, "✅ Transaction broadcasted! TXID: $txid")

                // Notify the recipient: the sale is broadcast → it is now safe to hand the cash
                // (`ui-rethink/41`). A DM the recipient's listener turns into a notification.
                if (!recipientNpub.isNullOrBlank() && !covenantAddress.isNullOrBlank()) {
                    withContext(Dispatchers.IO) {
                        runCatching {
                            val km = com.asgaya.husk.nostr.NostrKeyManager(this@TradeActivity)
                            val w = walletManager.getActiveWallet() ?: return@runCatching
                            val keys = km.ensureNostrKeys(w)
                            val msg = "[CASHOUT_BROADCAST]\ncovenantAddress=$covenantAddress\ntxid=$txid\n" +
                                "amount=${saleCash ?: ""}\ncurrency=${currency ?: ""}\n[/CASHOUT_BROADCAST]"
                            val gw = com.asgaya.husk.nostr.NIP17GiftWrap.wrapDM(
                                msg, keys.privateKeyHex, keys.publicKeyHex, recipientNpub
                            )
                            val client = com.asgaya.husk.nostr.NostrClient.getInstance(keys.publicKeyHex, null)
                            client.connect()
                            client.publishEvent(gw)
                            Log.i(TAG, "💸 [CASHOUT_BROADCAST] sent to recipient ${recipientNpub.take(12)}…")
                        }
                    }
                }

                // Success!
                showBroadcastSuccessDialog(txid, saleCash, currency)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Broadcast failed", e)
                withContext(Dispatchers.Main) {
                    AlertDialog.Builder(this@TradeActivity)
                        .setTitle("Broadcast Failed")
                        .setMessage("Failed to broadcast transaction:\n\n${e.message}")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }
    }

    /**
     * Show success dialog with transaction ID.
     * For a BCH sale, the merchant is told it is safe to hand the cash (BCH-first).
     */
    private fun showBroadcastSuccessDialog(txid: String, saleCash: Double? = null, currency: String? = null) {
        val message = if (saleCash != null) {
            // RS082: 0-conf is fine ≤ €200; above that we only ADVISE waiting for 1 confirmation.
            // It is the user's money — we warn, we do not block.
            val warn = if (com.asgaya.husk.covenant.CovenantRules.shouldWarnZeroConf(saleCash)) {
                "\n\n⚠️ Over €%.0f: this is a 0-confirmation broadcast. We advise waiting for 1 " +
                "confirmation before handing over the cash — it is your call.".format(
                    com.asgaya.husk.covenant.CovenantRules.ZERO_CONF_WARN_EUR
                )
            } else {
                ""
            }
            "The BCH sale has been broadcast to the network (0-conf).\n\n" +
            "TXID:\n$txid\n\n" +
            "✅ It is safe to hand %s %.2f cash to the customer now.".format(currency ?: "EUR", saleCash) + warn
        } else {
            "The cashout transaction has been successfully broadcasted to the BCH network.\n\n" +
            "TXID:\n$txid\n\n" +
            "The recipient will receive their BCH shortly."
        }
        AlertDialog.Builder(this)
            .setTitle("✅ Transaction Broadcasted!")
            .setMessage(message)
            .setPositiveButton("View on Explorer") { _, _ ->
                val explorerUrl = "https://chipnet.chaingraph.cash/tx/$txid"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(explorerUrl))
                startActivity(intent)
            }
            .setNegativeButton("Done") { _, _ ->
                finish()
            }
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
