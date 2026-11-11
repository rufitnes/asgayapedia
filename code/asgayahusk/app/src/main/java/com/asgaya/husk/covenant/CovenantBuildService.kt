package com.asgaya.husk.covenant

import android.util.Log
import com.asgaya.husk.ElectrumClient
import kotlinx.coroutines.withTimeout

/**
 * v0.2 Hybrid Architecture: Kotlin-side network operations
 *
 * This service handles all network operations (UTXO fetch + broadcast)
 * while WebView handles pure computation (CashScript transaction building).
 *
 * Why: JavaScript timers pause when Android WebView goes to background,
 * causing WebSocket connections to hang indefinitely.
 *
 * Solution: Move network to Kotlin (native TCP with OS-level timeouts).
 */
object CovenantBuildService {
    private const val TAG = "CovenantBuildService"
    private const val NETWORK_TIMEOUT_MS = 30000L  // 30 seconds for network ops

    /**
     * Fetch UTXOs for an address using native Kotlin Electrum client
     *
     * This replaces the WebView WebSocket UTXO fetch (which could hang).
     *
     * @param address BCH address (bchtest:... or bitcoincash:...)
     * @return List of UTXOs with enough info for transaction building
     */
    suspend fun fetchUTXOs(address: String): List<UTXO> {
        Log.d(TAG, "📡 Fetching UTXOs for $address")

        val client = ElectrumClient()
        return try {
            withTimeout(NETWORK_TIMEOUT_MS) {
                // ElectrumClient.getUTXOsForAddress() already has:
                // - Address to scripthash conversion
                // - Multi-server fallback
                // - 10-second per-server timeout
                // - Socket cleanup in finally blocks
                val utxos = client.getUTXOsForAddress(address)

                Log.d(TAG, "✅ Found ${utxos.size} UTXOs")

                // Convert ElectrumClient.UTXO to our UTXO format
                utxos.map { utxo ->
                    UTXO(
                        txHash = utxo.txHash,
                        txPos = utxo.txPos,
                        value = utxo.value,
                        height = utxo.height
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ UTXO fetch failed", e)
            throw Exception("Failed to fetch UTXOs: ${e.message}")
        }
    }

    /**
     * Broadcast a signed transaction hex using native Kotlin Electrum client
     *
     * This replaces the WebView WebSocket broadcast (which could hang).
     *
     * @param txHex Raw transaction hex (built and signed by WebView)
     * @return Transaction ID (txid) from successful broadcast
     */
    suspend fun broadcastTransaction(txHex: String): String {
        Log.d(TAG, "📡 Broadcasting transaction (${txHex.length} chars)")

        val client = ElectrumClient()
        return try {
            withTimeout(NETWORK_TIMEOUT_MS) {
                // ElectrumClient.broadcast() already has:
                // - Multi-server fallback (tries 4 servers)
                // - 10-second per-server timeout
                // - Socket cleanup in finally blocks
                val txid = client.broadcast(txHex)

                Log.d(TAG, "✅ Broadcast successful: $txid")
                txid
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Broadcast failed", e)
            throw Exception("Failed to broadcast: ${e.message}")
        }
    }
}

/**
 * UTXO data class for Kotlin ↔ JavaScript interface
 *
 * Matches the format JavaScript expects for transaction building.
 */
data class UTXO(
    val txHash: String,
    val txPos: Int,
    val value: Long,
    val height: Int
)

/**
 * Transaction build parameters passed from Kotlin to JavaScript
 *
 * JavaScript will build+sign the transaction using these UTXOs,
 * then return the txHex back to Kotlin for broadcasting.
 */
data class BuildTransactionParams(
    val senderWIF: String,
    val senderAddress: String,
    val recipientAddress: String,
    val amountSats: Long,
    val utxos: List<UTXO>
)
