package com.asgaya.husk

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.asgaya.husk.data.Wallet
import com.asgaya.husk.data.WalletRole
import com.asgaya.husk.databinding.ActivitySettingsBinding
import com.asgaya.husk.wallet.WalletManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var walletManager: WalletManager
    private lateinit var covenantWebView: CovenantWebView

    companion object {
        private const val TAG = "SettingsActivity"
        const val PREFS_NAME = "BizumParserPrefs"  // Match NotificationListener
        const val CURRENCY_KEY = "fiat_currency"
        const val SELECTED_BANK_KEY = "selected_bank"
        const val SELLER_WIF_KEY = "seller_wif"
        const val SELLER_SCRIPTHASH_KEY = "seller_scripthash"
        const val SELLER_CHANGE_ADDRESS_KEY = "seller_change_address"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        walletManager = WalletManager.getInstance(this)
        covenantWebView = CovenantWebView.getInstance(this)

        setupToolbar()
        setupCurrencySelector()
        setupBankSelector()
        setupIdentity()
        setupWallets()
        setupAdvancedMode()
        setupProtection()
        loadCurrentSettings()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"
    }

    // ========== Identity & payments (`ui-rethink/03` group 1) ==========

    private fun setupIdentity() {
        val id = IdentityPrefs.load(this)
        binding.cashAccountInput.setText(id.cashAccount)
        binding.locationInput.setText(id.location)
        binding.methodCashCheck.isChecked = id.methods.contains(IdentityPrefs.METHOD_CASH)
        binding.methodCardCheck.isChecked = id.methods.contains(IdentityPrefs.METHOD_CARD)
        binding.methodBizumCheck.isChecked = id.methods.contains(IdentityPrefs.METHOD_BIZUM)
        binding.methodSepaCheck.isChecked = id.methods.contains(IdentityPrefs.METHOD_SEPA)
    }

    // ========== Wallets (role is a property of the wallet, not a setting) ==========

    private fun setupWallets() {
        binding.importWalletButton.setOnClickListener { showImportDialog() }
        binding.backupWalletButton.setOnClickListener { showBackupDialog() }

        lifecycleScope.launch {
            walletManager.getAllWalletsFlow().collect { wallets -> renderWallets(wallets) }
        }
    }

    private fun renderWallets(wallets: List<Wallet>) {
        binding.walletListContainer.removeAllViews()
        if (wallets.isEmpty()) {
            binding.walletListContainer.addView(TextView(this).apply {
                text = "No wallets yet — import one below."
                textSize = 14f
                setTextColor(android.graphics.Color.GRAY)
            })
            return
        }
        wallets.forEach { wallet ->
            binding.walletListContainer.addView(TextView(this).apply {
                text = "${if (wallet.isActive) "✓ " else "   "}${roleIcon(wallet.role)} ${wallet.label}\n      ${wallet.address}"
                textSize = 14f
                setPadding(0, 8, 0, 8)
                setTextColor(
                    if (wallet.isActive) android.graphics.Color.parseColor("#16A34A")
                    else android.graphics.Color.DKGRAY
                )
                isClickable = true
                setOnClickListener { activateWallet(wallet) }
            })
        }
    }

    private fun roleIcon(role: WalletRole): String = when (role) {
        WalletRole.SENDER -> "💼"
        WalletRole.RECIPIENT -> "👤"
        WalletRole.SELLER -> "🏦"
        WalletRole.MERCHANT -> "🏪"
        else -> "🔑"
    }

    private fun activateWallet(wallet: Wallet) {
        if (wallet.isActive) return
        lifecycleScope.launch {
            try {
                walletManager.switchWallet(wallet.id)
                Toast.makeText(this@SettingsActivity, "Active: ${wallet.label}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "❌ ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showImportDialog() {
        val roles = arrayOf("Sender", "Recipient", "Seller", "Merchant")
        val wifInput = EditText(this).apply { hint = "WIF (cT… / cN… / L…)" }
        val nameInput = EditText(this).apply { hint = "Wallet name" }
        val roleSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@SettingsActivity, android.R.layout.simple_spinner_dropdown_item, roles)
        }
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 0)
            addView(wifInput)
            addView(nameInput)
            addView(roleSpinner)
        }
        AlertDialog.Builder(this)
            .setTitle("📥 Import a wallet")
            .setView(container)
            .setPositiveButton("Import") { _, _ ->
                val wif = wifInput.text.toString().trim()
                val name = nameInput.text.toString().trim().ifEmpty { "Imported" }
                val role = when (roleSpinner.selectedItem.toString()) {
                    "Sender" -> WalletRole.SENDER
                    "Recipient" -> WalletRole.RECIPIENT
                    "Seller" -> WalletRole.SELLER
                    else -> WalletRole.MERCHANT
                }
                importWallet(wif, name, role)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun importWallet(wif: String, name: String, role: WalletRole) {
        if (wif.isBlank()) {
            Toast.makeText(this, "⚠️ Enter a WIF", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            try {
                // The WIF → address/pubkey derivation uses the WebView bridge (libauth), like the
                // covenant flows. Ensure it is ready first.
                if (!covenantWebView.isReady()) {
                    Toast.makeText(this@SettingsActivity, "Preparing…", Toast.LENGTH_SHORT).show()
                    covenantWebView.awaitReady(timeoutMs = 30_000)
                }
                val (address, publicKey) = withContext(Dispatchers.IO) { covenantWebView.deriveFromWIF(wif) }
                val wallet = withContext(Dispatchers.IO) {
                    walletManager.importWallet(
                        label = name, role = role, wif = wif, address = address, publicKey = publicKey
                    )
                }
                Toast.makeText(this@SettingsActivity, "✅ Imported: ${wallet.label}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e(TAG, "Import failed", e)
                Toast.makeText(this@SettingsActivity, "❌ Import failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showBackupDialog() {
        lifecycleScope.launch {
            val wallet = withContext(Dispatchers.IO) { walletManager.getActiveWallet() }
            if (wallet == null) {
                Toast.makeText(this@SettingsActivity, "No active wallet", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val wif = withContext(Dispatchers.IO) { runCatching { walletManager.getWIF(wallet) }.getOrNull() }
            if (wif.isNullOrBlank()) {
                Toast.makeText(this@SettingsActivity, "No backup available for this wallet", Toast.LENGTH_LONG).show()
                return@launch
            }

            val field = EditText(this@SettingsActivity).apply {
                setText(wif)
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                setSelection(0)
            }
            val container = LinearLayout(this@SettingsActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(48, 16, 48, 0)
                addView(TextView(this@SettingsActivity).apply {
                    text = "${wallet.label}\n${wallet.address}\n\n" +
                        "⚠️ Your key is like your card's PIN: if you lose it, you lose access; " +
                        "if someone finds it, they can use it. And here there is no bank to reset it — back it up."
                    textSize = 12f
                })
                addView(field)
            }
            AlertDialog.Builder(this@SettingsActivity)
                .setTitle("🔑 Back up: ${wallet.label}")
                .setView(container)
                .setPositiveButton("📋 Copy") { _, _ ->
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("BCH WIF", wif))
                    Toast.makeText(this@SettingsActivity, "📋 Copied — keep it safe offline", Toast.LENGTH_LONG).show()
                }
                .setNeutralButton("👁 Reveal") { _, _ ->
                    field.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                }
                .setNegativeButton("Close", null)
                .show()
        }
    }

    private fun setupProtection() {
        val modes = arrayOf("Automatic (recommended)", "Manual", "Always BCH")
        val adapter = ArrayAdapter(this, R.layout.spinner_item, modes)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.autoRefundSpinner.adapter = adapter
        binding.autoRefundSpinner.setSelection(AutoRefundPrefs.load(this).ordinal)
    }

    private fun setupAdvancedMode() {
        binding.advancedModeSwitch.isChecked = AdvancedMode.isEnabled(this)
        binding.advancedModeSwitch.setOnCheckedChangeListener { _, checked ->
            AdvancedMode.setEnabled(this, checked)
        }
    }

    // ========== Legacy dev settings ==========

    private fun setupCurrencySelector() {
        // Display currencies from the registry (`supported-fiats/01`) — this sets the wallet-balance currency.
        val labels = SupportedFiats.ALL.map { SupportedFiats.label(it.code) }
        val adapter = ArrayAdapter(this, R.layout.spinner_item, labels)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.currencySpinner.adapter = adapter
    }

    private fun setupBankSelector() {
        val banks = arrayOf(
            "Telegram (Covenant)",
            "Caja Rural",
            "BBVA",
            "Santander",
            "Sabadell",
            "Sabadell (Office Locator)",
            "ING"
        )
        val adapter = ArrayAdapter(this, R.layout.spinner_item, banks)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.bankSpinner.adapter = adapter
    }

    private fun loadCurrentSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val currency = prefs.getString(CURRENCY_KEY, "EUR") ?: "EUR"
        binding.currencySpinner.setSelection(SupportedFiats.codes.indexOf(currency).coerceAtLeast(0))

        val selectedBank = prefs.getString(SELECTED_BANK_KEY, "Telegram (Covenant)") ?: "Telegram (Covenant)"
        val banks = arrayOf(
            "Telegram (Covenant)",
            "Caja Rural",
            "BBVA",
            "Santander",
            "Sabadell",
            "Sabadell (Office Locator)",
            "ING"
        )
        binding.bankSpinner.setSelection((banks.indexOf(selectedBank)).takeIf { it >= 0 } ?: 0)

        binding.scriptHashInput.setText(prefs.getString(SELLER_SCRIPTHASH_KEY, "") ?: "")
        binding.wifInput.setText(prefs.getString(SELLER_WIF_KEY, "") ?: "")
        binding.changeAddressInput.setText(prefs.getString(SELLER_CHANGE_ADDRESS_KEY, "") ?: "")
    }

    override fun onPause() {
        super.onPause()
        saveSettings()
    }

    private fun saveSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(CURRENCY_KEY, SupportedFiats.codes.getOrElse(binding.currencySpinner.selectedItemPosition) { "EUR" })
            .putString(SELECTED_BANK_KEY, binding.bankSpinner.selectedItem.toString())
            .putString(SELLER_SCRIPTHASH_KEY, binding.scriptHashInput.text.toString().trim())
            .putString(SELLER_WIF_KEY, binding.wifInput.text.toString().trim())
            .putString(SELLER_CHANGE_ADDRESS_KEY, binding.changeAddressInput.text.toString().trim())
            .apply()

        val methods = mutableSetOf<String>()
        if (binding.methodCashCheck.isChecked) methods += IdentityPrefs.METHOD_CASH
        if (binding.methodCardCheck.isChecked) methods += IdentityPrefs.METHOD_CARD
        if (binding.methodBizumCheck.isChecked) methods += IdentityPrefs.METHOD_BIZUM
        if (binding.methodSepaCheck.isChecked) methods += IdentityPrefs.METHOD_SEPA
        IdentityPrefs.save(this, IdentityPrefs.Identity(
            cashAccount = binding.cashAccountInput.text.toString().trim(),
            location = binding.locationInput.text.toString().trim(),
            methods = methods
        ))

        AutoRefundPrefs.save(this, AutoRefundPrefs.Mode.values()[binding.autoRefundSpinner.selectedItemPosition])
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
