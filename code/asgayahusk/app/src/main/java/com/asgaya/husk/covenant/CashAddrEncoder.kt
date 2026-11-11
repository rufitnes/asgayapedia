package com.asgaya.husk.covenant

import android.util.Log

/**
 * CashAddr Encoder for Bitcoin Cash Addresses
 *
 * Implements the CashAddr format (BIP-173-like but BCH-specific):
 * - Base32 encoding (custom alphabet)
 * - BCH checksum (polymod algorithm)
 * - P2SH prefix for covenant addresses
 *
 * Reference: https://github.com/bitcoincashorg/bitcoincash.org/blob/master/spec/cashaddr.md
 */
object CashAddrEncoder {

    private const val TAG = "CashAddrEncoder"

    // CashAddr base32 alphabet (lowercase)
    private const val CHARSET = "qpzry9x8gf2tvdw0s3jn54khce6mua7l"

    /**
     * Encode a P2SH32 script hash as CashAddr address
     *
     * Uses P2SH32 format (32-byte hash) to match CashScript v0.13+
     *
     * @param scriptHash 32-byte SHA256 of the redeemScript
     * @param isTestnet true for bchtest:, false for bitcoincash:
     * @return CashAddr formatted address (e.g., "bchtest:pvfn737xgzfea0ujuj2407l84ddys7ywt8...")
     */
    fun encodeP2SH32(scriptHash: ByteArray, isTestnet: Boolean = true): String {
        require(scriptHash.size == 32) { "Script hash must be 32 bytes for P2SH32" }

        val prefix = if (isTestnet) "bchtest" else "bitcoincash"

        // Version byte for P2SH32: [type:5][size:3]
        // Type 1 (P2SH) = 00001
        // Size 3 (32 bytes / 256 bits) = 011
        // Combined: 00001011 = 0x0B
        val versionByte = 0x0B.toByte()

        // Build payload: version + script hash
        val payload = byteArrayOf(versionByte) + scriptHash

        // Convert to 5-bit groups for base32 encoding
        val fiveBitPayload = convertBits(payload, 8, 5, true)

        // Calculate checksum
        val checksum = createChecksum(prefix, fiveBitPayload)

        // Concatenate payload + checksum
        val combined = fiveBitPayload + checksum

        // Encode to base32
        val encoded = combined.map { CHARSET[it.toInt()] }.joinToString("")

        // Return CashAddr (prefix comes from base32 encoding of version byte)
        return "$prefix:$encoded"
    }

    /**
     * Encode a P2SH20 script hash as CashAddr address (legacy format)
     *
     * Uses P2SH20 format (20-byte hash) - standard Bitcoin format
     *
     * @param scriptHash 20-byte HASH160 of the redeemScript
     * @param isTestnet true for bchtest:, false for bitcoincash:
     * @return CashAddr formatted address (e.g., "bchtest:pqq3728yw0y47sqn6l2na30mcw6zm78dzq...")
     */
    fun encodeP2SH20(scriptHash: ByteArray, isTestnet: Boolean = true): String {
        require(scriptHash.size == 20) { "Script hash must be 20 bytes for P2SH20" }

        val prefix = if (isTestnet) "bchtest" else "bitcoincash"

        // Version byte for P2SH20 (type=1, size=0 for 20 bytes)
        val versionByte = 0x08.toByte() // 0001 0000 (P2SH20, 20 bytes)

        // Build payload: version + script hash
        val payload = byteArrayOf(versionByte) + scriptHash

        // Convert to 5-bit groups for base32 encoding
        val fiveBitPayload = convertBits(payload, 8, 5, true)

        // Calculate checksum
        val checksum = createChecksum(prefix, fiveBitPayload)

        // Concatenate payload + checksum
        val combined = fiveBitPayload + checksum

        // Encode to base32
        val encoded = combined.map { CHARSET[it.toInt()] }.joinToString("")

        // Return CashAddr (prefix comes from base32 encoding of version byte)
        return "$prefix:$encoded"
    }

    /**
     * Convert bits from one base to another
     *
     * Used to convert 8-bit bytes to 5-bit groups for base32 encoding
     *
     * FIXED: Use Long to prevent overflow with 33-byte payloads (version + 32-byte hash)
     */
    private fun convertBits(
        data: ByteArray,
        fromBits: Int,
        toBits: Int,
        pad: Boolean
    ): ByteArray {
        var acc = 0L               // Long prevents overflow
        var bits = 0
        val maxv = (1 shl toBits) - 1
        val maxAcc = (1L shl (fromBits + toBits - 1)) - 1  // mask to keep acc small
        val result = mutableListOf<Byte>()

        for (value in data) {
            val v = value.toInt() and 0xFF
            acc = ((acc shl fromBits) or v.toLong()) and maxAcc
            bits += fromBits

            while (bits >= toBits) {
                bits -= toBits
                result.add(((acc shr bits) and maxv.toLong()).toByte())
                // Clear the extracted top bits
                acc = acc and ((1L shl bits) - 1)
            }
        }

        if (pad && bits > 0) {
            result.add(((acc shl (toBits - bits)) and maxv.toLong()).toByte())
        }

        return result.toByteArray()
    }

    /**
     * Create CashAddr checksum using BCH polymod
     *
     * The checksum is calculated over:
     * - Prefix expanded to 5-bit values
     * - Separator (always 0)
     * - Payload in 5-bit groups
     * - 8 zero bytes
     *
     * Returns 8 checksum bytes (5-bit values)
     */
    private fun createChecksum(prefix: String, payload: ByteArray): ByteArray {
        // Expand prefix to 5-bit values (correct: shr 3 for top 5 bits of 8-bit ASCII)
        val prefixValues = prefix.map { (it.code shr 3).toByte() }.toByteArray() +
                          byteArrayOf(0) + // separator
                          prefix.map { (it.code and 0x1f).toByte() }.toByteArray()

        // Calculate polymod over prefix + payload + 8 zeros
        val values = prefixValues + payload + ByteArray(8)
        val polymod = polyMod(values)

        // Extract checksum (8 x 5-bit values from 40-bit polymod result)
        val checksum = ByteArray(8)
        for (i in 0..7) {
            checksum[i] = ((polymod shr (5 * (7 - i))) and 0x1f).toByte()
        }

        return checksum
    }

    /**
     * BCH polymod algorithm for checksum calculation
     *
     * Generator polynomial for BCH code used in CashAddr:
     * 0x98f2bc8e61, 0x79b76d99e2, 0xf33e5fb3c4, 0xae2eabe2a8, 0x1e4f43e470
     */
    private fun polyMod(values: ByteArray): Long {
        var c = 1L
        val generators = longArrayOf(
            0x98f2bc8e61L,
            0x79b76d99e2L,
            0xf33e5fb3c4L,
            0xae2eabe2a8L,
            0x1e4f43e470L
        )

        for (value in values) {
            val c0 = (c shr 35).toInt()
            c = ((c and 0x07ffffffffL) shl 5) xor (value.toLong() and 0x1fL)

            for (i in 0..4) {
                if ((c0 shr i) and 1 != 0) {
                    c = c xor generators[i]
                }
            }
        }

        return c xor 1
    }

    /**
     * Decode a CashAddr address to script hash
     *
     * @param address CashAddr formatted address
     * @return Pair of (scriptHash, isP2SH) or null if invalid
     */
    fun decode(address: String): Pair<ByteArray, Boolean>? {
        try {
            // Split prefix and data
            val parts = address.lowercase().split(":")
            if (parts.size != 2) return null

            val prefix = parts[0]
            val data = parts[1]

            // Remove 'p' prefix for P2SH
            val isP2SH = data.startsWith("p")
            val payload = if (isP2SH) data.drop(1) else data

            // Decode base32
            val decoded = payload.map { char ->
                val index = CHARSET.indexOf(char)
                if (index < 0) return null
                index.toByte()
            }.toByteArray()

            // Split payload and checksum (last 8 bytes)
            if (decoded.size < 8) return null
            val dataBytes = decoded.copyOfRange(0, decoded.size - 8)
            val checksum = decoded.copyOfRange(decoded.size - 8, decoded.size)

            // Verify checksum
            val expectedChecksum = createChecksum(prefix, dataBytes)
            if (!checksum.contentEquals(expectedChecksum)) {
                Log.w(TAG, "Checksum verification failed")
                return null
            }

            // Convert from 5-bit to 8-bit
            val bytes = convertBits(dataBytes, 5, 8, false)

            // Extract version byte and script hash
            if (bytes.isEmpty()) return null
            val versionByte = bytes[0].toInt() and 0xFF
            val scriptHash = bytes.copyOfRange(1, bytes.size)

            // Check version byte matches expected P2SH
            val type = (versionByte shr 4) and 0x0F
            val expectedP2SH = type == 1

            if (isP2SH != expectedP2SH) {
                Log.w(TAG, "Address type mismatch")
                return null
            }

            return Pair(scriptHash, isP2SH)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode address", e)
            return null
        }
    }
}
