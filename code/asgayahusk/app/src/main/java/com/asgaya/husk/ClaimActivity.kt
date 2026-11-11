package com.asgaya.husk

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.asgaya.husk.databinding.ActivityClaimBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

class ClaimActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClaimBinding
    private lateinit var database: AppDatabase

    companion object {
        private const val TAG = "ClaimActivity"
        const val PREFS_NAME = "AsgayaPrefs"
        const val CURRENCY_KEY = "fiat_currency"
        const val EXTRA_COVENANT_ADDRESS = "covenant_address"
        const val EXTRA_AMOUNT_BCH = "amount_bch"
        const val EXTRA_SENDER = "sender"
        const val EXTRA_EXPIRY_TIME = "expiry_time"
    }

    private var covenantAddress: String = ""
    private var amountBch: Double = 0.0
    private var sender: String = ""
    private var expiryTime: Long = 0L

    // Oracle BCH/EUR rate; null → unavailable ("—", never a hardcoded price, `ui-rethink/16` P0-2)
    @Volatile private var oracleEurRate: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClaimBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)

        // Get covenant details from intent
        covenantAddress = intent.getStringExtra(EXTRA_COVENANT_ADDRESS) ?: ""
        amountBch = intent.getDoubleExtra(EXTRA_AMOUNT_BCH, 0.0)
        sender = intent.getStringExtra(EXTRA_SENDER) ?: ""
        expiryTime = intent.getLongExtra(EXTRA_EXPIRY_TIME, 0L)

        // Query database for full covenant (to get redeemScript)
        lifecycleScope.launch {
            try {
                val covenant = database.covenantDao().getAll().find { it.covenantAddress == covenantAddress }
                if (covenant != null) {
                    Log.d(TAG, "✅ Found covenant in database")
                    Log.d(TAG, "   Address: ${covenant.covenantAddress}")
                    Log.d(TAG, "   EUR cents: ${covenant.eurCents}")
                    Log.d(TAG, "   Expiry time: ${covenant.expiryTime} (${covenant.expiryTime / 1000} seconds)")
                    Log.d(TAG, "   Initial price: ${covenant.initialBchPrice}")
                    Log.d(TAG, "   Min price %: ${covenant.minPricePercent}")
                    Log.d(TAG, "   RedeemScript: ${if (covenant.redeemScript != null) "PRESENT (${covenant.redeemScript!!.length} chars)" else "NULL ❌"}")
                    Log.d(TAG, "")
                    Log.d(TAG, "🚀 Ready to claim!")
                    Log.d(TAG, "   Expiry timestamp for Pi-chan: ${covenant.expiryTime / 1000}")
                    if (covenant.redeemScript != null) {
                        Log.d(TAG, "")
                        Log.d(TAG, "📝 REDEEMSCRIPT HEX (for Pi service):")
                        Log.d(TAG, covenant.redeemScript)
                    }
                    Log.d(TAG, "")
                    // Write redeemScript to file
                    if (covenant.redeemScript != null) {
                        try {
                            val file = java.io.File(filesDir, "redeem_script.txt")
                            file.writeText(covenant.redeemScript)
                            Log.d(TAG, "✅ RedeemScript saved to: ${file.absolutePath}")
                            Log.d(TAG, "   Length: ${covenant.redeemScript.length} chars")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to write redeemScript", e)
                        }
                    }
                } else {
                    Log.e(TAG, "❌ Covenant not found in database!")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to query covenant", e)
            }
        }

        setupToolbar()
        displayCovenantDetails()
        setupClaimButton()
        refreshOracleRate()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Claim BCH"
    }

    private fun displayCovenantDetails() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currency = prefs.getString(CURRENCY_KEY, "EUR") ?: "EUR"

        // Display sender
        binding.senderText.text = sender

        // Display BCH amount
        binding.amountBch.text = String.format(Locale.US, "%.8f BCH", amountBch)

        // Display fiat amount (oracle only; "—" if unavailable)
        binding.amountFiat.text = formatFiatFromOracle(amountBch, currency)

        // Display covenant address (shortened)
        val shortAddress = if (covenantAddress.length > 20) {
            "${covenantAddress.substring(0, 20)}...${covenantAddress.takeLast(10)}"
        } else {
            covenantAddress
        }
        binding.covenantAddress.text = shortAddress

        // Display expiry
        val timeRemaining = expiryTime - System.currentTimeMillis()
        val hoursRemaining = timeRemaining / (1000 * 60 * 60)
        val minutesRemaining = (timeRemaining / (1000 * 60)) % 60
        binding.expiryText.text = "⏰ Expires in ${hoursRemaining}h ${minutesRemaining}m"
    }

    private fun setupClaimButton() {
        binding.claimButton.setOnClickListener {
            attemptClaim()
        }
    }

    private fun attemptClaim() {
        val unlockCode = binding.unlockCodeInput.text.toString().trim()

        // Validate unlock code
        if (unlockCode.isEmpty()) {
            binding.unlockCodeInput.error = "Unlock code required"
            return
        }

        // For testing, accept any 4-digit code
        if (unlockCode.length < 4) {
            binding.unlockCodeInput.error = "Code must be at least 4 digits"
            return
        }

        // Show confirmation
        showClaimConfirmation(unlockCode)
    }

    private fun showClaimConfirmation(unlockCode: String) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currency = prefs.getString(CURRENCY_KEY, "EUR") ?: "EUR"
        val fiat = formatFiatFromOracle(amountBch, currency)

        val message = buildString {
            append("Claim ${String.format(Locale.US, "%.8f BCH", amountBch)}\n")
            append("($fiat)\n\n")
            append("From: $sender\n")
            append("Unlock code: $unlockCode")
        }

        AlertDialog.Builder(this)
            .setTitle("Confirm Claim")
            .setMessage(message)
            .setPositiveButton("Claim Now") { _, _ ->
                performClaim(unlockCode)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performClaim(unlockCode: String) {
        // TODO: In production, this would:
        // 1. Parse covenant transaction (CashScript bridge)
        // 2. Create claiming transaction with unlock code
        // 3. Broadcast to blockchain via Electrum
        // 4. Wait for confirmation
        // 5. Update balance

        // For testing: Show success message and return to main screen
        val instructions = buildString {
            append("✅ Claim Successful (Test)\n\n")
            append("📋 In production:\n")
            append("1️⃣ Parse covenant: $covenantAddress\n")
            append("2️⃣ Build claim transaction (CashScript)\n")
            append("3️⃣ Sign with unlock code: $unlockCode\n")
            append("4️⃣ Broadcast to blockchain (Electrum)\n")
            append("5️⃣ Wait for confirmation\n")
            append("6️⃣ Update balance: +${String.format("%.8f", amountBch)} BCH\n\n")
            append("🧪 For testing:\n")
            append("The covenant would be claimed and removed from your pending list.\n")
            append("Your balance would increase by ${String.format("%.8f", amountBch)} BCH.")
        }

        AlertDialog.Builder(this)
            .setTitle("Claiming BCH...")
            .setMessage(instructions)
            .setPositiveButton("Done") { _, _ ->
                // Remove from pending notifications
                lifecycleScope.launch {
                    // In production, mark notification as claimed
                    // For now, just finish activity
                    Toast.makeText(
                        this@ClaimActivity,
                        "Covenant claimed! (Test mode)",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
            .show()
    }

    /** Fiat from the oracle only; "—" when unavailable — never a hardcoded price (`ui-rethink/16` P0-2). */
    private fun formatFiatFromOracle(amountBch: Double, currency: String): String {
        if (currency != "EUR") return "—"  // the BCH oracle prices EUR only
        val rate = oracleEurRate ?: return "—"
        val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
        format.currency = Currency.getInstance(currency)
        return format.format(amountBch * rate)
    }

    /** Fetch the oracle BCH/EUR rate, then re-render the fiat line (`ui-rethink/16` P0-2). */
    private fun refreshOracleRate() {
        lifecycleScope.launch {
            val rate = OraclePriceSource.eurPerBch()
            if (rate == null) {
                Log.w(TAG, "⚠️ Oracle price unavailable — fiat shown as —")
            } else {
                oracleEurRate = rate
                displayCovenantDetails()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
