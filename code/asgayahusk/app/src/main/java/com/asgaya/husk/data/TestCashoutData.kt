package com.asgaya.husk.data

/**
 * Test data for merchant cashout UI development
 *
 * Uses real covenant parameters from existing testnet3 covenant:
 * bchtest:pvel3w5ymcam5y20rasj674s0hv4ef3fpm9p6khywlxcs3p7sh2y7n0qva46s
 *
 * This covenant was created and funded for testing.
 * Amount: €10.00 (1000 eurCents)
 * Initial BCH price: €650/BCH
 * Expected BCH: ~0.0154 BCH
 */
object TestCashoutData {

    /**
     * Real covenant parameters from [COVENANT_V25] message
     *
     * This is the actual covenant we'll use for UI testing.
     * When we wire up the backend, these exact parameters will work.
     */
    fun getTestCashoutRequest(): CashoutRequest {
        return CashoutRequest(
            // Covenant address
            covenantAddress = "bchtest:pvel3w5ymcam5y20rasj674s0hv4ef3fpm9p6khywlxcs3p7sh2y7n0qva46s",

            // Public keys
            senderPubkey = "032774f66536468a06a244568724bf1f8762682be1573ff9910667c0fea1226ece",
            recipientPubkey = "03886b4f4100f9e348caed5155474f0a1a96db3e2706dc59e15260ed3d2b35e148",
            funderPubkey = "032774f66536468a06a244568724bf1f8762682be1573ff9910667c0fea1226ece",
            oraclePubkey = "0387053eebfb132fc6219ada6cb726fc672a15fc9729b36783bf6b057b83f122b0",

            // Covenant parameters
            eurCents = 1000,  // €10.00
            expiryOracleTime = 1787625837,  // 2026-08-25 04:43:57 UTC
            initialBchPriceInCents = 65000,  // €650/BCH
            minPricePercent = 93,  // 93% = €604.50/BCH minimum

            // Merchant cashout specific (added for merchantCashout path)
            merchantPubkey = "03d6e1b33780e34dff1c6dc705c626deed385e827e762abfc596764be0ca3e4f45",  // Merchant (Bob) from WalletManager.seedTestWallets()

            // Signatures (placeholders for UI phase, will be real in Phase 2)
            recipientSig = "PLACEHOLDER_RECIPIENT_SIG_UI_TESTING",  // TODO: Generate real sig in Phase 2
            oracleSig = "PLACEHOLDER_ORACLE_SIG_UI_TESTING",        // TODO: Fetch from oracle in Phase 2
            oracleMessage = "PLACEHOLDER_ORACLE_MESSAGE_UI_TESTING", // TODO: Fetch from oracle in Phase 2

            // UI state
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * Multiple test requests for UI testing (different amounts)
     */
    fun getMultipleTestRequests(): List<CashoutRequest> {
        val base = getTestCashoutRequest()

        return listOf(
            // €10.00 request (the real one)
            base,

            // €50.00 request (5x the real one, for UI testing different amounts)
            base.copy(
                id = java.util.UUID.randomUUID().toString(),
                eurCents = 5000,  // €50.00
                timestamp = System.currentTimeMillis() - 300000  // 5 min ago
            ),

            // €100.00 request (10x the real one)
            base.copy(
                id = java.util.UUID.randomUUID().toString(),
                eurCents = 10000,  // €100.00
                timestamp = System.currentTimeMillis() - 600000  // 10 min ago
            )
        )
    }

    /**
     * Computed display values from real covenant
     */
    object DisplayValues {
        const val AMOUNT_FIAT = 10.0  // €10.00 (1000 eurCents / 100)
        const val AMOUNT_BCH = 0.0154  // ~0.0154 BCH (1000 / 65000)
        const val INITIAL_PRICE = 650.0  // €650/BCH
        const val PRICE_FLOOR = 604.5  // €604.50/BCH (650 * 0.93)
        const val ADDRESS_PREVIEW = "bchtest:pvel3w...7n0qva46s"
        const val EXPIRES_UTC = "2026-08-25 04:43:57 UTC"
    }

    /**
     * Test verification result (all checks pass for UI testing)
     *
     * In Phase 2, we'll replace this with real verification logic.
     */
    fun getTestVerificationResult(): VerificationResult {
        return VerificationResult(
            utxoExists = true,      // Covenant is funded on testnet3
            utxoUnspent = true,     // Not claimed/refunded yet
            amountCorrect = true,   // UTXO value matches eurCents
            notExpired = true,      // Current time < expiryOracleTime
            priceOk = true,         // Current BCH price >= price floor
            oracleSigValid = true,  // Oracle signature verifies
            oracleFresh = true,     // Oracle message < 1 minute old
            output0IsMine = true    // merchantPubkey matches my wallet
        )
    }
}

/**
 * CashoutRequest data class (same as in IMPLEMENTATION-SPEC-UI-FIRST.md)
 */
data class CashoutRequest(
    val id: String = java.util.UUID.randomUUID().toString(),

    // Covenant parameters
    val covenantAddress: String,
    val senderPubkey: String,
    val recipientPubkey: String,
    val funderPubkey: String,
    val oraclePubkey: String,
    val eurCents: Int,
    val expiryOracleTime: Long,
    val initialBchPriceInCents: Int,
    val minPricePercent: Int,

    // Merchant cashout specific
    val merchantPubkey: String,
    val recipientSig: String,
    val oracleSig: String,
    val oracleMessage: String,

    // UI state
    val timestamp: Long = System.currentTimeMillis(),
    val status: CashoutStatus = CashoutStatus.PENDING
) {
    // Computed display values
    val amountFiat: Double get() = eurCents / 100.0

    val amountBCH: Double get() {
        // TightDS Finding 1: Use oracle message price (fresh), not initial price
        val oraclePriceCents = parseOraclePriceFromMessage(oracleMessage)
        return if (oraclePriceCents > 0) {
            eurCents.toDouble() / oraclePriceCents
        } else {
            // Fallback to initial price if oracle message not parsed
            eurCents.toDouble() / initialBchPriceInCents
        }
    }

    val addressPreview: String get() =
        "${covenantAddress.take(14)}...${covenantAddress.takeLast(4)}"

    /**
     * Parse oracle price from 8-byte hex message
     * Format: [timestamp:4][price_in_cents:4] (little-endian)
     */
    private fun parseOraclePriceFromMessage(message: String): Int {
        if (message.length != 32) return 0 // Must be 16 hex bytes (8 bytes * 2)

        try {
            // Extract price bytes (bytes 8-15 of hex string = bytes 4-7 of data)
            val priceHex = message.substring(16, 24) // "e8fd0000"

            // Parse little-endian Int32
            val bytes = priceHex.chunked(2).map { it.toInt(16).toByte() }
            val price = bytes[0].toInt() and 0xFF or
                       ((bytes[1].toInt() and 0xFF) shl 8) or
                       ((bytes[2].toInt() and 0xFF) shl 16) or
                       ((bytes[3].toInt() and 0xFF) shl 24)

            return price
        } catch (e: Exception) {
            return 0
        }
    }
}

enum class CashoutStatus {
    PENDING,      // Just received
    VERIFIED,     // Passed 8-step check
    BROADCASTING, // Transaction sending
    CONFIRMED,    // On-chain confirmed
    COMPLETED     // Cash given
}

/**
 * VerificationResult data class
 */
data class VerificationResult(
    val utxoExists: Boolean,
    val utxoUnspent: Boolean,
    val amountCorrect: Boolean,
    val notExpired: Boolean,
    val priceOk: Boolean,
    val oracleSigValid: Boolean,
    val oracleFresh: Boolean,
    val output0IsMine: Boolean,
    val oracleAge: Int = -1  // Oracle age in seconds (-1 if unknown)
) {
    val allPassed: Boolean = utxoExists && utxoUnspent &&
        amountCorrect && notExpired && priceOk &&
        oracleSigValid && oracleFresh && output0IsMine
}

/**
 * SimpleCashoutRequest - Merchant-First Flow Step 1
 *
 * Recipient → Merchant: Simple covenant parameters only (no signatures!)
 * This is what the recipient shows to the merchant at the counter.
 */
data class SimpleCashoutRequest(
    val covenantAddress: String,
    val senderPubkey: String,
    val recipientPubkey: String,
    val funderPubkey: String,
    val oraclePubkey: String,
    val eurCents: Int,
    val expiryOracleTime: Long,
    val initialBchPriceInCents: Int,
    val minPricePercent: Int,
    val assetCurrency: String? = null,  // the merchant's ad asset = the exchange currency (`ui-rethink/40`)
    // Self-reported (untrusted): used only for the merchant's freshness gate, never for security.
    val timestamp: Long = System.currentTimeMillis()
) {
    // Computed display values
    val amountFiat: Double get() = eurCents / 100.0

    val estimatedBCH: Double get() = eurCents.toDouble() / initialBchPriceInCents

    val addressPreview: String get() =
        "${covenantAddress.take(14)}...${covenantAddress.takeLast(4)}"

    /**
     * Generate [BCH_FOR_SALE] message for QR code
     */
    fun toMessage(): String {
        return """
            [BCH_FOR_SALE]
            covenantAddress=$covenantAddress
            senderPubkey=$senderPubkey
            recipientPubkey=$recipientPubkey
            funderPubkey=$funderPubkey
            oraclePubkey=$oraclePubkey
            eurCents=$eurCents
            expiryOracleTime=$expiryOracleTime
            initialBchPriceInCents=$initialBchPriceInCents
            minPricePercent=$minPricePercent
            assetCurrency=${assetCurrency ?: ""}
            timestamp=$timestamp
            [/BCH_FOR_SALE]
        """.trimIndent()
    }

    companion object {
        /**
         * Parse [BCH_FOR_SALE] message from QR code
         */
        fun fromMessage(message: String): SimpleCashoutRequest? {
            if (!message.contains("[BCH_FOR_SALE]")) return null

            val params = mutableMapOf<String, String>()
            message.lines().forEach { line ->
                val trimmed = line.trim()
                if (trimmed.contains("=")) {
                    val (key, value) = trimmed.split("=", limit = 2)
                    params[key.trim()] = value.trim()
                }
            }

            return try {
                SimpleCashoutRequest(
                    covenantAddress = params["covenantAddress"] ?: return null,
                    senderPubkey = params["senderPubkey"] ?: return null,
                    recipientPubkey = params["recipientPubkey"] ?: return null,
                    funderPubkey = params["funderPubkey"] ?: return null,
                    oraclePubkey = params["oraclePubkey"] ?: return null,
                    eurCents = params["eurCents"]?.toIntOrNull() ?: return null,
                    expiryOracleTime = params["expiryOracleTime"]?.toLongOrNull() ?: return null,
                    initialBchPriceInCents = params["initialBchPriceInCents"]?.toIntOrNull() ?: return null,
                    minPricePercent = params["minPricePercent"]?.toIntOrNull() ?: return null,
                    assetCurrency = params["assetCurrency"]?.takeIf { it.isNotBlank() },
                    timestamp = params["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

/**
 * MerchantCosignResponse - Merchant-First Flow Step 2
 *
 * Merchant → Recipient: Pre-signed transaction + oracle + metadata
 * This is what the merchant shows to the recipient for co-signing.
 */
data class MerchantCosignResponse(
    val transactionHex: String,           // Full partial transaction hex (TightDS Amendment 3)
    val covenantAddress: String,          // Correlates the quote to the covenant (routing)
    val merchantPubkey: String,           // Merchant provides own pubkey
    val merchantAddress: String,          // Merchant's receive address (output 0)
    val funderAddress: String,            // Funder's remainder address (output 1)
    val oracleSig: String,                // Fresh oracle signature
    val oracleMessage: String,            // Fresh oracle message (timestamp + price)
    val currentPrice: Double,             // For recipient validation (TightDS Amendment 1)
    val amountCash: Double,               // cash amount for display, in `currency`
    val amountBCH: Double,                // BCH amount for display
    val currency: String? = null,         // the exchange currency (the merchant's ad asset, `ui-rethink/40`)
    val recipientSigIndex: Int = 0,       // Where to insert recipient's sig in unlocking script
    // Quote handshake (file 55): unique id + short TTL; recipient rejects if now > expiresAt,
    // merchant refuses to broadcast a signed tx whose quoteId != the live quote.
    val quoteId: String = java.util.UUID.randomUUID().toString(),
    val expiresAt: Long = 0L
) {
    /**
     * Generate [BCH_PURCHASE_COSIGN] message for QR code
     */
    fun toMessage(): String {
        return """
            [BCH_PURCHASE_COSIGN]
            covenantAddress=$covenantAddress
            transactionHex=$transactionHex
            merchantPubkey=$merchantPubkey
            merchantAddress=$merchantAddress
            funderAddress=$funderAddress
            oracleSig=$oracleSig
            oracleMessage=$oracleMessage
            currentPrice=$currentPrice
            amountCash=$amountCash
            amountBCH=$amountBCH
            currency=${currency ?: ""}
            recipientSigIndex=$recipientSigIndex
            quoteId=$quoteId
            expiresAt=$expiresAt
            [/BCH_PURCHASE_COSIGN]
        """.trimIndent()
    }

    companion object {
        /**
         * Parse [BCH_PURCHASE_COSIGN] message from QR code
         */
        fun fromMessage(message: String): MerchantCosignResponse? {
            if (!message.contains("[BCH_PURCHASE_COSIGN]")) return null

            val params = mutableMapOf<String, String>()
            message.lines().forEach { line ->
                val trimmed = line.trim()
                if (trimmed.contains("=")) {
                    val (key, value) = trimmed.split("=", limit = 2)
                    params[key.trim()] = value.trim()
                }
            }

            return try {
                MerchantCosignResponse(
                    transactionHex = params["transactionHex"] ?: return null,
                    covenantAddress = params["covenantAddress"] ?: return null,
                    merchantPubkey = params["merchantPubkey"] ?: return null,
                    merchantAddress = params["merchantAddress"] ?: return null,
                    funderAddress = params["funderAddress"] ?: return null,
                    oracleSig = params["oracleSig"] ?: return null,
                    oracleMessage = params["oracleMessage"] ?: return null,
                    currentPrice = params["currentPrice"]?.toDoubleOrNull() ?: return null,
                    amountCash = params["amountCash"]?.toDoubleOrNull() ?: return null,
                    amountBCH = params["amountBCH"]?.toDoubleOrNull() ?: return null,
                    currency = params["currency"]?.takeIf { it.isNotBlank() },
                    recipientSigIndex = params["recipientSigIndex"]?.toIntOrNull() ?: 0,
                    quoteId = params["quoteId"] ?: "",
                    expiresAt = params["expiresAt"]?.toLongOrNull() ?: 0L
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
