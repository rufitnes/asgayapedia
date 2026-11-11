package com.asgaya.husk

import android.util.Log
import com.asgaya.husk.utils.CashAddressHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.security.MessageDigest
import javax.net.ssl.SSLSocketFactory

class ElectrumClient {

    companion object {
        private const val TAG = "ElectrumClient"

        data class ElectrumServer(val host: String, val port: Int, val useSSL: Boolean)

        // Testnet3 Electrum servers — the local Fulcrum first, then a PUBLIC testnet3 Fulcrum.
        // `testnet.bitcoincash.network` (timeout) and `electroncash.de` (refused) were dead and
        // only slowed the failover (2026-09-27). blackie.c3-soft.com is a real testnet3 Fulcrum
        // (verified: it returns the covenant's UTXO).
        private val TESTNET_SERVERS = listOf(
            ElectrumServer("localhost", 60001, false),         // Pi-chan Fulcrum (local, TCP)
            ElectrumServer("tbch.loping.net", 60001, false),       // public testnet3 Fulcrum (replaces blackie, down)
            ElectrumServer("testnet.imaginary.cash", 50001, false) // public testnet3 Fulcrum
        )

        // Read/write timeout per request
        private const val TIMEOUT_MS = 10000
        // TCP connect timeout. Bounds the connect() phase itself (not just read/write).
        // Fixes balance hang when a server is unreachable (e.g. Pi-chan on cell data).
        private const val CONNECT_TIMEOUT_MS = 5000
        private var requestId = 0
    }

    /**
     * Create a socket (plain TCP or SSL) with a bounded connect timeout.
     *
     * The default Socket(host, port) connect can hang for 30-60s+ on an unroutable
     * address (e.g. LAN-only Pi-chan reached from cell data). This bounds connect()
     * so the server-fallback loop can advance quickly.
     */
    private fun createSocket(host: String, port: Int, useSSL: Boolean): Socket {
        val socket = Socket()
        socket.connect(InetSocketAddress(host, port), CONNECT_TIMEOUT_MS)
        return if (useSSL) {
            val sslFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
            sslFactory.createSocket(socket, host, port, true)
        } else {
            socket
        }
    }

    /**
     * Query balance for a BCH address on testnet
     */
    suspend fun getBalance(address: String): Double = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Querying balance for: $address")

            // Convert address to scripthash (Electrum format)
            val scriptHash = addressToScriptHash(address)
            Log.d(TAG, "ScriptHash: $scriptHash")

            // Try each server until one succeeds
            for (server in TESTNET_SERVERS) {
                try {
                    val balance = queryBalanceFromServer(server.host, server.port, server.useSSL, scriptHash)
                    Log.d(TAG, "✅ Balance from ${server.host}: $balance BCH")
                    return@withContext balance
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to query ${server.host}: ${e.message}")
                    continue
                }
            }

            Log.e(TAG, "All Electrum servers failed")
            throw Exception("Could not connect to any Electrum server")

        } catch (e: Exception) {
            Log.e(TAG, "Balance query failed", e)
            throw e
        }
    }

    /**
     * Query balance from specific Electrum server
     */
    private fun queryBalanceFromServer(host: String, port: Int, useSSL: Boolean, scriptHash: String): Double {
        var socket: Socket? = null
        try {
            Log.d(TAG, "Connecting to $host:$port (${if (useSSL) "SSL" else "TCP"})...")

            // Create socket (SSL or plain TCP) with bounded connect timeout
            socket = createSocket(host, port, useSSL)
            socket.soTimeout = TIMEOUT_MS

            val writer = PrintWriter(socket.getOutputStream(), true)
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

            // Build JSON-RPC request
            val id = ++requestId
            val request = JSONObject().apply {
                put("id", id)
                put("method", "blockchain.scripthash.get_balance")
                put("params", org.json.JSONArray().put(scriptHash))
            }

            Log.d(TAG, "Sending request: $request")
            writer.println(request.toString())

            // Read response
            val response = reader.readLine()
            Log.d(TAG, "Received response: $response")

            if (response == null) {
                throw Exception("No response from server")
            }

            val jsonResponse = JSONObject(response)

            // Check for errors
            if (jsonResponse.has("error")) {
                val error = jsonResponse.getJSONObject("error")
                throw Exception("Electrum error: ${error.getString("message")}")
            }

            // Log full response for debugging
            Log.d(TAG, "Full Electrum response: $jsonResponse")

            // Parse balance
            val result = jsonResponse.getJSONObject("result")
            val confirmed = result.getLong("confirmed")
            val unconfirmed = result.getLong("unconfirmed")

            // Convert satoshis to BCH (1 BCH = 100,000,000 satoshis)
            val totalSatoshis = confirmed + unconfirmed
            val balanceBch = totalSatoshis / 100_000_000.0

            Log.d(TAG, "Confirmed: $confirmed sat, Unconfirmed: $unconfirmed sat, Total: $balanceBch BCH")

            return balanceBch

        } finally {
            socket?.close()
        }
    }

    /**
     * Convert BCH address to Electrum scripthash format
     *
     * Uses proper CashAddr decoding via CashAddressHelper
     */
    private fun addressToScriptHash(address: String): String {
        try {
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "Computing scripthash for address: $address")

            // Determine network prefix (testnet or mainnet)
            val hrp = if (address.startsWith("bchtest:")) "bchtest" else "bitcoincash"
            val helper = CashAddressHelper(hrp)

            // Decode address to get hash and type
            val (hash, isP2PKH) = helper.decode(address)
            Log.d(TAG, "Decoded hash: ${hash.joinToString("") { "%02x".format(it) }}")
            Log.d(TAG, "Address type: ${if (isP2PKH) "P2PKH" else "P2SH"} (${hash.size} bytes)")

            // Build scriptPubKey based on address type
            val scriptPubKey = if (isP2PKH) {
                // P2PKH: OP_DUP OP_HASH160 <20-byte-hash> OP_EQUALVERIFY OP_CHECKSIG
                byteArrayOf(
                    0x76.toByte(),  // OP_DUP
                    0xa9.toByte(),  // OP_HASH160
                    0x14.toByte()   // Push 20 bytes
                ) + hash + byteArrayOf(
                    0x88.toByte(),  // OP_EQUALVERIFY
                    0xac.toByte()   // OP_CHECKSIG
                )
            } else if (hash.size == 32) {
                // P2SH32 (covenant): OP_HASH256 <32-byte-hash> OP_EQUAL
                byteArrayOf(
                    0xaa.toByte(),  // OP_HASH256
                    0x20.toByte()   // Push 32 bytes
                ) + hash + byteArrayOf(
                    0x87.toByte()   // OP_EQUAL
                )
            } else if (hash.size == 20) {
                // P2SH20 (standard): OP_HASH160 <20-byte-hash> OP_EQUAL
                byteArrayOf(
                    0xa9.toByte(),  // OP_HASH160
                    0x14.toByte()   // Push 20 bytes
                ) + hash + byteArrayOf(
                    0x87.toByte()   // OP_EQUAL
                )
            } else {
                throw IllegalArgumentException("Unsupported hash size: ${hash.size} bytes")
            }
            Log.d(TAG, "ScriptPubKey: ${scriptPubKey.joinToString("") { "%02x".format(it) }}")

            // Calculate SHA256(scriptPubKey) - Electrum uses SINGLE SHA256, not double!
            val scriptHash = MessageDigest.getInstance("SHA-256").digest(scriptPubKey)

            // Reverse bytes (Electrum format)
            val reversed = scriptHash.reversedArray()

            // Convert to hex string
            val scripthash = reversed.joinToString("") { "%02x".format(it) }
            Log.d(TAG, "Scripthash: $scripthash")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            Log.d(TAG, "Address decoded: ${address.take(30)}...")
            Log.d(TAG, "  Hash: ${hash.joinToString("") { "%02x".format(it) }}")
            Log.d(TAG, "  ScriptHash: $scripthash")

            return scripthash

        } catch (e: Exception) {
            Log.e(TAG, "Failed to convert address to scripthash: ${e.message}", e)
            throw IllegalArgumentException("Invalid BCH address: $address", e)
        }
    }

    /**
     * Query balance directly by scripthash (for testing)
     */
    suspend fun getBalanceByScriptHash(scriptHash: String): Double = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Querying balance for scripthash: $scriptHash")

            // Try each server until one succeeds
            for (server in TESTNET_SERVERS) {
                try {
                    val balance = queryBalanceFromServer(server.host, server.port, server.useSSL, scriptHash)
                    Log.d(TAG, "✅ Balance from ${server.host}: $balance BCH")
                    return@withContext balance
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to query ${server.host}: ${e.message}")
                    continue
                }
            }

            Log.e(TAG, "All Electrum servers failed")
            0.0

        } catch (e: Exception) {
            Log.e(TAG, "Balance query failed", e)
            0.0
        }
    }

    /**
     * Generate test address for sender
     * In production, this would derive from user's seed phrase
     */
    fun getTestAddress(): String {
        // For testing, return a hardcoded testnet address
        // In production, derive from HD wallet seed
        return "bchtest:qr4aadjrpu8r8w9e85g0j5pzzm5mhfmhfqgflj0skl"
    }

    /**
     * Get scripthash for test address
     * This is a placeholder - replace with proper address parsing
     */
    fun getTestScriptHash(): String {
        // This is the scripthash for the test address above
        // In production, compute from address
        // For now, return empty to trigger manual entry
        return ""
    }

    /**
     * Get list of unspent outputs (UTXOs) for an address
     */
    suspend fun getUTXOs(scriptHash: String): List<UTXO> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Querying UTXOs for scripthash: $scriptHash")

            for (server in TESTNET_SERVERS) {
                try {
                    val utxos = queryUTXOsFromServer(server.host, server.port, server.useSSL, scriptHash)
                    Log.d(TAG, "✅ Found ${utxos.size} UTXOs from ${server.host}")
                    return@withContext utxos
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to query ${server.host}: ${e.message}")
                    continue
                }
            }

            // A total failure is NOT "no UTXOs": throw so callers can tell them apart (2026-09-27).
            Log.e(TAG, "All Electrum servers failed")
            throw Exception("All Electrum servers failed")

        } catch (e: Exception) {
            Log.e(TAG, "UTXO query failed", e)
            throw e
        }
    }

    /**
     * Get list of unspent outputs (UTXOs) for an address (convenience method)
     *
     * Converts address to scripthash and calls getUTXOs(scriptHash)
     */
    suspend fun getUTXOsForAddress(address: String): List<UTXO> {
        val scriptHash = addressToScriptHash(address)
        return getUTXOs(scriptHash)
    }

    /**
     * Is the exact funding outpoint (`txid:vout`) still unspent? `UTXO` = present,
     * `null` = spent/unknown (positive evidence that the output is gone), **throws** if every
     * server fails — so the caller retries instead of concluding "spent" (`ui-rethink/16` P0-1).
     */
    suspend fun getOutpointInfo(txid: String, vout: Int): UTXO? = withContext(Dispatchers.IO) {
        for (server in TESTNET_SERVERS) {
            try {
                return@withContext queryOutpointFromServer(server.host, server.port, server.useSSL, txid, vout)
            } catch (e: Exception) {
                Log.w(TAG, "get_info failed on ${server.host}: ${e.message}")
                continue
            }
        }
        // A total failure is NOT "spent": throw so callers can tell them apart.
        Log.e(TAG, "All Electrum servers failed (get_info)")
        throw Exception("All Electrum servers failed")
    }

    data class HistoryEntry(val txid: String, val height: Int)
    data class TxInput(val prevTxid: String, val prevVout: Int)
    data class TxOutput(val address: String?, val valueSats: Long, val type: String)
    data class VerboseTransaction(val txid: String, val inputs: List<TxInput>, val outputs: List<TxOutput>)

    /**
     * Run a raw Electrum call, trying each server; throws if all fail (`ui-rethink/16` P1).
     * Returns the raw `result` (may be null).
     */
    private fun callElectrum(method: String, params: org.json.JSONArray): Any? {
        for (server in TESTNET_SERVERS) {
            try {
                return callElectrumOnServer(server.host, server.port, server.useSSL, method, params)
            } catch (e: Exception) {
                Log.w(TAG, "$method failed on ${server.host}: ${e.message}")
                continue
            }
        }
        throw Exception("All Electrum servers failed ($method)")
    }

    private fun callElectrumOnServer(
        host: String, port: Int, useSSL: Boolean, method: String, params: org.json.JSONArray
    ): Any? {
        var socket: Socket? = null
        try {
            socket = createSocket(host, port, useSSL)
            socket.soTimeout = TIMEOUT_MS
            val writer = PrintWriter(socket.getOutputStream(), true)
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val id = ++requestId
            val request = JSONObject().apply {
                put("id", id); put("method", method); put("params", params)
            }
            writer.println(request.toString())
            val response = reader.readLine() ?: throw Exception("No response from server")
            val json = JSONObject(response)
            if (json.has("error")) {
                throw Exception("Electrum error: ${json.getJSONObject("error").getString("message")}")
            }
            if (json.isNull("result")) return null
            return json.get("result")
        } finally {
            socket?.close()
        }
    }

    /** Tx history of an address (funding + spending), to find how a covenant resolved (`58`). */
    suspend fun getHistory(address: String): List<HistoryEntry> = withContext(Dispatchers.IO) {
        val sh = addressToScriptHash(address)
        val arr = callElectrum(
            "blockchain.scripthash.get_history", org.json.JSONArray().put(sh)
        ) as? org.json.JSONArray ?: return@withContext emptyList()
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            HistoryEntry(o.getString("tx_hash"), o.optInt("height", 0))
        }
    }

    /** A transaction's inputs (prevout refs) and outputs (address/value), for the classifier. */
    suspend fun getVerboseTransaction(txid: String): VerboseTransaction? = withContext(Dispatchers.IO) {
        val o = callElectrum(
            "blockchain.transaction.get", org.json.JSONArray().put(txid).put(true)
        ) as? JSONObject ?: return@withContext null

        val inputs = mutableListOf<TxInput>()
        o.optJSONArray("vin")?.let { vins ->
            for (i in 0 until vins.length()) {
                val v = vins.getJSONObject(i)
                if (!v.has("txid")) continue  // coinbase
                inputs.add(TxInput(v.getString("txid"), v.optInt("vout", -1)))
            }
        }
        val outputs = mutableListOf<TxOutput>()
        o.optJSONArray("vout")?.let { vouts ->
            for (i in 0 until vouts.length()) {
                val v = vouts.getJSONObject(i)
                val spk = v.optJSONObject("scriptPubKey")
                val addr = spk?.optJSONArray("addresses")?.optString(0)?.takeIf { it.isNotBlank() }
                outputs.add(TxOutput(addr, (v.optDouble("value", 0.0) * 1e8).toLong(), spk?.optString("type") ?: ""))
            }
        }
        VerboseTransaction(txid, inputs, outputs)
    }

    private fun queryOutpointFromServer(host: String, port: Int, useSSL: Boolean, txid: String, vout: Int): UTXO? {
        var socket: Socket? = null
        try {
            socket = createSocket(host, port, useSSL)
            socket.soTimeout = TIMEOUT_MS

            val writer = PrintWriter(socket.getOutputStream(), true)
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

            val id = ++requestId
            val request = JSONObject().apply {
                put("id", id)
                put("method", "blockchain.utxo.get_info")
                put("params", org.json.JSONArray().put(txid).put(vout))
            }

            writer.println(request.toString())
            val response = reader.readLine() ?: throw Exception("No response from server")
            val jsonResponse = JSONObject(response)

            if (jsonResponse.has("error")) {
                val error = jsonResponse.getJSONObject("error")
                throw Exception("Electrum error: ${error.getString("message")}")
            }
            if (jsonResponse.isNull("result")) return null  // spent / not found

            val r = jsonResponse.getJSONObject("result")
            return UTXO(
                txHash = r.optString("tx_hash", txid),
                txPos = r.optInt("tx_pos", vout),
                height = r.optInt("height", 0),
                value = r.optLong("value", 0)
            )
        } finally {
            socket?.close()
        }
    }

    private fun queryUTXOsFromServer(host: String, port: Int, useSSL: Boolean, scriptHash: String): List<UTXO> {
        var socket: Socket? = null
        try {
            socket = createSocket(host, port, useSSL)
            socket.soTimeout = TIMEOUT_MS

            val writer = PrintWriter(socket.getOutputStream(), true)
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

            val id = ++requestId
            val request = JSONObject().apply {
                put("id", id)
                put("method", "blockchain.scripthash.listunspent")
                put("params", org.json.JSONArray().put(scriptHash))
            }

            Log.d(TAG, "Sending UTXO request: $request")
            writer.println(request.toString())

            val response = reader.readLine()
            Log.d(TAG, "Received UTXO response: $response")

            if (response == null) {
                throw Exception("No response from server")
            }

            val jsonResponse = JSONObject(response)

            if (jsonResponse.has("error")) {
                val error = jsonResponse.getJSONObject("error")
                throw Exception("Electrum error: ${error.getString("message")}")
            }

            val result = jsonResponse.getJSONArray("result")
            val utxos = mutableListOf<UTXO>()

            for (i in 0 until result.length()) {
                val utxo = result.getJSONObject(i)
                utxos.add(UTXO(
                    txHash = utxo.getString("tx_hash"),
                    txPos = utxo.getInt("tx_pos"),
                    height = utxo.getInt("height"),
                    value = utxo.getLong("value")
                ))
            }

            return utxos

        } finally {
            socket?.close()
        }
    }

    /**
     * Broadcast a signed transaction to the network
     */
    suspend fun broadcast(txHex: String): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Broadcasting transaction: ${txHex.take(100)}...")

            for (server in TESTNET_SERVERS) {
                try {
                    val txid = broadcastToServer(server.host, server.port, server.useSSL, txHex)
                    Log.d(TAG, "✅ Transaction broadcast to ${server.host}: $txid")
                    return@withContext txid
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to broadcast to ${server.host}: ${e.message}")
                    continue
                }
            }

            Log.e(TAG, "All Electrum servers failed")
            throw Exception("Could not broadcast to any Electrum server")

        } catch (e: Exception) {
            Log.e(TAG, "Broadcast failed", e)
            throw e
        }
    }

    private fun broadcastToServer(host: String, port: Int, useSSL: Boolean, txHex: String): String {
        var socket: Socket? = null
        try {
            socket = createSocket(host, port, useSSL)
            socket.soTimeout = TIMEOUT_MS

            val writer = PrintWriter(socket.getOutputStream(), true)
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

            val id = ++requestId
            val request = JSONObject().apply {
                put("id", id)
                put("method", "blockchain.transaction.broadcast")
                put("params", org.json.JSONArray().put(txHex))
            }

            Log.d(TAG, "Sending broadcast request")
            writer.println(request.toString())

            val response = reader.readLine()
            Log.d(TAG, "Received broadcast response: $response")

            if (response == null) {
                throw Exception("No response from server")
            }

            val jsonResponse = JSONObject(response)

            if (jsonResponse.has("error")) {
                val error = jsonResponse.getJSONObject("error")
                throw Exception("Electrum error: ${error.getString("message")}")
            }

            // Result is the transaction ID
            return jsonResponse.getString("result")

        } finally {
            socket?.close()
        }
    }

    /**
     * Get transaction details from Electrum
     *
     * Queries blockchain.transaction.get to check if a transaction exists
     * and get its confirmation status.
     *
     * @param txid Transaction ID to query
     * @return TransactionInfo if found, null if not found
     */
    suspend fun getTransaction(txid: String): TransactionInfo? = withContext(Dispatchers.IO) {
        Log.d(TAG, "Querying transaction: $txid")
        var answered = false

        // Try each server. A server that answers "not found" is a valid negative (a later server
        // may still have it); a server that FAILS is not. If every server fails we THROW, so a
        // caller never reads a read-failure as "not found" (`ui-rethink/16` P0-3).
        for (server in TESTNET_SERVERS) {
            try {
                val txInfo = getTransactionFromServer(server.host, server.port, server.useSSL, txid)
                answered = true
                if (txInfo != null) {
                    Log.d(TAG, "✅ Transaction found on ${server.host}: confirmations=${txInfo.confirmations}")
                    return@withContext txInfo
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to query ${server.host}: ${e.message}")
                continue
            }
        }

        if (!answered) {
            Log.e(TAG, "All Electrum servers failed (transaction.get)")
            throw Exception("All Electrum servers failed")
        }
        Log.d(TAG, "Transaction not found on any server: $txid")
        null
    }

    private fun getTransactionFromServer(host: String, port: Int, useSSL: Boolean, txid: String): TransactionInfo? {
        var socket: Socket? = null
        try {
            socket = createSocket(host, port, useSSL)
            socket.soTimeout = TIMEOUT_MS

            val writer = PrintWriter(socket.getOutputStream(), true)
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

            // Request transaction details with verbose=true to get block info
            val id = ++requestId
            val request = JSONObject().apply {
                put("id", id)
                put("method", "blockchain.transaction.get")
                put("params", org.json.JSONArray().put(txid).put(true)) // verbose=true
            }

            writer.println(request.toString())
            val response = reader.readLine()

            if (response == null) {
                // No answer is a FAILURE, not "not found" — propagate so callers can retry.
                throw Exception("No response from server")
            }

            val jsonResponse = JSONObject(response)

            if (jsonResponse.has("error")) {
                // A missing transaction comes back as an Electrum error → a valid "not found".
                return null
            }

            val result = jsonResponse.getJSONObject("result")

            // Get block height and confirmations
            val confirmations = result.optInt("confirmations", 0)
            val blockHeight = if (result.has("blockhash") && !result.isNull("blockhash")) {
                // If in a block, calculate height from confirmations
                // Note: We'd need current block height to calculate exact height
                // For now, just use confirmations > 0 as indicator
                if (confirmations > 0) 1 else 0
            } else {
                0 // Mempool
            }

            return TransactionInfo(
                txid = txid,
                confirmations = confirmations,
                blockHeight = blockHeight,
                inMempool = confirmations == 0
            )

        } catch (e: Exception) {
            Log.w(TAG, "Error querying transaction from $host: ${e.message}")
            return null
        } finally {
            socket?.close()
        }
    }

    /**
     * Transaction information from Electrum
     */
    data class TransactionInfo(
        val txid: String,
        val confirmations: Int,
        val blockHeight: Int,  // 0 if in mempool, >0 if confirmed
        val inMempool: Boolean
    )

    /**
     * UTXO data class
     */
    data class UTXO(
        val txHash: String,   // Transaction hash containing this output
        val txPos: Int,       // Output index in transaction
        val height: Int,      // Block height (-1 if unconfirmed)
        val value: Long       // Value in satoshis
    )
}
