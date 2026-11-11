package com.asgaya.husk

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.content.Context
import android.os.PowerManager
import android.net.Uri
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.asgaya.husk.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.util.Log
import android.widget.Toast
import java.text.NumberFormat
import java.util.Locale
import java.util.Currency
import com.asgaya.husk.wallet.WalletManager
import com.asgaya.husk.data.Wallet
import com.asgaya.husk.data.WalletRole
import com.asgaya.husk.transaction.TransactionStatus
import com.asgaya.husk.remittance.Remittance
import com.asgaya.husk.remittance.RemittanceStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: AppDatabase
    private lateinit var walletManager: WalletManager
    private lateinit var covenantWebView: CovenantWebView
    private val NOTIFICATION_PERMISSION_CODE = 100
    private val electrumClient = ElectrumClient()

    companion object {
        private const val TAG = "MainActivity"
        const val PREFS_NAME = "BizumParserPrefs"  // Match SettingsActivity and NotificationListener
        const val CURRENCY_KEY = "fiat_currency"  // EUR, VES, ARS, USD
        const val LOCALE_KEY = "user_locale"      // ES, VE, AR, US
        const val WALLET_SCRIPTHASH_KEY = "seller_scripthash" // Use same key as SettingsActivity
    }

    // Current balance (will be calculated from UTXOs via Electrum)
    private var currentBalanceBch: Double = 0.0
    private var isQueryingBalance: Boolean = false

    // Oracle BCH/EUR rate; null → unavailable, so fiat shows "—" (never a hardcoded price,
    // `ui-rethink/16` P0-2).
    @Volatile private var oracleEurRate: Double? = null
    /** EUR→selected display currency (reference) for the balance; `null` = not loaded (`supported-fiats/01`). */
    @Volatile private var localRate: Double? = null
    @Volatile private var localRateCurrency: String? = null

    // Pending covenant (from notification)
    private var pendingCovenant: PendingCovenant? = null

    data class PendingCovenant(
        val address: String,
        val amountBch: Double,
        val sender: String,
        val expiryTime: Long
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        walletManager = WalletManager.getInstance(this)
        com.asgaya.husk.nostr.NostrHealth.init(this)

        // Initialize singleton WebView for covenant operations
        covenantWebView = CovenantWebView.getInstance(this)
        // Add WebView to layout (invisible, 1x1 pixel) so JavaScript can execute
        // Remove from parent first if already attached (happens when MainActivity recreates)
        (covenantWebView.webView.parent as? android.view.ViewGroup)?.removeView(covenantWebView.webView)
        binding.root.addView(covenantWebView.webView, 1, 1)

        // Setup UI
        setupButtons()
        setupBottomNavigation()
        updateBalanceDisplay()
        applyModeVisibility()
        hidePendingCovenant()

        // Observe notifications for pending covenants
        observeNotifications()

        // Home "Active" chips (standard home, `ui-rethink/12`)
        observeActiveCovenants()

        // Check permissions and battery optimization
        checkPermissionsAndSetup()

        // Initialize wallet system
        initializeWallets()

        // Query balance from Electrum
        queryBalance()

        // Seed test contacts on first run
        seedTestContacts()

        // Handle "open_tab" intent from notification
        handleOpenTabIntent(intent)

        // Nostr reliability (handler-owner race, 2026-09-25):
        // 1) the service owns the handler -> make sure it's alive
        try {
            android.service.notification.NotificationListenerService.requestRebind(
                android.content.ComponentName(this, NotificationListener::class.java)
            )
        } catch (e: Exception) {
            Log.w(TAG, "requestRebind failed", e)
        }
        // 2) if events arrive with no handler, do NOT fail silently — surface it with the fallbacks
        com.asgaya.husk.nostr.NostrClient.onNoHandlerDetected = {
            runOnUiThread {
                Toast.makeText(
                    this,
                    "⚠️ Nostr isn't working — use the fallbacks (contacts / QR), or try another relay",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun handleOpenTabIntent(intent: Intent?) {
        val openTab = intent?.getStringExtra("open_tab")
        if (openTab == "trade") {
            Log.d(TAG, "📱 Opening Trade tab from notification")
            // Navigate to Trade tab
            val tradeIntent = Intent(this, com.asgaya.husk.trade.TradeActivity::class.java)
            startActivity(tradeIntent)
        }
    }

    private fun queryBalance() {
        if (isQueryingBalance) return
        isQueryingBalance = true

        lifecycleScope.launch {
            try {
                // Get active wallet
                val activeWallet = walletManager.getActiveWallet()

                if (activeWallet == null) {
                    Log.w(TAG, "No active wallet. Balance query skipped.")
                    currentBalanceBch = 0.0
                    updateBalanceDisplay()
                    isQueryingBalance = false
                    return@launch
                }

                val queriedWalletLabel = activeWallet.label
                val queriedAddress = activeWallet.address
                Log.d(TAG, "Querying balance for $queriedWalletLabel ($queriedAddress)...")

                // Query balance from Electrum using active wallet's address
                val balance = electrumClient.getBalance(queriedAddress)

                // 🛡 Stale-result guard: only apply if this wallet is STILL the active one.
                // A slow query for a wallet the user switched away from must not overwrite
                // the display (previously: last coroutine to finish won, showing the wrong
                // wallet's balance after tap/switch during an in-flight query).
                val stillActive = walletManager.getActiveWallet()?.address == queriedAddress
                if (stillActive) {
                    currentBalanceBch = balance
                    Log.d(TAG, "✅ Balance for $queriedWalletLabel: $currentBalanceBch BCH")
                    updateBalanceDisplay()
                } else {
                    Log.d(TAG, "⏭️ Stale balance result ignored (wallet changed during query): $queriedWalletLabel")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to query balance", e)
                Toast.makeText(
                    this@MainActivity,
                    "⚠️ Failed to query balance: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                isQueryingBalance = false
            }
        }
    }

    private fun showScriptHashInfo() {
        AlertDialog.Builder(this)
            .setTitle("Configure Wallet")
            .setMessage("No wallet configured yet.\n\nTo see your BCH balance:\n1. Generate a testnet address\n2. Get the scripthash for that address\n3. Add scripthash in Settings\n4. Send testnet BCH to your address\n5. Refresh to see balance")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            .setNegativeButton("Later", null)
            .show()
    }

    private fun setupButtons() {
        // Wallet selector (tap the wallet NAME to switch wallets — no role label, `ui-rethink/12`)
        binding.walletName.setOnClickListener {
            showWalletSelectorDialog()
        }

        // Balance card - tap to refresh balance
        binding.balanceCard.setOnClickListener {
            Toast.makeText(this, "🔄 Refreshing balance...", Toast.LENGTH_SHORT).show()
            queryBalance()
        }
        binding.balanceCardAdv.setOnClickListener {
            Toast.makeText(this, "🔄 Refreshing balance...", Toast.LENGTH_SHORT).show()
            queryBalance()
        }

        // Settings icon
        binding.settingsIcon.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Contacts button (test wallets)
        binding.contactsIcon.setOnClickListener {
            val intent = Intent(this, ContactsActivity::class.java)
            startActivity(intent)
        }

        // Send Money → the Send remittance flow (`ui-rethink/12`)
        binding.sendMoneyButton.setOnClickListener {
            val intent = Intent(this, com.asgaya.husk.remittance.SendRemittanceActivity::class.java)
            startActivity(intent)
        }

        // Receive Money button (placeholder)
        binding.receiveMoneyButton.setOnClickListener {
            showReceiveDialog()
        }

        // Claim button
        binding.claimButton.setOnClickListener {
            pendingCovenant?.let { covenant ->
                claimCovenant(covenant)
            }
        }

        // Cash out → the Market tab, scoped to this covenant (`ui-rethink/41`)
        binding.cashOutButton.setOnClickListener {
            val covenant = pendingCovenant ?: return@setOnClickListener
            val mainCurrency = getSharedPreferences("BizumParserPrefs", Context.MODE_PRIVATE)
                .getString("fiat_currency", "EUR") ?: "EUR"
            startActivity(
                Intent(this, com.asgaya.husk.trade.MarketActivity::class.java)
                    .putExtra(com.asgaya.husk.trade.MarketActivity.EXTRA_CASHOUT_COVENANT, covenant.address)
                    .putExtra(com.asgaya.husk.trade.MarketActivity.EXTRA_PRESELECT_ASSET, mainCurrency)
            )
        }

        // Activity / History — the Activity screen, contextual (`ui-rethink/41`)
        binding.activityHistoryButton.setOnClickListener {
            startActivity(Intent(this, com.asgaya.husk.remittance.RemittanceActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_wallet

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_wallet -> {
                    // Already here
                    true
                }
                R.id.nav_market -> {
                    // Market — the bulletin board as a first-class tab (`ui-rethink/41`)
                    startActivity(Intent(this, com.asgaya.husk.trade.MarketActivity::class.java))
                    true
                }
                R.id.nav_trade -> {
                    // Trade is the passive/merchant surface: require Advanced mode (`ui-rethink/08`).
                    if (!AdvancedMode.isEnabled(this)) {
                        showAdvancedRequiredDialog()
                        false
                    } else {
                        val intent = Intent(this, com.asgaya.husk.trade.TradeActivity::class.java)
                        startActivity(intent)
                        true
                    }
                }
                else -> false
            }
        }
    }

    /** The Trade tab needs Advanced mode; warn instead of hiding it. */
    private fun showAdvancedRequiredDialog() {
        AlertDialog.Builder(this)
            .setTitle("⚙️ Advanced mode required")
            .setMessage("The Trade tab is for merchants / passive use. Enable Advanced mode in Settings to open it.")
            .setPositiveButton("Open Settings") { _, _ ->
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            .setNegativeButton("Not now", null)
            .show()
    }

    private fun showReceiveDialog() {
        lifecycleScope.launch {
            val activeWallet = walletManager.getActiveWallet()

            if (activeWallet == null) {
                Toast.makeText(this@MainActivity, "No active wallet", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val address = activeWallet.address

            // Simple dialog showing address with copy button
            AlertDialog.Builder(this@MainActivity)
                .setTitle("📥 Receive BCH")
                .setMessage("Your address:\n\n$address\n\n(Tap Copy to copy address)")
                .setPositiveButton("Copy") { _, _ ->
                    // Copy to clipboard
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("BCH Address", address)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(this@MainActivity, "✅ Address copied!", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Close", null)
                .show()
        }
    }

    private fun claimCovenant(covenant: PendingCovenant) {
        Log.d(TAG, "Claiming covenant: ${covenant.address}")

        val intent = Intent(this, ClaimActivity::class.java).apply {
            putExtra(ClaimActivity.EXTRA_COVENANT_ADDRESS, covenant.address)
            putExtra(ClaimActivity.EXTRA_AMOUNT_BCH, covenant.amountBch)
            putExtra(ClaimActivity.EXTRA_SENDER, covenant.sender)
            putExtra(ClaimActivity.EXTRA_EXPIRY_TIME, covenant.expiryTime)
        }
        startActivity(intent)
    }

    private fun updateBalanceDisplay() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currency = prefs.getString(CURRENCY_KEY, "EUR") ?: "EUR"

        // Format BCH amount (8 decimals)
        val bch = String.format(Locale.US, "%.8f BCH", currentBalanceBch)

        // Fiat from the oracle only; "—" while unavailable (`ui-rethink/16` P0-2)
        val fiat = fiatFromOracle(currentBalanceBch, currency)

        // Standard (discreet line) + Advanced (hero) variants (`ui-rethink/12`)
        binding.balanceBch.text = bch
        binding.balanceFiat.text = fiat
        binding.balanceBchAdv.text = bch
        binding.balanceFiatAdv.text = fiat
    }

    /** Fiat line: BCH→EUR (signed oracle) → selected local currency (reference); "—" if unavailable. */
    private fun fiatFromOracle(amountBch: Double, currency: String): String {
        val eur = oracleEurRate ?: return "—"
        if (currency == "EUR") return formatFiatAmount(amountBch * eur, currency)
        val local = localRate?.takeIf { localRateCurrency == currency } ?: return "—"
        return formatFiatAmount(amountBch * eur * local, currency)
    }

    /** Refresh the oracle BCH/EUR rate and re-render the fiat lines (`ui-rethink/16` P0-2). */
    private fun refreshOracleRate() {
        lifecycleScope.launch {
            val rate = OraclePriceSource.eurPerBch()
            if (rate == null) {
                Log.w(TAG, "⚠️ Oracle price unavailable — fiat shown as —")
            } else {
                oracleEurRate = rate
            }
            // Display currency (`supported-fiats/01`): EUR (signed oracle) → local (reference).
            val currency = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(CURRENCY_KEY, "EUR") ?: "EUR"
            if (currency != "EUR") {
                localRate = FiatRateSource.eurPer(currency)
                localRateCurrency = currency
            } else {
                localRate = null
                localRateCurrency = null
            }
            updateBalanceDisplay()
            pendingCovenant?.let { showPendingCovenant(it) }
        }
    }

    /**
     * The STANDARD home is the remittance-first redesign (`ui-rethink/12`); the ADVANCED home
     * keeps the previous wallet-centric view. Toggle the two variants + the tab titles.
     */
    private fun applyModeVisibility() {
        // One Home + one set of tab titles for both modes (`ui-rethink/41`): the Advanced toggle only
        // gates the Trade tab (merchant surfaces), not the Home or the nav titles.
        binding.walletName.visibility = View.VISIBLE
        binding.balanceCard.visibility = View.VISIBLE
        binding.balanceCardAdv.visibility = View.GONE
        binding.historyCardAdv.visibility = View.GONE
    }

    private fun showPendingCovenant(covenant: PendingCovenant) {
        pendingCovenant = covenant

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currency = prefs.getString(CURRENCY_KEY, "EUR") ?: "EUR"

        // Show covenant card
        binding.pendingCovenantCard.visibility = View.VISIBLE
        binding.emptyStateText.visibility = View.GONE

        // Set sender
        binding.covenantSender.text = "From: ${covenant.sender}"

        // Set BCH amount
        binding.covenantAmountBch.text = String.format(Locale.US, "%.8f BCH", covenant.amountBch)

        // Set fiat amount (oracle only; "—" if unavailable)
        binding.covenantAmountFiat.text = fiatFromOracle(covenant.amountBch, currency)

        // Set expiry countdown (TODO: implement countdown timer)
        val expiryHours = (covenant.expiryTime - System.currentTimeMillis()) / (1000 * 60 * 60)
        val expiryMinutes = ((covenant.expiryTime - System.currentTimeMillis()) / (1000 * 60)) % 60
        binding.covenantExpiry.text = "⏰ Expires in ${expiryHours}h ${expiryMinutes}m"

        Log.d(TAG, "Showing pending covenant: ${covenant.address}")
    }

    private fun hidePendingCovenant() {
        pendingCovenant = null
        binding.pendingCovenantCard.visibility = View.GONE
        binding.emptyStateText.visibility = View.VISIBLE
    }

    private fun formatFiatAmount(amount: Double, currency: String): String {
        val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
        format.currency = Currency.getInstance(currency)
        return "≈ ${format.format(amount)}"
    }

    /**
     * Home "Active" chips (standard home): the live covenants of the active wallet, max 2,
     * each opening the Activity tab. (`ui-rethink/12`)
     */
    private var activeRemittances: List<Remittance> = emptyList()

    private fun observeActiveCovenants() {
        lifecycleScope.launch {
            database.remittanceDao().getAllRemittances().collectLatest { list ->
                activeRemittances = list.filter {
                    it.status == RemittanceStatus.AWAITING_FUNDING ||
                        it.status == RemittanceStatus.PENDING ||
                        it.status == RemittanceStatus.CONFIRMED
                }.take(2)
                renderActiveChips()
            }
        }
    }

    /** (Re)render the Active chips — also called on a tick so the claim countdown stays live. */
    private fun renderActiveChips() {
        binding.activeContainer.removeAllViews()
        activeRemittances.forEach { binding.activeContainer.addView(createActiveChip(it)) }

        // The Activity/History entry: "Activity" when something is live, else "History" (`ui-rethink/41`)
        binding.activityHistoryButton.text =
            if (activeRemittances.isEmpty()) "📜 History" else "📋 Activity (${activeRemittances.size})"

        // The shared empty state only when nothing is active and no pending card is shown
        if (binding.pendingCovenantCard.visibility != android.view.View.VISIBLE) {
            binding.emptyStateText.visibility =
                if (activeRemittances.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun fmtDur(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        return when {
            h > 0 -> "${h}h ${m}m"
            m > 0 -> "${m}m"
            else -> "${seconds}s"
        }
    }

    private fun createActiveChip(remittance: Remittance): android.view.View {
        // Funded = the money is in the covenant (broadcast/pending or confirmed). `awaiting_funding`
        // is the only NOT-funded state (`ui-rethink/12`).
        val funded = remittance.status == RemittanceStatus.PENDING ||
            remittance.status == RemittanceStatus.CONFIRMED
        val card = com.google.android.material.card.MaterialCardView(this).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(16, 8, 16, 16) }
            radius = 12f
            cardElevation = 4f
            setCardBackgroundColor(
                android.graphics.Color.parseColor(if (funded) "#F0FDF4" else "#FFF7ED")
            )
            isClickable = true
            setOnClickListener {
                startActivity(Intent(this@MainActivity, com.asgaya.husk.remittance.RemittanceActivity::class.java))
            }
        }

        val content = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val cur = localRateCurrency ?: "EUR"
        val warn = SupportedFiats.warningLevel(cur)

        // 1) "<sender> has sent you" (bold identity) — received; "to <name>" — sent (`ui-rethink/40`).
        val who = (if (remittance.isReceived) {
            remittance.senderCashAccount ?: remittance.senderPubkey?.take(12) ?: "Someone"
        } else {
            remittance.recipientName ?: "${remittance.recipientAddress.take(14)}…"
        }).trim()
        val head = if (remittance.isReceived) "$who has sent you" else "to $who"
        val hStart = if (remittance.isReceived) 0 else 3
        content.addView(android.widget.TextView(this).apply {
            text = android.text.SpannableStringBuilder(head).apply {
                setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD), hStart,
                    hStart + who.length, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            textSize = 16f
            setTextColor(android.graphics.Color.BLACK)
            setPadding(0, 0, 0, 8)
        })

        // 2) The amount in the user's currency — ORANGE when the rate source is dubious.
        val state = if (remittance.status == RemittanceStatus.AWAITING_FUNDING) "waiting for the seller to fund" else "funded"
        content.addView(android.widget.TextView(this).apply {
            text = "${FiatDisplay.amount(remittance.eurCents ?: 0, cur, localRate)} · $state"
            textSize = 20f
            setTextColor(android.graphics.Color.parseColor(
                when { warn != null -> "#EA580C"; funded -> "#16A34A"; else -> "#F59E0B" }
            ))
            setPadding(0, 0, 0, 8)
        })

        // 3) The claim window — "this offer expires in …" reads as urgent (`ui-rethink/40`).
        if (funded) {
            val secsLeft = (remittance.expiryOracleTime ?: 0L) - System.currentTimeMillis() / 1000
            val line = when {
                remittance.isReceived && secsLeft <= 0 -> "⚠️ this offer has expired"
                remittance.isReceived -> "⏳ this offer expires in ${fmtDur(secsLeft)}"
                secsLeft <= 0 -> "⏳ can no longer claim"
                else -> "⏳ has ${fmtDur(secsLeft)} to claim"
            }
            content.addView(android.widget.TextView(this).apply {
                text = line
                textSize = 14f
                setTextColor(android.graphics.Color.parseColor(
                    if (remittance.isReceived && secsLeft <= 0) "#B91C1C" else "#64748B"))
                setPadding(0, 0, 0, 8)
            })
        }

        // 4) The rate warning — ORANGE, when the currency's price source is dubious (`SupportedFiats`).
        if (warn != null) {
            content.addView(android.widget.TextView(this).apply {
                text = "$warn $cur rate warning — the real rate may differ"
                textSize = 13f
                setTextColor(android.graphics.Color.parseColor("#EA580C"))
                setPadding(0, 0, 0, 8)
            })
        }

        remittance.covenantAddress?.takeIf { it.isNotBlank() }?.let { addr ->
            content.addView(android.widget.TextView(this).apply {
                text = "Covenant: ${addr.take(14)}…${addr.takeLast(8)}"
                textSize = 12f
                setTextColor(android.graphics.Color.GRAY)
            })
        }

        card.addView(content)
        return card
    }

    private fun observeNotifications() {
        lifecycleScope.launch {
            database.notificationDao().getAllNotifications().collectLatest { notifications ->
                // Find most recent parsed covenant notification
                val latestCovenant = notifications
                    .filter { it.parsedSuccessfully && it.reference.startsWith("bchtest:") }
                    .maxByOrNull { it.timestamp }

                if (latestCovenant != null && pendingCovenant == null) {
                    val rate = OraclePriceSource.eurPerBch()
                    if (rate == null || rate <= 0.0) {
                        // Never convert with a hardcoded price (`ui-rethink/16` P0-2)
                        Log.w(TAG, "⚠️ Oracle price unavailable — cannot convert the notification to BCH")
                    } else {
                        // New covenant detected (the notification amount is in EUR)
                        val covenant = PendingCovenant(
                            address = latestCovenant.reference,
                            amountBch = latestCovenant.amount / rate,
                            sender = latestCovenant.sender,
                            expiryTime = latestCovenant.timestamp + (8 * 60 * 60 * 1000) // 8 hours from now
                        )
                        showPendingCovenant(covenant)
                    }
                }

                // Update transaction history (future)
                // adapter.submitList(notifications)
            }
        }
    }

    // Tick so the Active chips' claim countdown stays live (`ui-rethink/12`).
    private val chipTickHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val chipTick = object : Runnable {
        override fun run() {
            renderActiveChips()
            chipTickHandler.postDelayed(this, 30_000L)
        }
    }

    override fun onResume() {
        super.onResume()

        // Refresh balance from Electrum on every return to this screen
        // (recipient may have claimed/cashed out while in another activity).
        // queryBalance() updates the display and is guarded against duplicates.
        queryBalance()

        // Refresh the oracle BCH/EUR rate for the fiat lines (`ui-rethink/16` P0-2)
        refreshOracleRate()

        // Check permissions
        checkPermissionsAndSetup()

        // Rebroadcast pending transactions (RS083 pattern)
        lifecycleScope.launch {
            rebroadcastPendingTransactions()
        }

        // Surface a recent Nostr-transport failure instead of letting it drop silently
        // (`ui-rethink/16` P0-3).
        com.asgaya.husk.nostr.NostrHealth.last(this)?.let { (msg, at) ->
            if (System.currentTimeMillis() - at < 10 * 60 * 1000) {
                Toast.makeText(this, "⚠️ Nostr problem: $msg", Toast.LENGTH_LONG).show()
            }
        }

        // Keep the Active-chips claim countdown live
        chipTickHandler.postDelayed(chipTick, 30_000L)
    }

    override fun onPause() {
        super.onPause()
        chipTickHandler.removeCallbacks(chipTick)
    }

    private fun checkPermissionsAndSetup() {
        // Check notification listener permission
        if (!isNotificationListenerEnabled()) {
            showNotificationPermissionDialog()
        }

        // Check battery optimization
        if (!isBatteryOptimizationDisabled()) {
            requestBatteryOptimizationExemption()
        }

        // Check notification permission (Android 13+)
        checkAndRequestNotificationPermission()
    }

    private fun showNotificationPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Enable Notifications")
            .setMessage("Asgaya needs notification access to detect incoming covenant notifications.\n\nPlease enable notification access in the next screen.")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Later", null)
            .show()
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val packageName = packageName
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(packageName)
    }

    private fun isBatteryOptimizationDisabled(): Boolean {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }

    private fun requestBatteryOptimizationExemption() {
        AlertDialog.Builder(this)
            .setTitle("Battery Optimization")
            .setMessage("Asgaya needs to stay active to monitor covenant notifications.\n\nPlease disable battery optimization to ensure reliable operation.")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
            .setNegativeButton("Later", null)
            .show()
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }

    private fun seedTestContacts() {
        lifecycleScope.launch {
            try {
                TestContacts.seedContactsIfNeeded(database.contactDao())
                Log.d(TAG, "✅ Test contacts seeded")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to seed test contacts", e)
            }
        }
    }

    // ========== Wallet Management ==========

    /**
     * Initialize wallet system
     * - Seed test wallets on first launch
     * - Observe active wallet changes
     * - Update UI with active wallet info
     */
    private fun initializeWallets() {
        lifecycleScope.launch {
            try {
                // Seed test wallets if needed
                walletManager.seedTestWallets()

                // Observe active wallet changes
                lifecycleScope.launch {
                    walletManager.getActiveWalletFlow().collectLatest { wallet ->
                        wallet?.let {
                            Log.d(TAG, "Active wallet changed: ${it.label}")
                            updateWalletDisplay(it)
                        }
                    }
                }

                // Initial wallet display
                val activeWallet = walletManager.getActiveWallet()
                activeWallet?.let { updateWalletDisplay(it) }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize wallets", e)
                Toast.makeText(
                    this@MainActivity,
                    "⚠️ Failed to initialize wallets: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Update UI to show active wallet info
     */
    private fun updateWalletDisplay(wallet: Wallet) {
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "📱 Updating wallet display: ${wallet.label}")
        Log.d(TAG, "   Role: ${wallet.role}")
        Log.d(TAG, "   Address: ${wallet.address}")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        // Wallet NAME only (standard home) + the previous role label (advanced home)
        binding.walletName.text = "💼 ${wallet.label} ▼"
        val roleIcon = when (wallet.role) {
            WalletRole.SENDER -> "📤"
            WalletRole.RECIPIENT -> "📥"
            WalletRole.SELLER -> "💰"
            WalletRole.MERCHANT -> "🏪"
            WalletRole.ORACLE -> "🔮"
            WalletRole.CUSTOM -> "💼"
        }
        binding.walletRoleLabel.text = "$roleIcon ${wallet.label} ▼"

        // Update address display (truncated)
        val truncatedAddress = "${wallet.address.take(20)}..."
        Log.d(TAG, "Active wallet: $truncatedAddress")

        // Refresh balance for this wallet
        Log.d(TAG, "🔄 Triggering balance query for ${wallet.label}...")
        queryBalance()

        // Role-aware first-run default for the advanced surface (`ui-rethink/08`)
        if (!AdvancedMode.isSet(this)) AdvancedMode.setEnabled(this, AdvancedMode.defaultFor(wallet.role))
    }

    /**
     * Show wallet selector dialog
     */
    private fun showWalletSelectorDialog() {
        lifecycleScope.launch {
            try {
                val wallets = walletManager.getAllWallets()
                val activeWallet = walletManager.getActiveWallet()

                if (wallets.isEmpty()) {
                    Toast.makeText(
                        this@MainActivity,
                        "No wallets available",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                // Build wallet list items
                val items = wallets.map { wallet ->
                    val checkmark = if (wallet.isActive) "✓ " else "  "
                    val roleIcon = when (wallet.role) {
                        WalletRole.SENDER -> "📤"
                        WalletRole.RECIPIENT -> "📥"
                        WalletRole.SELLER -> "💰"
                        WalletRole.MERCHANT -> "🏪"
                        WalletRole.ORACLE -> "🔮"
                        WalletRole.CUSTOM -> "💼"
                    }
                    "$checkmark$roleIcon ${wallet.label}\n      ${wallet.address.take(25)}..."
                }.toTypedArray()

                // Show dialog
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Select Wallet")
                    .setItems(items) { dialog, which ->
                        val selectedWallet = wallets[which]
                        if (!selectedWallet.isActive) {
                            switchWallet(selectedWallet)
                        }
                        dialog.dismiss()
                    }
                    .setNeutralButton("+ Add Wallet") { _, _ ->
                        showAddWalletDialog()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()

            } catch (e: Exception) {
                Log.e(TAG, "Failed to show wallet selector", e)
                Toast.makeText(
                    this@MainActivity,
                    "⚠️ Failed to load wallets: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Switch to a different wallet
     */
    private fun switchWallet(wallet: Wallet) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "╔═══════════════════════════════════╗")
                Log.d(TAG, "║ SWITCHING WALLET                  ║")
                Log.d(TAG, "║ To: ${wallet.label.padEnd(28)}║")
                Log.d(TAG, "╚═══════════════════════════════════╝")

                walletManager.switchWallet(wallet.id)

                Toast.makeText(
                    this@MainActivity,
                    "Switched to ${wallet.label}",
                    Toast.LENGTH_SHORT
                ).show()

                Log.d(TAG, "✅ Wallet switch initiated. Flow will trigger update...")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to switch wallet", e)
                Toast.makeText(
                    this@MainActivity,
                    "⚠️ Failed to switch wallet: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Show Add Wallet dialog
     * Allows user to import a wallet from WIF
     */
    private fun showAddWalletDialog() {
        val dialogView = layoutInflater.inflate(android.R.layout.select_dialog_item, null)

        // Create input fields programmatically
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(60, 40, 60, 20)
        }

        // Label input
        val labelInput = android.widget.EditText(this).apply {
            hint = "Wallet Name (e.g., Funder Wallet)"
            textSize = 16f
        }
        layout.addView(labelInput)

        // Role selector
        val roleLabel = android.widget.TextView(this).apply {
            text = "Role:"
            textSize = 14f
            setPadding(0, 24, 0, 8)
        }
        layout.addView(roleLabel)

        val roleSpinner = android.widget.Spinner(this).apply {
            val roles = arrayOf("SENDER", "RECIPIENT", "SELLER", "MERCHANT", "ORACLE", "CUSTOM")
            adapter = android.widget.ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_item,
                roles
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            // Default to SELLER (index 2)
            setSelection(2)
        }
        layout.addView(roleSpinner)

        // Address input
        val addressLabel = android.widget.TextView(this).apply {
            text = "BCH Address:"
            textSize = 14f
            setPadding(0, 24, 0, 8)
        }
        layout.addView(addressLabel)

        val addressInput = android.widget.EditText(this).apply {
            hint = "bchtest:q..."
            textSize = 14f
        }
        layout.addView(addressInput)

        // WIF input
        val wifLabel = android.widget.TextView(this).apply {
            text = "Private Key (WIF):"
            textSize = 14f
            setPadding(0, 16, 0, 8)
        }
        layout.addView(wifLabel)

        val wifInput = android.widget.EditText(this).apply {
            hint = "cS..."
            textSize = 14f
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        layout.addView(wifInput)

        // Show WIF checkbox
        val showWifCheckbox = android.widget.CheckBox(this).apply {
            text = "Show WIF characters"
            textSize = 12f
            setPadding(0, 8, 0, 0)
            setOnCheckedChangeListener { _, isChecked ->
                wifInput.inputType = if (isChecked) {
                    android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                } else {
                    android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                }
                // Move cursor to end after changing input type
                wifInput.setSelection(wifInput.text.length)
            }
        }
        layout.addView(showWifCheckbox)

        // Info text
        val infoText = android.widget.TextView(this).apply {
            text = "Public key will be derived from WIF. Get address from: bitcoin-cli -testnet -rpcwallet=<name> listreceivedbyaddress 0 true"
            textSize = 12f
            setPadding(0, 16, 0, 0)
            setTextColor(0xFF666666.toInt())
        }
        layout.addView(infoText)

        // Show dialog
        AlertDialog.Builder(this)
            .setTitle("Add Wallet")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val label = labelInput.text.toString().trim()
                val roleStr = roleSpinner.selectedItem.toString()
                val address = addressInput.text.toString().trim()
                val wif = wifInput.text.toString().trim()

                if (label.isEmpty()) {
                    Toast.makeText(this, "⚠️ Please enter wallet name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (address.isEmpty()) {
                    Toast.makeText(this, "⚠️ Please enter address", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (wif.isEmpty()) {
                    Toast.makeText(this, "⚠️ Please enter WIF", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Import wallet
                importWalletFromWIF(label, roleStr, address, wif)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Import wallet from WIF and address
     * Derives public key from WIF, uses provided address
     */
    private fun importWalletFromWIF(label: String, roleStr: String, address: String, wif: String) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.d(TAG, "📥 IMPORTING WALLET")
                Log.d(TAG, "  Label: $label")
                Log.d(TAG, "  Role: $roleStr")
                Log.d(TAG, "  Address: $address")
                Log.d(TAG, "  WIF: ${wif.take(10)}...${wif.takeLast(10)}")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                Toast.makeText(this@MainActivity, "Deriving public key from WIF...", Toast.LENGTH_SHORT).show()

                // Derive public key from WIF using WebView
                val publicKey = covenantWebView.derivePubKeyFromWIF(wif)

                Log.d(TAG, "✅ Derived public key: $publicKey")

                // Parse role
                val role = WalletRole.valueOf(roleStr)

                // Import wallet
                val wallet = walletManager.importWallet(
                    label = label,
                    role = role,
                    wif = wif,
                    address = address,
                    publicKey = publicKey
                )

                Toast.makeText(
                    this@MainActivity,
                    "✅ Wallet added: ${wallet.label}",
                    Toast.LENGTH_LONG
                ).show()

                Log.d(TAG, "✅ Wallet imported successfully: ${wallet.id}")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to import wallet", e)
                Log.e(TAG, "  Error: ${e.message}")
                Log.e(TAG, "  Stack trace:", e)
                Toast.makeText(
                    this@MainActivity,
                    "❌ ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Rebroadcast pending transactions (RS083 pattern from Selene wallet)
     *
     * Called on app resume to retry any transactions that were broadcast
     * but haven't been confirmed yet. This handles cases where:
     * - App was killed mid-transaction
     * - Network connection failed during broadcast
     * - Transaction didn't propagate to mempool
     *
     * Based on Selene wallet's rebroadcastUnresolved() pattern.
     */
    private suspend fun rebroadcastPendingTransactions() {
        try {
            // Get all transactions with status=BROADCAST (not yet in mempool)
            val pendingTxs = database.pendingTransactionDao()
                .getByStatus(TransactionStatus.BROADCAST)

            if (pendingTxs.isEmpty()) {
                Log.d(TAG, "No pending transactions to rebroadcast")
                return
            }

            Log.i(TAG, "🔄 Rebroadcasting ${pendingTxs.size} pending transaction(s)...")

            pendingTxs.forEach { tx ->
                try {
                    // Skip if already retried recently (within last 5 minutes)
                    val lastRetry = tx.last_retry_at ?: 0
                    val fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000)
                    if (lastRetry > fiveMinutesAgo) {
                        Log.d(TAG, "  Skipping ${tx.txid} (retried ${(System.currentTimeMillis() - lastRetry) / 1000}s ago)")
                        return@forEach
                    }

                    Log.d(TAG, "  Checking transaction: ${tx.txid}")

                    // Query Electrum for transaction status (RS083 pattern). A read failure must
                    // NOT be read as "not found" (which would rebroadcast) nor abort the sweep —
                    // just skip this round (`ui-rethink/16` P0-3).
                    val txStatus = try {
                        electrumClient.getTransaction(tx.txid)
                    } catch (e: Exception) {
                        Log.w(TAG, "  Transaction lookup failed for ${tx.txid} - will retry", e)
                        return@forEach
                    }

                    if (txStatus != null) {
                        // Transaction found!
                        if (txStatus.confirmations > 0) {
                            // Confirmed in blockchain
                            database.pendingTransactionDao().updateConfirmation(
                                tx.txid,
                                TransactionStatus.CONFIRMED,
                                System.currentTimeMillis(),
                                txStatus.blockHeight,
                                txStatus.confirmations
                            )
                            Log.i(TAG, "  ✅ Transaction confirmed: ${tx.txid} (${txStatus.confirmations} confirmations)")

                            // Update linked remittance status
                            tx.remittance_id?.let { remittanceId ->
                                val remittance = database.remittanceDao().getRemittanceById(remittanceId)
                                if (remittance != null && remittance.status == RemittanceStatus.PENDING) {
                                    database.remittanceDao().updateRemittance(
                                        remittance.copy(status = RemittanceStatus.CONFIRMED)
                                    )
                                    Log.i(TAG, "  📝 Updated remittance $remittanceId to CONFIRMED")
                                }
                            }
                        } else if (txStatus.inMempool) {
                            // In mempool but not confirmed
                            database.pendingTransactionDao().updateStatus(
                                tx.txid,
                                TransactionStatus.MEMPOOL
                            )
                            Log.i(TAG, "  ✅ Transaction in mempool: ${tx.txid}")
                        }

                        // Record successful check
                        database.pendingTransactionDao().recordRetry(
                            tx.txid,
                            "Found: ${if (txStatus.confirmations > 0) "confirmed" else "mempool"}",
                            System.currentTimeMillis()
                        )
                    } else {
                        // Transaction not found - rebroadcast if we have the hex
                        if (tx.tx_hex.isNotEmpty()) {
                            try {
                                electrumClient.broadcast(tx.tx_hex)
                                Log.i(TAG, "  🔄 Rebroadcast: ${tx.txid}")
                                database.pendingTransactionDao().recordRetry(
                                    tx.txid,
                                    "Rebroadcast attempted",
                                    System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                Log.w(TAG, "  ⚠️ Rebroadcast failed: ${e.message}")
                                database.pendingTransactionDao().recordRetry(
                                    tx.txid,
                                    "Rebroadcast failed: ${e.message}",
                                    System.currentTimeMillis()
                                )
                            }
                        } else {
                            Log.w(TAG, "  ⚠️ Transaction not found and no hex to rebroadcast: ${tx.txid}")
                            database.pendingTransactionDao().recordRetry(
                                tx.txid,
                                "Not found, no hex available",
                                System.currentTimeMillis()
                            )
                        }
                    }

                } catch (e: Exception) {
                    Log.w(TAG, "  ⚠️ Failed to check/rebroadcast ${tx.txid}", e)
                    database.pendingTransactionDao().recordRetry(
                        tx.txid,
                        "Error: ${e.message}",
                        System.currentTimeMillis()
                    )
                }
            }

            Log.i(TAG, "✅ Rebroadcast check complete")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Rebroadcast failed", e)
            // Don't fail app startup - just log and continue
        }
    }
}