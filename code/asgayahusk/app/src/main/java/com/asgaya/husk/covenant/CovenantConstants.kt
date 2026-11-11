package com.asgaya.husk.covenant

/**
 * Covenant v2.5 Constants
 *
 * Static data for manual covenant construction:
 * - Bytecode template (from price-oracle-v2.5.json)
 * - Oracle pubkey
 * - Network parameters
 */
object CovenantConstants {

    /**
     * v2.5 Bytecode Template
     * From: price-oracle-v2.5.json (compiled 2026-07-27)
     *
     * This is the raw contract bytecode WITHOUT constructor parameters.
     * Parameters are pushed onto stack BEFORE this bytecode.
     *
     * Fingerprint: db7c643e5730713b88962d84c83626ecffbaa0e327de25bbe196a412310bc509
     */
    const val BYTECODE_V25_HEX = "5879009c635a7a5b79557abb59798277609d5979587f755a7a587f777c817c817c567a9f69557a567a9501649678a169547a0400e1f505957c96557a5379ad00cc9d00cd0376a914537aa97e0288ac7e8851cd0376a914537aa97e0288ac7e877777675879519c635c7a5d79557abb5b798277609d5b79587f755c7a587f777c817c817c567a9f69557a567a9501649678a169547a0400e1f505957c96557a537aad547a5579ad00cc9d00cd0376a914557aa97e0288ac7e8851cd0376a914537aa97e0288ac7e877777675879529c63597a78ad597a5a79557abb58798277609d587a587f7781547a0400e1f505957c9600cc9d00cd0376a9147ba97e0288ac7e8851cd0376a914537aa97e0288ac7e886d6d755167587a539d587a5379ad587a5979557abb57798277609d5779587f75587a587f777c817c817c567aa269547a0400e1f505957c9600cc9d00cd0376a9147ba97e0288ac7e8851cd0376a914537aa97e0288ac7e87777777686868"

    /**
     * Oracle API Configuration
     *
     * Oracle runs on Pi-chan and provides:
     * - /oracle/info - Get oracle pubkey (no auth needed)
     * - /oracle/price - Get signed price+timestamp
     */
    const val ORACLE_URL = "http://localhost:3001"

    /**
     * Oracle Public Key (Deprecated - fetch from API instead)
     *
     * @deprecated Use fetchOraclePubkey() to get current oracle pubkey from API
     * This hardcoded value is kept only for reference/fallback
     */
    @Deprecated("Use fetchOraclePubkey() instead")
    const val ORACLE_PUBKEY_HEX = "02f2c7e020377863ca973d9b68b20dc40b8da38fcb77536333cde0e886c8400651"

    /**
     * Default Parameters
     */
    object Defaults {
        const val EXPIRY_HOURS = 8 // Covenant expires 8 hours after creation
        const val MIN_PRICE_PERCENT = 93 // Allow 7% price drop max
        const val BUFFER_PERCENT = 7 // Funder adds 7% buffer
    }

    /**
     * Network Parameters (Testnet3)
     */
    object Network {
        const val IS_TESTNET = true
        const val ADDRESS_PREFIX = "bchtest"
        const val P2SH_PREFIX = "p" // P2SH addresses start with 'p'
    }
}
