package com.asgaya.husk

import android.content.Context
import com.asgaya.husk.utils.TestnetSecrets
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellableContinuation
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * WebView Bridge for Covenant Operations
 *
 * Based on RS079 research - follows proven pattern used by:
 * - Paytaca (Capacitor)
 * - Cashonize (Quasar)
 * - Bitcoin.com (Cordova)
 *
 * Responsibilities:
 * - Load CashScript SDK in WebView
 * - Provide Kotlin ↔ JavaScript bridge
 * - Handle covenant claim/refund operations
 * - Return results via coroutines
 *
 * SINGLETON PATTERN: Only one instance should exist per app lifecycle
 */
class CovenantWebView private constructor(private val context: Context) {

    companion object {
        private const val TAG = "CovenantWebView"

        @Volatile
        private var INSTANCE: CovenantWebView? = null

        fun getInstance(context: Context): CovenantWebView {
            return INSTANCE ?: synchronized(this) {
                val instance = CovenantWebView(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    val webView: WebView = WebView(context).apply {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true

        // Allow file access for ES modules (needed for cashscript-bundle.js)
        settings.allowFileAccessFromFileURLs = true
        settings.allowUniversalAccessFromFileURLs = true

        // Add JavaScript interface
        addJavascriptInterface(AndroidBridge(), "Android")

        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d(TAG, "✅ Covenant bridge page loaded")
            }
        }

        // Load covenant bridge HTML from assets
        loadUrl("file:///android_asset/covenant-bridge.html")
    }

    // Coroutine continuations for async JavaScript calls
    private var claimContinuation: ((String) -> Unit)? = null
    private var claimErrorContinuation: ((Exception) -> Unit)? = null
    private var refundContinuation: ((String) -> Unit)? = null
    private var refundErrorContinuation: ((Exception) -> Unit)? = null
    private var abortContinuation: ((String) -> Unit)? = null
    private var abortErrorContinuation: ((Exception) -> Unit)? = null
    private var sendContinuation: ((String) -> Unit)? = null
    private var sendErrorContinuation: ((Exception) -> Unit)? = null
    private var broadcastContinuation: ((String) -> Unit)? = null
    private var broadcastErrorContinuation: ((Exception) -> Unit)? = null
    private var isReady = false
    private val readyCallbacks = mutableListOf<() -> Unit>()

    // v0.2 Hybrid Architecture - Transaction build listener
    interface TransactionBuildListener {
        fun onBuilt(resultJson: String)
        fun onError(errorMessage: String)
    }
    var transactionBuildListener: TransactionBuildListener? = null
    var merchantPreSignListener: TransactionBuildListener? = null
    var recipientCoSignListener: TransactionBuildListener? = null

    /**
     * Check if WebView bridge is ready (⚠️1: Lifecycle gate)
     *
     * SELLER AUTO-FUNDING: NotificationListener may run before any Activity has loaded
     * the WebView. Always check isReady() before calling sendBchHybrid.
     */
    fun isReady(): Boolean {
        return isReady
    }

    /**
     * Suspend until WebView bridge is ready (⚠️1: Lifecycle gate)
     *
     * Use this in NotificationListener to wait for bridge to load before funding.
     * Includes timeout to prevent infinite waiting.
     *
     * @param timeoutMs Maximum time to wait (default 30s)
     * @throws Exception if timeout reached
     */
    suspend fun awaitReady(timeoutMs: Long = 30_000): Unit = withContext(Dispatchers.Main) {
        if (isReady) {
            Log.d(TAG, "✅ WebView already ready")
            return@withContext
        }

        Log.d(TAG, "⏳ Waiting for WebView bridge to load...")

        suspendCancellableCoroutine { continuation ->
            val startTime = System.currentTimeMillis()

            val callback = {
                if (continuation.isActive) {
                    Log.d(TAG, "✅ WebView ready after ${System.currentTimeMillis() - startTime}ms")
                    continuation.resume(Unit)
                }
            }

            // Register callback
            readyCallbacks.add(callback)

            // Timeout handler
            val timeoutHandler = android.os.Handler(android.os.Looper.getMainLooper())
            timeoutHandler.postDelayed({
                if (continuation.isActive) {
                    readyCallbacks.remove(callback)
                    if (!isReady) {
                        Log.e(TAG, "❌ WebView bridge timeout after ${timeoutMs}ms")
                        continuation.resumeWithException(
                            Exception("WebView bridge not ready after ${timeoutMs}ms")
                        )
                    } else {
                        continuation.resume(Unit)
                    }
                }
            }, timeoutMs)

            // Cleanup on cancellation
            continuation.invokeOnCancellation {
                readyCallbacks.remove(callback)
                timeoutHandler.removeCallbacksAndMessages(null)
            }
        }
    }

    /**
     * JavaScript Interface
     * Exposed to JavaScript as "Android" object
     */
    inner class AndroidBridge {

        @JavascriptInterface
        fun onReady() {
            Log.d(TAG, "JavaScript bridge ready")
            isReady = true

            // Trigger waiting callbacks (⚠️1: Notify awaitReady() callers)
            val callbacks = readyCallbacks.toList()
            readyCallbacks.clear()
            callbacks.forEach { it.invoke() }
        }

        @JavascriptInterface
        fun onLog(message: String) {
            Log.d(TAG, message)
        }

        @JavascriptInterface
        fun onClaimSuccess(txHex: String) {
            Log.d(TAG, "✅ Claim transaction built: ${txHex.take(50)}...")
            claimContinuation?.invoke(txHex)
            claimContinuation = null
            claimErrorContinuation = null
        }

        @JavascriptInterface
        fun onClaimError(error: String) {
            Log.e(TAG, "❌ Claim failed: $error")
            claimErrorContinuation?.invoke(Exception(error))
            claimContinuation = null
            claimErrorContinuation = null
        }

        @JavascriptInterface
        fun onRefundSuccess(txid: String) {
            Log.d(TAG, "✅ Refund successful: $txid")
            refundContinuation?.invoke(txid)
            refundContinuation = null
            refundErrorContinuation = null
        }

        @JavascriptInterface
        fun onRefundError(error: String) {
            Log.e(TAG, "❌ Refund failed: $error")
            refundErrorContinuation?.invoke(Exception(error))
            refundContinuation = null
            refundErrorContinuation = null
        }

        @JavascriptInterface
        fun onAbortSuccess(txid: String) {
            Log.d(TAG, "✅ Abort successful: $txid")
            abortContinuation?.invoke(txid)
            abortContinuation = null
            abortErrorContinuation = null
        }

        @JavascriptInterface
        fun onAbortError(error: String) {
            Log.e(TAG, "❌ Abort failed: $error")
            abortErrorContinuation?.invoke(Exception(error))
            abortContinuation = null
            abortErrorContinuation = null
        }

        @JavascriptInterface
        fun onSendSuccess(txid: String) {
            Log.d(TAG, "✅ Send transaction broadcast: $txid")
            sendContinuation?.invoke(txid)
            sendContinuation = null
            sendErrorContinuation = null
        }

        @JavascriptInterface
        fun onSendError(error: String) {
            Log.e(TAG, "❌ Send failed: $error")
            sendErrorContinuation?.invoke(Exception(error))
            sendContinuation = null
            sendErrorContinuation = null
        }

        // v0.2 Hybrid Architecture - Build callbacks (DO NOT use continuation pattern)
        // These are called from buildTransactionFromUTXOs() which is fire-and-forget
        // The broadcast happens in the callback, not in the caller

        @JavascriptInterface
        fun onTransactionBuilt(resultJson: String) {
            Log.d(TAG, "📦 Transaction built by WebView")
            // Note: Broadcast will be triggered from ReviewSendActivity listener
            // We just store the result and let the activity handle it
            transactionBuildListener?.onBuilt(resultJson)
        }

        @JavascriptInterface
        fun onTransactionBuildError(errorMessage: String) {
            Log.e(TAG, "❌ Build error: $errorMessage")
            transactionBuildListener?.onError(errorMessage)
        }

        @JavascriptInterface
        fun onMerchantPreSignSuccess(resultJson: String) {
            Log.d(TAG, "✅ Merchant pre-sign complete!")
            merchantPreSignListener?.onBuilt(resultJson)
        }

        @JavascriptInterface
        fun onMerchantPreSignError(errorMessage: String) {
            Log.e(TAG, "❌ Merchant pre-sign error: $errorMessage")
            merchantPreSignListener?.onError(errorMessage)
        }

        @JavascriptInterface
        fun onRecipientCoSignSuccess(resultJson: String) {
            Log.d(TAG, "✅ Recipient co-sign complete!")
            recipientCoSignListener?.onBuilt(resultJson)
        }

        @JavascriptInterface
        fun onRecipientCoSignError(errorMessage: String) {
            Log.e(TAG, "❌ Recipient co-sign error: $errorMessage")
            recipientCoSignListener?.onError(errorMessage)
        }

        @JavascriptInterface
        fun onBroadcastSuccess(txid: String) {
            Log.d(TAG, "✅ Transaction broadcasted! TXID: $txid")
            broadcastContinuation?.invoke(txid)
            broadcastContinuation = null
            broadcastErrorContinuation = null
        }

        @JavascriptInterface
        fun onBroadcastError(errorMessage: String) {
            Log.e(TAG, "❌ Broadcast error: $errorMessage")
            broadcastErrorContinuation?.invoke(Exception(errorMessage))
            broadcastContinuation = null
            broadcastErrorContinuation = null
        }

        @JavascriptInterface
        fun onPreSignComplete(resultJson: String) {
            Log.d(TAG, "✅ Pre-sign complete!")
            val result = JSONObject(resultJson)
            val recipientSig = result.getString("recipientSig")
            Log.d(TAG, "  recipientSig: ${recipientSig.substring(0, 20)}...")
            preSignContinuation?.resume(recipientSig)
            preSignContinuation = null
        }

        @JavascriptInterface
        fun onPreSignError(error: String) {
            Log.e(TAG, "❌ Pre-sign error: $error")
            preSignContinuation?.resumeWithException(Exception(error))
            preSignContinuation = null
        }
    }

    /**
     * Claim covenant via JavaScript bridge
     *
     * @param redeemScriptHex RedeemScript from database (hex string)
     * @param recipientWIF Recipient's private key (WIF format)
     * @param recipientAddress Recipient's BCH address
     * @param utxoTxHash Covenant UTXO transaction hash
     * @param utxoIndex Covenant UTXO output index
     * @param utxoAmount Covenant UTXO amount in satoshis
     * @param unlockCode Unlock code (default 1234)
     * @return Transaction hex ready to broadcast
     */
    /**
     * Claim covenant (recipient receives payment at current price)
     *
     * Recipient can claim after covenant is funded.
     * Recipient gets EUR amount at current price, seller gets remainder.
     *
     * @param covenantParams Covenant constructor parameters (senderPubkey, recipientPubkey, etc.)
     * @param oracleSig Oracle signature data (message, signature, price, timestamp)
     * @param recipientWIF Recipient's private key (WIF format)
     * @param recipientAddress Recipient's BCH address (receives EUR amount)
     * @param fulcrumHost Fulcrum server host
     * @param fulcrumPort Fulcrum server port
     * @return Transaction ID
     */
    suspend fun claimCovenant(
        covenantParams: Map<String, Any>,
        oracleSig: Map<String, Any>,
        recipientWIF: String,
        recipientAddress: String,
        funderAddress: String,  // Funder's address for remainder (volatility buffer)
        fulcrumHost: String = "localhost",
        fulcrumPort: Int = 60003
    ): String = suspendCancellableCoroutine { continuation ->

        Log.d(TAG, "Calling JavaScript claimCovenant()")

        // Store continuation for callback
        claimContinuation = { txid ->
            continuation.resume(txid)
        }
        claimErrorContinuation = { error ->
            continuation.resumeWithException(error)
        }

        // Build parameters JSON matching JavaScript function
        val params = JSONObject().apply {
            put("covenantParams", JSONObject(covenantParams))
            put("oracleSig", JSONObject(oracleSig))
            put("recipientWIF", recipientWIF)
            put("recipientAddress", recipientAddress)
            put("funderAddress", funderAddress)  // Funder gets remainder (volatility buffer)
            put("fulcrumHost", fulcrumHost)
            put("fulcrumPort", fulcrumPort)
        }

        Log.d(TAG, "JavaScript code to execute:")
        Log.d(TAG, "claimCovenant('${params.toString().take(200)}...')")

        val paramsJson = params.toString().replace("\"", "\\\"")

        // Call JavaScript function
        webView.post {
            webView.evaluateJavascript(
                "claimCovenant(\"$paramsJson\")",
                null
            )
        }

        Log.d(TAG, "JavaScript execution result: {}")

        // Handle cancellation
        continuation.invokeOnCancellation {
            claimContinuation = null
            claimErrorContinuation = null
        }
    }

    /**
     * Refund covenant (sender reclaims ANYTIME - v2.5 has no timelock!)
     *
     * Contract allows refund anytime, but UI should warn before expiry.
     * Sender gets EUR amount back at current price, seller gets remainder.
     *
     * @param covenantParams Covenant constructor parameters (senderPubkey, recipientPubkey, etc.)
     * @param oracleSig Oracle signature data (message, signature, price, timestamp)
     * @param senderWIF Sender's private key (WIF format)
     * @param senderAddress Sender's BCH address (receives EUR amount)
     * @param funderAddress Funder's BCH address (receives remainder)
     * @param fulcrumHost Fulcrum server host
     * @param fulcrumPort Fulcrum server port
     * @return Transaction ID
     */
    suspend fun refundCovenant(
        covenantParams: Map<String, Any>,
        oracleSig: Map<String, Any>,
        senderWIF: String,
        senderAddress: String,
        funderAddress: String,
        fulcrumHost: String = "localhost",
        fulcrumPort: Int = 60003
    ): String = suspendCancellableCoroutine { continuation ->

        Log.d(TAG, "Calling JavaScript refundCovenant()")

        // Store continuation for callback
        refundContinuation = { txid ->
            continuation.resume(txid)
        }
        refundErrorContinuation = { error ->
            continuation.resumeWithException(error)
        }

        // Build parameters JSON
        val params = JSONObject().apply {
            put("covenantParams", JSONObject(covenantParams))
            put("oracleSig", JSONObject(oracleSig))
            put("senderWIF", senderWIF)
            put("senderAddress", senderAddress)
            put("funderAddress", funderAddress)
            put("fulcrumHost", fulcrumHost)
            put("fulcrumPort", fulcrumPort)
        }

        val paramsJson = params.toString()
        val jsCode = "refundCovenant('${paramsJson.replace("'", "\\'")}')"

        Log.d(TAG, "JavaScript code to execute:")
        Log.d(TAG, jsCode.take(200) + "...")

        // Call JavaScript function
        webView.post {
            webView.evaluateJavascript(jsCode) { result ->
                Log.d(TAG, "JavaScript execution result: $result")
            }
        }

        // Handle cancellation
        continuation.invokeOnCancellation {
            refundContinuation = null
            refundErrorContinuation = null
        }
    }

    /**
     * v0.2 HYBRID: Refund covenant with Kotlin broadcast
     *
     * This method implements the hybrid pattern for refund:
     * - WebView builds CashScript refund transaction
     * - Kotlin broadcasts via native TCP (no WebSocket hang!)
     *
     * @param covenantParams Covenant constructor parameters
     * @param oracleSig Oracle signature data
     * @param senderWIF Sender's WIF
     * @param senderAddress Sender's address
     * @param funderAddress Funder's address
     * @param fulcrumHost Fulcrum server (for UTXO fetch only)
     * @param fulcrumPort Fulcrum port
     * @return Transaction ID
     */
    suspend fun refundCovenantHybrid(
        covenantParams: Map<String, Any>,
        oracleSig: Map<String, Any>,
        senderWIF: String,
        senderAddress: String,
        funderAddress: String,
        covenantAddress: String,
        fulcrumHost: String = "localhost",
        fulcrumPort: Int = 60003
    ): String {
        Log.d(TAG, "=== v0.2 Hybrid Refund (no WebSocket) ===")

        // Step 1: Fetch covenant UTXOs via Kotlin (native TCP) — replaces contract.getUtxos()
        Log.d(TAG, "📡 Step 1: Fetching covenant UTXOs via Kotlin...")
        val utxos = withContext(Dispatchers.IO) {
            com.asgaya.husk.covenant.CovenantBuildService.fetchUTXOs(covenantAddress)
        }
        Log.d(TAG, "✅ Found ${utxos.size} UTXOs")

        if (utxos.isEmpty()) {
            throw Exception("No UTXOs found - covenant not funded or already spent")
        }

        // Build transaction in WebView, get txHex back
        val txHex = suspendCancellableCoroutine<String> { continuation ->

            // Set up transaction build listener
            transactionBuildListener = object : TransactionBuildListener {
                override fun onBuilt(resultJson: String) {
                    Log.d(TAG, "📦 Refund transaction built by WebView")
                    try {
                        val result = JSONObject(resultJson)
                        val txHex = result.getString("txHex")

                        transactionBuildListener = null
                        continuation.resume(txHex)

                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Failed to parse refund result", e)
                        transactionBuildListener = null
                        continuation.resumeWithException(Exception("Parse failed: ${e.message}"))
                    }
                }

                override fun onError(errorMessage: String) {
                    Log.e(TAG, "❌ Refund build failed: $errorMessage")
                    transactionBuildListener = null
                    continuation.resumeWithException(Exception("Build failed: $errorMessage"))
                }
            }

            // Convert UTXOs to JSON array
            val utxosJson = JSONArray()
            utxos.forEach { utxo ->
                utxosJson.put(JSONObject().apply {
                    put("txHash", utxo.txHash)
                    put("txPos", utxo.txPos)
                    put("value", utxo.value)
                    put("height", utxo.height)
                })
            }

            // Build parameters JSON
            val params = JSONObject().apply {
                put("covenantParams", JSONObject(covenantParams))
                put("oracleSig", JSONObject(oracleSig))
                put("senderWIF", senderWIF)
                put("senderAddress", senderAddress)
                put("funderAddress", funderAddress)
                put("utxos", utxosJson)
            }

            val paramsJson = params.toString()
            val jsCode = "buildRefundTransactionFromUTXOs('${paramsJson.replace("'", "\\'")}')"

            Log.d(TAG, "Calling buildRefundTransactionFromUTXOs()...")

            // Call JavaScript function
            webView.post {
                webView.evaluateJavascript(jsCode) { result ->
                    Log.d(TAG, "JavaScript call result: $result")
                }
            }

            // Handle cancellation
            continuation.invokeOnCancellation {
                transactionBuildListener = null
            }
        }

        // Broadcast via Kotlin (native TCP)
        Log.d(TAG, "📡 Broadcasting refund via Kotlin...")
        val txid = withContext(Dispatchers.IO) {
            com.asgaya.husk.covenant.CovenantBuildService.broadcastTransaction(txHex)
        }

        Log.d(TAG, "✅ Refund broadcast successful! $txid")

        return txid
    }

    /**
     * v0.2 HYBRID: Seller recovers its buffer after expiry (sender offline).
     * Covenant path: sellerRecoverBuffer(sellerSig, oracleSig, oracleMessage).
     * The active wallet is the SELLER/funder: output[0] = payment → sender, output[1] = buffer → seller.
     * Kotlin fetches UTXOs, JavaScript builds, Kotlin broadcasts.
     *
     * @param covenantParams Covenant parameters
     * @param oracleSig Oracle signature data
     * @param sellerWIF Seller's (funder's) WIF (to sign)
     * @param sellerAddress Seller's (funder's) address (gets the buffer)
     * @param senderAddress Sender's address (gets the payment; empty → JS derives from senderPubkey)
     * @param covenantAddress Covenant P2SH32 address
     * @return Transaction ID
     */
    suspend fun sellerRecoverBufferHybrid(
        covenantParams: Map<String, Any>,
        oracleSig: Map<String, Any>,
        sellerWIF: String,
        sellerAddress: String,
        senderAddress: String,
        covenantAddress: String,
        fulcrumHost: String = "localhost",
        fulcrumPort: Int = 60003
    ): String {
        Log.d(TAG, "=== v0.2 Hybrid Seller Recover Buffer (no WebSocket) ===")

        // Step 1: Fetch covenant UTXOs via Kotlin (native TCP) — replaces contract.getUtxos()
        Log.d(TAG, "📡 Step 1: Fetching covenant UTXOs via Kotlin...")
        val utxos = withContext(Dispatchers.IO) {
            com.asgaya.husk.covenant.CovenantBuildService.fetchUTXOs(covenantAddress)
        }
        Log.d(TAG, "✅ Found ${utxos.size} UTXOs")

        if (utxos.isEmpty()) {
            throw Exception("No UTXOs found - covenant not funded or already spent")
        }

        // Build transaction in WebView, get txHex back
        val txHex = suspendCancellableCoroutine<String> { continuation ->

            // Set up transaction build listener
            transactionBuildListener = object : TransactionBuildListener {
                override fun onBuilt(resultJson: String) {
                    Log.d(TAG, "📦 sellerRecoverBuffer transaction built by WebView")
                    try {
                        val result = JSONObject(resultJson)
                        val txHex = result.getString("txHex")

                        transactionBuildListener = null
                        continuation.resume(txHex)

                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Failed to parse sellerRecoverBuffer result", e)
                        transactionBuildListener = null
                        continuation.resumeWithException(Exception("Parse failed: ${e.message}"))
                    }
                }

                override fun onError(errorMessage: String) {
                    Log.e(TAG, "❌ sellerRecoverBuffer build failed: $errorMessage")
                    transactionBuildListener = null
                    continuation.resumeWithException(Exception("Build failed: $errorMessage"))
                }
            }

            // Convert UTXOs to JSON array
            val utxosJson = JSONArray()
            utxos.forEach { utxo ->
                utxosJson.put(JSONObject().apply {
                    put("txHash", utxo.txHash)
                    put("txPos", utxo.txPos)
                    put("value", utxo.value)
                    put("height", utxo.height)
                })
            }

            // Build parameters JSON
            val params = JSONObject().apply {
                put("covenantParams", JSONObject(covenantParams))
                put("oracleSig", JSONObject(oracleSig))
                put("sellerWIF", sellerWIF)
                put("sellerAddress", sellerAddress)
                put("senderAddress", senderAddress)
                put("utxos", utxosJson)
            }

            val paramsJson = params.toString()
            val jsCode = "buildSellerRecoverTransactionFromUTXOs('${paramsJson.replace("'", "\\'")}')"

            Log.d(TAG, "Calling buildSellerRecoverTransactionFromUTXOs()...")

            // Call JavaScript function
            webView.post {
                webView.evaluateJavascript(jsCode) { result ->
                    Log.d(TAG, "JavaScript call result: $result")
                }
            }

            // Handle cancellation
            continuation.invokeOnCancellation {
                transactionBuildListener = null
            }
        }

        // Broadcast via Kotlin (native TCP)
        Log.d(TAG, "📡 Broadcasting sellerRecoverBuffer via Kotlin...")
        val txid = withContext(Dispatchers.IO) {
            com.asgaya.husk.covenant.CovenantBuildService.broadcastTransaction(txHex)
        }

        Log.d(TAG, "✅ sellerRecoverBuffer broadcast successful! $txid")

        return txid
    }

    /**
     * v0.2 HYBRID: Claim covenant (recipient gets EUR payment)
     * Kotlin fetches UTXOs, JavaScript builds transaction, Kotlin broadcasts
     *
     * @param covenantAddress Covenant P2SH32 address
     * @param covenantParams Covenant parameters
     * @param oracleSig Oracle signature data
     * @param recipientWIF Recipient's WIF (to sign transaction)
     * @param recipientAddress Recipient's address (gets payment)
     * @param funderAddress Funder's address (gets remainder)
     * @return Transaction ID
     */
    suspend fun claimCovenantHybrid(
        covenantAddress: String,
        covenantParams: Map<String, Any>,
        oracleSig: Map<String, Any>,
        recipientWIF: String,
        recipientAddress: String,
        funderAddress: String
    ): String {
        Log.d(TAG, "=== v0.2 Hybrid Claim ===")
        Log.d(TAG, "  Covenant: $covenantAddress")

        // Step 1: Fetch UTXOs via Kotlin (native TCP)
        Log.d(TAG, "📡 Step 1: Fetching covenant UTXOs via Kotlin...")
        val utxos = withContext(Dispatchers.IO) {
            com.asgaya.husk.covenant.CovenantBuildService.fetchUTXOs(covenantAddress)
        }
        Log.d(TAG, "✅ Found ${utxos.size} UTXOs")

        if (utxos.isEmpty()) {
            throw Exception("No UTXOs found - covenant not funded or already claimed")
        }

        // Step 2: Build transaction in WebView with provided UTXOs
        Log.d(TAG, "🔨 Step 2: Building claim transaction in JavaScript...")
        val txHex = suspendCancellableCoroutine<String> { continuation ->

            // Set up transaction build listener
            transactionBuildListener = object : TransactionBuildListener {
                override fun onBuilt(resultJson: String) {
                    Log.d(TAG, "📦 Claim transaction built by WebView")
                    try {
                        val result = JSONObject(resultJson)
                        val txHex = result.getString("txHex")

                        transactionBuildListener = null
                        continuation.resume(txHex)

                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Failed to parse claim result", e)
                        transactionBuildListener = null
                        continuation.resumeWithException(Exception("Parse failed: ${e.message}"))
                    }
                }

                override fun onError(errorMessage: String) {
                    Log.e(TAG, "❌ Claim build failed: $errorMessage")
                    transactionBuildListener = null
                    continuation.resumeWithException(Exception("Build failed: $errorMessage"))
                }
            }

            // Convert UTXOs to JSON array
            val utxosJson = JSONArray()
            utxos.forEach { utxo ->
                utxosJson.put(JSONObject().apply {
                    put("txHash", utxo.txHash)
                    put("txPos", utxo.txPos)
                    put("value", utxo.value)
                    put("height", utxo.height)
                })
            }

            // Build parameters JSON with UTXOs
            val params = JSONObject().apply {
                put("covenantParams", JSONObject(covenantParams))
                put("oracleSig", JSONObject(oracleSig))
                put("recipientWIF", recipientWIF)
                put("recipientAddress", recipientAddress)
                put("funderAddress", funderAddress)
                put("utxos", utxosJson)  // UTXOs from Kotlin!
            }

            val paramsJson = params.toString()
            val jsCode = "buildClaimTransactionFromUTXOs('${paramsJson.replace("'", "\\'")}')"

            Log.d(TAG, "Calling buildClaimTransactionFromUTXOs()...")

            // Call JavaScript function
            webView.post {
                webView.evaluateJavascript(jsCode) { result ->
                    Log.d(TAG, "JavaScript call result: $result")
                }
            }

            // Handle cancellation
            continuation.invokeOnCancellation {
                transactionBuildListener = null
            }
        }

        // Broadcast via Kotlin (native TCP)
        Log.d(TAG, "📡 Broadcasting claim via Kotlin...")
        val txid = withContext(Dispatchers.IO) {
            com.asgaya.husk.covenant.CovenantBuildService.broadcastTransaction(txHex)
        }

        Log.d(TAG, "✅ Claim broadcast successful! $txid")

        return txid
    }

    /**
     * v0.2 Hybrid: Merchant cashout (recipient sells BCH for cash at merchant)
     *
     * Flow: BCH-first (payment-first strategy)
     * 1. Kotlin fetches covenant UTXOs (native TCP)
     * 2. JavaScript builds transaction with recipientSig + merchantSig (no network)
     * 3. Kotlin broadcasts IMMEDIATELY (native TCP)
     * 4. Merchant gives cash to buyer (safe - UTXO already spent, refund blocked)
     *
     * Outputs:
     * - Output 0: Merchant gets BCH (payment)
     * - Output 1: Funder gets buffer (remainder)
     *
     * @param covenantAddress Covenant p2sh32 address
     * @param covenantParams Covenant constructor parameters
     * @param recipientSig Recipient's pre-signature (hex string)
     * @param merchantWIF Merchant's WIF (signs live)
     * @param merchantPubkey Merchant's public key (hex string)
     * @param merchantAddress Merchant's receive address
     * @param funderAddress Funder's address for buffer
     * @param oracleSig Fresh oracle signature (< 1 min old)
     * @return Transaction ID
     */
    suspend fun preSignMerchantCashout(
        covenantParams: Map<String, Any>,
        recipientWIF: String,
        merchantPubkey: String,
        merchantAddress: String,
        funderAddress: String,
        oracleSig: Map<String, Any>,
        utxos: List<ElectrumClient.UTXO>
    ): String = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            Log.d(TAG, "🖊️ Pre-signing merchantCashout for recipient...")

            // Build params for JavaScript
            val params = mapOf(
                "covenantParams" to covenantParams,
                "recipientWIF" to recipientWIF,
                "merchantPubkey" to merchantPubkey,
                "merchantAddress" to merchantAddress,
                "funderAddress" to funderAddress,
                "oracleSig" to oracleSig,
                "utxos" to utxos.map { utxo ->
                    mapOf<String, Any>(
                        "txHash" to utxo.txHash,
                        "txPos" to utxo.txPos,
                        "value" to utxo.value,
                        "height" to utxo.height
                    )
                }
            )

            val paramsJson = JSONObject(params).toString()

            // Store continuation for callback
            preSignContinuation = continuation

            // Call JavaScript
            Log.d(TAG, "  🔧 Calling JavaScript preSignMerchantCashout()...")
            evaluateJavascript("window.preSignMerchantCashout('${paramsJson.replace("'", "\\'")}')", null)
        }
    }

    // Continuation for pre-sign result
    private var preSignContinuation: CancellableContinuation<String>? = null

    suspend fun merchantCashoutHybrid(
        covenantAddress: String,
        covenantParams: Map<String, Any>,
        recipientSig: String,
        merchantWIF: String,
        merchantPubkey: String,
        merchantAddress: String,
        funderAddress: String,
        oracleSig: Map<String, Any>
    ): String {
        Log.d(TAG, "=== v0.2 Hybrid MerchantCashout ===")
        Log.d(TAG, "  Covenant: $covenantAddress")
        Log.d(TAG, "  Merchant: $merchantAddress")

        // Step 1: Fetch UTXOs via Kotlin (native TCP)
        Log.d(TAG, "📡 Step 1: Fetching covenant UTXOs via Kotlin...")
        val utxos = withContext(Dispatchers.IO) {
            com.asgaya.husk.covenant.CovenantBuildService.fetchUTXOs(covenantAddress)
        }
        Log.d(TAG, "✅ Found ${utxos.size} UTXOs")

        if (utxos.isEmpty()) {
            throw Exception("No UTXOs found - covenant not funded or already spent")
        }

        // Step 2: Build transaction in WebView with provided UTXOs
        Log.d(TAG, "🔨 Step 2: Building merchantCashout transaction in JavaScript...")
        val txHex = suspendCancellableCoroutine<String> { continuation ->

            // Set up transaction build listener
            transactionBuildListener = object : TransactionBuildListener {
                override fun onBuilt(resultJson: String) {
                    Log.d(TAG, "📦 MerchantCashout transaction built by WebView")
                    try {
                        val result = JSONObject(resultJson)
                        val txHex = result.getString("txHex")

                        transactionBuildListener = null
                        continuation.resume(txHex)

                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Failed to parse merchantCashout result", e)
                        transactionBuildListener = null
                        continuation.resumeWithException(Exception("Parse failed: ${e.message}"))
                    }
                }

                override fun onError(errorMessage: String) {
                    Log.e(TAG, "❌ MerchantCashout build failed: $errorMessage")
                    transactionBuildListener = null
                    continuation.resumeWithException(Exception("Build failed: $errorMessage"))
                }
            }

            // Convert UTXOs to JSON array
            val utxosJson = JSONArray()
            utxos.forEach { utxo ->
                utxosJson.put(JSONObject().apply {
                    put("txHash", utxo.txHash)
                    put("txPos", utxo.txPos)
                    put("value", utxo.value)
                    put("height", utxo.height)
                })
            }

            // Build parameters JSON with UTXOs
            val params = JSONObject().apply {
                put("covenantParams", JSONObject(covenantParams))
                put("recipientSig", recipientSig)          // Pre-signed by recipient
                put("merchantWIF", merchantWIF)            // Merchant signs live
                put("merchantPubkey", merchantPubkey)      // Merchant's pubkey
                put("merchantAddress", merchantAddress)    // Merchant's receive address
                put("funderAddress", funderAddress)        // Funder gets buffer
                put("oracleSig", JSONObject(oracleSig))   // Fresh oracle sig
                put("utxos", utxosJson)                    // UTXOs from Kotlin!
            }

            val paramsJson = params.toString()
            val jsCode = "buildMerchantCashoutTransactionFromUTXOs('${paramsJson.replace("'", "\\'")}')"

            Log.d(TAG, "Calling buildMerchantCashoutTransactionFromUTXOs()...")

            // Call JavaScript function
            webView.post {
                webView.evaluateJavascript(jsCode) { result ->
                    Log.d(TAG, "JavaScript call result: $result")
                }
            }

            // Handle cancellation
            continuation.invokeOnCancellation {
                transactionBuildListener = null
            }
        }

        // Step 3: Broadcast IMMEDIATELY via Kotlin (native TCP)
        Log.d(TAG, "📡 Step 3: Broadcasting merchantCashout via Kotlin (BCH-first!)...")
        val txid = withContext(Dispatchers.IO) {
            com.asgaya.husk.covenant.CovenantBuildService.broadcastTransaction(txHex)
        }

        Log.d(TAG, "✅ MerchantCashout broadcast successful! $txid")
        Log.d(TAG, "⚠️  NOW give cash to buyer (UTXO spent, refund blocked)")

        return txid
    }

    /**
     * Execute abort from covenant (emergency price drop exit)
     * Sends entire balance to sender (single output)
     *
     * @param covenantParams Covenant parameters (pubkeys, eurCents, etc.)
     * @param oracleSig Oracle signature with current price
     * @param senderWIF Sender's WIF (to sign transaction)
     * @param senderAddress Sender's address (gets entire balance)
     * @param fulcrumHost Fulcrum server host
     * @param fulcrumPort Fulcrum server port
     * @return Transaction ID
     */
    suspend fun abortCovenant(
        covenantParams: Map<String, Any>,
        oracleSig: Map<String, Any>,
        senderWIF: String,
        senderAddress: String,
        fulcrumHost: String = "localhost",
        fulcrumPort: Int = 60003
    ): String = suspendCancellableCoroutine { continuation ->

        Log.d(TAG, "Calling JavaScript executeAbort()")

        // Store continuation for callback
        abortContinuation = { txid ->
            continuation.resume(txid)
        }
        abortErrorContinuation = { error ->
            continuation.resumeWithException(error)
        }

        // Build parameters JSON
        val params = JSONObject().apply {
            put("covenantParams", JSONObject(covenantParams))
            put("oracleSig", JSONObject(oracleSig))
            put("senderWIF", senderWIF)
            put("senderAddress", senderAddress)
            put("fulcrumHost", fulcrumHost)
            put("fulcrumPort", fulcrumPort)
        }

        val paramsJson = params.toString()
        val jsCode = "executeAbort('${paramsJson.replace("'", "\\'")}')"

        Log.d(TAG, "JavaScript code to execute:")
        Log.d(TAG, jsCode.take(200) + "...")

        // Call JavaScript function
        webView.post {
            webView.evaluateJavascript(jsCode) { result ->
                Log.d(TAG, "JavaScript execution result: $result")
            }
        }

        // Handle cancellation
        continuation.invokeOnCancellation {
            abortContinuation = null
            abortErrorContinuation = null
        }
    }

    /**
     * Send simple P2PKH BCH transaction
     *
     * @param senderWIF Sender's private key (WIF format)
     * @param senderAddress Sender's BCH address
     * @param recipientAddress Recipient's BCH address
     * @param amountSats Amount to send in satoshis
     * @param fulcrumHost Fulcrum server host (default: Pi-chan)
     * @param fulcrumPort Fulcrum server port (default: 60003)
     * @return Transaction ID
     */
    suspend fun sendBch(
        senderWIF: String,
        senderAddress: String,
        recipientAddress: String,
        amountSats: Long,
        fulcrumHost: String = "localhost",
        fulcrumPort: Int = 60003
    ): String = suspendCancellableCoroutine { continuation ->

        Log.d(TAG, "Calling JavaScript sendBch()")
        Log.d(TAG, "  From: $senderAddress")
        Log.d(TAG, "  To: $recipientAddress")
        Log.d(TAG, "  Amount: $amountSats sats")
        Log.d(TAG, "  WebView ready: $isReady")

        // Store continuation for callback
        sendContinuation = { txid ->
            continuation.resume(txid)
        }
        sendErrorContinuation = { error ->
            continuation.resumeWithException(error)
        }

        // Build parameters JSON
        val params = JSONObject().apply {
            put("senderWIF", senderWIF)
            put("senderAddress", senderAddress)
            put("recipientAddress", recipientAddress)
            put("amountSats", amountSats)
            put("fulcrumHost", fulcrumHost)
            put("fulcrumPort", fulcrumPort)
        }

        // Use single quotes in JavaScript to avoid escaping hell
        val paramsJson = params.toString()
        val jsCode = "sendBch('${paramsJson.replace("'", "\\'")}')"

        Log.d(TAG, "JavaScript code to execute:")
        Log.d(TAG, jsCode.take(200) + "...")

        // Call JavaScript function
        webView.post {
            webView.evaluateJavascript(jsCode) { result ->
                Log.d(TAG, "JavaScript execution result: $result")
            }
        }

        // Handle cancellation
        continuation.invokeOnCancellation {
            sendContinuation = null
            sendErrorContinuation = null
        }
    }

    /**
     * v0.2 HYBRID ARCHITECTURE: Send BCH with Kotlin network layer
     *
     * This method implements the hybrid pattern:
     * - Kotlin fetches UTXOs (native TCP, no WebSocket)
     * - JavaScript builds+signs transaction (CashScript/libauth)
     * - Kotlin broadcasts (native TCP, no WebSocket)
     *
     * Why: JavaScript timers pause when Android screen goes off, causing
     * WebSocket connections to hang indefinitely.
     *
     * @param senderWIF Sender's private key (WIF format)
     * @param senderAddress Sender's BCH address
     * @param recipientAddress Recipient's BCH address
     * @param amountSats Amount in satoshis
     * @return Transaction ID
     */

    /**
     * v0.2 HYBRID: Execute abort from covenant (emergency exit)
     * Kotlin fetches UTXOs, JavaScript builds, Kotlin broadcasts
     *
     * @param covenantAddress The covenant address to fetch UTXOs from
     * @param covenantParams Covenant parameters
     * @param oracleSig Oracle signature data (shows price drop)
     * @param senderWIF Sender's WIF (to sign transaction)
     * @param senderAddress Sender's address (gets all funds back)
     * @return Transaction ID
     */
    suspend fun executeAbortHybrid(
        covenantAddress: String,
        covenantParams: Map<String, Any>,
        oracleSig: Map<String, Any>,
        senderWIF: String,
        senderAddress: String
    ): String {
        Log.d(TAG, "=== v0.2 Hybrid Abort ===")
        Log.d(TAG, "  Covenant: $covenantAddress")

        // Step 1: Fetch UTXOs via Kotlin (native TCP)
        Log.d(TAG, "📡 Step 1: Fetching covenant UTXOs via Kotlin...")
        val utxos = withContext(Dispatchers.IO) {
            com.asgaya.husk.covenant.CovenantBuildService.fetchUTXOs(covenantAddress)
        }
        Log.d(TAG, "✅ Found ${utxos.size} UTXOs")

        if (utxos.isEmpty()) {
            throw Exception("No UTXOs found - covenant not funded or already spent")
        }

        // Step 2: Build transaction in WebView with provided UTXOs
        Log.d(TAG, "🔨 Step 2: Building abort transaction in JavaScript...")
        val txHex = suspendCancellableCoroutine<String> { continuation ->

            // Set up transaction build listener
            transactionBuildListener = object : TransactionBuildListener {
                override fun onBuilt(resultJson: String) {
                    Log.d(TAG, "📦 Abort transaction built by WebView")
                    try {
                        val result = JSONObject(resultJson)
                        val txHex = result.getString("txHex")

                        transactionBuildListener = null
                        continuation.resume(txHex)

                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Failed to parse abort result", e)
                        transactionBuildListener = null
                        continuation.resumeWithException(Exception("Parse failed: ${e.message}"))
                    }
                }

                override fun onError(errorMessage: String) {
                    Log.e(TAG, "❌ Abort build failed: $errorMessage")
                    transactionBuildListener = null
                    continuation.resumeWithException(Exception("Build failed: $errorMessage"))
                }
            }

            // Convert UTXOs to JSON array
            val utxosJson = JSONArray()
            utxos.forEach { utxo ->
                utxosJson.put(JSONObject().apply {
                    put("txHash", utxo.txHash)
                    put("txPos", utxo.txPos)
                    put("value", utxo.value)
                    put("height", utxo.height)
                })
            }

            // Build parameters JSON with UTXOs
            val params = JSONObject().apply {
                put("covenantParams", JSONObject(covenantParams))
                put("oracleSig", JSONObject(oracleSig))
                put("senderWIF", senderWIF)
                put("senderAddress", senderAddress)
                put("utxos", utxosJson)  // UTXOs from Kotlin!
            }

            val paramsJson = params.toString()
            val jsCode = "buildAbortTransactionFromUTXOs('${paramsJson.replace("'", "\\'")}')"

            Log.d(TAG, "Calling buildAbortTransactionFromUTXOs()...")

            // Call JavaScript function
            webView.post {
                webView.evaluateJavascript(jsCode) { result ->
                    Log.d(TAG, "JavaScript call result: $result")
                }
            }

            // Handle cancellation
            continuation.invokeOnCancellation {
                transactionBuildListener = null
            }
        }

        // Step 3: Broadcast via Kotlin (native TCP)
        Log.d(TAG, "📡 Step 3: Broadcasting abort via Kotlin...")
        val txid = withContext(Dispatchers.IO) {
            com.asgaya.husk.covenant.CovenantBuildService.broadcastTransaction(txHex)
        }

        Log.d(TAG, "✅ Abort broadcast successful! $txid")
        Log.d(TAG, "⚠️ Funder gets nothing (buffer consumed by price drop)")

        return txid
    }

    suspend fun sendBchHybrid(
        senderWIF: String,
        senderAddress: String,
        recipientAddress: String,
        amountSats: Long
    ): String {
        Log.d(TAG, "=== v0.2 Hybrid Send ===")
        Log.d(TAG, "  From: $senderAddress")
        Log.d(TAG, "  To: $recipientAddress")
        Log.d(TAG, "  Amount: $amountSats sats")

        // Step 1: Fetch UTXOs via Kotlin (native TCP)
        Log.d(TAG, "📡 Step 1: Fetching UTXOs via Kotlin...")
        val utxos = withContext(Dispatchers.IO) {
            com.asgaya.husk.covenant.CovenantBuildService.fetchUTXOs(senderAddress)
        }

        if (utxos.isEmpty()) {
            throw Exception("No UTXOs available (balance is 0)")
        }

        Log.d(TAG, "✅ Found ${utxos.size} UTXOs")

        // Step 2: Wait for JavaScript to build transaction
        val (txHex, txid) = suspendCancellableCoroutine<Pair<String, String>> { continuation ->

            // Set up transaction build listener
            transactionBuildListener = object : TransactionBuildListener {
                override fun onBuilt(resultJson: String) {
                    Log.d(TAG, "📦 Transaction built by WebView")
                    try {
                        val result = JSONObject(resultJson)
                        val txHex = result.getString("txHex")
                        val txid = result.getString("txid")

                        // Clean up listener
                        transactionBuildListener = null

                        // Resume with txHex and txid
                        continuation.resume(Pair(txHex, txid))

                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Failed to parse build result", e)
                        transactionBuildListener = null
                        continuation.resumeWithException(Exception("Parse failed: ${e.message}"))
                    }
                }

                override fun onError(errorMessage: String) {
                    Log.e(TAG, "❌ Build failed: $errorMessage")
                    transactionBuildListener = null
                    continuation.resumeWithException(Exception("Build failed: $errorMessage"))
                }
            }

            // Call JavaScript to build+sign transaction
            Log.d(TAG, "🔨 Step 2: Calling JavaScript to build transaction...")

            val params = JSONObject().apply {
                put("senderWIF", senderWIF)
                put("senderAddress", senderAddress)
                put("recipientAddress", recipientAddress)
                put("amountSats", amountSats)
                // Convert UTXOs to JSON array
                put("utxos", org.json.JSONArray(utxos.map { utxo ->
                    JSONObject().apply {
                        put("txHash", utxo.txHash)
                        put("txPos", utxo.txPos)
                        put("value", utxo.value)
                        put("height", utxo.height)
                    }
                }))
            }

            val paramsJson = params.toString()
            val jsCode = "buildTransactionFromUTXOs('${paramsJson.replace("'", "\\'")}')"

            Log.d(TAG, "JavaScript: ${jsCode.take(150)}...")

            // Call JavaScript function
            webView.post {
                webView.evaluateJavascript(jsCode) { result ->
                    Log.d(TAG, "JavaScript call result: $result")
                }
            }

            // Handle cancellation
            continuation.invokeOnCancellation {
                transactionBuildListener = null
            }
        }

        // Step 3: Broadcast via Kotlin (native TCP)
        Log.d(TAG, "📡 Step 3: Broadcasting via Kotlin...")
        val broadcastedTxid = withContext(Dispatchers.IO) {
            com.asgaya.husk.covenant.CovenantBuildService.broadcastTransaction(txHex)
        }

        if (broadcastedTxid != txid) {
            Log.w(TAG, "⚠️ TXID mismatch: expected $txid, got $broadcastedTxid")
        }

        Log.d(TAG, "✅ Broadcast successful! $broadcastedTxid")

        return broadcastedTxid
    }

    /**
     * Broadcast a fully-signed transaction to the BCH network
     * Uses the Fulcrum connection from JavaScript
     */
    suspend fun broadcastTransaction(txHex: String): String = suspendCancellableCoroutine { continuation ->
        broadcastContinuation = { txid ->
            continuation.resume(txid)
        }
        broadcastErrorContinuation = { exception ->
            continuation.resumeWithException(exception)
        }

        continuation.invokeOnCancellation {
            broadcastContinuation = null
            broadcastErrorContinuation = null
        }

        // Call JavaScript to broadcast
        val jsCode = """
            (async () => {
                try {
                    console.log('📡 Broadcasting transaction...');
                    console.log('Transaction hex: ${txHex.take(100)}... (${txHex.length} chars)');

                    // Connect to Fulcrum if not connected
                    if (!window.electrumClient || !window.electrumClient.connected) {
                        console.log('Connecting to Fulcrum...');
                        window.electrumClient = new window.ElectrumClient(
                            window.ElectrumNetworkProvider.CHIPNET.electrum.host,
                            window.ElectrumNetworkProvider.CHIPNET.electrum.port,
                            window.ElectrumNetworkProvider.CHIPNET.electrum.scheme
                        );
                        await window.electrumClient.connect();
                        console.log('✅ Connected to Fulcrum');
                    }

                    // Broadcast transaction
                    const txid = await window.electrumClient.request(
                        'blockchain.transaction.broadcast',
                        '$txHex'
                    );

                    console.log('✅ Transaction broadcasted! TXID:', txid);
                    Android.onBroadcastSuccess(txid);

                } catch (error) {
                    console.error('❌ Broadcast failed:', error);
                    Android.onBroadcastError(error.message || String(error));
                }
            })();
        """.trimIndent()

        webView.post {
            webView.evaluateJavascript(jsCode, null)
        }
    }

    /**
     * Create covenant using WebView + CashScript SDK
     *
     * IMPORTANT: This is the ONLY way to create covenants! Do NOT use manual
     * bytecode construction (CovenantBuilder) as it creates incompatible addresses.
     *
     * Why WebView-only?
     * - Matches CashScript SDK encoding exactly
     * - Same method used by test scripts (claim/refund will work)
     * - Production pattern (Paytaca, Cashonize, Bitcoin.com all use this)
     * - See RS079 research for details
     *
     * @param senderPubkey Sender's public key (hex)
     * @param recipientPubkey Recipient's public key (hex)
     * @param funderPubkey Funder's public key (hex, usually same as sender for self-funded)
     * @param oraclePubkey Oracle's public key (hex)
     * @param eurCents EUR amount in cents (e.g., 1000 = €10)
     * @param expiryOracleTime Unix timestamp when covenant expires
     * @param initialBchPriceInCents Initial BCH price in EUR cents
     * @param minPricePercent Minimum price percentage (93 = 7% drop max)
     * @return Covenant P2SH32 address (bchtest:pv...)
     */
    suspend fun createCovenant(
        senderPubkey: String,
        recipientPubkey: String,
        funderPubkey: String,
        oraclePubkey: String,
        eurCents: Int,
        expiryOracleTime: Long,
        initialBchPriceInCents: Int,
        minPricePercent: Int
    ): String = suspendCancellableCoroutine { continuation ->

        Log.d(TAG, "Calling JavaScript createCovenant()")
        Log.d(TAG, "  EUR: €${eurCents / 100.0}")
        Log.d(TAG, "  Price: €${initialBchPriceInCents / 100.0}/BCH")
        Log.d(TAG, "  WebView ready: $isReady")

        // Create a one-time callback for the result
        var resultReceived = false

        // Build parameters JSON
        val params = JSONObject().apply {
            put("senderPubkey", senderPubkey)
            put("recipientPubkey", recipientPubkey)
            put("funderPubkey", funderPubkey)
            put("oraclePubkey", oraclePubkey)
            put("eurCents", eurCents)
            put("expiryOracleTime", expiryOracleTime)
            put("initialBchPriceInCents", initialBchPriceInCents)
            put("minPricePercent", minPricePercent)
        }

        val paramsJson = params.toString()
        val jsCode = "createCovenant('${paramsJson.replace("'", "\\'")}')"

        Log.d(TAG, "JavaScript code to execute:")
        Log.d(TAG, jsCode.take(200) + "...")

        // Call JavaScript function
        webView.post {
            webView.evaluateJavascript(jsCode) { result ->
                if (!resultReceived) {
                    resultReceived = true
                    Log.d(TAG, "JavaScript createCovenant result: $result")

                    // Result is a JSON string with quotes, e.g., "\"bchtest:p...\""
                    // Remove quotes and unescape
                    val address = result?.trim('"')?.replace("\\\"", "\"")?.trim('"')

                    // P2SH32 addresses start with "bchtest:p" (next char varies)
                    if (address != null && address.startsWith("bchtest:p") && address.length > 50) {
                        continuation.resume(address)
                    } else {
                        continuation.resumeWithException(
                            Exception("Invalid covenant address returned: $address")
                        )
                    }
                }
            }
        }

        // Handle cancellation
        continuation.invokeOnCancellation {
            resultReceived = true
        }
    }

    /**
     * Evaluate JavaScript in WebView
     * Useful for testing and calling JavaScript functions
     * Must be called from UI thread
     */
    fun evaluateJavascript(script: String, callback: ((String?) -> Unit)? = null) {
        Log.d(TAG, "evaluateJavascript called: ${script.take(100)}...")
        Log.d(TAG, "Executing JavaScript directly...")
        webView.evaluateJavascript(script) { result ->
            Log.d(TAG, "JavaScript result: $result")
            callback?.invoke(result)
        }
    }

    /**
     * Generate oracle signature for current timestamp and price
     * Uses oracle private key (testnet only!)
     *
     * @param currentPriceCents Current BCH price in cents (e.g., 65000 = €650/BCH)
     * @return Map with message, signature, price, timestamp
     */
    suspend fun generateOracleSignature(currentPriceCents: Int): Map<String, Any> = suspendCancellableCoroutine { continuation ->
        var resultReceived = false

        Log.d(TAG, "Generating oracle signature for price: €${currentPriceCents / 100}/BCH")

        // Local testnet oracle key (git-ignored; see TestnetSecrets)
        val oracleWIF = TestnetSecrets.load(context)?.oracleWif
        if (oracleWIF == null) {
            Log.e(TAG, "Oracle WIF not configured: assets/secrets/testnet-keys.json is missing")
            continuation.resumeWithException(IllegalStateException("Oracle WIF not configured (local testnet secrets missing)"))
            return@suspendCancellableCoroutine
        }

        val jsCode = """
            (function() {
                try {
                    // Use libauth (already loaded) for signing
                    const { decodePrivateKeyWif, secp256k1, sha256 } = window.libauth;

                    // Decode oracle WIF
                    const decoded = decodePrivateKeyWif('$oracleWIF');
                    if (typeof decoded === 'string') {
                        throw new Error('Invalid oracle WIF: ' + decoded);
                    }
                    const privateKey = decoded.privateKey;

                    const timestamp = Math.floor(Date.now() / 1000);
                    const price = $currentPriceCents;

                    // Create 16-byte message: timestamp (8 bytes) + price (8 bytes), little-endian
                    const message = new Uint8Array(16);
                    const view = new DataView(message.buffer);
                    view.setBigInt64(0, BigInt(timestamp), true); // little-endian
                    view.setBigInt64(8, BigInt(price), true);     // little-endian

                    // SHA256 hash the message
                    const messageHash = sha256.hash(message);

                    // Sign with secp256k1 (DER format)
                    const signatureResult = secp256k1.signMessageHashDER(privateKey, messageHash);
                    if (typeof signatureResult === 'string') {
                        throw new Error('Signing failed: ' + signatureResult);
                    }

                    const messageHex = Array.from(message).map(b => b.toString(16).padStart(2, '0')).join('');
                    const signatureHex = Array.from(signatureResult).map(b => b.toString(16).padStart(2, '0')).join('');

                    Android.onLog('[Oracle] Timestamp: ' + timestamp + ', Price: €' + (price/100));
                    Android.onLog('[Oracle] Signature length: ' + signatureResult.length + ' bytes');

                    return JSON.stringify({
                        message: messageHex,
                        signature: signatureHex,
                        price: price,
                        timestamp: timestamp
                    });
                } catch (e) {
                    Android.onLog('❌ Oracle signature error: ' + e.message);
                    return JSON.stringify({ error: e.message });
                }
            })()
        """.trimIndent()

        webView.post {
            webView.evaluateJavascript(jsCode) { result ->
                if (!resultReceived) {
                    resultReceived = true
                    Log.d(TAG, "Oracle signature result: $result")

                    try {
                        val jsonStr = result?.trim('"')?.replace("\\\"", "\"")
                        val json = JSONObject(jsonStr ?: "{}")

                        if (json.has("error")) {
                            continuation.resumeWithException(
                                Exception("Oracle signature failed: ${json.getString("error")}")
                            )
                        } else {
                            val oracleSig = mapOf(
                                "message" to json.getString("message"),
                                "signature" to json.getString("signature"),
                                "price" to json.getInt("price"),
                                "timestamp" to json.getLong("timestamp")
                            )
                            continuation.resume(oracleSig)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse oracle signature", e)
                        continuation.resumeWithException(e)
                    }
                }
            }
        }

        continuation.invokeOnCancellation {
            resultReceived = true
        }
    }

    /**
     * Derive public key from WIF
     * Uses libauth loaded in WebView
     *
     * @param wif Private key in WIF format
     * @return Public key as hex string
     */
    suspend fun derivePubKeyFromWIF(wif: String): String = suspendCancellableCoroutine { continuation ->
        var resultReceived = false

        Log.d(TAG, "Deriving public key from WIF...")

        val jsCode = """
            (function() {
                try {
                    // Use libauth (already loaded) - same approach as Selene Wallet
                    if (!window.libauth) {
                        return JSON.stringify({ error: 'libauth not loaded yet' });
                    }

                    const { decodePrivateKeyWif, secp256k1 } = window.libauth;

                    // Decode WIF to get private key (exactly like Selene does it)
                    const decoded = decodePrivateKeyWif('$wif');
                    if (typeof decoded === 'string') {
                        return JSON.stringify({ error: 'Invalid WIF: ' + decoded });
                    }

                    const privateKeyBin = decoded.privateKey;

                    // Derive public key (compressed)
                    const publicKeyCompressed = secp256k1.derivePublicKeyCompressed(privateKeyBin);

                    if (typeof publicKeyCompressed === 'string') {
                        return JSON.stringify({ error: 'Failed to derive public key: ' + publicKeyCompressed });
                    }

                    // Convert public key to hex
                    const publicKeyHex = Array.from(publicKeyCompressed)
                        .map(b => b.toString(16).padStart(2, '0'))
                        .join('');

                    return JSON.stringify({ publicKey: publicKeyHex });
                } catch (e) {
                    Android.onLog('❌ Public key derivation error: ' + e.message);
                    return JSON.stringify({ error: e.message });
                }
            })()
        """.trimIndent()

        webView.post {
            webView.evaluateJavascript(jsCode) { result ->
                if (!resultReceived) {
                    resultReceived = true
                    Log.d(TAG, "Public key derivation result: $result")

                    try {
                        // Parse JSON result
                        val jsonStr = result?.trim('"')?.replace("\\\"", "\"")
                        val json = JSONObject(jsonStr ?: "{}")

                        if (json.has("error")) {
                            continuation.resumeWithException(
                                Exception("Invalid WIF: ${json.getString("error")}")
                            )
                        } else {
                            val publicKey = json.getString("publicKey")
                            continuation.resume(publicKey)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse public key derivation result", e)
                        continuation.resumeWithException(e)
                    }
                }
            }
        }

        continuation.invokeOnCancellation {
            resultReceived = true
        }
    }

    /**
     * Derive BCH address and public key from WIF (DEPRECATED - use derivePubKeyFromWIF)
     * Uses libauth loaded in WebView
     *
     * @param wif Private key in WIF format
     * @return Pair of (address, publicKey) or null if invalid
     */
    @Deprecated("Use derivePubKeyFromWIF and provide address separately")
    suspend fun deriveFromWIF(wif: String): Pair<String, String> = suspendCancellableCoroutine { continuation ->
        var resultReceived = false

        Log.d(TAG, "Deriving address from WIF...")

        val jsCode = """
            (function() {
                try {
                    // Use libauth (already loaded) instead of bitcoincashjs-lib
                    if (!window.libauth) {
                        return JSON.stringify({ error: 'libauth not loaded yet' });
                    }

                    const { decodePrivateKeyWif, encodeAddress } = window.libauth;

                    // Decode WIF to get private key and public key
                    const decoded = decodePrivateKeyWif('$wif');
                    if (typeof decoded === 'string') {
                        return JSON.stringify({ error: 'Invalid WIF: ' + decoded });
                    }

                    const privateKeyBin = decoded.privateKey;
                    const publicKeyUncompressed = window.libauth.secp256k1.derivePublicKeyUncompressed(privateKeyBin);
                    const publicKeyCompressed = window.libauth.secp256k1.derivePublicKeyCompressed(privateKeyBin);

                    // Convert public key to hex
                    const publicKeyHex = Array.from(publicKeyCompressed)
                        .map(b => b.toString(16).padStart(2, '0'))
                        .join('');

                    // Encode as P2PKH address (testnet)
                    const pubkeyHash = window.libauth.hash160(publicKeyCompressed);
                    const address = encodeAddress({
                        payload: pubkeyHash,
                        type: 'p2pkh',
                        prefix: 'bchtest'
                    });

                    return JSON.stringify({
                        address: address,
                        publicKey: publicKeyHex
                    });
                } catch (e) {
                    Android.onLog('❌ WIF derivation error: ' + e.message);
                    return JSON.stringify({ error: e.message });
                }
            })()
        """.trimIndent()

        webView.post {
            webView.evaluateJavascript(jsCode) { result ->
                if (!resultReceived) {
                    resultReceived = true
                    Log.d(TAG, "WIF derivation result: $result")

                    try {
                        // Parse JSON result
                        val jsonStr = result?.trim('"')?.replace("\\\"", "\"")
                        val json = JSONObject(jsonStr ?: "{}")

                        if (json.has("error")) {
                            continuation.resumeWithException(
                                Exception("Invalid WIF: ${json.getString("error")}")
                            )
                        } else {
                            val address = json.getString("address")
                            val publicKey = json.getString("publicKey")
                            continuation.resume(Pair(address, publicKey))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse WIF derivation result", e)
                        continuation.resumeWithException(e)
                    }
                }
            }
        }

        continuation.invokeOnCancellation {
            resultReceived = true
        }
    }

    /**
     * Clean up WebView resources
     */
    fun destroy() {
        webView.destroy()
    }
}
