package com.asgaya.husk

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Transaction Builder for BCH
 *
 * Handles:
 * - UTXO selection
 * - Calls RaspberryPi service for transaction construction and signing
 * - Broadcasting to testnet3
 */
class TransactionBuilder(
    private val electrumClient: ElectrumClient
) {

    companion object {
        private const val TAG = "TransactionBuilder"

        // RaspberryPi transaction service
        private const val PI_SERVICE_URL = "http://localhost:5001"

        // Fee estimation (satoshis per byte)
        private const val FEE_RATE = 1L  // 1 sat/byte (testnet3 standard)

        // Transaction size estimates
        private const val TX_OVERHEAD = 10  // Version + locktime + counts
        private const val INPUT_SIZE = 148  // Typical P2PKH input
        private const val OUTPUT_SIZE = 34  // Typical P2PKH/P2SH output
    }

    /**
     * Fund a covenant by sending BCH to covenant address
     *
     * @param sellerWif Funder's private key in WIF format
     * @param sellerScriptHash Funder's address scripthash (for UTXO query)
     * @param covenantAddress Covenant P2SH32 address to fund
     * @param eurCents Payment amount in EUR cents
     * @param bchPriceInCents Current BCH price in EUR cents
     * @param changeAddress Funder's change address
     * @return Transaction ID
     */
    suspend fun fundCovenant(
        sellerWif: String,
        sellerScriptHash: String,
        covenantAddress: String,
        eurCents: Int,
        bchPriceInCents: Int,
        changeAddress: String
    ): String {
        try {
            Log.d(TAG, "=== Funding Covenant ===")
            Log.d(TAG, "Amount: €${eurCents / 100.0}")
            Log.d(TAG, "BCH Price: €${bchPriceInCents / 100.0}/BCH")
            Log.d(TAG, "Covenant: ${covenantAddress.take(30)}...")

            // Step 1: Calculate BCH amount needed (EUR amount + 7% buffer)
            val eurWithBuffer = eurCents * 107 / 100  // 107% of payment
            val satoshisNeeded = (eurWithBuffer.toLong() * 100_000_000L) / bchPriceInCents

            Log.d(TAG, "Satoshis needed: $satoshisNeeded (€${eurWithBuffer / 100.0} at current price)")

            // Step 2: Get seller's UTXOs
            val utxos = electrumClient.getUTXOs(sellerScriptHash)
            if (utxos.isEmpty()) {
                throw Exception("No UTXOs available in seller's wallet")
            }

            Log.d(TAG, "Available UTXOs: ${utxos.size}")

            // Step 3: Select UTXOs (simple: take largest first)
            val selectedUTXOs = selectUTXOs(utxos, satoshisNeeded)
            val totalInput = selectedUTXOs.sumOf { it.value }

            Log.d(TAG, "Selected ${selectedUTXOs.size} UTXOs, total: $totalInput sat")

            // Step 4: Calculate fee
            val estimatedSize = estimateTransactionSize(selectedUTXOs.size, 2) // 2 outputs (covenant + change)
            val fee = estimatedSize * FEE_RATE

            Log.d(TAG, "Estimated size: $estimatedSize bytes, fee: $fee sat")

            // Step 5: Calculate change
            val change = totalInput - satoshisNeeded - fee
            if (change < 0) {
                throw Exception("Insufficient funds: need ${satoshisNeeded + fee} sat, have $totalInput sat")
            }

            Log.d(TAG, "Change: $change sat")

            // Step 6-8: Build, sign, and serialize via Pi service
            val txHex = buildAndSignViaPi(
                sellerWif = sellerWif,
                utxos = selectedUTXOs,
                covenantAddress = covenantAddress,
                satoshisToSend = satoshisNeeded,
                changeAddress = changeAddress
            )

            Log.d(TAG, "Signed transaction: ${txHex.length} bytes")

            val txid = electrumClient.broadcast(txHex)
            Log.d(TAG, "✅ Transaction broadcast: $txid")

            return txid

        } catch (e: Exception) {
            Log.e(TAG, "Failed to fund covenant", e)
            throw e
        }
    }

    /**
     * Build and sign transaction via RaspberryPi service
     */
    private suspend fun buildAndSignViaPi(
        sellerWif: String,
        utxos: List<ElectrumClient.UTXO>,
        covenantAddress: String,
        satoshisToSend: Long,
        changeAddress: String
    ): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Calling Pi transaction builder service at $PI_SERVICE_URL")

            // Build request JSON
            val utxosJson = utxos.map { utxo ->
                JSONObject().apply {
                    put("txHash", utxo.txHash)
                    put("txPos", utxo.txPos)
                    put("value", utxo.value)
                }
            }

            val requestBody = JSONObject().apply {
                put("sellerWif", sellerWif)
                put("utxos", JSONArray(utxosJson))
                put("covenantAddress", covenantAddress)
                put("satoshisToSend", satoshisToSend)
                put("changeAddress", changeAddress)
            }

            // Make HTTP request
            val url = java.net.URL("$PI_SERVICE_URL/build-transaction")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            // Send request
            connection.outputStream.use { os ->
                os.write(requestBody.toString().toByteArray())
            }

            // Read response
            val responseCode = connection.responseCode
            val responseBody = if (responseCode == 200) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }
                    ?: "Unknown error"
            }

            if (responseCode != 200) {
                throw Exception("Pi service error ($responseCode): $responseBody")
            }

            // Parse response
            val response = JSONObject(responseBody)
            if (!response.getBoolean("success")) {
                throw Exception("Transaction build failed: ${response.getString("error")}")
            }

            val txHex = response.getString("txHex")
            val txid = response.getString("txid")
            val fee = response.getInt("fee")

            Log.d(TAG, "✅ Pi built transaction: TXID=$txid, fee=$fee sat")

            return@withContext txHex

        } catch (e: Exception) {
            Log.e(TAG, "Failed to build transaction via Pi", e)
            throw Exception("Pi transaction builder failed: ${e.message}", e)
        }
    }

    /**
     * Select UTXOs to cover the required amount + fees
     * Strategy: Largest first (simple, works for testing)
     */
    private fun selectUTXOs(utxos: List<ElectrumClient.UTXO>, amountNeeded: Long): List<ElectrumClient.UTXO> {
        val sorted = utxos.sortedByDescending { it.value }
        val selected = mutableListOf<ElectrumClient.UTXO>()
        var total = 0L

        for (utxo in sorted) {
            selected.add(utxo)
            total += utxo.value

            // Estimate fee for current selection
            val estimatedSize = estimateTransactionSize(selected.size, 2)
            val estimatedFee = estimatedSize * FEE_RATE

            if (total >= amountNeeded + estimatedFee) {
                break
            }
        }

        if (selected.sumOf { it.value } < amountNeeded) {
            throw Exception("Insufficient funds")
        }

        return selected
    }

    /**
     * Estimate transaction size in bytes
     */
    private fun estimateTransactionSize(numInputs: Int, numOutputs: Int): Long {
        return (TX_OVERHEAD + (numInputs * INPUT_SIZE) + (numOutputs * OUTPUT_SIZE)).toLong()
    }
}
