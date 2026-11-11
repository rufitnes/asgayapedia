package com.asgaya.husk.covenant

import android.content.Context
import android.util.Log
import com.asgaya.husk.AppDatabase
import com.asgaya.husk.CovenantWebView
import com.asgaya.husk.data.MerchantCosignResponse
import com.asgaya.husk.data.SimpleCashoutRequest
import com.asgaya.husk.data.Wallet
import com.asgaya.husk.wallet.WalletManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Merchant-first cash-out quote (file 55).
 *
 * Shared component so the merchant can auto-quote **in the background**
 * (NotificationListener) as well as from the Trade tab (QR path).
 *
 * The merchant fetches a fresh oracle, builds + pre-signs the cashout tx
 * (placeholder recipient sig), and returns a `[BCH_PURCHASE_COSIGN]` message
 * with a `quoteId` + `expiresAt` (short TTL).
 *
 * NOTE: this mirrors `TradeActivity.buildAndSignMerchantTransaction` +
 * `fetchFreshOracle`. Unifying the two is v0-rebuild work (see CURRENT-STATE "Later").
 */
object MerchantCashout {

    private const val TAG = "MerchantCashout"
    private const val ORACLE_URL = "http://localhost:3001/oracle/price"

    /** Quote TTL (file 55 §3/§8): short handshake; widen to 90–120s if relay latency is slow. */
    const val QUOTE_TTL_SECONDS = 60L

    data class Quote(val message: String, val quoteId: String, val expiresAt: Long)

    /**
     * Produce a merchant quote for a recipient's `[BCH_FOR_SALE]` request.
     * Returns null if the covenant is not funded (no UTXO) or the build fails.
     */
    suspend fun quote(
        context: Context,
        request: SimpleCashoutRequest,
        merchantWallet: Wallet
    ): Quote? {
        // 1. Fresh oracle at build time (the merchant controls the timing).
        val (oracleSig, oracleMessage, currentPrice) = fetchOracle()
        Log.d(TAG, "Oracle: €$currentPrice/BCH")

        // 2. Covenant UTXOs
        val utxos = withContext(Dispatchers.IO) {
            CovenantBuildService.fetchUTXOs(request.covenantAddress)
        }
        if (utxos.isEmpty()) {
            Log.w(TAG, "No UTXOs for ${request.covenantAddress} — not funded or spent")
            return null
        }

        // 3. Build + pre-sign via the WebView bridge
        val wm = WalletManager.getInstance(context)
        val merchantWIF = wm.getWIF(merchantWallet)
        val merchantAddress = merchantWallet.address
        val result = buildPreSign(
            context = context,
            request = request,
            merchantPubkey = merchantWallet.publicKey,
            merchantWIF = merchantWIF,
            merchantAddress = merchantAddress,
            oracleSig = oracleSig,
            oracleMessage = oracleMessage,
            currentPrice = currentPrice,
            utxos = utxos
        )

        // 4. Cosign message (quoteId + expiresAt)
        val quoteId = UUID.randomUUID().toString()
        val expiresAt = System.currentTimeMillis() / 1000 + QUOTE_TTL_SECONDS
        // Exchange currency = the merchant's ad asset (carried by the request, `ui-rethink/40`). The
        // amount is the EUR face → local via the reference, **net of the merchant's own ad fee**
        // (`bulletin-board/20`; a negative fee pays the recipient a premium — the price discovery).
        val exchangeCurrency = request.assetCurrency?.takeIf { it.isNotBlank() }
        val fee = exchangeCurrency?.let { cur ->
            AppDatabase.getDatabase(context).listingDao().getByPublicKey(merchantWallet.publicKey)
                .map { it.toListing() }
                .firstOrNull { l -> l.assets.any { a -> a.equals(cur, ignoreCase = true) } }
                ?.fee
        } ?: 0.0
        val rate = exchangeCurrency?.let { com.asgaya.husk.FiatRateSource.eurPer(it) } ?: 1.0
        val amountCash = (request.eurCents / 100.0) * rate * (1.0 - fee)
        val amountBCH = (request.eurCents.toDouble() / currentPrice) / 100.0
        Log.d(TAG, "  exchange=$exchangeCurrency fee=${"%.2f".format(fee * 100)}% rate=$rate → amountCash=$amountCash")

        val response = MerchantCosignResponse(
            transactionHex = result.txHex,
            covenantAddress = request.covenantAddress,
            merchantPubkey = merchantWallet.publicKey,
            merchantAddress = result.merchantAddress,
            funderAddress = result.funderAddress,
            oracleSig = oracleSig,
            oracleMessage = oracleMessage,
            currentPrice = currentPrice,
            amountCash = amountCash,
            amountBCH = amountBCH,
            currency = exchangeCurrency,
            recipientSigIndex = 0,
            quoteId = quoteId,
            expiresAt = expiresAt
        )

        Log.i(TAG, "✅ Quote ${request.covenantAddress.take(20)}… quoteId=$quoteId expiresAt=$expiresAt")
        return Quote(response.toMessage(), quoteId, expiresAt)
    }

    private suspend fun fetchOracle(): Triple<String, String, Double> = withContext(Dispatchers.IO) {
        val response = URL(ORACLE_URL).readText()
        val json = JSONObject(response)
        Triple(
            json.getString("signature"),
            json.getString("message"),
            json.getInt("price") / 100.0
        )
    }

    private data class PreSignResult(
        val txHex: String,
        val merchantAddress: String,
        val funderAddress: String
    )

    private suspend fun buildPreSign(
        context: Context,
        request: SimpleCashoutRequest,
        merchantPubkey: String,
        merchantWIF: String,
        merchantAddress: String,
        oracleSig: String,
        oracleMessage: String,
        currentPrice: Double,
        utxos: List<UTXO>
    ): PreSignResult {
        val covenantWebView = CovenantWebView.getInstance(context)

        // Wait for the bridge to be ready (background service may start it cold).
        try {
            if (!covenantWebView.isReady()) {
                covenantWebView.awaitReady(timeoutMs = 30_000)
            }
        } catch (e: Exception) {
            throw Exception("WebView bridge not ready: ${e.message}")
        }

        val resultJson = suspendCancellableCoroutine<String> { continuation ->
            covenantWebView.merchantPreSignListener = object : CovenantWebView.TransactionBuildListener {
                override fun onBuilt(resultJson: String) {
                    covenantWebView.merchantPreSignListener = null
                    continuation.resume(resultJson)
                }

                override fun onError(errorMessage: String) {
                    covenantWebView.merchantPreSignListener = null
                    continuation.resumeWithException(Exception("Pre-sign failed: $errorMessage"))
                }
            }

            val utxosJson = org.json.JSONArray()
            utxos.forEach { utxo ->
                utxosJson.put(JSONObject().apply {
                    put("txHash", utxo.txHash)
                    put("txPos", utxo.txPos)
                    put("value", utxo.value)
                    put("height", utxo.height)
                })
            }

            val covenantParams = mapOf(
                "senderPubkey" to request.senderPubkey,
                "recipientPubkey" to request.recipientPubkey,
                "funderPubkey" to request.funderPubkey,
                "oraclePubkey" to request.oraclePubkey,
                "eurCents" to request.eurCents,
                "expiryOracleTime" to request.expiryOracleTime,
                "initialBchPriceInCents" to request.initialBchPriceInCents,
                "minPricePercent" to request.minPricePercent
            )

            val params = JSONObject().apply {
                put("covenantParams", JSONObject(covenantParams))
                put("merchantWIF", merchantWIF)
                put("merchantPubkey", merchantPubkey)
                put("merchantAddress", merchantAddress)
                put("funderAddress", "")  // F4: derived in JS from funderPubkey; empty fails loudly
                put("expectedCovenantAddress", request.covenantAddress)  // F3
                put("oracleSig", JSONObject().apply {
                    put("signature", oracleSig)
                    put("message", oracleMessage)
                    put("price", (currentPrice * 100).toInt())
                })
                put("utxos", utxosJson)
            }

            val jsCode = "buildMerchantPreSignTransaction('${params.toString().replace("'", "\\'")}')"
            covenantWebView.webView.post {
                covenantWebView.webView.evaluateJavascript(jsCode, null)
            }

            continuation.invokeOnCancellation {
                covenantWebView.merchantPreSignListener = null
            }
        }

        val obj = JSONObject(resultJson)
        return PreSignResult(
            txHex = obj.getString("txHex"),
            merchantAddress = obj.getString("merchantAddress"),
            funderAddress = obj.getString("funderAddress")
        )
    }
}
