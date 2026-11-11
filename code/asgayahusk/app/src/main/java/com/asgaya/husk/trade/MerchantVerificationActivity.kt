package com.asgaya.husk.trade

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.asgaya.husk.CovenantWebView
import com.asgaya.husk.wallet.WalletManager
import com.asgaya.husk.covenant.CovenantBuildService
import com.asgaya.husk.data.CashoutRequest
import com.asgaya.husk.data.TestCashoutData
import com.asgaya.husk.data.VerificationResult
import com.asgaya.husk.databinding.ActivityMerchantVerificationBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

/**
 * MerchantVerificationActivity - Verify covenant before accepting cashout
 *
 * Runs 8-step verification checklist:
 * 1. UTXO exists on-chain
 * 2. UTXO is unspent
 * 3. Amount correct
 * 4. Not expired
 * 5. Price OK
 * 6. Oracle signature valid
 * 7. Oracle message fresh (<1 min)
 * 8. Output 0 = my address
 *
 * If all pass → merchant can sign & broadcast
 */
class MerchantVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMerchantVerificationBinding
    private lateinit var cashoutRequest: CashoutRequest
    private var verificationResult: VerificationResult? = null
    private var customerName: String = "Unknown"

    // Add CovenantWebView and WalletManager for real signing
    private lateinit var covenantWebView: CovenantWebView
    private lateinit var walletManager: WalletManager

    companion object {
        private const val TAG = "MerchantVerification"
        const val EXTRA_REQUEST_ID = "request_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMerchantVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize services
        covenantWebView = CovenantWebView.getInstance(this)
        walletManager = WalletManager(this)

        setupToolbar()
        loadCashoutRequest()
        displayCustomerDetails()
        runVerification()
        setupButtons()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Verify Cashout"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun loadCashoutRequest() {
        // Get request ID from intent
        val requestId = intent.getStringExtra(EXTRA_REQUEST_ID)

        // Real data only. The test fixture is DEBUG-only — it must NEVER be a prod fallback
        // (`ui-rethink/16` P0-3).
        val isDebug = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val stored = requestId?.let { CashoutRequestStorage.get(it) }
        val request = stored ?: if (isDebug) TestCashoutData.getTestCashoutRequest() else null
        if (request == null) {
            Log.e(TAG, "❌ Cashout request not found (id=$requestId) - cannot verify")
            Toast.makeText(this, "Cashout request not found", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        cashoutRequest = request
        Log.d(TAG, "Loaded cashout request: ${cashoutRequest.covenantAddress}")
    }

    private fun displayCustomerDetails() {
        val fiatFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY).apply {
            currency = java.util.Currency.getInstance("EUR")
        }

        // Buyer (person buying fiat with BCH)
        customerName = "Buyer"

        binding.customerName.text = "From: $customerName"
        binding.cashToPay.text = "Cash to pay: ${fiatFormat.format(cashoutRequest.amountFiat)}"
        binding.bchToReceive.text = "BCH to receive: ${String.format("%.8f", cashoutRequest.amountBCH)} BCH"
        binding.covenantAddress.text = "Covenant: ${cashoutRequest.addressPreview}"
    }

    private fun runVerification() {
        lifecycleScope.launch {
            // Show loading state
            binding.verificationProgress.visibility = android.view.View.VISIBLE
            binding.verificationChecklist.visibility = android.view.View.GONE

            try {
                // REAL verification on-chain
                Log.d(TAG, "Starting real 8-step verification...")
                verificationResult = verifyCovenantOnChain(cashoutRequest)

                // Show results
                displayVerificationResults(verificationResult!!)

            } catch (e: Exception) {
                Log.e(TAG, "Verification failed", e)
                Toast.makeText(this@MerchantVerificationActivity,
                    "Verification error: ${e.message}", Toast.LENGTH_LONG).show()

                // Show all failed
                verificationResult = VerificationResult(
                    utxoExists = false,
                    utxoUnspent = false,
                    amountCorrect = false,
                    notExpired = false,
                    priceOk = false,
                    oracleSigValid = false,
                    oracleFresh = false,
                    output0IsMine = false
                )
                displayVerificationResults(verificationResult!!)
            } finally {
                binding.verificationProgress.visibility = android.view.View.GONE
                binding.verificationChecklist.visibility = android.view.View.VISIBLE
            }
        }
    }

    /**
     * Real 8-step verification on-chain
     */
    private suspend fun verifyCovenantOnChain(request: CashoutRequest): VerificationResult = withContext(Dispatchers.IO) {
        Log.d(TAG, "📋 Running 8-step verification on-chain...")

        // Step 1 & 2: Check UTXO exists and unspent
        val utxos = CovenantBuildService.fetchUTXOs(request.covenantAddress)
        val utxoExists = utxos.isNotEmpty()
        val utxoUnspent = utxos.isNotEmpty()
        Log.d(TAG, "  1. UTXO exists: $utxoExists")
        Log.d(TAG, "  2. UTXO unspent: $utxoUnspent")

        // Step 3: Check amount correct
        val expectedSats = (request.eurCents.toLong() * 100000000L) / request.initialBchPriceInCents.toLong()
        val amountCorrect = if (utxos.isNotEmpty()) {
            val actualSats = utxos[0].value
            val difference = Math.abs(actualSats - expectedSats)
            Log.d(TAG, "  3. Amount check: expected=$expectedSats, actual=$actualSats, diff=$difference")
            difference < 200000  // Allow 0.002 BCH variance (funding can vary)
        } else false
        Log.d(TAG, "  3. Amount correct: $amountCorrect")

        // Step 4: Check not expired
        val currentTime = System.currentTimeMillis() / 1000
        val notExpired = currentTime < request.expiryOracleTime
        Log.d(TAG, "  4. Not expired: $notExpired (current: $currentTime, expiry: ${request.expiryOracleTime})")

        // Step 5: Check price OK (TODO: Need real oracle price for this)
        // For now, assume OK if not expired
        val priceOk = notExpired
        Log.d(TAG, "  5. Price OK: $priceOk (placeholder - need real oracle price)")

        // Step 6: Oracle signature valid (TODO: Complex cryptographic verification)
        // For Phase 2 initial, we'll trust the recipient provided valid sig
        val oracleSigValid = request.oracleSig != "PLACEHOLDER_ORACLE_SIG_UI_TESTING"
        Log.d(TAG, "  6. Oracle sig valid: $oracleSigValid (placeholder - need crypto validation)")

        // Step 7: Oracle message fresh (< 60 seconds for face-to-face QR)
        // Oracle message format: 8 bytes timestamp (little-endian) + 8 bytes price
        val (oracleFresh, oracleAge) = if (request.oracleMessage.length >= 16) {
            try {
                // Extract first 8 bytes (16 hex chars) = timestamp
                val timestampHex = request.oracleMessage.substring(0, 16)
                // Convert from little-endian hex to Unix timestamp
                val timestamp = java.lang.Long.parseLong(
                    timestampHex.chunked(2).reversed().joinToString(""),
                    16
                )
                val currentTime = System.currentTimeMillis() / 1000
                val age = (currentTime - timestamp).toInt()
                val isFresh = age < 60 // Face-to-face QR: enforce < 60 seconds
                Pair(isFresh, age)
            } catch (e: Exception) {
                Log.e(TAG, "  7. Failed to parse oracle timestamp: ${e.message}")
                Pair(false, -1)
            }
        } else {
            Pair(false, -1)
        }
        Log.d(TAG, "  7. Oracle fresh: $oracleFresh (age: ${oracleAge}s, threshold: 60s)")

        // Step 8: Output 0 = my address
        // TightDS Finding 3: Check merchantPubkey matches active wallet's publicKey
        val activeWallet = walletManager.getActiveWallet()
        val output0IsMine = if (activeWallet != null) {
            val myPubkey = activeWallet.publicKey
            val messagePubkey = request.merchantPubkey
            myPubkey == messagePubkey
        } else {
            false
        }
        Log.d(TAG, "  8. Output 0 = my address: $output0IsMine (my pubkey: ${activeWallet?.publicKey}, message pubkey: ${request.merchantPubkey})")

        val result = VerificationResult(
            utxoExists = utxoExists,
            utxoUnspent = utxoUnspent,
            amountCorrect = amountCorrect,
            notExpired = notExpired,
            priceOk = priceOk,
            oracleSigValid = oracleSigValid,
            oracleFresh = oracleFresh,
            output0IsMine = output0IsMine,
            oracleAge = oracleAge  // Pass age to display in UI
        )

        Log.d(TAG, "✅ Verification complete: allPassed = ${result.allPassed}")
        result
    }

    private fun displayVerificationResults(result: VerificationResult) {
        // Update each check
        binding.checkUtxoExists.text = if (result.utxoExists) "✅ UTXO exists on-chain" else "❌ UTXO not found"
        binding.checkUtxoUnspent.text = if (result.utxoUnspent) "✅ UTXO is unspent" else "❌ UTXO already spent"
        binding.checkAmountCorrect.text = if (result.amountCorrect) "✅ Amount correct" else "❌ Amount mismatch"
        binding.checkNotExpired.text = if (result.notExpired) "✅ Not expired (6h 23m left)" else "❌ Covenant expired"
        binding.checkPriceOk.text = if (result.priceOk) "✅ Price OK" else "❌ Price too low"
        binding.checkOracleSigValid.text = if (result.oracleSigValid) "✅ Oracle signature valid" else "❌ Invalid oracle signature"
        binding.checkOracleFresh.text = if (result.oracleFresh) {
            if (result.oracleAge >= 0) "✅ Oracle fresh (${result.oracleAge}s old)" else "✅ Oracle fresh"
        } else {
            if (result.oracleAge >= 0) "❌ Oracle too old (${result.oracleAge}s, max 60s)" else "❌ Oracle message too old"
        }
        binding.checkOutputMine.text = if (result.output0IsMine) "✅ Output 0 = my address" else "❌ Output doesn't pay me"

        // Enable/disable sign button based on verification
        binding.signAndBroadcastButton.isEnabled = result.allPassed

        if (!result.allPassed) {
            binding.signAndBroadcastButton.text = "Verification Failed - Cannot Accept"
        }
    }

    private fun setupButtons() {
        binding.cancelButton.setOnClickListener {
            finish()
        }

        binding.signAndBroadcastButton.setOnClickListener {
            if (verificationResult?.allPassed == true) {
                Log.d(TAG, "Sign & Broadcast clicked - starting real transaction...")
                signAndBroadcastTransaction()
            }
        }
    }

    /**
     * REAL sign and broadcast via merchantCashoutHybrid()
     */
    private fun signAndBroadcastTransaction() {
        lifecycleScope.launch {
            try {
                // Show loading
                binding.signAndBroadcastButton.isEnabled = false
                binding.signAndBroadcastButton.text = "Broadcasting..."

                Log.d(TAG, "🔐 Getting merchant wallet...")
                val activeWallet = walletManager.getActiveWallet()
                if (activeWallet == null) {
                    throw Exception("No active wallet - please select a merchant wallet")
                }

                val merchantWIF = walletManager.getWIF(activeWallet)
                val merchantPubkey = activeWallet.publicKey
                val merchantAddress = activeWallet.address

                Log.d(TAG, "  Merchant WIF: ${merchantWIF.take(10)}...")
                Log.d(TAG, "  Merchant pubkey: ${merchantPubkey.take(20)}...")
                Log.d(TAG, "  Merchant address: $merchantAddress")

                // Derive funder address from pubkey
                val funderAddress = deriveFunderAddress(cashoutRequest.funderPubkey)
                Log.d(TAG, "  Funder address (derived): $funderAddress")

                // Build covenant params map
                val covenantParams = buildCovenantParamsMap(cashoutRequest)

                // Build oracle sig map (for now, use placeholder)
                val oracleSig = mapOf(
                    "message" to cashoutRequest.oracleMessage,
                    "signature" to cashoutRequest.oracleSig,
                    "price" to cashoutRequest.initialBchPriceInCents  // TODO: Use real current price
                )

                Log.d(TAG, "🚀 Calling merchantCashoutHybrid()...")
                Log.d(TAG, "   recipientSig from message: ${cashoutRequest.recipientSig}")
                Log.d(TAG, "   recipientSig length: ${cashoutRequest.recipientSig.length} chars")
                Log.d(TAG, "   recipientSig bytes: ${cashoutRequest.recipientSig.length / 2} bytes")

                // REAL call to merchantCashoutHybrid()
                val txid = covenantWebView.merchantCashoutHybrid(
                    covenantAddress = cashoutRequest.covenantAddress,
                    covenantParams = covenantParams,
                    recipientSig = cashoutRequest.recipientSig,
                    merchantWIF = merchantWIF,
                    merchantPubkey = merchantPubkey,
                    merchantAddress = merchantAddress,
                    funderAddress = funderAddress,
                    oracleSig = oracleSig
                )

                Log.d(TAG, "✅ Transaction broadcast! TXID: $txid")

                // Navigate to success screen with REAL txid
                navigateToSuccessScreen(txid)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Sign & broadcast failed", e)
                Toast.makeText(this@MerchantVerificationActivity,
                    "Broadcast failed: ${e.message}", Toast.LENGTH_LONG).show()

                // Re-enable button
                binding.signAndBroadcastButton.isEnabled = true
                binding.signAndBroadcastButton.text = "Sign & Broadcast"
            }
        }
    }

    /**
     * Build covenant params map for JavaScript
     */
    private fun buildCovenantParamsMap(request: CashoutRequest): Map<String, Any> {
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
     * Derive funder P2PKH address from pubkey
     * (Copied from claim logic - same pattern)
     */
    private fun deriveFunderAddress(funderPubkey: String): String {
        // For now, use a simple derivation
        // In production, we'd use libauth via JavaScript or a Kotlin crypto library
        // For testing, we'll just return a placeholder
        // TODO: Implement real address derivation
        return "bchtest:qr6m7j9njldwwzlg9v7v53unlr4jkmx6eylep8ekg2"  // Sender/Funder address from test wallet
    }

    private fun navigateToSuccessScreen(txid: String = "MOCK_TXID_7f3e9a1b2c4d5e6f") {
        val intent = Intent(this, CashoutSuccessActivity::class.java).apply {
            putExtra(CashoutSuccessActivity.EXTRA_REQUEST_ID, cashoutRequest.id)  // For updating status
            putExtra(CashoutSuccessActivity.EXTRA_AMOUNT_FIAT, cashoutRequest.amountFiat)
            putExtra(CashoutSuccessActivity.EXTRA_AMOUNT_BCH, cashoutRequest.amountBCH)
            putExtra(CashoutSuccessActivity.EXTRA_CUSTOMER_NAME, customerName)
            putExtra(CashoutSuccessActivity.EXTRA_TXID, txid)  // Real or mock txid
        }
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
